# Spec Delta

## MODIFIED Requirements

### Requirement: Estado de la plataforma en Rodaje
Rodaje SHALL mostrar, a la izquierda de la fila de Ajustes (con el modo y la transmisión en la fila de debajo), el estado de la plataforma elegida en Plataformas (hoy Zwift), con palabra y forma: «Conectado a Zwift» (punto), «Esperando conexión a Zwift…» (aro que gira, atenuado), «Zwift no visible» o «Sin wifi para Zwift» (aro vacío con borde de alerta). Con el mando ofrecido aparte y sin conectar, SHALL decir «Conectado a Zwift · sin mando». Tocarlo SHALL abrir Plataformas.

#### Scenario: Esperando a Zwift
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** Rodaje muestra «Esperando conexión a Zwift…» en la fila de Ajustes, atenuado y con el aro que gira, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Conectado a Zwift
- **WHEN** Zwift está conectado al rodillo y, si el modo lo ofrece aparte, al mando
- **THEN** Rodaje muestra «Conectado a Zwift» con el punto, sin cambiar la marcha ni el área de datos

#### Scenario: Rodillo conectado y mando esperando
- **WHEN** el modo es «Rodillo por wifi (FTMS)» con «Mando de Zwift» activado, Zwift está conectado al rodillo y aún no al mando
- **THEN** Rodaje muestra «Conectado a Zwift · sin mando» y la descripción accesible dice «Plataforma: Zwift, conectado; mando, esperando»

#### Scenario: Zwift Ride
- **WHEN** el modo es «Todo por wifi (modo Zwift Ride)» y Zwift está conectado
- **THEN** Rodaje muestra «Conectado a Zwift», sin «sin mando», porque el mando va dentro del mismo dispositivo

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y Zwift está conectado al rodillo
- **THEN** Rodaje muestra «Conectado a Zwift», sin «sin mando»

#### Scenario: Zwift no visible
- **WHEN** la app muestra Zwift como «No visible» (spec `platform-bridge`)
- **THEN** Rodaje muestra «Zwift no visible» con el aro vacío y el borde de alerta, y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** Rodaje muestra «Sin wifi para Zwift» con el aro vacío y el borde de alerta

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** el estado pasa a «Esperando conexión a Zwift…» y el resto de la pantalla no cambia

#### Scenario: Tocar el estado
- **WHEN** se toca el estado de la plataforma, que mide al menos 48 dp de alto
- **THEN** se abre Plataformas con la barra inferior, y Atrás o «Rodar» vuelven a Rodaje con la sesión en marcha

#### Scenario: Sin cambiar el área de datos
- **WHEN** el estado de la plataforma pasa de «Esperando conexión a Zwift…» a «Conectado a Zwift»
- **THEN** la rejilla de datos mantiene su área y su cifra, y nada se mueve

#### Scenario: Nombre de la plataforma
- **WHEN** la plataforma elegida en Plataformas es otra distinta de Zwift
- **THEN** el estado nombra esa plataforma en lugar de Zwift
