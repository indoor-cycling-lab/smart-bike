## MODIFIED Requirements

### Requirement: Dispositivos
La maqueta de Dispositivos SHALL mostrar el Neo y el Square con su estado en palabra, su ayuda para despertarlos y «Olvidar» si están recordados, Zwift con su estado y cómo emparejar la app en Zwift, el mando de Zwift con su estado y cómo emparejarlo, «Buscar dispositivos» y la navegación inferior. SHALL tener los estados de primer uso, todo conectado, Neo conectando, Square no encontrado, Zwift esperando, Zwift no visible, sin wifi, Bluetooth apagado, mando esperando, mando desactivado y mando no disponible. SHALL NOT mostrar la batería del Square ni la elección de wifi o Bluetooth.

#### Scenario: Primer uso
- **WHEN** se elige el estado de primer uso
- **THEN** el Neo y el Square dicen «Sin conexión», sin «Olvidar», y «Buscar dispositivos» es la acción principal

#### Scenario: Todo conectado
- **WHEN** se elige el estado todo conectado
- **THEN** el Neo, el Square, Zwift y el mando dicen «Conectado», y el Neo y el Square tienen «Olvidar»

#### Scenario: Neo conectando
- **WHEN** se elige el estado Neo conectando
- **THEN** la fila del Neo dice «Conectando» con «Pedalea para despertarlo» y tiene «Olvidar»

#### Scenario: Square no encontrado
- **WHEN** se elige el estado Square no encontrado
- **THEN** la fila del Square dice «No encontrado» y se ve el aviso de error con qué hacer

#### Scenario: Zwift no visible
- **WHEN** se elige el estado Zwift no visible
- **THEN** el Neo y el Square dicen «Conectado», arriba se ve el aviso de error «Zwift no puede encontrar la app en esta red wifi. Apaga y vuelve a encender el wifi del móvil; si sigue igual, reinícialo.», la fila de Zwift dice «No visible» con el anillo vacío y el borde de alerta, sin la ayuda de emparejar, y el mando dice «Esperando» como en el estado Zwift esperando

#### Scenario: Sin wifi y Bluetooth apagado
- **WHEN** se eligen los estados sin wifi y Bluetooth apagado
- **THEN** Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, o se ve el aviso de Bluetooth apagado con «Activar Bluetooth» y el mando dice «Sin conexión»

#### Scenario: Estados del mando
- **WHEN** se eligen los estados mando esperando, mando desactivado y mando no disponible
- **THEN** la fila del mando dice «Esperando» con cómo elegir el móvil en la lista de mandos de Zwift y que el Square no se empareja en Zwift, «Desactivado» con que se activa en Ajustes, o «No disponible» con que este móvil no puede hacer de mando

## ADDED Requirements

### Requirement: Zwift no visible en las maquetas de rodaje
Las maquetas de rodaje y de rodaje en vertical SHALL tener el estado «Zwift no visible», con el chip «Zwift» en el anillo vacío y el borde de alerta, y el resto como en el estado sin Zwift.

#### Scenario: Rodaje con Zwift no visible
- **WHEN** se elige el estado Zwift no visible en la maqueta de rodaje, en horizontal o en vertical
- **THEN** los chips del Neo y del Square tienen el punto lleno, el chip «Zwift» tiene el anillo vacío y el borde de alerta con la descripción «Zwift, no visible» y enlaza con Dispositivos, y el resto se ve como en el estado sin Zwift, sin moverse
