# screen-mockups Specification

## Purpose

Define las maquetas de las pantallas de la app (rodaje, dispositivos, botones, marchas, ajustes, datos en pantalla y diagnóstico), su navegación y los estados que cada una debe representar, como referencia validada antes de implementarlas.

## Requirements

### Requirement: Maquetas sobre el sistema de diseño
Cada maqueta SHALL ser una página HTML estática que use solo los tokens y componentes del sistema de diseño, sin estilos propios ni atributos `style` salvo variables que el sistema documente, y la hoja compartida de la herramienta de revisión.

#### Scenario: Sin estilos propios
- **WHEN** se revisa el código de cualquier maqueta
- **THEN** no contiene hojas de estilo propias ni atributos `style` con valores literales

### Requirement: Herramienta de revisión de estado y tema
Cada maqueta SHALL tener una barra de revisión, visiblemente distinta de la app, con un selector de tema (rodaje o claro) y, si tiene varios estados, un selector de estado; el estado y el tema SHALL ir en la dirección para poder enlazarlos. La dirección SHALL admitir además una vista de captura (`vista=captura`) que oculta la barra de revisión y muestra solo la pantalla de la app, para fotografiarla.

#### Scenario: Enlace a un estado
- **WHEN** se abre `maquetas/rodaje.html#estado=tope&tema=claro`
- **THEN** se ve la pantalla de rodaje en el estado de tope y en tema claro, con los selectores mostrando esos valores

#### Scenario: Sin JavaScript
- **WHEN** se abre una maqueta con JavaScript desactivado
- **THEN** se ven todos sus estados apilados

#### Scenario: Vista de captura
- **WHEN** se abre `maquetas/rodaje.html#estado=tope&tema=claro&vista=captura`
- **THEN** se ve solo la pantalla de rodaje en ese estado y tema, sin la barra de revisión

### Requirement: Índice de maquetas
El índice SHALL enlazar cada pantalla y cada uno de sus estados, indicar cuál es el estado por defecto y relacionar cada pantalla con sus funcionalidades del documento funcional.

#### Scenario: Revisar un cambio
- **WHEN** alguien abre el índice para validar un cambio
- **THEN** puede ir con un clic a cada estado afectado

### Requirement: Datos ficticios sin servicios externos
Las maquetas SHALL usar datos ficticios con forma realista (potencias, cadencias, transmisiones de marca, combinaciones de botones), marcar entre corchetes lo pendiente de capturar con el equipo real y SHALL NOT hacer peticiones a servicios externos.

#### Scenario: Abrir sin conexión
- **WHEN** se abre una maqueta servida en local sin conexión a internet
- **THEN** se ve completa, con sus fuentes e iconos

### Requirement: Marco de dispositivo
Cada maqueta SHALL mostrarse en un ordenador dentro de un marco del tamaño del móvil (844 × 390 dp en horizontal, 390 dp de ancho en vertical) y SHALL ocupar todo el ancho al abrirla en un móvil.

#### Scenario: Rodaje en el móvil
- **WHEN** se abre la maqueta de rodaje en un móvil en horizontal
- **THEN** ocupa toda la pantalla sin el marco

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal, sin Zwift y con Zwift, de los escenarios siguientes. Ningún estado SHALL mostrar velocidad ni distancia.

#### Scenario: Estado por defecto
- **WHEN** se abre la maqueta de rodaje sin estado
- **THEN** se ve la transmisión lineal de 24 marchas con 2 datos (potencia y cadencia) y sin botones − y +

#### Scenario: Transmisiones de marca y datos
- **WHEN** se recorren los estados de rodaje
- **THEN** hay al menos Shimano, SRAM y Campagnolo, y rejillas de 1, 2, 3, 4 y 6 datos elegidos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima

#### Scenario: Botones táctiles
- **WHEN** se elige el estado con botones − y +
- **THEN** aparecen a la derecha, de 72 dp, con 4 datos visibles a la vez

#### Scenario: Tope
- **WHEN** se elige el estado de tope
- **THEN** la marcha aparece atenuada con la etiqueta de tope

#### Scenario: Conexión perdida
- **WHEN** se elige el estado de Neo desconectado
- **THEN** el icono del Neo se ve tachado, la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas, y la rejilla tiene potencia y cadencia, sin aviso de cadencia baja ni datos del intervalo de Zwift

#### Scenario: Sin Zwift
- **WHEN** se elige el estado sin Zwift
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los iconos del Neo y del Square normales, el estado de la plataforma dice «Esperando conexión a Zwift…» y el aviso «+1 marcha»

#### Scenario: Tope en lineal
- **WHEN** se elige el estado de tope en lineal
- **THEN** «24» aparece atenuado con «Tope · marcha más dura» y la escalera tiene resaltado el último segmento

#### Scenario: Square sin conexión
- **WHEN** se elige el estado de Square sin conexión
- **THEN** el icono del Square se ve tachado, la marcha sigue visible, la potencia y la cadencia tienen valor y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado sin dispositivos y se toca la barra de conexiones
- **THEN** los iconos del Neo y del Square se ven tachados, la potencia y la cadencia muestran «—», el pie dice «Toca las conexiones para conectar el Neo y el Square» y la barra abre la maqueta de Dispositivos

#### Scenario: Neo conectando al abrir
- **WHEN** se elige el estado Neo conectando
- **THEN** el icono del Neo está atenuado con la marca girando, el del Square se ve normal, la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo»

#### Scenario: Square reconectando
- **WHEN** se elige el estado Square reconectando
- **THEN** el icono del Square está atenuado con la marca girando, la potencia y la cadencia tienen valor y el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** se elige el estado de los dos conectando
- **THEN** los iconos del Neo y del Square están atenuados con la marca girando, la potencia y la cadencia muestran «—» y el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Ruta de Zwift
- **WHEN** se elige el estado ruta de Zwift
- **THEN** los iconos del Neo y del Square se ven normales, el estado de la plataforma dice «Conectado a Zwift», el modo dice «SIM» con «Lineal · 24 marchas», se ve «14» con «de 24» y la escalera, potencia y cadencia, y no aparece ninguna pendiente

#### Scenario: Entrenamiento ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift en lineal
- **THEN** el modo dice «ERG APP», sin línea explicativa de las palancas, la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el estado de la plataforma dice «Sin wifi para Zwift», el modo dice «LIBRE», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

### Requirement: Barra de conexiones y plataforma en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener la barra de conexiones de los dispositivos (un icono por enlace, agrupados con 4 dp entre ellos, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, y el estado de la publicación en palabras, sin nombre de plataforma, en la misma fila, enlazando con Conexión.

#### Scenario: Enlaces de la barra y de la plataforma
- **WHEN** se toca la barra de conexiones o el estado de la publicación
- **THEN** se abre Dispositivos o Conexión respectivamente

#### Scenario: Estados de la publicación
- **WHEN** se recorren los estados de la maqueta de rodaje
- **THEN** el estado dice «Conectado», «Conectado · sin controlador», «Conectado · sin pulso», «Esperando conexión…», «Sin wifi», «No visible en la red» o «Sin rodillo», y ninguno nombra Zwift

### Requirement: Modo y transmisión en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener el modo y la transmisión en la línea superior, a la izquierda del estado de la publicación (centrado en la línea) en horizontal, y en la fila de debajo, con el icono de Ajustes a su derecha, en vertical. En horizontal, los datos y la marcha SHALL empezar justo bajo la línea superior, sin reservar el sitio de una esquina.

#### Scenario: Modo, transmisión y Ajustes en una línea
- **WHEN** se abre la maqueta de rodaje en vertical
- **THEN** el modo con la transmisión y el icono de Ajustes están en una sola línea

#### Scenario: Modo en la línea superior en horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado, también en ERG
- **THEN** el modo con la transmisión está a la izquierda de la línea superior, el estado de la publicación centrado y, al final, la barra de conexiones con Ajustes, y la zona principal empieza justo debajo

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar, bajo la barra de estado del sistema (dibujada en el marco del dispositivo y no como parte de la pantalla), la fila de la plataforma, con el estado de la publicación a la izquierda y la barra de conexiones a la derecha, y debajo, en una sola fila, el modo con la transmisión y el icono de Ajustes a la derecha. No SHALL tener franja superior propia ni hora, wifi o batería dibujados por la app.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven en la fila de la plataforma los iconos del Neo y del Square normales y el de Zwift atenuado con la marca girando, «Lineal · 24 marchas» con Ajustes a su derecha, «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los iconos afectados se ven tachados, la barra enlaza con Dispositivos, y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los iconos afectados están atenuados con la marca girando, el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con la barra de conexiones en la fila de la plataforma, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

#### Scenario: Peor caso de la fila de la plataforma
- **WHEN** se elige el estado «Conectado · sin controlador» con los cuatro iconos de conexión a 360 dp de ancho
- **THEN** el estado se lee entero, en una o dos líneas, y los cuatro iconos se ven enteros y juntos a su derecha, sin solaparse

#### Scenario: Botones y seis datos en vertical
- **WHEN** se elige el estado con botones − y + en vertical
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento, con la misma cifra que en el estado de 6 datos sin botones

### Requirement: Cuerpo y estados de la maqueta de rodaje en vertical
Bajo esas líneas, la maqueta vertical SHALL apilar marcha con plato y cassette, rejilla de datos a 2 columnas en su área fija, aviso y, si están activados, botones − y + al pie, sin velocidad ni distancia. SHALL tener también los estados lineales de la maqueta horizontal, sin Zwift y con Zwift, con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

#### Scenario: Estado lineal en vertical
- **WHEN** se abre la maqueta vertical en un estado lineal
- **THEN** se ve la marcha con su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette

### Requirement: Navegación entre pantallas
Las pantallas de pestaña (Conexión, Dispositivos y Ajustes) SHALL compartir la navegación inferior (Rodar, Conexión, Dispositivos, Ajustes), sin botón de volver; Rodaje SHALL NOT mostrarla. Las secundarias SHALL tener un botón de volver a su origen y no SHALL mostrar la navegación inferior: Botones, Marchas, Datos en pantalla y Diagnóstico vuelven a Ajustes, y Asignar acción a Botones. Los enlaces SHALL funcionar entre maquetas, sin estados «abierta desde Diagnóstico».

#### Scenario: De Ajustes a Marchas y vuelta
- **WHEN** en Ajustes se toca «Marchas» y luego volver
- **THEN** se abre la maqueta de Marchas y después se vuelve a Ajustes

#### Scenario: Botones desde Ajustes
- **WHEN** en la maqueta de Ajustes se toca «Botones» y luego volver
- **THEN** se abre la maqueta de Botones sin navegación inferior y después se vuelve a Ajustes

#### Scenario: Diagnóstico desde Ajustes
- **WHEN** en la maqueta de Ajustes se toca «Diagnóstico» y luego volver
- **THEN** se abre la maqueta de Diagnóstico sin navegación inferior y después se vuelve a Ajustes

#### Scenario: Pestañas
- **WHEN** se toca cada pestaña de la navegación inferior desde Conexión, Dispositivos o Ajustes
- **THEN** se abre su maqueta con esa pestaña marcada, y «Rodar» abre la maqueta de rodaje sin navegación inferior

### Requirement: Asignar acción
La maqueta de Asignar acción SHALL mostrar las entradas de la acción, la zona de captura y el gesto, con los estados esperando, Square sin conexión, combinación libre, entrada ya asignada, función de Zwift y botón que usa Zwift con el mando desactivado.

#### Scenario: Square sin conexión al asignar
- **WHEN** en Asignar acción se elige el estado Square sin conexión
- **THEN** la zona de captura dice «Conecta el Square para capturar un botón» y «Guardar» está deshabilitado

#### Scenario: Botón que usa Zwift
- **WHEN** en Asignar acción se elige el estado botón que usa Zwift con el mando desactivado
- **THEN** se ve A capturada y un aviso de que Zwift también la recibirá con el Square emparejado

#### Scenario: Función de Zwift
- **WHEN** en Asignar acción se elige el estado función de Zwift
- **THEN** se ve Asignar acción de «Ride On» con Z como entrada, Campag. D + △ capturada con «pulsar» y el aviso de que está libre

### Requirement: Marchas
La maqueta de Marchas SHALL mostrar la elección de transmisión (Lineal, por defecto, y Shimano 105 2×12 · 52/36 · 11–36), los platos y el cassette de la Shimano con la referencia resaltada, el modo de cambio («Automático», por defecto, y «Manual», con su ayuda), la sección «Al empezar» con la marcha de referencia y «Recordar la última marcha», y la simulación avanzada plegada. SHALL tener los estados lineal, Shimano automático, Shimano manual y simulación avanzada abierta.

#### Scenario: Transmisión lineal
- **WHEN** se elige el estado de transmisión lineal
- **THEN** se ve la escalera de 24 marchas y el número de marchas, y no aparece el modo de cambio con platos

#### Scenario: Shimano automático
- **WHEN** se elige el estado Shimano automático
- **THEN** está elegida «Shimano 105 2×12», se ven los platos 36 y 52 y el cassette del 36 al 11 con la 36×17 resaltada, «Automático» elegido con su ayuda y la marcha de referencia «36×17»

#### Scenario: Shimano manual
- **WHEN** se elige el estado Shimano manual
- **THEN** está elegido «Manual», con la ayuda de que las palancas D cambian el piñón y las I el plato

#### Scenario: Solo dos transmisiones
- **WHEN** se recorren los estados de Marchas
- **THEN** no aparecen SRAM, Campagnolo ni «A medida» como opciones

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas, Botones y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro», y «Orientación», con «Según el móvil», «Vertical» y «Horizontal») y Ciclista (peso del ciclista y peso de la bici), con la navegación inferior.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el paso ERG, «Mantener la pantalla encendida», el aviso de cadencia baja, «Botones y gestos» ni la elección de wifi o Bluetooth para Zwift

#### Scenario: Modelos en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=simple`, `#estado=tabla` y `#estado=fisico`
- **THEN** «Modelo de resistencia» muestra elegido «Simple», «Por tabla» y «Físico», respectivamente, y «Tabla de marchas» solo aparece en el segundo y «Potencia mínima» solo en el tercero

#### Scenario: Tema en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio` y `maquetas/ajustes.html#estado=claro&tema=claro`
- **THEN** la sección «Pantalla», entre «Al rodar» y «Ciclista», muestra «Tema» con «Oscuro» elegido en el primer enlace y con «Claro» elegido, y la maqueta en claro, en el segundo

#### Scenario: Orientación en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`, `maquetas/ajustes.html#estado=vertical` y `maquetas/ajustes.html#estado=horizontal`
- **THEN** la sección «Pantalla» muestra, debajo de «Tema», «Orientación» con sus tres opciones y su ayuda, con «Según el móvil» elegido en el primer enlace, «Vertical» en el segundo y «Horizontal» en el tercero, y «Tema» con «Oscuro» elegido en los tres

#### Scenario: Orientación en tema claro
- **WHEN** se abre `maquetas/ajustes.html#estado=horizontal&tema=claro`
- **THEN** «Orientación» se ve con «Horizontal» elegido, distinguido por la marca de selección, con el contraste del tema claro

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6», están marcados seis, la vista previa los muestra todos y las casillas de los tres sin marcar están deshabilitadas con su ayuda

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

#### Scenario: Velocidad y distancia en Datos en pantalla
- **WHEN** se abre `maquetas/datos.html#estado=velocidad`
- **THEN** la lista tiene nueve datos, la vista previa muestra velocidad y distancia y sus ayudas explican que la velocidad la calcula el modelo físico

#### Scenario: Tabla de marchas
- **WHEN** se abre `maquetas/tabla-marchas.html#estado=lineal` y `#estado=shimano`
- **THEN** se ven 24 filas con su ajuste y su paso a paso, la marcha puesta marcada y «Restablecer», en los dos temas

#### Scenario: Mando de Zwift en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la entrada «Botones», con el icono de los cuatro símbolos y la transmisión de la asignación, va entre «Marchas» y «Diagnóstico», y no hay sección «Zwift»

### Requirement: Sección Rodillo en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL tener «Modelo de resistencia», con «Simple», «Por tabla» y «Físico», «Tabla de marchas» con «Por tabla», «Dificultad», «Potencia mínima» con «Físico» y «Bajadas», sin «Pendiente sin Zwift», y la maqueta SHALL NOT tener sección «Zwift».

#### Scenario: Opciones de Rodillo
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la sección Rodillo muestra el modelo de resistencia y sus opciones, sin «Pendiente sin Zwift», y no hay sección «Zwift»

### Requirement: Maquetas de Datos en pantalla y Tabla de marchas
La maqueta de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad, distancia y pendiente, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos. La de Tabla de marchas SHALL mostrar la lista de marchas con su ajuste editable en los estados de la lineal y de la Shimano.

#### Scenario: Estados de datos y de marchas
- **WHEN** se abre la maqueta de Datos en pantalla con 4 datos
- **THEN** se ven los 4 datos elegidos, su orden y la vista previa

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver a Ajustes, la sección «Datos de conexión» (solo datos, sin estado de conexión ni acciones de búsqueda), los botones del Square con la máscara, el último gesto con su acción y la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al rodillo, la latencia de la palanca y, con Zwift por wifi, lo último que envía su simulación de marchas virtuales). SHALL NOT enlazar con Botones ni con Ajustes salvo por el botón de volver.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** «Datos de conexión» muestra solo las filas con datos, hay un botón de volver a Ajustes, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Sin estado de conexión
- **WHEN** se recorren todos los estados de la maqueta de Diagnóstico
- **THEN** ninguno muestra filas de estado del rodillo, el Square, Zwift o el controlador, ni «Buscar dispositivos», ni avisos de conexión

#### Scenario: Enlace a Botones
- **WHEN** se recorre la sección de botones del Square de la maqueta de Diagnóstico
- **THEN** no hay enlace «Asignación de botones»: Botones se abre desde la entrada «Botones» de Ajustes

#### Scenario: Tráfico al abrir
- **WHEN** se abre la maqueta de Diagnóstico en cualquier estado
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar», y al marcarla aparecen el volcado del estado y los dos botones

### Requirement: Tráfico y control manual en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar también el tráfico con la casilla «Tráfico en directo», desmarcada al abrir, que al marcarla muestra el volcado con pausar y limpiar, y el control manual con el último envío.

#### Scenario: Tráfico en directo en la maqueta
- **WHEN** se marca «Tráfico en directo»
- **THEN** aparece el volcado con pausar y limpiar

### Requirement: Estados de la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL conservar los estados que ya tenía (todo conectado por defecto, buscando, conectando al abrir, rodillo no encontrado, Square reconectando, Bluetooth apagado, sin permiso, límites del control manual, rodando con Zwift, ruta y ERG de Zwift en lineal, sin wifi y los de batería, pulso, ventilador, nombre ante la app y Solo Bluetooth) y SHALL añadir un estado con todos los datos a la vez, de modo que los enlaces y las capturas no cambien, y cada uno SHALL pintar en «Datos de conexión» solo sus datos, y en las demás secciones lo que ya pintaba.

#### Scenario: Dispositivo no encontrado
- **WHEN** se elige el estado rodillo no encontrado
- **THEN** no hay aviso ni fila del rodillo, los botones de envío al rodillo están deshabilitados con su explicación y «Enviada al rodillo» muestra «—»

#### Scenario: Conectando al abrir
- **WHEN** se elige el estado conectando al abrir
- **THEN** no hay filas del rodillo ni del Square ni ayuda para despertarlos, ni «Buscar dispositivos», y las demás secciones muestran lo que corresponde sin dispositivos

#### Scenario: Conexión perdida
- **WHEN** se elige el estado Square reconectando
- **THEN** no hay aviso de reconexión, ninguna tecla se ve pulsada, la máscara dice «—» y la sección de botones dice «Conecta el Square para ver sus botones.»

#### Scenario: Bluetooth y permisos
- **WHEN** se eligen los estados Bluetooth apagado y sin permiso
- **THEN** no hay avisos ni botones «Activar Bluetooth» o «Abrir ajustes», y las demás secciones muestran lo que corresponde sin Bluetooth

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado

#### Scenario: Zwift esperando
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** la fila «App de entrenamiento» dice «—», sin la ayuda de emparejamiento, y «Nombre anunciado» dice «Smart Bike»

#### Scenario: Simulación de Zwift
- **WHEN** se elige el estado de todo por wifi con la simulación de Zwift
- **THEN** la fila «Simulación de Zwift» está en la sección Simulación, con la pendiente, la relación de su marcha, Crr, CW, las masas y la hora, y «Datos de conexión» no la tiene

#### Scenario: Ruta de Zwift en lineal
- **WHEN** se elige el estado ruta de Zwift
- **THEN** la fila «App de entrenamiento» dice la dirección del PC, la simulación dice modo SIM · Zwift con la pendiente y el terreno de Zwift, el ajuste de la marcha, lo enviado al rodillo y el último mensaje de Zwift, y, al marcar «Tráfico en directo», el tráfico tiene filas «Zwift» y «→ Zwift»

#### Scenario: ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la simulación dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al rodillo 240 W, con la marcha visible

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** la fila «App de entrenamiento» dice «—», no hay aviso y «Nombre anunciado» muestra el nombre que se anunciará

#### Scenario: Batería del Square
- **WHEN** se eligen los estados de batería (80 %, sin lectura, baja y Square reconectando con la última conocida)
- **THEN** la fila de batería dice «80 %», «Sin lectura todavía», «15 % · baja» y «Última lectura: 60 %», sin avisos: el aviso de batería baja está en Dispositivos

#### Scenario: Todos los datos a la vez
- **WHEN** se elige el estado de todos los datos
- **THEN** «Datos de conexión» muestra a la vez la app de entrenamiento («Desde 192.168.0.10»), el nombre anunciado, la batería del Square («15 % · baja»), el pulso («128 bpm») y el ventilador («Manual · 40 %»), la simulación de Zwift está en Simulación, y todo se ve a 360 dp sin desplazamiento horizontal

#### Scenario: Pulso y ventilador
- **WHEN** se eligen los estados del pulsómetro y del ventilador
- **THEN** sus filas dicen el pulso o el modo y la velocidad con su antigüedad, o «—» sin lectura o sin notificación, sin estado de conexión ni avisos

### Requirement: Ajuste «Bajadas» en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL incluir el ajuste «Bajadas» con sus tres opciones y su ayuda.

#### Scenario: Ajustes completa
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección Rodillo muestra «Bajadas» con «Sin protección», «La marcha no crea bajadas» (elegida) y «Nunca empujar», cada una con su ayuda

#### Scenario: Desde Diagnóstico
- **WHEN** en la maqueta de Diagnóstico se toca volver
- **THEN** se abre la maqueta de Ajustes con la sección Rodillo y el ajuste «Bajadas», con la navegación inferior

### Requirement: Pendiente limitada en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar en Simulación el modo de bajadas y SHALL tener un estado con la pendiente limitada por él, con la indicación en texto y la pendiente sin límite, y SHALL tener el acceso a Ajustes junto al de Botones.

#### Scenario: Bajada de Zwift limitada
- **WHEN** se elige el estado de pendiente limitada
- **THEN** Simulación muestra pendiente Zwift −7,0 %, ajuste −11,0 %, enviada al rodillo −7,0 % y «Limitada por Bajadas: sin límite sería −18,0 %»

#### Scenario: Sin límite
- **WHEN** se elige cualquier otro estado con el rodillo conectado
- **THEN** Simulación muestra el modo de bajadas y no aparece la indicación de límite

### Requirement: Shimano 105 en la maqueta de rodaje
Las maquetas de rodaje horizontal y vertical SHALL tener estados con la Shimano 105 (52/36 · 11–36) y potencia y cadencia: automático sin Zwift en la 52×17 con «+1 marcha», cambio de plato automático con «Plato 52» en la 52×19 y, solo en horizontal, el tope de plato en manual. Los estados de marca que ya existían SHALL usar el desarrollo 52/36 · 11–36 en lugar del 50/34 · 11–34.

#### Scenario: Automático sin Zwift
- **WHEN** se elige el estado Shimano automático, en horizontal o en vertical
- **THEN** la barra superior dice «LIBRE» y «Shimano 105 · automático», se ve «52×17» con el plato 52 relleno y el cassette del 36 al 11 con el 17 resaltado, potencia y cadencia, y el pie dice «+1 marcha»

#### Scenario: Cambio de plato
- **WHEN** se elige el estado de cambio de plato
- **THEN** se ve «52×19» con el plato 52 relleno y el pie dice «Plato 52»

#### Scenario: Tope de plato en manual
- **WHEN** se elige el estado de tope de plato
- **THEN** la barra superior dice «Shimano 105 · manual», «52×17» se ve atenuado y debajo «Tope · plato 52»

#### Scenario: Shimano en vertical sin desplazamiento
- **WHEN** se eligen los estados Shimano en vertical a 360 dp de ancho
- **THEN** la marcha, los platos, el cassette, la potencia y la cadencia se ven sin desplazamiento, y al pasar de un estado Shimano a otro no se mueve nada

### Requirement: Botones con la asignación de la Shimano
La maqueta de Botones SHALL tener un estado con la asignación por defecto de la Shimano, con la línea que dice de qué transmisión es y las acciones de plato tras «−3 marchas».

#### Scenario: Asignación de la Shimano
- **WHEN** se elige el estado de la Shimano
- **THEN** se lee que es la asignación de Shimano 105 y que cada transmisión tiene la suya, «+1 marcha» con la palanca D2, «−1 marcha» con la palanca D1, «Plato más grande» con la palanca I1 y «Plato más pequeño» con la palanca I2

#### Scenario: Asignación de la lineal
- **WHEN** se elige el estado de asignación por defecto
- **THEN** se lee que es la asignación de la lineal y no aparecen las acciones de plato

### Requirement: Diagnóstico con la Shimano
La maqueta de Diagnóstico SHALL tener un estado con la Shimano en el que Simulación muestra la marcha como plato × piñón con su relación y el ajuste por la marcha.

#### Scenario: Simulación con la Shimano
- **WHEN** se elige el estado de la Shimano
- **THEN** Simulación dice marcha 52×17 · relación 3,06, ajuste +5,1 % y enviada al rodillo +5,1 %, y «Último gesto» dice «Palanca D2 · pulsar · +1 marcha»

### Requirement: Área de datos fija en las maquetas de rodaje
Las maquetas de rodaje, horizontal y vertical, SHALL reproducir el área fija de datos (40 % del alto útil en vertical, 40 % del ancho útil en horizontal), la marcha reducida para caber en el resto, el espacio de los botones − y + tomado primero de la holgura de la rejilla, la cifra medida con el valor más largo de cada dato y el último dato impar centrado. SHALL tener estados de 1 a 6 datos, con y sin botones, en las dos orientaciones.

#### Scenario: Con y sin botones
- **WHEN** se alterna entre el estado de 5 datos sin botones y el de 5 datos con botones, en vertical o en horizontal
- **THEN** la cifra de los datos es la misma en los dos y solo cambian el alto o el ancho de la rejilla y, si hace falta, el tamaño de la marcha

#### Scenario: Último dato impar
- **WHEN** se elige un estado de 3 o 5 datos en vertical, o de 5 en horizontal
- **THEN** el último dato va centrado en el ancho de la rejilla

#### Scenario: Filas alineadas
- **WHEN** se elige el estado de 6 datos
- **THEN** la etiqueta de cada dato queda a la misma distancia del borde de su celda en todas las filas, tenga unidad o no

#### Scenario: Transmisión y modo
- **WHEN** se pasa del estado lineal al de la Shimano o al de ERG con el mismo número de datos
- **THEN** la rejilla de datos no cambia de área, de posición ni de cifra

### Requirement: Acceso a Ajustes en las maquetas de rodaje
Todos los estados de las maquetas de rodaje, en horizontal y en vertical, SHALL mostrar el botón con el icono de Ajustes, enlazado con la maqueta de Ajustes, con descripción accesible «Ajustes» y objetivo táctil de `touch-min`, en los dos temas: a la derecha de la barra de conexiones en horizontal, y al final de la fila del modo (SIM o ERG) en vertical.

#### Scenario: Horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado
- **THEN** el icono de Ajustes está en la línea superior, a la derecha de la barra de conexiones y separado de ella 12 dp, y al tocarlo se abre `maquetas/ajustes.html`

#### Scenario: Vertical a 360 dp
- **WHEN** se abre `maquetas/rodaje-vertical.html` en el estado «Sin Zwift · Neo y Square conectando» con el marco a 360 dp de ancho
- **THEN** la barra de conexiones queda a la derecha de la fila de la plataforma, y debajo caben en una sola fila el modo con la transmisión y el icono de Ajustes al final

#### Scenario: Vertical en ERG
- **WHEN** se abren los estados de ERG de Zwift y de ERG manual en vertical
- **THEN** el icono de Ajustes está al final de la fila del modo («ERG APP» o «ERG MANUAL») y no empuja ni tapa su texto

#### Scenario: Rodaje en claro
- **WHEN** se abre `maquetas/rodaje.html#tema=claro` y `maquetas/rodaje-vertical.html#tema=claro`
- **THEN** la pantalla de rodaje y el icono de Ajustes se ven con los valores claros de los tokens y el icono se distingue del fondo

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado «Sin Zwift · sin dispositivos»
- **THEN** el icono de Ajustes sigue en su sitio y la barra muestra los iconos del Neo y del Square tachados

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el controlador esperando, con el estado de la publicación en «Conectado · sin controlador» y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el controlador esperando, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin controlador», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Mando de Zwift en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar, en Conexiones, la fila del mando de Zwift bajo la de Zwift, con «Conectado» en los estados con Zwift conectado y «Esperando» en los que Zwift espera.

#### Scenario: Mando en Diagnóstico
- **WHEN** se elige el estado rodando con Zwift de la maqueta de Diagnóstico
- **THEN** bajo la fila de Zwift se ve la del mando con «Conectado»

### Requirement: Zwift no visible en las maquetas de rodaje
Las maquetas de rodaje y de rodaje en vertical SHALL tener el estado no visible, con el estado de la publicación en «No visible en la red» con el borde de alerta, y el resto como en el estado sin app de entrenamiento.

#### Scenario: Rodaje con Zwift no visible
- **WHEN** se elige el estado no visible en la maqueta de rodaje, en horizontal o en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «No visible en la red» con el aro vacío y el borde de alerta y enlaza con Conexión, la barra enlaza con Dispositivos, y el resto se ve como sin app de entrenamiento, sin moverse

### Requirement: Mando sin conexión en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el controlador sin conexión, con el estado de la publicación en «Conectado · sin controlador», y otro con el pulso sin elegir, en «Conectado · sin pulso», con el resto como en la ruta de Zwift.

#### Scenario: Mando sin conexión en rodaje
- **WHEN** se elige el estado ruta de Zwift con el controlador sin conexión, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin controlador», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

#### Scenario: Pulso sin elegir en rodaje
- **WHEN** se elige el estado ruta de Zwift con el pulso sin elegir, en horizontal y en vertical
- **THEN** los iconos se ven normales, el estado dice «Conectado · sin pulso», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar los iconos de los enlaces: Dispositivos, junto al nombre del Neo y del Square, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas; Diagnóstico, en «Datos de conexión», el icono de Conexión (genérico, no el de ninguna plataforma) en las filas de la app de entrenamiento y del nombre anunciado y, si las lleva, los de las filas del pulsómetro, el ventilador y el Square, sin filas del Neo ni del Square como dispositivos; el icono de Zwift solo en la simulación de Zwift.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo y el manillar para el Square, y no hay filas de Zwift ni del controlador publicado

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** la app de entrenamiento y el nombre anunciado llevan el icono de Conexión y no el de Zwift, que solo lleva la «Simulación de Zwift», y no hay filas del rodillo ni del Square como dispositivos

### Requirement: Velocidad y distancia en las maquetas de rodaje
Las maquetas de rodaje, en vertical y en horizontal, SHALL tener estados con velocidad y distancia entre los datos, también con «—» en la velocidad, sin cambiar el tamaño de la cifra ni el área de datos fija.

#### Scenario: Velocidad y distancia
- **WHEN** se abre `maquetas/rodaje.html#estado=velocidad` y `maquetas/rodaje-vertical.html#estado=velocidad`
- **THEN** la rejilla muestra potencia, cadencia, velocidad y distancia con la misma cifra y sin desplazamiento

#### Scenario: Velocidad sin valor
- **WHEN** se abre `maquetas/rodaje.html#estado=velocidad-sin-valor`
- **THEN** la velocidad muestra «—», la distancia conserva su valor y la marcha sigue visible

### Requirement: Modelo en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener un estado con el modelo físico que muestre la potencia calculada, la mínima aplicada, la velocidad y la indicación de límite por la potencia mínima.

#### Scenario: Físico
- **WHEN** se abre `maquetas/diagnostico.html#estado=fisico`
- **THEN** la sección Simulación muestra el modelo Físico con sus valores y, en otro estado, la limitación por la potencia mínima con texto

### Requirement: Maqueta de identidad
Las maquetas SHALL incluir una pantalla de identidad con el icono de la app en el launcher (con máscara circular y de cuadrado redondeado y en monocromo), la splash con su animación y su estado final, y el icono de Botones en la barra inferior, con un estado por cada caso, en los dos temas y enlazada desde el índice.

#### Scenario: Ver el icono en el launcher
- **WHEN** se abre la maqueta de identidad en el estado del icono
- **THEN** se ve la bici con las máscaras de Android y a 48 dp, en tema rodaje y en tema claro

#### Scenario: Ver la splash
- **WHEN** se abre la maqueta de identidad en el estado de la splash
- **THEN** se ve la rueda trasera ámbar girando cada vez más rápido y quedándose en la posición del icono, sobre fondo oscuro también en tema claro, y con movimiento reducido se ve solo el estado final

#### Scenario: Icono monocromo
- **WHEN** se abre la maqueta de identidad en el estado monocromo
- **THEN** el icono se ve en un solo color y se distingue cuál es la rueda trasera

### Requirement: Simulación de Zwift en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener un estado con la simulación de marchas virtuales de Zwift en la sección Simulación, con la pendiente, los coeficientes, las masas, la relación de la marcha de Zwift y la hora, y SHALL NOT mostrarla en «Datos de conexión».

#### Scenario: Simulación en Diagnóstico
- **WHEN** se elige el estado de simulación de Zwift de la maqueta de Diagnóstico
- **THEN** en la sección Simulación se ve la pendiente de Zwift y la relación de su marcha, y aparte lo que simula el rodillo con la marcha de la app

#### Scenario: Sin simulación
- **WHEN** se elige el estado rodando con Zwift de la maqueta de Diagnóstico
- **THEN** no aparece la línea de la simulación de Zwift

### Requirement: Maqueta de Dispositivos de la app
La maqueta de Dispositivos SHALL mostrar una sección por tipo (Rodillo, Controlador, Pulsómetro y Ventilador), cada una con su fila (modelo configurado o «Ninguno configurado», estado en palabra y ayuda), «Buscar» sin configurado, u «Olvidar» y, sin conexión, «Reconectar», y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Neo no encontrado, Square no encontrado, Square con la batería baja, sin permiso de Bluetooth, Bluetooth apagado y los del pulsómetro y el ventilador. SHALL NOT mostrar lo publicado ni «Buscar dispositivos».

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** las cuatro filas dicen «Ninguno configurado», cada sección tiene «Buscar» y ninguna tiene «Olvidar»

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** las filas Rodillo y Controlador dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo, y tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Neo no encontrado
- **WHEN** se elige el estado Neo no encontrado
- **THEN** la fila Rodillo dice «No encontrado» y se ve el aviso de error «No se encuentra el Neo. Comprueba que está enchufado, pedalea para despertarlo y vuelve a buscar.»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila Controlador dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Sin permiso de Bluetooth
- **WHEN** se elige el estado sin permiso de Bluetooth
- **THEN** se ve el aviso de error «Sin permiso para buscar dispositivos Bluetooth. Concédelo en los ajustes de la app.» con el botón «Abrir ajustes», y las filas configuradas dicen «Sin conexión»

#### Scenario: Batería baja del Square
- **WHEN** se elige el estado Square con la batería baja
- **THEN** la fila Controlador dice «Conectado» y, bajo ella, el aviso informativo «Batería del Square baja (15 %). Revisa la pila antes de rodar.» con icono y texto, y la fila conserva «Olvidar»

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y las filas configuradas dicen «Sin conexión»

#### Scenario: Sin Zwift
- **WHEN** se revisan todos los estados de la maqueta de Dispositivos
- **THEN** ninguno muestra una fila de Zwift ni de lo publicado

#### Scenario: Botones sin el nombre del tipo
- **WHEN** se revisan los botones de cualquier sección
- **THEN** dicen «Buscar», «Reconectar» u «Olvidar» según el estado, y su etiqueta accesible nombra el tipo («Reconectar controlador»)

### Requirement: Maqueta de Botones desde Ajustes
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift y acciones de la app, cada uno con una fila por función o acción con sus botones y gestos y enlace a Asignar acción, los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», sin navegación inferior y con un botón de volver a Ajustes. Sus estados SHALL ser asignación por defecto, asignación modificada, sin Square configurado y confirmar la restauración.

#### Scenario: Volver a Ajustes
- **WHEN** en la maqueta de Botones se toca volver
- **THEN** se abre la maqueta de Ajustes con la navegación inferior

#### Scenario: Funciones de Zwift primero
- **WHEN** se abre la maqueta de Botones
- **THEN** el primer grupo tiene una fila por función de un mando oficial de Zwift (↑, ↓, ←, →, A, B, Y, Z, girar a la izquierda y a la derecha, y los dos frenos), cada una con su botón y enlace a Asignar acción, y dice que llegan por el mando de la app

#### Scenario: Asignación modificada
- **WHEN** se elige el estado de asignación modificada
- **THEN** una acción aparece con «Sin botón», otra con una combinación nueva, una función de Zwift con «Sin botón» y «Invertir palancas» activado

#### Scenario: Acciones de pantalla
- **WHEN** se elige el estado de asignación por defecto
- **THEN** tras «Alternar SIM ⇄ ERG» aparecen «Pantalla anterior» con Campag. I + cruceta ← y «Pantalla siguiente» con Campag. I + cruceta →

#### Scenario: Mando desactivado en Botones
- **WHEN** se recorren los estados de la maqueta de Botones
- **THEN** no hay estado de mando desactivado: su lugar lo ocupa el estado sin Square configurado

#### Scenario: Sin Square configurado en Botones
- **WHEN** se elige el estado sin Square configurado
- **THEN** el grupo de Zwift dice que el mando no se publica hasta configurar un Square, con sus filas iguales

### Requirement: Iconos de la barra inferior de las maquetas
Las maquetas con barra inferior SHALL mostrar la pestaña Conexión con su icono propio, distinto del de Dispositivos, y la entrada «Botones» de Ajustes con el icono de los cuatro símbolos; el mando SHALL conservar su icono en las maquetas donde aparece.

#### Scenario: Barra inferior de las maquetas
- **WHEN** se abre Conexión, Dispositivos o Ajustes en la maqueta
- **THEN** la barra tiene Rodar, Conexión, Dispositivos y Ajustes, la de Conexión con su icono propio

#### Scenario: Entrada de Botones
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la entrada «Botones» lleva el icono de los cuatro símbolos

### Requirement: Maqueta de Diagnóstico con el modo por Bluetooth
La maqueta de Diagnóstico SHALL tener estados del modo por Bluetooth, con una app de entrenamiento conectada y con el Bluetooth apagado, que muestren en «Datos de conexión» la fila «App de entrenamiento» con «Por Bluetooth» o «—» y el nombre Bluetooth del móvil en lugar del nombre anunciado, sin ningún aviso de wifi ni de Bluetooth.

#### Scenario: Zwift conectado por Bluetooth
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-rodando`
- **THEN** la fila «App de entrenamiento» dice «Por Bluetooth», el nombre anunciado es el nombre Bluetooth del móvil y no hay aviso de wifi

#### Scenario: Bluetooth apagado
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-sin-bluetooth`
- **THEN** la fila «App de entrenamiento» dice «—» y no hay aviso de Bluetooth apagado: está en Dispositivos y en Conexión

### Requirement: Maqueta de Conexión
La maqueta `maquetas/conexion.html` SHALL mostrar la introducción, los avisos, la sección de estado (rodillo, controlador Zwift y pulso con por dónde van, su estado y su ayuda), la sección de modo con los tres modos, su detalle y su compatibilidad, «Nombre ante la app» según el modo (con wifi, el selector; con Bluetooth, la casilla «Usar el nombre también por Bluetooth» y, con «Solo Bluetooth», el selector solo con la casilla marcada) y la navegación inferior con Conexión marcada.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/conexion.html#estado=inicio`
- **THEN** se ve «Wifi + Bluetooth» elegido y recomendado, «Smart Bike» elegido, la casilla «Usar el nombre también por Bluetooth» desmarcada, el rodillo, el controlador Zwift y el pulso «Esperando», sin interruptor del controlador ni elección de plataforma, y la pestaña Conexión marcada

#### Scenario: Orden de la pantalla
- **WHEN** se abre cualquier estado de la maqueta de Conexión
- **THEN** de arriba abajo se ven la introducción, los avisos si los hay, el estado, el modo y «Nombre ante la app», con el selector y la casilla según el modo

### Requirement: Estados de la maqueta de Conexión
La maqueta de Conexión SHALL tener los estados: todo conectado; rodillo conectado con controlador y pulso esperando; «Solo Bluetooth»; «Solo Bluetooth» con el nombre por Bluetooth; «Wifi + Bluetooth» con el nombre por Bluetooth; no se pudo renombrar; «Solo wifi (modo Zwift Ride)»; sin pulsómetro; sin Square; primer uso sin rodillo; pulso sin lectura; Neo y Square reconectando; KICKR BIKE PRO; no visible; sin wifi; Bluetooth apagado; y móvil sin modo periférico.

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el rodillo y el controlador dicen «Conectado» y el pulso «Enviando», sin mostrar los bpm

#### Scenario: Servicios no ofrecidos
- **WHEN** se eligen los estados sin pulsómetro y sin Square
- **THEN** el pulso o el controlador dicen «No se ofrece», atenuados, con qué dispositivo falta y el enlace a Dispositivos

#### Scenario: Primer uso
- **WHEN** se elige el estado primer uso sin rodillo
- **THEN** se ve el aviso de que la app no publica nada hasta configurar un rodillo y las tres filas dicen «No se ofrece»

#### Scenario: Dispositivos reconectando
- **WHEN** se elige el estado Neo y Square reconectando
- **THEN** el rodillo y el controlador siguen «Conectado» y cada uno añade que su dispositivo se está reconectando

#### Scenario: Solo Bluetooth
- **WHEN** se elige el estado «Solo Bluetooth»
- **THEN** el modo está elegido sin la marca de recomendado, «Nombre ante la app» muestra solo la casilla desmarcada, los tres servicios dicen «por Bluetooth» y las ayudas nombran el móvil por su nombre Bluetooth

#### Scenario: Solo Bluetooth con el nombre por Bluetooth
- **WHEN** se elige el estado «Solo Bluetooth» con el nombre por Bluetooth
- **THEN** la casilla está marcada, el selector muestra «Smart Bike», la ayuda de la casilla nombra «Smart Bike» y «OnePlus 8», y las ayudas del rodillo y del controlador dicen que se elige «Smart Bike»

#### Scenario: Wifi + Bluetooth con el nombre por Bluetooth
- **WHEN** se elige el estado «Wifi + Bluetooth» con el nombre por Bluetooth
- **THEN** la casilla está marcada y la ayuda del controlador dice que en la lista de mandos se elige «Smart Bike»

#### Scenario: No se pudo renombrar
- **WHEN** se elige el estado no se pudo renombrar
- **THEN** la casilla está marcada, debajo se ve el aviso de que no se pudo cambiar el nombre y se usa «OnePlus 8», con icono y texto, y las ayudas nombran «OnePlus 8»

#### Scenario: Solo wifi
- **WHEN** se elige el estado «Solo wifi (modo Zwift Ride)»
- **THEN** los tres servicios dicen «por wifi», el controlador dice que va dentro del mismo dispositivo, la descripción del modo dice que la app de entrenamiento mostrará su propia marcha y no se ve la casilla

#### Scenario: No visible y sin wifi
- **WHEN** se eligen los estados no visible y sin wifi
- **THEN** arriba se ve el aviso correspondiente y el rodillo dice «No visible» o «Sin wifi» con el aro vacío y el borde de alerta

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado con «Wifi + Bluetooth»
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth», el controlador dice «Sin Bluetooth» y el rodillo conserva su estado por wifi

#### Scenario: Acceso desde Rodaje
- **WHEN** en las maquetas de rodaje se toca el estado de la publicación
- **THEN** se abre la maqueta de Conexión, y volver abre Rodaje

### Requirement: Teclas dibujadas en las maquetas
Las maquetas de Diagnóstico, Botones y Asignar acción SHALL mostrar los botones de símbolo y de cruceta del Square con las teclas dibujadas, en todos sus estados y en los dos temas, y la guía del sistema de diseño SHALL mostrar los ocho símbolos junto a las letras y los nombres, en reposo y pulsados.

#### Scenario: Diagnóstico con la cruceta y los símbolos
- **WHEN** se abre `maquetas/diagnostico.html#estado=rodando`
- **THEN** la sección «Botones del Square» muestra ↑ ↓ ← → alineadas y △ ○ □ ✕ con la misma proporción, y la tecla de Palanca D1 pulsada

#### Scenario: Botones y Asignar acción
- **WHEN** se abren las maquetas de Botones y de Asignar acción con una combinación que lleva un símbolo (Campag. D + △)
- **THEN** la tecla del triángulo es el icono dibujado, junto a «Campag. D» como texto

#### Scenario: Guía de las teclas
- **WHEN** se abre la sección SquareKey de la guía del sistema de diseño
- **THEN** se ven los ocho símbolos dibujados, las cuatro letras y las teclas anchas, en reposo y pulsadas, en los dos temas

#### Scenario: Panel de captura
- **WHEN** se abre `maquetas/asignar.html#estado=detectado`
- **THEN** las teclas capturadas de 48 dp muestran el icono del símbolo al tamaño de la captura

### Requirement: Maqueta de Sesiones grabadas
La maqueta de Sesiones grabadas SHALL mostrar, como secundaria de Ajustes (botón de volver y sin barra inferior), la ayuda sobre el contenido de los archivos, el estado de la grabación, la lista de sesiones con fecha, hora y tamaño, y «Borrar todas». Cada sesión SHALL ofrecer abrir, compartir y borrar con objetivos táctiles de al menos 48 dp.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/sesiones.html#estado=inicio`
- **THEN** se ve una sesión «Grabando» la primera, sin acción de borrar, seguida de sesiones cerradas, cada una con su fecha, su hora, su tamaño y las acciones de abrir, compartir y borrar, la ayuda sobre el contenido y «Borrar todas»

#### Scenario: Sin sesiones
- **WHEN** se abre `maquetas/sesiones.html#estado=vacia`
- **THEN** se ve el estado vacío con cómo grabar sesiones y sin «Borrar todas»

#### Scenario: Detenida por poco espacio y por un error
- **WHEN** se eligen los estados de parada por poco espacio y por un error
- **THEN** la sesión afectada dice «Detenida por poco espacio» o «Detenida por un error» y la parte superior explica cómo reanudar la grabación

#### Scenario: Estados disponibles
- **WHEN** se abre el selector de estado de `maquetas/sesiones.html`
- **THEN** ofrece la lista con una sesión grabándose, la lista con todas cerradas, sin sesiones, detenida por poco espacio, detenida por un error y las confirmaciones de borrar una y todas

#### Scenario: Confirmaciones
- **WHEN** se eligen los estados de confirmar el borrado de una y de todas
- **THEN** se pide confirmar antes de borrar, con «Borrar» y «Cancelar», y la sesión que se está grabando no se ofrece para borrar

### Requirement: Sección Depuración en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar al final, después de «Copia de seguridad», la sección «Depuración» con el interruptor «Grabar sesiones», «Espacio libre mínimo» con su stepper y la entrada «Sesiones grabadas» con su resumen, con estados para la grabación apagada, grabando y detenida por poco espacio.

#### Scenario: Apagada
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-apagada`
- **THEN** «Grabar sesiones» está apagado, «Espacio libre mínimo» dice 10 % y la entrada «Sesiones grabadas» resume las sesiones existentes

#### Scenario: Grabando
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-grabando`
- **THEN** «Grabar sesiones» está activado y debajo se lee «Grabando»

#### Scenario: Detenida por poco espacio
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-detenida`
- **THEN** debajo del interruptor se lee «Detenida por poco espacio» con cómo reanudarla

### Requirement: Ajuste «Idioma» en la maqueta de Ajustes
La sección «Pantalla» de la maqueta de Ajustes SHALL incluir, encima de «Tema», el ajuste «Idioma · Language» con las opciones «Español» y «English», cada una en su propio idioma, y SHALL tener un estado con «English» elegido. Las maquetas SHALL seguir en español: el estado solo muestra la opción elegida, no la interfaz traducida.

#### Scenario: Estado inicial
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección «Pantalla» muestra «Idioma · Language», encima de «Tema», con «Español» elegido

#### Scenario: Inglés elegido
- **WHEN** se abre el estado «Idioma inglés» de la maqueta de Ajustes
- **THEN** «English» está elegido y «Español» no, con la marca de selección

#### Scenario: Guía
- **WHEN** se abre la guía de estilo
- **THEN** el grupo de opciones usado por «Idioma» es el mismo que el de «Tema» y «Orientación», sin componente nuevo

### Requirement: Estados del aviso de salida en las maquetas de rodaje
Las maquetas `rodaje.html` y `rodaje-vertical.html` SHALL incluir los estados «salida-confirmar» («Para cerrar la aplicación, pulsa Atrás otra vez») y «salida-cerrando» («Cerrando…»), accesibles desde el selector de estado de `revision.js` y por enlace con `#estado=`, con el aviso flotante sobre la pantalla en los dos temas.

#### Scenario: Estado de confirmación
- **WHEN** se abre `maquetas/rodaje.html#estado=salida-confirmar`
- **THEN** se ve Rodaje con el aviso «Para cerrar la aplicación, pulsa Atrás otra vez» flotando, sin que nada se mueva

#### Scenario: Estado de cierre en vertical
- **WHEN** se abre `maquetas/rodaje-vertical.html#estado=salida-cerrando`
- **THEN** se ve Rodaje vertical con el aviso «Cerrando…»

#### Scenario: Tema claro
- **WHEN** se abre cualquiera de los dos estados con `tema=claro`
- **THEN** el aviso se lee con el contraste exigido y se distingue del fondo por algo más que el color

### Requirement: Guía de inicio en las maquetas de rodaje
Las maquetas de rodaje, en vertical (390 × 844 y pantalla baja 360 × 640) y en horizontal, SHALL tener estados con la guía de inicio: las cuatro páginas de la hoja abierta, la página 2 con el rodillo conectando, con el rodillo conectado y con el rodillo y el controlador conectados, la página 4 en cada modo de conexión, con la marca «Opcional», la tira minimizada y el estado sin guía con todo hecho. Todos SHALL verse en los dos temas.

#### Scenario: Página 1
- **WHEN** se abre el estado de la página 1
- **THEN** la hoja muestra «Smart Bike», las tres piezas con su ejemplo, «Opcional» en el controlador y la app, el indicador «1 de 4», «Siguiente», minimizar, la equis y «No volver a mostrar»

#### Scenario: Página 2
- **WHEN** se abre el estado de la página 2 sin dispositivos
- **THEN** el paso «Empareja tus dispositivos» dice «Pendiente», «Configura la conexión» (opcional) y «Ajusta la app» no tienen estado, ninguno usa un color de alerta, y el indicador dice «2 de 4» con «Atrás» y «Siguiente»

#### Scenario: Estados de los pasos
- **WHEN** se abren los estados con el rodillo conectando, el rodillo conectado y el rodillo y el controlador conectados
- **THEN** el primer paso dice «Conectando», «Falta el controlador» y «Hecho»

#### Scenario: Página 3
- **WHEN** se abre el estado de la página 3
- **THEN** la hoja muestra los modos «LIBRE», «SIM» y «ERG» (con «ERG MANUAL» y «ERG APP»), cada uno con su frase y la indicación de cuáles necesitan una app conectada, el indicador «3 de 4», «Atrás» y «Siguiente»

#### Scenario: Página 4
- **WHEN** se abre la página 4 en cada modo
- **THEN** lleva la marca «Opcional», dice el modo, el estado de la conexión con la app y sus tres pasos, con «Atrás» como único botón de navegación

#### Scenario: Minimizada
- **WHEN** se abre el estado minimizado
- **THEN** la tira ocupa el pie con «Para empezar» y lo siguiente por hacer, sin contadores

#### Scenario: Todo hecho
- **WHEN** se abre el estado con todo hecho
- **THEN** Rodaje no muestra la guía

#### Scenario: Pantalla estrecha
- **WHEN** se abre un estado con la hoja en vertical a 360 × 640 dp y en horizontal a 640 × 360 dp
- **THEN** la hoja se ve entera, sin desplazamiento

### Requirement: Guía de inicio en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar la entrada «Guía de inicio» con su ayuda de una línea y un enlace a la maqueta de Rodaje con la hoja abierta en la página 1.

#### Scenario: Entrada
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** se ve la entrada «Guía de inicio» y, al tocarla, se abre la maqueta de Rodaje con la guía en la página 1

### Requirement: Maquetas de Libre, pendiente y botones sin Square
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener estados para: el modo «LIBRE» (sin app, con la transmisión), el modo «SIM» con app (ruta de Zwift), el dato «Pendiente» con una subida, una bajada y «0,0 %» en Libre, el dato «Pendiente» en ERG con «—», y los botones − y + visibles con el Square desconectado (con el ajuste desactivado) junto al mismo estado con el Square conectado, sin ellos. La maqueta de Ajustes SHALL mostrar la ayuda de la casilla «Botones − y + en pantalla» y «Datos en pantalla» SHALL ofrecer «Pendiente» entre sus nueve datos. Cada estado SHALL abrirse por su `#estado=…` y estar en el índice de maquetas.

#### Scenario: Libre
- **WHEN** se elige el estado Libre, en horizontal o en vertical
- **THEN** el modo dice «LIBRE» con «Lineal · 24 marchas», no hay pendiente a la vista y el modo, Ajustes y la barra de conexiones no se solapan a 360 dp ni a la anchura mínima

#### Scenario: SIM con app
- **WHEN** se elige el estado ruta de Zwift con el dato «Pendiente»
- **THEN** el modo dice «SIM» y el dato dice «+4,2 %»

#### Scenario: Pendiente en bajada y en Libre
- **WHEN** se elige el estado de bajada de Zwift y después el de Libre, con «Pendiente» elegido
- **THEN** el dato dice «−7,0 %» y «0,0 %», respectivamente

#### Scenario: Pendiente en ERG
- **WHEN** se elige un estado ERG con «Pendiente» elegido
- **THEN** el dato dice «—»

#### Scenario: Botones con el Square desconectado
- **WHEN** se elige el estado Square desconectado con el ajuste de los botones desactivado
- **THEN** se ven los botones − y +, la marcha y los datos reducidos para hacerles sitio, y en 360 × 640 solo los 4 primeros datos

#### Scenario: Botones con el Square conectado
- **WHEN** se elige el estado Square conectado con el ajuste desactivado
- **THEN** no se ven los botones − y +

#### Scenario: Ayuda de la casilla
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la casilla de los botones − y + dice en su ayuda que solo manda con el Square conectado

### Requirement: Página de modos de uso en las maquetas
Las maquetas de rodaje, en vertical (390 × 844 y 360 × 640) y en horizontal, SHALL tener el estado de la página 3 de la guía («Cómo puedes usar la app») con los modos «LIBRE», «SIM» y «ERG» tal como los pinta el indicador de modo, en los dos temas, y SHALL abrirse por su `#estado=…` y estar en el índice de maquetas. La página SHALL verse entera sin desplazamiento.

#### Scenario: Página 3 en pantalla baja
- **WHEN** se abre el estado de la página 3 en vertical a 360 × 640 dp
- **THEN** se leen los tres modos completos, el indicador «3 de 4», «Atrás» y «Siguiente», sin desplazamiento

#### Scenario: Página 3 en horizontal
- **WHEN** se abre el estado de la página 3 en horizontal a 640 × 360 dp
- **THEN** se leen los tres modos completos sin desplazamiento

#### Scenario: Coincide con el indicador
- **WHEN** se compara la página 3 con los estados Libre, SIM con app y ERG de las maquetas de rodaje
- **THEN** los nombres de los modos son los mismos

### Requirement: Modo tocable en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener estados con el cambio de modo activado en los que el indicador de modo se ve como un botón con relleno tintado y borde marcado, sin glifo (Libre, SIM, ERG manual y ERG APP), y estados con el ajuste desactivado en los que sigue siendo una etiqueta, con o sin los botones − y + visibles. En los estados tocables, el selector de estado SHALL permitir ver el cambio SIM ⇄ ERG manual con su aviso del pie, y todos SHALL verse en los dos temas.

#### Scenario: Modo tocable en Libre
- **WHEN** se abre el estado de Libre tocable, en vertical y en horizontal
- **THEN** el indicador «LIBRE» se ve como un botón junto al icono de Ajustes, sin solaparse con él ni con la barra de conexiones

#### Scenario: Modo tocable en ERG manual
- **WHEN** se abre el estado de ERG manual tocable
- **THEN** el indicador «ERG MANUAL» se ve como un botón y el pie dice «ERG manual · 200 W»

#### Scenario: Sin el ajuste
- **WHEN** se abre un estado de rodaje con el cambio de modo desactivado, con los botones − y + visibles o sin ellos
- **THEN** el indicador de modo se ve como la etiqueta de hoy, sin aspecto de botón

#### Scenario: Entrenamiento de la app
- **WHEN** se abre el estado de un entrenamiento ERG APP tocable
- **THEN** el indicador «ERG APP» se ve como un botón, igual que los demás, y el pie muestra «Entrenamiento de la app» al tocarlo

### Requirement: Casilla del cambio de modo en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar, en «Al rodar», la casilla «Cambiar de modo tocando» con su ayuda, activada y desactivada, además de la casilla «Botones − y + en pantalla» sin cambios.

#### Scenario: Casilla desactivada
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la casilla «Cambiar de modo tocando» está desactivada y su ayuda dice qué hace y que solo vale fuera de un entrenamiento de la app

#### Scenario: Casilla activada
- **WHEN** se abre el estado de Ajustes con el cambio de modo activado
- **THEN** la casilla se ve activada por algo más que el color
