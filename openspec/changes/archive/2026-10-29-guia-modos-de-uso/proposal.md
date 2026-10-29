# Proposal

## Why

La guía de inicio explica de qué piezas se compone Smart Bike (rodillo, controlador y app de ciclismo indoor) y cómo ponerlas a punto, pero no dice **cómo se puede usar** la app. Quien la abre por primera vez no sabe que puede rodar sin ninguna app de entrenamiento (cambiando de marcha), con una que le mande la pendiente de la ruta, o con una potencia fija (ERG), ni que cada forma tiene su nombre en el indicador de Rodaje. Además, la última página (conectar con la app de ciclismo indoor) parece hoy un paso obligatorio, cuando solo hace falta para algunos modos: esa idea da el puente natural entre los modos y la conexión.

## What Changes

- La guía pasa de **tres a cuatro páginas**; la nueva va **la tercera**, después de los pasos y antes de conectar con la app:
  1. **Qué es** (sin cambios): rodillo, controlador y app de ciclismo indoor.
  2. **Para empezar** (sin cambios): los tres pasos con estado.
  3. **Cómo puedes usar la app** (nueva): una frase de qué es la app y las formas de rodar, con el nombre que ve el ciclista en el indicador de modo de Rodaje: **LIBRE** (sin app de ciclismo indoor; terreno llano, la dureza la da la marcha virtual), **SIM** (con una app como Zwift que envía la pendiente de la ruta; las marchas se suman a ella) y **ERG** (potencia fija sea cual sea la cadencia): **ERG MANUAL**, que fija el ciclista, y **ERG APP**, que manda el entrenamiento de la app. Remata diciendo que **SIM y ERG APP necesitan una app conectada** (la página siguiente) y que LIBRE y ERG MANUAL no. Sin tutorial de botones ni de cómo pasar de un modo a otro.
  4. **Conecta Smart Bike a tu app de ciclismo indoor** (la actual página 3), ahora con la marca **«Opcional»**: solo hace falta para SIM y ERG APP.
- El indicador pasa a «n de 4». La hoja sigue abriéndose en la página 1 (qué es), «Guía de inicio» de Ajustes sigue reabriéndola en la página 1, y los pasos, su estado, la tira minimizada, el descarte y el fin de la guía no cambian.
- Los nombres de los modos salen de las mismas cadenas que usa el indicador de Rodaje, en español y en inglés, para que no puedan divergir.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `getting-started-guide`: cuatro páginas en lugar de tres, la nueva página 3 de modos de uso, la antigua página 3 (conectar con la app) pasa a ser la 4 y se marca «Opcional», con los escenarios y el texto de accesibilidad que citan el número de páginas.
- `screen-mockups`: los estados de la guía en las maquetas de rodaje pasan a cuatro páginas, con el estado nuevo de la página 3 de modos y la marca «Opcional» en la 4.

## Impact

- **Código:** `GuideSheet` (una página más: `PaginaQueEs` y `PaginaPasos` siguen siendo las páginas 1 y 2, se añade `PaginaModos` como la 3 y `PaginaConectar` pasa a la 4 con la marca «Opcional»), `GuiaPantalla` (`ULTIMA` de 3 a 4) y su estado en `RodajeUiState`, y las cadenas de la guía en `values/strings.xml` y `values-en/strings.xml` (`TextosInglesTest` exige las dos). Tests: `GuiaInicioTest` (`:core` y `androidTest`) y los de `RodajeUiState` que cuenten páginas.
- **Sistema de diseño:** ningún componente nuevo; la página de modos usa `GuideSheet` y tokens existentes. Si necesita una fila de modo con la insignia, se añade a `design-system/` primero (README y `preview.html`) y se regenera la guía.
- **Datos:** ninguno. La página actual vive en memoria y lo que recuerda la guía en `estado.json` (descarte) no cambia.
- **Documentación (`docs/`):** `funcional.md` (F11.12, que habla de «tres páginas»), `arquitectura.md` (la hoja de tres páginas, en la tabla de maquetas), `planificacion.md` (fila del change: «En curso», dependencias 36 y 39) y `desarrollo.md`: ninguno. Hay que corregir a mano el `Purpose` de `openspec/specs/getting-started-guide/spec.md`, que dice «tres páginas» y los deltas no alcanzan.
- **Dependencias entre changes:** va **después** de `modo-libre-y-pendiente` (el nombre «LIBRE» aún no existe en la app) y de `guia-inicio` (archivado). Si `modo-tactil-rodaje` llega antes, no cambia nada aquí: la página no habla de cómo alternar modos.
- **Prueba con el equipo real:** en el OnePlus 8 sin datos, abrir la app, ir a la página 3 con los modos; comparar sus nombres con el indicador de Rodaje sin Zwift («LIBRE»), con Zwift en una ruta («SIM») y en ERG manual y en un entrenamiento; recorrer las cuatro páginas, comprobar que la 4 lleva «Opcional» y que la 3 remite a ella desde SIM y ERG APP, y que el resto de la guía sigue igual.

## Fuera de alcance

- Explicar los botones, las marchas o cómo alternar entre modos (el tutorial sigue fuera de la guía).
- Cambiar los pasos con estado, la tira minimizada, el descarte, el fin de la guía o lo que se recuerda de ella.
- Hacer tocable el indicador de modo o cambiar los modos: eso es de `modo-tactil-rodaje` y de `modo-libre-y-pendiente`.
- Enlazar desde la página de modos a Ajustes, Conexión o Diagnóstico (solo remite a la página siguiente de la guía).
- Cambiar cuándo termina la guía: el fin de la guía y la tira minimizada siguen pidiendo una app conectada; «Opcional» es solo la marca de la página 4.
