# Spec Delta

## MODIFIED Requirements

### Requirement: Nombre ante la plataforma
Con el nombre elegido en «Nombre ante la app» de Conexión, la app SHALL anunciarse por wifi con ese nombre: «Smart Bike», por defecto, o «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, en cualquiera de los modos con wifi. Lo que se ofrece y se envía SHALL ser lo mismo con los dos nombres. Con «Solo Bluetooth», el nombre SHALL ser el nombre Bluetooth del móvil, que es el elegido si «Usar el nombre también por Bluetooth» está marcada (specs `ftms-bluetooth` y `bluetooth-name`).

#### Scenario: Nombre por defecto
- **WHEN** «Smart Bike» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike»

#### Scenario: KICKR BIKE PRO
- **WHEN** «KICKR BIKE PRO» está elegido y se abre la pantalla de emparejamiento de Zwift
- **THEN** Zwift lista un dispositivo «KICKR BIKE PRO» con cuatro caracteres hexadecimales y no lista «Smart Bike»

#### Scenario: Mismo nombre en cada sesión
- **WHEN** «KICKR BIKE PRO» está elegido y se cierran y se vuelven a abrir la app y Zwift
- **THEN** Zwift lista el dispositivo con el mismo nombre y lo reconoce como el mismo, sin volver a elegirlo

#### Scenario: Mismo comportamiento
- **WHEN** «KICKR BIKE PRO» está elegido, Zwift está conectado y se rueda con el Neo y el Square
- **THEN** Zwift recibe la potencia, la cadencia, el pulso y los botones, y el Neo simula el terreno con la marcha de la app, igual que con «Smart Bike»

#### Scenario: Modo por Bluetooth
- **WHEN** el modo es «Solo Bluetooth», «KICKR BIKE PRO» está elegido y «Usar el nombre también por Bluetooth» está desmarcada
- **THEN** Zwift lista el móvil por su nombre Bluetooth y el nombre elegido no se usa hasta volver a un modo con wifi o marcar la casilla

#### Scenario: Modo por Bluetooth con el nombre elegido
- **WHEN** el modo es «Solo Bluetooth», «KICKR BIKE PRO» está elegido y «Usar el nombre también por Bluetooth» está marcada
- **THEN** Zwift lista «KICKR BIKE PRO» con sus cuatro caracteres por Bluetooth y recibe lo mismo que con «Smart Bike»
