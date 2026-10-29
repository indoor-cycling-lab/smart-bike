## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs del change.

Estado actual:
- `ModeloSimple.comando` devuelve `Pendiente(terreno + (marcha − neutra) × incremento)`, sin límite.
- `ControlRodillo.comandoDe` usa el modelo en `SimLibre`, con `terrenoBase`, y en `SimZwift`, con el terreno de Zwift. En `ErgZwift` envía la potencia, y en `Manual`, el comando elegido en Diagnóstico.
- `ControlRodillo` ya aplica dos protecciones comunes a todos los envíos: no repetir el último comando y la ventana del terreno. `arquitectura.md` § 5.5 sitúa allí también «evitar pendiente negativa (F6.5)».
- `Configuracion` se guarda en `configuracion.json` desde el change 7 (`gestos-y-asignacion`), que aún no está archivado y deja `version = 1`, `ignoreUnknownKeys` y valores por defecto en el dominio.
- La app no tiene pantalla de Ajustes. Botones se abre desde Diagnóstico de forma provisional (spec `button-mapping` del change 7).
- `SettingsList.kt` tiene sección, lista, fila, casilla y segmentado, pero no la opción de radio con descripción (`sq-opt`), que sí existe en `design-system/`.

Este change se implementa después de archivar el 7 y parte de su código.

## Goals / Non-Goals

**Goals:**
- Una sola regla del límite inferior, en `:nucleo`, probada con tests unitarios y válida para cualquier modelo que devuelva una pendiente.
- Cambiar de modo en caliente con el mismo camino que un cambio de marcha: un comando nuevo solo si cambia lo enviado.
- Que Diagnóstico pueda enseñar la pendiente sin límite sin recalcular nada en la UI.

**Non-Goals:**
- Suelo configurable, porque depende de la prueba con el Neo (ver Open Questions).
- Tocar el control manual o la ERG.
- Rehacer la navegación: Ajustes entra como pantalla secundaria de Diagnóstico, como Botones.

## Decisions

### 1. `ModoBajadas` en `:nucleo`, como enum serializable con la regla dentro

```kotlin
@Serializable
enum class ModoBajadas {
    SIN_PROTECCION, LA_MARCHA_NO_CREA_BAJADAS, NUNCA_EMPUJAR;

    /** Pendiente mínima que se envía al Neo con el terreno [pendienteTerrenoPct], o `null` si no hay límite. */
    fun suelo(pendienteTerrenoPct: Double): Double? = when (this) {
        SIN_PROTECCION -> null
        LA_MARCHA_NO_CREA_BAJADAS -> minOf(pendienteTerrenoPct, 0.0)
        NUNCA_EMPUJAR -> 0.0
    }
}
```

La regla queda en una función pura, fácil de probar con la tabla de las specs.

Alternativas descartadas:
- Un `Double` con el suelo: no expresa el modo «La marcha no crea bajadas», cuyo suelo depende del terreno.
- Un booleano más un suelo: obliga a combinar dos campos para tres casos.

Si más adelante el suelo de «Nunca empujar» se hace configurable, se le añade un parámetro sin cambiar los nombres guardados.

### 2. El límite se aplica en `ControlRodillo`, no en el modelo

`comandoDe` aplica el suelo al resultado del modelo en `SimLibre` y `SimZwift`, solo si es un `ComandoRodillo.Pendiente`, usando la pendiente del terreno de ese modo. `ErgZwift` y `Manual` no pasan por él.

Así se cumple `arquitectura.md` § 5.5 (protección común a todos los modelos), y los modelos por tabla y físico del change 15 la heredan sin hacer nada. El físico devuelve `Potencia` y no se ve afectado; su protección equivalente es la potencia mínima.

Alternativa descartada: aplicar el límite dentro de `ModeloSimple`. Habría que repetirlo en cada modelo, y el modelo no debe depender de un ajuste de seguridad del rodillo.

El umbral de 0,1 % y la ventana de 250 ms del terreno de Zwift comparan el comando ya limitado. Así, en una bajada larga de Zwift con «Nunca empujar», los terrenos sucesivos dan siempre 0 % y no se reenvían.

### 3. Cambio de modo en caliente

`ControlRodillo` recibe el modo en el constructor y expone `cambiarBajadas(modo, marcha): ComandoRodillo?`. Este método fija el modo y devuelve `siCambia(vigente(marcha))`, es decir, `null` en ERG, en manual o sin Neo, o si no cambia nada. Cuando el Neo se conecta, `neoListo` ya envía el vigente con el modo puesto.

`Sesion` observa la configuración (el flujo que deja el change 7) y llama al método en el hilo de control cuando cambia `bajadas`. Los cambios de los demás campos no lo llaman.

Alternativa descartada: recrear `ControlRodillo` al cambiar la configuración. Perdería `ultimoEnviado`, el modo de control y la espera de Zwift.

### 4. Pendiente sin límite para Diagnóstico

`ControlRodillo` expone `sinLimite(marcha): Double?`: la pendiente que daría el modelo sin aplicar el suelo, o `null` si el comando vigente no es una pendiente del modelo. Diagnóstico la muestra solo cuando difiere de la enviada en más de 0,05 %, para no enseñar diferencias de redondeo. El `DiagnosticoViewModel` la añade a su estado de Simulación junto al modo.

Alternativa descartada: guardar en `ControlRodillo` un indicador de «limitada» con el último envío. Tendría que mantenerse en todos los caminos de envío, y Diagnóstico puede calcularlo comparando las dos pendientes.

### 5. Persistencia sin cambio de versión

`Configuracion` gana `val bajadas: ModoBajadas = ModoBajadas.LA_MARCHA_NO_CREA_BAJADAS`.

Los documentos guardados sin ese campo se leen con el valor por defecto, gracias a `ignoreUnknownKeys` y al valor por defecto del campo. Es el mismo criterio que `puenteZwift` en `estado.json` (`arquitectura.md` § 10), así que no hace falta subir `VERSION` ni escribir una migración.

Un valor desconocido en el enum hace fallar la lectura entera y se vuelve a los valores por defecto, según la regla del change 7. Sería un retroceso de versión, aceptable porque solo hay un usuario.

### 6. Pantalla Ajustes mínima

Se crea `ui/ajustes` con `Ajustes.kt`, `AjustesViewModel` y su ruta. La pantalla tiene un `AppBar` con volver a Diagnóstico y una `SettingsSection` «Rodillo» con el grupo «Bajadas».

Las tres opciones usan un composable nuevo, `SettingsOpcion`, que reproduce `sq-opt` (radio con nombre y ayuda, anillo `gear` en la elegida y la fila entera como objetivo táctil de `touch-min`). Va en `SettingsList.kt`, porque la pieza ya está en el sistema de diseño y solo falta en Compose.

Se usa `sq-opt` y no `sq-seg` porque `SettingsList/README.md` reserva el segmentado para 2–4 opciones cortas, y «La marcha no crea bajadas» no lo es. Además, cada opción necesita su ayuda.

En Diagnóstico, la fila «Ajustes» va junto a «Asignación de botones», con el mismo patrón de enlace. El change 10 moverá Ajustes a la barra inferior y le añadirá el resto de secciones.

Textos de las opciones (ayudas en tuteo, una línea), a validar en la maqueta:
- **Sin protección**: «Lo que pida el terreno más la marcha, aunque el Neo empuje».
- **La marcha no crea bajadas**: «Bajar de marcha nunca hace la bajada más pronunciada».
- **Nunca empujar**: «El Neo no empuja nunca, ni en las bajadas».

### 7. Diagnóstico

En la cuadrícula de Simulación se añade la métrica «Bajadas», con el nombre corto del modo. Cuando el límite actúa, se añade una fila apilada (`SettingsItemApilado`, como «Terreno de Zwift») con «Limitada por Bajadas: sin límite sería −18,0 %».

La indicación va en texto, no solo en color, como pide `AGENTS.md`. «Enviada al Neo» conserva su color de subida o bajada.

### 8. Validación con el equipo real

El empuje del motor del Neo con pendientes negativas no está documentado. Se mide con el control manual de Diagnóstico, que no pasa por el límite:
1. Se envía cada pendiente de −0,5 % a −5 %, en pasos de 0,5 %.
2. En cada una se pedalea unos segundos y se deja de pedalear.
3. Se anota a partir de cuál el volante se mantiene o acelera sin pedalear.

Después, rodando:
- Sin Zwift, en la marcha 1 con el modo por defecto: no hay empuje.
- En una bajada de Zwift con «Nunca empujar»: tampoco.
- Cambiando de modo con la sesión en marcha: la pendiente cambia al momento y queda reflejada en Diagnóstico.

**Resultado (26-10-2026, Tacx Neo 1 del usuario, sin Zwift, control manual):**

| Pendiente | Al dejar de pedalear | Motor |
|---|---|---|
| −0,5 % | Se frena solo | No se oye |
| −1,0 % | Se frena solo | No se oye |
| −1,5 % | Se mantiene | Se oye |
| −2,0 % | Se acelera | Se oye más |

El motor empieza a empujar entre −1,0 % y −1,5 %. No se siguió hasta −5 %, porque a partir de −2,0 % ya acelera. Un suelo de −1,0 % en «Nunca empujar» conservaría algo de diferencia entre marchas sin que el Neo empuje (Open Questions).

## Risks / Trade-offs

- **Por debajo del límite, varias marchas se sienten igual.** Con Zwift a −7 % y el modo por defecto, las marchas de la 1 a la 12 envían −7 %. → Es lo que el usuario eligió al plantear los modos. Diagnóstico enseña cuándo actúa el límite, y la marcha sigue cambiando en pantalla.
- **Cambio de comportamiento sin que el usuario haga nada.** El modo por defecto deja de enviar pendientes negativas en llano. → Lo pidió el usuario. «Sin protección» devuelve el comportamiento anterior.
- **Choque con el change 7 en `Configuracion`, Diagnóstico y las maquetas.** → Se implementa después de archivar el 7 (decisión del usuario), sobre su código ya integrado.
- **El Neo podría empujar también a 0 % en algunas condiciones**, por ejemplo con viento a favor que mande Zwift en el terreno. → La prueba del punto 8 lo comprobaría. Si pasa, se estudia en un change posterior junto con el suelo configurable.

## Migration Plan

No hace falta migración: un `configuracion.json` anterior se lee con el modo por defecto. Para volver atrás basta con elegir «Sin protección».

## Open Questions

- **Suelo configurable en «Nunca empujar».** Si la prueba demuestra que el Neo no empuja con −1 % o −2 %, se puede ofrecer un suelo configurable. Encaja como parámetro de `ModoBajadas.suelo` sin tocar estas specs; se decidiría en un change aparte.
