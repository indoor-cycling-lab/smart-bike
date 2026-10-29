# Spec Delta

## ADDED Requirements

### Requirement: Diagnóstico desde Ajustes
Diagnóstico SHALL abrirse desde la entrada «Diagnóstico» de Ajustes, sin barra inferior, con un botón de volver a Ajustes y sus secciones en este orden: conexiones, botones del Square, simulación, tráfico y control manual del Neo. SHALL NOT tener enlaces a Botones ni a Ajustes, que están en la barra inferior.

#### Scenario: Volver a Ajustes
- **WHEN** se abre Diagnóstico desde Ajustes y se toca volver, o se pulsa Atrás
- **THEN** se vuelve a Ajustes sin terminar la sesión

#### Scenario: Sin enlaces a otras pestañas
- **WHEN** se recorre Diagnóstico
- **THEN** no aparecen «Asignación de botones» ni «Ajustes»

#### Scenario: Control manual al salir
- **WHEN** en Diagnóstico se ha enviado al Neo una pendiente a mano y se vuelve a Ajustes
- **THEN** el Neo vuelve al comando de la marcha, como al salir de Diagnóstico hacia Rodaje

## REMOVED Requirements

### Requirement: Diagnóstico como pantalla secundaria
**Reason**: Con la barra inferior, los chips de Rodaje abren Dispositivos y Diagnóstico se abre desde Ajustes; Botones ya no se enlaza desde Diagnóstico porque es una pestaña.
**Migration**: Diagnóstico se abre con Ajustes → «Diagnóstico» y vuelve a Ajustes («Diagnóstico desde Ajustes»); Botones, desde la barra inferior.
