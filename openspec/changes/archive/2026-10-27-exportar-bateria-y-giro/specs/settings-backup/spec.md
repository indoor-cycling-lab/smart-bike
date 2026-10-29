# Spec Delta

## Purpose

Permite sacar la configuración de la app a un archivo y volver a ponerla, para cambiar de móvil, reinstalar la app o guardar una copia, sin rehacer los ajustes a mano.

## ADDED Requirements

### Requirement: Exportar la configuración
Ajustes SHALL permitir exportar la configuración a un archivo JSON, elegido por el usuario con el selector de archivos del sistema. El archivo SHALL contener todos los ajustes de la app (transmisión, modelo y tablas, asignación de botones y gestos, datos en pantalla, ciclista, rodillo, tema, orientación y mando de Zwift) y SHALL NOT contener los dispositivos recordados, la última marcha ni el registro de sesión. El nombre propuesto SHALL incluir la fecha. La app SHALL NOT pedir permisos nuevos.

#### Scenario: Exportar
- **WHEN** se toca «Exportar configuración» y se elige un destino
- **THEN** se guarda el archivo y Ajustes dice «Configuración exportada»

#### Scenario: Cancelar el selector
- **WHEN** se toca «Exportar configuración» y se cierra el selector sin elegir destino
- **THEN** no se guarda nada y no aparece ningún mensaje

#### Scenario: No se puede escribir
- **WHEN** el destino elegido no admite escritura
- **THEN** Ajustes dice que no se pudo guardar el archivo y que se pruebe en otra ubicación, y los ajustes siguen igual

#### Scenario: Sin dispositivos
- **WHEN** se exporta con el Neo y el Square recordados
- **THEN** el archivo no contiene sus direcciones ni nombres

### Requirement: Importar la configuración
Ajustes SHALL permitir importar la configuración desde un archivo JSON elegido con el selector de archivos del sistema. La app SHALL validar el archivo entero antes de cambiar nada, y SHALL pedir confirmación antes de reemplazar los ajustes. Al confirmar, todos los ajustes SHALL pasar a ser los del archivo, y los dispositivos recordados SHALL NOT cambiar.

#### Scenario: Importar un archivo válido
- **WHEN** se toca «Importar configuración» y se elige un archivo válido
- **THEN** Ajustes pide confirmar con «Se reemplazarán todos los ajustes por los del archivo. Los dispositivos no cambian.» y los botones «Importar» y «Cancelar», sin haber cambiado nada

#### Scenario: Confirmar
- **WHEN** se confirma la importación
- **THEN** los ajustes son los del archivo, el tema y la orientación se aplican al momento, y Ajustes dice «Configuración importada»

#### Scenario: Cancelar la confirmación
- **WHEN** se elige un archivo válido y se toca «Cancelar»
- **THEN** los ajustes siguen como estaban

#### Scenario: Cancelar el selector
- **WHEN** se toca «Importar configuración» y se cierra el selector sin elegir archivo
- **THEN** no cambia nada y no aparece ningún mensaje

#### Scenario: Importar rodando
- **WHEN** se rueda con una transmisión y se importa una configuración con otra
- **THEN** la nueva transmisión se aplica en cuanto se confirma, igual que al cambiarla en Marchas, sin cortar las conexiones

#### Scenario: Dispositivos intactos
- **WHEN** se importa una configuración con el Neo y el Square recordados
- **THEN** siguen recordados los mismos dispositivos que antes de importar

### Requirement: Archivos no válidos
Si el archivo elegido no es una configuración válida, la app SHALL NOT cambiar ningún ajuste y SHALL decir el motivo, sin disculpas y con qué hacer. Los motivos SHALL distinguir: archivo que no se puede leer o vacío, que no es JSON, que no es una configuración de Square Shift, y que viene de una versión más nueva de la app. Una configuración de una versión anterior SHALL importarse igual que se lee al abrir la app. Un valor fuera de sus límites SHALL recortarse a ellos, como al abrir la app.

#### Scenario: No es JSON
- **WHEN** se elige un archivo de texto que no es JSON
- **THEN** Ajustes dice «El archivo no es una configuración de Square Shift» y los ajustes siguen igual

#### Scenario: JSON de otra cosa
- **WHEN** se elige un JSON que no tiene la forma de una configuración
- **THEN** Ajustes dice «El archivo no es una configuración de Square Shift» y los ajustes siguen igual

#### Scenario: Versión más nueva
- **WHEN** se elige una configuración exportada por una versión más nueva de la app
- **THEN** Ajustes dice «El archivo es de una versión más nueva de Square Shift. Actualiza la app para importarlo.» y los ajustes siguen igual

#### Scenario: Versión anterior
- **WHEN** se elige una configuración exportada por una versión anterior
- **THEN** se importa con los ajustes que no tenía en sus valores por defecto

#### Scenario: Archivo vacío o ilegible
- **WHEN** se elige un archivo vacío o que el sistema no deja leer
- **THEN** Ajustes dice «No se pudo leer el archivo» y los ajustes siguen igual

#### Scenario: Valor fuera de límites
- **WHEN** el archivo tiene un peso de ciclista de 400 kg
- **THEN** se importa con el máximo permitido

### Requirement: Exportar e importar sin conexión
Exportar e importar SHALL funcionar con cualquier estado de conexión del Neo, el Square y Zwift, con el Bluetooth apagado y sin wifi.

#### Scenario: Sin dispositivos
- **WHEN** no hay ningún dispositivo conectado
- **THEN** se puede exportar e importar igual
