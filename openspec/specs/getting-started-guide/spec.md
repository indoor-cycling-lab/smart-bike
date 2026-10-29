# getting-started-guide Specification

## Purpose
Explica al usuario que abre la app por primera vez qué es, qué piezas intervienen y qué hace falta para empezar a rodar: una hoja inferior de cuatro páginas en Rodaje (qué es, los pasos para empezar, cómo puedes usar la app y cómo conectar con la app de ciclismo indoor, opcional), que se ve mientras falta algo, se puede minimizar y descartar y se puede recuperar desde Ajustes.

## Requirements

### Requirement: Páginas de la guía
La guía SHALL ser una hoja inferior de cuatro páginas por las que se avanza con «Siguiente» y se retrocede con «Atrás», también deslizando, con un indicador de la página actual («1 de 4»). La página 1 SHALL decir qué es la app y qué piezas intervienen, la página 2 los pasos para empezar, la página 3 cómo se puede usar la app (sus modos) y la página 4 cómo conectar la app con una app de ciclismo indoor. En la última página SHALL NOT haber «Siguiente».

#### Scenario: Avanzar y retroceder
- **WHEN** se toca «Siguiente» en la página 1 y después «Atrás»
- **THEN** se ve la página 2 y luego otra vez la 1, con el indicador de página al día

#### Scenario: Deslizar
- **WHEN** se desliza la hoja hacia un lado
- **THEN** cambia a la página contigua, y en la primera o la última no pasa nada

#### Scenario: Última página
- **WHEN** se está en la página 4
- **THEN** el único botón de navegación es «Atrás»: no hay «Siguiente» ni «Abrir Conexión»

### Requirement: Página 1, qué es
La página 1 SHALL decir en pocas líneas que la app conecta un rodillo, un controlador y una app de ciclismo indoor, qué es cada pieza con un ejemplo (p. ej. Tacx Neo, Elite Square, Zwift), y que la app simula las marchas y muestra los datos mientras se rueda. El controlador y la app SHALL llevar la marca «Opcional». La página SHALL NOT explicar los botones, las marchas ni cómo se comunican los equipos.

#### Scenario: Primer uso
- **WHEN** se abre la página 1
- **THEN** se lee «Rodillo», «Controlador» y «App de ciclismo indoor», cada uno con su ejemplo y una frase, los dos últimos como «Opcional»

#### Scenario: Otro modelo
- **WHEN** hay configurado un rodillo de otra marca que el Neo
- **THEN** la página 1 sigue diciendo el ejemplo genérico, porque explica el tipo de pieza y no el modelo

### Requirement: Página 2, pasos para empezar
La página 2 SHALL mostrar tres pasos en este orden, cada uno con un acceso directo (chevron) a su pantalla: «Empareja tus dispositivos» (Dispositivos), «Configura la conexión» (Conexión; opcional) y «Ajusta la app» (Ajustes: tipo de marcha, botones, pantalla…). Ningún paso pendiente SHALL mostrarse como un error.

#### Scenario: Primer uso
- **WHEN** se abre la página 2 sin ningún dispositivo configurado
- **THEN** el primer paso dice «Pendiente», sin color de alerta, con «Rodillo (necesario) y controlador (opcional)», y los otros dos no tienen estado

#### Scenario: Configurar la conexión
- **WHEN** se lee el segundo paso
- **THEN** lleva la marca «Opcional», no tiene estado y dice cómo se conecta la app de ciclismo indoor a Smart Bike y el modo de conexión que hay ahora («Ahora: Wifi + Bluetooth»)

#### Scenario: Ajustar la app
- **WHEN** se lee el tercer paso
- **THEN** no tiene estado, solo su chevron, y dice «Tipo de marcha, botones, pantalla…»

### Requirement: Estado de los pasos
Los pasos con estado SHALL decirlo con una palabra y una marca, no solo con el color, y la guía SHALL actualizarse sola cuando cambie el estado de un dispositivo o de la plataforma. «Empareja tus dispositivos» SHALL decir «Pendiente» (sin rodillo), «Conectando» (el rodillo, mientras la app lo intenta), «Falta el controlador» (rodillo conectado y sin controlador conectado) o «Hecho» (rodillo y controlador conectados).

#### Scenario: Dispositivo dormido al abrir
- **WHEN** se abre la app con el Neo configurado pero dormido
- **THEN** el primer paso dice «Conectando» y no «Pendiente»

#### Scenario: Todo conectado
- **WHEN** el Neo y el Square están conectados
- **THEN** el primer paso dice «Hecho» con los dos modelos reales («Tacx Neo 1 y Elite Square conectados»)

#### Scenario: Conexión perdida
- **WHEN** el rodillo y el controlador estaban conectados, la guía seguía a la vista y se pierde la conexión con el Neo
- **THEN** el primer paso vuelve a «Conectando» o «Pendiente», sin avisos nuevos

### Requirement: Estado de la conexión con la app
La página 4 SHALL decir el estado de la conexión con la app de ciclismo indoor: «Antes, empareja el rodillo» (sin rodillo configurado, no se publica nada), «Esperando a tu app» (el rodillo se ofrece y falta emparejarlo en la app) o «App conectada» (una plataforma conectada), con una marca además del color.

#### Scenario: Rodillo conectado
- **WHEN** el Neo se conecta con la página 2 a la vista
- **THEN** el primer paso pasa a «Falta el controlador» y la página 4 pasa a «Esperando a tu app», sin que el usuario toque nada

#### Scenario: Plataforma conectada
- **WHEN** una plataforma se conecta a la app
- **THEN** la página 4 pasa a «App conectada»

### Requirement: Página 3, cómo se puede usar la app
La página 3 SHALL decir en una frase qué es la app y explicar las formas de rodar con el nombre que muestra el indicador de modo de Rodaje: «LIBRE» (sin app de ciclismo indoor: el terreno es llano y la dureza la da la marcha virtual), «SIM» (con una app de ciclismo indoor que envía la pendiente de la ruta, a la que se suma la marcha) y «ERG» (potencia fija sea cual sea la cadencia), distinguiendo «ERG MANUAL», que fija el ciclista, de «ERG APP», que manda el entrenamiento de la app. Los nombres SHALL ser los del indicador, en el idioma de la app. La página SHALL NOT explicar los botones, las marchas ni cómo alternar entre modos.

#### Scenario: Llegar a los modos
- **WHEN** se avanza desde la página 2 con «Siguiente»
- **THEN** se ve la página 3, con «3 de 4», «Atrás», «Siguiente» y los modos «LIBRE», «SIM» y «ERG», cada uno con una frase

#### Scenario: Los dos ERG
- **WHEN** se lee el modo «ERG»
- **THEN** dice que mantiene la potencia fija sea cual sea la cadencia, que en «ERG MANUAL» la fija el ciclista y que en «ERG APP» la manda el entrenamiento de la app

#### Scenario: Sin app
- **WHEN** se lee el modo «LIBRE»
- **THEN** dice que es el modo de rodar sin app de ciclismo indoor, con terreno llano, y que la dureza se pone cambiando de marcha

#### Scenario: Mismos nombres que el indicador
- **WHEN** la app está en inglés o en español
- **THEN** los nombres de los modos de la página coinciden con los que muestra el indicador de modo de Rodaje en ese idioma

#### Scenario: Puente a la app
- **WHEN** se leen los modos «SIM» y «ERG APP»
- **THEN** dicen que necesitan una app de ciclismo indoor conectada y remiten a la página siguiente, mientras que «LIBRE» y «ERG MANUAL» dicen que no la necesitan

#### Scenario: Sin tutorial
- **WHEN** se lee la página 3
- **THEN** no nombra ningún botón del Square ni explica cómo pasar de un modo a otro

#### Scenario: Sin desplazamiento
- **WHEN** la hoja está abierta en la página 3 en vertical a 360 × 640 dp y en horizontal
- **THEN** la página se lee entera sin desplazarse, como las demás

### Requirement: Página 4, conectar con la app de ciclismo indoor
La página 4 SHALL llevar la marca «Opcional» y decir que solo hace falta para rodar en «SIM» o «ERG APP». SHALL decir el modo de conexión elegido en Conexión y los pasos para emparejar la app en la app de ciclismo indoor según ese modo, con los mismos textos de ayuda que Conexión. No SHALL llevar botones hacia otras pantallas ni permitir cambiar el modo: Conexión se abre desde el paso «Configura la conexión» de la página 2.

#### Scenario: Opcional
- **WHEN** se abre la página 4
- **THEN** lleva la marca «Opcional» y dice que solo hace falta para «SIM» y «ERG APP»; sin ella se puede rodar en «LIBRE» y «ERG MANUAL»

#### Scenario: Wifi + Bluetooth
- **WHEN** el modo es Wifi + Bluetooth
- **THEN** pide poner el móvil en la misma red wifi que el equipo de la app, elegir «Smart Bike» como potencia, cadencia y rodillo, y, con controlador, elegirlo en la lista de mandos sin emparejar el Square en la app

#### Scenario: Solo Bluetooth
- **WHEN** el modo es Solo Bluetooth
- **THEN** pide activar el Bluetooth del equipo, sin wifi, elegir el móvil con su nombre Bluetooth como rodillo, potencia y cadencia y, con controlador, elegirlo también como mando

#### Scenario: Solo wifi
- **WHEN** el modo es Solo wifi (modo Zwift Ride)
- **THEN** pide la misma red wifi, elegir «Smart Bike» como un solo dispositivo con rodillo, pulso y controlador, y avisa de que Zwift mostrará su propia marcha; y dice que es solo para Zwift

### Requirement: Adónde lleva cada acceso
Tocar un paso de la página 2 SHALL abrir la pantalla donde se resuelve: Dispositivos, Conexión o Ajustes. Abrirla SHALL NOT terminar la sesión ni cambiar la marcha, y volver con Atrás o con la barra SHALL dejar la guía como estaba.

#### Scenario: Dispositivos
- **WHEN** se toca «Empareja tus dispositivos»
- **THEN** se abre Dispositivos con la barra inferior y la sesión sigue en marcha

#### Scenario: Volver
- **WHEN** se conecta el Neo desde Dispositivos y se vuelve a Rodar
- **THEN** la guía sigue a la vista, en la página en la que estaba, con el paso actualizado

### Requirement: Cuándo se ve la guía
La guía SHALL verse en Rodaje mientras no esté descartada y falte algún paso con estado. SHALL abrirse sola, en la página 1, solo mientras no haya ningún dispositivo configurado; en los demás arranques SHALL aparecer minimizada. No SHALL verse en las demás pantallas ni a quien actualice la app con algún dispositivo ya configurado.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez
- **THEN** Rodaje muestra la hoja abierta en la página 1

#### Scenario: Siguiente arranque con algo configurado
- **WHEN** se abre la app con el Neo configurado y la guía sin descartar ni terminar
- **THEN** Rodaje muestra la guía minimizada

#### Scenario: Actualización con dispositivos
- **WHEN** se abre una versión con guía por primera vez con el Neo ya configurado
- **THEN** Rodaje no muestra la guía y no la vuelve a mostrar sola

#### Scenario: Otras pantallas
- **WHEN** se abre Ajustes, Dispositivos o Conexión con la guía a la vista en Rodaje
- **THEN** esas pantallas no la muestran

### Requirement: Minimizar la guía
La hoja SHALL tener un botón de minimizar que la convierte en una tira de una línea con «Para empezar» y, debajo, lo siguiente por hacer: «Empareja tus dispositivos» mientras falte el rodillo o el controlador y «Conecta tu app de ciclismo indoor» después. La tira SHALL ocupar el sitio del pie de Rodaje, sin contadores, y al tocarla SHALL abrir la hoja en la página en la que estaba. Minimizar SHALL NOT cambiar la marcha, el modo ni ninguna conexión.

#### Scenario: Minimizar
- **WHEN** se toca el botón de minimizar con la hoja en la página 2
- **THEN** queda la tira «Para empezar», con «Empareja tus dispositivos» o «Conecta tu app de ciclismo indoor» debajo, y la marcha no se mueve

#### Scenario: Reabrir
- **WHEN** se toca la tira
- **THEN** se abre la hoja en la página 2

#### Scenario: Con todo hecho
- **WHEN** los dispositivos están emparejados y hay una app conectada
- **THEN** no hay tira

#### Scenario: Aviso de acción
- **WHEN** se cambia de marcha con la tira a la vista y «Mostrar la acción» activado
- **THEN** el aviso de la acción ocupa el pie mientras dura y la tira vuelve después

### Requirement: Descartar la guía
La hoja y la tira SHALL ofrecer una equis que la oculta hasta que se vuelva a abrir la app, y la hoja un botón «No volver a mostrar» que la oculta y lo recuerda. Todos SHALL tener un objetivo táctil de al menos 48 dp y descartar la guía SHALL NOT cambiar la marcha, el modo ni ninguna conexión.

#### Scenario: Cerrar con la equis
- **WHEN** se toca la equis y se cierra y vuelve a abrir la app con algún paso aún pendiente
- **THEN** la guía vuelve a verse

#### Scenario: No volver a mostrar
- **WHEN** se toca «No volver a mostrar» y se cierra y vuelve a abrir la app
- **THEN** la guía no se ve

#### Scenario: Descarte rodando
- **WHEN** se descarta la guía con la sesión en marcha
- **THEN** la marcha, el modo y las conexiones siguen igual

### Requirement: Fin de la guía
La guía SHALL ocultarse sola, y no volver, cuando los dispositivos estén emparejados y haya una app conectada a la vez: rodillo y controlador conectados y una plataforma conectada. Los pasos sin estado de la página 2 no cuentan.

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square y una plataforma están conectados
- **THEN** la guía desaparece y no vuelve a verse al abrir la app otra vez

#### Scenario: Una plataforma que se va
- **WHEN** todo estaba conectado y después se pierde la plataforma
- **THEN** la guía no reaparece

### Requirement: Recuperar la guía
Ajustes SHALL tener una entrada «Guía de inicio» que vuelve a mostrar la guía abierta en la página 1, también si se había descartado o completada, y SHALL abrir Rodaje. La guía descartada SHALL NOT entrar en la exportación de la configuración.

#### Scenario: Reabrir
- **WHEN** se descartó la guía con «No volver a mostrar» y se toca «Guía de inicio» en Ajustes
- **THEN** se abre Rodaje con la hoja abierta en la página 1

#### Scenario: Otro móvil
- **WHEN** se exporta la configuración con la guía descartada y se importa en otro móvil sin dispositivos
- **THEN** en el otro móvil la guía se ve en el primer uso

#### Scenario: Reabrir con todo hecho
- **WHEN** los pasos están hechos y se toca «Guía de inicio»
- **THEN** se abre Rodaje con la hoja a la vista y los pasos hechos

### Requirement: Accesibilidad de la guía
La hoja SHALL ser un diálogo no modal cuyo nombre diga la página («Guía de inicio, página 1 de 4»), los pasos SHALL leerse como lista con su estado («Empareja tus dispositivos, hecho», «Configura la conexión, opcional. Modo Wifi + Bluetooth») y la tira SHALL leerse entera («Para empezar. Conecta tu app de ciclismo indoor»). Los textos SHALL tener el contraste de 4,5:1 en los dos temas y la guía SHALL ofrecer los mismos botones que los gestos.

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la página 2 con el Neo y el Square conectados y sin plataforma
- **THEN** dice «Empareja tus dispositivos, hecho» y «Configura la conexión, opcional. Modo Wifi + Bluetooth»

#### Scenario: Tema claro
- **WHEN** se ve la guía en el tema claro
- **THEN** sus textos y sus marcas pasan el contraste mínimo
