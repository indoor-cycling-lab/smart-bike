## MODIFIED Requirements

### Requirement: Iconos en la pantalla de rodaje
Las pautas de iconografía SHALL permitir iconos en la pantalla de rodaje solo para el acceso a Ajustes, la barra de conexiones y los indicadores de estado del móvil (wifi y batería); la marcha, los datos y los avisos SHALL seguir sin iconos. SHALL documentar qué icono representa cada enlace y que los iconos de Zwift y del manillar son propios, no de Material, y SHALL NOT imitar el logotipo de Zwift.

#### Scenario: Consultar la iconografía
- **WHEN** alguien consulta las pautas de iconografía para añadir un elemento a Rodaje
- **THEN** lee que solo Ajustes, la barra de conexiones y el wifi y la batería del móvil llevan iconos en Rodaje, y qué icono corresponde al Neo, al Square (el manillar), a Zwift y al controlador publicado (el de Zwift)

#### Scenario: Licencia del icono propio
- **WHEN** alguien revisa el origen de los iconos del sprite
- **THEN** encuentra que todos son de Material Symbols salvo el de Zwift y el manillar, dibujados para el proyecto

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

## ADDED Requirements

### Requirement: Manillar legible en pequeño
La guía SHALL mostrar el icono del manillar a 24 dp y a 14 dp, en los estados de la barra de conexiones (conectado, esperando, sin conexión y batería baja) y en los dos temas, con la pauta de que el manillar es el icono del Square y no se usa para otro enlace.

#### Scenario: Manillar en los estados de la barra
- **WHEN** alguien consulta la iconografía en tema rodaje y en tema claro
- **THEN** ve el manillar a 24 dp y a 14 dp, normal, atenuado con el hueco y la marca, tachado y con la insignia de batería, y se reconoce en todos
