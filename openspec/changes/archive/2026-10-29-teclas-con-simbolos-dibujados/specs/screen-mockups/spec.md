# Spec Delta

## ADDED Requirements

### Requirement: Teclas dibujadas en las maquetas
Las maquetas de Diagnóstico, Botones y Asignar acción SHALL mostrar los botones de símbolo y de cruceta del Square con las teclas dibujadas, en todos sus estados y en los dos temas, y la guía del sistema de diseño SHALL mostrar los ocho símbolos junto a las letras y los nombres, en reposo y pulsados.

#### Scenario: Diagnóstico con la cruceta y los símbolos
- **WHEN** se abre `maquetas/diagnostico.html#estado=rodando`
- **THEN** la sección «Botones del Square» muestra ↑ ↓ ← → alineadas y △ ○ □ ✕ con la misma proporción, y la tecla de Palanca D1 pulsada

#### Scenario: Botones y Asignar acción
- **WHEN** se abren las maquetas de Botones y de Asignar acción con una combinación que lleva un símbolo (Campag. D + △)
- **THEN** la tecla del triángulo es el icono dibujado, junto a «Campag. D» como texto

#### Scenario: Guía de las teclas
- **WHEN** se abre la sección SquareKey de la guía del sistema de diseño
- **THEN** se ven los ocho símbolos dibujados, las cuatro letras y las teclas anchas, en reposo y pulsadas, en los dos temas

#### Scenario: Panel de captura
- **WHEN** se abre `maquetas/asignar.html#estado=detectado`
- **THEN** las teclas capturadas de 48 dp muestran el icono del símbolo al tamaño de la captura
