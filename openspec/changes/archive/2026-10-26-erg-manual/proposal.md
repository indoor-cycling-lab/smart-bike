# Proposal

## Why

Hoy el Neo solo funciona en ERG cuando lo pide un entrenamiento de Zwift. Para hacer series propias a potencia fija (sin Zwift o rodando una ruta de Zwift) hace falta poner el Neo en ERG desde el Square y subir o bajar la potencia sin tocar el móvil (F7.2, F7.3). Es el change 9 de `docs/planificacion.md` (fase 3), y con él se cierra la prueba de ERG que quedó a medias en el change 3: comprobar que el Neo mantiene la potencia al cambiar de cadencia.

## What Changes

- Nuevo modo **ERG manual**: el Neo mantiene una potencia fijada por el usuario, entre 50 W y 1000 W, independiente de la cadencia y de la marcha.
- **Alternar SIM ⇄ ERG** desde el Square (por defecto, △ larga). Al entrar, la potencia es la última usada en ERG manual, recordada entre sesiones (150 W la primera vez). Al salir, se vuelve a SIM con la marcha que había, que se conserva sin enviarse mientras dura el ERG.
- En ERG manual, las palancas ajustan la potencia como en ERG de Zwift: 10 W por cada marcha que pedirían, en el mismo sentido; plato, marcha más dura y marcha más ligera no hacen nada; □ corta vuelve a la potencia con la que se entró en ERG manual; □ larga sigue fijando la marcha de referencia.
- Nuevas acciones asignables **«+10 W»** y **«−10 W»**, que se repiten al mantener, y **«Alternar SIM ⇄ ERG»**. Por defecto, en todas las transmisiones, △ larga alterna (anexo A.2 de `funcional.md`); «+10 W» y «−10 W» quedan sin botón, porque en ERG las palancas ya ajustan la potencia (decisión del usuario al validar la maqueta: Campag. D + cruceta ↓ haría además un cambio de sentido en Zwift). «+10 W» y «−10 W» funcionan también en ERG de Zwift; en SIM no hacen nada.
- Convivencia con Zwift: en una ruta de Zwift, el ERG manual manda sobre el terreno hasta volver a SIM (con el terreno que Zwift haya enviado entre tanto). Si Zwift empieza un entrenamiento ERG, manda Zwift y el ERG manual termina. En ERG de Zwift, alternar no hace nada y el pie lo explica.
- Las configuraciones guardadas reciben la regla nueva (△ larga) al leerse, una sola vez, solo en las entradas y gestos que estén libres: no se pisa ninguna reasignación del usuario.
- Rodaje muestra «ERG MANUAL» y la potencia objetivo en el lugar de la marcha, en horizontal y en vertical; avisos de la acción al alternar y al ajustar. Diagnóstico y la notificación de la sesión muestran el ERG manual.

## Capabilities

### New Capabilities

- `manual-erg`: modo ERG manual del Neo: activarlo y desactivarlo, potencia inicial recordada, ajuste con las palancas y con ±10 W, límites, marcha conservada, convivencia con Zwift y con la pérdida del Neo o del Square.

### Modified Capabilities

- `button-mapping`: nuevas acciones asignables («+10 W», «−10 W», «Alternar SIM ⇄ ERG»), la regla de △ larga en la asignación por defecto de cada transmisión y su incorporación a las configuraciones guardadas.
- `ride-screen`: modo «ERG MANUAL» en la barra superior, potencia objetivo también en ERG manual y avisos de la acción para alternar y para los ajustes de potencia.
- `zwift-bridge`: en ERG de Zwift, «+10 W» y «−10 W» ajustan la potencia y alternar SIM ⇄ ERG no hace nada; un entrenamiento de Zwift termina el ERG manual.
- `diagnostics`: la sección Simulación muestra el modo ERG manual con su potencia.
- `ride-session`: la notificación muestra la potencia objetivo también en ERG manual.

## Impact

- `:nucleo`: `ModoControl` (modo `ErgManual`), `ControlRodillo` (entrar, salir, ajustar y convivencia con Zwift), `AjusteErg` (acciones de vatios), `Accion` (acciones nuevas y su orden), `Asignacion` (reglas por defecto), `Configuracion` (versión 3 con migración que añade las reglas) y `EstadoGuardado` (última potencia de ERG manual).
- `:app`: `Sesion` (camino de las acciones nuevas, avisos y vibración, guardar la potencia), Rodaje, Diagnóstico, Botones y Asignar acción (textos de las acciones nuevas), notificación del `RideService` y textos.
- Sistema de diseño y maquetas: marcha, zona ERG y datos de `MetricGrid` centrados en horizontal en su zona, en las dos orientaciones (todos los modos); variante de `ModeBadge` / `ErgTarget` para ERG manual (sin «W de Zwift»), estados nuevos en `maquetas/rodaje.html`, `rodaje-vertical.html`, `diagnostico.html` y `botones.html`.
- Sin dependencias nuevas. Sin cambios de protocolo: la página FE-C 0x31 ya está validada.
- Documentos de `docs/`: `funcional.md` (F7.2 y F7.3 con lo decidido; anexo A.2), `arquitectura.md` (§ 4.3 `AjusteErg`, § 5.3 acciones, § 5.4 `ErgManual`, que hoy dice que las palancas cambian de marcha, y § 10 versión 3 y última potencia), `planificacion.md` (fila y notas del 9) y `desarrollo.md` (ninguno salvo que cambie algo del despliegue).

## Fuera de alcance

- Alternar SIM ⇄ ERG o ajustar la potencia desde la pantalla del móvil (botones táctiles, F11.8, change `rodaje-completo-y-ajustes`).
- Fijar la potencia inicial en Ajustes o elegir el paso de vatios (±25 W, etc.): el paso es fijo, 10 W, y los saltos se hacen con la repetición o con ±3 marchas (±30 W).
- Protecciones del ERG manual (rampa suave, alivio con cadencia baja), que llegan con el modelo físico (change `modelos-tabla-y-fisico`).
- Recordar el modo entre sesiones: la app siempre empieza en SIM.
- Que el ERG manual se imponga a un entrenamiento ERG de Zwift.
