# Proposal

## Why

El icono actual (una piñonera con el piñón activo en ámbar) se diseñó cuando la app era «Square Shift» y solo cambiaba marchas. Desde el 18 el proyecto es «Smart Bike»: una bici sobre un rodillo inteligente que habla con Zwift. La piñonera sigue siendo válida como símbolo del cambio, pero no dice «bici» ni «rodillo». El usuario propone una bici de perfil cuya rueda trasera, en ámbar, representa el rodillo (es la rueda que el Neo frena y mueve), y una splash en la que esa rueda gira acelerando.

Ninguna funcionalidad de `docs/funcional.md` cambia: es identidad visual, sin porción vertical con el equipo real. Se valida en el OnePlus 8 viendo el icono (con máscara y con iconos temáticos) y el arranque.

## What Changes

- **Icono de la app.** Una bici de perfil sobre el fondo `bg` de Rodaje: cuadro en `ink`, rueda delantera en `line-strong` y rueda trasera en `gear-fill` (ámbar), que representa el rodillo, con un tambor gris debajo. Sillín a la izquierda y manillar a la derecha. La rueda trasera lleva radios o una marca para que el giro de la splash se vea. Sigue siendo adaptativo, entero con cualquier máscara y el mismo con el tema claro.
- **Tres alternativas en la maqueta** (borradores en `borradores/`): D1, rueda trasera como anillo ámbar; D2, como disco ámbar macizo con hueco en el buje; D3, como anillo ámbar con un tambor gris debajo. **El usuario eligió D3** al validar la maqueta (28-10-2026): el tambor le da el toque «indoor». D1 y D2 se eliminaron del repositorio.
- **Capa monocroma (Android 13 o posterior).** Un solo color: la rueda trasera se distingue de la delantera por la forma (rellena o con radios frente a un anillo fino), no por el color. Se prueba con cada alternativa.
- **Splash animada.** El mismo icono sobre el fondo oscuro de Rodaje; en Android 12 o posterior la rueda trasera gira acelerando, de lento a rápido, durante como mucho 1 s y termina en una posición equivalente a la del icono. En versiones anteriores, el icono se ve quieto. Sigue oscura con el tema «Claro».
- **Sistema de diseño y guía.** Los componentes `AppIcon` y `Splash` pasan a la bici: máscaras, zona segura, 48 dp, monocromo y la animación del giro (con `prefers-reduced-motion`, solo el estado final). Se añaden a `contraste.py` los pares del icono nuevo.
- **Maqueta.** `maquetas/identidad.html` muestra la bici elegida en cada estado (`icono`, `monocromo`, `splash`); el estado `botones` no cambia. Durante la validación mostró las tres alternativas.
- **Generador.** `generar.py` deja de entender solo rectángulos redondeados: lee círculos y trazos de la fuente y emite el vector del icono, el monocromo y la `animated-vector` de la splash con un `group` que rota. `IdentidadSincronizadaTest` sigue comprobando los recursos.
- **Web pública.** `scripts/sitio/generar.py` toma el logo y el icono de pestaña de la misma fuente; se regenera `site/`.
- **App.** No cambian el manifiesto, el tema de arranque ni `MainActivity`: solo se regeneran los recursos `identidad_*`.

## Capabilities

### New Capabilities

(ninguna)

### Modified Capabilities
- `app-identity`: el icono pasa de piñonera a bici con rueda trasera ámbar; el monocromo y la legibilidad se refieren a esa rueda; la splash gira la rueda trasera acelerando en vez de saltar de barra en barra.
- `style-guide`: la guía muestra el icono nuevo y la splash con el giro.
- `screen-mockups`: la maqueta de identidad muestra el icono, el monocromo y la splash.

## Impact

- **Código y recursos:** recursos generados `res/drawable/identidad_*.xml` y, si cambian, `res/mipmap-anydpi/ic_launcher*.xml`; `scripts/design-system/generar.py` y `contraste.py`; `scripts/sitio/generar.py` y `site/sitio.css`; `IdentidadSincronizadaTest` solo si cambia su contrato. Sin dependencias nuevas.
- **Sistema de diseño:** `design-system/identidad/` (fuentes del icono y del monocromo), `components.css`, componentes `AppIcon` y `Splash`, `styleguide/index.html` regenerada.
- **Maquetas:** `maquetas/identidad.html` y `maquetas/index.html`.
- **Documentos de `docs/`:** `planificacion.md` (fila 22 «En curso» y nota) y `arquitectura.md` (los recursos de identidad y la animación). `funcional.md` y `desarrollo.md`: ninguno.
- **Coordinación:** `completar-protocolo-dircon-zwift` (13e) está en curso y toca `MainActivity`; este change no la modifica, así que no hay conflicto previsto.

## Out of scope

- Icono de la notificación del servicio de rodaje (`directions_bike`) y del icono «Botones».
- Cambiar el nombre, los textos o la web más allá de regenerar el logo y el icono de pestaña.
- Una pantalla de bienvenida o de carga propia, o animar el icono dentro de la app.
- Un logotipo con texto o un icono de la organización `indoor-cycling-lab`.
- Un icono de tienda (Play Store) o de varias densidades en PNG: sigue siendo un vector adaptativo.
