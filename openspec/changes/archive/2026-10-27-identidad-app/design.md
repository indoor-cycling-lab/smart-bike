# Design

## Context

Hoy la app no declara icono en el manifiesto, el tema de arranque (`Theme.SquareShift`, padre `android:Theme.Material.NoActionBar`) solo pone `windowBackground` con `@color/ventana_bg` (el `bg` de Rodaje, generado) y la pestaña Botones usa `ic_sports_esports`, el mismo recurso que `ConnectionBar` da al mando de Zwift (`Enlace.Mando`). `minSdk` es 28, `compileSdk` 37 y `targetSdk` 36.

Los derivados de la app salen de `scripts/design-system/generar.py` y se comprueban con tests de sincronización (`TokensSincronizadosTest`, `IconosSincronizadosTest`). `escribir_iconos` borra de `res/drawable/` los `ic_*.xml` generados que ya no están en `icons.svg`, y `leer_iconos` exige un solo `<path>` por icono con `viewBox="0 -960 960 960"`. Los componentes del sistema de diseño son carpetas con `README.md` y `preview.html` que el generador vuelca en la guía.

Ver `proposal.md` para el porqué y la lista de cambios; los requisitos están en `specs/`.

## Goals / Non-Goals

**Goals:**
- Una sola fuente para el dibujo de la piñonera: de ella salen la guía, el icono adaptativo, la capa monocroma y la animación de la splash.
- Que el icono quepa en cualquier máscara de Android sin cortar barras.
- Que la splash no añada tiempo de arranque.

**Non-Goals:**
- Un editor de vectores ni una conversión general de SVG: el generador solo admite la geometría de esta fuente (rectángulos redondeados).
- Cambiar el icono de la notificación del servicio (`directions_bike`).
- Mostrar la identidad dentro de la app una vez arrancada.

## Decisions

### 1. Fuente del icono: SVG de geometría, colores por clase

La fuente es `design-system/identidad/icono-app.svg`: lienzo de 108 × 108 (el del icono adaptativo) con siete `<rect rx>` y las clases `fondo`, `barra` y `barra-activa`. No lleva colores: el generador los toma de los tokens (`bg`, `line-strong`, `gear-fill`), y en la guía los ponen las clases `sq-appicon__*` de `components.css`. Así un cambio de color de marca sale de `tokens.json` y no hay hexadecimales duplicados.

Alternativas: un JSON con las medidas (más fácil de generar, pero no se ve en un editor ni en el navegador sin pasar por el generador); o convertir cualquier SVG a `pathData` (más general, pero obliga a un analizador de trazados que aquí no hace falta). Se descartan los dos.

El generador comprueba además que cada barra cabe en el círculo de la **zona segura** (66 dp de diámetro en el lienzo de 108) y falla si no, como falla el contraste.

### 2. Recursos generados con el prefijo `identidad_`

El generador escribe en `app/src/main/res/`:
- `drawable/identidad_icono_primer_plano.xml` y `drawable/identidad_icono_monocromo.xml` (vectores de 108 dp).
- `drawable/identidad_splash.xml` (`animated-vector`) y `drawable/identidad_splash_vector.xml` (su vector).
- `mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml` (`adaptive-icon` con `@color/ventana_bg` de fondo, el primer plano y el monocromo). Con `minSdk` 28 no hacen falta PNG ni la carpeta `-v26`.

El prefijo `identidad_` evita que `escribir_iconos` los borre: su limpieza solo mira `res/drawable/ic_*.xml`. Los XML de `mipmap-anydpi` quedan fuera de ese patrón. El fondo adaptativo y el de la splash reutilizan `ventana_bg`, que ya sale de `bg`; no hay color nuevo ni token nuevo.

`IdentidadSincronizadaTest` (junto a `IconosSincronizadosTest`) regenera en memoria los recursos desde la fuente y falla, con el nombre del recurso y el comando, si alguno del repositorio difiere.

### 3. Dibujo: la piñonera cabe en el círculo seguro

El primer boceto (siete barras de hasta 300 de 512 de alto y 280 de ancho) se sale del círculo seguro: la esquina de la barra mayor queda a unos 41 dp del centro y el círculo visible llega a 36 dp (33 dp la zona segura). Hay que escalarlo en torno a 0,8 o acortar las barras mayores. Se decide al hacer la maqueta mirando las máscaras (círculo, cuadrado redondeado, con la zona segura dibujada), y la comprobación del generador lo hace cumplir. La barra activa es la cuarta de siete, como en el boceto: es el piñón del medio, ni el más duro ni el más ligero, y sirve de posición final de la animación.

### 4. Capa monocroma: la forma distingue el piñón activo

Android tiñe la capa monocroma con un solo color e ignora los colores del dibujo. El piñón activo se distingue por la forma: se prueban en la maqueta dos variantes (barras inactivas más estrechas que la activa, o barras inactivas solo en contorno) y el usuario elige. La variante elegida se escribe en la fuente como un segundo conjunto de geometría (`icono-app-monocromo.svg`).

### 5. Splash: `androidx.core:splashscreen`, animación con el estado final como base

- Tema `Theme.SquareShift.Splash` con padre `Theme.SplashScreen`: `windowSplashScreenBackground` = `@color/ventana_bg`, `windowSplashScreenAnimatedIcon` = `@drawable/identidad_splash`, `windowSplashScreenAnimationDuration` = 900 ms (máximo 1000 ms) y `postSplashScreenTheme` = `Theme.SquareShift`. `MainActivity` llama a `installSplashScreen()` antes de `super.onCreate`, sin `setKeepOnScreenCondition`: la splash se va en cuanto Rodaje está listo (spec «Arranque sin retraso añadido»).
- La splash es siempre oscura, también con el tema «Claro»: es la ventana anterior a que se lea la configuración, como ya se aceptó en el change `acceso-ajustes-y-tema`.
- **El estado base del vector es el final** (cuarta barra en ámbar, el resto en gris). Donde el sistema no anima (Android 9 a 11, animaciones desactivadas), se ve ese estado. La animación empieza con otro estado: cada barra anima `fillColor` con fotogramas clave de la duración total, sin `startOffset`, porque durante un retardo se vería el estado base. Primera barra en ámbar → segunda → tercera → cuarta, unos 150 ms cada una, y el resto del tiempo quieto.
- Un solo `animated-vector` generado desde la misma geometría.

Alternativas: una pantalla propia en Compose tras la splash del sistema (permite textos y más animación, pero se ve como dos splash seguidas y retrasa Rodaje); o solo `windowBackground` y una capa con el icono (`layer-list`), sin dependencia (en Android 12+ el sistema la ignora por su splash, y entre 9 y 11 funciona). Se elige la biblioteca: es la forma estándar de tener lo mismo en todas las versiones con un solo tema, a costa de una dependencia pequeña de AndroidX. La versión se fija al implementar, la estable vigente compatible con `compileSdk` 37. Si diera problemas, la alternativa de la `layer-list` queda como plan B.

### 6. Icono `i-botones`

Se añade a `icons.svg` como `i-botones` (un solo `<path>` relleno, `viewBox="0 -960 960 960"`, como `i-zwift`): triángulo y círculo en anillo de unas 70 unidades de grosor, cruz con extremos redondeados y el cuadrado relleno, en cuadrícula 2×2. Los trazos del boceto (`icono-botones-b.svg`) se convierten a contornos rellenos a mano con arcos y segmentos, con los anillos como subtrayectos de sentido contrario, sin depender de ninguna herramienta de vectores. Se comprueba superponiendo el resultado sobre el boceto.

`leer_iconos` lo recoge solo y emite `ic_botones.xml`. `Navegacion.kt` pasa a usarlo para la pestaña Botones; `ConnectionBar` conserva `ic_sports_esports` para el mando. No se toca `i-sports_esports`.

### 7. Guía y maqueta

Dos componentes nuevos, `AppIcon` y `Splash` (estilos `sq-appicon` y `sq-splash` en `components.css`, `README.md` y `preview.html`):
- `AppIcon`: el icono con máscara circular y de cuadrado redondeado (con `clip-path` / `border-radius`), con la zona segura marcada, a 48 dp y la variante monocroma en un color neutro.
- `Splash`: la secuencia con una animación CSS por pasos con `animation-fill-mode: forwards` y, con `prefers-reduced-motion`, solo el estado final.

El icono es el mismo en los dos temas, y la guía lo dice. La maqueta `identidad.html` tiene los estados `icono`, `monocromo`, `splash` y `botones` (la barra inferior), usa los componentes y no define estilos propios.

### 8. Contraste

Se añaden a `contraste.py` los pares del icono: `barra` (`line-strong`) sobre `bg` y `barra-activa` (`gear-fill`) sobre `bg` y sobre `line-strong`, con un mínimo de 3:1 (elementos gráficos). Si el ámbar sobre el gris no llega, se ajusta la barra inactiva con otro token existente antes de seguir.

## Risks / Trade-offs

- [El launcher guarda el icono anterior en caché] → al probar, desinstalar y volver a instalar, o reiniciar el launcher; no se da por mala la prueba sin hacerlo.
- [La animación de la splash solo se ve en Android 12 o posterior, y los iconos temáticos en Android 13 o posterior] → es un adorno: el estado base es el icono. El OnePlus 8 de pruebas tiene Android 13 (`docs/desarrollo.md`), así que se ven los dos; el arranque sin animación (Android 9 a 11) no se puede probar en ese móvil y queda cubierto por el estado base y la guía.
- [El icono se ve pequeño tras escalarlo para la zona segura] → la maqueta lo muestra a 48 dp; si queda flojo, se acortan las barras mayores en lugar de escalar todo.
- [El ámbar sobre el gris de las barras inactivas contrasta poco] → comprobación de `contraste.py`; ver decisión 8.
- [El generador crece] → solo gestiona rectángulos redondeados y un `animated-vector` fijo; no es un convertidor de SVG.
- [Una nueva dependencia] → pequeña, de AndroidX, y con plan B sin ella (decisión 5).

## Migration Plan

Sin migración de datos: no cambia `configuracion.json` ni ningún comportamiento. Para desplegar, `generar.py` y compilar; en el móvil, reinstalar para refrescar el icono. Revertir es volver al commit anterior, con el icono genérico y la ventana oscura de siempre.

## Open Questions

- Variante monocroma (barras inactivas más estrechas o en contorno): la elige el usuario al validar la maqueta (decisión 4).
- Cuánto escalar o acortar el dibujo para la zona segura: se decide al verlo con las máscaras (decisión 3).
- Si más adelante se quiere el icono de la app en la notificación del servicio: fuera de este change.
