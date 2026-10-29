## MODIFIED Requirements

### Requirement: Estado de conexión por forma y palabra
El chip de conexión SHALL mostrar el nombre del dispositivo y su estado con una forma que no dependa solo del color (punto lleno conectado, anillo abierto girando buscando, anillo vacío con el borde del chip de alerta sin conexión) y, si no está conectado, con una palabra. La variante compacta de la pantalla de rodaje SHALL NOT mostrar la palabra y SHALL darla en su descripción accesible («Square, sin conexión»).

#### Scenario: Zwift sin conexión
- **WHEN** Zwift no está conectado a la app
- **THEN** el chip muestra «Zwift sin conexión» con anillo vacío y borde de alerta

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un dispositivo está buscando
- **THEN** el anillo no gira y el estado se sigue leyendo por la palabra o, en la variante compacta, por la forma: anillo abierto, distinto del anillo vacío con borde de alerta

#### Scenario: Variante compacta sin conexión
- **WHEN** en la variante compacta el Square no está conectado
- **THEN** el chip muestra «Square» con el anillo vacío y el borde de alerta, sin «sin conexión», y un lector de pantalla lee «Square, sin conexión»
