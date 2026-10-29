# Spec Delta

## Purpose

Conectar la app con el Tacx Neo y el Elite Square por Bluetooth y mostrar en todo momento el estado de cada conexión, incluidos los fallos que impiden conectar.

## ADDED Requirements

### Requirement: Buscar y conectar el Neo y el Square
Al tocar «Buscar dispositivos», la app SHALL buscar a la vez un rodillo cuyo nombre empiece por `Tacx Neo` y un mando cuyo nombre empiece por `SQUARE`, sin distinguir mayúsculas, y SHALL conectarse al primero de cada tipo que encuentre, sin que el usuario tenga que elegirlo de una lista.

#### Scenario: Los dos encendidos
- **WHEN** el Neo y el Square están encendidos y despiertos, y el usuario toca «Buscar dispositivos»
- **THEN** cada uno pasa por «Buscando» y «Conectando» hasta «Conectado», cada uno a su ritmo

#### Scenario: Uno ya conectado
- **WHEN** el Neo está conectado, el Square no, y el usuario toca «Buscar dispositivos»
- **THEN** la app busca solo el Square y la conexión del Neo no se interrumpe

### Requirement: Estado de cada conexión visible
La app SHALL mostrar el estado de la conexión del Neo y del Square por separado, con palabra y forma además de color: buscando, conectando, conectado, no encontrado o conexión perdida.

#### Scenario: Square buscando
- **WHEN** la app busca el Square y aún no lo encuentra
- **THEN** su fila dice «Buscando» con la ayuda «Pulsa cualquier botón para despertarlo»

#### Scenario: Neo buscando
- **WHEN** la app busca el Neo y aún no lo encuentra
- **THEN** su fila dice «Buscando» con la ayuda «Pedalea para despertarlo»

### Requirement: Dispositivo no encontrado
Si un dispositivo no aparece en 30 s de búsqueda, la app SHALL dejar de buscarlo, mostrar que no se ha encontrado con qué hacer y permitir volver a buscar.

#### Scenario: Neo apagado
- **WHEN** el Neo está desenchufado y pasan 30 s de búsqueda
- **THEN** su fila dice «No encontrado», aparece el aviso de error «No se encuentra el Neo. Comprueba que está enchufado, pedalea para despertarlo y vuelve a buscar.» y «Buscar dispositivos» vuelve a estar disponible

#### Scenario: Square dormido
- **WHEN** el Square no se despierta y pasan 30 s de búsqueda
- **THEN** su fila dice «No encontrado» y el aviso dice «No se encuentra el Square. Pulsa cualquier botón para despertarlo y vuelve a buscar.»

### Requirement: Conexión perdida
Si se pierde la conexión con un dispositivo ya conectado, la app SHALL mostrarlo en su fila y con un aviso, conservar el resto de conexiones y permitir reconectarlo con «Buscar dispositivos»; en este change no reconecta sola.

#### Scenario: Square apagado a mitad
- **WHEN** el Square está conectado y se queda sin batería
- **THEN** su fila dice «Conexión perdida», el Neo sigue conectado y al tocar «Buscar dispositivos» el Square vuelve a conectarse cuando se despierta

### Requirement: Permisos de Bluetooth
Si falta el permiso de Bluetooth necesario para buscar y conectar, la app SHALL pedirlo al tocar «Buscar dispositivos» y, si se deniega, SHALL explicar para qué hace falta y ofrecer abrir los ajustes de la app, sin buscar.

#### Scenario: Permiso denegado
- **WHEN** el usuario deniega el permiso de dispositivos cercanos
- **THEN** aparece el aviso de error «Sin permiso para buscar dispositivos Bluetooth. Concédelo en los ajustes de la app.» con el botón «Abrir ajustes», y no se busca nada

#### Scenario: Permiso concedido
- **WHEN** el usuario concede el permiso tras tocar «Buscar dispositivos»
- **THEN** la búsqueda empieza sin tener que volver a tocar el botón

### Requirement: Bluetooth apagado
Si el Bluetooth del móvil está apagado, la app SHALL decirlo y ofrecer activarlo; al apagarse con dispositivos conectados, SHALL mostrar las dos conexiones como perdidas.

#### Scenario: Bluetooth apagado al buscar
- **WHEN** el Bluetooth está apagado y el usuario toca «Buscar dispositivos»
- **THEN** aparece el aviso de error «El Bluetooth está apagado. Actívalo para buscar el Neo y el Square.» con el botón «Activar Bluetooth», y no se busca nada

#### Scenario: Bluetooth apagado durante la sesión
- **WHEN** el Neo y el Square están conectados y el usuario apaga el Bluetooth
- **THEN** las dos filas dicen «Conexión perdida» y aparece el aviso de Bluetooth apagado

### Requirement: Conexión mientras la app está abierta
Las conexiones SHALL mantenerse mientras la app esté abierta en primer plano y SHALL cerrarse al salir de la app; en este change no se mantienen en segundo plano.

#### Scenario: Cerrar la app
- **WHEN** el usuario sale de la app con el botón atrás desde Diagnóstico
- **THEN** la app se desconecta del Neo y del Square, que quedan libres para otra app
