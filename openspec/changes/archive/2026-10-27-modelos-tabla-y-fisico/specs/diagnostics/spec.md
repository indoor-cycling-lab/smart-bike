# Spec Delta

## ADDED Requirements

### Requirement: Modelo físico en la simulación
Cuando el modelo es «Físico», la sección Simulación de Diagnóstico SHALL mostrar el nombre del modelo, la potencia calculada, la potencia mínima aplicada, la cadencia suavizada, la velocidad y la relación de la marcha. Con «Por tabla» SHALL mostrar el ajuste de la tabla para la marcha puesta, y con cualquier modelo la dificultad si no es 100 %. Cuando la potencia mínima o el alivio por cadencia baja actúan, SHALL indicarlo con texto, junto con la potencia que habría correspondido sin ellos.

#### Scenario: Físico rodando
- **WHEN** se rueda sin Zwift con el físico a 90 rpm en la 36×17 y el Neo está conectado
- **THEN** la sección dice modelo Físico, la potencia calculada, la mínima de 30 W, cadencia 90 rpm, velocidad en km/h, relación 2,12 y enviada al Neo en W

#### Scenario: Potencia mínima actuando
- **WHEN** en una bajada el físico calcula 12 W y la mínima es 30 W
- **THEN** la sección dice enviada al Neo 30 W y «Limitada por la potencia mínima: sin límite sería 12 W»

#### Scenario: Por tabla
- **WHEN** el modelo es «Por tabla» y la marcha 17 tiene +7,0 % en la tabla
- **THEN** la sección dice modelo Por tabla, ajuste +7,0 % y enviada al Neo la suma con el terreno

#### Scenario: Dificultad distinta de 100 %
- **WHEN** la dificultad es 120 %
- **THEN** la sección dice «Dificultad 120 %»

#### Scenario: Neo sin conexión
- **WHEN** el Neo no está conectado con el físico elegido
- **THEN** la sección dice modelo Físico, la velocidad y «Enviada al Neo» muestran «—»
