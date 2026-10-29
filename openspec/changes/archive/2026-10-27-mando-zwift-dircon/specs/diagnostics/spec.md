## ADDED Requirements

### Requirement: Simulación de Zwift en Diagnóstico
Cuando Zwift envía su simulación de marchas virtuales, Diagnóstico SHALL mostrar en Conexiones, bajo la fila del mando, lo último recibido: pendiente, coeficiente de rodadura, coeficiente aerodinámico, masas, relación de la marcha de Zwift y la hora; y SHALL registrar cada simulación recibida y su respuesta, con su trama, en el registro. Si Zwift no ha enviado ninguna, SHALL NOT mostrar esa línea.

#### Scenario: Simulación recibida
- **WHEN** Zwift envía una pendiente de −1,64 % con una relación de marcha de 1,68
- **THEN** Diagnóstico muestra la pendiente −1,64 % y la relación 1,68 de Zwift con su hora, y el registro tiene la trama recibida y la respuesta

#### Scenario: Sin simulación de Zwift
- **WHEN** el mando va por Bluetooth o Zwift no ha enviado ninguna simulación
- **THEN** Diagnóstico no muestra la línea de la simulación de Zwift

#### Scenario: Diferencia con lo que simula el Neo
- **WHEN** Zwift envía una pendiente de 4 % y la marcha de la app es la 14
- **THEN** Diagnóstico muestra la pendiente de Zwift (4 %) y, aparte, lo que simula el Neo (+6 %), sin mezclarlos
