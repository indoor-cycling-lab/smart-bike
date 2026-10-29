## MODIFIED Requirements

### Requirement: Palancas en ERG de Zwift
En ERG de Zwift, las palancas SHALL ajustar la potencia en lugar de la marcha: 10 W por cada marcha que pedirían, en el mismo sentido, y □ SHALL quitar el ajuste. El ajuste SHALL mantenerse entre bloques del entrenamiento y la potencia resultante SHALL quedar entre 50 W y 1000 W.

#### Scenario: Subir 10 W
- **WHEN** Zwift pide 230 W y se pulsa la palanca D1
- **THEN** el Neo pasa a 240 W y la marcha no cambia

#### Scenario: Salto con modificador
- **WHEN** Zwift pide 230 W, el ajuste es 0 y se mantiene Campag. I y se pulsa la palanca I1
- **THEN** el Neo pasa a 200 W

#### Scenario: Quitar el ajuste
- **WHEN** Zwift pide 230 W, el ajuste es +20 W y se pulsa □
- **THEN** el Neo vuelve a 230 W

#### Scenario: Ajuste entre bloques
- **WHEN** el ajuste es +10 W y Zwift pasa de pedir 230 W a pedir 280 W
- **THEN** el Neo pasa a 290 W

#### Scenario: Potencia mínima
- **WHEN** Zwift pide 60 W y se pulsa la palanca I1 dos veces
- **THEN** la primera pulsación deja el Neo en 50 W y la segunda no cambia nada y vibra como en un tope

#### Scenario: Marcha más dura o más ligera en ERG
- **WHEN** en ERG se mantiene Campag. D y se pulsa la palanca D2
- **THEN** ni la potencia ni la marcha cambian

#### Scenario: Mantener la palanca en ERG
- **WHEN** Zwift pide 230 W, la autorrepetición está activada y se mantiene la palanca D1 hasta que repite dos veces
- **THEN** el Neo pasa a 240 W al pulsar y después a 250 W y 260 W, y la marcha no cambia

#### Scenario: Fijar la referencia en ERG
- **WHEN** en ERG de Zwift con la marcha en la 15 se mantiene □ hasta el umbral de pulsación larga
- **THEN** la referencia pasa a la 15 y la potencia no cambia
