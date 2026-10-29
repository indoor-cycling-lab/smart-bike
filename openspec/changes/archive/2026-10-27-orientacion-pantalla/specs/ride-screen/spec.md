## MODIFIED Requirements

### Requirement: Rodaje como pantalla inicial
La app SHALL abrirse en la pantalla de rodaje, a pantalla completa y sin las barras del sistema, en la orientación que marque el ajuste «Orientación»: con «Según el móvil», en la orientación del móvil, en horizontal o en vertical, y girando con él salvo que el bloqueo de rotación del sistema esté activado; con «Vertical» u «Horizontal», siempre en esa orientación.

#### Scenario: Abrir la app
- **WHEN** se abre la app con el móvil en el soporte en vertical y «Según el móvil» elegido
- **THEN** se ve la pantalla de rodaje en vertical

#### Scenario: Girar el móvil
- **WHEN** se está en Rodaje en la marcha 14 con «Según el móvil» elegido y se gira el móvil a horizontal
- **THEN** la pantalla pasa a la disposición horizontal, sigue en la marcha 14 y no se pierde ninguna conexión

#### Scenario: Bloqueo de rotación
- **WHEN** «Según el móvil» está elegido, el bloqueo de rotación del sistema está activado en vertical y se gira el móvil
- **THEN** la pantalla de rodaje sigue en vertical

#### Scenario: Abrir la app con una orientación fija
- **WHEN** «Horizontal» está elegido y se abre la app con el móvil en vertical
- **THEN** se ve la pantalla de rodaje en la disposición horizontal

#### Scenario: Girar el móvil con una orientación fija
- **WHEN** se está en Rodaje en la marcha 14 con «Vertical» elegido y se gira el móvil a horizontal
- **THEN** la pantalla sigue en la disposición vertical, en la marcha 14, sin parpadeos ni pérdida de conexiones
