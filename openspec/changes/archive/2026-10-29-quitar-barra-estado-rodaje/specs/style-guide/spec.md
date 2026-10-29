# Spec Delta

## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Indicadores del móvil en la guía
**Reason**: El componente de indicadores del móvil se elimina del sistema de diseño.
**Migration**: Ninguna; se quita la sección de la guía generada.
