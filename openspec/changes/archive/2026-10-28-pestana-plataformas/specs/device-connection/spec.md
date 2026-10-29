# Spec Delta

## ADDED Requirements

### Requirement: Dispositivos de la app
Dispositivos SHALL mostrar los dispositivos a los que se conecta la app: una fila para el Neo y otra para el Square, con su nombre y su estado en palabra («Conectado», «Conectando», «Buscando», «No encontrado» o «Sin conexión») y, mientras la app intenta conectarlo, la ayuda para despertarlo. SHALL NOT mostrar filas de Zwift ni del mando de Zwift: lo que la app publica y su estado están en Plataformas (spec `platform-settings`). Al pie SHALL tener «Buscar dispositivos».

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados y se abre Dispositivos
- **THEN** las dos filas dicen «Conectado», con el nombre del Neo y del Square

#### Scenario: Primer uso
- **WHEN** se abre Dispositivos sin dispositivos recordados
- **THEN** el Neo y el Square dicen «Sin conexión» y «Buscar dispositivos» está disponible

#### Scenario: Neo dormido
- **WHEN** el Neo recordado está dormido y la app intenta conectarlo
- **THEN** su fila dice «Conectando» con «Pedalea para despertarlo»

#### Scenario: Sin filas de Zwift
- **WHEN** se abre Dispositivos con o sin wifi, con Zwift conectado o no
- **THEN** no hay ninguna fila de Zwift ni del mando, y el estado de Zwift no cambia nada de lo que se ve

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

## REMOVED Requirements

### Requirement: Pantalla de Dispositivos
**Reason**: Dispositivos deja de mostrar las filas de Zwift y del mando, que pasan a Plataformas.
**Migration**: Esta misma spec, «Dispositivos de la app», y spec `platform-settings`, «Estado de la plataforma y del mando».

### Requirement: Mando de Zwift por wifi en Dispositivos
**Reason**: La fila del mando de Zwift sale de Dispositivos, que queda solo con los dispositivos a los que se conecta la app.
**Migration**: Spec `platform-settings`, «Estado de la plataforma y del mando» (escenarios del mando por wifi).

### Requirement: Nombre que hay que elegir en Zwift
**Reason**: La ayuda de emparejamiento con Zwift sale de Dispositivos junto con las filas de Zwift y del mando.
**Migration**: Spec `platform-settings`, «Estado de la plataforma y del mando» (escenarios «Nombre en la ayuda» y «Cambio de nombre con la pantalla abierta»).
