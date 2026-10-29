# Spec Delta

## ADDED Requirements

### Requirement: Aviso de giro
Con «Mando de Zwift» activado, Rodaje SHALL indicar el giro de Zwift con una flecha a cada lado de la marcha: la izquierda mientras se mantiene el giro a la izquierda y la derecha mientras se mantiene el giro a la derecha, con el botón que tenga asignado el giro. Un giro breve SHALL verse al menos 0,5 s. Con el freno de Zwift de la misma palanca pulsado, SHALL NOT mostrarse su giro, como en Zwift.

#### Scenario: Girar a la izquierda
- **WHEN** se mantiene X (giro a la izquierda por defecto)
- **THEN** se ve la flecha de la izquierda a la izquierda de la marcha y, al soltar, desaparece

#### Scenario: Girar a la derecha
- **WHEN** se mantiene Círculo (giro a la derecha por defecto)
- **THEN** se ve la flecha de la derecha a la derecha de la marcha y, al soltar, desaparece

#### Scenario: Pulsación breve
- **WHEN** el giro a la derecha se asigna a una pulsación corta de un botón y se pulsa
- **THEN** la flecha de la derecha se ve al menos 0,5 s

#### Scenario: Giro con otro botón
- **WHEN** se reasigna el giro a la izquierda a la palanca I2 y se mantiene
- **THEN** se ve la flecha de la izquierda

#### Scenario: Los dos giros
- **WHEN** se mantienen X y Círculo a la vez
- **THEN** se ven las dos flechas

#### Scenario: Freno y giro
- **WHEN** se mantienen el freno izquierdo de Zwift y el giro a la izquierda
- **THEN** no se ve la flecha de la izquierda

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y se mantiene X
- **THEN** no se ve ninguna flecha

#### Scenario: Se pierde el Square girando
- **WHEN** se mantiene X y el Square pierde la conexión
- **THEN** la flecha desaparece

#### Scenario: Aviso de acción a la vez
- **WHEN** se pulsa la palanca D1 (+1 marcha) y, antes de 1,5 s, se mantiene X
- **THEN** el pie sigue diciendo «+1 marcha» y se ve la flecha de la izquierda

### Requirement: Aviso de giro sin mover la pantalla
El indicador de giro SHALL ocupar siempre su sitio, de modo que al aparecer o desaparecer no se mueva ni cambie de tamaño nada, y SHALL NOT tapar la marcha. SHALL NOT depender solo del color y SHALL anunciarse al lector de pantalla («Girando a la izquierda», «Girando a la derecha»).

#### Scenario: Sin movimiento
- **WHEN** aparece o desaparece una flecha
- **THEN** la marcha, los datos, la barra de conexiones y los botones − y + no cambian de sitio ni de tamaño

#### Scenario: Lector de pantalla
- **WHEN** se mantiene el giro a la derecha con el lector de pantalla activado
- **THEN** el lector dice «Girando a la derecha»

#### Scenario: Marcha a la vista
- **WHEN** se mantiene el giro con una marcha de dos cifras en pantalla
- **THEN** la marcha se ve completa en vertical y en horizontal

### Requirement: Batería baja del Square en la barra de conexiones
Con la batería del Square baja (spec `square-battery`) y el Square conectado, el icono del Square de la barra de conexiones SHALL mostrar una marca de aviso que no existe en los demás estados, en el color normal y sin cambiar el ancho de la barra. La descripción accesible de la barra SHALL decir «Square, batería baja». Con el Square reconectando o sin conexión, el icono SHALL mostrar su estado de conexión, no el aviso.

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su batería baja
- **THEN** el icono del Square se ve con la marca de aviso, la descripción accesible dice «Conexiones: Neo, conectado; Square, batería baja; …» y no hay porcentaje en pantalla

#### Scenario: Batería normal
- **WHEN** el Square está conectado y su batería no está baja
- **THEN** el icono del Square se ve normal, sin marca

#### Scenario: Square reconectando con la batería baja
- **WHEN** la batería está baja y el Square pierde la conexión
- **THEN** el icono del Square pasa a atenuado con la marca girando, sin la marca de aviso

#### Scenario: Ancho estable
- **WHEN** el icono del Square pasa de normal a batería baja
- **THEN** la barra no cambia de ancho y nada se mueve

#### Scenario: Batería baja en vertical y en horizontal
- **WHEN** la batería del Square está baja
- **THEN** la marca se ve igual en vertical y en horizontal, en los dos temas

### Requirement: Sin porcentaje de la batería del Square en Rodaje
Rodaje SHALL NOT mostrar el porcentaje de la batería del Square. El aviso de batería baja SHALL NOT competir con el aviso de acción del pie ni ocupar sitio nuevo.

#### Scenario: Sin porcentaje
- **WHEN** la batería del Square está baja y se rueda
- **THEN** no hay ningún porcentaje del Square en pantalla

#### Scenario: Aviso de acción a la vez
- **WHEN** la batería del Square está baja y se pulsa una palanca
- **THEN** el pie dice la acción durante 1,5 s como siempre
