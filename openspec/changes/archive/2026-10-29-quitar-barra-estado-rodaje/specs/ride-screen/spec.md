# Spec Delta

## ADDED Requirements

### Requirement: Barras del sistema visibles en Rodaje
Rodaje SHALL dejar visibles las barras del sistema, que son las que muestran la hora, el wifi y la batería del móvil; la app SHALL NOT repetir esos datos en su propia pantalla. Rodaje SHALL empezar bajo la barra de estado y por encima de la de navegación, sin que ninguna tape sus elementos ni la cámara tape nada.

#### Scenario: Barra de estado a la vista
- **WHEN** se abre Rodaje, en vertical o en horizontal
- **THEN** se ven la barra de estado del sistema con la hora, el wifi y la batería, y la pantalla de rodaje empieza debajo de ella, sin tapar la marcha, las conexiones ni Ajustes

#### Scenario: Cámara en el borde superior
- **WHEN** el móvil tiene la cámara en el borde superior, a la izquierda o en el centro
- **THEN** Rodaje no dibuja nada bajo la cámara y todos sus elementos se ven enteros

#### Scenario: Sin hora, wifi ni batería propios
- **WHEN** se rueda con cualquier estado de wifi y de batería del móvil
- **THEN** Rodaje no muestra la hora, el icono de wifi ni la batería del móvil por su cuenta; el estado de la plataforma sigue diciendo «Sin wifi» cuando falta

#### Scenario: Colores de la barra
- **WHEN** Rodaje se ve en tema rodaje o en tema claro
- **THEN** los iconos de la barra de estado se distinguen del fondo, como en el resto de la app (spec `app-theme`)

### Requirement: Línea superior en horizontal
En horizontal, la línea superior de Rodaje SHALL llevar a la izquierda el modo con la transmisión («SIM · Shimano 105 · automático», «ERG ZWIFT»…), el estado de la plataforma centrado en la línea y, a la derecha, la barra de conexiones y el icono de Ajustes, separados entre sí 12 dp. La zona principal SHALL empezar justo bajo la línea.

#### Scenario: Línea superior en horizontal
- **WHEN** el móvil está en horizontal con la transmisión «Shimano 105 · automático» y las conexiones con Ajustes
- **THEN** se ven en una sola línea el modo con la transmisión a la izquierda, el estado de la plataforma en el centro y, al final, la barra de conexiones y Ajustes, y nada queda en una esquina de la zona principal

#### Scenario: Estado de la plataforma centrado
- **WHEN** el móvil está en horizontal, con cualquier estado de la plataforma y cualquier modo
- **THEN** el estado de la plataforma queda centrado en el ancho de la pantalla, aunque el modo y las conexiones midan distinto

#### Scenario: Más sitio para la marcha y los datos
- **WHEN** el móvil está en horizontal y se pasa de una disposición con el modo en la esquina de la zona principal a esta
- **THEN** la marcha y los datos empiezan justo bajo la línea superior y disponen de la altura que antes ocupaba el modo

#### Scenario: Modo en ERG en horizontal
- **WHEN** el móvil está en horizontal en un entrenamiento ERG de Zwift o en ERG manual
- **THEN** la línea superior dice «ERG ZWIFT» o «ERG MANUAL» a la izquierda del estado de la plataforma, sin ninguna línea explicativa de las palancas

### Requirement: Qué cede en la línea superior en horizontal
Si el modo no cabe en su lado de la línea superior en horizontal, la transmisión SHALL pasar a dos líneas antes de mover el estado de la plataforma del centro; y si el estado no cabe en el centro, SHALL pasar a dos líneas antes de empujar las conexiones y Ajustes, que no ceden.

#### Scenario: Todo largo a la anchura más estrecha
- **WHEN** el móvil está en horizontal a la anchura más estrecha que se soporta, el modo es «SIM · Shimano 105 · automático», el estado dice «Conectado · sin controlador» y hay cuatro iconos de conexión
- **THEN** la transmisión pasa a dos líneas, el estado sigue centrado y se lee entero, y los iconos y Ajustes se ven enteros y sin solaparse

## MODIFIED Requirements

### Requirement: Rodaje como pantalla inicial
La app SHALL abrirse en la pantalla de rodaje, con las barras del sistema visibles, en la orientación que marque el ajuste «Orientación»: con «Según el móvil», en la orientación del móvil, en horizontal o en vertical, y girando con él salvo que el bloqueo de rotación del sistema esté activado; con «Vertical» u «Horizontal», siempre en esa orientación.

#### Scenario: Abrir la app
- **WHEN** se abre la app con el móvil en el soporte en vertical y «Según el móvil» elegido
- **THEN** se ve la pantalla de rodaje en vertical

#### Scenario: Girar el móvil
- **WHEN** se está en Rodaje en la marcha 14 con «Según el móvil» elegido y se gira el móvil a horizontal
- **THEN** la pantalla pasa a la disposición horizontal, sigue en la marcha 14 y no se pierde ninguna conexión

#### Scenario: Bloqueo de rotación
- **WHEN** «Según el móvil» está elegido, el bloqueo de rotación del sistema está activado en vertical y se gira el móvil
- **THEN** la pantalla de rodaje sigue en vertical

#### Scenario: Abrir la app con una orientación fija
- **WHEN** «Horizontal» está elegido y se abre la app con el móvil en vertical
- **THEN** se ve la pantalla de rodaje en la disposición horizontal

#### Scenario: Girar el móvil con una orientación fija
- **WHEN** se está en Rodaje en la marcha 14 con «Vertical» elegido y se gira el móvil a horizontal
- **THEN** la pantalla sigue en la disposición vertical, en la marcha 14, sin parpadeos ni pérdida de conexiones

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo, bajo la barra de estado del sistema, la fila de la plataforma, con el estado de la publicación a la izquierda y la barra de conexiones a la derecha, y la fila del modo, con el modo y la transmisión a la izquierda y el icono de Ajustes a la derecha, y debajo el resto de la pantalla. No SHALL haber ninguna franja propia encima de esas filas.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados, la marcha es la 14 y los datos son los de por defecto
- **THEN** se ven, sin desplazamiento, el estado de la publicación con la barra de los iconos del Neo y del Square a su derecha, «SIM» y «Lineal · 24 marchas» con el icono de Ajustes a su derecha, «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Seis datos en vertical
- **WHEN** el móvil está en vertical con los 6 datos elegidos
- **THEN** los datos se ven en 2 columnas y 3 filas, sin desplazamiento y sin cifras cortadas

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los iconos del Neo y del Square se ven tachados, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexiones en una línea en vertical
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión, Zwift esperando y el mando esperando
- **THEN** el estado de la publicación y los cuatro iconos caben en una sola fila, el estado a la izquierda y los iconos a la derecha, y debajo caben en una sola fila el modo con la transmisión y el icono de Ajustes; al pasar todo a conectado, la marcha y los datos no se mueven

#### Scenario: Estado de la publicación largo junto a las conexiones
- **WHEN** el móvil está en vertical a 360 dp, el estado dice «Conectado · sin controlador» y hay cuatro iconos de conexión
- **THEN** el estado se lee entero, en una o dos líneas, sin tapar ni empujar los iconos, que se ven enteros y juntos

#### Scenario: Móvil sin cámara en el borde
- **WHEN** el móvil está en vertical, con o sin cámara en el borde superior
- **THEN** la fila de la plataforma empieza justo bajo la barra de estado del sistema, sin una franja vacía encima, la fila del modo con Ajustes queda debajo sin solaparse, y todo se ve sin desplazamiento

#### Scenario: Transmisión larga junto a Ajustes
- **WHEN** el móvil está en vertical a 360 dp, la transmisión es «Shimano 105 · automático» y el modo es SIM, o se está en ERG de Zwift
- **THEN** el modo con la transmisión se lee entero, en una o dos líneas, sin cortarse ni tapar el icono de Ajustes

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Cinco datos en vertical
- **WHEN** el móvil está en vertical con 5 datos elegidos
- **THEN** los cuatro primeros se ven en 2 columnas y 2 filas y el quinto, debajo, centrado en el ancho de la rejilla

#### Scenario: Los indicadores no mueven la marcha
- **WHEN** el móvil está en vertical y un enlace de la barra pasa de esperando a conectado o a sin conexión, o el estado de la publicación cambia de palabra
- **THEN** las filas superiores, la marcha y los datos no se mueven ni cambian de tamaño

### Requirement: Ajustes desde Rodaje
Rodaje SHALL mostrar un botón con el icono de Ajustes, el mismo de la pestaña, con objetivo táctil de al menos 48 dp y descripción accesible «Ajustes», separado de la barra de conexiones para que no se toque uno por otro. En horizontal SHALL ir en la línea superior, a la derecha de la barra; en vertical, al final de la fila del modo, bajo la fila de la barra de conexiones. Al tocarlo SHALL abrirse Ajustes como pestaña, sin terminar la sesión ni cambiar la marcha.

#### Scenario: Abrir Ajustes en horizontal
- **WHEN** el móvil está en horizontal, se rueda en la marcha 14 y se toca el icono de Ajustes
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada, sin botón de volver, y al tocar «Rodar» se vuelve a Rodaje en la marcha 14

#### Scenario: Abrir Ajustes en vertical
- **WHEN** el móvil está en vertical y se toca el icono de Ajustes, al final de la fila del modo
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada

#### Scenario: Atrás desde Ajustes abierto con el icono
- **WHEN** se abre Ajustes con el icono de Rodaje y se pulsa Atrás
- **THEN** se vuelve a Rodaje y la sesión sigue en marcha

#### Scenario: Línea de chips en vertical con el icono
- **WHEN** el móvil está en vertical a 360 dp, con el Neo conectando, el Square sin conexión y Zwift esperando
- **THEN** la barra de conexiones queda en la fila de la plataforma y el icono de Ajustes en la fila de debajo, al final de la del modo, sin que sus objetivos táctiles se solapen, y al pasar todo a conectado ni el icono, ni la marcha ni los datos se mueven

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados
- **THEN** el icono de Ajustes se ve en su sitio y abre Ajustes como siempre, y el pie sigue diciendo «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexión perdida al abrir Ajustes
- **WHEN** se abre Ajustes con el icono y, con Ajustes abierto, se pierde la conexión con el Square
- **THEN** la app intenta reconectarlo como desde Rodaje y, al volver a Rodaje, el icono del Square muestra su estado y la marcha es la misma

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la parte superior de Rodaje
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
- **THEN** los cuatro iconos caben en la barra, sin recortarse ni empujar al estado de la publicación

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
- **THEN** los tres iconos caben en la barra, sin recortarse ni empujar al estado de la publicación

## REMOVED Requirements

### Requirement: Hora, wifi y batería del móvil en Rodaje
**Reason**: Rodaje deja de ocultar las barras del sistema, que ya muestran la hora, el wifi y la batería; repetirlos en la app era código y pantalla duplicados.
**Migration**: Ver «Barras del sistema visibles en Rodaje». El aviso de wifi para Zwift sigue en «Estado de la publicación en Rodaje» («Sin wifi»).

### Requirement: Colocación vertical de los indicadores del móvil
**Reason**: Sin indicadores propios no hay grupo que colocar ni franja superior que esquive la cámara; de eso se ocupa el sistema.
**Migration**: Ver «Disposición en vertical» y «Barras del sistema visibles en Rodaje».

### Requirement: Colocación horizontal de los indicadores del móvil
**Reason**: Sin indicadores propios no hay conjunto que centrar en la línea superior.
**Migration**: Ver «Línea superior en horizontal».

### Requirement: Indicadores del móvil solo informativos
**Reason**: Los indicadores propios desaparecen, y con ellos la regla de que no fueran táctiles; Rodaje ya no oculta las barras del sistema.
**Migration**: La pantalla siempre encendida sigue en «Pantalla siempre encendida»; las barras del sistema, en «Barras del sistema visibles en Rodaje».
