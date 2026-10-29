## ADDED Requirements

### Requirement: Nombre anunciado a Zwift en Diagnóstico
Diagnóstico SHALL mostrar en Conexiones, bajo la fila de Zwift, el nombre con el que la app se anuncia ahora ante Zwift, y SHALL registrar en el registro cada vez que se anuncia o se retira con ese nombre. El nombre SHALL mostrarse también con Zwift sin conectar y sin wifi, como el último que se anunció o se anunciaría.

#### Scenario: Ajuste desactivado
- **WHEN** «Simular un KICKR BIKE» está desactivado y se abre Diagnóstico
- **THEN** bajo la fila de Zwift se muestra «Square Shift» como nombre anunciado

#### Scenario: Ajuste activado
- **WHEN** «Simular un KICKR BIKE» está activado y se abre Diagnóstico
- **THEN** bajo la fila de Zwift se muestra el «KICKR BIKE PRO» con sus cuatro caracteres y el registro tiene el anuncio con ese nombre

#### Scenario: Cambio del ajuste
- **WHEN** se cambia «Simular un KICKR BIKE» con Diagnóstico abierto
- **THEN** el registro tiene la retirada del nombre anterior y el anuncio del nuevo, y bajo la fila de Zwift se muestra el nuevo

#### Scenario: Sin wifi
- **WHEN** no hay wifi y se abre Diagnóstico con el ajuste activado
- **THEN** bajo la fila de Zwift se muestra el nombre que se anunciará, junto al estado sin conexión
