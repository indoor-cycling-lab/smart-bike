# Spec Delta

## ADDED Requirements

### Requirement: Pulsómetro en Conexiones
Si hay un pulsómetro recordado, la sección Conexiones de Diagnóstico SHALL mostrar una fila «Pulsómetro» con su estado, con palabra y forma, el último pulso válido en bpm y cuántos segundos hace que se leyó, y «—» si no hay pulso. Sin pulsómetro recordado, la fila SHALL NOT aparecer. El tráfico de la banda y de lo que se envía a Zwift SHALL verse en el tráfico en crudo cuando esté activado.

#### Scenario: Banda dando pulso
- **WHEN** el pulsómetro está conectado y da 128 bpm
- **THEN** la fila dice «Conectado» y «128 bpm» con el tiempo de la última lectura

#### Scenario: Sin pulso
- **WHEN** el pulsómetro está conectado pero no da lecturas válidas desde hace más de 5 s
- **THEN** la fila dice «Conectado» y «—» en lugar del pulso

#### Scenario: Banda reconectando
- **WHEN** el pulsómetro se ha perdido y la app intenta recuperarlo
- **THEN** la fila dice «Reconectando» y «—», con un aviso informativo que dice cómo despertarla

#### Scenario: Sin pulsómetro
- **WHEN** no hay pulsómetro recordado
- **THEN** Conexiones muestra las filas de siempre, sin «Pulsómetro»

#### Scenario: Tráfico de la banda
- **WHEN** el tráfico en directo está activado y la banda da una lectura que se reenvía a Zwift
- **THEN** el tráfico muestra la trama recibida de la banda y la enviada a Zwift, cada una con su origen y su sentido
