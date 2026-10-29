# Design

## Context

Ver `proposal.md` para el motivo. Estado actual, medido el 28-10-2026 en la maqueta (`maquetas/rodaje-vertical.html`, estado `botones-6`, ventana de 360 × 640) y confirmado en el OnePlus 8 con `RodajeTest`:

- En vertical, `Rodaje` apila la franja (48), la fila de plataforma y Ajustes, el modo, la marcha (flexible), el cassette o la escalera (56), la rejilla de datos, el pie (40), y los botones − y + (72), con 16 dp entre bloques. La rejilla ocupa el 40 % del alto útil, y los botones se llevan primero lo que le sobra sin bajar su cifra; lo que falta lo pierde la marcha (`rejilla-datos-fija`).
- A 360 × 640 con 6 datos y botones, la cifra de los datos queda en 50 px, la rejilla en 220 dp y la marcha en 0 dp: escala 0,01 en la maqueta y `assertIsDisplayed` falla en la app.
- `maquetas/rodaje.js` calcula el reparto con el mismo algoritmo que la app (área fija, cifra, cesión a los botones, escala de la marcha). Tanto la maqueta como `Rodaje.kt` miden el alto de lo que se les da, no el de la pantalla del móvil.
- Mediciones con la marcha 48 × 19 (SRAM) y el modo compacto de abajo: **4 datos con botones, 0,72; 6 datos sin botones, 0,72**; sin el modo compacto, 0,36 en los dos casos. Con 6 datos y botones, 0,65 solo si la rejilla encoge la cifra a 36 px (descartado, decisión 3).
- El prototipo de las pruebas está en `maquetas/_tmp-baja.html` y `maquetas/_tmp-rodaje.js` (sin commitear); sirven de punto de partida y se borran.

## Goals / Non-Goals

**Goals:**

- Que en vertical, con la pantalla baja, la marcha se vea siempre con una escala razonable (≥ 0,5; medido 0,72).
- Que la regla se pueda explicar al usuario en una frase: «Con la pantalla baja y los botones, 4 datos».
- Que la maqueta, `RideScreen` y la app compartan el mismo umbral y los mismos tamaños.

**Non-Goals:**

- Un diseño nuevo de Rodaje ni cambios en horizontal.
- Tocar el algoritmo del 40 %, la cifra estable ni la cesión a los botones, que siguen igual.

## Decisions

### 1. Umbral: el alto de Rodaje, menor de 760 dp

La «pantalla baja» es Rodaje en vertical con `maxHeight < 760.dp` (en la maqueta, la altura del `.sq-ride`). Es el alto del propio composable, no un tipo de móvil, así que también cubre ventanas en pantalla dividida o móviles con un alto pequeño. 760 dp sale de medir en la maqueta la marcha con 6 datos y botones **sin** la regla: 0,25 a 700 dp, 0,35 a 720, 0,68 a 780 y 0,82 a 844, es decir, solo llega a la mitad hacia los 750 dp. Con la regla, de 640 a 799 dp la marcha queda entre 0,61 y 0,82. 760 cubre los móviles de 640 a 740 dp (360 × 640, 360 × 720, 360 × 740) y deja fuera los de 760 dp o más, incluido el OnePlus 8, que en inmersivo mide 764 dp (medido): sin la regla su marcha con 6 datos y botones queda hacia 0,6. Un primer umbral de 700 dp dejaba la marcha a 0,25-0,35 en los de 720 dp.

Alternativa descartada: una clase por el modelo o por el ancho. El problema es el alto, y el ancho no lo determina.

### 2. Modo compacto: dos tamaños, no una disposición nueva

Con la pantalla baja: espacio entre bloques de `space-4` (16) a `space-2` (8) y cassette de 56 a 40 dp (la escalera del modo lineal mide 28 dp y no cambia). No cambia el orden, ni quita elementos, ni cambia los botones (`touch-ride`, 72 dp), la franja ni las cifras. En `design-system/` es un modificador de `RideScreen` (`sq-ride--baja`) y no un componente nuevo, y `rodaje.js` lo pone al medir. Con estos dos cambios la marcha pasa de 0,36 a 0,72 (medido), con cualquiera de las dos combinaciones permitidas.

Alternativas descartadas:
- Fundir el modo y la fila de plataforma en una línea: el texto de la plataforma («Esperando conexión a Zwift…») es largo y la fila perdería su claridad.
- Quitar la reserva del pie (40 dp): el aviso de acción desplazaría la rejilla o taparía el modo cada vez que aparece.

### 3. Límite de datos en lugar de encoger la cifra

Con la pantalla baja y los botones activados, se muestran como máximo los 4 primeros datos. Es el límite que decidió el usuario. Se probó el otro camino, que la rejilla encoja su cifra hasta 36 px para que la marcha llegue a 0,65 con 6 datos y botones; da una marcha de tamaño parecido pero cifras mucho más pequeñas, y rompe la regla de `rejilla-datos-fija` de que la cifra no cambia con los botones. Quitar datos mantiene la cifra y deja la regla sencilla.

El recorte es **solo de visualización**: `datosVisibles` (en `RodajeUiState`) no cambia, y Rodaje corta a 4 al pintar, con el alto del composable que ya tiene. Así la configuración elegida no se pierde y no hay que tocar `DataStore` ni `Sesion`. En ERG la potencia objetivo ocupa el lugar de la marcha y la regla es la misma.

Alternativa descartada: impedir en Ajustes la combinación de botones y más de 4 datos. Bloquea lo que el usuario ha pedido, y una configuración existente con 6 datos y botones tendría que migrarse. El usuario eligió avisar, no bloquear.

### 4. El aviso en «Datos en pantalla»

La pantalla «Datos en pantalla» (`ui/datos/`) muestra un `Notice` cuando se cumple la regla (pantalla baja, botones activados, más de 4 datos), con el texto de los datos que no se ven. La condición es una función pura compartida con Rodaje: `recorteDatos(alto, botones, datos)`, que devuelve los datos visibles y los ocultos; la pantalla usa los ocultos, Rodaje los visibles. Se prueba en unitario sin Compose.

«Pantalla baja» en esa pantalla se calcula con el alto de la ventana (`LocalConfiguration.screenHeightDp`), que en Rodaje, sin barra inferior, coincide con su alto. La vista previa de la pantalla sigue mostrando todos los datos elegidos (es la lista que se edita), y el aviso explica la diferencia. Medido en el OnePlus 8 en inmersivo (28-10-2026): `screenHeightDp` 764 y alto de Rodaje 764,3 dp; difieren 0,3 dp, así que basta la ventana.

### 5. Maqueta: un tamaño de pantalla baja

Las maquetas de Rodaje solo existen a 390 × 844. Se añade el tamaño 360 × 640 (`rv-device--baja`) y estados que lo usan: `baja-botones-4`, `baja-datos-6` y `baja-botones-6-recorte` (6 datos elegidos, se ven 4). `datos.html` gana un estado con el aviso. `rodaje.js` toma la clase `sq-ride--baja` de la altura del `.sq-ride`, como hará la app. La maqueta de «Datos en pantalla» ya tiene un contador; se le añade el aviso con el componente `Notice`, sin color como único portador.

### 6. Medidas de la maqueta (28-10-2026)

Escala de la marcha (`data-reparto`) en `rodaje-vertical.html` con la regla aplicada:

| Alto | 4 datos con botones, SRAM 48 × 19 | 4 datos con botones, Shimano 52 × 17 | 6 datos sin botones |
|---|---|---|---|
| 640 dp | 0,71 | 0,61 | 0,72 |
| 680 dp | 0,75 | 0,73 | 0,75 |
| 699 dp | 0,72 | 0,72 | 0,75 |

Sin la regla, 6 datos con botones: 700 dp, 0,25; 720 dp, 0,35; 780 dp, 0,68; 844 dp, 0,82. Con el umbral de 760 dp, 6 datos con botones y la regla: 0,79-0,82 de 720 a 799 dp (con 4 datos).

## Risks / Trade-offs

- **[El recorte oculta datos que el usuario eligió]** → Solo ocurre con la pantalla baja y los botones; el aviso lo dice, y desactivar los botones los devuelve. No se pierde nada de la configuración.
- **[760 dp no es el límite exacto de lo que cabe]** → Medido: sin la regla la marcha llega a 0,5 hacia los 750 dp, y con ella queda en ≥ 0,61 desde 640 hasta 759 dp. Si la validación o un móvil real piden otro corte, es una constante en la maqueta y otra en la app.
- **[La regla solo se ve en la app con una ventana baja]** → El OnePlus 8 no la dispara. La cubren los tests de instrumentación (`requiredSize(360.dp, 640.dp)`) y la maqueta; no hay otro móvil en las pruebas.
- **[Diferencia entre el alto de Rodaje y `screenHeightDp`]** → Decisión 4; se mide al aplicar.

## Migration Plan

Sin migración: no cambia ningún dato guardado. Marcha atrás: revertir el commit; la configuración de datos y botones no se ha tocado.

## Open Questions

- ¿Conviene que el aviso también salga en Ajustes, junto a «Botones − y + en pantalla»? Se deja fuera hasta ver si el aviso en «Datos en pantalla» basta.
