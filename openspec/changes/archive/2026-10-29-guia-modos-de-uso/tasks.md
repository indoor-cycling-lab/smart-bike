# Tasks

## 1. Maqueta

- [x] 1.0 Comprobar que `modo-libre-y-pendiente` está archivado o al menos que su etiqueta «LIBRE» está en `ModeBadge` y en las maquetas de rodaje; si no, parar y avisar al usuario, porque la página usa esos nombres
- [x] 1.1 Añadir la página de modos de uso a la hoja de la guía en `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html`: nuevo estado de la página 3, entre los pasos y la conexión (con los modos «LIBRE», «SIM» y «ERG MANUAL / ERG APP», con su frase y la indicación de que SIM y ERG APP necesitan la app de la página siguiente), pasar el estado de conectar a la página 4 con la marca «Opcional», poner `n de 4` en todas las páginas y actualizar `rodaje.js` si hace falta; usar `ModeBadge` o el mismo estilo de palabra de modo que el indicador. Si hace falta un elemento nuevo en `GuideSheet`, darlo de alta en `design-system/` (`components.css`, `README.md` y `preview.html`) y regenerar con `python3 scripts/design-system/generar.py`; verificar que el generador termina sin error y que cada estado se abre por su `#estado=…` en los dos temas
- [x] 1.2 Comprobar la página 3 en vertical a 360 × 640 y a 390 × 844 y en horizontal a 640 × 360, y la tira minimizada (sin cambios); actualizar `maquetas/index.html` con los estados; verificar que la hoja se ve entera sin desplazamiento en las tres medidas y que los enlaces del índice abren los estados
- [x] 1.3 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/rodaje.html#estado=guia-3`, `maquetas/rodaje-vertical.html#estado=guia-3` y el de pantalla baja, y la página 4 con «Opcional») y esperar su aprobación explícita; si pide cambios, ajustar la maqueta, `design.md` y los specs, y volver a pedir validación

## 2. App

- [x] 2.1 Subir `GuiaPantalla.ULTIMA` a 4 en `:core` (`irA`, `siguiente`, `anterior` y `reabrir` no cambian de lógica); verificar con `GuiaInicioTest` (`:core`) que «Siguiente» llega de la 1 a la 4, que «Atrás» y el deslizamiento se detienen en los extremos y que reabrir deja la página 1
- [x] 2.2 Añadir `PaginaModos` a `GuideSheet.kt` como página 3 y mover `PaginaConectar` a la 4 con la marca «Opcional» (`PaginaQueEs` y `PaginaPasos` siguen en la 1 y la 2); que «Siguiente» no aparezca en la 4 y que el nombre accesible diga «página n de 4»; verificar con `GuiaInicioTest` (`androidTest`) que la hoja se abre en la página 1 con «qué es», que el recorrido llega a la 4 con «Opcional», que los modos SIM y ERG APP remiten a ella y que los pasos de la 2 siguen llevando a Dispositivos, Conexión y Ajustes
- [x] 2.3 Añadir las cadenas de la página de modos (`guia_p3_*`), renumerar las de conectar (`guia_p3_*` → `guia_p4_*`) y añadir la marca «Opcional» de la página 4, en español y en inglés, tomando los nombres de los modos de las cadenas del indicador; verificar que `TextosInglesTest` pasa y que la página muestra los mismos nombres que el indicador de Rodaje en los dos idiomas
- [x] 2.4 Ejecutar `./gradlew :app:testDebugUnitTest ktlintCheck` y corregir lo que falle con `./gradlew ktlintFormat` o a mano; verificar que ambos terminan sin errores
- [x] 2.5 Probar con el equipo real en el OnePlus 8 sin datos (guion de `proposal.md`, Impact): ver la página 3, comparar sus nombres con el indicador en los cuatro modos, recorrer las cuatro páginas, ver «Opcional» en la 4 y reabrir desde Ajustes; verificar que la página 3 se ve entera y que el resto de la guía sigue igual

## 3. Documentación

- [x] 3.1 Regenerar las capturas de la web con `python3 scripts/sitio/capturas.py` si cambia alguna de la guía y revisar `site/contenido/es.json` y `en.json`; verificar que `site/img/` refleja las cuatro páginas
- [x] 3.2 Actualizar la documentación: revisar `docs/funcional.md` (F11.12 y los «tres» de la guía), `docs/arquitectura.md` (la hoja de tres páginas en la tabla de maquetas), `docs/planificacion.md` (la fila de este change, «En curso» al crearlo y «Archivado» al cerrarlo) y `docs/desarrollo.md`, y actualizar lo que el change haya cambiado; corregir el `Purpose` de `openspec/specs/getting-started-guide/spec.md` (dice «tres páginas»); verificar que la fila de planificación dice «En curso» o «Archivado» según corresponda

## Workflow follow-up

- Archivar con `/openspec-archive-change` después de `modo-libre-y-pendiente`, con las specs sincronizadas en `openspec/specs/`.
