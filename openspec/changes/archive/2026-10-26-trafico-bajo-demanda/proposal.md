## Why

Con el Neo, el Square o Zwift conectados, la sección Tráfico de Diagnóstico se actualiza diez veces por segundo y la pantalla va a tirones al desplazarla. El commit `7eadf56` ya aligeró el volcado: lista perezosa, filas calculadas fuera del hilo principal y hexadecimal sin `String.format`. Aun así, el tráfico en directo sigue siendo lo más costoso de la pantalla, y casi siempre se entra en Diagnóstico por las conexiones, los botones, la simulación o el control manual, no a leer tramas. El volcado solo hace falta para depurar protocolos (F13.1).

## What Changes

- La sección Tráfico de Diagnóstico empieza con un interruptor «Tráfico en directo», desactivado cada vez que se abre la pantalla. Mientras está desactivado no se muestran ni el volcado ni «Pausar» y «Limpiar».
- Al activarlo aparece el volcado con las tramas más recientes, también las llegadas mientras estaba oculto, junto con «Pausar» y «Limpiar», que se comportan como hasta ahora.
- Al desactivarlo, el volcado y sus acciones desaparecen y la pausa se anula: si se vuelve a activar, se ve en directo.
- Ocultar el tráfico no detiene el registro de sesión, que sigue grabando cada trama (F13.4, `docs/arquitectura.md` § 11).
- La elección se conserva al girar el móvil. No se guarda entre visitas: al volver a Diagnóstico, el tráfico vuelve a estar oculto.
- En la maqueta de Diagnóstico, la casilla funciona en todos los estados y al abrir aparece desmarcada. Las filas de cada estado se ven al marcarla.

Fuera de alcance:
- Arrancar en pausa con una foto fija de las tramas, la alternativa descartada por el usuario.
- Cambiar el formato del volcado (`HexLog`), sus columnas, el resaltado de bytes o el número de filas de la instantánea.
- Recordar la elección entre visitas o llevarla a Ajustes.
- Guardar o compartir el registro (F13.4, change 16 `registro-sesion`).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `diagnostics`: nuevo requisito «Tráfico en directo bajo demanda». «Tráfico en crudo» y «Pausar y limpiar el tráfico» pasan a aplicarse con el tráfico en directo activado.
- `screen-mockups`: «Maqueta de Diagnóstico con sesión de rodaje» incluye el interruptor, desactivado al abrir. En «Estados de la maqueta de Diagnóstico», las filas de Zwift de la ruta en lineal se ven al activarlo.

## Impact

- Maquetas: `maquetas/diagnostico.html` (sección Tráfico en todos los estados) y una regla en `maquetas/revision.css` que oculta el volcado y sus acciones mientras la casilla está desmarcada. El sistema de diseño no cambia: se reutiliza la fila con casilla de `SettingsList` (`label.sq-item` con `sq-check`).
- App: `ui/diagnostico/DiagnosticoViewModel.kt` (estado del interruptor; sin el interruptor no se muestrea el registro ni se forman filas), `DiagnosticoUiState.kt`, `Diagnostico.kt` (sección Tráfico con `SettingsItemCasilla`), `res/values/strings.xml` y los tests de `DiagnosticoUiStateTest` y `DiagnosticoTest`.
- Funcionalidades: F13.1 (ver en crudo lo que envían el Square y el Neo).
- Prueba con el equipo real: en el OnePlus 8, con el Neo y el Square conectados y pedaleando, desplazar Diagnóstico con el tráfico oculto y con el tráfico en directo, y comprobar que al activarlo aparecen las tramas recientes.
- Documentos de `docs/`: `planificacion.md` (fila nueva del change, 3b). `funcional.md` (nota en F13.1), `arquitectura.md` (§ 11, la UI solo muestrea el registro con el tráfico visible) y `desarrollo.md` se revisan al final.
