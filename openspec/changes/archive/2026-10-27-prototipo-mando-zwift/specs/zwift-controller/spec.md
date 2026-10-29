## Purpose

La app se presenta ante Zwift como un mando oficial de Zwift para enviarle botones de navegación, dirección, powerup, Ride On y freno, sin emparejar el Square ni un teclado en el equipo de Zwift. De momento se acciona desde una pantalla de prueba de la compilación de depuración.

## ADDED Requirements

### Requirement: Pantalla de prueba del mando en la compilación de depuración
La compilación de depuración SHALL incluir una pantalla de prueba del mando de Zwift que se abre sin pasar por la navegación de la app y que no arranca la sesión de rodaje ni se conecta al Neo ni al Square. La compilación de distribución SHALL NOT incluirla.

#### Scenario: Abrir la pantalla de prueba
- **WHEN** se abre la pantalla de prueba del mando en la compilación de depuración
- **THEN** se ve la elección de configuración, el estado de la conexión con Zwift y los botones del mando, y la app no busca el Neo ni el Square

#### Scenario: Compilación de distribución
- **WHEN** se instala la compilación de distribución
- **THEN** la pantalla de prueba del mando no existe

### Requirement: Configuración del mando
La pantalla de prueba SHALL dejar elegir, antes de publicar el mando, entre «Solo mando por wifi», «Mando y rodillo por wifi» y «Solo mando por Bluetooth», y SHALL no permitir cambiar la configuración mientras el mando está publicado.

#### Scenario: Publicar el mando por wifi
- **WHEN** se elige «Solo mando por wifi» y se pulsa «Publicar» con el móvil conectado a una red wifi
- **THEN** el estado pasa a «Esperando a Zwift» y la elección de configuración queda deshabilitada hasta pulsar «Parar»

#### Scenario: Parar
- **WHEN** el mando está publicado y se pulsa «Parar»
- **THEN** el mando deja de anunciarse, Zwift pierde la conexión si la tenía y la elección de configuración vuelve a estar disponible

#### Scenario: Sin wifi
- **WHEN** se elige una configuración por wifi y el móvil no está conectado a ninguna red wifi
- **THEN** la pantalla avisa de que hace falta wifi y no publica el mando

#### Scenario: Móvil sin modo periférico
- **WHEN** se elige «Solo mando por Bluetooth» en un móvil que no puede anunciarse como dispositivo Bluetooth
- **THEN** la pantalla avisa de que el móvil no lo admite y no publica el mando

#### Scenario: Bluetooth apagado
- **WHEN** se elige «Solo mando por Bluetooth» con el Bluetooth apagado
- **THEN** la pantalla avisa de que el Bluetooth está apagado y no publica el mando

### Requirement: Mando reconocido por Zwift
Con el mando publicado por Bluetooth y una fuente de potencia ya emparejada en Zwift, Zwift SHALL listar el móvil en la pantalla de emparejamiento como mando y SHALL poder emparejarlo. Por wifi, Zwift no lo reconoce como mando: la potencia, la cadencia y el rodillo siguen por wifi y el mando va por Bluetooth, dos dispositivos ante Zwift.

#### Scenario: Emparejar el mando en Zwift para PC
- **WHEN** el mando está publicado con «Solo mando por Bluetooth», «Square Shift» está emparejado por wifi como potencia, cadencia y rodillo, y el usuario abre la lista de mandos de Zwift
- **THEN** el móvil aparece con su nombre Bluetooth, se puede emparejar y la pantalla de prueba muestra «Conectado» y, tras el saludo de Zwift, «Handshake hecho»

#### Scenario: Mando por wifi
- **WHEN** el mando está publicado con «Mando y rodillo por wifi» y el usuario abre la pantalla de emparejamiento de Zwift
- **THEN** el dispositivo aparece como potencia, cadencia y rodillo controlable, pero no en la lista de mandos, y sus botones no hacen nada en Zwift

#### Scenario: Otras conexiones Bluetooth del móvil
- **WHEN** el mando está publicado por Bluetooth mientras la app está conectada al Neo y al Square, y el Square se desconecta y vuelve a conectarse
- **THEN** Zwift sigue recibiendo los botones del mando y el handshake no se pierde

#### Scenario: Rodillo sin Neo
- **WHEN** Zwift está conectado con «Mando y rodillo por wifi»
- **THEN** Zwift recibe potencia y cadencia 0 y la app no envía nada al Neo

### Requirement: Botones mantenidos hacia Zwift
Con Zwift conectado y el handshake hecho, cada botón de la pantalla de prueba SHALL llegar a Zwift como pulsado mientras se mantiene y como suelto al soltarlo, sin esperar a otro gesto.

#### Scenario: Navegar un menú
- **WHEN** se pulsa y suelta la cruceta abajo y luego A en un menú de Zwift
- **THEN** Zwift baja una opción y la selecciona

#### Scenario: Elegir desvío
- **WHEN** se pulsa la flecha izquierda al llegar a un cruce en Zwift
- **THEN** Zwift elige el desvío de la izquierda y la pantalla de prueba muestra el botón pulsado mientras se mantiene

#### Scenario: Powerup y Ride On
- **WHEN** se pulsa Y con un powerup disponible y luego Z con otro ciclista cerca
- **THEN** Zwift usa el powerup y da Ride On

#### Scenario: Antes del handshake
- **WHEN** Zwift está conectado pero aún no ha hecho el handshake y se pulsa un botón
- **THEN** la pantalla de prueba muestra el botón pulsado y no envía nada a Zwift

### Requirement: Varios botones a la vez
La pantalla de prueba SHALL enviar a Zwift todos los botones que se mantienen a la vez en el mismo estado, y soltar uno SHALL NOT soltar los demás.

#### Scenario: Dos botones a la vez
- **WHEN** se mantiene la cruceta derecha y, sin soltarla, se pulsa y suelta A
- **THEN** Zwift sigue recibiendo la cruceta derecha pulsada después de soltar A

### Requirement: Freno
Mientras se mantiene un botón de freno de la pantalla de prueba, el avatar SHALL frenar en Zwift, y SHALL dejar de frenar al soltarlo sin que haga falta otra pulsación; con los dos frenos a la vez SHALL frenar al menos igual que con uno.

#### Scenario: Frenar con un freno
- **WHEN** se rueda en Zwift a más de 30 km/h y se mantiene el freno derecho durante 3 s
- **THEN** el avatar reduce la velocidad mientras se mantiene y deja de frenar al soltarlo

#### Scenario: Frenar con los dos
- **WHEN** se mantienen el freno izquierdo y el derecho a la vez
- **THEN** el avatar frena al menos igual que con uno solo

### Requirement: Dirección con las palancas
Mientras se mantiene la palanca izquierda hacia fuera, el avatar SHALL desplazarse a la izquierda en la calzada, y con la palanca derecha hacia fuera, a la derecha; al soltar, SHALL volver a ir recto. Las flechas izquierda y derecha SHALL elegir el desvío en los cruces.

#### Scenario: Desplazarse a la izquierda
- **WHEN** se rueda en Zwift y se mantiene la palanca izquierda hacia fuera durante 2 s
- **THEN** el avatar se desplaza a la izquierda mientras se mantiene y vuelve a ir recto al soltarla

#### Scenario: Desplazarse a la derecha
- **WHEN** se rueda en Zwift y se mantiene la palanca derecha hacia fuera durante 2 s
- **THEN** el avatar se desplaza a la derecha mientras se mantiene y vuelve a ir recto al soltarla

#### Scenario: Palanca hacia dentro
- **WHEN** se mantiene la palanca derecha hacia dentro
- **THEN** el avatar frena, como con el freno

### Requirement: Mando activo sin pulsar nada
Con el handshake hecho, la app SHALL seguir indicando a Zwift que el mando está activo, también cuando no se pulsa ningún botón, para que Zwift no lo dé por perdido.

#### Scenario: Diez minutos sin tocar nada
- **WHEN** Zwift está conectado al mando y no se pulsa ningún botón durante 10 minutos
- **THEN** Zwift sigue mostrando el mando conectado y el siguiente botón pulsado le llega

### Requirement: Conexión con Zwift perdida
Si Zwift se desconecta, la app SHALL dar por sueltos todos los botones, SHALL seguir anunciando el mando mientras esté publicado y SHALL aceptar una nueva conexión de Zwift, que vuelve a hacer el handshake.

#### Scenario: Zwift se cierra con un botón mantenido
- **WHEN** se mantiene la cruceta izquierda y se cierra Zwift
- **THEN** la pantalla de prueba pasa a «Esperando a Zwift» y, al volver a abrir Zwift, el mando se reconecta sin ningún botón pulsado

#### Scenario: Se pierde el wifi
- **WHEN** el mando está publicado por wifi y el móvil pierde la red
- **THEN** la pantalla de prueba muestra que no hay wifi y, al volver la red, se anuncia otra vez sin pulsar «Publicar»

#### Scenario: Zwift recuerda un mando anterior
- **WHEN** se vuelve a publicar el mando por Bluetooth con Zwift abierto y Zwift se queda en «Conectando…»
- **THEN** al cerrar y abrir Zwift, el mando se empareja de nuevo y hace el handshake

### Requirement: Variantes del protocolo elegibles en la prueba
La pantalla de prueba SHALL dejar elegir, mientras el mando no está publicado, la respuesta al handshake, la respuesta a la petición de información del dispositivo que hace Zwift tras el handshake, la tabla de valores de los botones a partir de Z y la codificación del freno; y, por Bluetooth, los datos de fabricante del anuncio y si lleva el nombre del móvil, para probar cada variante con Zwift.

#### Scenario: Responder a la petición de información
- **WHEN** se elige una respuesta a la petición de información, se publica y Zwift pide la información del dispositivo tras el handshake
- **THEN** la app le responde con la variante elegida y el registro muestra la petición y la respuesta

#### Scenario: Probar otra tabla de botones
- **WHEN** Z no da Ride On con la tabla elegida, se pulsa «Parar», se elige la otra tabla y se vuelve a publicar
- **THEN** los botones a partir de Z se envían con los valores de la nueva tabla

### Requirement: Tráfico del mando en el registro
La app SHALL anotar en el registro de sesión, con hora y sentido, todo lo que Zwift escribe en el mando y todo lo que la app le envía, para leerlo desde el ordenador durante la prueba.

#### Scenario: Leer el handshake
- **WHEN** Zwift se conecta al mando y hace el handshake
- **THEN** el registro muestra en hexadecimal lo que escribió Zwift y la respuesta de la app
