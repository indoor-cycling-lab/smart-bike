# Spec Delta

## Purpose

Traducir la marcha puesta en resistencia del Tacx Neo cuando se rueda sin Zwift: qué pendiente se envía con cada marcha, cuándo se envía y en cuánto tiempo llega desde la palanca.

## ADDED Requirements

### Requirement: Pendiente según la marcha sin Zwift
Sin Zwift, la app SHALL simular en el Neo la pendiente base de 0 % más 1 % por cada marcha por encima de la 12 (menos 1 % por cada marcha por debajo), en modo SIM.

#### Scenario: Marcha de referencia
- **WHEN** la marcha es la 12 y el Neo está conectado
- **THEN** el Neo simula una pendiente de 0 %

#### Scenario: Marcha dura
- **WHEN** la marcha pasa a la 17
- **THEN** el Neo simula una pendiente de +5 % y la resistencia aumenta de forma perceptible

#### Scenario: Extremos
- **WHEN** la marcha es la 1 o la 24
- **THEN** el Neo simula −11 % o +12 %, respectivamente

### Requirement: Envío inmediato en cada cambio
La app SHALL enviar la pendiente nueva al Neo en cuanto cambia la marcha, sin esperar a ningún envío periódico, y SHALL NOT reenviarla si no cambia.

#### Scenario: Tope
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** no se envía nada nuevo al Neo

#### Scenario: Volver a la referencia
- **WHEN** la marcha es la 12 y se pulsa □
- **THEN** no se envía nada nuevo al Neo

### Requirement: Sin envíos acumulados con cambios rápidos
Con varios cambios de marcha seguidos más rápidos de lo que el Neo confirma las escrituras, la app SHALL enviar siempre la pendiente de la marcha más reciente y SHALL descartar las intermedias que aún no se hayan enviado.

#### Scenario: Cinco cambios rápidos
- **WHEN** se pulsa la palanca D1 cinco veces lo más rápido posible desde la 12
- **THEN** la última pendiente que recibe el Neo es +5 % y llega sin esperar a que se envíen las cinco intermedias

### Requirement: Reenvío al conectar el Neo
Al conectarse o volver a conectarse el Neo, la app SHALL enviarle la pendiente de la marcha puesta sin que el usuario haga nada.

#### Scenario: Neo reconectado
- **WHEN** la marcha es la 17, se pierde la conexión con el Neo y se vuelve a conectar desde Diagnóstico
- **THEN** el Neo vuelve a simular +5 % en cuanto está conectado

### Requirement: Latencia de la palanca
La app SHALL medir, en cada cambio de marcha con una palanca, el tiempo desde que llega la pulsación hasta que el comando se entrega al Neo, y SHALL mostrar el último valor en Diagnóstico. El objetivo es que la resistencia cambie en menos de 100 ms desde la pulsación (NF1); el cumplimiento SHALL validarse con el equipo real antes de darse por bueno.

#### Scenario: Medir un cambio
- **WHEN** se pulsa la palanca D1 con el Neo y el Square conectados
- **THEN** Diagnóstico muestra la latencia de esa palanca en milisegundos y el registro de sesión guarda la hora de llegada de la pulsación, del envío y de la entrega al Neo

#### Scenario: Sin cambios todavía
- **WHEN** aún no se ha cambiado de marcha con una palanca en la sesión
- **THEN** la latencia de la palanca se muestra como «—»
