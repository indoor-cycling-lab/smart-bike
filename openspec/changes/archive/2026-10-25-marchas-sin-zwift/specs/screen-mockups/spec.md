# Spec Delta

## MODIFIED Requirements

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia. SHALL tener también los estados del rodaje sin Zwift: sin Zwift, tope en lineal, Square sin conexión y sin dispositivos, en los que los chips enlazan con Diagnóstico.

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

## REMOVED Requirements

### Requirement: Diagnóstico
**Reason**: Diagnóstico ya no existe sin una sesión de rodaje: siempre tiene botón de volver, último gesto con acción y sección Simulación.
**Migration**: Sustituido por «Maqueta de Diagnóstico con sesión de rodaje» en esta capacidad.

## ADDED Requirements

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver, las conexiones con la acción de buscar, los botones del Square con la máscara, el último gesto con su acción, la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al Neo y la latencia de la palanca), el tráfico con pausar y limpiar, y el control manual con el último envío.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, hay un botón de volver a Rodaje, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

### Requirement: Estados de la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener los estados todo conectado (por defecto), buscando, Neo no encontrado, Square con conexión perdida, Bluetooth apagado, sin permiso, límites del control manual y rodando con Zwift.

#### Scenario: Dispositivo no encontrado
- **WHEN** se elige el estado Neo no encontrado
- **THEN** la fila del Neo dice «No encontrado», hay un aviso de error que dice qué hacer, los botones de envío al Neo están deshabilitados con su explicación y «Enviada al Neo» muestra «—»

#### Scenario: Conexión perdida
- **WHEN** se elige el estado Square con conexión perdida
- **THEN** la fila del Square dice «Conexión perdida», ninguna tecla se ve pulsada, la máscara dice «—» y el Neo sigue conectado

#### Scenario: Bluetooth y permisos
- **WHEN** se eligen los estados Bluetooth apagado y sin permiso
- **THEN** cada uno muestra su aviso de error con la acción que lo resuelve («Activar Bluetooth», «Abrir ajustes»)

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado
