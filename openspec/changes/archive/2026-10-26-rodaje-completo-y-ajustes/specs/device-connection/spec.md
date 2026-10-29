# Spec Delta

## ADDED Requirements

### Requirement: Pantalla de Dispositivos
Dispositivos SHALL mostrar una fila para el Neo y otra para el Square, con su nombre y su estado en palabra («Conectado», «Conectando», «Buscando», «No encontrado» o «Sin conexión») y, mientras la app intenta conectarlo, la ayuda para despertarlo; y una fila para Zwift con su estado («Conectado», «Esperando» o «Sin conexión») y cómo emparejarlo. Al pie SHALL tener «Buscar dispositivos».

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square y Zwift están conectados y se abre Dispositivos
- **THEN** las tres filas dicen «Conectado», con el nombre del Neo y del Square

#### Scenario: Primer uso
- **WHEN** se abre Dispositivos sin dispositivos recordados
- **THEN** el Neo y el Square dicen «Sin conexión» y «Buscar dispositivos» está disponible

#### Scenario: Neo dormido
- **WHEN** el Neo recordado está dormido y la app intenta conectarlo
- **THEN** su fila dice «Conectando» con «Pedalea para despertarlo»

#### Scenario: Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** la fila de Zwift dice «Esperando» y explica que en la pantalla de emparejamiento de Zwift se elige «Square Shift» como rodillo, potencia y cadencia

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la fila de Zwift dice «Sin conexión» y pide conectar el móvil a la misma red wifi que Zwift

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth del móvil está apagado y se abre Dispositivos
- **THEN** el Neo y el Square dicen «Sin conexión» y se ve el aviso «El Bluetooth está apagado. Actívalo para buscar el Neo y el Square.» con «Activar Bluetooth», como en «Bluetooth apagado»

#### Scenario: Sin permiso
- **WHEN** falta el permiso de Bluetooth y se toca «Buscar dispositivos» en Dispositivos
- **THEN** la app pide el permiso y, si se deniega, muestra el aviso con «Abrir ajustes», como en «Permisos de Bluetooth»

#### Scenario: No encontrado con aviso
- **WHEN** el Neo no aparece en 30 s de búsqueda desde Dispositivos
- **THEN** su fila dice «No encontrado» y se ve el aviso de «Dispositivo no encontrado» con qué hacer

#### Scenario: Conexión perdida con la pantalla abierta
- **WHEN** Dispositivos está abierta y se pierde la conexión con el Square
- **THEN** su fila pasa a «Conectando» con «Pulsa un botón del Square para despertarlo», y vuelve a «Conectado» al reconectar

### Requirement: Olvidar un dispositivo
Cada fila del Neo o del Square con un dispositivo recordado SHALL tener «Olvidar». Olvidar SHALL desconectarlo y SHALL hacer que la app deje de conectarse a él sola, también en las siguientes sesiones, hasta que una búsqueda lo vuelva a encontrar. Sin dispositivo recordado, la fila SHALL NOT tener «Olvidar». La marcha SHALL NOT cambiar.

#### Scenario: Olvidar el Square
- **WHEN** el Square está conectado y se toca «Olvidar» en su fila
- **THEN** el Square se desconecta, su fila dice «Sin conexión» sin «Olvidar» y la marcha sigue igual

#### Scenario: Olvidado en la siguiente sesión
- **WHEN** se olvida el Neo, se termina la sesión y se vuelve a abrir la app con el Neo encendido
- **THEN** la app no se conecta al Neo hasta tocar «Buscar dispositivos»

#### Scenario: Volver a buscar
- **WHEN** se ha olvidado el Square y se toca «Buscar dispositivos» con el Square despierto
- **THEN** la app lo encuentra, se conecta y lo vuelve a recordar

#### Scenario: Olvidar el Neo rodando
- **WHEN** se rueda en la marcha 14 y se olvida el Neo
- **THEN** el Neo se desconecta sin cambios de resistencia pendientes, la potencia y la cadencia muestran «—» en Rodaje y la marcha sigue en la 14

#### Scenario: Olvidar mientras conecta
- **WHEN** el Neo recordado está dormido, su fila dice «Conectando» y se toca «Olvidar»
- **THEN** la app deja de intentarlo y la fila dice «Sin conexión»
