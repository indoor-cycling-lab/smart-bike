## ADDED Requirements

### Requirement: Ajuste «Idioma» en la maqueta de Ajustes
La sección «Pantalla» de la maqueta de Ajustes SHALL incluir, encima de «Tema», el ajuste «Idioma · Language» con las opciones «Español» y «English», cada una en su propio idioma, y SHALL tener un estado con «English» elegido. Las maquetas SHALL seguir en español: el estado solo muestra la opción elegida, no la interfaz traducida.

#### Scenario: Estado inicial
- **WHEN** se abre la maqueta de Ajustes en su estado inicial
- **THEN** la sección «Pantalla» muestra «Idioma · Language», encima de «Tema», con «Español» elegido

#### Scenario: Inglés elegido
- **WHEN** se abre el estado «Idioma inglés» de la maqueta de Ajustes
- **THEN** «English» está elegido y «Español» no, con la marca de selección

#### Scenario: Guía
- **WHEN** se abre la guía de estilo
- **THEN** el grupo de opciones usado por «Idioma» es el mismo que el de «Tema» y «Orientación», sin componente nuevo
