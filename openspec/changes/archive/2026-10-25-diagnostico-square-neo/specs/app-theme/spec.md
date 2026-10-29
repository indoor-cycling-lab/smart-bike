# Spec Delta

## ADDED Requirements

### Requirement: Iconos de la app iguales a los del sistema de diseño
La app SHALL mostrar los iconos del sistema de diseño con la misma forma que en la guía, empaquetados en la app; los iconos de la app SHALL regenerarse con el mismo comando que los demás derivados y la comprobación de la app SHALL fallar si no coinciden con la fuente de iconos.

#### Scenario: Icono en la app
- **WHEN** Diagnóstico muestra el icono de error en un aviso
- **THEN** tiene la misma forma que el icono `error` de la guía y toma el color del aviso en los dos temas

#### Scenario: Icono cambiado sin regenerar
- **WHEN** se cambia o se añade un icono en la fuente de iconos y se ejecutan los tests de la app sin regenerar
- **THEN** los tests fallan, nombran el icono e indican el comando de regeneración
