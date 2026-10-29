## MODIFIED Requirements

### Requirement: Pendiente de Zwift con la marcha
Cuando Zwift envía el terreno de la ruta, la app SHALL simular en el Neo la pendiente de Zwift en lugar de la pendiente base, más el ajuste de la marcha puesta (1 % por marcha respecto a la 12), con el límite inferior que fije el ajuste «Bajadas», y SHALL seguir enviando cada cambio de marcha al instante.

#### Scenario: Subida en marcha neutra
- **WHEN** Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 %

#### Scenario: Subida en marcha dura
- **WHEN** Zwift envía una pendiente de 4 % y se pasa de la marcha 12 a la 14
- **THEN** el Neo simula +6 % en cuanto se pulsa la palanca, sin esperar al siguiente terreno de Zwift

#### Scenario: Bajada
- **WHEN** Zwift envía una pendiente de −3 % y la marcha es la 10, con el ajuste «Bajadas» por defecto
- **THEN** el Neo simula −3 %, porque la marcha no hace la bajada más pronunciada que la de Zwift

#### Scenario: Bajada sin protección
- **WHEN** Zwift envía una pendiente de −3 % y la marcha es la 10, con «Bajadas» en «Sin protección»
- **THEN** el Neo simula −5 %

#### Scenario: Bajada sin empuje
- **WHEN** Zwift envía una pendiente de −3 % y la marcha es la 10, con «Bajadas» en «Nunca empujar»
- **THEN** el Neo simula 0 %

#### Scenario: Terreno que cambia muy seguido
- **WHEN** Zwift envía pendientes nuevas varias veces por segundo
- **THEN** el Neo recibe como mucho 4 pendientes por segundo, siempre con la última de Zwift, y no recibe las que difieren menos de 0,1 % de la anterior
