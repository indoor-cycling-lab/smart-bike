## ADDED Requirements

### Requirement: Acceso a Ajustes en las maquetas de rodaje
Todos los estados de las maquetas de rodaje, en horizontal y en vertical, SHALL mostrar en la esquina superior derecha el botón con el icono de Ajustes, enlazado con la maqueta de Ajustes, con descripción accesible «Ajustes» y objetivo táctil de `touch-min`, en los dos temas.

#### Scenario: Horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado
- **THEN** el icono de Ajustes está a la derecha de los chips de conexión, en la barra superior, y al tocarlo se abre `maquetas/ajustes.html`

#### Scenario: Vertical a 360 dp
- **WHEN** se abre `maquetas/rodaje-vertical.html` en el estado «Sin Zwift · Neo y Square conectando» con el marco a 360 dp de ancho
- **THEN** los tres chips y el icono de Ajustes caben en una sola línea, con el icono al final

#### Scenario: Rodaje en claro
- **WHEN** se abre `maquetas/rodaje.html#tema=claro` y `maquetas/rodaje-vertical.html#tema=claro`
- **THEN** la pantalla de rodaje y el icono de Ajustes se ven con los valores claros de los tokens y el icono se distingue del fondo

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado «Sin Zwift · sin dispositivos»
- **THEN** el icono de Ajustes sigue en su sitio, junto a los chips con el borde de alerta

## MODIFIED Requirements

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar las entradas Marchas y Diagnóstico y las secciones Al rodar, Pantalla («Tema», con «Oscuro» y «Claro»), Ciclista (solo el peso del ciclista) y Rodillo («Bajadas» y «Pendiente sin Zwift»), con la navegación inferior. La de Datos en pantalla SHALL permitir elegir de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima, ordenarlos, y ver la vista previa, en los estados de 1, 2, 4 y 6 datos.

#### Scenario: Sin ajustes de otros changes
- **WHEN** se abre la maqueta de Ajustes
- **THEN** no aparecen el peso de la bici, la potencia mínima, el paso ERG, «Mantener la pantalla encendida», el aviso de cadencia baja ni «Botones y gestos»

#### Scenario: Tema en la maqueta de Ajustes
- **WHEN** se abre `maquetas/ajustes.html#estado=inicio` y `maquetas/ajustes.html#estado=claro&tema=claro`
- **THEN** la sección «Pantalla», entre «Al rodar» y «Ciclista», muestra «Tema» con «Oscuro» elegido en el primer enlace y con «Claro» elegido, y la maqueta en claro, en el segundo

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6», están marcados los seis y la vista previa los muestra todos

#### Scenario: Un dato
- **WHEN** se elige el estado de 1 dato
- **THEN** el contador dice «1 de 6», la casilla del dato marcado no se puede desmarcar y la ayuda dice que hace falta al menos uno
