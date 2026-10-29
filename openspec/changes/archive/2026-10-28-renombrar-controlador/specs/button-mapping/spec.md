## MODIFIED Requirements

### Requirement: Estado de Zwift y ajustes en la pantalla de Botones
El grupo de Zwift SHALL decir que las funciones llegan a la app de entrenamiento por el controlador Zwift de la app y, sin Square recordado, que el controlador no se publica hasta recordar uno; las filas se mantienen siempre, para no perder la asignación. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Con Square recordado
- **WHEN** se abre Botones con un Square recordado
- **THEN** el grupo de Zwift dice que las funciones llegan por el controlador Zwift de la app y sus filas están ahí

#### Scenario: Sin el mando de Zwift
- **WHEN** se abre Botones sin Square recordado
- **THEN** el grupo de Zwift dice que el controlador no se publica hasta recordar un Square en Dispositivos, y sus filas siguen ahí
