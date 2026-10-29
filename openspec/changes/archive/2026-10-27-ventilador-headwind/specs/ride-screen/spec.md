# Spec Delta

## ADDED Requirements

### Requirement: Ventilador en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás (el pulsómetro incluido), un icono del ventilador solo si hay uno recordado, con los estados de los demás enlaces (conectado, esperando y sin conexión) y su descripción accesible en la de la barra («Ventilador, conectado»). El icono SHALL NOT mostrar el modo ni la velocidad, y la barra SHALL mantener su objetivo táctil de 48 dp y su ancho independiente del estado.

#### Scenario: Sin ventilador recordado
- **WHEN** no hay ventilador recordado
- **THEN** la barra muestra los enlaces de siempre y ningún ventilador

#### Scenario: Ventilador conectado
- **WHEN** hay ventilador recordado y está conectado
- **THEN** la barra termina con el icono del ventilador normal y su descripción dice «Ventilador, conectado»

#### Scenario: Ventilador reconectando
- **WHEN** el ventilador recordado se ha desconectado y la app intenta recuperarlo
- **THEN** el icono se ve atenuado con la marca que gira, y su descripción dice «Ventilador, reconectando»

#### Scenario: Ventilador sin conexión
- **WHEN** el ventilador recordado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el icono se ve tachado, y su descripción dice «Ventilador, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square, Zwift, el mando, el pulsómetro y el ventilador están en uso, a 360 dp en vertical
- **THEN** los seis iconos caben en la barra, sin recortarse ni empujar al icono de Ajustes ni a la hora, el wifi y la batería

### Requirement: Aviso de las acciones del ventilador
Tras una acción del ventilador, el pie SHALL mostrar 1,5 s su resultado: «Ventilador: 50 %» al cambiar la velocidad (también en un extremo), «Ventilador: manual · 40 %» o «Ventilador: automático» al alternar, «Ventilador en automático» al pedir velocidad en automático y «Sin ventilador» si no hay conexión. Una acción nueva SHALL sustituir a la anterior, el aviso SHALL respetar «Mostrar la acción» y Rodaje SHALL NOT mostrar el modo ni la velocidad del ventilador en otro sitio.

#### Scenario: Subir la velocidad
- **WHEN** el ventilador está en manual al 40 % y se pulsa «Ventilador: + velocidad»
- **THEN** el pie dice «Ventilador: 50 %» durante 1,5 s

#### Scenario: Acciones seguidas
- **WHEN** se pulsa «Ventilador: + velocidad» y, antes de 1,5 s, «Ventilador: − velocidad»
- **THEN** el aviso del segundo sustituye al primero, sin apilarse

#### Scenario: Alternar
- **WHEN** el ventilador está en manual al 40 % y se pulsa «Ventilador: manual ⇄ automático»
- **THEN** el pie dice «Ventilador: automático»

#### Scenario: Velocidad en automático
- **WHEN** el ventilador está en automático y se pulsa «Ventilador: − velocidad»
- **THEN** el pie dice «Ventilador en automático»

#### Scenario: Sin ventilador
- **WHEN** el ventilador no está conectado y se pulsa «Ventilador: + velocidad»
- **THEN** el pie dice «Sin ventilador»

#### Scenario: Aviso desactivado
- **WHEN** «Mostrar la acción» está desactivada y se pulsa «Ventilador: + velocidad»
- **THEN** el ventilador cambia y el pie no muestra ningún aviso

#### Scenario: Sin velocidad en la rejilla
- **WHEN** se rueda con el ventilador en manual al 70 %
- **THEN** Rodaje no muestra 70 % en ningún sitio, y el icono del ventilador no cambia por la velocidad
