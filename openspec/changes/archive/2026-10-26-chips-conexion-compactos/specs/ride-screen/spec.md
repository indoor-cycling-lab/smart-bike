## MODIFIED Requirements

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo las conexiones en una sola línea, el modo con la transmisión, la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas que ocupan el resto del alto, al pie el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

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

### Requirement: Conexiones en la barra superior
La barra superior SHALL mostrar un chip para el Neo, otro para el Square y otro para Zwift, con su nombre y la forma de su estado, sin el estado en palabra: el punto lleno si está conectado; el anillo de espera, girando, para el Neo y el Square mientras la app intenta conectarlo o reconectarlo y para Zwift mientras la app se anuncia en la red wifi sin Zwift conectado; y el anillo vacío con el borde del chip de alerta si no lo intenta o, para Zwift, sin wifi. El estado en palabra («conectando», «esperando», «sin conexión») SHALL leerse en la descripción accesible del chip y en Dispositivos, al tocarlo. Los chips SHALL caber en una línea y no cambiar de ancho al cambiar de estado.

#### Scenario: Todo conectado
- **WHEN** el Neo, el Square y Zwift están conectados
- **THEN** se ven los chips Neo, Square y Zwift con el punto lleno

#### Scenario: Sin Zwift
- **WHEN** el Neo y el Square están conectados, el móvil tiene wifi y Zwift no está conectado
- **THEN** se ven los chips Neo y Square con el punto lleno y el chip «Zwift» con el anillo de espera (girando), sin la palabra «esperando»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** el chip «Zwift» se ve con el anillo vacío y el borde de alerta y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Square reconectando
- **WHEN** se pierde la conexión con el Square y la app intenta recuperarla
- **THEN** el chip «Square» pasa al anillo de espera, sin cambiar de ancho, la marcha sigue visible y la potencia y la cadencia siguen actualizándose

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado y la app no lo intenta (no hay ninguno recordado, o el Bluetooth está apagado)
- **THEN** el chip «Square» se ve con el anillo vacío y el borde de alerta, y su descripción accesible dice «Square, sin conexión»

#### Scenario: Estado al tocar
- **WHEN** el chip «Square» tiene el anillo de espera y se toca
- **THEN** se abre Dispositivos, donde la fila del Square dice «Conectando»

#### Scenario: Zwift desconectado a mitad
- **WHEN** se rueda en Zwift y se cierra Zwift
- **THEN** el chip «Zwift» pasa al anillo de espera y el resto de la pantalla no cambia

### Requirement: Potencia objetivo en ERG de Zwift
En un entrenamiento ERG de Zwift, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia objetivo que se envía al Neo en grande, con la potencia que pide Zwift debajo y, si las palancas la han ajustado, el ajuste; la marcha SHALL NOT mostrarse mientras dure el ERG. La zona SHALL ir centrada en horizontal en su columna.

#### Scenario: Bloque sin ajuste
- **WHEN** Zwift pide 230 W y no se han tocado las palancas
- **THEN** se lee «230 W» en grande con «230 W de Zwift» debajo, sin ajuste, y no se ve la marcha ni su escalera

#### Scenario: Con ajuste de las palancas
- **WHEN** Zwift pide 230 W y se ha pulsado la palanca D1 una vez
- **THEN** se lee «240 W» en grande, «230 W de Zwift» y «+10 W con las palancas»

#### Scenario: ERG en vertical
- **WHEN** el móvil está en vertical y Zwift pide 230 W
- **THEN** la potencia objetivo ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Fin del entrenamiento
- **WHEN** Zwift vuelve a enviar el terreno tras un entrenamiento con la marcha 14
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG
- **WHEN** Zwift pide 230 W y se pierde la conexión con el Neo
- **THEN** la potencia objetivo sigue visible, la potencia y la cadencia muestran «—» y el chip del Neo pasa al anillo de espera

#### Scenario: Tope de potencia
- **WHEN** la potencia objetivo es 50 W y se pulsa la palanca I1
- **THEN** «50 W» no cambia y el pie dice «Tope · 50 W» durante 1,5 s

### Requirement: Potencia objetivo en ERG manual
En ERG manual, la pantalla de rodaje SHALL mostrar en el lugar de la marcha, en horizontal y en vertical, la potencia fijada en grande, sin línea de Zwift ni de ajuste, centrada en horizontal en su columna; la marcha SHALL NOT mostrarse mientras dure el ERG manual.

#### Scenario: ERG manual a 200 W
- **WHEN** se entra en ERG manual a 200 W
- **THEN** se lee «200 W» en grande, sin «de Zwift», y no se ve la marcha ni su escalera

#### Scenario: Ajuste
- **WHEN** en ERG manual a 200 W se pulsa la palanca D1
- **THEN** la cifra pasa a «210 W»

#### Scenario: ERG manual en vertical
- **WHEN** el móvil está en vertical y se está en ERG manual
- **THEN** la potencia fijada ocupa el lugar de la marcha, centrada en horizontal, y la potencia y la cadencia siguen en su fila, sin desplazamiento y sin que se mueva el resto de la pantalla

#### Scenario: Vuelta a SIM
- **WHEN** en ERG manual con la marcha 14 se vuelve a SIM
- **THEN** la pantalla vuelve a mostrar «14» con «de 24» y la escalera, y el modo «SIM»

#### Scenario: Neo desconectado en ERG manual
- **WHEN** en ERG manual a 200 W se pierde la conexión con el Neo
- **THEN** «200 W» sigue visible, la potencia y la cadencia muestran «—» y el chip del Neo pasa al anillo de espera

#### Scenario: Tope de potencia
- **WHEN** en ERG manual a 1000 W se pulsa la palanca D1
- **THEN** «1000 W» no cambia y el pie dice «Tope · 1000 W» durante 1,5 s

