# trainer-manual-control Specification

## Purpose
Fijar a mano en el Tacx Neo una pendiente o una potencia, para comprobar que el rodillo obedece a la app sin marchas ni Zwift y para diagnosticar la resistencia.

## Requirements

### Requirement: Enviar una pendiente
Con el rodillo conectado, la app SHALL enviarle la pendiente elegida al tocar «Enviar pend.», y el rodillo SHALL simularla: la resistencia depende de la velocidad, como en una cuesta.

#### Scenario: Subir al 10 %
- **WHEN** el usuario pedalea en pendiente 0 %, elige 10,0 % y toca «Enviar pend.»
- **THEN** la resistencia aumenta de forma perceptible en menos de un segundo y el envío aparece en el tráfico como «→ Rodillo»

#### Scenario: Bajada
- **WHEN** el usuario elige −5,0 % y toca «Enviar pend.»
- **THEN** el rodillo simula una bajada y la resistencia es menor que en 0 %

### Requirement: Enviar una potencia
Con el Neo conectado, la app SHALL enviarle la potencia elegida al tocar «Enviar potencia», y el Neo SHALL mantenerla aunque cambie la cadencia.

#### Scenario: ERG a 200 W
- **WHEN** el usuario elige 200 W, toca «Enviar potencia» y pedalea primero a 70 rpm y luego a 95 rpm
- **THEN** el Neo ajusta la resistencia para mantener unos 200 W en las dos cadencias

### Requirement: Límites del control manual
La pendiente SHALL ajustarse entre −10,0 % y +20,0 % en pasos de 0,5 %, y la potencia entre 50 W y 1000 W en pasos de 10 W; en cada límite, el botón que lo superaría SHALL estar deshabilitado.

#### Scenario: Pendiente máxima
- **WHEN** la pendiente elegida es +20,0 %
- **THEN** el botón «+» de la pendiente está deshabilitado y el «−» no

#### Scenario: Potencia mínima
- **WHEN** la potencia elegida es 50 W
- **THEN** el botón «−» de la potencia está deshabilitado y el «+» no

#### Scenario: Valores iniciales
- **WHEN** se abre Diagnóstico por primera vez
- **THEN** la pendiente elegida es 0,0 % y la potencia, 150 W

### Requirement: Último envío visible
La app SHALL mostrar qué se envió al rodillo por última vez y a qué hora, en la sección de control manual.

#### Scenario: Tras enviar una potencia
- **WHEN** el usuario envía 200 W a las 12:04:31
- **THEN** la sección dice «Último envío: 200 W · 12:04:31»

### Requirement: Envío sin Neo
Sin el rodillo conectado, la app SHALL deshabilitar «Enviar pend.» y «Enviar potencia», conservar los valores elegidos y explicar por qué no se puede enviar; si se pierde la conexión con un envío manual activo, SHALL dejarlo sin efecto y, al recuperarla, el rodillo SHALL recibir la pendiente de la marcha puesta.

#### Scenario: Neo desconectado
- **WHEN** el rodillo no está conectado
- **THEN** los dos botones de envío están deshabilitados y la sección dice «Conecta el rodillo para enviarle una pendiente o una potencia.»

#### Scenario: Conexión perdida y recuperada
- **WHEN** el usuario elige 3,0 %, la envía, se pierde la conexión con el rodillo y se recupera
- **THEN** la pendiente elegida sigue siendo 3,0 %, los botones de envío vuelven a estar disponibles y el rodillo recibe la pendiente de la marcha puesta, no la de 3,0 %

### Requirement: Vuelta a pendiente 0 % al terminar
Al terminar la sesión con el Neo conectado, la app SHALL enviarle una pendiente de 0 %, para que no quede fija una potencia o una cuesta.

#### Scenario: Salir con ERG activo
- **WHEN** el Neo está en 300 W enviados a mano y el usuario termina la sesión
- **THEN** antes de desconectarse la app envía pendiente 0 % y el Neo deja de mantener los 300 W

#### Scenario: Terminar en una marcha dura
- **WHEN** la marcha es la 20 y el usuario termina la sesión
- **THEN** antes de desconectarse la app envía pendiente 0 %

### Requirement: Control manual temporal
Una pendiente o una potencia enviadas a mano SHALL sustituir a la resistencia de la marcha y a lo que pida Zwift solo mientras se está en Diagnóstico y el Neo sigue conectado; mientras tanto, las palancas SHALL seguir cambiando la marcha y lo que llegue de Zwift SHALL guardarse, sin enviar nada al Neo, y al salir de Diagnóstico SHALL enviarse lo que corresponde en ese momento.

#### Scenario: Cambiar de marcha con una potencia manual
- **WHEN** se envían 200 W a mano y se pulsa la palanca D1
- **THEN** la marcha sube, el Neo sigue en 200 W y la sección Simulación muestra la marcha nueva

#### Scenario: Volver a Rodaje
- **WHEN** el Neo está en 200 W enviados a mano, la marcha es la 15, Zwift no está conectado y se vuelve a Rodaje
- **THEN** el Neo deja los 200 W y simula la pendiente de la marcha 15

#### Scenario: Envío manual rodando en Zwift
- **WHEN** se rueda en una ruta de Zwift, se envían 3,0 % a mano y Zwift pasa a enviar una pendiente de 6 %
- **THEN** el Neo sigue en 3,0 % y la sección Simulación muestra la pendiente de Zwift de 6,0 %

#### Scenario: Volver a Rodaje rodando en Zwift
- **WHEN** Zwift envía una pendiente de 6 %, la marcha es la 13, hay una pendiente enviada a mano y se vuelve a Rodaje
- **THEN** el Neo simula +7 %

#### Scenario: Volver a Rodaje en un entrenamiento
- **WHEN** Zwift pide 230 W, hay 150 W enviados a mano y se vuelve a Rodaje
- **THEN** el Neo pasa a 230 W más el ajuste de las palancas
