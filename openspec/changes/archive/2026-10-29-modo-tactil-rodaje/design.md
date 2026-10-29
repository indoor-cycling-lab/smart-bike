# Design

## Context

Ver `proposal.md` para el porqué. Hoy:

- El modo se muestra con `ModeBadge` (32 dp de alto, solo etiqueta), dentro de `Modo()` en `Rodaje.kt`: «LIBRE» o «SIM» con la transmisión (según `estado.libre`, change `modo-libre-y-pendiente`), «ERG MANUAL» o «ERG APP» según `estado.erg`.
- SIM ⇄ ERG manual lo resuelve `Accion.AlternarErgManual`, que llega a `Sesion` por `sesion.accion(...)`, el mismo camino que usan las palancas y los botones − y + (`RodajeViewModel.mas()` y `menos()`). `Sesion` ya decide qué hacer en ruta de la app, en entrenamiento de la app (no cambia nada, vibra y avisa) y sin Neo.
- Los botones − y + se ven con el ajuste `Configuracion.botonesEnPantalla` (desactivado por defecto) o con el Square no conectado; `RodajeUiState.botones` ya recoge las dos cosas (change `modo-libre-y-pendiente`). Ese ajuste no cambia en este change.

## Goals / Non-Goals

**Goals:**
- Que el indicador de modo sea otro punto de entrada a `AlternarErgManual`, sin lógica de modo nueva.
- Que no cambie nada para quien no activa el ajuste nuevo.

**Non-Goals:**
- Cambiar la semántica de Libre o SIM ⇄ ERG ni el modelo de resistencia.
- Un gesto distinto (toque largo) o una confirmación.

## Decisions

**1. El toque envía la acción existente.** `RodajeViewModel` gana `alternarModo() = sesion.accion(Accion.AlternarErgManual)`. Los tres casos que la spec exige (ruta de la app, entrenamiento de la app, Neo ausente) ya los resuelve `Sesion` para el Square, así que el toque no puede divergir de la palanca. Alternativa descartada: una función propia de la UI que cambie el modo; duplicaría reglas y obligaría a probar dos veces lo mismo.

**2. Ajuste propio, independiente de los botones − y +.** `Configuracion` gana `modoTactil: Boolean = false` y `RodajeUiState` expone `modoTocable = modoTactil`. No depende de `botones`: con los botones visibles y el ajuste apagado, el indicador sigue siendo etiqueta, y con el Square caído el indicador no se vuelve tocable por sí solo. El valor por defecto cubre los documentos guardados antes (kotlinx.serialization con valor por defecto), así que no hay migración ni cambio de versión; el campo entra en la copia de seguridad. Alternativa descartada: reutilizar `botonesEnPantalla`; el usuario quiere decidir por separado los botones y el toque del modo, porque tocar el indicador por error cambia de modo y tocar − o + no.

**3. Nombre y ayuda de la casilla.** «Cambiar de modo tocando», en «Al rodar», con la ayuda «Toca el indicador de modo para pasar de Libre o SIM a ERG manual y volver. Solo en rodaje sin entrenamiento de la app». La casilla de los botones conserva «Botones − y + en pantalla» y su ayuda. La clave interna de los botones sigue siendo `botonesEnPantalla`.

**4. Objetivo táctil de 48 dp sin agrandar el indicador.** `ModeBadge` sigue mostrando 32 dp, pero con `modoTocable` su área de toque sube a `touchMin` (48 dp) con relleno invisible, como ya hace el icono de Ajustes con su `offset`. Se usa `touchMin` y no `touchRide` (72 dp), porque no es una acción de rodaje sino de ajuste puntual. Se añade al componente una variante tocable: `role = Role.Button`, descripción accesible «Modo Libre, toca para pasar a ERG manual» (o «Modo SIM, …») y, en «ERG APP», «Modo ERG de la app», porque tocarlo da el aviso. Alternativa descartada: un botón aparte junto al indicador; ocupa más en la fila de modo, que en vertical a 360 dp ya lleva la transmisión y Ajustes.

**5. «ERG APP» también es botón.** Así el indicador no cambia de aspecto según quién manda el modo, y el aviso «Entrenamiento de la app» explica por qué no cambia. Alternativa descartada: ocultar la interacción en ese caso; obligaría a un tercer aspecto del indicador y quien lo toque se quedaría sin respuesta.

**6. Apariencia de botón sin glifo.** `ModeBadge` tocable es un `<button>` con relleno sutil tintado con el color del modo (`color-mix` con el fondo), borde más marcado que el de la etiqueta y un fondo más oscuro al pulsar. El indicio de que se puede tocar es la forma de botón (relleno y borde), no solo el color: la etiqueta sigue siendo solo contorno sobre `bg`. Se descartó un glifo ⇄ y un chevron por ruido visual.

## Risks / Trade-offs

- [Toque accidental con las manos en el manillar] → el ajuste «Cambiar de modo tocando» está desactivado por defecto, el cambio es reversible con otro toque y el pie avisa; si al probar en el equipo real hay toques fortuitos, se reconsidera el toque largo, que la spec actual no necesita.
- [El indicador cambia de ancho entre «LIBRE», «SIM» y «ERG MANUAL» y puede empujar a Ajustes en 360 dp] → la maqueta valida los dos modos en vertical y en horizontal, y los specs exigen que Ajustes, la barra de conexiones y la marcha no se muevan.
- [Dos puntos de entrada a la misma acción pueden alternar dos veces seguidas] → cada toque es una pulsación independiente, como pulsar △ dos veces; no se filtra.

## Migration Plan

Sin migración de datos. No requiere reversión: desactivar la casilla devuelve el indicador a etiqueta.

Prueba con el equipo real (OnePlus 8 y Neo): sin Square asignado y con «Cambiar de modo tocando» activado, alternar tocando el indicador LIBRE → ERG manual y comprobar que el Neo pasa de la pendiente de la marcha a la potencia recordada y mantiene la potencia a dos cadencias distintas; volver a LIBRE y comprobar que recibe la pendiente. Repetirlo en una ruta de la app y comprobar que en un entrenamiento el toque no cambia nada y avisa.

## Open Questions

Ninguna.
