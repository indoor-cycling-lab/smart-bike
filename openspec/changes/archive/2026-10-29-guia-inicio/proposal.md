# Proposal

## Why

Quien abre la app por primera vez ve Rodaje con los iconos tachados, la potencia en «—» y un pie que dice «Toca las conexiones para conectar el Neo y el Square». Nada le explica qué es la app, qué piezas intervienen (un rodillo, un controlador y una app de ciclismo indoor), en qué orden se pone todo a punto ni cómo se empareja con la app de entrenamiento, que es lo habitual pero no obligatorio. Los specs cubren el primer uso como un estado vacío («no busca ni conecta sola»), no como un camino. Una guía corta, por páginas, que se cierre sola cuando ya no hace falta y que se pueda minimizar o descartar, resuelve el arranque sin estorbar a quien ya conoce la app.

## What Changes

- Rodaje muestra una **hoja inferior** «Para empezar» de **tres páginas** por las que se avanza y se retrocede con «Siguiente» y «Atrás» (también deslizando), con un indicador de página:
  1. **Qué es:** «Smart Bike» conecta un rodillo, un controlador y una app de ciclismo indoor. Explica cada pieza en genérico, con un ejemplo (p. ej. Tacx Neo, Elite Square, Zwift), y que la app simula las marchas y muestra los datos mientras se rueda. Sin tutorial de botones ni de marchas.
  2. **Para empezar:** tres pasos en orden natural, cada uno con un acceso directo (chevron) a su pantalla: **Empareja tus dispositivos** (Dispositivos), **Configura la conexión** (opcional; Conexión, donde se elige cómo se conecta la app de ciclismo indoor a Smart Bike) y **Ajusta la app** (Ajustes: tipo de marcha, botones, pantalla…). Solo el primero tiene estado real: pendiente, conectando, a medias («Falta el controlador») o hecho.
  3. **Conecta Smart Bike a tu app de ciclismo indoor:** un resumen de qué hacer en la app de entrenamiento según el modo de conexión elegido (Wifi + Bluetooth, Solo Bluetooth o Solo wifi), con el estado de esa conexión: pendiente (sin rodillo), esperando a la app o app conectada.
- Solo el rodillo es imprescindible para rodar. El controlador y la app se marcan «Opcional», con lo que se pierde sin ellos (sin app se rueda en SIM o ERG manual; sin controlador se cambia de marcha desde la pantalla). Ningún paso pendiente se muestra como un error.
- La hoja se **abre sola** solo en el primer uso (ningún dispositivo configurado). Se puede **minimizar** a una tira de una línea en el hueco del pie, que dice «Para empezar» y lo siguiente por hacer («Empareja tus dispositivos» o «Conecta tu app de ciclismo indoor»), o **cerrar** con una equis (se vuelve a ver al abrir la app). «No volver a mostrar» la oculta y se recuerda. Desaparece sola, y para siempre, cuando los dispositivos están emparejados y hay una app conectada (rodillo, controlador y app conectados).
- Una fila «Guía de inicio» en Ajustes la reabre, en la página 1, para que descartarla no sea irreversible.
- No aparece a quien actualiza la app con algún dispositivo ya configurado.
- Los textos y la colocación están validados en la maqueta.

## Capabilities

### New Capabilities

- `getting-started-guide`: la guía de inicio de Rodaje: sus tres páginas, sus pasos y estados, cuándo se ve y cuándo se oculta, cómo se minimiza, se descarta y se recupera, y adónde lleva cada acceso.

### Modified Capabilities

- `ride-screen`: la hoja inferior y la tira minimizada en el pie de Rodaje, en vertical y en horizontal.
- `settings-screen`: la fila «Guía de inicio», que la reabre.
- `screen-mockups`: los estados de la guía en las maquetas de rodaje y de Ajustes.

## Impact

- **Código:** `Rodaje.kt` y `RodajeUiState` (página actual, abierta o minimizada, y los pasos a partir de `Sesion.estado`, el modo de conexión y los dispositivos configurados), los composables de la hoja, de los pasos y de la tira, la fila en `Ajustes.kt` y el campo opcional que recuerda el descarte en `estado.json` (`Almacen`). Sin cambios en BLE ni en la publicación.
- **Sistema de diseño:** componentes `GuideSheet` y `GuideStep` (`components.css`, `README.md` y `preview.html`); tokens existentes, sin colores nuevos. Ya están en `design-system/`.
- **Datos:** campo opcional en `estado.json`, sin cambio de versión y sin entrar en la exportación de la configuración (depende del móvil). La página y el estado abierta/minimizada viven en memoria.
- **Textos:** todos van a recursos de cadenas en español e inglés. Los textos de la página 3 salen de las mismas cadenas que usa Conexión para cada modo, no se copian.
- **Documentación (`docs/`):** `funcional.md` (la guía en F11 o F1), `arquitectura.md` (estado de la guía y su persistencia), `planificacion.md` (fila del change) y `desarrollo.md`, si cambia algo.
- **Dependencias entre changes:** va después de `modo-tactil-rodaje`, porque «sin controlador cambias de marcha desde la pantalla» solo es cierto con ese change; y los textos nuevos van a recursos de cadenas, que `idioma-es-en` ya traduce.
- **Prueba con el equipo real:** instalar la app en el OnePlus 8 sin datos, comprobar que la hoja se abre en la página 1, recorrer las páginas, emparejar el Neo y el Square desde sus pasos, conectar Zwift siguiendo la página 3 y ver que la guía desaparece; y repetirlo con datos de una versión anterior para comprobar que no aparece.

## Fuera de alcance

- Un tutorial paso a paso o una visita guiada de la interfaz: la guía no explica los botones ni las marchas, solo qué es la app y cómo se pone a punto.
- Explicar el modelo mental de la app (quién habla con quién: BLE, wifi, DirCon).
- Pedir los permisos de Bluetooth de antemano: se siguen pidiendo al buscar.
- El pulsómetro y el ventilador: son opcionales, no son pasos.
- Buscar y conectar solos en el primer uso: la app sigue sin buscar hasta que el usuario toca «Buscar».
- Cambiar el modo de conexión desde la guía: la página 3 lo dice y enlaza a Conexión, donde se cambia.
