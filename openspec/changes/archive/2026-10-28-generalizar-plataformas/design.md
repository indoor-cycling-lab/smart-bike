# Design

## Context

Ver `proposal.md` (por qué y qué). Estado actual, tal como lo he leído en el código:

- `ConexionZwift` (interfaz) mezcla lo genérico (anunciarse, estado, telemetría, pulso) con lo propio de Zwift (`pulsar(FuncionZwift)`, `soltar`, `giro`, `mandoActivado`). Tiene dos implementaciones: `ConexionZwiftFtms` (DirCon + FTMS, más el mando de Zwift por Bluetooth que ella misma publica y para según el ajuste, el Bluetooth, los permisos y el modo periférico) y `ConexionZwiftRide` (todo por DirCon, con `MandoZwift` dentro).
- `Sesion.kt` (1.805 líneas) tiene el puente a mano: `usarConexionZwift`, `wifiCambia`, `zwiftCambia`, `telemetriaAZwift`, `receptorZwift`, `zwiftPerdido`, `actualizarConexionZwift`, la identidad (`identidad`, `ultimas`) y las funciones del mando (`funcionZwift`, `pulsarFuncion`, `soltarZwift`).
- `Configuracion` (versión 8) guarda `plataforma`, `identidad`, `conexionZwift` y `mandoZwift` sueltos; `EstadoGuardado` guarda cuatro identidades fijas (`puenteZwift`, `puenteZwiftRide`, `puenteKickr`, `puenteKickrRide`), una por modo y por nombre.
- `Compatibilidad` (en `:core`) ya liga plataforma, modos y mando, pero con `ModoConexionZwift` y solo Zwift.
- `Accion.Zwift(FuncionZwift)` es la acción asignable de las funciones del mando.
- `DirconServer`, `AnuncioNsd`, `SondaMdns`, `FtmsCodec` y `DirconCodec` ya son genéricos y solo cambian de paquete.
- La app no la usa nadie más que el autor: no hay datos que conservar.

## Goals / Non-Goals

**Goals:**
- Que añadir una plataforma o un modo (change 11 y siguientes) sea añadir una entrada a la tabla de compatibilidad y, como mucho, una conexión nueva, sin tocar `Sesion` ni los nombres existentes.
- Que lo propio de Zwift quede identificable por su paquete y su nombre, y lo genérico no nombre a Zwift.
- Sacar el puente de `Sesion` sin cambiar el comportamiento observable: las specs `platform-bridge` y `zwift-ride` recogen lo que ya hacía `zwift-bridge`.

**Non-Goals:**
- Ningún protocolo ni plataforma nuevos, ni UI nueva.
- Que `DirconServer` atienda a varios clientes a la vez.
- Mover `SimulacionZwift` y los codecs de Zwift Ride a un modelo «propio de cada plataforma» más abstracto: se quedan como están, en `plataforma/zwift/`.

## Decisions

### 1. Dominio: el modo es solo transporte y perfil; el mando lo dice la tabla

```
Plataforma      ZWIFT (hoy)                    @SerialName("zwift")
ModoConexion    FTMS_WIFI  "ftms-wifi"         DirCon + FTMS
                ZWIFT_RIDE "zwift-ride"        DirCon + protocolo de Zwift
                (FTMS_BLUETOOTH lo añade el change 11)
MandoEnModo     NINGUNO | BLUETOOTH_OPCIONAL | DENTRO_DEL_MODO
ModoPlataforma(modo, mando, recomendado, validacion)
Compatibilidad  Plataforma -> List<ModoPlataforma>
AjustesPlataforma(modo, mando: Boolean, identidad: Identidad)
```

`ModoConexionZwift.RODILLO_Y_MANDO_BLUETOOTH` decía «rodillo y mando por Bluetooth», pero el mando ya es un interruptor aparte. Pasa a `FTMS_WIFI`, y el mando lo decide la tabla: para Zwift, `BLUETOOTH_OPCIONAL` en `FTMS_WIFI` y `DENTRO_DEL_MODO` en `ZWIFT_RIDE`; una plataforma sin mando tendrá `NINGUNO`. `Configuracion.mandoActivo` se deriva de ahí (`NINGUNO` → no, `DENTRO_DEL_MODO` → sí, `BLUETOOTH_OPCIONAL` → el interruptor).

*Alternativa descartada:* un `enum` de transportes (`DIRCON`, `BLE`) más otro de perfiles. Con solo tres modos reales, la combinación sigue siendo corta, y la tabla ya impide las inválidas.

### 2. `Configuracion` guarda los ajustes por plataforma, sin migración

`Configuracion` pasa a la versión 9 con `plataforma` (la elegida) y `plataformas: Map<Plataforma, AjustesPlataforma>`. Los ajustes de la plataforma elegida se leen con `ajustesPlataforma` (los de la tabla por defecto si no hay entrada). `normalizada()` fuerza, por plataforma, que el modo esté entre los suyos. Se eliminan `conexionZwift`, `mandoZwift`, `identidad` sueltos y `migrarV7`. Una configuración de la versión 8 se lee igual (`ignoreUnknownKeys` ya está activo): se conserva todo lo que no es de la plataforma y los ajustes de la plataforma toman los valores por defecto. Lo mismo vale para importar una copia.

*Alternativa descartada:* seguir con los campos sueltos y añadir el mapa solo en el change 11. Habría que migrar entonces, con datos de verdad.

### 3. Las identidades de dispositivo, en un mapa con clave `(plataforma, modo, nombre)`

`PuenteZwift` pasa a `IdentidadDispositivo(mac, serie)`. `EstadoGuardado` sustituye las cuatro propiedades por `identidades: Map<String, IdentidadDispositivo>` con clave `"zwift/ftms-wifi/smart-bike"`, construida por una función de `ClaveIdentidad`. `EstadoGuardado` sube a la versión 3 sin migración: las identidades antiguas se ignoran y se crean nuevas (hay que emparejar de nuevo en Zwift una vez).

La plataforma entra en la clave para que cada una cachee la suya y una no contamine a otra.

### 4. La conexión: lo común en una interfaz, el mando como pieza opcional

```
interface ConexionPlataforma {
  plataforma, modo, nombre(identidad)
  estado: StateFlow<EstadoTransporte>
  suscrito
  publicar(identidad) / retirar() / terminar()
  telemetria(t) / pulso(bpm)
  val mando: MandoEmulado?          // null si el modo no tiene mando
}
interface MandoEmulado {
  estado: StateFlow<EstadoMando>
  activar(Boolean)
  pulsar(f: FuncionMando) / soltar(f) / soltarTodo()
  giro: Set<LadoGiro>
}
```

Piezas, en `plataforma/` (genérico) y `plataforma/zwift/` (propio de Zwift):

```
plataforma/                          plataforma/zwift/
  ConexionPlataforma, MandoEmulado     MandoZwift (+ MandoZwiftCodec, FC82)
  ConexionFtmsWifi                     MandoZwiftBluetooth  (implementa MandoEmulado)
  ReceptorPlataforma                   ConexionZwiftRide    (su mando va dentro)
  PuenteFtms (antes ZwiftBridge)       SimulacionZwiftCodec, PedaleoZwiftCodec
  DirconServer, AnuncioNsd, SondaMdns
  TransporteDircon, EstadoTransporte
```

- `ConexionFtmsWifi` es la parte de `ConexionZwiftFtms` que no es del mando: el servidor GATT FTMS por DirCon, la telemetría y el pulso. Recibe el `MandoEmulado?` ya construido.
- `MandoZwiftBluetooth` se lleva la lógica de `actualizarMando` (ajuste, Bluetooth, permisos, modo periférico, publicar y parar) y el transporte BLE propio del mando.
- `ConexionZwiftRide` sigue siendo una sola clase propia de Zwift, y su `mando` es un adaptador de `MandoZwift` en modo `EN_RODILLO`.
- `FuncionZwift` pasa a implementar `FuncionMando` (`sealed interface FuncionMando { val plataforma: Plataforma }`) y `Accion.Zwift(funcion)` pasa a `Accion.Mando(funcion: FuncionMando)`. Un mando de otra plataforma ignora las funciones que no son suyas.

*Alternativa descartada:* dejar el mando dentro de `ConexionZwiftFtms` y hacer una `ConexionFtmsWifi` aparte para las demás plataformas. Duplicaría el servidor FTMS y la telemetría.

### 5. `PuentePlataforma` sale de `Sesion`

Una clase en `app/plataforma/` que recibe el ámbito, el registro, el wifi, la fábrica de conexiones y un `DestinoPlataforma` (una interfaz que implementa `Sesion` con `terreno`, `potenciaErg`, `sinCambio`, `ultimo` y `perdida`). Se lleva:

- usar una conexión nueva cuando cambian los ajustes (soltar, terminar la anterior, crear la nueva con su ámbito, buscar o crear su identidad, publicar si hay wifi);
- reaccionar al wifi y al estado del transporte, y calcular `EstadoPlataforma` (`SinWifi`, `Esperando`, `NoVisible`, `Conectado`);
- el bucle de telemetría a 4 Hz, que recibe del `Sesion` una función con la potencia, la cadencia y la biela;
- pulso, `pulsar`, `soltar`, `soltarTodo` y `giro`, que delegan en `conexion.mando`.

`Sesion` conserva lo que toca al Neo y al control: `entregarDePlataforma`, `plataformaPerdida`, la espera de 30 s, la ventana del terreno y la asignación de botones. Expone `estado.plataforma` en vez de `estado.zwift` y sus campos de modo y nombre.

*Alternativa descartada:* extraer también el control de la plataforma perdida. Está acoplado al Neo y a `ControlRodillo`; moverlo no mejora la frontera.

### 6. Nombres

Se renombra en cuatro grupos, con búsqueda y compilación tras cada uno:

| De | A |
|---|---|
| `ModoConexionZwift` | `ModoConexion` |
| `EstadoZwift` | `EstadoPlataforma` |
| `ReceptorZwift`, `UltimoDeZwift` | `ReceptorPlataforma`, `UltimoDePlataforma` |
| `ConexionZwift`, `TransporteZwift` | `ConexionPlataforma`, `TransporteDircon` |
| `PuenteZwift` | `IdentidadDispositivo` |
| `ZwiftBridge` (el servidor GATT con FTMS que usan los dos modos) | `PuenteFtms` |
| `ControlRodillo`: `SimZwift`, `ErgZwift`, `ZwiftManda`, `zwiftEnEspera`, `zwiftPerdido`, `zwiftAusente`, `terrenoZwift`, `potenciaZwift` | `SimPlataforma`, `ErgPlataforma`, `PlataformaManda`, `plataformaEnEspera`, `plataformaPerdida`, `plataformaAusente`, `terrenoPlataforma`, `potenciaPlataforma` |
| `Origen.Zwift` | `Origen.Plataforma` |
| `Configuracion.mandoZwift`, `conexionZwift`, `nombreZwift`, `EstadoMandoZwift` (la máquina del mando) | dentro de `AjustesPlataforma`; `nombreAnunciado`; `EstadoMandoZwift` se queda |

Se quedan con su nombre, por ser de Zwift: `FuncionZwift`, `MandoZwift`, `MandoZwiftCodec`, `SimulacionZwift`, `SimulacionZwiftCodec`, `PedaleoZwiftCodec`, `ConexionZwiftRide`, `EstadoMandoZwift`. `UltimoDePlataforma` conserva una variante `Simulacion(SimulacionZwift)` mientras ninguna otra plataforma tenga algo parecido.

### 7. Textos

El nombre de la plataforma sale de `Plataforma.nombre`, con recursos de texto con un marcador (`Mando de %1$s`, `Conectado a %1$s`). Los textos que describen un modo (`conexion_zwift_mixto`, `conexion_zwift_todo_wifi` y sus ayudas) pasan a `modo_ftms_wifi_*` y `modo_zwift_ride_*`, porque cada modo sabe a qué plataforma sirve. El origen «Zwift» del tráfico de Diagnóstico se pinta con el nombre de la plataforma elegida; con Zwift, el texto no cambia. Nada de esto altera lo que ve el usuario hoy, por eso no pasa por maqueta.

### 8. Orden de trabajo

Cada paso compila y pasa sus tests antes del siguiente (`:core:test`, `:app:testDebugUnitTest`, `:app:assembleDebug` y `:app:compileDebugAndroidTestKotlin`):

1. Dominio y `Configuracion`/`EstadoGuardado` (decisiones 1 a 3), con sus tests.
2. Interfaces y conexiones (decisión 4) y `PuentePlataforma` (decisión 5), con sus tests de `ConexionFtmsWifi`, `MandoZwiftBluetooth`, `ConexionZwiftRide` y `Sesion`.
3. Nombres, paquetes, textos y UI (decisiones 6 y 7).
4. Specs aplicadas y documentación.

## Risks / Trade-offs

- **Un renombre grande puede romper algo que los tests no ven** (textos, orden de las filas, claves de registro) → tests de las filas de Plataformas y Rodaje que ya existen, comparación de la salida de Diagnóstico antes y después y prueba del usuario con Zwift en el PC.
- **Perder las identidades emparejadas** → intencionado: hay que emparejar de nuevo en Zwift una vez; queda dicho en la propuesta y en la spec.
- **Condiciones de carrera al mover el puente fuera de `Sesion`** (todo corre en el hilo de control, y la telemetría y el wifi tienen sus propios colectores) → el `PuentePlataforma` recibe el mismo ámbito y el mismo dispatcher, y los tests de `Sesion` con ámbito de test cubren el cambio de conexión, la plataforma perdida y la telemetría.
- **`UltimoDePlataforma.Simulacion(SimulacionZwift)` filtra un tipo de Zwift al genérico** → aceptado y anotado: se resuelve cuando otra plataforma lo necesite.
- **Cambio de modo en caliente con Zwift abierto** → ya conocido y fuera de alcance (`DirconServer` atiende a un solo cliente); este change no lo empeora ni lo arregla.

## Validación con el equipo real

La hace el usuario con Zwift en el PC, tras emparejar de nuevo una vez: «Rodillo por wifi (FTMS)» (rodillo, potencia, cadencia, pulso, mando por Bluetooth, ERG y terreno) y Zwift Ride (mando dentro, terreno y ERG), y comprobar que cambiar de modo y de nombre rodando se comporta como en el change 23. Nada nuevo queda «a validar».
