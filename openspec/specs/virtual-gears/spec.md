# virtual-gears Specification

## Purpose
Marcha virtual que el ciclista cambia con los botones del Square, como en un grupo inalámbrico: subir y bajar, saltos, extremos y marcha de referencia, con topes avisados y la marcha configurada al reconectar y entre sesiones.

## Requirements

### Requirement: Transmisión lineal por defecto
La app SHALL ofrecer una transmisión lineal de 24 marchas numeradas del 1 (la más ligera) al 24 (la más dura), con la 12 como marcha de referencia por defecto. La lineal SHALL ser la transmisión elegida mientras el usuario no elija otra en Marchas.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez
- **THEN** la transmisión es la lineal, la marcha puesta es la 12 de 24 y la marca de referencia está en la 12

### Requirement: Subir y bajar una marcha con las palancas
Con la transmisión lineal y su asignación por defecto, al pulsar una palanca derecha (1 o 2) sin modificador, la app SHALL subir una marcha, y al pulsar una palanca izquierda (1 o 2), bajar una, en el momento de pulsar y sin esperar a soltarla. Al mantenerla, SHALL repetir el cambio mientras la autorrepetición esté activada.

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
Con la transmisión lineal y su asignación por defecto, y un Campagnolo mantenido antes de pulsar una palanca de su lado, la app SHALL hacer el salto de esa combinación, sin cambiar además una marcha suelta: Campag. D + palanca D1 sube 3, Campag. D + palanca D2 va a la más dura, Campag. I + palanca I1 baja 3 y Campag. I + palanca I2 va a la más ligera. Con los dos Campagnolo mantenidos, las palancas SHALL NOT cambiar la marcha.

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
Con la asignación por defecto de cualquier transmisión, al pulsar y soltar □ antes del umbral de pulsación larga, la app SHALL poner la marcha de referencia de la transmisión elegida; con la Shimano, su plato y su piñón a la vez.

#### Scenario: Desde una marcha dura
- **WHEN** la marcha es la 20, la referencia la 12 y se pulsa y se suelta □
- **THEN** la marcha pasa a la 12 al soltar □

#### Scenario: Ya en la referencia
- **WHEN** la marcha es la 12, la referencia la 12 y se pulsa y se suelta □
- **THEN** la marcha no cambia y no se avisa de un tope

#### Scenario: Referencia con la Shimano
- **WHEN** la marcha es la 52×13, la referencia la 36×17 y se pulsa y se suelta □
- **THEN** la marcha pasa a la 36×17

### Requirement: Topes
Con la transmisión lineal, al pedir una marcha más allá de la 1 o de la 24, la app SHALL quedarse en el extremo y avisar del tope en pantalla y con una vibración doble; un salto que lo sobrepasa SHALL avanzar hasta el extremo, y solo SHALL avisar del tope si la marcha ya estaba en él.

#### Scenario: Subir en la más dura
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** la marcha sigue en la 24, la pantalla muestra «Tope · marcha más dura» y el móvil vibra dos veces

#### Scenario: Bajar en la más ligera
- **WHEN** la marcha es la 1 y se pulsa la palanca I1
- **THEN** la marcha sigue en la 1 y la pantalla muestra «Tope · marcha más ligera»

#### Scenario: Salto que llega al extremo
- **WHEN** la marcha es la 23 y se pide subir 3
- **THEN** la marcha pasa a la 24, sin aviso de tope

### Requirement: Vibración en cada cambio
Con «Vibrar» activado (por defecto), la app SHALL acompañar cada cambio de marcha de una vibración corta, distinta de la doble del tope, también con la pantalla apagada. Con «Vibrar» desactivado, SHALL NOT vibrar (spec `button-gestures`, «Vibración desactivable»).

#### Scenario: Cambio con la pantalla apagada
- **WHEN** la pantalla del móvil está apagada y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil vibra una vez

#### Scenario: Vibración desactivada
- **WHEN** «Vibrar» está desactivado y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil no vibra

### Requirement: Cambiar de marcha sin el Neo
La app SHALL permitir cambiar de marcha con el Square aunque el Neo no esté conectado, y SHALL aplicar la marcha vigente en cuanto se conecte.

#### Scenario: Neo desconectado
- **WHEN** el Neo no está conectado, el Square sí, y se pulsa la palanca D1 dos veces desde la 14
- **THEN** la pantalla muestra la marcha 16 y, al conectar el Neo, este recibe la resistencia de la 16

### Requirement: Marcha conservada al perder la conexión
Si se pierde la conexión con el Neo o con el Square, la app SHALL conservar la marcha puesta y seguir con ella al recuperar la conexión.

#### Scenario: Square que se reconecta
- **WHEN** la marcha es la 17, se pierde la conexión con el Square y se vuelve a conectar desde Diagnóstico
- **THEN** la marcha sigue siendo la 17

### Requirement: Recordar la última marcha entre sesiones
La app SHALL guardar la marcha puesta de cada transmisión y, al empezar una sesión nueva con «Recordar la última marcha» activado (por defecto), SHALL empezar en la última marcha de la transmisión elegida; si no hay ninguna guardada, la guardada no es válida o el ajuste está desactivado, SHALL empezar en la marcha de referencia de esa transmisión.

#### Scenario: Volver al día siguiente
- **WHEN** se termina la sesión en la marcha 17 y se vuelve a abrir la app
- **THEN** la marcha puesta es la 17

#### Scenario: Cierre inesperado
- **WHEN** la marcha pasa a la 9 y unos segundos después Android cierra la app
- **THEN** al volver a abrirla, la marcha es la 9

#### Scenario: Con la Shimano
- **WHEN** se termina la sesión con la Shimano en la 52×15 y se vuelve a abrir la app
- **THEN** la transmisión es la Shimano y la marcha la 52×15

#### Scenario: Sin recordar
- **WHEN** «Recordar la última marcha» está desactivado, la referencia es la 15 y se termina la sesión en la 20
- **THEN** al volver a abrir la app, la marcha es la 15

### Requirement: Fijar la marcha de referencia
Con la asignación por defecto de cualquier transmisión, al mantener □ hasta el umbral de pulsación larga, la app SHALL fijar la marcha puesta como marcha de referencia de la transmisión elegida, sin cambiar la marcha, y SHALL guardarla para las sesiones siguientes. La referencia de las demás transmisiones SHALL NOT cambiar.

#### Scenario: Fijar la referencia
- **WHEN** la marcha es la 15 y se mantiene □
- **THEN** la marca de referencia pasa a la 15 y, al pulsar □ corta desde la 20, la marcha pasa a la 15

#### Scenario: Referencia guardada
- **WHEN** se fija la referencia en la 15 y se cierra y se vuelve a abrir la app
- **THEN** la marca de referencia sigue en la 15

#### Scenario: Referencia de la Shimano
- **WHEN** con la Shimano en la 52×19 se mantiene □ y después se elige la lineal
- **THEN** la referencia de la Shimano es la 52×19 y la de la lineal sigue siendo la que era

### Requirement: Apagar el Square sin cambiar la marcha
Al apagar el Square con su combinación (los dos Campagnolo con las palancas I1 y D1, pulsados a la vez en cualquier orden), la app SHALL dejar la marcha como estaba antes de empezar a pulsar la combinación. Este comportamiento SHALL validarse con el Square real antes de darse por bueno.

#### Scenario: Palancas antes que los Campagnolo
- **WHEN** la marcha es la 14 y se apaga el Square pulsando primero las palancas I1 y D1 y después los dos Campagnolo
- **THEN** la marcha sigue en la 14 al perder la conexión con el Square, y el Neo recibe la resistencia de la 14

#### Scenario: Campagnolo antes que las palancas
- **WHEN** la marcha es la 14 y se apaga el Square pulsando primero Campag. I y después Campag. D y las palancas
- **THEN** la marcha sigue en la 14

### Requirement: Transmisión Shimano 105
La app SHALL ofrecer, además de la lineal, la transmisión «Shimano 105 2×12» con platos 52 y 36 y cassette 11-12-13-14-15-17-19-21-24-28-32-36. Con ella, cada marcha SHALL ser un plato y un piñón, con la 36×36 como la más ligera y la 52×11 como la más dura, y su marcha de referencia por defecto SHALL ser la 36×17.

#### Scenario: Elegir la Shimano por primera vez
- **WHEN** la transmisión es la lineal y se elige «Shimano 105 2×12» en Marchas
- **THEN** la marcha puesta pasa a ser la 36×17 y la referencia es la 36×17

#### Scenario: Extremos
- **WHEN** con la Shimano se va a la marcha más ligera y después a la más dura
- **THEN** la marcha es primero la 36×36 y después la 52×11

### Requirement: Modo automático con la Shimano
Con la Shimano en modo automático, que es su modo por defecto, «+1 marcha» y «−1 marcha» SHALL recorrer los piñones y cambiar de plato solos. Al endurecer en el plato 36 desde el piñón 15 o uno más pequeño, SHALL pasar al plato 52 dos piñones más grande. Al aligerar en el 52 desde el 24 o uno más grande, SHALL pasar al 36 dos piñones más pequeño. Así, desde un extremo hasta el otro hay 15 marchas, en un sentido y en el otro.

#### Scenario: Subir de plato
- **WHEN** la marcha es la 36×15 en automático y se pide «+1 marcha»
- **THEN** la marcha pasa a la 52×19

#### Scenario: Antes del punto de subida
- **WHEN** la marcha es la 36×17 en automático y se pide «+1 marcha»
- **THEN** la marcha pasa a la 36×15

#### Scenario: Bajar de plato
- **WHEN** la marcha es la 52×24 en automático y se pide «−1 marcha»
- **THEN** la marcha pasa a la 36×19

#### Scenario: Histéresis
- **WHEN** la marcha es la 52×19 en automático y se pide «−1 marcha» dos veces
- **THEN** la marcha pasa a la 52×21 y después a la 52×24, sin cambiar de plato

#### Scenario: Recorrido completo
- **WHEN** desde la 36×36 en automático se pide «+1 marcha» hasta llegar a la 52×11
- **THEN** la marcha pasa por 36×32, 36×28, 36×24, 36×21, 36×19, 36×17, 36×15, 52×19, 52×17, 52×15, 52×14, 52×13, 52×12 y 52×11, sin ninguna combinación cruzada

#### Scenario: Desde una marcha cruzada
- **WHEN** la marcha es la 52×28 en automático y se pide «−1 marcha»
- **THEN** la marcha pasa a la 36×21

#### Scenario: Validación con el equipo real
- **WHEN** el usuario rueda en automático subiendo y bajando todo el recorrido
- **THEN** los puntos de cambio de plato y la compensación de dos piñones se dan por buenos solo si el usuario los aprueba; si no, se ajustan antes de archivar

### Requirement: Plato a mano en modo automático
Con la Shimano en automático, «Plato más grande» y «Plato más pequeño» SHALL cambiar de plato y compensar dos piñones para que la marcha no salte tanto: al plato grande, dos piñones más grande; al pequeño, dos piñones más pequeño, sin pasar del primer o del último piñón.

#### Scenario: Al plato grande
- **WHEN** la marcha es la 36×17 en automático y se pide «Plato más grande»
- **THEN** la marcha pasa a la 52×21

#### Scenario: Al plato pequeño
- **WHEN** la marcha es la 52×17 en automático y se pide «Plato más pequeño»
- **THEN** la marcha pasa a la 36×14

#### Scenario: Compensación recortada
- **WHEN** la marcha es la 36×32 en automático y se pide «Plato más grande»
- **THEN** la marcha pasa a la 52×36

### Requirement: Modo manual con la Shimano
Con la Shimano en modo manual, «+1 marcha» y «−1 marcha» SHALL cambiar solo el piñón, uno más pequeño o uno más grande, y «Plato más grande» y «Plato más pequeño» SHALL cambiar solo el plato, sin tocar el piñón. Se SHALL poder llegar a cualquiera de las 24 combinaciones.

#### Scenario: Piñón sin cambiar de plato
- **WHEN** la marcha es la 36×15 en manual y se pide «+1 marcha»
- **THEN** la marcha pasa a la 36×14

#### Scenario: Plato sin compensar
- **WHEN** la marcha es la 36×17 en manual y se pide «Plato más grande»
- **THEN** la marcha pasa a la 52×17

#### Scenario: Combinación cruzada
- **WHEN** la marcha es la 52×28 en manual y se pide «−1 marcha»
- **THEN** la marcha pasa a la 52×32

### Requirement: Palancas con la Shimano
Con la Shimano y su asignación por defecto, como los mandos Shimano, la palanca D1 SHALL pedir «−1 marcha», la D2 «+1 marcha», la I1 «Plato más grande» y la I2 «Plato más pequeño», al pulsar y sin esperar a soltar. Las de marcha SHALL repetirse al mantenerlas, si la autorrepetición está activada; las de plato, no. La correspondencia con los mandos del usuario SHALL validarse en su bici antes de darse por buena.

#### Scenario: Palanca D2
- **WHEN** la marcha es la 52×17 y se pulsa la palanca D2
- **THEN** la marcha pasa a la 52×15 antes de soltar la palanca

#### Scenario: Palanca D1
- **WHEN** la marcha es la 52×17 y se pulsa la palanca D1
- **THEN** la marcha pasa a la 52×19

#### Scenario: Palanca I1 en automático
- **WHEN** la marcha es la 36×17 en automático y se pulsa la palanca I1
- **THEN** la marcha pasa a la 52×21

#### Scenario: Mantener la palanca de plato
- **WHEN** la marcha es la 36×17 en manual y se mantiene la palanca I1
- **THEN** la marcha pasa a la 52×17 una sola vez, sin repetir

### Requirement: Saltos y extremos con la Shimano
Con la Shimano y su asignación por defecto, Campag. D + palanca D1 SHALL pedir «−3 marchas», Campag. D + palanca D2 «+3 marchas», Campag. I + palanca I1 «Marcha más dura» y Campag. I + palanca I2 «Marcha más ligera». En manual, ±3 SHALL ser tres piñones en el plato puesto; en automático, tres pasos seguidos de su secuencia. La más dura SHALL ser la 52×11 y la más ligera la 36×36 en los dos modos.

#### Scenario: Tres piñones en manual
- **WHEN** la marcha es la 36×21 en manual, se mantiene Campag. D y se pulsa la palanca D2
- **THEN** la marcha pasa a la 36×15

#### Scenario: Tres pasos en automático
- **WHEN** la marcha es la 36×17 en automático, se mantiene Campag. D y se pulsa la palanca D2
- **THEN** la marcha pasa a la 52×17

#### Scenario: Salto recortado en manual
- **WHEN** la marcha es la 52×12 en manual y se pide «+3 marchas»
- **THEN** la marcha pasa a la 52×11, sin aviso de tope

#### Scenario: A la más dura desde el plato pequeño
- **WHEN** la marcha es la 36×24, se mantiene Campag. I y se pulsa la palanca I1
- **THEN** la marcha pasa a la 52×11

### Requirement: Topes con la Shimano
Con la Shimano, al pedir una marcha o un plato que no existe, la app SHALL quedarse donde está y avisar del tope en pantalla y con la vibración doble: más allá de la 52×11 o de la 36×36, el tope de marcha más dura o más ligera; en manual, más allá del piñón 11 o del 36 en el otro plato, el tope de ese piñón; y al pedir el plato en el que ya está, el tope de ese plato.

#### Scenario: Más dura en automático
- **WHEN** la marcha es la 52×11 en automático y se pide «+1 marcha»
- **THEN** la marcha sigue en la 52×11 y se avisa «Tope · marcha más dura»

#### Scenario: Piñón más pequeño en manual
- **WHEN** la marcha es la 36×11 en manual y se pide «+1 marcha»
- **THEN** la marcha sigue en la 36×11 y se avisa «Tope · piñón 11»

#### Scenario: Plato que ya está puesto
- **WHEN** la marcha es la 52×17 y se pide «Plato más grande»
- **THEN** la marcha sigue en la 52×17 y se avisa «Tope · plato 52»

### Requirement: Cambiar de transmisión
Al elegir otra transmisión en Marchas, la app SHALL aplicarla al momento, sin terminar la sesión ni perder las conexiones: la marcha SHALL pasar a la última usada con esa transmisión si se recuerda y es válida, o a su referencia, y el Neo SHALL recibir la resistencia de esa marcha. La marcha y la referencia de cada transmisión SHALL conservarse al cambiar a otra.

#### Scenario: Ida y vuelta
- **WHEN** con la lineal en la 17 se elige la Shimano, se cambia a la 52×15 y se vuelve a elegir la lineal
- **THEN** la marcha vuelve a ser la 17, y al elegir otra vez la Shimano, la 52×15

#### Scenario: Rodando
- **WHEN** se rueda sin Zwift con la lineal en la 17 (el Neo simula +5 %), «Bajadas» por defecto, y se elige la Shimano por primera vez
- **THEN** la marcha pasa a la 36×17 y el Neo pasa a simular 0 %, sin terminar la sesión

#### Scenario: Sin Neo conectado
- **WHEN** el Neo no está conectado y se elige la Shimano
- **THEN** la marcha pasa a la de la Shimano y el Neo la recibe en cuanto se conecta

### Requirement: Cambiar de modo
Al cambiar el modo de cambio de la Shimano en Marchas, la app SHALL aplicarlo a la siguiente acción, sin cambiar la marcha puesta, aunque sea una combinación que el modo automático no usa.

#### Scenario: De manual a automático en una marcha cruzada
- **WHEN** la marcha es la 36×13 en manual, se elige «Automático» y se pide «+1 marcha»
- **THEN** la marcha sigue en la 36×13 al elegir el modo y pasa a la 52×15 con la acción
