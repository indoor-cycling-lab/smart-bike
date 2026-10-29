# Spec Delta

## Purpose

Marcha virtual que el ciclista cambia con los botones del Square, como en un grupo inalámbrico: subir y bajar, saltos, extremos y marcha de referencia, con topes avisados y la marcha recordada al reconectar y entre sesiones.

## ADDED Requirements

### Requirement: Transmisión lineal por defecto
La app SHALL ofrecer una transmisión lineal de 24 marchas numeradas del 1 (la más ligera) al 24 (la más dura), con la 12 como marcha inicial y como marcha de referencia.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez
- **THEN** la marcha puesta es la 12 de 24 y la marca de referencia está en la 12

### Requirement: Subir y bajar una marcha con las palancas
Al pulsar una palanca derecha (1 o 2) sin modificador, la app SHALL subir una marcha, y al pulsar una palanca izquierda (1 o 2), bajar una, en el momento de pulsar y sin esperar a soltarla.

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
- **WHEN** se mantiene pulsada la palanca D1
- **THEN** sube una sola marcha; mantenerla no repite el cambio

### Requirement: Saltos y extremos con el Campagnolo como modificador
Con un Campagnolo mantenido antes de pulsar una palanca de su lado, la app SHALL hacer el salto de esa combinación, sin cambiar además una marcha suelta: Campag. D + palanca D1 sube 3, Campag. D + palanca D2 va a la más dura, Campag. I + palanca I1 baja 3 y Campag. I + palanca I2 va a la más ligera.

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

### Requirement: Volver a la marcha de referencia
Al pulsar □, la app SHALL poner la marcha de referencia.

#### Scenario: Desde una marcha dura
- **WHEN** la marcha es la 20 y se pulsa □
- **THEN** la marcha pasa a la 12

#### Scenario: Ya en la referencia
- **WHEN** la marcha es la 12 y se pulsa □
- **THEN** la marcha no cambia y no se avisa de un tope

### Requirement: Topes
Al pedir una marcha más allá de la 1 o de la 24, la app SHALL quedarse en el extremo y avisar del tope en pantalla y con una vibración doble; un salto que lo sobrepasa SHALL avanzar hasta el extremo, y solo SHALL avisar del tope si la marcha ya estaba en él.

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
La app SHALL acompañar cada cambio de marcha de una vibración corta, distinta de la doble del tope, también con la pantalla apagada.

#### Scenario: Cambio con la pantalla apagada
- **WHEN** la pantalla del móvil está apagada y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil vibra una vez

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
La app SHALL guardar la marcha puesta y, al empezar una sesión nueva, SHALL empezar en la última marcha de la sesión anterior; si no hay ninguna guardada o la guardada no es válida, SHALL empezar en la marcha inicial.

#### Scenario: Volver al día siguiente
- **WHEN** se termina la sesión en la marcha 17 y se vuelve a abrir la app
- **THEN** la marcha puesta es la 17

#### Scenario: Cierre inesperado
- **WHEN** la marcha pasa a la 9 y unos segundos después Android cierra la app
- **THEN** al volver a abrirla, la marcha es la 9

### Requirement: Solo cambian la marcha las palancas, los Campagnolo y □
Mientras no exista la asignación configurable, el resto de botones del Square SHALL NOT cambiar la marcha ni la resistencia.

#### Scenario: Botón A
- **WHEN** se pulsa A
- **THEN** la marcha no cambia y Diagnóstico muestra «A · pulsar · sin acción»
