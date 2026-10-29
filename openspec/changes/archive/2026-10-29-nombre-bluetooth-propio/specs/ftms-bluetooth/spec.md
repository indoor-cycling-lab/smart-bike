# Spec Delta

## MODIFIED Requirements

### Requirement: Nombre del móvil ante la plataforma
La plataforma SHALL ver la app con el nombre Bluetooth del móvil. Con «Usar el nombre también por Bluetooth» desmarcada, ese nombre SHALL ser el del móvil, sin cambiarlo. Con la casilla marcada, SHALL ser el nombre ante la app, porque el móvil se renombra mientras publica (spec `bluetooth-name`).

#### Scenario: Nombre del móvil
- **WHEN** «Usar el nombre también por Bluetooth» está desmarcada, el nombre Bluetooth del móvil es «OnePlus 8» y se abre el emparejamiento de Zwift
- **THEN** Zwift lista «OnePlus 8» y no «Smart Bike» ni «KICKR BIKE PRO»

#### Scenario: Nombre elegido
- **WHEN** «Usar el nombre también por Bluetooth» está marcada, «Smart Bike» está elegido y se abre el emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike» y no «OnePlus 8»
