# Spec Delta

## Purpose

Define la pestaña «Plataformas»: lo que la app simula y publica ante las apps de indoor cycling. El usuario elige una plataforma y un modo de conexión compatible, sin combinaciones que haya que acertar, y ve el estado de esa conexión.

## ADDED Requirements

### Requirement: Pestaña Plataformas
La app SHALL tener una pestaña «Plataformas» en la barra inferior, entre Dispositivos y Ajustes, que SHALL mostrar, de arriba abajo: la plataforma elegida, los modos de conexión que admite, el mando si esa plataforma lo tiene, el nombre ante la app y el estado de la conexión. SHALL abrirse sin botón de volver. Un cambio de cualquier elección SHALL aplicarse al momento, también rodando, y guardarse.

#### Scenario: Abrir Plataformas
- **WHEN** se toca «Plataformas» en la barra inferior
- **THEN** se ve la plataforma Zwift elegida, sus dos modos de conexión con el elegido marcado, el nombre ante la app y el estado de Zwift, sin botón de volver

#### Scenario: Valores por defecto
- **WHEN** se abre Plataformas por primera vez
- **THEN** la plataforma es Zwift, el modo es «Rodillo por wifi (FTMS)», «Mando de Zwift» está activado y el nombre es «Smart Bike»

#### Scenario: Cambio rodando
- **WHEN** se rueda con el Neo y el Square y se cambia de modo en Plataformas
- **THEN** la marcha y lo que simula el Neo no cambian y la app pasa a publicar el modo nuevo, como en «Cambio de modo de conexión»

### Requirement: Una sola plataforma y un solo modo
La pantalla SHALL permitir elegir una única plataforma y un único modo de conexión a la vez, y SHALL NOT ofrecer ninguna combinación que no esté implementada. Al elegir una plataforma, la pantalla SHALL mostrar solo los modos que la app admite para ella, con el recomendado marcado y elegido si no había un modo válido. Los modos que aún no existen SHALL NOT aparecer.

#### Scenario: Plataforma con varios modos
- **WHEN** se elige Zwift
- **THEN** se ofrecen «Rodillo por wifi (FTMS)», marcado como recomendado, y «Todo por wifi (modo Zwift Ride)», y ninguno más

#### Scenario: Un solo modo elegido
- **WHEN** está elegido «Rodillo por wifi (FTMS)» y se toca «Todo por wifi (modo Zwift Ride)»
- **THEN** pasa a estar elegido el segundo y deja de estarlo el primero

#### Scenario: Plataforma única
- **WHEN** Zwift es la única plataforma implementada
- **THEN** la pantalla muestra Zwift como la plataforma elegida y no ofrece ninguna otra

### Requirement: Rodillo por wifi con mando opcional
Con el modo «Rodillo por wifi (FTMS)», la pantalla SHALL mostrar el interruptor «Mando de Zwift», activado por defecto, con una ayuda que diga que con él los botones del Square llegan a Zwift por Bluetooth a través de la app y que el Square ya no se empareja en Zwift. Desactivarlo SHALL dejar de anunciar el mando y SHALL NOT afectar al rodillo.

#### Scenario: Mando activado
- **WHEN** el modo es «Rodillo por wifi (FTMS)» y «Mando de Zwift» está activado
- **THEN** Zwift ve el rodillo por wifi y el mando por Bluetooth

#### Scenario: Mando desactivado
- **WHEN** se desactiva «Mando de Zwift»
- **THEN** el mando deja de anunciarse, el rodillo sigue conectado por wifi y los botones del Square no llegan a Zwift por la app

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth y el modo es «Rodillo por wifi (FTMS)»
- **THEN** «Mando de Zwift» sigue pudiéndose cambiar y su ayuda dice que este móvil no puede hacer de mando por Bluetooth

### Requirement: Zwift Ride con el mando dentro de DirCon
Con el modo «Todo por wifi (modo Zwift Ride)», la pantalla SHALL explicar que Zwift verá un solo dispositivo por wifi, que mostrará su propia marcha, que no es la de la app, y que hay que emparejar de nuevo en Zwift. SHALL NOT mostrar el interruptor «Mando de Zwift» ni ofrecer un mando por Bluetooth: el mando SHALL ir siempre dentro del mismo dispositivo por wifi.

#### Scenario: Elegir Zwift Ride
- **WHEN** se elige «Todo por wifi (modo Zwift Ride)»
- **THEN** queda elegido, se guarda, la ayuda dice que Zwift ve un solo dispositivo, que mostrará su marcha y que hay que emparejar de nuevo, y no aparece «Mando de Zwift»

#### Scenario: Sin mando Bluetooth aparte
- **WHEN** el modo es «Todo por wifi (modo Zwift Ride)»
- **THEN** la app no se anuncia por Bluetooth como mando y los botones del Square llegan a Zwift por wifi

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth y se elige Zwift Ride
- **THEN** el modo se puede elegir y su ayuda dice que no hace falta el modo periférico

#### Scenario: Modo guardado
- **WHEN** se elige Zwift Ride y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y el mando se ofrece por wifi desde el primer momento

### Requirement: Nombre ante la app
La pantalla SHALL tener un selector «Nombre ante la app» con «Smart Bike», por defecto, y «KICKR BIKE PRO», con una ayuda que diga que Zwift verá la app con ese nombre, que «KICKR BIKE PRO» es una prueba y que hay que emparejar de nuevo en Zwift al cambiar. SHALL poder cambiarse con cualquiera de los modos, con «Mando de Zwift» desactivado y sin wifi.

#### Scenario: Valor por defecto
- **WHEN** se abre Plataformas por primera vez
- **THEN** el nombre es «Smart Bike»

#### Scenario: Elegir KICKR BIKE PRO
- **WHEN** se elige «KICKR BIKE PRO»
- **THEN** queda elegido, se guarda y la ayuda dice que Zwift verá un «KICKR BIKE PRO», que es una prueba y que hay que emparejarlo de nuevo

#### Scenario: Nombre guardado
- **WHEN** se elige «KICKR BIKE PRO» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegido y la app se anuncia como «KICKR BIKE PRO» desde el primer momento

#### Scenario: Con cualquier modo
- **WHEN** el modo es Zwift Ride, o «Mando de Zwift» está desactivado, o no hay wifi, y se cambia el nombre
- **THEN** el nombre se puede cambiar y queda guardado

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con «KICKR BIKE PRO» elegido y se importa en otro móvil
- **THEN** el nombre queda elegido y el móvil nuevo usa su propia identidad, no la del móvil anterior

### Requirement: Estado de la plataforma y del mando
Plataformas SHALL mostrar el estado de la conexión con Zwift en palabra («Conectado», «Esperando», «No visible» o «Sin conexión») y cómo emparejarla en Zwift con el nombre con el que la app se anuncia ahora; y, cuando el modo la ofrezca por Bluetooth, una fila del mando con su estado («Conectado», «Esperando», «Desactivado», «Sin conexión» o «No disponible») y cómo emparejarlo. En Zwift Ride el mando SHALL mostrarse dentro del mismo dispositivo y SHALL NOT decir «No disponible».

#### Scenario: Zwift esperando
- **WHEN** el móvil tiene wifi y Zwift no está conectado
- **THEN** el estado de Zwift dice «Esperando» y explica que en la pantalla de emparejamiento de Zwift se elige «Smart Bike» como rodillo, potencia y cadencia

#### Scenario: Nombre en la ayuda
- **WHEN** el nombre elegido es «KICKR BIKE PRO» y se abre Plataformas
- **THEN** la ayuda del emparejamiento dice que se elija el «KICKR BIKE PRO» con sus cuatro caracteres, tal como lo lista Zwift

#### Scenario: Cambio de nombre con la pantalla abierta
- **WHEN** se rueda con Plataformas abierta y se elige «KICKR BIKE PRO»
- **THEN** la ayuda pasa a nombrar el «KICKR BIKE PRO» sin cambiar el estado del Neo ni del Square

#### Scenario: Mando esperando por Bluetooth
- **WHEN** el modo es «Rodillo por wifi (FTMS)», «Mando de Zwift» está activado, el Bluetooth encendido y Zwift no está conectado al mando
- **THEN** la fila del mando dice «Esperando» y explica que en Zwift, en la lista de mandos, se elige el móvil por su nombre Bluetooth (por ejemplo «OnePlus 8») y que el Square no se empareja en Zwift

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado
- **THEN** la fila del mando dice «Desactivado», explica que se activa en esta pantalla y que, sin él, el Square puede seguir emparejado en Zwift para girar

#### Scenario: Mando no disponible
- **WHEN** «Mando de Zwift» está activado con el modo «Rodillo por wifi (FTMS)» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** la fila del mando dice «No disponible» y explica que este móvil no puede hacer de mando de Zwift

#### Scenario: Mando por wifi esperando
- **WHEN** el modo es Zwift Ride, hay wifi y Zwift no está conectado
- **THEN** el mando dice «Esperando» y explica que en Zwift se elige «Smart Bike» como potencia y mando a la vez

#### Scenario: Mando por wifi conectado
- **WHEN** Zwift está conectado al mando por wifi y lo ha saludado
- **THEN** el mando dice «Conectado»

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** el estado de Zwift dice «Sin conexión» y pide conectar el móvil a la misma red wifi que Zwift; en Zwift Ride el mando también dice «Sin conexión», y con el modo de rodillo por wifi el mando conserva su estado

#### Scenario: Bluetooth apagado
- **WHEN** el Bluetooth del móvil está apagado
- **THEN** con «Rodillo por wifi (FTMS)» el mando dice «Sin conexión» y Zwift conserva su estado; con Zwift Ride el mando sigue con su estado por wifi

### Requirement: Zwift no visible en Plataformas
Cuando la app muestra Zwift como «No visible» (spec `zwift-bridge`), Plataformas SHALL mostrar ese estado con el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo, y SHALL quitarlo en cuanto el anuncio vuelva a responder. Dispositivos SHALL NOT mostrar este estado.

#### Scenario: Anuncio que no responde
- **WHEN** el anuncio no responde en la red ni tras volver a anunciarse
- **THEN** Plataformas muestra Zwift como «No visible» con el aviso, y las marchas siguen funcionando como sin Zwift

#### Scenario: Vuelve a responder
- **WHEN** Zwift está «No visible» y el anuncio vuelve a responder
- **THEN** Plataformas pasa a «Esperando» y el aviso desaparece sin tocar nada

### Requirement: Plataformas desde Rodaje
Tocar el estado de la plataforma de Rodaje SHALL abrir Plataformas; tocar la barra de conexiones SHALL abrir Dispositivos. Desde Plataformas, «Rodar» o Atrás SHALL volver a Rodaje.

#### Scenario: Tocar el estado de la plataforma
- **WHEN** se toca el estado de la plataforma en Rodaje
- **THEN** se abre Plataformas con la barra inferior, y Atrás vuelve a Rodaje con la sesión en marcha

#### Scenario: Tocar el icono del Neo
- **WHEN** se toca el icono del Neo en Rodaje
- **THEN** se abre Dispositivos

### Requirement: Ajustes de Zwift guardados con migración
La plataforma, el modo, el mando y el nombre SHALL guardarse con los demás ajustes y seguir vigentes al reabrir la app. Una configuración guardada con la versión anterior SHALL abrirse conservando el modo, el mando y el nombre que tenía, de modo que Zwift reconozca el dispositivo sin volver a emparejarlo. Un archivo de copia de seguridad de la versión anterior SHALL importarse igual.

#### Scenario: Configuración anterior con KICKR BIKE
- **WHEN** se abre la app con una configuración guardada con «Simular un KICKR BIKE» activado y el modo «Todo por wifi»
- **THEN** la plataforma es Zwift, el modo es «Todo por wifi (modo Zwift Ride)», el nombre es «KICKR BIKE PRO» y se conservan los demás ajustes

#### Scenario: Configuración anterior por defecto
- **WHEN** se abre la app con una configuración guardada con los valores por defecto de la versión anterior
- **THEN** el modo es «Rodillo por wifi (FTMS)», el mando está activado y el nombre es «Smart Bike»

#### Scenario: Zwift Ride con el mando desactivado
- **WHEN** se abre la app con una configuración guardada con «Todo por wifi» y «Mando de Zwift» desactivado
- **THEN** el modo sigue siendo Zwift Ride, con el mando dentro de DirCon, y Zwift reconoce el dispositivo sin emparejarlo de nuevo

#### Scenario: Importar una copia anterior
- **WHEN** se importa una copia de seguridad hecha con la versión anterior
- **THEN** se aplican el modo, el mando y el nombre como en una configuración guardada de esa versión

### Requirement: Tabla de compatibilidad
Cada plataforma y cada modo de conexión que la app ofrece SHALL tener documentado qué está validado con el equipo real y qué queda «a validar», y la pantalla SHALL NOT presentar como confirmado lo que no lo está. Un modo SHALL ofrecerse solo si está implementado.

#### Scenario: Modos de Zwift
- **WHEN** se consulta la compatibilidad de Zwift
- **THEN** «Rodillo por wifi (FTMS)» y «Todo por wifi (modo Zwift Ride)» constan como validados en Zwift en PC en lo que recogen las specs `zwift-bridge` y `zwift-controller`, y lo marcado «a validar» ahí sigue marcado

#### Scenario: Modo sin implementar
- **WHEN** una plataforma o un modo (por ejemplo FTMS por Bluetooth) no está implementado
- **THEN** no aparece en la pantalla
