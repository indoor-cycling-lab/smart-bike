# Spec Delta

## MODIFIED Requirements

### Requirement: Un solo cliente por Bluetooth
Con el modo por Bluetooth, la app SHALL atender a la plataforma que se conecte a ella y SHALL seguir conectada al Neo y al Square como hasta ahora, sin que el anuncio o la conexión de la plataforma afecten a esos enlaces. Cada aviso SHALL llegar solo a los dispositivos conectados a la app que estén suscritos a él, y un dispositivo que no recoge los avisos SHALL NOT retrasar los que van a la plataforma.

#### Scenario: Neo y Square conectados
- **WHEN** el Neo y el Square están conectados y Zwift se conecta a la app por Bluetooth
- **THEN** el Neo y el Square siguen conectados y las palancas siguen respondiendo

#### Scenario: Plataforma desconectada
- **WHEN** se rueda en una subida de Zwift al 5 % por Bluetooth y se cierra Zwift
- **THEN** el Neo mantiene +5 % durante 30 s, después pasa a la pendiente base con la marcha puesta y la app sigue anunciándose para que Zwift se reconecte sola

#### Scenario: Otro dispositivo conectado sin suscribirse
- **WHEN** Zwift está conectado por Bluetooth con el controlador Zwift y otro dispositivo cercano se conecta a la app y lee una característica sin suscribirse
- **THEN** cada pulsación del Square llega a Zwift con la misma latencia que sin ese dispositivo, y el otro dispositivo no recibe los avisos

#### Scenario: Escritura que no se admite
- **WHEN** un dispositivo conectado por Bluetooth envía una escritura que la app no puede atender (demasiado larga o a una característica que no la admite)
- **THEN** la app le responde con un error en lugar de dejarlo esperando, lo anota en el registro y no cambia la resistencia del Neo

#### Scenario: Dejar de publicar mientras se prepara el anuncio
- **WHEN** la app está preparando el anuncio por Bluetooth y el usuario cambia a «Solo wifi» o apaga el Bluetooth
- **THEN** la app no se queda anunciándose por Bluetooth: Zwift deja de listarla por Bluetooth
