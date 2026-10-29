## ADDED Requirements

### Requirement: Marcha plato × piñón con la Shimano
Con la Shimano, la pantalla de rodaje SHALL mostrar en el lugar de la marcha el plato y el piñón en grande («36×17», con el × más pequeño y atenuado), el indicador de los dos platos con el puesto relleno y el cassette con una barra por piñón, del 36 a la izquierda al 11 a la derecha, con el puesto resaltado y numerado, en horizontal y en vertical. SHALL NOT mostrar la escalera ni la relación.

#### Scenario: Marcha 52×17
- **WHEN** la transmisión es la Shimano y la marcha la 52×17
- **THEN** se lee «52×17» en grande, el plato 52 está relleno y el 36 solo con borde, y la barra del 17 está resaltada con su número

#### Scenario: Shimano en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×21
- **THEN** se ven, sin desplazamiento, «36×21», los platos, el cassette y la potencia y la cadencia en una fila

#### Scenario: Cambio de plato
- **WHEN** la marcha pasa de la 36×15 a la 52×19
- **THEN** la cifra nueva entra desplazándose hacia arriba y el plato relleno pasa al 52, sin que se mueva el resto de la pantalla

## MODIFIED Requirements

### Requirement: Marcha en grande
Con la transmisión lineal, la pantalla SHALL mostrar el número de la marcha puesta en grande, con «Marcha · de 24» al lado y una escalera con un segmento por marcha (la ligera a la izquierda), la actual resaltada y la marca de la marcha de referencia.

#### Scenario: Marcha 14
- **WHEN** la marcha es la 14
- **THEN** se lee «14» en grande, «de 24», y la escalera tiene resaltado el segmento 14 y marcado el 12

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo y lo que hacen las palancas: «SIM» con la transmisión al rodar sin Zwift o en una ruta de Zwift («Lineal · 24 marchas», o «Shimano 105 · automático» o «Shimano 105 · manual»), y «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia» en un entrenamiento ERG de Zwift; el modo SHALL leerse por la palabra, no solo por el color.

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Ruta de Zwift
- **WHEN** se rueda en una ruta de Zwift, fuera de un entrenamiento
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG ZWIFT» y «Entrenamiento de Zwift · las palancas ajustan la potencia»

#### Scenario: Shimano en automático
- **WHEN** se rueda sin Zwift con la Shimano en automático
- **THEN** la barra superior dice «SIM» y «Shimano 105 · automático»

#### Scenario: Cambiar de modo
- **WHEN** la Shimano está en automático y se elige «Manual» en Marchas
- **THEN** al volver a Rodaje, la barra superior dice «Shimano 105 · manual»

### Requirement: Aviso de tope en la marcha
Al llegar a un tope, la cifra de la marcha SHALL atenuarse y SHALL aparecer debajo la etiqueta del tope durante 1,5 s, sin aviso de acción: «Tope · marcha más dura» o «Tope · marcha más ligera» en los extremos, y con la Shimano también «Tope · plato 52», «Tope · plato 36», «Tope · piñón 11» o «Tope · piñón 36».

#### Scenario: Tope en la más dura
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, y después vuelve a verse normal

#### Scenario: Tope de plato
- **WHEN** con la Shimano en la 52×17 se pulsa la palanca I1
- **THEN** «52×17» se ve atenuado con «Tope · plato 52» durante 1,5 s

### Requirement: Aviso de la acción
Tras cada cambio de marcha, el pie SHALL mostrar durante 1,5 s la acción, sin el botón ni el gesto: «+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera» o «Marcha de referencia»; con la Shimano, «Plato 52» o «Plato 36» cuando la acción cambia el plato, también si lo cambia solo el modo automático; al fijar la referencia, «Referencia en la N» con la marcha fijada, o «Referencia en 36×17» con la Shimano; en ERG de Zwift, tras cada ajuste de la potencia, «+10 W», «−10 W», «+30 W», «−30 W» o «Potencia de Zwift». Una acción nueva SHALL sustituir a la anterior.

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

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo las conexiones, el modo con la transmisión, la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), la potencia y la cadencia en una fila de 2 columnas que ocupa el resto del alto, y al pie el aviso de acción o la ayuda para conectar, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados y la marcha es la 14
- **THEN** se ven, sin desplazamiento, los chips del Neo y del Square, «SIM» y «Lineal · 24 marchas», «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los dos chips dicen «sin conexión», la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»
