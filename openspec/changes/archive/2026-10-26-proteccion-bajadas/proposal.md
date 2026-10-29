## Why

Cuando la pendiente que recibe el Neo es negativa, su motor empuja el volante, y se oye cómo gira solo aunque no pedalees. Con el modelo simple esto pasa incluso en llano: las marchas por debajo de la 12 restan pendiente, y en la 1 se envía −11 % con Zwift a 0 %. En una bajada de Zwift a −7 % se llega a −18 %. El Neo no tiene ningún ajuste para desactivar el empuje, pero la pendiente se la envía Square Shift, así que es la app la que puede evitarlo.

Este change adelanta F6.5 y la parte de F12.5 que le corresponde, previstas en el change 15 (`modelos-tabla-y-fisico`, fase 5). Se adelantan porque el efecto se nota en cada sesión y no dependen del modelo físico. Se implementa después del change 7 (`gestos-y-asignacion`), que toca los mismos ficheros (Diagnóstico, Ajustes, `Configuracion`).

## What Changes

- **Ajuste «Bajadas» con tres modos (F6.5):** decide la pendiente mínima que se envía al Neo en modo SIM.
  - **Sin protección**: pendiente del terreno + marcha, como hasta ahora.
  - **La marcha no crea bajadas** (por defecto): la marcha nunca deja la pendiente por debajo de la del terreno si este es negativo, ni por debajo de 0 % si es llano o subida. En llano y en subida el motor no empuja por culpa de la marcha; en una bajada de Zwift empuja lo que corresponde a esa bajada y no más.
  - **Nunca empujar**: la pendiente enviada no baja nunca de 0 %, ni siquiera en las bajadas de Zwift.
- **BREAKING** respecto a `trainer-resistance` y `zwift-bridge`: con el modo por defecto, las marchas ligeras ya no envían pendiente negativa en llano (la 1 envía 0 % y no −11 %), y en una bajada de Zwift no la hacen más negativa que la de Zwift.
- **Dónde se aplica:** sin Zwift, sobre la pendiente base (F7.1), y con Zwift, sobre la pendiente de Zwift (F8.2). No afecta a la ERG de Zwift ni al control manual de Diagnóstico, que envía exactamente lo que se elige.
- **Pantalla Ajustes mínima:** solo la sección «Rodillo» con el ajuste «Bajadas», que se abre desde Diagnóstico de forma provisional, como Botones en el change 7. El change 10 la completa y la lleva a la barra inferior.
- **Configuración guardada (parte de F12.5):** el modo elegido se guarda en `configuracion.json` y se aplica al momento, sin reiniciar la sesión.
- **Diagnóstico:** la sección Simulación muestra el modo de bajadas y, cuando limita la pendiente, la que habría correspondido y la enviada.
- **Rodaje no cambia:** sigue mostrando solo la marcha.
- **Maqueta primero:** Ajustes (sección Rodillo y el estado abierto desde Diagnóstico) y Diagnóstico (Simulación con la pendiente limitada), validadas por el usuario antes de la app.
- **Prueba con el equipo real:** localizar a partir de qué pendiente negativa empieza a empujar el motor del Neo, como dato para decidir si «Nunca empujar» necesita un suelo configurable.

Fuera de alcance:

- Suelo configurable en «Nunca empujar» (por ejemplo −2 %): solo si la prueba demuestra que el Neo no empuja con pendientes ligeramente negativas. Se decidiría en un change posterior.
- Potencia mínima en bajadas, pendiente base configurable y el resto de opciones de rodillo de F12.5, y el resto de Ajustes (change 10).
- Modelos por tabla y físico, dificultad global y demás protecciones de `ControlRodillo` (change 15). El modo de bajadas se aplicará también a esos modelos cuando lleguen.
- ERG manual (change 9).

## Capabilities

### New Capabilities

- `settings-screen`: pantalla de Ajustes de la app: sus secciones, cómo se llega a ella y cómo se guardan y aplican los ajustes. Empieza con la sección Rodillo y el ajuste «Bajadas»; el change 10 añade el resto.

### Modified Capabilities

- `trainer-resistance`: la pendiente según la marcha sin Zwift respeta el modo de bajadas; nuevo requisito con los tres modos.
- `zwift-bridge`: la pendiente de Zwift con la marcha respeta el modo de bajadas (escenario «Bajada»).
- `diagnostics`: Simulación muestra el modo de bajadas y la pendiente limitada. El acceso a Ajustes desde Diagnóstico se especifica en `settings-screen`.
- `screen-mockups`: Ajustes con el ajuste «Bajadas» y el estado abierto desde Diagnóstico; Diagnóstico con la pendiente limitada.

## Impact

- **Código `:nucleo`:** `ModoBajadas` y la regla del suelo, aplicada en `ControlRodillo` a todos los modelos (`arquitectura.md` § 4.3, protecciones comunes); `Configuracion` con el modo.
- **Código `:app`:** pantalla `ui/ajustes` con su `ViewModel`, navegación desde Diagnóstico, `Sesion` aplicando el modo al cambiar la configuración y los datos de Simulación en Diagnóstico. Sin dependencias nuevas.
- **Maquetas y sistema de diseño:** `maquetas/ajustes.html`, `maquetas/diagnostico.html` y `maquetas/index.html`. Se reutilizan `SettingsList` (`sq-opt`), `MetricGrid` y `AppBar`; no hace falta ningún componente nuevo.
- **Documentos de `docs/`:**
  - `funcional.md`: F6.5 cumplida y el modo por defecto; decisión abierta 8, con las marchas ligeras en llano sin pendiente negativa.
  - `arquitectura.md`: § 4.3, el modo de bajadas en `ControlRodillo`; § 10, el modo en `configuracion.json`; lo que mida la prueba sobre el empuje del Neo.
  - `planificacion.md`: fila nueva del change en la fase 3, y F6.5 fuera del change 15.
  - `desarrollo.md`: ninguno.
