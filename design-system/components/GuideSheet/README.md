# GuideSheet

Hoja inferior de Rodaje con la guía de inicio, en tres páginas por las que se avanza y se retrocede: **1. qué es Smart Bike** (rodillo, controlador y app de ciclismo indoor, en genérico y con un ejemplo cada uno) y **2. Para empezar** (los tres pasos naturales, `GuideStep`: empareja los dispositivos, conecta tu app de ciclismo indoor y ajusta la app, con el estado real de los dos primeros). Se ve mientras falte algo y no esté descartada; se oculta sola cuando los tres pasos están hechos a la vez.

**Recibe:** la página actual (1 a 3), el estado de los tres pasos y si está abierta o minimizada.

**Hoja abierta** (`sq-guide sq-guide--sheet`)
- Sale desde abajo, flota sobre Rodaje y no mueve nada. Asa (`sq-guide__grip`) arriba; se puede arrastrar hacia abajo para minimizarla y hacia arriba para abrirla.
- Cabecera: indicador de página (puntos, el activo más ancho, y «1 de 3» en texto) y, a la derecha, **Minimizar** (`expand_more`, 48 × 48 dp) y **Cerrar** (`close`, 48 × 48 dp).
- Página 1: «Smart Bike», una línea de qué conecta y tres elementos con su rol, el ejemplo y una frase; el controlador y la app llevan «Opcional». Cierra con una línea de lo que hace la app. Sin tutorial de botones ni de marchas.
- Página 2: «Para empezar» y tres `GuideStep` en el orden natural: **1. Empareja tus dispositivos** (abre Dispositivos; hecho con el rodillo y el controlador conectados), **2. Configura la conexión** (opcional; abre Conexión, donde se elige el modo; sin estado) y **3. Ajusta la app** (abre Ajustes: tipo de marcha, botones, pantalla…; sin estado). La página 1 dice qué es cada pieza; la 2 dice qué hacer, sin repetirla.
- Página 3: «Conecta Smart Bike a tu app de ciclismo indoor». Dice el modo de conexión elegido en Conexión, el estado de la conexión con la app (`sq-gstate`: «Antes, empareja el rodillo» si no hay rodillo configurado, «Esperando a tu app» si el rodillo se ofrece, «App conectada») y sus pasos numerados (`sq-gnum`), que cambian con el modo:
  - **Wifi + Bluetooth:** móvil en la misma red wifi que el equipo de la app; elegir «Smart Bike» como potencia, cadencia y rodillo; con controlador, elegirlo en la lista de mandos (nombre Bluetooth del móvil) sin emparejar el Square en la app.
  - **Solo Bluetooth:** activar el Bluetooth del equipo (sin wifi); elegir el móvil como rodillo, potencia y cadencia; con controlador, elegirlo también como mando (va en el mismo dispositivo).
  - **Solo wifi** (modo Zwift Ride, solo Zwift): misma red wifi; elegir «Smart Bike», un solo dispositivo con rodillo, pulso y controlador; Zwift mostrará su propia marcha.

  No lleva botones hacia otras pantallas: el modo se ve y se cambia en Conexión, que se abre desde el paso «Configura la conexión» de la página 2. El nombre «Smart Bike» sale del ajuste «Nombre ante la app».
- Pie: «No volver a mostrar» a la izquierda y la navegación a la derecha (`sq-guide__nav`): **Siguiente** (página 1), **Atrás** y **Siguiente** (página 2), **Atrás** (página 3). Si no caben en una línea, «No volver a mostrar» pasa a la suya. Deslizar a los lados cambia de página.
- En horizontal (`sq-guide--wide`) los tres elementos van en columnas, para que la hoja no pase de la mitad del alto.

**Hoja minimizada** (`sq-guide is-compact`)
- Una línea de 48 dp que **ocupa el sitio del pie** (`sq-ride__foot`) mientras la guía está a la vista, en lugar del texto «Toca las conexiones para conectar el Neo y el Square»: «Para empezar» y debajo, en pequeño, lo siguiente por hacer («Empareja tus dispositivos» mientras falte el rodillo o el controlador; después, «Conecta tu app de ciclismo indoor»), un chevron hacia arriba y la equis. Sin contadores: los puntos y «1 de 3» solo están en la hoja abierta, donde sí hay páginas. Tocarla reabre la hoja en la página en la que estaba. No añade ninguna línea al reparto: solo el pie pasa de 40 a 48 dp.
- Con la hoja abierta, la minimizada sigue en el pie, oculta (`is-covered`), para que la marcha no se mueva al abrir ni al cerrar.
- Los avisos que usan el pie (`ActionToast` tras una acción, `ExitNotice` va flotante y no lo usa) tienen prioridad: mientras dura el aviso, ocupa el pie y la tira vuelve después.

**Cerrada:** la equis la oculta hasta que se vuelva a abrir la app; «No volver a mostrar» la oculta y lo recuerda.

**Reglas**
- Un paso pendiente no es un error: `ink-muted`, nunca `danger`.
- Todos los objetivos táctiles miden `touch-min` (48 dp) como mínimo.
- Accesibilidad: la hoja es un diálogo no modal (`role="dialog"`) con la página en su nombre («página 1 de 3»); los pasos se leen como lista con su estado.
- No usarla como pantalla de bienvenida bloqueante ni como tutorial de la interfaz.
