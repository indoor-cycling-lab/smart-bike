# Spec Delta

## ADDED Requirements

### Requirement: Aviso de salida
El sistema de diseño SHALL incluir el componente `ExitNotice` (`sq-exit-notice`): un aviso flotante que se superpone a la pantalla sin ocupar sitio en su disposición, translúcido y discreto, con texto de una o varias líneas, fondo, borde y radio de los tokens existentes, y que se ve en los dos temas con el contraste de texto exigido. SHALL tener su carpeta con `README.md` y `preview.html`, y verse en la guía de estilo.

#### Scenario: Alta del componente
- **WHEN** se regenera la guía con `python3 scripts/design-system/generar.py`
- **THEN** la guía muestra `ExitNotice` en los dos temas, centrado sobre una pantalla de Rodaje

#### Scenario: Contraste
- **WHEN** se genera la guía
- **THEN** el texto del aviso cumple 4,5:1 sobre su fondo en los dos temas y el generador no falla

#### Scenario: Sin sitio propio
- **WHEN** el aviso se coloca sobre una pantalla
- **THEN** no cambia el tamaño ni la posición de ningún otro componente de esa pantalla
