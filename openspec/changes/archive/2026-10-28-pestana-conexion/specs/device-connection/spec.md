## ADDED Requirements

### Requirement: Buscar el rodillo y el mando por separado
Dispositivos SHALL tener una sección «Rodillo» y otra «Mando», cada una con su «Buscar». El del rodillo SHALL buscar un dispositivo cuyo nombre empiece por `Tacx Neo` y el del mando uno cuyo nombre empiece por `SQUARE`, sin distinguir mayúsculas, y cada uno SHALL conectarse al primero que encuentre, sin que el usuario lo elija de una lista ni se toque la conexión del otro.

#### Scenario: Buscar el rodillo
- **WHEN** el Neo está encendido y despierto y el usuario toca «Buscar» en la sección Rodillo
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», y el Square, el pulsómetro y el ventilador no cambian

#### Scenario: Buscar el mando con el rodillo conectado
- **WHEN** el Neo está conectado, el Square no, y el usuario toca «Buscar» en la sección Mando
- **THEN** la app busca solo el Square y la conexión del Neo no se interrumpe

#### Scenario: Primer uso
- **WHEN** no hay ningún dispositivo recordado y se abre Dispositivos
- **THEN** cada sección tiene su «Buscar» disponible y ninguna dice «Olvidar»

### Requirement: Botones de cada sección
Sin dispositivo recordado, cada sección SHALL tener solo «Buscar». Con uno recordado, SHALL tener «Olvidar» y, si no está conectado, también «Reconectar»; SHALL NOT tener «Buscar». La descripción accesible de cada botón SHALL nombrar el tipo («Buscar rodillo», «Reconectar mando», «Olvidar pulsómetro»). Mientras busca, el «Buscar» de esa sección SHALL estar desactivado.

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al botón «Buscar» de la sección Pulsómetro
- **THEN** lo lee como «Buscar pulsómetro»

#### Scenario: Botones según el estado
- **WHEN** el rodillo está recordado y conectado, el mando recordado y reconectando, y no hay pulsómetro recordado
- **THEN** la sección Rodillo tiene solo «Olvidar», la del Mando «Reconectar» y «Olvidar», y la del Pulsómetro solo «Buscar»

#### Scenario: Cambiar de dispositivo
- **WHEN** hay una banda recordada y el usuario quiere usar otra
- **THEN** toca «Olvidar» y, con la sección ya sin recordado, «Buscar»

#### Scenario: Buscando uno
- **WHEN** se toca «Buscar» en la sección Rodillo
- **THEN** ese botón queda desactivado mientras busca y el «Buscar» del Mando sigue disponible

### Requirement: Reconectar un dispositivo recordado
«Reconectar» SHALL reiniciar en el momento el intento de conexión con el dispositivo recordado de esa sección, por su identidad y no por su nombre, sin conectarse a otro del mismo tipo, y la app SHALL seguir intentándolo sola después, como con cualquier conexión perdida. SHALL NOT tocar las conexiones de las demás secciones ni cambiar la marcha.

#### Scenario: Reconectar el Square dormido
- **WHEN** el Square recordado está «Reconectando» y el usuario lo despierta y toca «Reconectar»
- **THEN** la app intenta conectar con ese Square al momento, la fila pasa a «Conectando» y después a «Conectado», y el Neo no se interrumpe

#### Scenario: Sigue sin aparecer
- **WHEN** el usuario toca «Reconectar» y el Square sigue apagado
- **THEN** la fila sigue en «Reconectando» con la ayuda para despertarlo, sin aviso de error, y la app sigue intentándolo sola

#### Scenario: Otro del mismo tipo encendido
- **WHEN** el Neo recordado está apagado, hay otro Neo encendido cerca y se toca «Reconectar»
- **THEN** la app no se conecta al otro Neo

#### Scenario: Con el Bluetooth apagado
- **WHEN** el Bluetooth está apagado y se toca «Reconectar»
- **THEN** la app muestra el aviso de Bluetooth apagado con «Activar Bluetooth», y no intenta nada

## MODIFIED Requirements

### Requirement: Dispositivo no encontrado
Si un dispositivo no aparece en 30 s de búsqueda, la app SHALL dejar de buscarlo, mostrar que no se ha encontrado con qué hacer y permitir volver a buscar.

#### Scenario: Neo apagado
- **WHEN** el Neo está desenchufado y pasan 30 s de búsqueda
- **THEN** su fila dice «No encontrado», aparece el aviso de error «No se encuentra el Neo. Comprueba que está enchufado, pedalea para despertarlo y vuelve a buscar.» y el «Buscar» del Rodillo vuelve a estar disponible

#### Scenario: Square dormido
- **WHEN** el Square no se despierta y pasan 30 s de búsqueda
- **THEN** su fila dice «No encontrado» y el aviso dice «No se encuentra el Square. Pulsa cualquier botón para despertarlo y vuelve a buscar.»

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
- **WHEN** el Square se está reconectando y el usuario toca «Reconectar» en la sección Mando
- **THEN** la app reinicia el intento con ese Square, sin buscar otro por su nombre (ver «Reconectar un dispositivo recordado»)

#### Scenario: Búsqueda sin resultado mientras reconecta
- **WHEN** el Square se está reconectando, el usuario toca «Reconectar» y el Square no aparece
- **THEN** su fila sigue en «Reconectando», sin «No encontrado» ni aviso de error, y la app sigue intentándolo sola

#### Scenario: Lo publicado no cambia
- **WHEN** la app de entrenamiento está conectada y se pierde la conexión con el Square
- **THEN** el mando se sigue publicando mientras el Square se reconecta (spec `platform-bridge`)

### Requirement: Permisos de Bluetooth
Si falta el permiso de Bluetooth necesario para buscar y conectar, la app SHALL pedirlo al tocar cualquier «Buscar» de Dispositivos y, si se deniega, SHALL explicar para qué hace falta y ofrecer abrir los ajustes de la app, sin buscar.

#### Scenario: Permiso denegado
- **WHEN** el usuario deniega el permiso de dispositivos cercanos
- **THEN** aparece el aviso de error «Sin permiso para buscar dispositivos Bluetooth. Concédelo en los ajustes de la app.» con el botón «Abrir ajustes», y no se busca nada

#### Scenario: Permiso concedido
- **WHEN** el usuario concede el permiso tras tocar «Buscar» en una sección
- **THEN** la búsqueda de esa sección empieza sin tener que volver a tocar el botón

### Requirement: Bluetooth apagado
Si el Bluetooth del móvil está apagado, la app SHALL decirlo y ofrecer activarlo; al apagarse con dispositivos conectados o reconectándose, SHALL mostrarlos sin conexión y, al volver a encenderse, SHALL conectarse sola a los recordados.

#### Scenario: Bluetooth apagado al buscar
- **WHEN** el Bluetooth está apagado y el usuario toca «Buscar» en cualquier sección
- **THEN** aparece el aviso de error «El Bluetooth está apagado. Actívalo para buscar dispositivos.» con el botón «Activar Bluetooth», y no se busca nada

#### Scenario: Bluetooth apagado durante la sesión
- **WHEN** el Neo y el Square están conectados y el usuario apaga el Bluetooth
- **THEN** las dos filas dicen «Sin conexión» y aparece el aviso de Bluetooth apagado

#### Scenario: Bluetooth encendido de nuevo
- **WHEN** el Bluetooth se apagó durante la sesión con la marcha 14 y el usuario lo vuelve a encender
- **THEN** el aviso desaparece, el Neo y el Square se conectan solos y el Neo recibe la resistencia de la 14

### Requirement: Recordar los dispositivos
Al conectar con un Neo o un Square, la app SHALL recordarlo para las próximas sesiones, uno de cada tipo. Si una búsqueda manual conecta con otro del mismo tipo, SHALL recordar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conectan el Neo y el Square con el «Buscar» de cada sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esos dos sin buscarlos

#### Scenario: Otro Square
- **WHEN** hay un Square recordado que no está encendido, se enciende otro Square, se toca «Olvidar» y después «Buscar» en la sección Mando
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
- **THEN** la app no busca ni se conecta sola: las filas dicen «Sin conexión» hasta tocar el «Buscar» de su sección

#### Scenario: Sin permiso al abrir
- **WHEN** hay dispositivos recordados pero falta el permiso de Bluetooth al abrir la app, y el usuario lo concede después desde Diagnóstico
- **THEN** la app no intenta conectar hasta tener el permiso y, al concederlo, se conecta a los recordados sin buscar

### Requirement: Olvidar un dispositivo
Cada sección del Rodillo o del Mando con un dispositivo recordado SHALL tener «Olvidar». Olvidar SHALL desconectarlo, SHALL hacer que la app deje de conectarse a él sola, también en las siguientes sesiones, hasta que una búsqueda lo vuelva a encontrar, y SHALL dejar de publicar su servicio (spec `platform-bridge`). Sin dispositivo recordado, la sección SHALL NOT tener «Olvidar». La marcha SHALL NOT cambiar.

#### Scenario: Olvidar el Square
- **WHEN** el Square está conectado y se toca «Olvidar» en la sección Mando
- **THEN** el Square se desconecta, su fila dice «Sin conexión» sin «Olvidar», el mando deja de publicarse y la marcha sigue igual

#### Scenario: Olvidado en la siguiente sesión
- **WHEN** se olvida el Neo, se termina la sesión y se vuelve a abrir la app con el Neo encendido
- **THEN** la app no se conecta al Neo hasta tocar «Buscar» en la sección Rodillo

#### Scenario: Volver a buscar
- **WHEN** se ha olvidado el Square y se toca «Buscar» en la sección Mando con el Square despierto
- **THEN** la app lo encuentra, se conecta, lo vuelve a recordar y vuelve a publicar el mando

#### Scenario: Olvidar el Neo rodando
- **WHEN** se rueda en la marcha 14 y se olvida el Neo
- **THEN** el Neo se desconecta sin cambios de resistencia pendientes, la potencia y la cadencia muestran «—» en Rodaje, la marcha sigue en la 14 y la app deja de publicar

#### Scenario: Olvidar mientras conecta
- **WHEN** el Neo recordado está dormido, su fila dice «Conectando» y se toca «Olvidar»
- **THEN** la app deja de intentarlo y la fila dice «Sin conexión»

### Requirement: Buscar el ventilador aparte
Dispositivos SHALL mostrar una sección «Ventilador» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar» que busca un dispositivo cuyo nombre empiece por «HEADWIND» y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. Ningún otro «Buscar» SHALL buscar el ventilador.

#### Scenario: Buscar el ventilador
- **WHEN** el ventilador está encendido y el usuario toca «Buscar» en la sección Ventilador
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de la app de entrenamiento

#### Scenario: Buscar sin ventilador
- **WHEN** el usuario toca «Buscar» en la sección Rodillo sin ningún ventilador recordado
- **THEN** la app busca solo el Neo, y no aparece ningún aviso ni fila de error del ventilador

#### Scenario: Ventilador no encontrado
- **WHEN** el usuario toca «Buscar» en la sección Ventilador y no aparece ninguno en 30 s
- **THEN** la fila dice «No encontrado», aparece el aviso de error «No se encuentra el ventilador. Enchúfalo, ciérralo en la app de Wahoo y vuelve a buscar.» y su «Buscar» vuelve a estar disponible

#### Scenario: Ventilador ocupado por otra app
- **WHEN** el ventilador está conectado a otra app y no admite otra conexión
- **THEN** la fila dice «No encontrado» o «Sin conexión» como en cualquier otro fallo, y el aviso dice que puede estar conectado a otra app

#### Scenario: Más de un ventilador a la vista
- **WHEN** el usuario toca «Buscar» en la sección Ventilador y hay dos ventiladores encendidos
- **THEN** la app se conecta al primero que encuentra y lo recuerda

#### Scenario: Sin permiso o con Bluetooth apagado
- **WHEN** falta el permiso de Bluetooth o el Bluetooth está apagado y el usuario toca «Buscar» en la sección Ventilador
- **THEN** la app muestra los mismos avisos y acciones que con cualquier otro «Buscar», y no busca nada

### Requirement: Recordar y reconectar el ventilador
Al conectar con un ventilador, la app SHALL recordarlo para las próximas sesiones, uno solo, conectarse a él sola al abrir la app y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL recordar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta el ventilador con el «Buscar» de su sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a ese ventilador sin buscarlo

#### Scenario: Ventilador apagado al abrir
- **WHEN** hay un ventilador recordado y está apagado al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otro ventilador
- **WHEN** hay un ventilador recordado y el usuario toca «Olvidar» y después «Buscar» en la sección Ventilador y conecta con otro
- **THEN** la app recuerda el nuevo y deja de intentar con el anterior

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del ventilador, que queda libre para otra app, con el modo y la velocidad que tenía

### Requirement: Olvidar el ventilador
La fila «Ventilador» con uno recordado SHALL tener «Olvidar». Olvidarlo SHALL desconectarlo, quitar su sección de Ajustes, su icono de Rodaje y su fila de Diagnóstico, y hacer que las acciones del ventilador avisen «Sin ventilador». Sin ventilador recordado, la fila SHALL NOT tener «Olvidar». Olvidarlo SHALL NOT cambiar el modo ni la velocidad del ventilador.

#### Scenario: Olvidar el ventilador
- **WHEN** el usuario toca «Olvidar» en la fila del ventilador
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin «Olvidar», y desaparecen la sección de Ajustes, el icono de Rodaje y la fila de Diagnóstico

#### Scenario: El ventilador sigue como estaba
- **WHEN** se olvida el ventilador mientras está en manual al 40 %
- **THEN** el ventilador sigue en manual al 40 %

#### Scenario: Olvidar y volver a buscar
- **WHEN** se olvida el ventilador y se toca «Buscar» en la sección Ventilador con él encendido
- **THEN** la app lo encuentra, lo conecta y lo vuelve a recordar

### Requirement: Buscar el pulsómetro aparte
Dispositivos SHALL mostrar una sección «Pulsómetro» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar» que busca un dispositivo que ofrezca el servicio de frecuencia cardiaca y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. Ningún otro «Buscar» SHALL buscar el pulsómetro.

#### Scenario: Buscar la banda
- **WHEN** la banda está puesta y encendida, y el usuario toca «Buscar» en la sección Pulsómetro
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de la app de entrenamiento

#### Scenario: Buscar sin banda
- **WHEN** el usuario toca «Buscar» en la sección Mando sin ningún pulsómetro recordado
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

### Requirement: Recordar y reconectar el pulsómetro
Al conectar con un pulsómetro, la app SHALL recordarlo para las próximas sesiones, uno solo, y SHALL conectarse a él sola al abrir la app, sin que el usuario busque, y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL recordar el nuevo en su lugar. Olvidar el pulsómetro en Dispositivos SHALL desconectarlo, quitar su icono de Rodaje y su fila de Diagnóstico y dejar de publicar el pulso.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta la banda con el «Buscar» de su sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esa banda sin buscarla

#### Scenario: Banda apagada al abrir
- **WHEN** hay una banda recordada y está apagada al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otra banda
- **WHEN** hay una banda recordada y el usuario toca «Olvidar» y después «Buscar» en la sección Pulsómetro y conecta con otra
- **THEN** la app recuerda la nueva y deja de intentar con la anterior

#### Scenario: Olvidar la banda
- **WHEN** el usuario olvida el pulsómetro en Dispositivos
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin recordado, el icono de Rodaje desaparece y la app deja de publicar el pulso

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del pulsómetro, que queda libre para otro dispositivo

### Requirement: Dispositivos de la app
Dispositivos SHALL mostrar los dispositivos a los que se conecta la app, una sección por tipo en este orden: Rodillo, Mando, Pulsómetro y Ventilador. Cada sección SHALL tener una fila con el tipo, el modelo recordado debajo («Tacx Neo 12345», «Elite Square») o «Ninguno recordado», su estado en palabra y, mientras la app intenta conectarlo, la ayuda para despertarlo. SHALL NOT mostrar lo que la app publica: eso está en Conexión.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados y se abre Dispositivos
- **THEN** las filas Rodillo y Mando dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo

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
- **THEN** la fila Mando pasa a «Conectando» con «Pulsa un botón del Square para despertarlo», y vuelve a «Conectado» al reconectar

## REMOVED Requirements

### Requirement: Buscar y conectar el Neo y el Square
**Reason**: Desaparece la búsqueda conjunta «Buscar dispositivos»; cada tipo se busca en su sección.
**Migration**: Ver «Buscar el rodillo y el mando por separado» y «Botones de cada sección».
