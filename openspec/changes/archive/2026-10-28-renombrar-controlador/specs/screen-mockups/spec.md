## MODIFIED Requirements

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar, junto al nombre del Neo y del Square, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas; Diagnóstico lleva además los de Zwift y del controlador publicado junto a su nombre.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo y el manillar para el Square, y no hay filas de Zwift ni del controlador publicado

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos para el Neo y el Square, y los de Zwift y del controlador publicado en sus filas, estos dos iguales

### Requirement: Maqueta de Dispositivos de la app
La maqueta de Dispositivos SHALL mostrar una sección por tipo (Rodillo, Controlador, Pulsómetro y Ventilador), cada una con su fila (modelo recordado o «Ninguno recordado», estado en palabra y ayuda), «Buscar» sin recordado, u «Olvidar» y, sin conexión, «Reconectar», y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado, Bluetooth apagado y los del pulsómetro y el ventilador. SHALL NOT mostrar lo publicado ni «Buscar dispositivos».

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** las cuatro filas dicen «Ninguno recordado», cada sección tiene «Buscar» y ninguna tiene «Olvidar»

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** las filas Rodillo y Controlador dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo, y tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila Controlador dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y las filas recordadas dicen «Sin conexión»

#### Scenario: Sin Zwift
- **WHEN** se revisan todos los estados de la maqueta de Dispositivos
- **THEN** ninguno muestra una fila de Zwift ni de lo publicado

#### Scenario: Botones sin el nombre del tipo
- **WHEN** se revisan los botones de cualquier sección
- **THEN** dicen «Buscar», «Reconectar» u «Olvidar» según el estado, y su etiqueta accesible nombra el tipo («Reconectar controlador»)

### Requirement: Maqueta de Conexión
La maqueta `maquetas/conexion.html` SHALL mostrar la introducción, los avisos, la sección de estado (rodillo, controlador Zwift y pulso con por dónde van, su estado y su ayuda), la sección de modo con los tres modos, su detalle y su compatibilidad, «Nombre ante la app» solo en los modos con wifi y la navegación inferior con Conexión marcada.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/conexion.html#estado=inicio`
- **THEN** se ve «Wifi + Bluetooth» elegido y recomendado, «Smart Bike» elegido, el rodillo, el controlador Zwift y el pulso «Esperando», sin interruptor del controlador ni elección de plataforma, y la pestaña Conexión marcada

#### Scenario: Orden de la pantalla
- **WHEN** se abre cualquier estado de la maqueta de Conexión
- **THEN** de arriba abajo se ven la introducción, los avisos si los hay, el estado, el modo y, con wifi, «Nombre ante la app»

### Requirement: Estados de la maqueta de Conexión
La maqueta de Conexión SHALL tener los estados: todo conectado; rodillo conectado con controlador y pulso esperando; «Solo Bluetooth»; «Solo wifi (modo Zwift Ride)»; sin pulsómetro; sin Square; primer uso sin rodillo; pulso sin lectura; Neo y Square reconectando; KICKR BIKE PRO; no visible; sin wifi; Bluetooth apagado; y móvil sin modo periférico.

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el rodillo y el controlador dicen «Conectado» y el pulso «Enviando», sin mostrar los bpm

#### Scenario: Servicios no ofrecidos
- **WHEN** se eligen los estados sin pulsómetro y sin Square
- **THEN** el pulso o el controlador dicen «No se ofrece», atenuados, con qué dispositivo falta y el enlace a Dispositivos

#### Scenario: Primer uso
- **WHEN** se elige el estado primer uso sin rodillo
- **THEN** se ve el aviso de que la app no publica nada hasta recordar un rodillo y las tres filas dicen «No se ofrece»

#### Scenario: Dispositivos reconectando
- **WHEN** se elige el estado Neo y Square reconectando
- **THEN** el rodillo y el controlador siguen «Conectado» y cada uno añade que su dispositivo se está reconectando

#### Scenario: Solo Bluetooth
- **WHEN** se elige el estado «Solo Bluetooth»
- **THEN** el modo está elegido sin la marca de recomendado, no hay «Nombre ante la app», los tres servicios dicen «por Bluetooth» y las ayudas nombran el móvil por su nombre Bluetooth

#### Scenario: Solo wifi
- **WHEN** se elige el estado «Solo wifi (modo Zwift Ride)»
- **THEN** los tres servicios dicen «por wifi», el controlador dice que va dentro del mismo dispositivo y la descripción del modo dice que la app de entrenamiento mostrará su propia marcha

#### Scenario: No visible y sin wifi
- **WHEN** se eligen los estados no visible y sin wifi
- **THEN** arriba se ve el aviso correspondiente y el rodillo dice «No visible» o «Sin wifi» con el aro vacío y el borde de alerta

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado con «Wifi + Bluetooth»
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth», el controlador dice «Sin Bluetooth» y el rodillo conserva su estado por wifi

#### Scenario: Acceso desde Rodaje
- **WHEN** en las maquetas de rodaje se toca el estado de la publicación
- **THEN** se abre la maqueta de Conexión, y volver abre Rodaje

### Requirement: Barra de conexiones y plataforma en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener la barra de conexiones de los dispositivos (un icono por enlace, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, y el estado de la publicación en palabras, sin nombre de plataforma, en la fila de Ajustes y enlazando con Conexión.

#### Scenario: Enlaces de la barra y de la plataforma
- **WHEN** se toca la barra de conexiones o el estado de la publicación
- **THEN** se abre Dispositivos o Conexión respectivamente

#### Scenario: Estados de la publicación
- **WHEN** se recorren los estados de la maqueta de rodaje
- **THEN** el estado dice «Conectado», «Conectado · sin controlador», «Conectado · sin pulso», «Esperando conexión…», «Sin wifi», «No visible en la red» o «Sin rodillo», y ninguno nombra Zwift

### Requirement: Mando de Zwift en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el controlador esperando, con el estado de la publicación en «Conectado · sin controlador» y el resto como en la ruta de Zwift.

#### Scenario: Mando esperando en rodaje
- **WHEN** se elige el estado ruta de Zwift con el controlador esperando, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin controlador», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

### Requirement: Mando sin conexión en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener un estado con el rodillo conectado y el controlador sin conexión, con el estado de la publicación en «Conectado · sin controlador», y otro con el pulso sin elegir, en «Conectado · sin pulso», con el resto como en la ruta de Zwift.

#### Scenario: Mando sin conexión en rodaje
- **WHEN** se elige el estado ruta de Zwift con el controlador sin conexión, en horizontal y en vertical
- **THEN** los iconos del Neo y del Square se ven normales, el estado dice «Conectado · sin controlador», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift

#### Scenario: Pulso sin elegir en rodaje
- **WHEN** se elige el estado ruta de Zwift con el pulso sin elegir, en horizontal y en vertical
- **THEN** los iconos se ven normales, el estado dice «Conectado · sin pulso», la barra no cambia de ancho y nada más cambia respecto a la ruta de Zwift
