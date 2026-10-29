---
name: desplegar-movil
description: Conecta por depuración inalámbrica con el OnePlus 8 de pruebas, compila e instala la app de depuración, la abre y lee su registro o hace una captura. Úsala para desplegar o probar la app en el móvil real, o cuando el usuario pida instalarla, abrirla, ver el registro o una captura.
---

# Desplegar en el móvil

Referencia completa: `docs/desarrollo.md`. El móvil es el **personal** del usuario: solo se toca la app del proyecto. El hook `.claude/hooks/adb_guard.py` bloquea el resto; si bloquea algo, no busques otra forma de hacerlo, pídelo al usuario (`! <comando>`).

Identificador de la app de depuración: `app.smartbike.debug`. Si `app/build.gradle.kts` dijera otro, avisa al usuario: el hook y `docs/desarrollo.md` tendrían que cambiar.

## 1. Conectar

1. `adb devices -l`. Si el móvil ya aparece como `device`, sigue al paso 2.
2. `python3 .claude/skills/desplegar-movil/buscar_movil.py`: imprime una o más direcciones `<ip>:<puerto>`. Busca con `dns-sd`, el mDNS de macOS, que encuentra el móvil aunque `adb mdns services` no lo vea; si el móvil no se anuncia, prueba los puertos de la última IP encontrada (`movil.local`, fuera del repositorio). Haz `adb connect` con cada dirección hasta que una conecte. No le pidas la IP al usuario mientras el script la encuentre.
3. Si el script no encuentra nada: pide al usuario que compruebe que el móvil está en la misma wifi que el Mac y que la depuración inalámbrica está activada. La IP que muestra el móvil puede no servir (con una VPN activa muestra la de la VPN, y el Mac no llega a ella). Si nunca se ha vinculado, que haga `! adb pair <ip>:<puerto>` con el código que muestra el móvil (`docs/desarrollo.md` § 2). No intentes vincular tú.
4. Si hay más de un dispositivo, pregunta cuál usar y añade `-s <serie>` a todos los comandos de `adb`.

## 2. Compilar e instalar

1. `./gradlew installDebug`. Después de `connectedDebugAndroidTest`, vuelve a instalar: los tests desinstalan la app al terminar y se pierden sus datos y permisos. Si falla la compilación, muestra el error y para; no instales una versión anterior.
2. Si la app pide permisos de Bluetooth o notificaciones y la prueba no trata de ellos, concédelos:
   `adb shell pm grant <app> android.permission.BLUETOOTH_SCAN` (y `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`, `POST_NOTIFICATIONS`).

## 3. Abrir y observar

1. `adb logcat -c` y después `adb shell am start -n <app>/<actividad principal del manifiesto>`, con el nombre de clase completo (`app.smartbike.debug/app.smartbike.MainActivity`): por el sufijo `.debug`, la forma corta `/.MainActivity` falla.
2. Registro: `adb logcat -d --pid=$(adb shell pidof -s <app>)`. Si la app se ha cerrado, `adb logcat -d -s AndroidRuntime:E` para ver el fallo.
3. Captura: `adb exec-out screencap -p > <directorio temporal>/captura.png` y léela con Read. Antes de usar `input tap`, `swipe` o `keyevent`, comprueba con una captura que la app está en primer plano.
4. Registro de sesión de la app: `adb exec-out run-as <app> cat files/<fichero>` hacia un fichero local.

## 4. Informar

Di qué versión se instaló (commit), si arrancó, y lo relevante del registro o la captura. Si la prueba necesita al usuario (pedalear, pulsar el Square, emparejar en Zwift), dile exactamente qué hacer y espera antes de leer el registro.
