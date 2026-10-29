## MODIFIED Requirements

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
- **THEN** la fila de Zwift dice «Esperando» con «Elige Smart Bike en Zwift como rodillo, potencia y cadencia»

#### Scenario: Ruta de Zwift en lineal
- **WHEN** se elige el estado ruta de Zwift
- **THEN** la fila de Zwift dice «Conectado» con la dirección del PC, la simulación dice modo SIM · Zwift con la pendiente y el terreno de Zwift, el ajuste de la marcha, lo enviado al Neo y el último mensaje de Zwift, y, al marcar «Tráfico en directo», el tráfico tiene filas «Zwift» y «→ Zwift»

#### Scenario: ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la simulación dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al Neo 240 W, con la marcha visible

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** la fila de Zwift dice «Sin conexión» con «Conecta el móvil a la misma red wifi que Zwift» y hay un aviso informativo que lo explica

### Requirement: Mando por wifi en la maqueta de Dispositivos
La maqueta de Dispositivos SHALL tener estados con el mando por wifi: esperando, conectado, sin wifi y desactivado, con la fila del mando explicando cómo emparejarlo en Zwift como potencia y mando a la vez.

#### Scenario: Mando por wifi esperando
- **WHEN** se elige el estado de mando por wifi esperando
- **THEN** la fila del mando dice «Esperando» y que en Zwift se elige «Smart Bike» como potencia y mando a la vez

#### Scenario: Mando por wifi conectado y sin wifi
- **WHEN** se eligen los estados de mando por wifi conectado y sin wifi
- **THEN** la fila del mando dice «Conectado», o «Sin conexión» con la ayuda de conectar el móvil a la misma red wifi que Zwift

#### Scenario: Sin «No disponible» por wifi
- **WHEN** se revisan los estados del mando por wifi
- **THEN** ninguno dice «No disponible»

### Requirement: Simular un KICKR BIKE en las maquetas
Las maquetas SHALL mostrar «Simular un KICKR BIKE» en la sección «Zwift» de Ajustes, con su ayuda, en un estado con el interruptor desactivado y en otro con él activado; la ayuda de emparejamiento de Dispositivos con el nombre que se anuncia; y el nombre anunciado en Conexiones de Diagnóstico, bajo la fila de Zwift, en los dos casos.

#### Scenario: Ajuste desactivado en Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la sección «Zwift» muestra «Simular un KICKR BIKE» desactivado con su ayuda

#### Scenario: Ajuste activado en Ajustes
- **WHEN** se elige el estado de simulación de KICKR BIKE de `maquetas/ajustes.html`
- **THEN** «Simular un KICKR BIKE» aparece activado, con la ayuda de que es una prueba y hay que emparejar de nuevo

#### Scenario: Nombre en Dispositivos
- **WHEN** se elige el estado de simulación de KICKR BIKE de `maquetas/dispositivos.html`
- **THEN** la ayuda del emparejamiento nombra el «KICKR BIKE PRO» con sus cuatro caracteres y, en el resto de estados, «Smart Bike»

#### Scenario: Nombre en Diagnóstico
- **WHEN** se abren los estados de `maquetas/diagnostico.html` con y sin la simulación de KICKR BIKE
- **THEN** Conexiones muestra, bajo la fila de Zwift, «KICKR BIKE PRO» con sus cuatro caracteres o «Smart Bike», según el estado

#### Scenario: Índice, temas y ancho
- **WHEN** se abre `maquetas/index.html` y se revisan los estados nuevos en los dos temas y a 360 dp
- **THEN** los estados nuevos están enlazados desde el índice, se leen sin cortes y cumplen el contraste de 4,5:1
