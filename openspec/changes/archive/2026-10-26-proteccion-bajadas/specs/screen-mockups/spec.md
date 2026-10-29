## ADDED Requirements

### Requirement: Ajuste «Bajadas» en la maqueta de Ajustes
La sección Rodillo de la maqueta de Ajustes SHALL incluir el ajuste «Bajadas» con sus tres opciones y su ayuda, y la maqueta SHALL tener un estado abierto desde Diagnóstico con solo la sección Rodillo y el botón de volver a Diagnóstico.

#### Scenario: Ajustes completa
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección Rodillo muestra «Bajadas» con «Sin protección», «La marcha no crea bajadas» (elegida) y «Nunca empujar», cada una con su ayuda

#### Scenario: Desde Diagnóstico
- **WHEN** en la maqueta de Diagnóstico se toca «Ajustes»
- **THEN** se abre la maqueta de Ajustes con solo la sección Rodillo y el ajuste «Bajadas», y volver lleva a Diagnóstico

### Requirement: Pendiente limitada en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar en Simulación el modo de bajadas y SHALL tener un estado con la pendiente limitada por él, con la indicación en texto y la pendiente sin límite, y SHALL tener el acceso a Ajustes junto al de Botones.

#### Scenario: Bajada de Zwift limitada
- **WHEN** se elige el estado de pendiente limitada
- **THEN** Simulación muestra pendiente Zwift −7,0 %, ajuste −11,0 %, enviada al Neo −7,0 % y «Limitada por Bajadas: sin límite sería −18,0 %»

#### Scenario: Sin límite
- **WHEN** se elige cualquier otro estado con el Neo conectado
- **THEN** Simulación muestra el modo de bajadas y no aparece la indicación de límite
