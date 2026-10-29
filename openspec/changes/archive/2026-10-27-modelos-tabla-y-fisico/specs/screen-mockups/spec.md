# Spec Delta

## MODIFIED Requirements

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro», y «Orientación», con «Según el móvil», «Vertical» y «Horizontal»), Ciclista (peso del ciclista y peso de la bici), Rodillo («Modelo de resistencia», con «Simple», «Por tabla» y «Físico», «Tabla de marchas» con «Por tabla», «Dificultad», «Potencia mínima» con «Físico», «Bajadas» y «Pendiente sin Zwift») y Zwift («Mando de Zwift», activado, con su ayuda), con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad y distancia, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos. La de Tabla de marchas SHALL mostrar la lista de marchas con su ajuste editable en los estados de la lineal y de la Shimano.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el paso ERG, «Mantener la pantalla encendida», el aviso de cadencia baja, «Botones y gestos» ni la elección de wifi o Bluetooth para Zwift

#### Scenario: Modelos en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=simple`, `#estado=tabla` y `#estado=fisico`
- **THEN** «Modelo de resistencia» muestra elegido «Simple», «Por tabla» y «Físico», respectivamente, y «Tabla de marchas» solo aparece en el segundo y «Potencia mínima» solo en el tercero

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
- **THEN** el contador dice «6 de 6», están marcados seis, la vista previa los muestra todos y las casillas de los dos sin marcar están deshabilitadas con su ayuda

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno

#### Scenario: Velocidad y distancia en Datos en pantalla
- **WHEN** se abre `maquetas/datos.html#estado=velocidad`
- **THEN** la lista tiene ocho datos, la vista previa muestra velocidad y distancia y sus ayudas explican que la velocidad la calcula el modelo físico

#### Scenario: Tabla de marchas
- **WHEN** se abre `maquetas/tabla-marchas.html#estado=lineal` y `#estado=shimano`
- **THEN** se ven 24 filas con su ajuste y su paso a paso, la marcha puesta marcada y «Restablecer», en los dos temas

#### Scenario: Mando de Zwift en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio`
- **THEN** la última sección, «Zwift», tiene «Mando de Zwift» activado con su ayuda

## ADDED Requirements

### Requirement: Velocidad y distancia en las maquetas de rodaje
Las maquetas de rodaje, en vertical y en horizontal, SHALL tener estados con velocidad y distancia entre los datos, también con «—» en la velocidad, sin cambiar el tamaño de la cifra ni el área de datos fija.

#### Scenario: Velocidad y distancia
- **WHEN** se abre `maquetas/rodaje.html#estado=velocidad` y `maquetas/rodaje-vertical.html#estado=velocidad`
- **THEN** la rejilla muestra potencia, cadencia, velocidad y distancia con la misma cifra y sin desplazamiento

#### Scenario: Velocidad sin valor
- **WHEN** se abre `maquetas/rodaje.html#estado=velocidad-sin-valor`
- **THEN** la velocidad muestra «—», la distancia conserva su valor y la marcha sigue visible

### Requirement: Modelo en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener un estado con el modelo físico que muestre la potencia calculada, la mínima aplicada, la velocidad y la indicación de límite por la potencia mínima.

#### Scenario: Físico
- **WHEN** se abre `maquetas/diagnostico.html#estado=fisico`
- **THEN** la sección Simulación muestra el modelo Físico con sus valores y, en otro estado, la limitación por la potencia mínima con texto
