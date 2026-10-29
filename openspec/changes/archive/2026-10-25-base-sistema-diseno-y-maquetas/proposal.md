# Proposal

## Why

El sistema de diseño (`design-system/`) y las maquetas (`maquetas/`) ya están validados, pero OpenSpec no tiene ninguna spec que los describa. Sin specs de base, los cambios de interfaz que vengan no tienen contra qué escribir sus deltas, y el flujo «maqueta primero» (AGENTS.md) no puede comprobar que una maqueta cumple lo acordado. Conviene fijarlo ahora, antes de empezar la app, para que cada change de UI parta de un contrato explícito.

## What Changes

- Se crean cuatro specs de base que describen lo que ya existe y se ha validado: tokens, componentes, guía de estilo y maquetas de pantallas, con sus estados.
- Se ajustan el sistema de diseño y las maquetas para que cumplan esas specs donde hoy no lo hacen:
  - Las maquetas tienen estilos en línea (`style="…"`) que pasan a clases o variantes documentadas del sistema de diseño.
  - Las fuentes (Barlow, Barlow Condensed, JetBrains Mono) y los iconos se sirven desde `design-system/` en lugar de Google Fonts, para que la guía y las maquetas funcionen sin conexión, como la app (NF5).
  - Se añade una comprobación automática de contraste de los tokens en los dos temas.
- No se escribe código de la app Android.

## Capabilities

### New Capabilities
- `design-tokens`: valores con nombre (color por función, tipografía, espaciado, radios, tamaños táctiles) en los temas rodaje y claro, y sus reglas de contraste y de un color por significado.
- `ui-components`: componentes de la interfaz (marcha, plato, cassette, rejilla de datos, conexión, modo, aviso de acción, teclas del Square, asignación, ajustes, navegación, diagnóstico), con su anatomía, variantes y estados.
- `style-guide`: guía navegable generada a partir de los tokens y de los componentes, que los muestra en los dos temas y explica cómo usarlos en una maqueta.
- `screen-mockups`: maquetas estáticas de cada pantalla con su selector de estado y de tema, el índice de revisión y los estados que cada pantalla debe representar.

### Modified Capabilities
- Ninguna: no hay specs previas.

## Impact

- `design-system/`: `components.css` (clases nuevas que sustituyen estilos en línea; fuentes locales), `fonts/` nueva con las fuentes OFL y la fuente de iconos, componentes que cambien de pautas.
- `maquetas/`: retirada de estilos en línea, enlaces a la guía, sin dependencias externas.
- `scripts/design-system/`: comprobación de contraste junto al generador.
- `openspec/specs/`: cuatro specs nuevas al archivar.
- Sin dependencias nuevas: Python 3 de la biblioteca estándar para los scripts.

## Fuera de alcance

- Cualquier código de la app (tema de Compose, composables, BLE).
- Cambios de diseño o de comportamiento respecto a lo validado: las specs describen lo que hay, y los ajustes son de forma (dónde vive el estilo, de dónde salen las fuentes), no de aspecto.
- Specs de las pantallas reales de la app: se crearán con los changes que las implementen y deberán mantenerse coherentes con `screen-mockups`.
- Los artefactos de claude.ai: quedan como copia antigua y no se sincronizan.
