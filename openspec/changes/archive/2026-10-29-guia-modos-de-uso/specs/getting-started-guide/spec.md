## RENAMED Requirements

- FROM: `### Requirement: Página 3, conectar con la app de ciclismo indoor`
- TO: `### Requirement: Página 4, conectar con la app de ciclismo indoor`

## MODIFIED Requirements

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

### Requirement: Estado de la conexión con la app
La página 4 SHALL decir el estado de la conexión con la app de ciclismo indoor: «Antes, empareja el rodillo» (sin rodillo configurado, no se publica nada), «Esperando a tu app» (el rodillo se ofrece y falta emparejarlo en la app) o «App conectada» (una plataforma conectada), con una marca además del color.

#### Scenario: Rodillo conectado
- **WHEN** el Neo se conecta con la página 2 a la vista
- **THEN** el primer paso pasa a «Falta el controlador» y la página 4 pasa a «Esperando a tu app», sin que el usuario toque nada

#### Scenario: Plataforma conectada
- **WHEN** una plataforma se conecta a la app
- **THEN** la página 4 pasa a «App conectada»

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

### Requirement: Accesibilidad de la guía
La hoja SHALL ser un diálogo no modal cuyo nombre diga la página («Guía de inicio, página 1 de 4»), los pasos SHALL leerse como lista con su estado («Empareja tus dispositivos, hecho», «Configura la conexión, opcional. Modo Wifi + Bluetooth») y la tira SHALL leerse entera («Para empezar. Conecta tu app de ciclismo indoor»). Los textos SHALL tener el contraste de 4,5:1 en los dos temas y la guía SHALL ofrecer los mismos botones que los gestos.

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la página 2 con el Neo y el Square conectados y sin plataforma
- **THEN** dice «Empareja tus dispositivos, hecho» y «Configura la conexión, opcional. Modo Wifi + Bluetooth»

#### Scenario: Tema claro
- **WHEN** se ve la guía en el tema claro
- **THEN** sus textos y sus marcas pasan el contraste mínimo

## ADDED Requirements

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
