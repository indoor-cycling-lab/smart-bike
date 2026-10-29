## ADDED Requirements

### Requirement: Iconos en la pantalla de rodaje
Las pautas de iconografía SHALL permitir iconos en la pantalla de rodaje solo para el acceso a Ajustes y la barra de conexiones; la marcha, los datos y los avisos SHALL seguir sin iconos. SHALL documentar qué icono representa cada enlace y que el icono de Zwift es propio, no de Material, y SHALL NOT imitar el logotipo de Zwift.

#### Scenario: Consultar la iconografía
- **WHEN** alguien consulta las pautas de iconografía para añadir un elemento a Rodaje
- **THEN** lee que solo Ajustes y la barra de conexiones llevan iconos en Rodaje, y qué icono corresponde al Neo, al Square, a Zwift y al mando

#### Scenario: Licencia del icono propio
- **WHEN** alguien revisa el origen de los iconos del sprite
- **THEN** encuentra que todos son de Material Symbols salvo el de Zwift, dibujado para el proyecto

### Requirement: Barra con seis enlaces en la guía
La vista previa de la barra de conexiones en la guía SHALL mostrar sus estados con los enlaces actuales y, además, la barra con seis enlaces (con la banda de pulso y el ventilador, aún no disponibles en la app) a 360 dp, en la franja superior de Rodaje, para comprobar que cabe.

#### Scenario: Seis enlaces a 360 dp
- **WHEN** se abre la sección de la barra de conexiones en la guía
- **THEN** se ve la barra con seis iconos en una sola línea a 360 dp, a la derecha de su franja, en los dos temas

#### Scenario: Estados en la guía
- **WHEN** se abre la sección de la barra de conexiones en la guía
- **THEN** se ve cada icono conectado, esperando y sin conexión, en tema rodaje y en tema claro
