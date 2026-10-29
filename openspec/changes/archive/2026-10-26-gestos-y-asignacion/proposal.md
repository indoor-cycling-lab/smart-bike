## Why

Desde el change 4, los botones del Square solo producen el gesto `Press` y una tabla fija decide qué hacen: las palancas, los Campagnolo con palanca y □. No hay pulsación larga ni doble, mantener una palanca no repite, no se puede cambiar qué hace cada botón y apagar el Square (los dos Campagnolo con I1 y D1) sube dos marchas. Este change es el 7 de `planificacion.md` (fase 3, F3, F4 y F12.4) y es la base de los que añaden acciones a los botones: ERG manual (9), mando de Zwift emulado (13) y teclado (14).

## What Changes

- **Gestos completos (F3.1–F3.4):** pulsación corta, larga y doble, autorrepetición al mantener y combinaciones con los Campagnolo como modificadores. Las palancas siguen respondiendo al pulsar, sin espera; solo esperan los botones con gestos ambiguos asignados (corta y larga, o doble).
- **Autorrepetición de las palancas (F3.2, anexo A.1):** mantener una palanca repite el cambio tras un umbral, como en un mando de Zwift. Activada por defecto y desactivable. **BREAKING** respecto a la spec `virtual-gears`, que decía que mantener la palanca no repite.
- **Tiempos configurables (F3.5):** umbral de pulsación larga, ventana de la doble pulsación, retardo y ritmo de la repetición, con valores por defecto y límites.
- **Vibración por gesto (F3.6):** patrón distinto para la pulsación larga, la doble y la captura, además de la corta del cambio y la doble del tope.
- **Asignación configurable (F4.1–F4.3, F4.5):** asignación por defecto del anexo A con las acciones que ya existen en la app; reasignar cualquier botón, gesto o combinación a cualquiera de esas acciones capturándolo en el Square («asignar pulsando»), con aviso si ya está en uso; invertir el sentido de las palancas; restaurar la asignación por defecto.
- **Fijar la marcha de referencia (anexo A.2):** □ larga fija la marcha puesta como referencia; □ corta sigue volviendo a ella. Es la única acción nueva: el resto de acciones del anexo A (ERG manual, modelo, Zwift, pantallas) llegan con sus changes, que añaden su sitio en la asignación por defecto.
- **Apagar el Square sin cambiar de marcha:** la combinación que lo apaga deja la marcha como estaba. Se graba antes la traza de combinaciones que quedó pendiente en el change 3.
- **Configuración guardada (F12.4):** la asignación y los tiempos se guardan en `configuracion.json` y sobreviven al cerrar la app. Es la primera parte de la configuración que se guarda.
- **Pantallas de Botones y Asignar acción:** según `maquetas/botones.html` y `maquetas/asignar.html`, con una sección de tiempos e inversión de palancas, y las funciones de Zwift como informativas («las lee Zwift» con el Square emparejado). Se llega desde Diagnóstico, de forma provisional hasta la barra inferior y Ajustes del change 10.
- **Diagnóstico:** «Último gesto» dice el gesto reconocido (pulsar, corta, larga, doble, repetir) y su acción según la asignación vigente.
- **Maqueta:** Botones, Asignar acción y Diagnóstico actualizados con sus estados, validados por el usuario antes de la app.

Fuera de alcance:

- Acciones que todavía no existen: ERG manual, ±W y SIM ⇄ ERG (change 9), cambiar de modelo de resistencia (15), funciones de Zwift por el mando emulado (13), atajos de teclado (14) y pantalla anterior/siguiente (con la barra inferior del 10).
- Capas alternativas (F4.4): descartadas por ahora (decisión 4 de `funcional.md`).
- Barra inferior, Ajustes y exportar e importar la configuración (F12.6; change 10). Mientras tanto, Botones se abre desde Diagnóstico.
- Transmisiones de marca y modos de cambio con plato (F5.7, F5.8; change 8): la asignación usa acciones de marcha lineales.
- Que los botones reasignados dejen de llegar a Zwift: con el Square emparejado en Zwift, Zwift sigue leyéndolos aunque la app les dé otra acción (llega con el mando emulado del 13).

## Capabilities

### New Capabilities

- `button-gestures`: gestos sobre los botones del Square (pulsar, corta, larga, doble, repetir, combinaciones con modificador), sus tiempos configurables, la espera solo en botones ambiguos y la vibración por gesto.
- `button-mapping`: asignación de botones y gestos a acciones: asignación por defecto, reasignar capturando en el Square, conflictos, inversión de palancas, restaurar y guardar; pantallas de Botones y Asignar acción.

### Modified Capabilities

- `virtual-gears`: mantener la palanca repite el cambio; □ larga fija la referencia; apagar el Square no cambia la marcha; los botones cambian la marcha según la asignación vigente, no según una tabla fija.
- `diagnostics`: «Último gesto» con el gesto reconocido y la acción de la asignación vigente; acceso a Botones desde Diagnóstico.
- `ride-screen`: aviso «Referencia en la N» en el pie al fijar la referencia, y un aviso por cada cambio de la repetición.
- `zwift-bridge`: en ERG de Zwift, la repetición de las palancas ajusta la potencia igual que las pulsaciones sueltas.
- `ui-components`: fila de Zwift solo informativa y acción «Sin botón» en `ButtonMappingRow`; `CapturePanel` sin el Square conectado.
- `screen-mockups`: Botones con las acciones que existen, tiempos e inversión de palancas; Asignar acción con las entradas de la acción, el gesto y los estados sin Square y botón de Zwift; Diagnóstico con el acceso a Botones.

## Impact

- **Código `:nucleo`:** `ReconocedorGestos` completo con reloj y temporizadores; `Gesto` con los tipos nuevos; `Asignacion` pasa de objeto fijo a perfil configurable con la asignación por defecto; `Accion.FijarReferencia`; `Configuracion` con asignación, tiempos e inversión, serializable con versión; `Cambio` admite cambiar la referencia.
- **Código `:app`:** `Sesion` (temporizadores del reconocedor en el hilo de control, captura de gestos, configuración viva), `Almacen` (`configuracion.json`), `Vibrador` con patrones por gesto, `ui/botones` (Botones y Asignar acción) con sus `ViewModel`, navegación desde Diagnóstico y textos. Sin dependencias nuevas.
- **Maquetas y sistema de diseño:** `maquetas/botones.html`, `maquetas/asignar.html`, `maquetas/diagnostico.html` y `maquetas/index.html`. Se reutilizan `ButtonMappingRow`, `CapturePanel`, `SquareKey`, `SettingsList`, `Stepper` y `Notice`; si falta una variante (por ejemplo, la fila de Zwift no editable), se añade antes a `design-system/`.
- **Documentos de `docs/`:** `funcional.md` (anexo A con lo que ya funciona, F3 y F4 cumplidas en lo que corresponda, valores por defecto de los tiempos); `arquitectura.md` (§ 4.3 `ReconocedorGestos` y `Asignacion` sin «provisional», § 5.3 acciones, § 6 temporizadores en el hilo de control, § 10 `configuracion.json` ya en uso); `planificacion.md` (estado del change y lo que pase a los changes 9, 10 y 13); `desarrollo.md` (cómo grabar y reproducir la traza de combinaciones, si cambia).
