## 0. Prueba del mando por DirCon

- [x] 0.1 En `ServicioMando`, variantes de servicio (`0001-19CA` / `FC82`) y de características (completas / mínimas: `0003` escritura, `0002` notificación, `0004` notificación, sin `0005`, `0006`, Device Information ni Battery), por defecto `FC82` y mínimas (decisión 0); tests en `ServicioMandoTest` y `MandoPorDirconTest`: con `FC82` y mínimas, el descubrimiento DirCon da un único servicio `0000FC82-…` con las tres características y sus propiedades, y el handshake, la petición de información y los botones siguen respondiendo igual
- [x] 0.2 *Keepalive* por SYNC_TX o por ASYNC (reposo, o el estado actual si hay algo pulsado), por defecto ASYNC; test: cada 5 s sin cambios sale por la característica elegida
- [x] 0.3 En `PruebaMando` y la pantalla de prueba, variante del nombre del anuncio DirCon («Square Shift Mando» / «KICKR BIKE PRO» + 4 cifras fijas) y del formato de `FC82` en `ble-service-uuids` (`0xFC82` / `FC82`), sin cambiar el anuncio de la sesión; tests en `PruebaMandoTest`: nombre y TXT según la variante, y con «Mando y rodillo por wifi» y características mínimas, sin Device Information ni Battery del mando
- [x] 0.3b En `MandoZwiftCodec`, respuesta a la información con las páginas del protobuf (`PAGINA_RIDE`, `PAGINA_CLICK_V2`): `DevInfoPage` completa a la 0 (protocolo, firmware, nombre, serie, hardware, capacidades, fabricante, producto y UID), `ClientServerCfgPage` a la 16 y solo el identificador a las demás; elegible en la pantalla de prueba; tests de vectores en `MandoZwiftCodecTest` (añadida en la prueba: la página «Zwift SF2» ponía el nombre en el campo del firmware y Zwift no identificaba el mando)
- [x] 0.4 Ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest` en verde e instalar en el móvil con `desplegar-movil`
- [x] 0.5 Prueba con Zwift en PC, mando aparte: «Solo mando por wifi» con `FC82` y mínimas, con la sesión de rodaje abierta (rodillo «Square Shift» emparejado) y el Square desemparejado en Zwift; si no aparece o no reacciona, nombre «KICKR BIKE PRO …» y luego `FC82` sin `0x` (decisión 0, pasos 1 y 2)
- [x] 0.6 Prueba de un solo dispositivo: «Mando y rodillo por wifi» con la variante que haya funcionado (paso 3)
- [x] 0.7 Con la configuración que funcione: Z, freno, dirección y diez minutos sin tocar nada (paso 4)
- [x] 0.8 Anotar los resultados en «Resultados de la prueba por DirCon» de `design.md` y en `arquitectura.md` § 7.4
- [x] 0.9 Decisión del transporte con el usuario (puerta): si va por DirCon, detenerse y reescribir la propuesta, el diseño, las specs y las tareas desde la sección 1 con `/openspec-update-change` antes de seguir; si no, seguir con la sección 1 tal como está

## 1. Maqueta

- [x] 1.1 En `maquetas/botones.html`, sustituir las filas informativas de «Funciones de Zwift» por una fila por función (↑ Barra de acciones, ↓ Cambio de sentido, ← Izquierda, → Derecha, A Seleccionar, B Atrás, Y Usar powerup, Z Ride On, Girar a la izquierda ✕, Girar a la derecha ○, Freno I, Freno D), con su entrada y gesto y enlace a `asignar.html`, como las acciones de la app; quitar la fila «Cambiar marcha» (decisión 9); ayuda del grupo «Llegan a Zwift por el mando de la app»; estado nuevo `mando-desactivado` con la ayuda de que no llegan por la app; en `modificada`, una función de Zwift con «Sin botón»
- [x] 1.2 En `maquetas/asignar.html`, estado `funcion-zwift` («Ride On» con Z, Campag. D + △ capturada con «pulsar» explicado como «mientras se mantiene», aviso de entrada libre); el estado `zwift` pasa a describirse como «Botón que usa Zwift (mando desactivado)»
- [x] 1.3 En `maquetas/ajustes.html`, sección «Zwift» al final con «Mando de Zwift» activado y su ayuda (los botones del Square llegan a Zwift por Bluetooth a través de la app; el Square ya no se empareja en Zwift)
- [x] 1.4 En `maquetas/dispositivos.html`, fila «Mando de Zwift» bajo la de Zwift, con el nombre Bluetooth del móvil de ejemplo («OnePlus 8») y los estados `mando-esperando`, `mando-desactivado` y `mando-no-disponible`; en los demás estados, «Conectado» o «Sin conexión» según corresponda; quitar «El Square puede seguir emparejado en Zwift para girar» salvo con el mando desactivado
- [x] 1.5 Decidir con la maqueta la forma de «Desactivado» y «No disponible» en la fila del mando; si hace falta una variante neutra de `sq-conn`, añadirla en `design-system/components.css` con su `README.md` y `preview.html` de `ConnectionChip`/`ConnectionStatus`, sin color como único indicador, y comprobar el contraste en los dos temas (decisión 9)
- [x] 1.6 En `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html` (`rodaje.js`), estado «Ruta de Zwift · mando esperando» con el chip «Zwift» en el anillo de espera y nada más cambiado; verificar a 360 dp en vertical que la línea de chips no cambia de ancho
- [x] 1.7 En `maquetas/diagnostico.html`, fila del mando bajo la de Zwift en Conexiones: «Conectado» en los estados con Zwift conectado y «Esperando» en los que Zwift espera
- [x] 1.8 Actualizar `maquetas/index.html` con los estados nuevos, regenerar la guía (`python3 scripts/design-system/generar.py`, sin errores de contraste) y revisar los estados en los dos temas
- [x] 1.9 Validación de la maqueta por el usuario

## 2. Dominio y protocolo (`:nucleo`)

- [x] 2.1 Añadir `FuncionZwift` y `Accion.Zwift(funcion)` con nombres serializados estables, en `TODAS` al final y en `asignables` de las dos transmisiones, no repetibles (decisión 1); tests en `AsignacionTest`: serializar y leer cada función, posiciones anteriores de `TODAS` sin cambiar, `repetible` falso
- [x] 2.2 Añadir `ZWIFT_POR_DEFECTO` (doce reglas «pulsar» del anexo A.1) a `POR_DEFECTO` y `POR_DEFECTO_PLATOS` (decisión 7); tests: cada botón con su función en las dos transmisiones, Campag. I + cruceta ← y → siguen en pantalla anterior y siguiente, cruceta → sola da `Zwift(DERECHA)`
- [x] 2.3 Añadir `EstadoMandoZwift`, puro: pulsar y soltar funciones con recuento por función, `soltarTodo` y la trama de botones con `MandoZwiftCodec` (`ENUM_MANUAL`, campo 3, las dos palancas siempre presentes, el freno gana a su giro) (decisión 2); tests de vectores: cada función sola, dos frenos, giro y freno en la misma palanca, la misma función mantenida por dos entradas, soltar en distinto orden y reposo con las palancas a 0
- [x] 2.4 `Configuracion` versión 5: campo `mandoZwift = true` y migración que añade `ZWIFT_POR_DEFECTO` con `conReglasSiLibres` en cada transmisión (decisión 7); tests en `ConfiguracionTest`: documento v4 sin el campo se lee activado; A corta en «Marcha de referencia» se conserva y «Seleccionar» queda sin botón; una regla de Zwift quitada por el usuario no vuelve al releer; restaurar devuelve las funciones de Zwift; documento ilegible da los valores por defecto

## 3. Servidor GATT, anuncio y mando (`:app`)

- [x] 3.1 Crear `ble/GattServerHost` a partir de `PerifericoMando`: servidor nativo con los servicios de un `ServidorGatt` añadidos de uno en uno, lecturas, escrituras y CCCD en el hilo de control, clientes solo por uso, notificaciones en serie con `onNotificationSent`, implementando `TransporteZwift` (decisión 4); tests en la JVM de la lógica separable de Android (clientes por uso, cola de notificaciones), si se puede extraer sin dobles de `BluetoothGattServer`
- [x] 3.2 Crear `ble/Advertiser`: anuncio conectable con `FC82`, fabricante `0x094A` + `0x08` + 2 bytes y nombre del móvil en la respuesta de escaneo; comprobación de modo periférico (NF3) (decisión 4)
- [x] 3.3 Crear `zwift/MandoZwift` a partir de `ServicioMando`, con las respuestas fijas validadas (información con `PAGINA_RIDE`), botones solo tras el handshake, *keepalive* de 5 s por ASYNC, `estado: StateFlow<EstadoMando>` y todo el tráfico en el `RegistroSesion`, sin depender de `ble/`: su `ServicioGatt` se puede montar en cualquier `ServidorGatt` (decisión 5, reutilización en el 13b); `MandoZwiftTest` con un transporte de prueba: handshake y respuesta, petición de información, nada antes del handshake, cambio de estado al instante por ASYNC, *keepalive* con el estado actual, cliente perdido suelta todo y olvida el handshake
- [x] 3.4 Mover `BLUETOOTH_ADVERTISE` al manifiesto principal y pedirlo con `BLUETOOTH_SCAN` y `BLUETOOTH_CONNECT` en `AdaptadorBluetooth` (decisión 8); sin él, el mando queda «Sin conexión»
- [x] 3.5 En `AppGraph`, crear `GattServerHost`, `Advertiser` y `MandoZwift` una vez y pasarlos a la `Sesion` (decisión 6)

## 4. Sesión

- [x] 4.1 Ciclo de vida del mando en la `Sesion`: publicar con el ajuste activado, el Bluetooth encendido y el permiso; parar al apagar el Bluetooth, al desactivar el ajuste y al terminar; una sola publicación por sesión; `EstadoRodaje.mando` (decisión 6); tests de la `Sesion` con dobles: abrir con el ajuste activado publica, desactivarlo rodando para sin tocar el puente DirCon, Bluetooth apagado y encendido republica, sin modo periférico da `NoDisponible`, terminar para
- [x] 4.2 Reenvío de funciones de Zwift (decisión 3): «pulsar» pulsa y el `Soltado` del botón suelta; corta, larga y doble dan una pulsación breve de 100 ms; antes del camino de ERG, sin vibración ni aviso de acción, con el último gesto actualizado; tests: cruceta → pulsada mientras se mantiene, A soltada sin soltar →, △ larga con Z da un solo Ride On, en ERG A no ajusta la potencia, Y no vibra, Z no sustituye el aviso «+1 marcha», mando desactivado no envía nada
- [x] 4.3 Soltar todas las funciones al perder el Square, al abrir la captura, al desactivar el ajuste, al perder a Zwift y al terminar; tests: freno mantenido y Square perdido suelta el freno; captura abierta con ○ mantenido lo suelta y la captura no envía nada
- [x] 4.4 Comprobar con un test de la `Sesion` que la latencia interna de la palanca (notificación del Square → comando al Neo) no cambia con el mando conectado (NF1)

## 5. Interfaz

- [x] 5.1 Botones: grupo «Funciones de Zwift» con una fila por `FuncionZwift`, sus entradas y gestos y enlace a Asignar acción, ayuda según el ajuste, sin la fila de las palancas, como la maqueta aprobada; tests en `BotonesUiStateTest` y `BotonesTest`: filas por defecto, «Sin botón» tras quitar, ayuda con el mando desactivado, tocar «Ride On» abre Asignar acción
- [x] 5.2 Asignar acción con funciones de Zwift: «pulsar» explicado como «mientras se mantiene»; `AvisoAsignar.Zwift` solo con el ajuste desactivado; tests en `AsignarAccionUiStateTest` y `AsignarAccionTest`: con el mando activado, capturar A en «+1 marcha» solo avisa de que A deja de hacer «Seleccionar»; desactivado, avisa de que Zwift también la recibe
- [x] 5.3 Ajustes: sección «Zwift» al final con «Mando de Zwift» y su ayuda (también la de móvil sin modo periférico), `AjustesViewModel.mandoZwift(activo)`; tests en `AjustesTest`: orden de secciones, activado por defecto, al desactivar se guarda
- [x] 5.4 Dispositivos: fila del mando con estado en palabra, nombre Bluetooth del móvil y ayuda de cada estado, como la maqueta; tests en `DispositivosTest` para esperando, conectado, desactivado, no disponible y Bluetooth apagado
- [x] 5.5 Rodaje: chip «Zwift» con el estado del puente y el del mando y la descripción accesible con los dos (spec `ride-screen`); tests en `RodajeTest`: rodillo conectado y mando esperando da el anillo de espera sin cambiar de ancho, mando desactivado y rodillo conectado da punto lleno
- [x] 5.6 Diagnóstico: fila del mando en Conexiones; test en `DiagnosticoTest` con el mando conectado y desactivado

## 6. Retirada del prototipo

- [x] 6.1 Borrar `app/src/debug/kotlin/jmlopezdona/squareshift/prototipo/`, `app/src/testDebug/kotlin/jmlopezdona/squareshift/prototipo/` y la actividad del manifiesto de depuración, tras pasar lo útil de sus tests a `MandoZwiftTest` (decisión 10)
- [x] 6.2 Ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest :app:connectedDebugAndroidTest` en verde

## 7. Validación con el equipo real

- [x] 7.1 En el OnePlus 8 con el Neo y el Square y Zwift en PC, con el Square desemparejado en Zwift: emparejar rodillo por wifi y mando por Bluetooth; Dispositivos y el chip «Zwift» pasan a conectado (design.md, decisión 11, paso 1)
- [x] 7.2 Funciones desde el Square: menús, cargar una ruta, ↑ y B en rodaje, Y, Z, ✕ y ○ en la calzada, ← y → en un cruce, cada maneta y las dos a la vez (paso 2)
- [x] 7.3 Palancas sin aviso de marchas virtuales y latencia de la palanca en Diagnóstico como antes; reasignar A a «+1 marcha» y comprobar que no llega a Zwift (pasos 3 y 4)
- [x] 7.4 Apagar y encender el Square rodando: el mando no se pierde (paso 7)
- [ ] 7.4b Pendiente de probar: diez minutos sin tocar nada (*keepalive* por ASYNC) y luego A; cerrar Zwift con ○ mantenido; apagar el Square con un freno mantenido (pasos 5 y 6)
- [x] 7.5 Desactivar y activar «Mando de Zwift» rodando (paso 8)
- [x] 7.6 Anotar los resultados en `arquitectura.md` § 7.4 (lo pendiente del 12 que quede resuelto) y en este `design.md`

## 8. Documentación

- [x] 8.1 Actualizar la documentación: revisar `docs/funcional.md` (F9 cumplida y lo que no, anexo A.1 «Cómo se resuelve» y A.3, F12 con «Mando de Zwift», decisión abierta 1), `docs/arquitectura.md` (§ 4.3 `ReconocedorGestos` sin `Hold`, `EstadoMandoZwift`, `GattServerHost`, `Advertiser` y `MandoZwift`; § 4.4 y `AppGraph`; § 5.3 acciones de Zwift; § 7.4; § 10 `configuracion.json` versión 5; § 12 `BLUETOOTH_ADVERTISE`), `docs/planificacion.md` (fila y notas del 13; el 11 ya no bloquea el 13 y puede no hacerse; el aviso de giro del 10b con el mando) y `docs/desarrollo.md` (quitar la prueba del mando de depuración), y actualizar lo que haya cambiado
