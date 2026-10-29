# Spec Delta

## ADDED Requirements

### Requirement: Estado de la plataforma por Bluetooth en Rodaje
Con el modo «Rodillo por Bluetooth (FTMS)», el estado de la plataforma de Rodaje SHALL usar las mismas palabras y formas que con wifi («Conectado a Zwift», «Esperando conexión a Zwift…»), SHALL decir «Sin Bluetooth para Zwift» (aro vacío con borde de alerta) cuando el Bluetooth esté apagado o falte un permiso, y «Zwift no disponible» cuando el móvil no pueda anunciarse como dispositivo Bluetooth. SHALL NOT decir «Sin wifi para Zwift» ni «sin mando». Tocarlo SHALL abrir Plataformas.

#### Scenario: Esperando por Bluetooth
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)», el Bluetooth está encendido y Zwift no está conectado
- **THEN** Rodaje muestra «Esperando conexión a Zwift…» atenuado y con el aro que gira

#### Scenario: Conectado por Bluetooth
- **WHEN** Zwift está conectado al rodillo por Bluetooth
- **THEN** Rodaje muestra «Conectado a Zwift» con el punto, sin «sin mando»

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** Zwift está conectado al rodillo por Bluetooth y aún no ha saludado al mando
- **THEN** Rodaje sigue mostrando «Conectado a Zwift», sin «sin mando», porque el mando va dentro del mismo dispositivo

#### Scenario: Bluetooth apagado
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el Bluetooth del móvil está apagado
- **THEN** Rodaje muestra «Sin Bluetooth para Zwift» con el aro vacío y el borde de alerta, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Móvil sin modo periférico
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** Rodaje muestra «Zwift no disponible» con el aro vacío y el borde de alerta

#### Scenario: Sin wifi con el modo por Bluetooth
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el móvil no tiene wifi
- **THEN** el estado de la plataforma no cambia
