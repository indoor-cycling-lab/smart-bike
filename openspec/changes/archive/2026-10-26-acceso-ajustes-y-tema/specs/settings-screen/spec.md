## ADDED Requirements

### Requirement: Tema en Ajustes
La sección «Pantalla» de Ajustes SHALL tener el ajuste «Tema» con dos opciones excluyentes, «Oscuro» y «Claro», con «Oscuro» por defecto. Al elegir una SHALL aplicarse al momento a toda la app, también a Rodaje y a las pantallas secundarias, sin terminar la sesión, cambiar la marcha ni cortar ninguna conexión.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** la sección «Pantalla» muestra «Tema» con «Oscuro» elegido

#### Scenario: Cambiar a claro
- **WHEN** se rueda en la marcha 14 con el Neo y el Square conectados, se elige «Claro» en Ajustes y se vuelve a Rodaje
- **THEN** Ajustes pasa a verse en claro en cuanto se elige, Rodaje se ve en claro con la marcha 14 y los chips del Neo y del Square siguen con el punto lleno

#### Scenario: Pantallas secundarias en claro
- **WHEN** el tema es «Claro» y se abren Marchas, Datos en pantalla y Diagnóstico desde Ajustes
- **THEN** las tres se ven en claro

#### Scenario: Volver a oscuro
- **WHEN** el tema es «Claro» y se elige «Oscuro»
- **THEN** toda la app vuelve a verse como antes de este change

#### Scenario: Opción elegida sin color
- **WHEN** se mira la sección «Pantalla»
- **THEN** la opción elegida se distingue por la marca de selección, no solo por el color, y el lector de pantalla anuncia cuál está elegida

#### Scenario: Cambio con Zwift conectado
- **WHEN** se rueda en Zwift y se cambia el tema
- **THEN** Zwift sigue conectado, recibiendo la potencia y la cadencia, y la resistencia del Neo no cambia

## MODIFIED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema»; la sección «Ciclista»; y la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista y Rodillo, sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

#### Scenario: Abrir Diagnóstico
- **WHEN** se toca «Diagnóstico» en Ajustes
- **THEN** se abre Diagnóstico con un botón de volver a Ajustes

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** las secciones se recorren desplazando la pantalla y la barra inferior sigue visible al pie

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Pantalla», «Ciclista» y «Rodillo», SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

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

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro»
