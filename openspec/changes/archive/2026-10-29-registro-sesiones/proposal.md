# Proposal

## Why

Cuando algo falla rodando (una reconexión, un gesto que no hace lo esperado, un cambio de modo con Zwift), el registro de la app solo vive en memoria y en `logcat` de la compilación de depuración: hace falta el Mac conectado por `adb` y se pierde al cerrar la sesión. F13.4 pide poder guardar un registro de la sesión para analizar problemas después, sin el Mac delante. Es pequeño, no depende de nada pendiente y sirve para depurar las pruebas con el equipo real que aún quedan abiertas.

## What Changes

- **Grabación opcional de sesiones.** Un ajuste de depuración, apagado por defecto, graba cada sesión en un archivo de texto plano en la carpeta de la app. Al activarlo empieza un archivo nuevo en ese momento; mientras está activado, cada sesión nueva tiene el suyo.
- **Solo sucesos.** El archivo lleva los sucesos con hora de reloj (conexiones y reconexiones, gestos y acciones, cambios de marcha y de modo, envíos al Neo, lo que pide la plataforma, wifi, Bluetooth y errores) y una cabecera con la fecha, la versión de la app y la configuración de la sesión (plataforma, modo, transmisión y modelo). Las tramas crudas del Neo, del Square y de la plataforma no se graban: para eso sigue `logcat` en la compilación de depuración.
- **Escritura continua.** Cada suceso se escribe según llega, fuera del hilo de control, para no perder lo ocurrido si la app se cierra de golpe y sin frenar el rodaje.
- **Espacio libre mínimo.** Un ajuste «Espacio libre mínimo» (por defecto 10 %, de 5 % a 50 % de 5 en 5). Se comprueba al empezar y cada 30 s; por debajo del mínimo la grabación se detiene sola, deja una última línea con el motivo y no se reanuda hasta reactivarla o hasta la siguiente sesión. La app no borra nada por su cuenta.
- **Pantalla «Sesiones grabadas».** Secundaria de Ajustes, siempre accesible aunque la grabación esté apagada: lista las sesiones (más reciente primero) con su fecha y su tamaño; cada una se puede abrir con otra app del móvil, compartir y borrar, y hay «Borrar todas». La que se está grabando no se puede borrar. Una línea de ayuda avisa de que el archivo puede incluir direcciones de red y nombres de dispositivos.
- **Ajustes.** Una sección «Depuración» al final, con el interruptor, el espacio libre mínimo y la entrada «Sesiones grabadas», que resume cuántas hay y cuánto ocupan.
- **Configuración.** `Configuracion` pasa a la versión 11 con `grabarSesiones` y `espacioLibreMinimoPct`, sin migración (los documentos anteriores toman los valores por defecto).
- **Etapas.** Cambio con impacto visual: etapa 1, maqueta (Sesiones grabadas, sección «Depuración» de Ajustes) y puerta de validación del usuario; etapa 2, la app.

Fuera de alcance:

- Grabar tramas crudas, o un nivel de detalle configurable (queda `logcat`).
- Anonimizar el contenido: solo se avisa en la pantalla.
- Un tope automático de sesiones o de espacio ocupado, el borrado automático y reanudar la grabación sola al recuperar espacio.
- Subir los archivos a ningún servicio o enviarlos desde la app: compartir es la hoja del sistema.
- Otros formatos (JSON) y la exportación en lote.
- Cambios en el registro en memoria ni en el tráfico en directo de Diagnóstico (F13.1, F13.3).

## Capabilities

### New Capabilities
- `session-recording`: grabar la sesión en un archivo de sucesos cuando el ajuste de depuración está activado, con cabecera, escritura continua y parada por espacio libre mínimo, y gestionar las sesiones grabadas (listar con tamaño, abrir, compartir y borrar una o todas).

### Modified Capabilities
- `settings-screen`: Ajustes añade la sección «Depuración» (interruptor, espacio libre mínimo y la entrada «Sesiones grabadas») y los dos ajustes se guardan con valor por defecto.
- `app-navigation`: «Sesiones grabadas» es una pantalla secundaria de Ajustes.
- `screen-mockups`: nueva maqueta de Sesiones grabadas y sección «Depuración» en la maqueta de Ajustes, con sus estados.

## Impact

- **Código.** `:core` (`Configuracion` v10; el cálculo del espacio libre mínimo y el formato de línea y cabecera, probables en `dominio/`). `:app`: `datos/` (`GrabadorSesion`, `RepositorioSesiones` y un sumidero de sucesos en `RegistroSesion`), `AppGraph.kt` (crear el grabador y atarlo a la sesión y al ajuste), `ui/` (Ajustes, la pantalla `sesiones`, la ruta y la navegación), `AndroidManifest.xml` y `res/xml/` (un `FileProvider` sobre `files/sesiones/`) y los textos. Sin permisos nuevos.
- **Sistema de diseño.** Una fila de sesión con sus acciones y los iconos de abrir, compartir y borrar, si no se pueden resolver con los componentes de lista existentes (`design-system/`, con su guía regenerada).
- **Maquetas y web.** `maquetas/sesiones.html`, `maquetas/ajustes.html`, `maquetas/index.html` y las capturas de `site/`.
- **Datos.** `Configuracion` sube de versión sin migración. Los archivos viven en `files/sesiones/` y no entran en la copia de seguridad de la configuración.
- **Specs.** Una nueva (`session-recording`) y tres modificadas (`settings-screen`, `app-navigation`, `screen-mockups`).
- **Documentación.** Cambian `docs/funcional.md` (F13.4 y la lista de Ajustes), `docs/arquitectura.md` (componentes y datos), `docs/planificacion.md` (la fila del 16, que pasa a llamarse `registro-sesiones`) y `docs/desarrollo.md` (cómo leer las sesiones grabadas por `adb` y desde el móvil).
- **Coordinación.** `ftms-bluetooth` está en curso y dice no subir la versión de `Configuracion`; este change la sube a la 11 (hoy es la 10). El orden de archivado no importa mientras cada uno parta de la versión vigente.
- **Verificación con el equipo real.** Prueba del usuario en el OnePlus 8: grabar una sesión rodando, abrirla y compartirla con otra app, borrarla y comprobar la parada por espacio libre mínimo (subiendo el mínimo hasta superar el espacio libre del móvil).
