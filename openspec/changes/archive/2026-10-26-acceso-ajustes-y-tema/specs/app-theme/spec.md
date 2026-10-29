## MODIFIED Requirements

### Requirement: Tema rodaje por defecto y tema claro por contenedor
La app SHALL mostrarse entera en el tema elegido en Ajustes («Oscuro» es el tema rodaje y «Claro», el claro), y en tema rodaje cuando no se ha elegido ninguno. Las barras del sistema SHALL llevar iconos legibles sobre el fondo del tema. La app SHALL poder aplicar el tema claro a una parte de una pantalla sin afectar al resto.

#### Scenario: Arranque sin preferencia
- **WHEN** se abre la app sin haber elegido tema
- **THEN** el fondo es `bg` de rodaje (`#0f1216`) y el texto principal es `ink` de rodaje

#### Scenario: Tema claro elegido
- **WHEN** el tema elegido es «Claro» y se abre cualquier pantalla de la app, Rodaje incluida
- **THEN** el fondo es `bg` de claro y cada elemento usa el valor claro de su token, el mismo que la guía de estilo muestra en tema claro

#### Scenario: Barras del sistema en claro
- **WHEN** el tema elegido es «Claro» y se abre Ajustes
- **THEN** la hora, la batería y los botones de navegación del sistema se ven oscuros sobre el fondo claro

#### Scenario: Barras del sistema en oscuro
- **WHEN** el tema elegido es «Oscuro» y se abre Ajustes
- **THEN** los iconos de las barras del sistema se ven claros sobre el fondo de rodaje

#### Scenario: Girar el móvil en claro
- **WHEN** el tema elegido es «Claro» y se gira el móvil en Rodaje
- **THEN** Rodaje cambia de disposición sin mostrarse en oscuro en ningún momento

#### Scenario: Parte de la pantalla en claro
- **WHEN** una zona de una pantalla en rodaje se muestra con el tema claro
- **THEN** esa zona usa los valores claros de cada token y el resto de la pantalla sigue en rodaje

#### Scenario: Token sin valor propio en claro
- **WHEN** se usa `focus`, que remite a `gear`, dentro de una zona en tema claro
- **THEN** su color es el de `gear` en claro, no el de rodaje
