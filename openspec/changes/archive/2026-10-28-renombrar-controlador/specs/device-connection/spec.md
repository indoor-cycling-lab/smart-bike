## RENAMED Requirements

- FROM: `### Requirement: Buscar el rodillo y el mando por separado`
- TO: `### Requirement: Buscar el rodillo y el controlador por separado`

## MODIFIED Requirements

### Requirement: Dispositivos de la app
Dispositivos SHALL mostrar los dispositivos a los que se conecta la app, una sección por tipo en este orden: Rodillo, Controlador, Pulsómetro y Ventilador. El tipo «Controlador» SHALL ser genérico: hoy solo admite el Elite Square, pero no nombra ese modelo ni a una plataforma. Cada sección SHALL tener una fila con su icono (el del enlace, ver la spec `ui-components`), el tipo, el modelo recordado debajo («Tacx Neo 12345», «Elite Square») o «Ninguno recordado», su estado en palabra y, mientras la app intenta conectarlo, la ayuda para despertarlo. SHALL NOT mostrar lo que la app publica: eso está en Conexión.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados y se abre Dispositivos
- **THEN** las filas Rodillo y Controlador dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo

#### Scenario: Primer uso
- **WHEN** se abre Dispositivos sin dispositivos recordados
- **THEN** las cuatro filas dicen «Ninguno recordado» y cada sección tiene su «Buscar», sin «Buscar dispositivos»

#### Scenario: Neo dormido
- **WHEN** el Neo recordado está dormido y la app intenta conectarlo
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo»

#### Scenario: Sin filas de Zwift
- **WHEN** se abre Dispositivos con o sin wifi, con la app de entrenamiento conectada o no
- **THEN** no hay ninguna fila de Zwift ni de lo publicado, y su estado no cambia nada de lo que se ve

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth del móvil está apagado y se abre Dispositivos
- **THEN** las filas recordadas dicen «Sin conexión» y se ve el aviso «El Bluetooth está apagado. Actívalo para buscar dispositivos.» con «Activar Bluetooth», como en «Bluetooth apagado»

#### Scenario: Sin permiso
- **WHEN** falta el permiso de Bluetooth y se toca un «Buscar» en Dispositivos
- **THEN** la app pide el permiso y, si se deniega, muestra el aviso con «Abrir ajustes», como en «Permisos de Bluetooth»

#### Scenario: No encontrado con aviso
- **WHEN** el Neo no aparece en 30 s de búsqueda desde Dispositivos
- **THEN** la fila Rodillo dice «No encontrado» y se ve el aviso de «Dispositivo no encontrado» con qué hacer

#### Scenario: Conexión perdida con la pantalla abierta
- **WHEN** Dispositivos está abierta y se pierde la conexión con el Square
- **THEN** la fila Controlador pasa a «Conectando» con «Pulsa un botón del Square para despertarlo», y vuelve a «Conectado» al reconectar

#### Scenario: Controlador genérico
- **WHEN** se abre Dispositivos con el Square recordado
- **THEN** la sección se llama «Controlador», lleva delante el manillar y el modelo «Elite Square» va debajo

### Requirement: Buscar el rodillo y el controlador por separado
Dispositivos SHALL tener una sección «Rodillo» y otra «Controlador», cada una con su «Buscar». El del rodillo SHALL buscar un dispositivo cuyo nombre empiece por `Tacx Neo` y el del controlador uno cuyo nombre empiece por `SQUARE`, sin distinguir mayúsculas, y cada uno SHALL conectarse al primero que encuentre, sin que el usuario lo elija de una lista ni se toque la conexión del otro.

#### Scenario: Buscar el rodillo
- **WHEN** el Neo está encendido y despierto y el usuario toca «Buscar» en la sección Rodillo
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», y el Square, el pulsómetro y el ventilador no cambian

#### Scenario: Buscar el mando con el rodillo conectado
- **WHEN** el Neo está conectado, el Square no, y el usuario toca «Buscar» en la sección Controlador
- **THEN** la app busca solo el Square y la conexión del Neo no se interrumpe

#### Scenario: Primer uso
- **WHEN** no hay ningún dispositivo recordado y se abre Dispositivos
- **THEN** cada sección tiene su «Buscar» disponible y ninguna dice «Olvidar»

### Requirement: Botones de cada sección
Sin dispositivo recordado, cada sección SHALL tener solo «Buscar». Con uno recordado, SHALL tener «Olvidar» y, si no está conectado, también «Reconectar»; SHALL NOT tener «Buscar». La descripción accesible de cada botón SHALL nombrar el tipo («Buscar rodillo», «Reconectar controlador», «Olvidar pulsómetro»). Mientras busca, el «Buscar» de esa sección SHALL estar desactivado.

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al botón «Buscar» de la sección Pulsómetro
- **THEN** lo lee como «Buscar pulsómetro»

#### Scenario: Botones según el estado
- **WHEN** el rodillo está recordado y conectado, el controlador recordado y reconectando, y no hay pulsómetro recordado
- **THEN** la sección Rodillo tiene solo «Olvidar», la del Controlador «Reconectar» y «Olvidar», y la del Pulsómetro solo «Buscar»

#### Scenario: Cambiar de dispositivo
- **WHEN** hay una banda recordada y el usuario quiere usar otra
- **THEN** toca «Olvidar» y, con la sección ya sin recordado, «Buscar»

#### Scenario: Buscando uno
- **WHEN** se toca «Buscar» en la sección Rodillo
- **THEN** ese botón queda desactivado mientras busca y el «Buscar» del Controlador sigue disponible

### Requirement: Olvidar un dispositivo
Cada sección del Rodillo o del Controlador con un dispositivo recordado SHALL tener «Olvidar». Olvidar SHALL desconectarlo, SHALL hacer que la app deje de conectarse a él sola, también en las siguientes sesiones, hasta que una búsqueda lo vuelva a encontrar, y SHALL dejar de publicar su servicio (spec `platform-bridge`). Sin dispositivo recordado, la sección SHALL NOT tener «Olvidar». La marcha SHALL NOT cambiar.

#### Scenario: Olvidar el Square
- **WHEN** el Square está conectado y se toca «Olvidar» en la sección Controlador
- **THEN** el Square se desconecta, su fila dice «Sin conexión» sin «Olvidar», el controlador deja de publicarse y la marcha sigue igual

#### Scenario: Olvidado en la siguiente sesión
- **WHEN** se olvida el Neo, se termina la sesión y se vuelve a abrir la app con el Neo encendido
- **THEN** la app no se conecta al Neo hasta tocar «Buscar» en la sección Rodillo

#### Scenario: Volver a buscar
- **WHEN** se ha olvidado el Square y se toca «Buscar» en la sección Controlador con el Square despierto
- **THEN** la app lo encuentra, se conecta, lo vuelve a recordar y vuelve a publicar el controlador

#### Scenario: Olvidar el Neo rodando
- **WHEN** se rueda en la marcha 14 y se olvida el Neo
- **THEN** el Neo se desconecta sin cambios de resistencia pendientes, la potencia y la cadencia muestran «—» en Rodaje, la marcha sigue en la 14 y la app deja de publicar

#### Scenario: Olvidar mientras conecta
- **WHEN** el Neo recordado está dormido, su fila dice «Conectando» y se toca «Olvidar»
- **THEN** la app deja de intentarlo y la fila dice «Sin conexión»

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
- **WHEN** el Square se está reconectando y el usuario toca «Reconectar» en la sección Controlador
- **THEN** la app reinicia el intento con ese Square, sin buscar otro por su nombre (ver «Reconectar un dispositivo recordado»)

#### Scenario: Búsqueda sin resultado mientras reconecta
- **WHEN** el Square se está reconectando, el usuario toca «Reconectar» y el Square no aparece
- **THEN** su fila sigue en «Reconectando», sin «No encontrado» ni aviso de error, y la app sigue intentándolo sola

#### Scenario: Lo publicado no cambia
- **WHEN** la app de entrenamiento está conectada y se pierde la conexión con el Square
- **THEN** el controlador se sigue publicando mientras el Square se reconecta (spec `platform-bridge`)

### Requirement: Recordar los dispositivos
Al conectar con un Neo o un Square, la app SHALL recordarlo para las próximas sesiones, uno de cada tipo. Si una búsqueda manual conecta con otro del mismo tipo, SHALL recordar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conectan el Neo y el Square con el «Buscar» de cada sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esos dos sin buscarlos

#### Scenario: Otro Square
- **WHEN** hay un Square recordado que no está encendido, se enciende otro Square, se toca «Olvidar» y después «Buscar» en la sección Controlador
- **THEN** la app se conecta al Square encendido y, desde entonces, recuerda ese y no el anterior

### Requirement: Buscar el pulsómetro aparte
Dispositivos SHALL mostrar una sección «Pulsómetro» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar» que busca un dispositivo que ofrezca el servicio de frecuencia cardiaca y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. Ningún otro «Buscar» SHALL buscar el pulsómetro.

#### Scenario: Buscar la banda
- **WHEN** la banda está puesta y encendida, y el usuario toca «Buscar» en la sección Pulsómetro
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de la app de entrenamiento

#### Scenario: Buscar sin banda
- **WHEN** el usuario toca «Buscar» en la sección Controlador sin ningún pulsómetro recordado
- **THEN** la app busca solo el Square, y no aparece ningún aviso ni fila de error de pulso

#### Scenario: Banda no encontrada
- **WHEN** el usuario toca «Buscar» en la sección Pulsómetro y no aparece ninguno en 30 s
- **THEN** la fila dice «No encontrado», aparece el aviso de error «No se encuentra el pulsómetro. Póntelo, comprueba que tiene batería y vuelve a buscar.» y su «Buscar» vuelve a estar disponible

#### Scenario: Más de una banda a la vista
- **WHEN** el usuario toca «Buscar» en la sección Pulsómetro y hay dos bandas encendidas
- **THEN** la app se conecta a la primera que encuentra y la recuerda

#### Scenario: Banda con el límite de conexiones agotado
- **WHEN** la banda ya está conectada a otros dispositivos y no admite otra conexión
- **THEN** la fila dice «No encontrado» o «Sin conexión» como en cualquier otro fallo, y el aviso dice que puede estar conectada a otro dispositivo

#### Scenario: Sin permiso o con Bluetooth apagado
- **WHEN** falta el permiso de Bluetooth o el Bluetooth está apagado y el usuario toca «Buscar» en la sección Pulsómetro
- **THEN** la app muestra los mismos avisos y acciones que con cualquier otro «Buscar», y no busca nada
