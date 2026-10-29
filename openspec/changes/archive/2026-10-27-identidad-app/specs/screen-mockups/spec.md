## ADDED Requirements

### Requirement: Maqueta de identidad
Las maquetas SHALL incluir una pantalla de identidad con el icono de la app en el launcher (con máscara circular y de cuadrado redondeado y en monocromo), la splash con su animación y su estado final, y el icono de Botones en la barra inferior, con un estado por cada caso, en los dos temas y enlazada desde el índice.

#### Scenario: Ver el icono en el launcher
- **WHEN** se abre la maqueta de identidad en el estado del icono
- **THEN** se ve el icono con las máscaras de Android y a 48 dp, en tema rodaje y en tema claro

#### Scenario: Ver la splash
- **WHEN** se abre la maqueta de identidad en el estado de la splash
- **THEN** se ve el piñón ámbar saltando de barra en barra y quedándose en la posición del icono, sobre fondo oscuro también en tema claro, y con movimiento reducido se ve solo el estado final

#### Scenario: Icono monocromo
- **WHEN** se abre la maqueta de identidad en el estado monocromo
- **THEN** el icono se ve en un solo color y se distingue cuál es el piñón activo

### Requirement: Icono de Botones en la barra inferior de las maquetas
Las maquetas con barra inferior SHALL mostrar la pestaña Botones con el icono propio de los cuatro símbolos, y el mando de Zwift SHALL conservar su icono en las maquetas donde aparece.

#### Scenario: Barra inferior de las maquetas
- **WHEN** se abre Dispositivos, Botones o Ajustes en la maqueta
- **THEN** la pestaña Botones lleva el icono de los cuatro símbolos y el mando de Zwift de Dispositivos, el suyo
