## MODIFIED Requirements

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL abrirse desde la entrada «Botones» de Ajustes, con un botón de volver a Ajustes, y SHALL mostrar de qué transmisión es la asignación y dos grupos, en este orden: «Acciones de la app» y «Funciones de Zwift», con una fila por función o acción de esa transmisión con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una fila de cualquiera de los dos grupos SHALL abrir Asignar acción.

#### Scenario: Abrir desde Ajustes
- **WHEN** en Ajustes se toca «Botones»
- **THEN** se abre Botones con la asignación de la transmisión elegida, sin barra inferior y con un botón de volver a Ajustes

#### Scenario: Orden de los grupos
- **WHEN** se abre Botones
- **THEN** «Acciones de la app» aparece antes que «Funciones de Zwift», y ambos antes de los gestos y de «Restaurar asignación por defecto»

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones con un Square configurado y el modo «Wifi + Bluetooth»
- **THEN** el grupo de Zwift dice que esas funciones llegan a la app de entrenamiento por el mando de la app, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con el mando desactivado
- **WHEN** se abre Botones sin un Square configurado
- **THEN** el grupo de Zwift dice que, sin un Square configurado, el mando no se publica y esas funciones no llegan a la app de entrenamiento, y sus filas siguen pudiéndose asignar

#### Scenario: Transmisión de la asignación
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** la pantalla dice que es la asignación de Shimano 105 y que cada transmisión tiene la suya
