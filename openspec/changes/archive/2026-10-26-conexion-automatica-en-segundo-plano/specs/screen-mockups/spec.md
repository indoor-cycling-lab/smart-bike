## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje sin Zwift: sin Zwift, tope en lineal, Square sin conexión, sin dispositivos, Neo conectando al abrir, Square reconectando y los dos conectando, en los que los chips enlazan con Diagnóstico.

#### Scenario: Estado por defecto
- **WHEN** se abre la maqueta de rodaje sin estado
- **THEN** se ve la transmisión lineal de 24 marchas con 2 datos (potencia y cadencia) y sin botones − y +

#### Scenario: Transmisiones de marca y datos
- **WHEN** se recorren los estados de rodaje
- **THEN** hay al menos Shimano, SRAM y Campagnolo, y rejillas de 2, 3, 4 y 6 datos

#### Scenario: Botones táctiles
- **WHEN** se elige el estado con botones − y +
- **THEN** aparecen a la derecha, de 72 dp

#### Scenario: Tope
- **WHEN** se elige el estado de tope
- **THEN** la marcha aparece atenuada con la etiqueta de tope

#### Scenario: Conexión perdida
- **WHEN** se elige el estado de Neo desconectado
- **THEN** el chip del Neo muestra «sin conexión», la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas y la alerta de cadencia baja

#### Scenario: Sin Zwift
- **WHEN** se elige el estado sin Zwift
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los chips del Neo y del Square sin chip de Zwift, y el aviso «+1 marcha»

#### Scenario: Tope en lineal
- **WHEN** se elige el estado de tope en lineal
- **THEN** «24» aparece atenuado con «Tope · marcha más dura» y la escalera tiene resaltado el último segmento

#### Scenario: Square sin conexión
- **WHEN** se elige el estado de Square sin conexión
- **THEN** el chip del Square dice «sin conexión», la marcha sigue visible, la potencia y la cadencia tienen valor y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado sin dispositivos y se toca un chip
- **THEN** los chips del Neo y del Square dicen «sin conexión», la potencia y la cadencia muestran «—», el pie dice «Toca Neo o Square para conectarlos» y el chip abre la maqueta de Diagnóstico

#### Scenario: Neo conectando al abrir
- **WHEN** se elige el estado Neo conectando
- **THEN** el chip del Neo dice «conectando» con el anillo de espera, el del Square tiene el punto lleno, la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo»

#### Scenario: Square reconectando
- **WHEN** se elige el estado Square reconectando
- **THEN** el chip del Square dice «conectando» con el anillo de espera, la potencia y la cadencia tienen valor y el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** se elige el estado de los dos conectando
- **THEN** los dos chips dicen «conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie. SHALL tener también los estados lineales sin Zwift: sin Zwift, tope en lineal, Square sin conexión, sin dispositivos, Neo conectando al abrir, Square reconectando y los dos conectando, con la marcha y su escalera en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven los chips del Neo y del Square sin chip de Zwift, «Lineal · 24 marchas», «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los chips afectados dicen «sin conexión», enlazan con Diagnóstico, y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los chips afectados dicen «conectando», el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

### Requirement: Estados de la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener los estados todo conectado (por defecto), buscando, conectando al abrir, Neo no encontrado, Square reconectando, Bluetooth apagado, sin permiso, límites del control manual y rodando con Zwift.

#### Scenario: Dispositivo no encontrado
- **WHEN** se elige el estado Neo no encontrado
- **THEN** la fila del Neo dice «No encontrado», hay un aviso de error que dice qué hacer, los botones de envío al Neo están deshabilitados con su explicación y «Enviada al Neo» muestra «—»

#### Scenario: Conectando al abrir
- **WHEN** se elige el estado conectando al abrir
- **THEN** las filas del Neo y del Square dicen «Conectando» con su ayuda para despertarlos, sin aviso, y «Buscar dispositivos» está disponible

#### Scenario: Conexión perdida
- **WHEN** se elige el estado Square reconectando
- **THEN** la fila del Square dice «Reconectando» con «Pulsa cualquier botón para despertarlo», hay un aviso informativo «Reconectando con el Square. Pulsa cualquier botón para despertarlo.», ninguna tecla se ve pulsada, la máscara dice «—», el Neo sigue conectado y «Buscar dispositivos» está disponible

#### Scenario: Bluetooth y permisos
- **WHEN** se eligen los estados Bluetooth apagado y sin permiso
- **THEN** cada uno muestra su aviso de error con la acción que lo resuelve («Activar Bluetooth», «Abrir ajustes»), y las filas dicen «Sin conexión»

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado
