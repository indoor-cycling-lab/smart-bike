# Tasks

## 1. Maqueta

- [x] 1.1 En `design-system/`, dar de alta la fila de marcha con su paso a paso de 0,1 % (variante compacta de `SettingsList`/`Stepper`, con la marcha puesta marcada por algo más que el color): `components.css`, carpeta con `README.md` y `preview.html`; regenerar con `python3 scripts/design-system/generar.py` y comprobar que la guía la muestra en los dos temas y que el contraste pasa
- [x] 1.2 Maqueta de Ajustes (`maquetas/ajustes.html`): «Modelo de resistencia», «Tabla de marchas», «Dificultad», «Potencia mínima» y «Peso de la bici», con los estados `simple`, `tabla` y `fisico` en el selector de `revision.js`; verificar abriéndola en los dos temas
- [x] 1.3 Maqueta `maquetas/tabla-marchas.html` con los estados `lineal` y `shimano`, y enlazarla desde Ajustes y desde `maquetas/index.html`; verificar que las 24 filas caben y se desplazan en el marco del móvil, en los dos temas
- [x] 1.4 Maqueta `maquetas/datos.html` con los ocho datos, el máximo de seis y las casillas deshabilitadas; verificar los estados de 1, 2, 4 y 6 datos
- [x] 1.5 Maquetas de rodaje (vertical y horizontal) con los estados `velocidad` y `velocidad-sin-valor`, y de Diagnóstico con el estado `fisico` y la limitación por la potencia mínima; verificar que el área de datos no cambia de tamaño
- [x] 1.6 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/ajustes.html#estado=tabla`, `#estado=fisico`, `maquetas/tabla-marchas.html#estado=lineal`, `maquetas/datos.html`, `maquetas/rodaje-vertical.html#estado=velocidad`, `maquetas/diagnostico.html#estado=fisico` y la sección de la guía) y esperar su aprobación explícita; si pide cambios, ajustar maqueta, `design.md` y specs

## 2. Modelos en el núcleo

- [x] 2.1 `ModeloTabla` en `dominio/Resistencia.kt`, con la tabla por defecto igual al reparto del simple de cada transmisión y el orden de la escalera (lineal por posición, platos por `ordenadas`); test: tabla sin editar igual a `ModeloSimple` en las 24 marchas de la lineal y de la Shimano, y una marcha editada solo cambia esa
- [x] 2.2 `ModeloFisico` (potencia de equilibrio, relación de la lineal repartida entre 1,00 y 4,73, valores por defecto de Crr, cw y viento); test: marcha más dura, más cadencia, más pendiente y más peso suben la potencia, y los ejemplos de la spec (relación 2,0 a 90 rpm = 22,6 km/h)
- [x] 2.3 Velocidad y distancia del físico (cálculo y acumulación acotada a 2 s por muestra); test: 10 min a 24 km/h ≈ 4 km, sin avance con cadencia 0 y sin avance si no hay velocidad

## 3. `ControlRodillo`

- [x] 3.1 Dificultad global en simple y tabla (escala la suma antes del suelo de «Bajadas») y en el físico (escala la potencia); test con el reloj virtual: 150 % → +7,5 % en la 17, 50 % → mitad de potencia, sin efecto en ERG ni Manual
- [x] 3.2 Entrada de cadencia `alCambiarCadencia` con suavizado de cadencia y de pendiente, potencia mínima, alivio por debajo de 40 rpm, rampa (100 W/s sube, 200 W/s baja) y límite de 4 Hz con umbral de 2 W; test de cada protección con reloj virtual y con una pendiente en zigzag
- [x] 3.3 El cambio de marcha se salta la rampa y el límite de frecuencia y reinicia el estado del físico; test: la potencia nueva sale en la misma llamada (NF1)
- [x] 3.4 Cambio de modelo en caliente (simple ⇄ tabla ⇄ físico), con y sin Neo, y en ERG o Manual sin enviar nada; test de los escenarios de «Tres modelos de resistencia»

## 4. Configuración

- [x] 4.1 Campos `modelo`, `dificultadPct`, `potenciaMinimaW`, `pesoBiciKg` y `tablas` opcionales con valor por defecto, recortados al leer, y `DatoRodaje.VELOCIDAD` y `DISTANCIA` (`velocidad`, `distancia`); test: documento anterior sin los campos, valores fuera de límites, tabla con el número equivocado de marchas, y lista de datos con un valor desconocido
- [x] 4.2 `Accion.SiguienteModelo` sin botón por defecto, fuera del ajuste de ERG y con su texto; test: aparece después de «Pantalla siguiente» en todas las transmisiones y recorre simple → tabla → físico → simple

## 5. Sesión

- [x] 5.1 `Sesion` crea el modelo según la configuración y la transmisión, lo cambia en caliente (Ajustes y acción), pasa la cadencia del Neo a `ControlRodillo`, envía la potencia al Neo y reenvía al conectar; test de la `Sesion` con el Neo simulado: cambio de modelo rodando, Neo ausente y reconexión
- [x] 5.2 Velocidad y distancia de la sesión (`—` sin físico o sin Neo, distancia que se conserva, cero en cada sesión) y aviso «Modelo: …» con «Mostrar la acción»; test de los escenarios de velocidad y distancia
- [x] 5.3 Registro de sesión con el modelo y la potencia calculada, y la latencia de la palanca en el físico; verificar con un test que el registro guarda el modelo y la potencia

## 6. Interfaz

- [x] 6.1 Ajustes: «Modelo de resistencia» (lista de tres con ayuda), «Dificultad», «Potencia mínima» solo con el físico, «Tabla de marchas» solo con «Por tabla», y «Peso de la bici»; los textos en español con tildes; test de `AjustesUiState` y `AjustesTest` con los estados de la maqueta
- [x] 6.2 Pantalla «Tabla de marchas» con una fila por marcha, la marcha puesta marcada, el paso a paso de 0,1 % con límites y «Restablecer», aplicada en caliente; test de `UiState` y de la pantalla con los estados `lineal` y `shimano`
- [x] 6.3 Datos en pantalla con los ocho datos, el máximo de seis y las casillas deshabilitadas al llegar a él; Rodaje con velocidad y distancia y «—» cuando no hay valor; tests de `DatosUiState`, `RodajeUiState`, `DatosTest` y `RodajeTest`
- [x] 6.4 Diagnóstico con el modelo, la potencia calculada, la mínima, la cadencia suavizada, la velocidad, la dificultad y las indicaciones de límite; test de `DiagnosticoUiState`
- [x] 6.5 Ejecutar `./gradlew :nucleo:test :app:testDebugUnitTest` y `python3 scripts/design-system/generar.py`, y comprobar que `TokensSincronizadosTest` e `IconosSincronizadosTest` pasan
- [x] 6.6 Desplegar en el OnePlus 8 con la skill `desplegar-movil`, abrir los Ajustes y Rodaje con cada modelo y comprobar que las pantallas reproducen la maqueta aprobada

## 7. Documentación

- [x] 7.1 Actualizar la documentación: revisar `docs/funcional.md` (F6.2–F6.4, F11.3, F12.5, decisiones 2 y 12 y abierta 8), `docs/arquitectura.md` (§ 5.4, 5.5, 10 y 15), `docs/planificacion.md` (fila 15 y decisiones pendientes) y `docs/desarrollo.md`, y verificar que no queda en ellas ninguna referencia a que la velocidad o el modelo físico están pendientes
- [x] 7.2 Marcar en `docs/planificacion.md` las pruebas con el equipo real que quedan por hacer (las 4 de «Validación con el equipo real» de `design.md`) y verificar que cada una figura como pendiente y no como confirmada

## Workflow follow-up

- Pruebas con el Neo y Zwift (el usuario): potencia del físico mientras cambia la cadencia, parar y arrancar, subida/llano/bajada en los tres modelos y velocidad contra Zwift. Con su resultado se elige el modelo predeterminado y se cierra la decisión abierta 8.
- Archivar con `/openspec-archive-change` tras la revisión; el change se marca «Archivado» en `docs/planificacion.md`.
