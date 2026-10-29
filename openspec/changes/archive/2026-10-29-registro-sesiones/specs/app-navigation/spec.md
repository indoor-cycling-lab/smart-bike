# Spec Delta

## MODIFIED Requirements

### Requirement: Pantallas secundarias
Botones, Marchas, Datos en pantalla, Diagnóstico y Sesiones grabadas SHALL abrirse desde Ajustes, y Asignar acción desde Botones, sin barra inferior y con un botón de volver a la pantalla desde la que se abrieron. Abrir o cerrar una pantalla SHALL NOT terminar la sesión ni cambiar la marcha.

#### Scenario: Ajustes a Marchas y vuelta
- **WHEN** en Ajustes se toca «Marchas» y luego volver
- **THEN** se abre Marchas sin barra inferior y después se vuelve a Ajustes con la barra

#### Scenario: Ajustes a Botones y vuelta
- **WHEN** en Ajustes se toca «Botones» y luego volver
- **THEN** se abre Botones sin barra inferior y después se vuelve a Ajustes con la barra

#### Scenario: Ajustes a Sesiones grabadas y vuelta
- **WHEN** en la sección «Depuración» de Ajustes se toca «Sesiones grabadas» y luego volver
- **THEN** se abre Sesiones grabadas sin barra inferior y después se vuelve a Ajustes con la barra, sin cambiar la marcha ni terminar la sesión

#### Scenario: Asignar acción
- **WHEN** en Botones se toca una acción y luego volver
- **THEN** se abre Asignar acción y después se vuelve a Botones
