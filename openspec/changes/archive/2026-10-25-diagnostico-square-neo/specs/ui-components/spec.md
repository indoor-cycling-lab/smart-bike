# Spec Delta

## ADDED Requirements

### Requirement: Stepper con límites
El stepper SHALL mostrar deshabilitado, de forma distinguible y anunciada como no disponible, el botón que superaría el mínimo o el máximo, y SHALL mantener habilitado el otro.

#### Scenario: En el máximo
- **WHEN** un stepper está en su valor máximo
- **THEN** su botón «+» se ve atenuado, no responde al tocarlo y se anuncia como deshabilitado, y el «−» sigue disponible

#### Scenario: En los dos temas
- **WHEN** se revisa el stepper deshabilitado en la guía de estilo
- **THEN** aparece en tema rodaje y en tema claro

## MODIFIED Requirements

### Requirement: Componentes de diagnóstico
Diagnóstico SHALL disponer de un indicador de pendiente (Zwift → efectiva, con signo y color de subida o bajada) y de un volcado de tráfico con hora con milésimas, origen, característica y bytes, las tramas más recientes arriba y un estado vacío, y estos componentes SHALL NOT usarse en la pantalla de rodaje.

#### Scenario: Pendiente efectiva
- **WHEN** Zwift manda 4,0 % y la marcha suma 1,5 %
- **THEN** el indicador muestra «4,0 %» atenuado y «+5,5 %» en color de subida

#### Scenario: Volcado de tráfico
- **WHEN** la app envía una pendiente al Neo
- **THEN** el volcado muestra arriba una fila con la hora (`12:04:31.236`), el origen «→ Neo» en color de marca, la característica `fec3` y los bytes, en columnas alineadas

#### Scenario: Sin tráfico
- **WHEN** todavía no ha llegado ni salido ninguna trama
- **THEN** el volcado dice «Sin tráfico todavía»
