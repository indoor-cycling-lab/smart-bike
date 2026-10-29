## MODIFIED Requirements

### Requirement: Conexiones de dispositivos en la barra superior
La barra superior SHALL mostrar los dispositivos a los que se conecta la app, uno por icono y en orden fijo: Neo, Square y, si están recordados, pulsómetro y ventilador. El Square lleva el icono de un manillar de bici de carretera, que SHALL leerse a 14 dp en todos los estados. En la descripción accesible, el Square SHALL llamarse «Controlador». El estado se ve por la forma: normal si conectado, atenuado con marca girando si se intenta conectar y tachado en color de alerta si no. La barra SHALL NOT mostrar iconos de Zwift ni del controlador publicado, ni cambiar de ancho, y es un solo objetivo táctil que abre Dispositivos.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados
- **THEN** se ven los iconos del Neo y del Square, normales, sin marcas, y ninguno de Zwift ni del controlador publicado

#### Scenario: Descripción accesible
- **WHEN** el Neo está conectado y el Square sin conexión
- **THEN** la descripción accesible de la barra dice «Conexiones: Neo, conectado; Controlador, sin conexión»

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** el icono del Square pasa a atenuado con la marca girando, sin que cambie el ancho de la barra, la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** el icono del Square se ve tachado y la descripción accesible dice «Controlador, sin conexión»

#### Scenario: Estado al tocar
- **WHEN** el icono del Square está atenuado con la marca girando y se toca la barra
- **THEN** se abre Dispositivos, donde la fila del Square dice «Conectando»

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y el Neo se está conectando
- **THEN** la marca del icono del Neo no gira y el icono sigue atenuado con la marca, distinto del tachado

#### Scenario: Manillar legible en cada estado
- **WHEN** el Square está conectado, reconectando, sin conexión o con la batería baja
- **THEN** el manillar se reconoce a 14 dp en los cuatro, y el hueco con la marca girando, el tachado y la insignia de batería no lo hacen confundir con otro enlace

### Requirement: Estado de la publicación en Rodaje
Rodaje SHALL mostrar, a la izquierda de la fila de Ajustes, el estado del rodillo publicado sin nombrar ninguna app de entrenamiento, con palabra y forma, sin decir por dónde va: «Conectado» (punto), «Esperando conexión…» (aro que gira, atenuado), y «No visible en la red», «Sin wifi», «Sin Bluetooth», «No disponible» o «Sin rodillo» (aro vacío con borde de alerta). Solo SHALL nombrar el transporte cuando falte. Tocarlo SHALL abrir Conexión.

#### Scenario: Esperando
- **WHEN** hay un rodillo recordado, el transporte del modo está disponible y ninguna app de entrenamiento está conectada
- **THEN** Rodaje muestra «Esperando conexión…» atenuado y con el aro que gira, y la marcha, la potencia y la cadencia siguen como sin app de entrenamiento

#### Scenario: Conectado por wifi
- **WHEN** el modo es «Wifi + Bluetooth» y la app de entrenamiento recibe el rodillo, el controlador y, si hay pulsómetro, el pulso
- **THEN** Rodaje muestra «Conectado» con el punto, sin «wifi» ni «Bluetooth», sin cambiar la marcha ni el área de datos

#### Scenario: Conectado por Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y la app de entrenamiento recibe el rodillo
- **THEN** Rodaje muestra «Conectado» con el punto, igual que por wifi

#### Scenario: Sin rodillo recordado
- **WHEN** no hay rodillo recordado
- **THEN** Rodaje muestra «Sin rodillo» con el aro vacío y el borde de alerta, y tocarlo abre Conexión

#### Scenario: No visible
- **WHEN** el anuncio por wifi no responde (spec `platform-bridge`)
- **THEN** Rodaje muestra «No visible en la red» con el aro vacío y el borde de alerta, y la marcha, la potencia y la cadencia siguen como sin app de entrenamiento

#### Scenario: Sin wifi
- **WHEN** el modo usa wifi para el rodillo y el móvil no tiene wifi
- **THEN** Rodaje muestra «Sin wifi» con el aro vacío y el borde de alerta

#### Scenario: Sin Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y el Bluetooth está apagado o falta un permiso
- **THEN** Rodaje muestra «Sin Bluetooth» con el aro vacío y el borde de alerta, y no dice nada del wifi

#### Scenario: No disponible
- **WHEN** el modo es «Solo Bluetooth» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** Rodaje muestra «No disponible» con el aro vacío y el borde de alerta

#### Scenario: Desconectado a mitad
- **WHEN** se rueda con la app de entrenamiento conectada y se cierra
- **THEN** el estado pasa a «Esperando conexión…» y el resto de la pantalla no cambia

#### Scenario: Tocar el estado
- **WHEN** se toca el estado, que mide al menos 48 dp de alto
- **THEN** se abre Conexión con la barra inferior, y Atrás o «Rodar» vuelven a Rodaje con la sesión en marcha

#### Scenario: Sin cambiar el área de datos
- **WHEN** el estado pasa de «Esperando conexión…» a «Conectado»
- **THEN** la rejilla de datos mantiene su área y su cifra, y nada se mueve

### Requirement: Lo que falta en el estado de Rodaje
Con el rodillo conectado, el estado de Rodaje SHALL añadir «· sin controlador» si el modo es «Wifi + Bluetooth», hay un Square recordado y la app de entrenamiento no ha saludado al controlador; «· sin pulso» si hay un pulsómetro recordado y no está suscrita al pulso; o «· sin controlador ni pulso» si faltan los dos. Lo no recordado SHALL NOT mencionarse.

#### Scenario: Mando esperando
- **WHEN** el modo es «Wifi + Bluetooth», el rodillo está conectado, hay un Square recordado y el controlador no se ha saludado
- **THEN** Rodaje muestra «Conectado · sin controlador» y la descripción accesible dice «Conexión: rodillo, conectado; controlador, esperando»

#### Scenario: Pulso sin elegir
- **WHEN** el rodillo y el controlador están conectados, hay un pulsómetro recordado y la app de entrenamiento no está suscrita al pulso
- **THEN** Rodaje muestra «Conectado · sin pulso»

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** el modo es «Solo Bluetooth» o «Solo wifi (modo Zwift Ride)», el rodillo está conectado, hay un Square recordado y la app de entrenamiento aún no ha saludado al controlador
- **THEN** Rodaje muestra «Conectado», sin «sin controlador», porque el controlador va dentro del mismo dispositivo

#### Scenario: Sin Square ni pulsómetro recordados
- **WHEN** el rodillo está conectado y no hay Square ni pulsómetro recordados
- **THEN** Rodaje muestra «Conectado», sin mencionar el controlador ni el pulso

#### Scenario: Banda sin lectura
- **WHEN** la app de entrenamiento está suscrita al pulso y la banda no da lecturas
- **THEN** Rodaje no añade «sin pulso»: lo que falta es la banda, que se ve en la barra de conexiones

### Requirement: Batería baja del Square en la barra de conexiones
Con la batería del Square baja (spec `square-battery`) y el Square conectado, el icono del Square de la barra de conexiones SHALL mostrar una marca de aviso que no existe en los demás estados, en el color normal y sin cambiar el ancho de la barra. La descripción accesible de la barra SHALL decir «Controlador, batería baja». Con el Square reconectando o sin conexión, el icono SHALL mostrar su estado de conexión, no el aviso.

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su batería baja
- **THEN** el icono del Square se ve con la marca de aviso, la descripción accesible dice «Conexiones: Neo, conectado; Controlador, batería baja; …» y no hay porcentaje en pantalla

#### Scenario: Batería normal
- **WHEN** el Square está conectado y su batería no está baja
- **THEN** el icono del Square se ve normal, sin marca

#### Scenario: Square reconectando con la batería baja
- **WHEN** la batería está baja y el Square pierde la conexión
- **THEN** el icono del Square pasa a atenuado con la marca girando, sin la marca de aviso

#### Scenario: Ancho estable
- **WHEN** el icono del Square pasa de normal a batería baja
- **THEN** la barra no cambia de ancho y nada se mueve

#### Scenario: Batería baja en vertical y en horizontal
- **WHEN** la batería del Square está baja
- **THEN** la marca se ve igual en vertical y en horizontal, en los dos temas
