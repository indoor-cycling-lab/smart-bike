## ADDED Requirements

### Requirement: Pulso por Bluetooth
Con «Solo Bluetooth» y un pulsómetro recordado, la app SHALL ofrecer el pulso en el mismo dispositivo y el mismo anuncio que el rodillo, con el mismo comportamiento que por wifi (spec `platform-bridge`, «Pulso hacia la plataforma»). Mientras no se compruebe con el equipo real, la compatibilidad SHALL marcarlo «a validar».

#### Scenario: Elegir el pulso por Bluetooth
- **WHEN** el modo es «Solo Bluetooth», hay un pulsómetro recordado y se abre el emparejamiento de Zwift
- **THEN** el móvil aparece también en la lista de pulso y, elegido, Zwift recibe los bpm de la banda

#### Scenario: Sin pulsómetro recordado
- **WHEN** el modo es «Solo Bluetooth» y no hay pulsómetro recordado
- **THEN** el móvil no aparece en la lista de pulso

### Requirement: Zwift como rodillo de Zwift por Bluetooth
Con «Solo Bluetooth» y el mando dentro, si la plataforma manda el terreno o el ERG por el mando, con el protocolo de Zwift, en lugar de por FTMS, la app SHALL atenderlo como en «Solo wifi (modo Zwift Ride)» y SHALL enviarle la potencia y la cadencia también por ese protocolo. El detalle del modo SHALL avisar de que la app de entrenamiento puede mostrar su propia marcha.

#### Scenario: Zwift usa su protocolo
- **WHEN** el modo es «Solo Bluetooth» con un Square recordado y Zwift manda una pendiente de 4 % por el mando
- **THEN** el Neo simula +4 % con la marcha de la app y Zwift ve la potencia y la cadencia del Neo

#### Scenario: Zwift usa FTMS
- **WHEN** el modo es «Solo Bluetooth» con un Square recordado y Zwift manda la pendiente por FTMS
- **THEN** la app la atiende por FTMS y no envía nada por el protocolo de Zwift

## MODIFIED Requirements

### Requirement: Mismo comportamiento que por wifi
Con el modo por Bluetooth, la app SHALL recibir de la plataforma el terreno y la potencia objetivo de un entrenamiento, y SHALL enviarle la potencia y la cadencia del Neo, con el mismo comportamiento que el modo «Wifi + Bluetooth»: el Neo simula la pendiente de la plataforma con el ajuste de la marcha de la app, y las palancas del Square cambian de marcha sin retraso añadido.

#### Scenario: Subida en Zwift
- **WHEN** el modo es «Solo Bluetooth», Zwift envía una pendiente de 4 % y la marcha es la 12
- **THEN** el Neo simula +4 % y Zwift ve la potencia y la cadencia del Neo

#### Scenario: Entrenamiento ERG
- **WHEN** Zwift pide 200 W en un entrenamiento por Bluetooth
- **THEN** el Neo mantiene 200 W y Rodaje muestra la potencia objetivo, como en el modo por wifi

#### Scenario: Latencia de la palanca
- **WHEN** se rueda en una subida de Zwift por Bluetooth y se pulsa la palanca D1
- **THEN** la latencia de la palanca sigue por debajo de 100 ms

#### Scenario: Neo sin conexión
- **WHEN** la plataforma está conectada por Bluetooth y el Neo todavía no se ha conectado
- **THEN** la plataforma sigue viendo el rodillo conectado con potencia y cadencia 0 y, al conectarse el Neo, empieza a recibir las suyas

### Requirement: Modo por validar
El modo por Bluetooth SHALL constar en la tabla de compatibilidad como validado solo en lo que se haya comprobado con el equipo real, y la app SHALL NOT presentar como confirmado lo que no lo esté.

#### Scenario: Marcado a validar
- **WHEN** se consulta la compatibilidad de Zwift
- **THEN** «Solo Bluetooth» consta como «a validar» en Zwift en Apple TV, en una subida real, en un entrenamiento ERG y en una sesión larga

#### Scenario: Lo comprobado con Zwift en PC
- **WHEN** se consulta lo validado del modo por Bluetooth con Zwift en PC
- **THEN** consta que Zwift lo lista como rodillo, potencia y cadencia, que recibe la potencia y la cadencia y que acepta el control del rodillo, y que no lo reconoce como el mismo dispositivo en otra sesión

### Requirement: Rodillo visible por Bluetooth
Con el modo «Solo Bluetooth» elegido y un rodillo recordado, mientras dura la sesión, el Bluetooth del móvil está encendido y la app tiene los permisos y el modo periférico, la app SHALL anunciarse por Bluetooth como un rodillo que la plataforma lista como rodillo controlable, fuente de potencia y fuente de cadencia, sin que el usuario active nada más en la app. SHALL funcionar sin red wifi. Sin rodillo recordado SHALL NOT anunciarse.

#### Scenario: Emparejar en Zwift
- **WHEN** el modo es «Solo Bluetooth», hay un Neo recordado, la app está abierta y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** el móvil aparece por Bluetooth, con su nombre Bluetooth, en las listas de potencia, cadencia y rodillo controlable, y se puede elegir en las tres (y también como mando si hay un Square recordado: ver «Mando de Zwift dentro del mismo dispositivo»)

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi y el modo es «Solo Bluetooth»
- **THEN** la app se anuncia igual por Bluetooth y el estado del rodillo no dice «Sin wifi»

#### Scenario: Sin rodillo recordado
- **WHEN** el modo es «Solo Bluetooth» y no hay rodillo recordado
- **THEN** la app no se anuncia por Bluetooth y Conexión dice que no se publica nada hasta recordar un rodillo

#### Scenario: Volver a otra sesión
- **WHEN** el móvil se emparejó en Zwift en una sesión anterior, se cierra la app con «Terminar» y se abren la app y Zwift otra vez
- **THEN** Zwift puede no reconocerlo como el mismo dispositivo: hay que quitar el anterior en su pantalla de emparejamiento y buscarlo de nuevo, y el móvil aparece otra vez con su nombre Bluetooth

### Requirement: Mando de Zwift dentro del mismo dispositivo
Con «Solo Bluetooth» y un Square recordado, la app SHALL ofrecer a Zwift el mando dentro del mismo dispositivo y del mismo anuncio que el rodillo, sin interruptor, de modo que los botones del Square lleguen a Zwift a través de la app y el Square no se empareje en Zwift. Sin Square recordado SHALL NOT ofrecerlo. Las palancas del Square SHALL seguir cambiando la marcha de la app.

#### Scenario: El móvil también es un mando
- **WHEN** el modo es «Solo Bluetooth», hay un Square recordado y se abre el emparejamiento de Zwift
- **THEN** «OnePlus 8» aparece también en la lista de mandos y se puede elegir junto al rodillo, la potencia y la cadencia

#### Scenario: Sin Square recordado
- **WHEN** el modo es «Solo Bluetooth» y no hay Square recordado
- **THEN** el móvil no aparece en la lista de mandos y Conexión dice que el mando no se ofrece

#### Scenario: Botones del Square en Zwift
- **WHEN** el mando está conectado y se pulsan A o la cruceta en el Square
- **THEN** Zwift reacciona como con un mando de Zwift, sin que el Square esté emparejado en Zwift

#### Scenario: Palancas
- **WHEN** el mando está conectado y se pulsa la palanca D1
- **THEN** cambia la marcha de la app y no se envía a Zwift como botón de cambio

#### Scenario: Función de mando sin asignar
- **WHEN** una función del mando de Zwift no tiene botón asignado
- **THEN** ningún botón del Square la envía a Zwift

#### Scenario: Zwift no conectado
- **WHEN** se pulsa un botón del Square con la app anunciada y sin que Zwift esté conectado al mando
- **THEN** no se envía nada ni hay ningún error, y el mando consta como «Esperando»

#### Scenario: Bluetooth apagado
- **WHEN** se apaga el Bluetooth del móvil con este modo elegido
- **THEN** el rodillo y el mando constan como «Sin Bluetooth», y al encenderlo la app vuelve a anunciarse con los dos

### Requirement: Cambio hacia y desde el modo por Bluetooth
La app SHALL permitir cambiar a este modo y desde él con la sesión abierta, desde Conexión. Al cambiar, SHALL soltar todas las funciones del mando, dejar de ofrecer lo anterior, empezar a ofrecer lo nuevo y mantener lo último que pidió la plataforma durante los 30 s habituales, sin cambiar la marcha ni lo que simula el Neo. La plataforma SHALL tener que reconectarse y emparejar el dispositivo nuevo.

#### Scenario: De wifi a Bluetooth rodando
- **WHEN** Zwift está conectado por wifi con un Square recordado y se elige «Solo Bluetooth»
- **THEN** la app deja de anunciarse por wifi y el mando aparte deja de anunciarse, se anuncia por Bluetooth con el rodillo y el mando en un solo dispositivo y la marcha y lo que simula el Neo no cambian

#### Scenario: De Bluetooth a wifi rodando
- **WHEN** Zwift está conectado por Bluetooth con un Square recordado y se elige «Wifi + Bluetooth»
- **THEN** la app deja de anunciarse como rodillo por Bluetooth, se anuncia por wifi con el nombre elegido y el mando vuelve a anunciarse aparte por Bluetooth

#### Scenario: Modo guardado
- **WHEN** se elige «Solo Bluetooth» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y la app se anuncia por Bluetooth desde el primer momento

### Requirement: Requisitos del móvil
El modo por Bluetooth SHALL requerir el Bluetooth encendido, los permisos de conectar y de anunciar y un móvil que pueda anunciarse como dispositivo Bluetooth. Si falta algo, la app SHALL seguir rodando sin la plataforma, SHALL decir qué falta y SHALL volver a anunciarse sola al cumplirse todo.

#### Scenario: Bluetooth apagado
- **WHEN** el modo es «Solo Bluetooth» y se apaga el Bluetooth del móvil
- **THEN** las marchas siguen funcionando con la pendiente base, el rodillo dice «Sin Bluetooth» en Conexión y explica que hay que encender el Bluetooth, y al encenderlo la app vuelve a anunciarse sin tocar nada más

#### Scenario: Sin permiso de anunciar
- **WHEN** el modo es «Solo Bluetooth» y falta el permiso de anunciar
- **THEN** el rodillo dice «Sin Bluetooth» y explica que hay que conceder el permiso, y al concederlo la app se anuncia

#### Scenario: Móvil sin modo periférico
- **WHEN** el modo es «Solo Bluetooth» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** el rodillo dice «No disponible» y explica que este móvil no puede publicar el rodillo por Bluetooth, y el resto de la app sigue funcionando
