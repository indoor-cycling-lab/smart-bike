# Spec Delta

## ADDED Requirements

### Requirement: Aviso de datos ocultos en pantalla baja
En una pantalla baja, la pantalla «Datos en pantalla» SHALL avisar, cuando los botones − y + estén activados y haya más de 4 datos elegidos, de que Rodaje solo muestra los 4 primeros. El aviso SHALL decir cuáles son los que no se muestran y no SHALL impedir elegir hasta 6 datos.

#### Scenario: Seis datos con botones en pantalla baja
- **WHEN** se abre «Datos en pantalla» en una pantalla baja con 6 datos elegidos y los botones − y + activados
- **THEN** un aviso dice que con los botones activados Rodaje solo muestra los 4 primeros datos, y nombra el quinto y el sexto como los que no se muestran

#### Scenario: Elegir el quinto dato
- **WHEN** en esa pantalla, con los botones activados, se marca un quinto dato
- **THEN** el aviso aparece y el contador dice «5 de 6»

#### Scenario: Sin botones
- **WHEN** en una pantalla baja hay 6 datos elegidos y los botones están desactivados
- **THEN** no hay aviso

#### Scenario: Pantalla alta
- **WHEN** en una pantalla de 760 dp de alto o más hay 6 datos elegidos y los botones activados
- **THEN** no hay aviso
