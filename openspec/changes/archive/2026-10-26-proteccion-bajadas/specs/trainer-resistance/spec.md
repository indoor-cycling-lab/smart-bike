## MODIFIED Requirements

### Requirement: Pendiente según la marcha sin Zwift
Sin Zwift, la app SHALL simular en el Neo la pendiente base de 0 % más 1 % por cada marcha por encima de la 12 (menos 1 % por cada marcha por debajo), en modo SIM, con el límite inferior que fije el ajuste «Bajadas».

#### Scenario: Marcha de referencia
- **WHEN** la marcha es la 12 y el Neo está conectado
- **THEN** el Neo simula una pendiente de 0 %

#### Scenario: Marcha dura
- **WHEN** la marcha pasa a la 17
- **THEN** el Neo simula una pendiente de +5 % y la resistencia aumenta de forma perceptible

#### Scenario: Extremos
- **WHEN** la marcha es la 1 o la 24 con el ajuste «Bajadas» por defecto
- **THEN** el Neo simula 0 % o +12 %, respectivamente

#### Scenario: Extremos sin protección
- **WHEN** la marcha es la 1 o la 24 con «Bajadas» en «Sin protección»
- **THEN** el Neo simula −11 % o +12 %, respectivamente

## ADDED Requirements

### Requirement: Protección en bajadas
En modo SIM, con o sin Zwift, la app SHALL calcular la pendiente del terreno más el ajuste de la marcha y SHALL aplicarle el límite inferior del ajuste «Bajadas»: ninguno con «Sin protección»; la pendiente del terreno si es negativa, o 0 % si no lo es, con «La marcha no crea bajadas»; y 0 % con «Nunca empujar». El límite SHALL NOT afectar a la ERG ni al control manual de Diagnóstico.

#### Scenario: Llano en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es 0 %, la marcha es la 1 y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 % y el motor no empuja el volante

#### Scenario: Subida en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es +4 % y la marcha es la 1, con «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %

#### Scenario: Bajada en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 1, con «La marcha no crea bajadas»
- **THEN** el Neo simula −7 %

#### Scenario: Bajada en marcha dura, por defecto
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 24, con «La marcha no crea bajadas»
- **THEN** el Neo simula +5 %

#### Scenario: Nunca empujar en una bajada
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 12, con «Nunca empujar»
- **THEN** el Neo simula 0 %

#### Scenario: Nunca empujar no endurece
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 22, con «Nunca empujar»
- **THEN** el Neo simula +3 %

#### Scenario: Sin protección
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 1, con «Sin protección»
- **THEN** el Neo simula −18 %

#### Scenario: Marchas que dan la misma pendiente
- **WHEN** la pendiente del terreno es 0 %, «Bajadas» está en «La marcha no crea bajadas» y se pasa de la marcha 3 a la 4
- **THEN** la marcha cambia en pantalla y no se envía nada nuevo al Neo, porque la pendiente sigue siendo 0 %

#### Scenario: Control manual
- **WHEN** en Diagnóstico se elige −5,0 % y se toca «Enviar pendiente», con cualquier modo de «Bajadas»
- **THEN** el Neo simula −5 %

#### Scenario: Prueba del empuje del motor
- **WHEN** se envían desde el control manual pendientes de −0,5 % a −5 % en pasos de 0,5 % y, en cada una, se pedalea unos segundos y se deja de pedalear
- **THEN** se anota a partir de qué pendiente el motor mantiene o acelera el volante sin pedalear; el resultado SHALL validarse con el equipo real antes de decidir un suelo distinto de 0 %
