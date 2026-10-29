# Spec Delta

## MODIFIED Requirements

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo y lo que hacen las palancas: «SIM» con la transmisión al rodar sin Zwift o en una ruta de Zwift («Lineal · 24 marchas», o «Shimano 105 · automático» o «Shimano 105 · manual»), «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia» en un entrenamiento ERG de Zwift, y «ERG MANUAL» con «ERG manual · las palancas ajustan la potencia» en ERG manual; el modo SHALL leerse por la palabra, no solo por el color.

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Ruta de Zwift
- **WHEN** se rueda en una ruta de Zwift, fuera de un entrenamiento
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG ZWIFT» y «Entrenamiento de Zwift · las palancas ajustan la potencia»

#### Scenario: ERG manual
- **WHEN** se entra en ERG manual, con o sin Zwift
- **THEN** la barra superior dice «ERG MANUAL» y «ERG manual · las palancas ajustan la potencia»

#### Scenario: Shimano en automático
- **WHEN** se rueda sin Zwift con la Shimano en automático
- **THEN** la barra superior dice «SIM» y «Shimano 105 · automático»

#### Scenario: Cambiar de modo
- **WHEN** la Shimano está en automático y se elige «Manual» en Marchas
- **THEN** al volver a Rodaje, la barra superior dice «Shimano 105 · manual»

### Requirement: Aviso de la acción
Tras cada cambio de marcha, el pie SHALL mostrar durante 1,5 s la acción, sin el botón ni el gesto: «+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera» o «Marcha de referencia»; con la Shimano, «Plato 52» o «Plato 36» cuando la acción cambia el plato, también si lo cambia solo el modo automático; al fijar la referencia, «Referencia en la N» con la marcha fijada, o «Referencia en 36×17» con la Shimano; en ERG de Zwift o en ERG manual, tras cada ajuste de la potencia, la diferencia real («+10 W», «−10 W», «+30 W», «−30 W»…); en ERG de Zwift, al quitar el ajuste, «Potencia de Zwift»; en ERG manual, al volver a la potencia de entrada, «Vuelta a 200 W» con esa potencia; al alternar, «ERG manual · 200 W» con la potencia o «Modo SIM». Una acción nueva SHALL sustituir a la anterior.

#### Scenario: Salto
- **WHEN** se mantiene Campag. D y se pulsa la palanca D1 desde la 14
- **THEN** el pie dice «+3 marchas» durante 1,5 s

#### Scenario: Acciones seguidas
- **WHEN** se pulsa la palanca D1 y, antes de 1,5 s, la palanca I1
- **THEN** «−1 marcha» sustituye a «+1 marcha», sin apilarse

#### Scenario: Salto recortado por el extremo
- **WHEN** la marcha es la 23 y se pide subir 3
- **THEN** el pie dice «+1 marcha»

#### Scenario: Ajuste en ERG
- **WHEN** Zwift pide 230 W y se pulsa la palanca D1
- **THEN** el pie dice «+10 W» durante 1,5 s

#### Scenario: Quitar el ajuste en ERG
- **WHEN** en ERG de Zwift con un ajuste de +20 W se pulsa □
- **THEN** el pie dice «Potencia de Zwift» durante 1,5 s

#### Scenario: Ajuste recortado por el límite
- **WHEN** la potencia objetivo es 60 W y se pide bajar 30 W
- **THEN** el pie dice «−10 W»

#### Scenario: Entrar en ERG manual
- **WHEN** la última potencia de ERG manual es 200 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «ERG manual · 200 W» durante 1,5 s

#### Scenario: Volver a SIM
- **WHEN** en ERG manual se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «Modo SIM» durante 1,5 s

#### Scenario: Ajuste en ERG manual
- **WHEN** en ERG manual a 200 W se pulsa la palanca I1
- **THEN** el pie dice «−10 W» durante 1,5 s

#### Scenario: Volver a la potencia de entrada
- **WHEN** se entró en ERG manual a 200 W, se ha subido a 240 W y se pulsa □
- **THEN** el pie dice «Vuelta a 200 W» durante 1,5 s

#### Scenario: Alternar en un entrenamiento de Zwift
- **WHEN** Zwift pide 230 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «Entrenamiento de Zwift» durante 1,5 s y el modo sigue siendo «ERG ZWIFT»

#### Scenario: Fijar la referencia
- **WHEN** la marcha es la 15 y se mantiene □ hasta el umbral de pulsación larga
- **THEN** el pie dice «Referencia en la 15» durante 1,5 s y la marca de referencia de la escalera pasa a la 15

#### Scenario: Repetición
- **WHEN** se mantiene la palanca D1 y la marcha sube tres veces por la repetición
- **THEN** el pie dice «+1 marcha» con cada cambio, sustituyendo al anterior

#### Scenario: Plato que cambia solo
- **WHEN** con la Shimano en automático en la 36×15 se pulsa la palanca D2
- **THEN** la marcha pasa a la 52×19 y el pie dice «Plato 52»

#### Scenario: Piñón sin cambiar de plato
- **WHEN** con la Shimano en la 52×17 se pulsa la palanca D1
- **THEN** el pie dice «−1 marcha»

#### Scenario: Referencia con la Shimano
- **WHEN** con la Shimano en la 36×17 se mantiene □ hasta el umbral de pulsación larga
- **THEN** el pie dice «Referencia en 36×17» durante 1,5 s

### Requirement: Potencia objetivo en ERG de Zwift
En un entrenamiento ERG de Zwift, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia objetivo que se envía al Neo en grande, con la potencia que pide Zwift debajo y, si las palancas la han ajustado, el ajuste; la marcha SHALL NOT mostrarse mientras dure el ERG. La zona SHALL ir centrada en horizontal en su columna.

#### Scenario: Bloque sin ajuste
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** se lee «230 W» en grande con «230 W de Zwift» debajo, sin ajuste, y no se ve la marcha ni su escalera

#### Scenario: Con ajuste de las palancas
- **WHEN** Zwift pide 230 W y se ha pulsado la palanca D1 una vez
- **THEN** se lee «240 W» en grande, «230 W de Zwift» y «+10 W con las palancas»

#### Scenario: ERG en vertical
- **WHEN** el móvil está en vertical y Zwift pide 230 W
- **THEN** la potencia objetivo ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift vuelve a enviar el terreno tras un entrenamiento con la marcha 14
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG
- **WHEN** Zwift pide 230 W y se pierde la conexión con el Neo
- **THEN** la potencia objetivo sigue visible, la potencia y la cadencia muestran «—» y el chip del Neo dice «conectando»

#### Scenario: Tope de potencia
- **WHEN** la potencia objetivo es 50 W y se pulsa la palanca I1
- **THEN** «50 W» no cambia y el pie dice «Tope · 50 W» durante 1,5 s

## ADDED Requirements

### Requirement: Potencia objetivo en ERG manual
En ERG manual, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia fijada en grande, sin línea de Zwift ni de ajuste, centrada en horizontal en su columna; la marcha SHALL NOT mostrarse mientras dure el ERG manual.

#### Scenario: ERG manual a 200 W
- **WHEN** se entra en ERG manual a 200 W
- **THEN** se lee «200 W» en grande, sin «de Zwift», y no se ve la marcha ni su escalera

#### Scenario: Ajuste
- **WHEN** en ERG manual a 200 W se pulsa la palanca D1
- **THEN** la cifra pasa a «210 W»

#### Scenario: ERG manual en vertical
- **WHEN** el móvil está en vertical y se está en ERG manual
- **THEN** la potencia fijada ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Vuelta a SIM
- **WHEN** en ERG manual con la marcha 14 se vuelve a SIM
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG manual
- **WHEN** en ERG manual a 200 W se pierde la conexión con el Neo
- **THEN** «200 W» sigue visible, la potencia y la cadencia muestran «—» y el chip del Neo dice «conectando»

#### Scenario: Tope de potencia
- **WHEN** en ERG manual a 1000 W se pulsa la palanca D1
- **THEN** «1000 W» no cambia y el pie dice «Tope · 1000 W» durante 1,5 s
