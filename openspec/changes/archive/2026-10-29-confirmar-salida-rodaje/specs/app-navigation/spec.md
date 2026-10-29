# Spec Delta

## MODIFIED Requirements

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
