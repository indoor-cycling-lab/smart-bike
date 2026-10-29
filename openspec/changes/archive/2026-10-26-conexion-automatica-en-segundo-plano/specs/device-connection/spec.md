## ADDED Requirements

### Requirement: Recordar los dispositivos
Al conectar con un Neo o un Square, la app SHALL recordarlo para las próximas sesiones, uno de cada tipo. Si una búsqueda manual conecta con otro del mismo tipo, SHALL recordar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conectan el Neo y el Square con «Buscar dispositivos» y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esos dos sin buscarlos

#### Scenario: Otro Square
- **WHEN** hay un Square recordado que no está encendido, se enciende otro Square y se toca «Buscar dispositivos»
- **THEN** la app se conecta al Square encendido y, desde entonces, recuerda ese y no el anterior

### Requirement: Conexión al abrir la app
Al abrir la app con dispositivos recordados y el permiso de Bluetooth concedido, la app SHALL conectarse a ellos sola, sin buscar y sin que el usuario toque nada; mientras no conecta, la fila de cada uno SHALL decir «Conectando» con la ayuda para despertarlo.

#### Scenario: Los dos encendidos al abrir
- **WHEN** hay un Neo y un Square recordados, los dos están despiertos y se abre la app
- **THEN** los dos llegan a «Conectado» sin tocar nada, y el Neo recibe la resistencia de la marcha guardada

#### Scenario: Neo dormido al abrir
- **WHEN** el Neo recordado está dormido al abrir la app y se empieza a pedalear al cabo de un minuto
- **THEN** mientras tanto su fila dice «Conectando» con «Pedalea para despertarlo» y, al despertar, el Neo se conecta solo

#### Scenario: Primer uso
- **WHEN** no hay ningún dispositivo recordado y se abre la app
- **THEN** la app no busca ni se conecta sola: las dos filas dicen «Sin conexión» hasta tocar «Buscar dispositivos»

#### Scenario: Sin permiso al abrir
- **WHEN** hay dispositivos recordados pero falta el permiso de Bluetooth al abrir la app, y el usuario lo concede después desde Diagnóstico
- **THEN** la app no intenta conectar hasta tener el permiso y, al concederlo, se conecta a los recordados sin buscar

## MODIFIED Requirements

### Requirement: Estado de cada conexión visible
La app SHALL mostrar el estado de la conexión del Neo y del Square por separado, con palabra y forma además de color: buscando, conectando, reconectando, conectado, no encontrado o sin conexión.

#### Scenario: Square buscando
- **WHEN** la app busca el Square y aún no lo encuentra
- **THEN** su fila dice «Buscando» con la ayuda «Pulsa cualquier botón para despertarlo»

#### Scenario: Neo buscando
- **WHEN** la app busca el Neo y aún no lo encuentra
- **THEN** su fila dice «Buscando» con la ayuda «Pedalea para despertarlo»

#### Scenario: Square reconectando
- **WHEN** la app intenta recuperar la conexión con el Square
- **THEN** su fila dice «Reconectando» con la ayuda «Pulsa cualquier botón para despertarlo»

### Requirement: Conexión perdida
Si se pierde la conexión con un dispositivo ya conectado, la app SHALL intentar recuperarla sola, sin límite de intentos mientras dure la sesión y también con la pantalla apagada, conservando el resto de conexiones y la marcha puesta, y SHALL mostrarlo en su fila con un aviso informativo que dice cómo despertarlo.

#### Scenario: Square apagado a mitad
- **WHEN** el Square está conectado y se apaga y se vuelve a encender
- **THEN** su fila dice «Reconectando» con el aviso «Reconectando con el Square. Pulsa cualquier botón para despertarlo.», el Neo sigue conectado y el Square vuelve a «Conectado» sin tocar el móvil

#### Scenario: Neo perdido con una marcha puesta
- **WHEN** la marcha es la 17 y se pierde la conexión con el Neo
- **THEN** su fila dice «Reconectando» con el aviso «Reconectando con el Neo. Comprueba que está enchufado y pedalea para despertarlo.», la marcha sigue siendo la 17 y, al volver el Neo, recibe sola la resistencia de la 17

#### Scenario: Cambios mientras reconecta el Neo
- **WHEN** el Neo se está reconectando y se sube de la 17 a la 19 con el Square
- **THEN** al volver, el Neo recibe la resistencia de la 19

#### Scenario: Square apagado con un botón mantenido
- **WHEN** se apaga el Square con su combinación de apagado: Campag. I, Campag. D, una palanca izquierda y una derecha, mantenidas más de 5 s
- **THEN** al cortarse la conexión ningún botón sigue pulsado, el Neo sigue conectado con la marcha que había al apagarse y, al volver el Square, una palanca D1 sola sube una marcha y no tres

#### Scenario: Ausencia larga
- **WHEN** el Square se queda sin batería y se vuelve a encender al cabo de 30 minutos, con la pantalla apagada todo el tiempo
- **THEN** el Square se reconecta solo

#### Scenario: Buscar mientras reconecta
- **WHEN** el Square se está reconectando y el usuario toca «Buscar dispositivos»
- **THEN** la app busca el Square por su nombre, se conecta al primero que encuentra y deja de intentar con el anterior si es otro

#### Scenario: Búsqueda sin resultado mientras reconecta
- **WHEN** el Square se está reconectando, el usuario toca «Buscar dispositivos» y no aparece ninguno en 30 s
- **THEN** su fila dice «No encontrado» con el aviso de error, y la app no vuelve a intentarlo sola hasta otra búsqueda, hasta que el Bluetooth se apague y se encienda o hasta la siguiente apertura

### Requirement: Bluetooth apagado
Si el Bluetooth del móvil está apagado, la app SHALL decirlo y ofrecer activarlo; al apagarse con dispositivos conectados o reconectándose, SHALL mostrarlos sin conexión y, al volver a encenderse, SHALL conectarse sola a los recordados.

#### Scenario: Bluetooth apagado al buscar
- **WHEN** el Bluetooth está apagado y el usuario toca «Buscar dispositivos»
- **THEN** aparece el aviso de error «El Bluetooth está apagado. Actívalo para buscar el Neo y el Square.» con el botón «Activar Bluetooth», y no se busca nada

#### Scenario: Bluetooth apagado durante la sesión
- **WHEN** el Neo y el Square están conectados y el usuario apaga el Bluetooth
- **THEN** las dos filas dicen «Sin conexión» y aparece el aviso de Bluetooth apagado

#### Scenario: Bluetooth encendido de nuevo
- **WHEN** el Bluetooth se apagó durante la sesión con la marcha 14 y el usuario lo vuelve a encender
- **THEN** el aviso desaparece, el Neo y el Square se conectan solos y el Neo recibe la resistencia de la 14
