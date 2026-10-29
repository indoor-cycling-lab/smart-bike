## MODIFIED Requirements

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro», y «Orientación», con «Según el móvil», «Vertical» y «Horizontal»), Ciclista (solo el peso del ciclista), Rodillo («Bajadas» y «Pendiente sin Zwift») y Zwift («Mando de Zwift», activado, con su ayuda), con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el peso de la bici, la potencia mínima, el paso ERG, «Mantener la pantalla encendida», el aviso de cadencia baja, «Botones y gestos» ni la elección de wifi o Bluetooth para Zwift

#### Scenario: Tema en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio` y `maquetas/ajustes.html#estado=claro&tema=claro`
- **THEN** la sección «Pantalla», entre «Al rodar» y «Ciclista», muestra «Tema» con «Oscuro» elegido en el primer enlace y con «Claro» elegido, y la maqueta en claro, en el segundo

#### Scenario: Orientación en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`, `maquetas/ajustes.html#estado=vertical` y `maquetas/ajustes.html#estado=horizontal`
- **THEN** la sección «Pantalla» muestra, debajo de «Tema», «Orientación» con sus tres opciones y su ayuda, con «Según el móvil» elegido en el primer enlace, «Vertical» en el segundo y «Horizontal» en el tercero, y «Tema» con «Oscuro» elegido en los tres

#### Scenario: Orientación en tema claro
- **WHEN** se abre `maquetas/ajustes.html#estado=horizontal&tema=claro`
- **THEN** «Orientación» se ve con «Horizontal» elegido, distinguido por la marca de selección, con el contraste del tema claro

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6», están marcados los seis y la vista previa los muestra todos

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

#### Scenario: Mando de Zwift en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la última sección, «Zwift», tiene «Mando de Zwift» activado con su ayuda
