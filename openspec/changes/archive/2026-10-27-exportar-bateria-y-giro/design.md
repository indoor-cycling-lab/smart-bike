# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs (`settings-backup`, `square-battery`, y los deltas de `settings-screen`, `ride-screen` y `diagnostics`). Estado actual relevante:

- `Almacen` guarda `configuracion.json` y `estado.json` en DataStore. `Configuracion.deJson` nunca falla: ante un documento ilegible devuelve los valores por defecto, migra versiones anteriores, recorta valores fuera de límites y descarta lo desconocido (`arquitectura.md` § 10). `Configuracion.VERSION` es 5.
- `SquareClient` lee `0x2A19` al conectar y cada 10 min (`PERIODO_BATERIA_MS`) y solo lo escribe en el registro. `arquitectura.md` § 7.1: justo después de encender el Square da valores sin sentido, y solo cuentan las lecturas periódicas.
- `Sesion.funcionZwift` envía las funciones de Zwift al mando emulado: «pulsar» las mantiene hasta soltar el botón (`mantenidas`), y corta, larga o doble son pulsaciones de `PULSACION_BREVE_MS` (100 ms). `EstadoMandoZwift` ya sabe qué funciones están pulsadas y que el freno gana al giro de su palanca.
- Rodaje pinta los enlaces con `ConnectionBar` (`EnlaceBarra`, `EstadoChip`) y el pie con `AvisoUi`. Las funciones de Zwift no dan aviso de acción (spec `ride-screen`).
- El planteamiento original del aviso de batería era una variante de `ConnectionChip`; el change 10f sustituyó los chips por la barra de iconos, así que la variante pasa al icono del Square.

## Goals / Non-Goals

**Goals:**
- Tres porciones verticales, cada una probable con el equipo real, que no se estorban entre sí.
- Que el aviso de giro y el de batería no muevan nada en Rodaje (principio de los changes 10d y 10f).
- Importar sin riesgo: nada cambia hasta que el archivo entero es válido y el usuario confirma.

**Non-Goals:**
- Un esquema de exportación distinto del documento de configuración: el archivo es el propio `configuracion.json`.
- Decidir el umbral de batería definitivo ni persistir el nivel entre sesiones de la app.

## Decisions

### 1. El archivo exportado es `configuracion.json` tal cual

`Configuracion.aJson` ya produce el documento versionado que `arquitectura.md` § 10 declara «el mismo documento que se exporta e importa». Se exporta sin envoltorio ni campos nuevos. **Alternativas:** un formato propio con cabecera (aplicación, fecha, versión de la app): se descarta porque duplica la versión y exige una segunda migración; un ZIP con configuración y estado: el estado depende del móvil (dispositivos, identidad del puente con Zwift) y no debe viajar.

### 2. Validación estricta aparte de la lectura tolerante

`deJson` sigue siendo tolerante (es la lectura al abrir). Se añade `Configuracion.validar(texto): ResultadoImportacion` en `:nucleo`, puro, con `Valida(configuracion)` o `Invalida(motivo)`, con `motivo` en `{Vacio, NoEsJson, NoEsConfiguracion, VersionMasNueva}`. Reglas: texto vacío → `Vacio`; JSON mal formado → `NoEsJson`; no es un objeto, o `version` falta o no es un entero ≥ 1, o un campo conocido tiene un tipo equivocado → `NoEsConfiguracion`; `version` > `VERSION` → `VersionMasNueva`. Si pasa, delega en la lectura existente para migrar y normalizar (recortar a límites, descartar lo desconocido), de modo que importar y abrir la app dan el mismo resultado. **Alternativa:** reutilizar `deJson` y comparar con los valores por defecto para adivinar si falló: no distingue un archivo ajeno de una configuración que casualmente coincide con los valores por defecto, ni da motivo.

**Qué cuenta como «tipo equivocado».** Un campo que `kotlinx.serialization` no puede decodificar (p. ej. `"pesoCiclistaKg": "mucho"`) es `NoEsConfiguracion`. Un valor decodificable pero fuera de límites no es un error: se recorta (spec, «Valor fuera de límites»). Un valor de enumeración desconocido se descarta como ya hace la lectura (p. ej. una acción desconocida en una regla). Esto se prueba con un documento de ejemplo de cada versión anterior, que ya existen para las migraciones.

### 3. Selector de archivos del sistema (SAF), sin permisos

`ActivityResultContracts.CreateDocument("application/json")` para exportar (nombre propuesto `square-shift-configuracion-AAAA-MM-DD.json`) y `OpenDocument` con `application/json` y `text/plain`, porque algunos gestores de archivos marcan el JSON como texto. Se lee con `ContentResolver`, con un tope de 1 MB (una configuración real son unos pocos KB; un archivo enorme es `NoEsConfiguracion`), en `Dispatchers.IO`. No hay permisos nuevos ni `FileProvider`. **Alternativas:** hoja de compartir (`ACTION_SEND`): sirve para sacar el archivo pero no para importar, y deja al usuario sin saber dónde quedó; escribir en `Downloads` con `MediaStore`: más código para lo mismo, y no sirve para elegir el archivo al importar.

### 4. Importar en dos pasos, con la configuración pendiente en el estado de Ajustes

`AjustesViewModel` guarda un `Importacion` pendiente (`Ninguna`, `Pendiente(configuracion)`, `Hecha`, `Error(motivo)`, `ErrorExportar`, `Exportada`), que sale en `AjustesUiState`. Elegir un archivo válido pasa a `Pendiente` y muestra la confirmación en línea (un `Notice` informativo con los botones «Importar» y «Cancelar»); solo al confirmar se llama a `Almacen.actualizarConfiguracion { pendiente }`. No hay diálogo emergente: la confirmación en línea reutiliza `Notice` y `sq-actions` y evita un componente nuevo en el sistema de diseño. Los mensajes de éxito y error son `Notice` (el error con `is-danger` y `role="alert"`); se descartan al salir de Ajustes. La `Sesion` ya observa `configuracion` y aplica los cambios en caliente (transmisión, modelo, mando de Zwift…), así que importar rodando no necesita código propio; se prueba que no corta las conexiones. **Alternativa:** diálogo del sistema (`AlertDialog`): componente nuevo, sin maqueta propia ni estilo del sistema de diseño.

La importación reemplaza la configuración entera, incluida `version` (queda en `VERSION` por la lectura). No toca `estado.json`.

### 5. Nivel de batería: solo lecturas periódicas, en memoria

`SquareClient` expone `nivelBateria: Flow<Int>` con las lecturas válidas (0–100) tras la primera de cada conexión. La primera lectura de cada conexión se sigue escribiendo en el registro (como hoy) pero no se emite como nivel: es la regla de `arquitectura.md` § 7.1, que se aplica tal cual en lugar de inventar «los primeros minutos» con un reloj. `Sesion` guarda `bateriaSquare: Int?` en `EstadoRodaje`; la mantiene al reconectar y la pone a `null` al olvidar el Square. No se persiste en `estado.json`: el nivel se vuelve a leer a los 10 min de conectar, y una lectura vieja del día anterior sería peor que ninguna. **Consecuencia aceptada:** hasta 10 min tras abrir la app no hay nivel ni aviso. **Alternativas:** emitir la primera lectura si es > 20 % (la basura al encender da 0 % o 35 %, no se distingue de un nivel real); acortar el primer periodo a 2 min (más lecturas del Square, que ya va con prioridad alta sostenida, sin ganar certeza).

### 6. Umbral y estado «batería baja» en `:nucleo`

`BateriaSquare` (puro) con `UMBRAL_BAJA_PCT = 20` y `fun baja(nivel: Int?): Boolean = nivel != null && nivel <= UMBRAL_BAJA_PCT`. Sin histéresis: la lectura es gruesa y cada 10 min, y una pila de botón se descarga despacio, así que el aviso no parpadea; si en la prueba lo hiciera, se añade entonces. El valor 20 % es provisional (spec): el valor de `0x2A19` da 100 % con una pila de dos años, de modo que se decide con las lecturas periódicas de la prueba de 2 h del change 6, que sigue pendiente.

### 7. Icono del Square con batería baja: una forma nueva, sin color nuevo

`ConnectionBar` y `IconoEnlace` ganan `EstadoChip.Baja` solo para el Square (el estado se compone en `RodajeUiState`: el Square conectado y `BateriaSquare.baja` → `Baja`). Se dibuja como las marcas de espera y de tachado: el icono en `ink` con un hueco circular recortado en la esquina inferior derecha y un «!» dentro. No usa `danger` (reservado a «sin conexión», con otra forma) ni un color de aviso nuevo, así que no hay token nuevo ni comprobación de contraste nueva: se distingue por la forma, como pide la accesibilidad. La descripción accesible de la barra cambia a «Square, batería baja». **Alternativa:** la variante de `ConnectionChip` del planteamiento original, sin objeto desde el 10f; un `Notice` en Rodaje: la spec lo prohíbe y ocuparía sitio.

Diagnóstico muestra el nivel en la fila del Square (texto en su `sq-caption`) y, con la batería baja, un `Notice` informativo en la sección Conexiones, como los otros avisos de esa sección.

### 8. Aviso de giro: derivado de las funciones de Zwift pulsadas

`Sesion` ya sabe qué funciones están pulsadas (`EstadoMandoZwift.pulsadas`) cuando el mando está activado. Se añade `giro: Giro?` (`Izquierda`, `Derecha`, `Ambos`, o ninguno) a `EstadoRodaje`, calculado a partir de `GIRAR_IZQUIERDA` / `GIRAR_DERECHA` pulsadas, descontando la que tenga el freno de su palanca (misma regla que `trama()`: el freno gana). Reutiliza la lógica pura de `EstadoMandoZwift` en lugar de repetirla en `Sesion`: se le añade `giro(): Set<Lado>`. Un giro breve (100 ms) dura menos que un fotograma útil, así que el estado se mantiene al menos 500 ms desde que empieza (un `delay` en `Sesion`, con el reloj de pruebas inyectable); un giro «pulsar» dura mientras se mantiene y, al soltar, desaparece de inmediato si ya pasaron los 500 ms. `soltarZwift` (perder el Square, desactivar el mando, terminar) lo borra todo. Con el mando desactivado no hay giro: la app no lo envía, y el indicador solo dice lo que la app envía. **Limitación conocida:** con el Square emparejado directamente en Zwift, que era el uso que motivó la petición, el aviso no sale. Con el mando emulado (change 13, validado) ese uso queda sustituido; si el usuario lo echa en falta, se plantea como change aparte.

**Componente.** `TurnIndicator` en `design-system/` (`sq-turn`): dos flechas (`i-arrow_back`, y la derecha reflejada con `scaleX(-1)`) a los lados de la marcha, cada una en una caja de `touch-min` que se reserva siempre (invisible con `visibility: hidden` / `alpha`, no `display: none`), de modo que no mueve nada. Se ve con forma y posición, no con color. `Rodaje` la coloca en la fila de `GearDisplay`; en vertical y en horizontal, en los dos temas. Descripción accesible mediante un `liveRegion` («Girando a la izquierda»). **Dónde exactamente cabe en la fila de la marcha, sobre todo con la marcha de dos cifras y con la Shimano (plato × piñón), es lo que valida la maqueta; si no cabe sin tocar la marcha, se decide con el usuario al validarla.**

### 9. Maqueta primero

Los cuatro cambios visuales (sección «Copia de seguridad» y sus estados, `TurnIndicator`, el icono con batería baja y la fila del Square de Diagnóstico) van a maqueta antes de la app, con la puerta de validación de `AGENTS.md`. Estados nuevos de `data-mock-state`:

- `ajustes.html`: `copia-confirmar`, `copia-importada`, `copia-exportada`, `copia-error-version`, `copia-error-archivo`.
- `rodaje.html` y `rodaje-vertical.html`: `giro-izquierda`, `giro-derecha`, `giro-ambos`, `giro-shimano` y `square-bateria-baja` (`bateria-baja` ya era la batería del móvil). Todas las maquetas de Rodaje llevan el sitio de las flechas reservado (`sq-turn`).
- `diagnostico.html`: `bateria-ok`, `bateria-sin-lectura`, `bateria-baja`, `square-sin-conexion-bateria`.

## Risks / Trade-offs

- [Hasta 10 min sin aviso tras abrir la app] → Aceptado y escrito en la spec; la batería baja se avisa en la siguiente lectura periódica.
- [El umbral de 20 % puede ser demasiado pronto o tarde: el valor es grueso y da 100 % con pilas viejas] → Provisional; se ajusta con las lecturas de la prueba de 2 h del change 6. Una constante en `:nucleo`.
- [Importar rodando cambia transmisión o modelo en caliente y puede cambiar la marcha de golpe] → La confirmación lo avisa («reemplazarán todos los ajustes»); es el mismo efecto que cambiarlos a mano rodando, y se prueba que no corta las conexiones. No se bloquea la importación.
- [Un gestor de archivos del móvil puede no ofrecer crear o abrir documentos] → El selector del sistema existe desde Android 4.4 (minSdk 28), pero un móvil sin él es posible; si no hay actividad que lo atienda, se muestra el error «No se pudo abrir el selector de archivos».
- [El `delay` de 500 ms del giro rompe el principio de «el estado es lo que se envía»] → Es solo de presentación: el mando envía su trama sin esperar; el estado de Rodaje (`giro`) añade el mínimo visible.
- [Una configuración importada de un archivo manipulado] → Se valida entera, se recortan los límites y se descartan las acciones desconocidas, como al leer el documento propio; no se ejecuta nada del archivo ni hay rutas ni URL en la configuración.

## Migration Plan

Sin migración de datos: `configuracion.json` no cambia de versión (el aviso de giro, el nivel de batería y el estado de importación no se guardan). Revertir el change deja la app como estaba; los archivos ya exportados siguen siendo válidos para la versión 5.

**Validación con el equipo real** (reglas de `openspec/config.yaml`): el nivel de `0x2A19` y que la primera lectura al conectar se ignora se comprueban en el OnePlus 8 con el Square leyendo el registro a los 10 min (en la fila del Square de Diagnóstico debe aparecer el nivel); la batería baja no se puede provocar con el Square real, y se cubre con tests del dominio y de la UI con niveles inyectados. El giro se prueba con Zwift en PC y el Square desemparejado en Zwift: mantener X y Círculo, y ver la flecha y a la vez que Zwift gira; con un freno y el giro de la misma palanca, no se ve la flecha. Exportar e importar se prueban en el móvil: exportar a `Descargas`, cambiar un ajuste, importar y ver que vuelve; y con un archivo no válido (un `.txt`).
