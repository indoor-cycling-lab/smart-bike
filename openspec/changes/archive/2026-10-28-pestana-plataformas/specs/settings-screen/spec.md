# Spec Delta

## MODIFIED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, «Botones», con la transmisión de la asignación, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; y la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible. Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Plataformas.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Botones», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista, Rodillo y Copia de seguridad, sin sección «Zwift» y sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

#### Scenario: Entrada de Botones
- **WHEN** la transmisión es la Shimano y se abre Ajustes
- **THEN** la entrada «Botones» dice que es la asignación de «Shimano 105», lleva el icono propio de los cuatro símbolos y al tocarla se abre Botones con un botón de volver a Ajustes

#### Scenario: Abrir Diagnóstico
- **WHEN** se toca «Diagnóstico» en Ajustes
- **THEN** se abre Diagnóstico con un botón de volver a Ajustes

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** las secciones se recorren desplazando la pantalla y la barra inferior sigue visible al pie

#### Scenario: Ajustes en horizontal
- **WHEN** «Horizontal» está elegido y se abre Ajustes
- **THEN** todas las secciones, «Orientación» incluida, se recorren desplazando la pantalla y la barra inferior sigue visible al pie

#### Scenario: Copia de seguridad al final
- **WHEN** se recorre Ajustes hasta el final
- **THEN** la sección «Copia de seguridad» va después de «Rodillo», con «Exportar configuración» e «Importar configuración», cada una con un objetivo táctil de al menos 48 dp

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Pantalla», «Ciclista», «Rodillo», y los de Plataformas (spec `platform-settings`), SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

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
- **WHEN** se desactiva «Mando de Zwift» en Plataformas y se cierra y se vuelve a abrir la app
- **THEN** sigue desactivado y la app no se anuncia como mando

#### Scenario: Configuración anterior al mando
- **WHEN** se abre la app con una configuración guardada antes de que existiera «Mando de Zwift»
- **THEN** se conservan los demás ajustes y «Mando de Zwift» está activado

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro» y la orientación «Según el móvil»

## REMOVED Requirements

### Requirement: Mando de Zwift en Ajustes
**Reason**: Lo que la app publica ante Zwift pasa a la pestaña «Plataformas», donde «Mando de Zwift» solo existe con el modo de rodillo por wifi.
**Migration**: Spec `platform-settings`, «Rodillo por wifi con mando opcional» y «Zwift Ride con el mando dentro de DirCon».

### Requirement: Conexión con Zwift en Ajustes
**Reason**: La elección del modo de conexión sale de Ajustes y pasa a Plataformas, ligada a la plataforma elegida.
**Migration**: Spec `platform-settings`, «Una sola plataforma y un solo modo» y «Ajustes de Zwift guardados con migración».

### Requirement: Simular un KICKR BIKE en Ajustes
**Reason**: El interruptor se sustituye por el selector «Nombre ante la app» de Plataformas.
**Migration**: Spec `platform-settings`, «Nombre ante la app»; una configuración con el interruptor activado se abre con «KICKR BIKE PRO» elegido.
