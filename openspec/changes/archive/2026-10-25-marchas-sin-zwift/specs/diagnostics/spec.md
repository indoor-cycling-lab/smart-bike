# Spec Delta

## REMOVED Requirements

### Requirement: Diagnóstico como pantalla inicial
**Reason**: La app abre ahora en la pantalla de rodaje; Diagnóstico pasa a ser una pantalla secundaria.
**Migration**: Sustituido por «Diagnóstico como pantalla secundaria» en esta capacidad y por «Rodaje como pantalla inicial» en `ride-screen`.

## ADDED Requirements

### Requirement: Diagnóstico como pantalla secundaria
Diagnóstico SHALL abrirse desde los chips de conexión de la pantalla de rodaje, con un botón de volver a Rodaje y sus secciones en este orden: conexiones, botones del Square, simulación, tráfico y control manual del Neo.

#### Scenario: Volver a Rodaje
- **WHEN** se abre Diagnóstico desde un chip y se toca volver, o se pulsa Atrás
- **THEN** se vuelve a la pantalla de rodaje sin terminar la sesión

### Requirement: Simulación
Diagnóstico SHALL mostrar en la sección Simulación la marcha puesta, el modelo («Simple»), la pendiente base, el ajuste por la marcha, la última pendiente o potencia enviada al Neo y la latencia de la última palanca, y SHALL actualizarlos con cada cambio.

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17 y el Neo está conectado
- **THEN** la sección dice marcha 17 de 24, modelo Simple, pendiente base 0,0 %, ajuste +5,0 % y enviada al Neo +5,0 %

#### Scenario: Neo sin conexión
- **WHEN** el Neo no está conectado
- **THEN** la marcha, el modelo, la pendiente base y el ajuste siguen visibles, y «Enviada al Neo» muestra «—»

## MODIFIED Requirements

### Requirement: Última combinación pulsada
Diagnóstico SHALL mostrar el último gesto reconocido: los botones que estaban pulsados a la vez, con el modificador (Campagnolo) primero, el gesto («pulsar») y la acción que produjo, o «sin acción» si no tiene ninguna.

#### Scenario: Combinación con modificador
- **WHEN** se mantiene Campag. D, se pulsa la palanca D1 y se sueltan los dos
- **THEN** en «Último gesto» se ve «Campag. D + Palanca D1 · pulsar · +3 marchas», y sigue visible después de soltarlos

#### Scenario: Botón sin acción
- **WHEN** se pulsa Y
- **THEN** en «Último gesto» se ve «Y · pulsar · sin acción»

#### Scenario: Tope
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** en «Último gesto» se ve «Palanca D1 · pulsar · Tope · marcha más dura»

#### Scenario: Sin pulsaciones todavía
- **WHEN** el Square está conectado y aún no se ha pulsado nada
- **THEN** «Último gesto» dice «Ninguno todavía»

### Requirement: Registro de sesión
La app SHALL registrar con hora monotónica en milisegundos cada trama recibida o enviada al Neo y al Square, los cambios de conexión y de sus parámetros, los gestos, las acciones, los cambios de marcha, los comandos al Neo con su entrega y los envíos manuales, conservando al menos las últimas 10 000 entradas, y en la compilación de depuración SHALL volcar cada entrada al registro del sistema con una etiqueta propia.

#### Scenario: Capturar una traza
- **WHEN** en la compilación de depuración se pulsan botones del Square y se lee el registro del sistema filtrado por la etiqueta de la app
- **THEN** cada trama aparece con su hora, origen, característica y bytes, en un formato que se puede guardar como traza para los tests

#### Scenario: Seguir un cambio de marcha
- **WHEN** se pulsa la palanca D1 y se lee el registro
- **THEN** aparecen, en orden y con su hora, la notificación del Square, el gesto, la acción, la marcha nueva, el comando enviado al Neo y su entrega

#### Scenario: Registro lleno
- **WHEN** se superan las 10 000 entradas
- **THEN** se descartan las más antiguas y la app sigue registrando sin ralentizarse
