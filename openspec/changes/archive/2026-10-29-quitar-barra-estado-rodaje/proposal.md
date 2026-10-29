# Proposal

## Why

Rodaje oculta las barras del sistema y por eso el change `barra-estado-rodaje` (27-10-2026) dibujó dentro de la app la hora, el wifi y la batería del móvil (F11.10). Es una réplica de lo que Android ya pinta, con su propio adaptador, su componente, su colocación esquivando la cámara y sus iconos. Dejar las barras del sistema a la vista da la misma información sin código propio y libera la franja superior de la pantalla vertical, que existía para la cámara y el estado del móvil.

Al quitar el grupo, la disposición de la fila superior se rehace: las conexiones suben a la fila de la plataforma, Ajustes baja a la fila del modo, y los iconos de la barra de conexiones se agrupan, porque con un hueco de 10 dp entre iconos de 14 dp se ven sueltos.

## What Changes

- **BREAKING (F11.10)**: Rodaje deja de ocultar las barras del sistema. La hora, el wifi y la batería los muestra Android; la app deja de mostrarlos. Se retira F11.10 y se reescribe el requisito de «Rodaje como pantalla inicial» (ya no es «a pantalla completa y sin las barras del sistema»). La pantalla sigue siempre encendida.
- Se elimina la réplica y su código muerto: `DeviceStatus` (componente, estilos `sq-devstatus*`, vista previa y pauta), `AdaptadorEstadoMovil`, `EstadoMovil`, `Bateria`, `colocarFranja`/`Tramo`/`Colocacion`, `iconoBateria`, los iconos `ic_battery_android_*` e `ic_wifi` y sus símbolos de `icons.svg`, el cableado en `AppGraph`, `RodajeViewModel`, `RodajeUiState` y `Rodaje.kt`, los textos, los tests y los estados de las maquetas (`sin-wifi` del móvil, batería baja, cargando, cámara en el centro) y las capturas del sitio.
- Rodaje en vertical pierde la franja superior (`sq-ride__strip`). La barra de conexiones pasa a la derecha de la fila de la plataforma, donde estaba Ajustes, y Ajustes baja al final de la fila del modo (SIM/ERG), con objetivo táctil de 48 dp.
- Rodaje en horizontal pierde el grupo central: la plataforma queda a la izquierda y las conexiones con Ajustes a la derecha, con menos hueco entre la barra y Ajustes (de `space-6` a `space-3`).
- La barra de conexiones se agrupa: el hueco entre iconos baja de 10 dp a 4 dp (`space-1`) en el sistema de diseño y en la app. La marca de espera y el tachado se revisan con estados mezclados.
- Se mantienen el wifi tachado del icono de Zwift y el estado «Sin wifi» de la plataforma (vienen de `AdaptadorWifi`) y el permiso `ACCESS_NETWORK_STATE`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: Rodaje deja las barras del sistema visibles; desaparecen los requisitos de la hora, el wifi y la batería (contenido, colocación vertical y horizontal, solo informativos); la disposición vertical sin franja superior, con las conexiones en la fila de la plataforma y Ajustes en la fila del modo; la disposición horizontal sin el grupo central; los escenarios de las barras de conexiones, del pulsómetro y del ventilador dejan de nombrar el grupo.
- `ui-components`: se retira el componente de indicadores de estado del móvil; la barra de conexiones se agrupa (hueco de 4 dp).
- `screen-mockups`: las maquetas de rodaje pierden los indicadores del móvil y sus estados y se rehacen en vertical (conexiones y Ajustes) y en horizontal.
- `style-guide`: la guía deja de mostrar los indicadores del móvil y las pautas de iconos de Rodaje dejan de permitirlos.

## Impact

- **Fuera de alcance**: el tema de las barras del sistema (`app-theme` ya fija el color de sus iconos), el estado de la plataforma y su texto, el wifi de `AdaptadorWifi`, el contenido de la barra de conexiones (orden, estados y descripción accesible), y cualquier cambio en Ajustes, Dispositivos o Diagnóstico.
- **Código**: `:core` (`EstadoMovil`, `Bateria`, `ColocacionFranja` y sus tests); `:app` (`movil/AdaptadorEstadoMovil`, `ui/componentes/DeviceStatus.kt`, `ConnectionBar.kt`, `ui/rodaje/*`, `AppGraph`, `strings.xml`, drawables generados); tests unitarios y de instrumentación asociados.
- **Sistema de diseño y maquetas**: `components.css`, `components/DeviceStatus/`, `icons.svg` (símbolos `wifi` y `battery_android_*`), guía generada, `maquetas/rodaje*.html`, `maquetas/index.html`, `site/capturas.json` y capturas.
- **Documentos**: `docs/funcional.md` (F11.10 retirada), `docs/arquitectura.md` (componentes y decisión de las barras), `docs/planificacion.md` (fila del change y nota en `barra-estado-rodaje`), `docs/desarrollo.md` solo si menciona la prueba de los indicadores.
- **Prueba con el equipo**: en el OnePlus 8, en vertical y en horizontal, comprobar que las barras del sistema se ven y no tapan nada, que Android esquiva la cámara, que la fila de la plataforma y las conexiones caben a 360 dp con los cuatro iconos, y que Ajustes se toca bien junto a SIM/ERG.
