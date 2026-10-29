# Spec Delta

## MODIFIED Requirements

### Requirement: Modo y transmisión en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener el modo y la transmisión en la línea superior, a la izquierda del estado de la publicación (centrado en la línea) en horizontal, y en la fila de debajo, con el icono de Ajustes a su derecha, en vertical. En horizontal, los datos y la marcha SHALL empezar justo bajo la línea superior, sin reservar el sitio de una esquina.

#### Scenario: Modo, transmisión y Ajustes en una línea
- **WHEN** se abre la maqueta de rodaje en vertical
- **THEN** el modo con la transmisión y el icono de Ajustes están en una sola línea

#### Scenario: Modo en la línea superior en horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado, también en ERG
- **THEN** el modo con la transmisión está a la izquierda de la línea superior, el estado de la publicación centrado y, al final, la barra de conexiones con Ajustes, y la zona principal empieza justo debajo

### Requirement: Barra de conexiones y plataforma en la maqueta de rodaje
Los estados de la maqueta de rodaje SHALL tener la barra de conexiones de los dispositivos (un icono por enlace, agrupados con 4 dp entre ellos, con el estado por la forma: normal, atenuado con la marca girando o tachado, sin nombres ni palabras) enlazando con Dispositivos, y el estado de la publicación en palabras, sin nombre de plataforma, en la misma fila, enlazando con Conexión.

#### Scenario: Enlaces de la barra y de la plataforma
- **WHEN** se toca la barra de conexiones o el estado de la publicación
- **THEN** se abre Dispositivos o Conexión respectivamente

#### Scenario: Estados de la publicación
- **WHEN** se recorren los estados de la maqueta de rodaje
- **THEN** el estado dice «Conectado», «Conectado · sin controlador», «Conectado · sin pulso», «Esperando conexión…», «Sin wifi», «No visible en la red» o «Sin rodillo», y ninguno nombra Zwift

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar, bajo la barra de estado del sistema (dibujada en el marco del dispositivo y no como parte de la pantalla), la fila de la plataforma, con el estado de la publicación a la izquierda y la barra de conexiones a la derecha, y debajo, en una sola fila, el modo con la transmisión y el icono de Ajustes a la derecha. No SHALL tener franja superior propia ni hora, wifi o batería dibujados por la app.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

#### Scenario: Lineal sin Zwift en vertical
- **WHEN** se elige el estado sin Zwift en vertical
- **THEN** se ven en la fila de la plataforma los iconos del Neo y del Square normales y el de Zwift atenuado con la marca girando, «Lineal · 24 marchas» con Ajustes a su derecha, «14» con «de 24» y la escalera, potencia y cadencia en una fila, y el aviso «+1 marcha», sin desplazamiento

#### Scenario: Estados de conexión en vertical
- **WHEN** se eligen los estados Square sin conexión y sin dispositivos en vertical
- **THEN** los iconos afectados se ven tachados, la barra enlaza con Dispositivos, y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Estados de reconexión en vertical
- **WHEN** se eligen los estados Neo conectando, Square reconectando y los dos conectando en vertical
- **THEN** los iconos afectados están atenuados con la marca girando, el pie dice cómo despertarlos como en horizontal y nada se mueve respecto al estado sin Zwift

#### Scenario: Estados con Zwift en vertical
- **WHEN** se eligen los estados ruta de Zwift, ERG de Zwift, tope de potencia en ERG y sin wifi en vertical
- **THEN** se ven como en horizontal, con la barra de conexiones en la fila de la plataforma, la potencia objetivo de ERG en el lugar de la marcha y nada movido respecto al estado sin Zwift, a 360 dp de ancho sin desplazamiento

#### Scenario: Peor caso de la fila de la plataforma
- **WHEN** se elige el estado «Conectado · sin controlador» con los cuatro iconos de conexión a 360 dp de ancho
- **THEN** el estado se lee entero, en una o dos líneas, y los cuatro iconos se ven enteros y juntos a su derecha, sin solaparse

#### Scenario: Botones y seis datos en vertical
- **WHEN** se elige el estado con botones − y + en vertical
- **THEN** los botones van al pie, de 72 dp, y los datos siguen visibles sin desplazamiento, con la misma cifra que en el estado de 6 datos sin botones

### Requirement: Acceso a Ajustes en las maquetas de rodaje
Todos los estados de las maquetas de rodaje, en horizontal y en vertical, SHALL mostrar el botón con el icono de Ajustes, enlazado con la maqueta de Ajustes, con descripción accesible «Ajustes» y objetivo táctil de `touch-min`, en los dos temas: a la derecha de la barra de conexiones en horizontal, y al final de la fila del modo (SIM o ERG) en vertical.

#### Scenario: Horizontal
- **WHEN** se abre `maquetas/rodaje.html` en cualquier estado
- **THEN** el icono de Ajustes está en la línea superior, a la derecha de la barra de conexiones y separado de ella 12 dp, y al tocarlo se abre `maquetas/ajustes.html`

#### Scenario: Vertical a 360 dp
- **WHEN** se abre `maquetas/rodaje-vertical.html` en el estado «Sin Zwift · Neo y Square conectando» con el marco a 360 dp de ancho
- **THEN** la barra de conexiones queda a la derecha de la fila de la plataforma, y debajo caben en una sola fila el modo con la transmisión y el icono de Ajustes al final

#### Scenario: Vertical en ERG
- **WHEN** se abren los estados de ERG de Zwift y de ERG manual en vertical
- **THEN** el icono de Ajustes está al final de la fila del modo («ERG ZWIFT» o «ERG MANUAL») y no empuja ni tapa su texto

#### Scenario: Rodaje en claro
- **WHEN** se abre `maquetas/rodaje.html#tema=claro` y `maquetas/rodaje-vertical.html#tema=claro`
- **THEN** la pantalla de rodaje y el icono de Ajustes se ven con los valores claros de los tokens y el icono se distingue del fondo

#### Scenario: Sin dispositivos
- **WHEN** se elige el estado «Sin Zwift · sin dispositivos»
- **THEN** el icono de Ajustes sigue en su sitio y la barra muestra los iconos del Neo y del Square tachados

## REMOVED Requirements

### Requirement: Indicadores del móvil en las maquetas de rodaje
**Reason**: Rodaje deja de mostrar la hora, el wifi y la batería del móvil por su cuenta; las muestra la barra de estado del sistema.
**Migration**: Se quitan de las maquetas los estados de batería baja, móvil cargando y cámara en el centro, y la hora, el wifi y la batería de todos los estados. El estado «sin wifi» se queda, con el estado de la plataforma «Sin wifi» y la Z de Zwift tachada.
