# Spec Delta

## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal, sin Zwift y con Zwift, de los escenarios siguientes, con los chips enlazando con Dispositivos. Ningún estado SHALL mostrar velocidad ni distancia.

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
- **THEN** el chip del Neo muestra «sin conexión», la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas, y la rejilla tiene potencia y cadencia, sin aviso de cadencia baja ni datos del intervalo de Zwift

#### Scenario: Sin Zwift
- **WHEN** se elige el estado sin Zwift
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los chips del Neo y del Square con el punto lleno, el chip «Zwift esperando» y el aviso «+1 marcha»

#### Scenario: Tope en lineal
- **WHEN** se elige el estado de tope en lineal
- **THEN** «24» aparece atenuado con «Tope · marcha más dura» y la escalera tiene resaltado el último segmento

#### Scenario: Square sin conexión
- **WHEN** se elige el estado de Square sin conexión
- **THEN** el chip del Square dice «sin conexión», la marcha sigue visible, la potencia y la cadencia tienen valor y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado sin dispositivos y se toca un chip
- **THEN** los chips del Neo y del Square dicen «sin conexión», la potencia y la cadencia muestran «—», el pie dice «Toca Neo o Square para conectarlos» y el chip abre la maqueta de Dispositivos

#### Scenario: Neo conectando al abrir
- **WHEN** se elige el estado Neo conectando
- **THEN** el chip del Neo dice «conectando» con el anillo de espera, el del Square tiene el punto lleno, la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo»

#### Scenario: Square reconectando
- **WHEN** se elige el estado Square reconectando
- **THEN** el chip del Square dice «conectando» con el anillo de espera, la potencia y la cadencia tienen valor y el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** se elige el estado de los dos conectando
- **THEN** los dos chips dicen «conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Ruta de Zwift
- **WHEN** se elige el estado ruta de Zwift
- **THEN** los chips del Neo, del Square y de Zwift tienen el punto lleno, el modo dice «SIM» con «Lineal · 24 marchas», se ve «14» con «de 24» y la escalera, potencia y cadencia, y no aparece ninguna pendiente

#### Scenario: Entrenamiento ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift en lineal
- **THEN** el modo dice «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia», la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el chip de Zwift dice «Zwift sin conexión», el modo dice «SIM», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

#### Scenario: Aviso desactivado
- **WHEN** se elige el estado sin aviso de acción
- **THEN** el pie está vacío tras un cambio de marcha, y la marcha y los datos no se mueven

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie, sin velocidad ni distancia. SHALL tener también los estados lineales de la maqueta horizontal, sin Zwift y con Zwift, con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven los chips del Neo y del Square con el punto lleno y el chip «Zwift esperando», «Lineal · 24 marchas», «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los chips afectados dicen «sin conexión», enlazan con Dispositivos, y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los chips afectados dicen «conectando», el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con los tres chips en la fila de conexiones, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

#### Scenario: Botones y seis datos en vertical
- **WHEN** se elige el estado con botones − y + en vertical
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento

### Requirement: Navegación entre pantallas
Las pantallas de pestaña (Dispositivos, Botones y Ajustes) SHALL compartir la navegación inferior (Rodar, Dispositivos, Botones, Ajustes), sin botón de volver; Rodaje SHALL NOT mostrarla. Las secundarias SHALL tener un botón de volver a su origen y no SHALL mostrar la navegación inferior: Marchas, Datos en pantalla y Diagnóstico vuelven a Ajustes, y Asignar acción a Botones. Los enlaces SHALL funcionar entre maquetas, sin estados «abierta desde Diagnóstico».

#### Scenario: De Ajustes a Marchas y vuelta
- **WHEN** en Ajustes se toca «Marchas» y luego volver
- **THEN** se abre la maqueta de Marchas y después se vuelve a Ajustes

#### Scenario: Diagnóstico desde Ajustes
- **WHEN** en la maqueta de Ajustes se toca «Diagnóstico» y luego volver
- **THEN** se abre la maqueta de Diagnóstico sin navegación inferior y después se vuelve a Ajustes

#### Scenario: Pestañas
- **WHEN** se toca cada pestaña de la navegación inferior desde Dispositivos, Botones o Ajustes
- **THEN** se abre su maqueta con esa pestaña marcada, y «Rodar» abre la maqueta de rodaje sin navegación inferior

### Requirement: Dispositivos
La maqueta de Dispositivos SHALL mostrar el Neo y el Square con su estado en palabra, su ayuda para despertarlos y «Olvidar» si están recordados, Zwift con su estado y cómo emparejar la app en Zwift, «Buscar dispositivos» y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado, Zwift esperando, sin wifi y Bluetooth apagado. SHALL NOT mostrar la batería del Square, el mando de Zwift ni la elección de wifi o Bluetooth.

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** el Neo y el Square dicen «Sin conexión», sin «Olvidar», y «Buscar dispositivos» es la acción principal

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el Neo, el Square y Zwift dicen «Conectado», y el Neo y el Square tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila del Neo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila del Square dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Sin wifi y Bluetooth apagado
- **WHEN** se eligen los estados sin wifi y Bluetooth apagado
- **THEN** Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, o se ve el aviso de Bluetooth apagado con «Activar Bluetooth»

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas y Diagnóstico y las secciones Al rodar, Ciclista (solo el peso del ciclista) y Rodillo («Bajadas» y «Pendiente sin Zwift»), con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el peso de la bici, la potencia mínima, el paso ERG, el tema, «Mantener la pantalla encendida», el aviso de cadencia baja ni «Botones y gestos»

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6», están marcados los seis y la vista previa los muestra todos

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver a Ajustes, las conexiones con la acción de buscar, los botones del Square con la máscara, el último gesto con su acción, la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al Neo y la latencia de la palanca), el tráfico con pausar y limpiar, y el control manual con el último envío. SHALL NOT enlazar con Botones ni con Ajustes salvo por el botón de volver.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, hay un botón de volver a Ajustes, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Enlace a Botones
- **WHEN** se recorre la sección de botones del Square de la maqueta de Diagnóstico
- **THEN** no hay enlace «Asignación de botones»: Botones se abre desde la navegación inferior de Ajustes

### Requirement: Ajuste «Bajadas» en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL incluir el ajuste «Bajadas» con sus tres opciones y su ayuda.

#### Scenario: Ajustes completa
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección Rodillo muestra «Bajadas» con «Sin protección», «La marcha no crea bajadas» (elegida) y «Nunca empujar», cada una con su ayuda

#### Scenario: Desde Diagnóstico
- **WHEN** en la maqueta de Diagnóstico se toca volver
- **THEN** se abre la maqueta de Ajustes con la sección Rodillo y el ajuste «Bajadas», con la navegación inferior

## ADDED Requirements

### Requirement: Maqueta de Botones como pestaña
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift (informativas) y acciones de la app con sus botones y gestos, los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», con la navegación inferior y sin botón de volver. Sus estados SHALL ser asignación por defecto, asignación modificada y confirmar la restauración.

#### Scenario: Funciones de Zwift primero
- **WHEN** se abre la maqueta de Botones
- **THEN** el primer grupo reproduce un mando oficial de Zwift (palancas, girar, frenar, cruceta, A, B, Y, Z), sin enlaces, y dice que lo resuelve Zwift con el Square emparejado

#### Scenario: Asignación modificada
- **WHEN** se elige el estado de asignación modificada
- **THEN** una acción aparece con «Sin botón», otra con una combinación nueva y «Invertir palancas» activado

#### Scenario: Acciones de pantalla
- **WHEN** se elige el estado de asignación por defecto
- **THEN** tras «Alternar SIM ⇄ ERG» aparecen «Pantalla anterior» con Campag. I + cruceta ← y «Pantalla siguiente» con Campag. I + cruceta →

## REMOVED Requirements

### Requirement: Botones y asignación
**Reason**: Botones pasa a ser una pestaña: desaparece el estado «abierta desde Diagnóstico» y se añaden las acciones de pantalla.
**Migration**: Sus escenarios siguen en «Maqueta de Botones como pestaña», sin el estado abierta desde Diagnóstico.
