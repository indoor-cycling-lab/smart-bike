## MODIFIED Requirements

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL abrirse desde la entrada «Botones» de Ajustes, con un botón de volver a Ajustes, y SHALL mostrar de qué transmisión es la asignación y dos grupos: «Funciones de Zwift» y «Acciones de la app», con una fila por función o acción de esa transmisión con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una fila de cualquiera de los dos grupos SHALL abrir Asignar acción.

#### Scenario: Abrir desde Ajustes
- **WHEN** en Ajustes se toca «Botones»
- **THEN** se abre Botones con la asignación de la transmisión elegida, sin barra inferior y con un botón de volver a Ajustes

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones con un Square recordado y el modo «Wifi + Bluetooth»
- **THEN** el grupo de Zwift dice que esas funciones llegan a la app de entrenamiento por el mando de la app, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con el mando desactivado
- **WHEN** se abre Botones sin un Square recordado
- **THEN** el grupo de Zwift dice que, sin un Square recordado, el mando no se publica y esas funciones no llegan a la app de entrenamiento, y sus filas siguen pudiéndose asignar

#### Scenario: Transmisión de la asignación
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** la pantalla dice que es la asignación de Shimano 105 y que cada transmisión tiene la suya

### Requirement: Estado de Zwift y ajustes en la pantalla de Botones
El grupo de Zwift SHALL decir que las funciones llegan a la app de entrenamiento por el mando de la app y, sin Square recordado, que el mando no se publica hasta recordar uno; las filas se mantienen siempre, para no perder la asignación. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Con Square recordado
- **WHEN** se abre Botones con un Square recordado
- **THEN** el grupo de Zwift dice que las funciones llegan por el mando de la app y sus filas están ahí

#### Scenario: Sin el mando de Zwift
- **WHEN** se abre Botones sin Square recordado
- **THEN** el grupo de Zwift dice que el mando no se publica hasta recordar un Square en Dispositivos, y sus filas siguen ahí

### Requirement: Mando de Zwift en Zwift Ride para la asignación
Con cualquier modo, el grupo de Zwift de Botones SHALL decir por dónde llegan las funciones a la app de entrenamiento: por Bluetooth aparte con «Wifi + Bluetooth», y dentro del mismo dispositivo con «Solo Bluetooth» y «Solo wifi (modo Zwift Ride)».

#### Scenario: Funciones de Zwift con Zwift Ride
- **WHEN** el modo es «Solo wifi (modo Zwift Ride)», hay un Square recordado y se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones llegan por el mando de la app, que va dentro del mismo dispositivo por wifi, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con Wifi + Bluetooth
- **WHEN** el modo es «Wifi + Bluetooth», hay un Square recordado y se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones llegan por el mando de la app, por Bluetooth

## REMOVED Requirements

### Requirement: Aviso de botones que usa Zwift
**Reason**: Desaparece el interruptor «Mando de Zwift», que permitía tener el Square emparejado directamente en Zwift. El mando de la app se publica siempre que hay un Square recordado.
**Migration**: Si se quiere el Square directamente en Zwift, basta con no emparejar el mando de la app en Zwift; la app ya no avisa de los botones que Zwift leería.
