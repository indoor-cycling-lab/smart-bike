# Spec Delta

## ADDED Requirements

### Requirement: Batería del Square en Conexiones
La fila del Square de la sección Conexiones de Diagnóstico SHALL mostrar el nivel de su batería en porcentaje. Mientras no haya un nivel válido SHALL decir «Batería: sin lectura todavía». Con el Square sin conexión SHALL seguir mostrando el último nivel válido, si lo hay, junto a su estado de conexión.

#### Scenario: Nivel conocido
- **WHEN** el Square está conectado y su último nivel válido es 80 %
- **THEN** la fila del Square dice «Conectado» y «Batería 80 %», sin aviso de batería

#### Scenario: Sin lectura
- **WHEN** el Square acaba de conectarse y aún no ha llegado una lectura periódica
- **THEN** la fila del Square dice «Batería: sin lectura todavía»

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y su último nivel válido fue 60 %
- **THEN** la fila dice el estado de conexión del Square y «Última batería 60 %»

#### Scenario: Nunca conectado
- **WHEN** no hay ningún Square recordado
- **THEN** la fila no menciona la batería

### Requirement: Aviso de batería baja en Diagnóstico
Con la batería del Square baja (spec `square-battery`) y el Square conectado, la fila del Square SHALL mostrar el nivel con «baja» y la sección Conexiones SHALL mostrar un aviso informativo, con icono y texto, sin depender solo del color.

#### Scenario: Batería baja
- **WHEN** el Square está conectado y su último nivel válido es 15 %
- **THEN** la fila dice «Batería 15 % · baja» y hay un aviso informativo «Batería del Square baja (15 %). Revisa la pila antes de rodar.»

#### Scenario: Batería recuperada
- **WHEN** llega una lectura periódica de 100 % tras cambiar la pila
- **THEN** el aviso desaparece y la fila dice «Batería 100 %»
