## MODIFIED Requirements

### Requirement: Capturas de las maquetas
Cada captura SHALL proceder de una maqueta del proyecto, con sus datos ilustrativos y sin la barra de revisión, dentro de un marco de móvil, y SHALL existir en tema oscuro y en tema claro. Cada captura SHALL existir también en español y en inglés, con la interfaz de la app en el idioma de la versión de la página que la muestra, y sin ningún texto de la interfaz en el otro idioma. La página SHALL mostrar la captura del tema activo. Ninguna captura SHALL contener datos personales ni reales de dispositivos o de red.

#### Scenario: Captura del tema activo
- **WHEN** la página se muestra en tema claro
- **THEN** todas las capturas se ven en tema claro

#### Scenario: Captura del idioma de la página
- **WHEN** se abre la versión inglesa de la página
- **THEN** todas las capturas muestran la interfaz de la app en inglés, con los mismos datos, estado y tema que en la versión española

#### Scenario: Captura en la versión española
- **WHEN** se abre la versión española de la página
- **THEN** todas las capturas muestran la interfaz de la app en español

#### Scenario: Formato de cifras
- **WHEN** una captura inglesa muestra un número con decimales o un porcentaje
- **THEN** lo escribe con el formato numérico que usa la app en inglés (por ejemplo, punto decimal en lugar de coma)

#### Scenario: Descripción de las capturas
- **WHEN** una captura no se puede ver o la lee un lector de pantalla
- **THEN** su texto alternativo, en el idioma de la página, describe la pantalla y el estado que muestra
