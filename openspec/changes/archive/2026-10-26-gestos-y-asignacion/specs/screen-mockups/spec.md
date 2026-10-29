## MODIFIED Requirements

### Requirement: Botones y asignación
La maqueta de Botones SHALL mostrar dos grupos, funciones de Zwift (informativas) y acciones de la app (con las acciones que existen en la app y sus botones y gestos), los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto», con los estados asignación por defecto, asignación modificada, confirmar la restauración y abierta desde Diagnóstico. La de Asignar acción SHALL mostrar las entradas de la acción, el gesto y los estados esperando, Square sin conexión, combinación libre, entrada ya asignada y botón que usa Zwift.

#### Scenario: Funciones de Zwift primero
- **WHEN** se abre la maqueta de Botones
- **THEN** el primer grupo reproduce un mando oficial de Zwift (palancas, girar, frenar, cruceta, A, B, Y, Z), sin enlaces, y dice que lo resuelve Zwift con el Square emparejado

#### Scenario: Asignación modificada
- **WHEN** se elige el estado de asignación modificada
- **THEN** una acción aparece con «Sin botón», otra con una combinación nueva y «Invertir palancas» activado

#### Scenario: Abierta desde Diagnóstico
- **WHEN** se elige el estado abierta desde Diagnóstico
- **THEN** la pantalla tiene un botón de volver a Diagnóstico y no tiene navegación inferior

#### Scenario: Square sin conexión al asignar
- **WHEN** en Asignar acción se elige el estado Square sin conexión
- **THEN** la zona de captura dice «Conecta el Square para capturar un botón» y «Guardar» está deshabilitado

#### Scenario: Botón que usa Zwift
- **WHEN** en Asignar acción se elige el estado botón que usa Zwift
- **THEN** se ve A capturada y un aviso de que Zwift también la recibirá con el Square emparejado

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver, las conexiones con la acción de buscar, los botones del Square con la máscara y el enlace «Asignación de botones», el último gesto con su acción, la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al Neo y la latencia de la palanca), el tráfico con pausar y limpiar, y el control manual con el último envío.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, hay un botón de volver a Rodaje, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Enlace a Botones
- **WHEN** en la maqueta de Diagnóstico se toca «Asignación de botones»
- **THEN** se abre la maqueta de Botones en el estado abierta desde Diagnóstico
