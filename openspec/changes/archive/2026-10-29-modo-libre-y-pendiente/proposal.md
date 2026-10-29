# Proposal

## Why

La app es de marchas virtuales: lo que el ciclista controla en todos los modos es la marcha. Hoy, sin app de entrenamiento, el indicador de Rodaje dice «SIM» aunque no haya nada que simular: el terreno es llano (0 %) fijo y solo cambia la marcha. El nombre sugiere una pendiente que no existe y esconde la diferencia con el «SIM» de una app de ciclismo indoor, que sí envía pendiente. Además, el usuario no puede ver la pendiente de la simulación en Rodaje porque el requisito «Sin datos del modelo de resistencia» se escribió demasiado ancho: lo que no debe verse es la pendiente base y lo que llega al Neo tras aplicar la marcha virtual, no la pendiente que manda la app. Y si el Square se desconecta (se cae el Bluetooth) y los botones − y + están desactivados, el ciclista se queda sin poder cambiar de marcha.

## What Changes

- **Modo «Libre»** (F6, F11.5): sin app de entrenamiento enviando pendiente, el indicador de modo de Rodaje dice **«LIBRE»** en lugar de «SIM». La pendiente es 0 % siempre y solo se cambia de marcha. Con una app enviando pendiente (ruta de Zwift) sigue diciendo «SIM»; «ERG MANUAL» y «ERG ZWIFT» no cambian. La etiqueta sigue al modo de control que ya distingue el código (`SimLibre` y `SimPlataforma`): si la app se va, se mantiene lo último 30 s y después pasa a «LIBRE». **No hay control manual de la pendiente.**
- **Dato «Pendiente»** (F11.3): un dato más, opcional, en «Datos en pantalla» (nueve a elegir, máximo seis a la vez), con la pendiente de la simulación: la que envía la app, o «0,0 %» en Libre; en ERG no aplica y muestra «—». Se corrige la regla de Rodaje: sigue sin mostrar la pendiente base ni lo que se envía al Neo tras sumar la marcha (eso es de Diagnóstico).
- **Botones − y + según el Square** (F11.8, F12.7): con el Square **no conectado** en ese momento, Rodaje muestra siempre los botones − y +; con el Square conectado, según la casilla de Ajustes (desactivada por defecto). Si el Square se cae rodando, los botones aparecen y la marcha y los datos se encogen para hacerles sitio. La casilla de Ajustes lo explica en su ayuda.
- **Diagnóstico y textos** llaman «Libre» al modo sin app, y «SIM · Zwift» al modo con ella. «SIM» queda como nombre de la familia (por ejemplo, «Alternar SIM ⇄ ERG») y como etiqueta con app.
- **Coordinación con los changes en curso**: `guia-inicio`, ya archivado, se ajusta después (sus textos de la app y su spec dicen «SIM» sin app); la página de modos de la guía es de `guia-modos-de-uso`, posterior; `modo-tactil-rodaje`, sin implementar, se reescribe antes de empezarlo (ver `design.md`).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: etiqueta del modo («LIBRE» o «SIM»), aviso al volver del ERG manual, la regla de lo que Rodaje no muestra del modelo, el dato «Pendiente», y los botones − y + (cuándo se muestran, el espacio que ocupan y los datos en pantalla baja).
- `settings-screen`: la lista de datos pasa de ocho a nueve («Pendiente») y la casilla de los botones − y + dice que solo manda con el Square conectado.
- `trainer-resistance`: el modo sin app de entrenamiento se llama Libre.
- `manual-erg`: alternar Libre o SIM con ERG manual y el nombre del modo al volver.
- `diagnostics`: la sección Simulación dice «Libre» sin app.
- `ui-components`: la insignia de modo gana «LIBRE».
- `screen-mockups`: los estados nuevos de Rodaje, de Ajustes y de «Datos en pantalla».

## Impact

- **Código:** `RodajeUiState` (etiqueta del modo a partir del modo de control, `botones` efectivo a partir de `Square` y la casilla, dato «Pendiente»), `Rodaje.kt`, `PantallaBaja.kt` y `MetricGrid`/`Datos` (nuevo dato), `ModeBadge`, `Ajustes.kt` (ayuda de la casilla), `DiagnosticoUiState`/`Diagnostico.kt` y los recursos de texto en español e inglés (`TextosInglesTest` exige los dos). `Sesion` ya distingue `SimLibre` de `SimPlataforma`: se expone la pendiente del terreno sin cambiar la lógica de resistencia. Sin cambios en BLE, en `:core` salvo lo que haga falta para exponer el terreno, ni en la publicación hacia Zwift.
- **Sistema de diseño:** `ModeBadge` (variante «LIBRE»), `MetricTile`/`MetricGrid` (dato «Pendiente») y el README de `GradeReadout`, que hoy dice «no ponerla en Rodaje» (solo se refiere a la base y a la efectiva). Se regenera la guía.
- **Datos:** el dato «Pendiente» entra en la lista de datos elegibles de `Configuracion`; las configuraciones guardadas no cambian (no está elegido por defecto). Sin cambio de versión del documento.
- **Documentación (`docs/`):** `funcional.md` (F6, F11.3, F11.5, F11.8, F12.7), `arquitectura.md` (la etiqueta del modo y los botones según el Square), `planificacion.md` (fila del change y, en las de `modo-tactil-rodaje` y `guia-inicio`, su dependencia) y `desarrollo.md`: ninguno. También la línea de `AGENTS.md` sobre lo que no se muestra en Rodaje.
- **Dependencias entre changes:** va después de `guia-inicio` (ya archivado) y antes de `modo-tactil-rodaje`, que aún no tiene implementación y toma de aquí el nombre del modo y la regla de los botones, y de `guia-modos-de-uso`, que usa esos nombres.
- **Prueba con el equipo real:** en el OnePlus 8 con el Neo y el Square, sin Zwift: Rodaje dice «LIBRE» y cambia de marcha; con Zwift en una ruta dice «SIM» y el dato «Pendiente» sigue la pendiente de Zwift; en un entrenamiento dice «ERG ZWIFT»; cerrar Zwift y ver «LIBRE» a los 30 s; apagar el Square y ver aparecer los botones − y +, que funcionan.

## Fuera de alcance

- Un control manual de la pendiente en Rodaje o en Ajustes: sin app el terreno es llano y la dureza la da la marcha (decisión del usuario).
- Mostrar en Rodaje la pendiente base, el ajuste por marcha o lo que se envía al Neo: siguen siendo de Diagnóstico.
- Cambiar la lógica de resistencia, los modelos, «Bajadas» o la acción «Alternar SIM ⇄ ERG» (conserva su nombre y su asignación).
- Hacer tocable el indicador de modo: es de `modo-tactil-rodaje`.
- Renombrar la casilla de los botones: es de `modo-tactil-rodaje` («Controles en pantalla»).
