## MODIFIED Requirements

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar un chip para el Neo y otro para el Square, con su nombre y el punto lleno si está conectado, y con su estado en palabra si no lo está: «conectando» mientras la app intenta conectarlo o reconectarlo, y «sin conexión» si no lo intenta. Mientras no exista el puente, SHALL NOT mostrar el chip de Zwift.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados
- **THEN** se ven los chips Neo y Square con el punto lleno, y ninguno de Zwift

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** su chip dice «Square conectando», la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** su chip dice «Square sin conexión»

### Requirement: Diagnóstico desde los chips
Al tocar un chip de conexión, la app SHALL abrir Diagnóstico. Mientras la app intenta conectar algún dispositivo, el pie SHALL decir cómo despertarlo; si falta alguno y no lo intenta, SHALL decir «Toca Neo o Square para conectarlos».

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app sin dispositivos recordados
- **THEN** los dos chips dicen «sin conexión», la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

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
