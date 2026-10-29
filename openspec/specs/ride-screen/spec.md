# ride-screen Specification

## Purpose
Pantalla de rodaje de la app, legible a 1–2 m y manejable sin tocarla: la marcha puesta, los datos del Neo, el modo, las conexiones y los avisos de cada acción, sin nada del modelo de resistencia.

## Requirements

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
- **THEN** se ven, sin desplazamiento, el estado de la publicación con la barra de los iconos del Neo y del Square a su derecha, «LIBRE» y «Lineal · 24 marchas» con el icono de Ajustes a su derecha, «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

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

### Requirement: Cuerpo de la pantalla en vertical
Bajo el modo y la transmisión, la pantalla de rodaje en vertical SHALL apilar la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas en su área fija y, al pie, el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

#### Scenario: Todo visible en vertical
- **WHEN** se abre el rodaje en vertical
- **THEN** la marcha, los datos y el pie se ven sin desplazar la pantalla

### Requirement: Marcha en grande
Con la transmisión lineal, la pantalla SHALL mostrar el número de la marcha puesta en grande, con «Marcha · de 24» al lado y una escalera con un segmento por marcha (la ligera a la izquierda), la actual resaltada y la marca de la marcha de referencia.

#### Scenario: Marcha 14
- **WHEN** la marcha es la 14
- **THEN** se lee «14» en grande, «de 24», y la escalera tiene resaltado el segmento 14 y marcado el 12

### Requirement: Animación del cambio
Al cambiar de marcha, la cifra nueva SHALL entrar desplazándose en la dirección del cambio (hacia arriba si es más dura) en unos 120 ms, y SHALL aparecer sin animación si el sistema tiene activado reducir el movimiento.

#### Scenario: Movimiento reducido
- **WHEN** el móvil tiene desactivadas las animaciones y se cambia de marcha
- **THEN** la cifra nueva aparece directamente

### Requirement: Aviso de tope en la marcha
Al llegar a un tope, la cifra de la marcha SHALL atenuarse y SHALL aparecer debajo la etiqueta del tope durante 1,5 s, sin aviso de acción: «Tope · marcha más dura» o «Tope · marcha más ligera» en los extremos, y con la Shimano también «Tope · plato 52», «Tope · plato 36», «Tope · piñón 11» o «Tope · piñón 36».

#### Scenario: Tope en la más dura
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, y después vuelve a verse normal

#### Scenario: Tope de plato
- **WHEN** con la Shimano en la 52×17 se pulsa la palanca I1
- **THEN** «52×17» se ve atenuado con «Tope · plato 52» durante 1,5 s

### Requirement: Potencia y cadencia
La potencia SHALL mostrarse como la media de los últimos 3 s de lo que mide el Neo, en W, y la cadencia como la mide el Neo, en rpm, las dos actualizadas mientras se pedalea; sin el Neo conectado, las dos SHALL mostrarse como «—».

#### Scenario: Pedaleando
- **WHEN** se pedalea con el Neo conectado
- **THEN** la potencia y la cadencia cambian con el pedaleo

#### Scenario: Media de 3 s
- **WHEN** el Neo mide 200 W durante unos segundos y después 300 W
- **THEN** la potencia pasa de 200 W a 300 W en unos 3 s, sin saltar de golpe

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 3 s la cadencia baja a 0 y la potencia a 0

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo
- **THEN** la potencia y la cadencia muestran «—» y la marcha sigue visible

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo: «LIBRE» con la transmisión al rodar sin una app de entrenamiento que envíe pendiente, «SIM» con la transmisión en una ruta de la app de entrenamiento (fuera de un entrenamiento), siempre con la transmisión («Lineal · 24 marchas», o «Shimano 105 · automático» o «Shimano 105 · manual»), «ERG APP» en un entrenamiento ERG de Zwift y «ERG MANUAL» en ERG manual; en ERG SHALL NOT añadir una línea que explique lo que hacen las palancas, porque el modo ya lo dice y el aviso de la acción («+10 W») lo muestra al usar las palancas. En «LIBRE» la pendiente del terreno es 0 % y solo cambia la marcha. El modo SHALL leerse por la palabra, no solo por el color, y SHALL seguir al modo de control: al irse la app de entrenamiento, «SIM» se mantiene 30 s, lo mismo que la pendiente, y después pasa a «LIBRE».

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «LIBRE» y «Lineal · 24 marchas»

#### Scenario: Ruta de Zwift
- **WHEN** se rueda en una ruta de Zwift, fuera de un entrenamiento
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

#### Scenario: Zwift se va
- **WHEN** se cierra Zwift rodando en una ruta
- **THEN** la barra superior sigue diciendo «SIM» durante 30 s y después «LIBRE»

#### Scenario: Zwift conectado sin pendiente todavía
- **WHEN** Zwift está conectado pero aún no ha enviado ninguna pendiente
- **THEN** la barra superior dice «LIBRE» hasta que llega la primera

#### Scenario: Entrenamiento de la app
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG APP», sin ninguna línea explicativa de las palancas

#### Scenario: ERG manual
- **WHEN** se entra en ERG manual, con o sin Zwift
- **THEN** la barra superior dice «ERG MANUAL», sin ninguna línea explicativa de las palancas

#### Scenario: Shimano en automático
- **WHEN** se rueda sin Zwift con la Shimano en automático
- **THEN** la barra superior dice «LIBRE» y «Shimano 105 · automático»

#### Scenario: Cambiar de modo
- **WHEN** la Shimano está en automático y se elige «Manual» en Marchas
- **THEN** al volver a Rodaje, la barra superior dice «Shimano 105 · manual»

### Requirement: Aviso de la acción
Tras cada cambio de marcha, el pie SHALL mostrar durante 1,5 s la acción, sin el botón ni el gesto: «+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera» o «Marcha de referencia»; con la Shimano, «Plato 52» o «Plato 36» cuando la acción cambia el plato, también si lo cambia solo el modo automático; al fijar la referencia, «Referencia en la N» con la marcha fijada, o «Referencia en 36×17» con la Shimano. Una acción nueva SHALL sustituir a la anterior.

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

#### Scenario: Entrar en ERG manual
- **WHEN** la última potencia de ERG manual es 200 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «ERG manual · 200 W» durante 1,5 s

#### Scenario: Volver a SIM
- **WHEN** en ERG manual sin Zwift se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «Modo Libre» durante 1,5 s

#### Scenario: Volver a SIM en una ruta de Zwift
- **WHEN** en ERG manual en una ruta de Zwift se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «Modo SIM» durante 1,5 s

#### Scenario: Ajuste en ERG manual
- **WHEN** en ERG manual a 200 W se pulsa la palanca I1
- **THEN** el pie dice «−10 W» durante 1,5 s

#### Scenario: Volver a la potencia de entrada
- **WHEN** se entró en ERG manual a 200 W, se ha subido a 240 W y se pulsa □
- **THEN** el pie dice «Vuelta a 200 W» durante 1,5 s

#### Scenario: Alternar en un entrenamiento de Zwift
- **WHEN** Zwift pide 230 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el pie dice «Entrenamiento de Zwift» durante 1,5 s y el modo sigue siendo «ERG APP»

#### Scenario: Fijar la referencia
- **WHEN** la marcha es la 15 y se mantiene □ hasta el umbral de pulsación larga
- **THEN** el pie dice «Referencia en la 15» durante 1,5 s y la marca de referencia de la escalera pasa a la 15

#### Scenario: Repetición
- **WHEN** se mantiene la palanca D1 y la marcha sube tres veces por la repetición
- **THEN** el pie dice «+1 marcha» con cada cambio, sustituyendo al anterior

#### Scenario: Plato que cambia solo
- **WHEN** con la Shimano en automático en la 36×15 se pulsa la palanca D2
- **THEN** la marcha pasa a la 52×19 y el pie dice «Plato 52»

#### Scenario: Piñón sin cambiar de plato
- **WHEN** con la Shimano en la 52×17 se pulsa la palanca D1
- **THEN** el pie dice «−1 marcha»

#### Scenario: Referencia con la Shimano
- **WHEN** con la Shimano en la 36×17 se mantiene □ hasta el umbral de pulsación larga
- **THEN** el pie dice «Referencia en 36×17» durante 1,5 s

### Requirement: Aviso de la acción en ERG
En ERG de Zwift o en ERG manual, el pie SHALL mostrar tras cada ajuste de la potencia la diferencia real («+10 W», «−10 W», «+30 W», «−30 W»…); en ERG de Zwift, al quitar el ajuste, «Potencia de Zwift»; en ERG manual, al volver a la potencia de entrada, «Vuelta a 200 W» con esa potencia; al alternar, «ERG manual · 200 W» con la potencia o, al volver a la simulación, «Modo Libre» sin Zwift o «Modo SIM» en una ruta de Zwift. Una acción nueva SHALL sustituir a la anterior.

#### Scenario: Ajuste de potencia en ERG
- **WHEN** se pulsa +10 W en ERG manual
- **THEN** el pie muestra «+10 W»

### Requirement: Sin datos del modelo de resistencia
La pantalla de rodaje SHALL NOT mostrar la pendiente base, el ajuste por marcha ni lo que se envía al Neo tras aplicar la marcha (pendiente o potencia efectiva), ni el nombre del modelo. SHALL poder mostrar la pendiente de la simulación, la del terreno, como el dato «Pendiente» (ver «Pendiente en Rodaje»).

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17 sin Zwift y «Pendiente» no está elegido
- **THEN** en la pantalla de rodaje no aparece «+5 %» ni ninguna pendiente

#### Scenario: Marcha dura con el dato elegido
- **WHEN** la marcha es la 17 sin Zwift y «Pendiente» está elegido
- **THEN** el dato dice «0,0 %» y no aparece «+5 %»

#### Scenario: Ruta de Zwift con el dato elegido
- **WHEN** Zwift envía una pendiente de 4,0 %, la marcha da un ajuste de +2,0 % y «Pendiente» está elegido
- **THEN** el dato dice «+4,0 %» y no aparece «+6,0 %»

### Requirement: Pantalla siempre encendida
Mientras se ve la pantalla de rodaje, el móvil SHALL NOT apagar la pantalla por inactividad.

#### Scenario: Rodaje largo sin tocar el móvil
- **WHEN** se rueda 10 minutos sin tocar el móvil con la pantalla de rodaje delante
- **THEN** la pantalla sigue encendida

### Requirement: Potencia objetivo en ERG de Zwift
En un entrenamiento ERG de Zwift, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia objetivo que se envía al Neo en grande, con la potencia que pide Zwift debajo y, si las palancas la han ajustado, el ajuste; la marcha SHALL NOT mostrarse mientras dure el ERG. La zona SHALL ir centrada en horizontal en su columna.

#### Scenario: Bloque sin ajuste
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** se lee «230 W» en grande con «230 W de Zwift» debajo, sin ajuste, y no se ve la marcha ni su escalera

#### Scenario: Con ajuste de las palancas
- **WHEN** Zwift pide 230 W y se ha pulsado la palanca D1 una vez
- **THEN** se lee «240 W» en grande, «230 W de Zwift» y «+10 W con las palancas»

#### Scenario: ERG en vertical
- **WHEN** el móvil está en vertical y Zwift pide 230 W
- **THEN** la potencia objetivo ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift vuelve a enviar el terreno tras un entrenamiento con la marcha 14
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG
- **WHEN** Zwift pide 230 W y se pierde la conexión con el Neo
- **THEN** la potencia objetivo sigue visible, la potencia y la cadencia muestran «—» y el icono del Neo pasa a atenuado con la marca girando

#### Scenario: Tope de potencia
- **WHEN** la potencia objetivo es 50 W y se pulsa la palanca I1
- **THEN** «50 W» no cambia y el pie dice «Tope · 50 W» durante 1,5 s

### Requirement: Marcha plato × piñón con la Shimano
Con la Shimano, la pantalla de rodaje SHALL mostrar en el lugar de la marcha el plato y el piñón en grande («36×17», con el × más pequeño y atenuado), el indicador de los dos platos con el puesto relleno y el cassette con una barra por piñón, del 36 a la izquierda al 11 a la derecha, con el puesto resaltado y numerado, en horizontal y en vertical. SHALL NOT mostrar la escalera ni la relación.

#### Scenario: Marcha 52×17
- **WHEN** la transmisión es la Shimano y la marcha la 52×17
- **THEN** se lee «52×17» en grande, el plato 52 está relleno y el 36 solo con borde, y la barra del 17 está resaltada con su número

#### Scenario: Shimano en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×21
- **THEN** se ven, sin desplazamiento, «36×21», los platos, el cassette y la potencia y la cadencia en una fila

#### Scenario: Cambio de plato
- **WHEN** la marcha pasa de la 36×15 a la 52×19
- **THEN** la cifra nueva entra desplazándose hacia arriba y el plato relleno pasa al 52, sin que se mueva el resto de la pantalla

### Requirement: Potencia objetivo en ERG manual
En ERG manual, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia fijada en grande, sin línea de Zwift ni de ajuste, centrada en horizontal en su columna; la marcha SHALL NOT mostrarse mientras dure el ERG manual.

#### Scenario: ERG manual a 200 W
- **WHEN** se entra en ERG manual a 200 W
- **THEN** se lee «200 W» en grande, sin «de Zwift», y no se ve la marcha ni su escalera

#### Scenario: Ajuste
- **WHEN** en ERG manual a 200 W se pulsa la palanca D1
- **THEN** la cifra pasa a «210 W»

#### Scenario: ERG manual en vertical
- **WHEN** el móvil está en vertical y se está en ERG manual
- **THEN** la potencia fijada ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Vuelta a SIM
- **WHEN** en ERG manual sin Zwift y con la marcha 14 se vuelve a la simulación
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «LIBRE»

#### Scenario: Neo desconectado en ERG manual
- **WHEN** en ERG manual a 200 W se pierde la conexión con el Neo
- **THEN** «200 W» sigue visible, la potencia y la cadencia muestran «—» y el icono del Neo pasa a atenuado con la marca girando

#### Scenario: Tope de potencia
- **WHEN** en ERG manual a 1000 W se pulsa la palanca D1
- **THEN** «1000 W» no cambia y el pie dice «Tope · 1000 W» durante 1,5 s

### Requirement: Datos elegidos en Rodaje
La pantalla de rodaje SHALL mostrar los datos elegidos en «Datos en pantalla», de 1 a 6, en el orden elegido, en horizontal y en vertical. Por defecto SHALL mostrar potencia y cadencia. Al cambiar los datos en Ajustes, Rodaje SHALL mostrarlos al volver, sin terminar la sesión.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez
- **THEN** Rodaje muestra la potencia y la cadencia, en ese orden

#### Scenario: Un solo dato
- **WHEN** solo está elegida la cadencia
- **THEN** Rodaje muestra solo la cadencia, con la cifra más grande que cabe

#### Scenario: Orden elegido
- **WHEN** se eligen tiempo, potencia y W/kg, en ese orden, y se vuelve a Rodaje
- **THEN** Rodaje muestra los tres en ese orden, con el mismo tamaño de cifra, y la marcha no ha cambiado

#### Scenario: En ERG
- **WHEN** se está en ERG con cuatro datos elegidos
- **THEN** la potencia objetivo ocupa el lugar de la marcha y los cuatro datos siguen visibles en la rejilla

### Requirement: Datos de la sesión
Tiempo, potencia media y potencia máxima SHALL contar desde que empieza la sesión y solo mientras se pedalea (cadencia mayor que 0). W/kg SHALL ser la potencia de 3 s dividida por el peso del ciclista de Ajustes, con un decimal. Sin el Neo conectado, W/kg SHALL mostrarse como «—», y tiempo, potencia media y máxima SHALL conservar su valor sin avanzar.

#### Scenario: Tiempo parado
- **WHEN** se pedalea 10 min, se para 2 min y se vuelve a pedalear 5 min
- **THEN** el tiempo dice «15:00»

#### Scenario: Media sin las paradas
- **WHEN** se pedalea 10 min a 200 W, se para 5 min y se pedalea otros 10 min a 200 W
- **THEN** la potencia media dice 200 W

#### Scenario: Máxima
- **WHEN** durante la sesión la potencia de 3 s llega a 650 W y después baja
- **THEN** la potencia máxima sigue diciendo 650 W

#### Scenario: W/kg
- **WHEN** el peso del ciclista es 75 kg y la potencia es 247 W
- **THEN** W/kg dice «3,3»

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo tras 20 min pedaleando
- **THEN** W/kg muestra «—» y el tiempo sigue en 20 min, sin avanzar hasta que el Neo vuelve y se pedalea

#### Scenario: Sesión nueva
- **WHEN** se termina la sesión y se vuelve a abrir la app
- **THEN** el tiempo, la potencia media y la máxima empiezan desde cero

### Requirement: Botones − y + en Rodaje
Rodaje SHALL mostrar un botón + y un botón −, de al menos 72 dp, que hacen lo mismo que las acciones «+1 marcha» y «−1 marcha» del Square, con sus topes, avisos y vibración, y en ERG ajustan ±10 W, siempre que el Square no esté conectado en ese momento (sin conexión, buscando o reconectando, o sin Square configurado) y, con el Square conectado, solo si «Botones − y + en pantalla» está activado (desactivado por defecto). Con el Square conectado y el ajuste desactivado, SHALL NOT mostrarlos. Si el Square se conecta o se desconecta rodando, los botones SHALL ocultarse o mostrarse al momento, sin cambiar la marcha ni cortar la sesión.

#### Scenario: Por defecto
- **WHEN** el Square está conectado y el ajuste está desactivado
- **THEN** Rodaje no muestra los botones − y +

#### Scenario: Sin Square
- **WHEN** se abre la app por primera vez, sin Square conectado
- **THEN** Rodaje muestra los botones − y +, aunque el ajuste esté desactivado

#### Scenario: Se cae el Square
- **WHEN** rodando con el Square conectado y el ajuste desactivado, el Square pierde la conexión
- **THEN** aparecen los botones − y +, la marcha no cambia y se puede cambiar de marcha con ellos

#### Scenario: Vuelve el Square
- **WHEN** con el Square desconectado y el ajuste desactivado, el Square vuelve a conectarse
- **THEN** los botones − y + desaparecen y la marcha no cambia

#### Scenario: Square conectado con el ajuste activado
- **WHEN** el Square está conectado y el ajuste está activado
- **THEN** Rodaje muestra los botones − y +

#### Scenario: Subir con el botón
- **WHEN** los botones están visibles, la marcha es la 14 y se toca +
- **THEN** la marcha pasa a la 15, el Neo recibe la resistencia nueva y el pie dice «+1 marcha»

#### Scenario: Tope con el botón
- **WHEN** los botones están visibles, la marcha es la 1 y se toca −
- **THEN** la marcha sigue en la 1 y se ve «Tope · marcha más ligera»

#### Scenario: Con la Shimano
- **WHEN** los botones están visibles, la Shimano está en automático en la 36×15 y se toca +
- **THEN** la marcha pasa a la 52×19, como con la palanca D2

#### Scenario: En ERG
- **WHEN** los botones están visibles, se está en ERG manual a 200 W y se toca +
- **THEN** la potencia pasa a 210 W y el pie dice «+10 W»

#### Scenario: Sin Neo
- **WHEN** los botones están visibles, el Neo no está conectado y se toca +
- **THEN** la marcha sube y el Neo la recibe al conectarse

#### Scenario: Seis datos con botones
- **WHEN** los botones están visibles con 6 datos elegidos, en horizontal o en vertical
- **THEN** los botones y los 6 datos se ven a la vez, sin desplazamiento, con la misma cifra que sin botones

### Requirement: Aviso de la acción desactivable
Con «Mostrar la acción» desactivado, el pie de Rodaje SHALL NOT mostrar el aviso de la acción. Los topes de la marcha y de la potencia y la ayuda para conectar SHALL seguir viéndose. Activado (por defecto), el aviso SHALL mostrarse como hasta ahora.

#### Scenario: Desactivado
- **WHEN** «Mostrar la acción» está desactivado y se pulsa la palanca D1 en la 14
- **THEN** la marcha pasa a la 15 y el pie no dice «+1 marcha»

#### Scenario: Tope con el aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura»

#### Scenario: Tope de potencia con el aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado, se está en ERG manual a 1000 W y se pulsa la palanca D1
- **THEN** el pie dice «Tope · 1000 W»

#### Scenario: Ayuda para conectar
- **WHEN** «Mostrar la acción» está desactivado y el Neo se está reconectando
- **THEN** el pie dice «Pedalea para despertar el Neo»

### Requirement: Dispositivos desde la barra de conexiones
Al tocar la barra de conexiones, con un objetivo táctil de al menos 48 dp de alto, la app SHALL abrir Dispositivos. Mientras la app intenta conectar algún dispositivo, el pie SHALL decir cómo despertarlo; si falta alguno y no lo intenta, SHALL decir «Toca las conexiones para conectar el Neo y el Square». El estado de Zwift y del mando SHALL NOT cambiar lo que dice el pie.

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app sin dispositivos configurados
- **THEN** los iconos del Neo y del Square se ven tachados y la descripción accesible dice «sin conexión» para los dos, la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Neo dormido al abrir
- **WHEN** se abre la app con el Neo y el Square configurados, el Square se conecta y el Neo está dormido
- **THEN** el icono del Neo se ve atenuado con la marca girando y la descripción accesible dice «Neo, conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo» hasta que se conecta

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
- **WHEN** se toca cualquier icono de la barra de conexiones, o el hueco entre dos iconos
- **THEN** se abre Dispositivos con la barra inferior, y «Rodar» o Atrás vuelven a Rodaje

#### Scenario: Tocar el chip de Zwift
- **WHEN** el Neo y el Square están conectados, Zwift no, y se toca la barra
- **THEN** se abre Dispositivos, y el pie de Rodaje no dice nada sobre Zwift

### Requirement: Área de datos fija
La rejilla de datos de Rodaje SHALL ocupar un área fija: el 40 % del alto útil de la pantalla en vertical y el 40 % del ancho útil en horizontal. El área SHALL NOT cambiar con la transmisión, el modo (SIM o ERG), el estado de las conexiones ni el aviso del pie; solo los botones − y + pueden reducirla, sin bajar la cifra de los datos. La zona de la marcha SHALL ocupar el resto y su cifra SHALL reducirse lo necesario para caber, sin pasar de su tamaño normal.

#### Scenario: Misma área en lineal y con la Shimano
- **WHEN** el móvil está en vertical con 4 datos y se cambia de la transmisión lineal a la Shimano
- **THEN** la rejilla de datos tiene el mismo alto, la misma posición y la misma cifra, y solo cambia la zona de la marcha

#### Scenario: Misma área en ERG
- **WHEN** se entra en ERG manual con 4 datos elegidos
- **THEN** la potencia objetivo ocupa la zona de la marcha y la rejilla de datos mantiene su área y su cifra

#### Scenario: Área en horizontal
- **WHEN** el móvil está en horizontal, sin botones, con 2 datos
- **THEN** la columna de datos ocupa el 40 % del ancho útil y la marcha, con su escalera o su cassette, el resto

#### Scenario: Marcha que no cabe
- **WHEN** el móvil está en horizontal con los botones activados, la Shimano en la 52×17 y 6 datos
- **THEN** «52×17» y los platos se ven completos, más pequeños que sin botones, sin cortarse ni invadir la columna de datos

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados y los datos muestran «—»
- **THEN** la rejilla tiene la misma área y la misma cifra que con los dispositivos conectados

### Requirement: Espacio de los botones − y +
Al mostrarse los botones − y + (por el ajuste o por no estar el Square conectado), el espacio que ocupan SHALL salir primero de lo que le sobra a la rejilla de datos sin bajar su cifra (alto en vertical, ancho en horizontal) y, lo que falte, de la zona de la marcha. La cifra de los datos SHALL ser la misma con y sin botones. Si los botones aparecen rodando porque el Square se desconecta, la marcha y la rejilla SHALL poder reducirse para hacerles sitio, sin ocultar ningún dato salvo lo que dice «Datos con botones en pantalla baja».

#### Scenario: Dos datos en vertical
- **WHEN** el móvil está en vertical con 2 datos y se activan los botones
- **THEN** los datos se ven con la misma cifra que sin botones, la rejilla se acorta para dejar sitio a los botones y la marcha no cambia de tamaño

#### Scenario: Seis datos en horizontal
- **WHEN** el móvil está en horizontal con 6 datos y se activan los botones
- **THEN** la columna de datos y su cifra no cambian y la zona de la marcha se estrecha para dejar sitio a la columna de botones, con la marcha más pequeña si no cabe

#### Scenario: Aparecen al caerse el Square
- **WHEN** en vertical con 4 datos y el ajuste desactivado, el Square pierde la conexión
- **THEN** aparecen los botones, la marcha y la rejilla se reducen lo necesario y los 4 datos y la marcha siguen visibles

#### Scenario: Desactivar los botones
- **WHEN** con el Square conectado, los botones activados y 5 datos se desactivan en Ajustes y se vuelve a Rodaje
- **THEN** la rejilla recupera el 40 % y la cifra de los datos es la misma que con los botones

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

### Requirement: Sin aviso para las funciones de Zwift
Las funciones de Zwift que se envían por el mando SHALL NOT mostrar aviso de la acción en el pie de Rodaje ni sustituir el aviso que haya.

#### Scenario: Ride On
- **WHEN** se pulsa la palanca D1 y, antes de 1,5 s, Z (Ride On)
- **THEN** el pie sigue diciendo «+1 marcha» hasta cumplir su tiempo, sin mencionar Ride On

### Requirement: Velocidad y distancia en Rodaje
Rodaje SHALL poder mostrar «Velocidad», en km/h con un decimal, y «Distancia», en km con dos decimales, como datos de la rejilla cuando estén elegidos en «Datos en pantalla», con el mismo tamaño de cifra que los demás. La velocidad SHALL tener valor solo con el modelo físico y el Neo conectado y, si no, mostrarse como «—»; la distancia SHALL conservar su valor cuando no avanza.

#### Scenario: Con el físico
- **WHEN** se pedalea con el modelo físico, a unos 24 km/h, con velocidad y distancia elegidas
- **THEN** Rodaje muestra «24,0» km/h y una distancia que crece mientras se pedalea

#### Scenario: Con otro modelo
- **WHEN** el modelo es «Simple» y la velocidad está elegida
- **THEN** la velocidad muestra «—» y la distancia conserva su valor sin avanzar

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo con el físico y 3,20 km recorridos
- **THEN** la velocidad muestra «—» y la distancia sigue en «3,20»

#### Scenario: Parado
- **WHEN** se deja de pedalear con el físico
- **THEN** la velocidad muestra «0,0» y la distancia no avanza

#### Scenario: Seis datos con los nuevos
- **WHEN** están elegidos potencia, cadencia, tiempo, W/kg, velocidad y distancia
- **THEN** Rodaje los muestra todos sin desplazamiento ni cambio de tamaño de la cifra

#### Scenario: Sin mostrar el modelo
- **WHEN** se rueda con cualquier modelo
- **THEN** Rodaje no muestra el nombre del modelo ni la potencia que calcula, y estos solo se ven en Diagnóstico

#### Scenario: Sesión nueva
- **WHEN** se termina la sesión y se vuelve a abrir la app
- **THEN** la distancia empieza en cero

### Requirement: Ventilador en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás (el pulsómetro incluido), un icono del ventilador solo si hay uno configurado, con los estados de los demás enlaces (conectado, esperando y sin conexión) y su descripción accesible en la de la barra («Ventilador, conectado»). El icono SHALL NOT mostrar el modo ni la velocidad, y la barra SHALL mantener su objetivo táctil de 48 dp y su ancho independiente del estado.

#### Scenario: Sin ventilador configurado
- **WHEN** no hay ventilador configurado
- **THEN** la barra muestra los enlaces de siempre y ningún ventilador

#### Scenario: Ventilador conectado
- **WHEN** hay ventilador configurado y está conectado
- **THEN** la barra termina con el icono del ventilador normal y su descripción dice «Ventilador, conectado»

#### Scenario: Ventilador reconectando
- **WHEN** el ventilador configurado se ha desconectado y la app intenta recuperarlo
- **THEN** el icono se ve atenuado con la marca que gira, y su descripción dice «Ventilador, reconectando»

#### Scenario: Ventilador sin conexión
- **WHEN** el ventilador configurado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el icono se ve tachado, y su descripción dice «Ventilador, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square, el pulsómetro y el ventilador están en uso, a 360 dp en vertical
- **THEN** los cuatro iconos caben en la barra, sin recortarse ni empujar al estado de la publicación

### Requirement: Aviso de las acciones del ventilador
Tras una acción del ventilador, el pie SHALL mostrar 1,5 s su resultado: «Ventilador: 50 %» al cambiar la velocidad (también en un extremo), «Ventilador: manual · 40 %» o «Ventilador: automático» al alternar, «Ventilador en automático» al pedir velocidad en automático y «Sin ventilador» si no hay conexión. Una acción nueva SHALL sustituir a la anterior, el aviso SHALL respetar «Mostrar la acción» y Rodaje SHALL NOT mostrar el modo ni la velocidad del ventilador en otro sitio.

#### Scenario: Subir la velocidad
- **WHEN** el ventilador está en manual al 40 % y se pulsa «Ventilador: + velocidad»
- **THEN** el pie dice «Ventilador: 50 %» durante 1,5 s

#### Scenario: Acciones seguidas
- **WHEN** se pulsa «Ventilador: + velocidad» y, antes de 1,5 s, «Ventilador: − velocidad»
- **THEN** el aviso del segundo sustituye al primero, sin apilarse

#### Scenario: Alternar
- **WHEN** el ventilador está en manual al 40 % y se pulsa «Ventilador: manual ⇄ automático»
- **THEN** el pie dice «Ventilador: automático»

#### Scenario: Velocidad en automático
- **WHEN** el ventilador está en automático y se pulsa «Ventilador: − velocidad»
- **THEN** el pie dice «Ventilador en automático»

#### Scenario: Sin ventilador
- **WHEN** el ventilador no está conectado y se pulsa «Ventilador: + velocidad»
- **THEN** el pie dice «Sin ventilador»

#### Scenario: Aviso desactivado
- **WHEN** «Mostrar la acción» está desactivada y se pulsa «Ventilador: + velocidad»
- **THEN** el ventilador cambia y el pie no muestra ningún aviso

#### Scenario: Sin velocidad en la rejilla
- **WHEN** se rueda con el ventilador en manual al 70 %
- **THEN** Rodaje no muestra 70 % en ningún sitio, y el icono del ventilador no cambia por la velocidad

### Requirement: Pulsómetro en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás, un icono del pulsómetro (un corazón) solo si hay un pulsómetro configurado, con los mismos estados y formas que los demás enlaces: conectado, esperando (reconectando o buscando) y sin conexión. Su descripción accesible SHALL entrar en la de la barra («Pulsómetro, conectado»). El icono SHALL NOT mostrar el pulso, y la barra SHALL mantener su único objetivo táctil de al menos 48 dp y su ancho independiente del estado.

#### Scenario: Sin pulsómetro configurado
- **WHEN** no hay pulsómetro configurado
- **THEN** la barra muestra los enlaces de siempre y ningún corazón

#### Scenario: Pulsómetro conectado
- **WHEN** hay pulsómetro configurado y está conectado
- **THEN** la barra termina con el corazón normal y su descripción dice «Pulsómetro, conectado»

#### Scenario: Pulsómetro reconectando
- **WHEN** el pulsómetro configurado se ha desconectado y la app intenta recuperarlo
- **THEN** el corazón se ve atenuado con la marca que gira, y su descripción dice «Pulsómetro, reconectando»

#### Scenario: Pulsómetro sin conexión
- **WHEN** el pulsómetro configurado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el corazón se ve tachado, y su descripción dice «Pulsómetro, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square y el pulsómetro están en uso, a 360 dp en vertical
- **THEN** los tres iconos caben en la barra, sin recortarse ni empujar al estado de la publicación

### Requirement: Sin pulso en los datos de Rodaje
Rodaje SHALL NOT mostrar los bpm ni en la rejilla de datos ni en ningún otro lugar, ni «Datos en pantalla» SHALL ofrecer el pulso. El pulso solo se ve en Diagnóstico.

#### Scenario: Rodando con la banda
- **WHEN** se rueda con el pulsómetro conectado dando 140 bpm
- **THEN** Rodaje no muestra 140 en ningún sitio, y el corazón de la barra no cambia por los latidos

### Requirement: Aviso de giro
Con un Square configurado, Rodaje SHALL indicar el giro de Zwift con una flecha a cada lado de la marcha: la izquierda mientras se mantiene el giro a la izquierda y la derecha mientras se mantiene el giro a la derecha, con el botón que tenga asignado el giro. Un giro breve SHALL verse al menos 0,5 s. Con el freno de Zwift de la misma palanca pulsado, SHALL NOT mostrarse su giro, como en Zwift.

#### Scenario: Girar a la izquierda
- **WHEN** se mantiene X (giro a la izquierda por defecto)
- **THEN** se ve la flecha de la izquierda a la izquierda de la marcha y, al soltar, desaparece

#### Scenario: Girar a la derecha
- **WHEN** se mantiene Círculo (giro a la derecha por defecto)
- **THEN** se ve la flecha de la derecha a la derecha de la marcha y, al soltar, desaparece

#### Scenario: Pulsación breve
- **WHEN** el giro a la derecha se asigna a una pulsación corta de un botón y se pulsa
- **THEN** la flecha de la derecha se ve al menos 0,5 s

#### Scenario: Giro con otro botón
- **WHEN** se reasigna el giro a la izquierda a la palanca I2 y se mantiene
- **THEN** se ve la flecha de la izquierda

#### Scenario: Los dos giros
- **WHEN** se mantienen X y Círculo a la vez
- **THEN** se ven las dos flechas

#### Scenario: Freno y giro
- **WHEN** se mantienen el freno izquierdo de Zwift y el giro a la izquierda
- **THEN** no se ve la flecha de la izquierda

#### Scenario: Mando sin conectar
- **WHEN** el mando publicado aún no se ha conectado a la app de entrenamiento y se mantiene X
- **THEN** se ve la flecha de la izquierda, igual que con el mando conectado

#### Scenario: Mando desactivado
- **WHEN** la configuración venía de una versión con «Mando de Zwift» desactivado, hay un Square configurado y se mantiene X
- **THEN** se ve la flecha de la izquierda: el mando ya no se puede desactivar

#### Scenario: Se pierde el Square girando
- **WHEN** se mantiene X y el Square pierde la conexión
- **THEN** la flecha desaparece

#### Scenario: Aviso de acción a la vez
- **WHEN** se pulsa la palanca D1 (+1 marcha) y, antes de 1,5 s, se mantiene X
- **THEN** el pie sigue diciendo «+1 marcha» y se ve la flecha de la izquierda

### Requirement: Aviso de giro sin mover la pantalla
El indicador de giro SHALL ocupar siempre su sitio, de modo que al aparecer o desaparecer no se mueva ni cambie de tamaño nada, y SHALL NOT tapar la marcha. SHALL NOT depender solo del color y SHALL anunciarse al lector de pantalla («Girando a la izquierda», «Girando a la derecha»).

#### Scenario: Sin movimiento
- **WHEN** aparece o desaparece una flecha
- **THEN** la marcha, los datos, la barra de conexiones y los botones − y + no cambian de sitio ni de tamaño

#### Scenario: Lector de pantalla
- **WHEN** se mantiene el giro a la derecha con el lector de pantalla activado
- **THEN** el lector dice «Girando a la derecha»

#### Scenario: Marcha a la vista
- **WHEN** se mantiene el giro con una marcha de dos cifras en pantalla
- **THEN** la marcha se ve completa en vertical y en horizontal

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

### Requirement: Sin porcentaje de la batería del Square en Rodaje
Rodaje SHALL NOT mostrar el porcentaje de la batería del Square. El aviso de batería baja SHALL NOT competir con el aviso de acción del pie ni ocupar sitio nuevo.

#### Scenario: Sin porcentaje
- **WHEN** la batería del Square está baja y se rueda
- **THEN** no hay ningún porcentaje del Square en pantalla

#### Scenario: Aviso de acción a la vez
- **WHEN** la batería del Square está baja y se pulsa una palanca
- **THEN** el pie dice la acción durante 1,5 s como siempre

### Requirement: Conexiones de dispositivos en la barra superior
La barra superior SHALL mostrar los dispositivos a los que se conecta la app, uno por icono y en orden fijo: Neo, Square y, si están configurados, pulsómetro y ventilador. El Square lleva el icono de un manillar de bici de carretera, que SHALL leerse a 14 dp en todos los estados. En la descripción accesible, el Square SHALL llamarse «Controlador». El estado se ve por la forma: normal si conectado, atenuado con marca girando si se intenta conectar y tachado en color de alerta si no. La barra SHALL NOT mostrar iconos de Zwift ni del controlador publicado, ni cambiar de ancho, y es un solo objetivo táctil que abre Dispositivos.

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
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno configurado, o el Bluetooth está apagado)
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

### Requirement: Pantalla baja en vertical
En vertical, cuando el alto de la pantalla de rodaje es menor de 760 dp, la pantalla SHALL usar una disposición compacta: menos espacio entre sus bloques y el cassette más bajo, sin quitar ningún elemento ni cambiar su orden. Con 700 dp o más, la disposición SHALL ser la de siempre.

#### Scenario: Pantalla baja
- **WHEN** Rodaje está en vertical y mide 360 × 640 dp
- **THEN** el espacio entre los bloques es de 8 dp en vez de 16 dp y el cassette mide 40 dp en vez de 56 dp (la escalera del modo lineal no cambia), con los mismos elementos y en el mismo orden

#### Scenario: Pantalla alta
- **WHEN** Rodaje está en vertical y mide 390 × 844 dp
- **THEN** la disposición es la de siempre, sin compactar

#### Scenario: Horizontal
- **WHEN** Rodaje está en horizontal, con cualquier alto
- **THEN** la disposición es la de siempre

### Requirement: La marcha no desaparece en pantalla baja
En vertical, con la pantalla baja, la marcha SHALL verse siempre, reducida lo necesario para caber, a no menos de la mitad de su tamaño con la combinación de datos y botones que la app permite en esa pantalla.

#### Scenario: Cuatro datos con botones en pantalla baja
- **WHEN** Rodaje en vertical mide 360 × 640 dp, la marcha es 48 × 19 y hay 4 datos con los botones − y + activados
- **THEN** se ven, sin desplazamiento, la marcha 48 × 19 con su plato y su cassette, los 4 datos y los botones − y +

#### Scenario: Seis datos sin botones en pantalla baja
- **WHEN** Rodaje en vertical mide 360 × 640 dp, la marcha es 52 × 17 y hay 6 datos con los botones desactivados
- **THEN** se ven, sin desplazamiento, la marcha 52 × 17 con sus platos y su cassette y los 6 datos

### Requirement: Datos con botones en pantalla baja
En vertical, con la pantalla baja y los botones − y + visibles (por el ajuste o por no estar el Square conectado), Rodaje SHALL mostrar como máximo los 4 primeros datos elegidos, en su orden. Los demás SHALL ocultarse sin cambiar los datos elegidos en Ajustes, y SHALL volver a verse cuando los botones dejan de verse (al desactivarlos con el Square conectado o al conectarse el Square) o en una pantalla más alta.

#### Scenario: Seis datos elegidos con botones
- **WHEN** hay 6 datos elegidos, los botones están visibles y Rodaje en vertical mide 360 × 640 dp
- **THEN** Rodaje muestra los 4 primeros datos y los botones, y no muestra el quinto ni el sexto

#### Scenario: Sin Square
- **WHEN** hay 6 datos elegidos, el Square no está conectado y Rodaje en vertical mide 360 × 640 dp
- **THEN** Rodaje muestra los 4 primeros datos y los botones, aunque el ajuste esté desactivado

#### Scenario: Se conecta el Square
- **WHEN** en esa situación el Square se conecta y el ajuste está desactivado
- **THEN** los botones desaparecen y Rodaje vuelve a mostrar los 6 datos

#### Scenario: Quitar los botones
- **WHEN** con el Square conectado y en esa situación se desactivan los botones en Ajustes y se vuelve a Rodaje
- **THEN** Rodaje muestra los 6 datos, sin que haya habido que volver a elegirlos

#### Scenario: Cuatro datos o menos
- **WHEN** hay 4 datos elegidos o menos, los botones están visibles y Rodaje en vertical mide 360 × 640 dp
- **THEN** Rodaje muestra todos los datos elegidos

#### Scenario: Móvil de 720 dp
- **WHEN** hay 6 datos elegidos, los botones están visibles y Rodaje en vertical mide 360 × 720 dp
- **THEN** Rodaje muestra los 4 primeros datos y los botones, con la marcha visible

#### Scenario: Pantalla alta
- **WHEN** hay 6 datos elegidos, los botones están visibles y Rodaje en vertical mide 390 × 844 dp
- **THEN** Rodaje muestra los 6 datos y los botones

#### Scenario: En ERG
- **WHEN** se está en ERG con 6 datos elegidos y los botones visibles en un Rodaje de 360 × 640 dp
- **THEN** Rodaje muestra la potencia objetivo, los 4 primeros datos y los botones

### Requirement: Estado de la publicación en Rodaje
Rodaje SHALL mostrar, a la izquierda de la fila de Ajustes, el estado del rodillo publicado sin nombrar ninguna app de entrenamiento, con palabra y forma, sin decir por dónde va: «Conectado» (punto), «Esperando conexión…» (aro que gira, atenuado), y «No visible en la red», «Sin wifi», «Sin Bluetooth», «No disponible» o «Sin rodillo» (aro vacío con borde de alerta). Solo SHALL nombrar el transporte cuando falte. Tocarlo SHALL abrir Conexión.

#### Scenario: Esperando
- **WHEN** hay un rodillo configurado, el transporte del modo está disponible y ninguna app de entrenamiento está conectada
- **THEN** Rodaje muestra «Esperando conexión…» atenuado y con el aro que gira, y la marcha, la potencia y la cadencia siguen como sin app de entrenamiento

#### Scenario: Conectado por wifi
- **WHEN** el modo es «Wifi + Bluetooth» y la app de entrenamiento recibe el rodillo, el controlador y, si hay pulsómetro, el pulso
- **THEN** Rodaje muestra «Conectado» con el punto, sin «wifi» ni «Bluetooth», sin cambiar la marcha ni el área de datos

#### Scenario: Conectado por Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y la app de entrenamiento recibe el rodillo
- **THEN** Rodaje muestra «Conectado» con el punto, igual que por wifi

#### Scenario: Sin rodillo configurado
- **WHEN** no hay rodillo configurado
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
Con el rodillo conectado, el estado de Rodaje SHALL añadir «· sin controlador» si el modo es «Wifi + Bluetooth», hay un Square configurado y la app de entrenamiento no ha saludado al controlador; «· sin pulso» si hay un pulsómetro configurado y no está suscrita al pulso; o «· sin controlador ni pulso» si faltan los dos. Lo no configurado SHALL NOT mencionarse.

#### Scenario: Mando esperando
- **WHEN** el modo es «Wifi + Bluetooth», el rodillo está conectado, hay un Square configurado y el controlador no se ha saludado
- **THEN** Rodaje muestra «Conectado · sin controlador» y la descripción accesible dice «Conexión: rodillo, conectado; controlador, esperando»

#### Scenario: Pulso sin elegir
- **WHEN** el rodillo y el controlador están conectados, hay un pulsómetro configurado y la app de entrenamiento no está suscrita al pulso
- **THEN** Rodaje muestra «Conectado · sin pulso»

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** el modo es «Solo Bluetooth» o «Solo wifi (modo Zwift Ride)», el rodillo está conectado, hay un Square configurado y la app de entrenamiento aún no ha saludado al controlador
- **THEN** Rodaje muestra «Conectado», sin «sin controlador», porque el controlador va dentro del mismo dispositivo

#### Scenario: Sin Square ni pulsómetro configurados
- **WHEN** el rodillo está conectado y no hay Square ni pulsómetro configurados
- **THEN** Rodaje muestra «Conectado», sin mencionar el controlador ni el pulso

#### Scenario: Banda sin lectura
- **WHEN** la app de entrenamiento está suscrita al pulso y la banda no da lecturas
- **THEN** Rodaje no añade «sin pulso»: lo que falta es la banda, que se ve en la barra de conexiones

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
En horizontal, la línea superior de Rodaje SHALL llevar a la izquierda el modo con la transmisión («SIM · Shimano 105 · automático», «ERG APP»…), el estado de la plataforma centrado en la línea y, a la derecha, la barra de conexiones y el icono de Ajustes, separados entre sí 12 dp. La zona principal SHALL empezar justo bajo la línea.

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
- **THEN** la línea superior dice «ERG APP» o «ERG MANUAL» a la izquierda del estado de la plataforma, sin ninguna línea explicativa de las palancas

### Requirement: Qué cede en la línea superior en horizontal
Si el modo no cabe en su lado de la línea superior en horizontal, la transmisión SHALL pasar a dos líneas antes de mover el estado de la plataforma del centro; y si el estado no cabe en el centro, SHALL pasar a dos líneas antes de empujar las conexiones y Ajustes, que no ceden.

#### Scenario: Todo largo a la anchura más estrecha
- **WHEN** el móvil está en horizontal a la anchura más estrecha que se soporta, el modo es «SIM · Shimano 105 · automático», el estado dice «Conectado · sin controlador» y hay cuatro iconos de conexión
- **THEN** la transmisión pasa a dos líneas, el estado sigue centrado y se lee entero, y los iconos y Ajustes se ven enteros y sin solaparse

### Requirement: Confirmar la salida con Atrás
En Rodaje, el primer Atrás SHALL mostrar durante 3 s un aviso de varias líneas con el texto «Para cerrar la aplicación, pulsa Atrás otra vez». Un segundo Atrás dentro de esos 3 s SHALL terminar la sesión (spec `ride-session`) y cambiar el aviso a «Cerrando…» hasta que la app se cierre. Pasados los 3 s sin segundo Atrás, el aviso SHALL desaparecer y el siguiente Atrás SHALL volver a pedir confirmación.

#### Scenario: Primer Atrás
- **WHEN** se rueda en la marcha 14 y se pulsa Atrás una vez
- **THEN** se ve «Para cerrar la aplicación, pulsa Atrás otra vez», la marcha sigue en la 14 y las conexiones siguen activas

#### Scenario: Segundo Atrás a tiempo
- **WHEN** se ve el aviso de salida y, antes de 3 s, se pulsa Atrás de nuevo
- **THEN** el aviso pasa a «Cerrando…» y la app se cierra al terminar la sesión

#### Scenario: Segundo Atrás tarde
- **WHEN** se pulsa Atrás una vez, pasan más de 3 s y se pulsa Atrás de nuevo
- **THEN** el aviso desaparece a los 3 s y la segunda pulsación vuelve a mostrar «Para cerrar la aplicación, pulsa Atrás otra vez», sin terminar la sesión

#### Scenario: Atrás mientras se cierra
- **WHEN** se ve «Cerrando…» y se pulsa Atrás otra vez
- **THEN** no cambia nada y la sesión termina una sola vez

#### Scenario: Otra pantalla entre medias
- **WHEN** se pulsa Atrás en Rodaje, se abre Ajustes desde el icono antes de 3 s y se vuelve a Rodaje
- **THEN** no se ve el aviso de salida, y un Atrás en Rodaje vuelve a pedir confirmación

#### Scenario: Neo o Square desconectados
- **WHEN** el Neo está desconectado y el Square reconectando, y se pulsa Atrás dos veces seguidas
- **THEN** el aviso se comporta igual y la sesión termina sin esperar a ningún dispositivo

### Requirement: Aviso de salida sin mover la pantalla
El aviso de salida SHALL mostrarse flotando, translúcido y centrado en la pantalla de Rodaje, en horizontal y en vertical, sin mover ni cambiar de tamaño ningún otro elemento, de forma que lo que tapa se siga viendo. SHALL leerse entero, en una línea desde 360 dp de ancho y en varias si la pantalla es más estrecha. SHALL NOT depender solo del color y SHALL anunciarse al lector de pantalla. El aviso de acción SHALL poder verse a la vez sin pisarse.

#### Scenario: Sin movimiento
- **WHEN** aparece o desaparece el aviso de salida
- **THEN** la marcha, los datos, la barra de conexiones y los botones − y + no cambian de sitio ni de tamaño

#### Scenario: Texto completo en vertical
- **WHEN** se muestra «Para cerrar la aplicación, pulsa Atrás otra vez» en un móvil de 360 dp de ancho en vertical
- **THEN** el texto se lee entero en una línea, y en un móvil más estrecho se parte en dos sin salirse de la pantalla

#### Scenario: Centrado y translúcido
- **WHEN** se muestra el aviso de salida en horizontal o en vertical
- **THEN** está en el centro de la pantalla y la marcha que queda detrás se sigue viendo

#### Scenario: Lector de pantalla
- **WHEN** se pulsa Atrás con el lector de pantalla activado
- **THEN** el lector dice «Para cerrar la aplicación, pulsa Atrás otra vez» y, al cerrar, «Cerrando»

#### Scenario: Con un aviso de acción
- **WHEN** se ve «+1 marcha» en el pie y se pulsa Atrás
- **THEN** se ven los dos avisos, el de salida en el centro y el de acción en el pie, sin taparse entre sí

#### Scenario: Aviso de acción desactivado
- **WHEN** «Mostrar la acción» está desactivado en Ajustes y se pulsa Atrás
- **THEN** el aviso de salida se ve igual: no depende de ese ajuste

### Requirement: Guía de inicio en Rodaje
Mientras la guía de inicio esté a la vista (spec `getting-started-guide`), Rodaje SHALL mostrarla como una hoja inferior que, abierta, flota sobre la pantalla sin mover ni cambiar de tamaño ningún elemento, y, minimizada, como una tira de 48 dp en el sitio del pie. La hoja y la tira SHALL verse enteras, sin solaparse con las barras y sin desplazamiento, en vertical y en horizontal. La hoja SHALL no bloquear: los avisos y el estado de lo publicado SHALL seguir actualizándose.

#### Scenario: Primer uso en vertical
- **WHEN** se abre la app sin dispositivos en vertical a 360 × 640 dp
- **THEN** la hoja abierta muestra la página 1 entera sobre la marcha, sin desplazamiento y con la barra de conexiones y el modo a la vista

#### Scenario: Primer uso en horizontal
- **WHEN** se abre la app sin dispositivos en horizontal a la anchura más estrecha que se soporta
- **THEN** la hoja abierta muestra cada página entera, con los tres elementos de la página en columnas, sin desplazamiento

#### Scenario: Minimizada
- **WHEN** la hoja está minimizada
- **THEN** la tira ocupa el pie, con el texto «Toca las conexiones…» no visible, y la marcha y los datos no cambian más que los 8 dp que el pie gana a 48 dp

#### Scenario: Abrir y cerrar
- **WHEN** se abre y se minimiza la hoja con la sesión en marcha
- **THEN** la marcha no se mueve ni cambia de tamaño

#### Scenario: Al desaparecer
- **WHEN** la guía se oculta con la sesión en marcha
- **THEN** el pie recupera su alto y su texto, y la marcha y los datos recuperan solo esa diferencia

#### Scenario: Con los botones − y +
- **WHEN** la guía está minimizada con los botones − y + activados
- **THEN** los botones mantienen su objetivo táctil de 72 dp y la tira no los tapa; la hoja abierta puede taparlos mientras se lee

#### Scenario: Aviso de acción
- **WHEN** se hace una acción con «Mostrar la acción» activado y la guía minimizada
- **THEN** el aviso ocupa el pie mientras dura y después vuelve la tira

#### Scenario: Objetivos táctiles
- **WHEN** se miden la hoja y la tira
- **THEN** cada paso, cada botón, la equis y el botón de minimizar tienen un objetivo táctil de al menos 48 dp

### Requirement: Pendiente en Rodaje
Rodaje SHALL poder mostrar «Pendiente» como dato de la rejilla cuando esté elegido en «Datos en pantalla», con la pendiente del terreno de la simulación, en % con signo explícito y un decimal, con coma («+4,2 %», «−1,0 %», «0,0 %»), con el mismo tamaño de cifra que los demás. Con una app de entrenamiento enviando pendiente SHALL mostrar la que envía (y mantenerla los 30 s siguientes a su marcha); en modo LIBRE SHALL mostrar «0,0 %»; en ERG de Zwift o en ERG manual SHALL mostrar «—». El dato SHALL NOT incluir el ajuste de la marcha ni el límite de «Bajadas», y SHALL NOT leerse solo por el color: el signo y el valor lo dicen.

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de +4,2 % y «Pendiente» está elegido
- **THEN** el dato dice «+4,2 %»

#### Scenario: Bajada
- **WHEN** Zwift envía una pendiente de −7,0 %, la marcha es la 1, «Bajadas» está en «La marcha no crea bajadas» y «Pendiente» está elegido
- **THEN** el dato dice «−7,0 %», la pendiente del terreno, y no el 0,0 % que se envía al Neo

#### Scenario: Libre
- **WHEN** se rueda sin Zwift, en cualquier marcha, con «Pendiente» elegido
- **THEN** el dato dice «0,0 %»

#### Scenario: ERG
- **WHEN** se está en ERG manual o en un entrenamiento de Zwift con «Pendiente» elegido
- **THEN** el dato dice «—»

#### Scenario: Zwift se va
- **WHEN** se cierra Zwift rodando en una subida de +4,2 % con «Pendiente» elegido
- **THEN** el dato sigue en «+4,2 %» durante 30 s y después dice «0,0 %»

#### Scenario: Entre los seis datos
- **WHEN** están elegidos potencia, cadencia, tiempo, W/kg, velocidad y distancia
- **THEN** «Pendiente» no se puede añadir sin quitar otro dato (el máximo sigue siendo seis)

#### Scenario: Sin control
- **WHEN** se rueda en una ruta de Zwift o sin Zwift con «Pendiente» elegido
- **THEN** Rodaje no ofrece ningún control para cambiar la pendiente

### Requirement: Cambiar el modo tocándolo
Con el ajuste «Cambiar de modo tocando» activado, el indicador de modo de Rodaje SHALL ser un botón de al menos 48 dp que alterna Libre o SIM ⇄ ERG manual con el mismo efecto que la acción «Alternar SIM ⇄ ERG» del Square, sin ventana de confirmación. Con el ajuste desactivado (por defecto), SHALL ser solo una etiqueta, aunque los botones − y + estén visibles, sin respuesta al toque.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez y se toca el indicador de modo
- **THEN** no cambia nada y el indicador no se ve como un botón

#### Scenario: De Libre a ERG manual
- **WHEN** el ajuste está activado, se rueda sin Zwift en la marcha 14 con la última potencia de ERG manual en 200 W y se toca el indicador «LIBRE»
- **THEN** el indicador pasa a «ERG MANUAL», el Neo pasa a 200 W, la marcha sigue siendo la 14 y el pie dice «ERG manual · 200 W»

#### Scenario: De ERG manual a Libre
- **WHEN** el ajuste está activado, se está en ERG manual con la marcha 14 sin Zwift y se toca el indicador «ERG MANUAL»
- **THEN** el indicador pasa a «LIBRE», el Neo recibe la pendiente de la marcha 14 y el pie dice «Modo Libre»

#### Scenario: En una ruta de la app
- **WHEN** el ajuste está activado, se rueda en una ruta de la app y se toca el indicador «SIM»
- **THEN** el indicador pasa a «ERG MANUAL» con la potencia recordada, como con el Square

#### Scenario: En un entrenamiento de la app
- **WHEN** el ajuste está activado, Zwift pide 230 W en un entrenamiento y se toca el indicador «ERG APP»
- **THEN** el Neo sigue a 230 W, el indicador sigue en «ERG APP» y el pie dice «Entrenamiento de la app» durante 1,5 s

#### Scenario: Botones visibles sin el ajuste
- **WHEN** los botones − y + están visibles (Square sin conexión) y el ajuste de cambiar de modo está desactivado
- **THEN** el indicador sigue siendo una etiqueta, sin respuesta al toque

#### Scenario: Sin Square
- **WHEN** no hay ningún Square configurado, el ajuste está activado y se toca el indicador
- **THEN** el modo alterna igual, sin necesitar el controlador

#### Scenario: Neo ausente
- **WHEN** el Neo no está conectado, el ajuste está activado y se toca el indicador
- **THEN** el modo alterna y, al conectarse el Neo, recibe la potencia o la pendiente vigente

#### Scenario: Toque repetido
- **WHEN** se toca el indicador dos veces seguidas
- **THEN** el modo vuelve al de partida con la marcha y la potencia como estaban, sin perder ninguna conexión

### Requirement: Espacio del indicador de modo tocable
El indicador de modo tocable SHALL mantener su objetivo táctil de 48 dp sin solaparse con el icono de Ajustes ni con la barra de conexiones, tanto en la fila del modo en vertical como en la línea superior en horizontal, y al pasar de Libre o SIM a ERG, o al activarse o desactivarse el ajuste, SHALL NOT mover el icono de Ajustes, la barra de conexiones ni la marcha.

#### Scenario: En vertical
- **WHEN** el móvil está en vertical a 360 dp con el ajuste activado
- **THEN** el indicador de modo y el icono de Ajustes tienen cada uno su objetivo táctil de al menos 48 dp, sin solaparse

#### Scenario: En horizontal
- **WHEN** el móvil está en horizontal a la anchura más estrecha que se soporta con el ajuste activado
- **THEN** el indicador de modo, la barra de conexiones y el icono de Ajustes no se solapan y todo se ve sin desplazamiento

#### Scenario: Al cambiar de modo
- **WHEN** se toca el indicador y pasa de «LIBRE» a «ERG MANUAL»
- **THEN** la marcha, los datos y el icono de Ajustes no se mueven
