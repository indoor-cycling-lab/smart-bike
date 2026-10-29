# Spec Delta

## ADDED Requirements

### Requirement: Buscar el ventilador aparte
Dispositivos SHALL mostrar una fila «Ventilador» con su estado (buscando, conectando, reconectando, conectado, no encontrado o sin conexión), con palabra y forma además de color, y un botón «Buscar ventilador» que busca un dispositivo cuyo nombre empiece por «HEADWIND» y se conecta al primero que encuentra, sin que el usuario lo elija de una lista. «Buscar dispositivos» SHALL NOT buscar el ventilador, salvo que ya haya uno recordado.

#### Scenario: Buscar el ventilador
- **WHEN** el ventilador está encendido y el usuario toca «Buscar ventilador»
- **THEN** la fila pasa por «Buscando» y «Conectando» hasta «Conectado», sin tocar la conexión del Neo, del Square ni de Zwift

#### Scenario: Buscar sin ventilador
- **WHEN** el usuario toca «Buscar dispositivos» sin ningún ventilador recordado
- **THEN** la app busca solo el Neo y el Square, y no aparece ningún aviso ni fila de error del ventilador

#### Scenario: Ventilador no encontrado
- **WHEN** el usuario toca «Buscar ventilador» y no aparece ninguno en 30 s
- **THEN** la fila dice «No encontrado», aparece el aviso de error «No se encuentra el ventilador. Enchúfalo, ciérralo en la app de Wahoo y vuelve a buscar.» y «Buscar ventilador» vuelve a estar disponible

#### Scenario: Ventilador ocupado por otra app
- **WHEN** el ventilador está conectado a otra app y no admite otra conexión
- **THEN** la fila dice «No encontrado» o «Sin conexión» como en cualquier otro fallo, y el aviso dice que puede estar conectado a otra app

#### Scenario: Más de un ventilador a la vista
- **WHEN** el usuario toca «Buscar ventilador» y hay dos ventiladores encendidos
- **THEN** la app se conecta al primero que encuentra y lo recuerda

#### Scenario: Sin permiso o con Bluetooth apagado
- **WHEN** falta el permiso de Bluetooth o el Bluetooth está apagado y el usuario toca «Buscar ventilador»
- **THEN** la app muestra los mismos avisos y acciones que con «Buscar dispositivos», y no busca nada

### Requirement: Recordar y reconectar el ventilador
Al conectar con un ventilador, la app SHALL recordarlo para las próximas sesiones, uno solo, conectarse a él sola al abrir la app y recuperar la conexión sola si se pierde. Si una búsqueda manual conecta con otro, SHALL recordar el nuevo en su lugar.

#### Scenario: Primera conexión
- **WHEN** en el primer uso se conecta el ventilador con «Buscar ventilador» y se termina la sesión
- **THEN** la próxima vez que se abre la app, se conecta a ese ventilador sin buscarlo

#### Scenario: Ventilador apagado al abrir
- **WHEN** hay un ventilador recordado y está apagado al abrir la app
- **THEN** la fila dice «Reconectando», sin aviso de error, y se conecta sola cuando se encienda

#### Scenario: Otro ventilador
- **WHEN** hay un ventilador recordado y el usuario toca «Buscar ventilador» y conecta con otro
- **THEN** la app recuerda el nuevo y deja de intentar con el anterior

#### Scenario: Terminar la sesión
- **WHEN** el usuario termina la sesión
- **THEN** la app se desconecta del ventilador, que queda libre para otra app, con el modo y la velocidad que tenía

### Requirement: Olvidar el ventilador
La fila «Ventilador» con uno recordado SHALL tener «Olvidar». Olvidarlo SHALL desconectarlo, quitar su sección de Ajustes, su icono de Rodaje y su fila de Diagnóstico, y hacer que las acciones del ventilador avisen «Sin ventilador». Sin ventilador recordado, la fila SHALL NOT tener «Olvidar». Olvidarlo SHALL NOT cambiar el modo ni la velocidad del ventilador.

#### Scenario: Olvidar el ventilador
- **WHEN** el usuario toca «Olvidar» en la fila del ventilador
- **THEN** se desconecta, la fila vuelve a «Sin conexión» sin «Olvidar», y desaparecen la sección de Ajustes, el icono de Rodaje y la fila de Diagnóstico

#### Scenario: El ventilador sigue como estaba
- **WHEN** se olvida el ventilador mientras está en manual al 40 %
- **THEN** el ventilador sigue en manual al 40 %

#### Scenario: Olvidar y volver a buscar
- **WHEN** se olvida el ventilador y se toca «Buscar ventilador» con él encendido
- **THEN** la app lo encuentra, lo conecta y lo vuelve a recordar
