# Spec Delta

## Purpose

Define las maquetas de las pantallas de la app (rodaje, dispositivos, botones, marchas, ajustes, datos en pantalla y diagnóstico), su navegación y los estados que cada una debe representar, como referencia validada antes de implementarlas.

## ADDED Requirements

### Requirement: Maquetas sobre el sistema de diseño
Cada maqueta SHALL ser una página HTML estática que use solo los tokens y componentes del sistema de diseño, sin estilos propios ni atributos `style` salvo variables que el sistema documente, y la hoja compartida de la herramienta de revisión.

#### Scenario: Sin estilos propios
- **WHEN** se revisa el código de cualquier maqueta
- **THEN** no contiene hojas de estilo propias ni atributos `style` con valores literales

### Requirement: Herramienta de revisión de estado y tema
Cada maqueta SHALL tener una barra de revisión, visiblemente distinta de la app, con un selector de tema (rodaje o claro) y, si tiene varios estados, un selector de estado; el estado y el tema SHALL ir en la dirección para poder enlazarlos.

#### Scenario: Enlace a un estado
- **WHEN** se abre `maquetas/rodaje.html#estado=tope&tema=claro`
- **THEN** se ve la pantalla de rodaje en el estado de tope y en tema claro, con los selectores mostrando esos valores

#### Scenario: Sin JavaScript
- **WHEN** se abre una maqueta con JavaScript desactivado
- **THEN** se ven todos sus estados apilados

### Requirement: Índice de maquetas
El índice SHALL enlazar cada pantalla y cada uno de sus estados, indicar cuál es el estado por defecto y relacionar cada pantalla con sus funcionalidades del documento funcional.

#### Scenario: Revisar un cambio
- **WHEN** alguien abre el índice para validar un cambio
- **THEN** puede ir con un clic a cada estado afectado

### Requirement: Datos ficticios sin servicios externos
Las maquetas SHALL usar datos ficticios con forma realista (potencias, cadencias, transmisiones de marca, combinaciones de botones), marcar entre corchetes lo pendiente de capturar con el equipo real y SHALL NOT hacer peticiones a servicios externos.

#### Scenario: Abrir sin conexión
- **WHEN** se abre una maqueta servida en local sin conexión a internet
- **THEN** se ve completa, con sus fuentes e iconos

### Requirement: Marco de dispositivo
Cada maqueta SHALL mostrarse en un ordenador dentro de un marco del tamaño del móvil (844 × 390 dp en horizontal, 390 dp de ancho en vertical) y SHALL ocupar todo el ancho al abrirla en un móvil.

#### Scenario: Rodaje en el móvil
- **WHEN** se abre la maqueta de rodaje en un móvil en horizontal
- **THEN** ocupa toda la pantalla sin el marco

### Requirement: Pantalla de rodaje
La maqueta de rodaje SHALL mostrar, sin tocar la pantalla, el modo, la transmisión, las conexiones, la marcha con su indicador de posición, la rejilla de datos y el aviso de acción, y SHALL NOT mostrar datos del modelo de resistencia.

#### Scenario: Estado por defecto
- **WHEN** se abre la maqueta de rodaje sin estado
- **THEN** se ve la transmisión lineal de 24 marchas con 2 datos (potencia y cadencia) y sin botones − y +

#### Scenario: Transmisiones de marca y datos
- **WHEN** se recorren los estados de rodaje
- **THEN** hay al menos Shimano, SRAM y Campagnolo, y rejillas de 2, 3, 4 y 6 datos

#### Scenario: Botones táctiles
- **WHEN** se elige el estado con botones − y +
- **THEN** aparecen a la derecha, de 72 dp

#### Scenario: Tope
- **WHEN** se elige el estado de tope
- **THEN** la marcha aparece atenuada con la etiqueta de tope

#### Scenario: Conexión perdida
- **WHEN** se elige el estado de Neo desconectado
- **THEN** el chip del Neo muestra «sin conexión», la marcha sigue visible y los datos que dependen del Neo muestran «—»

#### Scenario: Entrenamiento ERG
- **WHEN** se elige el estado ERG de Zwift
- **THEN** la cifra principal es la potencia objetivo en el color de ERG, con el ajuste de las palancas y la alerta de cadencia baja

### Requirement: Pantalla de rodaje en vertical
La maqueta vertical SHALL apilar conexiones y modo, marcha con plato y cassette, rejilla de datos a 2 columnas que ocupa el resto del alto, aviso y, si están activados, botones − y + al pie.

#### Scenario: Seis datos en vertical
- **WHEN** se elige el estado de 6 datos en vertical
- **THEN** se ven 2 columnas y 3 filas sin desplazamiento

### Requirement: Navegación entre pantallas
Las pantallas de pestaña SHALL compartir la navegación inferior (Rodar, Dispositivos, Botones, Ajustes), y las secundarias (Marchas, Datos en pantalla, Diagnóstico, Asignar acción) SHALL tener un botón de volver a su pantalla de origen; los enlaces SHALL funcionar entre maquetas.

#### Scenario: De Ajustes a Marchas y vuelta
- **WHEN** en Ajustes se toca «Marchas» y luego volver
- **THEN** se abre la maqueta de Marchas y después se vuelve a Ajustes

### Requirement: Dispositivos
La maqueta de Dispositivos SHALL mostrar el Neo, el Square, Zwift y el mando emulado con su estado, cómo emparejar la app en Zwift y las acciones de buscar y olvidar, con los estados de primer uso, todo conectado y Neo sin respuesta.

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** el Square aparece buscando, con la ayuda «Pulsa cualquier botón para despertarlo», y Zwift sin conexión

### Requirement: Botones y asignación
La maqueta de Botones SHALL mostrar la asignación por defecto en dos grupos, funciones de Zwift y extras de la app, y la de Asignar acción SHALL mostrar los estados esperando, combinación libre y combinación ya asignada.

#### Scenario: Funciones de Zwift primero
- **WHEN** se abre la maqueta de Botones
- **THEN** el primer grupo reproduce un mando oficial de Zwift (palancas, girar, frenar, cruceta, A, B, Y, Z)

### Requirement: Marchas
La maqueta de Marchas SHALL mostrar la elección de transmisión (lineal por defecto, Shimano, SRAM, Campagnolo, a medida), los platos y el cassette de la elegida, el modo de cambio (como los mandos de la marca por defecto, o sincronizado), la marcha de referencia y la simulación avanzada plegada.

#### Scenario: Transmisión lineal
- **WHEN** se elige el estado de transmisión lineal
- **THEN** se ve la escalera de 24 marchas y el número de marchas, y no aparece el modo de cambio con platos

### Requirement: Ajustes y datos en pantalla
La maqueta de Ajustes SHALL agrupar Marchas, Diagnóstico, Al rodar, Ciclista, Rodillo, Gestos y Pantalla, y la de Datos en pantalla SHALL permitir elegir hasta 6 datos con vista previa, en los estados de 2, 4 y 6 datos.

#### Scenario: Límite de seis
- **WHEN** se elige el estado de 6 datos
- **THEN** el contador dice «6 de 6» y los datos no elegidos no se pueden marcar

### Requirement: Diagnóstico
La maqueta de Diagnóstico SHALL mostrar los botones del Square con los pulsados resaltados, el último gesto con su acción, la simulación (marcha, modelo, pendiente de Zwift, ajuste, lo enviado al Neo), el tráfico y el control manual del Neo.

#### Scenario: Último gesto
- **WHEN** se abre la maqueta de Diagnóstico
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas»
