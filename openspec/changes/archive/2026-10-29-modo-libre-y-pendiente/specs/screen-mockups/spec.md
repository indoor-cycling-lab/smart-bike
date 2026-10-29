## MODIFIED Requirements

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
- **THEN** el modo dice «ERG ZWIFT», sin línea explicativa de las palancas, la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el estado de la plataforma dice «Sin wifi para Zwift», el modo dice «LIBRE», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

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

### Requirement: Maquetas de Datos en pantalla y Tabla de marchas
La maqueta de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad, distancia y pendiente, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos. La de Tabla de marchas SHALL mostrar la lista de marchas con su ajuste editable en los estados de la lineal y de la Shimano.

#### Scenario: Estados de datos y de marchas
- **WHEN** se abre la maqueta de Datos en pantalla con 4 datos
- **THEN** se ven los 4 datos elegidos, su orden y la vista previa

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


## ADDED Requirements

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
