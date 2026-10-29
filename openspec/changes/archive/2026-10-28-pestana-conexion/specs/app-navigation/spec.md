## MODIFIED Requirements

### Requirement: Barra inferior
Conexión, Dispositivos y Ajustes SHALL mostrar al pie una barra con cuatro pestañas, en este orden: Rodar, Conexión, Dispositivos y Ajustes, cada una con icono y texto. La pestaña de la pantalla abierta SHALL distinguirse por algo más que el color. Rodaje y las pantallas secundarias SHALL NOT mostrar la barra.

#### Scenario: Pestaña activa
- **WHEN** se abre Ajustes
- **THEN** la barra inferior muestra las cuatro pestañas y Ajustes se ve marcada como la actual

#### Scenario: Orden de las pestañas
- **WHEN** se abre cualquier pantalla con la barra inferior
- **THEN** las pestañas son, de izquierda a derecha, Rodar, Conexión, Dispositivos y Ajustes

#### Scenario: Cambiar de pestaña
- **WHEN** en Dispositivos se toca «Conexión» en la barra
- **THEN** se abre Conexión, sin botón de volver y con la barra inferior

#### Scenario: Volver a rodar
- **WHEN** en Conexión se toca «Rodar»
- **THEN** se abre la pantalla de rodaje, sin barra inferior, con la misma marcha y sin perder ninguna conexión

#### Scenario: Rodaje sin barra
- **WHEN** se está en Rodaje
- **THEN** no se ve la barra inferior

### Requirement: Atrás del sistema
Atrás SHALL cerrar la pantalla secundaria abierta y volver a su origen; en Conexión, Dispositivos o Ajustes SHALL volver a Rodaje; en Rodaje SHALL terminar la sesión, como hasta ahora (spec `ride-session`).

#### Scenario: Atrás en una pestaña
- **WHEN** se está en Ajustes y se pulsa Atrás
- **THEN** se abre Rodaje y la sesión sigue en marcha

#### Scenario: Atrás en Conexión
- **WHEN** se está en Conexión y se pulsa Atrás
- **THEN** se abre Rodaje y la sesión sigue en marcha

#### Scenario: Atrás en una secundaria
- **WHEN** se está en Diagnóstico, abierto desde Ajustes, y se pulsa Atrás
- **THEN** se vuelve a Ajustes

#### Scenario: Atrás en Asignar acción
- **WHEN** se está en Asignar acción, abierta desde Botones, y se pulsa Atrás
- **THEN** se vuelve a Botones

#### Scenario: Atrás en Rodaje
- **WHEN** se está en Rodaje tras haber pasado por Dispositivos y Ajustes, y se pulsa Atrás
- **THEN** la sesión termina, sin volver antes a Ajustes ni a Dispositivos

### Requirement: Pantalla anterior o siguiente con el Square
Las acciones «Pantalla anterior» y «Pantalla siguiente» SHALL recorrer Rodar, Conexión, Dispositivos y Ajustes en ese orden y de forma circular: la siguiente a Ajustes es Rodar y la anterior a Rodar es Ajustes. Desde una pantalla secundaria SHALL ir a la pestaña anterior o siguiente a la de su origen, cerrando la secundaria; el origen de Botones y de Asignar acción es Ajustes. Mientras Asignar acción espera una pulsación, SHALL NOT cambiar de pantalla.

#### Scenario: Desde Rodaje
- **WHEN** se rueda en Rodaje con la asignación por defecto, se mantiene Campag. I y se pulsa la cruceta →
- **THEN** se abre Conexión y la marcha no cambia

#### Scenario: Vuelta circular
- **WHEN** se está en Ajustes y se pide la pantalla siguiente
- **THEN** se abre Rodaje

#### Scenario: Anterior desde Rodaje
- **WHEN** se está en Rodaje y se pide la pantalla anterior
- **THEN** se abre Ajustes

#### Scenario: Entre Dispositivos y Ajustes
- **WHEN** se está en Conexión y se pide la pantalla siguiente, y de nuevo la siguiente
- **THEN** se abre Dispositivos y después Ajustes

#### Scenario: Desde una secundaria
- **WHEN** se está en Marchas, abierta desde Ajustes, y se pide la pantalla anterior
- **THEN** se abre Dispositivos

#### Scenario: Desde Botones
- **WHEN** se está en Botones, abierta desde Ajustes, y se pide la pantalla siguiente
- **THEN** se abre Rodaje

#### Scenario: Capturando una pulsación
- **WHEN** Asignar acción espera una pulsación y se mantiene Campag. I y se pulsa la cruceta →
- **THEN** la pantalla no cambia y Asignar acción muestra la combinación pulsada

#### Scenario: App en segundo plano
- **WHEN** la pantalla del móvil está apagada o la app en segundo plano y se pide la pantalla siguiente
- **THEN** no cambia nada: al volver a la app se está en la misma pantalla, y la sesión y las conexiones siguen en marcha
