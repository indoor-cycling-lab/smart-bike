## ADDED Requirements

### Requirement: Estado de la publicación en Rodaje
Rodaje SHALL mostrar, a la izquierda de la fila de Ajustes, el estado del rodillo publicado sin nombrar ninguna app de entrenamiento, con palabra y forma, sin decir por dónde va: «Conectado» (punto), «Esperando conexión…» (aro que gira, atenuado), y «No visible en la red», «Sin wifi», «Sin Bluetooth», «No disponible» o «Sin rodillo» (aro vacío con borde de alerta). Solo SHALL nombrar el transporte cuando falte. Tocarlo SHALL abrir Conexión.

#### Scenario: Esperando
- **WHEN** hay un rodillo recordado, el transporte del modo está disponible y ninguna app de entrenamiento está conectada
- **THEN** Rodaje muestra «Esperando conexión…» atenuado y con el aro que gira, y la marcha, la potencia y la cadencia siguen como sin app de entrenamiento

#### Scenario: Conectado por wifi
- **WHEN** el modo es «Wifi + Bluetooth» y la app de entrenamiento recibe el rodillo, el mando y, si hay pulsómetro, el pulso
- **THEN** Rodaje muestra «Conectado» con el punto, sin «wifi» ni «Bluetooth», sin cambiar la marcha ni el área de datos

#### Scenario: Conectado por Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y la app de entrenamiento recibe el rodillo
- **THEN** Rodaje muestra «Conectado» con el punto, igual que por wifi

#### Scenario: Sin rodillo recordado
- **WHEN** no hay rodillo recordado
- **THEN** Rodaje muestra «Sin rodillo» con el aro vacío y el borde de alerta, y tocarlo abre Conexión

#### Scenario: No visible
- **WHEN** el anuncio por wifi no responde (spec `platform-bridge`)
- **THEN** Rodaje muestra «No visible en la red» con el aro vacío y el borde de alerta, y la marcha, la potencia y la cadencia siguen como sin app de entrenamiento

#### Scenario: Sin wifi
- **WHEN** el modo usa wifi para el rodillo y el móvil no tiene wifi
- **THEN** Rodaje muestra «Sin wifi» con el aro vacío y el borde de alerta

#### Scenario: Sin Bluetooth
- **WHEN** el modo es «Solo Bluetooth» y el Bluetooth está apagado o falta un permiso
- **THEN** Rodaje muestra «Sin Bluetooth» con el aro vacío y el borde de alerta, y no dice nada del wifi

#### Scenario: No disponible
- **WHEN** el modo es «Solo Bluetooth» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** Rodaje muestra «No disponible» con el aro vacío y el borde de alerta

#### Scenario: Desconectado a mitad
- **WHEN** se rueda con la app de entrenamiento conectada y se cierra
- **THEN** el estado pasa a «Esperando conexión…» y el resto de la pantalla no cambia

#### Scenario: Tocar el estado
- **WHEN** se toca el estado, que mide al menos 48 dp de alto
- **THEN** se abre Conexión con la barra inferior, y Atrás o «Rodar» vuelven a Rodaje con la sesión en marcha

#### Scenario: Sin cambiar el área de datos
- **WHEN** el estado pasa de «Esperando conexión…» a «Conectado»
- **THEN** la rejilla de datos mantiene su área y su cifra, y nada se mueve

### Requirement: Lo que falta en el estado de Rodaje
Con el rodillo conectado, el estado de Rodaje SHALL añadir «· sin mando» si el modo es «Wifi + Bluetooth», hay un Square recordado y la app de entrenamiento no ha saludado al mando; «· sin pulso» si hay un pulsómetro recordado y no está suscrita al pulso; o «· sin mando ni pulso» si faltan los dos. Lo no recordado SHALL NOT mencionarse.

#### Scenario: Mando esperando
- **WHEN** el modo es «Wifi + Bluetooth», el rodillo está conectado, hay un Square recordado y el mando no se ha saludado
- **THEN** Rodaje muestra «Conectado · sin mando» y la descripción accesible dice «Conexión: rodillo, conectado; mando, esperando»

#### Scenario: Pulso sin elegir
- **WHEN** el rodillo y el mando están conectados, hay un pulsómetro recordado y la app de entrenamiento no está suscrita al pulso
- **THEN** Rodaje muestra «Conectado · sin pulso»

#### Scenario: Mando dentro del mismo dispositivo
- **WHEN** el modo es «Solo Bluetooth» o «Solo wifi (modo Zwift Ride)», el rodillo está conectado, hay un Square recordado y la app de entrenamiento aún no ha saludado al mando
- **THEN** Rodaje muestra «Conectado», sin «sin mando», porque el mando va dentro del mismo dispositivo

#### Scenario: Sin Square ni pulsómetro recordados
- **WHEN** el rodillo está conectado y no hay Square ni pulsómetro recordados
- **THEN** Rodaje muestra «Conectado», sin mencionar el mando ni el pulso

#### Scenario: Banda sin lectura
- **WHEN** la app de entrenamiento está suscrita al pulso y la banda no da lecturas
- **THEN** Rodaje no añade «sin pulso»: lo que falta es la banda, que se ve en la barra de conexiones

## MODIFIED Requirements

### Requirement: Aviso de giro
Con un Square recordado, Rodaje SHALL indicar el giro de Zwift con una flecha a cada lado de la marcha: la izquierda mientras se mantiene el giro a la izquierda y la derecha mientras se mantiene el giro a la derecha, con el botón que tenga asignado el giro. Un giro breve SHALL verse al menos 0,5 s. Con el freno de Zwift de la misma palanca pulsado, SHALL NOT mostrarse su giro, como en Zwift.

#### Scenario: Girar a la izquierda
- **WHEN** se mantiene X (giro a la izquierda por defecto)
- **THEN** se ve la flecha de la izquierda a la izquierda de la marcha y, al soltar, desaparece

#### Scenario: Girar a la derecha
- **WHEN** se mantiene Círculo (giro a la derecha por defecto)
- **THEN** se ve la flecha de la derecha a la derecha de la marcha y, al soltar, desaparece

#### Scenario: Pulsación breve
- **WHEN** el giro a la derecha se asigna a una pulsación corta de un botón y se pulsa
- **THEN** la flecha de la derecha se ve al menos 0,5 s

#### Scenario: Giro con otro botón
- **WHEN** se reasigna el giro a la izquierda a la palanca I2 y se mantiene
- **THEN** se ve la flecha de la izquierda

#### Scenario: Los dos giros
- **WHEN** se mantienen X y Círculo a la vez
- **THEN** se ven las dos flechas

#### Scenario: Freno y giro
- **WHEN** se mantienen el freno izquierdo de Zwift y el giro a la izquierda
- **THEN** no se ve la flecha de la izquierda

#### Scenario: Mando sin conectar
- **WHEN** el mando publicado aún no se ha conectado a la app de entrenamiento y se mantiene X
- **THEN** se ve la flecha de la izquierda, igual que con el mando conectado

#### Scenario: Mando desactivado
- **WHEN** la configuración venía de una versión con «Mando de Zwift» desactivado, hay un Square recordado y se mantiene X
- **THEN** se ve la flecha de la izquierda: el mando ya no se puede desactivar

#### Scenario: Se pierde el Square girando
- **WHEN** se mantiene X y el Square pierde la conexión
- **THEN** la flecha desaparece

#### Scenario: Aviso de acción a la vez
- **WHEN** se pulsa la palanca D1 (+1 marcha) y, antes de 1,5 s, se mantiene X
- **THEN** el pie sigue diciendo «+1 marcha» y se ve la flecha de la izquierda

## REMOVED Requirements

### Requirement: Estado de la plataforma en Rodaje
**Reason**: Ya no se elige plataforma y la app no sabe qué app de entrenamiento se ha conectado.
**Migration**: Ver «Estado de la publicación en Rodaje» y «Lo que falta en el estado de Rodaje».

### Requirement: Estado de la plataforma por Bluetooth en Rodaje
**Reason**: Se integra en el estado único de la publicación, válido para los tres modos.
**Migration**: Ver «Estado de la publicación en Rodaje» (escenarios «Conectado por Bluetooth», «Sin Bluetooth» y «No disponible»).
