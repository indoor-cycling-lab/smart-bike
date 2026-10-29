## MODIFIED Requirements

### Requirement: Exportar la configuración
Ajustes SHALL permitir exportar la configuración a un archivo JSON, elegido con el selector de archivos del sistema. El archivo SHALL contener todos los ajustes de la app (transmisión, modelo y tablas, botones y gestos, datos en pantalla, ciclista, rodillo, idioma, tema, orientación, modo de conexión y nombre ante la app) y SHALL NOT contener los dispositivos configurados, la última marcha ni el registro de sesión. El nombre propuesto SHALL incluir la fecha. La app SHALL NOT pedir permisos nuevos.

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
- **WHEN** se exporta con el Neo y el Square configurados
- **THEN** el archivo no contiene sus direcciones ni nombres

#### Scenario: Modo de conexión
- **WHEN** se exporta con «Solo Bluetooth» elegido y se importa en otro móvil
- **THEN** el modo es «Solo Bluetooth», sin ningún ajuste de plataforma ni de «Mando de Zwift»

#### Scenario: Idioma
- **WHEN** se exporta con «English» elegido y se importa en otro móvil
- **THEN** el idioma es «English» (spec `app-language`)
