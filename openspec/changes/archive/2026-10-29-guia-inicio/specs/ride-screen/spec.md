## ADDED Requirements

### Requirement: Guía de inicio en Rodaje
Mientras la guía de inicio esté a la vista (spec `getting-started-guide`), Rodaje SHALL mostrarla como una hoja inferior que, abierta, flota sobre la pantalla sin mover ni cambiar de tamaño ningún elemento, y, minimizada, como una tira de 48 dp en el sitio del pie. La hoja y la tira SHALL verse enteras, sin solaparse con las barras y sin desplazamiento, en vertical y en horizontal. La hoja SHALL no bloquear: los avisos y el estado de lo publicado SHALL seguir actualizándose.

#### Scenario: Primer uso en vertical
- **WHEN** se abre la app sin dispositivos en vertical a 360 × 640 dp
- **THEN** la hoja abierta muestra la página 1 entera sobre la marcha, sin desplazamiento y con la barra de conexiones y el modo a la vista

#### Scenario: Primer uso en horizontal
- **WHEN** se abre la app sin dispositivos en horizontal a la anchura más estrecha que se soporta
- **THEN** la hoja abierta muestra cada página entera, con los tres elementos de la página en columnas, sin desplazamiento

#### Scenario: Minimizada
- **WHEN** la hoja está minimizada
- **THEN** la tira ocupa el pie, con el texto «Toca las conexiones…» no visible, y la marcha y los datos no cambian más que los 8 dp que el pie gana a 48 dp

#### Scenario: Abrir y cerrar
- **WHEN** se abre y se minimiza la hoja con la sesión en marcha
- **THEN** la marcha no se mueve ni cambia de tamaño

#### Scenario: Al desaparecer
- **WHEN** la guía se oculta con la sesión en marcha
- **THEN** el pie recupera su alto y su texto, y la marcha y los datos recuperan solo esa diferencia

#### Scenario: Con los botones − y +
- **WHEN** la guía está minimizada con los botones − y + activados
- **THEN** los botones mantienen su objetivo táctil de 72 dp y la tira no los tapa; la hoja abierta puede taparlos mientras se lee

#### Scenario: Aviso de acción
- **WHEN** se hace una acción con «Mostrar la acción» activado y la guía minimizada
- **THEN** el aviso ocupa el pie mientras dura y después vuelve la tira

#### Scenario: Objetivos táctiles
- **WHEN** se miden la hoja y la tira
- **THEN** cada paso, cada botón, la equis y el botón de minimizar tienen un objetivo táctil de al menos 48 dp
