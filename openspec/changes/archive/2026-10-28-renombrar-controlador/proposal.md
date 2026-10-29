# Proposal

## Why

Hoy «Mando» y el icono de un mando de consola (`i-sports_esports`) nombran dos cosas distintas: el Elite Square, el dispositivo que la app busca y al que se conecta, y el servicio que la app publica a la app de entrenamiento, una emulación del controlador de Zwift. Tras `pestana-conexion` (26) esa confusión se ve en Dispositivos y en Conexión, y el gamepad no dice nada de unas palancas de cambio en un manillar de carretera. Además, Dispositivos busca «controladores» en general (hoy solo el Elite Square; más adelante Zwift Play u otros), y el nombre «Mando» no lo expresa.

## What Changes

- **Dispositivos y estado del dispositivo.** La sección «Mando» pasa a llamarse **«Controlador»**, un tipo genérico sin nombrar modelo ni plataforma (el modelo recordado, «Elite Square», va debajo). Sus botones dicen «Buscar», «Reconectar» y «Olvidar», con la descripción accesible «Buscar controlador», «Reconectar controlador» y «Olvidar controlador».
- **Icono de manillar.** El Square y su estado de conexión (barra de Rodaje, Dispositivos y Diagnóstico) llevan un icono nuevo de **manillar de bici de carretera** dibujado a medida, `i-manillar`, que sustituye al mando de consola. Se lee a 14 dp en la barra de Rodaje con todos sus estados por forma: normal, atenuado con el hueco y la marca que gira, tachado y con la insignia de batería baja.
- **Conexión: el servicio que publica la app.** La fila «Mando» pasa a **«Controlador Zwift»** con el icono de Zwift (`i-zwift`), porque es una emulación del controlador de Zwift; si otras plataformas lo admiten se verá más adelante. Fuera de ese nombre, «Zwift» sigue apareciendo solo en la ayuda de emparejamiento y en la compatibilidad.
- **Textos que nombran el controlador como lo que falta o lo que se publica**, sin la plataforma: Rodaje dice «Conectado · sin controlador» y «Conectado · sin controlador ni pulso», y la descripción accesible dice «controlador, esperando».
- **La barra de conexiones de Rodaje** llama «Controlador» al Square en su descripción accesible («Conexiones: Neo, conectado; Controlador, sin conexión» y «Controlador, batería baja»), como Dispositivos; decidido por el usuario al validar la maqueta. Los textos de Conexión, de Botones y de las ayudas siguen el mismo vocabulario.
- **Retirada de `i-sports_esports`** del sprite (`icons.svg`) y de `ic_sports_esports.xml`, que ya nada usa.
- **Sistema de diseño y web.** El icono nuevo, las pautas de iconografía, la leyenda de `ConnectionBar` y la guía se actualizan; la web pública y sus capturas se regeneran.
- **Maqueta primero.** Se dibujaron dos propuestas de manillar (frontal y de perfil) y el usuario eligió la frontal y, con una foto de referencia, se redibujó con bajadas en curva, puntas cortas y un puente corto; la de perfil se retiró.

**Fuera de alcance:**

- Diagnóstico: su refactor para ser agnóstico a la plataforma es otro change, sin planificar. Solo cambia lo que sigue a `Enlace` (el Square lleva el manillar; la fila «Mando de Zwift» y la de «Zwift» llevan las dos `i-zwift`); su texto, «Mando de Zwift» y «Zwift», no se toca.
- Cambiar los nombres de `Enlace.Mando`, `MandoZwift*`, `EstadoMando*` ni los paquetes: es dominio interno.
- Cambiar el protocolo, la publicación o la lógica de conexión.
- Admitir otros controladores (Zwift Play, Click…) o elegir uno de una lista.
- Los mensajes del registro de sesión («Mando: publicado»).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `device-connection`: la sección «Mando» pasa a «Controlador» (genérico) en Dispositivos, con sus botones y su descripción accesible, y el icono propio de cada fila.
- `ride-screen`: el Square lleva el manillar en la barra de conexiones y se llama «Controlador» en su descripción accesible; el estado de Rodaje dice «controlador» donde decía «mando».
- `platform-settings`: el servicio de Conexión se llama «Controlador Zwift» y lleva el icono de Zwift; el resto del texto de la pestaña dice «controlador».
- `ui-components`: el icono de cada enlace (el Square, el manillar; el controlador publicado, el de Zwift), la entrada «Botones» distinta del manillar, y el texto accesible de la barra de conexiones y del estado.
- `button-mapping`: el grupo de Zwift de Botones dice «controlador».
- `screen-mockups`: las maquetas de Dispositivos, Conexión y los iconos de los enlaces usan los nombres y los iconos nuevos.
- `style-guide`: la iconografía documenta el manillar y la nueva guía lo muestra a 24 y a 14 dp en todos sus estados.

## Impact

- **`design-system/`:** `icons.svg` (alta de `i-manillar`, baja de `i-sports_esports`), `README.md` (tabla de iconografía y vocabulario), `components/ConnectionBar`, `ServiceRow`, `BottomNav`, `ConnectionChip`, `DeviceStatus`, `RideScreen` (sus `README.md` y `preview.html`) y la guía regenerada con `python3 scripts/design-system/generar.py`.
- **`:app`:** `ConnectionBar.kt` (`Enlace.Square` → `ic_manillar`, `Enlace.Mando` → `ic_zwift`), `strings.xml` (`servicio_mando` se divide en el de Dispositivos y el de Conexión, `tipo_mando`, `enlace_mando`, `publicado_*`, ayudas y `dispositivos_intro`), `Dispositivos.kt`, `Conexion.kt`, `Rodaje.kt`, `Botones` y los tests que leen esos textos o iconos. `ic_manillar.xml` se genera; `ic_sports_esports.xml` se borra al regenerar.
- **`maquetas/`:** `dispositivos.html`, `conexion.html`, `rodaje.html`, `rodaje-vertical.html`, `rodaje.js`, `botones.html`, `diagnostico.html` (solo los iconos), `identidad.html` e `index.html`.
- **`site/`:** `contenido/es.json` (y `en.json` si cambia alguna clave) y las capturas regeneradas.
- **Equipo real:** comprobar en el OnePlus 8 que el manillar se lee a 14 dp y que el estado se distingue por la forma; ver cómo llama la app de entrenamiento a la lista donde se elige el controlador publicado.
- **Documentos de `docs/`:** `funcional.md` (descripción de Dispositivos y Conexión, si nombran «mando»), `arquitectura.md` (la línea sobre `ic_sports_esports` y los iconos de los enlaces) y `planificacion.md` (fila 27, «En curso» al crearlo y «Archivado» al cerrarlo). `desarrollo.md`: ninguno, salvo que cambie algún comando.
