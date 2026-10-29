# Spec Delta

## MODIFIED Requirements

### Requirement: Navegación entre pantallas
Las pantallas de pestaña (Dispositivos, Plataformas y Ajustes) SHALL compartir la navegación inferior (Rodar, Dispositivos, Plataformas, Ajustes), sin botón de volver; Rodaje SHALL NOT mostrarla. Las secundarias SHALL tener un botón de volver a su origen y no SHALL mostrar la navegación inferior: Botones, Marchas, Datos en pantalla y Diagnóstico vuelven a Ajustes, y Asignar acción a Botones. Los enlaces SHALL funcionar entre maquetas, sin estados «abierta desde Diagnóstico».

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
- **WHEN** se toca cada pestaña de la navegación inferior desde Dispositivos, Plataformas o Ajustes
- **THEN** se abre su maqueta con esa pestaña marcada, y «Rodar» abre la maqueta de rodaje sin navegación inferior

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas, Botones y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro», y «Orientación», con «Según el móvil», «Vertical» y «Horizontal»), Ciclista (peso del ciclista y peso de la bici), Rodillo («Modelo de resistencia», con «Simple», «Por tabla» y «Físico», «Tabla de marchas» con «Por tabla», «Dificultad», «Potencia mínima» con «Físico», «Bajadas» y «Pendiente sin Zwift»), sin sección «Zwift», con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad y distancia, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos. La de Tabla de marchas SHALL mostrar la lista de marchas con su ajuste editable en los estados de la lineal y de la Shimano.

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
- **THEN** el contador dice «6 de 6», están marcados seis, la vista previa los muestra todos y las casillas de los dos sin marcar están deshabilitadas con su ayuda

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

#### Scenario: Velocidad y distancia en Datos en pantalla
- **WHEN** se abre `maquetas/datos.html#estado=velocidad`
- **THEN** la lista tiene ocho datos, la vista previa muestra velocidad y distancia y sus ayudas explican que la velocidad la calcula el modelo físico

#### Scenario: Tabla de marchas
- **WHEN** se abre `maquetas/tabla-marchas.html#estado=lineal` y `#estado=shimano`
- **THEN** se ven 24 filas con su ajuste y su paso a paso, la marcha puesta marcada y «Restablecer», en los dos temas

#### Scenario: Mando de Zwift en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la entrada «Botones», con el icono de los cuatro símbolos y la transmisión de la asignación, va entre «Marchas» y «Diagnóstico», y no hay sección «Zwift»

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver a Ajustes, las conexiones con la acción de buscar, los botones del Square con la máscara, el último gesto con su acción, la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al Neo y la latencia de la palanca), el tráfico con la casilla «Tráfico en directo», desmarcada al abrir, que al marcarla muestra el volcado con pausar y limpiar, y el control manual con el último envío. SHALL NOT enlazar con Botones ni con Ajustes salvo por el botón de volver.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, hay un botón de volver a Ajustes, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Enlace a Botones
- **WHEN** se recorre la sección de botones del Square de la maqueta de Diagnóstico
- **THEN** no hay enlace «Asignación de botones»: Botones se abre desde la entrada «Botones» de Ajustes

#### Scenario: Tráfico al abrir
- **WHEN** se abre la maqueta de Diagnóstico en cualquier estado
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar», y al marcarla aparecen el volcado del estado y los dos botones

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal, sin Zwift y con Zwift, de los escenarios siguientes, con la barra de conexiones de los dispositivos (un icono por enlace, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, con el estado de la plataforma en palabras, en la fila de Ajustes y enlazando con Plataformas, y con el modo y la transmisión en la fila de debajo (en horizontal, en la esquina de la zona principal, de modo que los datos suben hasta la fila superior), y con el icono de Ajustes, en la línea del modo y la transmisión. Ningún estado SHALL mostrar velocidad ni distancia.

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
- **THEN** el estado de la plataforma dice «Sin wifi para Zwift», el modo dice «SIM», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado a Zwift y el mando esperando, con el estado de la plataforma en «Conectado a Zwift · sin mando» y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando esperando, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado de la plataforma dice «Conectado a Zwift · sin mando», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Zwift no visible en las maquetas de rodaje
Las maquetas de rodaje y de rodaje en vertical SHALL tener el estado «Zwift no visible», con el estado de la plataforma en «Zwift no visible» con el borde de alerta, y el resto como en el estado sin Zwift.

#### Scenario: Rodaje con Zwift no visible
- **WHEN** se elige el estado Zwift no visible en la maqueta de rodaje, en horizontal o en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado de la plataforma dice «Zwift no visible» con el aro vacío y el borde de alerta y enlaza con Plataformas, la barra enlaza con Dispositivos, y el resto se ve como en el estado sin Zwift, sin moverse

### Requirement: Mando sin conexión en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado a Zwift y el mando sin conexión, con el estado de la plataforma en «Conectado a Zwift · sin mando» y el resto como en la ruta de Zwift.

#### Scenario: Mando sin conexión en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando sin conexión, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado de la plataforma dice «Conectado a Zwift · sin mando», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar, junto al nombre del Neo y del Square, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas; Diagnóstico y Plataformas llevan además los de Zwift y del mando junto a su nombre.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo y el mando de consola para el Square, y no hay filas de Zwift ni del mando

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos para el Neo y el Square, y los de Zwift y del mando en sus filas

## ADDED Requirements

### Requirement: Maqueta de Dispositivos de la app
La maqueta de Dispositivos SHALL mostrar el Neo y el Square con su estado en palabra, su ayuda para despertarlos y «Olvidar» si están recordados, «Buscar dispositivos» y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado y Bluetooth apagado. SHALL NOT mostrar filas de Zwift ni del mando, la batería del Square ni la elección de wifi o Bluetooth.

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** el Neo y el Square dicen «Sin conexión», sin «Olvidar», y «Buscar dispositivos» es la acción principal

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el Neo y el Square dicen «Conectado» y tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila del Neo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila del Square dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y el Neo y el Square dicen «Sin conexión»

#### Scenario: Sin Zwift
- **WHEN** se revisan todos los estados de la maqueta de Dispositivos
- **THEN** ninguno muestra una fila de Zwift ni del mando

### Requirement: Maqueta de Plataformas
La maqueta de Plataformas SHALL mostrar Zwift elegida, sus dos modos con su ayuda, el interruptor «Mando de Zwift» solo con el de rodillo por wifi, el selector «Nombre ante la app», el estado de Zwift y del mando con cómo emparejarlos y la navegación inferior, con estados para cada modo, mando desactivado y no disponible, KICKR BIKE PRO, Zwift no visible, sin wifi y Bluetooth apagado.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/plataformas.html#estado=inicio`
- **THEN** se ve Zwift con «Rodillo por wifi (FTMS)» elegido y recomendado, «Mando de Zwift» activado, «Smart Bike» elegido, Zwift y el mando «Esperando» y la pestaña Plataformas marcada

#### Scenario: Zwift Ride
- **WHEN** se elige el estado de Zwift Ride
- **THEN** «Todo por wifi (modo Zwift Ride)» está elegido, la ayuda dice que Zwift ve un solo dispositivo y mostrará su marcha, no hay interruptor «Mando de Zwift» y el mando se explica como parte del mismo dispositivo

#### Scenario: Mando desactivado y no disponible
- **WHEN** se eligen los estados de mando desactivado y mando no disponible
- **THEN** la fila del mando dice «Desactivado», con que se activa en esta pantalla, o «No disponible», con que este móvil no puede hacer de mando de Zwift

#### Scenario: KICKR BIKE PRO
- **WHEN** se elige el estado KICKR BIKE PRO
- **THEN** «KICKR BIKE PRO» está elegido, la ayuda dice que es una prueba y que hay que emparejar de nuevo, y la ayuda de emparejar nombra el «KICKR BIKE PRO» con sus cuatro caracteres

#### Scenario: Zwift no visible
- **WHEN** se elige el estado Zwift no visible
- **THEN** arriba se ve el aviso de error «Zwift no puede encontrar la app en esta red wifi. Apaga y vuelve a encender el wifi del móvil; si sigue igual, reinícialo.» y la fila de Zwift dice «No visible» con el anillo vacío y el borde de alerta, sin la ayuda de emparejar

#### Scenario: Sin wifi y Bluetooth apagado
- **WHEN** se eligen los estados sin wifi y Bluetooth apagado
- **THEN** Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, o el mando dice «Sin conexión» con el aviso de Bluetooth apagado, según el modo

#### Scenario: Nombre anunciado en Diagnóstico
- **WHEN** se abre la maqueta de Diagnóstico con el nombre «KICKR BIKE PRO» o «Smart Bike»
- **THEN** Conexiones muestra, bajo la fila de Zwift, ese nombre anunciado

#### Scenario: Acceso desde Rodaje
- **WHEN** en las maquetas de rodaje se toca el icono de Zwift o el del mando
- **THEN** se abre la maqueta de Plataformas, y volver abre Rodaje

### Requirement: Maqueta de Botones desde Ajustes
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift y acciones de la app, cada uno con una fila por función o acción con sus botones y gestos y enlace a Asignar acción, los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», sin navegación inferior y con un botón de volver a Ajustes. Sus estados SHALL ser asignación por defecto, asignación modificada, mando desactivado y confirmar la restauración.

#### Scenario: Volver a Ajustes
- **WHEN** en la maqueta de Botones se toca volver
- **THEN** se abre la maqueta de Ajustes con la navegación inferior

#### Scenario: Funciones de Zwift primero
- **WHEN** se abre la maqueta de Botones
- **THEN** el primer grupo tiene una fila por función de un mando oficial de Zwift (↑, ↓, ←, →, A, B, Y, Z, girar a la izquierda y a la derecha, y los dos frenos), cada una con su botón y enlace a Asignar acción, y dice que llegan a Zwift por el mando de la app

#### Scenario: Asignación modificada
- **WHEN** se elige el estado de asignación modificada
- **THEN** una acción aparece con «Sin botón», otra con una combinación nueva, una función de Zwift con «Sin botón» y «Invertir palancas» activado

#### Scenario: Acciones de pantalla
- **WHEN** se elige el estado de asignación por defecto
- **THEN** tras «Alternar SIM ⇄ ERG» aparecen «Pantalla anterior» con Campag. I + cruceta ← y «Pantalla siguiente» con Campag. I + cruceta →

#### Scenario: Mando desactivado en Botones
- **WHEN** se elige el estado mando desactivado
- **THEN** el grupo de Zwift dice que, sin el mando, esas funciones no llegan a Zwift por la app, con sus filas iguales

### Requirement: Iconos de la barra inferior de las maquetas
Las maquetas con barra inferior SHALL mostrar la pestaña Plataformas con su icono propio, distinto del de Dispositivos, y la entrada «Botones» de Ajustes con el icono de los cuatro símbolos; el mando de Zwift SHALL conservar su icono en las maquetas donde aparece.

#### Scenario: Barra inferior de las maquetas
- **WHEN** se abre Dispositivos, Plataformas o Ajustes en la maqueta
- **THEN** la barra tiene Rodar, Dispositivos, Plataformas y Ajustes, la de Plataformas con su icono propio

#### Scenario: Entrada de Botones
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la entrada «Botones» lleva el icono de los cuatro símbolos

## REMOVED Requirements

### Requirement: Dispositivos
**Reason**: La maqueta de Dispositivos deja de mostrar las filas de Zwift y del mando, que pasan a la maqueta de Plataformas.
**Migration**: Esta misma spec, «Maqueta de Dispositivos de la app» y «Maqueta de Plataformas».

### Requirement: Maqueta de Botones como pestaña
**Reason**: Botones deja de ser una pestaña; la maqueta pasa a ser una pantalla secundaria de Ajustes.
**Migration**: Esta misma spec, «Maqueta de Botones desde Ajustes».

### Requirement: Icono de Botones en la barra inferior de las maquetas
**Reason**: La barra inferior ya no tiene la pestaña Botones; el icono pasa a la entrada de Ajustes.
**Migration**: Esta misma spec, «Iconos de la barra inferior de las maquetas».

### Requirement: Modo de conexión con Zwift en la maqueta de Ajustes
**Reason**: La elección del modo sale de Ajustes y pasa a la maqueta de Plataformas.
**Migration**: Esta misma spec, «Maqueta de Plataformas».

### Requirement: Mando por wifi en la maqueta de Dispositivos
**Reason**: Las filas de Zwift y del mando salen de la maqueta de Dispositivos.
**Migration**: Esta misma spec, «Maqueta de Plataformas».

### Requirement: Simular un KICKR BIKE en las maquetas
**Reason**: El interruptor se sustituye por el selector «Nombre ante la app» de la maqueta de Plataformas.
**Migration**: Esta misma spec, «Maqueta de Plataformas» (escenarios «KICKR BIKE PRO» y «Nombre anunciado en Diagnóstico»).
