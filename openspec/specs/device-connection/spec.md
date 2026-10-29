# device-connection Specification

## Purpose
Conectar la app con el Tacx Neo y el Elite Square por Bluetooth y mostrar en todo momento el estado de cada conexión, incluidos los fallos que impiden conectar.

## Requirements

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
- **WHEN** el Square se está reconectando y el usuario toca «Reconectar» en la sección Controlador
- **THEN** la app reinicia el intento con ese Square, sin buscar otro por su nombre (ver «Reconectar un dispositivo configurado»)

#### Scenario: Búsqueda sin resultado mientras reconecta
- **WHEN** el Square se está reconectando, el usuario toca «Reconectar» y el Square no aparece
- **THEN** su fila sigue en «Reconectando», sin «No encontrado» ni aviso de error, y la app sigue intentándolo sola

#### Scenario: Lo publicado no cambia
- **WHEN** la app de entrenamiento está conectada y se pierde la conexión con el Square
- **THEN** el controlador se sigue publicando mientras el Square se reconecta (spec `platform-bridge`)

### Requirement: Permisos de Bluetooth
Si falta el permiso de Bluetooth necesario para buscar y conectar, la app SHALL pedirlo al tocar cualquier «Buscar» de Dispositivos y, si se deniega, SHALL explicar para qué hace falta y ofrecer abrir los ajustes de la app, sin buscar.

#### Scenario: Permiso denegado
- **WHEN** el usuario deniega el permiso de dispositivos cercanos
- **THEN** aparece el aviso de error «Sin permiso para buscar dispositivos Bluetooth. Concédelo en los ajustes de la app.» con el botón «Abrir ajustes», y no se busca nada

#### Scenario: Permiso concedido
- **WHEN** el usuario concede el permiso tras tocar «Buscar» en una sección
- **THEN** la búsqueda de esa sección empieza sin tener que volver a tocar el botón

### Requirement: Bluetooth apagado
Si el Bluetooth del móvil está apagado, la app SHALL decirlo y ofrecer activarlo; al apagarse con dispositivos conectados o reconectándose, SHALL mostrarlos sin conexión y, al volver a encenderse, SHALL conectarse sola a los configurados.

#### Scenario: Bluetooth apagado al buscar
- **WHEN** el Bluetooth está apagado y el usuario toca «Buscar» en cualquier sección
- **THEN** aparece el aviso de error «El Bluetooth está apagado. Actívalo para buscar dispositivos.» con el botón «Activar Bluetooth», y no se busca nada

#### Scenario: Bluetooth apagado durante la sesión
- **WHEN** el Neo y el Square están conectados y el usuario apaga el Bluetooth
- **THEN** las dos filas dicen «Sin conexión» y aparece el aviso de Bluetooth apagado

#### Scenario: Bluetooth encendido de nuevo
- **WHEN** el Bluetooth se apagó durante la sesión con la marcha 14 y el usuario lo vuelve a encender
- **THEN** el aviso desaparece, el Neo y el Square se conectan solos y el Neo recibe la resistencia de la 14

#### Scenario: Bluetooth apagado con el Square dormido
- **WHEN** el Square está configurado pero dormido, la app espera a que se despierte para conectarse y el usuario apaga y vuelve a encender el Bluetooth
- **THEN** la fila del Square vuelve a «Conectando» y, al pulsar un botón del Square, se conecta sin reiniciar la app ni buscarlo otra vez

### Requirement: Conexión mientras dura la sesión
Las conexiones SHALL mantenerse mientras dure la sesión de rodaje, también con la pantalla apagada o la app en segundo plano, y SHALL cerrarse al terminarla.

#### Scenario: Pantalla apagada
- **WHEN** el Neo y el Square están conectados y la pantalla se apaga durante 5 minutos
- **THEN** al encenderla, las dos conexiones siguen activas sin haber tenido que buscar otra vez

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del Neo y del Square, que quedan libres para otra app

### Requirement: Configurar los dispositivos
Al conectar con un Neo o un Square, la app SHALL configurarlo para las próximas sesiones, uno de cada tipo. Si una búsqueda manual conecta con otro del mismo tipo, SHALL configurar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conectan el Neo y el Square con el «Buscar» de cada sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esos dos sin buscarlos

#### Scenario: Otro Square
- **WHEN** hay un Square configurado que no está encendido, se enciende otro Square, se toca «Olvidar» y después «Buscar» en la sección Controlador
- **THEN** la app se conecta al Square encendido y, desde entonces, recuerda ese y no el anterior

### Requirement: Conexión al abrir la app
Al abrir la app con dispositivos configurados y el permiso de Bluetooth concedido, la app SHALL conectarse a ellos sola, sin buscar y sin que el usuario toque nada; mientras no conecta, la fila de cada uno SHALL decir «Conectando» con la ayuda para despertarlo.

#### Scenario: Los dos encendidos al abrir
- **WHEN** hay un Neo y un Square configurados, los dos están despiertos y se abre la app
- **THEN** los dos llegan a «Conectado» sin tocar nada, y el Neo recibe la resistencia de la marcha guardada

#### Scenario: Neo dormido al abrir
- **WHEN** el Neo configurado está dormido al abrir la app y se empieza a pedalear al cabo de un minuto
- **THEN** mientras tanto su fila dice «Conectando» con «Pedalea para despertarlo» y, al despertar, el Neo se conecta solo

#### Scenario: Primer uso
- **WHEN** no hay ningún dispositivo configurado y se abre la app
- **THEN** la app no busca ni se conecta sola: las filas dicen «Sin conexión» hasta tocar el «Buscar» de su sección

#### Scenario: Sin permiso al abrir
- **WHEN** hay dispositivos configurados pero falta el permiso de Bluetooth al abrir la app, y el usuario lo concede después desde Diagnóstico
- **THEN** la app no intenta conectar hasta tener el permiso y, al concederlo, se conecta a los configurados sin buscar

### Requirement: Olvidar un dispositivo
Cada sección del Rodillo o del Controlador con un dispositivo configurado SHALL tener «Olvidar». Olvidar SHALL desconectarlo, SHALL hacer que la app deje de conectarse a él sola, también en las siguientes sesiones, hasta que una búsqueda lo vuelva a encontrar, y SHALL dejar de publicar su servicio (spec `platform-bridge`). Sin dispositivo configurado, la sección SHALL NOT tener «Olvidar». La marcha SHALL NOT cambiar.

#### Scenario: Olvidar el Square
- **WHEN** el Square está conectado y se toca «Olvidar» en la sección Controlador
- **THEN** el Square se desconecta, su fila dice «Sin conexión» sin «Olvidar», el controlador deja de publicarse y la marcha sigue igual

#### Scenario: Olvidado en la siguiente sesión
- **WHEN** se olvida el Neo, se termina la sesión y se vuelve a abrir la app con el Neo encendido
- **THEN** la app no se conecta al Neo hasta tocar «Buscar» en la sección Rodillo

#### Scenario: Volver a buscar
- **WHEN** se ha olvidado el Square y se toca «Buscar» en la sección Controlador con el Square despierto
- **THEN** la app lo encuentra, se conecta, lo vuelve a configurar y vuelve a publicar el controlador

#### Scenario: Olvidar el Neo rodando
- **WHEN** se rueda en la marcha 14 y se olvida el Neo
- **THEN** el Neo se desconecta sin cambios de resistencia pendientes, la potencia y la cadencia muestran «—» en Rodaje, la marcha sigue en la 14 y la app deja de publicar

#### Scenario: Olvidar mientras conecta
- **WHEN** el Neo configurado está dormido, su fila dice «Conectando» y se toca «Olvidar»
- **THEN** la app deja de intentarlo y la fila dice «Sin conexión»

### Requirement: Buscar el ventilador aparte
Dispositivos SHALL mostrar una sección «Ventilador» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar» que busca un dispositivo cuyo nombre empiece por «HEADWIND» y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. Ningún otro «Buscar» SHALL buscar el ventilador.

#### Scenario: Buscar el ventilador
- **WHEN** el ventilador está encendido y el usuario toca «Buscar» en la sección Ventilador
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de la app de entrenamiento

#### Scenario: Buscar sin ventilador
- **WHEN** el usuario toca «Buscar» en la sección Rodillo sin ningún ventilador configurado
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

### Requirement: Configurar y reconectar el ventilador
Al conectar con un ventilador, la app SHALL configurarlo para las próximas sesiones, uno solo, conectarse a él sola al abrir la app y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL configurar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta el ventilador con el «Buscar» de su sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a ese ventilador sin buscarlo

#### Scenario: Ventilador apagado al abrir
- **WHEN** hay un ventilador configurado y está apagado al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otro ventilador
- **WHEN** hay un ventilador configurado y el usuario toca «Olvidar» y después «Buscar» en la sección Ventilador y conecta con otro
- **THEN** la app recuerda el nuevo y deja de intentar con el anterior

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del ventilador, que queda libre para otra app, con el modo y la velocidad que tenía

### Requirement: Olvidar el ventilador
La fila «Ventilador» con uno configurado SHALL tener «Olvidar». Olvidarlo SHALL desconectarlo, quitar su sección de Ajustes, su icono de Rodaje y su fila de Diagnóstico, y hacer que las acciones del ventilador avisen «Sin ventilador». Sin ventilador configurado, la fila SHALL NOT tener «Olvidar». Olvidarlo SHALL NOT cambiar el modo ni la velocidad del ventilador.

#### Scenario: Olvidar el ventilador
- **WHEN** el usuario toca «Olvidar» en la fila del ventilador
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin «Olvidar», y desaparecen la sección de Ajustes, el icono de Rodaje y la fila de Diagnóstico

#### Scenario: El ventilador sigue como estaba
- **WHEN** se olvida el ventilador mientras está en manual al 40 %
- **THEN** el ventilador sigue en manual al 40 %

#### Scenario: Olvidar y volver a buscar
- **WHEN** se olvida el ventilador y se toca «Buscar» en la sección Ventilador con él encendido
- **THEN** la app lo encuentra, lo conecta y lo vuelve a configurar

### Requirement: Buscar el pulsómetro aparte
Dispositivos SHALL mostrar una sección «Pulsómetro» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar» que busca un dispositivo que ofrezca el servicio de frecuencia cardiaca y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. Ningún otro «Buscar» SHALL buscar el pulsómetro.

#### Scenario: Buscar la banda
- **WHEN** la banda está puesta y encendida, y el usuario toca «Buscar» en la sección Pulsómetro
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de la app de entrenamiento

#### Scenario: Buscar sin banda
- **WHEN** el usuario toca «Buscar» en la sección Controlador sin ningún pulsómetro configurado
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

### Requirement: Configurar y reconectar el pulsómetro
Al conectar con un pulsómetro, la app SHALL configurarlo para las próximas sesiones, uno solo, y SHALL conectarse a él sola al abrir la app, sin que el usuario busque, y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL configurar el nuevo en su lugar. Olvidar el pulsómetro en Dispositivos SHALL desconectarlo, quitar su icono de Rodaje y su fila de Diagnóstico y dejar de publicar el pulso.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta la banda con el «Buscar» de su sección y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esa banda sin buscarla

#### Scenario: Banda apagada al abrir
- **WHEN** hay una banda configurada y está apagada al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otra banda
- **WHEN** hay una banda configurada y el usuario toca «Olvidar» y después «Buscar» en la sección Pulsómetro y conecta con otra
- **THEN** la app recuerda la nueva y deja de intentar con la anterior

#### Scenario: Olvidar la banda
- **WHEN** el usuario olvida el pulsómetro en Dispositivos
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin configurado, el icono de Rodaje desaparece y la app deja de publicar el pulso

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del pulsómetro, que queda libre para otro dispositivo

### Requirement: Dispositivos de la app
Dispositivos SHALL mostrar los dispositivos a los que se conecta la app, una sección por tipo en este orden: Rodillo, Controlador, Pulsómetro y Ventilador. El tipo «Controlador» SHALL ser genérico: hoy solo admite el Elite Square, pero no nombra ese modelo ni a una plataforma. Cada sección SHALL tener una fila con su icono (el del enlace, ver la spec `ui-components`), el tipo, el modelo configurado debajo («Tacx Neo 12345», «Elite Square») o «Ninguno configurado», su estado en palabra y, mientras la app intenta conectarlo, la ayuda para despertarlo. SHALL NOT mostrar lo que la app publica: eso está en Conexión.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados y se abre Dispositivos
- **THEN** las filas Rodillo y Controlador dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo

#### Scenario: Primer uso
- **WHEN** se abre Dispositivos sin dispositivos configurados
- **THEN** las cuatro filas dicen «Ninguno configurado» y cada sección tiene su «Buscar», sin «Buscar dispositivos»

#### Scenario: Neo dormido
- **WHEN** el Neo configurado está dormido y la app intenta conectarlo
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo»

#### Scenario: Sin filas de Zwift
- **WHEN** se abre Dispositivos con o sin wifi, con la app de entrenamiento conectada o no
- **THEN** no hay ninguna fila de Zwift ni de lo publicado, y su estado no cambia nada de lo que se ve

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth del móvil está apagado y se abre Dispositivos
- **THEN** las filas configuradas dicen «Sin conexión» y se ve el aviso «El Bluetooth está apagado. Actívalo para buscar dispositivos.» con «Activar Bluetooth», como en «Bluetooth apagado»

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
- **WHEN** se abre Dispositivos con el Square configurado
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
- **WHEN** no hay ningún dispositivo configurado y se abre Dispositivos
- **THEN** cada sección tiene su «Buscar» disponible y ninguna dice «Olvidar»

### Requirement: Botones de cada sección
Sin dispositivo configurado, cada sección SHALL tener solo «Buscar». Con uno configurado, SHALL tener «Olvidar» y, si no está conectado, también «Reconectar»; SHALL NOT tener «Buscar». La descripción accesible de cada botón SHALL nombrar el tipo («Buscar rodillo», «Reconectar controlador», «Olvidar pulsómetro»). Mientras busca, el «Buscar» de esa sección SHALL estar desactivado.

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al botón «Buscar» de la sección Pulsómetro
- **THEN** lo lee como «Buscar pulsómetro»

#### Scenario: Botones según el estado
- **WHEN** el rodillo está configurado y conectado, el controlador configurado y reconectando, y no hay pulsómetro configurado
- **THEN** la sección Rodillo tiene solo «Olvidar», la del Controlador «Reconectar» y «Olvidar», y la del Pulsómetro solo «Buscar»

#### Scenario: Cambiar de dispositivo
- **WHEN** hay una banda configurada y el usuario quiere usar otra
- **THEN** toca «Olvidar» y, con la sección ya sin configurado, «Buscar»

#### Scenario: Buscando uno
- **WHEN** se toca «Buscar» en la sección Rodillo
- **THEN** ese botón queda desactivado mientras busca y el «Buscar» del Controlador sigue disponible

### Requirement: Reconectar un dispositivo configurado
«Reconectar» SHALL reiniciar en el momento el intento de conexión con el dispositivo configurado de esa sección, por su identidad y no por su nombre, sin conectarse a otro del mismo tipo, y la app SHALL seguir intentándolo sola después, como con cualquier conexión perdida. SHALL NOT tocar las conexiones de las demás secciones ni cambiar la marcha.

#### Scenario: Reconectar el Square dormido
- **WHEN** el Square configurado está «Reconectando» y el usuario lo despierta y toca «Reconectar»
- **THEN** la app intenta conectar con ese Square al momento, la fila pasa a «Conectando» y después a «Conectado», y el Neo no se interrumpe

#### Scenario: Sigue sin aparecer
- **WHEN** el usuario toca «Reconectar» y el Square sigue apagado
- **THEN** la fila sigue en «Reconectando» con la ayuda para despertarlo, sin aviso de error, y la app sigue intentándolo sola

#### Scenario: Otro del mismo tipo encendido
- **WHEN** el Neo configurado está apagado, hay otro Neo encendido cerca y se toca «Reconectar»
- **THEN** la app no se conecta al otro Neo

#### Scenario: Con el Bluetooth apagado
- **WHEN** el Bluetooth está apagado y se toca «Reconectar»
- **THEN** la app muestra el aviso de Bluetooth apagado con «Activar Bluetooth», y no intenta nada

### Requirement: Batería baja del Square en Dispositivos
Con el Square conectado y su batería baja (spec `square-battery`), la sección Controlador de Dispositivos SHALL mostrar un aviso informativo, con icono y texto, que diga el nivel y que revise la pila antes de rodar, sin depender solo del color. SHALL dejar de mostrarlo cuando llegue un nivel válido por encima del umbral, y SHALL NOT mostrarlo con el Square sin conexión ni sin Square configurado. El aviso SHALL NOT cambiar el estado de conexión del Controlador ni deshabilitar sus botones.

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su último nivel válido es 15 %
- **THEN** la sección Controlador dice «Conectado» y muestra el aviso informativo «Batería del Square baja (15 %). Revisa la pila antes de rodar.»

#### Scenario: Batería recuperada
- **WHEN** llega una lectura periódica de 100 % tras cambiar la pila
- **THEN** el aviso desaparece

#### Scenario: Square sin conexión
- **WHEN** la batería estaba baja y el Square pierde la conexión
- **THEN** la sección Controlador dice el estado de conexión y no muestra el aviso de batería

#### Scenario: Sin Square configurado
- **WHEN** no hay ningún Square configurado
- **THEN** no hay aviso de batería

### Requirement: Dispositivo configurado inválido
Si un dispositivo configurado tiene una dirección que el móvil no puede usar (por ejemplo, tras importar una configuración dañada), la app SHALL seguir abierta, SHALL mostrarlo sin conexión y SHALL permitir buscarlo de nuevo u olvidarlo.

#### Scenario: Dirección inválida al abrir
- **WHEN** el Neo configurado tiene una dirección inválida y se abre la app
- **THEN** la app abre sin cerrarse, la fila del rodillo dice «Sin conexión», el registro anota la dirección inválida y el Square se conecta con normalidad

#### Scenario: Buscarlo de nuevo
- **WHEN** el rodillo configurado tiene una dirección inválida y el usuario toca «Buscar» con el Neo despierto
- **THEN** la app encuentra el Neo, lo configura con su dirección real y se conecta
