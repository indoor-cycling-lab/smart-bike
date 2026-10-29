# diagnostics Specification

## Purpose
Pantalla de Diagnóstico de la app y registro de sesión: ver en vivo y en crudo lo que envían el Square y el Neo y lo que la app les manda, para validar el equipo real y analizar problemas.

## Requirements

### Requirement: Botones del Square en vivo
Diagnóstico SHALL mostrar los 20 botones del Square como teclas que se ven pulsadas mientras se mantienen, varias a la vez si hace falta, y la máscara de los botones pulsados en hexadecimal.

#### Scenario: Dos botones a la vez
- **WHEN** se mantienen Campag. D y la palanca D1
- **THEN** las dos teclas se ven pulsadas y la máscara dice `0x80020000`

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado
- **THEN** ninguna tecla se ve pulsada, la máscara dice «—» y la sección dice «Conecta el Square para ver sus botones.»

### Requirement: Última combinación pulsada
Diagnóstico SHALL mostrar el último gesto reconocido: los botones que estaban pulsados a la vez, con el modificador (Campagnolo) primero, el gesto («pulsar», «corta», «larga», «doble» o «repetir») y la acción que produjo según la asignación vigente, o «sin acción» si no tiene ninguna.

#### Scenario: Combinación con modificador
- **WHEN** se mantiene Campag. D, se pulsa la palanca D1 y se sueltan los dos
- **THEN** en «Último gesto» se ve «Campag. D + Palanca D1 · pulsar · +3 marchas», y sigue visible después de soltarlos

#### Scenario: Botón sin acción
- **WHEN** se pulsa Y
- **THEN** en «Último gesto» se ve «Y · pulsar · sin acción»

#### Scenario: Tope
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** en «Último gesto» se ve «Palanca D1 · pulsar · Tope · marcha más dura»

#### Scenario: Sin pulsaciones todavía
- **WHEN** el Square está conectado y aún no se ha pulsado nada
- **THEN** «Último gesto» dice «Ninguno todavía»

#### Scenario: Pulsación larga
- **WHEN** la marcha es la 15 y se mantiene □ hasta el umbral de pulsación larga
- **THEN** en «Último gesto» se ve «□ · larga · Fijar la marcha de referencia»

#### Scenario: Repetición
- **WHEN** se mantiene la palanca D1 hasta que repite
- **THEN** en «Último gesto» se ve «Palanca D1 · repetir · +1 marcha»

#### Scenario: Asignación cambiada
- **WHEN** △ corta tiene asignada «−3 marchas» y se pulsa y se suelta △
- **THEN** en «Último gesto» se ve «△ · corta · −3 marchas»

#### Scenario: Plato con la Shimano
- **WHEN** con la Shimano en la 36×17 en automático se pulsa la palanca I1
- **THEN** en «Último gesto» se ve «Palanca I1 · pulsar · Plato 52»

### Requirement: Tráfico en crudo
Con el tráfico en directo activado, Diagnóstico SHALL mostrar las tramas recibidas del Square, del rodillo y de Zwift, y las enviadas al rodillo y a Zwift, las más recientes arriba, con hora (con milésimas), origen, característica y bytes en hexadecimal; lo que envía la app SHALL distinguirse por el origen «→ Rodillo» o «→ Zwift» y no solo por el color.

#### Scenario: Notificación de botones
- **WHEN** se pulsa la palanca D1
- **THEN** aparece una fila con la hora, el origen «Square», la característica de botones y los bytes recibidos, con resaltados los que cambian respecto a la trama anterior de la misma característica

#### Scenario: Telemetría del Neo
- **WHEN** se pedalea con el rodillo conectado
- **THEN** aparecen filas «Rodillo» de potencia, cadencia y respuestas FE-C, cada una con su característica

#### Scenario: Terreno de Zwift
- **WHEN** Zwift envía una pendiente nueva
- **THEN** aparece una fila «Zwift» con el punto de control de FTMS (`2AD9`) y sus bytes, seguida de la respuesta «→ Zwift» y del comando «→ Rodillo» si cambia lo que se envía al rodillo

#### Scenario: Telemetría hacia Zwift
- **WHEN** se pedalea con Zwift conectado
- **THEN** aparecen filas «→ Zwift» con los datos de la bici (`2AD2`) y la potencia (`2A63`)

### Requirement: Pausar y limpiar el tráfico
Con el tráfico en directo activado, Diagnóstico SHALL permitir pausar la lista de tráfico, para leerla sin que se mueva, y limpiarla; pausar SHALL NOT detener el registro de sesión.

#### Scenario: Pausar
- **WHEN** el usuario toca «Pausar» y sigue pulsando botones
- **THEN** la lista no cambia, el botón dice «Reanudar» y al tocarlo aparecen también las tramas llegadas durante la pausa

#### Scenario: Limpiar
- **WHEN** el usuario toca «Limpiar»
- **THEN** la lista queda vacía y dice «Sin tráfico todavía»

### Requirement: Registro de sesión
La app SHALL registrar con hora monotónica en milisegundos cada trama recibida o enviada al Neo, al Square y a Zwift, los cambios de conexión y de sus parámetros (también la red wifi y el anuncio a Zwift), los gestos, las acciones, los cambios de marcha y de modo de control, los comandos al Neo con su entrega y los envíos manuales, conservando al menos las últimas 10 000 entradas, y en la compilación de depuración SHALL volcar cada entrada al registro del sistema con una etiqueta propia.

#### Scenario: Capturar una traza
- **WHEN** en la compilación de depuración se pulsan botones del Square y se lee el registro del sistema filtrado por la etiqueta de la app
- **THEN** cada trama aparece con su hora, origen, característica y bytes, en un formato que se puede guardar como traza para los tests

#### Scenario: Seguir un cambio de marcha
- **WHEN** se pulsa la palanca D1 y se lee el registro
- **THEN** aparecen, en orden y con su hora, la notificación del Square, el gesto, la acción, la marcha nueva, el comando enviado al Neo y su entrega

#### Scenario: Seguir una sesión con Zwift
- **WHEN** se empareja la app en Zwift, se rueda y se cierra Zwift, y se lee el registro
- **THEN** aparecen, en orden y con su hora, el anuncio en la red, la conexión de Zwift con su dirección, cada mensaje de Zwift y cada respuesta en bytes, los cambios de modo de control, la desconexión de Zwift y la vuelta a la pendiente base a los 30 s

#### Scenario: Registro lleno
- **WHEN** se superan las 10 000 entradas
- **THEN** se descartan las más antiguas y la app sigue registrando sin ralentizarse

### Requirement: Simulación
Diagnóstico SHALL mostrar en la sección Simulación el modo de control, la marcha puesta (con la Shimano, plato × piñón con su relación), el modelo («Simple»), el modo de bajadas y la pendiente base o la de Zwift con su terreno, y SHALL actualizarlos con cada cambio. Cuando el modo de bajadas limita la pendiente, SHALL indicarlo con texto, junto con la pendiente que habría correspondido sin el límite.

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17, el rodillo está conectado y Zwift no
- **THEN** la sección dice modo Libre, marcha 17 de 24, modelo Simple, bajadas «La marcha no crea bajadas», pendiente base 0,0 %, ajuste +5,0 % y enviada al rodillo +5,0 %, sin indicación de límite

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de 4,0 % con Crr 0,0040, la marcha es la 14 y el rodillo está conectado
- **THEN** la sección dice modo SIM · Zwift, pendiente Zwift 4,0 %, el terreno con Crr 0,0040, ajuste +2,0 %, enviada al rodillo +6,0 % y «Simulación» como último mensaje de Zwift con su hora

#### Scenario: Pendiente limitada
- **WHEN** Zwift envía una pendiente de −7,0 %, la marcha es la 1 y el modo de bajadas es «La marcha no crea bajadas»
- **THEN** la sección dice pendiente Zwift −7,0 %, ajuste −11,0 %, enviada al rodillo −7,0 % y «Limitada por Bajadas: sin límite sería −18,0 %»

#### Scenario: Límite que deja de actuar
- **WHEN** la pendiente está limitada y se sube de marcha hasta que la suma supera el límite
- **THEN** desaparece la indicación de límite y la enviada al rodillo coincide con la suma

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide 230 W y el ajuste de las palancas es +10 W
- **THEN** la sección dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al rodillo 240 W, y la marcha sigue visible

#### Scenario: ERG manual
- **WHEN** se entró en ERG manual a 200 W, se ha subido a 220 W y el rodillo está conectado
- **THEN** la sección dice modo ERG · manual, potencia fijada 220 W, entrada 200 W y enviada al rodillo 220 W, y la marcha sigue visible

#### Scenario: ERG manual sobre una ruta de Zwift
- **WHEN** en ERG manual sobre una ruta de Zwift, Zwift envía una pendiente de 3,0 %
- **THEN** la sección sigue diciendo modo ERG · manual, el último mensaje de Zwift es «Simulación» con su hora y la enviada al rodillo no cambia

#### Scenario: Zwift desconectado
- **WHEN** se cierra Zwift rodando en una subida
- **THEN** durante 30 s la sección dice que se mantiene lo último de Zwift y después vuelve a modo Libre con la pendiente base

#### Scenario: Neo sin conexión
- **WHEN** el rodillo no está conectado
- **THEN** la marcha, el modelo, el modo de bajadas, la pendiente base o la de Zwift y el ajuste siguen visibles, «Enviada al rodillo» muestra «—» y no hay indicación de límite

#### Scenario: Shimano
- **WHEN** la transmisión es la Shimano, la marcha la 52×17, el rodillo está conectado, Zwift no y «Bajadas» está en «Sin protección»
- **THEN** la sección dice marcha 52×17 · relación 3,06, modelo Simple, pendiente base 0,0 %, ajuste +5,1 % y enviada al rodillo +5,1 %

### Requirement: Potencia y envíos en Simulación
La sección Simulación SHALL mostrar además, en ERG de Zwift, la potencia de Zwift y el ajuste de las palancas; en ERG manual, la potencia fijada y la de entrada; el ajuste por la marcha, la última pendiente o potencia enviada al rodillo, el último mensaje de Zwift con su hora y la latencia de la última palanca, y SHALL actualizarlos con cada cambio.

#### Scenario: Último envío al Neo
- **WHEN** la app envía una pendiente o potencia nueva al rodillo
- **THEN** Simulación muestra ese último valor enviado

### Requirement: Diagnóstico desde Ajustes
Diagnóstico SHALL abrirse desde la entrada «Diagnóstico» de Ajustes, sin barra inferior, con un botón de volver a Ajustes y sus secciones en este orden: datos de conexión, botones del Square, simulación, tráfico y control manual del rodillo. SHALL NOT tener enlaces a Botones ni a Ajustes, que están en la barra inferior.

#### Scenario: Volver a Ajustes
- **WHEN** se abre Diagnóstico desde Ajustes y se toca volver, o se pulsa Atrás
- **THEN** se vuelve a Ajustes sin terminar la sesión

#### Scenario: Sin enlaces a otras pestañas
- **WHEN** se recorre Diagnóstico
- **THEN** no aparecen «Asignación de botones» ni «Ajustes»

#### Scenario: Control manual al salir
- **WHEN** en Diagnóstico se ha enviado al rodillo una pendiente a mano y se vuelve a Ajustes
- **THEN** el rodillo vuelve al comando de la marcha, como al salir de Diagnóstico hacia Rodaje

### Requirement: Tráfico en directo bajo demanda
La sección Tráfico de Diagnóstico SHALL empezar con la casilla «Tráfico en directo» desmarcada cada vez que se abre la pantalla, y sin el volcado ni «Pausar» y «Limpiar» mientras siga desmarcada. Al marcarla SHALL aparecer el volcado con las tramas más recientes, también las llegadas con la casilla desmarcada. Al desmarcarla SHALL ocultarse de nuevo y anularse la pausa. La casilla SHALL conservar su valor al girar el móvil, y ocultar el tráfico SHALL NOT detener el registro de sesión.

#### Scenario: Al abrir Diagnóstico
- **WHEN** el usuario abre Diagnóstico desde Ajustes con el rodillo y el Square conectados
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar»

#### Scenario: Activar el tráfico
- **WHEN** con la casilla desmarcada se pulsa la palanca D1 y después se marca la casilla
- **THEN** aparece el volcado con la fila «Square» de esa pulsación entre las más recientes, y «Pausar» y «Limpiar» están disponibles

#### Scenario: Ocultar el tráfico
- **WHEN** con el tráfico en directo y en pausa el usuario desmarca la casilla, pulsa botones del Square y la vuelve a marcar
- **THEN** el volcado desaparece al desmarcarla y, al marcarla otra vez, muestra en directo las tramas de esas pulsaciones y el botón dice «Pausar»

#### Scenario: Volver a Diagnóstico
- **WHEN** con el tráfico en directo el usuario vuelve a Ajustes y abre Diagnóstico de nuevo
- **THEN** la casilla «Tráfico en directo» está desmarcada y el volcado no se ve

#### Scenario: Girar el móvil
- **WHEN** con el tráfico en directo el usuario gira el móvil
- **THEN** la casilla sigue marcada y el volcado sigue visible

#### Scenario: El registro sigue grabando
- **WHEN** con el tráfico oculto se pedalea con el rodillo conectado y se lee el registro del sistema de la compilación de depuración
- **THEN** cada trama del rodillo aparece en el registro como con el tráfico visible

#### Scenario: Sin dispositivos
- **WHEN** sin el rodillo ni el Square conectados y sin tramas registradas se marca la casilla
- **THEN** el volcado dice «Sin tráfico todavía»

#### Scenario: Conexión perdida con el tráfico visible
- **WHEN** con el tráfico en directo se pierde la conexión con el Square
- **THEN** las filas del Square ya mostradas siguen en el volcado, no llegan filas nuevas suyas y las del rodillo siguen apareciendo

#### Scenario: Accesibilidad
- **WHEN** TalkBack recorre la sección Tráfico
- **THEN** anuncia «Tráfico en directo» como casilla con su estado, y toda la fila, de 48 dp de alto como mínimo, la cambia

### Requirement: Modelo físico en la simulación
Cuando el modelo es «Físico», la sección Simulación de Diagnóstico SHALL mostrar el nombre del modelo, la potencia calculada, la potencia mínima aplicada, la cadencia suavizada, la velocidad y la relación de la marcha. Con «Por tabla» SHALL mostrar el ajuste de la tabla para la marcha puesta, y con cualquier modelo la dificultad si no es 100 %. Cuando la potencia mínima o el alivio por cadencia baja actúan, SHALL indicarlo con texto, junto con la potencia que habría correspondido sin ellos.

#### Scenario: Físico rodando
- **WHEN** se rueda sin Zwift con el físico a 90 rpm en la 36×17 y el rodillo está conectado
- **THEN** la sección dice modelo Físico, la potencia calculada, la mínima de 30 W, cadencia 90 rpm, velocidad en km/h, relación 2,12 y enviada al rodillo en W

#### Scenario: Potencia mínima actuando
- **WHEN** en una bajada el físico calcula 12 W y la mínima es 30 W
- **THEN** la sección dice enviada al rodillo 30 W y «Limitada por la potencia mínima: sin límite sería 12 W»

#### Scenario: Por tabla
- **WHEN** el modelo es «Por tabla» y la marcha 17 tiene +7,0 % en la tabla
- **THEN** la sección dice modelo Por tabla, ajuste +7,0 % y enviada al rodillo la suma con el terreno

#### Scenario: Dificultad distinta de 100 %
- **WHEN** la dificultad es 120 %
- **THEN** la sección dice «Dificultad 120 %»

#### Scenario: Neo sin conexión
- **WHEN** el rodillo no está conectado con el físico elegido
- **THEN** la sección dice modelo Físico, la velocidad y «Enviada al rodillo» muestran «—»

### Requirement: Datos de conexión
Diagnóstico SHALL tener una sección «Datos de conexión» con solo los datos de conexión que no se ven en Conexión ni en Dispositivos: cada fila lleva un dato y, cuando importa, hace cuánto se leyó. La sección SHALL NOT repetir el estado de conexión de los dispositivos ni de Zwift, SHALL NOT tener el botón «Buscar dispositivos» ni las acciones «Activar Bluetooth» y «Abrir ajustes», y SHALL NOT mostrar avisos de conexión (rodillo no encontrado, reconectando, Bluetooth apagado, sin permiso o sin wifi), que quedan en Dispositivos y en Conexión.

#### Scenario: Sin estado repetido
- **WHEN** se abre Diagnóstico con el rodillo, el Square y Zwift conectados
- **THEN** la sección «Datos de conexión» no tiene filas del rodillo, del Square como dispositivo, de la app de entrenamiento como conexión ni del controlador, ni «Buscar dispositivos»

#### Scenario: Conexión perdida
- **WHEN** el Square se está reconectando
- **THEN** Diagnóstico no muestra ningún aviso de reconexión y las demás secciones siguen con sus propios mensajes («Conecta el Square para ver sus botones.»)

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth está apagado
- **THEN** Diagnóstico no muestra el aviso de Bluetooth apagado ni el botón «Activar Bluetooth»

#### Scenario: Dispositivo ausente
- **WHEN** no hay ningún Square, pulsómetro ni ventilador configurado y Zwift no ha conectado
- **THEN** la sección solo tiene las filas de la app de entrenamiento, con «—» en la dirección, y nunca las de batería, pulso ni ventilador

### Requirement: Datos de la app de entrenamiento en Diagnóstico
La sección «Datos de conexión» SHALL tener una fila «App de entrenamiento» con la dirección de red desde la que se ha conectado, sea cual sea la app (Zwift, Rouvy u otra), o «—» si no hay ninguna conectada, y una fila «Nombre anunciado» con el nombre con el que la app se anuncia ahora, también sin app conectada y sin wifi, como el último que se anunció o se anunciaría. SHALL registrar en el registro cada vez que la app se anuncia o se retira con ese nombre. SHALL NOT nombrar a Zwift salvo en los datos propios de su protocolo.

#### Scenario: App conectada por wifi
- **WHEN** una app de entrenamiento, sea Zwift o Rouvy, está conectada desde el PC 192.168.0.10
- **THEN** la fila «App de entrenamiento» dice «Desde 192.168.0.10», sin el nombre de la app

#### Scenario: Conectada por Bluetooth
- **WHEN** la app de entrenamiento está conectada por Bluetooth
- **THEN** la fila dice «Por Bluetooth»

#### Scenario: Sin app conectada
- **WHEN** ninguna app de entrenamiento está conectada
- **THEN** la fila dice «—» y no explica cómo emparejar, que está en Conexión

#### Scenario: Nombre anunciado por defecto
- **WHEN** «Smart Bike» está elegido en «Nombre ante la app» y se abre Diagnóstico
- **THEN** «Nombre anunciado» dice «Smart Bike»

#### Scenario: Nombre anunciado como KICKR
- **WHEN** «KICKR BIKE PRO» está elegido en «Nombre ante la app» y se abre Diagnóstico
- **THEN** se muestra «KICKR BIKE PRO» con sus cuatro caracteres y el registro tiene el anuncio con ese nombre

#### Scenario: Cambio del ajuste
- **WHEN** se cambia «Nombre ante la app» con Diagnóstico abierto
- **THEN** el registro tiene la retirada del nombre anterior y el anuncio del nuevo, y se muestra el nuevo

#### Scenario: Sin wifi
- **WHEN** no hay wifi y se abre Diagnóstico con «KICKR BIKE PRO» elegido
- **THEN** la fila «App de entrenamiento» dice «—» y «Nombre anunciado» muestra el nombre que se anunciará

### Requirement: Simulación de Zwift en Simulación
Cuando Zwift envía su simulación de marchas virtuales, la sección Simulación de Diagnóstico SHALL mostrar lo último recibido: pendiente, coeficiente de rodadura, coeficiente aerodinámico, masas, relación de la marcha de Zwift y la hora, aparte de lo que simula el rodillo; y SHALL registrar cada simulación recibida y su respuesta, con su trama, en el registro. Si Zwift no ha enviado ninguna, SHALL NOT mostrar esa línea. SHALL NOT mostrarse en «Datos de conexión».

#### Scenario: Simulación recibida
- **WHEN** Zwift envía una pendiente de −1,64 % con una relación de marcha de 1,68
- **THEN** la sección Simulación muestra la pendiente −1,64 % y la relación 1,68 de Zwift con su hora, y el registro tiene la trama recibida y la respuesta

#### Scenario: Sin simulación de Zwift
- **WHEN** el mando va por Bluetooth o Zwift no ha enviado ninguna simulación
- **THEN** Diagnóstico no muestra la línea de la simulación de Zwift

#### Scenario: Diferencia con lo que simula el rodillo
- **WHEN** Zwift envía una pendiente de 4 % y la marcha de la app es la 14
- **THEN** la sección Simulación muestra la pendiente de Zwift (4 %) y, aparte, lo que simula el rodillo (+6 %), sin mezclarlos

#### Scenario: Fuera de los datos de conexión
- **WHEN** Zwift ha enviado su simulación
- **THEN** «Datos de conexión» no tiene ninguna fila de la simulación de Zwift

### Requirement: Batería del Square en Datos de conexión
Si hay un Square configurado, «Datos de conexión» SHALL tener una fila «Batería del Square» con su nivel en porcentaje y, si es bajo (spec `square-battery`), la palabra «baja» junto al nivel, sin depender solo del color. Mientras no haya un nivel válido SHALL decir «Sin lectura todavía». Con el Square sin conexión SHALL seguir mostrando el último nivel válido, si lo hay. Sin Square configurado, la fila SHALL NOT aparecer. SHALL NOT mostrar un aviso aparte.

#### Scenario: Nivel conocido
- **WHEN** el Square está conectado y su último nivel válido es 80 %
- **THEN** la fila dice «80 %», sin «baja»

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su último nivel válido es 15 %
- **THEN** la fila dice «15 % · baja» y no hay aviso aparte

#### Scenario: Batería recuperada
- **WHEN** llega una lectura periódica de 100 % tras cambiar la pila
- **THEN** la fila dice «100 %» sin «baja»

#### Scenario: Sin lectura
- **WHEN** el Square acaba de conectarse y aún no ha llegado una lectura periódica
- **THEN** la fila dice «Sin lectura todavía»

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y su último nivel válido fue 60 %
- **THEN** la fila dice «Última lectura: 60 %»

#### Scenario: Nunca conectado
- **WHEN** no hay ningún Square configurado
- **THEN** no hay fila de batería

### Requirement: Pulso en Datos de conexión
Si hay un pulsómetro configurado, «Datos de conexión» SHALL tener una fila «Pulsómetro» con el último pulso válido en bpm y cuántos segundos hace que se leyó, y «—» si no hay pulso. Sin pulsómetro configurado, la fila SHALL NOT aparecer. El tráfico de la banda y de lo que se envía a Zwift SHALL verse en el tráfico en crudo cuando esté activado.

#### Scenario: Banda dando pulso
- **WHEN** el pulsómetro da 128 bpm
- **THEN** la fila dice «128 bpm» con el tiempo de la última lectura

#### Scenario: Sin pulso
- **WHEN** el pulsómetro no da lecturas válidas desde hace más de 5 s
- **THEN** la fila dice «—» en lugar del pulso

#### Scenario: Banda reconectando
- **WHEN** la banda configurada se ha desconectado
- **THEN** la fila dice «—», sin aviso: el estado está en Dispositivos

#### Scenario: Sin pulsómetro configurado
- **WHEN** no hay pulsómetro configurado
- **THEN** no hay fila «Pulsómetro»

#### Scenario: Tráfico de la banda
- **WHEN** el tráfico en directo está activado y la banda da una lectura que se reenvía a Zwift
- **THEN** el tráfico muestra la trama recibida de la banda y la enviada a Zwift, cada una con su origen y su sentido

### Requirement: Ventilador en Datos de conexión
Si hay un ventilador configurado, «Datos de conexión» SHALL tener una fila «Ventilador» con su modo y su velocidad tal como los comunica el ventilador y cuánto hace de su última notificación, o «—» si no hay ninguna. Sin ventilador configurado, la fila SHALL NOT aparecer. El tráfico del ventilador, lo enviado y lo recibido, SHALL verse en el tráfico en crudo y en el registro de sesión con su propio origen, igual que el de los demás dispositivos.

#### Scenario: Sin ventilador configurado
- **WHEN** no hay ventilador configurado y se abre Diagnóstico
- **THEN** no hay fila del ventilador

#### Scenario: Ventilador conectado
- **WHEN** el ventilador está conectado en manual al 40 %
- **THEN** su fila dice «Manual · 40 %» y hace cuánto fue la última notificación

#### Scenario: Ventilador reconectando
- **WHEN** el ventilador configurado se ha desconectado
- **THEN** su fila dice el último modo y velocidad conocidos con su antigüedad, sin aviso: el estado está en Dispositivos

#### Scenario: Sin notificación
- **WHEN** el ventilador está conectado y aún no ha notificado
- **THEN** su fila dice «—» en lugar del modo, la velocidad y la antigüedad

#### Scenario: Tráfico del ventilador
- **WHEN** el tráfico en directo está activado y el ventilador recibe una orden y notifica
- **THEN** el tráfico muestra la trama enviada y la recibida con el origen del ventilador, y el registro de sesión las recoge igual
