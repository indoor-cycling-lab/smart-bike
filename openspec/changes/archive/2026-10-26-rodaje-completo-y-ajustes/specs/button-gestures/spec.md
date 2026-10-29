# Spec Delta

## ADDED Requirements

### Requirement: Vibración desactivable
Con «Vibrar» desactivado en Ajustes, el móvil SHALL NOT vibrar por ningún motivo: ni por el gesto, ni al cambiar de marcha, ni en los topes de marcha o de potencia. El resto (la acción, los topes en pantalla, el envío al Neo) SHALL seguir igual. Activado (por defecto), las vibraciones SHALL ser las de «Vibración por gesto». El cambio SHALL aplicarse al momento, también con la pantalla apagada.

#### Scenario: Desactivada
- **WHEN** «Vibrar» está desactivado y se mantiene □ hasta el umbral de pulsación larga
- **THEN** la referencia se fija y el móvil no vibra

#### Scenario: Tope sin vibración
- **WHEN** «Vibrar» está desactivado, la marcha es la 24 y se pulsa la palanca D1
- **THEN** la pantalla muestra «Tope · marcha más dura» y el móvil no vibra

#### Scenario: Tope de potencia sin vibración
- **WHEN** «Vibrar» está desactivado, se está en ERG manual a 50 W y se pulsa la palanca I1
- **THEN** la potencia sigue en 50 W, el pie dice «Tope · 50 W» y el móvil no vibra

#### Scenario: Volver a activarla
- **WHEN** se vuelve a activar «Vibrar» y se pulsa la palanca D1
- **THEN** la marcha sube y el móvil vibra una vez
