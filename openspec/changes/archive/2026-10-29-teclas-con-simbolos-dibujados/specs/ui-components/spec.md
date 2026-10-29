# Spec Delta

## MODIFIED Requirements

### Requirement: Teclas del Square
Cada botón del Square SHALL dibujarse como una tecla con un estado pulsado: los de símbolo (△ ○ □ ✕) y los de la cruceta (↑ ↓ ← →) con un icono dibujado, de la misma caja, el mismo grosor y el mismo tamaño óptico, y los de letra (A B Y Z) y los nombres cortos («Palanca D1», «Freno I», «Campag. D») con texto. Los iconos SHALL tomar el color de la tecla, y su descripción accesible SHALL decir el nombre del símbolo, no el glifo. SHALL NOT depender de la fuente del sistema para ningún símbolo.

#### Scenario: Combinación
- **WHEN** se representa la combinación Campag. D + Palanca D1
- **THEN** se ven las dos teclas unidas por «+», con el modificador primero

#### Scenario: Cruceta alineada
- **WHEN** se ven juntas las cuatro teclas de la cruceta
- **THEN** las cuatro flechas están centradas en su tecla, a la misma altura y con el mismo tamaño

#### Scenario: Símbolos con la misma proporción
- **WHEN** se ven juntas las teclas △ ○ □ ✕
- **THEN** los cuatro símbolos parecen del mismo tamaño y grosor y están centrados en su tecla

#### Scenario: Tecla pulsada
- **WHEN** se mantiene pulsado un botón de símbolo o de cruceta
- **THEN** su tecla se rellena con el color de marca y el icono toma el color de texto sobre ese relleno

#### Scenario: Descripción accesible
- **WHEN** un lector de pantalla recorre la tecla del triángulo
- **THEN** dice «triángulo», y la de la flecha de la cruceta dice «flecha arriba», «flecha abajo», «flecha a la izquierda» o «flecha a la derecha»

#### Scenario: Letras y nombres
- **WHEN** se ven las teclas A, B, Y, Z, «Palanca D1» y «Campag. I»
- **THEN** siguen con su texto, sin icono
