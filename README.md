# Smart Bike

Smart Bike es un **proyecto personal**: una app Android que reúne los distintos elementos de mi setup de entrenamiento en rodillo para usarlos como si fueran una smart bike integrada. Con el móvil montado en la bici, el rodillo, los mandos de cambio, el pulsómetro, el ventilador y Zwift se manejan desde un solo sitio y sin tocar la pantalla mientras se rueda.

Nació para resolver un caso concreto: el Tacx Neo 1 no es compatible con los cambios virtuales de un cuadro Elite Square, así que esta app hace de intermediaria, lee los botones del Square y cambia la resistencia del Neo. Después ha ido creciendo hasta cubrir el resto del setup. Lo publico por si a alguien le sirve de referencia o de punto de partida, no como un producto terminado.

**Web del proyecto:** <https://indoor-cycling-lab.github.io/smart-bike/> (español e inglés, con capturas de las pantallas).

## Qué hace

```
 Elite Square (mandos) --BLE--+
 Tacx Neo 1 (rodillo) --BLE---+--> Smart Bike (móvil Android) --wifi o BLE--> Zwift
 Pulsómetro Bluetooth --BLE---+                    |
                                                   +--BLE--> Ventilador Wahoo KICKR Headwind
```

- **Marchas virtuales.** Los botones del Square cambian de marcha en el Neo, que no las tiene de serie. La app traduce cada marcha en más o menos resistencia, con un modelo simple o uno físico. La transmisión puede ser lineal o la de una bici real (de momento, solo la mía: Shimano 105 2×12), y las palancas pueden comportarse como las de un grupo inalámbrico (probado con Shimano Di2).
- **Rodillo para Zwift.** La app se presenta ante Zwift como un rodillo inteligente con potencia y cadencia, y le reenvía lo que mide el Neo. Zwift le manda de vuelta la pendiente. En la pestaña Conexión se elige el modo: wifi + Bluetooth (el rodillo por DirCon y el controlador aparte, por defecto), solo Bluetooth (FTMS) o solo wifi, con todo en un solo dispositivo como una Zwift Ride. Por Bluetooth, la app puede renombrar el móvil a «Smart Bike» mientras publica y devolverle su nombre al terminar.
- **Otras plataformas.** Zwift es la plataforma principal, pero el rodillo se publica con estándares (FTMS por Bluetooth y por wifi con DirCon), así que otras como Rouvy o MyWhoosh deberían poder usarlo como cualquier rodillo inteligente. El controlador y el modo solo wifi son propios de Zwift.
- **Controlador de Zwift.** Los botones del Square llegan a Zwift como los de un controlador oficial (menús, dirección, freno y Ride On), sin emparejar el Square en Zwift.
- **Pulso y ventilador.** Reenvía a Zwift el pulso de una banda Bluetooth y controla un ventilador Wahoo KICKR Headwind, de forma manual o según el pulso.
- **Sin Zwift.** También funciona sola: en modo Libre (terreno llano, la dureza la da la marcha virtual) o en ERG manual.
- **Pantalla de rodaje** pensada para verse a 1 metro o menos y manejarse con los botones del Square, con gestos y acciones configurables y tema claro y oscuro. Sin el Square también se rueda: la pantalla muestra botones − y + para cambiar de marcha y, si se activa en Ajustes, el modo se cambia tocando su indicador.
- **Guía de inicio.** En el primer uso, una guía explica qué hace la app, los pasos para empezar con su estado, las formas de rodar y cómo conectarla a la app de entrenamiento. Se puede volver a abrir desde Ajustes.
- **Español e inglés.** La interfaz está en los dos idiomas: la app elige uno en el primer arranque según el idioma del móvil y se cambia en Ajustes.
- **Ajustes y diagnóstico.** Copia de seguridad de la configuración, una pantalla de diagnóstico con el tráfico de los dispositivos y, opcionalmente, la grabación de cada sesión en un archivo de texto para revisarla o compartirla después.

## Estado y límites

- Está hecho **para mi equipo**: Tacx Neo 1, Elite Square, Zwift en un PC y un móvil Android 13. Con otros rodillos, controladores o versiones de Android puede no funcionar. Con plataformas distintas de Zwift (Rouvy, MyWhoosh…) aún no se ha probado.
- Algunas partes están validadas con el equipo real y otras se anotan como «a validar». El detalle de cada change, con lo que está probado y lo que falta, está en [`docs/planificacion.md`](docs/planificacion.md).
- ¿Otro rodillo, controlador o plataforma? Aunque es un proyecto de uso personal, si [abres un issue](https://github.com/indoor-cycling-lab/smart-bike/issues/new?template=compatibilidad.yml) contando tu equipo, se puede estudiar añadir su compatibilidad.
- Solo Android (`minSdk 28`). No hay versión para iOS ni publicación en tiendas: el APK firmado de cada versión se descarga de [Releases](https://github.com/indoor-cycling-lab/smart-bike/releases/latest), con su SHA-256 y la huella del certificado para comprobarlo, o se compila desde el código.
- Es software hecho a medida y en evolución, **sin garantías**. Algunos protocolos (el de DirCon y el del controlador de Zwift, por ejemplo) no son oficiales y se han reconstruido por observación, así que Zwift puede cambiar su comportamiento en cualquier momento.

Tacx, Elite, Zwift, Wahoo y el resto de marcas citadas pertenecen a sus propietarios. Este proyecto es independiente y no está afiliado ni respaldado por ninguno de ellos.

## Compilar y probar

Hace falta el JDK 21 y el SDK de Android (la ruta va en `local.properties` con `sdk.dir=…`, que no se sube al repositorio). Compilar contra la API 37; la app requiere Android 9 o superior.

```sh
git clone https://github.com/indoor-cycling-lab/smart-bike.git
cd smart-bike

./gradlew :core:test :app:testDebugUnitTest   # tests unitarios (no necesitan móvil)
./gradlew installDebug                          # instala la app de depuración en el móvil conectado
```

El id de la app es `app.smartbike` (`app.smartbike.debug` en depuración). Los tests de interfaz (`connectedDebugAndroidTest`) necesitan un móvil real, y Bluetooth y wifi solo se pueden probar con el equipo físico: el emulador de Android no tiene Bluetooth real. Los comandos, el entorno de pruebas y los problemas conocidos están en [`docs/desarrollo.md`](docs/desarrollo.md).

## Estructura

```
app/             app Android (Kotlin y Jetpack Compose): interfaz, Bluetooth, wifi y servicio de rodaje
core/            Kotlin puro: dominio (marchas, modelos, asignaciones) y protocolos; se prueba sin Android
design-system/   tokens, estilos de componentes (sq-*), pautas y guía de estilo generada
maquetas/        una página por pantalla con sus estados; index.html las enlaza
docs/            documentos funcional, de arquitectura, de planificación y de desarrollo
openspec/        especificaciones y cambios (OpenSpec)
site/            web pública de GitHub Pages (español e inglés), generada con scripts/sitio/
scripts/         utilidades (generación del sistema de diseño y de la web)
AGENTS.md        reglas de trabajo para agentes de IA
```

- [`docs/funcional.md`](docs/funcional.md): qué hace la app y por qué.
- [`docs/arquitectura.md`](docs/arquitectura.md): módulos, flujo de datos y protocolos.
- [`docs/planificacion.md`](docs/planificacion.md): changes realizados y previstos, con lo validado.
- [`docs/desarrollo.md`](docs/desarrollo.md): entorno, despliegue y pruebas en el móvil.

## Cómo se ha desarrollado

El proyecto se ha construido con ayuda de un agente de IA (Claude Code), siguiendo un flujo de especificaciones con OpenSpec: cada cambio parte de una propuesta, un diseño y unas tareas que se validan con el equipo real antes de darse por buenos. Los cambios de interfaz se validan antes en una maqueta HTML (`maquetas/`), construida con el sistema de diseño (`design-system/`), y después se implementan en la app. Las reglas de trabajo están en [`AGENTS.md`](AGENTS.md) y el historial de cambios, en `openspec/changes/archive/`.

Los protocolos de los dispositivos se han **reimplementado a partir de documentación y de observar su comportamiento**. No se ha copiado código de otras aplicaciones: en particular, ni de qdomyos-zwift (GPL-3.0) ni de BikeControl (no comercial).

El código, las especificaciones y la documentación están en español; la interfaz de la app y la web, en español e inglés.

## Invitar a un café

Si Smart Bike te resulta útil o te gusta el proyecto, puedes [invitarme a un café en Ko-fi](https://ko-fi.com/jmlopezdona). Es un proyecto personal: el café se agradece, pero no compromete soporte ni nuevas funciones.

## Licencia

Copyright (C) 2026 Jose Manuel Lopez Doña.

Smart Bike es software libre, publicado bajo la **GNU General Public License v3.0 o posterior** (GPL-3.0-or-later); el texto completo está en [`LICENSE`](LICENSE). En resumen: se puede usar, estudiar, modificar y compartir, también con fines comerciales, pero cualquier obra que use o derive de este código **debe distribuirse con su código fuente abierto y bajo la misma licencia**.

Algunos recursos incluidos tienen su propia licencia y se mantienen con ella:

- Las fuentes Barlow, Barlow Condensed y JetBrains Mono, bajo la SIL Open Font License (`design-system/fonts/OFL-*.txt`).
- Los iconos de Material Design, bajo la licencia Apache 2.0 (`design-system/icons-LICENSE.txt`).
