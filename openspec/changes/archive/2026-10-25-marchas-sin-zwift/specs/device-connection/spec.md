# Spec Delta

## MODIFIED Requirements

### Requirement: Conexión perdida
Si se pierde la conexión con un dispositivo ya conectado, la app SHALL mostrarlo en su fila y con un aviso, conservar el resto de conexiones y la marcha puesta, y permitir reconectarlo con «Buscar dispositivos»; en este change no reconecta sola.

#### Scenario: Square apagado a mitad
- **WHEN** el Square está conectado y se queda sin batería
- **THEN** su fila dice «Conexión perdida», el Neo sigue conectado y al tocar «Buscar dispositivos» el Square vuelve a conectarse cuando se despierta

#### Scenario: Neo perdido con una marcha puesta
- **WHEN** la marcha es la 17 y se pierde la conexión con el Neo
- **THEN** la fila del Neo dice «Conexión perdida», la marcha sigue siendo la 17 y, al reconectarlo con «Buscar dispositivos», el Neo vuelve a la resistencia de la 17

## REMOVED Requirements

### Requirement: Conexión mientras la app está abierta
**Reason**: Las conexiones pasan a vivir en la sesión de rodaje, que sigue con la pantalla apagada o la app en segundo plano y se cierra al terminarla.
**Migration**: Sustituido por «Conexión mientras dura la sesión» en esta capacidad y por la capacidad `ride-session`.

## ADDED Requirements

### Requirement: Conexión mientras dura la sesión
Las conexiones SHALL mantenerse mientras dure la sesión de rodaje, también con la pantalla apagada o la app en segundo plano, y SHALL cerrarse al terminarla.

#### Scenario: Pantalla apagada
- **WHEN** el Neo y el Square están conectados y la pantalla se apaga durante 5 minutos
- **THEN** al encenderla, las dos conexiones siguen activas sin haber tenido que buscar otra vez

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del Neo y del Square, que quedan libres para otra app
