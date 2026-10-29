# Tasks

## 1. Páginas de información en el códec

- [x] 1.1 En `MandoZwiftCodec`, responder a las páginas 1024 y 771 con las páginas observadas, y añadir la variante de la 1024 «con analógicas» (`PaginaEntradas`: `ACTUAL`, `OBSERVADA` y `ANALOGICAS`), manteniendo `ACTUAL` como lo de hoy. Tests en `MandoZwiftCodecTest`: los bytes exactos de cada variante, y que las páginas 0 y 16 no cambian en ninguna.
- [x] 1.2 En `MandoZwiftCodec`, el aviso de saludo `0x2A` (`2a 08 03 12 0d 22 0b "RIDE_ON(0)" 00`) y la respuesta `RideOn 02 00` ya existente. Tests con los bytes de la captura.

## 2. Información del dispositivo

- [x] 2.1 En `ZwiftBridge`, ofrecer `2A25` (serie), `2A26` (firmware = versión de la app) y `2A27` (hardware, «1.0»), con la serie leída de la identidad vigente en el momento de la lectura. Que `ConexionZwiftRide` y `ConexionZwiftFtms` la fijen al publicar. Tests: lectura de las tres, la serie cambia con la identidad (con y sin «Simular un KICKR BIKE») y no depende de que el Neo esté conectado.
- [x] 2.2 Anotar en el registro de sesión la lectura de las tres características con su valor, y comprobar que el DirCon las contesta sin error. Test en `DirconServerTest`.

## 3. Variantes de prueba solo en depuración

- [x] 3.1 Que `MandoZwift` reciba por constructor la variante del saludo, la de la información del dispositivo y la de la página 1024, con la de hoy por defecto, y las anote en el registro al arrancar. Tests en `MandoZwiftTest`: sin variante responde como hoy, y con cada variante la respuesta cambia como se espera.
- [x] 3.2 Leer las variantes de los extras del Intent solo con `BuildConfig.DEBUG` (`MainActivity`, `AppGraph`), con el mismo patrón que `EXTRA_SONDA_NOMBRE_FALSO`, y dejarlas fuera de la compilación normal. Test: sin extras, o con extras no válidos, responde como hoy.
- [x] 3.3 Documentar en `docs/desarrollo.md` el comando de `adb` para elegir cada variante (con `desplegar-movil`).

## 4. Prueba con el equipo real

- [x] 4.1 Con la skill `desplegar-movil`, instalar la compilación de depuración y comprobar con Zwift, el Neo y el Square (el usuario pedalea, pulsa y empareja) que, **sin variantes**, nada empeora: botones, freno, giro y 10 minutos sin pulsar. Anotar el resultado en `design.md`.
- [x] 4.2 Probar la página 1024 `OBSERVADA` y, si el freno o el giro dejan de funcionar, `ANALOGICAS`. Anotar cuál conserva botones, freno y giro.
- [x] 4.3 Probar la información de Click v2 y el saludo `RideOn 02 00`, uno por uno y combinados. Anotar si Zwift hace algo distinto (emparejado, nombre, menús, marcha de su HUD) o si no cambia nada.
- [x] 4.4 Decidir con los resultados qué variantes pasan a normal y retirar las demás del código, de los extras y de `docs/desarrollo.md`. Las páginas y las características de la información del dispositivo se quedan si no empeoran nada. Anotar la decisión en `design.md`.
- [x] 4.5 Validación de la prueba por el usuario: dar al usuario el resultado de la comparativa y marcar esta tarea solo con su aprobación explícita.

## 5. Documentación

- [x] 5.1 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md` (§ 7.4: protocolo del mando por DirCon y lo validado), `docs/planificacion.md` (fila del change y la nota del 13c) y `docs/desarrollo.md`, y actualizar lo que el change haya cambiado o decidido. Lo validado deja de estar «a validar».
