## MODIFIED Requirements

### Requirement: Control manual temporal
Una pendiente o una potencia enviadas a mano SHALL sustituir a la resistencia de la marcha y a lo que pida Zwift solo mientras se está en Diagnóstico y el Neo sigue conectado; mientras tanto, las palancas SHALL seguir cambiando la marcha sin enviarla al Neo, lo que llegue de Zwift SHALL guardarse sin enviarlo, y al salir de Diagnóstico SHALL enviarse lo que corresponde ahora: la pendiente de la marcha puesta, con la de Zwift si está en una ruta, o la potencia de Zwift con el ajuste si está en un entrenamiento.

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
