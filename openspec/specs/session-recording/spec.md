# session-recording Specification

## Purpose
Grabar la sesión de rodaje en archivos de texto de la app (solo sucesos, con cabecera y escritura continua), parar la grabación cuando queda poco espacio libre y gestionar las sesiones grabadas: listarlas con su tamaño, abrirlas con otra app, compartirlas y borrarlas, una o todas.

## Requirements

### Requirement: Grabación de sesiones opcional
La app SHALL grabar la sesión en un archivo solo mientras el ajuste «Grabar sesiones» de Ajustes esté activado, disponible en cualquier compilación y apagado por defecto; apagado, la app SHALL NOT escribir ningún archivo de sesión. Al activarlo con una sesión en curso, la grabación SHALL empezar en ese momento con un archivo nuevo, y con el ajuste activado cada sesión nueva SHALL tener el suyo. Al desactivarlo o al terminar la sesión, el archivo SHALL cerrarse y quedar en la lista.

#### Scenario: Apagado por defecto
- **WHEN** se abre la app por primera vez y se rueda
- **THEN** no se crea ningún archivo de sesión y «Sesiones grabadas» está vacía

#### Scenario: Activar con la sesión en curso
- **WHEN** con la sesión abierta se activa «Grabar sesiones»
- **THEN** empieza un archivo nuevo en ese momento, sin esperar a la siguiente sesión, y lo ocurrido antes de activarlo no aparece en él

#### Scenario: Una sesión nueva con el ajuste activado
- **WHEN** con «Grabar sesiones» activado se termina la sesión y se abre otra
- **THEN** la sesión anterior queda cerrada en la lista y la nueva se graba en otro archivo

#### Scenario: Desactivar
- **WHEN** con una grabación en curso se desactiva «Grabar sesiones»
- **THEN** el archivo se cierra con lo grabado hasta entonces y deja de crecer

### Requirement: Cabecera del archivo de sesión
El archivo SHALL ser de texto plano y SHALL empezar por una cabecera con la fecha y la hora de inicio, la versión de la app y la configuración de la sesión: plataforma, modo de conexión, nombre ante la app, transmisión y modelo de resistencia.

#### Scenario: Cabecera
- **WHEN** se abre un archivo recién grabado
- **THEN** sus primeras líneas dicen cuándo empezó, la versión de la app, la plataforma y el modo elegidos, el nombre ante la app, la transmisión y el modelo de resistencia

### Requirement: Sucesos del archivo de sesión
Tras la cabecera, el archivo SHALL llevar una línea por cada suceso de la app, con su hora de reloj y su origen: conexiones y reconexiones, gestos y acciones, cambios de marcha y de modo de control, comandos al Neo, lo que pide la plataforma, cambios de wifi y Bluetooth y errores. El archivo SHALL NOT incluir las tramas crudas del Neo, del Square ni de la plataforma.

#### Scenario: Seguir un cambio de marcha
- **WHEN** durante la grabación se cambia de marcha con la palanca
- **THEN** el archivo contiene, en orden y con su hora, el gesto, la acción, la marcha nueva y el comando enviado al Neo

#### Scenario: Sin tramas crudas
- **WHEN** se rueda un rato con el Neo y el Square conectados
- **THEN** el archivo no contiene líneas de tramas con bytes del Neo, del Square ni de la plataforma, solo sucesos

### Requirement: Escritura continua sin frenar el rodaje
Cada suceso SHALL escribirse en el archivo según llega, sin esperar al final de la sesión, de modo que un cierre inesperado de la app conserve lo ocurrido salvo, como mucho, los últimos instantes. La escritura SHALL NOT retrasar el cambio de marcha ni los envíos al Neo, y un fallo al escribir SHALL anotarse y SHALL NOT interrumpir la sesión de rodaje.

#### Scenario: Cierre inesperado
- **WHEN** con la grabación activa se mata la app
- **THEN** el archivo contiene los sucesos hasta unos instantes antes del cierre y aparece en la lista

#### Scenario: Fallo al escribir
- **WHEN** la escritura del archivo falla
- **THEN** la grabación se detiene, la pantalla de Sesiones grabadas dice que se detuvo por un error y el rodaje sigue con normalidad

### Requirement: Ajuste del espacio libre mínimo
Ajustes SHALL permitir elegir el espacio libre mínimo del almacenamiento para grabar, como porcentaje del total, de 5 % a 50 % de 5 en 5, con 10 % por defecto.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** «Espacio libre mínimo» dice 10 %

#### Scenario: Límites del ajuste
- **WHEN** el mínimo está en 50 % y se pulsa «+», o en 5 % y se pulsa «−»
- **THEN** el valor no cambia

### Requirement: Parada por poco espacio libre
La app SHALL comprobar el espacio libre al empezar a grabar y cada 30 s mientras graba. Si es menor que el mínimo, la grabación SHALL detenerse, el archivo SHALL cerrarse con una última línea que diga el motivo y la grabación SHALL NOT reanudarse sola: solo al desactivar y activar el ajuste o en la siguiente sesión. Para liberar espacio, la app SHALL NOT borrar sesiones por su cuenta; solo borra las que superan el límite de «Sesiones conservadas».

#### Scenario: Parada por poco espacio
- **WHEN** durante una grabación el espacio libre baja del mínimo y pasan hasta 30 s
- **THEN** la grabación se detiene, la última línea del archivo dice que se detuvo por poco espacio libre y la sección «Depuración» y la sesión dicen «Detenida por poco espacio»

#### Scenario: Activar sin espacio
- **WHEN** se activa «Grabar sesiones» con el espacio libre ya por debajo del mínimo
- **THEN** no se crea ningún archivo y la sección «Depuración» dice que no hay espacio libre suficiente

#### Scenario: Sin reanudar sola
- **WHEN** tras una parada por poco espacio vuelve a haber espacio libre
- **THEN** el archivo no crece más y la grabación empieza de nuevo solo al desactivar y volver a activar el ajuste o en la siguiente sesión

#### Scenario: Poco espacio con menos de 20 sesiones
- **WHEN** hay 5 sesiones grabadas y el espacio libre baja del mínimo durante una grabación
- **THEN** la grabación se detiene y las 5 sesiones siguen en la lista

### Requirement: Lista de sesiones grabadas
La pantalla «Sesiones grabadas» SHALL listar las sesiones, la más reciente primero, con la fecha y la hora de inicio y el tamaño de cada una, y SHALL verse aunque «Grabar sesiones» esté apagado. La sesión que se está grabando SHALL marcarse como «Grabando» y la detenida por poco espacio o por un error, con su motivo. Sin sesiones SHALL decirlo con un estado vacío. Una línea de ayuda SHALL avisar de que los archivos pueden incluir direcciones de red y nombres de dispositivos y de que se conservan las 20 sesiones más recientes.

#### Scenario: Lista con sesiones
- **WHEN** hay tres sesiones grabadas y se abre «Sesiones grabadas»
- **THEN** se ven las tres, la más reciente primero, cada una con su fecha, su hora y su tamaño

#### Scenario: Sesión en curso
- **WHEN** se abre la pantalla con una grabación en curso
- **THEN** esa sesión sale la primera con «Grabando» y su tamaño crece al volver a mirar

#### Scenario: Sin sesiones
- **WHEN** no hay ninguna sesión grabada
- **THEN** la pantalla dice que no hay sesiones y cómo grabarlas, sin lista

#### Scenario: Aviso de contenido
- **WHEN** se abre la pantalla
- **THEN** una línea de ayuda dice que los archivos pueden incluir direcciones de red y nombres de dispositivos y que se conservan las 20 sesiones más recientes

### Requirement: Abrir y compartir una sesión
Cada sesión de la lista SHALL poder abrirse con otra app del móvil y compartirse con la hoja de compartir del sistema, como texto plano, dando acceso de lectura solo a ese archivo y sin pedir permisos nuevos. Si ninguna app puede abrirla, la app SHALL decirlo sin cerrarse.

#### Scenario: Abrir
- **WHEN** se toca una sesión de la lista
- **THEN** el sistema ofrece las apps que abren texto plano y la elegida muestra el contenido de esa sesión

#### Scenario: Compartir
- **WHEN** se toca «Compartir» en una sesión
- **THEN** se abre la hoja de compartir del sistema con ese archivo y la app elegida recibe solo ese archivo

#### Scenario: Ninguna app lo abre
- **WHEN** no hay ninguna app que abra texto plano y se toca una sesión
- **THEN** la app avisa de que no hay con qué abrirla y sigue funcionando

### Requirement: Borrar sesiones
Cada sesión SHALL poder borrarse y la lista SHALL ofrecer «Borrar todas», ambas con una confirmación antes de borrar. La sesión que se está grabando SHALL NOT poder borrarse y «Borrar todas» SHALL respetarla. Al borrar, el espacio SHALL liberarse y la lista SHALL actualizarse.

#### Scenario: Borrar una
- **WHEN** se toca «Borrar» en una sesión y se confirma
- **THEN** el archivo desaparece de la lista y del almacenamiento de la app

#### Scenario: Cancelar el borrado
- **WHEN** se toca «Borrar» y se cancela la confirmación
- **THEN** la sesión sigue en la lista

#### Scenario: Borrar todas
- **WHEN** hay cuatro sesiones cerradas y se toca «Borrar todas» y se confirma
- **THEN** la lista queda vacía

#### Scenario: Borrar con una grabación en curso
- **WHEN** hay una grabación en curso y se toca «Borrar todas» y se confirma
- **THEN** se borran las cerradas y la que se graba sigue en la lista, grabando

#### Scenario: La que se graba no se borra
- **WHEN** se mira la sesión que se está grabando
- **THEN** no ofrece «Borrar»

### Requirement: Sesiones conservadas
La app SHALL conservar como mucho las 20 sesiones grabadas más recientes. Al empezar una grabación, SHALL borrar las sesiones cerradas más antiguas hasta que queden 20 contando la nueva, y SHALL anotarlo en el registro. La sesión que se está grabando SHALL NOT borrarse nunca. Si no se puede borrar una sesión antigua, la grabación SHALL empezar igualmente y el fallo SHALL anotarse en el registro.

#### Scenario: Por debajo del límite
- **WHEN** hay 12 sesiones grabadas y empieza una grabación nueva
- **THEN** no se borra ninguna y la lista tiene 13

#### Scenario: En el límite
- **WHEN** hay 20 sesiones grabadas y empieza una grabación nueva
- **THEN** se borra la más antigua, la lista tiene 20 con la nueva arriba marcada «Grabando» y el registro dice que se borró una sesión antigua

#### Scenario: Por encima del límite
- **WHEN** hay 25 sesiones grabadas (por ejemplo, de una versión anterior sin límite) y empieza una grabación nueva
- **THEN** se borran las 6 más antiguas y quedan 20 con la nueva

#### Scenario: Sin grabar no se borra nada
- **WHEN** hay 25 sesiones grabadas y se rueda con «Grabar sesiones» apagado
- **THEN** las 25 siguen en la lista

#### Scenario: No se puede borrar una antigua
- **WHEN** hay 20 sesiones grabadas, la más antigua no se puede borrar y empieza una grabación nueva
- **THEN** la grabación empieza, la lista tiene 21 y el registro dice que no se pudo borrar la sesión antigua
