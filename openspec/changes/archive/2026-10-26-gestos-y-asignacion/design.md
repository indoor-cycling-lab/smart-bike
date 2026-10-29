## Context

Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

Estado de partida:

- `ReconocedorGestos` (`:nucleo/dominio/Gestos.kt`) solo produce `Gesto.Press(boton, modificadores, tMs)` en cada `Pulsado`, sin temporizadores. `Asignacion` es un `object` con la tabla fija del change 4 (palancas, Campagnolo + palanca del mismo lado, □ → `MarchaReferencia`).
- `Sesion.gesto()` aplica la acción con `Cambio` o, en ERG de Zwift, con `AjusteErg`, y vibra con `Vibrador.corta()` o `doble()`. Todo en el hilo de control, sin `suspend` entre la notificación y `NeoClient.enviar` (`arquitectura.md` § 5.1, § 6.1).
- `Configuracion` es un `data class` con los valores por defecto, sin guardar: la `Sesion` la recibe en el constructor. `Almacen` solo guarda `estado.json`. `Cambio` recibe la referencia fija al crearse.
- Rodaje y Diagnóstico son las únicas pantallas; no hay barra inferior. `SquareKey`, `SettingsList`, `Stepper` y `Notice` ya tienen composable; `ButtonMappingRow` y `CapturePanel` solo existen en `design-system/`.
- Límite conocido (`arquitectura.md` § 6.3): el Square alterna intervalos de conexión de 12,5 y 187,5 ms, así que la hora de llegada de una pulsación o una suelta puede retrasarse hasta ~190 ms, y no siempre lo mismo para la pulsación que para la suelta.
- Pendiente del change 3: grabar la traza de combinaciones (Campagnolo + palanca, freno + palanca). Visto en el 5: apagar el Square (los dos Campagnolo con I1 y D1) se reconoce hoy como «−1» y «+3».

## Goals / Non-Goals

**Goals:**

- Reconocedor de gestos puro y probado con reloj virtual, que solo hace esperar a lo ambiguo (F3.4) y deja las palancas igual de rápidas que hoy.
- `Asignacion` como dato serializable, con la asignación por defecto como valor y la forma que ampliarán los changes 9, 13 y 14 añadiendo acciones, sin rehacerla.
- Primera configuración guardada (`configuracion.json`), con versión, que el change 10 ampliará y exportará.

**Non-Goals:**

- Reenviar a Zwift pulsar y soltar de un botón (`Hold`, mando emulado del change 13).
- Elegir el número del salto (±3 fijo) ni acciones de otros grupos (rodillo, Zwift, app).
- Cambiar la vibración en ajustes (F12.7, change 10): sigue el `vibracion = true` por defecto.

## Decisions

### 1. Gestos: entrada + tipo, sin `Chord` ni `Hold` como gestos

Un gesto es `Gesto(entrada: Entrada, tipo: TipoGesto, tMs)`, con `Entrada(boton, modificador: SquareButton?)` y `TipoGesto` ∈ {`Pulsar`, `Corta`, `Larga`, `Doble`, `Repetir`}. La combinación con un Campagnolo es parte de la entrada, no un gesto aparte: así «Campag. D + △ corta» y «△ corta» son la misma regla con distinta entrada, y la pantalla de asignar elige entrada y gesto por separado, como en `maquetas/asignar.html`.

- La entrada se fija al pulsar: modificador = el único Campagnolo que ya estaba pulsado. Con los dos pulsados no hay entrada válida y el botón no produce gestos (spec `button-gestures`, «Los dos Campagnolo»).
- Un Campagnolo solo produce gestos con entrada `(Campagnolo, null)` y solo si mientras se mantenía no se pulsó otro botón; nunca `Pulsar`, porque se dispararía cada vez que se usa como modificador.
- **Alternativa: los tipos de `arquitectura.md` § 4.3 (`Press`, `Tap`, `DoubleTap`, `LongPress`, `Repeat`, `Hold`, `Chord`).** `Chord` duplica lo que ya dice la entrada, y `Hold` solo tiene sentido con el mando emulado. Se renombran a los términos de la UI y se actualiza § 4.3; el change 13 añadirá el reenvío de pulsar y soltar.

### 2. El reconocedor consulta qué gestos tiene asignados cada entrada

Para no hacer esperar a lo que no es ambiguo, `ReconocedorGestos` recibe una vista de la asignación: qué tipos tiene cada entrada y si su acción de `Pulsar` es repetible. Con eso:

| Asignado a la entrada | Al pulsar | Al mantener | Al soltar |
|---|---|---|---|
| `Pulsar` | `Pulsar` al instante | `Repetir` tras el retardo y a su ritmo, si la acción es repetible y la autorrepetición está activada | Fin de la repetición |
| `Corta` sin `Doble` | — | `Larga` al cumplir el umbral, si está asignada | `Corta` si no hubo `Larga` |
| `Corta` y `Doble` | Si es la segunda dentro de la ventana: `Doble` | `Larga` al umbral, si está asignada | `Corta` al vencer la ventana, si no llega otra pulsación |
| Nada | — | — | — |

Los gestos sin asignación también se reconocen para Diagnóstico («Y · pulsar · sin acción»): una entrada sin reglas produce `Pulsar` al instante, como hoy.

- **Alternativa: reconocer siempre todos los gestos y que la asignación filtre.** Obliga a esperar la ventana de doble pulsación en cualquier `Corta` y a retrasar `Pulsar` hasta saber si es larga, justo lo que F3.4 prohíbe.

### 3. Temporizadores en el hilo de control con reloj explícito

`ReconocedorGestos` sigue siendo puro: `procesar(evento): List<Gesto>`, `vencer(ahoraMs): List<Gesto>` y `proximoVencimiento(): Long?`. No tiene reloj; la hora llega con cada evento (`tMs` de la notificación, monotónica) y con cada `vencer`. La `Sesion` mantiene un único `Job` en el hilo de control que espera hasta `proximoVencimiento()` y llama a `vencer(relojMonotonico())`; tras cada `procesar` o `vencer` lo reprograma. Los gestos que salen de `vencer` siguen el mismo camino que los de una notificación, sin `suspend` hasta `NeoClient.enviar`.

- `EventosBoton.perdida()` ya suelta los botones al perder el Square; el reconocedor trata esos `Soltado` como cualquier otro y la repetición se detiene.
- Los tiempos se miden con la hora de llegada de las notificaciones, la única que hay: el retraso variable del Square (hasta ~190 ms) se traslada a la duración medida. Ver riesgos.
- **Alternativa: un temporizador por botón.** Más `Job`s que cancelar y el mismo resultado; con uno basta porque solo importa el más próximo.

### 4. `Asignacion` como dato serializable

```kotlin
@Serializable data class Regla(val entrada: Entrada, val gesto: TipoGesto, val accion: Accion)
@Serializable data class Asignacion(val reglas: List<Regla>, val invertirPalancas: Boolean = false)
```

- `Asignacion.POR_DEFECTO` reproduce la spec `button-mapping` («Asignación por defecto»). `accion(gesto)` busca la regla de la entrada y el tipo; con `invertirPalancas`, primero refleja la entrada si su botón es una palanca (D↔I con el mismo número y el Campagnolo de su lado).
- Invariantes que garantiza `asignar(entrada, gesto, accion)`: cada (entrada, gesto) tiene como mucho una acción, y `Pulsar` excluye `Corta`, `Larga` y `Doble` en la misma entrada. Devuelve la asignación nueva y las reglas que se pierden, que la pantalla enseña antes de guardar.
- `Accion` pasa a ser `@Serializable` con `@SerialName` estables («marcha-mas», «fijar-referencia»…). Se añade `FijarReferencia`. Es repetible si es `MarchaMas` o `MarchaMenos`.
- Una regla con una acción desconocida al leer (de una versión futura) se descarta sin invalidar el resto.
- **Alternativa: mapa por botón con un objeto por gesto.** Hace más difícil representar combinaciones y varias entradas por acción, que la pantalla de Botones agrupa por acción.

### 5. `configuracion.json` en `Almacen`, observado por la `Sesion`

- `Configuracion` pasa a ser `@Serializable`, con `version = 1` y una sección `botones` (asignación, `TiemposGestos` y `autorrepeticion`). `marchaReferencia` también se guarda, porque □ larga la cambia. El resto de campos se serializan con sus valores por defecto, sin UI para cambiarlos hasta los changes 8 y 10.
- `Almacen` añade un segundo DataStore, `configuracion.json`, con la misma pauta que `estado.json`: documento ilegible o inválido → valores por defecto (spec `button-mapping`, «Configuración ilegible»). Expone `configuracion: Flow<Configuracion>` y `actualizarConfiguracion(cambio)`.
- La `Sesion` recibe un `StateFlow<Configuracion>` en lugar del valor fijo y, en el hilo de control, al cambiar: sustituye la asignación y los tiempos del reconocedor (sin perder los botones pulsados) y la referencia de `Cambio`. Las pantallas de Botones escriben en `Almacen`; la `Sesion` escribe la referencia al fijarla. DataStore serializa las escrituras, así que no hay carreras.
- `Cambio` recibe la referencia en cada `aplicar` en lugar de fijarla al crearse.
- **Alternativa: guardar la referencia en `estado.json`.** Es un ajuste (F12.3), no estado de la sesión, y se exportará con la configuración (F12.6).

### 6. Apagar el Square: deshacer la ráfaga

F3.4 impide esperar en las palancas, así que no se puede evitar que I1 o D1 cambien la marcha antes de saber que vienen los dos Campagnolo. En su lugar, la `Sesion` guarda la marcha (y el ajuste de ERG) al pulsarse el primero de los cuatro botones de la combinación cuando ninguno estaba pulsado; si llegan a estar los cuatro pulsados a la vez, vuelve a esa marcha, la envía al Neo y no ejecuta más acciones hasta que se suelten todos (o se pierda el Square). Si se sueltan todos sin completar la combinación, se olvida la marcha guardada.

- Se valida primero con la traza de apagado (tareas): que los cuatro llegan pulsados a la vez antes de que el Square se desconecte. Si la traza muestra que no llega la última pulsación, se para y se actualiza este diseño (por ejemplo, deshacer al perder la conexión si hubo una ráfaga de esos botones en el último segundo).
- Se acepta que un salto legítimo con Campagnolo dentro de la misma ráfaga también se deshaga: solo pasa si se acaban pulsando los cuatro.
- **Validado con la traza** `square-combinaciones.txt` (26-10-2026): el usuario pulsa Campag. D y, ~360 ms después, I1, Campag. I y D1 llegan en la misma notificación; los cuatro quedan pulsados unos 4,6 s antes de que el Square corte la conexión, así que la app tiene tiempo de sobra para deshacer. Dentro de esa notificación I1 se procesa antes que Campag. I (orden de bit) y hace Campag. D + I1 → −1, que se deshace. En la misma prueba, el Square se volvió a conectar tras el primer apagado (lo apagó dos veces).
- Otras observaciones de esa traza: con los dos Campagnolo, la palanca no produce gestos; en la △ doble rápida, la suelta y la segunda pulsación llegan en la misma notificación (se reconoce como doble); una palanca mantenida junto al freno 1,5 s repite tres veces, como debe.

### 7. Captura: entradas en crudo, no gestos

Mientras Asignar acción está abierta, la `Sesion` está en modo captura: los eventos de botón no pasan por el reconocedor ni ejecutan acciones; se convierten en una `Entrada` (botón con el Campagnolo pulsado, o un Campagnolo soltado sin otro botón) que se publica en `Sesion.captura: StateFlow<EstadoCaptura>` (sin Square, esperando, capturada). El gesto lo elige el usuario en la pantalla. La captura empieza al abrir la pantalla y termina al cerrarla (el `ViewModel` la cancela en `onCleared`), y si se pierde el Square vuelve a «sin Square». Se actualiza `arquitectura.md` § 9.2, que decía «pide a la `Sesion` el siguiente gesto».

### 8. Repetición y topes en la `Sesion`

Cada `Repetir` ejecuta la acción como un `Pulsar` (en ERG de Zwift, `AjusteErg` lo convierte en ±10 W). Si el resultado es un tope, la `Sesion` avisa una vez y pide al reconocedor que pare la repetición de esa entrada hasta soltarla, para no vibrar en cada ciclo.

### 9. Vibración por gesto

`Vibrador` añade `larga()` (80 ms) y `triple()` (3 × 20 ms con 60 ms de pausa). La `Sesion` elige: tope → `doble()`; acción ejecutada con `Larga` → `larga()`, con `Doble` → `triple()`, con el resto → `corta()`; sin acción, nada. Los patrones exactos se ajustan con el usuario en el móvil si no se distinguen.

### 10. Pantallas y navegación

- `ui/botones/`: `Botones` y `AsignarAccion`, con `BotonesViewModel` y `AsignarAccionViewModel` sobre `Almacen.configuracion` y `Sesion.captura`. Composables nuevos `ButtonMappingRow` y `CapturePanel` en `ui.componentes`, reproduciendo `design-system/`.
- Rutas `botones` y `asignar/{accion}` en el `NavHost` de `MainActivity`; Diagnóstico enlaza a `botones` desde la sección de botones del Square. Botones lleva botón de volver y no tiene barra inferior hasta el change 10 (estado «desde Diagnóstico» de la maqueta).
- Los textos de las acciones salen de `TextosAccion`, que ya da los del aviso; se añade «Fijar la marcha de referencia» y el aviso «Referencia en la N».

## Risks / Trade-offs

- [El retraso variable del Square (12,5 o 187,5 ms) hace que una suelta llegue tarde y la repetición meta una marcha de más, o que una corta cerca del umbral se lea como larga] → Valores por defecto holgados (larga 800 ms, retardo 800 ms, ritmo 250 ms), autorrepetición desactivable y prueba con el Square real contando marchas al mantener; si se cuelan, se suben los valores por defecto o se desactiva por defecto con el usuario.
- [Doble pulsación con ventana de 300 ms y llegadas retrasadas hasta ~190 ms] → La asignación por defecto no usa `Doble`; se prueba con △ asignada y, si falla, se sube la ventana por defecto.
- [Con el Square emparejado en Zwift, un botón reasignado sigue llegando a Zwift] → Aviso en Asignar acción (spec `button-mapping`, «Aviso de botones que usa Zwift»); se resuelve con el mando emulado (13).
- [Deshacer la ráfaga de apagado depende de cómo envía el Square sus últimas notificaciones] → Traza antes de implementar y punto de parada si no encaja (decisión 6).
- [Cambiar `Configuracion` de valor fijo a flujo toca la `Sesion` entera] → Los tests de `SesionTest` pasan a un `MutableStateFlow` con los valores por defecto; el comportamiento sin cambios de configuración es el mismo.
- [□ corta pasa a ejecutarse al soltar, más tarde que hoy] → Aceptado en el change 4 (decisión 4): □ tiene gestos ambiguos desde que se le asigna la larga.

**Duración medida de una pulsación normal.** En las trazas del Square real de los changes 3 y 4, la suelta de una palanca pulsada con normalidad llega hasta 562 ms después de la pulsación en un rodaje (`rodaje-marchas.txt`) y hasta 645 ms pulsando con calma (`square-20-botones.txt`): con el intervalo de 187,5 ms, las duraciones medidas van en saltos de ~187 ms (0, ~185, ~375, ~560, ~645). Con 500 ms de retardo, la repetición metería una marcha de más en esas pulsaciones, y con 500 ms de umbral un □ pausado se leería como larga. Por eso el retardo de repetición y el umbral de larga son de 800 ms por defecto (decidido con el usuario); se confirman con el Square en la validación.

**Validación con el Square real (26-10-2026, sin Neo conectado).**
- Campag. D + △ corta asignada a «−3 marchas» desde Asignar acción, guardada, app cerrada a la fuerza y abierta: baja 3 al soltar △ (dos veces, 24 → 21 → 18).
- □ larga fija la referencia (18) con su aviso; □ corta vuelve a ella desde 19, 20, 21 y 24.
- D1 mantenida ~2 s: pulsar y cinco repeticiones a 800 ms y cada 250 ms; no se cuela ninguna al soltar.
- D1 pulsada cinco veces con normalidad: sube exactamente cinco, sin repeticiones. Con 800 ms de retardo no se cuela ninguna.
- Pendiente con el equipo completo: latencia de la palanca con el Neo (NF1), invertir palancas y restaurar en el móvil, vibración con la pantalla apagada, apagado del Square con el Neo recibiendo la resistencia y Zwift.

## Migration Plan

- Sin `configuracion.json`, la app arranca con los valores por defecto (asignación del anexo A, referencia en la 12), que reproducen el comportamiento actual salvo la repetición y □ larga. No hay datos que migrar.
- Vuelta atrás: reinstalar la versión anterior ignora `configuracion.json`.

## Open Questions

- Si los patrones de vibración de la decisión 9 se distinguen sin mirar: se ajustan con el usuario en el móvil, sin cambiar las specs.
