# Arquitectura de la aplicación

> Estado: **borrador para revisión**. Describe la arquitectura objetivo de la app Android. Las decisiones que dependen del equipo real están marcadas como **a validar** y no se dan por buenas hasta probarlas con el Neo, el Square y Zwift.

Este documento explica cómo se construye la app que describe [`funcional.md`](funcional.md). Los identificadores F1…F15 y NF1…NF7 remiten a ese documento. Las decisiones de cada cambio concreto se detallan en el `design.md` de su change de OpenSpec; este documento recoge el marco común que esos diseños respetan.

## 1. Objetivos y restricciones

| Restricción | Origen | Consecuencia en la arquitectura |
|---|---|---|
| Palanca → cambio de resistencia en menos de 100 ms | NF1, F3.4, F5 | Camino caliente corto, sin colas de UI ni esperas: un único hilo de control y escrituras BLE que se sustituyen en lugar de acumularse (§ 6) |
| Uso completo sin tocar el móvil | NF2 | Todo lo que hace la UI durante el rodaje también se puede hacer desde los botones del Square (acciones, § 5.3) |
| Android 9 o superior; modo periférico solo para Bluetooth hacia Zwift y el mando | NF3 | `minSdk 28`. El transporte principal hacia Zwift es wifi (§ 7.3), que no necesita modo periférico. Si se elige Bluetooth o el mando emulado, se comprueba `bluetoothLeAdvertiser != null` y se avisa si el móvil no sirve |
| Sesiones de 2 h o más, en segundo plano | NF4, F1.5 | Las conexiones viven en un *foreground service*, no en la Activity (§ 4.4) |
| Sin cuentas, servidores ni internet | NF5 | El permiso `INTERNET` se declara solo porque Android lo exige para cualquier socket, incluido el de la red local con Zwift; la app no abre conexiones fuera de ella. Fuentes e iconos empaquetados. Configuración y registros en local |
| Código propio | NF6 | Los protocolos se reimplementan a partir de su descripción (§ 7). No se copia código de qdomyos-zwift (GPL-3.0) ni de BikeControl (no comercial) |
| Interfaz en español e inglés | NF7 | Textos en `values/strings.xml` (español) y `values-en/strings.xml`; el idioma es `Configuracion.idioma` (`IdiomaApp`, `null` = sin decidir) y `IdiomaElegido` lo aplica en la composición con un contexto localizado, sin recrear la actividad (§ 9.1). `RideService` usa `Context.conIdioma`; los formatos reciben el `Locale` del idioma |

Principios de diseño:

1. **Dominio puro y probable.** Gestos, marchas, modelos de resistencia y codificación de protocolos son Kotlin sin dependencias de Android, probados con tests unitarios y trazas grabadas del equipo real.
2. **Un único dueño del estado.** La sesión de rodaje (`Sesion`) es la única que cambia el estado; el resto lo observa como `StateFlow`.
3. **La UI no está en el camino caliente.** Un cambio de marcha llega al Neo sin pasar por Compose; la pantalla se entera después.
4. **Porciones verticales.** Cada change de [`planificacion.md`](planificacion.md) atraviesa todas las capas y se prueba de extremo a extremo con el equipo real.

## 2. Contexto

El móvil hace de intermediario entre tres equipos. Es **central** BLE del Neo y del Square. Ante Zwift, que corre en otro dispositivo (PC, Mac o Apple TV), se presenta como un rodillo **por wifi** en la red local (DirCon, transporte principal) o, como alternativa, como **periférico** BLE (F8.5).

```
                    ┌───────────────────────── Móvil (Smart Bike) ─────────────────────────┐
 Elite Square ─────▶│ SquareClient ─▶ ReconocedorGestos ─▶ Asignacion ─▶ Sesion            │
 (botones, notify)  │                                                      │   ▲            │
                    │                                                      ▼   │ telemetría │
                    │                                    ControlRodillo ─▶ NeoClient ───────│──▶ Tacx Neo 1
                    │                                          ▲                             │◀── potencia, cadencia
                    │                                          │ terreno / ERG               │
 Zwift ── wifi ────▶│ PuenteFtms (FTMS + Cycling Power) ──────┘                             │
 (PC, Mac, ATV)  ◀──│   ◀── potencia, cadencia, velocidad                                    │
  BLE (mando)    ◀──│ MandoZwift (periférico BLE propio, § 7.4) ◀── acciones                 │
                    └────────────────────────────────────────────────────────────────────────┘
```

- **Zwift no se empareja con el Neo.** Si lo hiciera habría dos controladores del rodillo. En Zwift se elige la app como rodillo controlable, potencia y cadencia (F8.1).
- **El Square admite varias conexiones a la vez.** Hasta que exista el mando emulado (F9), sigue emparejado en Zwift para la dirección (`funcional.md`, anexo A.3). Validado en `puente-zwift-wifi`: X y Círculo giran en Zwift mientras las palancas cambian de marcha en la app.
- **El puente usa un solo transporte hacia la app de entrenamiento.** Se elige con el modo en la pestaña «Conexión» («Wifi + Bluetooth» por defecto) y el servidor del otro no se arranca. Con «Wifi + Bluetooth» el mando emulado va por Bluetooth aparte (§ 7.4), así que Zwift ve dos dispositivos; con «Solo Bluetooth» el mando va dentro del mismo servidor GATT (§ 7.3) y Zwift ve uno. Se publica lo configurado en Dispositivos (§ 7.3, «Lo publicado según lo configurado»).
- **Por wifi, el móvil y el equipo de Zwift deben estar en la misma red**, sin aislamiento de clientes en el router.
- Zwift **en el mismo móvil** queda fuera de alcance.

## 3. Pila tecnológica

| Pieza | Elección | Alternativas descartadas |
|---|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, una sola Activity | Vistas XML: más código para una UI que ya está maquetada por componentes |
| BLE central (Neo y Square) | Nordic Android-BLE-Library (BSD-3): cola de operaciones GATT, reintentos, prioridad de conexión y los fallos conocidos de Android (error 133, caché de servicios, cambios de API en Android 13) | `android.bluetooth` con cola propia: control total, pero cientos de líneas que la biblioteca ya resuelve y cuyos fallos solo aparecen en rodajes largos (NF4). La sustitución de comandos del camino caliente (§ 6.2) va en `NeoClient`, por encima de la biblioteca |
| Puente por wifi (principal) | DirCon: anuncio con `NsdManager` (mDNS) y servidor TCP con sockets de Java y coroutines, sin dependencias nuevas | jmDNS: no hace falta, `NsdManager` es nativo desde Android 4.1. Ktor o Netty: excesivos para un servidor de un solo cliente con mensajes pequeños |
| BLE periférico (puente alternativo y mando) | `android.bluetooth` nativo: `BluetoothGattServer` y `BluetoothLeAdvertiser` | `BleServerManager` de Nordic: pocas características y el mando Zwift necesita control fino del handshake y de las indicaciones |
| Concurrencia | Coroutines, `StateFlow` / `SharedFlow`, `Channel` | RxJava: dependencia extra sin ventaja aquí |
| Persistencia | DataStore con documentos JSON tipados (`kotlinx.serialization`), en dos ficheros (§ 10) | Proto DataStore: no es legible y exportar exigiría convertirlo a otro formato, con dos esquemas que mantener. Room: no hay datos relacionales. `SharedPreferences`: sin tipos ni migraciones |
| Inyección de dependencias | Manual, con un contenedor `AppGraph` creado en `Application` y fábricas de `ViewModel` con `initializer` | Hilt: añade plugin y generación de código por unos 15 objetos únicos. Koin: dependencia extra con errores en ejecución. Solo afecta a `:app`; se puede migrar con un refactor si la app crece |
| Tema de Compose | Tema propio `SqTema` con los nombres de los tokens, generados de `tokens.json` (§ 9.1) | `MaterialTheme` escrito a mano: se separa de las maquetas y los roles de Material 3 no encajan con los tokens |
| Navegación | Navigation Compose | Navegación manual con estado: aceptable, pero Navigation resuelve el botón atrás y la restauración |
| Protobuf del mando Zwift | Codificación a mano del único mensaje necesario (varint y un campo) | `protobuf-lite`: dependencia y plugin de Gradle para un mensaje de pocos bytes |
| Tests | JUnit Jupiter (JUnit 6) en la JVM, `kotlinx-coroutines-test` (reloj virtual), Compose UI Test (JUnit 4) en el móvil | Plugin `android-junit5`: solo haría falta para usar JUnit Jupiter en los tests instrumentados |

Cada dependencia nueva que no esté en esta tabla se justifica en el `design.md` del change que la introduce, con sus alternativas (`openspec/config.yaml`).

## 4. Estructura

### 4.1 Módulos de Gradle

```
:core     Kotlin/JVM puro
  dominio/      marchas, gestos, asignación, modelos de resistencia, modos de control, configuración
  protocolo/    codificación y decodificación de bytes: Square, FE-C, FTMS, Cycling Power, mando Zwift

:app      Aplicación Android
  ble/          clientes centrales (Nordic), servidor GATT, anuncio
  red/          estado del wifi y WifiLock
  plataforma/   puente con la plataforma: ConexionPlataforma, PuentePlataforma, ConexionFtms, TransporteDircon, TransporteBluetooth, DirconServer y su anuncio mDNS, PuenteFtms
  plataforma/zwift/  lo propio de Zwift: MandoZwift, MandoZwiftBluetooth, ConexionZwiftRide
  sesion/       Sesion, ConexionesSesion, VentiladorSesion, RideService
  datos/        persistencia, RegistroSesion
  ui/           pantallas, componentes y tema generado del sistema de diseño

:app ──▶ :core
```

- La frontera que importa, y la que más cuesta rehacer después, es la de lógica pura frente a Android. `:core` no depende de Android: se compila y prueba en la JVM en segundos, y el compilador impide que se cuele una API de Android (`Context`, `BluetoothGatt`, `SystemClock`) en la lógica.
- Dentro de `:core`, `protocolo` no depende de `dominio` salvo tipos compartidos (por ejemplo `SquareButton`). Dentro de `:app`, `ble` no conoce el dominio: mueve bytes y estados de conexión.
- El prototipo del mando Zwift (fase 4) vivió en `app/src/debug/` y no hizo falta extraer `ble` a un módulo propio.

### 4.2 Nombres

Según `AGENTS.md`: el dominio en español sin tildes (`marcha`, `plato`, `pinon`, `transmision`) y la infraestructura genérica en inglés (`GattServerHost`, `Advertiser`). Los composables se llaman como los componentes del sistema de diseño (`GearDisplay`, `MetricGrid`, `CassetteView`).

### 4.3 Componentes

| Componente | Módulo | Responsabilidad | Funcionalidades |
|---|---|---|---|
| `SquareDecoder` | `:core` | Notificación de `0x45` → contadores por botón y máscara de 32 bits por paridad (§ 7.1) | F2 |
| `EventosBoton` | `:core` | Contadores → `Pulsado` / `Soltado` por botón con marca de tiempo, sin perder pulsaciones entre notificaciones; libera los botones si se pierde la conexión | F2.2 |
| `ReconocedorGestos` | `:core` | Eventos → gestos: `Gesto(entrada, tipo)`, con `Entrada(boton, modificador)` (el único Campagnolo pulsado antes) y `TipoGesto` `Pulsar`, `Corta`, `Larga`, `Doble` o `Repetir`. Puro, sin reloj: `procesar`, `vencer` y `proximoVencimiento`. Consulta la asignación para esperar solo en lo ambiguo (F3.4). No tiene un gesto de mantener: el reenvío de pulsar y soltar a Zwift lo hace la `Sesion` con «pulsar» y el `Soltado` del botón (change `mando-zwift-emulado`, decisión 3) | F3 |
| `Asignacion` | `:core` | Reglas (entrada, gesto) → `Accion`, serializables, con la asignación por defecto del anexo A e «Invertir palancas»; `asignar` quita las reglas que chocan (`Pulsar` excluye `Corta`, `Larga` y `Doble`). Una combinación sin reglas hace lo del botón solo | F4 |
| `Transmision`, `Cambio` | `:core` | Definición de la transmisión: `Lineal(marchas)` o `Platos` (platos, piñones, puntos del modo automático y referencia por defecto), con los desarrollos de fábrica en `Desarrollos`. `EstadoMarcha` es `Numerada` o `PlatoPinon` (en dientes, con su relación). `Cambio(transmision, modo)` aplica las acciones con topes (`TopeMarcha`: más dura, más ligera, plato o piñón); con platos, `ModoCambio.AUTOMATICO` sigue reglas con histéresis y compensación, y `MANUAL` mueve piñón y plato por separado | F5 |
| `ModeloResistencia` | `:core` | Marcha + terreno + cadencia → `ComandoRodillo` (`Pendiente` o `Potencia`). Implementaciones simple, por tabla y física | F6 |
| `ControlRodillo` | `:core` | Modo de control (§ 5.4), protecciones (entre ellas el suelo de `ModoBajadas`, § 5.5), frecuencia de envío y deduplicación de comandos | F6, F7, F8.2–F8.3 |
| `AjusteErg` | `:core` | En ERG (de Zwift o manual), acciones de marcha → ±10 W por marcha, «+10 W»/«−10 W», □ quita el ajuste (vuelve a lo de Zwift o a la potencia de entrada), potencia entre 50 y 1000 W | F7.2, F8.3 |
| `FecCodec` | `:core` | Páginas FE-C 0x33, 0x31 (y 0x37 experimental) con su checksum | F5, F7, F15 |
| `TelemetriaCodec` | `:core` | Cycling Power Measurement (potencia) y CSC Measurement (biela) del Neo; `CalculoCadencia` saca la cadencia de los eventos de biela | F11 |
| `DatosSesion` | `:core` | Muestras de telemetría con marca de tiempo → datos de Rodaje: potencia media de 3 s, tiempo pedaleando (huecos recortados a 2 s), potencia media y máxima y W/kg. Puro, con reloj virtual en los tests; se crea con la `Sesion` y no avanza sin muestras. Zwift sigue recibiendo la potencia instantánea | F11.3 |
| `FtmsCodec` | `:core` | Control Point, Indoor Bike Data, Cycling Power Measurement, Fitness Machine Feature, Supported Power Range | F8 |
| `MandoZwiftCodec` | `:core` | Handshake `RideOn`, petición y respuesta de información, mensaje de botones y palancas analógicas (§ 7.4) | F9 |
| `FuncionZwift`, `EstadoMandoZwift` | `:core` | Las doce funciones de un mando oficial (cruceta, A, B, Y, Z, girar y frenar), con el botón del Square de la asignación por defecto. `EstadoMandoZwift`, puro: cuántas entradas mantienen cada función y la trama de botones (tabla `ENUM_MANUAL`, las dos palancas siempre presentes, el freno gana a su giro) | F9 |
| `DirconCodec` | `:core` | Mensajes DirCon: cabecera, descubrimiento de servicios y características, lectura, escritura y notificaciones; separa los mensajes del flujo TCP y atiende cada petición contra el `ServidorGatt` | F8.5 |
| `ConsultaMdns` | `:core` | La consulta mDNS (SRV con el bit QU) con la que la app pregunta por su propio anuncio, y reconocer en la respuesta el SRV con su puerto, con nombres comprimidos | F8.5 |
| `ServidorGatt` | `:core` | Modelo de servicios y características que se ofrecen a Zwift, común a los dos transportes: quién atiende cada escritura y qué se notifica | F8, F9 |
| `VentiladorCodec`, `ConfiguracionVentilador`, `ReglasVentilador` | `:core` | Protocolo del Headwind (§ 7.5): órdenes, notificaciones `FD`/`FE` en un resultado tipado y la configuración tal cual llegó, con los cuatro umbrales de pulso como vista y `conUmbral` que cambia solo ese byte; reglas puras de las tres acciones (paso de 10 %, nada en automático) y sus avisos | F17 |
| `PulsoCodec`, `PulsoVigente` | `:core` | Medida de pulso `2A37` (8 o 16 bits según las banderas) y su reenvío a Zwift (`[0x00, bpm]`); `PulsoVigente` da el último bpm válido (1–250) y lo caduca a los 5 s o al perder la banda | F16 |
| `PulsoClient` | `:app` | Cliente central del pulsómetro sobre `ClienteBle`: exige el servicio `180D`, se suscribe a `2A37` con la prioridad de conexión normal y emite las notificaciones y el estado del enlace | F16 |
| `VentiladorClient` | `:app` | Cliente central del ventilador sobre `BleManager`: exige el servicio y el control point, escribe sin respuesta con la prioridad de conexión normal y pide velocidad, modo y configuración al conectar | F17 |
| `BleScanner` | `:app` | Búsqueda por prefijo de nombre, sin distinguir mayúsculas, con límite de tiempo | F1.1 |
| `SquareClient`, `NeoClient` | `:app` | Clientes centrales sobre `BleManager` de Nordic: UUID, suscripciones, prioridad de conexión, máquina de estados de conexión (§ 8). `NeoClient` guarda el comando pendiente (§ 6.2) | F1, F2 |
| `TransporteDircon` | `:app` | Interfaz común a los dos transportes: publicar el `ServidorGatt`, recibir escrituras y enviar notificaciones | F8.5 |
| `DirconServer` | `:app` | Transporte wifi: servidor TCP de un cliente en la IPv4 de la wifi (uno nuevo sustituye al anterior solo con `PoliticaClientes`, § 7.3) y anuncio mDNS con `AnuncioNsd`; atiende cada petición en el hilo de control. Sin cliente, comprueba el anuncio con `SondaMdns` y pasa a «No visible» si no responde | F8.5, F1.4 |
| `SondaMdns` | `:app` | Pregunta por el propio anuncio desde un puerto efímero, por multicast en la interfaz del wifi y, de respaldo, por unicast a la propia IP; sin `MulticastLock` | F8.5 |
| `AdaptadorWifi` | `:app` | Si hay wifi y la dirección del móvil (`NetworkCallback`, también sin internet), y `WifiLock` de baja latencia | F8.5, NF4 |
| `GattServerHost`, `Advertiser` | `:app` | Transporte Bluetooth, genérico: `GattServerHost` sirve un `ServidorGatt` con el servidor GATT nativo (servicios añadidos de uno en uno, clientes solo por uso, notificaciones en serie con `onNotificationSent`) e implementa `TransporteDircon`; `Advertiser` anuncia lo que se le pase (uno o varios servicios y datos de fabricante, con un callback nuevo en cada anuncio) y dice si el móvil tiene modo periférico (NF3). Hay dos `GattServerHost` con su `Anuncio`: el del mando aparte (modo por wifi) y el del rodillo por Bluetooth, que nunca están abiertos a la vez | F9 |
| `TransporteBluetooth` | `:app` | Transporte del modo «Solo Bluetooth»: publica el servidor GATT solo con el Bluetooth encendido, los permisos y el modo periférico, y lo retira si falta algo. Su estado añade `SinBluetooth` y `NoDisponible` al del servidor | F8.5, NF3 |
| `MandoZwiftEnServidor` | `:app` | El mando de Zwift como un servicio más del servidor del rodillo por Bluetooth. Su estado sigue al ajuste, a la publicación del rodillo, al saludo de Zwift y al estado del transporte | F9 |
| `PuenteFtms` | `:app` | Servicios FTMS y Cycling Power del `ServidorGatt`; terreno y potencia objetivo de la plataforma; notificaciones de telemetría. No sabe por qué transporte va | F8 |
| `MandoZwift` | `:app` | Servicio de mando Zwift Ride en un `ServidorGatt` propio (§ 7.4): handshake, información, funciones pulsadas por ASYNC y *keepalive* de 5 s. Independiente del transporte (no importa `ble/`): lo publica `GattServerHost` aparte (modo por wifi), `ConexionZwiftRide` lo monta dentro del rodillo por DirCon y `MandoZwiftEnServidor` dentro del servidor del rodillo por Bluetooth. `estado`: desactivado, esperando o conectado (tras el handshake); la `Sesion` añade «sin conexión» y «no disponible» | F9 |
| `Sesion` | `:app` | Orquesta todo lo anterior; único dueño del estado de rodaje (`EstadoRodaje` y sus tipos, en `EstadoRodaje.kt`). Delega en dos piezas internas que escriben en su estado y corren en su hilo de control: `ConexionesSesion` (búsquedas, intentos con los configurados, reconexión, «Olvidar» y el estado de cada dispositivo; le avisa de cada cambio por `ReaccionesConexion`) y `VentiladorSesion` (órdenes al ventilador, su verificación y los umbrales) | Todas |
| `RideService` | `:app` | *Foreground service* que mantiene viva la `Sesion` y muestra su notificación; no tiene lógica | F1.5, NF4 |
| `RegistroSesion` | `:app` | Registro circular de tramas BLE y DirCon, gestos, acciones y comandos con marca de tiempo; con un sumidero opcional de sucesos | F13 |
| `GrabadorSesion`, `RepositorioSesiones` | `:app` | Graba los sucesos en un archivo de texto por sesión (con parada por espacio libre mínimo) y gestiona las sesiones grabadas | F13.4 |
| `Configuracion` | `:core` | Modelo de la configuración, valores por defecto, validación y migraciones entre versiones | F12 |
| `Almacen` | `:app` | Guarda `configuracion.json` y `estado.json` en DataStore |
| `CopiaConfiguracion`, `CopiaArchivo` | `:app` | Exportar e importar `configuracion.json` por `Uri` del selector del sistema (`CreateDocument`/`OpenDocument`, sin permisos), con tope de 1 MB. La importación valida con `Configuracion.validar` (`:core`: motivos vacío, no es JSON, no es una configuración, versión más nueva) y solo aplica al confirmar | F12.6 |
| `TurnIndicator` | `:app` | Una flecha de 56 dp a cada lado de la marcha (la derecha reflejada), con el sitio siempre reservado y anunciada al lector de pantalla. `Sesion.estado.giro` sale de `EstadoMandoZwift.giro()` (el freno de la palanca anula su giro) con un mínimo visible de 500 ms (`GIRO_MINIMO_MS`), solo de presentación | F11 | F1.2, F5.6, F12.6 |
| `ui/` | `:app` | Pantallas Compose y sus `ViewModel` | F11, F12, F13 |

### 4.4 Ciclo de vida

El nombre Bluetooth del móvil sigue la publicación por Bluetooth: `AppGraph` recupera al abrir un nombre que quedó puesto por un cierre inesperado, la `Sesion` lo pone (`RenombradorBluetooth.aplicar`) antes de cada `PuentePlataforma.usar` y lo devuelve en `terminar()`, tras parar los servidores. Si cambia con la conexión ya anunciada, el puente vuelve a anunciar el Bluetooth. Si no se puede renombrar, se publica con el nombre del móvil y Conexión lo avisa.

```
Application ──crea──▶ AppGraph (Almacen, SquareClient, NeoClient, DirconServer, GattServerHost, Advertiser, MandoZwift, RegistroSesion…)
MainActivity ──abre──▶ AppGraph.iniciarSesion() ──crea──▶ Sesion
MainActivity ──inicia──▶ RideService (foreground, tipo connectedDevice) ──observa──▶ Sesion
ViewModel ──observa──▶ Sesion.estado (StateFlow) · envía intenciones ──▶ Sesion
```

- La `Sesion` la crea `AppGraph` al abrir la app y la descarta al terminar; el `RideService` la mantiene viva en primer plano, pero no la crea: desde Android 14, un servicio `connectedDevice` exige tener ya `BLUETOOTH_CONNECT`, que en el primer uso se concede después de abrir la app. El servicio arranca en cuanto el permiso está concedido. La `Sesion` no depende de la Activity. Girar, apagar la pantalla o cambiar de app no corta nada (F1.5).
- La sesión empieza al abrir la app (con dispositivos configurados, se conecta sola a ellos, § 8) y termina cuando el usuario sale explícitamente (Atrás dos veces seguidas desde Rodaje o «Terminar» en la notificación). La notificación permanente muestra la marcha y el estado de las conexiones.
- La `Sesion` se expone a los `ViewModel` a través de `AppGraph` (un `StateFlow<Sesion?>` que publica el servicio), sin *bound service* ni AIDL: todo corre en el mismo proceso.
- La pantalla de rodaje mantiene la pantalla encendida (`FLAG_KEEP_SCREEN_ON`, F11.7). La conexión no depende de ello.
- **A validar:** si hace falta pedir la exención de optimización de batería para sesiones de 2 h con la pantalla apagada en los móviles de prueba (NF4). Las conexiones y su reconexión ya funcionan con la pantalla apagada sin pedirla (change `conexion-automatica-en-segundo-plano`). La prueba de 2 h, ahora rodando en Zwift, quedó pendiente al archivar `puente-zwift-wifi`.
- El mando de Zwift (`MandoZwift`, con su `GattServerHost` y su `Advertiser`) se crea una vez en `AppGraph`, como el `DirconServer`; con «Solo Bluetooth» la conexión crea el suyo dentro del servidor del rodillo. La `Sesion` lo publica con un Square configurado (sin interruptor desde `pestana-conexion`), el Bluetooth encendido, los permisos y el modo periférico, y lo para si falta algo; se publica una vez mientras duren esas condiciones, no con cada reconexión del Neo o del Square (volver a publicar con Zwift abierto lo deja en «Conectando…», § 7.4).
- Al terminar (Atrás dos veces seguidas desde Rodaje o «Terminar» en la notificación), la `Sesion` suelta las funciones de Zwift y para el mando, guarda la marcha, deja de anunciarse a Zwift y suelta el `WifiLock`, deja el Neo en pendiente 0 %, desconecta los dos y el servicio se detiene.

## 5. Flujo de datos

### 5.1 Del Square al Neo (camino caliente)

```
notify 0x45 ─▶ SquareDecoder ─▶ EventosBoton ─▶ ReconocedorGestos ─▶ Asignacion ─▶ Sesion
                                                                                    │
                    ┌───────────────────────────────────────────────────────────────┘
                    ▼
     Cambio (marcha nueva) ─▶ ModeloResistencia ─▶ ControlRodillo ─▶ FecCodec ─▶ NeoClient ─▶ write fec3
                    │
                    └──▶ estado (StateFlow) ─▶ UI, vibración, aviso, notificación     (fuera del camino caliente)
```

Todo el tramo de la notificación a la escritura ocurre en el hilo de control (§ 6.1), sin cambios de hilo ni suspensiones intermedias. La UI, la vibración y el registro se enteran del cambio por el `StateFlow` y los eventos, después de que el comando ya esté en la cola del Neo.

### 5.2 De Zwift al Neo y del Neo a Zwift

```
Zwift ─write 0x2AD9─▶ TransporteDircon ─▶ PuenteFtms ─▶ FtmsCodec ─▶ Sesion ─▶ ControlRodillo ─▶ NeoClient
       (DirCon o BLE)                         │  respuesta 80 <op> 01/02 por indicación
                                              ▼
Zwift ◀─notify 0x2AD2 / 0x2A63── TransporteDircon ◀── PuenteFtms ◀── Sesion.telemetria ◀── NeoClient
```

- `PuenteFtms` trabaja con escrituras y notificaciones sobre UUID de características; el transporte solo las mueve. Así los tests del puente son los mismos para wifi y Bluetooth.

- `0x11` (simulación) actualiza el **terreno**: pendiente, coeficiente de rodadura, coeficiente de viento y viento (F8.2). El modelo simple solo usa la pendiente; el físico usa todo (F6.3).
- `0x05` (potencia objetivo) pasa a **ERG de Zwift** (F8.3).
- La telemetría del Neo se reenvía a Zwift a 4 Hz, independiente de la frecuencia de llegada (~1 Hz), con el último valor; sin Neo, a 0. Validado en `puente-zwift-wifi`: potencia y cadencia siguen en Zwift sin saltos.
- `0x11` y `0x05` llegan a la `Sesion` dentro de la escritura del punto de control, en el hilo de control; la respuesta `80 <op> 01` sale después como notificación.

### 5.3 Acciones

Toda entrada del usuario, venga del Square o de la pantalla, se convierte en una `Accion` del dominio y entra a la `Sesion` por el mismo punto:

| Grupo | Ejemplos | Destino |
|---|---|---|
| Marchas | `MarchaMas(n)`, `MarchaMenos(n)`, `MarchaMasLigera`, `MarchaMasDura`, `MarchaReferencia`, `FijarReferencia`, `PlatoMas`, `PlatoMenos` | `Cambio` |
| Rodillo | `AlternarErgManual`, `ErgMas`, `ErgMenos` (paso fijo de 10 W), `SiguienteModelo` | `ControlRodillo` |
| Zwift | `Zwift(funcion)`, con `FuncionZwift` | `MandoZwift` |
| App | `PantallaSiguiente`, `PantallaAnterior` | UI (evento de navegación) |

Así los botones − y + de la pantalla (F11.8) y la captura de botones (F4.3) reutilizan el mismo camino, y los tests del dominio cubren la UI táctil y el Square a la vez:

- El Square llega por `Sesion.gesto(g)`, que busca la acción en la asignación y llama a `ejecutar(accion, tipo, latencia)`. La pantalla llama a `Sesion.accion(a)`, que la ejecuta en el hilo de control con tipo `Pulsar` y sin latencia de palanca. Durante la captura, ninguna de las dos ejecuta nada.
- Las de Zwift van antes que el camino de ERG, sin vibración ni aviso de acción (ya se notan en Zwift): con «pulsar», la función queda pulsada en Zwift hasta el `Soltado` de su botón; con corta, larga o doble, es una pulsación de 100 ms. Se sueltan todas al perder el Square, al abrir la captura, al desactivar el mando, al perder a Zwift y al terminar. Con el mando desactivado no se envía nada.
- Las acciones de App no cambian el estado de la `Sesion`: emiten `EventoRodaje.Pantalla(siguiente)` y vibran como cualquier otra. `MainActivity` recoge los eventos con `repeatOnLifecycle(STARTED)` y navega a la pestaña vecina en orden circular; con la app en segundo plano el evento se pierde, porque el `SharedFlow` no tiene réplica.

### 5.4 Modos de control del rodillo

```
              0x11 de Zwift                     0x05 de Zwift
 ┌──────────┐ ─────────────▶ ┌───────────┐ ─────────────────▶ ┌───────────┐
 │ SimLibre │                │ SimZwift  │ ◀───────────────── │ ErgZwift  │
 └──────────┘ ◀───────────── └───────────┘       0x11         └───────────┘
   ▲    │      Zwift se va        ▲  │                              ▲
   │    │ AlternarErgManual       │  │ AlternarErgManual            │ 0x05 de Zwift
   │    ▼                         │  ▼                              │
 ┌──────────────────────────────────────────┐                     │
 │ ErgManual (debajo = SimLibre | SimZwift) │ ────────────────────┘
 └──────────────────────────────────────────┘
```

| Modo | Comando al Neo | Qué hacen las palancas |
|---|---|---|
| `SimLibre` | Pendiente base (siempre 0 %, F7.1; «Pendiente sin Zwift» se quitó en `pestana-conexion`) + marcha, según el modelo. Rodaje lo llama «LIBRE» | Cambian de marcha |
| `SimZwift` | Terreno de Zwift + marcha, según el modelo. Rodaje lo llama «SIM»; nace con el primer terreno y a los 30 s de perder la plataforma vuelve a `SimLibre` | Cambian de marcha |
| `ErgZwift` | Potencia que pide Zwift (± ajuste) | Ajustan ±10 W por marcha y □ quita el ajuste (F8.3, `AjusteErg`); la marcha se conserva para cuando Zwift vuelve a SIM |
| `ErgManual` | Potencia fijada por el usuario (entrada ± ajuste) | Como en `ErgZwift`: ±10 W por marcha y □ vuelve a la potencia de entrada (F7.2, `AjusteErg`); la marcha se conserva para cuando se vuelve a SIM |

- El modelo de resistencia solo actúa en los modos SIM. En ERG se envía la potencia tal cual.
- La etiqueta del modo en Rodaje sale de `Simulacion.modo` (`RodajeUiState.libre`: `SimLibre` → «LIBRE», `SimPlataforma` → «SIM»; con control manual o ERG manual, el modo de debajo) y el dato «Pendiente» de `pendienteDeSimulacion`: el terreno de la plataforma, `pendienteBasePct` (0) en Libre y `null` (—) en ERG. Los botones − y + son `RodajeUiState.botones = casilla || square != Conectado` (change `modo-libre-y-pendiente`).
- `ErgManual` guarda el SIM de debajo (`debajo`), como `Manual.anterior`: el terreno de Zwift lo actualiza sin enviar nada, y `AlternarErgManual` vuelve a él con la marcha puesta. Un `0x05` de Zwift lo termina (pasa a `ErgZwift` sin ajuste) y, si Zwift se va, solo `debajo` pasa a `SimLibre`. En `ErgZwift`, `AlternarErgManual` no hace nada. La potencia de entrada es la última del ERG manual (`ultimaPotenciaErgW`, 150 W la primera vez) (change `erg-manual`).
- Si Zwift se desconecta (o se pierde el wifi) en `SimZwift` o `ErgZwift`, se mantiene lo último que pidió 30 s y, si no vuelve, se pasa a `SimLibre` sin cambiar la marcha. El ajuste de ERG se pierde al salir de `ErgZwift`. Los 30 s son provisionales: la prueba de desconexión quedó pendiente al archivar `puente-zwift-wifi`.
- El control manual de Diagnóstico (F13.2) es un modo más, `Manual`, que guarda el modo de debajo: el comando del usuario sustituye al de la marcha y al de Zwift mientras se está en Diagnóstico; las palancas siguen cambiando la marcha sin enviarla, lo que llega de Zwift actualiza el modo de debajo, y al salir de Diagnóstico o perder el Neo se vuelve a él con su comando.

### 5.5 Modelos de resistencia

Todos implementan la misma interfaz, para poder compararlos en el Neo y alternarlos con un botón (F6):

```kotlin
interface ModeloResistencia {
    fun comando(terreno: Terreno, cadenciaRpm: Double, marcha: EstadoMarcha): ComandoRodillo
}

data class Terreno(val pendientePct: Double, val crr: Double?, val cw: Double?, val vientoMs: Double?)

sealed interface ComandoRodillo {
    data class Pendiente(val pct: Double) : ComandoRodillo   // FE-C 0x33
    data class Potencia(val vatios: Int) : ComandoRodillo    // FE-C 0x31
}
```

| Modelo | Cálculo | Comando |
|---|---|---|
| Simple (F6.1, por defecto) | Lineal: `pendiente_zwift + desplazamiento(marcha) × incremento`. Con platos: `pendiente_zwift + incremento × ln(r / r_neutra) / paso`, con `r_neutra` el centro geométrico del desarrollo y `paso = ln(r_max / r_min) / 23`, el recorrido de la lineal de 24 | `Pendiente` |
| Tabla (F6.2) | `pendiente_zwift + tabla[marcha]`; la tabla (`Configuracion.tablas`, una por transmisión, en el orden de `escalera()`) parte del reparto del simple | `Pendiente` |
| Físico (F6.3) | Velocidad = cadencia × relación × 2,096 m; potencia = (masa·g·(sen θ + Crr·cos θ) + cw·(v + viento)²) × velocidad / 0,975, con Crr 0,004 y cw 0,51 si Zwift no los manda; la lineal reparte su relación entre 1,00 y 4,73 (Shimano 105); escalada por la dificultad | `Potencia` |

`EstadoMarcha` es `Numerada` (posición) o `PlatoPinon` (plato y piñón, con su **relación**), de modo que el modelo físico no depende de cómo se define la transmisión (F5.7). Al cambiar de transmisión, la `Sesion` crea el modelo de la nueva y lo pasa a `ControlRodillo.cambiarModelo`, que envía la pendiente nueva si cambia.

Las protecciones del modelo físico viven en `ControlRodillo` (`alCambiarCadencia`, que la `Sesion` llama con cada muestra de cadencia del Neo) y no en el modelo, con sus constantes provisionales en `ProteccionesFisico`: media móvil de la cadencia (1 s) y de la pendiente (2 s), alivio lineal por debajo de 40 rpm hasta la potencia mínima, rampa de +100/−200 W/s salvo en el cambio de marcha, y envío como mucho cada 250 ms y con 2 W o más de diferencia. La dificultad multiplica la suma de terreno y marcha antes del suelo de «Bajadas» (o la potencia, en el físico); «Bajadas» no se aplica a una `Potencia`. La velocidad y la distancia (`Recorrido`) las calcula la `Sesion` con la cadencia suavizada y solo con el físico. Estas protecciones se pueden aplicar también a la ERG manual si hace falta. Comunes a todos los modelos: evitar pendiente negativa (F6.5), dificultad global (F6.4) y no reenviar un comando igual al anterior.

`ModoBajadas` (F6.5) fija el suelo de la pendiente en SIM, con y sin Zwift: ninguno, `mín(terreno, 0 %)` o 0 %. `ControlRodillo` lo aplica a la `Pendiente` que devuelve el modelo, nunca a la ERG ni al control manual, y lo cambia en caliente con `cambiarBajadas`. `sinLimite` da la pendiente sin suelo para Diagnóstico. La ventana y el umbral del terreno de Zwift comparan ya la pendiente limitada, así que una bajada larga bajo el suelo no genera envíos. Con el Neo 1 de pruebas, el motor empuja el volante a partir de −1,5 % (a −1,0 % aún frena; validado en `proteccion-bajadas`).

## 6. Concurrencia y latencia (NF1)

### 6.1 Hilo de control

- La `Sesion` procesa todo en un único despachador secuencial (`Dispatchers.Default.limitedParallelism(1)`): eventos del Square, comandos de Zwift, telemetría del Neo, acciones de la UI y temporizadores del reconocedor de gestos.
- El reconocedor tiene un único temporizador (`Job`) que espera a `proximoVencimiento()` y llama a `vencer` con el reloj monotónico; sus gestos (larga, repetición, corta tras la ventana de la doble) siguen el mismo camino que los de una notificación. Las palancas no lo usan para responder: su `Pulsar` sale con la notificación.
- Apagar el Square (los dos Campagnolo con I1 y D1): la `Sesion` guarda la marcha al pulsar el primero de los cuatro y, si llegan a estar los cuatro pulsados, vuelve a ella. Validado con el Square real: llegan unos 4,6 s antes del corte.
- Los *callbacks* de `BluetoothGattCallback` y `BluetoothGattServerCallback` solo copian los bytes y los mandan por un `Channel` al hilo de control. No hacen trabajo en el hilo de Binder.
- Al haber un solo hilo que cambia el estado, no hay bloqueos ni condiciones de carrera en el dominio, y los tests pueden reproducir cualquier orden de eventos con un reloj virtual.

### 6.2 Cola de escritura del Neo

Android admite una sola operación GATT en curso por conexión, y la biblioteca de Nordic las pone en cola. `NeoClient` no le entrega un comando hasta que termina la escritura anterior, y mientras tanto guarda **como mucho un comando pendiente**: uno nuevo sustituye al anterior (el último gana). Así la cola de la biblioteca nunca acumula comandos de resistencia, y un cambio de marcha no espera detrás de comandos que ya no valen.

- Cambio de marcha: se encola al instante, saltándose la limitación de frecuencia.
- Cambios de terreno o de cadencia (modelo físico): como máximo 4 Hz y solo si el comando cambia de forma apreciable. Con Zwift (`puente-zwift-wifi`): un terreno se envía si difiere 0,1 % o más de lo enviado y han pasado 250 ms desde el último envío; si no, se guarda y se envía el más reciente al cumplirse. La potencia de ERG, si cambia 2 W o más. Zwift manda la simulación cada pocos segundos con cambios de centésimas, que así no llegan al Neo.
- Los comandos se escriben **sin respuesta** (`fec3` lo admite): la escritura termina al entregarla a la pila Bluetooth y la ranura se libera sin esperar la confirmación del Neo, que con su intervalo de 150 ms tardaba 200–400 ms (validado en `marchas-sin-zwift`).
- Si la escritura no termina en un tiempo máximo, se descarta y se envía el comando vigente.

### 6.3 Presupuesto de latencia

| Tramo | Objetivo | Cómo se consigue |
|---|---|---|
| Pulsación → notificación del Square en el móvil | ≤ 30 ms | Prioridad alta **sostenida** en el Square: cada vez que renegocia 187,5 ms (latencia 4), se vuelve a pedir, con al menos 10 s entre peticiones. Alterna 12,5 y 187,5 ms cada ~5 s, así que una pulsación tarda hasta 12,5 o hasta ~190 ms en llegar según el momento. **Límite conocido** |
| Notificación → comando en cola | ≤ 5 ms | Hilo de control sin saltos; `Pulsar` sin espera en las palancas (F3.4). Medido: 1–4 ms |
| Comando → escritura en el Neo | ≤ 30 ms | Escritura sin respuesta y cola que sustituye. Prioridad alta solo al conectar: el Neo se desconecta si se le vuelve a pedir, y se queda en 150 ms (latencia 0). Medido de la notificación a la entrega: 33–78 ms, mediana 62. Después, hasta 150 ms al siguiente evento de conexión del Neo. **Límite conocido** |
| Neo aplica la resistencia | El resto | Depende del Neo. **A validar** con el equipo real |

`RegistroSesion` guarda las marcas de tiempo de cada tramo, y Diagnóstico muestra la latencia de la última palanca para comprobar NF1 en cada versión.

Resultado del change `marchas-sin-zwift` (25-10-2026), medido de la notificación del Square a la entrega al Neo: 228–786 ms (mediana 414) con escritura con respuesta y sin prioridad sostenida; 211–436 ms (mediana 270) con prioridad sostenida en el Square; 33–78 ms (mediana 62) añadiendo la escritura sin respuesta. Con esto, el usuario nota mucho menos retraso y la resistencia correcta tras ráfagas de cambios, y NF1 se acepta. Los intervalos que imponen el Square y el Neo quedan fuera del control de la app como límite conocido.

## 7. Protocolos

Resumen de lo que implementa el paquete `protocolo` de `:core`. Los detalles de bytes se fijan con tests de vectores (entrada → bytes esperados) y se contrastan con capturas del equipo real.

### 7.1 Elite Square (central)

| Elemento | Valor |
|---|---|
Validado con el Square real en el change `diagnostico-square-neo` (25-10-2026); trazas en `core/src/test/resources/trazas/`.

| Elemento | Valor |
|---|---|
| Nombre anunciado | `SQUARE`, con los servicios `0x180F` (batería) y `347b0001-…` en el anuncio |
| Servicio | `347b0001-7635-408b-8918-8ff3949ce592` |
| Botones (se usa) | Característica `347b0045-…`, notify, 11 bytes. Byte 0 a cero y, desde el *nibble* alto del byte 1, un contador de 4 bits por botón que sube uno al pulsar y otro al soltar (impar = pulsado). Orden: ↑, ←, ↓, →, ✕, □, Campag. I, freno I, palanca I1, palanca I2, Y, A, B, Z, ○, △, Campag. D, freno D, palanca D1, palanca D2 |
| Máscara (solo registro) | Característica `347b0043-…`, notify. Bytes 3–6: `uint32` *big endian* con un bit por botón. Solo da el estado del momento |
| Palancas | La palanca 1 es la grande y la 2 la pequeña, en los dos lados |
| Notificaciones | Las dos características notifican también en reposo, al ritmo del intervalo de conexión |
| Seguridad | Sin emparejamiento ni cifrado |

- **Dirección:** aleatoria estática (`DE:C1:…` en el Square de pruebas). No cambia al apagarlo y encenderlo (validado en `conexion-automatica-en-segundo-plano`), así que se puede reconectar con `autoConnect` sin buscar.
- **Batería:** `0x2A19` del servicio `0x180F`, leída al conectar y cada 10 min, al registro y, salvo la primera de cada conexión, como nivel (`SquareClient.nivelBateria`, filtro `FiltroBateria`, `Sesion.estado.bateriaSquare`). Justo después de encenderlo da valores sin sentido (0 % o 35 % con la pila que da 100 % en las conexiones sin reinicio); solo cuentan las lecturas periódicas, así que no hay nivel hasta 10 min tras conectar. El nivel se conserva al reconectar, se borra al olvidar el Square y no se persiste. `BateriaSquare.baja` (`:core`): 20 % o menos, provisional hasta la prueba de 2 h. Se muestra en la fila del Square de Diagnóstico y como insignia del icono del Square de `ConnectionBar` (`EstadoChip.Baja`), nunca como porcentaje en Rodaje.
- **Apagado:** se pulsan a la vez los dos Campagnolo, una palanca izquierda y una derecha durante más de 5 s. La app lo reconoce hoy como gestos y cambia la marcha; se corrige en `gestos-y-asignacion`.

Los botones se leen de los contadores de `0x45`: la diferencia con el anterior (módulo 16) es el número de transiciones, así que una pulsación completa entre dos notificaciones no se pierde. Con `0x43`, en cambio, diez pulsaciones rápidas por palanca daban solo 4 o 5. Varios botones a la vez funcionan sin casos especiales (F2.2). La primera notificación tras conectar fija la línea base; si se pierde la conexión con botones pulsados, se emite un `Soltado` sintético para que ninguno quede repitiendo.

### 7.2 Tacx Neo 1 (central)

| Elemento | Valor |
|---|---|
| Nombre anunciado | Empieza por `Tacx Neo` (validado: el Neo 1 de pruebas se anuncia como «Tacx Neo 12345», con los servicios `0x1818`, `0x1816`, `0x180A` y FE-C en el anuncio) |
| Canal FE-C | Servicio `6e40fec1-b5a3-f393-e0a9-e50e24dcca9e`; escritura en `6e40fec3-…`, respuestas en `6e40fec2-…` |
| Telemetría | Cycling Power Measurement `0x2A63` (potencia) y CSC Measurement `0x2A5B` (cadencia) |
| Mensajes | ANT+ FE-C encapsulado: `A4 09 4E 05` + página + 7 bytes + checksum (13 bytes) |
| Página 0x33 (pendiente) | `(pendiente % + 200) × 100`, `uint16` *little endian*; rodadura `0xFF` (por defecto) |
| Página 0x31 (potencia) | `vatios × 4`, `uint16` *little endian* |
| Página 0x37 (configuración) | Pesos, rueda y relación de marcha. Solo para el experimento F15 |

- La cadencia se calcula con el delta de revoluciones de biela y su tiempo (1/1024 s), con desbordamiento de 16 bits. Con potencia 0, cadencia 0; sin datos válidos en 2 s, cadencia 0.
- El checksum de la página 0x33 es el XOR estándar de ANT (validado en la fase 0).
- La potencia de `0x2A63` y la cadencia de `0x2A5B` coinciden con la página FE-C 0x19 (Trainer Data), que el Neo envía por `fec2` a ~4 Hz; las dos características estándar llegan a ~1 Hz (validado en `marchas-sin-zwift`, traza `neo-erg-200w.txt`).
- `fec3` admite escritura con y sin respuesta (propiedades `0x0c`); la app escribe sin respuesta (§ 6.2).
- La dirección (`EE:77:…` en el Neo de pruebas, aleatoria estática) no cambia al desenchufarlo y enchufarlo (validado en `conexion-automatica-en-segundo-plano`).
- **A validar:** la velocidad que da el Neo con marchas virtuales (decisión abierta 4).

### 7.3 Puente con Zwift

Servicios que se ofrecen a Zwift, iguales por wifi y por Bluetooth:

| Servicio | Características |
|---|---|
| Fitness Machine `0x1826` | `0x2ACC` Feature (read), `0x2AD6` Supported Resistance Range (read), `0x2AD9` Control Point (write, indicate), `0x2AD2` Indoor Bike Data (notify), `0x2ADA` Status (notify), `0x2AD3` Training Status (read) |
| Cycling Power `0x1818` | `0x2A65` Feature, `0x2A5D` Sensor Location, `0x2A63` Measurement (notify, con datos de biela para la cadencia) |
| Heart Rate `0x180D` | `0x2A37` Measurement (notify, `[0x00, bpm]`), `0x2A38` Body Sensor Location (read, 0). Solo se notifica con una lectura válida de un pulsómetro configurado (change `pulso-zwift`) |
| Device Information `0x180A` | Fabricante, modelo, versión de software, y desde el change `completar-protocolo-dircon-zwift` serie `0x2A25` (la de la identidad con que se publica), firmware `0x2A26` (versión de la app) y hardware `0x2A27` («1.0»), que Zwift lee al emparejar (validado con Zwift en PC) |

`0x2AD8` Supported Power Range (0–2000 W) se ofrece también; Zwift en PC no lo lee. Control Point: respuesta `80 <opcode> 01` (éxito), `80 <opcode> 02` (no soportado) o `80 <opcode> 03` (parámetro no válido) por indicación.

| Opcode | Comando | Tratamiento |
|---|---|---|
| `0x00` / `0x01` | Request Control / Reset | Éxito |
| `0x05` | Set Target Power | Pasa a `ErgZwift` |
| `0x07` / `0x08` | Start / Stop | Éxito |
| `0x11` | Indoor Bike Simulation | Viento (`sint16`, 0,001 m/s), pendiente (`sint16`, 0,01 %), Crr (0,0001), Cw (0,01 kg/m). Pasa a `SimZwift` |
| resto | — | No soportado |

**Validado (27-10-2026, COOSPO HW9 y Zwift en PC):** Zwift lista «Smart Bike» (entonces «Square Shift») en la pantalla de emparejamiento a la primera con potencia, cadencia, resistencia y pulso, y los bpm de la banda llegan. La banda solo se conecta al móvil; el PC la recibe por wifi desde la app, así que no hace falta probar cuántas conexiones Bluetooth admite. El ventilador Headwind sigue al pulso (por su ANT+, sin pasar por la app). **Sigue a validar:** 20 min rodando con pulso sin cortes de potencia, cadencia ni cambios de marcha.

Indoor Bike Data se notifica con velocidad (0,01 km/h), cadencia (0,5 rpm) y potencia (W). La velocidad va a 0: Zwift calcula la del avatar con la potencia (validado). Cycling Power Measurement lleva la potencia y los datos de biela del CSC del Neo tal cual.

- La dificultad del rodillo en Zwift escala la pendiente antes de enviarla. La recomendación para el usuario (dejarla en 100 % y ajustar la dureza en la app con F6.4, o al revés) se decide al comparar modelos en la fase 5.

#### Transporte por wifi: DirCon (principal)

DirCon (*Direct Connect* de Wahoo) lleva los mismos servicios GATT sobre TCP en la red local. Zwift lo admite en PC, Mac y Apple TV. No hay especificación pública: lo que sigue se dedujo de su descripción y se **validó con Zwift en PC** en `puente-zwift-wifi` (26-10-2026) sin tener que corregir nada. Apple TV y Mac, sin probar.

| Elemento | Valor |
|---|---|
| Descubrimiento | Anuncio mDNS de tipo `_wahoo-fitness-tnp._tcp` con nombre «Smart Bike» y `NsdManager`. Registros TXT: `ble-service-uuids=0x1826,0x1818,0x180A`, `mac-address` (`XX:XX:XX:XX:XX:XX`, local administrada) y `serial-number` (10 cifras), generados una vez y guardados en `estado.json` (§ 10) |
| Conexión | Servidor TCP en el puerto 36866 (si está ocupado, uno libre), enlazado a la IPv4 de la wifi, no a todas las interfaces. Un solo cliente: Zwift; uno nuevo lo sustituye solo si viene de la misma IP o si el actual lleva 15 s sin enviar nada (`PoliticaClientes`); si no, se rechaza |
| Mensajes | Cabecera de 6 bytes: versión (1), identificador, número de secuencia, código de respuesta y longitud del contenido (`uint16` *big endian*). La respuesta repite identificador y secuencia |
| Operaciones | `0x01` descubrir servicios, `0x02` características de un servicio (UUID + propiedades: leer `0x01`, escribir `0x02`, notificar `0x04`), `0x03` leer, `0x04` escribir, `0x05` activar notificaciones, `0x06` notificación del servidor. UUID siempre en 128 bits |
| Errores | 1 mensaje desconocido, 2 error inesperado, 3 servicio no encontrado, 4 característica no encontrada, 5 operación no admitida |
| Secuencia de Zwift en PC | Descubre los tres servicios y sus características, lee `2ACC`, se suscribe a `2AD9`, `2AD2`, `2ADA` y `2A63`, lee los datos del dispositivo y manda `00`, `01`, `00`, `07` al punto de control; en ruta, `11` cada pocos segundos |

- Las escrituras de Zwift en el Control Point y las notificaciones hacia Zwift son las mismas tramas FTMS de arriba: `FtmsCodec` no cambia.
- El móvil mantiene el wifi en modo de baja latencia mientras rueda (`WifiManager.WifiLock`, `WIFI_MODE_FULL_LOW_LATENCY` desde Android 10, `WIFI_MODE_FULL_HIGH_PERF` antes), para que el ahorro de energía con la pantalla apagada no retrase la telemetría (NF4).
- **Endurecido (change `endurecer-dircon-ble`, 29-10-2026).**
  - **Interfaz.** `DirconServer` recibe la dirección de la wifi (`wifi.direccion`), hace `bind` a esa IPv4 y la anota en el registro junto al puerto. Sin dirección no se abre, y si la wifi cambia de dirección se cierra y se vuelve a abrir.
  - **Sustitución de cliente.** `PoliticaClientes` (`:core`): el nuevo sustituye si no hay actual, si viene de la misma IP o si el actual lleva 15 s sin enviar nada; si no, se rechaza y se anota. Limitación conocida: con Zwift en los menús, que apenas envía, un equipo de la wifi puede sustituirlo pasados esos 15 s. Con Zwift rodando se rechaza al extraño.
  - **Cliente desaparecido.** Cada socket aceptado lleva `keepAlive` y `TCP_USER_TIMEOUT` de 15 s (`OsConstants.TCP_USER_TIMEOUT` existe desde la API 26), más un vigilante que cierra el socket si una escritura lleva 10 s bloqueada. No se desconecta por silencio de lectura, porque Zwift calla en los menús.
  - **Ritmo y errores.** `LimiteRitmo` por cliente (cubo de 100 mensajes, 50 por segundo) desconecta al que lo agota. `DirconCodec.Lector` usa un búfer fijo de `CABECERA + 65 535` bytes. Una excepción al atender responde `ERROR_INESPERADO` y la conexión sigue.
  - **Validado en el OnePlus 8 con Zwift en PC (29-10-2026).** La ráfaga inicial de Zwift (15 mensajes en 250 ms) no llega al límite de ritmo. Al cortar la red del PC, la plataforma se da por desconectada a los 13 s y a los 30 s el Neo vuelve a la pendiente base; Zwift reconecta solo al volver la red. Tres minutos en los menús sin cortes. Con Zwift rodando, un `nc` desde otro equipo queda rechazado. Palancas de 37 a 77 ms (9 cambios). **Sin probar:** 10 minutos seguidos, y la escucha con los datos móviles activos.
- Sin wifi, `DirconServer` para y retira el anuncio; al volver el wifi, se anuncia otra vez. No se usa `MulticastLock`: el servicio mDNS del sistema responde por la app.
- **Que el sistema dé el anuncio por registrado no garantiza que responda.** El 27-10-2026, en el OnePlus 8, `NsdManager` lo daba por anunciado (probing terminado en `wlan0`), pero no contestaba en la red, con errores `EADDRINUSE` del servicio mDNS del sistema al unirse al grupo multicast. Zwift no veía el rodillo hasta apagar y encender el wifi. Al usuario le pasaba también con qdomyos-zwift. Desde el change `comprobar-anuncio-zwift`, mientras no hay cliente, `DirconServer` pregunta por su propio anuncio al registrarse y cada 60 s (`SondaMdns`: SRV con el bit QU). Si no contesta, se vuelve a anunciar una vez y, si sigue igual, pasa a «No visible»: Dispositivos dice que se reinicie el wifi o el móvil. Validado en el OnePlus 8 que el responder del sistema contesta a la consulta multicast del propio móvil (2–5 ms) y el camino del aviso con un nombre falso. **A validar:** que detecta el fallo real cuando se repita (no se puede provocar) y si alguna vez basta con volver a anunciarse.
- Zwift reconoce el dispositivo aunque cambie la dirección del TXT (tras reinstalar la app se reconectó solo en 4 s): parece bastarle el nombre.
- **A validar** (pruebas pendientes al archivar `puente-zwift-wifi`): el comportamiento al perder el wifi y al cerrar Zwift (los 30 s de § 5.4), y si el `NsdManager` del OnePlus 8 anuncia de forma estable con la pantalla apagada durante 2 h.

#### Transporte por Bluetooth (alternativa)

El móvil se anuncia como periférico BLE con los mismos servicios en su servidor GATT.

- **Endurecido (change `endurecer-dircon-ble`).**
  - **Hilo de control.** Todos los avisos de `BluetoothGattServerCallback` saltan al hilo de control. Una instancia de `AltaServicios` por publicación hace que el `onServiceAdded` tardío de un servidor ya parado no añada servicios ni anuncie.
  - **Notificaciones.** `GattServerHost` guarda las suscripciones por dispositivo (CCCD) y `ColaNotificaciones` tiene una cola y una confirmación por dispositivo suscrito: uno lento no retrasa a otro, y quien no está suscrito no recibe nada.
  - **Escrituras.** Las preparadas y `onExecuteWrite` responden `GATT_REQUEST_NOT_SUPPORTED`. Una escritura que lanza responde `GATT_FAILURE` y se anota.
  - **Permisos.** `SecurityException` se captura en `Advertiser`, `GattServerHost` y `BleScanner`.
  - **Validado en el OnePlus 8 (29-10-2026).** Con «Solo Bluetooth» Zwift en PC conecta rodillo y controlador y las pulsaciones llegan como notificaciones. También con «Wifi + Bluetooth» y la opción de usar el mismo nombre por Bluetooth, alternando los tres modos en caliente. Con el mismo nombre, Zwift lista el controlador dos veces (el de la wifi y el de Bluetooth): hay que elegir el de Bluetooth. Un primer intento fallido se debió a tener elegido el de la wifi, no a la app.
  - **Resiliencia ante un corte de red (observado, sin medir).** «Solo wifi» es el modo más resiliente: tras un corte de wifi, Zwift vuelve a conectar solo (§ 7.3). En los modos con Bluetooth hay que esperar un rato y volver a sincronizar el dispositivo en Zwift.

- **A validar:** el contenido del anuncio. Un anuncio clásico tiene 31 bytes y no cabe el nombre con todos los UUID de servicio; se reparte entre anuncio y respuesta de escaneo, y se comprueba que Zwift en PC y en Apple TV lo reconoce.

**Lo publicado según lo configurado (change `pestana-conexion`, 28-10-2026; validado con Zwift en PC en los tres modos).** Ya no hay plataforma elegida: la app publica servicios y la app de entrenamiento se conecta a ellos. Qué se publica lo decide una función pura de `:core`, `Publicacion.de(modo, configurados)`, que devuelve `ServiciosPublicados` (modo, dónde va el mando y si va el pulso) o `null`:

- Sin rodillo configurado, `null`: no se publica nada y el estado es `EstadoPlataforma.NoSeOfrece`.
- El pulso (`180D`), solo con un pulsómetro configurado (`PuenteFtms(pulso)`): deshace la decisión 4 de `pulso-zwift`.
- El mando, solo con un Square configurado: aparte por Bluetooth (`MandoZwiftBluetooth`) con «Wifi + Bluetooth», dentro (`MandoZwiftEnServidor`) con «Solo Bluetooth». En «Solo wifi» va siempre (`ModoPublicado.mandoSiempre`), porque Zwift manda el terreno por él. Sin interruptor: se activa al crear la conexión.

La `Sesion` llama a `PuentePlataforma.usar(servicios, identidad)` cuando cambian la configuración o los configurados (después de leerlos), y el puente solo vuelve a montar la conexión si cambia `ServiciosPublicados`: que un dispositivo se conecte o se desconecte solo cambia el estado. Cada tic de telemetría, el puente cuenta a la `Sesion` si la app de entrenamiento está suscrita al rodillo y al pulso (`usoServicios`), de donde sale el estado de cada servicio en Conexión.

### 7.5 Ventilador Wahoo KICKR Headwind (central)

Change `ventilador-headwind`. El protocolo **no es oficial**: sale de la ingeniería inversa de la app de Wahoo publicada por otros proyectos (probado por terceros en un ventilador, hardware rev 3, firmware 2.0.43) y se reimplementa a partir de su descripción. **Todo está «a validar» con el ventilador real**; la tarea 2.1 del change (nRF Connect, con la app de Wahoo cerrada) y la 5.1 (prueba en el OnePlus 8) lo confirman.

| Elemento | Valor |
|---|---|
| Nombre anunciado | `HEADWIND XXXX`; no anuncia el servicio (búsqueda por prefijo de nombre) |
| Servicio / control point | `a026ee0c-0a7d-4ab3-97fa-f1500f9feb8b` / `a026e038-0a7d-4ab3-97fa-f1500f9feb8b`: escritura sin respuesta, notificación y lectura |
| Órdenes | `01` leer velocidad · `02 NN` velocidad 0–100 (solo manual) · `03` leer modo · `04 MM` fijar modo (4 manual, 2 pulso; 1 apagado, 5 standby y 3/6–9 son otros) · `05` leer configuración · `06 <config>` escribirla |
| Notificaciones | `FD 01 SS MM` (velocidad y modo, cada ~1 s y tras cada cambio) · `FE xx ST …` respuesta a la orden `xx`, con `ST = 01` si fue bien |
| Configuración | 8 bytes tras el `ST` en el ventilador probado (la fuente decía 9 o 18): los cuatro primeros son los umbrales de pulso en bpm («desde» cada uno entra la zona; por debajo del primero, parado: indicado por el usuario, a validar). No hay orden para cambiar un campo: leer, cambiar un byte, escribir y releer |
| Comportamiento | `04 04` reanuda la última velocidad manual (validado) |

- El ventilador admite una sola conexión: con la app de Wahoo abierta no se le ve. La app lo desconecta al terminar la sesión.
- La app no guarda modo, velocidad ni umbrales: los guarda el ventilador. Solo la velocidad tiene un objetivo local de 2 s para no perder pasos al pulsar seguido; cada orden se confirma con la notificación y, si no llega en 2 s, se repite una vez.
- Cambiar un umbral va con rebote de 400 ms, en serie, y si falla vuelve el valor del ventilador. Una configuración de menos de 9 bytes no se escribe.
- Límites provisionales de los umbrales: de 60 a 220 bpm y estrictamente crecientes.
- **Validado (27-10-2026, un Headwind real, `HEADWIND 1234`):** nombre, servicio y control point; `FD`/`FE` como en la tabla; la configuración son **8 bytes** tras el `ST` (no 9 o 18); `04 04` reanuda la última velocidad manual; `02 NN` y `06` con confirmación `FE xx 01`; leer, cambiar un byte, escribir y releer deja el resto idéntico; reconexión sola al abrir la app; el 0 % en manual deja el ventilador parado (indicado por el usuario). Las tres acciones con los botones del Square funcionan (comprobado por el usuario).
- **Comprobado por el usuario (27-10-2026):** el automático sigue a la banda HW9 con la app conectada y cambia al cruzar un umbral; 20 min rodando con Zwift sin cortes y con NF1 como antes.
- **Sigue a validar:** los límites de los umbrales (60–220); el significado de los otros bytes de la configuración; otro firmware; que las órdenes no empeoren la latencia de las palancas (NF1).

**Por Bluetooth (change `ftms-bluetooth`, 28-10-2026; validado con Zwift en PC y el OnePlus 8).** El modo «Solo Bluetooth» (antes «Rodillo por Bluetooth (FTMS)») ofrece los mismos servicios (`PuenteFtms`, `FtmsCodec` sin cambios) por un segundo `GattServerHost`, sin wifi:

- **Anuncio:** FTMS (`0x1826`), Cycling Power (`0x1818`) y `FC82` con los datos de fabricante de Zwift (`0x094A`, `08 5A 01`), unos 18 bytes en el paquete principal; el nombre Bluetooth del móvil va en la respuesta de escaneo: Android no deja anunciar otro. Por eso «Nombre ante la app» por Bluetooth exige renombrar el móvil (`RenombradorBluetooth`, `BluetoothAdapter.setName`), solo con la casilla «Usar el nombre también por Bluetooth» y mientras la app publica por Bluetooth (change `nombre-bluetooth-propio`). Validado en el OnePlus 8 (29-10-2026) con «Solo Bluetooth» y «KICKR BIKE PRO»: Android acepta `setName`, el Mac ve el móvil como «KICKR BIKE PRO 992C», Zwift lista ese nombre en su pantalla de emparejamiento y, al cerrar la app, el móvil vuelve a verse como «OnePlus 8». Igual con «Smart Bike». Tras un cierre forzado con el móvil renombrado, al reabrir la app el registro apunta «Smart Bike» → «OnePlus 8» y, con la casilla aún marcada, lo vuelve a renombrar. Con «Wifi + Bluetooth» y un Square recordado, el controlador aparece como «Smart Bike» en la lista de mandos de Zwift. Se probó con los tres a la vez; no se ha probado cuál es el mínimo que basta.
- **Mando dentro:** el servicio del mando (las cinco características de `MandoZwift`, no las tres de DirCon) va en el mismo servidor. Zwift lista el móvil como rodillo, potencia, cadencia **y mando**, hace el handshake (`RideOn`), responde las páginas de información 0, 16, 1024 y 771 y los botones del Square mueven Zwift, sin emparejar el Square en Zwift. Zwift sigue controlando el rodillo por FTMS (`PedirControl`, `Reiniciar`, `Iniciar` y `Simulacion`): no hay `SET` ni marchas virtuales.
- **Lo validado:** aparece en las listas, la potencia y la cadencia llegan a ~4 Hz sin cortes, el terreno llega al Neo, la latencia de la palanca con el Neo, el Square y Zwift conectados es de 62 y 81 ms (dos cambios de plato), Zwift cerrado devuelve la pendiente base a los 30 s, y el cambio de modo en los dos sentidos no toca la marcha ni manda ningún comando al Neo. Con el Bluetooth apagado el rodillo y el mando constan «Sin conexión» y al encenderlo la app se vuelve a anunciar y Zwift vuelve a verlos al buscar.
- **Limitación:** Zwift **no reconoce el dispositivo entre sesiones** (Android anuncia con una dirección aleatoria que cambia): tras cerrar la app hay que quitar el dispositivo anterior en su pantalla de emparejamiento y buscarlo de nuevo, y aparece por su nombre Bluetooth. El modo por wifi, que Zwift sí reconoce, sigue siendo el recomendado. Una dirección estable (anuncio con dirección pública, Android 14 o posterior) queda sin probar.
- **A validar:** Zwift en Apple TV (el usuario no tiene uno: sigue sin validar), una subida real con cambios de piñón, un entrenamiento ERG, una sesión larga y la reconexión de Zwift tras cada cambio de modo.
- **Dos fallos que destapó la prueba, ya corregidos:** `Advertiser` reutilizaba el mismo `AdvertiseCallback` y, tras apagar y encender el Bluetooth, Android lo rechazaba con `ADVERTISE_FAILED_ALREADY_STARTED` (error 3), con lo que la app no volvía a anunciarse (también afectaba al mando aparte); ahora cada anuncio crea su callback. Y «Terminar» en la notificación no hacía nada con un intento de conexión al Square pendiente, porque `Sesion.terminar()` esperaba para siempre su desconexión; ahora cada desconexión tiene un límite de 1,5 s.

### 7.4 Mando de Zwift emulado

La app se presenta ante Zwift como un mando **Zwift Ride**, que no cifra el handshake. Lo que sigue se **validó con Zwift en PC** en el prototipo `prototipo-mando-zwift` (26 y 27-10-2026) y, ya en la sesión de rodaje, en `mando-zwift-emulado` (27-10-2026); sus resultados detallados están en el `design.md` de cada change archivado.

**Por Bluetooth, con el rodillo por DirCon.** Zwift ve **dos dispositivos**: el rodillo por wifi (DirCon, § 7.3) y el mando como periférico BLE. Zwift solo ofrece la lista de mandos cuando ya hay una fuente de potencia emparejada.

**Por Bluetooth, dentro del rodillo (change `ftms-bluetooth`, 28-10-2026).** Con el modo «Solo Bluetooth» el mando va como un servicio más del servidor GATT del rodillo, con el mismo anuncio (§ 7.3): un solo dispositivo ante Zwift, `MandoPublicado.DENTRO`, sin interruptor y sin emparejar el Square en Zwift. Sin Square configurado no se añade y el anuncio va sin `FC82`. Validado con Zwift en PC. Al emparejar el mando, Zwift trata el dispositivo como un Zwift Ride: manda el terreno por `SET` en SYNC_RX y toma el pedaleo de ASYNC. `ConexionFtms` atiende los dos caminos con `SetsDeZwift` (compartido con `ConexionZwiftRide`) y, desde el primer `SET` de cada saludo del mando, envía también el pedaleo por ASYNC (change `pestana-conexion`, decisión 9). `MandoZwiftEnServidor` envuelve a `MandoZwift` y su estado sigue también al del transporte (sin Bluetooth o sin modo periférico, «Sin conexión» o «No disponible»).

**Por qué no por DirCon** (prueba del change `mando-zwift-emulado`, 27-10-2026; detalle en su `design.md`, «Resultados de la prueba por DirCon»). El mando funciona por DirCon, pero no junto al puente de la sesión:

- Zwift reconoce el mando por DirCon («Bienvenido a Zwift Ride») si se publica con el servicio **`FC82`** (con `0003` escritura, `0002` notificación y `0004` notificación) y responde a la página 0 con una **`DevInfoPage` completa** (nombre, capacidades, fabricante y producto 8). Menús, Z, Y, dirección, freno y el *keepalive* funcionan como por Bluetooth. Con el servicio `00000001-19CA-…`, o con la página mal formada, hace el handshake pero ignora los botones.
- **Zwift solo admite un dispositivo DirCon por IP**: con el rodillo de la sesión y el mando anunciados desde el móvil, no lista ninguno; al quitar el mando, aparece el rodillo.
- **`FC82` dentro del rodillo** activa las **marchas virtuales de Zwift** («GEAR» en el HUD): Zwift envía `SET` (`0x3F`) del protocolo de Zwift por SYNC_RX con `TrainerSimulationParam`: al conectar, CW 0,51, Crr 0,004 y las masas de bici y ciclista; después, la relación de la marcha virtual y, al rodar, la **pendiente** (−1,64 %, −1,53 %, −1,01 %, en centésimas con zigzag). No se vio ninguna escritura en el punto de control FTMS, pero tampoco en las conexiones de la prueba sin marchas virtuales, así que **no está probado que deje de usar FTMS** (a validar si se reconsidera esta vía). Como los `SET` llevan la pendiente, una alternativa sería aceptar el modo de marchas virtuales, tomar la pendiente de ahí e ignorar la marcha de Zwift; queda como opción a estudiar. Lo seguro: habría dos modelos de marchas y el HUD mostraría la de Zwift (choca con F9.3).
- Así, por DirCon solo valdría con el rodillo emparejado por otra vía (Bluetooth, change 11). Decidido con el usuario: el mando va por Bluetooth y el rodillo por DirCon; esta alternativa queda documentada por si cambia la decisión.
- Zwift recuerda cada dispositivo DirCon por `mac-address` y `serial-number`: con la misma identidad sigue mostrando el nombre anterior.
- **Con el mando dentro del rodillo y el modelo de marchas de la app (change `mando-zwift-dircon`, etapa 1, 27-10-2026, Zwift en PC; detalle en su `design.md`).** Zwift envía `SET` con la pendiente de la ruta y no escribe en el punto de control FTMS; **con `FC82`, la potencia y la cadencia solo las toma de `TRAINER_NOTIF` (`0x03`, `TrainerNotification` con cinco campos obligatorios) enviada por ASYNC**, no de FTMS ni de Cycling Power (0 W). Los entrenamientos ERG llegan como `ergPower` (campo 2) del `SET`. La respuesta `40 08 00 10 <msgId>` basta para que no repita. Sus botones de cambio hacen subir su marcha (`SET` con la relación nueva), pero Zwift avisa de que el rodillo no admite marchas virtuales aunque se declare la capacidad de rodillo (`0x14`). **A validar:** 10 minutos sin pulsar nada y la identidad al cambiar de transporte.

**Comparativa con la captura de BikeControl (change `completar-protocolo-dircon-zwift`, 28-10-2026, Zwift en PC, Neo y Square).** Con la 1024 y la 771 con datos y la información nueva del dispositivo, botones, freno y giro siguen funcionando y Zwift no cambia nada visible. Se descartaron: la información de Click v2 (Zwift trata el mando como un Click sin palancas analógicas y fallan el freno y el giro, y los botones tardan en detectarse), el saludo `RideOn 02 00` con el aviso `2a 08 03 12 0d 22 0b "RIDE_ON(0)" 00` por ASYNC (funciona igual, sin mejora observable) y una 1024 con las palancas analógicas (no hizo falta). Sigue sin saberse el significado de la 771, del campo 6 del mensaje de datos de pedaleo y del tipo `0x06` del anuncio.

| Elemento | Valor |
|---|---|
| Anuncio | UUID de servicio `FC82` y datos de fabricante `0x094A` + tipo `0x08` (Ride izquierdo) + 2 bytes. El nombre es el Bluetooth del móvil: Android no deja anunciar otro sin renombrar el teléfono |
| Servicio | `00000001-19CA-4651-86E5-FA29DCDD09D1`, con Device Information (`0x180A`) y Battery (`0x180F`) |
| `…0002…` ASYNC (notify) | Botones y palancas hacia Zwift |
| `…0003…` SYNC_RX (write) | Zwift escribe el handshake `RideOn 02 04`, la petición de información `00 08 <id>` (0 y 16) y `41 08 05` |
| `…0004…` SYNC_TX (read, indicate) | Respuesta `RideOn` (sin más bytes) y respuestas a la información: `3C` + `GetResponse` con una `DevInfoPage` completa a la 0 (validada por DirCon; por Bluetooth se validó una página incompleta con «Zwift SF2»), `ClientServerCfgPage` a la 16, y, por DirCon («Todo por wifi»), la 1024 (`3c 08 80 08 12 07 0a 05 08 ff 23 10 00`: botones `0x11ff`, ninguna palanca analógica) y la 771 (`3c 08 83 06 12 0a 0a 08 08 00 10 64 18 00 20 00`, significado desconocido), tomadas de la captura de BikeControl; a las demás, solo el identificador |
| `…0005…`, `…0006…` | Presentes como en el original; Zwift se suscribe a la 6 y la lee |

**Mensaje de botones:** `0x23` + `RideKeyPadStatus`. Campo 1: mapa de botones **invertido** (bit a 0 = pulsado), con los valores del enum: `←` `0x1`, `↑` `0x2`, `→` `0x4`, `↓` `0x8`, `A` `0x10`, `B` `0x20`, `Y` `0x40`, `Z` `0x80` (Ride On, validado). Campo 3, repetido: una palanca analógica por ubicación (`Location` varint, `AnalogValue` `sint32` en zigzag, de −100 a 100).

| Palanca | Negativo | Positivo |
|---|---|---|
| 0 (izquierda) | se desplaza a la izquierda | frena |
| 1 (derecha) | frena | se desplaza a la derecha |

- **Las palancas sueltas se envían a 0** en cada mensaje: Zwift conserva el último valor de cada una, y un freno sin su 0 deja al avatar frenado.
- Las flechas ←/→ eligen el desvío en los cruces; el desplazamiento lateral en la calzada es el de las palancas. Las dos manetas hacia dentro a la vez frenan más.
- Los botones de cambio del Ride no sirven con el puente FTMS: Zwift avisa de que el rodillo no admite marchas virtuales. Las palancas del Square cambian la marcha de la app (F9.3) y no se reenvían.
- **El servidor GATT de Android avisa de todas las conexiones del móvil**, también de las que la app abre como central (el Neo, el Square). Solo cuenta como cliente quien usa el servidor (se suscribe, lee o escribe); si no, los botones se envían al Neo o al Square y una reconexión del Square borra el handshake.
- Si el servidor se vuelve a publicar con Zwift abierto, Windows conserva el enlace anterior y Zwift se queda en «Conectando…»; se resuelve cerrando y abriendo Zwift.
- *Keepalive*: el reposo cada 5 s mientras hay cliente. Por DirCon, por ASYNC, aguanta 10 min sin pulsar nada (Zwift corta un mando por red tras unos 30 s sin tráfico).
- **Validado en la sesión de rodaje** (`mando-zwift-emulado`, OnePlus 8 y Zwift en PC, con el Square desemparejado en Zwift): Zwift empareja el rodillo por wifi y el móvil en Mandos, con la página completa; menús, ↑, B, Y, Z, giro, freno (cada maneta y las dos) y ← / → en los cruces desde el Square; las palancas cambian la marcha de la app sin aviso de marchas virtuales; un botón reasignado deja de llegar a Zwift; el mando sigue al apagar y encender el Square rodando, y al desactivar y activar «Mando de Zwift» rodando.
- **A validar** por Bluetooth: 10 min sin pulsar nada (*keepalive* por ASYNC, validado por DirCon), cerrar Zwift con ○ mantenido y apagar el Square con un freno mantenido (la app suelta todo en los dos casos; cubierto por tests). Apple TV, sin probar.


#### Modo «Solo wifi (modo Zwift Ride)» (`mando-zwift-dircon`; antes «Todo por wifi»)

Dos implementaciones de la interfaz `ConexionPlataforma` (`ConexionFtms`, los modos «Wifi + Bluetooth» y «Solo Bluetooth», válidos para cualquier plataforma y con un `MandoEmulado?` opcional —`MandoZwiftBluetooth` aparte con el de wifi y `MandoZwiftEnServidor` dentro con el de Bluetooth, en Zwift—, y `ConexionZwiftRide`), sin ramas por modo en la `Sesion`: el puente (conexión, wifi, telemetría, pulso, mando e identidad) vive en `PuentePlataforma` (change `generalizar-plataformas`). Las identidades se guardan en `EstadoGuardado.identidades` por `ClaveIdentidad` (modo y nombre, sin plataforma desde `pestana-conexion`) y la conexión en `Configuracion.conexion` (`AjustesConexion`: modo e identidad, versión 10). Desde el change `pestana-plataformas` el mando va **siempre** dentro del rodillo en `ConexionZwiftRide` (no se puede desactivar ni ofrecer por Bluetooth aparte) y no se emulan las marchas virtuales de Zwift (change 13c descartado): lo que se emula del protocolo de Zwift es lo necesario para conectar rodillo y mando/Square por wifi. En `ConexionZwiftRide`, el servicio `FC82` va dentro del rodillo DirCon, sin servidor GATT ni anuncio Bluetooth, con identidad propia (clave `zwift/zwift-ride/…` en `EstadoGuardado.identidades`). Zwift manda `SET` con pendiente, Crr, CW y potencia ERG; la app los contesta (`40 08 00 10 <msgId>`), simula el terreno con su marcha y su modelo e ignora la relación de marcha de Zwift. La potencia y la cadencia van a Zwift con `TrainerNotification` por ASYNC (~4 Hz): sin ellas Zwift ve 0 W. El modo se guarda en `Configuracion` versión 6.

**Validado (27-10-2026, OnePlus 8, Neo, Square y Zwift en PC):** conecta en los dos modos, se generan watts y el Square gira y mueve el control. **A validar con el equipo real:** frenar, cambiar de marcha en los tres modelos, entrenamiento ERG, cambiar de modo rodando, cerrar Zwift y volver, latencia de la palanca (NF1) y 10 minutos sin pulsar nada.

**Nombre «KICKR BIKE PRO» (`simular-kickr-bike`, 27-10-2026):** con el ajuste activado, Zwift en PC lista el rodillo como «KICKR BIKE PRO …». **A validar:** si desaparece el aviso de marchas virtuales y si Zwift pide las páginas de rodillo (512, 528, 544), desactivar el ajuste sin emparejar de nuevo, activarlo rodando y la latencia (NF1).

## 8. Conexiones (F1)

Cada cliente (`SquareClient`, `NeoClient`) da el estado del enlace (`EstadoEnlace`: `Conectando`, `Descubriendo`, `Listo`, `Perdida`, `Fallo`). La política de conexión es de la `Sesion`, que la traduce en lo que ve el usuario (`EstadoDispositivo`):

```
           al abrir o al volver el Bluetooth             pérdida
SinConexion ─────────────▶ Conectando ─▶ Conectado ─────────▶ Reconectando
     │  (con configurado, permiso y Bluetooth)  ▲                      │
     │                                        └──────────────────────┘
     └─ «Buscar» (por tipo) ─▶ Buscando ─▶ Conectando ─▶ Conectado
                                     └─ sin resultado en 30 s ─▶ NoEncontrado
```

- Los dispositivos se recuerdan por dirección y nombre en `estado.json` (§ 10, F1.2), uno de cada tipo, al llegar a `Listo`; una búsqueda que conecta con otro lo sustituye.
- Cada intento con un configurado tiene dos pasos: una conexión directa (3 reintentos, 15 s), que recupera un dispositivo despierto en 1–3 s, y, si falla, `autoConnect` sin límite de tiempo, que conecta en cuanto el dispositivo se anuncia, también con la pantalla apagada y sin que la app busque. Mientras dura, no se reintenta nada más. Con Nordic, `autoConnect` se pide sin su conexión directa previa (`useAutoConnect(true, false)`) y después de que se cierre el enlace de la directa fallida: si no, la petición se pierde.
- Se intenta al abrir la app (con permiso y Bluetooth), al perder un dispositivo y al volver el Bluetooth. Con el Bluetooth apagado, todo queda `SinConexion` y sin intentos. Tras una búsqueda sin resultado no se vuelve a intentar sola hasta que el Bluetooth se apague y se encienda o se abra otra vez la app.
- **Cancelar un intento (change `endurecer-dircon-ble`).** `Sesion.cancelarIntento` cancela el trabajo del intento, espera a que termine y desconecta con un límite de 10 s. Si se agota, `ClienteBle.cancelarConexionPendiente()` llama a `cancelPendingConnection()` y `cancelQueue()`; no se usa `close()` del gestor de Nordic, porque en el OnePlus 8 lo deja inservible (las conexiones siguientes no emiten nada y el Square queda en «Conectando» hasta reiniciar la app). Validado: Bluetooth apagado y encendido con el Square dormido, y «Olvidar» más «Buscar», conectan sin reiniciar. **Sin ejercitar en el equipo real:** la propia cancelación forzosa, que no llegó a dispararse. Una dirección guardada inválida se comprueba con `BluetoothAdapter.checkBluetoothAddress` y deja el dispositivo en «Sin conexión».
- Una pérdida de conexión no toca el estado del dominio: la marcha se conserva y, al volver a `Listo`, `ControlRodillo` reenvía el comando vigente al Neo (F1.3, criterio de aceptación de F1).
- La última marcha se guarda en `estado.json` 2 s después del último cambio y al terminar (F5.6). Es una sola: la app es para un solo Neo.
- Hacia Zwift, la app no conecta: publica su servidor (anuncio mDNS o BLE) y espera a que Zwift se conecte. Si Zwift se desconecta, el servidor sigue anunciándose; si se pierde el wifi, `DirconServer` vuelve a anunciarse al recuperar la red.
- El estado de cada conexión (Neo, Square, Zwift y mando) forma parte de `Sesion.estado` y se muestra con `ConnectionBar` en Rodaje (un icono por enlace, con el rodillo y el mando de Zwift por separado; `RodajeUiState.enlaces` es una lista en orden fijo con solo los enlaces en uso) y con `ConnectionChip` y el mismo icono de cada enlace (`Enlace`) en Dispositivos y Diagnóstico (F1.4, F11.5). `Enlace.Square` lleva `ic_manillar` y `Enlace.Mando`, el controlador que la app publica, `ic_zwift` como `Enlace.Zwift`; la barra de Rodaje llama «Controlador» al Square en su descripción accesible (`enlace_controlador_square`), y Diagnóstico conserva «Square» y «Mando de Zwift» hasta su refactor (change `renombrar-controlador`). Rodaje deja las barras del sistema a la vista (change `quitar-barra-estado-rodaje`; antes las ocultaba y replicaba la hora, el wifi y la batería con `DeviceStatus`, `AdaptadorEstadoMovil` y `colocarFranja`, ya retirados): `RodajeRuta` solo mantiene la pantalla encendida y el contenido respeta `safeDrawing`, así que el sistema esquiva la cámara. En vertical, la primera fila lleva el estado de la publicación a la izquierda, que pasa a dos líneas antes de empujar nada, y `ConnectionBar` a la derecha; la segunda, el modo con la transmisión y el icono de Ajustes. En horizontal, `BarraSuperior` es un `Layout` de tres columnas: el modo con la transmisión a la izquierda (si no cabe, la transmisión pasa a dos líneas), el estado de la publicación centrado en la pantalla y las conexiones con Ajustes a la derecha; la zona principal empieza justo bajo la línea. Los iconos de `ConnectionBar` van agrupados, con 4 dp entre ellos.
- Los datos que dependen de un equipo desconectado se muestran como «—» (estado ya maquetado en `maquetas/rodaje.html`).

## 9. Interfaz

### 9.1 Del sistema de diseño a Compose

- `design-system/` es la única fuente de estilos. El generador (`scripts/design-system/generar.py`) emite, a partir de `tokens.json`, `Tokens.kt` en el paquete `ui.tema` de `:app`: colores de los dos temas (`rodaje` y `claro`, con los alias resueltos), familias y estilos tipográficos (cifras con `tnum` e interlineado repartido como en CSS), espaciados, radios y tamaños, cada grupo con `porNombre()`. Genera también `res/font` y `res/values/color_ventana.xml` (fondo de la ventana antes de que Compose pinte).
- `Tokens.kt` se guarda en el repositorio y no se edita a mano, igual que `tokens.css`. Gradle no ejecuta Python: `TokensSincronizadosTest` (JVM, `:app`) lee `tokens.json`, compara cada valor y falla nombrando el token que difiere; `tokens.json` es entrada declarada de la tarea de test. `generar.py` solo escribe los derivados de la app si pasa la comprobación de contraste, así que la app nunca recibe un color sin comprobar.
- El tema es propio, `SqTema`, con `CompositionLocal` y los nombres de los tokens (`SqTema.colores.onGearFill`). Los roles de Material 3 (`primary`, `surface`…) no encajan con los tokens; Material 3 se usa solo como base técnica (ripple, accesibilidad) y sus colores no aparecen en las pantallas.
- El tema lo elige el usuario en Ajustes > Pantalla («Oscuro», el tema `rodaje`, por defecto, o «Claro»; change `acceso-ajustes-y-tema`) y se aplica a toda la app, Rodaje incluida. Se guarda como `tema` en `configuracion.json` (§ 10). `AppGraph.tema` lo expone como `StateFlow<TemaApp?>`, `null` hasta la primera lectura, y `MainActivity` lo pasa a la raíz con `SqTemaElegido`, que no compone nada mientras es `null`: al abrir la app solo se ve la ventana con `color_ventana.xml` (el `bg` de rodaje) y ninguna pantalla sale en un tema para cambiar al otro. En cada cambio, `MainActivity` pone el `bg` del tema como fondo de la ventana y vuelve a llamar a `enableEdgeToEdge` con barras del sistema claras u oscuras. Como `AppGraph` vive con la aplicación, al girar el móvil la actividad nueva ya conoce el tema y lo aplica antes de `setContent`.
- Las fuentes se distribuyen en `design-system/fonts/` en WOFF2 para la web y en TTF para Android, que no lee WOFF2; son la misma versión y `generar.py` copia los TTF a `res/font`. Al añadir un peso, se añaden los dos ficheros.
- Los iconos de `icons.svg` también son derivados: `generar.py` escribe cada uno como *vector drawable* (`res/drawable/ic_<nombre>.xml`, viewport de 960 y el dibujo desplazado por el `viewBox` que empieza en −960), y `IconosSincronizadosTest` falla si no coinciden con `icons.svg`. En Compose se pintan con `painterResource` y se tiñen con el color del contexto.
- La identidad de la app (changes `identidad-app` e `identidad-bici-rodillo`) sale también de `generar.py`, a partir de `design-system/identidad/icono-app.svg` (una bici de perfil: cuadro, ruedas, radios, buje y tambor; el generador lee `rect`, `circle` y `path` con `M`/`L`) y de su capa monocroma `icono-app-monocromo.svg`: `res/drawable/identidad_icono_primer_plano.xml`, `identidad_icono_monocromo.xml`, `identidad_splash_vector.xml` (la rueda trasera va en un `group` «rueda» con su centro de pivote) e `identidad_splash.xml` (`animated-vector` que anima `rotation` de 0 a 720° en 900 ms con `accelerate_quad`: termina en la posición del icono) y los iconos adaptativos `res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml`, con `@color/ventana_bg` de fondo. El generador falla si una forma sale de la zona segura (círculo de 66 dp) e `IdentidadSincronizadaTest` regenera los recursos en un directorio temporal (`generar.py --identidad-a`, requiere `python3`) y falla si alguno difiere. El arranque usa `androidx.core:splashscreen`: `MainActivity` tiene el tema `Theme.SmartBike.Splash` (fondo `ventana_bg`, icono animado, `postSplashScreenTheme` = `Theme.SmartBike`) y llama a `installSplashScreen()` antes de `super.onCreate`, sin condición de espera, así que la splash dura solo lo que tarda Rodaje. Es siempre oscura, también con el tema «Claro», porque va antes de leer la configuración. La pestaña Botones usa `ic_botones`. El Square lleva `ic_manillar`, un manillar de bici de carretera dibujado a medida en `icons.svg` (un solo trazado relleno: barra, dos bajadas en curva y puente corto; el hueco de «esperando» recorta su esquina inferior derecha) e `ic_sports_esports` se retiró del sprite (change `renombrar-controlador`).
- La compilación de depuración incluye también la «Prueba del mando» (`app/src/debug/.../prototipo/`, change `prototipo-mando-zwift`), que publica un mando Zwift con variantes del protocolo y botones en pantalla; tampoco es *launcher*.
- La compilación de depuración incluye la «Muestra del tema» (`app/src/debug/`, `MuestraTemaActivity`), que reproduce las secciones de color y tipografía de la guía en los dos temas. No es *launcher*: se abre con `adb shell am start` (`desarrollo.md` § 5).
- Cada componente `sq-*` tiene su composable con el mismo nombre (`GearDisplay`, `CassetteView`, `MetricGrid`, `ConnectionBar`, `ConnectionChip`, `ActionToast`…), en el paquete `ui.componentes`.

- **Idioma (change `idioma-es-en`).** `AppGraph.idioma` es `null` hasta leer `configuracion.json` y hasta que `fijarIdiomaInicial` guarda el del móvil (español si es `es`, inglés en cualquier otro caso); desde entonces no se vuelve a mirar el del móvil. `MainActivity` no compone nada hasta conocerlo, como con el tema. `IdiomaElegido` envuelve el contenido en un `ContextWrapper` de la actividad cuyos recursos salen de `createConfigurationContext` con el `Locale` elegido y da el mismo `LocalConfiguration`: `stringResource` cambia al momento y la actividad se sigue encontrando a través del contenedor. No se usa `AppCompatDelegate` (añade appcompat, exige `AppCompatActivity` por debajo de Android 13 y deja el estado en el sistema). Un archivo de copia sin `idioma` conserva el de la app al importarse. Fuera de la composición solo lee textos `RideService` (canal y notificación), que se rehacen al cambiar el idioma. Los formatos de números, fechas y tamaños reciben el `Locale` (`localeActual()` en la composición, `Configuracion.idioma` en `datosDeRodaje`); los meses de las sesiones son fijos por idioma. Los diálogos del sistema (selector de archivos, permisos) siguen el idioma del móvil. Los tests de instrumentación con `createComposeRuleEs()` pintan en español sea cual sea el móvil.

### 9.2 Pantallas

| Pantalla | Maqueta | Funcionalidades |
|---|---|---|
| Rodaje (horizontal y vertical) | `rodaje.html`, `rodaje-vertical.html` | F11 (con la guía de inicio, F11.12: hoja `GuideSheet` de cuatro páginas que flota sobre Rodaje y tira minimizada en el pie; su estado vive en `Sesion` y se ve en `RodajeUiState.guia`) |
| Dispositivos | `dispositivos.html` | F1, F12.1 |
| Marchas | `marchas.html` | F5, F12.3 |
| Botones y asignación | `botones.html`, `asignar.html` | F3, F4, F12.4 |
| Datos en pantalla | `datos.html` | F11.3, F12.7 |
| Ajustes | `ajustes.html` | F12 |
| Diagnóstico | `diagnostico.html` | F13 |

- Cada pantalla tiene un `ViewModel` que combina `Sesion.estado` y `Configuracion` en un estado de UI inmutable. Los composables son funciones sin estado de ese modelo, y se prueban con los mismos estados que el selector de las maquetas (`data-mock-state`).
- Rodaje no fija la orientación: la fija el ajuste «Orientación» (Ajustes > Pantalla; change `orientacion-pantalla`) desde `MainActivity` con `requestedOrientation`, en `onCreate` si ya se conoce y en `OrientacionElegida` en cada cambio. «Según el móvil» (`UNSPECIFIED`, por defecto) gira con el móvil y respeta el bloqueo de rotación del sistema; «Vertical» y «Horizontal» usan `SENSOR_PORTRAIT` y `SENSOR_LANDSCAPE`, que no dependen de ese bloqueo y siguen el sentido del móvil. Android puede ignorar la petición en pantallas grandes o ventanas libres, y la app seguiría girando con el móvil. `Rodaje` elige la disposición por el espacio disponible (`BoxWithConstraints`: vertical si el alto es mayor que el ancho), no por `LocalConfiguration`, así funciona igual en pantalla dividida y en los tests. Girar recrea la `Activity`; la `Sesion` y los `ViewModel` sobreviven.
- En Rodaje, la marcha, la potencia objetivo en ERG y cada dato de `MetricGrid` van centrados en horizontal en su zona, en las dos orientaciones (change `erg-manual`).
- La pantalla de rodaje **no muestra el modelo de resistencia**: pendiente de Zwift, ajuste por marcha y lo enviado al Neo van solo a Diagnóstico (F11.2).
- Los efectos puntuales (aviso de acción, tope alcanzado, vibración) son eventos de un `SharedFlow`, no estado, para que no se repitan al recomponer o girar. La vibración la lanza la `Sesion` (funciona con la pantalla apagada); el aviso visual, la UI.
- La captura de botones (F4.3): mientras Asignar acción está abierta, la `Sesion` no ejecuta acciones y publica en `captura` la entrada pulsada (botón con su Campagnolo, o un Campagnolo solo al soltarlo); el gesto lo elige el usuario.
- Navegación (change `rodaje-completo-y-ajustes`): barra inferior (`BottomNav`) con Rodar, Conexión, Dispositivos y Ajustes (change `pestana-conexion`; antes Plataformas, y Botones ya no es pestaña desde `pestana-plataformas`). Las cuatro son destinos de nivel superior del mismo `NavHost`, en `AppNavegacion`, sin transiciones animadas y navegando siempre en el hilo principal. Ir a una pestaña navega con `popUpTo(RUTA_RODAJE)` y `launchSingleTop`: la pila es `[rodaje]` o `[rodaje, pestaña, secundaria…]`, así que Atrás en una pestaña vuelve a Rodaje y Atrás en Rodaje pide confirmación y termina la sesión con un segundo Atrás en 3 s (`ConfirmacionSalida`, que usa `RodajeViewModel`: `Ninguna`, `Confirmar`, `Cerrando`; se cancela al salir de Rodaje; el aviso es `ExitNotice`, flotante y sin mover nada). Rodaje es inmersiva, sin barra del sistema (su hora, wifi y batería los pinta la app, `DeviceStatus`); cada pantalla de pestaña pinta su `BottomNav` al pie.
- Secundarias: Botones, Marchas, Datos en pantalla y Diagnóstico se abren desde Ajustes y vuelven a Ajustes; Asignar acción, desde Botones (y su pestaña de origen es también Ajustes). La pestaña de cada destino y su vecina salen de funciones puras (`Pestana.de`, `Pestana.vecina`). La barra de conexiones de Rodaje abre Dispositivos y el estado de la publicación de Rodaje (`PlatformBadge`), Conexión. Pantalla anterior y siguiente recorren Rodar, Conexión, Dispositivos y Ajustes.
- Conexión (change `pestana-conexion`, antes Plataformas): `ui/conexion` con `ConexionUiState` (`estadoDeConexion`), `ConexionViewModel` y `Conexion`. Lee y escribe `Configuracion.conexion` (modo e identidad) y lee de `EstadoRodaje` el estado de la publicación (`EstadoPlataforma`, `EstadoMando`, `usoRodillo`, `usoPulso`, `servicios`) y de los dispositivos. El estado de cada servicio (`ServiceRow`: rodillo, mando y pulso) lo calcula la UI a partir de eso: por ejemplo, el rodillo está «Conectado» solo si la app de entrenamiento se ha suscrito a su telemetría. Los modos salen de la **tabla de compatibilidad** de `:core` (`Compatibilidad.modos`: por modo, `ModoPublicado` con su enlace, dónde va el mando, si se elige el nombre, el recomendado y lo probado con cada app), así que no hay combinaciones inválidas. La `Sesion` aplica los cambios al guardarse, también rodando. En Rodaje, la barra de conexiones (`ConnectionBar`) lleva solo Neo, Square, pulsómetro y ventilador, y el estado de la publicación (`PlataformaUi`, calculado en `plataformaUi`, sin nombre de plataforma y con «· sin mando» solo en «Wifi + Bluetooth») va en la fila de Ajustes con el modo en la fila de debajo; en horizontal, el modo va en la esquina de la zona principal, de modo que los datos empiezan bajo la barra superior.
- Datos en pantalla ordena las filas con `detectDragGesturesAfterLongPress` sobre el asa y, para el lector de pantalla, con las acciones «Subir» y «Bajar» (`customActions`), sin bibliotecas.
- `MetricGrid` mide la etiqueta y la cifra con la escala de letra del móvil y toma la mayor que cabe en su celda, hasta 144 sp. Cada dato se mide con su valor y con su plantilla (su valor más largo, `plantillaDato`), así la cifra no cambia mientras se rueda (change `rejilla-datos-fija`).
- En Rodaje, la rejilla tiene un área fija: el 40 % del alto útil en vertical y del ancho útil en horizontal. `medirRejilla` da la cifra y el mínimo que necesita la rejilla con ella; con los botones − y +, la rejilla les cede lo que le sobra hasta ese mínimo y el resto lo pierde la marcha. La marcha, los platos o la potencia objetivo van en `Encajar`, un `Layout` que los mide a su tamaño natural y los reduce con `graphicsLayer` si no caben (change `rejilla-datos-fija`).

## 10. Persistencia y configuración

| Dato | Almacén | Notas |
|---|---|---|
| Ajustes (transmisión, modelo, asignación, tiempos de gestos, datos en pantalla…) | `configuracion.json` en DataStore, con `version` | Es el mismo documento que se exporta e importa (F12.6). Versión 2 (`transmisiones-de-marca`): `transmision` (identificador de la elegida), `lineal` (marchas, referencia, neutra y asignación), `platos` (por desarrollo: modo, referencia y asignación), `botones` (tiempos y autorrepetición, comunes) y `bajadas`. La versión 1 se migra al leer: su referencia, su neutra y su asignación pasan a `lineal`, y `marchaInicial` desaparece (se empieza en la referencia). Versión 3 (`erg-manual`): al leer una anterior, la lineal y cada desarrollo guardado reciben la regla △ larga → `AlternarErgManual` si esa entrada y gesto están libres, una sola vez. Versión 4 (`rodaje-completo-y-ajustes`): `datos` (lista de `DatoRodaje`, normalizada al leer: sin repetidos, como mucho 6, vacía → potencia y cadencia, valores desconocidos descartados), `botonesEnPantalla`, `pesoCiclistaKg` (recortado a 40–150) y `pendienteBasePct` (recortada a −5,0–15,0); al leer una anterior, la lineal y cada desarrollo reciben Campag. I + cruceta ← / → → `PantallaAnterior` / `PantallaSiguiente` si están libres, una sola vez. Versión 8 (`pestana-plataformas`): `plataforma` (`zwift`) e `identidad` (`smart-bike` o `kickr-bike-pro`) sustituyen al interruptor `simularKickrBike`; al leer una anterior se migra en `Configuracion.deJson` (y en la importación de copias) con `identidad = kickr-bike-pro` si `simularKickrBike` era verdadero, conservando `conexionZwift` y `mandoZwift` y las identidades de `estado.json`, así que Zwift reconoce el dispositivo sin emparejarlo de nuevo; `normalizada()` fuerza que el modo esté entre los de la plataforma. Versión 5 (`mando-zwift-emulado`): `mandoZwift` («Mando de Zwift», activado; un documento sin él lo lee activado) y reglas `zwift` (`Accion.Zwift` con su `FuncionZwift`); al leer una anterior, la lineal y cada desarrollo reciben las doce funciones de Zwift con «pulsar» en sus botones si están libres, una sola vez. Sin cambiar la versión (como `bajadas`), `modelo` (`simple`, `tabla` o `fisico`), `dificultadPct`, `potenciaMinimaW`, `pesoBiciKg` y `tablas` (change `modelos-tabla-y-fisico`; ausentes, valen sus valores por defecto, y los valores fuera de límites o una tabla con otro número de marchas se recortan o descartan al leer), y `tema` (`oscuro` o `claro`, change `acceso-ajustes-y-tema`): un documento sin él se lee en oscuro; y `orientacion` (`movil`, `vertical` u `horizontal`, change `orientacion-pantalla`): un documento sin él se lee «según el móvil». Versión 10 (`pestana-conexion`): `conexion` (`AjustesConexion`: `modo` e `identidad`) sustituye a `plataforma` y `plataformas`, y desaparecen `mandoZwift` y `pendienteBasePct`; al leer una anterior (y al importar una copia) se conserva todo lo que no es de conexión y la conexión toma sus valores por defecto, sin migrar (hay que emparejar de nuevo). La `Sesion` lo observa y aplica los cambios en caliente, también la transmisión y el modo. Ilegible → valores por defecto; un campo de primer nivel que no se entiende → su valor por defecto, conservando los demás (lo mismo en `estado.json`), con una copia del documento original en `<archivo>.ilegible` (fuera de la copia de seguridad) y un suceso «Almacén: …» en el registro; transmisión desconocida → lineal; reglas con acciones desconocidas se descartan. Desde `modo-tactil-rodaje`, `modoTactil` («Cambiar de modo tocando», desactivado por defecto), opcional y sin cambio de versión: un documento sin él se lee desactivado; se exporta e importa con el resto, y `RodajeViewModel.alternarModo()` envía `Accion.AlternarErgManual` por el mismo camino que las palancas. Desde `nombre-bluetooth-propio`, `conexion.nombrePorBluetooth` (casilla «Usar el nombre también por Bluetooth», desmarcada por defecto), opcional y sin cambio de versión: un documento sin él se lee desmarcado; se exporta e importa con el resto. |
| Dispositivos configurados | `estado.json` en DataStore | Dirección y nombre del Neo y del Square (`rodillo` y `mando`, desde la versión 2; la versión 1 solo tenía `direccionRodillo` y se migra al leerla). Depende del móvil: no se exporta. «Olvidar» en Dispositivos (`Sesion.olvidar`) pone el campo a `null`, cancela el intento en curso y desconecta, sin cambio de versión |
| Identidad del puente con Zwift | `estado.json` en DataStore | `puenteZwift` (dirección y número de serie del anuncio DirCon), opcional: se genera la primera vez. Sin cambio de versión, porque los documentos de la versión 2 sin él se leen igual |
| Última marcha | `estado.json` en DataStore | F5.6. `ultimaMarcha` (la lineal) y `ultimasMarchas` (plato y piñón por desarrollo, opcional y sin cambio de versión). Se escribe a menudo: va aparte para no reescribir la configuración en cada cambio |
| Ventilador configurado | `estado.json` en DataStore | `ventilador` (dirección y nombre), opcional y sin cambio de versión, como `puenteZwift`. Modo, velocidad y umbrales no se guardan |
| Nombre Bluetooth del móvil pendiente de recuperar | `estado.json` en DataStore | `nombreBluetoothOriginal` y `nombreBluetoothPuesto`, opcionales y sin cambio de versión: el nombre del móvil antes de renombrarlo y el que puso la app. Se borran al recuperarlo; si el móvil ya no se llama como el puesto, el usuario lo cambió y no se toca. No se exporta |
| Guía de inicio | `estado.json` en DataStore | F11.12. `guiaInicio` (`visible`, `descartada` o `finalizada`; ausente = aún no evaluada), opcional y sin cambio de versión; no se exporta. `Sesion` la evalúa al arrancar (con algún dispositivo configurado, `finalizada`; sin ninguno, `visible`), la pasa a `finalizada` cuando los dispositivos están emparejados y hay una app conectada, y a `descartada` con «No volver a mostrar»; «Guía de inicio» de Ajustes la deja `visible`. La página, abierta o minimizada y la equis viven solo en memoria (`GuiaPantalla`). Los pasos los calcula `PasosGuia`, una función pura de `:core` |
| Última potencia del ERG manual | `estado.json` en DataStore | F7.2. `ultimaPotenciaErgW`, opcional y sin cambio de versión; se guarda como la marcha, al rato de cambiarla y al terminar |
| Sesiones grabadas | `files/sesiones/sesion-AAAAMMDD-HHmmss.txt` en el almacenamiento interno de la app | Texto plano, un archivo por sesión (F13.4, § 11). Se abren y comparten con `FileProvider` (autoridad `${applicationId}.sesiones`, solo esa carpeta) y la hoja de compartir de Android. No entran en la copia de seguridad de la configuración ni en la de Android. Se conservan las 20 más recientes (`ArchivoSesion.aBorrar`, change `preparar-release`). Los ajustes `grabarSesiones` y `espacioLibreMinimoPct` están en `configuracion.json` (versión 11, sin migración) |

- Exportar e importar (change `exportar-bateria-y-giro`): se exporta `configuracion.json` tal cual (`Configuracion.aJson`), sin dispositivos ni estado. `Configuracion.validar` es la lectura estricta: motivo si no es válido, y si lo es, migra y recorta como `deJson`; importar rodando lo aplica en caliente la `Sesion`.
- Los dos documentos son JSON tipados con `kotlinx.serialization`; sus modelos viven en `:core` y no dependen de DataStore.
- El documento de configuración lleva un número de versión y una función de migración por versión, probada con ficheros de ejemplo de cada versión anterior.
- Al importar se valida el documento entero antes de aplicarlo; si no es válido, no se cambia nada y se explica el motivo.
- Copia de seguridad de Android (change `preparar-release`): `allowBackup` con `res/xml/reglas_copia.xml` (copia de Google y transferencia entre móviles, Android 12 o superior) y `res/xml/copia_completa.xml` (Android 9 a 11). Las dos incluyen solo `datastore/configuracion.json` (`Almacen.RUTA_CONFIGURACION`), lo mismo que se exporta; `estado.json` y `sesiones/` quedan fuera, así que al restaurar no vuelven los dispositivos, las identidades ante la plataforma, las últimas marchas ni el nombre Bluetooth original. Lo comprueba `ReglasCopiaTest`.
- Los valores por defecto viven en el dominio (no en la UI) y reproducen `funcional.md`: transmisión lineal de 24 marchas, modelo simple, asignación del anexo A, «Recordar la última marcha» activado.

## 11. Diagnóstico y registro (F13)

- `RegistroSesion` es un búfer circular en memoria (del orden de 10 000 entradas) con tramas BLE y DirCon en hexadecimal en los dos sentidos, gestos, acciones, cambios de modo y comandos al Neo, cada una con marca de tiempo monotónica.
- La pantalla de Diagnóstico lo lee en vivo (F13.1, F13.3): las secciones de datos (conexión, botones, simulación), siempre; el estado de cada conexión y sus avisos viven en Dispositivos y en Conexión, y los avisos de conexión comunes están en `ui/dispositivos/AvisosConexion.kt`; el volcado de tramas, solo con «Tráfico en directo» marcada. Entonces muestrea el registro como mucho 10 veces por segundo, forma las filas fuera del hilo principal y las pinta en una lista perezosa; con la casilla desmarcada no lo sigue. Además, permite enviar al Neo una pendiente o una potencia a mano (F13.2), que entra como un modo de control más y se cancela al salir de la pantalla.
- Guardar el registro (F13.4, change `registro-sesiones`): `RegistroSesion` tiene un sumidero opcional (`sumidero`, asignable en caliente) que recibe solo los sucesos (`Evt`) junto al `espejo`, sin tocar a los ~100 emisores. `GrabadorSesion` (`datos/`) formatea cada suceso, lo mete en una cola acotada (`DROP_OLDEST`, con una línea `# se descartaron N sucesos`) y una corrutina de IO lo escribe y vacía como mucho cada segundo, así que el hilo de control nunca espera al disco. Su estado (`EstadoGrabacion`: apagada, grabando, detenida por espacio, detenida por error, sin espacio) es un `StateFlow` que leen Ajustes y la pantalla de sesiones.
- `AppGraph` ata el grabador con `atarGrabador` a «Grabar sesiones» × sesión en curso: graba mientras haya las dos y abre un archivo nuevo cada vez que se activa. El formato (cabecera `# …`, línea `HH:mm:ss.SSS ORIGEN texto`, líneas de parada) y `hayEspacio(libre, total, minimoPct)` son funciones puras de `:core` (`ArchivoSesion`). El espacio (`StatFs` sobre `filesDir`) se comprueba al empezar y cada 30 s; por debajo del mínimo la grabación se detiene sola y no se reanuda. Para liberar espacio la app no borra nada; solo, al empezar a grabar, las sesiones más antiguas que pasen de 20 (`ArchivoSesion.aBorrar`, change `preparar-release`), anotando cada borrado en el registro.
- `RepositorioSesiones` lista `files/sesiones/` (la más reciente primero, con tamaño y estado) y borra una o todas, respetando la que se graba. `ui/sesiones/CompartirSesion` crea los `Intent` de abrir (`ACTION_VIEW`) y compartir (`ACTION_SEND`) sobre la `Uri` del `FileProvider`; sin permisos nuevos.
- El registro sirve también de fuente para los tests: una sesión real grabada se reproduce contra el dominio con reloj virtual.

## 12. Permisos y manifiesto

| Permiso | Para qué |
|---|---|
| `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE` | Android 12 o superior. Se piden juntos con «Buscar» en Dispositivos; `BLUETOOTH_ADVERTISE` (para el mando de Zwift) se pide también al volver a la app si faltaba solo él, y sin él el mando queda «Sin conexión» y lo demás funciona |
| `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION` | Android 11 o inferior (`maxSdkVersion="30"`) |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` | `RideService` (tipo `connectedDevice`, Android 14 o superior) |
| `POST_NOTIFICATIONS` | Notificación del servicio (Android 13 o superior) |
| `VIBRATE` | Vibración por gesto y tope (F3.6, F5.4) |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `WAKE_LOCK` | Servidor DirCon en la red local, anuncio mDNS y `WifiLock` |

`INTERNET` es necesario para cualquier socket, también en la red local; la app no contacta con servidores externos (NF5). La app declara `android.hardware.bluetooth_le` como requerida.

Compilación release (change `preparar-release`): R8 y `shrinkResources` sin ofuscar (`app/proguard-rules.pro`, `-dontobfuscate`, para que las trazas del registro se lean sin `mapping.txt`); firma con una clave propia (v2 y v3) leída de propiedades de Gradle o del entorno, sin firmar si falta; `versionName` y `versionCode` desde `-PetiquetaVersion=vX.Y.Z` (`X·1 000 000 + Y·1 000 + Z`; sin ella, `0.0.0-dev` y 1). `.github/workflows/release.yml` publica la Release de GitHub con cada etiqueta en `main`. Detalles en `desarrollo.md` § 7.

Versiones futuras de Android piden un permiso para acceder a la red local según el `targetSdk`; con el actual (36) no aplica. Al subirlo, revisar el servidor DirCon y el anuncio mDNS.

## 13. Pruebas

| Nivel | Qué cubre | Cómo |
|---|---|---|
| Unitarias de `protocolo` (`:core`) | Codificación y decodificación de cada trama | Vectores de bytes, incluidos los capturados del equipo real en la fase 0 |
| Unitarias de `dominio` (`:core`) | Gestos, asignación, cambio y topes, modelos, modos de control, protecciones | JUnit con reloj virtual; trazas grabadas del Square reproducidas contra el reconocedor |
| `Sesion` | Orquestación, reconexión, latencia interna | Dobles de prueba de `SquareClient`, `NeoClient` y `TransporteDircon` detrás de interfaces |
| DirCon | Servidor TCP y mensajes | Cliente DirCon de prueba en la JVM contra `DirconServer`, además de Zwift real |
| UI | Estados de cada pantalla | Compose UI Test con los estados de las maquetas |
| Con el equipo real | Lo marcado **a validar**, criterios de aceptación de `funcional.md`, latencia NF1 | Lista de comprobación por fase; nRF Connect como cliente o servidor de referencia; registro de sesión adjunto al change |

Las tareas de test van junto a la tarea que implementan, no al final (`openspec/config.yaml`).

## 14. Correspondencia con las fases

Qué piezas construye cada change, y en qué orden, está en [`planificacion.md`](planificacion.md).

## 15. Decisiones abiertas

Además de las de `funcional.md` § 8:

1. Cuánto se mantiene el último terreno de Zwift tras una desconexión antes de volver a la pendiente base (§ 5.4): 30 s, provisional hasta la prueba de desconexión pendiente de `puente-zwift-wifi`.
2. ~~Frecuencia de notificación de telemetría hacia Zwift~~: **resuelta** en `puente-zwift-wifi`, 4 Hz (§ 5.2).
3. Si hace falta la exención de optimización de batería (§ 4.4). La reconexión con la pantalla apagada funciona sin ella; falta la prueba de 2 h, pendiente al archivar `puente-zwift-wifi`.
4. ~~Si Zwift acepta el mando emulado por DirCon o solo por Bluetooth~~: **resuelta** y ampliada en `mando-zwift-dircon` (27-10-2026): hay dos modos, el de siempre y «Todo por wifi» con el mando dentro del rodillo, ambos validados con Zwift en PC (detalle en § 7.4). Antes: en `prototipo-mando-zwift` y revisada en `mando-zwift-emulado`: por DirCon funciona solo como único dispositivo de la IP o dentro del rodillo con las marchas virtuales de Zwift, así que el mando va por Bluetooth (§ 7.4).
5. Las constantes del modelo físico (`ProteccionesFisico`, la relación de la lineal y los 30 W de potencia mínima), provisionales hasta la prueba con el Neo del change `modelos-tabla-y-fisico`.
