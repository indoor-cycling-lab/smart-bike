# Spec Delta

## MODIFIED Requirements

### Requirement: Herramienta de revisión de estado y tema
Cada maqueta SHALL tener una barra de revisión, visiblemente distinta de la app, con un selector de tema (rodaje o claro) y, si tiene varios estados, un selector de estado; el estado y el tema SHALL ir en la dirección para poder enlazarlos. La dirección SHALL admitir además una vista de captura (`vista=captura`) que oculta la barra de revisión y muestra solo la pantalla de la app, para fotografiarla.

#### Scenario: Enlace a un estado
- **WHEN** se abre `maquetas/rodaje.html#estado=tope&tema=claro`
- **THEN** se ve la pantalla de rodaje en el estado de tope y en tema claro, con los selectores mostrando esos valores

#### Scenario: Sin JavaScript
- **WHEN** se abre una maqueta con JavaScript desactivado
- **THEN** se ven todos sus estados apilados

#### Scenario: Vista de captura
- **WHEN** se abre `maquetas/rodaje.html#estado=tope&tema=claro&vista=captura`
- **THEN** se ve solo la pantalla de rodaje en ese estado y tema, sin la barra de revisión
