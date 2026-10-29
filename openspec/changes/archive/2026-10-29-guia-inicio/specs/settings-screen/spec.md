## ADDED Requirements

### Requirement: Entrada «Guía de inicio» en Ajustes
Ajustes SHALL tener una entrada «Guía de inicio», con una ayuda de una línea, que vuelve a mostrar la guía de inicio, abierta en la página 1, y abre Rodaje. Tocar cualquier punto de la fila SHALL activarla y su objetivo táctil SHALL ser de al menos 48 dp.

#### Scenario: Ver la entrada
- **WHEN** se abre Ajustes
- **THEN** hay una entrada «Guía de inicio» con su ayuda de una línea

#### Scenario: Tocar la entrada
- **WHEN** se toca «Guía de inicio»
- **THEN** se abre Rodaje con la hoja abierta en la página 1, sin terminar la sesión ni cambiar la marcha

#### Scenario: Con la guía ya a la vista
- **WHEN** la guía está a la vista y se toca «Guía de inicio»
- **THEN** se abre Rodaje con la hoja abierta en la página 1, sin cambiar nada más
