## ADDED Requirements

### Requirement: Orientación en Ajustes
La sección «Pantalla» de Ajustes SHALL tener, debajo de «Tema», el ajuste «Orientación» con tres opciones excluyentes con su ayuda: «Según el móvil» (por defecto), «Vertical» y «Horizontal». La elegida SHALL aplicarse al momento a toda la app, sin terminar la sesión, cambiar la marcha ni cortar ninguna conexión. Con «Vertical» u «Horizontal», la app SHALL verse en esa orientación sea cual sea la posición del móvil y el bloqueo de rotación del sistema.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** la sección «Pantalla» muestra «Orientación», debajo de «Tema», con «Según el móvil» elegido

#### Scenario: Ayuda de cada opción
- **WHEN** se mira «Orientación»
- **THEN** «Según el móvil» dice «Gira con el móvil y respeta el bloqueo de rotación», «Vertical» dice «Siempre en vertical, aunque gires el móvil» y «Horizontal» dice «Siempre en horizontal, aunque gires el móvil»

#### Scenario: Fijar horizontal con el móvil en vertical
- **WHEN** se rueda en la marcha 14 con el Neo y el Square conectados, con el móvil en vertical, y se elige «Horizontal» en Ajustes
- **THEN** Ajustes pasa a verse en horizontal en cuanto se elige, con «Horizontal» elegido, y al volver a Rodaje se ve la disposición horizontal en la marcha 14 con el Neo y el Square conectados

#### Scenario: Girar el móvil con una orientación fija
- **WHEN** «Vertical» está elegido y se gira el móvil a horizontal en Rodaje
- **THEN** Rodaje sigue en vertical, sin cambiar de disposición

#### Scenario: Bloqueo de rotación del sistema contrario
- **WHEN** el bloqueo de rotación del sistema está activado en vertical y se elige «Horizontal»
- **THEN** la app se ve en horizontal, y las demás apps siguen bloqueadas en vertical

#### Scenario: Horizontal en el otro sentido
- **WHEN** «Horizontal» está elegido y se gira el móvil 180°, de horizontal a horizontal en el otro sentido
- **THEN** Rodaje se ve derecho, en horizontal

#### Scenario: Volver a «Según el móvil»
- **WHEN** «Horizontal» está elegido, el móvil está en vertical sin el bloqueo de rotación del sistema y se elige «Según el móvil»
- **THEN** la app pasa a verse en vertical, y vuelve a girar con el móvil

#### Scenario: Pantallas secundarias
- **WHEN** «Horizontal» está elegido, el móvil está en vertical y se abren Marchas, Datos en pantalla y Diagnóstico desde Ajustes
- **THEN** las tres se ven en horizontal

#### Scenario: Opción elegida sin color
- **WHEN** se mira la sección «Pantalla»
- **THEN** la opción de orientación elegida se distingue por la marca de selección, no solo por el color, y el lector de pantalla anuncia cuál está elegida

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción de orientación, incluida su ayuda
- **THEN** se elige esa opción

#### Scenario: Cambio con Zwift conectado
- **WHEN** se rueda en Zwift y se cambia la orientación
- **THEN** Zwift sigue conectado, recibiendo la potencia y la cadencia, y la resistencia del Neo no cambia

## MODIFIED Requirements

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Pantalla», «Ciclista», «Rodillo» y «Zwift», SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se elige «Nunca empujar» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegida «Nunca empujar» y se aplica desde el primer envío al Neo

#### Scenario: Configuración anterior al ajuste
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Bajadas»
- **THEN** se conservan los demás ajustes y «Bajadas» está en «La marcha no crea bajadas»

#### Scenario: Transmisión y modo guardados
- **WHEN** se elige la Shimano en «Manual» y se cierra y se vuelve a abrir la app
- **THEN** la transmisión es la Shimano en manual

#### Scenario: Datos guardados
- **WHEN** se eligen tiempo y potencia, en ese orden, se activan los botones − y + y se cierra y se vuelve a abrir la app
- **THEN** Rodaje muestra tiempo y potencia, en ese orden, y los botones − y +

#### Scenario: Configuración anterior a «Al rodar»
- **WHEN** se abre la app con una configuración guardada antes de este change
- **THEN** se conservan la transmisión, la asignación y los demás ajustes, y los datos son potencia y cadencia, los botones − y + están desactivados, el peso del ciclista es 75 kg y la pendiente sin Zwift 0,0 %

#### Scenario: Tema guardado
- **WHEN** se elige «Claro» y se cierra y se vuelve a abrir la app
- **THEN** Rodaje se abre en claro, sin mostrarse antes en oscuro

#### Scenario: Configuración anterior al tema
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Tema»
- **THEN** se conservan los demás ajustes y el tema es «Oscuro»

#### Scenario: Orientación guardada
- **WHEN** se elige «Horizontal» y se cierra y se vuelve a abrir la app con el móvil en vertical
- **THEN** Rodaje se abre en horizontal y «Orientación» sigue en «Horizontal»

#### Scenario: Configuración anterior a la orientación
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Orientación»
- **THEN** se conservan los demás ajustes y la orientación es «Según el móvil»

#### Scenario: Mando desactivado guardado
- **WHEN** se desactiva «Mando de Zwift» y se cierra y se vuelve a abrir la app
- **THEN** sigue desactivado y la app no se anuncia como mando

#### Scenario: Configuración anterior al mando
- **WHEN** se abre la app con una configuración guardada antes de que existiera «Mando de Zwift»
- **THEN** se conservan los demás ajustes y «Mando de Zwift» está activado

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro» y la orientación «Según el móvil»

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; y la sección «Zwift», con «Mando de Zwift». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista, Rodillo y Zwift, sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

#### Scenario: Abrir Diagnóstico
- **WHEN** se toca «Diagnóstico» en Ajustes
- **THEN** se abre Diagnóstico con un botón de volver a Ajustes

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** las secciones se recorren desplazando la pantalla y la barra inferior sigue visible al pie

#### Scenario: Ajustes en horizontal
- **WHEN** «Horizontal» está elegido y se abre Ajustes
- **THEN** todas las secciones, «Orientación» incluida, se recorren desplazando la pantalla y la barra inferior sigue visible al pie
