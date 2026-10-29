# Spec Delta

## MODIFIED Requirements

### Requirement: Rodaje como pantalla inicial
La app SHALL abrirse en la pantalla de rodaje, a pantalla completa y sin las barras del sistema, en la orientación del móvil: en horizontal o en vertical, y girando con él salvo que el bloqueo de rotación del sistema esté activado.

#### Scenario: Abrir la app
- **WHEN** se abre la app con el móvil en el soporte en vertical
- **THEN** se ve la pantalla de rodaje en vertical

#### Scenario: Girar el móvil
- **WHEN** se está en Rodaje en la marcha 14 y se gira el móvil a horizontal
- **THEN** la pantalla pasa a la disposición horizontal, sigue en la marcha 14 y no se pierde ninguna conexión

#### Scenario: Bloqueo de rotación
- **WHEN** el bloqueo de rotación del sistema está activado en vertical y se gira el móvil
- **THEN** la pantalla de rodaje sigue en vertical

## ADDED Requirements

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo las conexiones, el modo con la transmisión, la marcha con su escalera, la potencia y la cadencia en una fila de 2 columnas que ocupa el resto del alto, y al pie el aviso de acción o la ayuda para conectar, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados y la marcha es la 14
- **THEN** se ven, sin desplazamiento, los chips del Neo y del Square, «SIM» y «Lineal · 24 marchas», «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los dos chips dicen «sin conexión», la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»
