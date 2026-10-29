# Spec Delta

## MODIFIED Requirements

### Requirement: Sin wifi
Sin red wifi, la app SHALL seguir rodando sin la plataforma y SHALL decir que la plataforma necesita la misma red wifi; al recuperar el wifi, SHALL volver a anunciarse sin que el usuario haga nada.

#### Scenario: Wifi apagado al abrir
- **WHEN** se abre la app con el wifi del móvil apagado
- **THEN** las marchas funcionan con la pendiente base, el estado de la plataforma dice «sin conexión» y Conexión explica que hay que conectar el móvil a la misma red wifi que la plataforma

#### Scenario: Wifi perdido rodando
- **WHEN** se rueda en Zwift y el móvil pierde el wifi durante 1 minuto
- **THEN** tras 30 s el Neo vuelve a la pendiente base con la marcha puesta y, al volver el wifi, Zwift se reconecta a la app sin tocar el móvil
