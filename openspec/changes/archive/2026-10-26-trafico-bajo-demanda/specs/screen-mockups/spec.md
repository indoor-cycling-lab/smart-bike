## MODIFIED Requirements

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver a Ajustes, las conexiones con la acción de buscar, los botones del Square con la máscara, el último gesto con su acción, la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al Neo y la latencia de la palanca), el tráfico con la casilla «Tráfico en directo», desmarcada al abrir, que al marcarla muestra el volcado con pausar y limpiar, y el control manual con el último envío. SHALL NOT enlazar con Botones ni con Ajustes salvo por el botón de volver.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, hay un botón de volver a Ajustes, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Enlace a Botones
- **WHEN** se recorre la sección de botones del Square de la maqueta de Diagnóstico
- **THEN** no hay enlace «Asignación de botones»: Botones se abre desde la navegación inferior de Ajustes

#### Scenario: Tráfico al abrir
- **WHEN** se abre la maqueta de Diagnóstico en cualquier estado
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar», y al marcarla aparecen el volcado del estado y los dos botones

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
- **THEN** la fila de Zwift dice «Conectado» con la dirección del PC, la simulación dice modo SIM · Zwift con la pendiente y el terreno de Zwift, el ajuste de la marcha, lo enviado al Neo y el último mensaje de Zwift, y, al marcar «Tráfico en directo», el tráfico tiene filas «Zwift» y «→ Zwift»

#### Scenario: ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la simulación dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al Neo 240 W, con la marcha visible

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** la fila de Zwift dice «Sin conexión» con «Conecta el móvil a la misma red wifi que Zwift» y hay un aviso informativo que lo explica
