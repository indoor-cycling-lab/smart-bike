## ADDED Requirements

### Requirement: Pantalla de Marchas
Al tocar «Marchas» en Ajustes, la app SHALL abrir la pantalla de Marchas, con un botón de volver a Ajustes, y la sesión SHALL seguir en marcha. La pantalla SHALL mostrar la transmisión, con la Shimano sus platos, su cassette y el modo de cambio, y la sección «Al empezar» con la marcha de referencia y «Recordar la última marcha».

#### Scenario: Ida y vuelta
- **WHEN** en Ajustes se toca «Marchas», luego volver
- **THEN** se abre Marchas y después se vuelve a Ajustes, sin terminar la sesión

#### Scenario: Atrás del sistema
- **WHEN** Marchas está abierta y se pulsa Atrás
- **THEN** se vuelve a Ajustes

### Requirement: Elegir la transmisión
Marchas SHALL ofrecer dos transmisiones excluyentes, cada una con una ayuda de una línea: «Lineal» («24 marchas numeradas, sin platos ni piñones») y «Shimano 105 2×12» («52/36 · 11–36»). La elegida SHALL distinguirse por algo más que el color, y tocar cualquier punto de su fila SHALL elegirla.

#### Scenario: Valor por defecto
- **WHEN** se abre Marchas por primera vez
- **THEN** está elegida «Lineal» y se ve la escalera de 24 marchas, sin platos, cassette ni modo de cambio

#### Scenario: Elegir la Shimano
- **WHEN** se toca la fila de «Shimano 105 2×12»
- **THEN** queda elegida, aparecen los platos 36 y 52, el cassette del 36 al 11 y el modo de cambio, y la marcha de Rodaje pasa a ser plato × piñón

### Requirement: Modo de cambio en Marchas
Con la Shimano elegida, Marchas SHALL ofrecer dos modos excluyentes con su ayuda: «Automático» (por defecto: «Palancas D más dura y más ligera; el plato cambia solo, como el Di2 en Synchro Shift») y «Manual» («Palancas D cambian el piñón y palancas I el plato, como tus mandos»).

#### Scenario: Valor por defecto
- **WHEN** se elige la Shimano por primera vez
- **THEN** está elegido «Automático»

#### Scenario: Elegir manual
- **WHEN** se toca «Manual»
- **THEN** queda elegido «Manual», y la siguiente pulsación de la palanca D2 cambia solo el piñón

### Requirement: Marcha de referencia en Marchas
En «Al empezar», Marchas SHALL mostrar la marcha de referencia de la transmisión elegida con un paso a paso − y +: con la lineal, de la 1 a la 24; con la Shimano, por las 24 combinaciones de plato y piñón ordenadas de la más ligera a la más dura. Con la Shimano, los platos y el cassette SHALL resaltar la referencia. En el extremo, el botón de ese lado SHALL quedar deshabilitado.

#### Scenario: Referencia lineal
- **WHEN** la referencia es la 12 y se toca + en la marcha de referencia
- **THEN** la referencia pasa a la 13 y la marca de la escalera de Rodaje pasa a la 13, sin cambiar la marcha puesta

#### Scenario: Referencia con la Shimano
- **WHEN** la referencia es la 36×17 y se toca +
- **THEN** la referencia pasa a la 52×24, la siguiente combinación más dura

#### Scenario: Extremo
- **WHEN** la referencia es la 52×11
- **THEN** el + está deshabilitado y el − no

### Requirement: Recordar la última marcha en Marchas
En «Al empezar», Marchas SHALL tener la casilla «Recordar la última marcha», activada por defecto, con la ayuda «Si no, empieza en la de referencia».

#### Scenario: Desactivar
- **WHEN** se desactiva «Recordar la última marcha» con la referencia en la 12 y se cierra y se vuelve a abrir la app en la 20
- **THEN** la marcha puesta es la 12

## MODIFIED Requirements

### Requirement: Ajustes desde Diagnóstico
Mientras no exista la navegación inferior, Ajustes SHALL abrirse desde Diagnóstico, con un botón de volver a Diagnóstico, y SHALL mostrar la entrada «Marchas», con la transmisión elegida y su modo, y la sección Rodillo con el ajuste «Bajadas».

#### Scenario: Ida y vuelta
- **WHEN** en Diagnóstico se toca «Ajustes», luego volver
- **THEN** se abre Ajustes con «Marchas» y la sección Rodillo y después se vuelve a Diagnóstico, sin terminar la sesión

#### Scenario: Atrás del sistema
- **WHEN** Ajustes está abierta y se pulsa Atrás
- **THEN** se vuelve a Diagnóstico

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas, SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se elige «Nunca empujar» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegida «Nunca empujar» y se aplica desde el primer envío al Neo

#### Scenario: Configuración anterior al ajuste
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Bajadas»
- **THEN** se conservan los demás ajustes y «Bajadas» está en «La marcha no crea bajadas»

#### Scenario: Transmisión y modo guardados
- **WHEN** se elige la Shimano en «Manual» y se cierra y se vuelve a abrir la app
- **THEN** la transmisión es la Shimano en manual
