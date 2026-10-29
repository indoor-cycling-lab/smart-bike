## ADDED Requirements

### Requirement: Alternar el modo desde la pantalla
Con el ajuste «Cambiar de modo tocando» activado, Libre o SIM ⇄ ERG manual SHALL poder alternarse también tocando el indicador de modo de Rodaje, con las mismas reglas que la acción del Square: la potencia de entrada es la última usada, la marcha se conserva, en una ruta de la app manda el ERG manual y en un entrenamiento de la app no cambia nada. Esto SHALL funcionar sin Square configurado y con el Square sin conexión.

#### Scenario: Entrar sin Square
- **WHEN** no hay ningún Square, el ajuste está activado, se rueda sin Zwift en la marcha 14 y se toca el indicador de modo
- **THEN** el Neo mantiene la última potencia de ERG manual, sea cual sea la cadencia, y la marcha sigue siendo la 14

#### Scenario: Mismo efecto que el Square
- **WHEN** se alterna una vez tocando el indicador y otra con △ larga
- **THEN** en las dos ocasiones la potencia y la marcha resultantes son las mismas

#### Scenario: Ajustar la potencia sin Square
- **WHEN** sin Square se está en ERG manual a 200 W, con los botones − y + visibles, y se toca +
- **THEN** la potencia pasa a 210 W y el pie dice «+10 W»

#### Scenario: Square perdido
- **WHEN** con el ajuste activado, en ERG manual a 200 W, se pierde la conexión con el Square y se toca el indicador de modo
- **THEN** el modo pasa a Libre con la pendiente de la marcha puesta, y al volver el Square sigue en Libre

#### Scenario: Ajuste desactivado
- **WHEN** el ajuste «Cambiar de modo tocando» está desactivado, con el Square conectado o sin él
- **THEN** el modo solo se alterna con la acción del Square, y Rodaje lo muestra como una etiqueta, aunque los botones − y + estén visibles
