# Spec Delta

## Purpose

Manejar un ventilador Wahoo KICKR Headwind por Bluetooth desde la app: elegir si va en manual o en automático por pulso, subir y bajar su velocidad en manual y fijar los umbrales de pulso de las zonas automáticas, sin depender de la app de Wahoo.

## ADDED Requirements

### Requirement: Ventilador opcional
La app SHALL poder conectarse a un ventilador Wahoo KICKR Headwind por Bluetooth y SHALL funcionar igual que hasta ahora, sin filas, secciones, iconos ni avisos del ventilador, mientras no haya un ventilador recordado. La app SHALL NOT emparejar ni cambiar la banda de pulso del ventilador.

#### Scenario: Sin ventilador
- **WHEN** el usuario nunca ha buscado un ventilador
- **THEN** Rodaje, Ajustes, Dispositivos y Diagnóstico no muestran ningún aviso ni error del ventilador, y las conexiones del Neo, el Square y Zwift se comportan como siempre

#### Scenario: Dispositivo que no es un ventilador
- **WHEN** la app se conecta a un dispositivo con nombre «HEADWIND…» que no ofrece el control del ventilador
- **THEN** la app lo da por no válido, no lo recuerda y lo explica en Dispositivos

### Requirement: Modo del ventilador
La app SHALL mostrar el modo del ventilador como «Manual», «Automático» (sigue el pulso de la banda que el ventilador tiene emparejada) u «Otro modo», y SHALL permitir pasar de cualquiera a manual o a automático. El modo mostrado SHALL ser el que comunica el ventilador, también si se cambió desde fuera. La app SHALL NOT decidir la velocidad en automático: lo hace el ventilador.

#### Scenario: Pasar a automático
- **WHEN** el ventilador está en manual y se pide automático
- **THEN** el ventilador pasa a «Automático» y su velocidad la decide él según el pulso

#### Scenario: Volver a manual
- **WHEN** el ventilador está en automático y se pide manual
- **THEN** el ventilador pasa a «Manual» con la última velocidad manual que tenía

#### Scenario: Otro modo
- **WHEN** el ventilador está apagado o en un modo por sensor distinto del pulso
- **THEN** la app dice «Otro modo» y permite pasarlo a manual o a automático

#### Scenario: Cambio desde fuera
- **WHEN** con la app conectada se cambia el modo desde el propio ventilador
- **THEN** la app muestra el modo nuevo en pocos segundos, sin que el usuario haga nada

### Requirement: Velocidad manual
En manual, la app SHALL mostrar la velocidad del ventilador en tanto por ciento y SHALL permitir subirla y bajarla de 10 en 10 entre 0 y 100 %. Un paso que pasaría del extremo SHALL dejarla en el extremo. En automático, la app SHALL NOT cambiar la velocidad.

#### Scenario: Subir
- **WHEN** el ventilador está en manual al 40 % y se sube la velocidad
- **THEN** pasa al 50 %

#### Scenario: Extremo superior
- **WHEN** el ventilador está en manual al 100 % y se sube la velocidad
- **THEN** sigue al 100 %

#### Scenario: Extremo inferior
- **WHEN** el ventilador está en manual al 0 % y se baja la velocidad
- **THEN** sigue al 0 %, con el ventilador parado

#### Scenario: Subidas seguidas
- **WHEN** se suben tres pasos seguidos en menos de un segundo desde el 20 %
- **THEN** la velocidad acaba al 50 %, sin perder pasos aunque el ventilador aún no haya comunicado la velocidad intermedia

#### Scenario: Automático
- **WHEN** el ventilador está en automático y se pide subir o bajar la velocidad
- **THEN** la velocidad no cambia

### Requirement: Umbrales de pulso de las zonas
La app SHALL permitir ver y cambiar, en bpm y en manual o en automático, los umbrales de las cuatro zonas de pulso del automático, siempre en orden creciente: un umbral SHALL NOT ser menor ni igual que el anterior. Cambiar uno SHALL conservar el resto de la configuración del ventilador. Los valores mostrados SHALL ser los que tiene el ventilador, no lo último que la app le envió.

#### Scenario: Por debajo del primer umbral
- **WHEN** el ventilador está en automático y el pulso es menor que el umbral de la zona 1
- **THEN** el ventilador está parado, y empieza a moverse al llegar a ese umbral; la app no decide nada de esto

#### Scenario: Ver los umbrales
- **WHEN** se conecta un ventilador cuyas zonas terminan en 120, 135, 150 y 165 bpm
- **THEN** la app muestra esos cuatro valores

#### Scenario: Cambiar un umbral
- **WHEN** se sube el umbral de la zona 2 de 135 a 138 bpm
- **THEN** el ventilador pasa a usar 138 en esa zona y el resto de umbrales y de ajustes del ventilador no cambian

#### Scenario: Orden creciente
- **WHEN** el umbral de la zona 3 es 150 y se intenta bajar el de la zona 4 de 165 a 150
- **THEN** el umbral de la zona 4 se queda en 151 y no baja más

#### Scenario: Valores guardados en el ventilador
- **WHEN** se cambia un umbral, se cierra la app y se abre otra vez con el ventilador conectado
- **THEN** la app muestra el umbral cambiado

### Requirement: Acciones de los botones según el modo
Las acciones «Ventilador: manual ⇄ automático» (desde «Otro modo» pasa a manual), «Ventilador: + velocidad» y «Ventilador: − velocidad» SHALL actuar sobre el ventilador conectado según su modo; las de velocidad solo cambian algo en manual y en automático avisan «Ventilador en automático». Sin ventilador conectado, las tres SHALL NOT hacer nada y avisan «Sin ventilador». Ninguna SHALL cambiar la marcha, la potencia ni nada que llegue a Zwift o al Neo.

#### Scenario: Alternar desde manual
- **WHEN** el ventilador está en manual y se pulsa el botón de «Ventilador: manual ⇄ automático»
- **THEN** pasa a automático y el aviso dice «Ventilador: automático»

#### Scenario: Alternar desde automático
- **WHEN** el ventilador está en automático y se pulsa ese botón
- **THEN** pasa a manual y el aviso dice «Ventilador: manual · 40 %» con su velocidad

#### Scenario: Alternar desde otro modo
- **WHEN** el ventilador está en otro modo y se pulsa ese botón
- **THEN** pasa a manual

#### Scenario: Subir en manual
- **WHEN** el ventilador está en manual al 40 % y se pulsa el botón de «Ventilador: + velocidad»
- **THEN** pasa al 50 % y el aviso dice «Ventilador: 50 %»

#### Scenario: Extremo con el botón
- **WHEN** el ventilador está al 100 % y se pulsa «Ventilador: + velocidad»
- **THEN** sigue al 100 % y el aviso dice «Ventilador: 100 %»

#### Scenario: Velocidad en automático
- **WHEN** el ventilador está en automático y se pulsa «Ventilador: + velocidad» o «Ventilador: − velocidad»
- **THEN** no cambia nada y el aviso dice «Ventilador en automático»

#### Scenario: Sin ventilador conectado
- **WHEN** hay un ventilador recordado pero no conectado y se pulsa una de las tres acciones
- **THEN** no pasa nada en el ventilador y el aviso dice «Sin ventilador»

#### Scenario: Sin ventilador recordado
- **WHEN** no hay ventilador recordado y se asigna y pulsa una de las tres acciones
- **THEN** no pasa nada en la app y el aviso dice «Sin ventilador»

#### Scenario: Las marchas no cambian
- **WHEN** se pulsa cualquiera de las tres acciones rodando en Zwift
- **THEN** la marcha, la potencia y la resistencia del Neo no cambian y a Zwift no llega ningún dato nuevo

### Requirement: Conexión perdida con el ventilador
Si se pierde la conexión con el ventilador, la app SHALL intentar recuperarla sola, sin límite y sin avisos de error, y SHALL NOT afectar a la marcha, a los botones, al Neo, al Square ni a Zwift. El ventilador SHALL seguir con el modo y la velocidad que tenía, pues los guarda él. La app SHALL NOT enviarle nada de lo pendiente al reconectar, sino partir de lo que él comunique.

#### Scenario: Ventilador apagado o fuera de alcance
- **WHEN** se rueda con el ventilador conectado y se pierde la conexión
- **THEN** el icono del ventilador pasa a atenuado con la marca que gira, la marcha y los botones del Square siguen igual y el ventilador sigue como estaba

#### Scenario: Reconexión
- **WHEN** el ventilador vuelve a estar a su alcance
- **THEN** la app se conecta sola y muestra el modo y la velocidad que él comunica

#### Scenario: Cambio con la conexión perdida
- **WHEN** se pulsa «Ventilador: + velocidad» con la conexión perdida
- **THEN** no se envía nada ni se encola, y al reconectar el ventilador sigue como estaba
