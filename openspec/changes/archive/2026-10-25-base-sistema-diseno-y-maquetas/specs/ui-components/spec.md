# Spec Delta

## Purpose

Define los componentes de la interfaz (marcha, transmisión, datos de rodaje, conexión, modo, avisos, botones del Square, ajustes, navegación y diagnóstico) sobre los que se construyen todas las pantallas, con su anatomía, variantes y estados.

## ADDED Requirements

### Requirement: Marcha como en un grupo real
El componente de marcha SHALL mostrar la marcha puesta como cifra principal: el número en transmisión lineal, o plato × piñón (p. ej. `50×17`) en transmisión física, sin ningún dato del modelo de resistencia.

#### Scenario: Transmisión lineal
- **WHEN** la transmisión es lineal de 24 marchas y la marcha es la 14
- **THEN** se ve «14» en grande con «Marcha · de 24» al lado

#### Scenario: Transmisión física
- **WHEN** la transmisión es Shimano 2×12 y la marcha es plato 50 y piñón 17
- **THEN** se ve «50×17» en grande, con el signo × más pequeño y atenuado

#### Scenario: Sin datos del modelo
- **WHEN** se muestra el componente de marcha con cualquier modelo de resistencia
- **THEN** no aparece la pendiente de Zwift, el ajuste por marcha ni lo enviado al Neo

### Requirement: Aviso de tope
Al llegar a la marcha más ligera o a la más dura, el componente de marcha SHALL atenuar la cifra y mostrar una etiqueta de tope con texto, además de la vibración si está activada.

#### Scenario: Tope en la marcha más dura
- **WHEN** se pide una marcha más dura estando en la última
- **THEN** la marcha no cambia, la cifra se atenúa y aparece «Tope · marcha más dura»

### Requirement: Indicadores de plato y cassette
En transmisión física, el indicador de plato SHALL mostrar los platos con el activo relleno, y el cassette SHALL mostrar una barra por piñón, de mayor a menor de izquierda a derecha, con altura proporcional a sus dientes y el activo resaltado y numerado.

#### Scenario: Plato activo
- **WHEN** la marcha usa el plato de 50 en una transmisión 50/34
- **THEN** el 50 aparece relleno y el 34 solo con borde

#### Scenario: Piñón activo
- **WHEN** la marcha usa el piñón de 17 en un cassette 11–34
- **THEN** la barra del 17 está resaltada y su número se ve en el color de marca, con el 34 a la izquierda y el 11 a la derecha

#### Scenario: Un solo plato
- **WHEN** la transmisión tiene un solo plato
- **THEN** no se muestra el indicador de plato

### Requirement: Escalera de marchas lineal
En transmisión lineal, la escalera SHALL mostrar un segmento por marcha, de la más ligera a la izquierda a la más dura a la derecha, con la actual resaltada, las anteriores diferenciadas y una marca en la marcha de referencia.

#### Scenario: Marcha de referencia
- **WHEN** la marcha de referencia es la 12 y la actual la 14
- **THEN** el segmento 14 está resaltado y el 12 lleva la marca de referencia

### Requirement: Rejilla de datos sin desplazamiento
La rejilla de datos de rodaje SHALL mostrar de 1 a 6 datos elegidos por el usuario, en su orden, con el mismo tamaño de cifra para todos, el mayor que permita ver todos a la vez sin desplazamiento ni cifras cortadas.

#### Scenario: Pocos datos
- **WHEN** hay 2 datos en la pantalla horizontal
- **THEN** se apilan en una columna con cifras grandes (72 sp) que ocupan todo el alto de la zona

#### Scenario: Máximo de datos
- **WHEN** hay 6 datos en la pantalla horizontal
- **THEN** se colocan en 2 columnas y 3 filas, todos visibles y sin desplazamiento

#### Scenario: Valor largo
- **WHEN** un dato como «1:12:40» no cabe al tamaño de su rejilla
- **THEN** toda la rejilla baja al siguiente tamaño y ningún valor se corta ni pasa a dos líneas

#### Scenario: Dato sin valor
- **WHEN** un dato no está disponible porque su sensor no está conectado
- **THEN** se muestra «—» atenuado, nunca 0

### Requirement: Estado de conexión por forma y palabra
El chip de conexión SHALL mostrar el nombre del dispositivo y su estado con una forma (punto lleno conectado, anillo girando buscando, anillo vacío sin conexión) y, si no está conectado, con una palabra.

#### Scenario: Zwift sin conexión
- **WHEN** Zwift no está conectado a la app
- **THEN** el chip muestra «Zwift sin conexión» con anillo vacío y borde de alerta

#### Scenario: Movimiento reducido
- **WHEN** el sistema pide movimiento reducido y un dispositivo está buscando
- **THEN** el anillo no gira y el estado se sigue leyendo por la palabra

### Requirement: Insignia de modo
La insignia de modo SHALL mostrar «SIM» o «ERG», con su color de modo, y en ERG SHALL indicar quién fija la potencia (Zwift o manual).

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift fija una potencia
- **THEN** la insignia muestra «ERG» con «ZWIFT» al lado

### Requirement: Aviso de la acción realizada
El aviso de acción SHALL mostrar durante 1,5 s solo el resultado de una pulsación del Square («+1 marcha», «Plato 50», «+10 W»), sin el botón ni el gesto, y SHALL poder desactivarse en ajustes.

#### Scenario: Cambio de plato
- **WHEN** un cambio de marcha cambia también de plato
- **THEN** el aviso dice «Plato 50» y desaparece a los 1,5 s

#### Scenario: Aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado
- **THEN** no aparece ningún aviso al pulsar el Square

### Requirement: Teclas del Square
Cada botón del Square SHALL dibujarse como una tecla con su glifo (△ ○ □ ✕, A B Y Z, flechas) o su nombre corto («Palanca D1», «Freno I», «Campag. D»), con un estado pulsado.

#### Scenario: Combinación
- **WHEN** se representa la combinación Campag. D + Palanca D1
- **THEN** se ven las dos teclas unidas por «+», con el modificador primero

### Requirement: Fila de asignación con su capa
La fila de asignación SHALL mostrar el botón o la combinación, el gesto, la acción y si la acción va a Zwift o la resuelve la app, y SHALL tener un estado de captura.

#### Scenario: Función de Zwift
- **WHEN** se muestra la asignación de Y
- **THEN** la fila dice «Y · pulsar · Usar powerup» con la etiqueta ZWIFT

### Requirement: Captura de un gesto
El panel de captura SHALL esperar a que el usuario pulse en el Square el botón, gesto o combinación, mostrarlo con sus teclas y avisar si ya está asignado a otra acción.

#### Scenario: Combinación ya asignada
- **WHEN** se captura Campag. D + ↑, que ya sube 10 W en ERG
- **THEN** se muestra la combinación y un aviso de error que dice qué acción se quedará sin botón si se guarda

### Requirement: Piezas de ajustes accesibles
Las listas de ajustes SHALL componerse de secciones con título y filas cuyo objetivo táctil es la fila entera (al menos 48 dp): enlaces, interruptores, opciones excluyentes, controles segmentados, steppers y filas reordenables.

#### Scenario: Interruptor por la fila
- **WHEN** se toca el texto de la fila «Vibrar»
- **THEN** cambia el estado de su casilla

#### Scenario: Límite de selección
- **WHEN** ya hay 6 datos elegidos en «Datos en pantalla»
- **THEN** las filas no elegidas se muestran atenuadas y su casilla no se puede marcar

### Requirement: Navegación inferior
La navegación inferior SHALL ofrecer las pestañas Rodar, Dispositivos, Botones y Ajustes con icono y texto, marcar la activa por color y por estado accesible, y SHALL NOT aparecer en la pantalla de rodaje ni en pantallas secundarias.

#### Scenario: Pestaña activa
- **WHEN** se está en Ajustes
- **THEN** la pestaña Ajustes aparece en el color de marca y se anuncia como página actual

### Requirement: Avisos en línea
Los avisos en línea SHALL tener una variante informativa y otra de error, siempre con icono y texto; el de error SHALL anunciarse como alerta y decir qué pasa y qué hacer.

#### Scenario: Neo sin respuesta
- **WHEN** el Neo no responde en Dispositivos
- **THEN** aparece «El Neo no responde. Comprueba que está enchufado y pedalea para despertarlo.» con icono de error

### Requirement: Componentes de diagnóstico
Diagnóstico SHALL disponer de un indicador de pendiente (Zwift → efectiva, con signo y color de subida o bajada) y de un volcado de tráfico con hora, origen y bytes, y estos componentes SHALL NOT usarse en la pantalla de rodaje.

#### Scenario: Pendiente efectiva
- **WHEN** Zwift manda 4,0 % y la marcha suma 1,5 %
- **THEN** el indicador muestra «4,0 %» atenuado y «+5,5 %» en color de subida
