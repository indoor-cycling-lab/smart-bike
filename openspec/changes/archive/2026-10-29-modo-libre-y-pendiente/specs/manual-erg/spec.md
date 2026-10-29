## MODIFIED Requirements

### Requirement: Alternar SIM ⇄ ERG manual
La acción «Alternar SIM ⇄ ERG» SHALL pasar de la simulación (modo Libre sin Zwift, o modo SIM en una ruta de Zwift) a ERG manual y, en ERG manual, volver a la simulación que corresponda. «SIM» nombra aquí las dos. En ERG manual, el Neo SHALL mantener la potencia fijada sea cual sea la cadencia. Al volver a la simulación, el Neo SHALL recibir al momento la pendiente que corresponde a la marcha puesta.

#### Scenario: Entrar en ERG manual
- **WHEN** se rueda sin Zwift en la marcha 14, la última potencia de ERG manual es 200 W y se mantiene △ hasta el umbral de pulsación larga
- **THEN** el Neo pasa a 200 W y lo mantiene aunque cambie la cadencia

#### Scenario: Volver a SIM
- **WHEN** en ERG manual, con la marcha 14 sin Zwift, se mantiene △ hasta el umbral de pulsación larga
- **THEN** el Neo pasa a simular +2 % (la pendiente de la marcha 14) y la marcha sigue siendo la 14

#### Scenario: Potencia mantenida a dos cadencias
- **WHEN** en ERG manual a 200 W se pedalea 30 s a unas 70 rpm y otros 30 s a unas 95 rpm
- **THEN** la potencia medida del Neo se queda cerca de 200 W en los dos tramos, tras unos segundos de estabilización al cambiar de cadencia
