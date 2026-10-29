## MODIFIED Requirements

### Requirement: Vibración por gesto
La app SHALL acompañar cada acción ejecutada de una vibración según el gesto: una corta para «pulsar», «corta» y «repetir», una larga para «larga» y tres cortas para «doble»; un tope SHALL vibrar con su vibración doble en lugar de la del gesto. Un gesto sin acción SHALL NOT vibrar, ni uno con una función de Zwift, que ya se nota en Zwift. También con la pantalla apagada.

#### Scenario: Larga con la pantalla apagada
- **WHEN** la pantalla está apagada, □ larga tiene asignada «Fijar la marcha de referencia» y se mantiene □
- **THEN** el móvil vibra una vez de forma larga al cumplirse el umbral

#### Scenario: Botón sin acción
- **WHEN** se pulsa △ brevemente, que no tiene acción con la asignación por defecto
- **THEN** el móvil no vibra

#### Scenario: Función de Zwift
- **WHEN** se pulsa Y, que usa el powerup en Zwift con la asignación por defecto
- **THEN** el móvil no vibra
