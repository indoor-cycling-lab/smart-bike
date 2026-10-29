# Proposal

## Why

Hoy un solo Atrás en Rodaje termina la sesión: el Neo vuelve a pendiente 0 %, se pierden las conexiones con el Neo, el Square y Zwift y la app se cierra. Con el móvil en el soporte, un roce con el gesto o el botón Atrás en pleno rodaje corta todo sin aviso. La salida debe pedir confirmación (F11, F12).

## What Changes

- En Rodaje, el primer Atrás ya no termina la sesión: muestra durante 3 s un aviso flotante de varias líneas, «Para cerrar la aplicación, pulsa Atrás otra vez».
- Un segundo Atrás dentro de esos 3 s termina la sesión como hasta ahora y el aviso pasa a «Cerrando…» hasta que la app se cierra. Pasados los 3 s, el aviso desaparece y el siguiente Atrás vuelve a pedir confirmación.
- Componente nuevo del sistema de diseño, `ExitNotice` (`sq-exit-notice`): aviso flotante sobre Rodaje, con texto en varias líneas, que no mueve ni redimensiona nada de la pantalla.
- Estados nuevos en las maquetas de Rodaje (horizontal y vertical): «Pulsa Atrás otra vez» y «Cerrando…».
- «Terminar» en la notificación no cambia: cierra sin confirmación.
- Los dos textos nuevos nacen en español e inglés, en coordinación con `idioma-es-en` (change 34).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `app-navigation`: «Atrás del sistema» — en Rodaje, Atrás pide confirmación y solo el segundo Atrás termina la sesión.
- `ride-session`: «Terminar la sesión» — con Atrás desde Rodaje hacen falta dos pulsaciones seguidas; la notificación sigue cerrando sin confirmar.
- `ride-screen`: aviso de salida («Pulsa Atrás otra vez», «Cerrando…») sin mover la pantalla, accesible y compatible con los demás avisos.
- `ui-components`: componente `ExitNotice`.
- `screen-mockups`: estados «salida-confirmar» y «salida-cerrando» en las maquetas de Rodaje.

## Impact

- **Maqueta y sistema de diseño:** `design-system/components.css`, `design-system/components/ExitNotice/` (`README.md` y `preview.html`), guía regenerada, `maquetas/rodaje.html`, `maquetas/rodaje-vertical.html` y sus estados en `data-mock-states`; capturas de `site/` regeneradas si Rodaje cambia.
- **App:** `RodajeRuta.kt` (`BackHandler`), `RodajeViewModel.kt` (estado de la salida con ventana de 3 s), `RodajeUiState`, `Rodaje.kt`, nuevo `ExitNotice.kt` en `ui/componentes/` y textos en `strings.xml` (es y en). Sin dependencias nuevas.
- **Documentación:** cambian `docs/funcional.md` (F11, comportamiento de Atrás) y `docs/planificacion.md` (fila 35 y su estado); `docs/arquitectura.md` y `docs/desarrollo.md` se revisan y, si no cambian, se deja constancia.
- **Prueba con el equipo real:** en el OnePlus 8, con el Neo y el Square conectados, comprobar el doble Atrás (también con el gesto de navegación) y cuánto dura de verdad el «Cerrando…».
- **Orden:** va después de `idioma-es-en` (34) o coordinado con él, porque ambos tocan `strings.xml`.

## Fuera de alcance

- Confirmar el cierre desde la notificación («Terminar») o desde otras pantallas: Atrás en una pestaña o en una secundaria sigue como está.
- Plazo configurable, vibración o sonido al pedir la confirmación.
- Diálogo modal de confirmación.
- Cambiar qué hace terminar la sesión (guardar la marcha, pendiente 0 %, desconexiones).
