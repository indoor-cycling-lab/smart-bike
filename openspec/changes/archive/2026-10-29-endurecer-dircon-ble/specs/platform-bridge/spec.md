# Spec Delta

## MODIFIED Requirements

### Requirement: Plataforma desconectada
Si la plataforma se desconecta, la app SHALL mantener en el Neo lo último que pidió (terreno o potencia) durante 30 s y, si no vuelve, SHALL volver a la pendiente base con la marcha puesta; SHALL seguir anunciándose para que la plataforma se reconecte sola.

#### Scenario: Corte breve
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 12 y Zwift pierde la conexión durante 10 s
- **THEN** el Neo sigue simulando +5 % durante el corte y, al volver Zwift, la pendiente vuelve a seguir el terreno sin tocar el móvil

#### Scenario: Plataforma cerrada
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 14 y se cierra Zwift
- **THEN** a los 30 s el Neo pasa a simular +2 % (pendiente base 0 % y marcha 14) y las palancas siguen cambiando de marcha

#### Scenario: Plataforma cerrada en un entrenamiento
- **WHEN** Zwift pide 250 W en un entrenamiento y se cierra Zwift
- **THEN** el Neo mantiene 250 W durante 30 s y después pasa a la pendiente de la marcha puesta

#### Scenario: Plataforma que deja de responder
- **WHEN** se rueda en una subida de Zwift al 5 % con la marcha 14 por wifi y el PC de Zwift se suspende sin cerrar Zwift
- **THEN** la app da la plataforma por desconectada en menos de 30 s, el estado de la plataforma deja de decir «Conectado» y, 30 s después, el Neo pasa a simular +2 %

#### Scenario: Plataforma en los menús sin enviar nada
- **WHEN** Zwift está conectado por wifi y el usuario pasa 10 minutos en los menús sin rodar
- **THEN** la app no da la plataforma por desconectada mientras Zwift siga en marcha

#### Scenario: Zwift vuelve desde el mismo equipo
- **WHEN** Zwift está conectado desde un PC, pierde la wifi unos segundos y vuelve a conectarse desde el mismo PC antes de que la app detecte el corte
- **THEN** la app atiende a la conexión nueva y deja la anterior, sin esperar a que caduque

#### Scenario: Otro equipo de la plataforma
- **WHEN** Zwift está conectado desde un PC y le está enviando datos a la app, y otro equipo de la red se conecta a la app
- **THEN** la app rechaza la conexión nueva, sigue atendiendo a Zwift en el PC sin cortes y anota el rechazo con la dirección del otro equipo en el registro

#### Scenario: Otro equipo cuando la plataforma ya no responde
- **WHEN** Zwift estaba conectado desde un PC, lleva más de 15 s sin enviar nada y se conecta Zwift desde otro equipo de la red
- **THEN** la app atiende al otro equipo y deja de atender al PC

## ADDED Requirements

### Requirement: Plataforma solo por la red wifi
La app SHALL aceptar conexiones de la plataforma por wifi únicamente desde la red wifi a la que está conectado el móvil; SHALL NOT ser alcanzable por los datos móviles, una VPN ni el punto de acceso del móvil. Si cambia la red wifi o su dirección, SHALL volver a escuchar en la nueva sin que el usuario haga nada.

#### Scenario: Datos móviles activos
- **WHEN** la app está abierta con el wifi y los datos móviles activos
- **THEN** solo se puede conectar con la app desde la red wifi; desde internet, por los datos móviles, no se llega a ella

#### Scenario: Cambio de red wifi
- **WHEN** la app está esperando a la plataforma y el móvil pasa a otra red wifi
- **THEN** la app se anuncia y acepta conexiones en la red nueva, y deja de aceptarlas en la anterior

#### Scenario: Sin wifi
- **WHEN** el móvil no está conectado a ninguna red wifi
- **THEN** la app no acepta conexiones de la plataforma por ninguna otra red y Conexión explica que hace falta la misma red wifi que la plataforma

### Requirement: Cliente que inunda de mensajes
Si un cliente por wifi envía mensajes a un ritmo muy superior al de una plataforma real, la app SHALL desconectarlo y anotarlo en el registro, y SHALL seguir cambiando de marcha sin retrasos mientras tanto.

#### Scenario: Ráfaga de comandos
- **WHEN** un equipo de la red se conecta a la app y envía cientos de comandos por segundo mientras se rueda
- **THEN** la app lo desconecta, el registro dice por qué y las palancas siguen cambiando la resistencia del Neo con la misma latencia (NF1)

#### Scenario: Zwift en uso normal
- **WHEN** se rueda en Zwift por wifi durante una hora con cambios de pendiente, entrenamientos ERG y el controlador
- **THEN** la app nunca desconecta a Zwift por exceso de mensajes
