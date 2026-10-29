# Desarrollo, despliegue y pruebas

> Estado: **borrador para revisión**. El proyecto Android existe desde el change `tema-compose-desde-tokens`; los comandos del § 5 están comprobados con el OnePlus 8.

Cómo se compila la app, cómo se instala en el móvil de pruebas y qué hacen los agentes y qué hace el usuario en cada prueba. La arquitectura está en [`arquitectura.md`](arquitectura.md) y el orden del trabajo, en [`planificacion.md`](planificacion.md).

## 1. Entorno

Todo el desarrollo se hace en el Mac del usuario. Los agentes compilan, instalan y prueban desde la terminal; el usuario puede abrir el mismo proyecto en Android Studio cuando le sea útil (vistas previas de Compose, inspector de interfaz, perfilador).

| Pieza | Valor |
|---|---|
| Equipo | Mac con Apple Silicon |
| Android Studio y SDK | `~/Library/Android/sdk` (build-tools, platforms, platform-tools) |
| JDK | 21 |
| Compilación | Gradle Wrapper del proyecto (`./gradlew`, Gradle 9.8.2); `sdk.dir` en `local.properties`, que no se sube al repositorio. Versiones fijadas en `gradle/libs.versions.toml`: AGP 9.4.1, Kotlin 2.4.21, Compose BOM 2026.09.00, JUnit 6 |
| API de Android | `minSdk 28`, `targetSdk 36` y `compileSdk 37`: Compose 1.12 exige compilar contra la API 37, y AGP descarga la plataforma al SDK la primera vez |
| Móvil de pruebas | OnePlus 8 del usuario (IN2013), con Android 13 (SDK 33), comprobado el 25-10-2026. **Es su móvil personal** (§ 4) |
| Zwift | PC del usuario, en la misma red wifi que el móvil |
| Repositorio | [`indoor-cycling-lab/smart-bike`](https://github.com/indoor-cycling-lab/smart-bike), público desde el 29-10-2026; se trabaja en un clon suyo en el Mac. El historial anterior a la publicación está en una copia privada (`planificacion.md`) |

Descartados:

- **Emulador de Android.** En macOS no tiene Bluetooth real: no ve al Neo ni al Square. Solo sirve para revisar pantallas sueltas.
- **Entornos en la nube** (Claude Code web, integración continua). Pueden compilar y pasar los tests de `:core`, pero no llegan al móvil ni al Bluetooth. Más adelante pueden servir de comprobación automática en cada push.

## 2. Conexión con el móvil: depuración inalámbrica

El móvil va en el soporte del rodillo, así que se usa la depuración inalámbrica de Android 11 o superior. El cable USB queda como alternativa.

**Una sola vez (el usuario):**

1. En el móvil: Ajustes → Opciones de desarrollador → Depuración inalámbrica → activar.
2. «Vincular dispositivo con código de vinculación»: el móvil muestra una IP, un puerto y un código.
3. En la terminal de Claude Code: `! adb pair <ip>:<puerto>` e introducir el código. El agente no puede vincular (§ 4).

**En cada sesión (el agente):**

1. `python3 .claude/skills/desplegar-movil/buscar_movil.py` para encontrar el móvil en la red. Usa `dns-sd`, el mDNS de macOS, porque `adb mdns services` no siempre ve el anuncio del móvil, y si no lo encuentra, prueba los puertos de la última IP conocida (guardada en `movil.local`, fuera del repositorio). El puerto cambia al reiniciar el móvil o al desactivar y activar la depuración.
2. `adb connect <ip>:<puerto>` y `adb devices -l` para comprobarlo.
3. Si hay más de un dispositivo conectado, el agente pregunta cuál usar y lo indica con `-s` en cada comando.

El móvil y el Mac deben estar en la misma red. La depuración inalámbrica se desactiva sola al cambiar de red. Con una VPN activa en el móvil, la pantalla de depuración inalámbrica puede mostrar la IP de la VPN, a la que el Mac no llega; el script encuentra la de la wifi.

## 3. Qué hace cada uno

| Tarea | Agente | Usuario |
|---|---|---|
| Compilar y pasar los tests de `:core` y `:app` | ✓ | |
| Instalar la compilación de depuración y abrirla | ✓ | |
| Conceder los permisos de la app (`pm grant`) | ✓ | |
| Leer el registro de la app (`logcat` filtrado) y descargar el registro de sesión | ✓ | |
| Capturas de pantalla y pruebas de interfaz en el móvil | ✓ | |
| Vincular el móvil con el Mac | | ✓ |
| Encender el Neo, pedalear, pulsar los botones del Square | | ✓ |
| Emparejar la app en Zwift desde el PC | | ✓ |
| Aprobar cada validación con el equipo real | | ✓ |

**Sesión de validación guiada.** Para lo marcado **a validar** en `arquitectura.md`, el agente instala la versión, abre Diagnóstico y lee el registro en directo; el usuario hace lo que el agente le pide (pulsar cada botón, pedalear a una cadencia, cambiar de marcha) y el agente contrasta lo recibido con lo esperado. El resultado se anota en el change.

## 4. Restricciones de `adb`

El OnePlus 8 es el móvil personal del usuario. `adb` da control total del dispositivo, así que los agentes solo pueden tocar la app del proyecto.

**Cómo se aplica:** un hook `PreToolUse` (`.claude/hooks/adb_guard.py`, registrado en `.claude/settings.json`) revisa cada comando antes de ejecutarlo, también dentro de `sh -c`, `eval` y `$(…)`, y bloquea lo que no esté en la lista. Funciona en cualquier modo de permisos, incluido el modo bypass, y si el script fallara, el comando se bloquea. Las herramientas MCP con acceso por `adb` están bloqueadas todas, por el hook y por reglas `deny`.

**Identificador de la app:** `app.smartbike`. La compilación de depuración añade `.debug`, así que convive con una versión instalada sin sobrescribirla.

| Permitido | Ejemplos |
|---|---|
| Conexión | `adb devices`, `adb mdns services`, `adb connect`, `adb disconnect` |
| Instalar y desinstalar la app | `adb install` de un APK de `build/outputs/apk/` del proyecto; `adb uninstall` de la app; `./gradlew installDebug` |
| Abrir, parar y dar permisos a la app | `adb shell am start` / `force-stop` / `instrument`, `adb shell pm grant` / `revoke` / `clear` con la app |
| Datos de la app | `adb shell run-as <app> …`, `adb exec-out run-as <app> …`, `adb pull` desde `/sdcard/Android/data/<app>/` |
| Registro | `adb logcat` con `--pid=<pid de la app>` o `-s <etiquetas>`; `adb logcat -c` |
| Pantalla | `adb exec-out screencap -p`, `adb shell uiautomator dump`, `adb shell input tap` / `swipe` / `keyevent` |
| Estado del sistema | `adb shell dumpsys bluetooth_manager` / `battery` / `deviceidle` / `power` / `display`, `dumpsys package` / `meminfo` / `gfxinfo` de la app, `getprop`, `settings get`, `wm size` |
| Ficheros temporales | `adb push` a `/data/local/tmp/`, `adb pull` desde ahí |

| Bloqueado | Motivo |
|---|---|
| `adb shell` interactivo o con comandos fuera de la lista (`rm`, `settings put`, `pm uninstall`…) | Acceso libre al sistema |
| Cualquier operación sobre otras apps (desinstalar, listar, abrir, leer datos) | Datos personales |
| `adb logcat` sin filtro y `bugreport` | El registro completo incluye otras apps |
| `adb pull` fuera de los datos de la app, `backup`, `restore` | Datos personales |
| `adb install` de un APK de fuera del proyecto | Software no compilado aquí |
| `adb reboot`, `root`, `remount`, `sideload` | Cambios en el sistema |
| `adb pair` | La vinculación la autoriza el usuario |

Limitación conocida: `input tap`, `swipe` y `keyevent` actúan sobre la app que esté en primer plano. Antes de usarlos, el agente comprueba con una captura que la app del proyecto está delante.

Si un agente necesita algo bloqueado, lo pide al usuario, que puede ejecutarlo con `! <comando>` (esos comandos no pasan por el hook). Ampliar la lista es un cambio del hook y de esta tabla, revisado por el usuario.

## 5. Ciclo habitual

```sh
./gradlew :core:test                      # lógica pura, en segundos
./gradlew :app:testDebugUnitTest            # tests JVM de la app (incluye TokensSincronizadosTest)
./gradlew installDebug                      # compila e instala en el móvil conectado
adb shell am start -n app.smartbike.debug/app.smartbike.MainActivity
adb logcat -d --pid=$(adb shell pidof -s app.smartbike.debug)
adb exec-out screencap -p > captura.png
./gradlew connectedDebugAndroidTest         # tests de interfaz en el móvil
adb shell am start -n app.smartbike.debug/app.smartbike.ui.tema.MuestraTemaActivity   # muestra del tema (solo depuración)
```

- El nombre de la actividad va completo: con el sufijo `.debug`, la forma corta (`…debug/.MainActivity`) busca la clase en `app.smartbike.debug` y falla.
- Los tests de interfaz necesitan la pantalla del móvil encendida y desbloqueada; si se apaga, Compose espera sin fallar y la tarea no termina. Con el móvil cargando, «Pantalla activa» de las opciones de desarrollador lo evita; la activa el usuario, porque el hook bloquea `settings put` (§ 4).

### Textos de la interfaz en dos idiomas

- Cada cadena nueva va en `app/src/main/res/values/strings.xml` (español) y en `values-en/strings.xml` (inglés), con la misma clave y los mismos marcadores de formato (`%1$d`, `%%`…). `./gradlew :app:testDebugUnitTest` ejecuta `TextosInglesTest`, que falla si falta una traducción, sobra una clave o cambian los marcadores.
- Las opciones del selector de idioma («Español», «English») y su título («Idioma · Language») son iguales en los dos archivos.
- Los tests de instrumentación usan `createComposeRuleEs()` (en `androidTest/.../ui/ComposeEspanol.kt`) para pintar en español sea cual sea el idioma del móvil; los que prueban el cambio de idioma usan la regla normal con `IdiomaElegido`.
- Glosario del inglés: marcha → gear, plato → chainring, piñón → cog, rodillo → trainer, controlador → controller, Rodaje → Ride, Ajustes → Settings.

### Estilo del Kotlin (ktlint)

```sh
./gradlew ktlintCheck     # comprueba :app y :core; falla con el fichero, la línea y la regla
./gradlew ktlintFormat    # corrige lo que puede (imports, espacios, saltos de línea)
```

- `ktlintCheck` forma parte de `./gradlew check`, pero no hay integración continua ni hooks de Git: se ejecuta a mano antes de dar una tarea de código por terminada (`AGENTS.md`).
- Si falla, ejecuta `ktlintFormat` y arregla a mano lo que quede. No desactives una regla ni uses `@Suppress` sin una decisión explícita; la configuración está en `.editorconfig`.
- Se excluyen `Tokens.kt` (lo escribe `generar.py` y `TokensSincronizadosTest` exige que coincida) y todo lo de `build/`. Las funciones `@Composable` pueden ir en PascalCase.
- El formateo inicial está en `.git-blame-ignore-revs`. GitHub lo usa solo; en local, `git config blame.ignoreRevsFile .git-blame-ignore-revs`.

La skill `desplegar-movil` (`.claude/skills/desplegar-movil/`) encadena conexión, instalación, apertura y lectura del registro para que todos los agentes lo hagan igual.

### La web pública (`site/`)

La web de GitHub Pages se genera; no se edita a mano `site/es/`, `site/en/`, `site/assets/` ni `site/img/`.

```sh
python3 scripts/sitio/diccionario.py        # maquetas/en.js, el inglés de las maquetas (de strings.xml y maquetas/en.json)
python3 scripts/sitio/capturas.py           # fotografía las maquetas en español e inglés (Chrome y sips o Pillow; solo si cambia la UI)
python3 scripts/sitio/generar.py            # HTML de los dos idiomas, tokens y fuentes
python3 -m unittest scripts/sitio/test_sitio.py
python3 -m http.server 8000                 # y abrir http://localhost:8000/site/
```

- Los textos, en `site/contenido/es.json` y `en.json` (mismas claves); las capturas elegidas, en `site/capturas.json`; los estilos a mano, en `site/sitio.css`, solo con tokens.
- Tras un change de UI, vuelve a ejecutar `capturas.py` y `generar.py`: las capturas salen de las maquetas en `#vista=captura` y se guardan como `site/img/<id>-<idioma>-<tema>.png` (cada página enlaza las de su idioma). El renderizado cambia unos bytes de una pasada a otra: haz commit solo de las capturas de las pantallas que de verdad cambian y descarta el resto (`git checkout site/img/<archivo>`).
- Las maquetas se ven en inglés con `idioma=en` en el hash (`maquetas/rodaje.html#estado=shimano-auto&idioma=en`); sin él, o con `idioma=es`, siguen en español, que es el idioma en el que se validan. `revision.js` sustituye los textos por coincidencia exacta con `maquetas/en.js`, que `diccionario.py` genera con los pares de `values/strings.xml` y `values-en/strings.xml` más `maquetas/en.json` (a mano: `textos` para lo que no sale de la app, `contextos` para un mismo texto con otro sentido según la clase del elemento, `=` para lo que vale igual en los dos idiomas). `en.js` se versiona y `test_sitio.py` falla si está desactualizado: tras tocar `en.json` o un `strings.xml`, ejecuta `diccionario.py`.
- Un texto nuevo en una maqueta que sale en una captura de la web lleva su inglés: `capturas.py` descarga cada captura inglesa y falla, con la lista de textos, si queda texto visible en español. Las cifras llevan el formato inglés (`0.0040`, no `0,0040`). Solo cuentan los estados de `site/capturas.json`; el resto de las maquetas no hace falta traducirlo.
- `capturas.py` usa Google Chrome (o `CHROME=ruta`) y, para recortar, `sips` de macOS o, fuera de macOS, Pillow (`pip install pillow`); sin ninguno de los dos falla antes de empezar. Chrome sin interfaz no baja de 500 px de ancho, por eso las verticales se recortan.
- Se publica con `.github/workflows/pages.yml` al subir cambios de `site/` a `main`; hay que activar Pages con «Source: GitHub Actions».

### Trazas del equipo real

En la compilación de depuración, el registro de sesión copia cada trama y cada suceso al registro del sistema con la etiqueta `SqTrama`, una línea por entrada: `<tMs> <ORIGEN> <SENTIDO> <característica> <hex o texto>` (por ejemplo, `183402117 SQUARE RX 0x43 0a1b2c80020000ff`). `tMs` es el reloj monotónico del móvil en milisegundos. Los registros internos de la biblioteca Bluetooth van con la etiqueta `SqBle`.

```sh
adb logcat -c
adb logcat -s SqTrama                                  # en directo, durante la sesión guiada
adb logcat -d -s SqTrama > core/src/test/resources/trazas/<nombre>.txt   # guardar una traza
```

Las trazas se guardan tal cual, con la cabecera de `logcat`: el lector de los tests de `:core` (`Traza.leer`) la ignora. Cada traza empieza con un comentario (`# …`) que dice qué se hizo durante la captura. Los gestos que reconoce la app van como sucesos `Gesto: …` con su entrada, tipo, acción y resultado; `square-combinaciones.txt` (combinaciones, palanca mantenida, doble y apagado del Square) se reproduce en `CombinacionesTrazaTest`.

La configuración guardada (asignación de botones, tiempos, referencia) se lee con `adb exec-out run-as app.smartbike.debug cat files/datastore/configuracion.json`.

**Sesiones grabadas (F13.4).** Sin el Mac delante, activa «Grabar sesiones» en Ajustes, en «Depuración»: cada sesión se guarda en `files/sesiones/sesion-AAAAMMDD-HHmmss.txt` con solo los sucesos (sin tramas crudas; para esas sigue `logcat`). Desde el móvil se abren y comparten desde «Sesiones grabadas». Desde el Mac, solo con la compilación de depuración:

```sh
adb exec-out run-as app.smartbike.debug ls files/sesiones
adb exec-out run-as app.smartbike.debug cat files/sesiones/<archivo>.txt > <directorio temporal>/sesion.txt
```

El archivo puede incluir direcciones de red y nombres de dispositivos: revísalo antes de compartirlo. Para probar la parada por poco espacio, sube «Espacio libre mínimo» por encima del espacio libre del móvil.

Con el Square conectado, sus notificaciones (unas 5 por segundo y característica) llenan pronto el búfer de `logcat`, y `adb logcat -d` solo devuelve el final de la sesión. Para una prueba de más de un minuto, el agente graba en un fichero desde el principio, en segundo plano, y lo detiene al terminar:

```sh
adb logcat -c
adb logcat -s SqTrama > <directorio temporal>/captura.txt     # en segundo plano durante la prueba
```

**Conexión y reconexión.** Con la compilación de depuración, `SqBle` incluye también los pasos de la conexión de Nordic (`connectGatt(autoConnect = …)`, `Request timed out`, `gatt.close()`), que dicen si un intento en segundo plano llegó a pedirse. Para una prueba de reconexión, el agente graba en segundo plano solo los sucesos (`adb logcat -s SqTrama | grep --line-buffered " EVT "`) y las líneas de Nordic de cada dispositivo (`adb logcat -s SqBle | grep --line-buffered "Square:"`), que no se llenan con las tramas. El usuario apaga y enciende el dispositivo al cambiar el minuto del reloj, para medir del encendido a `Estado: Conectado`. La batería del Square aparece como `Batería: <n> %` al conectar y cada 10 min.

**Prueba de 2 h (NF4).** Con el Mac encendido, en la misma wifi y con las dos grabaciones anteriores en marcha desde el principio: el usuario rueda con la pantalla apagada, sin cargador y con la optimización de batería de la app por defecto, cambia de marcha de vez en cuando y, a la hora, apaga y enciende el Square. Al terminar, el agente comprueba en la grabación que no hay `Sesión terminada` ni cambios de PID (el sistema no cortó el servicio), que el corte acabó en `Estado: Conectado` sin búsqueda y cómo evolucionó la batería del Square.

**Latencia de las palancas (NF1).** Cada cambio de marcha con palanca deja en el registro la línea `Latencia: notificación <t0>, envío <t1>, entrega <t2> (<ms> ms)`, y Diagnóstico muestra la última. Mide de la llegada de la notificación del Square a la entrega del comando al Neo; las líneas `Parámetros de conexión` del Square y del Neo dan el intervalo en cada momento, que acota la parte que no se mide (`arquitectura.md` § 6.3).

El launcher guarda en caché el icono de la app: al probar un cambio de icono, desinstala la app e instálala de nuevo (o reinicia el launcher) antes de dar la prueba por mala. La animación de la splash solo se ve en Android 12 o posterior, y los iconos temáticos en Android 13 o posterior.

`./gradlew connectedDebugAndroidTest` desinstala la app al terminar, y con ella sus datos y permisos: después hay que volver a instalarla con `installDebug`, y la primera apertura vuelve a pedir el permiso de notificaciones. Se pierde también la configuración (transmisión, ajustes): avisa al usuario antes de lanzarlos. Desde la tarde del 27-10-2026 la tarea acaba con código de salida 1 («There were failing tests») aunque todos los tests pasen (233 de 233 en el informe, sin ningún error en el registro de `--info`), también con el código de antes del renombrado a Smart Bike y con o sin filtro de clase; esa mañana, con la misma versión de AGP, acababa en verde. Causa sin identificar: el resultado fiable es el informe de `app/build/reports/androidTests/connected/debug/index.html`, no el código de salida.

**El ventilador Headwind no aparece.** Admite una sola conexión Bluetooth: con otra app de entrenamiento del móvil abierta (por ejemplo, la del fabricante), aunque sea en segundo plano, no se le ve. Antes de probarlo, cierra esa app del todo (`! adb shell am force-stop <paquete>`, si el móvil lo permite, o desde los ajustes de Android).

**El Neo no se conecta o no se anuncia.** El Neo admite una sola conexión Bluetooth, y su luz azul indica que la tiene. Si está encendida con Smart Bike cerrada, lo tiene cogido alguna de las otras apps de entrenamiento del móvil, que a veces vuelven a conectar solas al encender el Bluetooth. El agente no puede cerrarlas (`adb` solo con la app del proyecto); el usuario lo hace con `! adb shell am force-stop <paquete>` y después apaga y enciende el Bluetooth. Cerrar Smart Bike no detiene la sesión: hay que tocar «Terminar» en la notificación.

## 6. Red local y Zwift

- El móvil, el Mac y el PC de Zwift comparten la red wifi: la depuración inalámbrica y la conexión con Zwift por DirCon (`arquitectura.md` § 7.3) son independientes, pero las dos necesitan que el router no aísle a los clientes.
- Para probar el puente sin Zwift se usa un cliente DirCon de prueba en el Mac y, para el transporte Bluetooth, nRF Connect. Para el modo «Solo Bluetooth» sin Zwift, basta con el registro de la app: «BLE: anunciado», los `addService()` de los servicios publicados (FTMS, Cycling Power e información siempre; el pulso con un pulsómetro configurado y el mando `00000001-19ca-…` con un Square configurado) y «BLE: parado» al cambiar de modo o apagar el Bluetooth. Sin rodillo configurado no se publica nada.
- **El volcado de `logcat -d` pierde líneas** con mucha actividad (las del Neo y el Square lo llenan) y no sirve para ver qué hizo la app en un momento dado. Para una prueba guiada, graba en directo desde antes de la acción, con la comprobación de que el proceso existe: `adb logcat -c; adb logcat --pid=$(adb shell pidof -s app.smartbike.debug) > registro.txt &`. Si el `pid` sale vacío, la grabación no empieza (`pid out of range`).
- Con el modo por Bluetooth, Zwift puede no reconocer el dispositivo entre sesiones (`arquitectura.md` § 7.3): si no aparece, hay que quitar el anterior en la pantalla de emparejamiento y buscarlo de nuevo por el nombre Bluetooth del móvil (o por el nombre ante la app, con «Usar el nombre también por Bluetooth»). Con el mando dentro lo ha reconocido (con un nombre como «OnePlus 8 1234»). Para saber si Zwift usa FTMS o su protocolo de Zwift Ride, busca en el registro «Mando: SET» (protocolo de Zwift) o «Plataforma: Simulacion» (FTMS).
- Comprobar el anuncio desde el Mac: `dns-sd -B _wahoo-fitness-tnp._tcp` lista «Smart Bike» y `dns-sd -L "Smart Bike" _wahoo-fitness-tnp._tcp` da el puerto (36866) y los registros TXT. `dns-sd` no termina solo: se corta con Ctrl+C (o con un límite de tiempo si lo lanza el agente).
- Si Zwift no ve «Smart Bike» y la app dice «Esperando», el anuncio puede estar registrado sin responder (`arquitectura.md` § 7.3): `dns-sd -B` desde el Mac no lo lista. Se arregla apagando y encendiendo el wifi del móvil. La app lo detecta sola (Zwift «No visible») y apunta cada comprobación en el registro (`Anuncio: responde en N ms (multicast)`, `Anuncio: sin respuesta…`).
- Para ver el aviso «No visible» sin el fallo real (solo depuración): `adb shell am start -n app.smartbike.debug/app.smartbike.MainActivity --ez sonda_nombre_falso true`. La sonda pregunta por un nombre que no existe; abrir la app sin el extra la devuelve al nombre real, y la siguiente comprobación (como mucho 60 s) quita el aviso.
- Con un pulsómetro configurado, «Smart Bike» aparece en Zwift también en la lista de pulso (potencia, cadencia, resistencia y pulso se reconocen a la vez). Si no sale, olvidarlo en Zwift y emparejar de nuevo.
- Emparejar en Zwift (lo hace el usuario): en la pantalla de emparejamiento, «Smart Bike» con el icono de wifi como potencia, cadencia y rodillo controlable. El Neo no se empareja en Zwift. El Square puede seguir emparejado en Zwift como mando para girar.
- En el registro, todo lo de Zwift va con el origen `ZWIFT`: sucesos (`EVT`: wifi, anuncio, conexión de Zwift, cada mensaje DirCon y cada comando del punto de control) y tramas (`RX 2ad9` lo que escribe Zwift, `TX` lo que se le notifica). Con Zwift conectado, la telemetría (8 tramas por segundo) llena el búfer de `logcat` en un minuto: para una prueba larga, el agente graba en segundo plano solo los sucesos (`adb logcat -s SqTrama | grep --line-buffered " EVT "`).
- **Prueba del mando Zwift** (change `prototipo-mando-zwift`):
  1. Abrir la app normal, que anuncia el rodillo por wifi, y encima la pantalla de prueba con el comando del § 5.
  2. Elegir «Solo mando por Bluetooth» y «Zwift SF2» en la respuesta a la información, y pulsar «Publicar». El permiso `BLUETOOTH_ADVERTISE` se concede en el diálogo o con `pm grant`.
  3. En Zwift, emparejar «Smart Bike» como rodillo y el móvil, con su nombre Bluetooth, como mando.

  Notas:
  - Tras reinstalar o volver a publicar, hay que cerrar y abrir Zwift: Windows conserva el enlace anterior y Zwift se queda en «Conectando…».
  - «Atrás» en la pantalla de prueba deja de anunciar el mando.
  - Para una prueba larga, grabar en segundo plano: `adb logcat -s SqTrama | grep --line-buffered -E "BLE|Mando|19ca"`.
- Para comprobar desde el PC de Zwift que se ve un anuncio BLE: con PowerShell 5 no se pueden recibir los eventos de WinRT. Hay que compilar un pequeño programa en C# con el `csc.exe` de .NET Framework, referenciando `Windows.Devices.winmd` y `Windows.Foundation.winmd`, y borrarlo al terminar.
- Cortar el wifi del móvil corta también la depuración inalámbrica: el agente vuelve a conectar después (skill `desplegar-movil`) y lee lo que quede en el búfer.
- Los tests instrumentados desinstalan la app y borran `estado.json`: después hay que volver a buscar el Neo y el Square, y la identidad del puente cambia (Zwift lo reconoce igual).

## 7. Publicar una versión

Las versiones se publican como Releases de GitHub con el APK firmado (spec `app-distribution`; change `preparar-release`). No hay rama de release: se publica empujando una etiqueta `vX.Y.Z` sobre `main`, y el workflow `.github/workflows/release.yml` hace el resto.

### La clave de firma

Todas las versiones se firman con la misma clave. **Si se pierde, quien tenga la app instalada tendrá que desinstalarla (y perder sus datos) para instalar la siguiente**: no hay forma de recuperarla. Un secret de GitHub no sirve de copia, porque no se puede volver a leer; la copia es la del gestor de contraseñas.

1. Crear la clave (lo hace el usuario: `keytool` pide las contraseñas y no deben pasar por el agente). Fuera del repositorio:

   ```bash
   mkdir -p ~/claves
   keytool -genkeypair -v -storetype PKCS12 -keystore ~/claves/smart-bike.jks -alias smart-bike \
     -keyalg RSA -keysize 4096 -validity 10000
   ```

   Con PKCS12 la contraseña de la clave es la misma que la del almacén.
2. Guardarla en un gestor de contraseñas: una entrada «Smart Bike · clave de firma» con la contraseña del almacén, el alias `smart-bike` y el archivo `smart-bike.jks` adjunto. La copia del gestor debe sobrevivir a la pérdida del Mac.
3. Obtener la huella del certificado:

   ```bash
   keytool -list -v -keystore ~/claves/smart-bike.jks -alias smart-bike | grep SHA256
   ```

### Firmar en local

En `~/.gradle/gradle.properties` (fuera del repositorio):

```properties
RELEASE_KEYSTORE=/Users/<usuario>/claves/smart-bike.jks
RELEASE_KEYSTORE_PASSWORD=…
RELEASE_KEY_ALIAS=smart-bike
RELEASE_KEY_PASSWORD=…
```

Con las cuatro, `./gradlew :app:assembleRelease` genera `app-release.apk` firmado y `./gradlew :app:installRelease` lo instala junto a la de depuración (otro identificador, `app.smartbike`). Sin ellas, la release se compila sin firmar (`app-release-unsigned.apk`) y Gradle lo avisa. La versión local es `0.0.0-dev` (código 1): nunca se instala encima de una publicada. Para otra versión, `-PetiquetaVersion=vX.Y.Z`.

La release usa R8 sin ofuscar (`app/proguard-rules.pro`): si algo funciona en depuración y falla en release, falta una regla. Cada regla se añade con un comentario del fallo que corrige.

### GitHub: secrets y variable

Una vez por repositorio. El job de `.github/workflows/release.yml` declara `environment: release`, así que los secrets van en ese entorno y solo los ve un workflow lanzado por una etiqueta `v*`:

1. Crear el entorno `release` en *Settings › Environments*, con *Deployment branches and tags* limitado a etiquetas `v*`.
2. Dar de alta los secrets en el entorno (lo hace el usuario), sin escribir las contraseñas en la línea de comandos (`gh` las pide):

   ```bash
   base64 -i ~/claves/smart-bike.jks | gh secret set RELEASE_KEYSTORE_BASE64 --env release
   gh secret set RELEASE_KEYSTORE_PASSWORD --env release
   gh secret set RELEASE_KEY_PASSWORD --env release
   gh secret set RELEASE_KEY_ALIAS --env release --body smart-bike
   ```

3. Guardar la huella como variable del repositorio (es pública): `gh variable set HUELLA_CERTIFICADO --body "<huella SHA-256>"`. El workflow no publica si el APK no tiene esa huella.
4. Comprobar: `gh secret list --env release` y `gh variable list` muestran los nombres, sin los valores.

### Publicar

```bash
git tag v0.2.0
git push origin v0.2.0
```

El workflow comprueba la etiqueta y que esté en `main`, pasa los tests, ktlint y lint, compila y firma, verifica la huella y crea la Release `0.2.0` con `smart-bike-0.2.0.apk`, su SHA-256 y la huella. Un push a `main` sin etiqueta no publica nada. El `versionCode` es `X·1 000 000 + Y·1 000 + Z`, así que cada etiqueta debe ser mayor que la anterior.

Para comprobar un APK descargado: `shasum -a 256 smart-bike-0.2.0.apk` y `apksigner verify --print-certs smart-bike-0.2.0.apk` (la huella debe ser la de la Release).

### Deshacer una Release

`gh release delete v0.2.0 --cleanup-tag` borra la Release y la etiqueta. Se publica después la siguiente versión de parche (`v0.2.1`): nunca se reutiliza un número que alguien pueda tener instalado.
