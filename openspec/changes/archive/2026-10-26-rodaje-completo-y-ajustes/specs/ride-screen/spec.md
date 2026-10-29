# Spec Delta

## MODIFIED Requirements

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo las conexiones, el modo con la transmisión, la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas que ocupan el resto del alto, al pie el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados, la marcha es la 14 y los datos son los de por defecto
- **THEN** se ven, sin desplazamiento, los chips del Neo y del Square, «SIM» y «Lineal · 24 marchas», «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Seis datos en vertical
- **WHEN** el móvil está en vertical con los 6 datos elegidos
- **THEN** los datos se ven en 2 columnas y 3 filas, sin desplazamiento y sin cifras cortadas

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los dos chips dicen «sin conexión», la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

### Requirement: Potencia y cadencia
La potencia SHALL mostrarse como la media de los últimos 3 s de lo que mide el Neo, en W, y la cadencia como la mide el Neo, en rpm, las dos actualizadas mientras se pedalea; sin el Neo conectado, las dos SHALL mostrarse como «—».

#### Scenario: Pedaleando
- **WHEN** se pedalea con el Neo conectado
- **THEN** la potencia y la cadencia cambian con el pedaleo

#### Scenario: Media de 3 s
- **WHEN** el Neo mide 200 W durante unos segundos y después 300 W
- **THEN** la potencia pasa de 200 W a 300 W en unos 3 s, sin saltar de golpe

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 3 s la cadencia baja a 0 y la potencia a 0

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo
- **THEN** la potencia y la cadencia muestran «—» y la marcha sigue visible

## ADDED Requirements

### Requirement: Datos elegidos en Rodaje
La pantalla de rodaje SHALL mostrar los datos elegidos en «Datos en pantalla», de 1 a 6, en el orden elegido, en horizontal y en vertical. Por defecto SHALL mostrar potencia y cadencia. Al cambiar los datos en Ajustes, Rodaje SHALL mostrarlos al volver, sin terminar la sesión.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez
- **THEN** Rodaje muestra la potencia y la cadencia, en ese orden

#### Scenario: Un solo dato
- **WHEN** solo está elegida la cadencia
- **THEN** Rodaje muestra solo la cadencia, con la cifra más grande que cabe

#### Scenario: Orden elegido
- **WHEN** se eligen tiempo, potencia y W/kg, en ese orden, y se vuelve a Rodaje
- **THEN** Rodaje muestra los tres en ese orden, con el mismo tamaño de cifra, y la marcha no ha cambiado

#### Scenario: En ERG
- **WHEN** se está en ERG con cuatro datos elegidos
- **THEN** la potencia objetivo ocupa el lugar de la marcha y los cuatro datos siguen visibles en la rejilla

### Requirement: Datos de la sesión
Tiempo, potencia media y potencia máxima SHALL contar desde que empieza la sesión y solo mientras se pedalea (cadencia mayor que 0). W/kg SHALL ser la potencia de 3 s dividida por el peso del ciclista de Ajustes, con un decimal. Sin el Neo conectado, W/kg SHALL mostrarse como «—», y tiempo, potencia media y máxima SHALL conservar su valor sin avanzar.

#### Scenario: Tiempo parado
- **WHEN** se pedalea 10 min, se para 2 min y se vuelve a pedalear 5 min
- **THEN** el tiempo dice «15:00»

#### Scenario: Media sin las paradas
- **WHEN** se pedalea 10 min a 200 W, se para 5 min y se pedalea otros 10 min a 200 W
- **THEN** la potencia media dice 200 W

#### Scenario: Máxima
- **WHEN** durante la sesión la potencia de 3 s llega a 650 W y después baja
- **THEN** la potencia máxima sigue diciendo 650 W

#### Scenario: W/kg
- **WHEN** el peso del ciclista es 75 kg y la potencia es 247 W
- **THEN** W/kg dice «3,3»

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo tras 20 min pedaleando
- **THEN** W/kg muestra «—» y el tiempo sigue en 20 min, sin avanzar hasta que el Neo vuelve y se pedalea

#### Scenario: Sesión nueva
- **WHEN** se termina la sesión y se vuelve a abrir la app
- **THEN** el tiempo, la potencia media y la máxima empiezan desde cero

### Requirement: Botones − y + en Rodaje
Con «Botones − y + en pantalla» activado, Rodaje SHALL mostrar un botón + y un botón −, de al menos 72 dp, que hacen lo mismo que las acciones «+1 marcha» y «−1 marcha» del Square, con sus topes, avisos y vibración, y en ERG ajustan ±10 W. Desactivado (por defecto), SHALL NOT mostrarlos.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez
- **THEN** Rodaje no muestra los botones − y +

#### Scenario: Subir con el botón
- **WHEN** los botones están activados, la marcha es la 14 y se toca +
- **THEN** la marcha pasa a la 15, el Neo recibe la resistencia nueva y el pie dice «+1 marcha»

#### Scenario: Tope con el botón
- **WHEN** los botones están activados, la marcha es la 1 y se toca −
- **THEN** la marcha sigue en la 1 y se ve «Tope · marcha más ligera»

#### Scenario: Con la Shimano
- **WHEN** los botones están activados, la Shimano está en automático en la 36×15 y se toca +
- **THEN** la marcha pasa a la 52×19, como con la palanca D2

#### Scenario: En ERG
- **WHEN** los botones están activados, se está en ERG manual a 200 W y se toca +
- **THEN** la potencia pasa a 210 W y el pie dice «+10 W»

#### Scenario: Sin Neo
- **WHEN** los botones están activados, el Neo no está conectado y se toca +
- **THEN** la marcha sube y el Neo la recibe al conectarse

#### Scenario: Seis datos con botones
- **WHEN** los botones están activados con 6 datos elegidos, en horizontal o en vertical
- **THEN** los botones y los 6 datos se ven a la vez, sin desplazamiento

### Requirement: Aviso de la acción desactivable
Con «Mostrar la acción» desactivado, el pie de Rodaje SHALL NOT mostrar el aviso de la acción. Los topes de la marcha y de la potencia y la ayuda para conectar SHALL seguir viéndose. Activado (por defecto), el aviso SHALL mostrarse como hasta ahora.

#### Scenario: Desactivado
- **WHEN** «Mostrar la acción» está desactivado y se pulsa la palanca D1 en la 14
- **THEN** la marcha pasa a la 15 y el pie no dice «+1 marcha»

#### Scenario: Tope con el aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura»

#### Scenario: Tope de potencia con el aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado, se está en ERG manual a 1000 W y se pulsa la palanca D1
- **THEN** el pie dice «Tope · 1000 W»

#### Scenario: Ayuda para conectar
- **WHEN** «Mostrar la acción» está desactivado y el Neo se está reconectando
- **THEN** el pie dice «Pedalea para despertar el Neo»

### Requirement: Dispositivos desde los chips
Al tocar un chip de conexión, también el de Zwift, la app SHALL abrir Dispositivos. Mientras la app intenta conectar algún dispositivo, el pie SHALL decir cómo despertarlo; si falta alguno y no lo intenta, SHALL decir «Toca Neo o Square para conectarlos». El estado de Zwift SHALL NOT cambiar lo que dice el pie.

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app sin dispositivos recordados
- **THEN** los chips del Neo y del Square dicen «sin conexión», la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Neo dormido al abrir
- **WHEN** se abre la app con el Neo y el Square recordados, el Square se conecta y el Neo está dormido
- **THEN** el chip del Neo dice «Neo conectando», la potencia y la cadencia muestran «—» y el pie dice «Pedalea para despertar el Neo» hasta que se conecta

#### Scenario: Square reconectando
- **WHEN** el Neo está conectado y el Square se está reconectando
- **THEN** el pie dice «Pulsa un botón del Square para despertarlo»

#### Scenario: Los dos conectando
- **WHEN** la app intenta conectar el Neo y el Square a la vez
- **THEN** el pie dice «Pedalea y pulsa un botón del Square para despertarlos»

#### Scenario: Aviso de acción mientras reconecta
- **WHEN** el Neo se está reconectando y se sube una marcha con la palanca D1
- **THEN** el pie dice «+1 marcha» durante 1,5 s y después vuelve a «Pedalea para despertar el Neo»

#### Scenario: Tocar un chip
- **WHEN** se toca el chip del Neo
- **THEN** se abre Dispositivos con la barra inferior, y «Rodar» o Atrás vuelven a Rodaje

#### Scenario: Tocar el chip de Zwift
- **WHEN** el Neo y el Square están conectados, Zwift no, y se toca el chip «Zwift esperando»
- **THEN** se abre Dispositivos, y el pie de Rodaje no dice nada sobre Zwift

## REMOVED Requirements

### Requirement: Diagnóstico desde los chips
**Reason**: Con la barra inferior, los chips abren Dispositivos, que muestra las conexiones y permite buscar y olvidar; Diagnóstico pasa a abrirse desde Ajustes.
**Migration**: Los escenarios del pie pasan sin cambios a «Dispositivos desde los chips». Para abrir Diagnóstico: Ajustes → «Diagnóstico».
