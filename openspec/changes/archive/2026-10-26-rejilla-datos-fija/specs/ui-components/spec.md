## MODIFIED Requirements

### Requirement: Rejilla de datos sin desplazamiento
La rejilla de datos de rodaje SHALL mostrar de 1 a 6 datos elegidos por el usuario, en su orden, con el mismo tamaño de cifra para todos: el mayor, hasta 144 px, con el que el valor más largo posible de cada dato cabe en su celda junto a su etiqueta, sin desplazamiento ni cifras cortadas. Con un número impar de datos en 2 columnas, el último SHALL ir centrado en su fila. Todas las celdas SHALL alinear igual la etiqueta y la cifra, tengan unidad o no.

#### Scenario: Pocos datos
- **WHEN** hay 2 datos en la pantalla horizontal
- **THEN** se apilan en una columna con la cifra más grande con la que caben en su área, igual para los dos

#### Scenario: Máximo de datos
- **WHEN** hay 6 datos en la pantalla horizontal
- **THEN** se colocan en 2 columnas y 3 filas, todos visibles y sin desplazamiento

#### Scenario: Valor largo
- **WHEN** el tiempo pasa de «59:59» a «1:00:00» o la potencia de «999» a «1000» W
- **THEN** la cifra de la rejilla no cambia de tamaño, porque ya estaba medida con el valor más largo de cada dato, y ningún valor se corta ni pasa a dos líneas

#### Scenario: Dato sin valor
- **WHEN** un dato no está disponible porque su sensor no está conectado
- **THEN** se muestra «—» atenuado, nunca 0, con la misma cifra que si tuviera valor

#### Scenario: Último dato impar
- **WHEN** hay 3 o 5 datos en 2 columnas
- **THEN** el último ocupa el ancho de las dos columnas, centrado, sin hueco a su derecha

#### Scenario: Filas con y sin unidad
- **WHEN** hay 6 datos y unos tienen unidad (potencia, en W) y otros no (tiempo, W/kg)
- **THEN** en todas las celdas la etiqueta queda a la misma distancia del borde superior y las cifras tienen el mismo alto

#### Scenario: Tope de la cifra
- **WHEN** hay 1 solo dato y su área permitiría una cifra mayor
- **THEN** la cifra se queda en 144 px, centrada en su área
