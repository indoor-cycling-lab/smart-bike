# Spec Delta

## ADDED Requirements

### Requirement: Ventilador en Ajustes
Ajustes SHALL añadir, después de «Zwift», una sección «Ventilador» solo con un ventilador recordado, con: el modo («Manual» y «Automático», y «Otro modo» si el ventilador está en otro); la velocidad manual, con un paso a paso − y + de 10 % entre 0 y 100 %; y el umbral de cada zona de pulso (1 a 4), con un paso a paso − y + de 1 bpm. En cada extremo, el botón de ese lado SHALL quedar deshabilitado. El modo elegido SHALL distinguirse por algo más que el color.

#### Scenario: Sin ventilador recordado
- **WHEN** no hay ventilador recordado y se abre Ajustes
- **THEN** no aparece la sección «Ventilador»

#### Scenario: Con ventilador
- **WHEN** hay un ventilador conectado en manual al 40 % con zonas hasta 120, 135, 150 y 165 bpm
- **THEN** la sección dice «Manual», «40 %» y los cuatro umbrales, y todo se puede cambiar

#### Scenario: Cambiar el modo
- **WHEN** se toca «Automático» con el ventilador en manual
- **THEN** el ventilador pasa a automático y la velocidad manual queda deshabilitada

#### Scenario: Velocidad en automático
- **WHEN** el ventilador está en automático
- **THEN** el paso a paso de la velocidad está deshabilitado y la sección explica que en automático la velocidad la decide el ventilador

#### Scenario: Cambiar un umbral en automático
- **WHEN** el ventilador está en automático y se sube el umbral de la zona 1 de 120 a 121 bpm
- **THEN** el ventilador usa 121 en esa zona sin salir del automático

#### Scenario: Extremo de la velocidad
- **WHEN** la velocidad manual es 100 %
- **THEN** el + está deshabilitado y el − no

#### Scenario: Límite entre zonas
- **WHEN** el umbral de la zona 2 es 135 y el de la zona 3 es 136
- **THEN** el + del umbral 2 está deshabilitado, y el − del umbral 3 también

#### Scenario: Ventilador sin conexión
- **WHEN** hay un ventilador recordado pero no conectado
- **THEN** la sección se ve con lo último que comunicó, todos sus controles deshabilitados y un texto que dice «Ventilador sin conexión», sin guardar nada

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** la sección se recorre desplazando la pantalla, sin recortes, y la barra inferior sigue visible al pie
