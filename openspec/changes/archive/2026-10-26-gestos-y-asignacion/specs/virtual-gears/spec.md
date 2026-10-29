## MODIFIED Requirements

### Requirement: Subir y bajar una marcha con las palancas
Con la asignación por defecto, al pulsar una palanca derecha (1 o 2) sin modificador, la app SHALL subir una marcha, y al pulsar una palanca izquierda (1 o 2), bajar una, en el momento de pulsar y sin esperar a soltarla. Al mantenerla, SHALL repetir el cambio mientras la autorrepetición esté activada.

#### Scenario: Palanca derecha
- **WHEN** la marcha es la 14 y se pulsa la palanca D1
- **THEN** la marcha pasa a la 15 antes de soltar la palanca

#### Scenario: Palanca izquierda pequeña
- **WHEN** la marcha es la 14 y se pulsa la palanca I2
- **THEN** la marcha pasa a la 13

#### Scenario: Pulsaciones rápidas
- **WHEN** la marcha es la 10 y se pulsa la palanca D1 cinco veces seguidas lo más rápido posible
- **THEN** la marcha termina en la 15

#### Scenario: Mantener la palanca
- **WHEN** la autorrepetición está activada y se mantiene pulsada la palanca D1
- **THEN** sube una marcha al pulsar y, pasado el retardo de repetición, sigue subiendo a su ritmo hasta soltarla

#### Scenario: Mantener hasta el tope
- **WHEN** la marcha es la 22 y se mantiene la palanca D1 hasta que la repetición llega a la 24
- **THEN** la marcha se queda en la 24, se avisa del tope una sola vez y la repetición no vuelve a avisar mientras se mantiene

#### Scenario: Pulsaciones rápidas sin repetición
- **WHEN** se pulsa la palanca D1 cinco veces seguidas, cada pulsación más corta que el retardo de repetición
- **THEN** la marcha sube cinco, sin ninguna repetición

### Requirement: Saltos y extremos con el Campagnolo como modificador
Con la asignación por defecto y un Campagnolo mantenido antes de pulsar una palanca de su lado, la app SHALL hacer el salto de esa combinación, sin cambiar además una marcha suelta: Campag. D + palanca D1 sube 3, Campag. D + palanca D2 va a la más dura, Campag. I + palanca I1 baja 3 y Campag. I + palanca I2 va a la más ligera. Con los dos Campagnolo mantenidos, las palancas SHALL NOT cambiar la marcha.

#### Scenario: Subir tres
- **WHEN** la marcha es la 14, se mantiene Campag. D y se pulsa la palanca D1
- **THEN** la marcha pasa a la 17

#### Scenario: A la más ligera
- **WHEN** la marcha es la 14, se mantiene Campag. I y se pulsa la palanca I2
- **THEN** la marcha pasa a la 1

#### Scenario: Modificador del otro lado
- **WHEN** la marcha es la 14, se mantiene Campag. I y se pulsa la palanca D1
- **THEN** la marcha pasa a la 15, como sin modificador

#### Scenario: Modificador pulsado después
- **WHEN** se pulsa la palanca D1 y, con ella pulsada, se pulsa Campag. D
- **THEN** solo sube una marcha

#### Scenario: Los dos Campagnolo
- **WHEN** la marcha es la 14, se mantienen Campag. I y Campag. D y se pulsa la palanca D1
- **THEN** la marcha sigue en la 14

### Requirement: Volver a la marcha de referencia
Con la asignación por defecto, al pulsar y soltar □ antes del umbral de pulsación larga, la app SHALL poner la marcha de referencia.

#### Scenario: Desde una marcha dura
- **WHEN** la marcha es la 20, la referencia la 12 y se pulsa y se suelta □
- **THEN** la marcha pasa a la 12 al soltar □

#### Scenario: Ya en la referencia
- **WHEN** la marcha es la 12, la referencia la 12 y se pulsa y se suelta □
- **THEN** la marcha no cambia y no se avisa de un tope

## ADDED Requirements

### Requirement: Fijar la marcha de referencia
Con la asignación por defecto, al mantener □ hasta el umbral de pulsación larga, la app SHALL fijar la marcha puesta como marcha de referencia, sin cambiar la marcha, y SHALL guardarla para las sesiones siguientes.

#### Scenario: Fijar la referencia
- **WHEN** la marcha es la 15 y se mantiene □
- **THEN** la marca de referencia pasa a la 15 y, al pulsar □ corta desde la 20, la marcha pasa a la 15

#### Scenario: Referencia guardada
- **WHEN** se fija la referencia en la 15 y se cierra y se vuelve a abrir la app
- **THEN** la marca de referencia sigue en la 15

### Requirement: Apagar el Square sin cambiar la marcha
Al apagar el Square con su combinación (los dos Campagnolo con las palancas I1 y D1, pulsados a la vez en cualquier orden), la app SHALL dejar la marcha como estaba antes de empezar a pulsar la combinación. Este comportamiento SHALL validarse con el Square real antes de darse por bueno.

#### Scenario: Palancas antes que los Campagnolo
- **WHEN** la marcha es la 14 y se apaga el Square pulsando primero las palancas I1 y D1 y después los dos Campagnolo
- **THEN** la marcha sigue en la 14 al perder la conexión con el Square, y el Neo recibe la resistencia de la 14

#### Scenario: Campagnolo antes que las palancas
- **WHEN** la marcha es la 14 y se apaga el Square pulsando primero Campag. I y después Campag. D y las palancas
- **THEN** la marcha sigue en la 14

## REMOVED Requirements

### Requirement: Solo cambian la marcha las palancas, los Campagnolo y □
**Reason**: La asignación configurable sustituye a la tabla fija: qué botones cambian la marcha depende de la asignación vigente.
**Migration**: Ver la spec `button-mapping` («Acciones asignables», «Asignación por defecto»). Con la asignación por defecto, el resto de botones sigue sin cambiar la marcha.
