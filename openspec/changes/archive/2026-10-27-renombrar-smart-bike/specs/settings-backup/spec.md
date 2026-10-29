## MODIFIED Requirements

### Requirement: Archivos no válidos
Si el archivo elegido no es una configuración válida, la app SHALL NOT cambiar ningún ajuste y SHALL decir el motivo, sin disculpas y con qué hacer. Los motivos SHALL distinguir: archivo que no se puede leer o vacío, que no es JSON, que no es una configuración de Smart Bike, y que viene de una versión más nueva de la app. Una configuración de una versión anterior SHALL importarse igual que se lee al abrir la app. Un valor fuera de sus límites SHALL recortarse a ellos, como al abrir la app.

#### Scenario: No es JSON
- **WHEN** se elige un archivo de texto que no es JSON
- **THEN** Ajustes dice «El archivo no es una configuración de Smart Bike» y los ajustes siguen igual

#### Scenario: JSON de otra cosa
- **WHEN** se elige un JSON que no tiene la forma de una configuración
- **THEN** Ajustes dice «El archivo no es una configuración de Smart Bike» y los ajustes siguen igual

#### Scenario: Versión más nueva
- **WHEN** se elige una configuración exportada por una versión más nueva de la app
- **THEN** Ajustes dice «El archivo es de una versión más nueva de Smart Bike. Actualiza la app para importarlo.» y los ajustes siguen igual

#### Scenario: Versión anterior
- **WHEN** se elige una configuración exportada por una versión anterior
- **THEN** se importa con los ajustes que no tenía en sus valores por defecto

#### Scenario: Archivo vacío o ilegible
- **WHEN** se elige un archivo vacío o que el sistema no deja leer
- **THEN** Ajustes dice «No se pudo leer el archivo» y los ajustes siguen igual

#### Scenario: Valor fuera de límites
- **WHEN** el archivo tiene un peso de ciclista de 400 kg
- **THEN** se importa con el máximo permitido
