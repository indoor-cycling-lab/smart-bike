## ADDED Requirements

### Requirement: Simular un KICKR BIKE en las maquetas
Las maquetas SHALL mostrar «Simular un KICKR BIKE» en la sección «Zwift» de Ajustes, con su ayuda, en un estado con el interruptor desactivado y en otro con él activado; la ayuda de emparejamiento de Dispositivos con el nombre que se anuncia; y el nombre anunciado en Conexiones de Diagnóstico, bajo la fila de Zwift, en los dos casos.

#### Scenario: Ajuste desactivado en Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la sección «Zwift» muestra «Simular un KICKR BIKE» desactivado con su ayuda

#### Scenario: Ajuste activado en Ajustes
- **WHEN** se elige el estado de simulación de KICKR BIKE de `maquetas/ajustes.html`
- **THEN** «Simular un KICKR BIKE» aparece activado, con la ayuda de que es una prueba y hay que emparejar de nuevo

#### Scenario: Nombre en Dispositivos
- **WHEN** se elige el estado de simulación de KICKR BIKE de `maquetas/dispositivos.html`
- **THEN** la ayuda del emparejamiento nombra el «KICKR BIKE PRO» con sus cuatro caracteres y, en el resto de estados, «Square Shift»

#### Scenario: Nombre en Diagnóstico
- **WHEN** se abren los estados de `maquetas/diagnostico.html` con y sin la simulación de KICKR BIKE
- **THEN** Conexiones muestra, bajo la fila de Zwift, «KICKR BIKE PRO» con sus cuatro caracteres o «Square Shift», según el estado

#### Scenario: Índice, temas y ancho
- **WHEN** se abre `maquetas/index.html` y se revisan los estados nuevos en los dos temas y a 360 dp
- **THEN** los estados nuevos están enlazados desde el índice, se leen sin cortes y cumplen el contraste de 4,5:1
