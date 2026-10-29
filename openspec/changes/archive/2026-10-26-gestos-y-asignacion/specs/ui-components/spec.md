## MODIFIED Requirements

### Requirement: Fila de asignación con su capa
La fila de asignación SHALL mostrar el botón o la combinación, el gesto, la acción y si la acción va a Zwift o la resuelve la app, y SHALL tener un estado de captura. Una fila de Zwift solo informativa SHALL distinguirse de una que se puede tocar por la falta de chevron y de estado táctil, y una acción sin entrada SHALL mostrar «Sin botón» en lugar de las teclas.

#### Scenario: Función de Zwift
- **WHEN** se muestra la asignación de Y
- **THEN** la fila dice «Y · pulsar · Usar powerup» con la etiqueta ZWIFT

#### Scenario: Acción sin botón
- **WHEN** se muestra una acción de la app sin ninguna entrada asignada
- **THEN** la fila dice «Sin botón» en `ink-muted` y la acción, con la etiqueta APP

### Requirement: Captura de un gesto
El panel de captura SHALL esperar a que el usuario pulse en el Square el botón, gesto o combinación, mostrarlo con sus teclas y avisar si ya está asignado a otra acción. Sin el Square conectado, SHALL decirlo en lugar de esperar.

#### Scenario: Combinación ya asignada
- **WHEN** se captura Campag. D + ↑, que ya sube 10 W en ERG
- **THEN** se muestra la combinación y un aviso de error que dice qué acción se quedará sin botón si se guarda

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado
- **THEN** el panel dice «Conecta el Square para capturar un botón», sin el borde de escucha
