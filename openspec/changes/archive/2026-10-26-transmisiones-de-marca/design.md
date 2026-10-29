## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs del change.

Estado actual:
- **`Transmision`** (`:nucleo`, `Marchas.kt`): un `sealed interface` con una sola variante, `Lineal(marchas)`, serializada como `{"type":"lineal","marchas":24}` dentro de `configuracion.json`.
- **`EstadoMarcha(posicion, total)`**: lo usan `Cambio`, `ModeloResistencia`, `ControlRodillo`, `Sesion`, `RodajeUiState` y los tests. `arquitectura.md` § 5.5 ya prevé que exponga también plato, piñón y relación.
- **`Cambio`**: puro; recibe la referencia en cada llamada. Devuelve `Cambiada`, `Tope(Extremo)`, `SinCambio` o `ReferenciaFijada(Int)`.
- **`ModeloSimple(incrementoPct, marchaNeutra)`**: suma `(posición − neutra) × incremento`. `ControlRodillo` lo recibe al construirse y no lo puede cambiar después; `Sesion` lo construye una vez con la configuración por defecto.
- **`Configuracion` versión 1**:
  - Campos de marcha: `transmision`, `marchaInicial`, `marchaReferencia`, `marchaNeutra` (enteros) y `botones.asignacion` (una sola `Asignacion`, que incluye `invertirPalancas`).
  - `EstadoGuardado` versión 2: una sola `ultimaMarcha: Int?`.
  - La versión 1 ya migra `estado.json` (`migrarV1`), así que hay precedente.
- **`Accion`**: enum cerrado de acciones de marcha. `AjusteErg.aplicar` hace un `when` exhaustivo sobre ella.
- **UI de la marcha:**
  - `GearDisplay.kt` solo pinta la marcha lineal.
  - En `design-system/` ya existen la variante física de `GearDisplay` (`sq-ratio-hero`), `ChainringIndicator` y `CassetteView`, sin composable en la app.
  - Ajustes (`ui/ajustes`) solo tiene Rodillo, y se abre desde Diagnóstico.
- **Synchro Shift de Shimano**, tal como se describe en guías públicas (bettershifting, BikeRadar, Freewheel):
  - En Full Synchro, el delantero cambia solo al llegar el trasero a un piñón fijado, con puntos de subida y de bajada distintos, y el trasero compensa para dar la siguiente relación.
  - En Semi Synchro, el plato se cambia a mano y el trasero compensa 2 piñones por defecto.
  - No se ha encontrado el mapa de fábrica para 52/36 · 11–36, y el grupo del usuario es mecánico, así que no se puede leer en E-TUBE.

## Goals / Non-Goals

**Goals:**
- Que el modelo de transmisión de platos y piñones sirva sin cambios para SRAM, Campagnolo y «A medida» en el change que los añada: el desarrollo es un dato, no código.
- Las reglas del cambio automático y manual, puras en `:nucleo` y probadas con tablas.
- Cambiar de transmisión, de modo o de asignación en caliente, con el mismo camino que `cambiarBajadas`.
- Migrar la configuración de la versión 1 sin perder nada.

**Non-Goals:**
- Hacer configurables los puntos de cambio o la compensación (valores fijos del desarrollo).
- Que la UI dependa de cómo se calcula la pendiente: Rodaje solo ve plato y piñón.
- Rehacer la navegación: Marchas cuelga de Ajustes, que sigue colgando de Diagnóstico.

## Decisions

### 1. El desarrollo es un dato: `Transmision.Platos` y el catálogo `Desarrollos`

```kotlin
sealed interface Transmision {
    data class Lineal(val marchas: Int = 24) : Transmision
    data class Platos(
        val id: String, val nombre: String,
        val platos: List<Int>,          // de menor a mayor: [36, 52]
        val pinones: List<Int>,         // de menor a mayor: [11, 12, ..., 36]
        val sincronizado: Sincronizado, // puntos del modo automático
    ) : Transmision
}

data class Sincronizado(val subirDesdePinon: Int, val bajarDesdePinon: Int, val compensacion: Int)

object Desarrollos {
    val SHIMANO_105 = Transmision.Platos(
        "shimano-105", "Shimano 105", listOf(36, 52),
        listOf(11, 12, 13, 14, 15, 17, 19, 21, 24, 28, 32, 36),
        Sincronizado(subirDesdePinon = 15, bajarDesdePinon = 24, compensacion = 2),
    )
}
```

- En `configuracion.json` solo se guarda el **identificador** de la transmisión elegida (`"lineal"` o `"shimano-105"`), no los dientes. Así, una corrección del catálogo llega a todos sin migrar. «A medida» guardará sus dientes cuando llegue.
- Los puntos del automático van con el desarrollo porque dependen del cassette.
- Alternativas descartadas:
  - Una variante `Shimano105` con los números en código: obliga a repetir la lógica para cada marca.
  - Guardar los dientes en la configuración: no aporta nada hasta «A medida» y complica corregir el catálogo.

### 2. `EstadoMarcha` pasa a `sealed`: `Numerada` y `PlatoPinon`

```kotlin
sealed interface EstadoMarcha {
    data class Numerada(val posicion: Int, val total: Int) : EstadoMarcha
    @Serializable data class PlatoPinon(val plato: Int, val pinon: Int) : EstadoMarcha {
        val relacion: Double get() = plato.toDouble() / pinon
    }
}
```

- Plato y piñón se guardan en **dientes**, no en índices: se leen en los registros (`36×17`), se guardan tal cual en `estado.json` y la UI no necesita la transmisión para pintarlos. Al leer uno guardado se comprueba que pertenece al desarrollo; si no, se usa la referencia.
- Alternativa descartada: añadir campos opcionales a la clase actual. Deja estados imposibles (lineal con plato) y obliga a comprobar nulos en cada uso.
- Coste: tocar todos los usos de `posicion`. Están acotados (`Cambio`, `ModeloSimple`, `Sesion`, `RodajeUiState`, Diagnóstico y tests), y el compilador los señala todos.

### 3. `Cambio(transmision, modo)` con reglas, no con tablas

`ModoCambio { AUTOMATICO, MANUAL }` es serializable y solo tiene sentido con `Platos`; con la lineal se ignora.

Con `Platos`, `p` es el plato y `q` el índice del piñón (0 = 11, el más duro; 11 = 36, el más ligero). En el desarrollo de la Shimano, «subir» es el índice del 15, «bajar» el del 24 y `k` la compensación (2).

| Acción | Manual | Automático |
|---|---|---|
| `MarchaMas(1)` (más dura) | `q − 1`; en el 11, tope | Si plato pequeño y `q <= subir`: plato grande y `q + k`. Si no, `q − 1`. En 52×11, tope |
| `MarchaMenos(1)` (más ligera) | `q + 1`; en el 36, tope | Si plato grande y `q >= bajar`: plato pequeño y `q − k`. Si no, `q + 1`. En 36×36, tope |
| `MarchaMas(n)` / `MarchaMenos(n)` | `n` piñones, recortado al extremo del cassette | `n` pasos seguidos de la regla anterior, parando en el tope |
| `PlatoMas` / `PlatoMenos` | Cambia de plato y deja el piñón; tope si ya está | Cambia de plato y compensa `k` piñones: `q + k` al subir, `q − k` al bajar, recortado al cassette |
| `MarchaMasDura` / `MarchaMasLigera` | 52×11 / 36×36 | Igual |
| `MarchaReferencia` | Va a la referencia (plato y piñón) | Igual |

- Las dos reglas del automático dan, desde 36×36 y desde 52×11, exactamente la secuencia con histéresis que se acordó: subiendo, 36×36 … 36×15 → 52×19 … 52×11; bajando, 52×11 … 52×24 → 36×19 … 36×36.
- Al subir, el salto del 36×15 al 52×19 es de +14 %; al bajar, del 52×24 al 36×19, de −13 %.
- Desde una combinación fuera de la secuencia (por ejemplo, al pasar de manual a automático en el 36×13, o al volver a una referencia cruzada como el 52×28), las reglas siguen dando un paso con sentido: del 36×13 al 52×15, y del 52×28 al 36×21.
- Por eso se eligen reglas en lugar de una tabla: una tabla tendría que decir qué hacer desde las 24 combinaciones y desde cada sentido.

**Resultado:**
- `ResultadoCambio.Cambiada(anterior, nueva, pasos)`:
  - `pasos` es lo que avanzó la marcha, con signo: piñones en manual, pasos de la secuencia en automático, posiciones en la lineal.
  - Sustituye a `diferencia`, que hoy se calcula con la posición.
- `ResultadoCambio.Tope(tope)` cambia el enum `Extremo` por un tipo cerrado, `TopeMarcha` (no `Tope`, para que no choque con `ResultadoCambio.Tope`):

```kotlin
sealed interface TopeMarcha {
    data object MasDura : TopeMarcha; data object MasLigera : TopeMarcha   // extremos absolutos
    data class Plato(val dientes: Int) : TopeMarcha                         // «Tope · plato 52»
    data class Pinon(val dientes: Int) : TopeMarcha                         // «Tope · piñón 11» (manual, sin ser extremo absoluto)
}
```

- En manual, el tope del piñón es `MasDura` o `MasLigera` cuando además es el extremo absoluto: 52×11 al endurecer y 36×36 al aligerar.
- `ReferenciaFijada` lleva un `EstadoMarcha`.

### 4. Acciones de plato y asignación por transmisión

- **Acciones nuevas:** `Accion.PlatoMas` y `Accion.PlatoMenos`, con `@SerialName` `"plato-mas"` y `"plato-menos"`. No son repetibles.
- **`Accion.asignables(transmision)`:**
  - Con la lineal devuelve la lista actual.
  - Con `Platos` le añade las dos de plato tras «−3 marchas».
- **Lineal y ERG:** con la lineal, una acción de plato no hace nada (`SinCambio`), aunque no se puede asignar desde Botones. En ERG, `AjusteErg` las trata como `SinCambio`.
- **`Asignacion.POR_DEFECTO_PLATOS`** (D1 es la palanca grande, decisión 2 de `funcional.md`; como el STI de Shimano):

| Entrada | Gesto | Acción |
|---|---|---|
| Palanca D1 | Pulsar | −1 marcha (piñón más grande, más ligera) |
| Palanca D2 | Pulsar | +1 marcha (piñón más pequeño, más dura) |
| Palanca I1 | Pulsar | Plato más grande |
| Palanca I2 | Pulsar | Plato más pequeño |
| Campag. D + D1 | Pulsar | −3 marchas |
| Campag. D + D2 | Pulsar | +3 marchas |
| Campag. I + I1 | Pulsar | Marcha más dura |
| Campag. I + I2 | Pulsar | Marcha más ligera |
| □ | Corta / larga | Volver a la referencia / fijarla |

- **Por qué la misma asignación en manual y en automático:** las acciones `MarchaMas` y `MarchaMenos` ya significan «más dura» y «más ligera», y `Cambio` las interpreta según el modo. Así, cambiar de modo no obliga a rehacer los botones. Esto es lo que hace el Di2: la derecha es «el cambio» y lo que mueve depende del modo.
- **Cada `Asignacion` conserva su `invertirPalancas`:** invertir tiene sentido distinto en cada transmisión.
- **Lo compartido:** los tiempos de los gestos y la autorrepetición (`botones.tiempos` y `botones.autorrepeticion`), porque son del Square, no de la transmisión.
- **Alternativa descartada:** una sola asignación con acciones distintas según el modo (por ejemplo, «Palanca D1 → piñón más grande»). Duplica acciones que significan lo mismo para la UI de Botones y para ERG.

### 5. Pendiente proporcional a la relación (decisión 3)

```
ajuste(PlatoPinon) = incremento × ln(r / rNeutra) / paso
rNeutra = sqrt(rMin × rMax)                         // 2,174 en 52/36 · 11–36
paso    = ln(rMax / rMin) / (MARCHAS_EQUIVALENTES − 1)   // MARCHAS_EQUIVALENTES = 24
```

- `ModeloSimple` recibe la transmisión. Con `Numerada` hace lo de siempre, y con `PlatoPinon` aplica la fórmula.
- `MARCHAS_EQUIVALENTES` es 24, la lineal por defecto, y no depende de cuántas marchas tenga configurada la lineal: el recorrido de la Shimano no debe cambiar si un día la lineal pasa a 30.
- Con 1 % por marcha:

| Marcha | Relación | Ajuste |
|---|---|---|
| 36×36 | 1,00 | −11,50 % |
| 36×19 | 1,89 | −2,04 % |
| 36×17 | 2,12 | −0,39 % |
| 52×24 | 2,17 | −0,05 % |
| 52×19 | 2,74 | +3,41 % |
| 52×17 | 3,06 | +5,05 % |
| 52×11 | 4,73 | +11,50 % |

- Los tests comparan con dos decimales; las specs hablan de «unos +5,1 %».
- Alternativa descartada: un valor fijo por puesto en la lista ordenada por relación. Da pesos distintos a marchas casi iguales (52×28 y 36×19) y no prepara el modelo físico, que también va por relación.
- **`ControlRodillo.cambiarModelo(modelo, marcha)`:** devuelve el comando si cambia, como `cambiarBajadas`, para cambiar de transmisión en caliente.

### 6. Configuración versión 2, con un apartado por transmisión

```kotlin
data class Configuracion(
    val version: Int = 2,
    val transmision: String = "lineal",               // id de la elegida
    val lineal: AjustesLineal = AjustesLineal(),      // marchas = 24, referencia = 12, neutra = 12, asignacion = POR_DEFECTO
    val platos: Map<String, AjustesPlatos> = emptyMap(), // por id; falta → valores por defecto del desarrollo
    val incrementoPct: Double = 1.0, val pendienteBasePct: Double = 0.0,
    val recordarUltimaMarcha: Boolean = true, val mostrarAccion: Boolean = true, val vibracion: Boolean = true,
    val botones: ConfiguracionBotones = ConfiguracionBotones(), // tiempos y autorrepetición
    val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS,
)
data class AjustesPlatos(val modo: ModoCambio = AUTOMATICO, val referencia: EstadoMarcha.PlatoPinon, val asignacion: Asignacion = POR_DEFECTO_PLATOS)
```

- **Referencia por defecto de la Shimano:** 36×17 (−0,39 %, casi neutra), que está en la secuencia del automático.
- **Desaparece `marchaInicial`:** al empezar sin «Recordar la última marcha», o sin última marcha válida, se empieza en la referencia, como en la maqueta de Marchas («Al empezar»).
- **Migración de la versión 1 a la 2** en `deJson`, con el mismo patrón que `EstadoGuardado.migrarV1`:
  - `transmision` (objeto lineal) pasa a `"lineal"`.
  - `marchaReferencia` y `marchaNeutra` pasan a `lineal.referencia` y `lineal.neutra`.
  - `botones.asignacion` pasa a `lineal.asignacion`.
  - El resto se copia y `marchaInicial` se descarta.
- **Documentos inválidos:**
  - Uno ilegible da los valores por defecto (spec `button-mapping`, «Configuración ilegible»).
  - Una transmisión desconocida da la lineal.
  - Una referencia que no pertenece al desarrollo da la de por defecto.
- **`EstadoGuardado`:** sigue en la versión 2 y gana un campo opcional, `ultimasMarchas: Map<String, EstadoMarcha.PlatoPinon> = emptyMap()`, junto a `ultimaMarcha` (la lineal). Se lee sin migrar, como `puenteZwift`.
- **Alternativas descartadas:**
  - Campos sueltos (`marchaReferenciaShimano`…): no escalan a más desarrollos.
  - Un solo mapa para todas las transmisiones con la lineal dentro: obliga a migrar más y la lineal tiene campos distintos.

### 7. Cambios en caliente en la `Sesion`

`aplicarConfiguracion` compara con la configuración anterior:

- **Transmisión distinta:**
  - Guarda la última marcha de la saliente.
  - Construye `Cambio` y `ModeloSimple` nuevos.
  - Pone la marcha que corresponde a la entrante (la última, si se recuerda y es válida, o su referencia).
  - Llama a `control.cambiarModelo` y envía el comando si cambia.
  - No emite un aviso de acción ni vibra: el cambio se ve en Rodaje.
- **Solo cambia el modo:** reconstruye `Cambio` y la marcha no cambia.
- **Solo cambia la referencia** (desde el paso a paso de Marchas): se actualiza la marca y la marcha no cambia.
- **La asignación vigente** es la de la transmisión elegida: `asignacionVigente(configuracion)`. El reconocedor de gestos se reinicia como hoy al cambiar la asignación.

Se guarda la última marcha de la transmisión elegida en la misma escritura de `estado.json` de cada cambio. `fijarReferencia` escribe en el apartado de la transmisión elegida.

### 8. Pantalla de Marchas

- `ui/marchas`, con `MarchasViewModel` y `MarchasUiState`. Ruta `marchas`, desde la entrada «Marchas» de Ajustes, que dice la transmisión elegida («Shimano 105 · automático»). Volver lleva a Ajustes.
- Contenido, según la maqueta validada:
  - Transmisión: Lineal o Shimano 105 2×12, con `sq-opt`.
  - Con la Shimano:
    - Los platos (`ChainringIndicator`) y el cassette (`CassetteView`), con la referencia resaltada.
    - El modo de cambio: «Automático», por defecto, con su ayuda, o «Manual», con la suya.
  - Con la lineal: la escalera.
  - «Al empezar»: marcha de referencia con `Stepper` y «Recordar la última marcha».
- El `Stepper` de la referencia recorre, con la Shimano, las 24 combinaciones ordenadas por relación (con desempate por plato). Es un ajuste, no un cambio de marcha, y deja elegir cualquier combinación, como □ larga. Con la lineal recorre del 1 al 24. Los extremos deshabilitan − o +.
- Todo se guarda al tocarlo, con `Almacen.actualizarConfiguracion`, y la `Sesion` lo aplica por la decisión 7.
- No se incluyen el número de marchas de la lineal, «Sensación» ni la simulación avanzada de la maqueta (proposal, fuera de alcance).

### 9. Rodaje, Botones y Diagnóstico

- **Rodaje:**
  - `RodajeUiState` conserva `marcha`, `total` y `referencia` para la lineal y añade `platos: PlatosUi?` (plato, piñón, platos y piñones, sin relación ni pendiente), la transmisión y el modo. Se prefirió a un tipo cerrado para no reescribir los tests de la lineal; con platos, los campos de la lineal no se usan.
  - Con `PlatoPinon` se pinta:
    - `GearDisplay` en su variante física (`gear-ratio-hero`, × en `ink-muted`), con la animación según el sentido de la relación.
    - `ChainringIndicator` a la derecha, o debajo en vertical, y `CassetteView` en lugar de la escalera.
  - La línea de la transmisión dice «Shimano 105 · automático» o «Shimano 105 · manual».
  - El aviso de un cambio con plato distinto dice «Plato N». Si el plato no cambia, dice «±n marchas» con `pasos`.
- **Textos de los topes:**
  - `Tope.Plato(d)`: «Tope · plato d».
  - `Tope.Pinon(d)`: «Tope · piñón d».
  - Los absolutos, como hoy.
  - «Referencia en 36×17» al fijar la referencia.
- **Botones:**
  - Muestra la asignación de la transmisión elegida, con una línea que lo dice («Asignación de Shimano 105. Cada transmisión tiene la suya.»).
  - «Restaurar» vuelve a la asignación por defecto de esa transmisión y a los tiempos y la autorrepetición por defecto.
  - Asignar acción guarda en el apartado de la transmisión elegida.
  - Los nombres de las acciones son «Plato más grande» y «Plato más pequeño».
  - La ruta de Asignar acción identifica la acción por su posición en `Accion.TODAS` (las de la lineal y las de plato).
- **Diagnóstico:**
  - Simulación dice «Marcha 36×17 · relación 2,12» con la Shimano, y «Marcha 14 de 24» con la lineal.
  - «Último gesto» usa los textos nuevos.
  - Los registros de sesión escriben la marcha como `36×17`.

### 10. Validación con el equipo real

- **Correspondencia de las palancas:** antes de la etapa de app, el usuario comprueba en su STI mecánico que la palanca grande derecha lleva al piñón más grande y la grande izquierda al plato grande. Si no fuera así, se cambia `POR_DEFECTO_PLATOS` en la spec y aquí.
- **Prueba en el Neo** con la Shimano en automático:
  - Subir de 36×36 a 52×11 y bajar, contando los pasos (15 al subir y 15 al bajar) y observando los dos cambios de plato.
  - En el llano, notar que el salto de plato pesa como un cambio normal.
  - Comprobar que la latencia de la palanca en Diagnóstico no empeora (NF1).
- **En manual:** probar el plato con la izquierda y los topes de plato y de piñón.
- **Puntos de cambio** (subir desde el 15, bajar desde el 24, compensar 2): son a validar. Si el usuario los quiere distintos tras rodar, se cambia el dato `Sincronizado` y las specs con `/openspec-update-change`, sin tocar las reglas.
- **En Zwift** (si está a mano): una ruta con la Shimano. Las pendientes suman el ajuste por relación y Diagnóstico lo muestra.

### Resultado de la validación (26-10-2026)

El usuario validó con el Neo, el Square y su bici: la correspondencia de las palancas con su STI, el modo automático (15 marchas en cada sentido, cambios de plato en 36×15 → 52×19 y 52×24 → 36×19, compensación de 2 piñones), el manual con sus topes, los saltos, la referencia y la persistencia al cambiar de transmisión y al reabrir la app. Los puntos de `Sincronizado` se quedan como están. Queda pendiente la prueba en Zwift (tarea 8.4).

## Risks / Trade-offs

- [Migrar la configuración rompe una asignación personalizada] → Tests de `deJson` con un documento de la versión 1 real (asignación modificada, referencia en la 15, palancas invertidas) que debe quedar igual en `lineal`; un documento dañado sigue dando los valores por defecto.
- [Oscilar entre platos en la zona de histéresis con los saltos ±3] → El ±3 son tres pasos de la regla, que ya tiene histéresis; test que sube y baja tres veces seguidas desde el 36×15 y comprueba que no cambia de plato más de lo necesario.
- [El sentido de D1 y D2 en la Shimano es el contrario que en la lineal (D1 aligera) y puede confundir] → Es lo que hace el STI del usuario y lo valida él en la maqueta y en la bici; Botones dice de qué transmisión es la asignación, y se puede invertir.
- [Tocar `EstadoMarcha` rompe muchas pruebas] → Se hace en una tarea propia antes de añadir la Shimano, manteniendo verdes las trazas del change 4 y el 7 con `Numerada`.
- [La pendiente con decimales cambia en cada marcha y añade envíos al Neo] → `ControlRodillo` ya no reenvía un comando igual; los cambios de marcha son pocos por minuto.
- [Los puntos del automático no son los de fábrica de Shimano] → Están marcados a validar y aislados en el dato `Sincronizado`.

## Migration Plan

1. Al abrir la versión nueva, `Configuracion.deJson` lee la versión 1 y la migra en memoria. Se escribe la versión 2 en el siguiente guardado (cualquier cambio de ajustes o referencia). Hasta entonces, la versión 1 sigue en disco y se vuelve a migrar al abrir.
2. `estado.json` no cambia de versión, y el campo nuevo es opcional.
3. Vuelta atrás: una versión anterior de la app lee la versión 2 con `ignoreUnknownKeys`, pero no encuentra `marchaReferencia` ni `botones.asignacion` y usa los valores por defecto (pierde la asignación personalizada). Es aceptable en desarrollo; no hay usuarios fuera del autor.

## Open Questions

- Si, tras rodar, los puntos de cambio o la compensación del automático no convencen, se ajustan los valores de `Sincronizado` (decisión 10) sin cambiar el enfoque ni las tareas.
