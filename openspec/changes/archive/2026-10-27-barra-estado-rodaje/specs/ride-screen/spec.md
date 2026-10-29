## ADDED Requirements

### Requirement: Hora, wifi y batería del móvil en Rodaje
La pantalla de rodaje SHALL mostrar, aunque las barras del sistema estén ocultas, la hora, el estado del wifi y la batería del móvil. La hora SHALL verse como `hh:mm` de 24 horas, sin segundos, y SHALL cambiar en el minuto exacto. El wifi SHALL verse con un icono normal si el móvil tiene wifi y tachado, en el color de alerta, si no. La batería SHALL verse con un icono cuyo relleno sigue el nivel, con el porcentaje al lado. Ningún estado SHALL depender solo del color.

#### Scenario: Hora en 24 horas
- **WHEN** son las 14:32 y el sistema muestra la hora en formato de 12 horas
- **THEN** Rodaje muestra «14:32», sin segundos ni «p. m.»

#### Scenario: Cambio de minuto
- **WHEN** la hora pasa de 14:32 a 14:33
- **THEN** Rodaje pasa a mostrar «14:33» sin esperar a que se toque la pantalla ni se cambie nada

#### Scenario: Cambio de zona horaria
- **WHEN** el sistema cambia de zona horaria o de hora mientras se rueda
- **THEN** Rodaje muestra la hora nueva en cuanto cambia

#### Scenario: Con wifi
- **WHEN** el móvil tiene wifi
- **THEN** se ve el icono de wifi normal, sin marca

#### Scenario: Sin wifi
- **WHEN** el móvil pierde el wifi mientras se rueda
- **THEN** el icono de wifi se ve tachado en el color de alerta, el icono de Zwift también se ve tachado (spec «Conexiones en la barra superior») y la marcha, la potencia y la cadencia siguen como sin Zwift

#### Scenario: Wifi que vuelve
- **WHEN** el icono de wifi está tachado y el móvil recupera el wifi
- **THEN** el icono pasa a normal, sin que cambie el ancho del grupo ni se mueva el resto de la pantalla

#### Scenario: Batería con carga media
- **WHEN** la batería está al 82 % y el móvil no está cargando
- **THEN** se ve un icono de batería con aproximadamente cuatro quintos del relleno y «82 %» al lado

#### Scenario: Batería llena
- **WHEN** la batería está al 100 %
- **THEN** el icono se ve lleno y el texto dice «100 %», sin cortarse ni mover el resto del grupo

#### Scenario: Móvil cargando
- **WHEN** el móvil está conectado al cargador
- **THEN** el icono de batería lleva un rayo, también si está al 100 %, y deja de llevarlo al desconectar el cargador

#### Scenario: Batería baja
- **WHEN** la batería baja del 15 % y el móvil no está cargando
- **THEN** el icono se ve en el color de alerta con el relleno reducido a una franja mínima, el porcentaje sigue visible y no aparece ningún aviso emergente ni sonoro

#### Scenario: Batería baja cargando
- **WHEN** la batería está al 9 % y el móvil está cargando
- **THEN** el icono lleva el rayo y no se ve en el color de alerta

#### Scenario: Estados sin color
- **WHEN** la pantalla de rodaje se ve en escala de grises con el wifi tachado y la batería al 9 %
- **THEN** el wifi tachado se distingue del normal por su forma, y la batería baja se distingue de la media por el relleno y por el número

#### Scenario: Batería no disponible
- **WHEN** el sistema no da el nivel de la batería
- **THEN** no se ve el icono de batería ni el porcentaje, la hora y el wifi siguen y el resto de la pantalla no cambia

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al grupo con la hora 14:32, el wifi conectado y la batería al 82 % cargando
- **THEN** lo lee como un solo elemento: «14:32, wifi conectado, batería 82 %, cargando»

### Requirement: Colocación vertical de los indicadores del móvil
En vertical, el grupo de la hora, el wifi y la batería SHALL ir en la franja superior, a la izquierda de los enlaces de la app, con aire a su alrededor, y SHALL NOT ocupar el hueco de la cámara: con la cámara a la izquierda va a su derecha, con la cámara en el centro va a su izquierda y sin recorte en la franja va desde el margen normal.

#### Scenario: Cámara a la izquierda en vertical
- **WHEN** el móvil está en vertical con la cámara en la esquina superior izquierda
- **THEN** la hora, el wifi y la batería se ven a la derecha de la cámara, con aire entre ellos, sin tapar el recorte, y los enlaces de la app siguen a la derecha de la franja

#### Scenario: Cámara en el centro en vertical
- **WHEN** el móvil está en vertical con la cámara en el centro del borde superior
- **THEN** la hora, el wifi y la batería se ven a la izquierda de la cámara, los enlaces de la app a su derecha, y ninguno tapa el recorte

#### Scenario: Sin cámara en el borde
- **WHEN** el móvil está en vertical y no tiene recorte en el borde superior
- **THEN** el grupo se ve a la izquierda de la franja, desde el margen normal, y la franja mide al menos 48 dp

#### Scenario: Todo en la franja a 360 dp
- **WHEN** el móvil está en vertical a 360 dp de ancho, con cuatro enlaces y el porcentaje de batería en «100 %»
- **THEN** la hora, el wifi, la batería y los cuatro enlaces caben en la franja sin solaparse y sin cortarse, con la cámara centrada o a la izquierda

### Requirement: Colocación horizontal de los indicadores del móvil
En horizontal, el conjunto de la hora, el wifi y la batería SHALL ir centrado en la línea superior. El modo con la transmisión SHALL quedar a la izquierda, y las conexiones con Ajustes a la derecha, sin tocar la hora.

#### Scenario: Conjunto centrado en horizontal
- **WHEN** el móvil está en horizontal con la transmisión «Shimano 105 · automático» y las conexiones con Ajustes
- **THEN** la hora, el wifi y la batería se ven como un conjunto centrado en la línea superior, y ni el modo con la transmisión ni las conexiones la tocan

### Requirement: Indicadores del móvil solo informativos
La hora, el wifi y la batería SHALL NOT ser táctiles ni formar parte del objetivo táctil de la barra de conexiones. Rodaje SHALL seguir sin las barras del sistema y con la pantalla encendida.

#### Scenario: Tocar los indicadores
- **WHEN** se toca la hora, el wifi o la batería
- **THEN** no se abre ninguna pantalla; tocar la barra de conexiones sigue abriendo Dispositivos

#### Scenario: Las barras del sistema siguen ocultas
- **WHEN** se está en Rodaje con la hora, el wifi y la batería a la vista
- **THEN** la barra de estado y la de navegación del sistema siguen ocultas y la pantalla sigue encendida

## MODIFIED Requirements

### Requirement: Disposición en vertical
En vertical, la pantalla de rodaje SHALL apilar de arriba abajo la franja superior (la de la cámara y el estado del móvil, de al menos 48 dp de alto, con el resto de la pantalla debajo), con la hora, el wifi y la batería del móvil a la izquierda y la barra de conexiones a la derecha; el modo con la transmisión y el icono de Ajustes en una sola línea; la marcha (con su escalera en lineal, o con los platos y el cassette con la Shimano), los datos elegidos en 2 columnas en su área fija, al pie el aviso de acción o la ayuda para conectar y, si están activados, los botones − y +, todo visible sin desplazamiento.

#### Scenario: Sin Zwift en vertical
- **WHEN** el móvil está en vertical, el Neo y el Square están conectados, la marcha es la 14 y los datos son los de por defecto
- **THEN** se ven, sin desplazamiento, la hora, el wifi y la batería a la izquierda de la franja, la barra con los iconos del Neo y del Square a la derecha, «SIM» y «Lineal · 24 marchas» con el icono de Ajustes, «14» en grande con «de 24» y la escalera, y potencia y cadencia una al lado de la otra

#### Scenario: Seis datos en vertical
- **WHEN** el móvil está en vertical con los 6 datos elegidos
- **THEN** los datos se ven en 2 columnas y 3 filas, sin desplazamiento y sin cifras cortadas

#### Scenario: Tope en vertical
- **WHEN** el móvil está en vertical, la marcha es la 24 y se pulsa la palanca D1
- **THEN** «24» se ve atenuado con «Tope · marcha más dura» durante 1,5 s, sin que se mueva el resto de la pantalla

#### Scenario: Sin dispositivos en vertical
- **WHEN** el móvil está en vertical y el Neo y el Square no están conectados
- **THEN** los iconos del Neo y del Square se ven tachados, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Conexiones en una línea en vertical
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión, Zwift esperando y el mando esperando
- **THEN** los cuatro iconos caben en la franja superior, a la derecha, y debajo caben en una sola línea el modo con la transmisión y el icono de Ajustes; al pasar todo a conectado, la marcha y los datos no se mueven

#### Scenario: Móvil sin cámara en el borde
- **WHEN** el móvil está en vertical y no tiene cámara en el borde superior, de modo que no hay margen seguro arriba
- **THEN** la barra ocupa una franja propia de al menos 48 dp, la línea del modo con Ajustes queda debajo sin solaparse, y todo se ve sin desplazamiento

#### Scenario: Transmisión larga junto a Ajustes
- **WHEN** el móvil está en vertical a 360 dp, la transmisión es «Shimano 105 · automático» y el modo es SIM, o se está en ERG de Zwift
- **THEN** el modo con la transmisión se lee entero, en una o dos líneas, sin cortarse ni tapar el icono de Ajustes

#### Scenario: Shimano sin dispositivos en vertical
- **WHEN** el móvil está en vertical con la Shimano en la 36×17 y el Neo y el Square no están conectados
- **THEN** «36×17», los platos y el cassette siguen visibles, la potencia y la cadencia muestran «—» y el pie dice «Toca las conexiones para conectar el Neo y el Square»

#### Scenario: Cinco datos en vertical
- **WHEN** el móvil está en vertical con 5 datos elegidos
- **THEN** los cuatro primeros se ven en 2 columnas y 2 filas y el quinto, debajo, centrado en el ancho de la rejilla

#### Scenario: Los indicadores no mueven la marcha
- **WHEN** el móvil está en vertical y el wifi pasa de conectado a tachado, la batería baja del 100 % al 9 % o la hora cambia de 09:59 a 10:00
- **THEN** la franja, la marcha y los datos no se mueven ni cambian de tamaño
