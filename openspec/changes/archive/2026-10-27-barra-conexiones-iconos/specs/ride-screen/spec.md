## RENAMED Requirements

- FROM: `### Requirement: Dispositivos desde los chips`
- TO: `### Requirement: Dispositivos desde la barra de conexiones`

## MODIFIED Requirements

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar las conexiones como una barra de iconos, uno por enlace en uso y en orden fijo: Neo, Square, Zwift (el rodillo) y, con «Mando de Zwift» activado y si el móvil puede hacer de mando, el mando. Cada icono SHALL mostrar su estado por la forma: normal si está conectado; atenuado con una marca girando mientras la app intenta conectarlo (el Neo, el Square), se anuncia sin Zwift conectado (Zwift) o espera a Zwift (el mando); y tachado, en el color de alerta, si no lo intenta, sin wifi, con Zwift «No visible» (spec `zwift-bridge`) o con el mando sin conexión. El rodillo y el mando SHALL mostrar cada uno su propio estado. El estado en palabra SHALL leerse en la descripción accesible de la barra («Conexiones: Neo, conectado; Square, sin conexión; Zwift, esperando; mando, esperando») y en Dispositivos. La barra SHALL NOT mostrar nombres ni cambiar de ancho al cambiar de estado.

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square, Zwift y el mando están conectados
- **THEN** se ven los iconos del Neo, del Square, de Zwift y del mando, normales, sin marcas

#### Scenario: Sin Zwift
- **WHEN** el Neo y el Square están conectados, el móvil tiene wifi y Zwift no está conectado
- **THEN** los iconos del Neo y del Square se ven normales, y los de Zwift y del mando atenuados con la marca girando, sin ninguna palabra

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** el icono de Zwift se ve tachado, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Zwift no visible
- **WHEN** se rueda sin Zwift conectado, con «Mando de Zwift» desactivado, y la app muestra Zwift como «No visible»
- **THEN** el icono de Zwift se ve tachado, no hay icono del mando, la barra no cambia de ancho, la descripción accesible dice «Zwift, no visible», y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Zwift no visible con el mando
- **WHEN** «Mando de Zwift» está activado, el mando espera y la app muestra Zwift como «No visible»
- **THEN** el icono de Zwift se ve tachado y el del mando atenuado con la marca girando, y la descripción accesible dice «Zwift, no visible; mando, esperando»

#### Scenario: Zwift vuelve a ser visible
- **WHEN** el icono de Zwift está tachado por «No visible» y el anuncio vuelve a responder
- **THEN** el icono pasa a atenuado con la marca girando, sin que cambie el ancho de la barra ni se mueva el resto de la pantalla

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** el icono del Square pasa a atenuado con la marca girando, sin que cambie el ancho de la barra, la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** el icono del Square se ve tachado y la descripción accesible dice «Square, sin conexión»

#### Scenario: Estado al tocar
- **WHEN** el icono del Square está atenuado con la marca girando y se toca la barra
- **THEN** se abre Dispositivos, donde la fila del Square dice «Conectando»

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** los iconos de Zwift y del mando pasan a atenuados con la marca girando y el resto de la pantalla no cambia

#### Scenario: Rodillo conectado y mando esperando
- **WHEN** «Mando de Zwift» está activado, Zwift está conectado al rodillo y aún no al mando
- **THEN** el icono de Zwift se ve normal, el del mando atenuado con la marca girando, y la descripción accesible dice «Zwift, conectado; mando, esperando»

#### Scenario: Mando sin conexión
- **WHEN** «Mando de Zwift» está activado, Zwift está conectado al rodillo y el mando no puede anunciarse (por ejemplo, con el Bluetooth apagado)
- **THEN** el icono de Zwift se ve normal y el del mando, tachado

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y Zwift está conectado al rodillo
- **THEN** el icono de Zwift se ve normal y no hay icono del mando

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y el Neo se está conectando
- **THEN** la marca del icono del Neo no gira y el icono sigue atenuado con la marca, distinto del tachado

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo la barra de conexiones, a la derecha de la franja superior (la de la cámara y el estado del móvil, de al menos 48 dp de alto, con el resto de la pantalla debajo); el modo con la transmisión y el icono de Ajustes en una sola línea; la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas en su área fija, al pie el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados, la marcha es la 14 y los datos son los de por defecto
- **THEN** se ven, sin desplazamiento, la barra con los iconos del Neo y del Square, «SIM» y «Lineal · 24 marchas» con el icono de Ajustes, «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Seis datos en vertical
- **WHEN** el móvil está en vertical con los 6 datos elegidos
- **THEN** los datos se ven en 2 columnas y 3 filas, sin desplazamiento y sin cifras cortadas

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los iconos del Neo y del Square se ven tachados, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexiones en una línea en vertical
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión, Zwift esperando y el mando esperando
- **THEN** los cuatro iconos caben en la franja superior, a la derecha, y debajo caben en una sola línea el modo con la transmisión y el icono de Ajustes; al pasar todo a conectado, la marcha y los datos no se mueven

#### Scenario: Móvil sin cámara en el borde
- **WHEN** el móvil está en vertical y no tiene cámara en el borde superior, de modo que no hay margen seguro arriba
- **THEN** la barra ocupa una franja propia de al menos 48 dp, la línea del modo con Ajustes queda debajo sin solaparse, y todo se ve sin desplazamiento

#### Scenario: Transmisión larga junto a Ajustes
- **WHEN** el móvil está en vertical a 360 dp, la transmisión es «Shimano 105 · automático» y el modo es SIM, o se está en ERG de Zwift
- **THEN** el modo con la transmisión se lee entero, en una o dos líneas, sin cortarse ni tapar el icono de Ajustes

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Cinco datos en vertical
- **WHEN** el móvil está en vertical con 5 datos elegidos
- **THEN** los cuatro primeros se ven en 2 columnas y 2 filas y el quinto, debajo, centrado en el ancho de la rejilla

### Requirement: Potencia objetivo en ERG de Zwift
En un entrenamiento ERG de Zwift, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia objetivo que se envía al Neo en grande, con la potencia que pide Zwift debajo y, si las palancas la han ajustado, el ajuste; la marcha SHALL NOT mostrarse mientras dure el ERG. La zona SHALL ir centrada en horizontal en su columna.

#### Scenario: Bloque sin ajuste
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** se lee «230 W» en grande con «230 W de Zwift» debajo, sin ajuste, y no se ve la marcha ni su escalera

#### Scenario: Con ajuste de las palancas
- **WHEN** Zwift pide 230 W y se ha pulsado la palanca D1 una vez
- **THEN** se lee «240 W» en grande, «230 W de Zwift» y «+10 W con las palancas»

#### Scenario: ERG en vertical
- **WHEN** el móvil está en vertical y Zwift pide 230 W
- **THEN** la potencia objetivo ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift vuelve a enviar el terreno tras un entrenamiento con la marcha 14
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG
- **WHEN** Zwift pide 230 W y se pierde la conexión con el Neo
- **THEN** la potencia objetivo sigue visible, la potencia y la cadencia muestran «—» y el icono del Neo pasa a atenuado con la marca girando

#### Scenario: Tope de potencia
- **WHEN** la potencia objetivo es 50 W y se pulsa la palanca I1
- **THEN** «50 W» no cambia y el pie dice «Tope · 50 W» durante 1,5 s

### Requirement: Potencia objetivo en ERG manual
En ERG manual, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia fijada en grande, sin línea de Zwift ni de ajuste, centrada en horizontal en su columna; la marcha SHALL NOT mostrarse mientras dure el ERG manual.

#### Scenario: ERG manual a 200 W
- **WHEN** se entra en ERG manual a 200 W
- **THEN** se lee «200 W» en grande, sin «de Zwift», y no se ve la marcha ni su escalera

#### Scenario: Ajuste
- **WHEN** en ERG manual a 200 W se pulsa la palanca D1
- **THEN** la cifra pasa a «210 W»

#### Scenario: ERG manual en vertical
- **WHEN** el móvil está en vertical y se está en ERG manual
- **THEN** la potencia fijada ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Vuelta a SIM
- **WHEN** en ERG manual con la marcha 14 se vuelve a SIM
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG manual
- **WHEN** en ERG manual a 200 W se pierde la conexión con el Neo
- **THEN** «200 W» sigue visible, la potencia y la cadencia muestran «—» y el icono del Neo pasa a atenuado con la marca girando

#### Scenario: Tope de potencia
- **WHEN** en ERG manual a 1000 W se pulsa la palanca D1
- **THEN** «1000 W» no cambia y el pie dice «Tope · 1000 W» durante 1,5 s

### Requirement: Dispositivos desde la barra de conexiones
Al tocar la barra de conexiones, con un objetivo táctil de al menos 48 dp de alto, la app SHALL abrir Dispositivos. Mientras la app intenta conectar algún dispositivo, el pie SHALL decir cómo despertarlo; si falta alguno y no lo intenta, SHALL decir «Toca las conexiones para conectar el Neo y el Square». El estado de Zwift y del mando SHALL NOT cambiar lo que dice el pie.

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app sin dispositivos recordados
- **THEN** los iconos del Neo y del Square se ven tachados y la descripción accesible dice «sin conexión» para los dos, la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Neo dormido al abrir
- **WHEN** se abre la app con el Neo y el Square recordados, el Square se conecta y el Neo está dormido
- **THEN** el icono del Neo se ve atenuado con la marca girando y la descripción accesible dice «Neo, conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo» hasta que se conecta

#### Scenario: Square reconectando
- **WHEN** el Neo está conectado y el Square se está reconectando
- **THEN** el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** la app intenta conectar el Neo y el Square a la vez
- **THEN** el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Aviso de acción mientras reconecta
- **WHEN** el Neo se está reconectando y se sube una marcha con la palanca D1
- **THEN** el pie dice «+1 marcha» durante 1,5 s y después vuelve a «Pedalea para despertar el Neo»

#### Scenario: Tocar un chip
- **WHEN** se toca cualquier icono de la barra de conexiones, o el hueco entre dos iconos
- **THEN** se abre Dispositivos con la barra inferior, y «Rodar» o Atrás vuelven a Rodaje

#### Scenario: Tocar el chip de Zwift
- **WHEN** el Neo y el Square están conectados, Zwift no, y se toca la barra
- **THEN** se abre Dispositivos, y el pie de Rodaje no dice nada sobre Zwift

### Requirement: Ajustes desde Rodaje
Rodaje SHALL mostrar en la esquina superior derecha un botón con el icono de Ajustes, el mismo de la pestaña, con objetivo táctil de al menos 48 dp y descripción accesible «Ajustes», separado de la barra de conexiones para que no se toque uno por otro. En horizontal SHALL ir en la línea del modo, a la derecha de la barra; en vertical, al final de la línea del modo, bajo la franja de la barra. Al tocarlo SHALL abrirse Ajustes como pestaña, sin terminar la sesión ni cambiar la marcha.

#### Scenario: Abrir Ajustes en horizontal
- **WHEN** el móvil está en horizontal, se rueda en la marcha 14 y se toca el icono de Ajustes
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada, sin botón de volver, y al tocar «Rodar» se vuelve a Rodaje en la marcha 14

#### Scenario: Abrir Ajustes en vertical
- **WHEN** el móvil está en vertical y se toca el icono de Ajustes, al final de la línea del modo
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada

#### Scenario: Atrás desde Ajustes abierto con el icono
- **WHEN** se abre Ajustes con el icono de Rodaje y se pulsa Atrás
- **THEN** se vuelve a Rodaje y la sesión sigue en marcha

#### Scenario: Línea de chips en vertical con el icono
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión, Zwift esperando y el mando esperando
- **THEN** el modo con la transmisión y el icono de Ajustes caben en una sola línea, bajo la barra de conexiones, y al pasar todo a conectado ni el icono, ni la marcha ni los datos se mueven

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados
- **THEN** el icono de Ajustes se ve en su sitio y abre Ajustes como siempre, y el pie sigue diciendo «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexión perdida al abrir Ajustes
- **WHEN** se abre Ajustes con el icono y, con Ajustes abierto, se pierde la conexión con el Square
- **THEN** la app intenta reconectarlo como desde Rodaje y, al volver a Rodaje, el icono del Square muestra su estado y la marcha es la misma

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la barra superior de Rodaje
- **THEN** anuncia la barra de conexiones como un solo elemento con el estado de cada enlace y, después, el modo y «Ajustes», botón

#### Scenario: En ERG
- **WHEN** se está en ERG manual y se toca el icono de Ajustes
- **THEN** se abre Ajustes, y al volver a Rodaje la potencia objetivo es la misma
