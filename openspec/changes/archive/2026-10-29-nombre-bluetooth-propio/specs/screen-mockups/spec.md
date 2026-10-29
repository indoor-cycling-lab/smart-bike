# Spec Delta

## MODIFIED Requirements

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
