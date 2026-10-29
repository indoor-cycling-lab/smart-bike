## ADDED Requirements

### Requirement: Mando sin conexión en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado a Zwift y el mando sin conexión, con el icono del mando tachado y el resto como en la ruta de Zwift.

#### Scenario: Mando sin conexión en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando sin conexión, en horizontal y en vertical
- **THEN** los iconos del Neo, del Square y de Zwift se ven normales, el del mando tachado, la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar, junto al nombre del Neo, del Square, de Zwift y del mando, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo, la bicicleta para el Square, la «Z» en recuadro para Zwift y el mando para el mando de Zwift

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos

## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal, sin Zwift y con Zwift, de los escenarios siguientes, con la barra de conexiones (un icono por enlace, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, y con el icono de Ajustes, en la línea del modo y la transmisión. Ningún estado SHALL mostrar velocidad ni distancia.

#### Scenario: Estado por defecto
- **WHEN** se abre la maqueta de rodaje sin estado
- **THEN** se ve la transmisión lineal de 24 marchas con 2 datos (potencia y cadencia) y sin botones − y +

#### Scenario: Transmisiones de marca y datos
- **WHEN** se recorren los estados de rodaje
- **THEN** hay al menos Shimano, SRAM y Campagnolo, y rejillas de 1, 2, 3, 4 y 6 datos elegidos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima

#### Scenario: Botones táctiles
- **WHEN** se elige el estado con botones − y +
- **THEN** aparecen a la derecha, de 72 dp, con 4 datos visibles a la vez

#### Scenario: Tope
- **WHEN** se elige el estado de tope
- **THEN** la marcha aparece atenuada con la etiqueta de tope

#### Scenario: Conexión perdida
- **WHEN** se elige el estado de Neo desconectado
- **THEN** el icono del Neo se ve tachado, la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas, y la rejilla tiene potencia y cadencia, sin aviso de cadencia baja ni datos del intervalo de Zwift

#### Scenario: Sin Zwift
- **WHEN** se elige el estado sin Zwift
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los iconos del Neo y del Square normales, el de Zwift atenuado con la marca girando y el aviso «+1 marcha»

#### Scenario: Tope en lineal
- **WHEN** se elige el estado de tope en lineal
- **THEN** «24» aparece atenuado con «Tope · marcha más dura» y la escalera tiene resaltado el último segmento

#### Scenario: Square sin conexión
- **WHEN** se elige el estado de Square sin conexión
- **THEN** el icono del Square se ve tachado, la marcha sigue visible, la potencia y la cadencia tienen valor y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado sin dispositivos y se toca la barra de conexiones
- **THEN** los iconos del Neo y del Square se ven tachados, la potencia y la cadencia muestran «—», el pie dice «Toca las conexiones para conectar el Neo y el Square» y la barra abre la maqueta de Dispositivos

#### Scenario: Neo conectando al abrir
- **WHEN** se elige el estado Neo conectando
- **THEN** el icono del Neo está atenuado con la marca girando, el del Square se ve normal, la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo»

#### Scenario: Square reconectando
- **WHEN** se elige el estado Square reconectando
- **THEN** el icono del Square está atenuado con la marca girando, la potencia y la cadencia tienen valor y el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** se elige el estado de los dos conectando
- **THEN** los iconos del Neo y del Square están atenuados con la marca girando, la potencia y la cadencia muestran «—» y el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Ruta de Zwift
- **WHEN** se elige el estado ruta de Zwift
- **THEN** los iconos del Neo, del Square, de Zwift y del mando se ven normales, el modo dice «SIM» con «Lineal · 24 marchas», se ve «14» con «de 24» y la escalera, potencia y cadencia, y no aparece ninguna pendiente

#### Scenario: Entrenamiento ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift en lineal
- **THEN** el modo dice «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia», la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el icono de Zwift se ve tachado, el modo dice «SIM», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar la barra de conexiones, a la derecha de la franja superior (la de la cámara y el estado del móvil); debajo, en una sola línea, el modo con la transmisión y el icono de Ajustes; y después marcha con plato y cassette, rejilla de datos a 2 columnas en su área fija, aviso y, si están activados, botones − y + al pie, sin velocidad ni distancia. SHALL tener también los estados lineales de la maqueta horizontal, sin Zwift y con Zwift, con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven en la franja superior los iconos del Neo y del Square normales y el de Zwift atenuado con la marca girando, «Lineal · 24 marchas» con Ajustes, «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los iconos afectados se ven tachados, la barra enlaza con Dispositivos, y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los iconos afectados están atenuados con la marca girando, el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con la barra de conexiones en su franja, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

#### Scenario: Botones y seis datos en vertical
- **WHEN** se elige el estado con botones − y + en vertical
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento, con la misma cifra que en el estado de 6 datos sin botones

### Requirement: Acceso a Ajustes en las maquetas de rodaje
Todos los estados de las maquetas de rodaje, en horizontal y en vertical, SHALL mostrar a la derecha, bajo la franja de la barra de conexiones en vertical, el botón con el icono de Ajustes, enlazado con la maqueta de Ajustes, con descripción accesible «Ajustes» y objetivo táctil de `touch-min`, en los dos temas.

#### Scenario: Horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado
- **THEN** el icono de Ajustes está en la línea del modo, a la derecha de la barra de conexiones y separado de ella, y al tocarlo se abre `maquetas/ajustes.html`

#### Scenario: Vertical a 360 dp
- **WHEN** se abre `maquetas/rodaje-vertical.html` en el estado «Sin Zwift · Neo y Square conectando» con el marco a 360 dp de ancho
- **THEN** la barra de conexiones queda en la franja superior, a la derecha, y debajo caben en una sola línea el modo con la transmisión y el icono de Ajustes al final

#### Scenario: Rodaje en claro
- **WHEN** se abre `maquetas/rodaje.html#tema=claro` y `maquetas/rodaje-vertical.html#tema=claro`
- **THEN** la pantalla de rodaje y el icono de Ajustes se ven con los valores claros de los tokens y el icono se distingue del fondo

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado «Sin Zwift · sin dispositivos»
- **THEN** el icono de Ajustes sigue en su sitio, junto a la barra con los iconos del Neo y del Square tachados

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado a Zwift y el mando esperando, con el icono de Zwift normal, el del mando atenuado con la marca girando y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando esperando, en horizontal y en vertical
- **THEN** los iconos del Neo, del Square y de Zwift se ven normales, el del mando atenuado con la marca girando, la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Zwift no visible en las maquetas de rodaje
Las maquetas de rodaje y de rodaje en vertical SHALL tener el estado «Zwift no visible», con el icono de Zwift tachado, el del mando atenuado con la marca girando, y el resto como en el estado sin Zwift.

#### Scenario: Rodaje con Zwift no visible
- **WHEN** se elige el estado Zwift no visible en la maqueta de rodaje, en horizontal o en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el de Zwift tachado y el del mando atenuado con la marca girando, la descripción de la barra dice «Zwift, no visible; mando, esperando», la barra enlaza con Dispositivos, y el resto se ve como en el estado sin Zwift, sin moverse
