# Proposal

## Why

La app nació para Zwift y su código, sus textos y sus specs lo llevan por todas partes: unas 520 menciones, un modo de conexión que se llama `ModoConexionZwift`, una conexión que mezcla el rodillo genérico con el mando propio de Zwift y un `Sesion.kt` de 1.800 líneas con el puente dentro. Con el change 11 (`ftms-bluetooth`) y las plataformas que vengan después (MyWhoosh, Rouvy…), cada novedad tendría que abrirse paso entre esos nombres y esa mezcla. La app todavía no la usa nadie más que el autor, así que este es el momento barato de dejarlo bien, sin cargar con migraciones.

## What Changes

Refactor interno completo; el usuario no ve ninguna pantalla nueva ni ningún cambio de comportamiento con Zwift.

- **Dominio.** `Plataforma` crece con lo que el código ya admite (hoy solo Zwift). El modo de conexión pasa a describir solo el transporte y el perfil (`ModoConexion`: FTMS por wifi y Zwift Ride; FTMS por Bluetooth lo añade el change 11), sin «mando» en el nombre. Dónde va el mando (ninguno, Bluetooth opcional, dentro del modo) lo dice la tabla de compatibilidad de cada plataforma. `Configuracion` guarda los ajustes de cada plataforma (modo, mando, nombre) y recuerda los de cada una al cambiar de plataforma.
- **Identidades.** Las cuatro identidades fijas de dispositivo simulado (`puenteZwift`, `puenteZwiftRide`, `puenteKickr`, `puenteKickrRide`) pasan a un mapa con clave (plataforma, modo, nombre). `PuenteZwift` pasa a llamarse `IdentidadDispositivo`.
- **Conexión.** `ConexionZwift` pasa a `ConexionPlataforma`, sin lo propio de Zwift. La conexión de siempre se parte en `ConexionFtmsWifi` (DirCon + FTMS, válida para cualquier plataforma) y un mando de Zwift opcional que se le enchufa; Zwift Ride sigue siendo propio de Zwift.
- **Sesión.** El puente (conexión, wifi, estado de la plataforma, telemetría, recepción de terreno y ERG, plataforma perdida) sale de `Sesion` a una clase `PuentePlataforma`.
- **Paquetes y nombres.** Lo genérico va a `plataforma/` y lo propio de Zwift a `plataforma/zwift/`. `EstadoZwift`, `ReceptorZwift`, `SimZwift`, `ErgZwift`, `Origen.Zwift`… pasan a nombres de plataforma. Los textos nombran la plataforma elegida.
- **Specs.** `zwift-bridge` se retira y su contenido pasa a `platform-bridge` (genérica) y `zwift-ride` (modo Ride); `zwift-controller` queda solo con lo propio del mando de Zwift; `platform-settings` y `ride-screen` se ajustan.
- **BREAKING.** La configuración y el `estado.json` guardados con la versión anterior no se migran: se leen con los valores por defecto y hay que emparejar de nuevo en Zwift una vez.

Fuera de alcance:

- Nuevos protocolos o plataformas (FTMS por Bluetooth, MyWhoosh, Rouvy…): son el change 11 y los siguientes.
- Que `DirconServer` atienda a varios clientes a la vez (tormenta de conexiones al cambiar de modo en caliente con Zwift abierto, anotada en `planificacion.md`).
- Marchas virtuales de Zwift (change 13c, descartado) y cualquier cambio de la UI: pantallas, textos visibles y maquetas se quedan como están, salvo que el nombre de la plataforma sale del dato en vez de estar escrito.
- Teclado HID (change 14).

## Capabilities

### New Capabilities
- `platform-bridge`: lo que la app hace ante la plataforma elegida por wifi: rodillo visible, potencia y cadencia, terreno y ERG con la marcha, pulso, plataforma desconectada, sin wifi, anuncio comprobado, no visible, nombre e identidad ante la plataforma, información del dispositivo, y cambio de modo con la sesión abierta.
- `zwift-ride`: el modo Zwift Ride, en que el mando va dentro del mismo dispositivo por wifi: terreno, potencia y cadencia, ERG, mando dentro del rodillo e información del mando.

### Modified Capabilities
- `zwift-bridge`: se retira entera; sus requisitos pasan a `platform-bridge` y `zwift-ride`.
- `zwift-controller`: salen los requisitos que no son del mando de Zwift (modo Ride, cambio de modo, identidad por modo, información del mando por wifi).
- `platform-settings`: los ajustes se guardan por plataforma y ya no hay migración; las referencias a las specs retiradas pasan a las nuevas.
- `ride-screen`: la referencia a `zwift-bridge` del estado de la plataforma pasa a `platform-bridge`.

## Impact

- **Código.** `:core` (`dominio/`: `Plataforma`, `Compatibilidad`, `Configuracion`, `ControlRodillo`, `EstadoMandoZwift`; `protocolo/`: sin cambios de contenido, solo paquete si procede) y `:app` (`zwift/` pasa a `plataforma/` y `plataforma/zwift/`, `sesion/Sesion.kt`, `AppGraph.kt`, `ui/` y recursos de texto). Tests de `:core` y de `:app` afectados: unos 50 ficheros.
- **Datos.** `Configuracion` y `EstadoGuardado` suben de versión sin migración.
- **Specs.** Dos nuevas (`platform-bridge`, `zwift-ride`), una retirada (`zwift-bridge`) y tres modificadas.
- **Documentación.** Cambian `docs/arquitectura.md` (componentes y paquetes), `docs/planificacion.md` (fila y nota del 24, y la del 11) y `docs/funcional.md` si nombra algo renombrado; `docs/desarrollo.md`, ninguno salvo que cite rutas.
- **Verificación con el equipo real.** Prueba del usuario con Zwift en el PC, en «Rodillo por wifi (FTMS)» y en Zwift Ride, tras emparejar de nuevo una vez.
