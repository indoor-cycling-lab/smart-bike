# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs del change.

Estado actual:
- `ControlRodillo` decide el comando del Neo según `ModoControl`: `SimLibre`, `SimZwift(terreno)`, `ErgZwift(objetivoW, ajusteW)` y `Manual(comando, anterior)` (Diagnóstico). En ERG no envía la marcha; `alCambiarMarcha`, `cambiarBajadas` y `cambiarModelo` devuelven `null`.
- `AjusteErg.aplicar(accion, objetivoW, ajusteW)` traduce una acción de marcha en ±10 W por marcha, □ en `Quitado` y los límites 50–1000 W en `Tope`. La `Sesion` lo llama desde `gestoErg` y emite `EventoRodaje.Ajuste` / `TopePotencia`, con vibración y parada de la repetición.
- `Accion` tiene solo acciones de marcha y plato; `ASIGNABLES`, `TODAS` (identificación por posición) y `asignables(transmision)` fijan el orden de Botones. `ReglasSerializer` descarta reglas con acciones desconocidas.
- `Configuracion` va por la versión 2, con migración de la 1 al leer; `EstadoGuardado` añade campos opcionales sin cambiar de versión.
- Rodaje pinta `ErgTarget` y `ModeBadge(erg = true, origen = "ZWIFT")` a partir de `ErgUi`, que sale de `ergUi(modo)`. La notificación usa `potenciaErg(e)`.
- `arquitectura.md` § 5.4 ya prevé `ErgManual`, pero con las palancas cambiando de marcha y la cruceta ajustando ±W. El usuario ha decidido otra cosa (palancas como en ERG de Zwift); se actualiza en este change.

## Goals / Non-Goals

**Goals:**
- Reutilizar el camino del ERG de Zwift (`AjusteErg`, eventos, avisos, `ErgTarget`) en lugar de duplicarlo.
- Que el ERG manual sea un modo más de `ControlRodillo`, probado con tests unitarios de `:nucleo`, incluida su convivencia con Zwift, el control manual de Diagnóstico y la pérdida del Neo.
- Migrar las asignaciones guardadas sin pisar nada del usuario.

**Non-Goals:**
- Protecciones de potencia (rampas, alivio con cadencia baja): llegan con el modelo físico.
- Cambiar el protocolo FE-C: la página 0x31 ya está validada.

## Decisions

### 1. `ErgManual` con la misma forma que `ErgZwift`

```kotlin
data class ErgManual(val entradaW: Int, val ajusteW: Int, val debajo: ModoControl) : ModoControl {
    val potenciaW: Int get() = AjusteErg.limitar(entradaW + ajusteW)
}
```

`entradaW` es la potencia con la que se entró; `ajusteW`, lo que han sumado las palancas y ±10 W. Así `AjusteErg.aplicar(accion, entradaW, ajusteW)` sirve sin cambios: □ da `Quitado` (vuelta a la potencia de entrada), los límites dan `Tope` y los saltos recortados dan la diferencia real para el aviso. `debajo` es el modo SIM al que se vuelve (`SimLibre` o `SimZwift` con el último terreno), igual que `Manual.anterior`.

Alternativa descartada: `ErgManual(vatiosW)` con su propia función de ajuste. Duplica límites, topes y diferencias recortadas, y □ necesitaría guardar la entrada aparte de todos modos.

### 2. Transiciones en `ControlRodillo`

- `alternarErgManual(vatiosEntrada, marcha)`: desde `SimLibre`/`SimZwift` pasa a `ErgManual(vatiosEntrada, 0, modo)` y devuelve la potencia; desde `ErgManual` vuelve a `debajo` y devuelve `vigente(marcha)`; desde `ErgZwift` devuelve un resultado «no permitido» sin tocar nada. Con `Manual` (Diagnóstico abierto) actúa sobre `anterior` sin enviar, como ya hacen la marcha y Zwift.
- `ajusteErg(ajusteW)` se generaliza a `ErgZwift` y `ErgManual`.
- `terrenoZwift` en `ErgManual` solo actualiza `debajo` (no envía). `potenciaZwift` en `ErgManual` pasa a `ErgZwift(vatios, 0)`: el ajuste del ERG manual no se traslada al entrenamiento.
- `zwiftPerdido` / `zwiftAusente` miran también `debajo` del `ErgManual`: tras 30 s, `debajo` pasa a `SimLibre` sin enviar nada, porque manda la potencia.
- `alCambiarMarcha`, `cambiarBajadas` y `cambiarModelo` tratan `ErgManual` como `ErgZwift` (no envían).
- `neoListo` y `neoPerdido` no cambian: el vigente de `ErgManual` es su potencia.

Un helper `esErg` (o un `sealed interface Erg`) evita repetir `is ErgZwift || is ErgManual` en cada método.

### 3. Acciones nuevas en `Accion`

`ErgMas` (`"erg-mas"`), `ErgMenos` (`"erg-menos"`) y `AlternarErgManual` (`"alternar-erg"`), `data object` con nombres serializados estables. El paso es fijo (`AjusteErg.PASO_W`), así que no llevan parámetro; `arquitectura.md` § 5.3 se ajusta (`ErgMas(w)` → `ErgMas`).

- `repetible` incluye `ErgMas` y `ErgMenos`.
- `ASIGNABLES` añade las tres tras `FijarReferencia`, y `asignables(transmision)` las mantiene al final también con platos. `TODAS` las añade **al final**, para no mover la posición de `PlatoMas`/`PlatoMenos` en los tests y en la navegación que la usan.
- `AjusteErg.aplicar` resuelve `ErgMas`/`ErgMenos` como ±`PASO_W`; `AlternarErgManual` no le llega (lo resuelve la `Sesion`).

### 4. Camino en la `Sesion`

`gesto(g)` reparte:
- `AlternarErgManual` → `control.alternarErgManual(potenciaRecordada, marcha)`. Emite `EventoRodaje.ModoErg(entrada = true, vatios)` / `ModoErg(entrada = false)` con vibración del gesto, o `EventoRodaje.ErgZwiftActivo` con vibración doble si manda Zwift.
- En `ErgZwift` o `ErgManual`, las acciones de marcha (salvo `FijarReferencia`), `ErgMas` y `ErgMenos` van a `gestoErg`, generalizado a los dos modos. En SIM, `ErgMas`/`ErgMenos` no hacen nada (sin aviso ni vibración).
- Para el aviso de □ en ERG manual, el evento lleva la potencia de entrada (`AvisoUi.VueltaA(vatios)`); en ERG de Zwift sigue `PotenciaZwift`.
- La potencia del ERG manual se guarda en `EstadoGuardado.ultimaPotenciaErgW` (opcional, sin cambio de versión, 150 W si falta) con `guardarEnUnRato` en cada cambio y al salir, y también al terminar el ERG manual porque empieza un entrenamiento de Zwift.
- El apagado del Square (`antesDeApagar`) guarda y deshace también el ajuste del ERG manual, con el mismo código que el de Zwift.

### 5. Configuración versión 3: reglas nuevas solo en huecos libres

`Configuracion.VERSION = 3`. Al leer un documento de versión < 3 (incluida la 1, tras su migración a 2), a la asignación de la lineal y a la de cada desarrollo guardado en `platos` se le añade la regla nueva (`△` larga → `AlternarErgManual`) **solo si** la asignación no tiene ya una regla en esa entrada que choque con el gesto (`enConflicto` vacío, o equivalente sobre la entrada guardada). Los desarrollos no guardados usan `AjustesPlatos()` y ya traen la regla por defecto.

Como la migración solo se ejecuta con versión < 3, quitar después una regla nueva no la hace volver.

Alternativas descartadas:
- Solo con «Restaurar»: el usuario tendría que perder sus reasignaciones para tener el ERG.
- Añadir las reglas en cada lectura si faltan: volverían tras quitarlas a propósito.

### 6. UI: `ErgUi` con origen

`ErgUi(potenciaW, origen, deZwiftW?, ajusteW)`, con `origen` `Zwift` o `Manual`. En manual, `ErgTarget` no pinta la línea de Zwift ni la del ajuste (parámetros `deZwift` y `ajuste` nulos), y `ModeBadge` lleva `origen = "MANUAL"` con la explicación «ERG manual · las palancas ajustan la potencia». La notificación (`potenciaErg`) y la sección Simulación de Diagnóstico (`ModoUi.ErgManual`, «Potencia fijada», «Entrada») leen el mismo modo.

La zona ERG va centrada en horizontal en su columna (`.sq-ride__erg`), en las dos orientaciones y en los dos ERG, y los datos de `MetricGrid` van centrados en su celda (`.sq-ride .sq-mgrid .sq-metric`) en todos los modos: lo pidió el usuario al revisar la maqueta. Después pidió también centrar la marcha en horizontal (`.sq-ride__gear > .sq-col` y `.sq-ride__gearrow` centrados), como ya estaba en vertical. Cambia también el `Objetivo` del ERG de Zwift, la rejilla de SIM y `GearDisplay` en horizontal.

Componentes del sistema de diseño: no hace falta ninguno nuevo. `sq-ride__erg` sin `sq-target` y `sq-mode erg` con `<small>MANUAL</small>` son combinaciones de lo existente; se documentan en el README de `RideScreen`/`ErgTarget` y se muestran en la maqueta. Si al maquetar hiciera falta otra variante, se añade antes a `design-system/` (tarea de la sección Maqueta).

### 7. Validación con el equipo real

La página FE-C 0x31 ya está validada (change 3, traza `neo-erg-200w.txt`). Falta comprobar que el Neo **mantiene** la potencia al cambiar de cadencia: ERG manual a 200 W, 30 s a ~70 rpm y 30 s a ~95 rpm, con el registro de sesión para leer la potencia de la página 0x19. Criterio: media de cada tramo de 30 s, quitando los 5 primeros segundos, dentro de ±5 % de 200 W. También: alternar sobre una ruta de Zwift y volver con el terreno vigente; entrenamiento de Zwift que interrumpe el ERG manual.

Resultado (26-10-2026, OnePlus 8 y Neo de pruebas): hecha a 150 W en lugar de 200 W, a petición del usuario. El usuario confirma que el Neo mantiene la potencia en los dos tramos de cadencia. No se ha comprobado el ±5 % con números: el registro de sesión aún no se puede exportar (change `registro-sesion`) y la app no escribe en logcat.

## Risks / Trade-offs

- [El anexo A.2 proponía Campag. D + cruceta ↑/↓ para ±10 W, pero con el Square emparejado en Zwift la flecha también llega a Zwift (↓ es «Cambio de sentido»)] → Decidido al validar la maqueta: «+10 W» y «−10 W» quedan sin botón por defecto, como en ERG de Zwift, donde ajustan las palancas. Siguen siendo asignables; Asignar acción avisa si la entrada la lee Zwift.
- [△ larga añade el umbral de pulsación larga (800 ms) al alternar] → Alternar no está sujeto a NF1 (solo las palancas); es intencionado para no alternar por accidente.
- [Un salto brusco de potencia al entrar en ERG manual si la recordada está lejos de lo que se lleva] → Decisión del usuario (última usada); el Neo hace su propia rampa. Si molesta, se revisa con el modelo físico.
- [Mover el orden de `ASIGNABLES` cambia filas de Botones y tests de UI] → Las nuevas van al final de las de marcha; `TODAS` las añade al final para no mover índices existentes.
- [Migración de la versión 3 con documentos raros (reglas ilegibles, platos de desarrollos desconocidos)] → Se migra lo que se lee tras `ReglasSerializer`; tests con ficheros de ejemplo de las versiones 1 y 2.

## Migration Plan

- Lectura de `configuracion.json` versión 1 o 2 → versión 3 con las reglas en huecos libres; se escribe la versión 3 en el siguiente guardado.
- `estado.json` sin `ultimaPotenciaErgW` → 150 W, sin cambio de versión.
- Vuelta atrás: una app anterior lee la versión 3 con `ignoreUnknownKeys` y `ReglasSerializer` descarta las reglas con acciones desconocidas, así que sigue funcionando sin ellas.
