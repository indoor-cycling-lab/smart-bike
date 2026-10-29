## ADDED Requirements

### Requirement: Una asignación por transmisión
Cada transmisión SHALL tener su propia asignación de botones, con su inversión de palancas; los tiempos de los gestos y la autorrepetición SHALL ser comunes. Los botones del Square SHALL hacer lo que diga la asignación de la transmisión elegida, y cambiar de transmisión SHALL cambiar de asignación al momento.

#### Scenario: Cambiar de transmisión
- **WHEN** la transmisión es la lineal y se elige la Shimano en Marchas, en la 52×17
- **THEN** la palanca D1 lleva a la 52×19 en lugar de subir una marcha

#### Scenario: Reasignar sin afectar a la otra
- **WHEN** con la Shimano se asigna △ corta a «Plato más grande» y después se elige la lineal
- **THEN** △ no hace nada con la lineal, y al volver a la Shimano, △ corta cambia al plato grande

#### Scenario: Tiempos comunes
- **WHEN** con la lineal se cambia el umbral de pulsación larga y después se elige la Shimano
- **THEN** la Shimano usa el umbral nuevo

## MODIFIED Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia y fijar la marcha de referencia; con la Shimano, además, plato más grande y plato más pequeño. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app.

#### Scenario: Botón sin acción
- **WHEN** se pulsa A con la asignación por defecto
- **THEN** la marcha no cambia y la app no hace nada

#### Scenario: Acciones de plato solo con platos
- **WHEN** se abre Botones con la lineal elegida
- **THEN** no aparecen «Plato más grande» ni «Plato más pequeño»

#### Scenario: Acciones de plato con la Shimano
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** aparecen «Plato más grande» y «Plato más pequeño» después de «−3 marchas»

### Requirement: Asignación por defecto
Mientras el usuario no la cambie, la lineal SHALL usar esta asignación: palancas D1 y D2 «pulsar» +1 marcha; palancas I1 e I2 «pulsar» −1 marcha; Campag. D + palanca D1 +3 marchas; Campag. D + palanca D2 marcha más dura; Campag. I + palanca I1 −3 marchas; Campag. I + palanca I2 marcha más ligera; □ corta volver a la referencia; □ larga fijar la referencia. La Shimano SHALL usar la de los mandos Shimano (spec `virtual-gears`, «Palancas con la Shimano» y «Saltos y extremos con la Shimano»), con □ igual. El resto de botones SHALL quedar sin acción en la app.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez y se mantiene □ más que el umbral de pulsación larga con la marcha en la 15
- **THEN** la referencia pasa a la 15

#### Scenario: Shimano por defecto
- **WHEN** se elige la Shimano por primera vez y se abre Botones
- **THEN** «+1 marcha» tiene la palanca D2, «−1 marcha» la palanca D1, «Plato más grande» la palanca I1, «Plato más pequeño» la palanca I2, «+3 marchas» Campag. D + palanca D2, «−3 marchas» Campag. D + palanca D1, «Marcha más dura» Campag. I + palanca I1 y «Marcha más ligera» Campag. I + palanca I2

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL mostrar de qué transmisión es la asignación y dos grupos: «Funciones de Zwift», informativo y sin poder tocarse, y «Acciones de la app», con una fila por acción de esa transmisión con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una acción de la app SHALL abrir Asignar acción. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones las resuelve Zwift con el Square emparejado, y sus filas no abren nada al tocarlas

#### Scenario: Transmisión de la asignación
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** la pantalla dice que es la asignación de Shimano 105 y que cada transmisión tiene la suya

### Requirement: Restaurar la asignación por defecto
«Restaurar asignación por defecto» SHALL pedir confirmación y, al confirmarla, SHALL volver a la asignación por defecto de la transmisión elegida, desactivar su inversión de palancas y poner los tiempos y la autorrepetición en sus valores por defecto. La asignación de las demás transmisiones SHALL NOT cambiar.

#### Scenario: Restaurar
- **WHEN** la palanca D1 baja tres marchas por una reasignación, se toca «Restaurar asignación por defecto» y se confirma
- **THEN** la palanca D1 vuelve a subir una marcha

#### Scenario: Restaurar cancelado
- **WHEN** se toca «Restaurar asignación por defecto» y se cancela
- **THEN** la asignación no cambia

#### Scenario: Restaurar solo la elegida
- **WHEN** con la lineal la palanca D1 está reasignada, se elige la Shimano y se restaura su asignación
- **THEN** al volver a la lineal, la palanca D1 sigue con su reasignación

### Requirement: Asignación guardada
Las asignaciones de cada transmisión, su inversión de palancas, los tiempos y la autorrepetición SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto. Una configuración guardada antes de que hubiera varias transmisiones SHALL conservar su asignación como la de la lineal.

#### Scenario: Volver a abrir la app
- **WHEN** se asigna Campag. D + △ corta a «−3 marchas» y se cierra y se vuelve a abrir la app
- **THEN** Campag. D + △ corta sigue bajando tres marchas

#### Scenario: Configuración ilegible
- **WHEN** el fichero de configuración está dañado y se abre la app
- **THEN** la app usa la asignación por defecto y funciona con normalidad

#### Scenario: Configuración anterior a la Shimano
- **WHEN** se abre la app nueva con una configuración guardada por la anterior, con △ corta en «−3 marchas», las palancas invertidas y la referencia en la 15
- **THEN** la transmisión es la lineal, △ corta sigue bajando tres marchas, las palancas siguen invertidas, la referencia sigue en la 15 y la Shimano tiene su asignación por defecto
