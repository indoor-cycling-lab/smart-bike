# Spec Delta

## MODIFIED Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia, fijar la marcha de referencia, +10 W, −10 W, alternar SIM ⇄ ERG, pantalla anterior y pantalla siguiente; con la Shimano, además, plato más grande y plato más pequeño. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app.

#### Scenario: Botón sin acción
- **WHEN** se pulsa A con la asignación por defecto
- **THEN** la marcha no cambia y la app no hace nada

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

### Requirement: Asignación por defecto
Mientras el usuario no la cambie, la lineal SHALL usar esta asignación: palancas D1 y D2 «pulsar» +1 marcha; palancas I1 e I2 «pulsar» −1 marcha; Campag. D + palanca D1 +3 marchas; Campag. D + palanca D2 marcha más dura; Campag. I + palanca I1 −3 marchas; Campag. I + palanca I2 marcha más ligera; □ corta volver a la referencia; □ larga fijar la referencia; △ larga alternar SIM ⇄ ERG; Campag. I + cruceta ← «pulsar» pantalla anterior; Campag. I + cruceta → «pulsar» pantalla siguiente. «+10 W» y «−10 W» SHALL quedar sin botón: en ERG, las palancas ya ajustan la potencia. La Shimano SHALL usar la de los mandos Shimano (spec `virtual-gears`, «Palancas con la Shimano» y «Saltos y extremos con la Shimano»), con □, △ y las de pantalla igual. El resto de botones SHALL quedar sin acción en la app.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez y se mantiene □ más que el umbral de pulsación larga con la marcha en la 15
- **THEN** la referencia pasa a la 15

#### Scenario: Shimano por defecto
- **WHEN** se elige la Shimano por primera vez y se abre Botones
- **THEN** «+1 marcha» tiene la palanca D2, «−1 marcha» la palanca D1, «Plato más grande» la palanca I1, «Plato más pequeño» la palanca I2, «+3 marchas» Campag. D + palanca D2, «−3 marchas» Campag. D + palanca D1, «Marcha más dura» Campag. I + palanca I1 y «Marcha más ligera» Campag. I + palanca I2

#### Scenario: ERG por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** «Alternar SIM ⇄ ERG» tiene △ larga, y «+10 W» y «−10 W» dicen «Sin botón»

#### Scenario: △ corta sin acción
- **WHEN** se pulsa △ brevemente con la asignación por defecto
- **THEN** la app no hace nada

#### Scenario: Pantalla por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** «Pantalla anterior» tiene Campag. I + cruceta ← y «Pantalla siguiente» Campag. I + cruceta →

#### Scenario: Cruceta sin Campagnolo
- **WHEN** se pulsa la cruceta → sin mantener ningún Campagnolo, con la asignación por defecto
- **THEN** la app no cambia de pantalla ni hace nada

### Requirement: Asignación guardada
Las asignaciones de cada transmisión, su inversión de palancas, los tiempos y la autorrepetición SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto. Una configuración guardada antes de que hubiera varias transmisiones SHALL conservar su asignación como la de la lineal. Una configuración guardada antes de las acciones de ERG SHALL recibir la regla por defecto de «Alternar SIM ⇄ ERG» una sola vez, en cada transmisión, solo en las entradas y gestos libres. Una configuración guardada antes de las acciones de pantalla SHALL recibir igual sus dos reglas por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se asigna Campag. D + △ corta a «−3 marchas» y se cierra y se vuelve a abrir la app
- **THEN** Campag. D + △ corta sigue bajando tres marchas

#### Scenario: Configuración ilegible
- **WHEN** el fichero de configuración está dañado y se abre la app
- **THEN** la app usa la asignación por defecto y funciona con normalidad

#### Scenario: Configuración anterior a la Shimano
- **WHEN** se abre la app nueva con una configuración guardada por la anterior, con △ corta en «−3 marchas», las palancas invertidas y la referencia en la 15
- **THEN** la transmisión es la lineal, △ corta sigue bajando tres marchas, las palancas siguen invertidas, la referencia sigue en la 15 y la Shimano tiene su asignación por defecto

#### Scenario: Configuración anterior al ERG manual
- **WHEN** se abre la app nueva con una configuración guardada antes del ERG manual, con △ corta en «−3 marchas»
- **THEN** △ corta sigue bajando tres marchas y △ larga alterna SIM ⇄ ERG

#### Scenario: Entrada ya ocupada
- **WHEN** la configuración guardada antes del ERG manual tiene △ «pulsar» en «Marcha de referencia»
- **THEN** △ sigue volviendo a la referencia y «Alternar SIM ⇄ ERG» queda «Sin botón»

#### Scenario: Regla nueva quitada por el usuario
- **WHEN** tras recibir las reglas de ERG se quita △ larga de «Alternar SIM ⇄ ERG» y se vuelve a abrir la app
- **THEN** «Alternar SIM ⇄ ERG» sigue «Sin botón»

#### Scenario: Configuración anterior a las acciones de pantalla
- **WHEN** se abre la app nueva con una configuración guardada antes de este change, con Campag. I + cruceta → «pulsar» en «+3 marchas»
- **THEN** Campag. I + cruceta → sigue subiendo tres marchas, «Pantalla siguiente» queda «Sin botón» y «Pantalla anterior» tiene Campag. I + cruceta ←

## ADDED Requirements

### Requirement: Botones como pestaña
Botones SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver; Asignar acción SHALL volver a Botones.

#### Scenario: Abrir Botones
- **WHEN** se toca «Botones» en la barra inferior
- **THEN** se abre Botones con la asignación de la transmisión elegida y sin botón de volver

#### Scenario: Volver de Asignar acción
- **WHEN** desde Botones se abre Asignar acción y se toca volver
- **THEN** se vuelve a Botones, con la barra inferior

## REMOVED Requirements

### Requirement: Botones desde Diagnóstico
**Reason**: Con la barra inferior, Botones es una pestaña y Diagnóstico deja de enlazarla.
**Migration**: Botones se abre desde la barra inferior («Botones como pestaña»).
