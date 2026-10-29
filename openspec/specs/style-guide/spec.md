# style-guide Specification

## Purpose

Ofrece una guía de estilo navegable, generada a partir de los tokens y de los componentes, que documenta el sistema de diseño en los dos temas y sirve de referencia para construir maquetas y pantallas.

## Requirements

### Requirement: Catálogo de tokens
La guía SHALL mostrar cada token de color con su nombre, su muestra y su uso, y cada estilo tipográfico con su nombre, tamaño, ejemplo y uso.

#### Scenario: Consultar un color
- **WHEN** alguien consulta la sección de color de la guía
- **THEN** ve `gear` con su muestra y la descripción «Color de la marcha…»

### Requirement: Catálogo de componentes en los dos temas
La guía SHALL mostrar cada componente del sistema de diseño, agrupado por zona (rodaje, ajustes, pantallas, Square, diagnóstico, patrones), con sus variantes y estados, una vez en tema rodaje y otra en tema claro, y con un enlace a sus pautas.

#### Scenario: Ver la marcha
- **WHEN** alguien consulta el componente de marcha
- **THEN** ve las variantes física (`50×17`), lineal y en tope, en rodaje y en claro, y un enlace a sus pautas

#### Scenario: Componente nuevo
- **WHEN** se añade un componente con su vista previa y sus pautas y se regenera la guía
- **THEN** el componente aparece en la guía sin editarla a mano

### Requirement: Guía generada, no editada
La guía y la hoja de tokens derivada SHALL generarse a partir de la fuente de tokens y de las carpetas de componentes con un único comando, y SHALL indicar que no se editan a mano.

#### Scenario: Cambio de un uso de color
- **WHEN** se cambia la descripción de uso de un token en la fuente y se ejecuta el generador
- **THEN** la guía muestra la nueva descripción

### Requirement: Guía de uso para maquetas
El sistema de diseño SHALL documentar sus principios, voz, color, tipografía, espaciado, movimiento e iconografía, y qué ficheros incluir y qué clases usar para construir una maqueta sin estilos propios.

#### Scenario: Crear una maqueta nueva
- **WHEN** alguien sigue la documentación para crear una pantalla de prueba
- **THEN** la pantalla se ve con el aspecto del sistema enlazando solo los ficheros del sistema de diseño

### Requirement: Guía usable en el móvil y sin conexión
La guía SHALL leerse en un móvil sin desplazamiento horizontal de la página (los ejemplos anchos se desplazan dentro de su caja) y SHALL funcionar servida en local sin conexión a internet.

#### Scenario: Ejemplo ancho en el móvil
- **WHEN** se abre la composición de la pantalla de rodaje (960 dp) en un móvil de 390 dp
- **THEN** la página no se desplaza en horizontal y el ejemplo se desplaza dentro de su caja

### Requirement: Iconos en la pantalla de rodaje
Las pautas de iconografía SHALL permitir iconos en la pantalla de rodaje solo para el acceso a Ajustes y la barra de conexiones; la marcha, los datos y los avisos SHALL seguir sin iconos, y la hora, el wifi y la batería del móvil los pinta el sistema. SHALL documentar qué icono representa cada enlace y que los iconos de Zwift y del manillar son propios, no de Material, y SHALL NOT imitar el logotipo de Zwift.

#### Scenario: Consultar la iconografía
- **WHEN** alguien consulta las pautas de iconografía para añadir un elemento a Rodaje
- **THEN** lee que solo Ajustes y la barra de conexiones llevan iconos en Rodaje, y qué icono corresponde al Neo, al Square (el manillar), a Zwift y al controlador publicado (el de Zwift)

#### Scenario: Licencia del icono propio
- **WHEN** alguien revisa el origen de los iconos del sprite
- **THEN** encuentra que todos son de Material Symbols salvo el de Zwift y el manillar, dibujados para el proyecto

### Requirement: Barra con seis enlaces en la guía
La vista previa de la barra de conexiones en la guía SHALL mostrar sus estados con los enlaces actuales y, además, la barra con seis enlaces (con la banda de pulso y el ventilador, aún no disponibles en la app) a 360 dp, en la fila de la plataforma de Rodaje, para comprobar que cabe, con los iconos agrupados a 4 dp.

#### Scenario: Seis enlaces a 360 dp
- **WHEN** se abre la sección de la barra de conexiones en la guía
- **THEN** se ve la barra con seis iconos en una sola línea a 360 dp, a la derecha de la fila, en los dos temas

#### Scenario: Estados en la guía
- **WHEN** se abre la sección de la barra de conexiones en la guía
- **THEN** se ve cada icono conectado, esperando y sin conexión, en tema rodaje y en tema claro

#### Scenario: Estados mezclados en la guía
- **WHEN** se abre la sección de la barra de conexiones en la guía
- **THEN** se ve la barra con un enlace esperando junto a uno sin conexión, sin que su marca y su tachado se toquen

### Requirement: Identidad de la app en la guía
La guía de estilo SHALL mostrar el icono de la app con las máscaras de Android (círculo y cuadrado redondeado), a 48 dp y en monocromo, la animación de la splash con su estado final, y el icono de Botones junto al resto de iconos en los dos temas, con sus pautas de uso.

#### Scenario: Consultar el icono de la app
- **WHEN** alguien abre la guía y busca el icono de la app
- **THEN** lo ve con máscara circular y de cuadrado redondeado, a 48 dp y en monocromo, con la rueda trasera distinguible en todas

#### Scenario: Consultar la splash
- **WHEN** alguien abre la sección de la splash en la guía
- **THEN** ve la animación de la rueda trasera girando cada vez más rápido y el estado final igual al icono, y con movimiento reducido ve solo el estado final

#### Scenario: Icono de Botones en los dos temas
- **WHEN** alguien consulta la iconografía en tema rodaje y en tema claro
- **THEN** ve el icono de Botones junto al manillar del controlador y se distinguen

### Requirement: Manillar legible en pequeño
La guía SHALL mostrar el icono del manillar a 24 dp y a 14 dp, en los estados de la barra de conexiones (conectado, esperando, sin conexión y batería baja) y en los dos temas, con la pauta de que el manillar es el icono del Square y no se usa para otro enlace.

#### Scenario: Manillar en los estados de la barra
- **WHEN** alguien consulta la iconografía en tema rodaje y en tema claro
- **THEN** ve el manillar a 24 dp y a 14 dp, normal, atenuado con el hueco y la marca, tachado y con la insignia de batería, y se reconoce en todos
