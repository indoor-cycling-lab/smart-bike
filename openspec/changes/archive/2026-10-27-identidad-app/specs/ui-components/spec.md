## ADDED Requirements

### Requirement: Icono propio de Botones
La pestaña Botones de la navegación inferior SHALL usar un icono propio, formado por un triángulo, un cuadrado relleno, un círculo y una cruz en cuadrícula 2×2, distinto del icono del mando de Zwift de la barra de conexiones. El icono SHALL ser de un solo color, tomar el de la pestaña (activa o no) y reconocerse a 24 dp en los dos temas.

#### Scenario: Pestaña Botones
- **WHEN** se abre cualquier pantalla con la barra inferior
- **THEN** la pestaña Botones muestra el icono de los cuatro símbolos y no el del mando de consola

#### Scenario: Botones y mando de Zwift distintos
- **WHEN** se ven a la vez la barra inferior de Dispositivos y el mando de Zwift de su lista de conexiones
- **THEN** los dos iconos son distintos

#### Scenario: Pestaña activa en los dos temas
- **WHEN** la pestaña Botones está activa en tema rodaje y en tema claro
- **THEN** el icono se ve en el color de marca de la pestaña, con el contraste del texto de la pestaña
