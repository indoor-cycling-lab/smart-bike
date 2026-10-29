# settings-screen Specification

## Purpose
Pantalla de Ajustes de la app: qué secciones tiene, cómo se llega a ella y cómo se guardan y aplican los ajustes. Empieza con la sección Rodillo y el ajuste «Bajadas».

## Requirements

### Requirement: Ajuste «Bajadas»
La sección Rodillo SHALL ofrecer el ajuste «Bajadas» con tres opciones excluyentes, cada una con una ayuda de una línea: «Sin protección», «La marcha no crea bajadas» y «Nunca empujar». La opción elegida SHALL distinguirse por algo más que el color.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** está elegida «La marcha no crea bajadas»

#### Scenario: Elegir otra opción
- **WHEN** se toca «Nunca empujar»
- **THEN** queda elegida «Nunca empujar» y las otras dos dejan de estarlo

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción, incluida su ayuda
- **THEN** se elige esa opción

### Requirement: Ajuste aplicado al momento
Al cambiar el ajuste «Bajadas», la app SHALL aplicarlo a la pendiente que envía al Neo sin reiniciar la sesión ni cambiar la marcha, y SHALL enviar la pendiente nueva si cambia.

#### Scenario: Cambiar de modo rodando
- **WHEN** se rueda sin Zwift en la marcha 5 con «Sin protección» (el Neo simula −7 %) y se elige «La marcha no crea bajadas»
- **THEN** el Neo pasa a simular 0 % y la marcha sigue siendo la 5

#### Scenario: Sin Neo conectado
- **WHEN** el Neo no está conectado y se cambia el ajuste
- **THEN** el ajuste queda elegido y el Neo recibe la pendiente con el modo nuevo en cuanto se conecta

### Requirement: Ajustes guardados
Los ajustes, también los de Marchas y los de «Al rodar», «Pantalla», «Ciclista», «Rodillo», y los de Conexión (spec `platform-settings`), SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

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
- **THEN** se conservan la transmisión, la asignación y los demás ajustes, y los datos son potencia y cadencia, los botones − y + están desactivados, el peso del ciclista es 75 kg

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

#### Scenario: Modo de conexión guardado
- **WHEN** se elige «Solo Bluetooth» en Conexión y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y la app se anuncia por Bluetooth desde el primer momento

#### Scenario: Mando desactivado guardado
- **WHEN** se abre la app con una configuración anterior que tenía «Mando de Zwift» desactivado
- **THEN** se conservan los demás ajustes salvo los de conexión, y el mando se publica si hay un Square configurado

#### Scenario: Configuración anterior al mando
- **WHEN** se abre la app con una configuración guardada antes de que existiera «Mando de Zwift»
- **THEN** se conservan los demás ajustes salvo los de conexión, y el mando se publica si hay un Square configurado

#### Scenario: Configuración con «Pendiente sin Zwift»
- **WHEN** se abre la app con una configuración anterior que tenía «Pendiente sin Zwift» en 3,0 %
- **THEN** se conservan los demás ajustes y, sin Zwift, el terreno es llano (0 %)

#### Scenario: Configuración ilegible
- **WHEN** lo guardado no se puede leer al abrir la app
- **THEN** la app empieza con los valores por defecto, con el tema «Oscuro» y la orientación «Según el móvil»

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

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, «Botones», con la transmisión de la asignación, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración»; y la sección «Depuración», con «Grabar sesiones», «Espacio libre mínimo» y la entrada «Sesiones grabadas». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible. Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Plataformas.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Botones», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista, Rodillo, Copia de seguridad y Depuración, sin sección «Zwift» y sin botón de volver

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
- **THEN** la sección «Copia de seguridad» va después de «Rodillo», con «Exportar configuración» e «Importar configuración», cada una con un objetivo táctil de al menos 48 dp, y solo la sección «Depuración» va después

### Requirement: Secciones de Ajustes
Tras las entradas y «Al rodar», Ajustes SHALL mostrar en este orden: la sección «Pantalla», con «Idioma», «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas»; y la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración». Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Conexión.

#### Scenario: Orden de las secciones
- **WHEN** se abre Ajustes y se desplaza hasta el final
- **THEN** las secciones aparecen en el orden indicado y la última es «Copia de seguridad»

#### Scenario: Orden de la sección Pantalla
- **WHEN** se mira la sección «Pantalla»
- **THEN** los ajustes aparecen en este orden: «Idioma · Language», «Tema» y «Orientación»

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

### Requirement: Ventilador en Ajustes
Ajustes SHALL añadir, después de «Zwift», una sección «Ventilador» solo con un ventilador configurado, con: el modo («Manual» y «Automático», y «Otro modo» si el ventilador está en otro); la velocidad manual, con un paso a paso − y + de 10 % entre 0 y 100 %; y el umbral de cada zona de pulso (1 a 4), con un paso a paso − y + de 1 bpm. En cada extremo, el botón de ese lado SHALL quedar deshabilitado. El modo elegido SHALL distinguirse por algo más que el color.

#### Scenario: Sin ventilador configurado
- **WHEN** no hay ventilador configurado y se abre Ajustes
- **THEN** no aparece la sección «Ventilador»

#### Scenario: Con ventilador
- **WHEN** hay un ventilador conectado en manual al 40 % con zonas hasta 120, 135, 150 y 165 bpm
- **THEN** la sección dice «Manual», «40 %» y los cuatro umbrales, y todo se puede cambiar

#### Scenario: Cambiar el modo
- **WHEN** se toca «Automático» con el ventilador en manual
- **THEN** el ventilador pasa a automático y la velocidad manual queda deshabilitada

#### Scenario: Velocidad en automático
- **WHEN** el ventilador está en automático
- **THEN** el paso a paso de la velocidad está deshabilitado y la sección explica que en automático la velocidad la decide el ventilador

#### Scenario: Cambiar un umbral en automático
- **WHEN** el ventilador está en automático y se sube el umbral de la zona 1 de 120 a 121 bpm
- **THEN** el ventilador usa 121 en esa zona sin salir del automático

#### Scenario: Extremo de la velocidad
- **WHEN** la velocidad manual es 100 %
- **THEN** el + está deshabilitado y el − no

#### Scenario: Límite entre zonas
- **WHEN** el umbral de la zona 2 es 135 y el de la zona 3 es 136
- **THEN** el + del umbral 2 está deshabilitado, y el − del umbral 3 también

#### Scenario: Ventilador sin conexión
- **WHEN** hay un ventilador configurado pero no conectado
- **THEN** la sección se ve con lo último que comunicó, todos sus controles deshabilitados y un texto que dice «Ventilador sin conexión», sin guardar nada

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** la sección se recorre desplazando la pantalla, sin recortes, y la barra inferior sigue visible al pie

### Requirement: Aviso de datos ocultos en pantalla baja
En una pantalla baja, la pantalla «Datos en pantalla» SHALL avisar, cuando los botones − y + estén activados y haya más de 4 datos elegidos, de que Rodaje solo muestra los 4 primeros. El aviso SHALL decir cuáles son los que no se muestran y no SHALL impedir elegir hasta 6 datos.

#### Scenario: Seis datos con botones en pantalla baja
- **WHEN** se abre «Datos en pantalla» en una pantalla baja con 6 datos elegidos y los botones − y + activados
- **THEN** un aviso dice que con los botones activados Rodaje solo muestra los 4 primeros datos, y nombra el quinto y el sexto como los que no se muestran

#### Scenario: Elegir el quinto dato
- **WHEN** en esa pantalla, con los botones activados, se marca un quinto dato
- **THEN** el aviso aparece y el contador dice «5 de 6»

#### Scenario: Sin botones
- **WHEN** en una pantalla baja hay 6 datos elegidos y los botones están desactivados
- **THEN** no hay aviso

#### Scenario: Pantalla alta
- **WHEN** en una pantalla de 760 dp de alto o más hay 6 datos elegidos y los botones activados
- **THEN** no hay aviso

### Requirement: Depuración en Ajustes
La sección «Depuración» de Ajustes SHALL mostrar el interruptor «Grabar sesiones», apagado por defecto, y debajo «Espacio libre mínimo» con un stepper de 5 % a 50 % de 5 en 5 (10 % por defecto) y su ayuda. Debajo SHALL mostrar el estado de la grabación («Grabando», «Detenida por poco espacio», «Detenida por un error» o nada si está apagada) y la entrada «Sesiones grabadas», que resume cuántas hay y cuánto ocupan y abre la pantalla del mismo nombre con un botón de volver a Ajustes.

#### Scenario: Valores por defecto
- **WHEN** se abre Ajustes por primera vez y se llega a «Depuración»
- **THEN** «Grabar sesiones» está apagado, «Espacio libre mínimo» dice 10 % y «Sesiones grabadas» dice que no hay ninguna

#### Scenario: Estado de la grabación
- **WHEN** hay una grabación en curso y se mira «Depuración»
- **THEN** dice «Grabando» y la entrada «Sesiones grabadas» cuenta la sesión en curso

#### Scenario: Entrada de Sesiones grabadas
- **WHEN** hay tres sesiones que ocupan 41 KB y se mira «Depuración»
- **THEN** la entrada «Sesiones grabadas» dice «3 sesiones · 41 KB» y al tocarla se abre la pantalla con un botón de volver a Ajustes

### Requirement: Ajustes de depuración guardados
«Grabar sesiones» y «Espacio libre mínimo» SHALL aplicarse al momento y guardarse con la configuración. Una configuración guardada antes de estos ajustes SHALL leerse con la grabación apagada y el mínimo en 10 %, conservando el resto.

#### Scenario: Activar y guardar
- **WHEN** se activa «Grabar sesiones», se sube el mínimo a 20 % y se reabre la app
- **THEN** «Grabar sesiones» sigue activado y el mínimo dice 20 %

#### Scenario: Configuración anterior
- **WHEN** se abre un `configuracion.json` guardado antes de estos ajustes
- **THEN** «Grabar sesiones» está apagado y «Espacio libre mínimo» dice 10 %, y el resto de los ajustes se conservan

### Requirement: Idioma en Ajustes
La sección «Pantalla» de Ajustes SHALL tener, encima de «Tema», el ajuste «Idioma» con dos opciones excluyentes, «Español» y «English», cada una escrita en su propio idioma sea cual sea el idioma de la app. El título del ajuste SHALL leerse «Idioma · Language» en los dos idiomas, para poder encontrarlo aunque la app esté en un idioma que no se lee. Elegir una opción SHALL aplicarse al momento (spec `app-language`).

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez con el móvil en español
- **THEN** la sección «Pantalla» muestra «Idioma · Language», encima de «Tema», con «Español» elegido

#### Scenario: Opciones en su propio idioma
- **WHEN** la app está en inglés y se mira «Idioma · Language»
- **THEN** las opciones dicen «Español» y «English», no «Spanish» y «English»

#### Scenario: Elegir otra opción
- **WHEN** se toca «English»
- **THEN** queda elegida «English» y «Español» deja de estarlo, y Ajustes pasa a inglés

#### Scenario: Opción elegida sin color
- **WHEN** se mira «Idioma · Language»
- **THEN** la opción elegida se distingue por la marca de selección, no solo por el color, y el lector de pantalla anuncia cuál está elegida

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción de idioma
- **THEN** se elige esa opción

### Requirement: Entrada «Guía de inicio» en Ajustes
Ajustes SHALL tener una entrada «Guía de inicio», con una ayuda de una línea, que vuelve a mostrar la guía de inicio, abierta en la página 1, y abre Rodaje. Tocar cualquier punto de la fila SHALL activarla y su objetivo táctil SHALL ser de al menos 48 dp.

#### Scenario: Ver la entrada
- **WHEN** se abre Ajustes
- **THEN** hay una entrada «Guía de inicio» con su ayuda de una línea

#### Scenario: Tocar la entrada
- **WHEN** se toca «Guía de inicio»
- **THEN** se abre Rodaje con la hoja abierta en la página 1, sin terminar la sesión ni cambiar la marcha

#### Scenario: Con la guía ya a la vista
- **WHEN** la guía está a la vista y se toca «Guía de inicio»
- **THEN** se abre Rodaje con la hoja abierta en la página 1, sin cambiar nada más

### Requirement: Casilla para cambiar de modo tocando
Ajustes SHALL ofrecer en «Al rodar» la casilla «Cambiar de modo tocando», independiente de «Botones − y + en pantalla», que activa el cambio de modo Libre o SIM ⇄ ERG manual tocando el indicador de Rodaje. Su ayuda SHALL explicar qué hace y que no cambia el modo durante un entrenamiento de la app. SHALL estar desactivada por defecto y las configuraciones guardadas antes del change SHALL quedar con ella desactivada. Su valor SHALL viajar en la copia de seguridad.

#### Scenario: Ayuda de la casilla
- **WHEN** se abre Ajustes y se lee la casilla «Cambiar de modo tocando»
- **THEN** su ayuda dice que se toca el indicador de modo para pasar de Libre o SIM a ERG manual y volver, y que no vale en un entrenamiento de la app

#### Scenario: Activar el cambio de modo
- **WHEN** se activa la casilla y se vuelve a Rodaje
- **THEN** el indicador de modo se puede tocar, sin que aparezcan los botones − y + si no estaban

#### Scenario: Independiente de los botones
- **WHEN** se activa «Botones − y + en pantalla» con «Cambiar de modo tocando» desactivada
- **THEN** Rodaje muestra los botones y el indicador de modo sigue siendo una etiqueta

#### Scenario: Desactivar rodando
- **WHEN** se desactiva la casilla con la sesión en marcha en ERG manual
- **THEN** el indicador deja de responder al toque, el Neo sigue en ERG manual y la sesión no se corta

#### Scenario: Configuración anterior
- **WHEN** se abre la app con una configuración guardada antes de este change, tuviera o no los botones activados
- **THEN** «Cambiar de modo tocando» está desactivada y el indicador es una etiqueta

#### Scenario: Copia de seguridad
- **WHEN** se exporta la configuración con la casilla activada y se importa
- **THEN** la casilla queda activada
