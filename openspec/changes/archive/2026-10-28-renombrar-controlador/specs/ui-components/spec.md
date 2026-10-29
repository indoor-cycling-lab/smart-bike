## MODIFIED Requirements

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

### Requirement: Barra de conexiones con iconos
La barra de conexiones SHALL mostrar un icono de 14 dp por enlace, sin nombres, con el estado por la forma: normal si está conectado; atenuado con una marca girando en la esquina si espera o se conecta; tachado en el color de alerta sin conexión. Su ancho SHALL depender solo del número de enlaces. SHALL ser un único objetivo táctil de al menos 48 dp de alto, con una descripción accesible que da el estado de cada enlace.

#### Scenario: Estados sin color
- **WHEN** la barra muestra un enlace conectado, uno esperando y uno sin conexión, y se ve en escala de grises
- **THEN** se distinguen los tres: el icono limpio, el icono con la marca en la esquina y el icono tachado

#### Scenario: Cambio de estado sin moverse
- **WHEN** un enlace pasa de esperando a conectado y después a sin conexión
- **THEN** la barra mantiene su ancho y ningún icono se desplaza

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un enlace espera
- **THEN** la marca de la esquina no gira y sigue siendo distinta del tachado

#### Scenario: Seis enlaces
- **WHEN** la barra muestra seis enlaces a 360 dp de ancho, en la franja superior de Rodaje
- **THEN** caben en una sola línea sin cortarse

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega a la barra con el Neo conectado y el controlador sin conexión
- **THEN** la lee como un solo elemento: «Conexiones: Neo, conectado; Controlador, sin conexión»

#### Scenario: Contraste en los dos temas
- **WHEN** se ve la barra en tema rodaje y en tema claro
- **THEN** los iconos en los tres estados se distinguen del fondo con al menos 3:1
