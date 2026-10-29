# Spec Delta

## ADDED Requirements

### Requirement: Batería baja del Square en Dispositivos
Con el Square conectado y su batería baja (spec `square-battery`), la sección Controlador de Dispositivos SHALL mostrar un aviso informativo, con icono y texto, que diga el nivel y que revise la pila antes de rodar, sin depender solo del color. SHALL dejar de mostrarlo cuando llegue un nivel válido por encima del umbral, y SHALL NOT mostrarlo con el Square sin conexión ni sin Square recordado. El aviso SHALL NOT cambiar el estado de conexión del Controlador ni deshabilitar sus botones.

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su último nivel válido es 15 %
- **THEN** la sección Controlador dice «Conectado» y muestra el aviso informativo «Batería del Square baja (15 %). Revisa la pila antes de rodar.»

#### Scenario: Batería recuperada
- **WHEN** llega una lectura periódica de 100 % tras cambiar la pila
- **THEN** el aviso desaparece

#### Scenario: Square sin conexión
- **WHEN** la batería estaba baja y el Square pierde la conexión
- **THEN** la sección Controlador dice el estado de conexión y no muestra el aviso de batería

#### Scenario: Sin Square recordado
- **WHEN** no hay ningún Square recordado
- **THEN** no hay aviso de batería
