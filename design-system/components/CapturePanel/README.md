# CapturePanel

Zona donde la app espera a que el usuario pulse en el Square el botón, gesto o combinación que quiere asignar a una acción.

**Uso**
- Borde discontinuo en `gear` mientras escucha. Las teclas detectadas aparecen con `SquareKey` pulsada, a 48 dp.
- Debajo, qué se ha reconocido («Detectado: combinación») y, si ya está asignado, un `Notice` con el conflicto.
- Sin el Square conectado (`.sq-capture.is-off`): borde continuo `line`, sin el discontinuo de escucha, y el texto «Conecta el Square para capturar un botón». «Guardar» queda deshabilitado.
- Encima, las entradas que ya tiene la acción, una por fila (`sq-entry` dentro de `sq-list`), con sus teclas, el gesto y un botón de quitar (`close`, `touch-min`, con `aria-label` «Quitar …»).
- Al detectar algo vibra (si está activado) y se puede repetir pulsando otra vez.

**No**
- No guardar automáticamente: la asignación se confirma con «Guardar».
