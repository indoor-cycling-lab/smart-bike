## ADDED Requirements

### Requirement: Casilla para cambiar de modo tocando
Ajustes SHALL ofrecer en «Al rodar» la casilla «Cambiar de modo tocando», independiente de «Botones − y + en pantalla», que activa el cambio de modo Libre o SIM ⇄ ERG manual tocando el indicador de Rodaje. Su ayuda SHALL explicar qué hace y que no cambia el modo durante un entrenamiento de la app. SHALL estar desactivada por defecto y las configuraciones guardadas antes del change SHALL quedar con ella desactivada. Su valor SHALL viajar en la copia de seguridad.

#### Scenario: Ayuda de la casilla
- **WHEN** se abre Ajustes y se lee la casilla «Cambiar de modo tocando»
- **THEN** su ayuda dice que se toca el indicador de modo para pasar de Libre o SIM a ERG manual y volver, y que no vale en un entrenamiento de la app

#### Scenario: Activar el cambio de modo
- **WHEN** se activa la casilla y se vuelve a Rodaje
- **THEN** el indicador de modo se puede tocar, sin que aparezcan los botones − y + si no estaban

#### Scenario: Independiente de los botones
- **WHEN** se activa «Botones − y + en pantalla» con «Cambiar de modo tocando» desactivada
- **THEN** Rodaje muestra los botones y el indicador de modo sigue siendo una etiqueta

#### Scenario: Desactivar rodando
- **WHEN** se desactiva la casilla con la sesión en marcha en ERG manual
- **THEN** el indicador deja de responder al toque, el Neo sigue en ERG manual y la sesión no se corta

#### Scenario: Configuración anterior
- **WHEN** se abre la app con una configuración guardada antes de este change, tuviera o no los botones activados
- **THEN** «Cambiar de modo tocando» está desactivada y el indicador es una etiqueta

#### Scenario: Copia de seguridad
- **WHEN** se exporta la configuración con la casilla activada y se importa
- **THEN** la casilla queda activada
