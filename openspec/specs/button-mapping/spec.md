# button-mapping Specification

## Purpose
Decidir qué hace cada botón, gesto o combinación del Square: la asignación por defecto, reasignarla capturando la entrada en el Square, invertir las palancas, restaurarla y guardarla, con las pantallas de Botones y Asignar acción.

## Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia, fijar la marcha de referencia, +10 W, −10 W, alternar SIM ⇄ ERG, pantalla anterior, pantalla siguiente y siguiente modelo; con la Shimano, además, plato más grande y plato más pequeño. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app ni llegar a Zwift por la app.

#### Scenario: Botón sin acción
- **WHEN** se quita la regla de A y se pulsa A
- **THEN** la marcha no cambia, la app no hace nada y Zwift no recibe nada por el mando

#### Scenario: Acciones de plato solo con platos
- **WHEN** se abre Botones con la lineal elegida
- **THEN** no aparecen «Plato más grande» ni «Plato más pequeño»

#### Scenario: Acciones de plato con la Shimano
- **WHEN** se abre Botones con la Shimano elegida
- **THEN** aparecen «Plato más grande» y «Plato más pequeño» después de «−3 marchas»

#### Scenario: Acciones de ERG
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Fijar la marcha de referencia» aparecen «+10 W», «−10 W» y «Alternar SIM ⇄ ERG»

#### Scenario: Asignar una acción de ERG
- **WHEN** se asigna A corta a «Alternar SIM ⇄ ERG» y se pulsa A rodando sin Zwift
- **THEN** la app pasa a ERG manual

#### Scenario: Acciones de pantalla
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Alternar SIM ⇄ ERG» aparecen «Pantalla anterior» y «Pantalla siguiente»

#### Scenario: Acción de modelo
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Pantalla siguiente» aparece «Siguiente modelo», sin ningún botón asignado por defecto

#### Scenario: Asignar el siguiente modelo
- **WHEN** se asigna △ larga a «Siguiente modelo» y se pulsa rodando con el modelo «Simple»
- **THEN** el modelo pasa a «Por tabla» y el aviso de la acción dice «Modelo: Por tabla»

#### Scenario: Asignar una función de Zwift
- **WHEN** se asigna Campag. D + △ «pulsar» a «Ride On» y se pulsa esa combinación con el mando conectado a Zwift y otro ciclista cerca
- **THEN** Zwift da Ride On

### Requirement: Funciones de Zwift asignables
En todas las transmisiones, la app SHALL permitir asignar también las funciones de Zwift: barra de acciones (↑), cambio de sentido (↓), izquierda (←), derecha (→), seleccionar (A), atrás (B), usar powerup (Y), Ride On (Z), girar a la izquierda, girar a la derecha, frenar con el freno izquierdo y frenar con el derecho.

#### Scenario: Funciones de Zwift en Botones
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** aparece el grupo «Funciones de Zwift» con una fila por cada una de estas funciones

### Requirement: Asignación por defecto
Mientras el usuario no la cambie, la lineal SHALL usar esta asignación de marchas: palancas D1 y D2 «pulsar» +1 marcha; palancas I1 e I2 «pulsar» −1 marcha; Campag. D + palanca D1 +3 marchas; Campag. D + palanca D2 marcha más dura; Campag. I + palanca I1 −3 marchas; Campag. I + palanca I2 marcha más ligera. «+10 W» y «−10 W» SHALL quedar sin botón: en ERG, las palancas ya ajustan la potencia.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez y se mantiene □ más que el umbral de pulsación larga con la marcha en la 15
- **THEN** la referencia pasa a la 15

#### Scenario: Shimano por defecto
- **WHEN** se elige la Shimano por primera vez y se abre Botones
- **THEN** «+1 marcha» tiene la palanca D2, «−1 marcha» la palanca D1, «Plato más grande» la palanca I1, «Plato más pequeño» la palanca I2, «+3 marchas» Campag. D + palanca D2, «−3 marchas» Campag. D + palanca D1, «Marcha más dura» Campag. I + palanca I1 y «Marcha más ligera» Campag. I + palanca I2

#### Scenario: ERG por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** «Alternar SIM ⇄ ERG» tiene △ larga, y «+10 W» y «−10 W» dicen «Sin botón»

#### Scenario: △ corta sin acción
- **WHEN** se pulsa △ brevemente con la asignación por defecto
- **THEN** la app no hace nada

#### Scenario: Pantalla por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** «Pantalla anterior» tiene Campag. I + cruceta ← y «Pantalla siguiente» Campag. I + cruceta →

#### Scenario: Cruceta sin Campagnolo
- **WHEN** se pulsa la cruceta → sin mantener ningún Campagnolo, con la asignación por defecto
- **THEN** la app no cambia de pantalla y, con el mando conectado, Zwift recibe la cruceta →

#### Scenario: Zwift por defecto
- **WHEN** se abre Botones por primera vez, con la lineal o con la Shimano
- **THEN** cada función de Zwift tiene su botón del mando oficial: ↑, ↓, ←, →, A, B, Y, Z, ✕, ○, Freno I y Freno D, con «pulsar»

### Requirement: Asignación por defecto de referencia, modo y pantallas
Mientras el usuario no la cambie, la lineal SHALL usar además esta asignación: □ corta volver a la referencia; □ larga fijar la referencia; △ larga alternar SIM ⇄ ERG; Campag. I + cruceta ← «pulsar» pantalla anterior; Campag. I + cruceta → «pulsar» pantalla siguiente. La Shimano SHALL usar la de los mandos Shimano (spec `virtual-gears`, «Palancas con la Shimano» y «Saltos y extremos con la Shimano»), con □, △ y las de pantalla igual.

#### Scenario: Referencia y modo por defecto
- **WHEN** se restaura la asignación por defecto con la lineal
- **THEN** □ corta vuelve a la referencia y △ larga alterna SIM ⇄ ERG

### Requirement: Asignación por defecto de las funciones de Zwift
Mientras el usuario no la cambie, las dos transmisiones SHALL tener, con «pulsar», las funciones de Zwift de un mando oficial: cruceta ↑, ↓, ← y → con su función; A, B, Y y Z con seleccionar, atrás, usar powerup y Ride On; ✕ girar a la izquierda; ○ girar a la derecha; y cada maneta su freno. El resto de botones SHALL quedar sin acción.

#### Scenario: Funciones de Zwift por defecto
- **WHEN** se restaura la asignación por defecto con cualquier transmisión
- **THEN** A «pulsar» es seleccionar y ✕ «pulsar» es girar a la izquierda

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

### Requirement: Estado de Zwift y ajustes en la pantalla de Botones
El grupo de Zwift SHALL decir que las funciones llegan a la app de entrenamiento por el controlador Zwift de la app y, sin Square configurado, que el controlador no se publica hasta configurar uno; las filas se mantienen siempre, para no perder la asignación. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Con Square configurado
- **WHEN** se abre Botones con un Square configurado
- **THEN** el grupo de Zwift dice que las funciones llegan por el controlador Zwift de la app y sus filas están ahí

#### Scenario: Sin el mando de Zwift
- **WHEN** se abre Botones sin Square configurado
- **THEN** el grupo de Zwift dice que el controlador no se publica hasta configurar un Square en Dispositivos, y sus filas siguen ahí

### Requirement: Asignar pulsando en el Square
En Asignar acción, la app SHALL esperar a que se pulse en el Square un botón o una combinación con un Campagnolo, mostrarlo con sus teclas y dejar elegir el gesto (pulsar, corta, larga o doble). Mientras la pantalla está abierta, los botones del Square SHALL NOT ejecutar sus acciones. La asignación SHALL aplicarse solo al tocar «Guardar».

#### Scenario: Capturar una combinación
- **WHEN** en Asignar acción de «−3 marchas» se mantiene Campag. D y se pulsa △, se elige «corta» y se toca «Guardar»
- **THEN** a partir de ese momento, Campag. D + △ corta baja tres marchas

#### Scenario: Botones sin efecto al capturar
- **WHEN** la marcha es la 14, Asignar acción está abierta y se pulsa la palanca D1
- **THEN** la palanca D1 aparece como capturada y la marcha sigue en la 14

#### Scenario: Cancelar
- **WHEN** se captura △ y se toca «Cancelar»
- **THEN** la asignación no cambia y se vuelve a Botones

#### Scenario: Square sin conexión
- **WHEN** se abre Asignar acción con el Square sin conexión
- **THEN** la zona de captura dice «Conecta el Square para capturar un botón» y «Guardar» está deshabilitado hasta capturar algo

#### Scenario: Campagnolo solo
- **WHEN** en la captura se pulsa y se suelta Campag. I sin pulsar otro botón
- **THEN** se captura Campag. I y el gesto «pulsar» no se puede elegir

### Requirement: Varias entradas por acción
Una acción SHALL poder tener varios botones o combinaciones; Asignar acción SHALL mostrar los que tiene, permitir quitar cada uno y añadir el capturado.

#### Scenario: Quitar una palanca
- **WHEN** en Asignar acción de «+1 marcha» se quita la palanca D2 y se guarda
- **THEN** la palanca D2 deja de subir marcha y la palanca D1 sigue subiendo

### Requirement: Aviso de entrada en uso
Si la entrada capturada con su gesto ya tiene otra acción, o «pulsar» y «corta», «larga» o «doble» chocan sobre el mismo botón, Asignar acción SHALL avisar, antes de guardar, de qué acciones perderán esa entrada; al guardar, la entrada SHALL pasar a la acción nueva.

#### Scenario: Entrada de otra acción
- **WHEN** en Asignar acción de «−3 marchas» se captura la palanca D1 con «pulsar»
- **THEN** un aviso dice que la palanca D1 dejará de hacer «+1 marcha», y al guardar la palanca D1 baja tres marchas

#### Scenario: Entrada libre
- **WHEN** se captura Campag. D + △ con «corta», que no tiene acción
- **THEN** un aviso informativo dice que está libre

### Requirement: Invertir las palancas
Con «Invertir palancas» activado, cada palanca SHALL hacer lo que tiene asignado la palanca del otro lado con el mismo número, también en combinación con el Campagnolo de su lado. Por defecto SHALL estar desactivado.

#### Scenario: Palancas invertidas
- **WHEN** se activa «Invertir palancas» y se pulsa la palanca D1 en la marcha 14
- **THEN** la marcha pasa a la 13

#### Scenario: Combinación invertida
- **WHEN** «Invertir palancas» está activado, se mantiene Campag. D y se pulsa la palanca D1 en la marcha 14
- **THEN** la marcha pasa a la 11

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
Las asignaciones de cada transmisión, su inversión de palancas, los tiempos y la autorrepetición SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se asigna Campag. D + △ corta a «−3 marchas» y se cierra y se vuelve a abrir la app
- **THEN** Campag. D + △ corta sigue bajando tres marchas

#### Scenario: Configuración ilegible
- **WHEN** el fichero de configuración está dañado y se abre la app
- **THEN** la app usa la asignación por defecto y funciona con normalidad

#### Scenario: Configuración anterior a la Shimano
- **WHEN** se abre la app nueva con una configuración guardada por la anterior, con △ corta en «−3 marchas», las palancas invertidas y la referencia en la 15
- **THEN** la transmisión es la lineal, △ corta sigue bajando tres marchas, las palancas siguen invertidas, la referencia sigue en la 15 y la Shimano tiene su asignación por defecto

#### Scenario: Configuración anterior al ERG manual
- **WHEN** se abre la app nueva con una configuración guardada antes del ERG manual, con △ corta en «−3 marchas»
- **THEN** △ corta sigue bajando tres marchas y △ larga alterna SIM ⇄ ERG

#### Scenario: Entrada ya ocupada
- **WHEN** la configuración guardada antes del ERG manual tiene △ «pulsar» en «Marcha de referencia»
- **THEN** △ sigue volviendo a la referencia y «Alternar SIM ⇄ ERG» queda «Sin botón»

#### Scenario: Regla nueva quitada por el usuario
- **WHEN** tras recibir las reglas de ERG se quita △ larga de «Alternar SIM ⇄ ERG» y se vuelve a abrir la app
- **THEN** «Alternar SIM ⇄ ERG» sigue «Sin botón»

#### Scenario: Configuración anterior a las acciones de pantalla
- **WHEN** se abre la app nueva con una configuración guardada antes de las acciones de pantalla, con Campag. I + cruceta → «pulsar» en «+3 marchas»
- **THEN** Campag. I + cruceta → sigue subiendo tres marchas, «Pantalla siguiente» queda «Sin botón» y «Pantalla anterior» tiene Campag. I + cruceta ←

#### Scenario: Configuración anterior a las funciones de Zwift
- **WHEN** se abre la app nueva con una configuración guardada antes de las funciones de Zwift, con A corta en «Marcha de referencia»
- **THEN** A corta sigue volviendo a la referencia, «Seleccionar» queda «Sin botón» y el resto de funciones de Zwift tienen su botón por defecto

### Requirement: Migración de la asignación guardada
Una configuración guardada antes de que hubiera varias transmisiones SHALL conservar su asignación como la de la lineal. Una configuración guardada antes de las acciones de ERG SHALL recibir la regla por defecto de «Alternar SIM ⇄ ERG» una sola vez, en cada transmisión, solo en las entradas y gestos libres. Una configuración guardada antes de las acciones de pantalla SHALL recibir igual sus dos reglas por defecto, y una guardada antes de las funciones de Zwift, las suyas.

#### Scenario: Configuración guardada antes de las acciones de pantalla
- **WHEN** se abre la app con una configuración guardada antes de esas acciones
- **THEN** recibe una sola vez las reglas por defecto de pantalla anterior y siguiente, solo en las entradas libres

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

### Requirement: Acciones del ventilador
La app SHALL permitir asignar, en todas las transmisiones, tres acciones del ventilador: «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad». Aparecerán en «Acciones de la app» después de «Siguiente modelo», sin botón por defecto, y asignarlas no exige un ventilador configurado. Su efecto es el de la spec `fan-control`.

#### Scenario: Acciones en la lista
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Siguiente modelo» aparecen «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad», las tres con «Sin botón»

#### Scenario: Asignar la velocidad
- **WHEN** se asigna □ doble a «Ventilador: + velocidad» y se pulsa rodando con el ventilador en manual al 40 %
- **THEN** el ventilador pasa al 50 % y el aviso de la acción dice «Ventilador: 50 %»

#### Scenario: Asignar sin ventilador
- **WHEN** no hay ventilador configurado y se asigna △ corta a «Ventilador: manual ⇄ automático»
- **THEN** se guarda la asignación y, al pulsar △, el aviso dice «Sin ventilador»

#### Scenario: Restaurar la asignación
- **WHEN** hay botones asignados a las acciones del ventilador y se restaura la asignación por defecto
- **THEN** las tres acciones vuelven a «Sin botón»

#### Scenario: Entrada ya en uso
- **WHEN** se intenta asignar a una acción del ventilador una entrada que ya tiene otra acción
- **THEN** se muestra el aviso de entrada en uso de siempre y no se asigna sin confirmar

#### Scenario: Una asignación por transmisión
- **WHEN** se asigna una acción del ventilador con la lineal y se cambia a la Shimano
- **THEN** la Shimano conserva su propia asignación, sin botón para las acciones del ventilador

### Requirement: Mando de Zwift en Zwift Ride para la asignación
Con cualquier modo, el grupo de Zwift de Botones SHALL decir por dónde llegan las funciones a la app de entrenamiento: por Bluetooth aparte con «Wifi + Bluetooth», y dentro del mismo dispositivo con «Solo Bluetooth» y «Solo wifi (modo Zwift Ride)».

#### Scenario: Funciones de Zwift con Zwift Ride
- **WHEN** el modo es «Solo wifi (modo Zwift Ride)», hay un Square configurado y se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones llegan por el mando de la app, que va dentro del mismo dispositivo por wifi, y tocar «Ride On» abre Asignar acción

#### Scenario: Funciones de Zwift con Wifi + Bluetooth
- **WHEN** el modo es «Wifi + Bluetooth», hay un Square configurado y se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones llegan por el mando de la app, por Bluetooth
