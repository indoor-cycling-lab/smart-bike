# Spec Delta

## ADDED Requirements

### Requirement: Acciones del ventilador
La app SHALL permitir asignar, en todas las transmisiones, tres acciones del ventilador: «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad». Aparecerán en «Acciones de la app» después de «Siguiente modelo», sin botón por defecto, y asignarlas no exige un ventilador recordado. Su efecto es el de la spec `fan-control`.

#### Scenario: Acciones en la lista
- **WHEN** se abre Botones con cualquier transmisión
- **THEN** después de «Siguiente modelo» aparecen «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad», las tres con «Sin botón»

#### Scenario: Asignar la velocidad
- **WHEN** se asigna □ doble a «Ventilador: + velocidad» y se pulsa rodando con el ventilador en manual al 40 %
- **THEN** el ventilador pasa al 50 % y el aviso de la acción dice «Ventilador: 50 %»

#### Scenario: Asignar sin ventilador
- **WHEN** no hay ventilador recordado y se asigna △ corta a «Ventilador: manual ⇄ automático»
- **THEN** se guarda la asignación y, al pulsar △, el aviso dice «Sin ventilador»

#### Scenario: Restaurar la asignación
- **WHEN** hay botones asignados a las acciones del ventilador y se restaura la asignación por defecto
- **THEN** las tres acciones vuelven a «Sin botón»

#### Scenario: Entrada ya en uso
- **WHEN** se intenta asignar a una acción del ventilador una entrada que ya tiene otra acción
- **THEN** se muestra el aviso de entrada en uso de siempre y no se asigna sin confirmar

#### Scenario: Una asignación por transmisión
- **WHEN** se asigna una acción del ventilador con la lineal y se cambia a la Shimano
- **THEN** la Shimano conserva su propia asignación, sin botón para las acciones del ventilador
