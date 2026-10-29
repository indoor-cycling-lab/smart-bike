## ADDED Requirements

### Requirement: Barra de conexiones con iconos
La barra de conexiones SHALL mostrar un icono de 18 dp por enlace, sin nombres, con el estado por la forma: normal si está conectado; atenuado con una marca girando en la esquina si espera o se conecta; tachado en el color de alerta sin conexión. Su ancho SHALL depender solo del número de enlaces. SHALL ser un único objetivo táctil de al menos 48 dp de alto, con una descripción accesible que da el estado de cada enlace.

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
- **WHEN** el lector de pantalla llega a la barra con el Neo conectado y el Square sin conexión
- **THEN** la lee como un solo elemento: «Conexiones: Neo, conectado; Square, sin conexión»

#### Scenario: Contraste en los dos temas
- **WHEN** se ve la barra en tema rodaje y en tema claro
- **THEN** los iconos en los tres estados se distinguen del fondo con al menos 3:1

### Requirement: Icono de cada enlace
Cada enlace SHALL tener un icono propio, el mismo en Rodaje, Dispositivos y Diagnóstico: el Neo, un rayo; el Square, una bicicleta con ciclista; Zwift, una «Z» en un recuadro redondeado, que SHALL NOT imitar el logotipo de Zwift; y el mando de Zwift, un mando de videojuegos. Dispositivos y Diagnóstico SHALL mostrar el icono junto al nombre de cada enlace, de modo que sirvan de leyenda para la barra de Rodaje.

#### Scenario: Leyenda en Dispositivos
- **WHEN** se abre Dispositivos
- **THEN** las filas del Neo, del Square, de Zwift y del mando llevan delante el mismo icono que en la barra de Rodaje

#### Scenario: Diagnóstico coherente
- **WHEN** se abre Diagnóstico
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos, sin que ninguno represente un enlace distinto en otra pantalla

### Requirement: Chip de conexión con forma y palabra
El chip de conexión SHALL mostrar el nombre del dispositivo y su estado con una forma que no dependa solo del color (punto lleno conectado, anillo abierto girando buscando, anillo vacío con el borde del chip de alerta sin conexión) y, si no está conectado, con una palabra. Se usa fuera de Rodaje.

#### Scenario: Zwift sin conexión
- **WHEN** Zwift no está conectado a la app
- **THEN** el chip muestra «Zwift sin conexión» con anillo vacío y borde de alerta

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un dispositivo está buscando
- **THEN** el anillo no gira y el estado se sigue leyendo por la palabra

## REMOVED Requirements

### Requirement: Estado de conexión por forma y palabra
**Reason**: Rodaje deja de usar la variante compacta del chip, sin palabra, y pasa a la barra de conexiones con iconos. El chip con palabra sigue igual fuera de Rodaje.
**Migration**: El chip con palabra queda en «Chip de conexión con forma y palabra». Rodaje usa «Barra de conexiones con iconos».
