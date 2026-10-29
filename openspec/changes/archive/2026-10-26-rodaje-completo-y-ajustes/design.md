# Design

## Context

Motivación y alcance en `proposal.md`; comportamiento en las specs del change.

Estado actual:
- `MainActivity` tiene un `NavHost` con Rodaje como inicio. Diagnóstico se abre desde los chips; Botones y Ajustes, desde Diagnóstico; Marchas, desde Ajustes; Asignar acción, desde Botones. No hay barra inferior ni pantalla de Dispositivos: buscar está en la sección Conexiones de Diagnóstico y no se puede olvidar un dispositivo.
- `RodajeRuta` termina la sesión con `BackHandler`. Rodaje pinta siempre potencia y cadencia con `MetricGrid`, que ya admite de 1 a 6 datos y elige columnas y tamaño por número (horizontal y vertical).
- `Configuracion` (versión 3) ya tiene `pendienteBasePct`, `mostrarAccion` y `vibracion`, sin pantalla que los cambie. `mostrarAccion` no se usa; `vibracion` sí, en `Sesion.vibrar` y en los topes.
- La `Sesion` solo recibe acciones del Square: `gesto(g)` busca la acción en la asignación y la aplica (marcha, ERG o alternar). Emite `EventoRodaje` por un `SharedFlow` para avisos, sin réplica.
- La potencia de Rodaje es la última de `0x2A63` (~1 Hz); la cadencia sale de `CalculoCadencia`.
- `Accion.TODAS` identifica las acciones por posición en la ruta de Asignar acción; las nuevas van al final para no mover las anteriores.
- `design-system/` ya tiene `BottomNav` (`sq-nav`), la fila con asa (`sq-handle`, `.sq-item.is-off`) y la vista previa `sq-ride-mini`, sin composable en la app.

## Goals / Non-Goals

**Goals:**
- Que los botones − y + y la pantalla anterior o siguiente entren por el mismo camino que el Square (`arquitectura.md` § 5.3), probados con los tests del dominio y de la `Sesion`.
- Calcular los datos de la sesión en `:nucleo`, sin Android, con reloj virtual en los tests.
- Migrar la configuración guardada sin pisar nada del usuario.
- Que ninguna pantalla nueva toque el camino caliente (NF1).

**Non-Goals:**
- Exportar e importar, batería del Square y aviso de giro (change 10b).
- Velocidad y distancia (change 15).
- Restaurar el estado de cada pestaña con varios niveles (por ejemplo, volver a Marchas al pulsar Ajustes): las pestañas abren su raíz.

## Decisions

### 1. Pestañas con Navigation Compose, Rodaje como raíz

Las cuatro pestañas son destinos de nivel superior del mismo `NavHost`. Ir a una pestaña navega con `popUpTo(RUTA_RODAJE)` y `launchSingleTop`, así que la pila es siempre `[rodaje]` o `[rodaje, pestaña, secundaria…]`:
- Atrás en una pestaña vuelve a Rodaje (spec `app-navigation`, «Atrás del sistema») sin código propio.
- Atrás en Rodaje sigue en su `BackHandler`, que termina la sesión.
- «Rodar» hace `popBackStack(RUTA_RODAJE, inclusive = false)`.

`BottomNav` es un composable nuevo en `ui.componentes` con el mismo nombre que el componente del sistema de diseño; lo pinta cada pantalla de pestaña al pie (no un `Scaffold` común), porque Rodaje y las secundarias no lo llevan y así cada pantalla controla su `WindowInsets`.

El `NavHost` vive en `AppNavegacion`, que recibe las pantallas (`Pantallas`) para poder probar la navegación con pantallas de prueba (`NavegacionUiTest`), y no tiene transiciones animadas: con el Square se cambia de pantalla seguido, y un cambio seco se lee mejor a 2 m.

Alternativa descartada: un `Scaffold` con la barra en `MainActivity` que se oculta según la ruta. Mezcla la navegación con el diseño de cada pantalla y hace parpadear la barra al entrar en Rodaje.

### 2. Los chips abren Dispositivos; Diagnóstico, desde Ajustes

Los chips hablan de conexiones, y Dispositivos es la pantalla de conexiones con la barra inferior: desde ella se llega al resto. Diagnóstico pasa a ser secundaria de Ajustes, como prevé `BottomNav/README.md` («Marchas y Diagnóstico se abren desde Ajustes»). Su sección Conexiones se queda: muestra el estado y busca, útil al depurar. `salirDiagnostico()` se sigue llamando al salir de la pantalla, vaya adonde vaya.

Es una decisión de UX que el usuario valida en la maqueta (puerta de la etapa 1). Si prefiere que los chips sigan abriendo Diagnóstico, cambian `ride-screen`, `screen-mockups` y esta decisión, y el resto del diseño no.

### 3. `Sesion.accion(accion)` para la pantalla

Se separa `gesto(g)` en buscar la acción y `ejecutar(accion, tipo, latencia)`. El Square llama a `ejecutar` con el tipo de su gesto; la pantalla, con `Sesion.accion(a)`, que pone la acción en el hilo de control y la ejecuta con tipo `Pulsar` (vibración corta) y sin latencia de palanca. Así:
- `+` y `−` son `Accion.MarchaMas(1)` y `Accion.MarchaMenos(1)`: en SIM cambian la marcha con sus topes (también el automático de la Shimano) y en ERG pasan por `AjusteErg` como ±10 W, sin código nuevo.
- Los avisos, la vibración y el registro son los mismos.

Mientras dura la captura de Asignar acción, `accion` no ejecuta nada, igual que el Square.

Alternativa descartada: que el `ViewModel` de Rodaje llame directamente a `Cambio` y `ControlRodillo`. Rompe el único dueño del estado (`arquitectura.md` § 1, principio 2).

### 4. Pantalla anterior o siguiente como evento

`Accion.PantallaAnterior` y `PantallaSiguiente` no cambian el estado de la `Sesion`: emiten `EventoRodaje.Pantalla(siguiente)` y vibran como cualquier acción. `MainActivity` recoge los eventos con `repeatOnLifecycle(STARTED)` y navega a la pestaña anterior o siguiente a la actual (o a la del origen de la secundaria abierta), en orden circular. Con la app en segundo plano nadie recoge el evento y, como el `SharedFlow` no tiene réplica, se pierde: es lo que pide la spec («App en segundo plano»).

`AppNavegacion` navega siempre en el hilo principal (`withContext(Dispatchers.Main.immediate)`): Navigation lo exige, y al implementar se vio que, sin ello, dos cambios seguidos dejaban entradas de la pila a medio crear y la app fallaba al cerrarse (`IllegalStateException`).

La pestaña de cada destino sale de una función pura, `Pestana.de` (Marchas, Datos en pantalla y Diagnóstico → Ajustes; Asignar acción → Botones), y la vecina, de `Pestana.vecina`, probadas en `NavegacionTest`.

Combinación por defecto: Campag. I + cruceta ← / → con gesto `Pulsar`, sin espera (F3.4). Con el Square emparejado en Zwift, la cruceta también llega a Zwift (anexo A.3): la pantalla de Zwift puede moverse por sus menús a la vez. Es el mismo límite conocido de cualquier botón de Zwift reasignado, y se resuelve con el mando emulado (change 13).

### 5. Datos de la sesión en `:nucleo`

`DatosSesion` (dominio puro) recibe muestras con marca de tiempo monotónica y da el estado de los datos:

| Dato | Cálculo |
|---|---|
| Potencia | Media ponderada por tiempo de las muestras de `0x2A63` en los últimos 3 s; a 0 si no hay muestras en 3 s |
| Tiempo | Suma de los intervalos entre muestras con cadencia > 0, cada uno recortado a 2 s para no contar los huecos de una desconexión |
| Potencia media | Media ponderada por tiempo de la potencia en esos mismos intervalos |
| Potencia máxima | Máximo de la potencia de 3 s |
| W/kg | Potencia de 3 s / peso del ciclista, a un decimal |

La `Sesion` le pasa cada muestra en el hilo de control, al llegar la telemetría, y publica el resultado en `EstadoRodaje`. Se crea con la `Sesion`, así que una sesión nueva empieza de cero. Con el Neo desconectado no llegan muestras: tiempo, media y máxima se quedan como estaban. Zwift sigue recibiendo la potencia instantánea; la media de 3 s es solo para la pantalla.

Alternativas descartadas: la media de 3 s en el `ViewModel` (se perdería al girar y no se probaría con reloj virtual) o con las muestras de 4 Hz de la página FE-C 0x19 (habría que decodificarla; con `0x2A63` a ~1 Hz, una ventana de 3 s tiene 3 muestras, suficiente para suavizar).

### 6. Configuración versión 4

```kotlin
enum class DatoRodaje { POTENCIA, CADENCIA, TIEMPO, W_KG, POTENCIA_MEDIA, POTENCIA_MAXIMA }

// Nuevos campos de Configuracion, con valor por defecto:
val datos: List<DatoRodaje> = listOf(POTENCIA, CADENCIA),
val botonesEnPantalla: Boolean = false,
val pesoCiclistaKg: Int = 75,
```

- `datos` se normaliza al leer: sin repetidos, como mucho 6, y vacía → la de por defecto. Un valor desconocido (por ejemplo, una `VELOCIDAD` futura en una configuración importada) se descarta, con un serializador que ignora los elementos que no reconoce, como `ReglasSerializer`.
- `pesoCiclistaKg` se recorta a 40–150 y `pendienteBasePct` a −5,0–15,0 al leer, como los tiempos de los gestos.
- La migración de la versión 3 añade a la lineal y a cada desarrollo guardado las reglas de pantalla con `conReglaSiLibre`, una sola vez, como `migrarV2` con △ larga.
- `mostrarAccion`, `vibracion` y `pendienteBasePct` ya existían y conservan su nombre.

Valores por defecto de «Al rodar» (decisión abierta 6, que se cierra): datos potencia y cadencia, botones − y + desactivados, mostrar la acción y vibrar activados.

### 7. Ajustes aplicados en caliente

La `Sesion` ya observa la configuración. Se añade:
- `pendienteBasePct`: si cambia, se pasa a `ControlRodillo` como nuevo terreno de `SimLibre` (y del `debajo` de `ErgManual` o `Manual` si es `SimLibre`), y se envía al Neo si el comando cambia. En `SimZwift` solo se guarda para cuando Zwift se vaya.
- `vibracion`: ya se consulta en cada vibración.
- `mostrarAccion`, `datos`, `botonesEnPantalla` y `pesoCiclistaKg`: solo los usa la UI; `RodajeViewModel` combina la configuración con el estado y deja pasar solo los topes cuando `mostrarAccion` está desactivado.

### 8. Olvidar un dispositivo

`Sesion.olvidar(tipo)` pone a `null` el `rodillo` o el `mando` de `estado.json`, cancela el intento de conexión en curso (directo o `autoConnect`) y desconecta el cliente. Su estado pasa a `SinConexion`, sin intento hasta la próxima búsqueda. Si es el Neo, `ControlRodillo` no envía nada más (no hay cliente), y la marcha no cambia. No hace falta nueva versión de `estado.json`: los dos campos ya son opcionales.

### 9. Datos en pantalla: orden sin dependencias

La lista ordenable se hace con `detectDragGesturesAfterLongPress` sobre el asa y `Modifier.semantics { customActions = [Subir, Bajar] }` para el lector de pantalla (spec `settings-screen`, «Ordenar sin arrastrar»). Son seis filas de alto fijo: el índice destino sale del desplazamiento dividido por el alto de fila.

Alternativa descartada: una biblioteca de listas ordenables (`sh.calvin.reorderable`). Resuelve listas largas con desplazamiento automático, que aquí no hacen falta, y añade una dependencia por seis filas.

### 10. Componentes del sistema de diseño

- `BottomNav` y `MetricGrid` (`sq-ride-mini`) ya están en `design-system/` y en la guía; solo falta su composable.
- La fila de dato con casilla y asa (`sq-item` + `sq-check` + `sq-handle`, `.is-off`) existe en `components.css` pero no está documentada: se documenta como variante de `SettingsList` (README y `preview.html`) antes de usarla.
- Los botones − y + de Rodaje usan `sq-btn ride` (72 dp), ya maquetados en `rodaje.html`.
- No hay tokens ni colores nuevos.

## Risks / Trade-offs

- [Los chips dejan de abrir Diagnóstico, al que el usuario está acostumbrado] → Se valida en la maqueta (decisión 2); Diagnóstico queda a dos toques (chip → Ajustes → Diagnóstico, o Ajustes directamente).
- [Campag. I + cruceta también mueve los menús de Zwift con el Square emparejado] → Límite conocido hasta el mando emulado (decisión 4); se comprueba en la prueba con Zwift y, si molesta, el usuario puede reasignarlo.
- [La potencia de 3 s retrasa lo que se ve respecto a lo que se nota] → Es lo habitual en los ciclocomputadores; si el usuario lo prefiere instantáneo, es un cambio de la ventana sin tocar specs de otros datos.
- [Seis datos con los botones − y + en vertical] → No cabían a 390 × 844: los botones quedaban por debajo del borde. Con los botones, la vertical se compacta (separación `space-4`, cifra a 104 px y cassette de 40 px, documentado en `RideScreen`); medido en la maqueta, los botones terminan a 828 px. La app reproduce esa compactación, y `MetricGrid` baja de tamaño si un valor no cabe.
- [Letra del sistema grande] → Visto en el OnePlus 8 del usuario: con seis datos en vertical, las cifras se cortaban por abajo y el pie con «Toca Neo o Square para conectarlos» las tapaba. `MetricGrid` usaba un tamaño fijo por número de datos; ahora mide la etiqueta y la cifra más larga con la escala de letra del móvil y baja el tamaño hasta que caben (spec `ui-components`, «Valor largo»). La vista previa de Datos en pantalla crece con la letra en lugar de tener 132 dp fijos.
- [Olvidar el Neo a mitad de un envío] → La escritura en curso termina o se descarta por su tiempo máximo (`arquitectura.md` § 6.2); no queda ningún comando pendiente tras desconectar.

## Migration Plan

- `configuracion.json` pasa a la versión 4 al leerse (decisión 6); se prueba con un documento de ejemplo de la versión 3, otro con la cruceta ocupada y otro con un dato desconocido.
- `estado.json` no cambia de versión.
- Sin vuelta atrás automática: una app anterior lee la versión 4 ignorando los campos nuevos (`ignoreUnknownKeys`) y sin las acciones de pantalla, que `ReglasSerializer` descarta.

## Open Questions

- Cómo de grande es el valor más largo previsto en una rejilla de 6 en vertical («1:12:40» en tiempo): se comprueba en la maqueta a 360 dp y en el OnePlus 8.
