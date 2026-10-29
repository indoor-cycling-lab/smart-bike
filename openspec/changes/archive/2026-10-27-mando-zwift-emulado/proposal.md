## Why

Hoy el Square tiene que seguir emparejado en Zwift para girar, y el freno, los menús, el powerup y el Ride On no llegan desde el Square: con él emparejado, además, un botón reasignado en la app también llega a Zwift (anexo A.3 de `funcional.md`). El prototipo del change 12 validó con Zwift en PC que la app puede hacerse pasar por un mando Zwift Ride por Bluetooth (menús, A, B, Z, freno y dirección; `arquitectura.md` § 7.4). Este change lleva ese mando a la sesión de rodaje para que los botones del Square lleguen a Zwift a través de la app (F9) y deje de hacer falta emparejar el Square en Zwift (F9.2). Es el change 13 de `planificacion.md`, fase 4.

## What Changes

- **Prueba previa del mando por wifi (DirCon), hecha.** Con el servicio `FC82` y una página de información completa, Zwift reconoce el mando por DirCon, pero solo como único dispositivo DirCon de la IP (no junto al rodillo de la sesión) o dentro del rodillo, y ahí activa sus propias marchas virtuales («GEAR» en el HUD) y le envía su configuración con el protocolo de Zwift. El usuario elige Bluetooth para el mando, y la alternativa queda documentada (`arquitectura.md` § 7.4). De la prueba se quedan la página de información completa y el *keepalive* por ASYNC.
- La sesión de rodaje publica el mando de Zwift por Bluetooth (servidor GATT y anuncio BLE nativos, solo con el servicio del mando), mientras el rodillo, la potencia y la cadencia siguen por wifi (DirCon). Zwift ve dos dispositivos de la app: el rodillo por wifi y el mando con el nombre Bluetooth del móvil.
- **Dependencia con el change 11 resuelta aquí**, por decisión del usuario: el 13 construye el servidor GATT nativo y el anuncio solo para el mando. El puente FTMS por Bluetooth y la elección de wifi o Bluetooth en Ajustes (change 11) quedan como estaban, previstos y sin compromiso de hacerse; si se hacen, se apoyarán en este servidor.
- Ajuste nuevo «Mando de Zwift», en una sección «Zwift» de Ajustes, **activado por defecto**. Desactivado, la app no se anuncia como mando y todo sigue como hoy (Square emparejado en Zwift).
- Funciones de Zwift como **acciones asignables** (F4.2): ←, ↑, →, ↓, A, B, Y, Z, girar a la izquierda y a la derecha, freno izquierdo y derecho. Con «pulsar», la función queda pulsada en Zwift mientras se mantiene el botón y se suelta al soltarlo, sin espera (F3.4); con corta, larga o doble, llega como una pulsación breve.
- Asignación por defecto con las funciones de Zwift del anexo A.1 en todas las transmisiones (cruceta, A, B, Y, Z, ✕ y ○ para girar, manetas para frenar). Las configuraciones guardadas las reciben una vez, solo en las entradas libres.
- Botones: el grupo «Funciones de Zwift» deja de ser informativo; cada función tiene su fila y se reasigna como las acciones de la app. Un botón reasignado deja de llegar a Zwift.
- Las palancas siguen cambiando las marchas de la app y no se reenvían a Zwift (F9.3).
- Estado del mando en Dispositivos y en Diagnóstico («Conectado», «Esperando», «Desactivado», «Sin conexión» con el Bluetooth apagado, «No disponible» sin modo periférico, NF3), con cómo emparejarlo en Zwift. El chip «Zwift» de Rodaje refleja el rodillo y el mando juntos.
- Se retira la pantalla de prueba del mando de la compilación de depuración (change 12); su lógica pasa a `main`.
- Pasa por maqueta (Botones, Asignar acción, Ajustes, Dispositivos, Rodaje y Diagnóstico) antes de implementarse en la app.

Fuera de alcance:

- El puente con Zwift por Bluetooth (rodillo, potencia y cadencia por BLE) y la elección de transporte en Ajustes (F8.5, F12.1): change 11.
- Por Bluetooth, anunciar el mando con un nombre propio («Square Shift Mando» o «Zwift SF2»): Android solo deja anunciar el nombre Bluetooth del móvil, y cambiarlo renombraría el teléfono. Por DirCon el nombre lo elige la app.
- Los botones de cambio del Ride hacia Zwift (F14) y cualquier otra función del mando que no esté en el anexo A.1.
- Por Bluetooth, averiguar cuál de los dos, los datos de fabricante del anuncio o la respuesta a la petición de información, es imprescindible para Zwift: se envían los dos, como en el prototipo.
- El mando por DirCon: documentado en `arquitectura.md` § 7.4 por si se reconsidera (por ejemplo, con el rodillo por Bluetooth del change 11).
- El aviso de giro en Rodaje: change 10b.
- Zwift en Apple TV con el mando: se anota si se prueba, sin bloquear el change.

## Capabilities

### New Capabilities

Ninguna: el comportamiento del mando ya tiene su spec, `zwift-controller`, que pasa de la pantalla de prueba a la sesión de rodaje.

### Modified Capabilities

- `zwift-controller`: el mando lo publica la sesión de rodaje con el ajuste activado y lo accionan los botones del Square; se quitan la pantalla de prueba, las configuraciones por wifi y las variantes del protocolo.
- `button-mapping`: funciones de Zwift entre las acciones asignables y en la asignación por defecto; Botones con el grupo de Zwift asignable; el aviso de botones que usa Zwift solo con el mando desactivado; migración de la asignación guardada.
- `button-gestures`: las funciones de Zwift no vibran.
- `settings-screen`: sección «Zwift» con «Mando de Zwift» en Ajustes, guardado con el resto.
- `device-connection`: fila del mando en Dispositivos.
- `ride-screen`: el chip «Zwift» refleja el rodillo y el mando; las funciones de Zwift no muestran aviso de acción.
- `diagnostics`: fila del mando en Conexiones.
- `screen-mockups`: maquetas de Botones, Asignar acción, Ajustes, Dispositivos, Rodaje y Diagnóstico con el mando.

## Impact

- `:nucleo`: `Accion.Zwift` y `FuncionZwift` en `dominio/`, asignación por defecto con las funciones de Zwift, `Configuracion` versión 5 con el ajuste del mando y la migración de la asignación. `MandoZwiftCodec` gana la respuesta con las páginas del protobuf (prueba previa).
- Prueba previa, hecha: variantes en la pantalla de prueba de depuración (servicio `FC82`, características mínimas, nombre e identidad del anuncio DirCon, *keepalive* por ASYNC) y `RespuestaInfo.PAGINA_RIDE` en `MandoZwiftCodec`; `DirconServer` con `sinPrefijoCorto`.
- `:app`: `GattServerHost` y `Advertiser` en `ble/` (a partir de `PerifericoMando`), `MandoZwift` en `zwift/` (a partir de `ServicioMando`), `Sesion` (reenvío de pulsar y soltar, ciclo de vida del mando, estado), pantallas de Botones, Asignar acción, Ajustes, Dispositivos, Rodaje y Diagnóstico. `BLUETOOTH_ADVERTISE` pasa al manifiesto principal y se pide con el resto de permisos de Bluetooth.
- Se borra `app/src/debug/kotlin/jmlopezdona/squareshift/prototipo/`, sus tests y su entrada en el manifiesto de depuración.
- `design-system/` y `maquetas/`: estados nuevos en las maquetas citadas; sin componentes nuevos previstos.
- Sin dependencias nuevas.
- Documentos de `docs/`: `funcional.md` (F9 cumplida, anexo A.1 y A.3, F12 con el ajuste), `arquitectura.md` (§ 4.3, § 4.4, § 5.3, § 7.4 con el resultado de la prueba por DirCon, § 12 y la dependencia del 11), `planificacion.md` (fila y notas del 13, dependencias del 11 y del 10b) y `desarrollo.md` (sin la pantalla de prueba del mando, si se cita).
