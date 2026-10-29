# Proposal

## Why

En las listas de opciones excluyentes (Conexión, Ajustes), la opción elegida suma tres marcas: el anillo dorado de la fila, el aro blanco del radio y el punto dorado. El aro blanco es el único elemento blanco puro de la tarjeta y compite con el anillo dorado, así que la selección se ve sobrecargada (observación del usuario, 29-10-2026, al ver Conexión en el OnePlus 8). El anillo dorado ya distingue la fila; el radio solo tiene que confirmarla, con el mismo acento.

## What Changes

- El radio de la opción elegida (`sq-opt`) pasa a dibujar el aro y el punto en el acento `gear`, en lugar de aro `ink` y punto `gear-fill`. Los demás radios siguen igual (aro `line-strong`, sin punto).
- La selección sigue sin depender solo del color: se conserva la forma (anillo de la fila y punto central del radio).
- La opción deshabilitada elegida conserva el anillo y el punto, atenuados como hasta ahora.
- El mismo cambio en la app: `SettingsOpcion` de `SettingsList.kt` (aro `c.gear` y punto `c.gear`).
- Sin tokens nuevos y sin cambios de comportamiento ni de textos.

Ya hecho en la etapa de maqueta, visto por el usuario en los dos temas (le gusta): `design-system/components.css` (`.sq-opt input:checked`) y la guía regenerada.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ui-components`: el requisito «Lista de modos de conexión» concreta la marca de selección del modo elegido (aro y punto del mismo acento que el anillo de la fila). Es el comportamiento visible de cualquier opción excluyente con `sq-opt`.

## Impact

- Código: `app/src/main/kotlin/app/smartbike/ui/componentes/SettingsList.kt` (`SettingsOpcion`).
- Sistema de diseño: `design-system/components.css`, `components/SettingsList/README.md` y `components/ModeList/README.md` (descripción del radio) y `styleguide/index.html` (generado).
- Pantallas afectadas: Conexión (modos), Ajustes (tema, idioma, orientación, transmisión en Marchas) y cualquier otra lista con `sq-opt`. Las capturas de la web (`site/img/`) que muestren una opción elegida se regeneran.
- Documentos de `docs/`: `planificacion.md` (fila y nota del change); `funcional.md`, `arquitectura.md` y `desarrollo.md`, ninguno salvo que la revisión final encuentre una mención al aro `ink`.
- Funcionalidades: F12 (ajustes) y F9 (modos de conexión); sin cambio de funcionalidad.

**Fuera de alcance**: el anillo de la fila, las casillas (`SettingsItemCasilla`) y el control segmentado, los tokens y colores, la disposición de las pantallas y cualquier cambio de comportamiento.
