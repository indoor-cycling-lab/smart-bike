## ADDED Requirements

### Requirement: Simular un KICKR BIKE en Ajustes
La sección «Zwift» de Ajustes SHALL tener el interruptor «Simular un KICKR BIKE», desactivado por defecto, con una ayuda que diga que Zwift verá la app como un «KICKR BIKE PRO», que es una prueba y que hay que emparejarla de nuevo. El cambio SHALL aplicarse al momento, también rodando, SHALL guardarse con el resto de los ajustes y SHALL poder hacerse con «Mando de Zwift» desactivado y con cualquiera de los dos modos de conexión con Zwift.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Simular un KICKR BIKE» está desactivado y la app se anuncia como «Square Shift»

#### Scenario: Activarlo
- **WHEN** se activa «Simular un KICKR BIKE»
- **THEN** queda activado, se guarda y la ayuda dice que Zwift verá un «KICKR BIKE PRO», que es una prueba y que hay que emparejarlo de nuevo

#### Scenario: Ajuste guardado
- **WHEN** se activa «Simular un KICKR BIKE» y se cierra y se vuelve a abrir la app
- **THEN** sigue activado y la app se anuncia como «KICKR BIKE PRO» desde el primer momento

#### Scenario: Configuración anterior al ajuste
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste
- **THEN** se conservan los demás ajustes y «Simular un KICKR BIKE» está desactivado

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con el ajuste activado y se importa en otro móvil
- **THEN** el ajuste queda activado, y el móvil nuevo usa su propia identidad y no la del móvil anterior

#### Scenario: Con el mando desactivado o en cualquier modo
- **WHEN** «Mando de Zwift» está desactivado, o la conexión con Zwift es «Todo por wifi», y se cambia «Simular un KICKR BIKE»
- **THEN** el ajuste se puede cambiar y queda guardado

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi y se abre Ajustes
- **THEN** «Simular un KICKR BIKE» se puede cambiar y su ayuda no dice nada de la red
