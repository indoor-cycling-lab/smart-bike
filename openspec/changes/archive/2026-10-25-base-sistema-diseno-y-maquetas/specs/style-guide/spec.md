# Spec Delta

## Purpose

Ofrece una guía de estilo navegable, generada a partir de los tokens y de los componentes, que documenta el sistema de diseño en los dos temas y sirve de referencia para construir maquetas y pantallas.

## ADDED Requirements

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
