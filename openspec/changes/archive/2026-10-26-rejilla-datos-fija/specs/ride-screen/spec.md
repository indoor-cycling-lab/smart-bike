## ADDED Requirements

### Requirement: Área de datos fija
La rejilla de datos de Rodaje SHALL ocupar un área fija: el 40 % del alto útil de la pantalla en vertical y el 40 % del ancho útil en horizontal. El área SHALL NOT cambiar con la transmisión, el modo (SIM o ERG), el estado de las conexiones ni el aviso del pie; solo los botones − y + pueden reducirla, sin bajar la cifra de los datos. La zona de la marcha SHALL ocupar el resto y su cifra SHALL reducirse lo necesario para caber, sin pasar de su tamaño normal.

#### Scenario: Misma área en lineal y con la Shimano
- **WHEN** el móvil está en vertical con 4 datos y se cambia de la transmisión lineal a la Shimano
- **THEN** la rejilla de datos tiene el mismo alto, la misma posición y la misma cifra, y solo cambia la zona de la marcha

#### Scenario: Misma área en ERG
- **WHEN** se entra en ERG manual con 4 datos elegidos
- **THEN** la potencia objetivo ocupa la zona de la marcha y la rejilla de datos mantiene su área y su cifra

#### Scenario: Área en horizontal
- **WHEN** el móvil está en horizontal, sin botones, con 2 datos
- **THEN** la columna de datos ocupa el 40 % del ancho útil y la marcha, con su escalera o su cassette, el resto

#### Scenario: Marcha que no cabe
- **WHEN** el móvil está en horizontal con los botones activados, la Shimano en la 52×17 y 6 datos
- **THEN** «52×17» y los platos se ven completos, más pequeños que sin botones, sin cortarse ni invadir la columna de datos

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados y los datos muestran «—»
- **THEN** la rejilla tiene la misma área y la misma cifra que con los dispositivos conectados

### Requirement: Espacio de los botones − y +
Al activar los botones − y +, el espacio que ocupan SHALL salir primero de lo que le sobra a la rejilla de datos sin bajar su cifra (alto en vertical, ancho en horizontal) y, lo que falte, de la zona de la marcha. La cifra de los datos SHALL ser la misma con y sin botones.

#### Scenario: Dos datos en vertical
- **WHEN** el móvil está en vertical con 2 datos y se activan los botones
- **THEN** los datos se ven con la misma cifra que sin botones, la rejilla se acorta para dejar sitio a los botones y la marcha no cambia de tamaño

#### Scenario: Seis datos en horizontal
- **WHEN** el móvil está en horizontal con 6 datos y se activan los botones
- **THEN** la columna de datos y su cifra no cambian y la zona de la marcha se estrecha para dejar sitio a la columna de botones, con la marcha más pequeña si no cabe

#### Scenario: Desactivar los botones
- **WHEN** con los botones activados y 5 datos se desactivan en Ajustes y se vuelve a Rodaje
- **THEN** la rejilla recupera el 40 % y la cifra de los datos es la misma que con los botones

## MODIFIED Requirements

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo las conexiones en una sola línea, el modo con la transmisión, la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas en su área fija, al pie el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados, la marcha es la 14 y los datos son los de por defecto
- **THEN** se ven, sin desplazamiento, los chips del Neo y del Square, «SIM» y «Lineal · 24 marchas», «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Seis datos en vertical
- **WHEN** el móvil está en vertical con los 6 datos elegidos
- **THEN** los datos se ven en 2 columnas y 3 filas, sin desplazamiento y sin cifras cortadas

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los chips «Neo» y «Square» se ven con el anillo vacío y el borde de alerta, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Conexiones en una línea en vertical
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión y Zwift esperando
- **THEN** los tres chips caben en una sola línea, y al pasar los tres a conectados la marcha y los datos no se mueven

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca Neo o Square para conectarlos»

#### Scenario: Cinco datos en vertical
- **WHEN** el móvil está en vertical con 5 datos elegidos
- **THEN** los cuatro primeros se ven en 2 columnas y 2 filas y el quinto, debajo, centrado en el ancho de la rejilla

### Requirement: Botones − y + en Rodaje
Con «Botones − y + en pantalla» activado, Rodaje SHALL mostrar un botón + y un botón −, de al menos 72 dp, que hacen lo mismo que las acciones «+1 marcha» y «−1 marcha» del Square, con sus topes, avisos y vibración, y en ERG ajustan ±10 W. Desactivado (por defecto), SHALL NOT mostrarlos.

#### Scenario: Por defecto
- **WHEN** se abre la app por primera vez
- **THEN** Rodaje no muestra los botones − y +

#### Scenario: Subir con el botón
- **WHEN** los botones están activados, la marcha es la 14 y se toca +
- **THEN** la marcha pasa a la 15, el Neo recibe la resistencia nueva y el pie dice «+1 marcha»

#### Scenario: Tope con el botón
- **WHEN** los botones están activados, la marcha es la 1 y se toca −
- **THEN** la marcha sigue en la 1 y se ve «Tope · marcha más ligera»

#### Scenario: Con la Shimano
- **WHEN** los botones están activados, la Shimano está en automático en la 36×15 y se toca +
- **THEN** la marcha pasa a la 52×19, como con la palanca D2

#### Scenario: En ERG
- **WHEN** los botones están activados, se está en ERG manual a 200 W y se toca +
- **THEN** la potencia pasa a 210 W y el pie dice «+10 W»

#### Scenario: Sin Neo
- **WHEN** los botones están activados, el Neo no está conectado y se toca +
- **THEN** la marcha sube y el Neo la recibe al conectarse

#### Scenario: Seis datos con botones
- **WHEN** los botones están activados con 6 datos elegidos, en horizontal o en vertical
- **THEN** los botones y los 6 datos se ven a la vez, sin desplazamiento, con la misma cifra que sin botones
