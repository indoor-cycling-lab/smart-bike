## Context

`ConnectionChip` (CSS `sq-conn`, composable `ConnectionChip`) se usa en Rodaje, Dispositivos y Diagnóstico. En Rodaje, `RodajeUiState` calcula para cada chip `ChipUi(estado, enPalabra)` y `Rodaje.kt` pasa `enPalabra` como `estadoTexto`. En vertical, el `FlowRow` de chips reserva el alto de dos líneas para que la reconexión no mueva la pantalla. Esa reserva también está en la maqueta (`.sq-ride--v .sq-ride__conns { min-height: … }`) y en las pautas de `RideScreen` y `ConnectionChip`.

## Goals / Non-Goals

**Goals:**
- Una única variante de chip para Rodaje, sin palabra y de ancho constante, en las dos orientaciones.
- Recuperar para la rejilla de datos el alto de una línea en vertical.
- Mantener el estado accesible (forma + descripción para lectores de pantalla).

**Non-Goals:**
- Cambiar el chip fuera de Rodaje o el modelo `ChipUi` más allá de lo necesario.

## Decisions

**1. La palabra se quita en Rodaje, no en el componente.** `ConnectionChip` ya admite `estadoTexto = null`. Rodaje deja de pasarlo a la vista y lo usa para la semántica. Alternativa descartada: un parámetro `compacto` que oculte la palabra dentro del componente. Mezclaría el relleno lateral, que ya es `compacto`, con la palabra, y Dispositivos y Diagnóstico no cambian.

**2. Descripción accesible en el componente.** `ConnectionChip` recibe un parámetro `descripcion: String?`. Si llega, fusiona la semántica del chip (`Modifier.semantics(mergeDescendants = true) { contentDescription = … }`) para que TalkBack lea «Square, sin conexión». En HTML, la maqueta pone `aria-label="Square, sin conexión"` en el `a.sq-conn`. `ChipUi.enPalabra` se mantiene: ahora alimenta la descripción en lugar del texto visible.

**3. Una línea en vertical.** La fila de chips en vertical pasa de `FlowRow` con alto mínimo de dos líneas a `Row` sin reserva. En CSS se elimina el `min-height` de `.sq-ride--v .sq-ride__conns`. Con solo los nombres (Neo, Square, Zwift) y el relleno `space-2`, los tres chips miden unos 250 dp y caben a 360 dp con el margen de la pantalla. Como el texto no cambia con el estado, el ancho es constante. La única diferencia es el borde de alerta, que en Compose y en CSS no altera la medida (1 dp siempre presente). En horizontal no cambia la disposición: los chips siguen en `sq-ride__top` junto al modo.

**4. Forma en movimiento reducido.** Sin palabra, «esperando» y «sin conexión» se distinguen por la forma: anillo abierto (270°) en `ink-muted` frente a anillo cerrado en `danger` con el borde del chip en `danger`. Ya es así hoy; no hace falta tocar el dibujo del punto.

**5. Pautas del sistema de diseño.** Se sustituyen en `ConnectionChip/README.md` y `RideScreen/README.md`:
- La regla «si todo está conectado, se reducen a los puntos; un fallo los expande». La maqueta ya no la seguía y entraba en conflicto con mostrar siempre el nombre.
- La reserva de dos líneas en vertical.
- «Un fallo de conexión… el chip afectado se expande».

La regla nueva es: en Rodaje, nombre y forma, sin palabra, en una línea; el detalle, en Dispositivos.

## Risks / Trade-offs

- [El usuario no distingue de un vistazo «conectando» de «sin conexión» a 1–2 m] → La diferencia es de forma y de color (anillo gris abierto que gira frente a anillo rojo y borde rojo), y el pie ya explica qué hacer («Toca Neo o Square para conectarlos», «Pedalea para despertar el Neo»). Se valida en la maqueta y en el móvil.
- [Fuentes del sistema más grandes desbordan la línea a 360 dp] → La fila mantiene `horizontalArrangement` con espacio fijo y el texto de los nombres es corto; el test de Compose comprueba que los tres chips caben en una fila a 360 dp.
