# Proposal

## Why

La app ya no solo sirve para Zwift, pero lo que publica hacia las plataformas de indoor cycling está repartido y con nombres de Zwift: tres ajustes sueltos al final de Ajustes (conexión, mando, «Simular un KICKR BIKE»), una fila de Zwift y otra del mando en Dispositivos, y los iconos de Rodaje. Al mismo tiempo, Botones ocupa una pestaña entera para algo que se toca poco. Hace falta separar con claridad cuatro cosas: rodar, **a qué se conecta** la app (Dispositivos), **qué simula y publica** ante las plataformas (nueva pestaña) y los ajustes de la app. Es además el sitio natural donde entrarán el FTMS por Bluetooth y otras plataformas (change 11), sin que el usuario tenga que acertar combinaciones de interruptores.

Se parte de F8.5, F9, F12.1, F12.4 y F12.9 de `docs/funcional.md`.

## What Changes

- **Barra inferior de cuatro pestañas: Rodar, Dispositivos, Plataformas y Ajustes.** Botones deja de ser pestaña y pasa a ser una entrada de Ajustes (con «Marchas», «Datos en pantalla» y «Diagnóstico») que abre su pantalla como secundaria, con botón de volver a Ajustes; Asignar acción vuelve a Botones. «Pantalla anterior» y «Pantalla siguiente» recorren las cuatro pestañas nuevas.
- **Nueva pestaña «Plataformas»**: lo que la app simula y publica. Se elige **una plataforma** (hoy solo Zwift; las demás aparecerán cuando existan) y la pantalla ofrece solo los modos de conexión **implementados** para ella, con el recomendado marcado. Nunca hay selecciones múltiples que obliguen a acertar: cada elección deja un modo compatible.
  - **Zwift · Rodillo por wifi (FTMS, DirCon)**, el de siempre: con el interruptor «Mando de Zwift», que lo anuncia por Bluetooth.
  - **Zwift · Zwift Ride (todo por wifi)**: un solo dispositivo por wifi con el mando **siempre dentro de DirCon**; no hay mando Bluetooth aparte ni interruptor del mando (**BREAKING** respecto a hoy, donde en este modo se podía desactivar el mando).
  - **Nombre ante la app**: un selector («Smart Bike» o «KICKR BIKE PRO») que sustituye al interruptor «Simular un KICKR BIKE».
  - Muestra también el estado de Zwift y del mando y cómo emparejarlos (filas que salen de Dispositivos), incluido «No visible».
- **Dispositivos** queda solo con lo que la app consume: Neo, Square, ventilador y pulsómetro. Las filas de Zwift y del mando pasan a Plataformas; en Rodaje, los iconos de Zwift y del mando salen de la barra de conexiones (que queda con Neo, Square, pulsómetro y ventilador, y el Square con el icono de mando de consola) y el estado de la plataforma pasa a un chip con palabras en la fila de Ajustes, con el modo en la fila de debajo («Conectado a Zwift», «Esperando conexión a Zwift…»), que abre Plataformas.
- **Ajustes** pierde la sección «Zwift» y gana la entrada «Botones».
- **Configuración**: `conexionZwift`, `mandoZwift` y `simularKickrBike` pasan a plataforma, modo, mando e identidad, con migración de la versión 7 a la 8 que conserva el modo y la identidad que el usuario tenía (y, por tanto, lo emparejado en Zwift).
- **Rodaje en ERG sin la línea explicativa.** Se quitan «Entrenamiento de Zwift · las palancas ajustan la potencia» y «ERG manual · las palancas ajustan la potencia»: la insignia del modo ya lo dice y el aviso «+10 W» lo muestra al usar las palancas.
- **Se descarta el change 13c (`marchas-de-zwift`).** La app implementa sus propias marchas virtuales y no se acopla a un protocolo de Zwift que no es público; lo que se emula del protocolo de Zwift es solo lo necesario para conectar rodillo y mando/Square por wifi.
- **Cambio de UI: maqueta primero.** Etapa 1 (sistema de diseño y maquetas) → puerta de validación del usuario → etapa 2 (app).

### Fuera de alcance

- FTMS por Bluetooth y otras plataformas (MyWhoosh, Rouvy…): change 11 `puente-zwift-bluetooth`. Lo que no está implementado no aparece en la pantalla; la tabla de compatibilidad queda preparada para añadirlo.
- Publicar más de un modo a la vez: no se permite ni se ofrece.
- Cambiar el comportamiento de los protocolos: DirCon, el mando, las identidades por modo y las marchas siguen como están.
- Un mando Zwift Ride por Bluetooth aparte del DirCon.
- Más identidades que «Smart Bike» y «KICKR BIKE PRO» (y su validación con Zwift más allá de lo ya probado).
- Una opción «sin plataforma»: el estado de la plataforma se muestra siempre, con la elegida.

## Capabilities

### New Capabilities

- `platform-settings`: la pestaña «Plataformas»: elección de plataforma y de modo de conexión compatible, mando, nombre ante la app, estado de la plataforma y del mando, tabla de compatibilidad con lo validado y lo «a validar», y la configuración guardada con su migración.

### Modified Capabilities

- `app-navigation`: barra de cuatro pestañas (Rodar, Dispositivos, Plataformas, Ajustes), Botones como pantalla secundaria de Ajustes, Atrás y «Pantalla anterior/siguiente».
- `settings-screen`: «Ajustes como pestaña» con la entrada Botones y sin sección «Zwift»; las tres exigencias de Zwift (mando, conexión, KICKR BIKE) pasan a `platform-settings`.
- `button-mapping`: Botones pasa de pestaña a pantalla secundaria de Ajustes.
- `device-connection`: Dispositivos sin las filas de Zwift y del mando.
- `ride-screen`: el modo en ERG sin línea explicativa de las palancas; la barra de conexiones queda solo con los dispositivos; el estado de la plataforma en Rodaje es un chip en palabras bajo la fila del modo.
- `zwift-bridge`: «Zwift no visible» se muestra en Plataformas; el nombre ante Zwift sale del selector de identidad; el modo de siempre se llama «Rodillo por wifi (FTMS)».
- `zwift-controller`: en Zwift Ride el mando siempre va dentro de DirCon; el mando por Bluetooth es el interruptor del modo de rodillo por wifi; cambio de modo desde Plataformas.
- `diagnostics`: el nombre anunciado a Zwift sigue al selector de identidad.
- `ui-components`: navegación inferior con Plataformas y su icono; el icono propio de Botones pasa a la entrada de Ajustes; el Square lleva el icono de mando; chip de estado de la plataforma.
- `screen-mockups`: maquetas de Plataformas, Ajustes, Dispositivos y Botones, y navegación entre ellas.

## Impact

- **Código**: `Navegacion.kt` (`Pestana`, `Rutas`), pantallas `ui/botones`, `ui/ajustes`, `ui/dispositivos`, nueva `ui/plataformas`; `Configuracion` y `ModoConexionZwift` en `:core` (versión 8 y migración); `Sesion` y `AppGraph` (identidad en lugar del interruptor); `ui/rodaje` (destino de los iconos de Zwift y del mando); recursos de texto e iconos.
- **Sistema de diseño**: icono de la pestaña Plataformas en `icons.svg` (y su derivado `ic_*.xml`); componente de elección de plataforma y de modo (posible reutilización de `SettingsList`) con su `README.md` y `preview.html`; guía regenerada.
- **Maquetas**: nueva `maquetas/plataformas.html`; cambian `ajustes.html`, `dispositivos.html`, `botones.html`, `asignar.html`, `index.html` y la barra inferior de todas; capturas de la web pública (`site/`) a regenerar.
- **Tests**: `Configuracion*Test` (migración), `AjustesUiStateTest`, `DispositivosUiStateTest`, `BotonesUiStateTest`, `RodajeUiStateTest`, pruebas de navegación y de instrumentación afectadas, y los tests de sincronía del diseño.
- **Documentos de `docs/`**: `funcional.md` (F8.5, F9, F12.1, F12.4 y F12.9; se anota que las marchas virtuales de Zwift no se emulan), `arquitectura.md` (navegación, configuración v8, tabla de compatibilidad), `planificacion.md` (fila 23 y 13c «Descartado»; el 11 hereda la pestaña) y `desarrollo.md` si cambia algo de las maquetas o la web. 
- **Validación con el equipo real**: la migración no debe obligar a emparejar de nuevo en Zwift (mismo modo, misma identidad); cambiar de modo e identidad rodando en Zwift en PC; «No visible» sigue funcionando. Nada de lo que ya estaba validado cambia de protocolo.
