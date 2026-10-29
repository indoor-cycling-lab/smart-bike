## MODIFIED Requirements

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo: «LIBRE» con la transmisión al rodar sin una app de entrenamiento que envíe pendiente, «SIM» con la transmisión en una ruta de la app de entrenamiento (fuera de un entrenamiento), siempre con la transmisión («Lineal · 24 marchas», o «Shimano 105 · automático» o «Shimano 105 · manual»), «ERG ZWIFT» en un entrenamiento ERG de Zwift y «ERG MANUAL» en ERG manual; en ERG SHALL NOT añadir una línea que explique lo que hacen las palancas, porque el modo ya lo dice y el aviso de la acción («+10 W») lo muestra al usar las palancas. En «LIBRE» la pendiente del terreno es 0 % y solo cambia la marcha. El modo SHALL leerse por la palabra, no solo por el color, y SHALL seguir al modo de control: al irse la app de entrenamiento, «SIM» se mantiene 30 s, lo mismo que la pendiente, y después pasa a «LIBRE».

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

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide una potencia en un entrenamiento
- **THEN** la barra superior dice «ERG ZWIFT», sin ninguna línea explicativa de las palancas

#### Scenario: ERG manual
- **WHEN** se entra en ERG manual, con o sin Zwift
- **THEN** la barra superior dice «ERG MANUAL», sin ninguna línea explicativa de las palancas

#### Scenario: Shimano en automático
- **WHEN** se rueda sin Zwift con la Shimano en automático
- **THEN** la barra superior dice «LIBRE» y «Shimano 105 · automático»

#### Scenario: Cambiar de modo
- **WHEN** la Shimano está en automático y se elige «Manual» en Marchas
- **THEN** al volver a Rodaje, la barra superior dice «Shimano 105 · manual»

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
- **THEN** el pie dice «Entrenamiento de Zwift» durante 1,5 s y el modo sigue siendo «ERG ZWIFT»

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


## ADDED Requirements

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
