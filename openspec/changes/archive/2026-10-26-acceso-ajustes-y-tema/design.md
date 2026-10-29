## Context

- **Navegación.** Rodaje es la raíz de la pila y no lleva barra inferior (spec `app-navigation`). Hoy solo sale de ella con los chips, que abren Dispositivos (`pantallas.rodaje { irA(Pestana.Dispositivos) }` en `AppNavegacion.kt`), o con «Pantalla anterior o siguiente» desde el Square. Ajustes es una pestaña con `irA(Pestana.Ajustes)`.
- **Tema.** `SqTema(tema: Tema = Tema.Rodaje)` ya admite `Tema.Claro`, con `ColoresClaro` generado de `tokens.json`. El contraste de los dos temas lo comprueba `contraste.py`. `MainActivity` aplica `SqTema` sin argumento a toda la app y fija barras del sistema oscuras (`SystemBarStyle.dark`). El fondo de la ventana (`color_ventana.xml`, generado) es el `bg` de rodaje.
- **Configuración.** `Configuracion` (en `:nucleo`) se guarda en `configuracion.json` con DataStore y se lee de forma asíncrona (`Almacen.configuracion: Flow`). `bajadas` se añadió como campo opcional con valor por defecto, sin subir la versión del documento.
- **Ajustes.** `AjustesViewModel.cambiar { … }` escribe en el almacén; las opciones excluyentes de «Bajadas» usan `SettingsOpcion` (`sq-opt`).
- **Rodaje.** En horizontal, `BarraSuperior` es un `FlowRow` con el modo a la izquierda y los chips a la derecha. En vertical, los chips compactos van en una `Row` de una línea y el modo debajo. Rodaje oculta las barras del sistema (`RodajeRuta`).

## Goals / Non-Goals

**Goals:**
- Llegar a Ajustes desde Rodaje con un toque, sin mover nada de lo que se lee al rodar.
- Elegir entre el tema oscuro y el claro y que se aplique a toda la app al momento y al volver a abrirla.
- Que Rodaje no se vea nunca un instante en el tema que no toca al girar el móvil o al volver de otra pantalla.

**Non-Goals:**
- Seguir el tema del sistema Android.
- Cambiar tokens, colores o la «Muestra del tema».
- El icono en otras pantallas.

## Decisions

**1. El icono es `tune`, el de la pestaña Ajustes.** Es el mismo que se ve en la barra inferior, así que se reconoce como la misma sección. `settings` (el engranaje) ya se usa para la entrada «Marchas» de Ajustes. Alternativa descartada: el engranaje, que se confundiría con Marchas.

**2. Botón del sistema de diseño: `sq-ride__settings`, parte de `RideScreen`.** Es un elemento de la pantalla de rodaje, no un componente general. Lleva el icono de 24 px en `ink-muted`, sin fondo ni borde, y el anillo de foco de siempre (`focus`). Como los chips, mide 32 × 32 y su zona táctil llega a `touch-min` (48 × 48) por fuera de la caja (`::before` en CSS; en Compose, la zona táctil ampliada sin cambiar el tamaño medido, como `ConnectionChip`). En HTML es un `<a href="ajustes.html" aria-label="Ajustes">`, último hijo de `sq-ride__conns`.
- Alternativa descartada: un componente `IconButton` general. Hoy solo lo usa Rodaje, y `AppBar` prohíbe acciones con icono en su barra.
- No va en `touch-ride` (72 dp): no se usa rodando, sino parado, como los chips.

**3. Posición.**
- En horizontal, el icono va al final de la fila de los chips, dentro del bloque derecho de `sq-ride__top` (`BarraSuperior`), así que sigue a los chips si el `FlowRow` los pasa debajo.
- En vertical, la línea de los chips pasa a ser `chips · hueco flexible · icono`: los chips siguen alineados a la izquierda y el icono queda en la esquina.
- Como el icono mide 32, lo mismo que los chips, la línea no crece y nada de debajo se mueve.
- A 360 dp de ancho, el útil en vertical es 312 dp (360 − 2 × `space6`). Los tres chips compactos más 32 dp y la separación tienen que caber. Se mide en la maqueta antes de seguir (tarea 1.2). Si no cabe, se para y se replantea con el usuario (por ejemplo, el icono en la línea del modo).

**4. Navegación: un segundo parámetro de Rodaje.** `Pantallas.rodaje` pasa a recibir `abrirDispositivos` y `abrirAjustes`, y `AppNavegacion` le da `irA(Pestana.Ajustes)`. La pila queda `[rodaje, ajustes]`, igual que desde la barra: Atrás y «Rodar» vuelven a Rodaje (spec `app-navigation`, sin cambios).

**5. El tema en la configuración: `tema: TemaApp = TemaApp.OSCURO`.** `TemaApp { OSCURO, CLARO }` va en `:nucleo` (`dominio`), con `@SerialName("oscuro")` y `@SerialName("claro")` como `ModoBajadas`. Así queda en el mismo documento que los demás ajustes y lo exportará el change 10b sin trabajo extra.
- Es un campo opcional con valor por defecto, como `bajadas`: un documento sin él se lee con `OSCURO` y no hace falta subir la versión (sigue en 4) ni migrar.
- `:app` traduce `TemaApp` al `Tema` de `ui.tema` (`OSCURO` → `Tema.Rodaje`, `CLARO` → `Tema.Claro`). Los nombres del sistema de diseño (`rodaje`, `claro`) no cambian; «Oscuro» es solo el texto de la UI.
- Alternativa descartada: un fichero o `SharedPreferences` aparte para leerlo de forma síncrona al arrancar. Duplica dónde viven los ajustes y lo que se exporta. A cambio, el arranque en frío tiene la espera de la decisión 7.

**6. Aplicación del tema: en la raíz, desde `AppGraph`.** `AppGraph` expone `tema: StateFlow<TemaApp?>`, que sale de `almacen.configuracion.map { it.tema }` con `stateIn(ambitoApp, Eagerly, null)`. Es `null` hasta la primera lectura. `MainActivity`:
- Pasa `SqTema(tema = …)` a la raíz del `setContent`. Al cambiar el ajuste, toda la composición se vuelve a pintar con los otros colores. La navegación, la sesión y los `ViewModel` no se tocan, así que la marcha y las conexiones siguen igual.
- En cada cambio de tema (efecto en la composición), vuelve a llamar a `enableEdgeToEdge` con `SystemBarStyle.light(TRANSPARENT, TRANSPARENT)` en claro o `SystemBarStyle.dark(TRANSPARENT)` en oscuro. También cambia el fondo de la ventana (`window.setBackgroundDrawable`) al `bg` del tema, con los colores de `Tokens.kt`, para que no asome otro color al girar ni en las transiciones. `RodajeRuta` sigue ocultando las barras como hasta ahora.
- Al girar, la actividad se recrea, pero `AppGraph` vive en la `Application`, así que el tema ya está leído. `onCreate` fija el fondo de la ventana y el estilo de las barras antes de `setContent`, y la primera composición ya sale con el tema bueno.

**7. Arranque en frío: no se pinta nada hasta conocer el tema.** Mientras `tema` es `null` (los milisegundos de la primera lectura de DataStore), `setContent` no compone las pantallas. Solo se ve la ventana con su fondo de `color_ventana.xml` (el `bg` de rodaje). Así Rodaje nunca aparece en oscuro para luego cambiar a claro.
- Lo que sí puede verse en claro es un instante de fondo oscuro liso (la pantalla de inicio del sistema y la ventana) antes de Rodaje. Se acepta.
- Alternativa descartada: la biblioteca `core-splashscreen` con `setKeepOnScreenCondition`. Añade una dependencia para el mismo efecto, y el fondo de la pantalla de inicio seguiría siendo uno solo, fijado en el tema de Android.
- Alternativa descartada: leer la configuración con `runBlocking` en `onCreate`. Bloquearía el hilo principal en disco.

**8. Sección «Pantalla» en Ajustes, entre «Al rodar» y «Ciclista».** Su título es «Pantalla» y dentro va «Tema», con un `fieldset` y su `legend`, como «Bajadas». Las dos opciones son `SettingsOpcion` (`sq-opt`), cada una con su ayuda:
- «Oscuro»: «Para rodar en interior, con poca luz».
- «Claro»: «Para leer con luz de día».

Va junto a «Al rodar» porque también es de cómo se ve la app. Alternativa descartada: un `Segmentado`. La ayuda de cada opción explica para qué sirve, y `sq-opt` ya distingue la elegida con la marca de radio, no solo con el color.

**9. Maquetas.**
- El icono se añade a todos los estados de `rodaje.html` y `rodaje-vertical.html` con el mismo marcado.
- `ajustes.html` pasa a tener dos estados: `inicio` (Oscuro elegido) y `claro` (Claro elegido). El tema de la maqueta lo sigue poniendo el selector de revisión (`#tema=claro`). La maqueta no cambia de tema al marcar la opción: es estática, como el resto.
- Se revisan las maquetas de rodaje en claro, que hasta ahora solo se miraban en oscuro.

## Risks / Trade-offs

- [Rodaje en claro no se había revisado nunca a 1–2 m.] → Los tokens claros pasan el contraste. Se revisa en la maqueta (`#tema=claro`) y en el OnePlus 8 desde la bici. Si algo no se lee, se trata en un change de tokens aparte: este no los toca.
- [La línea de los chips en vertical puede no caber a 360 dp con el icono.] → Se mide en la maqueta antes de implementar (decisión 3) y, si no cabe, se replantea con el usuario.
- [Fondo oscuro liso un instante al abrir la app en claro.] → Aceptado (decisión 7). Rodaje no se muestra en el tema equivocado.
- [Un valor de `tema` desconocido, de una versión futura, haría ilegible todo `configuracion.json` y se volvería a los valores por defecto.] → Pasa igual con `bajadas` y cualquier enum. El documento se lee con `ignoreUnknownKeys`, pero no con `coerceInputValues`. No se cambia aquí: afecta a toda la configuración y encaja mejor con exportar e importar (change 10b), que valida el documento entero.
- [El icono es un objetivo táctil más en Rodaje, cerca de los chips.] → Va en la esquina, separado por el hueco flexible en vertical y por `space2` en horizontal, y no se usa rodando, como los chips.
