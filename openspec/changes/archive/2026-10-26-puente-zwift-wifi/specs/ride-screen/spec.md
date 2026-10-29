## ADDED Requirements

### Requirement: Potencia objetivo en ERG de Zwift
En un entrenamiento ERG de Zwift, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia objetivo que se envía al Neo en grande, con la potencia que pide Zwift debajo y, si las palancas la han ajustado, el ajuste; la marcha SHALL NOT mostrarse mientras dure el ERG.

#### Scenario: Bloque sin ajuste
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** se lee «230 W» en grande con «230 W de Zwift» debajo, sin ajuste, y no se ve la marcha ni su escalera

#### Scenario: Con ajuste de las palancas
- **WHEN** Zwift pide 230 W y se ha pulsado la palanca D1 una vez
- **THEN** se lee «240 W» en grande, «230 W de Zwift» y «+10 W con las palancas»

#### Scenario: ERG en vertical
- **WHEN** el móvil está en vertical y Zwift pide 230 W
- **THEN** la potencia objetivo ocupa el lugar de la marcha y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift vuelve a enviar el terreno tras un entrenamiento con la marcha 14
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG
- **WHEN** Zwift pide 230 W y se pierde la conexión con el Neo
- **THEN** la potencia objetivo sigue visible, la potencia y la cadencia muestran «—» y el chip del Neo dice «conectando»

#### Scenario: Tope de potencia
- **WHEN** la potencia objetivo es 50 W y se pulsa la palanca I1
- **THEN** «50 W» no cambia y el pie dice «Tope · 50 W» durante 1,5 s

## MODIFIED Requirements

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo y lo que hacen las palancas: «SIM» con la transmisión «Lineal · 24 marchas» al rodar sin Zwift o en una ruta de Zwift, y «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia» en un entrenamiento ERG de Zwift; el modo SHALL leerse por la palabra, no solo por el color.

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Ruta de Zwift
- **WHEN** se rueda en una ruta de Zwift, fuera de un entrenamiento
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG ZWIFT» y «Entrenamiento de Zwift · las palancas ajustan la potencia»

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar un chip para el Neo, otro para el Square y otro para Zwift, con su nombre y el punto lleno si está conectado, y con su estado en palabra si no lo está: para el Neo y el Square, «conectando» mientras la app intenta conectarlo o reconectarlo y «sin conexión» si no lo intenta; para Zwift, «esperando» mientras la app se anuncia en la red wifi sin Zwift conectado y «sin conexión» sin wifi.

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square y Zwift están conectados
- **THEN** se ven los chips Neo, Square y Zwift con el punto lleno

#### Scenario: Sin Zwift
- **WHEN** el Neo y el Square están conectados, el móvil tiene wifi y Zwift no está conectado
- **THEN** se ven los chips Neo y Square con el punto lleno y el chip «Zwift esperando»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** el chip de Zwift dice «Zwift sin conexión» y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** su chip dice «Square conectando», la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** su chip dice «Square sin conexión»

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** el chip de Zwift pasa a «Zwift esperando» y el resto de la pantalla no cambia

### Requirement: Diagnóstico desde los chips
Al tocar un chip de conexión, también el de Zwift, la app SHALL abrir Diagnóstico. Mientras la app intenta conectar algún dispositivo, el pie SHALL decir cómo despertarlo; si falta alguno y no lo intenta, SHALL decir «Toca Neo o Square para conectarlos». El estado de Zwift SHALL NOT cambiar lo que dice el pie.

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app sin dispositivos recordados
- **THEN** los chips del Neo y del Square dicen «sin conexión», la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Neo dormido al abrir
- **WHEN** se abre la app con el Neo y el Square recordados, el Square se conecta y el Neo está dormido
- **THEN** el chip del Neo dice «Neo conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo» hasta que se conecta

#### Scenario: Square reconectando
- **WHEN** el Neo está conectado y el Square se está reconectando
- **THEN** el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** la app intenta conectar el Neo y el Square a la vez
- **THEN** el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Aviso de acción mientras reconecta
- **WHEN** el Neo se está reconectando y se sube una marcha con la palanca D1
- **THEN** el pie dice «+1 marcha» durante 1,5 s y después vuelve a «Pedalea para despertar el Neo»

#### Scenario: Tocar un chip
- **WHEN** se toca el chip del Neo
- **THEN** se abre Diagnóstico y, al volver, se está otra vez en Rodaje

#### Scenario: Tocar el chip de Zwift
- **WHEN** el Neo y el Square están conectados, Zwift no, y se toca el chip «Zwift esperando»
- **THEN** se abre Diagnóstico, y el pie de Rodaje no dice nada sobre Zwift

### Requirement: Aviso de la acción
Tras cada cambio de marcha, el pie SHALL mostrar durante 1,5 s la acción, sin el botón ni el gesto: «+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera» o «Marcha de referencia»; en ERG de Zwift, tras cada ajuste de la potencia, «+10 W», «−10 W», «+30 W», «−30 W» o «Potencia de Zwift». Una acción nueva SHALL sustituir a la anterior.

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
