# ui-components Specification

## Purpose

Define los componentes de la interfaz (marcha, transmisión, datos de rodaje, conexión, modo, avisos, botones del Square, ajustes, navegación y diagnóstico) sobre los que se construyen todas las pantallas, con su anatomía, variantes y estados.

## Requirements

### Requirement: Marcha como en un grupo real
El componente de marcha SHALL mostrar la marcha puesta como cifra principal: el número en transmisión lineal, o plato × piñón (p. ej. `50×17`) en transmisión física, sin ningún dato del modelo de resistencia.

#### Scenario: Transmisión lineal
- **WHEN** la transmisión es lineal de 24 marchas y la marcha es la 14
- **THEN** se ve «14» en grande con «Marcha · de 24» al lado

#### Scenario: Transmisión física
- **WHEN** la transmisión es Shimano 2×12 y la marcha es plato 50 y piñón 17
- **THEN** se ve «50×17» en grande, con el signo × más pequeño y atenuado

#### Scenario: Sin datos del modelo
- **WHEN** se muestra el componente de marcha con cualquier modelo de resistencia
- **THEN** no aparece la pendiente de Zwift, el ajuste por marcha ni lo enviado al Neo

### Requirement: Aviso de tope
Al llegar a la marcha más ligera o a la más dura, el componente de marcha SHALL atenuar la cifra y mostrar una etiqueta de tope con texto, además de la vibración si está activada.

#### Scenario: Tope en la marcha más dura
- **WHEN** se pide una marcha más dura estando en la última
- **THEN** la marcha no cambia, la cifra se atenúa y aparece «Tope · marcha más dura»

### Requirement: Indicadores de plato y cassette
En transmisión física, el indicador de plato SHALL mostrar los platos con el activo relleno, y el cassette SHALL mostrar una barra por piñón, de mayor a menor de izquierda a derecha, con altura proporcional a sus dientes y el activo resaltado y numerado.

#### Scenario: Plato activo
- **WHEN** la marcha usa el plato de 50 en una transmisión 50/34
- **THEN** el 50 aparece relleno y el 34 solo con borde

#### Scenario: Piñón activo
- **WHEN** la marcha usa el piñón de 17 en un cassette 11–34
- **THEN** la barra del 17 está resaltada y su número se ve en el color de marca, con el 34 a la izquierda y el 11 a la derecha

#### Scenario: Un solo plato
- **WHEN** la transmisión tiene un solo plato
- **THEN** no se muestra el indicador de plato

### Requirement: Escalera de marchas lineal
En transmisión lineal, la escalera SHALL mostrar un segmento por marcha, de la más ligera a la izquierda a la más dura a la derecha, con la actual resaltada, las anteriores diferenciadas y una marca en la marcha de referencia.

#### Scenario: Marcha de referencia
- **WHEN** la marcha de referencia es la 12 y la actual la 14
- **THEN** el segmento 14 está resaltado y el 12 lleva la marca de referencia

### Requirement: Rejilla de datos sin desplazamiento
La rejilla de datos de rodaje SHALL mostrar de 1 a 6 datos elegidos por el usuario, en su orden, con el mismo tamaño de cifra para todos: el mayor, hasta 144 px, con el que el valor más largo posible de cada dato cabe en su celda junto a su etiqueta, sin desplazamiento ni cifras cortadas. Con un número impar de datos en 2 columnas, el último SHALL ir centrado en su fila. Todas las celdas SHALL alinear igual la etiqueta y la cifra, tengan unidad o no.

#### Scenario: Pocos datos
- **WHEN** hay 2 datos en la pantalla horizontal
- **THEN** se apilan en una columna con la cifra más grande con la que caben en su área, igual para los dos

#### Scenario: Máximo de datos
- **WHEN** hay 6 datos en la pantalla horizontal
- **THEN** se colocan en 2 columnas y 3 filas, todos visibles y sin desplazamiento

#### Scenario: Valor largo
- **WHEN** el tiempo pasa de «59:59» a «1:00:00» o la potencia de «999» a «1000» W
- **THEN** la cifra de la rejilla no cambia de tamaño, porque ya estaba medida con el valor más largo de cada dato, y ningún valor se corta ni pasa a dos líneas

#### Scenario: Dato sin valor
- **WHEN** un dato no está disponible porque su sensor no está conectado
- **THEN** se muestra «—» atenuado, nunca 0, con la misma cifra que si tuviera valor

#### Scenario: Último dato impar
- **WHEN** hay 3 o 5 datos en 2 columnas
- **THEN** el último ocupa el ancho de las dos columnas, centrado, sin hueco a su derecha

#### Scenario: Filas con y sin unidad
- **WHEN** hay 6 datos y unos tienen unidad (potencia, en W) y otros no (tiempo, W/kg)
- **THEN** en todas las celdas la etiqueta queda a la misma distancia del borde superior y las cifras tienen el mismo alto

#### Scenario: Tope de la cifra
- **WHEN** hay 1 solo dato y su área permitiría una cifra mayor
- **THEN** la cifra se queda en 144 px, centrada en su área

### Requirement: Insignia de modo
La insignia de modo SHALL mostrar «LIBRE», «SIM» o «ERG», con su color de modo (el de «LIBRE» SHALL distinguirse del de «SIM» por la palabra, no solo por el color), y en ERG SHALL indicar quién fija la potencia (Zwift o manual).

#### Scenario: Libre
- **WHEN** se rueda sin app de entrenamiento
- **THEN** la insignia muestra «LIBRE»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift fija una potencia
- **THEN** la insignia muestra «ERG» con «ZWIFT» al lado

### Requirement: Aviso de la acción realizada
El aviso de acción SHALL mostrar durante 1,5 s solo el resultado de una pulsación del Square («+1 marcha», «Plato 50», «+10 W»), sin el botón ni el gesto, y SHALL poder desactivarse en ajustes.

#### Scenario: Cambio de plato
- **WHEN** un cambio de marcha cambia también de plato
- **THEN** el aviso dice «Plato 50» y desaparece a los 1,5 s

#### Scenario: Aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado
- **THEN** no aparece ningún aviso al pulsar el Square

### Requirement: Teclas del Square
Cada botón del Square SHALL dibujarse como una tecla con un estado pulsado: los de símbolo (△ ○ □ ✕) y los de la cruceta (↑ ↓ ← →) con un icono dibujado, de la misma caja, el mismo grosor y el mismo tamaño óptico, y los de letra (A B Y Z) y los nombres cortos («Palanca D1», «Freno I», «Campag. D») con texto. Los iconos SHALL tomar el color de la tecla, y su descripción accesible SHALL decir el nombre del símbolo, no el glifo. SHALL NOT depender de la fuente del sistema para ningún símbolo.

#### Scenario: Combinación
- **WHEN** se representa la combinación Campag. D + Palanca D1
- **THEN** se ven las dos teclas unidas por «+», con el modificador primero

#### Scenario: Cruceta alineada
- **WHEN** se ven juntas las cuatro teclas de la cruceta
- **THEN** las cuatro flechas están centradas en su tecla, a la misma altura y con el mismo tamaño

#### Scenario: Símbolos con la misma proporción
- **WHEN** se ven juntas las teclas △ ○ □ ✕
- **THEN** los cuatro símbolos parecen del mismo tamaño y grosor y están centrados en su tecla

#### Scenario: Tecla pulsada
- **WHEN** se mantiene pulsado un botón de símbolo o de cruceta
- **THEN** su tecla se rellena con el color de marca y el icono toma el color de texto sobre ese relleno

#### Scenario: Descripción accesible
- **WHEN** un lector de pantalla recorre la tecla del triángulo
- **THEN** dice «triángulo», y la de la flecha de la cruceta dice «flecha arriba», «flecha abajo», «flecha a la izquierda» o «flecha a la derecha»

#### Scenario: Letras y nombres
- **WHEN** se ven las teclas A, B, Y, Z, «Palanca D1» y «Campag. I»
- **THEN** siguen con su texto, sin icono

### Requirement: Fila de asignación con su capa
La fila de asignación SHALL mostrar el botón o la combinación, el gesto, la acción y si la acción va a Zwift o la resuelve la app, y SHALL tener un estado de captura. Una fila de Zwift solo informativa SHALL distinguirse de una que se puede tocar por la falta de chevron y de estado táctil, y una acción sin entrada SHALL mostrar «Sin botón» en lugar de las teclas.

#### Scenario: Función de Zwift
- **WHEN** se muestra la asignación de Y
- **THEN** la fila dice «Y · pulsar · Usar powerup» con la etiqueta ZWIFT

#### Scenario: Acción sin botón
- **WHEN** se muestra una acción de la app sin ninguna entrada asignada
- **THEN** la fila dice «Sin botón» en `ink-muted` y la acción, con la etiqueta APP

### Requirement: Captura de un gesto
El panel de captura SHALL esperar a que el usuario pulse en el Square el botón, gesto o combinación, mostrarlo con sus teclas y avisar si ya está asignado a otra acción. Sin el Square conectado, SHALL decirlo en lugar de esperar.

#### Scenario: Combinación ya asignada
- **WHEN** se captura Campag. D + ↑, que ya sube 10 W en ERG
- **THEN** se muestra la combinación y un aviso de error que dice qué acción se quedará sin botón si se guarda

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado
- **THEN** el panel dice «Conecta el Square para capturar un botón», sin el borde de escucha

### Requirement: Piezas de ajustes accesibles
Las listas de ajustes SHALL componerse de secciones con título y filas cuyo objetivo táctil es la fila entera (al menos 48 dp): enlaces, interruptores, opciones excluyentes, controles segmentados, steppers y filas reordenables.

#### Scenario: Interruptor por la fila
- **WHEN** se toca el texto de la fila «Vibrar»
- **THEN** cambia el estado de su casilla

#### Scenario: Límite de selección
- **WHEN** ya hay 6 datos elegidos en «Datos en pantalla»
- **THEN** las filas no elegidas se muestran atenuadas y su casilla no se puede marcar

### Requirement: Navegación inferior
La navegación inferior SHALL ofrecer las pestañas Rodar, Conexión, Dispositivos y Ajustes con icono y texto, marcar la activa por color y por estado accesible, y SHALL NOT aparecer en la pantalla de rodaje ni en pantallas secundarias.

#### Scenario: Pestaña activa
- **WHEN** se está en Ajustes
- **THEN** la pestaña Ajustes aparece en el color de marca y se anuncia como página actual

### Requirement: Avisos en línea
Los avisos en línea SHALL tener una variante informativa y otra de error, siempre con icono y texto; el de error SHALL anunciarse como alerta y decir qué pasa y qué hacer.

#### Scenario: Neo sin respuesta
- **WHEN** el Neo no responde en Dispositivos
- **THEN** aparece «El Neo no responde. Comprueba que está enchufado y pedalea para despertarlo.» con icono de error

### Requirement: Componentes de diagnóstico
Diagnóstico SHALL disponer de un indicador de pendiente (Zwift → efectiva, con signo y color de subida o bajada) y de un volcado de tráfico con hora con milésimas, origen, característica y bytes, las tramas más recientes arriba y un estado vacío, y estos componentes SHALL NOT usarse en la pantalla de rodaje.

#### Scenario: Pendiente efectiva
- **WHEN** Zwift manda 4,0 % y la marcha suma 1,5 %
- **THEN** el indicador muestra «4,0 %» atenuado y «+5,5 %» en color de subida

#### Scenario: Volcado de tráfico
- **WHEN** la app envía una pendiente al Neo
- **THEN** el volcado muestra arriba una fila con la hora (`12:04:31.236`), el origen «→ Neo» en color de marca, la característica `fec3` y los bytes, en columnas alineadas

#### Scenario: Sin tráfico
- **WHEN** todavía no ha llegado ni salido ninguna trama
- **THEN** el volcado dice «Sin tráfico todavía»

### Requirement: Stepper con límites
El stepper SHALL mostrar deshabilitado, de forma distinguible y anunciada como no disponible, el botón que superaría el mínimo o el máximo, y SHALL mantener habilitado el otro.

#### Scenario: En el máximo
- **WHEN** un stepper está en su valor máximo
- **THEN** su botón «+» se ve atenuado, no responde al tocarlo y se anuncia como deshabilitado, y el «−» sigue disponible

#### Scenario: En los dos temas
- **WHEN** se revisa el stepper deshabilitado en la guía de estilo
- **THEN** aparece en tema rodaje y en tema claro

### Requirement: Barra de conexiones con iconos
La barra de conexiones SHALL mostrar un icono de 14 dp por enlace, sin nombres, con el estado por la forma: normal si está conectado; atenuado con una marca girando en la esquina si espera o se conecta; tachado en el color de alerta sin conexión. Los iconos SHALL ir agrupados, con 4 dp entre ellos, y su ancho SHALL depender solo del número de enlaces. SHALL ser un único objetivo táctil de al menos 48 dp de alto, con una descripción accesible que da el estado de cada enlace.

#### Scenario: Estados sin color
- **WHEN** la barra muestra un enlace conectado, uno esperando y uno sin conexión, y se ve en escala de grises
- **THEN** se distinguen los tres: el icono limpio, el icono con la marca en la esquina y el icono tachado

#### Scenario: Cambio de estado sin moverse
- **WHEN** un enlace pasa de esperando a conectado y después a sin conexión
- **THEN** la barra mantiene su ancho y ningún icono se desplaza

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un enlace espera
- **THEN** la marca de la esquina no gira y sigue siendo distinta del tachado

#### Scenario: Iconos agrupados
- **WHEN** la barra muestra dos, tres o cuatro enlaces
- **THEN** los iconos se leen como un solo bloque, con 4 dp entre ellos, y no como elementos sueltos

#### Scenario: Estados mezclados sin tocarse
- **WHEN** la barra muestra un enlace esperando junto a uno sin conexión
- **THEN** la marca de la esquina del primero y el tachado del segundo no se tocan ni se confunden con el icono vecino

#### Scenario: Seis enlaces
- **WHEN** la barra muestra seis enlaces a 360 dp de ancho, en la fila de la plataforma de Rodaje
- **THEN** caben en una sola línea sin cortarse

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega a la barra con el Neo conectado y el controlador sin conexión
- **THEN** la lee como un solo elemento: «Conexiones: Neo, conectado; Controlador, sin conexión»

#### Scenario: Contraste en los dos temas
- **WHEN** se ve la barra en tema rodaje y en tema claro
- **THEN** los iconos en los tres estados se distinguen del fondo con al menos 3:1

### Requirement: Icono de cada enlace
Cada enlace SHALL tener un icono propio, el mismo en Rodaje, Dispositivos y Diagnóstico: el Neo, un rayo; el Square, un manillar de bici de carretera (el controlador); el controlador que la app publica a la app de entrenamiento, el icono de Zwift; y el pulsómetro y el ventilador, los suyos. Dispositivos y Diagnóstico SHALL mostrar el icono junto al nombre de cada enlace, de modo que sirvan de leyenda para la barra de Rodaje.

#### Scenario: Leyenda en Dispositivos
- **WHEN** se abre Dispositivos
- **THEN** las filas del Neo y del Square llevan delante el mismo icono que en la barra de Rodaje

#### Scenario: Diagnóstico coherente
- **WHEN** se abre Diagnóstico
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos, sin que ninguno represente un enlace distinto en otra pantalla

#### Scenario: Manillar y Zwift distintos
- **WHEN** se ven a la vez la fila del Square y la del controlador publicado en Diagnóstico
- **THEN** una lleva el manillar y la otra el icono de Zwift, y ninguno es el mando de consola

### Requirement: Icono de Zwift
Zwift SHALL tener una «Z» en un recuadro redondeado, que SHALL NOT imitar el logotipo de Zwift, y se usa en Diagnóstico, no en la barra de Rodaje.

#### Scenario: Icono de Zwift fuera de la barra
- **WHEN** se abre la barra de conexiones de Rodaje
- **THEN** no aparece la «Z» de Zwift

### Requirement: Chip de conexión con forma y palabra
El chip de conexión SHALL mostrar el nombre del dispositivo y su estado con una forma que no dependa solo del color (punto lleno conectado, anillo abierto girando buscando, anillo vacío con el borde del chip de alerta sin conexión) y, si no está conectado, con una palabra. Se usa fuera de Rodaje.

#### Scenario: Zwift sin conexión
- **WHEN** Zwift no está conectado a la app
- **THEN** el chip muestra «Zwift sin conexión» con anillo vacío y borde de alerta

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un dispositivo está buscando
- **THEN** el anillo no gira y el estado se sigue leyendo por la palabra

### Requirement: Icono de la entrada Botones
La entrada «Botones» de Ajustes SHALL usar un icono propio, formado por un triángulo, un cuadrado relleno, un círculo y una cruz en cuadrícula 2×2, distinto del icono del controlador (el manillar) de la barra de conexiones. El icono SHALL ser de un solo color, tomar el del texto de su fila y reconocerse a 24 dp en los dos temas.

#### Scenario: Entrada de Botones en Ajustes
- **WHEN** se abre Ajustes
- **THEN** la entrada «Botones» muestra el icono de los cuatro símbolos y no el del manillar

#### Scenario: Botones y mando de Zwift distintos
- **WHEN** se ven a la vez la entrada «Botones» de Ajustes y el manillar del controlador de la barra de conexiones de Rodaje
- **THEN** los dos iconos son distintos

#### Scenario: Entrada en los dos temas
- **WHEN** Ajustes se ve en tema rodaje y en tema claro
- **THEN** el icono se ve en el color del texto de la entrada, con su contraste

### Requirement: Icono de Conexión
La pestaña Conexión de la navegación inferior SHALL usar un icono propio que sugiera emitir o publicar una señal, de un solo color, que tome el de la pestaña (activa o no), distinto del icono de Bluetooth de Dispositivos, del de wifi y del del mando, y reconocible a 24 dp en los dos temas.

#### Scenario: Pestaña Conexión
- **WHEN** se abre cualquier pantalla con la barra inferior
- **THEN** la pestaña Conexión muestra su icono propio, distinto del de Dispositivos

#### Scenario: Pestaña activa en los dos temas
- **WHEN** la pestaña Conexión está activa en tema rodaje y en tema claro
- **THEN** el icono se ve en el color de marca de la pestaña, con el contraste del texto de la pestaña

### Requirement: Estado de la publicación
El estado de la publicación en Rodaje SHALL ser un chip de una línea con el estado del rodillo y lo que falta en palabra y forma (punto lleno conectado, aro que gira esperando en tono atenuado, aro vacío con borde de alerta si no se puede publicar), sin nombre de plataforma ni transporte, a la izquierda de la fila de Ajustes, con un objetivo táctil de al menos 48 dp de alto sin alargar la línea. La guía SHALL mostrar sus estados en los dos temas.

#### Scenario: Estados sin color
- **WHEN** el estado se ve en escala de grises
- **THEN** conectado, esperando y sin publicar se distinguen por la forma y por la palabra

#### Scenario: Contraste en los dos temas
- **WHEN** se ve el estado en tema rodaje y en tema claro
- **THEN** el texto se lee con al menos 4,5:1 y el borde de alerta con al menos 3:1

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al estado con el rodillo conectado y el controlador esperando
- **THEN** lo lee como un solo elemento: «Conexión: rodillo, conectado; controlador, esperando»

#### Scenario: Texto más largo
- **WHEN** el estado dice «Conectado · sin controlador ni pulso» en un móvil de 360 dp de ancho en vertical
- **THEN** cabe en una línea sin empujar el botón de Ajustes ni cambiar el alto de la fila

### Requirement: Lista de modos de conexión
El sistema de diseño SHALL ofrecer una lista de modos de elección única, cada modo con su nombre, su descripción de por dónde va cada servicio, su detalle técnico, sus etiquetas de compatibilidad y las marcas de recomendado y de elegido, esta última por algo más que el color. La marca de elegido SHALL ser el anillo del acento de marca en la fila y un radio con el aro y el punto central de ese mismo acento, sin otro color añadido; los modos sin elegir SHALL llevar el radio vacío con el aro neutro. La guía SHALL mostrarla con modos sin elegir, elegido y recomendado.

#### Scenario: Modo elegido
- **WHEN** un modo está elegido
- **THEN** se distingue por el anillo de la fila y por el radio con aro y punto del acento de marca, además del color, con el contraste de 4,5:1 para el texto y de 3:1 para el radio sobre la fila en los dos temas, y su fila tiene al menos 48 dp de alto

#### Scenario: Modo sin elegir
- **WHEN** un modo no está elegido
- **THEN** su radio es un aro neutro sin punto y su fila no lleva anillo

#### Scenario: Modo elegido en escala de grises
- **WHEN** la lista se ve en escala de grises
- **THEN** el modo elegido se sigue distinguiendo por el anillo de la fila y por el punto del radio

#### Scenario: Modo recomendado
- **WHEN** un modo es el recomendado y no está elegido
- **THEN** lleva la marca «Recomendado» en texto

#### Scenario: Etiquetas de compatibilidad
- **WHEN** un modo tiene «Zwift: validado» y «Otras apps con FTMS: a validar»
- **THEN** las dos etiquetas se distinguen por el texto, no solo por el color, y lo «a validar» no se presenta como confirmado

### Requirement: Fila de servicio publicado
El sistema de diseño SHALL ofrecer una fila de servicio con su icono, su nombre, por dónde se publica, su estado en palabra y forma (conectado, esperando, sin conexión, no disponible, no se ofrece) y una ayuda debajo, que puede llevar a otra pantalla. La guía SHALL mostrar todos sus estados en los dos temas.

#### Scenario: Estados sin color
- **WHEN** la fila se ve en escala de grises
- **THEN** cada estado se distingue por la palabra y la forma

#### Scenario: No se ofrece
- **WHEN** la fila está en «No se ofrece» con la ayuda que lleva a Dispositivos
- **THEN** el estado se ve atenuado, distinto de «Sin conexión», y la ayuda tiene un objetivo táctil de al menos 48 dp

### Requirement: Aviso de salida
El sistema de diseño SHALL incluir el componente `ExitNotice` (`sq-exit-notice`): un aviso flotante que se superpone a la pantalla sin ocupar sitio en su disposición, translúcido y discreto, con texto de una o varias líneas, fondo, borde y radio de los tokens existentes, y que se ve en los dos temas con el contraste de texto exigido. SHALL tener su carpeta con `README.md` y `preview.html`, y verse en la guía de estilo.

#### Scenario: Alta del componente
- **WHEN** se regenera la guía con `python3 scripts/design-system/generar.py`
- **THEN** la guía muestra `ExitNotice` en los dos temas, centrado sobre una pantalla de Rodaje

#### Scenario: Contraste
- **WHEN** se genera la guía
- **THEN** el texto del aviso cumple 4,5:1 sobre su fondo en los dos temas y el generador no falla

#### Scenario: Sin sitio propio
- **WHEN** el aviso se coloca sobre una pantalla
- **THEN** no cambia el tamaño ni la posición de ningún otro componente de esa pantalla
