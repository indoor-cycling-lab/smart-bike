## ADDED Requirements

### Requirement: Maqueta de Conexión
La maqueta `maquetas/conexion.html` SHALL mostrar la introducción, los avisos, la sección de estado (rodillo, mando y pulso con por dónde van, su estado y su ayuda), la sección de modo con los tres modos, su detalle y su compatibilidad, «Nombre ante la app» solo en los modos con wifi y la navegación inferior con Conexión marcada.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/conexion.html#estado=inicio`
- **THEN** se ve «Wifi + Bluetooth» elegido y recomendado, «Smart Bike» elegido, el rodillo, el mando y el pulso «Esperando», sin interruptor del mando ni elección de plataforma, y la pestaña Conexión marcada

#### Scenario: Orden de la pantalla
- **WHEN** se abre cualquier estado de la maqueta de Conexión
- **THEN** de arriba abajo se ven la introducción, los avisos si los hay, el estado, el modo y, con wifi, «Nombre ante la app»

### Requirement: Estados de la maqueta de Conexión
La maqueta de Conexión SHALL tener los estados: todo conectado; rodillo conectado con mando y pulso esperando; «Solo Bluetooth»; «Solo wifi (modo Zwift Ride)»; sin pulsómetro; sin Square; primer uso sin rodillo; pulso sin lectura; Neo y Square reconectando; KICKR BIKE PRO; no visible; sin wifi; Bluetooth apagado; y móvil sin modo periférico.

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el rodillo y el mando dicen «Conectado» y el pulso «Enviando», sin mostrar los bpm

#### Scenario: Servicios no ofrecidos
- **WHEN** se eligen los estados sin pulsómetro y sin Square
- **THEN** el pulso o el mando dicen «No se ofrece», atenuados, con qué dispositivo falta y el enlace a Dispositivos

#### Scenario: Primer uso
- **WHEN** se elige el estado primer uso sin rodillo
- **THEN** se ve el aviso de que la app no publica nada hasta recordar un rodillo y las tres filas dicen «No se ofrece»

#### Scenario: Dispositivos reconectando
- **WHEN** se elige el estado Neo y Square reconectando
- **THEN** el rodillo y el mando siguen «Conectado» y cada uno añade que su dispositivo se está reconectando

#### Scenario: Solo Bluetooth
- **WHEN** se elige el estado «Solo Bluetooth»
- **THEN** el modo está elegido sin la marca de recomendado, no hay «Nombre ante la app», los tres servicios dicen «por Bluetooth» y las ayudas nombran el móvil por su nombre Bluetooth

#### Scenario: Solo wifi
- **WHEN** se elige el estado «Solo wifi (modo Zwift Ride)»
- **THEN** los tres servicios dicen «por wifi», el mando dice que va dentro del mismo dispositivo y la descripción del modo dice que la app de entrenamiento mostrará su propia marcha

#### Scenario: No visible y sin wifi
- **WHEN** se eligen los estados no visible y sin wifi
- **THEN** arriba se ve el aviso correspondiente y el rodillo dice «No visible» o «Sin wifi» con el aro vacío y el borde de alerta

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado con «Wifi + Bluetooth»
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth», el mando dice «Sin Bluetooth» y el rodillo conserva su estado por wifi

#### Scenario: Acceso desde Rodaje
- **WHEN** en las maquetas de rodaje se toca el estado de la publicación
- **THEN** se abre la maqueta de Conexión, y volver abre Rodaje

## MODIFIED Requirements

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar, junto al nombre del Neo y del Square, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas; Diagnóstico lleva además los de Zwift y del mando junto a su nombre.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo y el mando de consola para el Square, y no hay filas de Zwift ni del mando

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos para el Neo y el Square, y los de Zwift y del mando en sus filas

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

### Requirement: Barra de conexiones y plataforma en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener la barra de conexiones de los dispositivos (un icono por enlace, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, y el estado de la publicación en palabras, sin nombre de plataforma, en la fila de Ajustes y enlazando con Conexión.

#### Scenario: Enlaces de la barra y de la plataforma
- **WHEN** se toca la barra de conexiones o el estado de la publicación
- **THEN** se abre Dispositivos o Conexión respectivamente

#### Scenario: Estados de la publicación
- **WHEN** se recorren los estados de la maqueta de rodaje
- **THEN** el estado dice «Conectado», «Conectado · sin mando», «Conectado · sin pulso», «Esperando conexión…», «Sin wifi», «No visible en la red» o «Sin rodillo», y ninguno nombra Zwift

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el mando esperando, con el estado de la publicación en «Conectado · sin mando» y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando esperando, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin mando», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Zwift no visible en las maquetas de rodaje
Las maquetas de rodaje y de rodaje en vertical SHALL tener el estado no visible, con el estado de la publicación en «No visible en la red» con el borde de alerta, y el resto como en el estado sin app de entrenamiento.

#### Scenario: Rodaje con Zwift no visible
- **WHEN** se elige el estado no visible en la maqueta de rodaje, en horizontal o en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «No visible en la red» con el aro vacío y el borde de alerta y enlaza con Conexión, la barra enlaza con Dispositivos, y el resto se ve como sin app de entrenamiento, sin moverse

### Requirement: Mando sin conexión en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el mando sin conexión, con el estado de la publicación en «Conectado · sin mando», y otro con el pulso sin elegir, en «Conectado · sin pulso», con el resto como en la ruta de Zwift.

#### Scenario: Mando sin conexión en rodaje
- **WHEN** se elige el estado ruta de Zwift con el mando sin conexión, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin mando», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

#### Scenario: Pulso sin elegir en rodaje
- **WHEN** se elige el estado ruta de Zwift con el pulso sin elegir, en horizontal y en vertical
- **THEN** los iconos se ven normales, el estado dice «Conectado · sin pulso», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Maqueta de Dispositivos de la app
La maqueta de Dispositivos SHALL mostrar una sección por tipo (Rodillo, Mando, Pulsómetro y Ventilador), cada una con su fila (modelo recordado o «Ninguno recordado», estado en palabra y ayuda), «Buscar» sin recordado, u «Olvidar» y, sin conexión, «Reconectar», y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado, Bluetooth apagado y los del pulsómetro y el ventilador. SHALL NOT mostrar lo publicado ni «Buscar dispositivos».

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** las cuatro filas dicen «Ninguno recordado», cada sección tiene «Buscar» y ninguna tiene «Olvidar»

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** las filas Rodillo y Mando dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo, y tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila Mando dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y las filas recordadas dicen «Sin conexión»

#### Scenario: Sin Zwift
- **WHEN** se revisan todos los estados de la maqueta de Dispositivos
- **THEN** ninguno muestra una fila de Zwift ni de lo publicado

#### Scenario: Botones sin el nombre del tipo
- **WHEN** se revisan los botones de cualquier sección
- **THEN** dicen «Buscar», «Reconectar» u «Olvidar» según el estado, y su etiqueta accesible nombra el tipo («Reconectar mando»)

### Requirement: Maqueta de Botones desde Ajustes
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift y acciones de la app, cada uno con una fila por función o acción con sus botones y gestos y enlace a Asignar acción, los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», sin navegación inferior y con un botón de volver a Ajustes. Sus estados SHALL ser asignación por defecto, asignación modificada, sin Square recordado y confirmar la restauración.

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
- **THEN** no hay estado de mando desactivado: su lugar lo ocupa el estado sin Square recordado

#### Scenario: Sin Square recordado en Botones
- **WHEN** se elige el estado sin Square recordado
- **THEN** el grupo de Zwift dice que el mando no se publica hasta recordar un Square, con sus filas iguales

### Requirement: Iconos de la barra inferior de las maquetas
Las maquetas con barra inferior SHALL mostrar la pestaña Conexión con su icono propio, distinto del de Dispositivos, y la entrada «Botones» de Ajustes con el icono de los cuatro símbolos; el mando SHALL conservar su icono en las maquetas donde aparece.

#### Scenario: Barra inferior de las maquetas
- **WHEN** se abre Conexión, Dispositivos o Ajustes en la maqueta
- **THEN** la barra tiene Rodar, Conexión, Dispositivos y Ajustes, la de Conexión con su icono propio

#### Scenario: Entrada de Botones
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la entrada «Botones» lleva el icono de los cuatro símbolos

### Requirement: Sección Rodillo en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL tener «Modelo de resistencia», con «Simple», «Por tabla» y «Físico», «Tabla de marchas» con «Por tabla», «Dificultad», «Potencia mínima» con «Físico» y «Bajadas», sin «Pendiente sin Zwift», y la maqueta SHALL NOT tener sección «Zwift».

#### Scenario: Opciones de Rodillo
- **WHEN** se abre la maqueta de Ajustes
- **THEN** la sección Rodillo muestra el modelo de resistencia y sus opciones, sin «Pendiente sin Zwift», y no hay sección «Zwift»

## REMOVED Requirements

### Requirement: Maqueta de Plataformas
**Reason**: La pantalla pasa a ser Conexión, sin plataforma ni interruptor del mando.
**Migration**: Ver «Maqueta de Conexión» y «Estados de la maqueta de Conexión»; `maquetas/plataformas.html` pasa a `maquetas/conexion.html`.
