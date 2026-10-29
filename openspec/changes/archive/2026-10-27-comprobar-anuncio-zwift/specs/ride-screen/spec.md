## MODIFIED Requirements

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar un chip para el Neo, otro para el Square y otro para Zwift, con su nombre y la forma de su estado, sin el estado en palabra: el punto lleno si está conectado; el anillo de espera, girando, para el Neo y el Square mientras la app intenta conectarlo o reconectarlo y para Zwift mientras la app se anuncia en la red wifi sin Zwift conectado; y el anillo vacío con el borde del chip de alerta si no lo intenta o, para Zwift, sin wifi o con Zwift «No visible» (spec `zwift-bridge`). Con «Mando de Zwift» activado, el chip «Zwift» SHALL tener el punto lleno solo si están conectados el rodillo y el mando, y el anillo de espera si el rodillo está conectado y el mando espera. El estado en palabra («conectando», «esperando», «sin conexión», «no visible»), también el del mando, SHALL leerse en la descripción accesible del chip y en Dispositivos, al tocarlo. Los chips SHALL caber en una línea y no cambiar de ancho al cambiar de estado.

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square, Zwift y el mando están conectados
- **THEN** se ven los chips Neo, Square y Zwift con el punto lleno

#### Scenario: Sin Zwift
- **WHEN** el Neo y el Square están conectados, el móvil tiene wifi y Zwift no está conectado
- **THEN** se ven los chips Neo y Square con el punto lleno y el chip «Zwift» con el anillo de espera (girando), sin la palabra «esperando»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** el chip «Zwift» se ve con el anillo vacío y el borde de alerta y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Zwift no visible
- **WHEN** se rueda sin Zwift conectado, con «Mando de Zwift» desactivado, y la app muestra Zwift como «No visible»
- **THEN** el chip «Zwift» se ve con el anillo vacío y el borde de alerta, sin cambiar de ancho, su descripción accesible dice «Zwift, no visible», y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Zwift no visible con el mando
- **WHEN** «Mando de Zwift» está activado, el mando espera y la app muestra Zwift como «No visible»
- **THEN** el chip «Zwift» se ve con el anillo vacío y el borde de alerta y su descripción accesible dice «Zwift, no visible; mando, esperando»

#### Scenario: Zwift vuelve a ser visible
- **WHEN** el chip «Zwift» está en «No visible» y el anuncio vuelve a responder
- **THEN** el chip pasa al anillo de espera, sin cambiar de ancho y sin que se mueva el resto de la pantalla

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** el chip «Square» pasa al anillo de espera, sin cambiar de ancho, la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** el chip «Square» se ve con el anillo vacío y el borde de alerta, y su descripción accesible dice «Square, sin conexión»

#### Scenario: Estado al tocar
- **WHEN** el chip «Square» tiene el anillo de espera y se toca
- **THEN** se abre Dispositivos, donde la fila del Square dice «Conectando»

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** el chip «Zwift» pasa al anillo de espera y el resto de la pantalla no cambia

#### Scenario: Rodillo conectado y mando esperando
- **WHEN** «Mando de Zwift» está activado, Zwift está conectado al rodillo y aún no al mando
- **THEN** el chip «Zwift» tiene el anillo de espera y su descripción accesible dice «Zwift, conectado; mando, esperando»

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y Zwift está conectado al rodillo
- **THEN** el chip «Zwift» tiene el punto lleno
