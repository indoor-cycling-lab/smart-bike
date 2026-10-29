# Spec Delta

## ADDED Requirements

### Requirement: Pantalla baja en vertical
En vertical, cuando el alto de la pantalla de rodaje es menor de 760 dp, la pantalla SHALL usar una disposición compacta: menos espacio entre sus bloques y el cassette más bajo, sin quitar ningún elemento ni cambiar su orden. Con 700 dp o más, la disposición SHALL ser la de siempre.

#### Scenario: Pantalla baja
- **WHEN** Rodaje está en vertical y mide 360 × 640 dp
- **THEN** el espacio entre los bloques es de 8 dp en vez de 16 dp y el cassette mide 40 dp en vez de 56 dp (la escalera del modo lineal no cambia), con los mismos elementos y en el mismo orden

#### Scenario: Pantalla alta
- **WHEN** Rodaje está en vertical y mide 390 × 844 dp
- **THEN** la disposición es la de siempre, sin compactar

#### Scenario: Horizontal
- **WHEN** Rodaje está en horizontal, con cualquier alto
- **THEN** la disposición es la de siempre

### Requirement: La marcha no desaparece en pantalla baja
En vertical, con la pantalla baja, la marcha SHALL verse siempre, reducida lo necesario para caber, a no menos de la mitad de su tamaño con la combinación de datos y botones que la app permite en esa pantalla.

#### Scenario: Cuatro datos con botones en pantalla baja
- **WHEN** Rodaje en vertical mide 360 × 640 dp, la marcha es 48 × 19 y hay 4 datos con los botones − y + activados
- **THEN** se ven, sin desplazamiento, la marcha 48 × 19 con su plato y su cassette, los 4 datos y los botones − y +

#### Scenario: Seis datos sin botones en pantalla baja
- **WHEN** Rodaje en vertical mide 360 × 640 dp, la marcha es 52 × 17 y hay 6 datos con los botones desactivados
- **THEN** se ven, sin desplazamiento, la marcha 52 × 17 con sus platos y su cassette y los 6 datos

### Requirement: Datos con botones en pantalla baja
En vertical, con la pantalla baja y los botones − y + activados, Rodaje SHALL mostrar como máximo los 4 primeros datos elegidos, en su orden. Los demás SHALL ocultarse sin cambiar los datos elegidos en Ajustes, y SHALL volver a verse al desactivar los botones o en una pantalla más alta.

#### Scenario: Seis datos elegidos con botones
- **WHEN** hay 6 datos elegidos, los botones están activados y Rodaje en vertical mide 360 × 640 dp
- **THEN** Rodaje muestra los 4 primeros datos y los botones, y no muestra el quinto ni el sexto

#### Scenario: Quitar los botones
- **WHEN** en esa situación se desactivan los botones en Ajustes y se vuelve a Rodaje
- **THEN** Rodaje muestra los 6 datos, sin que haya habido que volver a elegirlos

#### Scenario: Cuatro datos o menos
- **WHEN** hay 4 datos elegidos o menos, los botones están activados y Rodaje en vertical mide 360 × 640 dp
- **THEN** Rodaje muestra todos los datos elegidos

#### Scenario: Móvil de 720 dp
- **WHEN** hay 6 datos elegidos, los botones están activados y Rodaje en vertical mide 360 × 720 dp
- **THEN** Rodaje muestra los 4 primeros datos y los botones, con la marcha visible

#### Scenario: Pantalla alta
- **WHEN** hay 6 datos elegidos, los botones están activados y Rodaje en vertical mide 390 × 844 dp
- **THEN** Rodaje muestra los 6 datos y los botones

#### Scenario: En ERG
- **WHEN** se está en ERG con 6 datos elegidos y los botones activados en un Rodaje de 360 × 640 dp
- **THEN** Rodaje muestra la potencia objetivo, los 4 primeros datos y los botones
