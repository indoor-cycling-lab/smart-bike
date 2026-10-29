# Spec Delta

## ADDED Requirements

### Requirement: Estados del aviso de salida en las maquetas de rodaje
Las maquetas `rodaje.html` y `rodaje-vertical.html` SHALL incluir los estados «salida-confirmar» («Para cerrar la aplicación, pulsa Atrás otra vez») y «salida-cerrando» («Cerrando…»), accesibles desde el selector de estado de `revision.js` y por enlace con `#estado=`, con el aviso flotante sobre la pantalla en los dos temas.

#### Scenario: Estado de confirmación
- **WHEN** se abre `maquetas/rodaje.html#estado=salida-confirmar`
- **THEN** se ve Rodaje con el aviso «Para cerrar la aplicación, pulsa Atrás otra vez» flotando, sin que nada se mueva

#### Scenario: Estado de cierre en vertical
- **WHEN** se abre `maquetas/rodaje-vertical.html#estado=salida-cerrando`
- **THEN** se ve Rodaje vertical con el aviso «Cerrando…»

#### Scenario: Tema claro
- **WHEN** se abre cualquiera de los dos estados con `tema=claro`
- **THEN** el aviso se lee con el contraste exigido y se distingue del fondo por algo más que el color
