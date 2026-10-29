## ADDED Requirements

### Requirement: Pendiente según la relación con la Shimano
Con la Shimano, la app SHALL sumar a la pendiente del terreno un ajuste proporcional al logaritmo de la relación (plato / piñón): 0 % en el centro geométrico del desarrollo y, con 1 % por marcha, −11,5 % en la 36×36 y +11,5 % en la 52×11, el mismo recorrido que de la 1 a la 24 de la lineal. Dos marchas con la misma relación SHALL dar la misma pendiente. El límite del ajuste «Bajadas» SHALL aplicarse igual que con la lineal.

#### Scenario: Marcha casi neutra
- **WHEN** se rueda sin Zwift con la Shimano en la 36×17 y «Bajadas» en «Sin protección»
- **THEN** el Neo simula unos −0,4 %

#### Scenario: Plato grande
- **WHEN** se rueda sin Zwift con la Shimano en la 52×17
- **THEN** el Neo simula unos +5,1 %

#### Scenario: Extremos sin protección
- **WHEN** la marcha es la 36×36 o la 52×11 con «Bajadas» en «Sin protección» y sin Zwift
- **THEN** el Neo simula unos −11,5 % o +11,5 %, respectivamente

#### Scenario: Relaciones casi iguales
- **WHEN** se pasa de la 36×19 a la 52×28 con «Sin protección» y sin Zwift
- **THEN** la pendiente cambia menos de 0,5 % (de unos −2,0 % a unos −2,3 %)

#### Scenario: Cambio de plato en automático
- **WHEN** se rueda sin Zwift con «Sin protección» en la 36×15 en automático y se pide «+1 marcha»
- **THEN** la marcha pasa a la 52×19 y el Neo pasa de unos +1,5 % a unos +3,4 %, un salto parecido al de un piñón

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de 4,0 % y la marcha es la 52×17
- **THEN** el Neo simula unos +9,1 %

#### Scenario: Prueba con el equipo real
- **WHEN** el usuario rueda con la Shimano en automático de la 36×36 a la 52×11 y vuelve
- **THEN** cada marcha se nota más dura o más ligera que la anterior y el cambio de plato no se nota más que un cambio de piñón; si no es así, se ajusta antes de archivar

## MODIFIED Requirements

### Requirement: Pendiente según la marcha sin Zwift
Sin Zwift y con la transmisión lineal, la app SHALL simular en el Neo la pendiente base de 0 % más 1 % por cada marcha por encima de la 12 (menos 1 % por cada marcha por debajo), en modo SIM, con el límite inferior que fije el ajuste «Bajadas».

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
