## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje lineal: sin Zwift, tope en lineal, Square sin conexión, sin dispositivos, Neo conectando al abrir, Square reconectando y los dos conectando, con el chip «Zwift esperando», y los estados con Zwift: ruta de Zwift, entrenamiento ERG de Zwift, tope de potencia en ERG y sin wifi; en todos ellos los chips enlazan con Diagnóstico.

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
- **THEN** se ve la transmisión lineal de 24 marchas con potencia y cadencia, los chips del Neo y del Square con el punto lleno, el chip «Zwift esperando» y el aviso «+1 marcha»

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

#### Scenario: Ruta de Zwift
- **WHEN** se elige el estado ruta de Zwift
- **THEN** los chips del Neo, del Square y de Zwift tienen el punto lleno, el modo dice «SIM» con «Lineal · 24 marchas», se ve «14» con «de 24» y la escalera, potencia y cadencia, y no aparece ninguna pendiente

#### Scenario: Entrenamiento ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift en lineal
- **THEN** el modo dice «ERG ZWIFT» con «Entrenamiento de Zwift · las palancas ajustan la potencia», la cifra principal es «240 W» en el color de ERG con «230 W de Zwift» y «+10 W con las palancas», no se ve la marcha, la rejilla tiene potencia y cadencia, y el pie dice «+10 W»

#### Scenario: Tope de potencia en ERG
- **WHEN** se elige el estado de tope de potencia en ERG
- **THEN** la cifra principal es «50 W» y el pie dice «Tope · 50 W»

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** el chip de Zwift dice «Zwift sin conexión», el modo dice «SIM», la marcha, la potencia y la cadencia se ven como sin Zwift y el pie no dice nada sobre Zwift

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie. SHALL tener también los estados lineales sin Zwift (sin Zwift, tope en lineal, Square sin conexión, sin dispositivos, Neo conectando al abrir, Square reconectando y los dos conectando) con el chip «Zwift esperando», y los estados con Zwift (ruta de Zwift, entrenamiento ERG de Zwift, tope de potencia en ERG y sin wifi), con la marcha y su escalera, o la potencia objetivo en ERG, en lugar de plato y cassette.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven los chips del Neo y del Square con el punto lleno y el chip «Zwift esperando», «Lineal · 24 marchas», «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los chips afectados dicen «sin conexión», enlazan con Diagnóstico, y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los chips afectados dicen «conectando», el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con los tres chips en la fila de conexiones, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

### Requirement: Estados de la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener los estados todo conectado (por defecto), buscando, conectando al abrir, Neo no encontrado, Square reconectando, Bluetooth apagado, sin permiso, límites del control manual, rodando con Zwift, ruta de Zwift en lineal, ERG de Zwift en lineal y sin wifi, todos con la fila de Zwift en Conexiones.

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
- **THEN** cada uno muestra su aviso de error con la acción que lo resuelve («Activar Bluetooth», «Abrir ajustes»), y las filas del Neo y del Square dicen «Sin conexión»

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado

#### Scenario: Zwift esperando
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** la fila de Zwift dice «Esperando» con «Elige Square Shift en Zwift como rodillo, potencia y cadencia»

#### Scenario: Ruta de Zwift en lineal
- **WHEN** se elige el estado ruta de Zwift
- **THEN** la fila de Zwift dice «Conectado» con la dirección del PC, la simulación dice modo SIM · Zwift con la pendiente y el terreno de Zwift, el ajuste de la marcha, lo enviado al Neo y el último mensaje de Zwift, y el tráfico tiene filas «Zwift» y «→ Zwift»

#### Scenario: ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la simulación dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al Neo 240 W, con la marcha visible

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** la fila de Zwift dice «Sin conexión» con «Conecta el móvil a la misma red wifi que Zwift» y hay un aviso informativo que lo explica
