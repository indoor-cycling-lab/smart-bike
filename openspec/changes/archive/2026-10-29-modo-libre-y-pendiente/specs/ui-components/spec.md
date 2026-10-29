## MODIFIED Requirements

### Requirement: Insignia de modo
La insignia de modo SHALL mostrar «LIBRE», «SIM» o «ERG», con su color de modo (el de «LIBRE» SHALL distinguirse del de «SIM» por la palabra, no solo por el color), y en ERG SHALL indicar quién fija la potencia (Zwift o manual).

#### Scenario: Libre
- **WHEN** se rueda sin app de entrenamiento
- **THEN** la insignia muestra «LIBRE»

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift fija una potencia
- **THEN** la insignia muestra «ERG» con «ZWIFT» al lado
