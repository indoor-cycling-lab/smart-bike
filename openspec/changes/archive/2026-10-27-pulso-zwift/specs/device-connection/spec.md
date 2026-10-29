# Spec Delta

## ADDED Requirements

### Requirement: Buscar el pulsómetro aparte
Dispositivos SHALL mostrar una fila «Pulsómetro» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar pulsómetro» que busca un dispositivo que ofrezca el servicio de frecuencia cardiaca y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. «Buscar dispositivos» SHALL NOT buscar el pulsómetro, salvo que ya haya uno recordado.

#### Scenario: Buscar la banda
- **WHEN** la banda está puesta y encendida, y el usuario toca «Buscar pulsómetro»
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de Zwift

#### Scenario: Buscar sin banda
- **WHEN** el usuario toca «Buscar dispositivos» sin ningún pulsómetro recordado
- **THEN** la app busca solo el Neo y el Square, y no aparece ningún aviso ni fila de error de pulso

#### Scenario: Banda no encontrada
- **WHEN** el usuario toca «Buscar pulsómetro» y no aparece ninguno en 30 s
- **THEN** la fila dice «No encontrado», aparece el aviso de error «No se encuentra el pulsómetro. Póntelo, comprueba que tiene batería y vuelve a buscar.» y «Buscar pulsómetro» vuelve a estar disponible

#### Scenario: Más de una banda a la vista
- **WHEN** el usuario toca «Buscar pulsómetro» y hay dos bandas encendidas
- **THEN** la app se conecta a la primera que encuentra y la recuerda

#### Scenario: Banda con el límite de conexiones agotado
- **WHEN** la banda ya está conectada a otros dispositivos y no admite otra conexión
- **THEN** la fila dice «No encontrado» o «Sin conexión» como en cualquier otro fallo, y el aviso dice que puede estar conectada a otro dispositivo

#### Scenario: Sin permiso o con Bluetooth apagado
- **WHEN** falta el permiso de Bluetooth o el Bluetooth está apagado y el usuario toca «Buscar pulsómetro»
- **THEN** la app muestra los mismos avisos y acciones que con «Buscar dispositivos», y no busca nada

### Requirement: Recordar y reconectar el pulsómetro
Al conectar con un pulsómetro, la app SHALL recordarlo para las próximas sesiones, uno solo, y SHALL conectarse a él sola al abrir la app, sin que el usuario busque, y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL recordar el nuevo en su lugar. Olvidar el pulsómetro en Dispositivos SHALL desconectarlo, quitar su icono de Rodaje y su fila de Diagnóstico y dejar de enviar pulso a Zwift.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta la banda con «Buscar pulsómetro» y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a esa banda sin buscarla

#### Scenario: Banda apagada al abrir
- **WHEN** hay una banda recordada y está apagada al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otra banda
- **WHEN** hay una banda recordada y el usuario toca «Buscar pulsómetro» y conecta con otra
- **THEN** la app recuerda la nueva y deja de intentar con la anterior

#### Scenario: Olvidar la banda
- **WHEN** el usuario olvida el pulsómetro en Dispositivos
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin recordado, el icono de Rodaje desaparece, y Zwift deja de recibir pulso

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del pulsómetro, que queda libre para otro dispositivo
