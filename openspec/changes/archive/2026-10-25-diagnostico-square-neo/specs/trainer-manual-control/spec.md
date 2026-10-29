# Spec Delta

## Purpose

Fijar a mano en el Tacx Neo una pendiente o una potencia, para comprobar que el rodillo obedece a la app sin marchas ni Zwift y para diagnosticar la resistencia.

## ADDED Requirements

### Requirement: Enviar una pendiente
Con el Neo conectado, la app SHALL enviarle la pendiente elegida al tocar «Enviar pendiente», y el Neo SHALL simularla: la resistencia depende de la velocidad, como en una cuesta.

#### Scenario: Subir al 10 %
- **WHEN** el usuario pedalea en pendiente 0 %, elige 10,0 % y toca «Enviar pendiente»
- **THEN** la resistencia aumenta de forma perceptible en menos de un segundo y el envío aparece en el tráfico como «→ Neo»

#### Scenario: Bajada
- **WHEN** el usuario elige −5,0 % y toca «Enviar pendiente»
- **THEN** el Neo simula una bajada y la resistencia es menor que en 0 %

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
La app SHALL mostrar qué se envió al Neo por última vez y a qué hora, en la sección de control manual.

#### Scenario: Tras enviar una potencia
- **WHEN** el usuario envía 200 W a las 12:04:31
- **THEN** la sección dice «Último envío: 200 W · 12:04:31»

### Requirement: Envío sin Neo
Sin el Neo conectado, la app SHALL deshabilitar «Enviar pendiente» y «Enviar potencia», conservar los valores elegidos y explicar por qué no se puede enviar.

#### Scenario: Neo desconectado
- **WHEN** el Neo no está conectado
- **THEN** los dos botones de envío están deshabilitados y la sección dice «Conecta el Neo para enviarle una pendiente o una potencia.»

#### Scenario: Conexión perdida y recuperada
- **WHEN** el usuario elige 3,0 %, se pierde la conexión con el Neo y se recupera
- **THEN** la pendiente elegida sigue siendo 3,0 % y los botones de envío vuelven a estar disponibles, sin que se reenvíe nada sin tocarlos

### Requirement: Vuelta a pendiente 0 % al terminar
Al salir de Diagnóstico con el Neo conectado, la app SHALL enviarle una pendiente de 0 %, para que no quede fija una potencia o una cuesta.

#### Scenario: Salir con ERG activo
- **WHEN** el Neo está en 300 W enviados a mano y el usuario sale de la app
- **THEN** antes de desconectarse la app envía pendiente 0 % y el Neo deja de mantener los 300 W
