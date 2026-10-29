## ADDED Requirements

### Requirement: Idioma en Ajustes
La sección «Pantalla» de Ajustes SHALL tener, encima de «Tema», el ajuste «Idioma» con dos opciones excluyentes, «Español» y «English», cada una escrita en su propio idioma sea cual sea el idioma de la app. El título del ajuste SHALL leerse «Idioma · Language» en los dos idiomas, para poder encontrarlo aunque la app esté en un idioma que no se lee. Elegir una opción SHALL aplicarse al momento (spec `app-language`).

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez con el móvil en español
- **THEN** la sección «Pantalla» muestra «Idioma · Language», encima de «Tema», con «Español» elegido

#### Scenario: Opciones en su propio idioma
- **WHEN** la app está en inglés y se mira «Idioma · Language»
- **THEN** las opciones dicen «Español» y «English», no «Spanish» y «English»

#### Scenario: Elegir otra opción
- **WHEN** se toca «English»
- **THEN** queda elegida «English» y «Español» deja de estarlo, y Ajustes pasa a inglés

#### Scenario: Opción elegida sin color
- **WHEN** se mira «Idioma · Language»
- **THEN** la opción elegida se distingue por la marca de selección, no solo por el color, y el lector de pantalla anuncia cuál está elegida

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción de idioma
- **THEN** se elige esa opción

## MODIFIED Requirements

### Requirement: Secciones de Ajustes
Tras las entradas y «Al rodar», Ajustes SHALL mostrar en este orden: la sección «Pantalla», con «Idioma», «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas»; y la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración». Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Conexión.

#### Scenario: Orden de las secciones
- **WHEN** se abre Ajustes y se desplaza hasta el final
- **THEN** las secciones aparecen en el orden indicado y la última es «Copia de seguridad»

#### Scenario: Orden de la sección Pantalla
- **WHEN** se mira la sección «Pantalla»
- **THEN** los ajustes aparecen en este orden: «Idioma · Language», «Tema» y «Orientación»
