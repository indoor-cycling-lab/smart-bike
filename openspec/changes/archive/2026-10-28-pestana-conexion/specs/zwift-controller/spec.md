## MODIFIED Requirements

### Requirement: Botones mantenidos hacia Zwift
Con Zwift conectado al mando, una función de Zwift asignada a «pulsar» SHALL llegar a Zwift como pulsada mientras se mantiene el botón y como suelta al soltarlo, sin esperar a otro gesto ni repetirse.

#### Scenario: Navegar un menú
- **WHEN** con la asignación por defecto se pulsa y suelta la cruceta ↓ y luego A en un menú de Zwift
- **THEN** Zwift baja una opción y la selecciona

#### Scenario: Elegir desvío
- **WHEN** se mantiene la cruceta ← al llegar a un cruce en Zwift
- **THEN** Zwift elige el desvío de la izquierda

#### Scenario: Powerup y Ride On
- **WHEN** con la asignación por defecto se pulsa Y con un powerup disponible y luego Z con otro ciclista cerca
- **THEN** Zwift usa el powerup y da Ride On

#### Scenario: Antes del handshake
- **WHEN** Zwift se ha conectado al mando pero aún no lo ha saludado y se pulsa A
- **THEN** no se envía nada a Zwift y la marcha no cambia

#### Scenario: Mando desactivado
- **WHEN** el mando de la app no está emparejado en Zwift, el Square sí, y se pulsa A
- **THEN** la app no envía nada a Zwift y Zwift recibe A directamente del Square

### Requirement: Mando publicado con la sesión de rodaje
Mientras dura la sesión de rodaje, hay un Square recordado y el modo es «Wifi + Bluetooth», la app SHALL anunciarse por Bluetooth como mando de Zwift, con el nombre Bluetooth del móvil, aunque el rodillo, la potencia y la cadencia vayan por wifi, sin interruptor que lo desactive. Al terminar la sesión, olvidar el Square o cambiar a otro modo, SHALL dejar de anunciarse aparte y Zwift SHALL perder ese mando.

#### Scenario: Abrir la app con el ajuste activado
- **WHEN** se abre la app con un Square recordado, el modo «Wifi + Bluetooth» y el Bluetooth encendido
- **THEN** el mando se anuncia sin tocar nada y Conexión dice «Esperando» en la fila del mando

#### Scenario: Square dormido
- **WHEN** el Square recordado está dormido o reconectando
- **THEN** el mando se sigue anunciando y Zwift no lo pierde; los botones llegan en cuanto el Square se conecta

#### Scenario: Desactivar el ajuste rodando
- **WHEN** Zwift está conectado al mando y se olvida el Square en Dispositivos
- **THEN** Zwift pierde el mando y el rodillo sigue conectado por wifi sin cambios

#### Scenario: Sin Square recordado
- **WHEN** se abre la app sin Square recordado en el modo «Wifi + Bluetooth»
- **THEN** el mando no se anuncia y Conexión dice que no se ofrece

#### Scenario: Terminar la sesión
- **WHEN** se termina la sesión con el mando conectado a Zwift
- **THEN** el mando deja de anunciarse y Zwift lo da por desconectado

#### Scenario: Bluetooth apagado
- **WHEN** hay un Square recordado con el modo «Wifi + Bluetooth» y se apaga el Bluetooth del móvil
- **THEN** el mando deja de anunciarse y, al volver a encender el Bluetooth, se anuncia otra vez sin tocar nada

#### Scenario: Móvil sin modo periférico
- **WHEN** hay un Square recordado con el modo «Wifi + Bluetooth» en un móvil que no puede anunciarse como dispositivo Bluetooth
- **THEN** la app no anuncia el mando, Conexión dice «No disponible» en la fila del mando y el resto de la sesión funciona igual
