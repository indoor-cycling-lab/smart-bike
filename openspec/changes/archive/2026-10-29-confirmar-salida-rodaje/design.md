# Design

## Context

Hoy `RodajeRuta` instala `BackHandler { vm.terminar() }` y `terminarSesion()` (en `AppGraph`) es asíncrono e idempotente: al acabar, `sesion` pasa a `null` y `MainActivity` llama a `finish()`. Rodaje ya muestra avisos efímeros en el pie (`ActionToast`, `softWrap = false`, 1,5 s) generados en `RodajeViewModel` con `delay` y un `StateFlow` por aviso. Motivación en `proposal.md`.

El pie es un hueco de una línea y lo pisan los avisos de las palancas; no sirve para un texto largo.

## Goals / Non-Goals

**Goals:**
- Que un Atrás suelto en Rodaje no pueda cerrar la app ni cortar las conexiones.
- Un aviso legible de varias líneas que no mueva nada de Rodaje.
- Lógica de la ventana de 3 s comprobable con tiempo virtual, sin Android.

**Non-Goals:**
- Tocar `terminarSesion()` ni el flujo de cierre de `MainActivity`.
- Confirmar el cierre desde la notificación o desde otras pantallas.

## Decisions

### 1. La ventana de confirmación vive en `RodajeViewModel`

Un `StateFlow<SalidaUi>` con `Ninguna`, `Confirmar` y `Cerrando`, y `fun atras()`:
- `Ninguna` → pasa a `Confirmar` y lanza un `delay(VENTANA_SALIDA_MS = 3_000)` tras el que vuelve a `Ninguna`.
- `Confirmar` → cancela el temporizador, pasa a `Cerrando` y llama a `terminarSesion()`.
- `Cerrando` → no hace nada (Atrás repetido no termina dos veces).

`RodajeRuta` pasa a `BackHandler { vm.atras() }`. Alternativas: guardar la hora del primer Atrás y compararla en el segundo (sin temporizador, pero el aviso no se retiraría solo y habría que medirlo con reloj en la UI); o hacerlo en el composable con `remember` (se perdería al girar la pantalla y no se podría probar sin Compose). Sigue el patrón de los avisos del ViewModel (arquitectura.md § 9.2).

### 2. «Cerrando…» dura lo que tarde `terminarSesion()`

No se añade retardo: el estado `Cerrando` se mantiene hasta que `MainActivity` hace `finish()`. Si la actividad se recrea durante el cierre (giro), el ViewModel conserva el estado. La duración real se mide en el OnePlus 8; si fuera tan breve que no se lea, se decide con el usuario (no se alarga artificialmente sin pedirlo).

### 3. Salir de Rodaje cancela la confirmación

Al abrir otra pantalla (Ajustes, Conexión, Dispositivos) y volver, no debe quedar un aviso a medias ni un primer Atrás «armado». `RodajeRuta` llama a `vm.cancelarSalida()` en el `onDispose` de un `DisposableEffect`; no hace nada si el estado es `Cerrando`.

### 4. Componente `ExitNotice`, flotante, translúcido y al centro

Nuevo en `design-system/` (`sq-exit-notice`) y en la app (`ExitNotice`). Discreto: fondo `surface` al 82 % de opacidad, borde de 1 px `line`, `radius-md`, texto de 14 px en peso 500, en una sola línea desde 360 dp de ancho (304 px) y partido en varias en pantallas más estrechas o con la letra ampliada (ancho máximo: la pantalla menos 24 px a cada lado). La translucidez es una opacidad sobre el color, sin desenfoque (que en Android 9 no se puede hacer barato).

Se coloca en un `Box` sobre el contenido de Rodaje, centrado en la pantalla en horizontal y en vertical (elegido por el usuario al validar la maqueta, tras compararlo con una variante más abajo, sobre el pie): está donde se mira al rodar, tapa un instante la marcha y, al ser translúcido, se sigue viendo. El pie queda libre para el aviso de acción. Es solo visual (no consume toques) y no cambia la medida de nada.

Alternativas: reutilizar `ActionToast` (descartado: una línea, 1,5 s, depende del ajuste «Mostrar la acción»); `Snackbar` de Material (arrastra otro sistema de estilos fuera de los tokens); diálogo modal (el usuario pidió «Atrás otra vez», y con las manos en el manillar un diálogo exige tocar); aviso abajo sobre el pie (descartado en la validación).

El aviso no depende del ajuste «Mostrar la acción»: es una protección, no un detalle de las palancas.

### 5. Accesibilidad

Región viva `Polite` como `ActionToast`; «Cerrando…» se anuncia como «Cerrando». El texto lleva la información, no el color.

### 6. Textos

`salida_confirmar` («Para cerrar la aplicación, pulsa Atrás otra vez») y `salida_cerrando` («Cerrando…») en `strings.xml`. `idioma-es-en` (change 34) crea la versión en inglés de todos los textos: si ya está integrado, los dos nuevos se añaden a `values-en`; si no, se anotan en su lista para que no se queden sin traducir.

## Risks / Trade-offs

- [El aviso tapa parte de los datos o de − y + durante 3 s] → es transitorio y no consume toques; se valida en la maqueta, con el estado vertical y los de botones − y +, antes de implementar.
- [Con navegación por gestos, el Atrás de borde puede dispararse dos veces por accidente y cerrar igual] → los dos gestos tienen que caer en menos de 3 s; es el trade-off asumido de la confirmación, y se prueba con gestos en el OnePlus 8.
- [«Cerrando…» tan breve que no se lea] → se mide en el móvil y se decide con el usuario (decisión 2).
- [Atrás repetido durante el cierre] → el estado `Cerrando` lo ignora y `terminarSesion()` ya es idempotente.
- [La translucidez deja ver lo de detrás y resta legibilidad al texto sobre la marcha o los datos] → se valida en la maqueta en los dos temas; si no se lee, subir la opacidad.
- [Conflicto de `strings.xml` con `idioma-es-en`] → decisión 6.

## Migration Plan

Sin migración: no cambia la configuración, `configuracion.json` ni ningún dato guardado. Para deshacerlo basta con volver a `BackHandler { vm.terminar() }`.
