## Why

Hoy la app solo tiene la transmisión lineal de 24 marchas numeradas. El usuario quiere cambiar como en su bici de carretera (Shimano 105 R7120 mecánico, 52/36 · 11–36) y probar cómo sería ese grupo en electrónico, con el plato que cambia solo. Es el change 8 de `planificacion.md` (fase 3, F5.7, F5.8 y F12.3), **reducido a la transmisión del usuario**: en la exploración se decidió dejar SRAM, Campagnolo y «A medida» para un change posterior, a cambio de los dos modos de cambio de la Shimano. También se cerraron las decisiones abiertas 3 (cuánta pendiente da cada marcha) y 5 (desarrollo de fábrica) de `funcional.md`.

## What Changes

- **Transmisión Shimano 105 2×12 (F5.7):** platos 52/36 y cassette 11-12-13-14-15-17-19-21-24-28-32-36, como segunda transmisión. La lineal de 24 marchas sigue siendo la de por defecto (decisión 5 tomada). La marcha se ve y se cambia como plato × piñón (`36×17`), con el indicador de plato y el cassette.
- **Modo automático, por defecto en la Shimano (F5.8, «sincronizado»):** imita el Synchro Shift del Di2. La derecha sube y baja por una secuencia que cambia de plato sola, con histéresis: sube al 52 desde el 36×15 (al 52×19) y baja al 36 desde el 52×24 (al 36×19), compensando 2 piñones, sin cruces de cadena ni marchas repetidas. Desde la izquierda se cambia el plato a mano, y el piñón compensa 2 posiciones (como el Semi Synchro). Los puntos de cambio y la compensación están marcados **a validar** con el usuario al rodar.
- **Modo manual (F5.8, «como el grupo»):** la derecha cambia el piñón y la izquierda el plato, sin compensación, como los mandos de Shimano. El ±3 son 3 piñones en el plato puesto.
- **Pendiente proporcional a la relación (decisión 3, F6.1):** con la Shimano, el modelo simple suma a la pendiente del terreno un ajuste proporcional al logaritmo de la relación, normalizado para que el recorrido sea el de la lineal de 24 (de −11,5 % a +11,5 % con 1 % por marcha) y para que el 0 % caiga en el centro geométrico del desarrollo. Dos marchas con la misma relación dan la misma pendiente.
- **Una asignación de botones por transmisión (F4, F12.4):** la lineal conserva la suya. La Shimano tiene una asignación por defecto como los mandos Shimano: D1 piñón más grande (más ligera), D2 piñón más pequeño (más dura), I1 plato grande e I2 plato pequeño, con los saltos y extremos en los Campagnolo. La misma asignación sirve en manual y en automático. Se añaden las acciones «Plato más grande» y «Plato más pequeño». Botones edita y restaura la asignación de la transmisión elegida. **BREAKING** respecto a `button-mapping`: la asignación guardada deja de ser única.
- **Marchas (F12.3):** pantalla nueva, a la que se llega desde Ajustes, para elegir transmisión y modo de cambio, ver los platos y el cassette, mover la marcha de referencia y activar «Recordar la última marcha». La marcha inicial pasa a ser la de referencia, como en `maquetas/marchas.html` («Al empezar»).
- **Cambiar de transmisión en marcha:** se aplica al momento, sin terminar la sesión, y la marcha pasa a la última usada con esa transmisión (o a su referencia). Cada transmisión recuerda su última marcha y su referencia.
- **Rodaje:**
  - Con la Shimano, la marcha en grande es plato × piñón, con el indicador de plato y el cassette en lugar de la escalera.
  - La barra superior dice «Shimano 105 · automático» o «Shimano 105 · manual».
  - Los avisos dicen «Plato 52» al cambiar de plato y «Referencia en 36×17» al fijar la referencia.
  - Los topes nuevos: «Tope · plato 52», «Tope · piñón 11»…
- **Diagnóstico:** la marcha como plato × piñón con su relación, y el ajuste que da con el modelo simple.
- **ERG de Zwift con la Shimano:** las acciones de marcha ajustan la potencia en el mismo sentido que endurecerían (D2 +10 W, D1 −10 W), y las de plato no hacen nada.
- **Configuración guardada:** `configuracion.json` pasa a la versión 2, con un apartado por transmisión, y `estado.json` guarda la última marcha de cada una. Los documentos de la versión 1 se migran sin perder la asignación, la referencia ni la última marcha de la lineal.
- **Maqueta primero:**
  - `marchas.html` con la lineal y la Shimano, en automático y en manual.
  - Estados Shimano de `rodaje.html` y `rodaje-vertical.html` con el desarrollo real.
  - `botones.html` con la asignación de la Shimano.
  - `ajustes.html` con la entrada «Marchas».
  - `diagnostico.html` con la marcha plato × piñón.
  - El usuario valida las maquetas antes de la app.

Fuera de alcance:

- Desarrollos de SRAM y Campagnolo, el modo de SRAM «las dos palancas a la vez» y la transmisión «A medida» con su pantalla de edición (pasan a un change nuevo en `planificacion.md`).
- Número de marchas de la lineal (F5.5), dificultad (F6.4), elegir el modelo de resistencia y «Pendiente por marcha» en Marchas (F6.1 configurable, F12.2): la maqueta los muestra, pero la app no los incluye en este change (changes 10 y 15).
- Velocidad como dato de rodaje con la relación (decisión abierta 4) y modelo físico (F6.3, change 15).
- Barra inferior: Ajustes y Marchas siguen abriéndose desde Diagnóstico (change 10).
- Histéresis o compensación configurables por el usuario: en este change son valores fijos del diseño.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `virtual-gears`: transmisión Shimano 105 con sus modos automático y manual, sus saltos, extremos y topes; cambiar de transmisión; referencia y última marcha por transmisión; la marcha inicial es la de referencia.
- `trainer-resistance`: con la Shimano, la pendiente es proporcional a la relación y está normalizada al recorrido de la lineal.
- `button-mapping`: acciones de plato; una asignación por defecto y guardada por transmisión; Botones muestra, edita y restaura la de la transmisión elegida; migración de la configuración guardada.
- `ride-screen`: marcha plato × piñón con plato y cassette, transmisión y modo en la barra superior, avisos de plato y de referencia plato × piñón, topes de plato y de piñón.
- `settings-screen`: entrada «Marchas» en Ajustes y la pantalla de Marchas (transmisión, modo, platos y cassette, referencia y recordar la última marcha).
- `diagnostics`: Simulación con la marcha plato × piñón, su relación y el ajuste.
- `zwift-bridge`: en ERG de Zwift con la Shimano, el sentido del ajuste según la acción, y las acciones de plato sin efecto.
- `screen-mockups`: Marchas con la lineal y la Shimano (automático y manual), estados Shimano de rodaje (horizontal y vertical) con el desarrollo 52/36 · 11–36, Botones con la asignación de la Shimano, Ajustes con «Marchas» y Diagnóstico con plato × piñón.

## Impact

- **Código `:nucleo`:**
  - `Transmision` con la variante de platos y piñones y el desarrollo Shimano 105.
  - `EstadoMarcha` con plato, piñón y relación.
  - `ModoCambio` (automático, manual) y `Cambio` con sus reglas (secuencia con histéresis, compensación, topes de plato y de piñón).
  - `Accion.PlatoMas` y `Accion.PlatoMenos`, y la asignación por defecto de la Shimano.
  - `ModeloSimple` por relación.
  - `AjusteErg` con las acciones de plato.
  - `Configuracion` versión 2 con su migración, y `EstadoGuardado` con la última marcha por transmisión.
- **Código `:app`:**
  - `Sesion`: transmisión, modo y asignación en caliente.
  - `ui/marchas` (pantalla nueva con su `ViewModel`) y la entrada en Ajustes.
  - Rodaje: composables `ChainringIndicator` y `CassetteView`, y `GearDisplay` con plato × piñón.
  - Botones con la asignación de la transmisión elegida.
  - Diagnóstico y `TextosAccion`.
  - Sin dependencias nuevas.
- **Sistema de diseño y maquetas:** se reutilizan `GearDisplay` (`sq-ratio-hero`), `ChainringIndicator`, `CassetteView` y `SettingsList`; si falta una variante (por ejemplo, la ayuda del modo de cambio), se añade antes a `design-system/`. Maquetas: `marchas.html`, `rodaje.html`, `rodaje-vertical.html`, `botones.html`, `ajustes.html`, `diagnostico.html` e `index.html`.
- **Documentos de `docs/`:**
  - `funcional.md`: decisiones 3 y 5 cerradas, F5.7 y F5.8 con lo que se cumple, y la marcha inicial igual a la de referencia.
  - `arquitectura.md`:
    - § 4.3: `Transmision`, `Cambio` y `EstadoMarcha`.
    - § 5.3: acciones de plato.
    - § 5.5: modelo simple por relación.
    - § 10: configuración versión 2.
  - `planificacion.md`: fila 8 reducida, y un change nuevo para SRAM, Campagnolo y «A medida».
  - `desarrollo.md`: ninguno, salvo que cambie cómo se prueba.
