# Spec Delta

## MODIFIED Requirements

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

## ADDED Requirements

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
