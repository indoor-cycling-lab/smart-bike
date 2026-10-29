# Tasks

## 1. Maqueta

- [x] 1.1 Dibujar en `design-system/icons.svg` los ocho iconos (`i-tecla_triangulo`, `i-tecla_circulo`, `i-tecla_cuadrado`, `i-tecla_equis`, `i-tecla_arriba`, `i-tecla_abajo`, `i-tecla_izquierda`, `i-tecla_derecha`), de un solo trazado relleno, a partir de las formas de `i-botones`, y anotar su descripción en el comentario del archivo. Verificación: `python3 scripts/design-system/generar.py` termina sin error y los ocho aparecen en la guía.
- [x] 1.2 Hacer que `sq-key` dibuje el icono en los botones de símbolo y de cruceta (CSS en `components.css`, también `sq-capture .sq-key`) y actualizar `components/SquareKey`, `CapturePanel` y `ButtonMappingRow` (README y preview) y la regla «los glifos son texto». Verificación: la guía muestra las teclas en reposo y pulsadas, en los dos temas.
- [x] 1.3 Ajustar el tamaño óptico de cada símbolo con la cruz actual como referencia. Verificación: captura a doble resolución de `diagnostico.html#estado=rodando` con las 12 teclas del grupo, con las flechas a la misma altura y los cuatro símbolos de la misma proporción.
- [x] 1.4 Pasar `diagnostico.html`, `botones.html` y `asignar.html` a las teclas dibujadas, con todos sus estados, y actualizar `maquetas/index.html`. Verificación: abrir cada maqueta en sus estados con teclas de símbolo y de cruceta, sin glifos de texto en las teclas.
- [x] 1.5 Comprobar el contraste del icono en reposo y pulsado en los dos temas (`scripts/design-system/contraste.py` sin pares nuevos) y que la tecla de 36 dp y la de 48 dp se ven bien. Verificación: el generador no falla y las capturas de los dos temas se ven bien.
- [x] 1.6 Regenerar las capturas de la web (`python3 scripts/sitio/capturas.py`) y la web (`python3 scripts/sitio/generar.py`). Verificación: las capturas de Diagnóstico, Botones y Asignar muestran las teclas dibujadas.
- [x] 1.7 Validación de la maqueta por el usuario. Dar los enlaces exactos (la sección SquareKey de la guía, `maquetas/diagnostico.html#estado=rodando`, `maquetas/botones.html`, `maquetas/asignar.html#estado=detectado`) y esperar su aprobación explícita.

## 2. App

- [x] 2.1 Regenerar los iconos con `python3 scripts/design-system/generar.py` y comprobar que `IconosSincronizadosTest` los reconoce. Verificación: `./gradlew :app:testDebugUnitTest`.
- [x] 2.2 Hacer que `SquareKey` dibuje el icono cuando el texto es uno de los ocho glifos, con el color de la tecla (reposo y pulsada) y la descripción accesible con el nombre del símbolo (textos en `strings.xml`); letras y nombres siguen como texto. Test unitario del mapa de glifo a icono y a descripción. Verificación: `./gradlew :app:testDebugUnitTest`.
- [x] 2.3 Comprobar `ButtonMappingRow`, `CombinacionSquare`, Diagnóstico y el panel de captura con las teclas dibujadas; actualizar sus tests de instrumentación si buscaban el glifo como texto. Verificación: compilan con `./gradlew :app:assembleDebugAndroidTest` y, con el móvil conectado, pasan los de Diagnóstico y Botones.
- [x] 2.4 `./gradlew ktlintCheck` y la batería completa de tests sin errores.
- [x] 2.5 Probar en el OnePlus 8 (skill `desplegar-movil`): Diagnóstico con el Square conectado, Botones y Asignar acción. Verificación: capturas con las flechas alineadas y los símbolos de la misma proporción, y anotar lo que quede pendiente.

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md`, `docs/planificacion.md` (fila y sección del change 29, «En curso» al crearlo y «Archivado» al archivarlo) y `docs/desarrollo.md`, y actualizar lo que haya cambiado. Verificación: `git diff docs/` solo con cambios coherentes con el change.
