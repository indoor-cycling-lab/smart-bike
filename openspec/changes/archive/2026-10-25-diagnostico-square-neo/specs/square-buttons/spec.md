# Spec Delta

## Purpose

Reconocer cada uno de los 20 botones del Elite Square como un evento identificable de pulsar y soltar, también con varios botones a la vez y con pulsaciones rápidas.

## ADDED Requirements

### Requirement: Los 20 botones identificados
La app SHALL reconocer por separado cada botón del Square: las cuatro palancas de cambio (izquierda 1 y 2, derecha 1 y 2), los dos frenos, los dos Campagnolo, la cruceta (↑ ↓ ← →), A, B, Y, Z, △, ○, □ y ✕.

#### Scenario: Cada botón por separado
- **WHEN** se pulsa y se suelta cada botón del Square, de uno en uno
- **THEN** cada pulsación produce un evento de pulsar y otro de soltar con el botón correcto, y ningún otro botón cambia

### Requirement: Pulsar y soltar por cambio de estado
La app SHALL emitir un evento de pulsar cuando un botón pasa de suelto a pulsado y otro de soltar cuando pasa de pulsado a suelto, con la hora de llegada, y SHALL NOT emitir eventos si el estado de un botón no cambia aunque el Square vuelva a notificar.

#### Scenario: Notificación repetida
- **WHEN** el Square notifica dos veces seguidas el mismo estado con la palanca D1 pulsada
- **THEN** solo hay un evento de pulsar de la palanca D1

### Requirement: Varios botones a la vez
La app SHALL reconocer varios botones pulsados a la vez, cada uno con sus propios eventos, sea cual sea el orden en que se pulsan y se sueltan.

#### Scenario: Modificador y palanca
- **WHEN** se mantiene Campagnolo D, se pulsa y se suelta la palanca D1, y se suelta Campagnolo D
- **THEN** los eventos son pulsar Campag. D, pulsar palanca D1, soltar palanca D1 y soltar Campag. D, en ese orden, y mientras la palanca está pulsada los dos botones figuran como pulsados

### Requirement: Pulsaciones rápidas sin pérdidas
La app SHALL reconocer cada pulsación de una palanca aunque se pulse repetidamente lo más rápido posible; este comportamiento se SHALL validar con el Square real antes de darse por bueno.

#### Scenario: Diez pulsaciones rápidas
- **WHEN** se pulsa diez veces seguidas la palanca D1 lo más rápido posible
- **THEN** la app registra diez eventos de pulsar y diez de soltar de la palanca D1

#### Scenario: Pulsación completa entre dos notificaciones
- **WHEN** la palanca D1 se pulsa y se suelta entera entre dos notificaciones del Square
- **THEN** la app emite pulsar y soltar de la palanca D1, en ese orden, al llegar la segunda notificación

### Requirement: Botón pulsado al conectar
Si al conectar con el Square hay un botón que ya está pulsado, la app SHALL considerarlo pulsado desde la primera notificación y emitir su evento de pulsar, y SHALL NOT emitir eventos por los botones sueltos.

#### Scenario: Freno mantenido al conectar
- **WHEN** se mantiene el freno I mientras la app se conecta al Square
- **THEN** la app emite pulsar freno I con la primera notificación, y soltar freno I al soltarlo

### Requirement: Botones liberados al perder la conexión
Si se pierde la conexión con el Square con botones pulsados, la app SHALL emitir un evento de soltar por cada uno, para que ninguno quede pulsado.

#### Scenario: Conexión perdida con un freno pulsado
- **WHEN** se mantiene el freno I y se pierde la conexión con el Square
- **THEN** la app emite soltar freno I y la tecla del freno I deja de verse pulsada

### Requirement: Notificaciones mal formadas
La app SHALL ignorar una notificación del Square demasiado corta para contener el estado de los botones, sin cambiar el estado de ningún botón, y SHALL dejarla en el registro.

#### Scenario: Notificación corta
- **WHEN** llega una notificación de botones de menos de 11 bytes
- **THEN** no cambia ningún botón y la trama aparece en el tráfico
