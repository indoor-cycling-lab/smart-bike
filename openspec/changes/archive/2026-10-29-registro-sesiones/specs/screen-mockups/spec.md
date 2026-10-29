# Spec Delta

## ADDED Requirements

### Requirement: Maqueta de Sesiones grabadas
La maqueta de Sesiones grabadas SHALL mostrar, como secundaria de Ajustes (botón de volver y sin barra inferior), la ayuda sobre el contenido de los archivos, el estado de la grabación, la lista de sesiones con fecha, hora y tamaño, y «Borrar todas». Cada sesión SHALL ofrecer abrir, compartir y borrar con objetivos táctiles de al menos 48 dp.

#### Scenario: Estado inicial
- **WHEN** se abre `maquetas/sesiones.html#estado=inicio`
- **THEN** se ve una sesión «Grabando» la primera, sin acción de borrar, seguida de sesiones cerradas, cada una con su fecha, su hora, su tamaño y las acciones de abrir, compartir y borrar, la ayuda sobre el contenido y «Borrar todas»

#### Scenario: Sin sesiones
- **WHEN** se abre `maquetas/sesiones.html#estado=vacia`
- **THEN** se ve el estado vacío con cómo grabar sesiones y sin «Borrar todas»

#### Scenario: Detenida por poco espacio y por un error
- **WHEN** se eligen los estados de parada por poco espacio y por un error
- **THEN** la sesión afectada dice «Detenida por poco espacio» o «Detenida por un error» y la parte superior explica cómo reanudar la grabación

#### Scenario: Estados disponibles
- **WHEN** se abre el selector de estado de `maquetas/sesiones.html`
- **THEN** ofrece la lista con una sesión grabándose, la lista con todas cerradas, sin sesiones, detenida por poco espacio, detenida por un error y las confirmaciones de borrar una y todas

#### Scenario: Confirmaciones
- **WHEN** se eligen los estados de confirmar el borrado de una y de todas
- **THEN** se pide confirmar antes de borrar, con «Borrar» y «Cancelar», y la sesión que se está grabando no se ofrece para borrar

### Requirement: Sección Depuración en la maqueta de Ajustes
La maqueta de Ajustes SHALL mostrar al final, después de «Copia de seguridad», la sección «Depuración» con el interruptor «Grabar sesiones», «Espacio libre mínimo» con su stepper y la entrada «Sesiones grabadas» con su resumen, con estados para la grabación apagada, grabando y detenida por poco espacio.

#### Scenario: Apagada
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-apagada`
- **THEN** «Grabar sesiones» está apagado, «Espacio libre mínimo» dice 10 % y la entrada «Sesiones grabadas» resume las sesiones existentes

#### Scenario: Grabando
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-grabando`
- **THEN** «Grabar sesiones» está activado y debajo se lee «Grabando»

#### Scenario: Detenida por poco espacio
- **WHEN** se abre `maquetas/ajustes.html#estado=depuracion-detenida`
- **THEN** debajo del interruptor se lee «Detenida por poco espacio» con cómo reanudarla
