# Spec Delta

## ADDED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, y «Diagnóstico»; la sección «Al rodar»; la sección «Ciclista»; y la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Diagnóstico» y las secciones Al rodar, Ciclista y Rodillo, sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

#### Scenario: Abrir Diagnóstico
- **WHEN** se toca «Diagnóstico» en Ajustes
- **THEN** se abre Diagnóstico con un botón de volver a Ajustes

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** las secciones se recorren desplazando la pantalla y la barra inferior sigue visible al pie

### Requirement: Al rodar en Ajustes
La sección «Al rodar» SHALL tener la entrada «Datos en pantalla», que dice los datos elegidos y abre su pantalla, y tres casillas con su ayuda de una línea: «Botones − y + en pantalla» (desactivada por defecto), «Mostrar la acción» y «Vibrar» (las dos activadas por defecto). Tocar cualquier punto de la fila SHALL cambiar la casilla, y su estado SHALL distinguirse por algo más que el color.

#### Scenario: Valores por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Datos en pantalla» dice «Potencia y cadencia», «Botones − y + en pantalla» está desactivada y «Mostrar la acción» y «Vibrar» están activadas

#### Scenario: Activar los botones
- **WHEN** se activa «Botones − y + en pantalla» y se vuelve a Rodaje
- **THEN** Rodaje muestra los botones − y +

#### Scenario: Desactivar la vibración rodando
- **WHEN** se desactiva «Vibrar» con la sesión en marcha y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil no vibra

#### Scenario: Resumen de muchos datos
- **WHEN** hay cuatro datos elegidos
- **THEN** la entrada «Datos en pantalla» dice los cuatro, en su orden, sin pasar de una línea (con «…» si no caben)

### Requirement: Peso del ciclista
La sección «Ciclista» SHALL tener «Peso del ciclista», con un paso a paso − y + de 1 kg entre 40 y 150 kg y 75 kg por defecto, que usa el dato W/kg. En cada extremo, el botón de ese lado SHALL quedar deshabilitado.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** el peso del ciclista dice «75 kg»

#### Scenario: Cambiar el peso
- **WHEN** se toca + en el peso del ciclista con 75 kg y W/kg está en Rodaje
- **THEN** el peso pasa a 76 kg y W/kg se calcula con 76 kg

#### Scenario: Extremo
- **WHEN** el peso del ciclista es 150 kg
- **THEN** el + está deshabilitado y el − no

### Requirement: Pendiente sin Zwift
La sección «Rodillo» SHALL tener «Pendiente sin Zwift», con la ayuda «Al rodar solo con la app» y un paso a paso − y + de 0,5 % entre −5,0 % y +15,0 %, 0,0 % por defecto. Es la pendiente del terreno al rodar sin Zwift: la marcha y «Bajadas» se aplican sobre ella. En cada extremo, el botón de ese lado SHALL quedar deshabilitado.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Pendiente sin Zwift» dice «0,0 %»

#### Scenario: Cambiar rodando sin Zwift
- **WHEN** se rueda sin Zwift en la marcha 12 y se sube «Pendiente sin Zwift» a 3,0 %
- **THEN** el Neo pasa a simular +3 % al momento y la marcha sigue siendo la 12

#### Scenario: Con Zwift
- **WHEN** se rueda en una ruta de Zwift y se cambia «Pendiente sin Zwift»
- **THEN** el Neo sigue la pendiente de Zwift con la marcha, y la nueva pendiente sin Zwift se aplica si Zwift se va

#### Scenario: Extremo
- **WHEN** «Pendiente sin Zwift» es −5,0 %
- **THEN** el − está deshabilitado y el + no

#### Scenario: Sin Neo conectado
- **WHEN** el Neo no está conectado y se cambia «Pendiente sin Zwift» a 2,0 %
- **THEN** el ajuste queda guardado y el Neo recibe la pendiente con el valor nuevo en cuanto se conecta

### Requirement: Pantalla Datos en pantalla
Al tocar «Datos en pantalla», la app SHALL abrir su pantalla, con volver a Ajustes: una vista previa de Rodaje con los datos elegidos, el contador «N de 6» y la lista de los seis datos (potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima), cada uno con su casilla, su ayuda y un asa para ordenarlo. Los cambios SHALL aplicarse al momento.

#### Scenario: Por defecto
- **WHEN** se abre Datos en pantalla por primera vez
- **THEN** están marcadas potencia y cadencia, el contador dice «2 de 6» y la vista previa muestra los dos

#### Scenario: Añadir un dato
- **WHEN** se marca «Tiempo»
- **THEN** el contador dice «3 de 6», la vista previa muestra potencia, cadencia y tiempo, y Rodaje también

#### Scenario: Ordenar
- **WHEN** se arrastra «Cadencia» por encima de «Potencia»
- **THEN** la lista, la vista previa y Rodaje muestran la cadencia antes que la potencia

#### Scenario: Ordenar sin arrastrar
- **WHEN** se usa el lector de pantalla sobre el asa de «Cadencia»
- **THEN** ofrece «Subir» y «Bajar», que la mueven un puesto

#### Scenario: Todos los datos
- **WHEN** se marcan los seis datos
- **THEN** el contador dice «6 de 6» y la vista previa los muestra todos sin desplazamiento

#### Scenario: Al menos uno
- **WHEN** solo queda marcada la potencia
- **THEN** su casilla no se puede desmarcar, y la ayuda dice que hace falta al menos un dato

#### Scenario: En ERG
- **WHEN** se abre Datos en pantalla
- **THEN** una nota dice que en ERG la potencia objetivo se muestra siempre y no cuenta como dato

## MODIFIED Requirements

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Ciclista» y «Rodillo», SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

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

## REMOVED Requirements

### Requirement: Ajustes desde Diagnóstico
**Reason**: Con la barra inferior, Ajustes es una pestaña y no se abre desde Diagnóstico; ahora es Diagnóstico el que se abre desde Ajustes.
**Migration**: Ajustes se abre desde la barra inferior («Ajustes como pestaña»); sus entradas «Marchas» y la sección Rodillo siguen igual.
