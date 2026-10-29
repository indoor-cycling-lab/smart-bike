## Purpose

Decidir qué hace cada botón, gesto o combinación del Square: la asignación por defecto, reasignarla capturando la entrada en el Square, invertir las palancas, restaurarla y guardarla, con las pantallas de Botones y Asignar acción.

## ADDED Requirements

### Requirement: Acciones asignables
La app SHALL permitir asignar a un botón, gesto o combinación estas acciones: +1 marcha, −1 marcha, +3 marchas, −3 marchas, marcha más dura, marcha más ligera, volver a la marcha de referencia y fijar la marcha de referencia. Un botón, gesto o combinación sin acción SHALL NOT hacer nada en la app.

#### Scenario: Botón sin acción
- **WHEN** se pulsa A con la asignación por defecto
- **THEN** la marcha no cambia y la app no hace nada

### Requirement: Asignación por defecto
Mientras el usuario no la cambie, la app SHALL usar esta asignación: palancas D1 y D2 «pulsar» +1 marcha; palancas I1 e I2 «pulsar» −1 marcha; Campag. D + palanca D1 +3 marchas; Campag. D + palanca D2 marcha más dura; Campag. I + palanca I1 −3 marchas; Campag. I + palanca I2 marcha más ligera; □ corta volver a la referencia; □ larga fijar la referencia. El resto de botones SHALL quedar sin acción en la app.

#### Scenario: Primer uso
- **WHEN** se abre la app por primera vez y se mantiene □ más que el umbral de pulsación larga con la marcha en la 15
- **THEN** la referencia pasa a la 15

### Requirement: Pantalla de Botones
La pantalla de Botones SHALL mostrar dos grupos: «Funciones de Zwift», informativo y sin poder tocarse, y «Acciones de la app», con una fila por acción con sus botones y gestos, o «Sin botón» si no tiene ninguno. Tocar una acción de la app SHALL abrir Asignar acción. La pantalla SHALL incluir los tiempos de los gestos, la autorrepetición, la inversión de palancas y «Restaurar asignación por defecto».

#### Scenario: Acción sin botón
- **WHEN** se quita el único botón de «Fijar la marcha de referencia» y se vuelve a Botones
- **THEN** la fila de esa acción dice «Sin botón»

#### Scenario: Funciones de Zwift
- **WHEN** se abre Botones
- **THEN** el grupo de Zwift dice que esas funciones las resuelve Zwift con el Square emparejado, y sus filas no abren nada al tocarlas

### Requirement: Asignar pulsando en el Square
En Asignar acción, la app SHALL esperar a que se pulse en el Square un botón o una combinación con un Campagnolo, mostrarlo con sus teclas y dejar elegir el gesto (pulsar, corta, larga o doble). Mientras la pantalla está abierta, los botones del Square SHALL NOT ejecutar sus acciones. La asignación SHALL aplicarse solo al tocar «Guardar».

#### Scenario: Capturar una combinación
- **WHEN** en Asignar acción de «−3 marchas» se mantiene Campag. D y se pulsa △, se elige «corta» y se toca «Guardar»
- **THEN** a partir de ese momento, Campag. D + △ corta baja tres marchas

#### Scenario: Botones sin efecto al capturar
- **WHEN** la marcha es la 14, Asignar acción está abierta y se pulsa la palanca D1
- **THEN** la palanca D1 aparece como capturada y la marcha sigue en la 14

#### Scenario: Cancelar
- **WHEN** se captura △ y se toca «Cancelar»
- **THEN** la asignación no cambia y se vuelve a Botones

#### Scenario: Square sin conexión
- **WHEN** se abre Asignar acción con el Square sin conexión
- **THEN** la zona de captura dice «Conecta el Square para capturar un botón» y «Guardar» está deshabilitado hasta capturar algo

#### Scenario: Campagnolo solo
- **WHEN** en la captura se pulsa y se suelta Campag. I sin pulsar otro botón
- **THEN** se captura Campag. I y el gesto «pulsar» no se puede elegir

### Requirement: Varias entradas por acción
Una acción SHALL poder tener varios botones o combinaciones; Asignar acción SHALL mostrar los que tiene, permitir quitar cada uno y añadir el capturado.

#### Scenario: Quitar una palanca
- **WHEN** en Asignar acción de «+1 marcha» se quita la palanca D2 y se guarda
- **THEN** la palanca D2 deja de subir marcha y la palanca D1 sigue subiendo

### Requirement: Aviso de entrada en uso
Si la entrada capturada con su gesto ya tiene otra acción, o «pulsar» y «corta», «larga» o «doble» chocan sobre el mismo botón, Asignar acción SHALL avisar, antes de guardar, de qué acciones perderán esa entrada; al guardar, la entrada SHALL pasar a la acción nueva.

#### Scenario: Entrada de otra acción
- **WHEN** en Asignar acción de «−3 marchas» se captura la palanca D1 con «pulsar»
- **THEN** un aviso dice que la palanca D1 dejará de hacer «+1 marcha», y al guardar la palanca D1 baja tres marchas

#### Scenario: Entrada libre
- **WHEN** se captura Campag. D + △ con «corta», que no tiene acción
- **THEN** un aviso informativo dice que está libre

### Requirement: Aviso de botones que usa Zwift
Si la entrada capturada es un botón que Zwift lee con el Square emparejado (cruceta, A, B, Y, Z, ✕, ○ o los frenos), Asignar acción SHALL avisar de que Zwift también lo recibirá.

#### Scenario: Asignar A
- **WHEN** se captura A
- **THEN** un aviso dice que, con el Square emparejado en Zwift, Zwift también recibirá A (Seleccionar)

### Requirement: Invertir las palancas
Con «Invertir palancas» activado, cada palanca SHALL hacer lo que tiene asignado la palanca del otro lado con el mismo número, también en combinación con el Campagnolo de su lado. Por defecto SHALL estar desactivado.

#### Scenario: Palancas invertidas
- **WHEN** se activa «Invertir palancas» y se pulsa la palanca D1 en la marcha 14
- **THEN** la marcha pasa a la 13

#### Scenario: Combinación invertida
- **WHEN** «Invertir palancas» está activado, se mantiene Campag. D y se pulsa la palanca D1 en la marcha 14
- **THEN** la marcha pasa a la 11

### Requirement: Restaurar la asignación por defecto
«Restaurar asignación por defecto» SHALL pedir confirmación y, al confirmarla, SHALL volver a la asignación por defecto, desactivar la inversión de palancas y poner los tiempos y la autorrepetición en sus valores por defecto.

#### Scenario: Restaurar
- **WHEN** la palanca D1 baja tres marchas por una reasignación, se toca «Restaurar asignación por defecto» y se confirma
- **THEN** la palanca D1 vuelve a subir una marcha

#### Scenario: Restaurar cancelado
- **WHEN** se toca «Restaurar asignación por defecto» y se cancela
- **THEN** la asignación no cambia

### Requirement: Asignación guardada
La asignación, la inversión de palancas, los tiempos y la autorrepetición SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se asigna Campag. D + △ corta a «−3 marchas» y se cierra y se vuelve a abrir la app
- **THEN** Campag. D + △ corta sigue bajando tres marchas

#### Scenario: Configuración ilegible
- **WHEN** el fichero de configuración está dañado y se abre la app
- **THEN** la app usa la asignación por defecto y funciona con normalidad

### Requirement: Botones desde Diagnóstico
Mientras no exista la navegación inferior, Botones SHALL abrirse desde Diagnóstico, con un botón de volver a Diagnóstico; Asignar acción SHALL volver a Botones.

#### Scenario: Ida y vuelta
- **WHEN** en Diagnóstico se toca «Asignación de botones», luego volver
- **THEN** se abre Botones y después se vuelve a Diagnóstico, sin terminar la sesión
