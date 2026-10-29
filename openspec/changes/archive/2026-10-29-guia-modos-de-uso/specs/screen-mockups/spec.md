## MODIFIED Requirements

### Requirement: Guía de inicio en las maquetas de rodaje
Las maquetas de rodaje, en vertical (390 × 844 y pantalla baja 360 × 640) y en horizontal, SHALL tener estados con la guía de inicio: las cuatro páginas de la hoja abierta, la página 2 con el rodillo conectando, con el rodillo conectado y con el rodillo y el controlador conectados, la página 4 en cada modo de conexión, con la marca «Opcional», la tira minimizada y el estado sin guía con todo hecho. Todos SHALL verse en los dos temas.

#### Scenario: Página 1
- **WHEN** se abre el estado de la página 1
- **THEN** la hoja muestra «Smart Bike», las tres piezas con su ejemplo, «Opcional» en el controlador y la app, el indicador «1 de 4», «Siguiente», minimizar, la equis y «No volver a mostrar»

#### Scenario: Página 2
- **WHEN** se abre el estado de la página 2 sin dispositivos
- **THEN** el paso «Empareja tus dispositivos» dice «Pendiente», «Configura la conexión» (opcional) y «Ajusta la app» no tienen estado, ninguno usa un color de alerta, y el indicador dice «2 de 4» con «Atrás» y «Siguiente»

#### Scenario: Estados de los pasos
- **WHEN** se abren los estados con el rodillo conectando, el rodillo conectado y el rodillo y el controlador conectados
- **THEN** el primer paso dice «Conectando», «Falta el controlador» y «Hecho»

#### Scenario: Página 3
- **WHEN** se abre el estado de la página 3
- **THEN** la hoja muestra los modos «LIBRE», «SIM» y «ERG» (con «ERG MANUAL» y «ERG APP»), cada uno con su frase y la indicación de cuáles necesitan una app conectada, el indicador «3 de 4», «Atrás» y «Siguiente»

#### Scenario: Página 4
- **WHEN** se abre la página 4 en cada modo
- **THEN** lleva la marca «Opcional», dice el modo, el estado de la conexión con la app y sus tres pasos, con «Atrás» como único botón de navegación

#### Scenario: Minimizada
- **WHEN** se abre el estado minimizado
- **THEN** la tira ocupa el pie con «Para empezar» y lo siguiente por hacer, sin contadores

#### Scenario: Todo hecho
- **WHEN** se abre el estado con todo hecho
- **THEN** Rodaje no muestra la guía

#### Scenario: Pantalla estrecha
- **WHEN** se abre un estado con la hoja en vertical a 360 × 640 dp y en horizontal a 640 × 360 dp
- **THEN** la hoja se ve entera, sin desplazamiento

## ADDED Requirements

### Requirement: Página de modos de uso en las maquetas
Las maquetas de rodaje, en vertical (390 × 844 y 360 × 640) y en horizontal, SHALL tener el estado de la página 3 de la guía («Cómo puedes usar la app») con los modos «LIBRE», «SIM» y «ERG» tal como los pinta el indicador de modo, en los dos temas, y SHALL abrirse por su `#estado=…` y estar en el índice de maquetas. La página SHALL verse entera sin desplazamiento.

#### Scenario: Página 3 en pantalla baja
- **WHEN** se abre el estado de la página 3 en vertical a 360 × 640 dp
- **THEN** se leen los tres modos completos, el indicador «3 de 4», «Atrás» y «Siguiente», sin desplazamiento

#### Scenario: Página 3 en horizontal
- **WHEN** se abre el estado de la página 3 en horizontal a 640 × 360 dp
- **THEN** se leen los tres modos completos sin desplazamiento

#### Scenario: Coincide con el indicador
- **WHEN** se compara la página 3 con los estados Libre, SIM con app y ERG de las maquetas de rodaje
- **THEN** los nombres de los modos son los mismos
