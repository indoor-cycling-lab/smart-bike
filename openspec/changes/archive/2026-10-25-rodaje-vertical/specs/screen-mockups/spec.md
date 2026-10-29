# Spec Delta

## MODIFIED Requirements

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie. SHALL tener también los estados lineales sin Zwift: sin Zwift, tope en lineal, Square sin conexión y sin dispositivos, con la marcha y su escalera en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven los chips del Neo y del Square sin chip de Zwift, «Lineal · 24 marchas», «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los chips afectados dicen «sin conexión», enlazan con Diagnóstico, y el pie dice «Toca Neo o Square para conectarlos»
