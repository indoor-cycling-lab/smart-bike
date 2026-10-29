## MODIFIED Requirements

### Requirement: Última combinación pulsada
Diagnóstico SHALL mostrar el último gesto reconocido: los botones que estaban pulsados a la vez, con el modificador (Campagnolo) primero, el gesto («pulsar», «corta», «larga», «doble» o «repetir») y la acción que produjo según la asignación vigente, o «sin acción» si no tiene ninguna.

#### Scenario: Combinación con modificador
- **WHEN** se mantiene Campag. D, se pulsa la palanca D1 y se sueltan los dos
- **THEN** en «Último gesto» se ve «Campag. D + Palanca D1 · pulsar · +3 marchas», y sigue visible después de soltarlos

#### Scenario: Botón sin acción
- **WHEN** se pulsa Y
- **THEN** en «Último gesto» se ve «Y · pulsar · sin acción»

#### Scenario: Tope
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** en «Último gesto» se ve «Palanca D1 · pulsar · Tope · marcha más dura»

#### Scenario: Sin pulsaciones todavía
- **WHEN** el Square está conectado y aún no se ha pulsado nada
- **THEN** «Último gesto» dice «Ninguno todavía»

#### Scenario: Pulsación larga
- **WHEN** la marcha es la 15 y se mantiene □ hasta el umbral de pulsación larga
- **THEN** en «Último gesto» se ve «□ · larga · Fijar la marcha de referencia»

#### Scenario: Repetición
- **WHEN** se mantiene la palanca D1 hasta que repite
- **THEN** en «Último gesto» se ve «Palanca D1 · repetir · +1 marcha»

#### Scenario: Asignación cambiada
- **WHEN** △ corta tiene asignada «−3 marchas» y se pulsa y se suelta △
- **THEN** en «Último gesto» se ve «△ · corta · −3 marchas»

### Requirement: Diagnóstico como pantalla secundaria
Diagnóstico SHALL abrirse desde los chips de conexión de la pantalla de rodaje, con un botón de volver a Rodaje y sus secciones en este orden: conexiones, botones del Square, simulación, tráfico y control manual del Neo. La sección de botones del Square SHALL tener un enlace «Asignación de botones» que abre Botones.

#### Scenario: Volver a Rodaje
- **WHEN** se abre Diagnóstico desde un chip y se toca volver, o se pulsa Atrás
- **THEN** se vuelve a la pantalla de rodaje sin terminar la sesión

#### Scenario: Abrir Botones
- **WHEN** en Diagnóstico se toca «Asignación de botones»
- **THEN** se abre Botones, y al volver se regresa a Diagnóstico
