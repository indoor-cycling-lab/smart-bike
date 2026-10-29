# Spec Delta

## MODIFIED Requirements

### Requirement: Vibración en cada cambio
Con «Vibrar» activado (por defecto), la app SHALL acompañar cada cambio de marcha de una vibración corta, distinta de la doble del tope, también con la pantalla apagada. Con «Vibrar» desactivado, SHALL NOT vibrar (spec `button-gestures`, «Vibración desactivable»).

#### Scenario: Cambio con la pantalla apagada
- **WHEN** la pantalla del móvil está apagada y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil vibra una vez

#### Scenario: Vibración desactivada
- **WHEN** «Vibrar» está desactivado y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil no vibra
