## Why

Rodaje no tiene barra inferior: para llegar a Ajustes hay que tocar un chip, abrir Dispositivos y cambiar de pestaña, o usar la combinación del Square (Campag. I + cruceta). Falta un acceso directo y visible. Además, la app se ve siempre en el tema oscuro (rodaje), aunque el sistema de diseño tiene un tema claro completo, con el contraste comprobado, que se lee mejor con luz de día y que hoy no se puede elegir.

## What Changes

- **Icono de Ajustes en Rodaje.** Un botón con el icono de Ajustes (el mismo `tune` de la pestaña) en la esquina superior derecha de Rodaje, en horizontal y en vertical, con objetivo táctil de `touch-min` y descripción accesible «Ajustes». Al tocarlo se abre Ajustes como pestaña, con la barra inferior y Ajustes marcada, igual que desde la barra; Atrás vuelve a Rodaje. La sesión, la marcha y las conexiones no cambian.
  - En horizontal, va a la derecha de los chips de conexión, en la barra superior.
  - En vertical, va al final de la línea de los chips, que sigue ocupando una sola línea a 360 dp.
- **Tema en Ajustes.** Una sección nueva «Pantalla» con el ajuste «Tema» y dos opciones: «Oscuro» (por defecto, el tema rodaje de hoy) y «Claro».
  - Se aplica al momento a toda la app, Rodaje incluida, sin terminar la sesión ni cambiar la marcha.
  - Se guarda con los demás ajustes y sigue vigente al volver a abrir la app. Una configuración guardada antes de este change se lee con «Oscuro».
  - Las barras del sistema (estado y navegación) llevan iconos oscuros en el tema claro y claros en el oscuro.
- **Maqueta primero.** El icono en `rodaje.html` y `rodaje-vertical.html` (en todos sus estados) y la sección «Pantalla» en `ajustes.html`, revisados en los dos temas, antes de tocar la app.

Fuera de alcance:
- Seguir el tema del sistema Android: el usuario eligió solo «Oscuro» y «Claro».
- Un tema propio para Rodaje distinto del resto: el elegido se aplica a toda la app.
- El icono de Ajustes en Dispositivos, Botones o las pantallas secundarias, que ya tienen la barra inferior o volver.
- Cambiar los colores o tokens de cualquiera de los dos temas, la guía de estilo o la «Muestra del tema» de depuración.
- Cambiar el tema con un botón del Square (no hay acción asignable para ello).
- Exportar e importar la configuración (change 10b), que incluirá el tema sin trabajo adicional.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: requisito nuevo «Ajustes desde Rodaje» con el icono, su posición en las dos orientaciones y lo que abre.
- `settings-screen`: «Ajustes como pestaña» añade la sección «Pantalla» en su orden; requisito nuevo «Tema en Ajustes»; «Ajustes guardados» añade el tema y la lectura de configuraciones anteriores.
- `app-theme`: «Tema rodaje por defecto y tema claro por contenedor» pasa a usar el tema elegido en Ajustes para toda la app, con el rodaje (oscuro) por defecto, y añade las barras del sistema.
- `screen-mockups`: «Ajustes y datos en pantalla» deja de excluir el tema y muestra la sección «Pantalla»; requisito nuevo «Acceso a Ajustes en las maquetas de rodaje».

## Impact

- Sistema de diseño: `design-system/components.css` (botón de Ajustes de `sq-ride`, `sq-ride__settings`), `design-system/components/RideScreen/README.md` y `preview.html`, guía regenerada. Sin tokens nuevos.
- Maquetas: `maquetas/rodaje.html`, `maquetas/rodaje-vertical.html` (el icono en todos los estados) y `maquetas/ajustes.html` (sección «Pantalla»); `maquetas/index.html` si cambian los enlaces destacados.
- App:
  - `:nucleo`: `dominio/Configuracion.kt` (campo `tema`, opcional con valor por defecto) y su test.
  - `:app`: `ui/rodaje/Rodaje.kt` y `RodajeRuta.kt` (icono y apertura de Ajustes), `ui/navegacion/` (abrir Ajustes desde Rodaje), `ui/ajustes/` (sección «Pantalla»), `MainActivity.kt` (tema de la configuración, fondo de la ventana y barras del sistema), `AppGraph.kt` (tema disponible al recrear la actividad), `res/values/strings.xml`; tests de `RodajeTest`, `AjustesUiStateTest`, `NavegacionTest` y `ConfiguracionTest`.
- Funcionalidades: F11 (acceso a Ajustes desde Rodaje) y F12 (tema). No tienen identificador todavía: se añaden F11.9 y F12.8 a `funcional.md`.
- Prueba con el equipo real: en el OnePlus 8, rodando con el Neo y el Square, tocar el icono en vertical y en horizontal, cambiar a «Claro» y volver a Rodaje con la misma marcha y las conexiones activas; leer Rodaje en claro a 1–2 m; cerrar y abrir la app y girar el móvil en claro.
- Documentos de `docs/`: `planificacion.md` (fila nueva 10e), `funcional.md` (F11.9 y F12.8), `arquitectura.md` (§ 9.1: el tema sale de la configuración y se aplica a toda la app, fondo de la ventana). `desarrollo.md` se revisa al final; no se esperan cambios.
