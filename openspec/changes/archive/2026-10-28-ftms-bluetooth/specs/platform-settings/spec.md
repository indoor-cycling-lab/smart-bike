# Spec Delta

## ADDED Requirements

### Requirement: Modo de rodillo por Bluetooth en Plataformas
Plataformas SHALL ofrecer para Zwift el modo «Rodillo por Bluetooth (FTMS)», sin marcar como recomendado y con una ayuda que diga que Zwift verá el móvil por Bluetooth con su nombre Bluetooth, que no hace falta wifi, que el mando va dentro del mismo dispositivo (el Square no se empareja en Zwift) y que hay que emparejar de nuevo en Zwift. Con este modo elegido, la pantalla SHALL NOT mostrar el interruptor «Mando de Zwift» ni el selector «Nombre ante la app».

#### Scenario: Elegir Rodillo por Bluetooth
- **WHEN** se toca «Rodillo por Bluetooth (FTMS)»
- **THEN** queda elegido, se guarda, la ayuda dice que Zwift verá el móvil por su nombre Bluetooth con el mando dentro y que el Square no se empareja en Zwift, y no aparecen el interruptor «Mando de Zwift» ni «Nombre ante la app»

#### Scenario: Volver a un modo por wifi
- **WHEN** está elegido «Rodillo por Bluetooth (FTMS)» y se elige «Rodillo por wifi (FTMS)»
- **THEN** reaparecen «Mando de Zwift» y «Nombre ante la app» con los valores que tenían antes

#### Scenario: Móvil sin modo periférico
- **WHEN** el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** el modo se puede elegir y su ayuda dice que este móvil no puede hacer de rodillo por Bluetooth

### Requirement: Estado de Zwift por Bluetooth
Con el modo por Bluetooth, Plataformas SHALL mostrar el estado de Zwift en palabra («Conectado», «Esperando», «Sin conexión» o «No disponible») y el del mando dentro del mismo dispositivo («Dentro de Smart Bike · por Bluetooth») con su estado, y cómo emparejarlo en Zwift con el nombre Bluetooth del móvil, y SHALL NOT decir «No visible» ni pedir conectar el wifi.

#### Scenario: Zwift esperando
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)», el Bluetooth está encendido y Zwift no está conectado
- **THEN** el estado y el del mando dicen «Esperando» y explican que en la pantalla de emparejamiento de Zwift se elige el móvil por su nombre Bluetooth (por ejemplo «OnePlus 8») como rodillo, potencia, cadencia y mando, y que el Square no se empareja en Zwift

#### Scenario: Bluetooth apagado
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el Bluetooth del móvil está apagado
- **THEN** el estado de Zwift y el del mando dicen «Sin conexión» y piden encender el Bluetooth

#### Scenario: Móvil sin modo periférico
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el móvil no puede anunciarse como dispositivo Bluetooth
- **THEN** el estado de Zwift y el del mando dicen «No disponible» y explican que este móvil no puede hacer de rodillo ni de mando por Bluetooth

#### Scenario: Sin wifi
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)» y el móvil no tiene wifi
- **THEN** el estado no cambia y no se muestra ningún aviso de wifi

#### Scenario: Zwift conectado con el mando
- **WHEN** Zwift está conectado al rodillo y ha saludado al mando por Bluetooth
- **THEN** el estado de Zwift y el del mando dicen «Conectado»

## MODIFIED Requirements

### Requirement: Una sola plataforma y un solo modo
La pantalla SHALL permitir elegir una única plataforma y un único modo de conexión a la vez, y SHALL NOT ofrecer ninguna combinación que no esté implementada. Al elegir una plataforma, la pantalla SHALL mostrar solo los modos que la app admite para ella, con el recomendado marcado y elegido si no había un modo válido. Los modos que aún no existen SHALL NOT aparecer.

#### Scenario: Plataforma con varios modos
- **WHEN** se elige Zwift
- **THEN** se ofrecen «Rodillo por wifi (FTMS)», marcado como recomendado, «Todo por wifi (modo Zwift Ride)» y «Rodillo por Bluetooth (FTMS)», y ninguno más

#### Scenario: Un solo modo elegido
- **WHEN** está elegido «Rodillo por wifi (FTMS)» y se toca «Todo por wifi (modo Zwift Ride)»
- **THEN** pasa a estar elegido el segundo y deja de estarlo el primero

#### Scenario: Plataforma única
- **WHEN** Zwift es la única plataforma implementada
- **THEN** la pantalla muestra Zwift como la plataforma elegida y no ofrece ninguna otra

### Requirement: Nombre ante la app
La pantalla SHALL tener un selector «Nombre ante la app» con «Smart Bike», por defecto, y «KICKR BIKE PRO», con una ayuda que diga que Zwift verá la app con ese nombre, que «KICKR BIKE PRO» es una prueba y que hay que emparejar de nuevo en Zwift al cambiar. SHALL poder cambiarse con cualquiera de los modos por wifi, con «Mando de Zwift» desactivado y sin wifi. SHALL NOT mostrarse con el modo por Bluetooth, donde el nombre es el del móvil.

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
- **WHEN** el modo por wifi es Zwift Ride, o «Mando de Zwift» está desactivado, o no hay wifi, y se cambia el nombre
- **THEN** el nombre se puede cambiar y queda guardado

#### Scenario: Con el modo por Bluetooth
- **WHEN** el modo es «Rodillo por Bluetooth (FTMS)»
- **THEN** el selector «Nombre ante la app» no se muestra y el nombre elegido antes se conserva para cuando se vuelva a un modo por wifi

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con «KICKR BIKE PRO» elegido y se importa en otro móvil
- **THEN** el nombre queda elegido y el móvil nuevo usa su propia identidad, no la del móvil anterior

### Requirement: Tabla de compatibilidad
Cada plataforma y cada modo de conexión que la app ofrece SHALL tener documentado qué está validado con el equipo real y qué queda «a validar», y la pantalla SHALL NOT presentar como confirmado lo que no lo está. Un modo SHALL ofrecerse solo si está implementado.

#### Scenario: Modos de Zwift
- **WHEN** se consulta la compatibilidad de Zwift
- **THEN** «Rodillo por wifi (FTMS)» y «Todo por wifi (modo Zwift Ride)» constan como validados en Zwift en PC en lo que recogen las specs `platform-bridge`, `zwift-ride` y `zwift-controller`, «Rodillo por Bluetooth (FTMS)» consta como validado solo en lo que recoge la spec `ftms-bluetooth`, y lo marcado «a validar» ahí sigue marcado

#### Scenario: Modo sin implementar
- **WHEN** una plataforma o un modo no está implementado
- **THEN** no aparece en la pantalla
