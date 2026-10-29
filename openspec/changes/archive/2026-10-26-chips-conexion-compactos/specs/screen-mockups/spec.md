## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal, sin Zwift y con Zwift, de los escenarios siguientes, con los chips enlazando con Dispositivos y mostrando solo el nombre y la forma del estado, sin el estado en palabra. Ningún estado SHALL mostrar velocidad ni distancia.

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
- **THEN** el chip «Neo» se ve con el anillo vacío y el borde de alerta, sin la palabra, la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas, y la rejilla tiene potencia y cadencia, sin aviso de cadencia baja ni datos del intervalo de Zwift

#### Scenario: Sin Zwift
- **WHEN** se elige el estado sin Zwift
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los chips del Neo y del Square con el punto lleno, el chip «Zwift» con el anillo de espera y el aviso «+1 marcha»

#### Scenario: Tope en lineal
- **WHEN** se elige el estado de tope en lineal
- **THEN** «24» aparece atenuado con «Tope · marcha más dura» y la escalera tiene resaltado el último segmento

#### Scenario: Square sin conexión
- **WHEN** se elige el estado de Square sin conexión
- **THEN** el chip «Square» se ve con el anillo vacío y el borde de alerta, la marcha sigue visible, la potencia y la cadencia tienen valor y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado sin dispositivos y se toca un chip
- **THEN** los chips «Neo» y «Square» se ven con el anillo vacío y el borde de alerta, la potencia y la cadencia muestran «—», el pie dice «Toca Neo o Square para conectarlos» y el chip abre la maqueta de Dispositivos

#### Scenario: Neo conectando al abrir
- **WHEN** se elige el estado Neo conectando
- **THEN** el chip «Neo» tiene el anillo de espera, sin la palabra, el del Square tiene el punto lleno, la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo»

#### Scenario: Square reconectando
- **WHEN** se elige el estado Square reconectando
- **THEN** el chip «Square» tiene el anillo de espera, la potencia y la cadencia tienen valor y el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** se elige el estado de los dos conectando
- **THEN** los dos chips tienen el anillo de espera, la potencia y la cadencia muestran «—» y el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Ruta de Zwift
- **WHEN** se elige el estado ruta de Zwift
- **THEN** los chips del Neo, del Square y de Zwift tienen el punto lleno, el modo dice «SIM» con «Lineal · 24 marchas», se ve «14» con «de 24» y la escalera, potencia y cadencia, y no aparece ninguna pendiente

#### Scenario: Entrenamiento ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift en lineal
- **THEN** el modo dice «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia», la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el chip «Zwift» se ve con el anillo vacío y el borde de alerta, el modo dice «SIM», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones (en una sola línea) y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie, sin velocidad ni distancia. SHALL tener también los estados lineales de la maqueta horizontal, sin Zwift y con Zwift, con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven los chips del Neo y del Square con el punto lleno y el chip «Zwift» con el anillo de espera, «Lineal · 24 marchas», «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los chips afectados se ven con el anillo vacío y el borde de alerta, sin la palabra, enlazan con Dispositivos, y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los chips afectados tienen el anillo de espera, el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con los tres chips en la fila de conexiones, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

#### Scenario: Botones y seis datos en vertical
- **WHEN** se elige el estado con botones − y + en vertical
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento

