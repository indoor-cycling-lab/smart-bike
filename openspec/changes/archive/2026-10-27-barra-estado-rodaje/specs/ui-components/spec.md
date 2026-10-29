## ADDED Requirements

### Requirement: Indicadores de estado del móvil
El sistema de diseño SHALL tener un componente de indicadores de estado del móvil con la hora, el wifi y la batería, para Rodaje. La hora SHALL ser `hh:mm` en cifras de ancho fijo. El wifi y la batería SHALL llevar un icono de 14 dp, y la batería, además, el porcentaje. Los estados SHALL distinguirse por la forma, no por el color. El ancho del componente SHALL NOT cambiar con el estado ni con el valor. SHALL ser solo informativo, sin objetivo táctil, con una única descripción accesible.

#### Scenario: Estados sin color
- **WHEN** el componente se ve en escala de grises con el wifi conectado y tachado, y con la batería al 82 %, al 9 % y cargando
- **THEN** se distinguen el wifi normal del tachado, el relleno de cada nivel de batería, el rayo del icono sin él y el número

#### Scenario: Ancho estable
- **WHEN** la hora pasa de 09:59 a 10:00, el wifi de conectado a tachado y la batería del 100 % al 9 %
- **THEN** el componente mantiene su ancho y ningún elemento se desplaza

#### Scenario: Sin objetivo táctil
- **WHEN** se toca la hora, el wifi o la batería
- **THEN** el componente no reacciona y el lector de pantalla no lo anuncia como un botón

#### Scenario: Descripción accesible única
- **WHEN** el lector de pantalla llega al componente con la hora 14:32, el wifi conectado y la batería al 82 % cargando
- **THEN** lo lee como un solo elemento: «14:32, wifi conectado, batería 82 %, cargando», y los iconos van ocultos al lector

#### Scenario: Batería baja
- **WHEN** la batería baja del 15 % sin cargar
- **THEN** el icono pasa al color de alerta con el relleno reducido a una franja mínima, y con el móvil cargando no se ve en el color de alerta

#### Scenario: Batería no disponible
- **WHEN** no se da el nivel de la batería
- **THEN** el componente muestra solo la hora y el wifi, sin hueco

#### Scenario: Contraste en los dos temas
- **WHEN** se ve el componente en tema rodaje y en tema claro
- **THEN** la hora y el porcentaje se leen con al menos 4,5:1 y los iconos, en los tres estados de cada uno, se distinguen del fondo con al menos 3:1

## MODIFIED Requirements

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
- **WHEN** el lector de pantalla llega a la barra con el Neo conectado y el Square sin conexión
- **THEN** la lee como un solo elemento: «Conexiones: Neo, conectado; Square, sin conexión»

#### Scenario: Contraste en los dos temas
- **WHEN** se ve la barra en tema rodaje y en tema claro
- **THEN** los iconos en los tres estados se distinguen del fondo con al menos 3:1
