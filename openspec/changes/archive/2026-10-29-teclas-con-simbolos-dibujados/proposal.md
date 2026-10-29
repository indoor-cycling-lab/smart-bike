# Proposal

## Why

Las teclas del Square (`sq-key` en la maqueta, `SquareKey` en la app) pintan los botones de símbolo (△ ○ □ ✕) y la cruceta (↑ ↓ ← →) como texto en la fuente `numeric`, que no incluye esos glifos. El sistema usa una fuente de reserva distinta para cada uno, con su tamaño y su línea base. Se ve en las capturas a doble resolución de la maqueta de Diagnóstico (28-10-2026): ← → quedan más bajas que ↑ ↓ dentro de la tecla, y △ ○ □ se ven más pequeños que ✕. En el móvil el resultado depende además de la fuente del sistema. No se puede arreglar con CSS ni con Compose, porque cada glifo viene de una fuente distinta. El usuario ha pedido revisarlo en la maqueta y en la app.

## What Changes

- **Ocho símbolos dibujados** (△ ○ □ ✕ y las cuatro flechas) como iconos vectoriales en `design-system/icons.svg`: misma caja de 24 px, mismo grosor y mismo tamaño óptico, de un solo trazado relleno cada uno, como `i-manillar` e `i-botones`. Sus formas parten de las de `i-botones`, para que el Square se lea igual en las teclas y en la entrada «Botones» de Ajustes.
- **La tecla usa el icono** en los botones de símbolo y de cruceta, en `sq-key` (CSS, guía y preview), `CapturePanel` y `ButtonMappingRow`. Las letras A B Y Z y los nombres de palancas, frenos y Campagnolo se quedan como texto.
- **Maquetas**: Diagnóstico, Botones y Asignar acción pasan a las teclas dibujadas, con todos sus estados, y una sección de la guía muestra los ocho símbolos junto a las letras, en reposo, pulsados y en los dos temas.
- **App**: `SquareKey` dibuja el icono en lugar del glifo y los iconos se generan con `scripts/design-system/generar.py`, como el resto. La descripción accesible dice el nombre del símbolo («triángulo», «flecha arriba»), no el glifo.
- **Etapas.** Cambio con impacto visual: etapa 1, sistema de diseño y maquetas con tu validación; etapa 2, la app.

Fuera de alcance:

- Cambiar el diseño de las teclas anchas, las letras, el tamaño de las teclas o el modelo de botones.
- Los glifos que van dentro de frases (p. ej. «Campag. I + cruceta ←» en una ayuda, o «△ larga» en los textos de las acciones): siguen siendo texto, sin cambiar de fuente. Si desentonan, es otro change.
- Los textos de `strings.xml` y de `TextosAccion.kt` que nombran los botones.
- El change 28 (`simplificar-diagnostico-conexiones`): no depende de este. Los dos tocan `maquetas/diagnostico.html`, pero en secciones distintas.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities
- `ui-components`: las teclas del Square dibujan los símbolos y la cruceta como iconos, con su descripción accesible y el mismo tamaño óptico, y dejan el texto para letras y nombres.
- `screen-mockups`: las maquetas de Diagnóstico, Botones y Asignar acción muestran las teclas dibujadas, y la guía recoge los ocho símbolos.

## Impact

- **Sistema de diseño.** `design-system/icons.svg` (8 iconos), `components.css` (`.sq-key` con icono, en la captura también), `components/SquareKey`, `CapturePanel` y `ButtonMappingRow` (README y preview) y la guía regenerada; `design-system/README.md` si cita «los glifos son texto».
- **Maquetas.** `diagnostico.html`, `botones.html`, `asignar.html` e `index.html`; capturas de la web (`site/capturas.json`).
- **App.** `ui/componentes/SquareKey.kt`, `ButtonMappingRow.kt`, `ui/diagnostico/Diagnostico.kt` y los textos de accesibilidad; los `ic_*.xml` se generan. Tests: `IconosSincronizadosTest` y los de las teclas.
- **Documentación.** `docs/funcional.md` y `docs/arquitectura.md` solo si algo cambia; `docs/planificacion.md` (fila 29); `docs/desarrollo.md`, ninguno previsto.
