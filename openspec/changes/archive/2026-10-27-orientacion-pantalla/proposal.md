## Why

La app sigue siempre la orientación del móvil y su bloqueo de rotación (F11). Con el móvil en el soporte de la bici, eso obliga a tocar el bloqueo de rotación del sistema cada vez que se quiere fijar Rodaje en vertical o en horizontal, y el bloqueo afecta a todas las apps. Además, un golpe o un giro al coger el móvil puede cambiar la disposición en mitad de una ruta. Falta poder fijar la orientación solo en esta app.

## What Changes

- **Ajuste «Orientación» en la sección «Pantalla» de Ajustes**, debajo de «Tema», con tres opciones excluyentes, cada una con su ayuda:
  - «Según el móvil» (por defecto): lo de hoy; gira con el móvil y respeta el bloqueo de rotación del sistema.
  - «Vertical»: la app se ve siempre en vertical, aunque se gire el móvil o el bloqueo del sistema esté en horizontal.
  - «Horizontal»: la app se ve siempre en horizontal, aunque se gire el móvil o el bloqueo del sistema esté en vertical.
- **Se aplica a toda la app**, Rodaje incluida, y **al momento**: al elegir «Horizontal» con el móvil en vertical, Ajustes pasa a horizontal sin terminar la sesión, cambiar la marcha ni cortar ninguna conexión.
- **Se guarda** con los demás ajustes y sigue vigente al volver a abrir la app. Una configuración guardada antes de este change se lee con «Según el móvil».
- **Maqueta primero**: la opción nueva en `maquetas/ajustes.html`, con un estado por opción, revisada en los dos temas antes de tocar la app.

Fuera de alcance:
- Una orientación distinta para Rodaje y para el resto de pantallas: el ajuste vale para toda la app.
- Cambiar la disposición de Rodaje, de Ajustes o de cualquier otra pantalla en cada orientación: se usan las que ya existen.
- Elegir el sentido del giro (horizontal hacia la izquierda o la derecha, vertical boca abajo): en una orientación fija, la app sigue el sentido en que se sostenga el móvil (design.md, decisión 2).
- Cambiar la orientación con un botón del Square (no hay acción asignable para ello).
- Exportar e importar la configuración (change 10b), que incluirá este ajuste sin trabajo adicional.
- La barra de conexiones con iconos (change 10f, en curso): este change no toca la barra superior de Rodaje.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `settings-screen`: requisito nuevo «Orientación en Ajustes»; «Ajustes como pestaña» añade «Orientación» a la sección «Pantalla»; «Ajustes guardados» añade la orientación y la lectura de configuraciones anteriores.
- `ride-screen`: «Rodaje como pantalla inicial» sigue la orientación del móvil solo con «Según el móvil», y en vertical u horizontal fijos se queda en esa orientación.
- `screen-mockups`: «Ajustes y datos en pantalla» muestra «Orientación» en la sección «Pantalla», con un estado por opción.

## Impact

- Sistema de diseño: sin componentes ni tokens nuevos (se reutiliza `sq-opt` dentro de `SettingsSection`, como «Tema»). Guía sin cambios.
- Maquetas: `maquetas/ajustes.html` (fieldset «Orientación» y estados nuevos) y `maquetas/index.html` (enlace a la maqueta con «Horizontal» elegido).
- App:
  - `:nucleo`: `dominio/OrientacionApp.kt` (enum nuevo) y el campo `orientacion` en `dominio/Configuracion.kt`, opcional y con valor por defecto, sin subir la versión; tests en `ConfiguracionTest`.
  - `:app`: `AppGraph.kt` (`orientacion` disponible al crear la actividad), `MainActivity.kt` (`requestedOrientation`), `ui/ajustes/` (`AjustesUiState`, `AjustesViewModel`, `Ajustes.kt`), `res/values/strings.xml`; tests en `AjustesUiStateTest`, `AjustesTest` y `NavegacionUiTest`.
- Funcionalidades: F12.8 (Pantalla) pasa a incluir la orientación; F11 deja de decir que Rodaje siempre sigue la orientación del móvil.
- Prueba con el equipo real: en el OnePlus 8 en el soporte, rodando con el Neo y el Square, elegir «Horizontal» y «Vertical» y girar el móvil sin que la app gire; con el bloqueo de rotación del sistema en la orientación contraria; volver a «Según el móvil»; cerrar y abrir la app.
- Documentos de `docs/`: `funcional.md` (F11 y F12.8), `arquitectura.md` (§ 9.1, Rodaje y la orientación; § 10, el campo `orientacion` de `configuracion.json`), `planificacion.md` (fila nueva 10g y sus notas). `desarrollo.md` se revisa al final; no se esperan cambios.
