## Why

La barra superior de Rodaje muestra un chip con nombre para el Neo, el Square y Zwift, y ya va justa a 360 dp en vertical. El chip «Zwift» junta dos enlaces que no se pueden distinguir: el rodillo por wifi y el mando por Bluetooth. Su estado sale de combinarlos, y con «No visible» esa mezcla cuesta cada vez más de leer. Además, llegarán más enlaces (banda de pulso, ventilador), y con un chip con nombre por cada uno no caben. Hace falta una forma más compacta, como los iconos de estado de la barra de un móvil, que muestre cada enlace por separado (F1.4, F11.5).

## What Changes

- **BREAKING (visual).** En Rodaje, los chips con nombre se sustituyen por una **barra de conexiones**, con un icono por enlace:
  - Neo: `bolt`.
  - Square: `directions_bike`.
  - Zwift: una «Z» en un recuadro redondeado, propia, que no imita el logo de Zwift.
  - Mando de Zwift: `sports_esports`.
- **Estado por la forma del icono**, sin depender del color:
  - conectado: icono en `ink`, sin marca;
  - conectando o esperando: icono en `ink-muted` con una marca girando en la esquina;
  - sin conexión o no visible: icono en `danger`, tachado.
  
  El ancho de la barra no cambia con el estado.
- **El rodillo y el mando de Zwift van por separado**, cada uno con su icono y su estado. Desaparece la regla que combinaba los dos en el chip «Zwift»: «No visible» y «Sin wifi» tachan la Z y el mando muestra su propio estado.
- **Solo se ven los enlaces que se usan.** El mando no aparece con «Mando de Zwift» desactivado ni si el móvil no puede hacer de mando. El orden es fijo: Neo, Square, Zwift, Mando, y después Pulso y Ventilador cuando existan.
- **La barra es un único objetivo táctil** que abre Dispositivos. Su descripción accesible lee el estado de cada enlace («Conexiones: Neo, conectado; Square, sin conexión; Zwift, esperando»).
- **Disposición:**
  - En horizontal, una línea: modo y transmisión a la izquierda; barra y Ajustes a la derecha, con aire entre ellos.
  - En vertical, la barra va arriba a la derecha, en la franja de la cámara y del estado del móvil, como los iconos de estado del sistema. Debajo, en una sola línea, el modo con la transmisión a la izquierda y Ajustes a la derecha. Con ello la marcha, la escalera y los datos suben una línea. La franja mide como mínimo 48 dp y el contenido empieza siempre debajo de ella, también en móviles sin cámara en el borde.
  - Los iconos miden 18 dp (probado en el OnePlus 8).
- **El pie ya no puede nombrar los chips**, porque ya no se ven los nombres: «Toca Neo o Square para conectarlos» pasa a «Toca las conexiones para conectar el Neo y el Square».
- **Dispositivos y Diagnóstico usan los mismos iconos** junto al nombre de cada enlace, para que sirvan de leyenda. Diagnóstico deja de usar `directions_bike` para el Neo y `sports_esports` para el Square.
- La pauta del sistema de diseño «la pantalla de rodaje no usa iconos» pasa a admitir el icono de Ajustes y la barra de conexiones.

Fuera de alcance:
- La banda de pulso y el ventilador como funcionalidades (no están en `docs/funcional.md`). La barra admite más enlaces y la guía muestra cómo se verían con seis, pero la app no los conecta.
- El estado en palabra en Rodaje: sigue solo en la descripción accesible y en Dispositivos.
- Cambiar `ConnectionChip` fuera de Rodaje: Dispositivos y Diagnóstico mantienen su chip con palabra.
- Los estados de conexión en sí (cuándo se espera, se reconecta o se da por perdido): no cambian.
- El mando por DirCon (change 13b) y el puente por Bluetooth (change 11).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: «Conexiones en la barra superior» pasa a ser la barra de iconos, con el rodillo y el mando por separado. «Disposición en vertical», «Dispositivos desde los chips» y «Ajustes desde Rodaje» se adaptan a la barra y al nuevo texto del pie.
- `ui-components`: requisito nuevo «Barra de conexiones con iconos». «Estado de conexión por forma y palabra» pierde la variante compacta de Rodaje.
- `screen-mockups`: las maquetas de rodaje muestran la barra en todos sus estados, con un estado nuevo «mando sin conexión». Dispositivos y Diagnóstico llevan los iconos de cada enlace.
- `style-guide`: la iconografía admite iconos en Rodaje solo para Ajustes y la barra de conexiones, y documenta el icono propio de Zwift.

## Impact

- Funcionalidades: F1.4 (estado de cada conexión, ahora el mando también por separado) y F11.5 (estado de conexiones en Rodaje).
- Sistema de diseño:
  - Componente nuevo `ConnectionBar` (`sq-connbar`), con `README.md` y `preview.html`.
  - En `icons.svg`, los símbolos `bolt`, `zwift` (propio), `favorite` y `mode_fan`. Los dos últimos solo para la guía, para comprobar el ancho con seis enlaces.
  - Pautas de iconografía en `design-system/README.md` y de `RideScreen` y `ConnectionChip`.
  - Sin tokens nuevos.
- Maquetas: `rodaje.html`, `rodaje-vertical.html` (todos sus estados, más «mando sin conexión»), `dispositivos.html`, `diagnostico.html` e `index.html`.
- App:
  - Composable `ConnectionBar` en `ui/componentes`.
  - `RodajeUiState` con un estado por enlace en lugar de `chipZwift`.
  - La barra superior de `Rodaje.kt` y los iconos de las filas de `Dispositivos` y `Diagnostico`.
  - Drawables generados.
  - Cadena del pie.
- Prueba con el equipo real: en el OnePlus 8, en vertical y en horizontal, con el Neo, el Square y Zwift en PC:
  - conectar y desconectar cada uno;
  - Zwift conectado con el mando esperando;
  - sin wifi;
  - «Mando de Zwift» desactivado.
- Documentos de `docs/`:
  - `planificacion.md`: fila 10f y nota.
  - `funcional.md`: nota en F11 y F1.4.
  - `arquitectura.md`: componentes de UI, si los enumera.
  - `desarrollo.md`: ninguno.
