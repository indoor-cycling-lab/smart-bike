# Spec Delta

## MODIFIED Requirements

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL abrirse desde la entrada «Botones» de Ajustes, con un botón de volver a Ajustes, y SHALL mostrar de qué transmisión es la asignación y dos grupos: «Funciones de Zwift» y «Acciones de la app», con una fila por función o acción de esa transmisión con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una fila de cualquiera de los dos grupos SHALL abrir Asignar acción. El grupo de Zwift SHALL decir si las funciones llegan a Zwift por el mando de la app o, sin el mando (desactivado en Plataformas), que no llegan por la app; las filas se mantienen siempre, para no perder la asignación. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Abrir desde Ajustes
- **WHEN** en Ajustes se toca «Botones»
- **THEN** se abre Botones con la asignación de la transmisión elegida, sin barra inferior y con un botón de volver a Ajustes

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones con «Mando de Zwift» activado
- **THEN** el grupo de Zwift dice que esas funciones llegan a Zwift por el mando de la app, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con el mando desactivado
- **WHEN** se abre Botones con «Mando de Zwift» desactivado en Plataformas
- **THEN** el grupo de Zwift dice que, sin el mando, esas funciones no llegan a Zwift por la app, y sus filas siguen pudiéndose asignar

#### Scenario: Transmisión de la asignación
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** la pantalla dice que es la asignación de Shimano 105 y que cada transmisión tiene la suya

## ADDED Requirements

### Requirement: Mando de Zwift en Zwift Ride para la asignación
Con el modo «Todo por wifi (modo Zwift Ride)», el grupo de Zwift de Botones SHALL decir que las funciones llegan a Zwift por el mando de la app, que va dentro del mismo dispositivo por wifi.

#### Scenario: Funciones de Zwift con Zwift Ride
- **WHEN** el modo es Zwift Ride y se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones llegan a Zwift por el mando de la app, y tocar «Ride On» abre Asignar acción

## REMOVED Requirements

### Requirement: Botones como pestaña
**Reason**: Botones deja de ser una pestaña de la barra inferior y pasa a ser una pantalla secundaria de Ajustes; la barra tiene ahora Plataformas.
**Migration**: Spec `button-mapping`, «Pantalla de Botones» (se abre desde Ajustes) y spec `app-navigation`, «Pantallas secundarias».
