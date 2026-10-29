# Spec Delta

## Purpose

Pantalla de rodaje de la app, legible a 1–2 m y manejable sin tocarla: la marcha puesta, los datos del Neo, el modo, las conexiones y los avisos de cada acción, sin nada del modelo de resistencia.

## ADDED Requirements

### Requirement: Rodaje como pantalla inicial
La app SHALL abrirse en la pantalla de rodaje, en horizontal y a pantalla completa, sin las barras del sistema.

#### Scenario: Abrir la app
- **WHEN** se abre la app
- **THEN** se ve la pantalla de rodaje en horizontal, aunque el móvil esté en vertical

### Requirement: Marcha en grande
La pantalla SHALL mostrar el número de la marcha puesta en grande, con «Marcha · de 24» al lado y una escalera con un segmento por marcha (la ligera a la izquierda), la actual resaltada y la marca de la marcha de referencia.

#### Scenario: Marcha 14
- **WHEN** la marcha es la 14
- **THEN** se lee «14» en grande, «de 24», y la escalera tiene resaltado el segmento 14 y marcado el 12

### Requirement: Animación del cambio
Al cambiar de marcha, la cifra nueva SHALL entrar desplazándose en la dirección del cambio (hacia arriba si es más dura) en unos 120 ms, y SHALL aparecer sin animación si el sistema tiene activado reducir el movimiento.

#### Scenario: Movimiento reducido
- **WHEN** el móvil tiene desactivadas las animaciones y se cambia de marcha
- **THEN** la cifra nueva aparece directamente

### Requirement: Aviso de tope en la marcha
Al llegar a un tope, la cifra de la marcha SHALL atenuarse y SHALL aparecer debajo la etiqueta del tope durante 1,5 s, sin aviso de acción.

#### Scenario: Tope en la más dura
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, y después vuelve a verse normal

### Requirement: Potencia y cadencia
La pantalla SHALL mostrar la potencia (W) y la cadencia (rpm) que mide el Neo, actualizadas mientras se pedalea; sin el Neo conectado, las dos SHALL mostrarse como «—».

#### Scenario: Pedaleando
- **WHEN** se pedalea con el Neo conectado
- **THEN** la potencia y la cadencia cambian con el pedaleo

#### Scenario: Dejar de pedalear
- **WHEN** se deja de pedalear
- **THEN** en unos 2 s la cadencia baja a 0 y la potencia a 0

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo
- **THEN** la potencia y la cadencia muestran «—» y la marcha sigue visible

### Requirement: Modo y transmisión
La barra superior SHALL mostrar el modo «SIM» y la transmisión «Lineal · 24 marchas».

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift
- **THEN** la barra superior dice «SIM» y «Lineal · 24 marchas»

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar un chip para el Neo y otro para el Square, con su nombre y el punto lleno si está conectado, y con su estado en palabra («conectando», «sin conexión») si no lo está; mientras no exista el puente, SHALL NOT mostrar el chip de Zwift.

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados
- **THEN** se ven los chips Neo y Square con el punto lleno, y ninguno de Zwift

#### Scenario: Square sin conexión
- **WHEN** se pierde la conexión con el Square
- **THEN** su chip dice «Square sin conexión», la marcha sigue visible y la potencia y la cadencia siguen actualizándose

### Requirement: Diagnóstico desde los chips
Al tocar un chip de conexión, la app SHALL abrir Diagnóstico, donde se buscan y conectan los dispositivos; mientras falte alguno, el pie SHALL decir «Toca Neo o Square para conectarlos».

#### Scenario: Sin dispositivos al abrir
- **WHEN** se abre la app con el Neo y el Square sin conectar
- **THEN** los dos chips dicen «sin conexión», la marcha guardada es visible, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Tocar un chip
- **WHEN** se toca el chip del Neo
- **THEN** se abre Diagnóstico y, al volver, se está otra vez en Rodaje

### Requirement: Aviso de la acción
Tras cada cambio de marcha, el pie SHALL mostrar durante 1,5 s la acción, sin el botón ni el gesto: «+1 marcha», «−1 marcha», «+3 marchas», «−3 marchas», «Marcha más dura», «Marcha más ligera» o «Marcha de referencia»; una acción nueva SHALL sustituir a la anterior.

#### Scenario: Salto
- **WHEN** se mantiene Campag. D y se pulsa la palanca D1 desde la 14
- **THEN** el pie dice «+3 marchas» durante 1,5 s

#### Scenario: Acciones seguidas
- **WHEN** se pulsa la palanca D1 y, antes de 1,5 s, la palanca I1
- **THEN** «−1 marcha» sustituye a «+1 marcha», sin apilarse

#### Scenario: Salto recortado por el extremo
- **WHEN** la marcha es la 23 y se pide subir 3
- **THEN** el pie dice «+1 marcha»

### Requirement: Sin datos del modelo de resistencia
La pantalla de rodaje SHALL NOT mostrar la pendiente base, el ajuste por marcha ni lo que se envía al Neo.

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17
- **THEN** en la pantalla de rodaje no aparece «+5 %» ni ninguna pendiente

### Requirement: Pantalla siempre encendida
Mientras se ve la pantalla de rodaje, el móvil SHALL NOT apagar la pantalla por inactividad.

#### Scenario: Rodaje largo sin tocar el móvil
- **WHEN** se rueda 10 minutos sin tocar el móvil con la pantalla de rodaje delante
- **THEN** la pantalla sigue encendida
