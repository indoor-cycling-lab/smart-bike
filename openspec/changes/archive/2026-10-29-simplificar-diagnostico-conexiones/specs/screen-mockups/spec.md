# Spec Delta

## MODIFIED Requirements

### Requirement: Maqueta de Diagnóstico con sesión de rodaje
La maqueta de Diagnóstico SHALL mostrar el botón de volver a Ajustes, la sección «Datos de conexión» (solo datos, sin estado de conexión ni acciones de búsqueda), los botones del Square con la máscara, el último gesto con su acción y la simulación (marcha, modelo, pendiente base o de Zwift, ajuste, lo enviado al rodillo, la latencia de la palanca y, con Zwift por wifi, lo último que envía su simulación de marchas virtuales). SHALL NOT enlazar con Botones ni con Ajustes salvo por el botón de volver.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando con Zwift
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación con la pendiente de Zwift

#### Scenario: Sin Zwift
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** «Datos de conexión» muestra solo las filas con datos, hay un botón de volver a Ajustes, «Último gesto» muestra «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación muestra la pendiente base en lugar de la de Zwift, con la latencia de la palanca

#### Scenario: Sin estado de conexión
- **WHEN** se recorren todos los estados de la maqueta de Diagnóstico
- **THEN** ninguno muestra filas de estado del rodillo, el Square, Zwift o el controlador, ni «Buscar dispositivos», ni avisos de conexión

#### Scenario: Enlace a Botones
- **WHEN** se recorre la sección de botones del Square de la maqueta de Diagnóstico
- **THEN** no hay enlace «Asignación de botones»: Botones se abre desde la entrada «Botones» de Ajustes

#### Scenario: Tráfico al abrir
- **WHEN** se abre la maqueta de Diagnóstico en cualquier estado
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar», y al marcarla aparecen el volcado del estado y los dos botones

### Requirement: Estados de la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL conservar los estados que ya tenía (todo conectado por defecto, buscando, conectando al abrir, rodillo no encontrado, Square reconectando, Bluetooth apagado, sin permiso, límites del control manual, rodando con Zwift, ruta y ERG de Zwift en lineal, sin wifi y los de batería, pulso, ventilador, nombre ante la app y Solo Bluetooth) y SHALL añadir un estado con todos los datos a la vez, de modo que los enlaces y las capturas no cambien, y cada uno SHALL pintar en «Datos de conexión» solo sus datos, y en las demás secciones lo que ya pintaba.

#### Scenario: Dispositivo no encontrado
- **WHEN** se elige el estado rodillo no encontrado
- **THEN** no hay aviso ni fila del rodillo, los botones de envío al rodillo están deshabilitados con su explicación y «Enviada al rodillo» muestra «—»

#### Scenario: Conectando al abrir
- **WHEN** se elige el estado conectando al abrir
- **THEN** no hay filas del rodillo ni del Square ni ayuda para despertarlos, ni «Buscar dispositivos», y las demás secciones muestran lo que corresponde sin dispositivos

#### Scenario: Conexión perdida
- **WHEN** se elige el estado Square reconectando
- **THEN** no hay aviso de reconexión, ninguna tecla se ve pulsada, la máscara dice «—» y la sección de botones dice «Conecta el Square para ver sus botones.»

#### Scenario: Bluetooth y permisos
- **WHEN** se eligen los estados Bluetooth apagado y sin permiso
- **THEN** no hay avisos ni botones «Activar Bluetooth» o «Abrir ajustes», y las demás secciones muestran lo que corresponde sin Bluetooth

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado

#### Scenario: Zwift esperando
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** la fila «App de entrenamiento» dice «—», sin la ayuda de emparejamiento, y «Nombre anunciado» dice «Smart Bike»

#### Scenario: Simulación de Zwift
- **WHEN** se elige el estado de todo por wifi con la simulación de Zwift
- **THEN** la fila «Simulación de Zwift» está en la sección Simulación, con la pendiente, la relación de su marcha, Crr, CW, las masas y la hora, y «Datos de conexión» no la tiene

#### Scenario: Ruta de Zwift en lineal
- **WHEN** se elige el estado ruta de Zwift
- **THEN** la fila «App de entrenamiento» dice la dirección del PC, la simulación dice modo SIM · Zwift con la pendiente y el terreno de Zwift, el ajuste de la marcha, lo enviado al rodillo y el último mensaje de Zwift, y, al marcar «Tráfico en directo», el tráfico tiene filas «Zwift» y «→ Zwift»

#### Scenario: ERG de Zwift en lineal
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la simulación dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al rodillo 240 W, con la marcha visible

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi
- **THEN** la fila «App de entrenamiento» dice «—», no hay aviso y «Nombre anunciado» muestra el nombre que se anunciará

#### Scenario: Batería del Square
- **WHEN** se eligen los estados de batería (80 %, sin lectura, baja y Square reconectando con la última conocida)
- **THEN** la fila de batería dice «80 %», «Sin lectura todavía», «15 % · baja» y «Última lectura: 60 %», sin avisos: el aviso de batería baja está en Dispositivos

#### Scenario: Todos los datos a la vez
- **WHEN** se elige el estado de todos los datos
- **THEN** «Datos de conexión» muestra a la vez la app de entrenamiento («Desde 192.168.0.10»), el nombre anunciado, la batería del Square («15 % · baja»), el pulso («128 bpm») y el ventilador («Manual · 40 %»), la simulación de Zwift está en Simulación, y todo se ve a 360 dp sin desplazamiento horizontal

#### Scenario: Pulso y ventilador
- **WHEN** se eligen los estados del pulsómetro y del ventilador
- **THEN** sus filas dicen el pulso o el modo y la velocidad con su antigüedad, o «—» sin lectura o sin notificación, sin estado de conexión ni avisos

### Requirement: Maqueta de Dispositivos de la app
La maqueta de Dispositivos SHALL mostrar una sección por tipo (Rodillo, Controlador, Pulsómetro y Ventilador), cada una con su fila (modelo recordado o «Ninguno recordado», estado en palabra y ayuda), «Buscar» sin recordado, u «Olvidar» y, sin conexión, «Reconectar», y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Neo no encontrado, Square no encontrado, Square con la batería baja, sin permiso de Bluetooth, Bluetooth apagado y los del pulsómetro y el ventilador. SHALL NOT mostrar lo publicado ni «Buscar dispositivos».

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** las cuatro filas dicen «Ninguno recordado», cada sección tiene «Buscar» y ninguna tiene «Olvidar»

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** las filas Rodillo y Controlador dicen «Conectado», con «Tacx Neo 12345» y «Elite Square» debajo, y tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila Rodillo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Neo no encontrado
- **WHEN** se elige el estado Neo no encontrado
- **THEN** la fila Rodillo dice «No encontrado» y se ve el aviso de error «No se encuentra el Neo. Comprueba que está enchufado, pedalea para despertarlo y vuelve a buscar.»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila Controlador dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Sin permiso de Bluetooth
- **WHEN** se elige el estado sin permiso de Bluetooth
- **THEN** se ve el aviso de error «Sin permiso para buscar dispositivos Bluetooth. Concédelo en los ajustes de la app.» con el botón «Abrir ajustes», y las filas recordadas dicen «Sin conexión»

#### Scenario: Batería baja del Square
- **WHEN** se elige el estado Square con la batería baja
- **THEN** la fila Controlador dice «Conectado» y, bajo ella, el aviso informativo «Batería del Square baja (15 %). Revisa la pila antes de rodar.» con icono y texto, y la fila conserva «Olvidar»

#### Scenario: Bluetooth apagado
- **WHEN** se elige el estado Bluetooth apagado
- **THEN** se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y las filas recordadas dicen «Sin conexión»

#### Scenario: Sin Zwift
- **WHEN** se revisan todos los estados de la maqueta de Dispositivos
- **THEN** ninguno muestra una fila de Zwift ni de lo publicado

#### Scenario: Botones sin el nombre del tipo
- **WHEN** se revisan los botones de cualquier sección
- **THEN** dicen «Buscar», «Reconectar» u «Olvidar» según el estado, y su etiqueta accesible nombra el tipo («Reconectar controlador»)

### Requirement: Iconos de los enlaces en las maquetas
Las maquetas de Dispositivos y de Diagnóstico SHALL mostrar los iconos de los enlaces: Dispositivos, junto al nombre del Neo y del Square, el mismo icono que la barra de conexiones de Rodaje, en todos sus estados y en los dos temas; Diagnóstico, en «Datos de conexión», el icono de Conexión (genérico, no el de ninguna plataforma) en las filas de la app de entrenamiento y del nombre anunciado y, si las lleva, los de las filas del pulsómetro, el ventilador y el Square, sin filas del Neo ni del Square como dispositivos; el icono de Zwift solo en la simulación de Zwift.

#### Scenario: Dispositivos como leyenda
- **WHEN** se abre `maquetas/dispositivos.html` en el estado todo conectado
- **THEN** cada fila lleva delante el icono de su enlace: un rayo para el Neo y el manillar para el Square, y no hay filas de Zwift ni del controlador publicado

#### Scenario: Diagnóstico con los mismos iconos
- **WHEN** se abre `maquetas/diagnostico.html` en el estado rodando con Zwift
- **THEN** la app de entrenamiento y el nombre anunciado llevan el icono de Conexión y no el de Zwift, que solo lleva la «Simulación de Zwift», y no hay filas del rodillo ni del Square como dispositivos

### Requirement: Maqueta de Diagnóstico con el modo por Bluetooth
La maqueta de Diagnóstico SHALL tener estados del modo por Bluetooth, con una app de entrenamiento conectada y con el Bluetooth apagado, que muestren en «Datos de conexión» la fila «App de entrenamiento» con «Por Bluetooth» o «—» y el nombre Bluetooth del móvil en lugar del nombre anunciado, sin ningún aviso de wifi ni de Bluetooth.

#### Scenario: Zwift conectado por Bluetooth
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-rodando`
- **THEN** la fila «App de entrenamiento» dice «Por Bluetooth», el nombre anunciado es el nombre Bluetooth del móvil y no hay aviso de wifi

#### Scenario: Bluetooth apagado
- **WHEN** se abre `maquetas/diagnostico.html#estado=bt-sin-bluetooth`
- **THEN** la fila «App de entrenamiento» dice «—» y no hay aviso de Bluetooth apagado: está en Dispositivos y en Conexión

### Requirement: Simulación de Zwift en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL tener un estado con la simulación de marchas virtuales de Zwift en la sección Simulación, con la pendiente, los coeficientes, las masas, la relación de la marcha de Zwift y la hora, y SHALL NOT mostrarla en «Datos de conexión».

#### Scenario: Simulación en Diagnóstico
- **WHEN** se elige el estado de simulación de Zwift de la maqueta de Diagnóstico
- **THEN** en la sección Simulación se ve la pendiente de Zwift y la relación de su marcha, y aparte lo que simula el rodillo con la marcha de la app

#### Scenario: Sin simulación
- **WHEN** se elige el estado rodando con Zwift de la maqueta de Diagnóstico
- **THEN** no aparece la línea de la simulación de Zwift

### Requirement: Pendiente limitada en la maqueta de Diagnóstico
La maqueta de Diagnóstico SHALL mostrar en Simulación el modo de bajadas y SHALL tener un estado con la pendiente limitada por él, con la indicación en texto y la pendiente sin límite, y SHALL tener el acceso a Ajustes junto al de Botones.

#### Scenario: Bajada de Zwift limitada
- **WHEN** se elige el estado de pendiente limitada
- **THEN** Simulación muestra pendiente Zwift −7,0 %, ajuste −11,0 %, enviada al rodillo −7,0 % y «Limitada por Bajadas: sin límite sería −18,0 %»

#### Scenario: Sin límite
- **WHEN** se elige cualquier otro estado con el rodillo conectado
- **THEN** Simulación muestra el modo de bajadas y no aparece la indicación de límite

### Requirement: Diagnóstico con la Shimano
La maqueta de Diagnóstico SHALL tener un estado con la Shimano en el que Simulación muestra la marcha como plato × piñón con su relación y el ajuste por la marcha.

#### Scenario: Simulación con la Shimano
- **WHEN** se elige el estado de la Shimano
- **THEN** Simulación dice marcha 52×17 · relación 3,06, ajuste +5,1 % y enviada al rodillo +5,1 %, y «Último gesto» dice «Palanca D2 · pulsar · +1 marcha»
