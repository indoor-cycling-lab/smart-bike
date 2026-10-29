## MODIFIED Requirements

### Requirement: Iconos en la pantalla de rodaje
Las pautas de iconografía SHALL permitir iconos en la pantalla de rodaje solo para el acceso a Ajustes, la barra de conexiones y los indicadores de estado del móvil (wifi y batería); la marcha, los datos y los avisos SHALL seguir sin iconos. SHALL documentar qué icono representa cada enlace y que el icono de Zwift es propio, no de Material, y SHALL NOT imitar el logotipo de Zwift.

#### Scenario: Consultar la iconografía
- **WHEN** alguien consulta las pautas de iconografía para añadir un elemento a Rodaje
- **THEN** lee que solo Ajustes, la barra de conexiones y el wifi y la batería del móvil llevan iconos en Rodaje, y qué icono corresponde al Neo, al Square, a Zwift y al mando

#### Scenario: Licencia del icono propio
- **WHEN** alguien revisa el origen de los iconos del sprite
- **THEN** encuentra que todos son de Material Symbols salvo el de Zwift, dibujado para el proyecto

## ADDED Requirements

### Requirement: Indicadores del móvil en la guía
La guía SHALL mostrar los indicadores de estado del móvil con sus estados (con y sin wifi; batería llena, media, baja y cargando; batería no disponible), una vez en tema rodaje y otra en tema claro, y en la franja superior de Rodaje a 360 dp con la barra de seis enlaces, para comprobar que caben juntos.

#### Scenario: Estados en la guía
- **WHEN** alguien abre la sección de los indicadores en la guía
- **THEN** ve cada estado del wifi y de la batería, en tema rodaje y en tema claro

#### Scenario: Caben con seis enlaces
- **WHEN** se ve la franja superior a 360 dp con los indicadores y la barra de seis enlaces
- **THEN** todo cabe en una sola línea, sin cortarse ni solaparse
