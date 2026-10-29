# Spec Delta

## ADDED Requirements

### Requirement: Pulsómetro en la barra de conexiones
La barra de conexiones de Rodaje SHALL añadir, después de las demás, un icono del pulsómetro (un corazón) solo si hay un pulsómetro recordado, con los mismos estados y formas que los demás enlaces: conectado, esperando (reconectando o buscando) y sin conexión. Su descripción accesible SHALL entrar en la de la barra («Pulsómetro, conectado»). El icono SHALL NOT mostrar el pulso, y la barra SHALL mantener su único objetivo táctil de al menos 48 dp y su ancho independiente del estado.

#### Scenario: Sin pulsómetro recordado
- **WHEN** no hay pulsómetro recordado
- **THEN** la barra muestra los enlaces de siempre y ningún corazón

#### Scenario: Pulsómetro conectado
- **WHEN** hay pulsómetro recordado y está conectado
- **THEN** la barra termina con el corazón normal y su descripción dice «Pulsómetro, conectado»

#### Scenario: Pulsómetro reconectando
- **WHEN** el pulsómetro recordado se ha desconectado y la app intenta recuperarlo
- **THEN** el corazón se ve atenuado con la marca que gira, y su descripción dice «Pulsómetro, reconectando»

#### Scenario: Pulsómetro sin conexión
- **WHEN** el pulsómetro recordado no está conectado ni se intenta, por ejemplo con el Bluetooth apagado
- **THEN** el corazón se ve tachado, y su descripción dice «Pulsómetro, sin conexión»

#### Scenario: Todos los enlaces a la vez
- **WHEN** el Neo, el Square, Zwift, el mando y el pulsómetro están en uso, a 360 dp en vertical
- **THEN** los cinco iconos caben en la barra, sin recortarse ni empujar al icono de Ajustes ni a la hora, el wifi y la batería

### Requirement: Sin pulso en los datos de Rodaje
Rodaje SHALL NOT mostrar los bpm ni en la rejilla de datos ni en ningún otro lugar, ni «Datos en pantalla» SHALL ofrecer el pulso. El pulso solo se ve en Diagnóstico.

#### Scenario: Rodando con la banda
- **WHEN** se rueda con el pulsómetro conectado dando 140 bpm
- **THEN** Rodaje no muestra 140 en ningún sitio, y el corazón de la barra no cambia por los latidos
