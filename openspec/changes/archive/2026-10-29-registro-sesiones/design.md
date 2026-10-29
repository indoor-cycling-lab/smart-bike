# Design

## Context

Ver `proposal.md` (por qué y qué). Estado actual, tal como lo he leído en el código:

- `RegistroSesion` (`datos/`) es un buffer circular de 10 000 `EntradaRegistro`, con `trama(...)` (`Rx`/`Tx`) y `suceso(...)` (`Evt`). Se puede escribir desde cualquier hilo y cada entrada pasa por un `espejo` opcional (en depuración, `logcat` con la etiqueta `SqTrama`). Hay unos 100 puntos que emiten sucesos, todos de baja frecuencia: ninguno va por cada notificación del Neo.
- `EntradaRegistro.linea()` usa el reloj monotónico (`tMs`), pensado para los tests de `:core`; para leer un archivo después hace falta la hora de reloj (`horaMs`).
- `AppGraph` es dueño de `registro`, `almacen` (`configuracion.json` y `estado.json` con DataStore) y de `sesion: StateFlow<Sesion?>`; la `Sesion` vive mientras dura el rodaje (`iniciarSesion` / `terminarSesion`) dentro de un servicio en primer plano.
- Ajustes, Marchas, Datos en pantalla, Botones y Diagnóstico son secundarias o entradas de Ajustes; las rutas están en `Navegacion.kt` (`Rutas`, `Pestana.de`).
- No hay `FileProvider` ni `res/xml/` en el proyecto. Exportar la configuración usa el selector de archivos del sistema, otro mecanismo.
- `Configuracion` está en la versión 10 (`pestana-conexion` la subió); `ftms-bluetooth` no la sube.

## Goals / Non-Goals

**Goals:**
- Poder guardar lo ocurrido en una sesión sin el Mac delante, y revisarlo, abrirlo con otra app, compartirlo y borrarlo desde el móvil.
- Que activarlo sea una decisión explícita (apagado por defecto) y que no pueda dejar sin espacio al móvil ni frenar el rodaje.
- No tocar a quienes emiten sucesos: el grabador se cuelga del registro.

**Non-Goals:**
- Tramas crudas (queda `logcat`), anonimizar, tope automático, reanudar sola, otros formatos, subir a servicios.
- Cambiar el registro en memoria, Diagnóstico o el tráfico en directo.

## Decisions

### 1. Un sumidero de sucesos en `RegistroSesion`

`RegistroSesion` gana un sumidero opcional, `sumidero: ((EntradaRegistro) -> Unit)?`, que se asigna y se quita en caliente (`@Volatile`) y recibe solo las entradas `Evt`, junto al `espejo` que ya existe. Así los ~100 emisores no cambian y, con la grabación apagada, el coste es una lectura de un campo nulo.

*Alternativa descartada:* suscribirse a `cambios` y volver a pedir `ultimas(n)`. Muestrea (se pierden entradas con ráfagas) y recorre el buffer cada vez; el sumidero recibe cada suceso exactamente una vez.

### 2. `GrabadorSesion`: cola, corrutina de IO y estado

Una clase en `datos/` que recibe el directorio `files/sesiones/`, el reloj de pared, una función de espacio (`() -> Espacio(libreBytes, totalBytes)`) y un ámbito de IO. Con el sumidero activo, cada suceso se formatea (hora de reloj, origen y texto) y entra en un `Channel` con capacidad limitada y `DROP_OLDEST`; una sola corrutina en `Dispatchers.IO` lo escribe en un `BufferedWriter` y lo vacía (`flush`) al quedarse la cola sin nada y, como mucho, cada segundo. El hilo de control nunca espera al disco. Un fallo de escritura detiene la grabación (estado `Error`), se anota en el registro y no se propaga.

Estado público, `StateFlow<EstadoGrabacion>`: `Apagada`, `Grabando(archivo)`, `DetenidaPorEspacio(archivo)`, `DetenidaPorError(archivo)` y `SinEspacio` (activada con el espacio ya por debajo del mínimo, sin archivo). La pantalla de sesiones y la sección «Depuración» lo leen.

*Alternativa descartada:* escribir con `FileWriter` desde el propio sumidero, en el hilo que emite. Bloquearía el hilo de control con cada suceso.

### 3. Cuándo se graba: ajuste × sesión

`AppGraph` crea el grabador y lo ata a `combine(configuracion.grabarSesiones, sesion != null)`. Cuando pasa a verdadero se abre un archivo nuevo y se instala el sumidero; cuando pasa a falso (se apaga el ajuste o termina la sesión) se cierra el archivo y se quita el sumidero. Así «activar con la sesión en curso» empieza en ese momento y «una sesión nueva con el ajuste activado» abre otro archivo, sin lógica aparte. Activar con una sesión ya abierta no recupera lo anterior: no hay forma de reconstruirlo sin grabar las tramas, y es lo esperado.

El nombre del archivo es `sesion-AAAAMMDD-HHmmss.txt` con la hora de inicio, que ordena por nombre igual que por fecha y evita choques (si ya existe, se añade `-2`, `-3`…).

### 4. Formato del archivo

Texto plano UTF-8. Cabecera de líneas que empiezan por `# ` (inicio con fecha y hora, versión de la app, plataforma, modo, nombre ante la app, transmisión y modelo, según `Configuracion` en ese instante), una línea en blanco y después un suceso por línea: `HH:mm:ss.SSS ORIGEN texto`. Sin dirección MAC propia del móvil ni identificadores del dispositivo; los sucesos ya existentes sí pueden llevar direcciones de red y nombres de los dispositivos recordados, y por eso la pantalla de sesiones lo avisa (decisión 8). Las líneas de parada empiezan por `# ` para distinguirlas de los sucesos: `# Grabación detenida: queda 8 % libre, mínimo 10 %`.

La función que formatea la cabecera y las líneas es pura y vive en `:core` (`dominio/`), con sus tests, para que `GrabadorSesion` solo ponga el disco.

### 5. Espacio libre mínimo

`Espacio(libre, total)` se lee con `StatFs` sobre `filesDir` (`availableBytes` y `totalBytes`) y la decisión es una función pura de `:core`: `hayEspacio(espacio, minimoPct)`, que es verdadera si `libre * 100 >= total * minimoPct`. Se evalúa al abrir el archivo (si falla, estado `SinEspacio` y no se crea nada) y cada 30 s dentro de la corrutina de escritura; si falla, se escribe la línea de parada, se cierra el archivo y el estado pasa a `DetenidaPorEspacio`. No se reanuda sola (decisión 3 de la propuesta): solo el ciclo ajuste × sesión de la decisión 3 vuelve a abrir un archivo, y lo hará si hay espacio.

El rango del mínimo es de 5 % a 50 % de 5 en 5 (`ESPACIO_LIBRE_MINIMO_PCT` y `PASO_...` en `Configuracion`). El tope de 50 % permite probar la parada con el móvil real subiendo el mínimo; en los tests, con un `Espacio` falso.

### 6. Sesiones grabadas: `RepositorioSesiones`

Otra clase en `datos/` que lista `files/sesiones/*.txt` por nombre descendente (con `lastModified` como respaldo), con tamaño y si es la que se graba (comparando con el estado del grabador), borra una (nunca la activa) o todas (respetando la activa) y construye la `Uri` de `FileProvider` para abrir o compartir. El resumen de Ajustes («3 sesiones · 41 KB») sale de la misma lista. La lista se vuelve a leer al abrir la pantalla, tras cada borrado y con un muestreo corto mientras se graba, para que el tamaño de la activa crezca a la vista.

### 7. Abrir y compartir: `FileProvider`

Un `FileProvider` declarado en el manifiesto con la autoridad `${applicationId}.sesiones` y un `res/xml/rutas_sesiones.xml` con `files-path` sobre `sesiones/`: expone solo esa carpeta. Al tocar una sesión se crea un `Intent` `ACTION_VIEW` con `text/plain`, la `Uri` en `data` y `ClipData`, con `FLAG_GRANT_READ_URI_PERMISSION`; «Compartir» usa `ACTION_SEND` con `EXTRA_STREAM`, el mismo tipo y `Intent.createChooser`. Si `startActivity` lanza `ActivityNotFoundException`, la pantalla muestra un aviso y no falla. Sin permisos nuevos: el permiso de lectura es temporal, solo para esa `Uri`.

*Alternativa descartada:* copiar la sesión al almacenamiento compartido (`MediaStore`) para que cualquier app la vea. Pide más permisos o más código y deja archivos fuera del control de la app y de su borrado.

### 8. Pantalla y UI

`Rutas.SESIONES`, secundaria de Ajustes (`Pestana.de` → Ajustes), con su `UiState` y `ViewModel`, y la sección «Depuración» al final de Ajustes. La pantalla se compone de la ayuda sobre el contenido, el estado de la grabación, la lista y «Borrar todas», con confirmación en línea (como «Restaurar» en Botones) para borrar una o todas. La fila de sesión necesita tres acciones con objetivos de 48 dp; si `SettingsList` y `Button` no la resuelven con claridad, se añade `SessionRow` a `design-system/` (estilos en `components.css`, carpeta con `README.md` y `preview.html`, guía regenerada) y los iconos de abrir, compartir y borrar a `icons.svg`; se decide al hacer la maqueta.

El aviso de privacidad es un texto fijo en la pantalla; no se anonimiza nada.

### 9. Configuración

`Configuracion` sube a la versión 11 con `grabarSesiones: Boolean = false` y `espacioLibreMinimoPct: Int = 10`, con `normalizada()` que recorta el mínimo al rango. Sin migración: `ignoreUnknownKeys` y los valores por defecto bastan. Los dos ajustes entran en «Copia de seguridad» como el resto de la configuración. Si `ftms-bluetooth` se archiva antes, este change parte de la versión vigente (hoy la 10) y la sube a la siguiente; el orden no importa.

### 10. Orden de trabajo

1. Maqueta: sistema de diseño (si hace falta), `maquetas/sesiones.html` y la sección de `maquetas/ajustes.html`, con sus estados, el índice y las capturas. **Puerta:** validación del usuario.
2. `:core`: `Configuracion` v10, el formato de cabecera y línea y la decisión de espacio, con tests.
3. `:app`: sumidero, `GrabadorSesion`, `RepositorioSesiones`, `FileProvider`, atado en `AppGraph`.
4. `:app`: UI (Ajustes, ruta y pantalla) con sus tests.
5. Documentación y prueba del usuario.

## Risks / Trade-offs

- **El contenido puede incluir direcciones de red y nombres de dispositivos** (`Wifi: 192.168…`, `Recordado: … (MAC)`) y se abre o comparte con otras apps → aviso fijo en la pantalla; es una decisión puntual del usuario y no se anonimiza.
- **Un cierre brusco puede dejar la última línea a medias o perder el último segundo** → asumido; el vaciado cada segundo lo acota y el archivo sigue siendo legible.
- **La cola se llena con una ráfaga de sucesos** → capacidad limitada con `DROP_OLDEST` y una línea `# se descartaron N sucesos` al desbordar, para que no se pierdan sin aviso.
- **Comprobar el espacio cada 30 s deja una ventana** en la que un archivo puede crecer algo más de la cuenta → con sucesos de baja frecuencia son kilobytes; el mínimo del 10 % deja un margen enorme.
- **Dos changes tocan `Configuracion`** (`ftms-bluetooth` en curso) → ver decisión 9; sin migración, no hay dependencia de orden.
- **`StatFs` sobre `filesDir` mide el volumen de datos del usuario**, que es lo que se llena → es el volumen correcto en Android; no hay otro que medir.

## Validación con el equipo real

Nada de BLE queda «a validar». La prueba la hace el usuario en el OnePlus 8: activar «Grabar sesiones», rodar con el Neo y el Square (y Zwift) unos minutos y mirar la sesión en la lista con su tamaño; abrirla con otra app y compartirla; borrarla; comprobar que un cierre forzado de la app deja el archivo con lo ocurrido; y comprobar la parada por poco espacio subiendo el mínimo por encima del espacio libre del móvil.
