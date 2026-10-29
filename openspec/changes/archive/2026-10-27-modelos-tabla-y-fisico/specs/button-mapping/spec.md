# Spec Delta

## MODIFIED Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia, fijar la marcha de referencia, +10 W, −10 W, alternar SIM ⇄ ERG, pantalla anterior, pantalla siguiente y siguiente modelo; con la Shimano, además, plato más grande y plato más pequeño. En todas las transmisiones SHALL permitir asignar también las funciones de Zwift: barra de acciones (↑), cambio de sentido (↓), izquierda (←), derecha (→), seleccionar (A), atrás (B), usar powerup (Y), Ride On (Z), girar a la izquierda, girar a la derecha, frenar con el freno izquierdo y frenar con el derecho. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app ni llegar a Zwift por la app.

#### Scenario: Botón sin acción
- **WHEN** se quita la regla de A y se pulsa A
- **THEN** la marcha no cambia, la app no hace nada y Zwift no recibe nada por el mando

#### Scenario: Acciones de plato solo con platos
- **WHEN** se abre Botones con la lineal elegida
- **THEN** no aparecen «Plato más grande» ni «Plato más pequeño»

#### Scenario: Acciones de plato con la Shimano
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** aparecen «Plato más grande» y «Plato más pequeño» después de «−3 marchas»

#### Scenario: Acciones de ERG
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Fijar la marcha de referencia» aparecen «+10 W», «−10 W» y «Alternar SIM ⇄ ERG»

#### Scenario: Asignar una acción de ERG
- **WHEN** se asigna A corta a «Alternar SIM ⇄ ERG» y se pulsa A rodando sin Zwift
- **THEN** la app pasa a ERG manual

#### Scenario: Acciones de pantalla
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Alternar SIM ⇄ ERG» aparecen «Pantalla anterior» y «Pantalla siguiente»

#### Scenario: Acción de modelo
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Pantalla siguiente» aparece «Siguiente modelo», sin ningún botón asignado por defecto

#### Scenario: Asignar el siguiente modelo
- **WHEN** se asigna △ larga a «Siguiente modelo» y se pulsa rodando con el modelo «Simple»
- **THEN** el modelo pasa a «Por tabla» y el aviso de la acción dice «Modelo: Por tabla»

#### Scenario: Asignar una función de Zwift
- **WHEN** se asigna Campag. D + △ «pulsar» a «Ride On» y se pulsa esa combinación con el mando conectado a Zwift y otro ciclista cerca
- **THEN** Zwift da Ride On
