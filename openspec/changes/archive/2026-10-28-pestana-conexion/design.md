# Design

## Context

Ver `proposal.md` (Why). Punto de partida en el código:

- **Dominio (`:core`).**
  - `core/.../dominio/Plataforma.kt` define `Plataforma` (solo `ZWIFT`), `ModoConexion` (`FTMS_WIFI`, `ZWIFT_RIDE`, `FTMS_BLUETOOTH`), `MandoEnModo` (`NINGUNO`, `BLUETOOTH_OPCIONAL`, `DENTRO_DEL_MODO`), `AjustesPlataforma(modo, mando, identidad)` y la tabla `Compatibilidad` por plataforma.
  - `Configuracion` (v9) guarda los ajustes por plataforma.
  - `EstadoGuardado` (`estado.json`) guarda los dispositivos recordados (`rodillo`, `mando`, `pulsometro`, `ventilador`) y las identidades por `ClaveIdentidad` (plataforma, modo, nombre).
- **Publicación (`:app`, `plataforma/`).**
  - `PuentePlataforma` monta una `ConexionPlataforma`: `ConexionFtms` con `TransporteDircon` o `TransporteBluetooth`, o `ConexionZwiftRide`.
  - Con `BLUETOOTH_OPCIONAL`, monta además un `MandoZwiftBluetooth` aparte.
  - `PuenteFtms` ofrece siempre FTMS, Cycling Power, Heart Rate (`180D`, decisión 4 de `pulso-zwift`) y Device Information.
  - `MandoZwiftEnServidor` añade `FC82` al mismo servidor en los modos con el mando dentro.
  - El estado se publica como `EstadoPlataforma` (un solo estado de conexión) y `EstadoMando` (con `Desactivado`).
- **UI.**
  - `ui/plataformas/` (`Plataformas.kt`, `PlataformasUiState.kt`, `PlataformasViewModel.kt`).
  - Dispositivos tiene «Buscar dispositivos» para el Neo y el Square y botones propios para el pulsómetro y el ventilador.
  - Rodaje muestra `PlatformStatus` con el nombre de la plataforma.

## Goals / Non-Goals

**Goals:**
- Que lo publicado se derive de dos datos: el modo elegido y los dispositivos recordados. Nada más decide qué se anuncia.
- Un estado por servicio calculado a partir de lo que la app de entrenamiento usa de verdad.
- Quitar del dominio todo lo que dependía de la plataforma, sin dejar código muerto.

**Non-Goals:**
- Cambiar los protocolos o transportes que ya funcionan, más allá de ofrecer u omitir servicios.
- Un modelo de «perfil de plataforma» para el futuro: si alguna plataforma necesita un ajuste propio, se decidirá en su change.

## Decisions

### 1. Fuera `Plataforma`: `AjustesConexion` en la raíz de `Configuracion` (v10)

`Configuracion` pasa de `plataforma` y `plataformas: Map<Plataforma, AjustesPlataforma>` a un único `conexion: AjustesConexion(modo, identidad)`. Por defecto: `FTMS_WIFI` y `SMART_BIKE`.

- La versión sube a 10. Al leer una v9 o anterior se conserva todo lo que no es de conexión y la conexión toma los valores por defecto, sin migración: es lo mismo que se hizo en el 24 y lo que aceptó el usuario. La copia de seguridad sigue la misma regla.
- `ModoConexion` conserva sus nombres serializados (`ftms-wifi`, `zwift-ride`, `ftms-bluetooth`). Lo que cambia es solo el texto de la UI («Wifi + Bluetooth», «Solo wifi (modo Zwift Ride)», «Solo Bluetooth»).

**Alternativa:** mantener `Plataforma` con un único valor y ocultarlo. Se descarta porque deja una abstracción sin uso que vuelve a invitar a guardar por plataforma.

### 2. Compatibilidad por modo

`Compatibilidad` pasa a ser una lista fija de `ModoPublicado(modo, enlace, mando: MandoEnModo, nombreElegible, recomendado, probadoCon: List<Prueba(app: String, validacion)>, otrasFtmsAValidar: Boolean)`.

- `MandoEnModo` queda en `NINGUNO`, `BLUETOOTH_APARTE` y `DENTRO_DEL_MODO`. Desaparece la opcionalidad: no hay interruptor.
- `probadoCon` es texto de presentación con su nivel de validación. No es un enum de plataformas, porque solo se usa para mostrarse.

**Alternativa:** un enum `AppEntrenamiento`. Se descarta porque volvería a dar a la plataforma un papel en la lógica.

### 3. Lo publicado = f(modo, recordados)

Una función pura en `:core`, `Publicacion.de(modo, recordados): ServiciosPublicados?`, devuelve `null` sin rodillo recordado y, si hay rodillo, devuelve:
- `rodillo` (siempre);
- `pulso` (si hay pulsómetro);
- `mando`: `APARTE_BT`, `DENTRO` o ninguno, según el modo y si hay Square.

`PuentePlataforma` observa los recordados de `EstadoGuardado` junto con la configuración, y vuelve a montar la conexión **solo cuando cambia `ServiciosPublicados`**. No vuelve a montarla cuando cambia el estado de conexión de un dispositivo. Esto aplica la regla «configurado = se publica; la conexión solo cambia el estado» y evita que el anuncio se encienda y se apague cuando el Square se duerme.

- `PuenteFtms` recibe `pulso: Boolean` y solo añade `180D` si es `true`. Esto deshace la decisión 4 de `pulso-zwift`.
- `MandoZwiftEnServidor` solo se añade con `mando == DENTRO`.
- `MandoZwiftBluetooth` solo se monta con `mando == APARTE_BT`.

**Alternativa:** dejar los servicios siempre y ocultarlos en la UI. Se descarta: así la pantalla diría «No se ofrece» de algo que sí se ofrece. Queda solo como plan B de las validaciones V2 y V3.

### 4. Volver a anunciarse al cambiar los servicios

Si cambia la lista de servicios de un dispositivo DirCon, la app cierra la conexión y lo vuelve a anunciar (el TXT de mDNS con los nuevos UUID). Lo mismo hace con el anuncio BLE en «Solo Bluetooth». Es el mismo camino que «Cambio de modo o de nombre con la sesión abierta».

Se mantiene la misma identidad, la de (modo, nombre), para que la plataforma pueda reconocer el dispositivo. Si no lo reconoce por haber cambiado sus servicios, hay que volver a emparejarlo. Eso se comprueba en V2 y se avisa en la ayuda.

**Alternativa:** una identidad nueva por cada combinación de servicios. Se descarta porque obligaría a emparejar de nuevo siempre.

### 5. Identidades por (modo, nombre), sin conservar las anteriores

`ClaveIdentidad` pierde la plataforma. Las identidades guardadas con la clave anterior se ignoran y se crean nuevas, así que hay que emparejar de nuevo una vez, como al actualizar en el 24.

**Alternativa:** reescribir las claves antiguas quitando `zwift`, que evita emparejar de nuevo. Se descarta por coherencia con la regla de no migrar que aceptó el usuario, y porque solo hay un usuario. Es fácil de añadir si se quisiera.

### 6. Estado por servicio a partir de las suscripciones

Se conservan `EstadoPlataforma` (el transporte del rodillo, con el nuevo `NoSeOfrece`) y `EstadoMando` (el saludo del mando). `PuentePlataforma` añade en cada ciclo de telemetría si la app de entrenamiento usa el rodillo (`suscrito`) y el pulso (`suscritoPulso`), que la `Sesion` guarda como `usoRodillo` y `usoPulso`. El estado de cada servicio de esta tabla lo calculan funciones puras de la UI (`estadoDeConexion` y `plataformaUi`), sin un tipo `EstadoServicios` aparte: así no se duplica el estado del transporte. (Ajustado al implementar.)

**Rodillo**

| Estado | Cuándo |
|---|---|
| `NoSeOfrece` | Sin rodillo recordado. |
| `SinTransporte(Wifi \| Bluetooth)` | Falta el transporte del modo. |
| `NoDisponible` | El móvil no puede anunciarse como dispositivo Bluetooth. |
| `NoVisible` | Lo decide la sonda mDNS que ya existe. |
| `Esperando` | Se publica y la app de entrenamiento no lo usa. |
| `Conectado` | La app de entrenamiento está suscrita a Indoor Bike Data o a Cycling Power, o ha escrito en el Control Point. Coincide con `PuenteFtms.suscrito`. |

En `Esperando` y `Conectado` se añade una nota si el Neo se está reconectando.

**Mando**

| Estado | Cuándo |
|---|---|
| `NoSeOfrece` | Sin Square recordado. |
| `SinTransporte` | Falta el transporte por el que va. |
| `NoDisponible` | El móvil no puede anunciarse como dispositivo Bluetooth. |
| `Esperando` | Se publica y la app de entrenamiento no lo ha saludado. |
| `Conectado` | Tras el saludo de `FC82`. |

En `Esperando` y `Conectado` se añade una nota si el Square se está reconectando.

**Pulso**

| Estado | Cuándo |
|---|---|
| `NoSeOfrece` | Sin pulsómetro recordado. |
| `Esperando` | No hay suscripción a `2A37`. |
| `SinLectura` | Hay suscripción y `PulsoVigente` es `null`. |
| `Enviando` | Hay suscripción y pulso. |

Desaparece `EstadoMando.Desactivado`. Rodaje calcula su línea con una función pura de `EstadoServicios`, que se prueba sin Android.

**Alternativa:** un estado por conexión TCP o GATT, la opción A de la exploración. Se descarta porque no muestra el fallo típico de emparejar solo parte de los servicios.

### 7. UI: `ui/conexion/`

`ui/plataformas/` pasa a ser `ui/conexion/` (`Conexion.kt`, `ConexionUiState.kt`, `ConexionViewModel.kt`). La ruta y la `Pestana` también se renombran (`Pestana.CONEXION`), y el orden de la barra queda Rodar, Conexión, Dispositivos, Ajustes.

Componentes del sistema de diseño, primero en `design-system/`:
- **`ServiceRow`:** la fila de servicio, nueva (`sq-svc`), con «No se ofrece» como enlace a Dispositivos.
- **`ModeList`:** la lista de modos con etiquetas de compatibilidad (`sq-compat`). Sustituye a `PlatformPicker` y se quita `sq-platform`.
- **`PlatformBadge`:** se conserva el componente de Rodaje, con el mismo chip, sin nombre de plataforma ni transporte. Se mantiene el nombre para no renombrar sin necesidad el composable de la app.

La pestaña conserva su icono actual.

Dispositivos pasa a tener cuatro secciones con la misma estructura: fila, «Buscar» y «Olvidar». La búsqueda conjunta del Neo y el Square se divide en `buscarRodillo()` y `buscarMando()`, que reutilizan el escáner actual con un solo criterio cada una. «Buscar» solo aparece sin dispositivo recordado; con uno recordado, «Olvidar» y, si no está conectado, «Reconectar», que reinicia en el momento el intento con ese dispositivo por su dirección (no por nombre) sin cambiar la reconexión automática ilimitada (decidido por el usuario al revisar la maqueta).

### 8. Validaciones con el equipo real, al principio de la etapa 2

Se hacen con el OnePlus 8, el Neo, el Square, la banda HW9 y Zwift en PC, antes de implementar la decisión 3 en firme:

| Validación | Qué se comprueba | Si falla |
|---|---|---|
| **V1. Suscripciones** | Con el registro de sesión, cuándo se suscribe Zwift a Indoor Bike Data, Cycling Power, `2A37` y `FC82`: al conectar o al elegir el sensor. | Si se suscribe a todo al conectar, la decisión 6 se queda igual. Solo cambia que los estados se parecerán más entre sí. |
| **V2. Pulso añadido** | Con «Smart Bike» emparejado sin pulso, recordar la banda y ver si Zwift lista el pulso: tras reconectar, tras volver a abrir Zwift o solo tras olvidar el dispositivo y emparejarlo de nuevo. | Si exige olvidarlo, la ayuda lo dice. Si ni así lo ve, plan B: publicar el pulso siempre y mostrar «Sin pulsómetro» en lugar de «No se ofrece». |
| **V3. Zwift Ride sin mando** | **Resuelta al implementar, leyendo el código:** en este modo el terreno y el ERG llegan como `SET` por la característica del mando (`ConexionZwiftRide.escrituraDeZwift`), así que sin `FC82` el rodillo no se controla. | Se aplica la alternativa: el modo publica siempre el mando (`ModoPublicado.mandoSiempre`), sin botones si no hay Square. |
| **V4. Otras apps** | El experimento de «Otras plataformas» de `planificacion.md` con MyWhoosh o Rouvy, en los modos «Wifi + Bluetooth» y «Solo Bluetooth». | No hay fallo posible: el resultado solo añade entradas a `probadoCon`. Si una app pide algo propio, se anota para otro change. |

Resultado parcial de V1 (28-10-2026, «Solo Bluetooth» con el dispositivo ya recordado): Zwift se suscribe a todo al conectar, a Indoor Bike Data, Cycling Power, `2A37` y `FC82`. Falta verlo al emparejar por primera vez.

### 9. «Solo Bluetooth» con el mando dentro atiende también el protocolo de Zwift

Descubierto en la prueba con el equipo (28-10-2026): con el rodillo y el mando en un solo dispositivo Bluetooth, Zwift puede tratarlo como un Zwift Ride. Se suscribe a todo (Indoor Bike Data, Cycling Power, `2A37` y `FC82`), pero manda la pendiente como `SET` por SYNC_RX del mando y nunca escribe en el punto de control FTMS. Además, muestra su propia marcha y toma la potencia y la cadencia del mensaje de pedaleo de Zwift por ASYNC. En la validación de `ftms-bluetooth`, esa misma mañana, Zwift usó FTMS. Quien decide es Zwift, y desde la app no se ve por qué elige un camino u otro. Hipótesis del usuario, a validar:
- La primera vez se empareja primero el rodillo y después el mando, y Zwift usa FTMS.
- Con el dispositivo ya recordado, Zwift se reconecta a todo a la vez. Si ve el mando antes de tomar el rodillo como FTMS, lo trata como un Zwift Ride.

Encaja con el registro de la prueba: tras suscribirse, el handshake del mando llega medio segundo después, y desde entonces solo hay `SET`.

Prueba con esta decisión implementada (28-10-2026): con solo el rodillo emparejado, Zwift usa FTMS, no muestra marchas y ve la potencia. Al emparejar también el mando, muestra su marcha, pasa a su protocolo (la app envía el pedaleo por el mando) y sigue viendo la potencia. Lo que decide parece ser emparejar el mando del mismo dispositivo, más que el orden o que Zwift lo recuerde. En una subida, los `SET` llegan con la pendiente (2,16 %, 2,26 %, 2,36 %…) y el Neo se endurece.

- `ConexionFtms`, con el mando dentro, atiende los dos caminos. Los comandos FTMS siguen igual. Los `SET` se atienden como en el modo Zwift Ride: terreno o ERG a la `Sesion` y `SetResponse` por SYNC_TX.
- Desde el primer `SET` de un cliente, la telemetría sale también por ASYNC (`PedaleoZwiftCodec`). Si Zwift usa FTMS, no se envía nada que no pida.
- La atención de los `SET` (`SetsDeZwift`) se saca de `ConexionZwiftRide` y la comparten los dos modos.
- El detalle del modo avisa de que, con el mando, la app de entrenamiento puede mostrar su propia marcha.

**Alternativas:**
- Cambiar lo que contesta el mando para que Zwift no lo tome por un Zwift Ride. Se descarta por ahora: es un experimento sin garantías y podría romper el mando.
- Exigir un orden de emparejamiento. Se descarta: no se sabe si es la causa, y no se puede pedir al usuario.

## Risks / Trade-offs

- **Volver a emparejar** → Hay que hacerlo al actualizar (identidades nuevas) y quizá al recordar u olvidar un pulsómetro o un Square (V2). Se avisa en la ayuda de Conexión y en la de Dispositivos al recordar.
- **Quien quiera el Square directamente en Zwift pierde el interruptor** → Basta con no emparejar el mando de la app en Zwift. Además se quita el aviso de botones que Zwift leería (spec `button-mapping`).
- **«Sin rodillo» no publica nada** → Se pierde el uso de la app como puente solo de mando y pulso, que queda fuera de alcance. El aviso de Conexión lo explica.
- **Textos genéricos** → «la app de entrenamiento» suena menos directo que «Zwift». Las etiquetas de compatibilidad y las ayudas de emparejamiento siguen nombrando Zwift donde aplica.
- **«· sin mando» solo con «Wifi + Bluetooth»** → Decidido por el usuario al validar la maqueta: con el mando dentro del mismo dispositivo («Solo Bluetooth», «Solo wifi») no se muestra, como hasta ahora. El estado del mando sigue visible en Conexión.
- **Requisitos no tocados que siguen diciendo «Plataformas» o los nombres antiguos de los modos** (`diagnostics`, `zwift-controller` «Mando reconocido por Zwift», `ftms-bluetooth` «Mismo comportamiento»…) → Antes de archivar, se buscan y se añaden deltas de solo texto (tarea de documentación).

## Migration Plan

- `Configuracion` v10 sin migrar la conexión, y `ClaveIdentidad` sin plataforma. Al actualizar, la app arranca con «Wifi + Bluetooth» y «Smart Bike», y hay que emparejar en Zwift una vez.
- Los dispositivos recordados no cambian: `estado.json` conserva `rodillo`, `mando`, `pulsometro` y `ventilador`.
- **Marcha atrás:** volver a la versión anterior. Leería la v10 como ilegible y arrancaría con sus valores por defecto, conservando lo demás como hace con cualquier versión desconocida. Habría que emparejar de nuevo.

## Open Questions

- El texto exacto de la introducción y de las ayudas se ajusta sobre la maqueta. La idea de partida es: «Tu app de entrenamiento (Zwift, MyWhoosh, Rouvy…) se conecta a Smart Bike: la app publica estos servicios».
