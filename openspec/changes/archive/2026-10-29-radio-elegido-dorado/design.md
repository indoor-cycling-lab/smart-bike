# Design

## Context

El radio de `sq-opt` se dibuja a mano en los dos lados: CSS en `design-system/components.css` y Compose en `SettingsOpcion` (`SettingsList.kt`), con los mismos colores (aro `ink` y punto `gear-fill` al elegir). La fila elegida lleva además un anillo interior `gear` de 2 dp. Ver `proposal.md` para el motivo.

## Goals / Non-Goals

**Goals:**
- Un solo acento en la opción elegida: aro, punto y anillo de la fila en `gear`.
- Misma forma y tamaño que ahora (aro de 2 dp, 20 dp de diámetro, punto de 4 dp de radio).

**Non-Goals:**
- Tocar tokens, el anillo de la fila o el resto de controles.

## Decisions

- **Aro y punto en `gear`, no en `gear-fill`.** En tema claro `gear` (`#8f5400`) pasa el contraste de texto sobre `surface` (4,5:1, ya comprobado en `contraste.py`), mientras que `gear-fill` (`#ffb21f`) es un relleno pensado para llevar texto encima y sobre la fila clara queda muy tenue. En tema oscuro los dos valen `#ffb21f`. Así el radio coincide con el anillo de la fila en los dos temas.
- **Sin token ni par de contraste nuevos:** `gear` sobre `surface` ya está en `contraste.py`.
- **Alternativas descartadas** (propuestas al usuario el 29-10-2026): aro neutro `line-strong` con punto dorado (queda descoordinado junto al anillo dorado) y check dorado en círculo sin radio (se aparta del patrón de radio de `SettingsList`).
- **Pruebas:** la selección se comprueba por el estado accesible (`selected`), que no cambia; el color se valida en la maqueta y en el móvil.

## Risks / Trade-offs

- En tema claro, aro y punto pasan de casi negro a marrón dorado: se ve más tenue que el blanco del tema oscuro, pero es el mismo tono del anillo de la fila. Se valida en el OnePlus 8 en los dos temas.
- `SettingsOpcion` se usa en varias pantallas: el cambio es visual y global, sin riesgo funcional. Las capturas de la web con una opción elegida se regeneran.
