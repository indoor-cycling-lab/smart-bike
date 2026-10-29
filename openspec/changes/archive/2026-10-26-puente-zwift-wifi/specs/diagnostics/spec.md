## ADDED Requirements

### Requirement: Zwift en Conexiones
La sección Conexiones de Diagnóstico SHALL tener una fila de Zwift con su estado en palabra («Conectado», «Esperando» o «Sin conexión») y qué hacer: elegir «Square Shift» en Zwift mientras espera, desde qué dirección de la red está conectado, o conectar el móvil a la red wifi de Zwift si no hay wifi, con un aviso informativo.

#### Scenario: Esperando a Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** la fila de Zwift dice «Esperando» con «Elige Square Shift en Zwift como rodillo, potencia y cadencia»

#### Scenario: Zwift conectado
- **WHEN** Zwift está conectado desde el PC 192.168.0.10
- **THEN** la fila de Zwift dice «Conectado» con «Desde 192.168.0.10»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la fila de Zwift dice «Sin conexión» con «Conecta el móvil a la misma red wifi que Zwift» y hay un aviso informativo «Sin wifi. Para rodar en Zwift, conecta el móvil a la misma red wifi que el equipo de Zwift.»

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth está apagado y el móvil tiene wifi
- **THEN** las filas del Neo y del Square dicen «Sin conexión» y la de Zwift sigue con su estado

## MODIFIED Requirements

### Requirement: Tráfico en crudo
Diagnóstico SHALL mostrar las tramas recibidas del Square, del Neo y de Zwift, y las enviadas al Neo y a Zwift, las más recientes arriba, con hora (con milésimas), origen, característica y bytes en hexadecimal; lo que envía la app SHALL distinguirse por el origen «→ Neo» o «→ Zwift» y no solo por el color.

#### Scenario: Notificación de botones
- **WHEN** se pulsa la palanca D1
- **THEN** aparece una fila con la hora, el origen «Square», la característica de botones y los bytes recibidos, con resaltados los que cambian respecto a la trama anterior de la misma característica

#### Scenario: Telemetría del Neo
- **WHEN** se pedalea con el Neo conectado
- **THEN** aparecen filas «Neo» de potencia, cadencia y respuestas FE-C, cada una con su característica

#### Scenario: Terreno de Zwift
- **WHEN** Zwift envía una pendiente nueva
- **THEN** aparece una fila «Zwift» con el punto de control de FTMS (`2AD9`) y sus bytes, seguida de la respuesta «→ Zwift» y del comando «→ Neo» si cambia lo que se envía al Neo

#### Scenario: Telemetría hacia Zwift
- **WHEN** se pedalea con Zwift conectado
- **THEN** aparecen filas «→ Zwift» con los datos de la bici (`2AD2`) y la potencia (`2A63`)

### Requirement: Registro de sesión
La app SHALL registrar con hora monotónica en milisegundos cada trama recibida o enviada al Neo, al Square y a Zwift, los cambios de conexión y de sus parámetros (también la red wifi y el anuncio a Zwift), los gestos, las acciones, los cambios de marcha y de modo de control, los comandos al Neo con su entrega y los envíos manuales, conservando al menos las últimas 10 000 entradas, y en la compilación de depuración SHALL volcar cada entrada al registro del sistema con una etiqueta propia.

#### Scenario: Capturar una traza
- **WHEN** en la compilación de depuración se pulsan botones del Square y se lee el registro del sistema filtrado por la etiqueta de la app
- **THEN** cada trama aparece con su hora, origen, característica y bytes, en un formato que se puede guardar como traza para los tests

#### Scenario: Seguir un cambio de marcha
- **WHEN** se pulsa la palanca D1 y se lee el registro
- **THEN** aparecen, en orden y con su hora, la notificación del Square, el gesto, la acción, la marcha nueva, el comando enviado al Neo y su entrega

#### Scenario: Seguir una sesión con Zwift
- **WHEN** se empareja la app en Zwift, se rueda y se cierra Zwift, y se lee el registro
- **THEN** aparecen, en orden y con su hora, el anuncio en la red, la conexión de Zwift con su dirección, cada mensaje de Zwift y cada respuesta en bytes, los cambios de modo de control, la desconexión de Zwift y la vuelta a la pendiente base a los 30 s

#### Scenario: Registro lleno
- **WHEN** se superan las 10 000 entradas
- **THEN** se descartan las más antiguas y la app sigue registrando sin ralentizarse

### Requirement: Simulación
Diagnóstico SHALL mostrar en la sección Simulación el modo de control, la marcha puesta, el modelo («Simple»), la pendiente base o la de Zwift con su terreno, en ERG la potencia de Zwift y el ajuste de las palancas, el ajuste por la marcha, la última pendiente o potencia enviada al Neo, el último mensaje de Zwift con su hora y la latencia de la última palanca, y SHALL actualizarlos con cada cambio.

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17, el Neo está conectado y Zwift no
- **THEN** la sección dice modo SIM, marcha 17 de 24, modelo Simple, pendiente base 0,0 %, ajuste +5,0 % y enviada al Neo +5,0 %

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de 4,0 % con Crr 0,0040, la marcha es la 14 y el Neo está conectado
- **THEN** la sección dice modo SIM · Zwift, pendiente Zwift 4,0 %, el terreno con Crr 0,0040, ajuste +2,0 %, enviada al Neo +6,0 % y «Simulación» como último mensaje de Zwift con su hora

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide 230 W y el ajuste de las palancas es +10 W
- **THEN** la sección dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al Neo 240 W, y la marcha sigue visible

#### Scenario: Zwift desconectado
- **WHEN** se cierra Zwift rodando en una subida
- **THEN** durante 30 s la sección dice que se mantiene lo último de Zwift y después vuelve a modo SIM con la pendiente base

#### Scenario: Neo sin conexión
- **WHEN** el Neo no está conectado
- **THEN** la marcha, el modelo, la pendiente base o la de Zwift y el ajuste siguen visibles, y «Enviada al Neo» muestra «—»
