## MODIFIED Requirements

### Requirement: Pendiente según la marcha sin Zwift
Sin Zwift y con la transmisión lineal, la app SHALL simular en el Neo un terreno llano (0 %) más 1 % por cada marcha por encima de la 12 (menos 1 % por cada marcha por debajo), en modo SIM, con el límite inferior que fije el ajuste «Bajadas». La pendiente del terreno sin app de entrenamiento SHALL NOT poder cambiarse en Ajustes.

#### Scenario: Marcha de referencia
- **WHEN** la marcha es la 12 y el Neo está conectado
- **THEN** el Neo simula una pendiente de 0 %

#### Scenario: Marcha dura
- **WHEN** la marcha pasa a la 17
- **THEN** el Neo simula una pendiente de +5 % y la resistencia aumenta de forma perceptible

#### Scenario: Extremos
- **WHEN** la marcha es la 1 o la 24 con el ajuste «Bajadas» por defecto
- **THEN** el Neo simula 0 % o +12 %, respectivamente

#### Scenario: Extremos sin protección
- **WHEN** la marcha es la 1 o la 24 con «Bajadas» en «Sin protección»
- **THEN** el Neo simula −11 % o +12 %, respectivamente

#### Scenario: Pendiente sin Zwift de 3 %
- **WHEN** una configuración anterior tenía «Pendiente sin Zwift» en 3,0 % y la marcha es la 14
- **THEN** el Neo simula +2 %: el terreno sin app de entrenamiento es siempre llano

#### Scenario: Pendiente sin Zwift negativa
- **WHEN** la marcha es la 10 y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %: la marcha no baja la pendiente por debajo de la del terreno
