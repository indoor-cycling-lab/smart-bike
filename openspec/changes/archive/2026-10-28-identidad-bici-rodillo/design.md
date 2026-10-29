# Design

## Context

La identidad actual (change `identidad-app`) sale de `design-system/identidad/icono-app.svg`: siete `<rect rx>` con las clases `fondo`, `barra` y `barra-activa`. `generar.py` solo sabe leer rectángulos (`leer_barras`), comprobar que caben en la zona segura (círculo de 66 dp en el lienzo de 108), escribirlos como `<path>` de vector y componer una `animated-vector` que anima `fillColor` de cuatro barras (900 ms, estado base el final). La splash usa `androidx.core:splashscreen` con `postSplashScreenTheme`, y la web (`scripts/sitio/generar.py`) también lee las barras de la misma fuente para el logo y el icono de pestaña.

La bici nueva no son rectángulos: son círculos (ruedas, buje), trazos (cuadro, manillar, sillín, radios) y, en una alternativa, un rectángulo redondeado (tambor). Ver `proposal.md` para el porqué y los borradores.

## Goals / Non-Goals

**Goals:**
- Una sola fuente por alternativa, de la que salgan la guía, el icono adaptativo, el monocromo y la animación.
- Que la rueda trasera gire acelerando en la splash, de forma legible, en como mucho 1 s.
- Elegir entre las tres alternativas con la maqueta y los recursos reales, sin dejar código muerto de las descartadas.

**Non-Goals:**
- Un convertidor general de SVG: el generador admite solo `rect`, `circle` y `path` con órdenes absolutas `M` y `L` (más el círculo como dos arcos), que es lo que usa la fuente.
- Cambiar el tema de arranque, `MainActivity` o la dependencia de la splash.

## Decisions

### 1. Fuentes: un SVG y clases por parte de la bici

`design-system/identidad/icono-app.svg` (lienzo 108 × 108, sin colores) y su capa monocroma `icono-app-monocromo.svg`. Clases: `fondo`, `cuadro` (ink), `rueda-delantera` (line-strong), `rueda-trasera` (gear-fill), `radio` y `buje` (gear-fill) y `tambor` (line-strong). Los colores los ponen los tokens (`ink`, `line-strong`, `gear-fill`, `bg`), como antes; no hay token nuevo.

Durante la validación de la maqueta hubo tres fuentes (D1, D2 y D3, con `generar.py --alternativa`). El usuario eligió **D3** (anillo ámbar con radios y tambor gris): se renombró a `icono-app.svg` y se borraron las otras cinco fuentes y la opción `--alternativa`. La piñonera y sus variantes monocromas A y B también se eliminaron.

### 2. Generador: formas en vez de barras

`leer_barras` pasa a `leer_formas`: devuelve una lista de dicts (`clase`, `tipo`, geometría, `trazo`). Un `circle` se emite como `pathData` de dos arcos; un `path` se copia (solo `M`/`L` absolutos, con `stroke-linecap` y `stroke-linejoin` redondos); un `rect`, como ahora. Los trazos se emiten con `strokeColor`, `strokeWidth` y `strokeLineCap="round"`. `comprobar_zona_segura` calcula, por forma, el punto más lejano del centro (círculo: centro + radio + trazo/2; trazo: extremos + trazo/2) y falla si pasa de 33 dp, como hoy.

El archivo del vector estático (`identidad_icono_primer_plano.xml`) y el de la splash (`identidad_splash_vector.xml`) se generan de la misma lista; en el segundo, la rueda trasera (círculo, radios y buje) va dentro de un `<group android:name="rueda" android:pivotX android:pivotY>` con el centro de la rueda como pivote. Si D3, el tambor queda fuera del grupo.

### 3. Splash: giro por `rotation` con aceleración

La `animated-vector` anima `rotation` del grupo `rueda`: `valueFrom` 0, `valueTo` 720 (dos vueltas), duración 900 ms (máximo 1000) y el interpolador de aceleración del sistema (`@android:interpolator/accelerate_quad`). Termina en un múltiplo de 360° y el estado base del grupo es 0°, así que el último fotograma coincide con el icono quieto (y es lo que se ve donde el sistema no anima). Se anima la rotación del grupo, no `pathData`, para que no haya riesgo de interpolación de trazados.

Velocidad angular al final: 2 × 720° / 0,9 s = 1.600°/s, es decir, unos 27° por fotograma a 60 Hz (menos a 90 Hz, como el OnePlus 8). Con seis radios (cada 60°) el paso por fotograma queda por debajo de la mitad de la separación (30°), así que no aparece el efecto de la rueda de carro (que parezca parar o ir hacia atrás). Si en la maqueta o en el móvil se nota, se reduce a 540° (una vuelta y media más una marca asimétrica) o se baja el número de radios. Con D3 la rueda trasera lleva seis radios simétricos.

El icono de la splash de Android 12 se recorta en un círculo (2/3 del lienzo para iconos sin fondo); como las formas ya caben en el círculo de 66 dp, no hace falta más margen. La splash sigue siendo oscura, también con el tema «Claro».

### 4. Monocromo: relleno o radios frente a anillo fino

La capa monocroma pinta todo del mismo color, así que la rueda trasera se distingue por la forma. Para cada alternativa: la trasera con relleno (D2), o con radios y un anillo más grueso (D1, D3), y la delantera como anillo fino sin radios. El cuadro conserva el trazo. Se prueba en la maqueta con la máscara y a 48 dp antes de elegir.

### 5. Contraste

Pares nuevos en `contraste.py`, todos con 3:1 como mínimo (elementos gráficos) y en el tema rodaje, porque el icono es siempre oscuro: `rueda-trasera` (gear-fill) sobre `bg` y sobre `line-strong`; `cuadro` (ink) sobre `bg` y sobre `line-strong`; la rueda delantera y el tambor (line-strong) sobre `bg` ya estaban. Se confirmó el problema: `ink` sobre ámbar da 1,6:1, muy por debajo de 3:1. Se resuelve por geometría: el cuadro termina a 16,5 dp del buje trasero, antes del borde de la rueda, y deja un hueco del color de `bg` (el cuadro y la rueda trasera no se tocan). Un halo del color de `bg` alrededor del cuadro se probó y se descartó: parte el anillo ámbar y se ve peor. El par `ink` sobre `line-strong` (4,95:1) cubre el cuadro sobre la rueda delantera. No se baja el mínimo.

### 6. Web pública

`scripts/sitio/generar.py` copiaba solo `rect.barra` y `rect.barra-activa`. Pasa a copiar el cuerpo del SVG elegido entero (formas con sus clases) y `site/sitio.css` y el icono de pestaña definen los colores por clase con tokens, igual que la guía. El generador del sitio falla si encuentra una clase sin color. Se regenera `site/` y las capturas (`capturas.py`).

### 7. Guía y maqueta

`AppIcon` y `Splash` se reescriben para la bici: las clases `sq-appicon__*` pasan a las partes de la bici, `sq-appmask` queda igual, y `sq-splash--anim` anima `transform: rotate()` con `ease-in` (`cubic-bezier` equivalente a la aceleración cuadrática) durante 0,9 s en un ciclo de 3 s, con `prefers-reduced-motion` solo el estado final. `maquetas/identidad.html` repite cada estado con las tres alternativas, con una etiqueta (D1, D2, D3) para elegir.

## Risks / Trade-offs

- [Efecto rueda de carro en el giro] → seis radios o marca asimétrica, y velocidad final por debajo de 30° por fotograma; se comprueba en la maqueta y en el OnePlus 8, y la spec lo exige («Giro legible»).
- [Ámbar sobre ink no llega a 3:1 en D2] → decisión 5; el generador falla si no pasa.
- [A 48 dp la bici se vuelve borrosa] → la maqueta lo muestra; si no se reconoce, se engrosan los trazos antes de elegir, no después.
- [El generador crece] → solo `rect`, `circle` y `path` con `M`/`L`; no es un convertidor de SVG.
- [El launcher cachea el icono] → desinstalar e instalar para probar (ya en `docs/desarrollo.md`).
- [La splash termina a máxima velocidad y Rodaje aparece de golpe] → es lo que pide el usuario («de menos a más rápido»); si queda brusco en el móvil, se alarga el último tramo con otro interpolador sin pasar de 1 s.

## Migration Plan

Sin migración de datos. Para desplegar: elegir la alternativa, `python3 scripts/design-system/generar.py`, compilar, desinstalar e instalar en el móvil. Revertir es volver al commit anterior (piñonera y salto de barras).

## Open Questions

- Número de vueltas de la splash (dos) y brusquedad del final a máxima velocidad: se comprueban en el OnePlus 8 (tarea 2.4).
