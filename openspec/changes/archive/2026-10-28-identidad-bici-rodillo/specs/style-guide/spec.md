## MODIFIED Requirements

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
- **THEN** ve el icono de Botones junto al mando de Zwift y se distinguen
