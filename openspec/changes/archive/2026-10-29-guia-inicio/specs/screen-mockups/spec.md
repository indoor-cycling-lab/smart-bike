## ADDED Requirements

### Requirement: Guía de inicio en las maquetas de rodaje
Las maquetas de rodaje, en vertical (390 × 844 y pantalla baja 360 × 640) y en horizontal, SHALL tener estados con la guía de inicio: las tres páginas de la hoja abierta, la página 2 con el rodillo conectando, con el rodillo conectado y con el rodillo y el controlador conectados, la página 3 en cada modo de conexión, la tira minimizada y el estado sin guía con todo hecho. Todos SHALL verse en los dos temas.

#### Scenario: Página 1
- **WHEN** se abre el estado de la página 1
- **THEN** la hoja muestra «Smart Bike», las tres piezas con su ejemplo, «Opcional» en el controlador y la app, el indicador «1 de 3», «Siguiente», minimizar, la equis y «No volver a mostrar»

#### Scenario: Página 2
- **WHEN** se abre el estado de la página 2 sin dispositivos
- **THEN** el paso «Empareja tus dispositivos» dice «Pendiente», «Configura la conexión» (opcional) y «Ajusta la app» no tienen estado, y ninguno usa un color de alerta

#### Scenario: Estados de los pasos
- **WHEN** se abren los estados con el rodillo conectando, el rodillo conectado y el rodillo y el controlador conectados
- **THEN** el primer paso dice «Conectando», «Falta el controlador» y «Hecho»

#### Scenario: Página 3
- **WHEN** se abre la página 3 en cada modo
- **THEN** dice el modo, el estado de la conexión con la app y sus tres pasos, con «Atrás» como único botón de navegación

#### Scenario: Minimizada
- **WHEN** se abre el estado minimizado
- **THEN** la tira ocupa el pie con «Para empezar» y lo siguiente por hacer, sin contadores

#### Scenario: Todo hecho
- **WHEN** se abre el estado con todo hecho
- **THEN** Rodaje no muestra la guía

#### Scenario: Pantalla estrecha
- **WHEN** se abre un estado con la hoja en vertical a 360 × 640 dp y en horizontal a 640 × 360 dp
- **THEN** la hoja se ve entera, sin desplazamiento

### Requirement: Guía de inicio en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar la entrada «Guía de inicio» con su ayuda de una línea y un enlace a la maqueta de Rodaje con la hoja abierta en la página 1.

#### Scenario: Entrada
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** se ve la entrada «Guía de inicio» y, al tocarla, se abre la maqueta de Rodaje con la guía en la página 1
