## ADDED Requirements

### Requirement: Cambiar el modo tocándolo
Con el ajuste «Cambiar de modo tocando» activado, el indicador de modo de Rodaje SHALL ser un botón de al menos 48 dp que alterna Libre o SIM ⇄ ERG manual con el mismo efecto que la acción «Alternar SIM ⇄ ERG» del Square, sin ventana de confirmación. Con el ajuste desactivado (por defecto), SHALL ser solo una etiqueta, aunque los botones − y + estén visibles, sin respuesta al toque.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez y se toca el indicador de modo
- **THEN** no cambia nada y el indicador no se ve como un botón

#### Scenario: De Libre a ERG manual
- **WHEN** el ajuste está activado, se rueda sin Zwift en la marcha 14 con la última potencia de ERG manual en 200 W y se toca el indicador «LIBRE»
- **THEN** el indicador pasa a «ERG MANUAL», el Neo pasa a 200 W, la marcha sigue siendo la 14 y el pie dice «ERG manual · 200 W»

#### Scenario: De ERG manual a Libre
- **WHEN** el ajuste está activado, se está en ERG manual con la marcha 14 sin Zwift y se toca el indicador «ERG MANUAL»
- **THEN** el indicador pasa a «LIBRE», el Neo recibe la pendiente de la marcha 14 y el pie dice «Modo Libre»

#### Scenario: En una ruta de la app
- **WHEN** el ajuste está activado, se rueda en una ruta de la app y se toca el indicador «SIM»
- **THEN** el indicador pasa a «ERG MANUAL» con la potencia recordada, como con el Square

#### Scenario: En un entrenamiento de la app
- **WHEN** el ajuste está activado, Zwift pide 230 W en un entrenamiento y se toca el indicador «ERG APP»
- **THEN** el Neo sigue a 230 W, el indicador sigue en «ERG APP» y el pie dice «Entrenamiento de la app» durante 1,5 s

#### Scenario: Botones visibles sin el ajuste
- **WHEN** los botones − y + están visibles (Square sin conexión) y el ajuste de cambiar de modo está desactivado
- **THEN** el indicador sigue siendo una etiqueta, sin respuesta al toque

#### Scenario: Sin Square
- **WHEN** no hay ningún Square configurado, el ajuste está activado y se toca el indicador
- **THEN** el modo alterna igual, sin necesitar el controlador

#### Scenario: Neo ausente
- **WHEN** el Neo no está conectado, el ajuste está activado y se toca el indicador
- **THEN** el modo alterna y, al conectarse el Neo, recibe la potencia o la pendiente vigente

#### Scenario: Toque repetido
- **WHEN** se toca el indicador dos veces seguidas
- **THEN** el modo vuelve al de partida con la marcha y la potencia como estaban, sin perder ninguna conexión

### Requirement: Espacio del indicador de modo tocable
El indicador de modo tocable SHALL mantener su objetivo táctil de 48 dp sin solaparse con el icono de Ajustes ni con la barra de conexiones, tanto en la fila del modo en vertical como en la línea superior en horizontal, y al pasar de Libre o SIM a ERG, o al activarse o desactivarse el ajuste, SHALL NOT mover el icono de Ajustes, la barra de conexiones ni la marcha.

#### Scenario: En vertical
- **WHEN** el móvil está en vertical a 360 dp con el ajuste activado
- **THEN** el indicador de modo y el icono de Ajustes tienen cada uno su objetivo táctil de al menos 48 dp, sin solaparse

#### Scenario: En horizontal
- **WHEN** el móvil está en horizontal a la anchura más estrecha que se soporta con el ajuste activado
- **THEN** el indicador de modo, la barra de conexiones y el icono de Ajustes no se solapan y todo se ve sin desplazamiento

#### Scenario: Al cambiar de modo
- **WHEN** se toca el indicador y pasa de «LIBRE» a «ERG MANUAL»
- **THEN** la marcha, los datos y el icono de Ajustes no se mueven
