# Spec Delta

## ADDED Requirements

### Requirement: Modelo de resistencia en Ajustes
La sección «Rodillo» SHALL tener «Modelo de resistencia» con tres opciones excluyentes, «Simple», «Por tabla» y «Físico», cada una con una ayuda de una línea. La opción elegida SHALL distinguirse por algo más que el color, y el cambio SHALL aplicarse al momento.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** está elegido «Simple»

#### Scenario: Elegir otro modelo
- **WHEN** se toca «Físico»
- **THEN** queda elegido «Físico», las otras dos dejan de estarlo y el Neo recibe la potencia del modelo

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción, incluida su ayuda
- **THEN** se elige esa opción

### Requirement: Dificultad en Ajustes
La sección «Rodillo» SHALL tener «Dificultad», con un paso a paso − y + de 5 % entre 50 % y 150 % y 100 % por defecto. En cada extremo, el botón de ese lado SHALL quedar deshabilitado. El cambio SHALL aplicarse al momento y enviar al Neo la resistencia nueva si cambia.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Dificultad» dice «100 %»

#### Scenario: Cambiar rodando
- **WHEN** se rueda sin Zwift con «Simple» en la marcha 17 y se sube «Dificultad» a 110 %
- **THEN** el Neo pasa a simular +5,5 % y la marcha sigue siendo la 17

#### Scenario: Extremo
- **WHEN** «Dificultad» es 150 %
- **THEN** el + está deshabilitado y el − no

### Requirement: Potencia mínima en Ajustes
Con «Físico» elegido, la sección «Rodillo» SHALL tener «Potencia mínima», con la ayuda «Del modelo físico, en bajadas y al parar», y un paso a paso − y + de 5 W entre 0 W y 150 W, 30 W por defecto. Con otro modelo, el ajuste SHALL NOT aparecer. En cada extremo, el botón de ese lado SHALL quedar deshabilitado.

#### Scenario: Con el físico
- **WHEN** se elige «Físico»
- **THEN** aparece «Potencia mínima» con «30 W»

#### Scenario: Con otro modelo
- **WHEN** el modelo es «Simple»
- **THEN** «Potencia mínima» no aparece y su valor guardado se conserva

#### Scenario: Cambiar rodando
- **WHEN** se baja en una bajada con el físico y se sube la potencia mínima a 50 W
- **THEN** el Neo recibe 50 W al momento

#### Scenario: Extremo
- **WHEN** «Potencia mínima» es 0 W
- **THEN** el − está deshabilitado y el + no

### Requirement: Peso de la bici
La sección «Ciclista» SHALL tener «Peso de la bici», con un paso a paso − y + de 0,5 kg entre 5 kg y 25 kg y 8 kg por defecto, que usa el modelo físico. En cada extremo, el botón de ese lado SHALL quedar deshabilitado.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** el peso de la bici dice «8,0 kg»

#### Scenario: Cambiar el peso
- **WHEN** se toca + con 8,0 kg y se rueda con el físico
- **THEN** el peso pasa a 8,5 kg y el modelo lo usa en el siguiente cálculo

#### Scenario: Extremo
- **WHEN** el peso de la bici es 5,0 kg
- **THEN** el − está deshabilitado y el + no

### Requirement: Bajadas solo con pendiente
Con «Físico» elegido, la sección «Rodillo» SHALL NOT mostrar «Bajadas», porque el modelo físico fija una potencia y no una pendiente; su protección es la potencia mínima. Con «Simple» o «Por tabla» SHALL mostrarse, y la opción elegida SHALL conservarse al cambiar de modelo.

#### Scenario: Con el físico
- **WHEN** se elige «Físico»
- **THEN** «Bajadas» no aparece y sí «Potencia mínima»

#### Scenario: Volver a un modelo de pendiente
- **WHEN** se tenía «Nunca empujar», se pasa a «Físico» y se vuelve a «Simple»
- **THEN** «Bajadas» reaparece con «Nunca empujar» elegido

### Requirement: Pantalla Tabla de marchas
Con «Por tabla» elegido, la sección «Rodillo» SHALL tener la entrada «Tabla de marchas», que abre su pantalla con volver a Ajustes. La pantalla SHALL listar, de la más ligera a la más dura, todas las marchas de la transmisión elegida, cada una con su nombre como en Rodaje y su ajuste de pendiente, con un paso a paso − y + de 0,1 % entre −20,0 % y +20,0 %. SHALL ofrecer «Restablecer» para volver al reparto del modelo simple. Los cambios SHALL aplicarse al momento.

#### Scenario: Entrada
- **WHEN** el modelo es «Por tabla»
- **THEN** «Tabla de marchas» aparece bajo el modelo, con la transmisión elegida en su ayuda

#### Scenario: Con otro modelo
- **WHEN** el modelo es «Físico»
- **THEN** «Tabla de marchas» no aparece y las tablas editadas se conservan

#### Scenario: Marchas de la lineal
- **WHEN** se abre la tabla con la transmisión lineal
- **THEN** hay 24 filas, de la 1 a la 24, con −11,0 % en la 1 y +12,0 % en la 24 si no se ha editado

#### Scenario: Marchas de la Shimano
- **WHEN** se abre la tabla con la Shimano 105
- **THEN** hay una fila por combinación de plato y piñón, ordenadas por su relación, con su nombre «36×17»

#### Scenario: Cambiar una marcha rodando
- **WHEN** se rueda en la marcha 17 de la lineal, sin Zwift, y se sube su ajuste de +5,0 % a +5,1 %
- **THEN** el Neo pasa a simular +5,1 % y la marcha sigue siendo la 17

#### Scenario: Extremo
- **WHEN** el ajuste de una marcha es +20,0 %
- **THEN** su + está deshabilitado y su − no

#### Scenario: Restablecer
- **WHEN** se editan varias marchas y se toca «Restablecer»
- **THEN** todas las filas vuelven al reparto del modelo simple de esa transmisión y las tablas de otras transmisiones no cambian

#### Scenario: Marcha puesta
- **WHEN** se abre la tabla con la marcha 17 puesta
- **THEN** la fila de la 17 está marcada y a la vista, distinguida por algo más que el color

## MODIFIED Requirements

### Requirement: Pantalla Datos en pantalla
Al tocar «Datos en pantalla», la app SHALL abrir su pantalla, con volver a Ajustes: una vista previa de Rodaje con los datos elegidos, el contador «N de 6» y la lista de los ocho datos (potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad y distancia), cada uno con su casilla, su ayuda y un asa para ordenarlo. Se SHALL poder marcar como máximo seis. Los cambios SHALL aplicarse al momento.

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
- **WHEN** se marcan seis de los ocho datos
- **THEN** el contador dice «6 de 6», la vista previa los muestra todos sin desplazamiento y las casillas de los dos sin marcar quedan deshabilitadas, con la ayuda «Quita uno para añadir otro»

#### Scenario: Velocidad y distancia
- **WHEN** se marcan «Velocidad» y «Distancia»
- **THEN** su ayuda dice que la velocidad la calcula el modelo físico y que con otro modelo se muestra «—»

#### Scenario: Al menos uno
- **WHEN** solo queda marcada la potencia
- **THEN** su casilla no se puede desmarcar, y la ayuda dice que hace falta al menos un dato

#### Scenario: En ERG
- **WHEN** se abre Datos en pantalla
- **THEN** una nota dice que en ERG la potencia objetivo se muestra siempre y no cuenta como dato
