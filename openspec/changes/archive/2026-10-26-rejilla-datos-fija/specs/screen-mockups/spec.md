## ADDED Requirements

### Requirement: Área de datos fija en las maquetas de rodaje
Las maquetas de rodaje, horizontal y vertical, SHALL reproducir el área fija de datos (40 % del alto útil en vertical, 40 % del ancho útil en horizontal), la marcha reducida para caber en el resto, el espacio de los botones − y + tomado primero de la holgura de la rejilla, la cifra medida con el valor más largo de cada dato y el último dato impar centrado. SHALL tener estados de 1 a 6 datos, con y sin botones, en las dos orientaciones.

#### Scenario: Con y sin botones
- **WHEN** se alterna entre el estado de 5 datos sin botones y el de 5 datos con botones, en vertical o en horizontal
- **THEN** la cifra de los datos es la misma en los dos y solo cambian el alto o el ancho de la rejilla y, si hace falta, el tamaño de la marcha

#### Scenario: Último dato impar
- **WHEN** se elige un estado de 3 o 5 datos en vertical, o de 5 en horizontal
- **THEN** el último dato va centrado en el ancho de la rejilla

#### Scenario: Filas alineadas
- **WHEN** se elige el estado de 6 datos
- **THEN** la etiqueta de cada dato queda a la misma distancia del borde de su celda en todas las filas, tenga unidad o no

#### Scenario: Transmisión y modo
- **WHEN** se pasa del estado lineal al de la Shimano o al de ERG con el mismo número de datos
- **THEN** la rejilla de datos no cambia de área, de posición ni de cifra

## MODIFIED Requirements

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones (en una sola línea) y modo, marcha con plato y cassette, rejilla de datos a 2 columnas en su área fija, aviso y, si están activados, botones − y + al pie, sin velocidad ni distancia. SHALL tener también los estados lineales de la maqueta horizontal, sin Zwift y con Zwift, con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

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
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento, con la misma cifra que en el estado de 6 datos sin botones
