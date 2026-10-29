## ADDED Requirements

### Requirement: Mando publicado con la sesión de rodaje
Mientras dura la sesión de rodaje y «Mando de Zwift» está activado, la app SHALL anunciarse por Bluetooth como mando de Zwift, con el nombre Bluetooth del móvil, aunque el rodillo, la potencia y la cadencia vayan por wifi. Al terminar la sesión o desactivar el ajuste, SHALL dejar de anunciarse y Zwift SHALL perder el mando.

#### Scenario: Abrir la app con el ajuste activado
- **WHEN** se abre la app con «Mando de Zwift» activado y el Bluetooth encendido
- **THEN** el mando se anuncia sin tocar nada y Dispositivos dice «Esperando» en su fila

#### Scenario: Desactivar el ajuste rodando
- **WHEN** Zwift está conectado al mando y se desactiva «Mando de Zwift» en Ajustes
- **THEN** Zwift pierde el mando, los botones del Square dejan de llegarle por la app y el rodillo sigue conectado por wifi sin cambios

#### Scenario: Terminar la sesión
- **WHEN** se termina la sesión con el mando conectado a Zwift
- **THEN** el mando deja de anunciarse y Zwift lo da por desconectado

#### Scenario: Bluetooth apagado
- **WHEN** «Mando de Zwift» está activado y se apaga el Bluetooth del móvil
- **THEN** el mando deja de anunciarse y, al volver a encender el Bluetooth, se anuncia otra vez sin tocar nada

#### Scenario: Móvil sin modo periférico
- **WHEN** «Mando de Zwift» está activado en un móvil que no puede anunciarse como dispositivo Bluetooth
- **THEN** la app no anuncia el mando, Dispositivos dice «No disponible» en su fila y el resto de la sesión funciona igual

### Requirement: Funciones de Zwift desde el Square
Con el mando conectado a Zwift, cada botón o combinación del Square con una función de Zwift asignada SHALL hacer esa función en Zwift: ←, ↑, →, ↓, A, B, Y, Z, girar a la izquierda o a la derecha, y freno izquierdo o derecho. Un botón sin función de Zwift SHALL NOT llegar a Zwift por la app.

#### Scenario: Barra de acciones y atrás
- **WHEN** rodando en Zwift se pulsa la cruceta ↑ y después B
- **THEN** Zwift abre la barra de acciones y la cierra

#### Scenario: Botón reasignado
- **WHEN** A se reasigna a «+1 marcha» y se pulsa A en un menú de Zwift
- **THEN** la marcha sube y Zwift no selecciona nada

#### Scenario: Combinación de la app
- **WHEN** se mantiene Campag. I y se pulsa la cruceta → con la asignación por defecto
- **THEN** la app pasa a la pantalla siguiente y Zwift no recibe la cruceta

#### Scenario: Square sin conexión
- **WHEN** el mando está conectado a Zwift y el Square no está conectado
- **THEN** Zwift sigue con el mando conectado, sin ningún botón pulsado, y los botones vuelven a llegar cuando el Square se reconecta

### Requirement: Palancas fuera de Zwift
Las palancas del Square SHALL seguir cambiando las marchas de la app y SHALL NOT llegar a Zwift como botones de cambio.

#### Scenario: Cambiar de marcha en Zwift
- **WHEN** rodando en Zwift con el mando conectado se pulsa la palanca D1
- **THEN** la marcha de la app sube y Zwift no avisa de que el rodillo no admite marchas virtuales

### Requirement: Pulsación breve con gestos ambiguos
Una función de Zwift asignada a «corta», «larga» o «doble» SHALL llegar a Zwift como una pulsación breve, pulsada y suelta, al reconocerse el gesto.

#### Scenario: Ride On con una larga
- **WHEN** se asigna △ larga a Z (Ride On) y se mantiene △ más que el umbral de pulsación larga con otro ciclista cerca
- **THEN** Zwift da un solo Ride On al cumplirse el umbral, aunque △ siga pulsado

## MODIFIED Requirements

### Requirement: Mando reconocido por Zwift
Con el mando anunciado y una fuente de potencia ya emparejada en Zwift, Zwift SHALL listar el móvil en la pantalla de emparejamiento como mando y SHALL poder emparejarlo. Zwift ve dos dispositivos de la app: el rodillo, la potencia y la cadencia por wifi y el mando por Bluetooth.

#### Scenario: Emparejar el mando en Zwift para PC
- **WHEN** el mando está anunciado, «Square Shift» está emparejado por wifi como potencia, cadencia y rodillo, y el usuario abre la lista de mandos de Zwift
- **THEN** el móvil aparece con su nombre Bluetooth, se puede emparejar y Dispositivos muestra el mando «Conectado» tras el saludo de Zwift

#### Scenario: Sin el Square emparejado en Zwift
- **WHEN** el Square no está emparejado en Zwift y el mando está conectado
- **THEN** se navega, se gira, se frena, se usa el powerup y se da Ride On en Zwift con los botones del Square

#### Scenario: Otras conexiones Bluetooth del móvil
- **WHEN** el mando está conectado a Zwift mientras la app está conectada al Neo y al Square, y el Square se desconecta y vuelve a conectarse
- **THEN** Zwift sigue con el mando conectado, sin repetir el emparejamiento, y los botones vuelven a llegarle

#### Scenario: Mando por wifi
- **WHEN** Zwift empareja «Square Shift» por wifi
- **THEN** aparece como potencia, cadencia y rodillo controlable, pero no en la lista de mandos: el mando aparece aparte, con el nombre Bluetooth del móvil

#### Scenario: Rodillo sin Neo
- **WHEN** Zwift está conectado al rodillo y al mando y el Neo no está conectado
- **THEN** Zwift recibe potencia y cadencia 0 y los botones del Square siguen llegando a Zwift por el mando

### Requirement: Botones mantenidos hacia Zwift
Con Zwift conectado al mando, una función de Zwift asignada a «pulsar» SHALL llegar a Zwift como pulsada mientras se mantiene el botón y como suelta al soltarlo, sin esperar a otro gesto ni repetirse.

#### Scenario: Navegar un menú
- **WHEN** con la asignación por defecto se pulsa y suelta la cruceta ↓ y luego A en un menú de Zwift
- **THEN** Zwift baja una opción y la selecciona

#### Scenario: Elegir desvío
- **WHEN** se mantiene la cruceta ← al llegar a un cruce en Zwift
- **THEN** Zwift elige el desvío de la izquierda

#### Scenario: Powerup y Ride On
- **WHEN** con la asignación por defecto se pulsa Y con un powerup disponible y luego Z con otro ciclista cerca
- **THEN** Zwift usa el powerup y da Ride On

#### Scenario: Antes del handshake
- **WHEN** Zwift se ha conectado al mando pero aún no lo ha saludado y se pulsa A
- **THEN** no se envía nada a Zwift y la marcha no cambia

#### Scenario: Mando desactivado
- **WHEN** «Mando de Zwift» está desactivado y se pulsa A
- **THEN** la app no hace nada y, si el Square está emparejado en Zwift, Zwift recibe A directamente del Square

### Requirement: Varios botones a la vez
La app SHALL enviar a Zwift todas las funciones que se mantienen a la vez en el mismo estado, y soltar una SHALL NOT soltar las demás.

#### Scenario: Dos botones a la vez
- **WHEN** se mantiene la cruceta → y, sin soltarla, se pulsa y suelta A
- **THEN** Zwift sigue recibiendo la cruceta → pulsada después de soltar A

#### Scenario: Girar y pulsar otro botón
- **WHEN** se mantiene ○ y, sin soltarlo, se pulsa y suelta A
- **THEN** el avatar sigue desplazándose a la derecha hasta soltar ○

### Requirement: Freno
Mientras se mantiene una maneta de freno del Square con su freno de Zwift asignado, el avatar SHALL frenar en Zwift, y SHALL dejar de frenar al soltarla sin que haga falta otra pulsación; con las dos manetas a la vez SHALL frenar más que con una.

#### Scenario: Frenar con un freno
- **WHEN** se rueda en Zwift a más de 30 km/h y se mantiene la maneta derecha durante 3 s
- **THEN** el avatar reduce la velocidad mientras se mantiene y deja de frenar al soltarla

#### Scenario: Frenar con los dos
- **WHEN** se mantienen las dos manetas a la vez
- **THEN** el avatar frena más que con una sola

### Requirement: Dirección con las palancas
Mientras se mantiene el botón con «Girar a la izquierda» (✕ por defecto), el avatar SHALL desplazarse a la izquierda en la calzada, y con «Girar a la derecha» (○ por defecto), a la derecha; al soltar, SHALL volver a ir recto. Las flechas ← y → de la cruceta SHALL elegir el desvío en los cruces.

#### Scenario: Desplazarse a la izquierda
- **WHEN** se rueda en Zwift y se mantiene ✕ durante 2 s
- **THEN** el avatar se desplaza a la izquierda mientras se mantiene y vuelve a ir recto al soltarlo

#### Scenario: Desplazarse a la derecha
- **WHEN** se rueda en Zwift y se mantiene ○ durante 2 s
- **THEN** el avatar se desplaza a la derecha mientras se mantiene y vuelve a ir recto al soltarlo

#### Scenario: Palanca hacia dentro
- **WHEN** se mantiene la maneta derecha, que en un mando oficial es la palanca derecha hacia dentro
- **THEN** el avatar frena y no se desplaza

### Requirement: Conexión con Zwift perdida
Si Zwift se desconecta del mando, la app SHALL dar por sueltas todas las funciones, SHALL seguir anunciando el mando mientras dure la sesión con el ajuste activado y SHALL aceptar una nueva conexión de Zwift. Si se pierde el Square con una función mantenida, la app SHALL soltarla en Zwift.

#### Scenario: Zwift se cierra con un botón mantenido
- **WHEN** se mantiene ○ y se cierra Zwift
- **THEN** Dispositivos pasa el mando a «Esperando» y, al volver a abrir Zwift, el mando se reconecta sin nada pulsado y el avatar va recto

#### Scenario: Square perdido con el freno mantenido
- **WHEN** se mantiene una maneta de freno rodando en Zwift y se apaga el Square
- **THEN** el avatar deja de frenar

#### Scenario: Se pierde el wifi
- **WHEN** el mando está conectado a Zwift y el móvil pierde la red wifi
- **THEN** el rodillo deja de estar conectado a Zwift y el mando sigue conectado por Bluetooth, con sus botones llegando a Zwift

#### Scenario: Zwift recuerda un mando anterior
- **WHEN** se vuelve a anunciar el mando con Zwift abierto y Zwift se queda en «Conectando…»
- **THEN** al cerrar y abrir Zwift, el mando se empareja de nuevo

### Requirement: Tráfico del mando en el registro
La app SHALL anotar en el registro de sesión, con hora y sentido, todo lo que Zwift escribe en el mando y todo lo que la app le envía.

#### Scenario: Leer el handshake
- **WHEN** Zwift se conecta al mando y lo saluda
- **THEN** el registro muestra en hexadecimal lo que escribió Zwift y la respuesta de la app

## REMOVED Requirements

### Requirement: Pantalla de prueba del mando en la compilación de depuración
**Reason**: El mando pasa a la sesión de rodaje y lo accionan los botones del Square; la pantalla de prueba del change `prototipo-mando-zwift` cumplió su función.
**Migration**: El mando se activa con «Mando de Zwift» en Ajustes y su estado se ve en Dispositivos y en Diagnóstico.

### Requirement: Configuración del mando
**Reason**: Zwift solo acepta el mando por Bluetooth (validado en `prototipo-mando-zwift`); las configuraciones por wifi no sirven.
**Migration**: El mando va siempre por Bluetooth, y el rodillo sigue por wifi.

### Requirement: Variantes del protocolo elegibles en la prueba
**Reason**: Las variantes se resolvieron con Zwift en el prototipo; la app usa las validadas.
**Migration**: Ninguna.
