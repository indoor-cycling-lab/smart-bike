## ADDED Requirements

### Requirement: Shimano 105 en la maqueta de rodaje
Las maquetas de rodaje horizontal y vertical SHALL tener estados con la Shimano 105 (52/36 · 11–36) y potencia y cadencia: automático sin Zwift en la 52×17 con «+1 marcha», cambio de plato automático con «Plato 52» en la 52×19 y, solo en horizontal, el tope de plato en manual. Los estados de marca que ya existían SHALL usar el desarrollo 52/36 · 11–36 en lugar del 50/34 · 11–34.

#### Scenario: Automático sin Zwift
- **WHEN** se elige el estado Shimano automático, en horizontal o en vertical
- **THEN** la barra superior dice «SIM» y «Shimano 105 · automático», se ve «52×17» con el plato 52 relleno y el cassette del 36 al 11 con el 17 resaltado, potencia y cadencia, y el pie dice «+1 marcha»

#### Scenario: Cambio de plato
- **WHEN** se elige el estado de cambio de plato
- **THEN** se ve «52×19» con el plato 52 relleno y el pie dice «Plato 52»

#### Scenario: Tope de plato en manual
- **WHEN** se elige el estado de tope de plato
- **THEN** la barra superior dice «Shimano 105 · manual», «52×17» se ve atenuado y debajo «Tope · plato 52»

#### Scenario: Shimano en vertical sin desplazamiento
- **WHEN** se eligen los estados Shimano en vertical a 360 dp de ancho
- **THEN** la marcha, los platos, el cassette, la potencia y la cadencia se ven sin desplazamiento, y al pasar de un estado Shimano a otro no se mueve nada

### Requirement: Botones con la asignación de la Shimano
La maqueta de Botones SHALL tener un estado con la asignación por defecto de la Shimano, con la línea que dice de qué transmisión es y las acciones de plato tras «−3 marchas».

#### Scenario: Asignación de la Shimano
- **WHEN** se elige el estado de la Shimano
- **THEN** se lee que es la asignación de Shimano 105 y que cada transmisión tiene la suya, «+1 marcha» con la palanca D2, «−1 marcha» con la palanca D1, «Plato más grande» con la palanca I1 y «Plato más pequeño» con la palanca I2

#### Scenario: Asignación de la lineal
- **WHEN** se elige el estado de asignación por defecto
- **THEN** se lee que es la asignación de la lineal y no aparecen las acciones de plato

### Requirement: Diagnóstico con la Shimano
La maqueta de Diagnóstico SHALL tener un estado con la Shimano en el que Simulación muestra la marcha como plato × piñón con su relación y el ajuste por la marcha.

#### Scenario: Simulación con la Shimano
- **WHEN** se elige el estado de la Shimano
- **THEN** Simulación dice marcha 52×17 · relación 3,06, ajuste +5,1 % y enviada al Neo +5,1 %, y «Último gesto» dice «Palanca D2 · pulsar · +1 marcha»

## MODIFIED Requirements

### Requirement: Marchas
La maqueta de Marchas SHALL mostrar la elección de transmisión (Lineal, por defecto, y Shimano 105 2×12 · 52/36 · 11–36), los platos y el cassette de la Shimano con la referencia resaltada, el modo de cambio («Automático», por defecto, y «Manual», con su ayuda), la sección «Al empezar» con la marcha de referencia y «Recordar la última marcha», y la simulación avanzada plegada. SHALL tener los estados lineal, Shimano automático, Shimano manual y simulación avanzada abierta.

#### Scenario: Transmisión lineal
- **WHEN** se elige el estado de transmisión lineal
- **THEN** se ve la escalera de 24 marchas y el número de marchas, y no aparece el modo de cambio con platos

#### Scenario: Shimano automático
- **WHEN** se elige el estado Shimano automático
- **THEN** está elegida «Shimano 105 2×12», se ven los platos 36 y 52 y el cassette del 36 al 11 con la 36×17 resaltada, «Automático» elegido con su ayuda y la marcha de referencia «36×17»

#### Scenario: Shimano manual
- **WHEN** se elige el estado Shimano manual
- **THEN** está elegido «Manual», con la ayuda de que las palancas D cambian el piñón y las I el plato

#### Scenario: Solo dos transmisiones
- **WHEN** se recorren los estados de Marchas
- **THEN** no aparecen SRAM, Campagnolo ni «A medida» como opciones

### Requirement: Ajuste «Bajadas» en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL incluir el ajuste «Bajadas» con sus tres opciones y su ayuda, y la maqueta SHALL tener un estado abierto desde Diagnóstico con la entrada «Marchas», la sección Rodillo y el botón de volver a Diagnóstico.

#### Scenario: Ajustes completa
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección Rodillo muestra «Bajadas» con «Sin protección», «La marcha no crea bajadas» (elegida) y «Nunca empujar», cada una con su ayuda

#### Scenario: Desde Diagnóstico
- **WHEN** en la maqueta de Diagnóstico se toca «Ajustes»
- **THEN** se abre la maqueta de Ajustes con la entrada «Marchas» y la sección Rodillo con el ajuste «Bajadas», volver lleva a Diagnóstico y «Marchas» abre la maqueta de Marchas
