# Spec Delta

## ADDED Requirements

### Requirement: Copia de seguridad del sistema
La copia de seguridad de Android (la de Google y la transferencia a un móvil nuevo) SHALL incluir solo la configuración, lo mismo que «Exportar configuración»: SHALL NOT incluir las sesiones grabadas, los dispositivos configurados, la identidad ante la plataforma, las últimas marchas ni el nombre Bluetooth original. Al restaurarla, la app SHALL abrir como tras importar un archivo.

#### Scenario: Cambiar de móvil
- **WHEN** en el móvil anterior se eligió una transmisión SRAM, se asignaron los botones y se configuraron el Neo y el Square, y se pasa la app a un móvil nuevo con la transferencia de Android
- **THEN** en el móvil nuevo la transmisión, los botones, los datos en pantalla y el ciclista son los mismos, Dispositivos no tiene ninguno configurado y «Buscar» encuentra el Neo y el Square

#### Scenario: Reinstalar
- **WHEN** se desinstala la app y se vuelve a instalar en el mismo móvil con la copia de Google activada
- **THEN** la configuración vuelve, los dispositivos hay que buscarlos de nuevo y «Sesiones grabadas» está vacía

#### Scenario: Sesiones grabadas fuera de la copia
- **WHEN** hay sesiones grabadas que ocupan más de 25 MB y Android hace la copia de seguridad
- **THEN** la copia se completa con la configuración, sin las sesiones

#### Scenario: Nombre Bluetooth al restaurar
- **WHEN** la app tenía renombrado el Bluetooth del móvil anterior y se restaura la copia en un móvil nuevo
- **THEN** al abrir la app el móvil nuevo conserva su nombre Bluetooth y, si la app lo renombra al publicar por Bluetooth, al terminar recupera el del móvil nuevo, nunca el del anterior

#### Scenario: Sin copia de seguridad
- **WHEN** la copia de Google está desactivada en el móvil o no hay copia que restaurar
- **THEN** la app abre con los valores por defecto, como en la primera instalación
