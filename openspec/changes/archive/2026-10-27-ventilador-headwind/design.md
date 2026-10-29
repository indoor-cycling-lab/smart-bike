# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs (`fan-control` y los deltas de `button-mapping`, `settings-screen`, `device-connection`, `ride-screen` y `diagnostics`). Estado actual relevante:

- Los clientes BLE (`NeoClient`, `SquareClient`) heredan de `ClienteBle` (Nordic Android-BLE-Library): conectar, reconexión en segundo plano sin límite, estado `EstadoEnlace` y `Notificacion`. `Sesion` los orquesta con `EstadoDispositivo` y los recordados en `EstadoGuardado` (`rodillo`, `mando`).
- `BleScanner.buscar(prefijos)` ya busca por prefijo de nombre, sin `ScanFilter`, y registra cada anuncio. El ventilador no anuncia su servicio, solo su nombre `HEADWIND XXXX`, así que encaja sin cambiar el escáner.
- Las acciones de los botones son `Accion` (`:nucleo`, `Asignacion.kt`), con `ASIGNABLES` y el orden de Botones; `Sesion` ejecuta las que no mueven la marcha (`siguienteModelo`) con su aviso en el pie, y `TextosAccion` les da el texto.
- La barra de Rodaje admite más enlaces sumando uno a `RodajeUiState.enlaces`; el icono `ic_mode_fan` ya está generado desde `icons.svg` (`planificacion.md`, 10f).
- Ajustes ya tiene listas de opciones con el elegido marcado (Tema, Orientación) y el componente `Stepper` (peso, pendiente, dificultad), y aplica los ajustes al momento.
- El protocolo del Headwind **no es oficial**. Lo describe la ingeniería inversa de la app de Wahoo que publican otros proyectos (notas del `docs/referencias/` de trabajo): se reimplementa a partir de esa descripción, sin copiar su código, y queda **a validar** con el ventilador real. Está verificado por terceros en un solo ventilador (hardware rev 3, firmware 2.0.43).

Lo que hace falta del protocolo:

| Elemento | Valor |
|---|---|
| Nombre anunciado | `HEADWIND XXXX`; no anuncia el servicio |
| Servicio / control point | `a026ee0c-0a7d-4ab3-97fa-f1500f9feb8b` / `a026e038-0a7d-4ab3-97fa-f1500f9feb8b`: escritura **sin respuesta**, notificación y lectura |
| Órdenes (escritura) | `01` leer velocidad · `02 NN` velocidad 0–100 (solo manual) · `03` leer modo · `04 MM` fijar modo (1 apagado, 2 pulso, 4 manual, 5 standby, 3/6–9 otros por sensor) · `05` leer configuración · `06 <config>` escribir configuración |
| Configuración | Cuatro umbrales de pulso («desde») (1 byte cada uno, en bpm), seguidos de otros campos (rango de velocidad, temperatura…); en el ventilador probado son 8 bytes tras el `ST` (la fuente decía 9 o 18, a validar). No hay orden para cambiar un solo campo |
| Notificaciones | `FD 01 SS MM` (velocidad y modo, cada ~1 s y tras cada cambio); `FE xx ST …` respuesta a la orden `xx`, con `ST = 01` si fue bien |
| Comportamiento visto | `04 04` reanuda la última velocidad manual |

### Resultados de la prueba (27-10-2026, OnePlus 8 con un Headwind real)

Visto en el registro de la app, con la app de Wahoo cerrada: el ventilador se anuncia como `HEADWIND 1234`, ofrece el servicio y el control point de la tabla y notifica al suscribirse. Tras `01`, `03` y `05` respondió `FE 01 01 00`, `FD 01 00 02`, `FE 03 01 02` y `FE 05 01 5A 85 98 A0 BB 08 A8 2B`.
- Coincide: nombre, servicio, control point, `FD 01 SS MM`, `FE xx ST …` y el modo `02` (automático, parado con el pulso por debajo del primer umbral).
- **Difiere:** la configuración tiene **8 bytes** tras el `ST` (los «9 o 18» de la fuente incluyen, probablemente, el `ST`). Los cuatro primeros, 90, 133, 152 y 160, son crecientes, como umbrales «desde». Mínimo aceptado: 8 bytes.
- **Validado desde Ajustes** (mismo día): `04 04` → `FE 04 01 04` y el ventilador pasa a manual **reanudando la última velocidad manual** (25 %); `02 23` y `02 2D` → `FE 02 01 xx` y `FD` con la velocidad nueva (pasos de 10 desde 25); `06` con la configuración de 8 bytes cambiando solo el primer umbral (90 → 91) → `FE 06 01` y la relectura trae solo ese byte distinto; vuelto a 90, idéntica a la inicial. Al abrir la app se reconectó solo.
- Botones del Square: las tres acciones funcionan bien con el ventilador real (comprobado por el usuario, sin detalle del registro).
- Resto de 5.1, comprobado por el usuario (27-10-2026): el modo automático sigue a la banda HW9 con la app abierta y conectada y cambia de velocidad al cruzar un umbral; 20 min rodando con Zwift y los botones del ventilador sin cortes de potencia, cadencia ni marchas y con la latencia de las palancas como antes (NF1). Sin detalle de registro.
- (Antes pendiente de 5.1): el modo automático siguiendo a la banda HW9 con la app conectada, 20 min rodando con Zwift y la latencia (NF1), y comprobar que el 0 % en manual deja el ventilador parado (el usuario lo da por cierto).

## Goals / Non-Goals

**Goals:**
- Una porción vertical probable con el equipo real: Square → móvil → ventilador, con un icono de estado en Rodaje.
- Que el ventilador no afecte a nada de lo que ya funciona: ni a la latencia de las palancas (NF1), ni al Neo, ni a Zwift, ni a quien no tiene ventilador.
- Que lo que la app muestra del ventilador sea siempre lo que él dice, no lo que la app cree haberle mandado.

**Non-Goals:**
- Un modelo del ventilador con memoria propia: la app no persiste modo, velocidad ni umbrales; los guarda el ventilador.
- Cualquier lógica que decida la velocidad en automático.

## Decisions

### 1. Un cliente propio sobre `ClienteBle`, con la normal de conexión

`VentiladorClient` hereda de `ClienteBle` como `SquareClient`: exige el servicio `a026ee0c-…` con el control point, se suscribe a sus notificaciones y escribe **sin respuesta** (el control point no admite escritura con respuesta). Al conectar envía `01`, `03` y `05` para partir del estado real. Prioridad de conexión normal: el ventilador habla a ~1 Hz y las órdenes son raras; no hace falta la prioridad alta que el Square sostiene por NF1. **Alternativas:** una biblioteca de Headwind de otro proyecto: dependencias y licencias sin comprobar para unos pocos bytes; hablar con el ventilador por ANT+: el móvil no tiene radio ANT+.

El cliente trabaja en su propio ámbito y nunca bloquea el hilo de control (`arquitectura.md` § 6.1): una orden al ventilador es un encargo, no una espera. Sus escrituras van por la cola de Nordic de su propia conexión, sin tocar la del Neo ni la del Square.

### 2. Codec puro en `:nucleo`, que conserva lo que no entiende

`VentiladorCodec` (puro, con tests de vectores): codifica `velocidad(n)`, `modo(m)`, `leerConfiguracion()` y `escribirConfiguracion(config)`; decodifica `FD 01` y las `FE` en un resultado tipado y devuelve `null` si el paquete es corto o desconocido. `ConfiguracionVentilador` guarda los bytes de la configuración **tal cual llegaron**, con los cuatro umbrales como vista sobre los primeros cuatro, y `conUmbral(zona, bpm)` devuelve una copia con ese solo byte cambiado: el resto (rango de velocidad, campos de temperatura y los de un firmware que la app no conoce) vuelve idéntico. Una configuración de menos de 9 bytes se rechaza y no se escribe. **Alternativa:** modelar todos los campos: más código para campos que la app no usa y riesgo de estropearlos en un firmware distinto.

Cada umbral es el pulso **desde** el que el ventilador entra en esa zona; por debajo del primero está parado (indicado por el usuario; **a validar** con el ventilador real en la prueba 5.1, también el orden y el significado de cada byte). Límites de los umbrales: entre 60 y 220 bpm y siempre estrictamente crecientes (provisional; se ajusta con el ventilador). Es el mismo estilo que `PulsoVigente` y `FiltroBateria`: reglas puras, sin Bluetooth, que se prueban solas.

### 3. El estado sale del ventilador; solo la velocidad tiene un objetivo local

`EstadoVentilador(modo, velocidad, zonas)` se actualiza con `FD 01` y las `FE` con `ST = 01`. `ModoVentilador` es `Manual` (4), `Pulso` (2) y `Otro` (cualquier otro valor, incluidos apagado y standby). La app no guarda el estado entre sesiones.

La excepción es la velocidad al pulsar varias veces seguidas: la notificación tarda hasta ~1 s y partir de ella perdería pasos. `Sesion` guarda un `objetivoVelocidad` cuando envía `02 NN` y lo usa como base de la orden siguiente; se descarta cuando llega una notificación con ese valor o a los 2 s, y desde entonces se parte de lo notificado. El aviso muestra el objetivo, no la notificación. **Alternativa:** partir siempre de lo notificado (simple, pierde pasos) o esperar la respuesta `FE 02` antes de aceptar otra pulsación (latencia visible en el pie).

### 4. Escribir la configuración: leer, cambiar un byte, escribir, releer

Cambiar un umbral es una secuencia: `05` → esperar `FE 05` (2 s máx.) → `06` con esa configuración cambiada en un byte → esperar `FE 06` → `05` otra vez para mostrar lo que el ventilador tiene. Se hace en serie (una a la vez, con un `Mutex`) y con rebote de 400 ms desde Ajustes, para que mantener el + mande una sola escritura con el valor final. Si el `FE 05` no llega o la escritura da `ST ≠ 01`, no se muestra el valor nuevo: Ajustes vuelve al que tiene el ventilador y Diagnóstico lo registra. **Alternativa:** usar la última configuración leída al conectar: si se cambió desde fuera, se pisaría con valores viejos.

### 5. Tres acciones nuevas, sin botón por defecto y sin autorrepetición

`Accion.VentiladorAlternar`, `VentiladorMas` y `VentiladorMenos` (`data object`, como `SiguienteModelo`), al final de `ASIGNABLES` y de la lista de Botones, fuera de las asignaciones por defecto y de `repetible`: el ventilador es lento y mantener un botón no debe inundarlo de órdenes. `Sesion` las ejecuta como `siguienteModelo`: sin cambiar la marcha, con su aviso (`TextosAccion`) y sin mandar nada al mando de Zwift; el efecto y los textos son los de las specs `fan-control` y `ride-screen`. Sin ventilador conectado no se envía ni se encola nada: una orden de ventilador vieja ejecutada al reconectar sorprendería más que una pulsación perdida.

### 6. Búsqueda por nombre aparte de «Buscar dispositivos»

`buscarVentilador()` usa el `BuscadorDispositivos` actual con el prefijo `HEADWIND` y los mismos 30 s; «Buscar dispositivos» solo lo incluye si ya hay uno recordado, para reconectarlo. Así quien no tiene ventilador nunca ve un «No encontrado» del ventilador. Es la misma estructura que usa el pulsómetro del change `pulso-zwift`, pero este change **no depende** de él: si no está archivado, se usa el escáner como está (no se necesita búsqueda por servicio). **Alternativa:** buscarlo siempre con el Neo y el Square: errores para quien no lo tiene.

### 7. Recordarlo como al Square

`EstadoGuardado.ventilador: DispositivoRecordado?`, opcional con valor por defecto `null` y sin subir `VERSION` (como `puenteZwift`). Al abrir la sesión, si hay uno recordado, se conecta en segundo plano con reconexión sin límite. `Sesion` expone `ventilador: EstadoDispositivo`, `ventiladorRecordado` y el `EstadoVentilador`. Olvidarlo desconecta, borra el recordado y no escribe nada en el ventilador; al terminar la sesión se desconecta y lo deja libre para otra app. Si `pulso-zwift` añade `pulsometro` a la misma clase, los dos campos son independientes y se combinan sin conflicto de formato.

### 8. Interfaz: Ajustes y Dispositivos, sin controles en Rodaje

- **Ajustes:** sección «Ventilador» tras «Zwift», solo con ventilador recordado. El modo es una lista de opciones con el elegido marcado (como Tema y Orientación, sin componente nuevo); la velocidad y cada umbral, un `Stepper`. Es probable que no haga falta ningún componente nuevo del sistema de diseño: se confirma en la maqueta, y si hace falta alguno se da de alta en `design-system/` antes de usarlo.
- **Dispositivos:** fila «Ventilador» con «Buscar ventilador» y «Olvidar», como el Neo y el Square.
- **Rodaje:** `Enlace.Ventilador(R.drawable.ic_mode_fan)` al final de `RodajeUiState.enlaces`, solo con ventilador recordado, y el aviso en el pie. Seis iconos de 14 dp con 10 dp entre ellos son 134 dp, y caben en 360 dp con la hora, el wifi y la batería: se comprueba en la maqueta, no solo en la app.
- **Diagnóstico:** fila del ventilador en Conexiones y `Origen.Ventilador` en el registro y el tráfico en directo.

## Risks / Trade-offs

- [El protocolo no es oficial y está probado en un solo ventilador y firmware] → Todo queda «a validar»: la primera tarea de la parte de la app es ver cómo responde el ventilador real (con nRF Connect, sin la app de Wahoo), antes de escribir el cliente. El codec se prueba con vectores y, si el ventilador difiere, se corrige en este mismo change.
- [El ventilador admite una sola conexión Bluetooth y la app de Wahoo, abierta, lo ocupa] → El usuario no usará más la app de Wahoo. Si la ocupa, la fila dice «No encontrado» o «Sin conexión» con el aviso que menciona otra app. Se comprueba con el ventilador.
- [Escribir la configuración puede estropear campos que la app no entiende, sobre todo en otro firmware] → Se devuelve cada byte no tocado tal cual (decisión 2), se rechaza una configuración corta y se relee tras escribir. Antes de dar por bueno el cambio de umbrales se compara la configuración leída antes y después byte a byte.
- [El modo automático podría dejar de seguir a la banda mientras la app está conectada] → Es lo central de la prueba con el equipo; la app no hace nada del pulso, así que se espera que no cambie, pero no está confirmado.
- [Se pierden escrituras sin respuesta] → Cada orden se confirma con la notificación del ventilador (`FD 01` o `FE`); si no llega en 2 s se reintenta una vez y, si no, se muestra el valor real del ventilador.
- [Un `HEADWIND` de un vecino aparece antes que el propio] → Como con el Neo y el Square, se conecta al primero; se recuerda y no se vuelve a buscar. Si pasa, se olvida y se busca otra vez.
- [Las órdenes al ventilador compiten con las palancas por el Bluetooth del móvil] → Son raras y de pocos bytes; se mide en la prueba de 20 min rodando que NF1 no empeora.
- [Seis iconos en la barra no caben a 360 dp] → Se comprueba en la maqueta antes de implementar (decisión 8).

## Migration Plan

Sin migración de datos: `ventilador` es un campo opcional de `estado.json`. Marcha atrás: quitar la sección de Ajustes, la fila de Dispositivos y las tres acciones; el campo `ventilador` queda ignorado y una asignación guardada con las acciones nuevas se lee como «sin acción». El ventilador no se toca al actualizar ni al volver atrás.

## Open Questions

- ¿Los 10 % de paso de la velocidad son los adecuados, y qué hace el ventilador al 0 % en manual? Resuelto: se para (indicado por el usuario). No cambia las specs: se decide al probarlo y se cambia la constante.
- ¿Los límites de los umbrales (60–220 bpm) son los que acepta el ventilador? Se ajustan con el ventilador real.
- ¿`04 04` reanuda siempre la última velocidad manual o solo después de cierto modo? Se anota al probar y, si no es siempre, el aviso de «Ventilador: manual · N %» se toma de la notificación.
