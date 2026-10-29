# Spec Delta

## ADDED Requirements

### Requirement: Pulso hacia Zwift
La app SHALL ofrecer a Zwift, además de rodillo, potencia y cadencia, una fuente de pulso que Zwift lista como «Square Shift» en la lista de pulso. Con Zwift conectado y suscrito al pulso, la app SHALL enviarle los bpm del pulsómetro cada vez que la banda da una lectura válida. Mientras no haya pulso, la app SHALL NOT enviarle nada (ni un 0). El pulso SHALL NOT cambiar la potencia, la cadencia, la pendiente ni la marcha.

#### Scenario: Emparejar el pulso en Zwift para PC
- **WHEN** la app está abierta, el móvil y el PC están en la misma red wifi y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** «Square Shift» aparece por wifi también en la lista de pulso y se puede elegir

#### Scenario: Pedaleando con la banda
- **WHEN** el pulsómetro da 140 bpm y Zwift está conectado y suscrito al pulso
- **THEN** Zwift muestra 140 bpm y los cambios que dé la banda llegan sin cortes

#### Scenario: Pulsómetro sin lectura
- **WHEN** Zwift está suscrito al pulso, la banda se ha desconectado y no hay pulso
- **THEN** la app no envía pulso a Zwift, que lo trata como sensor sin datos, y la potencia y la cadencia siguen llegando

#### Scenario: Banda de vuelta
- **WHEN** no había pulso, Zwift sigue suscrito y la banda vuelve a dar lecturas válidas
- **THEN** Zwift vuelve a recibir los bpm sin volver a emparejar

#### Scenario: Sin pulsómetro recordado
- **WHEN** no hay pulsómetro recordado y Zwift está conectado
- **THEN** la potencia, la cadencia y el rodillo funcionan como siempre, y la fuente de pulso de «Square Shift» no envía nada

#### Scenario: Zwift sin suscribirse al pulso
- **WHEN** Zwift está conectado pero no ha elegido «Square Shift» como fuente de pulso
- **THEN** la app no envía pulso, y no cambia nada de lo demás

#### Scenario: Zwift vuelve a otra sesión
- **WHEN** «Square Shift» se eligió como fuente de pulso en una sesión anterior y se abren la app y Zwift otra vez
- **THEN** Zwift lo reconoce como la misma fuente y recibe el pulso sin volver a elegirla

#### Scenario: Otro equipo de Zwift
- **WHEN** Zwift está conectado desde un PC y se conecta Zwift desde otro equipo de la red
- **THEN** el pulso va al último que se ha conectado, como lo demás
