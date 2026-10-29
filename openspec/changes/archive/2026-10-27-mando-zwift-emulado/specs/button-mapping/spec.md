## MODIFIED Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia, fijar la marcha de referencia, +10 W, −10 W, alternar SIM ⇄ ERG, pantalla anterior y pantalla siguiente; con la Shimano, además, plato más grande y plato más pequeño. En todas las transmisiones SHALL permitir asignar también las funciones de Zwift: barra de acciones (↑), cambio de sentido (↓), izquierda (←), derecha (→), seleccionar (A), atrás (B), usar powerup (Y), Ride On (Z), girar a la izquierda, girar a la derecha, frenar con el freno izquierdo y frenar con el derecho. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app ni llegar a Zwift por la app.

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

#### Scenario: Asignar una función de Zwift
- **WHEN** se asigna Campag. D + △ «pulsar» a «Ride On» y se pulsa esa combinación con el mando conectado a Zwift y otro ciclista cerca
- **THEN** Zwift da Ride On

### Requirement: Asignación por defecto
Mientras el usuario no la cambie, la lineal SHALL usar esta asignación: palancas D1 y D2 «pulsar» +1 marcha; palancas I1 e I2 «pulsar» −1 marcha; Campag. D + palanca D1 +3 marchas; Campag. D + palanca D2 marcha más dura; Campag. I + palanca I1 −3 marchas; Campag. I + palanca I2 marcha más ligera; □ corta volver a la referencia; □ larga fijar la referencia; △ larga alternar SIM ⇄ ERG; Campag. I + cruceta ← «pulsar» pantalla anterior; Campag. I + cruceta → «pulsar» pantalla siguiente. «+10 W» y «−10 W» SHALL quedar sin botón: en ERG, las palancas ya ajustan la potencia. La Shimano SHALL usar la de los mandos Shimano (spec `virtual-gears`, «Palancas con la Shimano» y «Saltos y extremos con la Shimano»), con □, △ y las de pantalla igual. Las dos SHALL tener, con «pulsar», las funciones de Zwift de un mando oficial: cruceta ↑, ↓, ← y → con su función; A, B, Y y Z con seleccionar, atrás, usar powerup y Ride On; ✕ girar a la izquierda; ○ girar a la derecha; y cada maneta su freno. El resto de botones SHALL quedar sin acción.

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
- **THEN** la app no cambia de pantalla y, con el mando conectado, Zwift recibe la cruceta →

#### Scenario: Zwift por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** cada función de Zwift tiene su botón del mando oficial: ↑, ↓, ←, →, A, B, Y, Z, ✕, ○, Freno I y Freno D, con «pulsar»

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL mostrar de qué transmisión es la asignación y dos grupos: «Funciones de Zwift» y «Acciones de la app», con una fila por función o acción de esa transmisión con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una fila de cualquiera de los dos grupos SHALL abrir Asignar acción. El grupo de Zwift SHALL decir si las funciones llegan a Zwift por el mando de la app o, con «Mando de Zwift» desactivado, que no llegan por la app. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones con «Mando de Zwift» activado
- **THEN** el grupo de Zwift dice que esas funciones llegan a Zwift por el mando de la app, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con el mando desactivado
- **WHEN** se abre Botones con «Mando de Zwift» desactivado
- **THEN** el grupo de Zwift dice que, sin el mando, esas funciones no llegan a Zwift por la app, y sus filas siguen pudiéndose asignar

#### Scenario: Transmisión de la asignación
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** la pantalla dice que es la asignación de Shimano 105 y que cada transmisión tiene la suya

### Requirement: Aviso de botones que usa Zwift
Con «Mando de Zwift» desactivado, si la entrada capturada es un botón que Zwift lee con el Square emparejado (cruceta, A, B, Y, Z, ✕, ○ o los frenos), Asignar acción SHALL avisar de que Zwift también lo recibirá. Con el mando activado, este aviso SHALL NOT mostrarse.

#### Scenario: Asignar A
- **WHEN** «Mando de Zwift» está desactivado y se captura A
- **THEN** un aviso dice que, con el Square emparejado en Zwift, Zwift también recibirá A (Seleccionar)

#### Scenario: Asignar A con el mando activado
- **WHEN** «Mando de Zwift» está activado, en Asignar acción de «+1 marcha» se captura A con «pulsar»
- **THEN** solo se ve el aviso de entrada en uso, que dice que A dejará de hacer «Seleccionar», y al guardar A sube una marcha y deja de llegar a Zwift

### Requirement: Asignación guardada
Las asignaciones de cada transmisión, su inversión de palancas, los tiempos y la autorrepetición SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto. Una configuración guardada antes de que hubiera varias transmisiones SHALL conservar su asignación como la de la lineal. Una configuración guardada antes de las acciones de ERG SHALL recibir la regla por defecto de «Alternar SIM ⇄ ERG» una sola vez, en cada transmisión, solo en las entradas y gestos libres. Una configuración guardada antes de las acciones de pantalla SHALL recibir igual sus dos reglas por defecto, y una guardada antes de las funciones de Zwift, las suyas.

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
- **WHEN** se abre la app nueva con una configuración guardada antes de las acciones de pantalla, con Campag. I + cruceta → «pulsar» en «+3 marchas»
- **THEN** Campag. I + cruceta → sigue subiendo tres marchas, «Pantalla siguiente» queda «Sin botón» y «Pantalla anterior» tiene Campag. I + cruceta ←

#### Scenario: Configuración anterior a las funciones de Zwift
- **WHEN** se abre la app nueva con una configuración guardada antes de las funciones de Zwift, con A corta en «Marcha de referencia»
- **THEN** A corta sigue volviendo a la referencia, «Seleccionar» queda «Sin botón» y el resto de funciones de Zwift tienen su botón por defecto
