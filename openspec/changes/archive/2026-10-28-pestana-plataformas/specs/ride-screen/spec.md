# Spec Delta

## MODIFIED Requirements

### Requirement: Hora, wifi y batería del móvil en Rodaje
La pantalla de rodaje SHALL mostrar, aunque las barras del sistema estén ocultas, la hora, el estado del wifi y la batería del móvil. La hora SHALL verse como `hh:mm` de 24 horas, sin segundos, y SHALL cambiar en el minuto exacto. El wifi SHALL verse con un icono normal si el móvil tiene wifi y tachado, en el color de alerta, si no. La batería SHALL verse con un icono cuyo relleno sigue el nivel, con el porcentaje al lado. Ningún estado SHALL depender solo del color.

#### Scenario: Hora en 24 horas
- **WHEN** son las 14:32 y el sistema muestra la hora en formato de 12 horas
- **THEN** Rodaje muestra «14:32», sin segundos ni «p. m.»

#### Scenario: Cambio de minuto
- **WHEN** la hora pasa de 14:32 a 14:33
- **THEN** Rodaje pasa a mostrar «14:33» sin esperar a que se toque la pantalla ni se cambie nada

#### Scenario: Cambio de zona horaria
- **WHEN** el sistema cambia de zona horaria o de hora mientras se rueda
- **THEN** Rodaje muestra la hora nueva en cuanto cambia

#### Scenario: Con wifi
- **WHEN** el móvil tiene wifi
- **THEN** se ve el icono de wifi normal, sin marca

#### Scenario: Sin wifi
- **WHEN** el móvil pierde el wifi mientras se rueda
- **THEN** el icono de wifi se ve tachado en el color de alerta, el estado de la plataforma dice «Sin wifi para Zwift» (spec `ride-screen`, «Estado de la plataforma en Rodaje») y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Wifi que vuelve
- **WHEN** el icono de wifi está tachado y el móvil recupera el wifi
- **THEN** el icono pasa a normal, sin que cambie el ancho del grupo ni se mueva el resto de la pantalla

#### Scenario: Batería con carga media
- **WHEN** la batería está al 82 % y el móvil no está cargando
- **THEN** se ve un icono de batería con aproximadamente cuatro quintos del relleno y «82 %» al lado

#### Scenario: Batería llena
- **WHEN** la batería está al 100 %
- **THEN** el icono se ve lleno y el texto dice «100 %», sin cortarse ni mover el resto del grupo

#### Scenario: Móvil cargando
- **WHEN** el móvil está conectado al cargador
- **THEN** el icono de batería lleva un rayo, también si está al 100 %, y deja de llevarlo al desconectar el cargador

#### Scenario: Batería baja
- **WHEN** la batería baja del 15 % y el móvil no está cargando
- **THEN** el icono se ve en el color de alerta con el relleno reducido a una franja mínima, el porcentaje sigue visible y no aparece ningún aviso emergente ni sonoro

#### Scenario: Batería baja cargando
- **WHEN** la batería está al 9 % y el móvil está cargando
- **THEN** el icono lleva el rayo y no se ve en el color de alerta

#### Scenario: Estados sin color
- **WHEN** la pantalla de rodaje se ve en escala de grises con el wifi tachado y la batería al 9 %
- **THEN** el wifi tachado se distingue del normal por su forma, y la batería baja se distingue de la media por el relleno y por el número

#### Scenario: Batería no disponible
- **WHEN** el sistema no da el nivel de la batería
- **THEN** no se ve el icono de batería ni el porcentaje, la hora y el wifi siguen y el resto de la pantalla no cambia

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al grupo con la hora 14:32, el wifi conectado y la batería al 82 % cargando
- **THEN** lo lee como un solo elemento: «14:32, wifi conectado, batería 82 %, cargando»

### Requirement: Ajustes desde Rodaje
Rodaje SHALL mostrar en la esquina superior derecha un botón con el icono de Ajustes, el mismo de la pestaña, con objetivo táctil de al menos 48 dp y descripción accesible «Ajustes», separado de la barra de conexiones para que no se toque uno por otro. En horizontal SHALL ir en la línea del modo, a la derecha de la barra; en vertical, al final de la línea del modo, bajo la franja de la barra. Al tocarlo SHALL abrirse Ajustes como pestaña, sin terminar la sesión ni cambiar la marcha.

#### Scenario: Abrir Ajustes en horizontal
- **WHEN** el móvil está en horizontal, se rueda en la marcha 14 y se toca el icono de Ajustes
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada, sin botón de volver, y al tocar «Rodar» se vuelve a Rodaje en la marcha 14

#### Scenario: Abrir Ajustes en vertical
- **WHEN** el móvil está en vertical y se toca el icono de Ajustes, al final de la línea del modo
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada

#### Scenario: Atrás desde Ajustes abierto con el icono
- **WHEN** se abre Ajustes con el icono de Rodaje y se pulsa Atrás
- **THEN** se vuelve a Rodaje y la sesión sigue en marcha

#### Scenario: Línea de chips en vertical con el icono
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión y Zwift esperando
- **THEN** el estado de la plataforma y el icono de Ajustes caben en una sola línea, bajo la barra de conexiones, el modo con la transmisión va en la línea de debajo, y al pasar todo a conectado ni el icono, ni la marcha ni los datos se mueven

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados
- **THEN** el icono de Ajustes se ve en su sitio y abre Ajustes como siempre, y el pie sigue diciendo «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexión perdida al abrir Ajustes
- **WHEN** se abre Ajustes con el icono y, con Ajustes abierto, se pierde la conexión con el Square
- **THEN** la app intenta reconectarlo como desde Rodaje y, al volver a Rodaje, el icono del Square muestra su estado y la marcha es la misma

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la barra superior de Rodaje
- **THEN** anuncia la barra de conexiones como un solo elemento con el estado de cada enlace y, después, el modo y «Ajustes», botón

#### Scenario: En ERG
- **WHEN** se está en ERG manual y se toca el icono de Ajustes
- **THEN** se abre Ajustes, y al volver a Rodaje la potencia objetivo es la misma

### Requirement: Ventilador en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás (el pulsómetro incluido), un icono del ventilador solo si hay uno recordado, con los estados de los demás enlaces (conectado, esperando y sin conexión) y su descripción accesible en la de la barra («Ventilador, conectado»). El icono SHALL NOT mostrar el modo ni la velocidad, y la barra SHALL mantener su objetivo táctil de 48 dp y su ancho independiente del estado.

#### Scenario: Sin ventilador recordado
- **WHEN** no hay ventilador recordado
- **THEN** la barra muestra los enlaces de siempre y ningún ventilador

#### Scenario: Ventilador conectado
- **WHEN** hay ventilador recordado y está conectado
- **THEN** la barra termina con el icono del ventilador normal y su descripción dice «Ventilador, conectado»

#### Scenario: Ventilador reconectando
- **WHEN** el ventilador recordado se ha desconectado y la app intenta recuperarlo
- **THEN** el icono se ve atenuado con la marca que gira, y su descripción dice «Ventilador, reconectando»

#### Scenario: Ventilador sin conexión
- **WHEN** el ventilador recordado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el icono se ve tachado, y su descripción dice «Ventilador, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square, el pulsómetro y el ventilador están en uso, a 360 dp en vertical
- **THEN** los cuatro iconos caben en la barra, sin recortarse ni empujar al icono de Ajustes ni a la hora, el wifi y la batería

### Requirement: Pulsómetro en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás, un icono del pulsómetro (un corazón) solo si hay un pulsómetro recordado, con los mismos estados y formas que los demás enlaces: conectado, esperando (reconectando o buscando) y sin conexión. Su descripción accesible SHALL entrar en la de la barra («Pulsómetro, conectado»). El icono SHALL NOT mostrar el pulso, y la barra SHALL mantener su único objetivo táctil de al menos 48 dp y su ancho independiente del estado.

#### Scenario: Sin pulsómetro recordado
- **WHEN** no hay pulsómetro recordado
- **THEN** la barra muestra los enlaces de siempre y ningún corazón

#### Scenario: Pulsómetro conectado
- **WHEN** hay pulsómetro recordado y está conectado
- **THEN** la barra termina con el corazón normal y su descripción dice «Pulsómetro, conectado»

#### Scenario: Pulsómetro reconectando
- **WHEN** el pulsómetro recordado se ha desconectado y la app intenta recuperarlo
- **THEN** el corazón se ve atenuado con la marca que gira, y su descripción dice «Pulsómetro, reconectando»

#### Scenario: Pulsómetro sin conexión
- **WHEN** el pulsómetro recordado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el corazón se ve tachado, y su descripción dice «Pulsómetro, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square y el pulsómetro están en uso, a 360 dp en vertical
- **THEN** los tres iconos caben en la barra, sin recortarse ni empujar al icono de Ajustes ni a la hora, el wifi y la batería

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo: «SIM» con la transmisión al rodar sin Zwift o en una ruta de Zwift («Lineal · 24 marchas», o «Shimano 105 · automático» o «Shimano 105 · manual»), «ERG ZWIFT» en un entrenamiento ERG de Zwift y «ERG MANUAL» en ERG manual; en ERG SHALL NOT añadir una línea que explique lo que hacen las palancas, porque el modo ya lo dice y el aviso de la acción («+10 W») lo muestra al usar las palancas. El modo SHALL leerse por la palabra, no solo por el color.

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Ruta de Zwift
- **WHEN** se rueda en una ruta de Zwift, fuera de un entrenamiento
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG ZWIFT», sin ninguna línea explicativa de las palancas

#### Scenario: ERG manual
- **WHEN** se entra en ERG manual, con o sin Zwift
- **THEN** la barra superior dice «ERG MANUAL», sin ninguna línea explicativa de las palancas

#### Scenario: Shimano en automático
- **WHEN** se rueda sin Zwift con la Shimano en automático
- **THEN** la barra superior dice «SIM» y «Shimano 105 · automático»

#### Scenario: Cambiar de modo
- **WHEN** la Shimano está en automático y se elige «Manual» en Marchas
- **THEN** al volver a Rodaje, la barra superior dice «Shimano 105 · manual»

## ADDED Requirements

### Requirement: Conexiones de dispositivos en la barra superior
La barra superior SHALL mostrar los dispositivos a los que se conecta la app, uno por icono y en orden fijo: Neo, Square y, si están recordados, pulsómetro y ventilador. El Square lleva el icono de un mando de consola. El estado se ve por la forma: normal si conectado, atenuado con marca girando si se intenta conectar y tachado en color de alerta si no. La barra SHALL NOT mostrar iconos de Zwift ni del mando, ni cambiar de ancho, y es un solo objetivo táctil que abre Dispositivos.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados
- **THEN** se ven los iconos del Neo y del Square, normales, sin marcas, y ninguno de Zwift ni del mando

#### Scenario: Descripción accesible
- **WHEN** el Neo está conectado y el Square sin conexión
- **THEN** la descripción accesible de la barra dice «Conexiones: Neo, conectado; Square, sin conexión»

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** el icono del Square pasa a atenuado con la marca girando, sin que cambie el ancho de la barra, la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** el icono del Square se ve tachado y la descripción accesible dice «Square, sin conexión»

#### Scenario: Estado al tocar
- **WHEN** el icono del Square está atenuado con la marca girando y se toca la barra
- **THEN** se abre Dispositivos, donde la fila del Square dice «Conectando»

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y el Neo se está conectando
- **THEN** la marca del icono del Neo no gira y el icono sigue atenuado con la marca, distinto del tachado

### Requirement: Estado de la plataforma en Rodaje
Rodaje SHALL mostrar, a la izquierda de la fila de Ajustes (con el modo y la transmisión en la fila de debajo), el estado de la plataforma elegida en Plataformas (hoy Zwift), con palabra y forma: «Conectado a Zwift» (punto), «Esperando conexión a Zwift…» (aro que gira, atenuado), «Zwift no visible» o «Sin wifi para Zwift» (aro vacío con borde de alerta). Con el mando ofrecido aparte y sin conectar, SHALL decir «Conectado a Zwift · sin mando». Tocarlo SHALL abrir Plataformas.

#### Scenario: Esperando a Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** Rodaje muestra «Esperando conexión a Zwift…» en la fila de Ajustes, atenuado y con el aro que gira, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Conectado a Zwift
- **WHEN** Zwift está conectado al rodillo y, si el modo lo ofrece aparte, al mando
- **THEN** Rodaje muestra «Conectado a Zwift» con el punto, sin cambiar la marcha ni el área de datos

#### Scenario: Rodillo conectado y mando esperando
- **WHEN** el modo es «Rodillo por wifi (FTMS)» con «Mando de Zwift» activado, Zwift está conectado al rodillo y aún no al mando
- **THEN** Rodaje muestra «Conectado a Zwift · sin mando» y la descripción accesible dice «Plataforma: Zwift, conectado; mando, esperando»

#### Scenario: Zwift Ride
- **WHEN** el modo es «Todo por wifi (modo Zwift Ride)» y Zwift está conectado
- **THEN** Rodaje muestra «Conectado a Zwift», sin «sin mando», porque el mando va dentro del mismo dispositivo

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y Zwift está conectado al rodillo
- **THEN** Rodaje muestra «Conectado a Zwift», sin «sin mando»

#### Scenario: Zwift no visible
- **WHEN** la app muestra Zwift como «No visible» (spec `zwift-bridge`)
- **THEN** Rodaje muestra «Zwift no visible» con el aro vacío y el borde de alerta, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** Rodaje muestra «Sin wifi para Zwift» con el aro vacío y el borde de alerta

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** el estado pasa a «Esperando conexión a Zwift…» y el resto de la pantalla no cambia

#### Scenario: Tocar el estado
- **WHEN** se toca el estado de la plataforma, que mide al menos 48 dp de alto
- **THEN** se abre Plataformas con la barra inferior, y Atrás o «Rodar» vuelven a Rodaje con la sesión en marcha

#### Scenario: Sin cambiar el área de datos
- **WHEN** el estado de la plataforma pasa de «Esperando conexión a Zwift…» a «Conectado a Zwift»
- **THEN** la rejilla de datos mantiene su área y su cifra, y nada se mueve

#### Scenario: Nombre de la plataforma
- **WHEN** la plataforma elegida en Plataformas es otra distinta de Zwift
- **THEN** el estado nombra esa plataforma en lugar de Zwift

## REMOVED Requirements

### Requirement: Conexiones en la barra superior
**Reason**: La barra de conexiones queda solo con los dispositivos a los que se conecta la app; el estado de Zwift y del mando pasa a un estado de la plataforma en palabras bajo la fila del modo.
**Migration**: Esta misma spec, «Conexiones de dispositivos en la barra superior» y «Estado de la plataforma en Rodaje».
