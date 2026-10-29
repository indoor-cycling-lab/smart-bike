# Spec Delta

## MODIFIED Requirements

### Requirement: Navegación inferior
La navegación inferior SHALL ofrecer las pestañas Rodar, Dispositivos, Plataformas y Ajustes con icono y texto, marcar la activa por color y por estado accesible, y SHALL NOT aparecer en la pantalla de rodaje ni en pantallas secundarias.

#### Scenario: Pestaña activa
- **WHEN** se está en Ajustes
- **THEN** la pestaña Ajustes aparece en el color de marca y se anuncia como página actual

### Requirement: Icono de cada enlace
Cada enlace SHALL tener un icono propio, el mismo en Rodaje, Dispositivos y Diagnóstico: el Neo, un rayo; el Square, un mando de consola (el Square es el mando de la app); y el pulsómetro y el ventilador, los suyos. Zwift tiene una «Z» en un recuadro redondeado, que SHALL NOT imitar el logotipo de Zwift, y se usa en Plataformas y Diagnóstico, no en la barra de Rodaje. Dispositivos y Diagnóstico SHALL mostrar el icono junto al nombre de cada enlace, de modo que sirvan de leyenda para la barra de Rodaje.

#### Scenario: Leyenda en Dispositivos
- **WHEN** se abre Dispositivos
- **THEN** las filas del Neo y del Square llevan delante el mismo icono que en la barra de Rodaje

#### Scenario: Diagnóstico coherente
- **WHEN** se abre Diagnóstico
- **THEN** las filas de Conexiones llevan los mismos iconos que en Dispositivos, sin que ninguno represente un enlace distinto en otra pantalla

## ADDED Requirements

### Requirement: Icono de la entrada Botones
La entrada «Botones» de Ajustes SHALL usar un icono propio, formado por un triángulo, un cuadrado relleno, un círculo y una cruz en cuadrícula 2×2, distinto del icono del mando de Zwift de la barra de conexiones. El icono SHALL ser de un solo color, tomar el del texto de su fila y reconocerse a 24 dp en los dos temas.

#### Scenario: Entrada de Botones en Ajustes
- **WHEN** se abre Ajustes
- **THEN** la entrada «Botones» muestra el icono de los cuatro símbolos y no el del mando de consola

#### Scenario: Botones y mando de Zwift distintos
- **WHEN** se ven a la vez la entrada «Botones» de Ajustes y el mando de Zwift de la barra de conexiones de Rodaje
- **THEN** los dos iconos son distintos

#### Scenario: Entrada en los dos temas
- **WHEN** Ajustes se ve en tema rodaje y en tema claro
- **THEN** el icono se ve en el color del texto de la entrada, con su contraste

### Requirement: Icono de Plataformas
La pestaña Plataformas de la navegación inferior SHALL usar un icono propio que sugiera emitir o publicar una señal, de un solo color, que tome el de la pestaña (activa o no), distinto del icono de Bluetooth de Dispositivos, del de wifi y del del mando de Zwift, y reconocible a 24 dp en los dos temas.

#### Scenario: Pestaña Plataformas
- **WHEN** se abre cualquier pantalla con la barra inferior
- **THEN** la pestaña Plataformas muestra su icono propio, distinto del de Dispositivos

#### Scenario: Pestaña activa en los dos temas
- **WHEN** la pestaña Plataformas está activa en tema rodaje y en tema claro
- **THEN** el icono se ve en el color de marca de la pestaña, con el contraste del texto de la pestaña

### Requirement: Estado de la plataforma
El estado de la plataforma en Rodaje SHALL ser un chip de una línea con el nombre de la plataforma y su estado en palabra y forma (punto lleno conectado, aro que gira esperando en tono atenuado, aro vacío con borde de alerta no visible o sin wifi), a la izquierda de la fila de Ajustes, con un objetivo táctil de al menos 48 dp de alto sin alargar la línea. La guía SHALL mostrar todos sus estados en los dos temas.

#### Scenario: Estados sin color
- **WHEN** el estado se ve en escala de grises
- **THEN** conectado, esperando y no visible se distinguen por la forma y por la palabra

#### Scenario: Contraste en los dos temas
- **WHEN** se ve el estado en tema rodaje y en tema claro
- **THEN** el texto se lee con al menos 4,5:1 y el borde de alerta con al menos 3:1

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al estado con Zwift conectado y el mando esperando
- **THEN** lo lee como un solo elemento: «Plataforma: Zwift, conectado; mando, esperando»

### Requirement: Selector de plataforma y de modo
El sistema de diseño SHALL ofrecer un selector de plataforma y una lista de modos de conexión de elección única, cada modo con su nombre, su ayuda, la marca de recomendado y la de elegido, esta última por algo más que el color. La guía SHALL mostrarlos con una sola plataforma y con varias, y con modos sin elegir, elegido y recomendado.

#### Scenario: Una sola plataforma
- **WHEN** hay una única plataforma implementada
- **THEN** el selector muestra esa plataforma elegida y ninguna otra

#### Scenario: Modo elegido
- **WHEN** un modo está elegido
- **THEN** se distingue por la marca de selección además del color, con el contraste de texto de 4,5:1 en los dos temas, y su fila tiene al menos 48 dp de alto

#### Scenario: Modo recomendado
- **WHEN** un modo es el recomendado y no está elegido
- **THEN** lleva la marca «Recomendado» en texto

## REMOVED Requirements

### Requirement: Icono propio de Botones
**Reason**: Botones deja de ser una pestaña de la navegación inferior; el icono propio pasa a la entrada de Ajustes.
**Migration**: Esta misma spec, «Icono de la entrada Botones».
