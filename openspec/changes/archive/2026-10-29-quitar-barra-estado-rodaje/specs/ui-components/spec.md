# Spec Delta

## MODIFIED Requirements

### Requirement: Barra de conexiones con iconos
La barra de conexiones SHALL mostrar un icono de 14 dp por enlace, sin nombres, con el estado por la forma: normal si está conectado; atenuado con una marca girando en la esquina si espera o se conecta; tachado en el color de alerta sin conexión. Los iconos SHALL ir agrupados, con 4 dp entre ellos, y su ancho SHALL depender solo del número de enlaces. SHALL ser un único objetivo táctil de al menos 48 dp de alto, con una descripción accesible que da el estado de cada enlace.

#### Scenario: Estados sin color
- **WHEN** la barra muestra un enlace conectado, uno esperando y uno sin conexión, y se ve en escala de grises
- **THEN** se distinguen los tres: el icono limpio, el icono con la marca en la esquina y el icono tachado

#### Scenario: Cambio de estado sin moverse
- **WHEN** un enlace pasa de esperando a conectado y después a sin conexión
- **THEN** la barra mantiene su ancho y ningún icono se desplaza

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un enlace espera
- **THEN** la marca de la esquina no gira y sigue siendo distinta del tachado

#### Scenario: Iconos agrupados
- **WHEN** la barra muestra dos, tres o cuatro enlaces
- **THEN** los iconos se leen como un solo bloque, con 4 dp entre ellos, y no como elementos sueltos

#### Scenario: Estados mezclados sin tocarse
- **WHEN** la barra muestra un enlace esperando junto a uno sin conexión
- **THEN** la marca de la esquina del primero y el tachado del segundo no se tocan ni se confunden con el icono vecino

#### Scenario: Seis enlaces
- **WHEN** la barra muestra seis enlaces a 360 dp de ancho, en la fila de la plataforma de Rodaje
- **THEN** caben en una sola línea sin cortarse

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega a la barra con el Neo conectado y el controlador sin conexión
- **THEN** la lee como un solo elemento: «Conexiones: Neo, conectado; Controlador, sin conexión»

#### Scenario: Contraste en los dos temas
- **WHEN** se ve la barra en tema rodaje y en tema claro
- **THEN** los iconos en los tres estados se distinguen del fondo con al menos 3:1

## REMOVED Requirements

### Requirement: Indicadores de estado del móvil
**Reason**: Rodaje deja de repetir la hora, el wifi y la batería del móvil; los muestran las barras del sistema.
**Migration**: Ninguna; se elimina el componente `DeviceStatus` (`sq-devstatus`) del sistema de diseño y de la app.
