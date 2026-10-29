# Spec Delta

## ADDED Requirements

### Requirement: Información del mando completa por wifi
Con el modo «Todo por wifi (modo Zwift Ride)», la app SHALL contestar con datos, y no solo con el identificador, a las páginas de información que Zwift pide al mando al emparejar (números 0, 16, 1024 y 771). Lo que no sepa contestar SHALL contestarse con el identificador y anotarse en el registro de sesión, sin cambiar lo ya validado del mando.

#### Scenario: Zwift pide las páginas al emparejar
- **WHEN** el modo es «Todo por wifi» y Zwift empareja «Smart Bike» y saluda al mando
- **THEN** el registro de sesión muestra cada página pedida, con la respuesta enviada, y ninguna de las cuatro queda sin datos

#### Scenario: Botones tras las páginas nuevas
- **WHEN** Zwift ha pedido las páginas y se pulsa la cruceta ↓ y luego A en un menú de Zwift
- **THEN** Zwift baja una opción y la selecciona, como antes del cambio

#### Scenario: Freno y giro tras las páginas nuevas
- **WHEN** Zwift ha pedido las páginas, se rueda en Zwift, se mantiene una maneta de freno y después ✕
- **THEN** el avatar frena mientras se mantiene la maneta y se desplaza a la izquierda mientras se mantiene ✕, como antes del cambio

#### Scenario: Página que no se conoce
- **WHEN** Zwift pide una página que la app no sabe contestar
- **THEN** la app contesta con su identificador, lo anota en el registro con el número de la página y el mando sigue conectado

#### Scenario: Diez minutos sin pulsar nada
- **WHEN** el mando está conectado por wifi tras pedir las páginas y pasan diez minutos sin pulsar nada
- **THEN** Zwift sigue con el mando conectado y el siguiente botón llega

#### Scenario: El modo de siempre no cambia
- **WHEN** el modo es «Rodillo por wifi y mando por Bluetooth» y Zwift empareja el mando
- **THEN** el mando por Bluetooth funciona como antes y no se contestan páginas de información por wifi
