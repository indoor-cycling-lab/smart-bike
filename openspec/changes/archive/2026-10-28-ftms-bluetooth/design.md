# Design

## Context

Ver `proposal.md` (por qué y qué). Estado actual, tal como lo he leído en el código tras el change `generalizar-plataformas` (24):

- `ConexionPlataforma` es la interfaz de un modo; `ConexionFtmsWifi` (DirCon + `PuenteFtms`) es válida para cualquier plataforma y `ConexionZwiftRide` es propia de Zwift. La `Sesion` solo conoce `PuentePlataforma`.
- `PuenteFtms` ya habla en escrituras y notificaciones sobre UUID de características, sin saber del transporte (`arquitectura.md` § 4.3). Ofrece FTMS, Cycling Power, Heart Rate y Device Information.
- `TransporteDircon` es la interfaz de transporte (`publicar`, `parar`, `notificar`, `estado`). La implementan `DirconServer` (wifi) y `GattServerHost` (Bluetooth, nativo, genérico: anuncia el `Anuncio` que se le pasa con el `Advertiser`). Hoy `GattServerHost` solo lo usa el mando.
- `PuentePlataforma` supone que el enlace es el wifi: publica y retira con la dirección de wifi, pide el wifi en baja latencia y calcula `EstadoPlataforma.SinWifi`.
- `MandoZwiftBluetooth` ya resuelve las condiciones del periférico Bluetooth (ajuste, Bluetooth encendido, permisos y modo periférico) con `EstadoBluetooth` y `Advertiser.disponible()`.
- `Compatibilidad` ya liga plataforma, modos, dónde va el mando y la validación; `AjustesPlataforma` y las identidades por `ClaveIdentidad(plataforma, modo, nombre)` ya son por plataforma.
- Los modos son excluyentes (un solo modo activo): el servidor GATT del modo por Bluetooth y el del mando de wifi no se abren a la vez.

## Goals / Non-Goals

**Goals:**
- Que el modo sea una fila más de la tabla y una conexión que reutilice `PuenteFtms`, `FtmsCodec` y `GattServerHost` sin tocar sus protocolos.
- Que `PuentePlataforma` no suponga el wifi: cada modo dice qué enlace necesita y el estado de la plataforma habla de ese enlace.
- Una porción probable de principio a fin con el equipo real: Zwift ve el móvil por Bluetooth como rodillo, potencia, cadencia y mando, y se rueda con el Neo y el Square sin emparejar el Square en Zwift.

**Non-Goals:**
- Nombre distinto al del móvil, otras plataformas, cambios en `FtmsCodec` ni las marchas virtuales de Zwift.

## Decisions

### 1. Dominio: un modo más, con el mando dentro y sin nombre elegible

```
ModoConexion    FTMS_WIFI  "ftms-wifi"
                ZWIFT_RIDE "zwift-ride"
                FTMS_BLUETOOTH "ftms-bluetooth"      (nuevo)
Enlace          WIFI | BLUETOOTH                      (nuevo, en :core)
ModoPlataforma  (modo, mando, enlace, nombreElegible, recomendado, validacion)
Compatibilidad  zwift = [FTMS_WIFI(BLUETOOTH_OPCIONAL, WIFI, nombre sí, recomendado, VALIDADO),
                         ZWIFT_RIDE(DENTRO_DEL_MODO, WIFI, nombre sí, EN_PARTE),
                         FTMS_BLUETOOTH(DENTRO_DEL_MODO, BLUETOOTH, nombre no, EN_PARTE)]
```

`enlace` y `nombreElegible` van en la tabla para que la UI y el puente lo lean de ahí, no de comparaciones de modos: la pantalla no muestra el interruptor del mando con `DENTRO_DEL_MODO` (ya lo hace con `MandoEnModo`) y oculta «Nombre ante la app» (nuevo) leyendo la fila. `Configuracion` no sube de versión: el modo nuevo es un valor más del enumerado y una configuración sin él se lee igual. El nombre y el interruptor del mando elegidos se conservan en `AjustesPlataforma` y vuelven al salir del modo.

*Alternativa descartada:* decidir el enlace con `modo == FTMS_BLUETOOTH` en cada sitio. Es lo que 24 quiso evitar, y la siguiente plataforma por Bluetooth lo repetiría.

### 2. `ConexionFtmsWifi` pasa a `ConexionFtms`, con modo y transporte

`ConexionFtmsWifi` ya no contiene nada de wifi: el `PuenteFtms`, el tratamiento del comando FTMS (`0x11` terreno, `0x05` ERG), la telemetría y el pulso son los mismos. Se renombra a `ConexionFtms(modo, transporte, …)` y recibe el transporte (`DirconServer` o un `GattServerHost`) y el `modo` (`FTMS_WIFI` o `FTMS_BLUETOOTH`). Con `FTMS_BLUETOOTH`, además, sabe de las condiciones del periférico (decisión 4).

*Alternativa descartada:* una clase nueva `ConexionFtmsBluetooth` que copie el comando, la telemetría y el pulso (unas 40 líneas). Dos copias del manejo del Control Point divergerían; los tests del puente ya son los mismos para los dos transportes (`arquitectura.md` § 4.3).

### 3. Transporte: un segundo `GattServerHost` con el anuncio FTMS

`AppGraph` crea un `GattServerHost` más (`servidorFtmsBle`), con el mismo `Advertiser` y un `Anuncio` propio, en lugar del de Zwift Ride. Solo hay uno abierto cada vez: al cambiar de modo, `PuentePlataforma.usar` termina la conexión anterior (`gatt.close()`) antes de crear la nueva, y los dos servidores se crean solo con `by lazy`.

`Anuncio` pasa de un servicio a una **lista** de servicios de 16 bits, para anunciar FTMS (`0x1826`), Cycling Power (`0x1818`) y `FC82` con los datos de fabricante de Zwift en el paquete principal (unos 18 bytes, caben en los 31 con las banderas); el nombre del móvil va en la respuesta de escaneo, como con el mando. Con ese anuncio Zwift lista el móvil en las cuatro listas (decisiones 5 y 8); no se ha probado cuál es el mínimo que basta.

`GattServerHost` ignora la identidad DirCon (ver su documentación): la MAC es la del móvil. `PuenteFtms.fijarIdentidad` sigue dando la serie de Device Information.

### 4. `PuentePlataforma` deja de suponer el wifi: la conexión dice su enlace

`ConexionPlataforma` gana `val enlace: Enlace` (leído de la tabla). `PuentePlataforma`:

- `wifiCambia` solo pide el wifi en baja latencia y calcula `SinWifi` si `conexion.enlace == WIFI`. Con `BLUETOOTH` el wifi no cuenta.
- Con enlace `BLUETOOTH`, publica y retira la propia `ConexionFtms` según las condiciones del periférico (Bluetooth encendido, permisos de conectar y anunciar, modo periférico), igual que `MandoZwiftBluetooth` y con los mismos `EstadoBluetooth` y `Advertiser.disponible`. Su `estado` ya no es solo del transporte: `EstadoTransporte` gana `SinBluetooth` y `NoDisponible`.
- `EstadoPlataforma` gana `SinBluetooth` y `NoDisponible`; `transporteCambia` los traduce y el resto (telemetría a 4 Hz, `perdida()` a los 30 s) no cambia.

El primer modo sigue publicándose con el colector del wifi de la `Sesion`; el de Bluetooth, al crearse la conexión.

*Alternativa descartada:* meter las condiciones del Bluetooth en `PuentePlataforma` con ramas por modo. Las condiciones son del transporte, no del puente; el patrón de `MandoZwiftBluetooth` ya existe y está probado.

### 5. Mando dentro del mismo dispositivo

El modo lleva el mando de Zwift como un servicio más del servidor GATT del rodillo: `MandoEnModo.DENTRO_DEL_MODO`, siempre activo y sin interruptor, como Zwift Ride pero por Bluetooth. No hay una limitación física del móvil que lo impida: un `ServidorGatt` aloja FTMS, Cycling Power, pulso, información y el servicio del mando a la vez.

**Cómo.** `MandoZwiftEnServidor` envuelve a `MandoZwift` (que es independiente del transporte, `arquitectura.md` § 4.3): su `servicio` (las cinco características del mando por Bluetooth, no las tres mínimas de DirCon) se añade a `serviciosExtra` de `PuenteFtms`, y `usarServidor` le dice al mando que notifique por el servidor del rodillo. `ConexionFtms` lo publica y lo retira con el rodillo. El estado del mando sale de `MandoZwift` y de si el rodillo está publicado, como en `ConexionZwiftRide`. El anuncio lleva FTMS, Cycling Power y `FC82` con los datos de fabricante de Zwift (`0x094A`, `08 5A 01`): son unos 18 bytes, caben en el paquete principal y el nombre del móvil va en la respuesta de escaneo.

**Resultado del experimento (tarea 6.6, 28-10-2026, OnePlus 8, Zwift en PC).** Funciona: Zwift lista «OnePlus 8» también como mando, se suscribe a las características del mando, hace el handshake (`RideOn`), el mando pasa a «Conectado», responde a las páginas de información 0, 16, 1024 y 771 y los botones del Square mueven Zwift. Zwift sigue controlando el rodillo por FTMS (`Simulacion` por el punto de control), así que no hace falta el `SET` de las marchas virtuales y las marchas son las de la app.

*Alternativa descartada:* dejar el mando fuera del modo y el Square emparejado en Zwift (la primera etapa). Funcionaba, pero obliga a emparejar el Square en Zwift aparte y a que el usuario sepa qué hace cada dispositivo; con el mando dentro, la app es un solo dispositivo ante Zwift.

*Alternativa descartada:* un segundo servidor GATT para el mando, como en el modo por wifi. Android abre un servidor por app de forma fiable y no hace falta: Zwift acepta el servicio dentro del mismo.

### 6. Nombre: el del móvil

`Advertiser` anuncia el nombre Bluetooth del móvil en la respuesta de escaneo (único nombre que Android deja anunciar sin renombrar el teléfono). Con `nombreElegible = false`, `Sesion` pasa `Identidad.SMART_BIKE` a `PuentePlataforma.usar` para el modo, de modo que la clave de identidad es `zwift/ftms-bluetooth/smart-bike` y «KICKR BIKE PRO» no tiene efecto. `simularKickr` es falso.

### 7. UI: lo que cambia es el texto del estado y el modo nuevo

- **Plataformas:** tercera fila de modo; sin interruptor del mando (va dentro, como en Zwift Ride) y sin «Nombre ante la app» (lee `nombreElegible`); estado de Zwift y del mando con «Esperando», «Conectado», «Sin conexión» y «No disponible», y la ayuda para emparejar con el nombre Bluetooth del móvil (`BluetoothAdapter.name`).
- **Rodaje:** `PlataformaUi.Texto` gana `SinBluetooth` y `NoDisponible` («Sin Bluetooth para Zwift», «Zwift no disponible»), con las formas ya existentes (aro vacío con borde de alerta). `ConectadoSinMando` no aparece, porque el mando va dentro del mismo dispositivo.
- **Diagnóstico:** la fila del mando se ve («Dentro de Smart Bike · por Bluetooth»); el aviso `SinWifi` solo sale con enlace wifi; con Bluetooth sale el aviso de Bluetooth apagado que ya existe para el Neo y el Square, o uno nuevo de «móvil sin modo periférico» si no lo hay.
- Sin componentes ni tokens nuevos en `design-system/`: se reutilizan `ConnectionChip`, las filas de modo y el aviso. Se confirma al hacer la maqueta; si hace falta algo, se añade primero al sistema de diseño.

### 8. Cómo se valida con el equipo real

Todo lo de Bluetooth hacia Zwift es **a validar** (`AGENTS.md`, «Hardware y BLE»). El orden de la prueba, con el OnePlus 8, el Neo y el Square:

1. En Zwift para PC: el móvil aparece en rodillo, potencia y cadencia. Anotar qué servicios del anuncio hacen falta y qué nombre muestra.
2. Rodar una ruta con subidas y un entrenamiento ERG; medir que la pendiente sigue al terreno y que la potencia y la cadencia llegan sin cortes a 4 Hz (cola de notificaciones de `GattServerHost`, una en vuelo por conexión).
3. Latencia de la palanca con el Neo, el Square y Zwift conectados a la vez (NF1): el Neo y el Square van como centrales y Zwift como cliente del periférico, con un solo adaptador.
4. Cambiar de modo rodando en los dos sentidos y reabrir la app.
5. Zwift en Apple TV por Bluetooth (si el usuario puede), con la misma lista.
6. Anotar en `arquitectura.md` § 7.3 lo validado y lo que no, y marcar `Validacion` en la tabla.

## Risks / Trade-offs

- [Zwift no lista el móvil o lo lista solo como potencia] → Se prueba primero con el anuncio mínimo (FTMS) y se amplía a Cycling Power; si no basta, se replantea con el usuario antes de seguir, igual que se hizo con DirCon en el 6.
- [Un solo adaptador para dos centrales (Neo, Square) y un periférico] → Ya ocurre con el mando; se mide la latencia de la palanca con el rodillo Bluetooth activo (paso 3). Si sube, el modo se deja con la advertencia y el resultado en la tabla.
- [Cambiar de modo deja el servidor GATT anterior sin cerrar] → `terminar()` cierra el servidor antes de crear el nuevo y hay un test del orden; con wifi, lo probado en el 24.
- [Zwift guarda el emparejamiento por dirección y el móvil rota su MAC] → **Comprobado (28-10-2026, OnePlus 8, Zwift en PC): ocurre.** Tras cerrar la app con «Terminar» y volver a abrirla, Zwift no reconoce el dispositivo y hay que quitar el anterior y buscarlo de nuevo; se encuentra por su nombre Bluetooth y conecta. Se acepta como limitación del modo por Bluetooth y queda documentada en la spec; el modo por wifi, que Zwift sí reconoce entre sesiones, sigue siendo el recomendado. Una dirección estable (anuncio con dirección pública, Android 14 o posterior) quedaría para otro change.
- [El mando dentro del servidor se comporta distinto en Apple TV u otras versiones de Zwift] → Solo está comprobado con Zwift en PC; queda «a validar» en la tabla y la prueba de Apple TV (6.5) sigue siendo opcional.
- [El estado del mando y del rodillo dependen de un solo servidor GATT: apagar el Bluetooth los quita a la vez] → Es lo esperado y lo cubre la spec («Sin Bluetooth»); el mando también pasa a «Sin conexión».
- [Heart Rate siempre ofrecido] → Es lo que hace hoy por wifi sin problema; si por Bluetooth Zwift lo lista como fuente de pulso sin datos, se registra y se decide.

## Migration Plan

Sin migración de datos: `Configuracion` y `estado.json` no cambian de versión. Zwift tiene que emparejar el modo nuevo como dispositivo nuevo. Revertir es no elegir el modo; los otros modos no se tocan.

## Open Questions

- Si el usuario puede probar con Zwift en Apple TV en este change, o queda como prueba pendiente (se archivaría con la tarea sin marcar, como otros changes).
