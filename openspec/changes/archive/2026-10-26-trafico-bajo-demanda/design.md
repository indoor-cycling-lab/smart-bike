## Context

`DiagnosticoViewModel` muestrea `RegistroSesion.cambios` como mucho cada 100 ms. En cada muestra pide las últimas 200 entradas y forma las filas de `HexLog` fuera del hilo principal (commit `7eadf56`). `HexLog` es una `LazyColumn` de alto limitado a 12 filas. La sección Tráfico de `Diagnostico.kt` muestra el volcado y, debajo, «Pausar/Reanudar» y «Limpiar». El `ViewModel` vive mientras se está en Diagnóstico: `onCleared` ya se usa para devolver el Neo a la marcha al salir.

En la maqueta (`maquetas/diagnostico.html`), cada estado tiene su volcado con `data-mock-state`, y el estado `rodando` añade «Compartir registro» bajo «Pausar» y «Limpiar».

## Goals / Non-Goals

**Goals:**
- Que, con el tráfico oculto, Diagnóstico no haga ningún trabajo por las tramas: ni muestreo, ni filas, ni recomposición.
- Reutilizar componentes del sistema de diseño ya existentes, sin variantes nuevas.

**Non-Goals:**
- Cambiar `RegistroSesion`, su capacidad o el volcado al registro del sistema.
- Optimizar más `HexLog`: el commit `7eadf56` ya lo hizo.

## Decisions

1. **Interruptor: fila con casilla de `SettingsList`.** Es `SettingsItemCasilla` en la app y `label.sq-item` con `sq-check` en la maqueta, dentro de un `sq-list` al principio de la sección. Nombre: «Tráfico en directo». Ayuda: «Cada trama del Neo, el Square y Zwift según llega». Ya cumple el objetivo táctil de 48 dp, se anuncia como casilla y se usa así en Ajustes.
   - Alternativa: un botón «Mostrar tráfico» que se convierte en «Ocultar tráfico». Añadiría un tercer botón junto a «Pausar» y «Limpiar», y en tres columnas «Reanudar» no cabe en una línea a 411 dp.
   - Alternativa: un interruptor (*switch*). No existe en el sistema de diseño y habría que añadirlo solo para esto.
2. **El estado vive en el `ViewModel` y no se guarda.** Es un `MutableStateFlow(false)` en `DiagnosticoViewModel`, expuesto en `DiagnosticoUiState.traficoVisible`. Sobrevive al giro, como el resto del estado de la pantalla, y se pierde al salir de Diagnóstico, cuando se destruye el `ViewModel`. No va a DataStore, porque recordarlo entre visitas devolvería el coste al entrar.
3. **Sin el tráfico visible no se muestrea.** La cadena de muestreo se cuelga del interruptor con `flatMapLatest`. Con el interruptor apagado emite una lista vacía y no se suscribe a `registro.cambios`. Al encenderlo arranca con `onStart { emit(0) }`, como ahora, así que la primera muestra recoge las tramas llegadas mientras estaba oculto. «Limpiar» conserva su marca (`limpiadoHasta`) durante la visita.
4. **Al ocultar se anula la pausa.** `alternarTrafico()` pone `pausado` a `false` al apagarlo, para que al volver a encenderlo se vea en directo y el botón diga «Pausar». Es el escenario «Ocultar el tráfico» de la spec.
5. **«Compartir registro» queda fuera del interruptor** en la maqueta (estado `rodando`). Es una acción sobre el registro de sesión (F13.4), no sobre el volcado. En la app no existe todavía.
6. **Maqueta interactiva sin JavaScript nuevo.** El volcado y «Pausar»/«Limpiar» de cada estado van dentro de un contenedor `.rv-trafico`. Una regla en `maquetas/revision.css` los oculta mientras la casilla, marcada con `.rv-trafico-casilla`, no está marcada: `.sq-section:has(.rv-trafico-casilla:not(:checked)) .rv-trafico { display: none; }`. `revision.css` es la hoja de la herramienta de revisión, no del sistema de diseño. La regla imita un comportamiento de la app y no define estilos de componente, así que no va en `components.css`.

## Risks / Trade-offs

- [Quien entra a Diagnóstico a mirar tramas tiene que tocar una vez más] → La casilla está al principio de la sección y, al activarla, aparecen también las tramas llegadas antes.
- [Con el tráfico en directo, la pantalla puede seguir yendo a tirones] → Antes de este change se prueba el commit `7eadf56` con el equipo real. En el OnePlus 8 se compara `adb shell dumpsys gfxinfo jmlopezdona.squareshift.debug` (fotogramas lentos) al desplazar Diagnóstico con el tráfico oculto y visible. Si con el tráfico visible siguen los tirones, se abre una tarea aparte sobre `HexLog`; no se amplía este change.
- [`:has()` en la maqueta] → Lo soportan Chrome, Safari y Firefox actuales, que son los navegadores de revisión. Sin él, el volcado se vería siempre, como hasta ahora.
