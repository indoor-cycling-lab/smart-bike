# app-navigation Specification

## Purpose
Cómo se mueve el usuario por la app: la barra inferior con sus cuatro pestañas, las pantallas secundarias y adónde vuelven, Atrás del sistema y el cambio de pantalla con los botones del Square.

## Requirements

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

### Requirement: Pantallas secundarias
Botones, Marchas, Datos en pantalla, Diagnóstico y Sesiones grabadas SHALL abrirse desde Ajustes, y Asignar acción desde Botones, sin barra inferior y con un botón de volver a la pantalla desde la que se abrieron. Abrir o cerrar una pantalla SHALL NOT terminar la sesión ni cambiar la marcha.

#### Scenario: Ajustes a Marchas y vuelta
- **WHEN** en Ajustes se toca «Marchas» y luego volver
- **THEN** se abre Marchas sin barra inferior y después se vuelve a Ajustes con la barra

#### Scenario: Ajustes a Botones y vuelta
- **WHEN** en Ajustes se toca «Botones» y luego volver
- **THEN** se abre Botones sin barra inferior y después se vuelve a Ajustes con la barra

#### Scenario: Ajustes a Sesiones grabadas y vuelta
- **WHEN** en la sección «Depuración» de Ajustes se toca «Sesiones grabadas» y luego volver
- **THEN** se abre Sesiones grabadas sin barra inferior y después se vuelve a Ajustes con la barra, sin cambiar la marcha ni terminar la sesión

#### Scenario: Asignar acción
- **WHEN** en Botones se toca una acción y luego volver
- **THEN** se abre Asignar acción y después se vuelve a Botones

### Requirement: Atrás del sistema
Atrás SHALL cerrar la pantalla secundaria abierta y volver a su origen; en Conexión, Dispositivos o Ajustes SHALL volver a Rodaje; en Rodaje SHALL pedir confirmación y terminar la sesión solo con un segundo Atrás en los 3 s siguientes (specs `ride-screen` y `ride-session`).

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
- **WHEN** se está en Rodaje tras haber pasado por Dispositivos y Ajustes, y se pulsa Atrás dos veces seguidas
- **THEN** la sesión termina, sin volver antes a Ajustes ni a Dispositivos

#### Scenario: Primer Atrás en Rodaje
- **WHEN** se está en Rodaje y se pulsa Atrás una vez
- **THEN** no se abre ninguna otra pantalla y la sesión sigue en marcha

#### Scenario: Atrás en una pestaña no pide confirmación
- **WHEN** se está en Ajustes y se pulsa Atrás
- **THEN** se abre Rodaje sin ningún aviso de salida

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
