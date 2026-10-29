# Design

## Context

Ver `proposal.md` para el porqué. Hoy:

- `ModoControl` (`:core`, `ControlRodillo.kt`) ya distingue `SimLibre` (pendiente base + marcha; sin app el terreno es `Terreno(0.0)`), `SimPlataforma(terreno)` (terreno de la app + marcha) y los ERG. La UI de Rodaje los muestra con la misma etiqueta, «SIM» (`modo_sim`, `RodajeUiState`), y Diagnóstico los distingue con `ModoUi.Sim` y `ModoUi.SimPlataforma`.
- `Sesion` mantiene el último terreno 30 s tras perder la plataforma y después vuelve a la pendiente base (`Sesion.kt`, «Zwift se ha ido»).
- Los botones − y + dependen solo de `Configuracion.botonesEnPantalla`, que llega a `RodajeUiState.botones`; Rodaje los pinta con `botones`, y `PantallaBaja.kt` limita a 4 datos cuando están.
- `RodajeUiState` ya conoce el estado del Square (`EstadoRodaje.square`).
- La rejilla muestra de 1 a 6 datos de ocho posibles (`DatosUiState`, `MetricGrid`).
- `ride-screen` tiene el requisito «Sin datos del modelo de resistencia», que se escribió más ancho de lo que se quería, y `GradeReadout` repite en su README «no ponerla en Rodaje».

## Goals / Non-Goals

**Goals:**
- Que el nombre del modo diga la verdad: «LIBRE» sin pendiente que simular, «SIM» con ella.
- Poder ver la pendiente de la simulación en Rodaje, sin abrir la puerta a la base ni a lo enviado al Neo.
- Que nunca falte una forma de cambiar de marcha en pantalla cuando el Square no funciona.

**Non-Goals:**
- Cambiar resistencia, modelos, «Bajadas» o ERG: ninguna lógica de control cambia.
- Un control manual de la pendiente.

## Decisions

**1. La etiqueta sale de `ModoControl`, no de una bandera nueva.** `RodajeUiState` calcula el modo mostrado a partir del modo de control de `Simulacion.modo`: `SimLibre` → «LIBRE», `SimPlataforma` → «SIM», ERG sin cambio. Así el mantenimiento de 30 s y el «Zwift conectado pero aún sin pendiente» salen solos: mientras no haya terreno ni lo haya habido hace menos de 30 s, el modo es `SimLibre`. Alternativa descartada: decidir «LIBRE» por «plataforma desconectada»; sería falso con Zwift conectado y en un menú, y duplicaría lo que ya sabe `ControlRodillo`. **A verificar con un test** que `SimPlataforma` solo nace con el primer terreno.

**2. «SIM» sigue siendo el nombre de la familia.** La acción «Alternar SIM ⇄ ERG», su asignación guardada y los textos de `button-mapping` no cambian: los usuarios los han asignado y «SIM ⇄ ERG» sigue entendiéndose. Solo la etiqueta y el aviso del pie dependen del modo actual: «Modo Libre» o «Modo SIM» al volver del ERG manual. Alternativa descartada: renombrar la acción a «Alternar simulación ⇄ ERG»; toca la configuración guardada, la web, las capturas y el Square para ganar poco.

**3. El dato «Pendiente» muestra el terreno, no la pendiente enviada.** Es `control.terrenoBase.pendientePct` en `SimPlataforma` (lo que ya llega a Diagnóstico como «Pendiente de Zwift»), 0 en Libre y «—» en ERG. Es la que el usuario ve en la app de entrenamiento y la que corrige la regla; la suma con la marcha y el límite de «Bajadas» siguen siendo de Diagnóstico. `RodajeUiState` expone `pendientePct: Double?` (nulo en ERG). Se añade a las nueve posibles de `DatosUiState`, a continuación de la distancia, sin estar elegido por defecto; las configuraciones guardadas no cambian.
- **Formato:** signo explícito y un decimal con coma («+4,2 %», «−1,0 %», «0,0 %»), como el resto de los porcentajes de la app. El color de subida o de bajada (`climb`, `descent`) se decide en la maqueta, porque el signo ya lo dice y nada puede ir solo en color.
- **Plantilla de la cifra:** «−88,8» para medir, como las otras plantillas, para que la cifra no cambie de tamaño al rodar.
- Alternativa descartada: una pastilla fija junto a la marcha o al modo; ocupa un sitio en una pantalla que ya va justa y el dato encaja con la forma en que el usuario ya elige lo que ve (F11.3).

**4. Los botones − y + son visibles si el Square no está conectado o si la casilla lo pide.** `RodajeUiState.botones = botonesEnPantalla || square != Conectado`. «No conectado» incluye buscando y reconectando (el Square se duerme y se despierta con un botón): en esos momentos el ciclista no tiene palancas y necesita los botones. Alternativas descartadas:
- Mirar solo si hay Square configurado: no ayudaría cuando se cae el Bluetooth, que es el caso que pidió el usuario.
- Mostrarlos siempre y quitar la casilla: molesta a quien rueda con las palancas y quiere los datos más grandes.
- **Efecto:** los botones pueden aparecer a mitad de rodaje. La marcha y los datos se encogen para hacerles hueco (decisión del usuario); no se reserva el hueco con el Square conectado.

**5. En pantalla baja, el límite de 4 datos sigue a los botones visibles, no a la casilla.** Mi propuesta durante la exploración era seguir la casilla para que los datos 5 y 6 no parpadeasen al caerse el Square, pero con el Square desconectado y la casilla desactivada los botones sí ocupan el sitio: sin quitar datos no caben en 360 × 640 dp. El usuario aceptó que los datos se encojan para hacer sitio, así que el límite sigue a lo visible, y los datos 5 y 6 vuelven en cuanto el Square se conecta. **La maqueta lo valida**; si parpadear incomoda, se reconsidera reservar el hueco.

**6. La regla de lo que Rodaje no muestra se corrige en la spec y en tres sitios más.** `ride-screen` («Sin datos del modelo de resistencia»), el README de `GradeReadout` («No ponerla en la pantalla de rodaje»: sigue siendo cierto de la base y la efectiva, no de la pendiente de la simulación) y la línea de `AGENTS.md` («La pantalla de rodaje muestra la marcha, nunca el modelo de resistencia (pendiente de Zwift, …)»), que pasa a: nunca la pendiente base, el ajuste por marcha ni lo enviado al Neo; la pendiente de la simulación solo como dato opcional.

**7. Orden con los changes en curso.**
- **`guia-inicio`** ya está archivado y confirmado. Este change se aplica **después**, y ajusta su texto en la app («se rueda en SIM o ERG manual» pasa a «Libre») y su spec (`getting-started-guide`) con un delta de este change. La página de modos de uso de la guía es del change `guia-modos-de-uso`, que va después de este. Lo de «sin controlador se cambia de marcha desde la pantalla» pasa a ser cierto por la regla de los botones, sin tocar la guía.
- **`modo-tactil-rodaje`** no tiene código: este change va **antes** y sus deltas (`ride-screen`, `settings-screen`, `manual-erg`, `screen-mockups`) se reescriben sobre esta base: «LIBRE» donde dicen «SIM» sin app, el indicador tocable también «LIBRE» y su casilla («Controles en pantalla») conserva la ayuda de que solo manda con el Square conectado.

## Risks / Trade-offs

- [Los botones aparecen o desaparecen rodando, y la marcha se redimensiona] → es el comportamiento pedido; la maqueta enseña el antes y el después a 360 × 640 y se prueba con el Square apagado en el móvil real.
- [El Square oscila entre conectado y reconectando, y los botones parpadean] → el estado «reconectando» ya viene suavizado por la lógica de conexión; si en la prueba real parpadean, se añade un retardo corto (1–2 s) antes de mostrarlos. No se añade antes de medirlo.
- [«LIBRE» a los 30 s de cerrar Zwift sorprende en plena subida] → es lo que ya ocurre con la pendiente; ahora además lo dice el indicador. La pendiente mostrada también cae a «0,0 %» a los 30 s.
- [El dato «Pendiente» en ERG muestra «—», que parece un fallo] → su ayuda en «Datos en pantalla» lo dice.
- [Textos «SIM» repartidos por specs, docs, web y capturas] → la tarea de documentación lo revisa con una búsqueda y `TextosInglesTest` cubre los recursos.
- [Dos deltas sobre los mismos requisitos (este y `modo-tactil-rodaje`) chocan al archivar] → se archivan en orden y el segundo se reescribe sobre el texto del primero (decisión 7).
- [Puede haber trabajo sin confirmar de otros changes en los mismos ficheros] → la tarea 2.0 lo comprueba antes de empezar la sección App.

## Migration Plan

Sin migración de datos: el dato nuevo no está elegido por defecto y la casilla no cambia de valor ni de clave. Quien tenía los botones desactivados y el Square apagado empieza a verlos; es el comportamiento pedido y se revierte conectando el Square.

Prueba con el equipo real (OnePlus 8, Neo y Square, y Zwift): sin Zwift, el indicador dice «LIBRE» y las marchas cambian la resistencia; con Zwift en una ruta, «SIM» y «Pendiente» sigue a Zwift (subida y bajada, con «Bajadas» en «La marcha no crea bajadas»); en un entrenamiento, «ERG ZWIFT» y «—»; cerrar Zwift y ver «LIBRE» y «0,0 %» a los 30 s; apagar el Square rodando y ver aparecer los botones, usarlos y comprobar que desaparecen al reconectar.

## Open Questions

- El color de «Pendiente» (subida, bajada, llano) y el estilo de «LIBRE» frente a «SIM» en `ModeBadge`: se fijan en la maqueta, sin cambiar las specs.
