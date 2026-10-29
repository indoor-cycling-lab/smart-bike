## MODIFIED Requirements

### Requirement: Modelo por tabla
Con «Por tabla», la app SHALL simular en el Neo la pendiente del terreno más el ajuste propio de la marcha puesta, tomado de la tabla de la transmisión elegida. Mientras no se edite, la tabla SHALL dar a cada marcha el mismo ajuste que el modelo simple.

#### Scenario: Tabla sin editar
- **WHEN** se elige «Por tabla» con la transmisión lineal, sin Zwift, en la marcha 17
- **THEN** el Neo simula +5 %, igual que con «Simple»

#### Scenario: Marcha editada
- **WHEN** en la tabla de la lineal la marcha 17 pasa a +7,0 % y se rueda sin Zwift en la 17
- **THEN** el Neo simula +7 % y las demás marchas no cambian

#### Scenario: Con Zwift
- **WHEN** Zwift manda una pendiente de +3 % y la marcha tiene +2,0 % en la tabla
- **THEN** el Neo simula +5 %

#### Scenario: Una tabla por transmisión
- **WHEN** se edita la tabla de la lineal y se cambia a la Shimano 105
- **THEN** la Shimano usa su propia tabla, sin editar, y la de la lineal se conserva al volver

#### Scenario: Protección en bajadas
- **WHEN** la tabla da −8 % a la marcha puesta, el terreno es 0 % y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %

### Requirement: Dificultad global
La app SHALL aplicar el ajuste «Dificultad», de 50 % a 150 % con 100 % por defecto, a los tres modelos: con «Simple» y «Por tabla» multiplica la suma de la pendiente del terreno y el ajuste de la marcha, antes del límite de «Bajadas», y con «Físico» multiplica la potencia calculada. No SHALL afectar a la ERG ni al control manual.

#### Scenario: Valor por defecto
- **WHEN** la dificultad es 100 %
- **THEN** la resistencia es la que da el modelo sin escalar

#### Scenario: Más dura con el simple
- **WHEN** la dificultad es 150 %, se rueda sin Zwift en la marcha 17
- **THEN** el Neo simula +7,5 %

#### Scenario: Más suave con el físico
- **WHEN** la dificultad es 50 % con el físico
- **THEN** la potencia fijada en el Neo es la mitad de la calculada, sin bajar de la potencia mínima

#### Scenario: En ERG
- **WHEN** la dificultad es 150 % y se rueda en ERG manual a 200 W
- **THEN** el Neo sigue en 200 W

### Requirement: Modelo físico
Con «Físico», la app SHALL calcular la potencia que costaría pedalear a la velocidad que dan la cadencia y la relación de la marcha, con el peso del ciclista y de la bici y el terreno (pendiente, rodadura y viento) y SHALL fijarla en el Neo. A igual cadencia, una marcha más dura SHALL pedir más potencia, y a igual marcha, una pendiente mayor también.

#### Scenario: Marcha más dura
- **WHEN** se pedalea en llano a 90 rpm y se pasa a una marcha más dura
- **THEN** la potencia que se fija en el Neo sube

#### Scenario: Cadencia
- **WHEN** se mantiene la marcha y la cadencia pasa de 70 a 90 rpm en llano
- **THEN** la potencia que se fija en el Neo sube con la cadencia, no se queda fija

#### Scenario: Subida
- **WHEN** Zwift manda una pendiente de +6 % y se mantiene la cadencia
- **THEN** la potencia fijada es mayor que con 0 %

#### Scenario: Pesos
- **WHEN** sube el peso del ciclista o el de la bici
- **THEN** la potencia en subida sube y en llano casi no cambia

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift con el físico
- **THEN** el cálculo usa un terreno llano (0 %) con la rodadura y el aire por defecto

#### Scenario: Transmisión lineal
- **WHEN** se elige el físico con la transmisión lineal
- **THEN** las 24 marchas se reparten de la más ligera a la más dura en un recorrido equivalente al de la Shimano 105, y cada una pide más potencia que la anterior a igual cadencia
