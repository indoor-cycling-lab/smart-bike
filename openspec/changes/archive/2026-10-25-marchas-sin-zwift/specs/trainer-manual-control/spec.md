# Spec Delta

## ADDED Requirements

### Requirement: Control manual temporal
Una pendiente o una potencia enviadas a mano SHALL sustituir a la resistencia de la marcha solo mientras se está en Diagnóstico y el Neo sigue conectado; mientras tanto, las palancas SHALL seguir cambiando la marcha sin enviarla al Neo, y al salir de Diagnóstico SHALL enviarse la pendiente de la marcha puesta.

#### Scenario: Cambiar de marcha con una potencia manual
- **WHEN** se envían 200 W a mano y se pulsa la palanca D1
- **THEN** la marcha sube, el Neo sigue en 200 W y la sección Simulación muestra la marcha nueva

#### Scenario: Volver a Rodaje
- **WHEN** el Neo está en 200 W enviados a mano, la marcha es la 15 y se vuelve a Rodaje
- **THEN** el Neo deja los 200 W y simula la pendiente de la marcha 15

## MODIFIED Requirements

### Requirement: Envío sin Neo
Sin el Neo conectado, la app SHALL deshabilitar «Enviar pendiente» y «Enviar potencia», conservar los valores elegidos y explicar por qué no se puede enviar; si se pierde la conexión con un envío manual activo, SHALL dejarlo sin efecto y, al recuperarla, el Neo SHALL recibir la pendiente de la marcha puesta.

#### Scenario: Neo desconectado
- **WHEN** el Neo no está conectado
- **THEN** los dos botones de envío están deshabilitados y la sección dice «Conecta el Neo para enviarle una pendiente o una potencia.»

#### Scenario: Conexión perdida y recuperada
- **WHEN** el usuario elige 3,0 %, la envía, se pierde la conexión con el Neo y se recupera
- **THEN** la pendiente elegida sigue siendo 3,0 %, los botones de envío vuelven a estar disponibles y el Neo recibe la pendiente de la marcha puesta, no la de 3,0 %

### Requirement: Vuelta a pendiente 0 % al terminar
Al terminar la sesión con el Neo conectado, la app SHALL enviarle una pendiente de 0 %, para que no quede fija una potencia o una cuesta.

#### Scenario: Salir con ERG activo
- **WHEN** el Neo está en 300 W enviados a mano y el usuario termina la sesión
- **THEN** antes de desconectarse la app envía pendiente 0 % y el Neo deja de mantener los 300 W

#### Scenario: Terminar en una marcha dura
- **WHEN** la marcha es la 20 y el usuario termina la sesión
- **THEN** antes de desconectarse la app envía pendiente 0 %
