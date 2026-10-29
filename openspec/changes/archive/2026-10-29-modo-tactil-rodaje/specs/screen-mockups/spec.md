## ADDED Requirements

### Requirement: Modo tocable en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL tener estados con el cambio de modo activado en los que el indicador de modo se ve como un botón con relleno tintado y borde marcado, sin glifo (Libre, SIM, ERG manual y ERG APP), y estados con el ajuste desactivado en los que sigue siendo una etiqueta, con o sin los botones − y + visibles. En los estados tocables, el selector de estado SHALL permitir ver el cambio SIM ⇄ ERG manual con su aviso del pie, y todos SHALL verse en los dos temas.

#### Scenario: Modo tocable en Libre
- **WHEN** se abre el estado de Libre tocable, en vertical y en horizontal
- **THEN** el indicador «LIBRE» se ve como un botón junto al icono de Ajustes, sin solaparse con él ni con la barra de conexiones

#### Scenario: Modo tocable en ERG manual
- **WHEN** se abre el estado de ERG manual tocable
- **THEN** el indicador «ERG MANUAL» se ve como un botón y el pie dice «ERG manual · 200 W»

#### Scenario: Sin el ajuste
- **WHEN** se abre un estado de rodaje con el cambio de modo desactivado, con los botones − y + visibles o sin ellos
- **THEN** el indicador de modo se ve como la etiqueta de hoy, sin aspecto de botón

#### Scenario: Entrenamiento de la app
- **WHEN** se abre el estado de un entrenamiento ERG APP tocable
- **THEN** el indicador «ERG APP» se ve como un botón, igual que los demás, y el pie muestra «Entrenamiento de la app» al tocarlo

### Requirement: Casilla del cambio de modo en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar, en «Al rodar», la casilla «Cambiar de modo tocando» con su ayuda, activada y desactivada, además de la casilla «Botones − y + en pantalla» sin cambios.

#### Scenario: Casilla desactivada
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la casilla «Cambiar de modo tocando» está desactivada y su ayuda dice qué hace y que solo vale fuera de un entrenamiento de la app

#### Scenario: Casilla activada
- **WHEN** se abre el estado de Ajustes con el cambio de modo activado
- **THEN** la casilla se ve activada por algo más que el color
