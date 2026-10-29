# Spec Delta

## Purpose

Que la app de entrenamiento vea la app por Bluetooth con el nombre elegido en «Nombre ante la app» y no con el del móvil. Para ello, el móvil se renombra solo mientras la app publica por Bluetooth y después recupera su nombre.

## ADDED Requirements

### Requirement: Renombrar el móvil mientras publica por Bluetooth
Con «Usar el nombre también por Bluetooth» activado, la app SHALL cambiar el nombre Bluetooth del móvil por el nombre ante la app mientras publique algo por Bluetooth: el rodillo con «Solo Bluetooth», o el controlador con «Wifi + Bluetooth». SHALL anunciarse ya con el nombre nuevo. Con el ajuste desactivado, SHALL NOT cambiar el nombre del móvil.

#### Scenario: Solo Bluetooth con el ajuste activado
- **WHEN** el ajuste está activado, el modo es «Solo Bluetooth», «Smart Bike» está elegido, el móvil se llama «OnePlus 8» y se abre el emparejamiento de Zwift
- **THEN** Zwift lista «Smart Bike» como rodillo, potencia y cadencia, y no lista «OnePlus 8»

#### Scenario: Controlador aparte con el ajuste activado
- **WHEN** el ajuste está activado, el modo es «Wifi + Bluetooth», hay un Square configurado y se abre la lista de mandos de Zwift
- **THEN** el controlador aparece como «Smart Bike», el mismo nombre que el rodillo por wifi

#### Scenario: KICKR BIKE PRO
- **WHEN** el ajuste está activado, el modo es «Solo Bluetooth» y «KICKR BIKE PRO» está elegido
- **THEN** el móvil se llama «KICKR BIKE PRO» seguido de cuatro caracteres hexadecimales, los mismos en cada sesión

#### Scenario: Nada por Bluetooth
- **WHEN** el ajuste está activado y el modo es «Wifi + Bluetooth» sin Square configurado, o «Solo wifi (modo Zwift Ride)»
- **THEN** el móvil conserva su nombre

#### Scenario: Ajuste desactivado
- **WHEN** el ajuste está desactivado y el modo es «Solo Bluetooth»
- **THEN** el móvil conserva su nombre y Zwift lo lista como «OnePlus 8»

#### Scenario: Otros dispositivos durante la sesión
- **WHEN** el móvil está renombrado a «Smart Bike» y otro dispositivo (auriculares, coche, otro móvil) busca o muestra el móvil
- **THEN** lo ve como «Smart Bike» mientras dure el renombrado

### Requirement: Recuperar el nombre del móvil
Cuando la app deja de publicar por Bluetooth (termina la sesión, cambia a un modo o a un estado sin nada por Bluetooth o se desactiva el ajuste), SHALL devolver al móvil el nombre que tenía antes de renombrarlo. Si el usuario ha cambiado el nombre del móvil mientras estaba renombrado, SHALL NOT tocarlo.

#### Scenario: Terminar la sesión
- **WHEN** el móvil está renombrado a «Smart Bike» y se termina la sesión
- **THEN** el móvil vuelve a llamarse «OnePlus 8»

#### Scenario: Desactivar el ajuste rodando
- **WHEN** el móvil está renombrado y Zwift conectado por Bluetooth, y se desactiva el ajuste
- **THEN** el móvil vuelve a llamarse «OnePlus 8», la app se anuncia con ese nombre y la marcha y lo que simula el Neo no cambian

#### Scenario: Cambiar a Solo wifi
- **WHEN** el móvil está renombrado con «Solo Bluetooth» y se elige «Solo wifi (modo Zwift Ride)»
- **THEN** el móvil vuelve a llamarse «OnePlus 8»

#### Scenario: Olvidar el Square con el controlador aparte
- **WHEN** el móvil está renombrado con «Wifi + Bluetooth» por el controlador y se olvida el Square
- **THEN** el móvil vuelve a llamarse «OnePlus 8» y el rodillo sigue por wifi sin cambios

#### Scenario: Cambiar el nombre elegido
- **WHEN** el móvil está renombrado a «Smart Bike» y se elige «KICKR BIKE PRO»
- **THEN** el móvil pasa a llamarse «KICKR BIKE PRO» con sus cuatro caracteres y, al terminar la sesión, vuelve a «OnePlus 8»

#### Scenario: El usuario renombra el móvil
- **WHEN** el móvil está renombrado a «Smart Bike», el usuario lo llama «Bici» en los ajustes de Android y se termina la sesión
- **THEN** el móvil sigue llamándose «Bici»

### Requirement: Recuperar el nombre tras un cierre inesperado
Si la app se cierra sin haber recuperado el nombre del móvil (Android la mata, se fuerza su cierre, se reinicia el móvil), SHALL recuperarlo la siguiente vez que se abra, antes de anunciarse, salvo que el usuario lo haya cambiado entretanto.

#### Scenario: Cierre forzado
- **WHEN** el móvil está renombrado a «Smart Bike», se fuerza el cierre de la app y se vuelve a abrir con el ajuste desactivado
- **THEN** el móvil vuelve a llamarse «OnePlus 8»

#### Scenario: Reabrir con el ajuste activado
- **WHEN** el móvil quedó renombrado por un cierre forzado y se vuelve a abrir la app con el ajuste activado y «Solo Bluetooth»
- **THEN** el móvil sigue llamándose «Smart Bike» durante la sesión y vuelve a «OnePlus 8» al terminarla

#### Scenario: Nombre cambiado entretanto
- **WHEN** el móvil quedó renombrado por un cierre forzado y el usuario lo llama «Bici» antes de volver a abrir la app
- **THEN** la app no cambia el nombre «Bici»

### Requirement: Renombrado imposible
Si el móvil no se puede renombrar (Bluetooth apagado, Android lo rechaza o no confirma el cambio a tiempo), la app SHALL anunciarse con el nombre del móvil, decirlo en Conexión y volver a intentarlo cuando se encienda el Bluetooth. SHALL NOT dejar de publicar por no poder renombrar.

#### Scenario: Android lo rechaza
- **WHEN** el ajuste está activado, el modo es «Solo Bluetooth» y Android no acepta el nombre nuevo
- **THEN** la app se anuncia como «OnePlus 8», Conexión dice que no se pudo cambiar el nombre y que se usa «OnePlus 8», y el registro lo apunta

#### Scenario: Bluetooth apagado
- **WHEN** el ajuste está activado, el modo es «Solo Bluetooth» y se enciende el Bluetooth, que estaba apagado
- **THEN** el móvil pasa a llamarse «Smart Bike» antes de que la app se anuncie

#### Scenario: Registro
- **WHEN** la app renombra el móvil o recupera su nombre
- **THEN** el registro apunta el nombre anterior y el nuevo
