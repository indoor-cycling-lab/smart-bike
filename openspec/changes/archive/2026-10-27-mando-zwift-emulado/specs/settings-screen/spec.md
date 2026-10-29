## ADDED Requirements

### Requirement: Mando de Zwift en Ajustes
Ajustes SHALL tener una sección «Zwift» con el interruptor «Mando de Zwift», activado por defecto, y una ayuda que diga que con él los botones del Square llegan a Zwift a través de la app, por Bluetooth, y que el Square ya no se empareja en Zwift. El cambio SHALL aplicarse al momento, también rodando.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** la sección «Zwift» tiene «Mando de Zwift» activado

#### Scenario: Activar rodando
- **WHEN** «Mando de Zwift» está desactivado, se rueda con el Neo y el Square y se activa
- **THEN** el mando empieza a anunciarse sin cambiar la marcha ni cortar la conexión del rodillo con Zwift

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth y se abre Ajustes
- **THEN** «Mando de Zwift» sigue pudiéndose cambiar y su ayuda dice que este móvil no puede hacer de mando

## MODIFIED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; y la sección «Zwift», con «Mando de Zwift». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible.

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

#### Scenario: Mando desactivado guardado
- **WHEN** se desactiva «Mando de Zwift» y se cierra y se vuelve a abrir la app
- **THEN** sigue desactivado y la app no se anuncia como mando

#### Scenario: Configuración anterior al mando
- **WHEN** se abre la app con una configuración guardada antes de que existiera «Mando de Zwift»
- **THEN** se conservan los demás ajustes y «Mando de Zwift» está activado

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro»
