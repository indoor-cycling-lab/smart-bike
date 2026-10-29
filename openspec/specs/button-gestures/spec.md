# button-gestures Specification

## Purpose
Reconocer gestos sobre los botones del Square (pulsar, corta, larga, doble, repetir y combinaciones con los Campagnolo como modificadores), con tiempos configurables y sin hacer esperar a los botones que no lo necesitan.

## Requirements

### Requirement: Gestos reconocidos
La app SHALL reconocer sobre cada botón o combinación cinco gestos: «pulsar» (al pulsar), «corta» (pulsar y soltar antes del umbral de pulsación larga), «larga» (mantener hasta el umbral), «doble» (dos pulsaciones dentro de la ventana de doble pulsación) y «repetir» (mantener un botón con una acción repetible asignada a «pulsar»).

#### Scenario: Pulsación larga
- **WHEN** □ tiene asignada una acción a «larga», el umbral es 500 ms y se mantiene □ 600 ms
- **THEN** la acción de □ larga se ejecuta a los 500 ms, sin esperar a soltar, y la de □ corta no se ejecuta

#### Scenario: Pulsación corta
- **WHEN** □ tiene asignadas acciones a «corta» y a «larga» y se pulsa y se suelta □ en 200 ms
- **THEN** la acción de □ corta se ejecuta al soltar y la de □ larga no

#### Scenario: Doble pulsación
- **WHEN** △ tiene asignadas acciones a «corta» y a «doble», la ventana es 300 ms y se pulsa △ dos veces con 150 ms entre la primera suelta y la segunda pulsación
- **THEN** la acción de △ doble se ejecuta al pulsar por segunda vez y la de △ corta no se ejecuta

#### Scenario: Corta con doble asignada
- **WHEN** △ tiene asignadas acciones a «corta» y a «doble» y se pulsa y se suelta △ una sola vez
- **THEN** la acción de △ corta se ejecuta 300 ms después de soltar

### Requirement: Sin espera en lo que no es ambiguo
Un botón o combinación SHALL ejecutar su acción sin esperar cuando no tiene gestos ambiguos asignados: «pulsar» se ejecuta al pulsar, y «corta» sin «doble» al soltar. Solo «corta» con «doble» asignada SHALL esperar a que pase la ventana de doble pulsación.

#### Scenario: Palanca con la asignación por defecto
- **WHEN** se pulsa la palanca D1
- **THEN** la marcha sube al llegar la pulsación, sin esperar a soltarla ni a ningún umbral

#### Scenario: Solo corta asignada
- **WHEN** □ solo tiene asignada una acción a «corta» y se mantiene □ 2 s
- **THEN** la acción se ejecuta al soltar □, sin esperar ninguna ventana

### Requirement: Pulsar excluye corta, larga y doble
Un mismo botón o combinación SHALL tener asignado o bien «pulsar», o bien cualquiera de «corta», «larga» y «doble», pero no los dos tipos a la vez.

#### Scenario: Asignar pulsar sobre un botón con larga
- **WHEN** □ tiene acciones en «corta» y «larga» y se asigna otra acción a □ «pulsar»
- **THEN** al guardar, □ corta y □ larga quedan sin acción, y la pantalla lo avisa antes de guardar

### Requirement: Autorrepetición al mantener
Al mantener un botón o combinación con una acción repetible asignada a «pulsar» (subir o bajar marchas), la app SHALL repetir la acción tras el retardo de repetición y después a su ritmo, hasta soltarlo, si la autorrepetición está activada. Ir a un extremo o a la referencia SHALL NOT repetirse.

#### Scenario: Mantener la palanca
- **WHEN** la autorrepetición está activada con 500 ms de retardo y 250 ms de ritmo, la marcha es la 10 y se mantiene la palanca D1 1100 ms
- **THEN** la marcha sube a la 11 al pulsar, a la 12 a los 500 ms, a la 13 a los 750 ms y a la 14 a los 1000 ms, y no sube más al soltar

#### Scenario: Autorrepetición desactivada
- **WHEN** la autorrepetición está desactivada y se mantiene la palanca D1 2 s
- **THEN** la marcha sube una sola vez

#### Scenario: A un extremo
- **WHEN** se mantiene Campag. D y se mantiene la palanca D2 2 s
- **THEN** la marcha va a la más dura una sola vez

#### Scenario: Conexión perdida con la palanca mantenida
- **WHEN** se está repitiendo con la palanca D1 mantenida y se pierde la conexión con el Square
- **THEN** la repetición se detiene en ese momento

### Requirement: Combinaciones con los Campagnolo
Los Campagnolo SHALL hacer de modificadores: un botón pulsado mientras se mantiene un solo Campagnolo SHALL reconocerse como la combinación de los dos, distinta del botón solo. Un Campagnolo pulsado después del botón SHALL NOT formar combinación. Los Campagnolo solos SHALL admitir «corta», «larga» y «doble», pero no «pulsar», y solo si mientras se mantenían no se pulsó otro botón.

#### Scenario: Combinación con modificador
- **WHEN** se mantiene Campag. I y se pulsa △
- **THEN** se reconoce «Campag. I + △ · pulsar» y no «△ · pulsar»

#### Scenario: Campagnolo usado como modificador
- **WHEN** Campag. I tiene una acción en «corta», se mantiene Campag. I, se pulsa la palanca I1 y se suelta todo
- **THEN** se ejecuta la acción de la combinación y no la de Campag. I corta

#### Scenario: Los dos Campagnolo
- **WHEN** se mantienen Campag. I y Campag. D y se pulsa cualquier otro botón
- **THEN** no se reconoce ninguna combinación y la marcha no cambia

### Requirement: Tiempos configurables
La app SHALL permitir ajustar el umbral de pulsación larga (300–1500 ms, por defecto 800 ms), la ventana de doble pulsación (150–600 ms, por defecto 300 ms), el retardo de repetición (300–1000 ms, por defecto 800 ms) y el ritmo de repetición (100–1000 ms, por defecto 250 ms), en pasos de 50 ms, y activar o desactivar la autorrepetición (activada por defecto).

#### Scenario: Cambiar el umbral de pulsación larga
- **WHEN** se pone el umbral de pulsación larga en 1000 ms y se mantiene □ 900 ms
- **THEN** se reconoce «□ · corta», no «□ · larga»

#### Scenario: Límite de un tiempo
- **WHEN** la ventana de doble pulsación está en 600 ms
- **THEN** el botón + de la ventana está deshabilitado y el valor no sube

#### Scenario: Pulsación normal sin repetición
- **WHEN** con los tiempos por defecto se pulsa la palanca D1 y el Square avisa de la suelta 650 ms después de la pulsación
- **THEN** la marcha sube una sola vez

#### Scenario: Tiempos guardados
- **WHEN** se cambia el ritmo de repetición a 400 ms y se cierra y se vuelve a abrir la app
- **THEN** el ritmo de repetición sigue en 400 ms

### Requirement: Vibración por gesto
La app SHALL acompañar cada acción ejecutada de una vibración según el gesto: una corta para «pulsar», «corta» y «repetir», una larga para «larga» y tres cortas para «doble»; un tope SHALL vibrar con su vibración doble en lugar de la del gesto. Un gesto sin acción SHALL NOT vibrar, ni uno con una función de Zwift, que ya se nota en Zwift. También con la pantalla apagada.

#### Scenario: Larga con la pantalla apagada
- **WHEN** la pantalla está apagada, □ larga tiene asignada «Fijar la marcha de referencia» y se mantiene □
- **THEN** el móvil vibra una vez de forma larga al cumplirse el umbral

#### Scenario: Botón sin acción
- **WHEN** se pulsa △ brevemente, que no tiene acción con la asignación por defecto
- **THEN** el móvil no vibra

#### Scenario: Función de Zwift
- **WHEN** se pulsa Y, que usa el powerup en Zwift con la asignación por defecto
- **THEN** el móvil no vibra

### Requirement: Vibración desactivable
Con «Vibrar» desactivado en Ajustes, el móvil SHALL NOT vibrar por ningún motivo: ni por el gesto, ni al cambiar de marcha, ni en los topes de marcha o de potencia. El resto (la acción, los topes en pantalla, el envío al Neo) SHALL seguir igual. Activado (por defecto), las vibraciones SHALL ser las de «Vibración por gesto». El cambio SHALL aplicarse al momento, también con la pantalla apagada.

#### Scenario: Desactivada
- **WHEN** «Vibrar» está desactivado y se mantiene □ hasta el umbral de pulsación larga
- **THEN** la referencia se fija y el móvil no vibra

#### Scenario: Tope sin vibración
- **WHEN** «Vibrar» está desactivado, la marcha es la 24 y se pulsa la palanca D1
- **THEN** la pantalla muestra «Tope · marcha más dura» y el móvil no vibra

#### Scenario: Tope de potencia sin vibración
- **WHEN** «Vibrar» está desactivado, se está en ERG manual a 50 W y se pulsa la palanca I1
- **THEN** la potencia sigue en 50 W, el pie dice «Tope · 50 W» y el móvil no vibra

#### Scenario: Volver a activarla
- **WHEN** se vuelve a activar «Vibrar» y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil vibra una vez
