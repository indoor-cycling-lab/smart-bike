# Spec Delta

## MODIFIED Requirements

### Requirement: Batería baja
La app SHALL considerar baja la batería del Square cuando su último nivel válido sea de 20 % o menos. El umbral es provisional hasta la prueba de 2 h con el equipo real. La batería baja SHALL avisarse en Dispositivos, en la sección Controlador, mientras el Square esté conectado, y SHALL dejar de avisarse cuando llegue un nivel válido por encima del umbral. Diagnóstico SHALL mostrar «baja» junto al nivel, sin aviso aparte. La batería baja SHALL NOT cambiar el funcionamiento de los botones ni de las conexiones.

#### Scenario: Cruzar el umbral
- **WHEN** el último nivel válido pasa de 25 % a 20 %
- **THEN** la batería se considera baja

#### Scenario: Justo por encima
- **WHEN** el último nivel válido es 21 %
- **THEN** la batería no se considera baja

#### Scenario: Sustituir la pila
- **WHEN** la batería está baja y, tras cambiar la pila, llega una lectura periódica de 100 %
- **THEN** deja de considerarse baja

#### Scenario: Square desconectado
- **WHEN** la batería está baja y el Square pierde la conexión
- **THEN** se muestra el estado de conexión del Square, no el aviso de batería

#### Scenario: Se sigue rodando
- **WHEN** la batería está baja
- **THEN** los botones, las marchas y las conexiones funcionan igual

#### Scenario: Dónde se avisa
- **WHEN** la batería está baja con el Square conectado
- **THEN** Dispositivos muestra el aviso en la sección Controlador y Diagnóstico solo dice «baja» junto al nivel
