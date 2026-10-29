## ADDED Requirements

### Requirement: Nombre que hay que elegir en Zwift
La ayuda de la fila de Zwift y de la fila del mando en Dispositivos SHALL nombrar el dispositivo que hay que elegir en el emparejamiento de Zwift con el nombre con el que la app se anuncia en ese momento: «Square Shift», o «KICKR BIKE PRO» seguido de sus cuatro caracteres con «Simular un KICKR BIKE» activado.

#### Scenario: Con el ajuste desactivado
- **WHEN** «Simular un KICKR BIKE» está desactivado y se abre Dispositivos
- **THEN** la ayuda del emparejamiento dice que se elija «Square Shift»

#### Scenario: Con el ajuste activado
- **WHEN** «Simular un KICKR BIKE» está activado y se abre Dispositivos
- **THEN** la ayuda del emparejamiento dice que se elija el «KICKR BIKE PRO» con sus cuatro caracteres, tal como lo lista Zwift

#### Scenario: Cambio rodando
- **WHEN** se rueda con Dispositivos abierto y se activa «Simular un KICKR BIKE»
- **THEN** la ayuda pasa a nombrar el «KICKR BIKE PRO» sin cambiar el estado del Neo ni del Square
