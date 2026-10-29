## MODIFIED Requirements

### Requirement: Dispositivos
La maqueta de Dispositivos SHALL mostrar el Neo y el Square con su estado en palabra, su ayuda para despertarlos y «Olvidar» si están recordados, Zwift con su estado y cómo emparejar la app en Zwift, el mando de Zwift con su estado y cómo emparejarlo, «Buscar dispositivos» y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado, Zwift esperando, sin wifi, Bluetooth apagado, mando esperando, mando desactivado y mando no disponible. SHALL NOT mostrar la batería del Square ni la elección de wifi o Bluetooth.

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** el Neo y el Square dicen «Sin conexión», sin «Olvidar», y «Buscar dispositivos» es la acción principal

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el Neo, el Square, Zwift y el mando dicen «Conectado», y el Neo y el Square tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila del Neo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila del Square dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Sin wifi y Bluetooth apagado
- **WHEN** se eligen los estados sin wifi y Bluetooth apagado
- **THEN** Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, o se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y el mando dice «Sin conexión»

#### Scenario: Estados del mando
- **WHEN** se eligen los estados mando esperando, mando desactivado y mando no disponible
- **THEN** la fila del mando dice «Esperando» con cómo elegir el móvil en la lista de mandos de Zwift y que el Square no se empareja en Zwift, «Desactivado» con que se activa en Ajustes, o «No disponible» con que este móvil no puede hacer de mando

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

### Requirement: Maqueta de Botones como pestaña
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift y acciones de la app, cada uno con una fila por función o acción con sus botones y gestos y enlace a Asignar acción, los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», con la navegación inferior y sin botón de volver. Sus estados SHALL ser asignación por defecto, asignación modificada, mando desactivado y confirmar la restauración.

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

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro»), Ciclista (solo el peso del ciclista), Rodillo («Bajadas» y «Pendiente sin Zwift») y Zwift («Mando de Zwift», activado, con su ayuda), con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el peso de la bici, la potencia mínima, el paso ERG, «Mantener la pantalla encendida», el aviso de cadencia baja, «Botones y gestos» ni la elección de wifi o Bluetooth para Zwift

#### Scenario: Tema en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio` y `maquetas/ajustes.html#estado=claro&tema=claro`
- **THEN** la sección «Pantalla», entre «Al rodar» y «Ciclista», muestra «Tema» con «Oscuro» elegido en el primer enlace y con «Claro» elegido, y la maqueta en claro, en el segundo

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6», están marcados los seis y la vista previa los muestra todos

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

#### Scenario: Mando de Zwift en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la última sección, «Zwift», tiene «Mando de Zwift» activado con su ayuda

## ADDED Requirements

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado a Zwift y el mando esperando, con el chip «Zwift» en el anillo de espera y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando esperando, en horizontal y en vertical
- **THEN** los chips del Neo y del Square tienen el punto lleno, el chip «Zwift» el anillo de espera sin cambiar de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Mando de Zwift en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar, en Conexiones, la fila del mando de Zwift bajo la de Zwift, con «Conectado» en los estados con Zwift conectado y «Esperando» en los que Zwift espera.

#### Scenario: Mando en Diagnóstico
- **WHEN** se elige el estado rodando con Zwift de la maqueta de Diagnóstico
- **THEN** bajo la fila de Zwift se ve la del mando con «Conectado»
