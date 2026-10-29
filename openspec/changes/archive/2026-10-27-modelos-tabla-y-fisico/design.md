# Design

## Context

- Hoy `ModeloSimple` es el único `ModeloResistencia` (`dominio/Resistencia.kt`). `ControlRodillo` ya aplica el suelo de `ModoBajadas` a la `Pendiente` que devuelve el modelo, deduplica comandos y limita el terreno de Zwift (ventana de 250 ms y umbral de 0,1 %). `Sesion` crea el modelo al cambiar de transmisión (`cambiarModelo`).
- El modelo físico devuelve una `Potencia` que depende de la cadencia, que cambia continuamente: `ControlRodillo` hoy solo se entera de los cambios de marcha, de terreno y de modo. Hace falta una entrada de cadencia y un límite de envíos (arquitectura § 6.2: como máximo 4 Hz y solo si el comando cambia de forma apreciable).
- `Terreno` ya lleva `crr`, `cw` y `vientoMs` (los envía Zwift con el `0x11`); sin Zwift solo hay pendiente.
- `EstadoMarcha` es `Numerada` o `PlatoPinon` con su `relacion`, de modo que el físico no depende de cómo se define la transmisión. La lineal no tiene relación real.
- Contexto de decisiones: decisión 2 (simple por defecto), 12 (velocidad con el físico) y abierta 8 de `funcional.md`. Ver `proposal.md`.

## Goals / Non-Goals

**Goals:**
- Tres modelos intercambiables en caliente, comparables en el Neo con Zwift, sin reiniciar la sesión ni cambiar la marcha.
- Las protecciones del físico en `ControlRodillo` (arquitectura § 5.5), probadas con reloj virtual en `:nucleo`.
- Velocidad y distancia coherentes con la marcha elegida.

**Non-Goals:**
- Calibrar el físico contra Zwift ni enviar a Zwift la velocidad calculada.
- Cambiar el comportamiento de ERG (de Zwift o manual) ni del control manual de Diagnóstico.
- Un editor de tablas avanzado (curvas, importar, exportar).

## Decisions

### 1. Cada modelo es una clase de `:nucleo` con la misma interfaz; el filtrado va en `ControlRodillo`

`ModeloTabla` y `ModeloFisico` implementan `ModeloResistencia` sin estado: dado terreno, cadencia y marcha devuelven el comando «crudo». Todo lo que depende del tiempo (suavizado, rampa, alivio, límite de frecuencia) vive en `ControlRodillo`, como ya dice la arquitectura, para probarlo con reloj virtual y poder aplicarlo a otros modos si hace falta.
*Alternativa:* modelo con estado y filtros dentro. Descartada: mezcla cálculo y tiempo y obliga a recrear el modelo, perdiendo los filtros, en cada cambio de transmisión.

### 2. Tabla: un ajuste por marcha, una tabla por transmisión, con el reparto del simple por defecto

`ModeloTabla` da `pendiente_terreno + tabla[marcha]`. La tabla es una lista de porcentajes en el orden de la escalera (la lineal por posición; los platos, `ordenadas` de la más ligera a la más dura), de −20,0 % a +20,0 % en pasos de 0,1 %. En la configuración, `tablas: Map<idTransmision, List<Double>>` guarda solo las editadas; una ausente, o con un número de entradas que no encaja con la transmisión, se lee como el reparto del modelo simple de esa transmisión (con su incremento y su neutra). «Restablecer» borra la entrada.
- Los valores iniciales son los del simple, así que cambiar a «Por tabla» sin editar no cambia la resistencia: la comparación parte de una base conocida.
- El suelo de «Bajadas» se aplica igual que al simple (es una `Pendiente`).
*Alternativa:* tabla global por número de marcha. Descartada: 24 marchas de la lineal y 24 combinaciones de la Shimano no se corresponden.

### 3. Físico: potencia de equilibrio a la velocidad que fijan la cadencia y la marcha

`v = cadencia × relación × circunferencia / 60` (m/s), con circunferencia fija de 2,096 m (700×25c; un ajuste de rueda queda para otro change). La potencia es
`P = v × (m·g·(sen θ + Crr·cos θ) + cw·(v + viento)·|v + viento|) / η` con `m` = peso del ciclista + de la bici, `θ = atan(pendiente/100)`, `η = 0,975` (transmisión) y, si Zwift no manda `crr`, `cw` o viento, los valores que envía al conectar: Crr 0,004, cw 0,51 kg/m y viento 0. Un `P` negativo se lleva a 0 antes de la potencia mínima.
- **Relación de la lineal:** no tiene relación real, así que se reparte geométricamente entre 1,00 (36×36) y 4,73 (52×11), el recorrido de la Shimano 105 del usuario, de la marcha 1 a la última. Provisional; se revisa en la prueba.
- **Sin Zwift:** terreno = «Pendiente sin Zwift» de Ajustes, con Crr y cw por defecto.
- **Comando:** `Potencia(vatios)`; el Neo la mantiene sea cual sea la cadencia, y por eso el físico recalcula con la cadencia (decisión 4). El suelo de «Bajadas» no se aplica a una potencia: su función la cumple la potencia mínima.
*Alternativa:* enviar una pendiente calculada por el físico. Descartada: el motivo del modelo es que la cadencia y la marcha cambien la carga como en una bici, y el Neo en modo de pendiente ya hace su propio cálculo con su masa y velocidad.

### 4. Protecciones del físico en `ControlRodillo`

`ControlRodillo` recibe cada muestra de cadencia (`alCambiarCadencia(rpm, marcha, tMs)`, que `Sesion` llama con la telemetría del Neo, ~4 Hz) y devuelve el comando a enviar o `null`. Pasos, en este orden:
1. **Suavizado** de cadencia y de pendiente de Zwift con media móvil exponencial (constantes de tiempo provisionales de 1 s y 2 s). La pendiente se suaviza porque Zwift la manda con zigzag.
2. **Potencia calculada** por el modelo con los valores suavizados, escalada por la dificultad.
3. **Alivio con cadencia baja:** por debajo de 40 rpm, la potencia pasa linealmente de la calculada (a 40 rpm) a la mínima (a 0 rpm), para no frenar en seco al parar.
4. **Potencia mínima** de Ajustes (0–150 W, 5 W, 30 W provisional) como suelo.
5. **Rampa:** la potencia enviada sube como mucho 100 W/s y baja 200 W/s respecto a la anterior. **Un cambio de marcha se salta la rampa** (la nueva potencia sale al instante, NF1) y reinicia su estado.
6. **Envío:** como mucho 4 Hz y solo si difiere 2 W o más de lo enviado, como la ERG; el cambio de marcha no espera.
Todas las constantes son provisionales y viven juntas en un objeto, para ajustarlas tras la prueba con el Neo.
*Alternativa:* un temporizador propio de `ControlRodillo`. Descartada: la telemetría ya llega a ~4 Hz y `Sesion` es el único hilo de control (arquitectura § 6.1); con cadencia 0 el Neo sigue notificando.

### 5. Dificultad: escala el total, antes del suelo de «Bajadas»

Con simple y tabla, `pendiente = (terreno + ajuste de la marcha) × dificultad`, y después se aplica «Bajadas». Con el físico se escala la potencia calculada. La dificultad no afecta a ERG ni a Manual. Va de 50 % a 150 % en pasos de 5 % (100 % por defecto). Escalar el total, y no solo el terreno, hace que la dureza sea global y que en la marcha de referencia con terreno plano (0 %) no cambie nada, que es lo esperable.
*Alternativa:* escalar solo el terreno, como la dificultad de Zwift. Descartada: con Zwift ya existe esa dificultad; este ajuste es de la app y debe valer también sin Zwift. La recomendación de qué dejar a 100 % se da tras la prueba.

### 6. Velocidad y distancia las calcula el físico y se muestran solo con él

`Sesion` expone `velocidadKmh` (la `v` de la decisión 3 con la cadencia suavizada) y acumula la distancia mientras se pedalea (cadencia > 0, con cada muestra acotada a 2 s). Con simple o tabla, o sin Neo, la velocidad se muestra «—»; la distancia conserva su valor. Si se cambia de modelo a mitad de sesión, la distancia solo avanza mientras está el físico. Empieza en cero con cada sesión, como el tiempo.
*Alternativa:* calcularla siempre por cadencia y relación. Descartada: con simple o tabla la resistencia no sigue esa velocidad y el dato no sería coherente con lo que se siente (decisión 12).

### 7. Configuración: campos opcionales con valor por defecto, sin subir la versión

`modelo` (`simple` por defecto), `dificultadPct` (100), `potenciaMinimaW` (30), `pesoBiciKg` (8, entre 5 y 25) y `tablas` son opcionales y con valor por defecto, como `bajadas`: un documento anterior se lee igual. `DatoRodaje` añade `velocidad` y `distancia`; la lista de datos ya descarta los desconocidos, de modo que una versión anterior de la app abre la configuración sin ellos (el máximo de seis en pantalla no cambia). Los valores fuera de rango se recortan al leer.

### 8. Acción «Siguiente modelo»

Nueva `Accion.SiguienteModelo`, sin botón por defecto, que recorre simple → tabla → físico → simple, y se anuncia con el aviso de la acción («Modelo: Físico»). Se guarda como el ajuste. Como `AlternarErgManual`, no la cuenta el ajuste de ERG. En ERG o control manual, el modelo cambia pero no se envía nada hasta volver a SIM.

### 9. Interfaz: ajustes en Rodillo, pantalla propia para la tabla

- **Ajustes → Rodillo:** «Modelo de resistencia» (lista de tres opciones con ayuda, como «Bajadas»), «Dificultad» (stepper), «Potencia mínima» (stepper, solo con el físico elegido para no mostrar un ajuste sin efecto) y, bajo el modelo, «Tabla de marchas» (entrada a su pantalla, solo con «Por tabla»). **Ajustes → Ciclista:** «Peso de la bici».
- **Tabla de marchas:** lista de la más ligera a la más dura con una fila por marcha (nombre de la marcha como en Rodaje y stepper de 0,1 %) y «Restablecer». Reutiliza el `Stepper` y las filas de `SettingsList`; si hace falta una variante compacta, se añade antes a `design-system/`.
- **Datos en pantalla:** la lista pasa de 6 a 8 datos; se mantiene el máximo de 6 marcados y la casilla de un dato no marcado queda deshabilitada cuando ya hay 6, con su ayuda.
- **Rodaje** no muestra el modelo (AGENTS.md, `ride-screen`): solo la velocidad y la distancia como datos.
- **Diagnóstico** ya muestra el ajuste de la marcha; añade el modelo y, con el físico, la potencia calculada, la mínima aplicada y la velocidad.

## Risks / Trade-offs

- [El Neo tarda en seguir una potencia que cambia a 4 Hz, o la mantiene a trompicones] → Rampa, umbral de 2 W y límite de 4 Hz; se valida con el Neo (a validar, abajo) y las constantes son ajustables.
- [El físico no se siente como Zwift: la lineal se asigna a un rango de relaciones inventado] → La relación de la lineal es provisional; la prueba compara con la Shimano 105 del usuario, que sí tiene relaciones reales.
- [Parar de pedalear en una bajada deja al Neo sin carga y la rueda se pasa] → Potencia mínima y alivio con cadencia baja; si hace falta, se sube la mínima.
- [Dos protecciones para las bajadas (suelo de «Bajadas» y potencia mínima) confunden] → Cada una aplica a su modelo; la ayuda de «Potencia mínima» dice que es del modelo físico.
- [Más datos de pantalla que caben] → El máximo sigue en 6; la rejilla fija ya cubre ese caso.

## Migration Plan

Sin migración de datos: los campos nuevos son opcionales (decisión 7). Para volver atrás basta con elegir «Simple»; una versión anterior de la app ignora lo nuevo.

## Validación con el equipo real (a validar)

1. Con el Neo, sin Zwift, en físico: pedalear a cadencia constante y cambiar de marcha; la resistencia cambia al instante y la potencia mostrada sigue la calculada. Medir la latencia de la palanca (NF1).
2. Parar y arrancar de pedalear: la rueda no se queda sin freno ni se atasca (alivio y potencia mínima).
3. Con Zwift (PC): subida, llano y bajada en los tres modelos; el físico no oscila con el zigzag de la pendiente. Elegir el predeterminado y revisar el incremento y la neutra del simple (decisión abierta 8).
4. Contrastar la velocidad mostrada con la de Zwift en llano a una cadencia conocida; solo se anota la diferencia, no se corrige.

## Open Questions

- Las constantes de las protecciones (tiempos de suavizado, 40 rpm, rampas, 30 W) se afinan con la prueba; no cambian ni specs ni tareas.
