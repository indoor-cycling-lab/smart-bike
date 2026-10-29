# Proposal

## Why

La app no tiene identidad propia: en el móvil se ve con el icono genérico de Android, el arranque es la ventana oscura de `windowBackground` sin ningún dibujo, y el icono de la pestaña «Botones» (`sports_esports`, un mando de consola) es el mismo que usa la barra de conexiones para el mando de Zwift, así que dos cosas distintas se confunden. Conviene resolver las tres a la vez porque comparten dibujo, colores y proceso de validación, y porque ahora que hay una organización (`indoor-cycling-lab`) con su propio icono, cada app debe reconocerse por sí misma en el launcher.

Ninguna funcionalidad de `docs/funcional.md` cambia: es identidad visual, sin porción vertical con el equipo real. Se valida en el OnePlus 8 viendo el icono, el arranque y la barra inferior.

## What Changes

- **Icono de la app (launcher).** Una piñonera vista de perfil: siete barras redondeadas que se estrechan, con el piñón activo en `gear` (ámbar) y el resto en `line-strong`, sobre el fondo `bg` de Rodaje. Icono adaptativo de Android (primer plano y fondo por separado) y **capa monocroma** para los iconos temáticos de Android 13+, donde el piñón activo se distingue por la forma y no por el color. Se declara en el manifiesto (`icon` y `roundIcon`).
- **Splash animada.** El arranque muestra el mismo icono sobre el fondo de Rodaje y, en Android 12 o posterior, una animación de como mucho 1 s en la que el piñón ámbar salta de barra en barra, como un cambio de marcha, y se queda en la posición del icono. En Android 9 a 11 se ve el icono quieto. Es la splash del sistema (`androidx.core:splashscreen`), sin pantalla propia ni textos.
- **Icono «Botones».** Icono nuevo y monocromo en `icons.svg` (`i-botones`): triángulo, cuadrado relleno, círculo y cruz en cuadrícula 2×2. Sustituye a `sports_esports` en la pestaña Botones de la barra inferior; `sports_esports` se queda solo para el mando de Zwift.
- **Sistema de diseño y guía.** Componentes `AppIcon` y `Splash` en `design-system/components/` (README, `preview.html`, estilos en `components.css`), con el icono visto en las máscaras de Android (círculo, cuadrado redondeado), en pequeño y en monocromo, y la animación de la splash. `i-botones` se muestra en la guía junto al resto de iconos, en los dos temas.
- **Maquetas.** La barra inferior de las maquetas con pestañas (Dispositivos, Botones, Ajustes) usa el icono nuevo. Una maqueta nueva, `identidad.html`, reúne el icono, la splash y el icono de Botones, con selector de estado, para validarlos antes de la app.
- **Derivados de la app generados.** `generar.py` emite desde una única fuente en `design-system/` los recursos del icono (primer plano, monocromo, fondo y XML adaptativo), la animación de la splash y el icono `ic_botones`, y un test falla si no coinciden con la fuente, como ya pasa con `Tokens.kt` y los iconos.
- **App.** Manifiesto con `icon`/`roundIcon`, tema de arranque con `androidx.core:splashscreen` e `installSplashScreen()` en `MainActivity`, y el icono nuevo en la barra inferior.

## Capabilities

### New Capabilities
- `app-identity`: el icono de la app en el launcher (adaptativo y monocromo), la splash animada del arranque, y que sus recursos salgan de una única fuente del sistema de diseño y se mantengan sincronizados.

### Modified Capabilities
- `ui-components`: la pestaña Botones de la navegación inferior usa un icono propio, distinto del mando de Zwift de la barra de conexiones (requisito nuevo).
- `style-guide`: la guía documenta y muestra el icono de la app, la splash y `i-botones` en los dos temas (requisito nuevo).
- `screen-mockups`: la maqueta de identidad con sus estados, y la barra inferior de las maquetas con el icono nuevo (requisitos nuevos).

## Impact

- **Código y recursos:** `AndroidManifest.xml` (`icon`, `roundIcon`, tema), `res/values/themes.xml` (tema de arranque), nuevos recursos generados en `res/drawable/`, `res/mipmap-anydpi-v26/` y `res/animator/` o `res/drawable/` (animación), `MainActivity` (`installSplashScreen()`), `Navegacion.kt` (icono de Botones), `scripts/design-system/generar.py` y `contraste.py`, y un test de sincronización junto a `IconosSincronizadosTest`.
- **Dependencias:** `androidx.core:splashscreen` (nueva; se justifica en `design.md`).
- **Sistema de diseño:** `icons.svg` (`i-botones`), `components.css`, componentes `AppIcon` y `Splash`, `styleguide/index.html` regenerada.
- **Maquetas:** `maquetas/identidad.html`, `maquetas/index.html`, `revision.js` si hace falta un estado nuevo, y la barra inferior de `botones.html`, `ajustes.html`, `dispositivos.html` y las demás que la lleven.
- **Documentos de `docs/`:** `arquitectura.md` (recursos de identidad y arranque) y `planificacion.md` (fila nueva, «En curso»). `funcional.md` y `desarrollo.md`: ninguno, salvo que el despliegue de la prueba pida una nota. `AGENTS.md`: la lista de derivados generados incluye los recursos de identidad.

## Out of scope

- Icono de la organización `indoor-cycling-lab` y avatar de GitHub (ya hecho aparte, no es de la app).
- El G3 en color (la cuadrícula de símbolos con colores del sistema): queda reservado para una futura bienvenida o «Acerca de», que no entran aquí.
- Una pantalla de bienvenida o de carga propia, y la animación del icono dentro de la app una vez arrancada.
- Icono de la notificación del servicio de rodaje (hoy la bici de Material, `directions_bike`): se plantea aparte si se quiere el de la app.
- Rediseñar la pestaña Botones ni cambiar el icono del mando de Zwift en la barra de conexiones.
- Iconos de la interfaz que ya existen (`i-tune`, `i-settings`, etc.).
