## MODIFIED Requirements

### Requirement: Al rodar en Ajustes
La sección «Al rodar» SHALL tener la entrada «Datos en pantalla», que dice los datos elegidos y abre su pantalla, y tres casillas con su ayuda de una línea: «Botones − y + en pantalla» (desactivada por defecto), «Mostrar la acción» y «Vibrar» (las dos activadas por defecto). La ayuda de la primera SHALL decir que, sin el Square conectado, los botones se muestran siempre y que este ajuste manda solo con el Square conectado. Tocar cualquier punto de la fila SHALL cambiar la casilla, y su estado SHALL distinguirse por algo más que el color.

#### Scenario: Valores por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Datos en pantalla» dice «Potencia y cadencia», «Botones − y + en pantalla» está desactivada y «Mostrar la acción» y «Vibrar» están activadas

#### Scenario: Ayuda de los botones
- **WHEN** se mira la casilla «Botones − y + en pantalla»
- **THEN** su ayuda dice que solo manda con el Square conectado y que sin él los botones se muestran siempre

#### Scenario: Activar los botones
- **WHEN** se activa «Botones − y + en pantalla» y se vuelve a Rodaje
- **THEN** Rodaje muestra los botones − y + (con el Square conectado)

#### Scenario: Desactivar la vibración rodando
- **WHEN** se desactiva «Vibrar» con la sesión en marcha y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil no vibra

#### Scenario: Resumen de muchos datos
- **WHEN** hay cuatro datos elegidos
- **THEN** la entrada «Datos en pantalla» dice los cuatro, en su orden, sin pasar de una línea (con «…» si no caben)

### Requirement: Pantalla Datos en pantalla
Al tocar «Datos en pantalla», la app SHALL abrir su pantalla, con volver a Ajustes: una vista previa de Rodaje con los datos elegidos, el contador «N de 6» y la lista de los nueve datos (potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad, distancia y pendiente), cada uno con su casilla, su ayuda y un asa para ordenarlo. Se SHALL poder marcar como máximo seis. Los cambios SHALL aplicarse al momento.

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
- **WHEN** se marcan seis de los nueve datos
- **THEN** el contador dice «6 de 6», la vista previa los muestra todos sin desplazamiento y las casillas de los tres sin marcar quedan deshabilitadas, con la ayuda «Quita uno para añadir otro»

#### Scenario: Velocidad y distancia
- **WHEN** se marcan «Velocidad» y «Distancia»
- **THEN** su ayuda dice que la velocidad la calcula el modelo físico y que con otro modelo se muestra «—»

#### Scenario: Pendiente
- **WHEN** se marca «Pendiente»
- **THEN** su ayuda dice que es la pendiente de la simulación que envía la app de entrenamiento, «0,0 %» sin ella y «—» en ERG, y la vista previa la muestra

#### Scenario: Al menos uno
- **WHEN** solo queda marcada la potencia
- **THEN** su casilla no se puede desmarcar, y la ayuda dice que hace falta al menos un dato

#### Scenario: En ERG
- **WHEN** se abre Datos en pantalla
- **THEN** una nota dice que en ERG la potencia objetivo se muestra siempre y no cuenta como dato
