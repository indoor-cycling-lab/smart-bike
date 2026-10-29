# Spec Delta

## MODIFIED Requirements

### Requirement: Enviar una pendiente
Con el rodillo conectado, la app SHALL enviarle la pendiente elegida al tocar «Enviar pend.», y el rodillo SHALL simularla: la resistencia depende de la velocidad, como en una cuesta.

#### Scenario: Subir al 10 %
- **WHEN** el usuario pedalea en pendiente 0 %, elige 10,0 % y toca «Enviar pend.»
- **THEN** la resistencia aumenta de forma perceptible en menos de un segundo y el envío aparece en el tráfico como «→ Rodillo»

#### Scenario: Bajada
- **WHEN** el usuario elige −5,0 % y toca «Enviar pend.»
- **THEN** el rodillo simula una bajada y la resistencia es menor que en 0 %

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
