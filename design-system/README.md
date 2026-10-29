Smart Bike es la app Android que convierte los mandos de un Elite Square en marchas virtuales para un Tacx Neo 1 y hace de puente con Zwift. Se usa montado en la bici, con el móvil en un soporte a 1–2 m, sudando y sin tocar la pantalla. Todo el sistema parte de esa situación.

El prefijo `Sq` de los componentes y del tema (`sq-*`, `SqTema`) es una sigla del sistema de diseño y no significa «Square»; «Square» es solo el mando Elite que la app soporta.

## Principios

1. **Se lee de un vistazo a 2 m.** La marcha es lo más grande de la pantalla, siempre en el mismo sitio y en `gear`. Las cifras usan la familia `numeric`, tabular.
2. **Se maneja con el Square, no con el dedo.** Ningún flujo de rodaje exige tocar la pantalla. Cada acción del Square tiene respuesta visible (`GearDisplay` y, si está activado, `ActionToast`) y háptica, si está activada.
3. **Un color, un significado.** `gear` es la marcha, la acción principal y lo elegido o activo (foco, pestaña, opción, casilla, tecla pulsada, captura, lo que resuelve la app); `climb`/`descent` solo pendiente, y solo en Diagnóstico; `sim`/`erg` solo modo; `ok`/`danger` solo conexión y alertas. Ningún color se usa como decoración.
4. **El estado se lee por la forma y la palabra, no solo por el color.** Punto lleno o anillo vacío, «SIM» o «ERG», «sin conexión».
5. **Se ve la marcha, no el modelo.** En rodaje, la marcha se muestra como en un grupo inalámbrico real: el número en transmisión lineal, o plato × piñón con el plato y el cassette en una transmisión física (Shimano, SRAM, Campagnolo o a medida). Nunca la pendiente de Zwift, el ajuste por marcha ni lo que se envía al Neo: eso va en Diagnóstico.
6. **Oscuro por defecto.** El tema `rodaje` es el principal: cuarto de entrenamiento, pantalla a brillo medio. El tema `claro` es para leer con luz de día. El usuario elige uno en Ajustes («Oscuro» o «Claro») y se aplica a toda la app, también al rodar, así que cada pantalla y componente tiene que funcionar en los dos.

## Voz y texto

- Español de España, tuteo, frases cortas. Mayúscula solo al inicio («Asignación de botones»).
- Los botones dicen lo que hacen, en infinitivo: «Buscar dispositivos», «Empezar a rodar», «Olvidar Neo».
- Los nombres de dispositivo como los ve el usuario: «Neo», «Square», «Zwift», «Controlador», «Controlador Zwift». Nunca UUID ni «FTMS» fuera de Diagnóstico.
- Botones del Square: △ ○ □ ✕ y ↑ ↓ ← → como iconos dibujados, A B Y Z como letra, y «Palanca D1», «Freno I», «Campag. D» (I = izquierda, D = derecha).
- Gestos: «pulsar», «corta», «larga», «doble», «mantener». Combinaciones con «+», el modificador primero: «Campag. D + Palanca D2».
- Números: coma decimal y espacio antes de la unidad (`1,5 %`, `247 W`, `91 rpm`, `75 kg`). Relaciones con el signo ×: `52×17`. Pendientes con signo, solo en Diagnóstico: `+6,2 %`.
- Errores: qué pasa y qué hacer. «El Neo no responde. Comprueba que está enchufado y pedalea para despertarlo.» Sin disculpas.
- Sin emojis ni exclamaciones.

## Color

- Fondo `bg`; paneles y filas de ajustes en `surface`; pulsado, pistas y teclas en `surface-2`.
- Texto en `ink`; etiquetas, unidades y ayudas en `ink-muted`. Ambos cumplen 4,5:1 sobre `bg`, `surface` y `surface-2` en los dos temas.
- `line` solo separa; los bordes de controles usan `line-strong` (≥3:1).
- `gear` como texto o marca; `gear-fill` como relleno, siempre con `on-gear-fill` encima (nunca blanco); `gear-soft` como fondo de avisos de marcha y de la etiqueta APP.
- `ok` y `danger` no se distinguen bien entre sí solo por luminosidad: van siempre con forma (punto lleno / anillo vacío) y palabra.
- Foco: anillo sólido `focus` de `focus-width` con 2dp de separación. Funciona con teclado y con la cruceta del Square en la capa de app.

## Tipografía

- **`numeric`** (Barlow Condensed): solo cifras y letras de botón (A B Y Z). `gear-hero` para la marcha en transmisión lineal y `gear-ratio-hero` para plato × piñón (uno de los dos, una vez por pantalla), `metric-xl` para potencia, `metric-lg` para cadencia y objetivo ERG, `metric-md` para relaciones y valores de steppers.
- **`sans`** (Barlow): toda la interfaz. `title` por pantalla de ajustes, `heading` por sección, `body` para texto, `label` para etiquetas de métricas (mayúsculas, `ink-muted`), `caption` para ayudas.
- **`mono`** (JetBrains Mono): solo en Diagnóstico.
- En Android, las tres familias se empaquetan en la app (licencia OFL). Los tamaños son sp y respetan el escalado de fuente del sistema salvo `gear-hero` y `metric-xl`, que ya son máximos.

## Espaciado y forma

- Base de 4dp. Ajustes: `space-4` de margen lateral y `space-6` entre secciones. Rodaje: `space-6` de margen lateral, `space-8` entre zonas y `space-12` alrededor de la marcha.
- Esquinas firmes: `radius-sm` para segmentos e insignias, `radius-md` para botones, paneles y teclas, `radius-full` solo para chips de conexión.
- Sin sombras. La jerarquía viene del tamaño de las cifras y del espacio, no de tarjetas.
- Objetivos táctiles: `touch-min` en ajustes y `touch-ride` en la pantalla de rodaje.

## Movimiento y háptica

- Cambio de marcha: el número entra 12dp en la dirección del cambio en 120 ms (ease-out). Con movimiento reducido, sin animación.
- Tope de marchas: el número pasa a `ink-muted` y aparece la etiqueta de tope 1,5 s.
- Vibración: corta (20 ms) en cada cambio; doble en tope; larga (60 ms) al reconocer una pulsación larga o una combinación. Se activa o desactiva en Ajustes > Al rodar > «Vibrar» (activada por defecto).
- Nada se mueve en reposo salvo el anillo de «conectando».

## Iconografía

- Iconos de Google (`material-design-icons`, estilo Rounded), peso 400, relleno 0, tamaño 24dp, en `ink` o `ink-muted`. Solo en ajustes y navegación, y en Rodaje en dos sitios: el acceso a Ajustes (`tune`) y la barra de conexiones (`ConnectionBar`); la hora, el wifi y la batería del móvil los pinta el sistema. Por lo demás, la pantalla de rodaje no usa iconos, solo cifras, palabras y `SquareKey`.
- Se sirven desde el sprite `icons.svg` (licencia Apache 2.0 en `icons-LICENSE.txt`), que solo contiene los iconos que usa la interfaz o su guía, con identificador `i-` y el nombre del icono: `<svg class="sq-icon" aria-hidden="true"><use href="../design-system/icons.svg#i-chevron_right"/></svg>`. El icono toma `currentColor`, así que su color sale del componente que lo contiene. La ruta del `href` es relativa a la página.
- Para usar un icono nuevo, copia su SVG de 24px al sprite como `<symbol id="i-nombre">` con el mismo `viewBox`. En la app, los mismos SVG se importan como vectores de Android.
- Un icono por enlace del móvil con un dispositivo, el mismo en Rodaje (`ConnectionBar`), Dispositivos y Diagnóstico. Lo que la app publica no es un enlace de la barra: su estado va en palabras con `PlatformBadge`, e `i-zwift` queda para Diagnóstico y para el controlador que publica la app:

  | Enlace | Icono |
  |---|---|
  | Neo | `i-bolt` |
  | Square | `i-manillar` (un manillar de bici de carretera: el Square es el controlador de la app) |
  | Pulso | `i-favorite` (solo en la guía, hasta que exista el enlace) |
  | Ventilador | `i-mode_fan` (solo en la guía, hasta que exista el enlace) |

  La navegación inferior usa `i-pedal_bike` (Rodar), `i-bluetooth` (Dispositivos), `i-sensors` (Conexión, de Material: una señal que se emite) e `i-tune` (Ajustes).

  `i-botones` (entrada «Botones» de Ajustes) también es propio: triángulo, cuadrado relleno, círculo y cruz en cuadrícula 2×2, de un solo trazado relleno, distinto del manillar del controlador.

  `i-manillar` (el Square, en Rodaje, Dispositivos y Diagnóstico) es propio también, no de Material: un manillar de bici de carretera con la barra, las dos bajadas que se curvan hacia dentro y el puente central, de un solo trazado relleno de 80 unidades de grosor. Tiene que leerse a 14 dp en la `ConnectionBar` en todos sus estados (el hueco de «esperando» recorta la esquina inferior derecha y la insignia de batería baja asoma por la superior izquierda). No se usa para otro enlace.

  `i-zwift` es propio del proyecto, no de Material ni con su licencia: una «Z» en un recuadro redondeado, de un solo trazado relleno, que no imita el logo de Zwift.
- Los botones del Square se dibujan con `SquareKey`: los símbolos y la cruceta con los iconos `i-tecla_*` (solo dentro de la tecla) y las letras en `numeric`.

## Pantallas

Las maquetas de `maquetas/` son la referencia de cada pantalla y de sus estados.

| Pantalla | Maqueta | Componentes |
|---|---|---|
| Rodaje (horizontal y vertical) | `rodaje.html`, `rodaje-vertical.html` | `RideScreen`, `GearDisplay`, `ChainringIndicator`, `CassetteView` o `GearLadder`, `MetricGrid`, `MetricTile`, `ModeBadge`, `ConnectionBar`, `PlatformBadge`, `ActionToast`, `ExitNotice`, `GuideSheet`, `GuideStep` |
| Conexión | `conexion.html` | `AppBar`, `SettingsList`, `ServiceRow`, `ModeList`, `ConnectionChip`, `Notice`, `BottomNav` |
| Dispositivos | `dispositivos.html` | `AppBar`, `SettingsList`, `ConnectionChip`, `Notice`, `Button`, `BottomNav` |
| Botones (desde Ajustes) y asignar acción | `botones.html`, `asignar.html` | `ButtonMappingRow`, `SquareKey`, `CapturePanel`, `Notice` |
| Marchas | `marchas.html` | `SettingsList`, `ChainringIndicator`, `CassetteView`, `GearLadder`, `Stepper` |
| Ajustes y datos en pantalla | `ajustes.html`, `datos.html` | `SettingsList`, `Stepper`, `MetricGrid` (vista previa) |
| Diagnóstico | `diagnostico.html` | `SquareKey`, `ActionToast`, `GradeReadout`, `HexLog`, `Stepper`, `Button` |

## Construir una maqueta

Una maqueta es una página HTML estática en `maquetas/` que solo usa el sistema de diseño. Para crear una:

1. En el `<head>`, enlaza `../design-system/tokens.css`, `../design-system/components.css` y la herramienta de revisión (`revision.css` y `revision.js`). Copia la cabecera de cualquier maqueta.
2. Declara los estados en `<body data-mock-states="id:Nombre, …">` (el primero es el que se ve al abrir) y marca cada bloque con `data-mock-state="id"` (admite varios separados por espacios). Si un estado solo cambia un atributo, usa `data-mock-attr="id:atributo=valor"`.
3. Envuelve la pantalla en `<div class="rv-stage"><div class="rv-device rv-device--h">` (horizontal) o `rv-device--v` (vertical).
4. Compón la pantalla con las clases `sq-` de los componentes (ver la tabla de Pantallas y las pautas de cada uno) y con las piezas de composición:
   - Pantallas de ajustes: `sq-screen` > `sq-screen__body`, con `AppBar` arriba y `BottomNav` al pie en las pantallas de pestaña.
   - `sq-row` (fila que se ajusta) y `sq-col` (columna), separadas por `space-3`. Modificadores: `sq-row--between`, `sq-row--start`, `sq-row--end` y `sq-gap-1`, `-2`, `-3`, `-4`, `-6`, `-8`, `-12` para otra separación.
   - `sq-grid` (2 columnas iguales) y `sq-grid--4` (4), separadas por `space-2`.
   - `sq-actions` (botones al pie, en columnas iguales; `sq-actions--stack` los apila) y `sq-spacer` (empuja lo siguiente al final).
   - `sq-label`, `sq-caption` y `sq-mono` para los estilos de texto `label`, `caption` y `mono`.
   - En las vistas previas de componentes, `sq-w-phone` (390 dp) y `sq-w-panel` (640 dp) limitan el ancho de la demo.
5. Añade la pantalla y sus estados a `maquetas/index.html`, con sus funcionalidades del documento funcional.

Sin hojas ni atributos `style` propios: si una pantalla necesita algo que el sistema no tiene, se añade antes a `components.css` y a las pautas del componente. La única excepción son las variables que el sistema documenta como dato, como `--cog` en `CassetteView`. Los datos son ficticios; lo pendiente de capturar con el equipo real va entre corchetes (`[MÁSCARA EN HEX]`).

## Ficheros

- `tokens.json`: fuente de los tokens. `tokens.css` y `styleguide/index.html` se generan con `python3 scripts/design-system/generar.py`; no se editan a mano.
- El generador termina comprobando el contraste de los tokens en los dos temas (`scripts/design-system/contraste.py`, que también se puede ejecutar solo): texto a 4,5:1 y bordes de controles a 3:1. Si algún par no llega, indica cuál y con qué ratio, y sale con error. Al añadir un token de color, añade a `contraste.py` los fondos sobre los que se usa.
- `components.css`: estilos de referencia de todos los componentes (prefijo `sq-`). Declara las fuentes locales de `fonts/` (woff2, subconjunto latino, con sus licencias OFL); no hay peticiones externas.
- `icons.svg`: sprite de iconos (ver Iconografía).
- `components/<Componente>/`: pautas (`README.md`) y vista previa (`preview.html`) de cada componente.

## En Jetpack Compose

- Cada token de color es un campo de una `data class SquareColors`, con un valor por tema, expuesta con `CompositionLocal`. El `ColorScheme` de Material 3 se rellena a partir de ella (`primary` = `gear-fill`, `onPrimary` = `on-gear-fill`, `background` = `bg`, `surface` = `surface`, `outline` = `line-strong`, `error` = `danger`).
- Los estilos de texto son `TextStyle` con el mismo nombre (`gearHero`, `metricXl`…), con `fontFeatureSettings = "tnum"` en la familia `numeric`.
- Los espaciados y radios son constantes `Dp` con el mismo nombre (`Space4 = 16.dp`, `RadiusMd = 8.dp`).
- Cada componente de este sistema es un composable con el mismo nombre.
