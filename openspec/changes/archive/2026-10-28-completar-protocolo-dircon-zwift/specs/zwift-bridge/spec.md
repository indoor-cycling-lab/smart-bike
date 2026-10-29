# Spec Delta

## ADDED Requirements

### Requirement: Información del dispositivo hacia Zwift
Con el rodillo de la app emparejado en Zwift por wifi, en cualquiera de los dos modos de conexión, la app SHALL ofrecer a Zwift, además del fabricante, el modelo y la versión de la app, un número de serie, una versión de firmware y una versión de hardware, y SHALL contestar a su lectura sin error. El número de serie SHALL ser el de la identidad propia de la app ante Zwift y SHALL conservarse entre sesiones.

#### Scenario: Zwift lee la información al emparejar
- **WHEN** Zwift empareja el rodillo por wifi y lee la información del dispositivo
- **THEN** el registro de sesión muestra la lectura de la serie, el firmware y el hardware, cada una contestada con un valor, y Zwift termina de emparejar sin mensajes de error

#### Scenario: Misma serie en cada sesión
- **WHEN** se cierran y se vuelven a abrir la app y Zwift con el rodillo ya emparejado
- **THEN** Zwift lee el mismo número de serie y reconoce el dispositivo sin volver a emparejarlo

#### Scenario: Dos modos, dos identidades
- **WHEN** se cambia entre «Rodillo por wifi y mando por Bluetooth» y «Todo por wifi (modo Zwift Ride)»
- **THEN** cada modo ofrece la serie de su propia identidad, y la de uno no aparece en el otro

#### Scenario: Con el simulador de KICKR BIKE
- **WHEN** «Simular un KICKR BIKE» está activado y Zwift lee la información del dispositivo
- **THEN** Zwift recibe la serie de esa identidad, que es la misma en cada sesión, y el resto de lo observable no cambia

#### Scenario: Sin Neo conectado
- **WHEN** Zwift lee la información del dispositivo con el Neo desconectado
- **THEN** Zwift la recibe igual, porque no depende del Neo
