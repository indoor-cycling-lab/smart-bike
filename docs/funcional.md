# Documento funcional

> Estado: **borrador para revisión**. Las fases y prioridades son una propuesta.

## 1. Visión

App Android para entrenar en rodillo con un **Tacx Neo 1** y un cuadro **Elite Square**. Usa los mandos del Square para hacer **cambio de marchas virtual** en el Neo y hace de puente con **Zwift**.

El Neo 1 no es compatible con los cambios virtuales que trae el Square. Hoy eso se resuelve con una app genérica, con muchas funciones que no se usan y una interfaz que no está pensada para este uso. Esta app cubre **solo** este caso, con una interfaz centrada en las marchas y manejable **sin tocar el móvil**.

## 2. Usuario y contexto de uso

- Un ciclista, montado en el Square sobre el Neo, con el móvil en un soporte a 1–2 m.
- Zwift corre en otro dispositivo (PC, Mac o Apple TV).
- Durante el rodaje el ciclista **solo usa los botones del Square**. El móvil se toca para configurar antes o después de rodar.

## 3. Glosario

| Término | Significado |
|---|---|
| Marcha virtual | Marcha simulada por la app. El Square no tiene transmisión real; la app traduce cada marcha en más o menos resistencia en el Neo |
| Modo Libre | Sin app de entrenamiento enviando pendiente: el terreno es llano (0 %) y solo cambia la marcha. El indicador de Rodaje dice «LIBRE» |
| Modo SIM | El rodillo simula la pendiente que envía una app de entrenamiento y la resistencia depende de la velocidad, como en la calle. Se usa también como nombre de la familia Libre + SIM («Alternar SIM ⇄ ERG») |
| Modo ERG | El rodillo fija una potencia en vatios, independiente de la cadencia |
| Puente Zwift | La app se presenta ante Zwift como un rodillo inteligente y le reenvía potencia y cadencia del Neo. Se conecta por wifi o, como alternativa, por Bluetooth |
| Mando emulado | La app se presenta ante Zwift también como un mando oficial de Zwift y le envía los botones del Square (en la app, el servicio «Controlador Zwift») |
| Capa | Conjunto de asignaciones de botones activo en un momento dado |

## 4. Funcionalidades

Cada funcionalidad tiene un identificador para referenciarla al fasear. La prioridad es propuesta:
- **M:** imprescindible.
- **S:** importante.
- **C:** deseable.
- **E:** experimental.

### F1. Conexión con los dispositivos — M

- **F1.1** Buscar y conectar el Tacx Neo y el Elite Square.
- **F1.2** Configurar los dispositivos y conectarse a ellos automáticamente al abrir la app.
- **F1.3** Reconectar solo si se pierde la conexión, sin intervención del usuario.
- **F1.4** Mostrar en todo momento el estado de cada conexión: Neo, Square y Zwift (y mando y pulsómetro, si aplican). En Rodaje, la hora, el wifi y la batería del móvil los muestra la barra de estado del sistema, que Rodaje deja a la vista (change `quitar-barra-estado-rodaje`); la app solo dice «Sin wifi» en el estado de la publicación. En Rodaje, la barra de iconos lleva solo los dispositivos a los que se conecta la app (Neo, Square —que se llama «Controlador» y lleva el icono de un manillar de bici de carretera—, pulsómetro y ventilador); el estado de la publicación va aparte, en palabras y sin nombrar la plataforma («Conectado», «Esperando conexión…», «No visible en la red», «Sin wifi», «Sin rodillo», y «· sin controlador» o «· sin pulso» si falta algo configurado), a la izquierda de la barra de iconos, y abre Conexión (change `pestana-conexion`). Los iconos de la barra van agrupados, con 4 dp entre ellos.
- **F1.5** Mantener las conexiones activas con la pantalla apagada o la app en segundo plano.

Criterios de aceptación:
- Tras la primera configuración, al abrir la app se conecta a Neo y Square sin pasos manuales.
- Una desconexión breve se recupera sola sin perder la marcha actual.

Los dos se cumplen con el Neo y el Square desde el change `conexion-automatica-en-segundo-plano`, también con la pantalla apagada. Desde `puente-zwift-wifi`, el estado de Zwift también se ve en todo momento (F1.4). Falta validar F1.5 en una sesión de 2 h (NF4), pendiente al archivar `puente-zwift-wifi`.

### F2. Lectura de los botones del Square — M

- **F2.1** Reconocer **todos** los botones del Square:
  - Palancas de cambio (dos por lado).
  - Frenos.
  - Botones Campagnolo.
  - Cruceta.
  - A, B, Y, Z.
  - Cuadrado, Triángulo, Círculo y X.
- **F2.2** Reconocer varios botones pulsados a la vez.

Criterio de aceptación: cada botón del Square produce un evento identificable en la app, incluso con pulsaciones rápidas.

### F3. Gestos — S

- **F3.1** Pulsación corta, pulsación larga y doble pulsación.
- **F3.2** Autorrepetición al mantener un botón, por ejemplo para cambiar varias marchas seguidas.
- **F3.3** Combinaciones con botones modificadores: los botones Campagnolo, que Zwift no usa. Por ejemplo, Campagnolo izquierdo + palanca izquierda 2 salta a la marcha más ligera.
- **F3.4** Las palancas de cambio responden **al instante**. Solo los botones que tienen gestos ambiguos (corta y larga, o doble pulsación) esperan a saber qué gesto es.
- **F3.5** Tiempos configurables (umbral de pulsación larga, velocidad de repetición…). Por defecto: pulsación larga 800 ms, doble pulsación 300 ms, empezar a repetir 800 ms y un cambio cada 250 ms. La larga y el retardo de repetición quedan por encima de los ~650 ms que dura una pulsación normal medida con las notificaciones del Square.
- **F3.6** Vibración del móvil distinta según el gesto, para aprenderlos sin mirar.

### F4. Asignación de botones configurable — S

- **F4.1** Asignación por defecto lista para usar, que reproduce las funciones de un mando oficial de Zwift. Las funciones propias de la app van en los botones libres o en combinaciones (ver anexo A).
- **F4.2** Reasignar cualquier botón o gesto a cualquier acción.
- **F4.3** Asignar "pulsando": la app pide pulsar el botón o gesto en el Square y lo captura.
- **F4.4** (opcional) Capas alternativas, por ejemplo una para manejar la app con el Square. La asignación por defecto no las necesita: Zwift y los adicionales conviven sin cambiar de capa. Si existen, el cambio se hace con un gesto poco accidental y se ve en pantalla.
- **F4.5** Invertir el sentido de las palancas (hay ciclistas que esperan lo contrario).

### F5. Marchas virtuales — M

- **F5.1** Subir y bajar una marcha.
- **F5.2** Saltos de varias marchas (por ejemplo ±3).
- **F5.3** Ir directamente a la marcha más ligera, a la más dura o a una marcha de referencia.
- **F5.4** Topes: al llegar a la primera o la última marcha, no se cambia y se avisa en pantalla y con vibración.
- **F5.5** Número de marchas configurable. Por defecto, transmisión lineal de 24 marchas, con la 12 como marcha de referencia. Sin «Recordar la última marcha», se empieza en la de referencia (change `transmisiones-de-marca`).
- **F5.6** Recordar la marcha al reconectar y, opcionalmente, entre sesiones.
- **F5.7** Transmisión configurable. El usuario ve y cambia marchas como en un grupo inalámbrico real, sea cual sea el modelo de resistencia que haya detrás:
  - Lineal: N marchas numeradas (por defecto, 24).
  - Desarrollos predefinidos de Shimano, SRAM y Campagnolo (platos y cassette). Por ahora, solo el del usuario: Shimano 105 2×12, platos 52/36 y cassette 11-12-13-14-15-17-19-21-24-28-32-36 (change `transmisiones-de-marca`). SRAM, Campagnolo y «A medida» llegan con el change `transmisiones-otras-marcas`.
  - A medida: uno o dos platos y un cassette con sus piñones.
- **F5.8** Modo de cambio:
  - **Lineal:** palancas derechas (1 y 2) más dura, izquierdas (1 y 2) más ligera.
  - **Grupo de marca, como el grupo (por defecto):** las palancas del Square se comportan como los mandos inalámbricos de esa marca con su configuración de fábrica, tomando la palanca 1 (la grande) y la 2 (la pequeña) de cada lado como los dos botones de cada mando:
    - Shimano Di2: derecha, piñón (un botón más ligera, el otro más dura); izquierda, plato. Con la Shimano 105, modo «Manual»: la palanca 1 derecha lleva al piñón más grande y la 2 al más pequeño; la 1 izquierda al plato grande y la 2 al pequeño, como el STI del usuario (validado en su bici).
    - SRAM AXS: derecha más dura, izquierda más ligera; las dos a la vez cambian de plato.
    - Campagnolo Super Record Wireless: derecha, piñón; izquierda, plato.
  - **Sincronizado (opcional):** derecha más dura, izquierda más ligera, y el plato cambia solo, como Synchro Shift de Shimano o Compensating Shift de SRAM. Con la Shimano 105 es el modo «Automático», el de por defecto: la derecha recorre una secuencia de 15 marchas con histéresis (sube al 52 desde el 36×15 al 52×19 y baja al 36 desde el 52×24 al 36×19, compensando 2 piñones), y la izquierda cambia el plato a mano compensando 2 piñones, como el Semi Synchro. Puntos de cambio validados por el usuario al rodar.
  - A medida: como el grupo de la marca que se elija como base, o sincronizado.

Criterio de aceptación: la resistencia del Neo cambia de forma perceptible en menos de **100 ms** desde que se pulsa la palanca.

### F6. Modelos de resistencia — M (modelo simple) / S (modelo físico)

La app ofrece varios modelos de cómo una marcha se traduce en resistencia. El usuario elige uno en ajustes y puede alternarlo con un botón.

- **F6.1 Modelo simple (pendiente + marcha):** cada marcha suma o resta un porcentaje fijo a la pendiente que llega de Zwift. Es el comportamiento al que el usuario ya está acostumbrado. El incremento por marcha es configurable (por defecto, 1 % por marcha respecto a la 12; provisional, decisión abierta 8).
- **F6.2 Modelo por tabla:** igual que el anterior, pero cada marcha tiene su propio incremento, con pasos no uniformes. Una tabla por transmisión, editable en Ajustes → Rodillo → «Tabla de marchas» (de −20,0 % a +20,0 %, de 0,1 % en 0,1 %), que parte del reparto del modelo simple (change `modelos-tabla-y-fisico`).
- **F6.3 Modelo físico:**
  - A partir de la cadencia, la relación de la marcha, los pesos y el terreno que indica Zwift (pendiente, tipo de superficie y efecto del rebufo), calcula la potencia que costaría pedalear y la fija en el Neo.
  - El efecto de cada marcha depende de la velocidad, como en una bici real.
  - Incluye protecciones:
    - Suavizado de cadencia y pendiente.
    - Potencia mínima en bajadas.
    - Alivio si la cadencia cae mucho.
    - Cambios de potencia suaves, salvo en el momento de cambiar de marcha.
- **F6.4** Ajuste de dureza global (dificultad) en la app, de 50 % a 150 % (100 % por defecto): multiplica la pendiente (terreno más marcha) en simple y tabla, y la potencia en el físico; no toca la ERG. Con «Siguiente modelo» (acción asignable, sin botón por defecto) se alterna simple → tabla → físico.
- **F6.5** Ajuste «Bajadas» para que el motor del Neo no empuje el volante con pendiente negativa. Tres modos: «Sin protección» (terreno + marcha); «La marcha no crea bajadas», por defecto, en el que la marcha nunca deja la pendiente por debajo de la del terreno, ni de 0 % en llano o subida; y «Nunca empujar», que no baja nunca de 0 %. Se aplica con y sin Zwift, no en ERG ni en el control manual. Con el Neo de pruebas, el motor empieza a empujar a −1,5 % (change `proteccion-bajadas`).

### F7. Control del rodillo sin Zwift — S

- **F7.1** Rodar sin Zwift (modo Libre): pendiente base fija (0 %) y marchas funcionando.
- **F7.2** Modo ERG manual: el Neo mantiene una potencia fijada, entre 50 y 1000 W, sea cual sea la cadencia. Las palancas la ajustan como en ERG de Zwift: 10 W por cada marcha que pedirían (±30 W con los saltos con Campagnolo), y □ corta vuelve a la potencia con la que se entró. «+10 W» y «−10 W» son acciones asignables, sin botón por defecto. Al entrar, la potencia es la última usada, también de otra sesión (150 W la primera vez). La marcha se conserva y vuelve al salir.
- **F7.3** Alternar entre Libre o SIM y ERG manual con un botón (△ larga por defecto). En una ruta de Zwift, el ERG manual manda sobre el terreno y al volver a SIM se aplica el último terreno; si Zwift empieza un entrenamiento, manda Zwift y el ERG manual termina. Durante un entrenamiento de Zwift, alternar no hace nada (change `erg-manual`). También se alterna tocando el indicador de modo de Rodaje (F11.5), si se activa «Cambiar de modo tocando» en ajustes (F12.7; desactivado por defecto): sin Square, o sin asignar la acción, es la única forma de entrar en ERG manual (change `modo-tactil-rodaje`).

### F8. Puente con Zwift — M

- **F8.1** La app aparece en Zwift como un rodillo inteligente que da potencia, cadencia y velocidad y acepta control de resistencia.
- **F8.2** Recibir de Zwift la pendiente del terreno y aplicarla junto con la marcha actual.
- **F8.3** En entrenamientos de Zwift (modo ERG), aplicar la potencia que pide Zwift. Las palancas pueden ajustar esa potencia ±W en lugar de cambiar marchas.
- **F8.4** Funciona con Zwift en PC/Mac y en Apple TV.
- **F8.5** La conexión con Zwift es por **wifi**, en la misma red local que el equipo de Zwift (modo «Wifi + Bluetooth», por defecto). Como alternativa, se puede elegir **Bluetooth** en la pestaña «Conexión» (modo «Solo Bluetooth», change `ftms-bluetooth`), por ejemplo si la red no lo permite: sin wifi, con el rodillo, el pulso y el mando en un solo dispositivo y con el nombre Bluetooth del móvil, o con el nombre ante la app si se marca «Usar el nombre también por Bluetooth» (el móvil se renombra mientras la app publica por Bluetooth y recupera su nombre al terminar, change `nombre-bluetooth-propio`). Si Zwift no reconoce el dispositivo en otra sesión, hay que quitar el anterior en su pantalla de emparejamiento y buscarlo de nuevo; con el mando dentro, lo ha reconocido (change `pestana-conexion`).

Criterio de aceptación: en Zwift se emparejan rodillo, potencia y cadencia desde la app, por wifi y por Bluetooth, y la resistencia sigue al terreno.

Desde el change `pestana-plataformas` (28-10-2026), lo que la app publica se elige en la pestaña «Plataformas» (F12.9): una plataforma (hoy Zwift) y uno de los modos de conexión que la app admite para ella, siempre uno compatible; el FTMS por Bluetooth (change `ftms-bluetooth`) ya es una fila de esa tabla y otras plataformas se añadirán como filas nuevas. Desde el change `pestana-conexion` ya no se elige plataforma: se elige el modo en la pestaña «Conexión» (F12.9).

Por wifi se cumple con Zwift en PC desde el change `puente-zwift-wifi` (26-10-2026): se empareja en las tres listas, la resistencia sigue al terreno con las marchas y la potencia y la cadencia llegan sin cortes. Quedaron pendientes de probar el entrenamiento ERG (F8.3), las desconexiones y Apple TV (F8.4). Bluetooth, en el change `puente-zwift-bluetooth`.

Desde el change `comprobar-anuncio-zwift` (27-10-2026), si Android da el anuncio por registrado pero no responde en la red, la app lo detecta y muestra Zwift «No visible», con el aviso de reiniciar el wifi del móvil o el móvil (F1.4).

### F9. Mando de Zwift emulado — S

- **F9.1** La app se presenta también ante Zwift como un **mando de Zwift**. Así los botones del Square sirven para navegar menús, girar, usar powerups, dar Ride On, etc.
- **F9.2** Con ello **no hace falta emparejar el Square en Zwift** ni conectar un teclado al PC. Con «Wifi + Bluetooth» Zwift ve dos dispositivos de la app: el rodillo por wifi y el mando por Bluetooth. El mando se llama como el móvil o, con «Usar el nombre también por Bluetooth», como el rodillo («Smart Bike»). «Solo wifi (modo Zwift Ride)» los junta en un solo dispositivo por wifi (el mando va dentro del rodillo, porque Zwift solo admite un dispositivo DirCon por IP), y «Solo Bluetooth», en un solo dispositivo Bluetooth. El mando se publica solo con un Square configurado (F12.9).
- **F9.3** Las palancas siguen controlando las marchas de la app, no las de Zwift.
- **F9.4** Dirección: desplazarse a izquierda o derecha en la calzada mientras se mantiene el botón (X/Círculo), como con las palancas de un mando oficial. En los cruces, el desvío se elige con la cruceta.
- **F9.5** Freno: mientras se mantiene una maneta de freno del Square, el avatar frena en Zwift, como con los mandos oficiales. Con las dos manetas, frena más.

Riesgo: depende de un protocolo no documentado de Zwift. El prototipo `prototipo-mando-zwift` lo validó con Zwift en PC por Bluetooth: menús, Ride On, freno y dirección funcionan (`arquitectura.md` § 7.4). El móvil tiene que poder anunciarse por Bluetooth (NF3), aunque el puente vaya por wifi.

Desde el change `mando-zwift-emulado` (27-10-2026) se cumplen F9.1 a F9.5, validados con Zwift en PC y el Square desemparejado en Zwift. Las funciones de Zwift son acciones asignables (F4.2), con las del anexo A.1 por defecto, y se activan o desactivan con «Mando de Zwift» en Ajustes (F12.9). Quedan por probar diez minutos sin pulsar nada, cerrar Zwift con un botón mantenido y apagar el Square con un freno mantenido. Zwift en Apple TV, sin probar.

Desde el change `completar-protocolo-dircon-zwift` (28-10-2026), en «Todo por wifi» se contestan con datos las páginas de información del mando (1024 y 771) y la información del dispositivo incluye serie, firmware y hardware; validado con Zwift en PC sin cambiar botones, freno ni giro. Las marchas siguen siendo las de la app (F9.3).

Desde el change `mando-zwift-dircon` (27-10-2026) existe el modo «Todo por wifi» (F12.9), que Zwift trata como un rodillo con el protocolo de rodillo de Zwift. Las marchas siguen siendo de la app (F9.3): la relación de la marcha de Zwift se ignora y su HUD muestra una marcha fija. Validado con Zwift en PC: conecta en los dos modos, se generan watts y el Square gira. Con el nombre «KICKR BIKE PRO» (`simular-kickr-bike`, hoy el selector «Nombre ante la app» de Plataformas), Zwift lista el rodillo con ese nombre; si así admite las marchas virtuales, a validar. **A validar con el equipo real:** frenar, cambiar de marcha en los tres modelos, entrenamiento ERG, cambiar de modo rodando, cerrar Zwift y volver, latencia de la palanca (NF1) y 10 minutos sin pulsar nada.

### F10. Atajos de Zwift con teclado Bluetooth — C (alternativa a F9)

- **F10.1** Si el mando emulado no funciona, el móvil se empareja con el equipo de Zwift como teclado y los botones envían atajos de teclado.

### F11. Pantalla de rodaje — M

Pantalla principal, legible a 2 m, en horizontal o en vertical: según la orientación del móvil (respeta el bloqueo de rotación del sistema) o la que se fije en Ajustes > Pantalla:
- **F11.1** La marcha puesta, muy grande: el número en transmisión lineal, o plato × piñón (`50×17`) con indicador de plato y del piñón en el cassette en una transmisión física.
- **F11.2** La pantalla no muestra nada del modelo de resistencia (pendiente de Zwift, ajuste por marcha, pendiente o potencia enviada al Neo). Eso se ve en Diagnóstico (F13.3).
- **F11.3** Datos configurables, de 1 a 6, a elegir entre potencia (media de 3 s), cadencia, tiempo, W/kg, potencia media, potencia máxima, velocidad, distancia y pendiente (nueve, con un máximo de seis a la vez; velocidad y distancia las calcula el modelo físico y con otro modelo la velocidad es «—»), en el orden que quiera el usuario (por defecto potencia y cadencia). Tiempo, potencia media y máxima son de la sesión y solo cuentan mientras se pedalea. Velocidad y distancia vuelven con el modelo físico (decisión tomada 12). Su tamaño se ajusta para aprovechar el espacio: con pocos datos, cifras más grandes; con muchos, más pequeñas, pero siempre todos visibles a la vez, sin desplazamiento ni tocar la pantalla. Excepción en una pantalla baja (Rodaje en vertical con menos de 760 dp de alto, p. ej. 360 × 640 o 360 × 720): con los botones − y + visibles (F11.8) solo se muestran los 4 primeros datos elegidos, para que la marcha siga viéndose; los demás se ocultan sin cambiar lo elegido y «Datos en pantalla» lo avisa. Sin botones, hasta 6. La pendiente es la de la simulación: la que envía la app, «0,0 %» en Libre y «—» en ERG, con signo y un decimal (+4,2 %, −7,0 %); nunca la pendiente base ni lo enviado al Neo, que son de Diagnóstico (change `modo-libre-y-pendiente`).
- **F11.4** Animación en cada cambio y aviso al llegar a un tope.
- **F11.5** Estado de conexiones, modo (LIBRE, SIM o ERG: «LIBRE» sin app que envíe pendiente, «SIM» con ella; si la app se va, «SIM» pasa a «LIBRE» a los 30 s), capa activa y transmisión elegida. El modo en ERG no añade una línea que explique las palancas. Con el ajuste «Cambiar de modo tocando» (F12.7), el indicador es un botón de 48 dp (relleno tintado y borde marcado) que alterna Libre o SIM ⇄ ERG manual (F7.3); sin él, es una etiqueta.
- **F11.6** Aviso breve de la acción realizada («+1 marcha», «Plato 50», «+10 W»), sin el botón ni el gesto. Se activa o desactiva en ajustes (F12.7).
- **F11.7** La pantalla no se apaga mientras se rueda.
- **F11.8** Botones táctiles − y + para cambiar de marcha, si se activan en ajustes (F12.7) y, siempre, con el Square no conectado (también buscando o reconectando), para no quedarse sin forma de cambiar de marcha; aparecen y desaparecen con el estado del Square y la marcha y los datos se encogen para hacerles sitio. Hacen lo mismo que +1 y −1 marcha desde el Square, o ±10 W en ERG. En una pantalla baja, con ellos Rodaje muestra solo 4 datos (F11.3).
- **F11.9** Acceso a Ajustes: un icono en la esquina superior derecha, junto a los chips de conexión, que abre Ajustes. No se usa rodando.
- **F11.10** *(retirada por el change `quitar-barra-estado-rodaje`)* La hora, el wifi y la batería del móvil los muestra la barra de estado de Android, que Rodaje ya no oculta; la app no los replica ni tiene indicadores propios. Rodaje sigue avisando «Sin wifi» en el estado de la publicación (F8.5), y el icono de Ajustes va en la línea del modo en vertical y a la derecha de la barra de conexiones en horizontal.
- **F11.11** Atrás en Rodaje no cierra a la primera: muestra 3 s un aviso flotante y translúcido («Para cerrar la aplicación, pulsa Atrás otra vez») y un segundo Atrás en ese plazo termina la sesión, con «Cerrando…» hasta que la app se cierra. «Terminar» en la notificación no pide confirmación.
- **F11.12** Guía de inicio: una hoja inferior de cuatro páginas en Rodaje que se abre sola en el primer uso (sin ningún dispositivo configurado) y, en los demás arranques con algo por hacer, aparece minimizada como una tira del pie. Página 1, qué es Smart Bike (un rodillo, un controlador y una app de ciclismo indoor, cada uno con un ejemplo y los dos últimos opcionales). Página 2, tres pasos con acceso directo a su pantalla: «Empareja tus dispositivos» (Dispositivos; con estado: pendiente, conectando, «falta el controlador» o hecho), «Configura la conexión» (opcional; Conexión) y «Ajusta la app» (Ajustes). Página 3, cómo puedes usar la app: las formas de rodar con los nombres del indicador de Rodaje (LIBRE, SIM y ERG, manual o de la app) y cuáles necesitan una app conectada (SIM y ERG APP). Página 4 (opcional), qué hacer en la app de ciclismo indoor según el modo de conexión (Wifi + Bluetooth, Solo Bluetooth o Solo wifi), con el estado de esa conexión (pendiente, «Esperando a tu app» o «App conectada»). Se minimiza, se cierra con una equis (se vuelve a ver al abrir la app) o con «No volver a mostrar» (se recuerda), desaparece sola con los dispositivos emparejados y una app conectada, y se reabre desde Ajustes («Guía de inicio»). No aparece a quien actualiza con algún dispositivo ya configurado.

Desde el change `exportar-bateria-y-giro` (27-10-2026), Rodaje avisa del giro de Zwift con una flecha a cada lado de la marcha mientras se mantiene (solo con «Mando de Zwift» activado; un giro breve se ve 0,5 s) y marca con una insignia el icono del Square cuando su batería está baja (20 % o menos, umbral provisional), sin mostrar el porcentaje. Desde el change `rodaje-completo-y-ajustes` (26-10-2026) se cumplen F11.3, F11.6 y F11.8, en horizontal y en vertical; los chips de conexión abren Dispositivos. Desde el change `chips-conexion-compactos` (26-10-2026), los chips de Rodaje (F11.5) muestran el nombre y la forma del estado, sin el estado en palabra, que se lee en Dispositivos y con el lector de pantalla; en vertical ocupan una sola línea. Desde el change `rejilla-datos-fija` (26-10-2026), los datos (F11.3) tienen un área fija (el 40 % del alto útil en vertical y del ancho útil en horizontal), con una cifra que no cambia mientras se rueda, y los botones − y + (F11.8) no cambian su tamaño: el sitio sale de lo que le sobra a la rejilla y, si no basta, de la marcha. Desde el change `acceso-ajustes-y-tema` (26-10-2026) se cumple F11.9, en horizontal y en vertical. Desde el change `barra-conexiones-iconos` (27-10-2026), el estado de las conexiones de Rodaje (F11.5) se muestra con una barra de iconos, uno por enlace (Neo, Square, Zwift y mando), en lugar de los chips: el estado va en la forma del icono (normal, atenuado con una marca girando, tachado) y toda la barra abre Dispositivos. En vertical va en la franja superior, la de la cámara y el estado del móvil, y el modo con Ajustes en la línea de debajo. Está preparada para sumar el pulso y el ventilador. Desde el change `orientacion-pantalla` (27-10-2026), la orientación de la app sigue al móvil (por defecto) o queda fija en vertical u horizontal según Ajustes, para toda la app. El change `barra-estado-rodaje` (27-10-2026) cumplió F11.10 replicando la hora, el wifi y la batería del móvil en Rodaje; `quitar-barra-estado-rodaje` lo retiró y Rodaje deja ver la barra de estado del sistema. El wifi sigue siendo el dato con el que Zwift pasa a «Sin wifi» (F1.4).

### F12. Ajustes — M

- **F12.1** Dispositivos (una sección por tipo —rodillo, controlador, pulsómetro y ventilador— con «Buscar», «Reconectar» y «Olvidar»; «Controlador» es un tipo genérico, hoy solo el Elite Square, change `renombrar-controlador`) y, en la pestaña «Conexión», lo que la app publica y cómo (F12.9). La elección de Bluetooth es el modo «Solo Bluetooth» (change `ftms-bluetooth`).
- **F12.2** Modelo de resistencia y sus parámetros:
  - Incremento por marcha o tabla.
  - Transmisión.
  - Pesos del ciclista y de la bici. El del ciclista está en Ajustes (usado en W/kg); el de la bici llega con el modelo físico (change `modelos-tabla-y-fisico`).
  - Dificultad.
- **F12.3** Marchas: transmisión, modo de cambio, marcha inicial y de referencia, recordar la última. El modelo de resistencia queda en un apartado avanzado.
- **F12.4** Botones: asignación, capas y tiempos de gestos. Se abre desde Ajustes (ya no es una pestaña, change `pestana-plataformas`).
- **F12.5** Opciones de rodillo: bajadas (F6.5) y potencia mínima. La pendiente base sin Zwift es siempre 0 % («Pendiente sin Zwift» se quitó en el change `pestana-conexion`). La potencia mínima (0–150 W, 30 W provisional), solo con el modelo físico, y el peso de la bici (5–25 kg, 8 kg) llegan con el change `modelos-tabla-y-fisico`; con el físico, «Bajadas» no se muestra.
- **F12.6** Exportar e importar la configuración: «Copia de seguridad» al final de Ajustes. Exporta todos los ajustes a un archivo JSON elegido con el selector de archivos del sistema (sin permisos) y no incluye los dispositivos configurados. Al importar se valida el archivo entero, se confirma y solo entonces se reemplazan los ajustes; un archivo no válido (vacío, ilegible, que no es una configuración o de una versión más nueva) no cambia nada y dice el motivo.
- **F12.7** Al rodar: datos en pantalla (F11.3), botones − y + en pantalla (desactivado por defecto; solo manda con el Square conectado), cambiar de modo tocando el indicador de modo de Rodaje (desactivado por defecto, independiente de los botones − y +; F7.3), mostrar la acción (activado) y vibración (activada).
- **F12.8** Pantalla: idioma (Español o English; en el primer arranque, español si el móvil está en español e inglés en cualquier otro caso, y desde entonces el que elija el usuario), tema oscuro (por defecto) o claro, y orientación según el móvil (por defecto), vertical u horizontal, aplicados al momento a toda la app, también al rodar.
- **F12.9** Conexión (pestaña, change `pestana-conexion`): la app no elige plataforma; publica servicios y la app de entrenamiento (Zwift, MyWhoosh, Rouvy…) se conecta a ellos. Arriba, el estado de cada servicio (rodillo, controlador Zwift y pulso: «Conectado», «Esperando», «Sin wifi», «No se ofrece»…). Debajo, el modo, uno de tres, con la compatibilidad probada de cada uno: «Wifi + Bluetooth» (por defecto y recomendado: rodillo y pulso por wifi DirCon con FTMS, controlador por Bluetooth aparte), «Solo Bluetooth» (todo en un solo dispositivo Bluetooth con el nombre del móvil o, con la casilla «Usar el nombre también por Bluetooth», con el nombre ante la app; sin wifi) y «Solo wifi (modo Zwift Ride)» (todo por wifi con el protocolo de Zwift). Se publica lo configurado en Dispositivos: sin rodillo no se publica nada; el controlador, solo con un Square configurado (en «Solo wifi», siempre, porque Zwift manda por él el terreno); el pulso, solo con un pulsómetro configurado. No hay interruptor del controlador, que se llama «Controlador Zwift» y lleva el icono de Zwift porque emula el controlador de Zwift (change `renombrar-controlador`). «Nombre ante la app» («Smart Bike», por defecto, o «KICKR BIKE PRO») solo en los modos con wifi. Con el controlador dentro del mismo dispositivo, Zwift puede mostrar su propia marcha, pero las marchas son siempre las de la app.

Desde el change `rodaje-completo-y-ajustes` (26-10-2026), la app tiene barra inferior con Rodar, Dispositivos, Botones y Ajustes (desde el change `pestana-plataformas`, Rodar, Dispositivos, Plataformas y Ajustes, con Botones dentro de Ajustes; desde el change `pestana-conexion`, Rodar, Conexión, Dispositivos y Ajustes); se cumplen F12.1 (salvo wifi o Bluetooth), F12.7 y, de F12.2 y F12.5, el peso del ciclista y la pendiente sin Zwift. Marchas y Diagnóstico se abren desde Ajustes. Exportar e importar (F12.6) se cumple desde el change `exportar-bateria-y-giro` (27-10-2026). Desde el change `acceso-ajustes-y-tema` (26-10-2026) se cumple F12.8, en una sección «Pantalla» de Ajustes. Desde el change `orientacion-pantalla` (27-10-2026), esa sección suma la orientación de la app. Desde el change `mando-zwift-emulado` (27-10-2026) se cumple F12.9, en una sección «Zwift» al final de Ajustes. Desde el change `registro-sesiones`, Ajustes termina con la sección «Depuración» (grabar sesiones, espacio libre mínimo y «Sesiones grabadas»).

Desde el change `pestana-conexion` (28-10-2026), «Plataformas» pasa a «Conexión» (F12.9): sin elección ni guardado por plataforma, tres modos nombrados por transporte, estado por servicio y publicación según lo configurado en Dispositivos, que tiene una sección y una búsqueda por tipo (F12.1). Sale «Pendiente sin Zwift» (F12.5). Al actualizar hay que emparejar de nuevo en Zwift una vez. Validados con Zwift en PC los tres modos; con «Solo Bluetooth» y el mando emparejado, Zwift lo trata como un Zwift Ride y la app atiende también ese camino. Pendiente de probar con el equipo: si Zwift ve el pulso al configurar la banda con el dispositivo ya emparejado, y otras plataformas.

### F13. Diagnóstico — M (en la fase inicial)

- **F13.1** Ver en crudo lo que envían el Square y el rodillo, y el último botón o gesto reconocido con su acción. El tráfico en directo se activa con la casilla «Tráfico en directo», desmarcada cada vez que se abre Diagnóstico. Desde el change `simplificar-diagnostico-conexiones` (28-10-2026), la sección «Datos de conexión» solo tiene lo que no se ve en Conexión ni en Dispositivos: la dirección de la app de entrenamiento y el nombre anunciado, la batería del Square, el último pulso y el modo y la última notificación del ventilador. Ya no repite el estado de cada conexión, los avisos ni «Buscar dispositivos»; el aviso de batería baja del Square está en Dispositivos.
- **F13.2** Enviar al rodillo una pendiente o una potencia fija a mano.
- **F13.3** Ver qué envía Zwift a la app (pendiente, parámetros de terreno, comandos) y cómo se traduce la marcha: modelo activo, ajuste por marcha y pendiente o potencia enviada al rodillo. La última simulación de marchas virtuales que envía Zwift se ve en la sección Simulación.
- **F13.4** Guardar y compartir un registro de la sesión para analizar problemas. Cumplida desde el change `registro-sesiones`: con «Grabar sesiones» activado en la sección «Depuración» de Ajustes (apagado por defecto), cada sesión se graba en un archivo de texto de la app con una cabecera (fecha, versión de la app, modo de conexión, nombre ante la app, transmisión y modelo) y un suceso por línea, escrito según llega y sin las tramas crudas. «Espacio libre mínimo» (10 % por defecto, de 5 a 50 % de 5 en 5) detiene la grabación sola, con su última línea, y no se reanuda hasta reactivarla o hasta la siguiente sesión. «Sesiones grabadas», secundaria de Ajustes, lista las sesiones con su tamaño y permite abrirlas con otra app, compartirlas y borrarlas (una o todas, nunca la que se graba). Desde el change `preparar-release`, la app conserva las 20 sesiones más recientes: al empezar una grabación borra las más antiguas que sobren, y la pantalla lo dice. Puede incluir direcciones de red y nombres de dispositivos: la pantalla lo avisa.

### F14. Marchas visibles dentro de Zwift — E

- **F14.1** Presentar la app ante Zwift como un rodillo con cambio virtual propio de Zwift. Así Zwift gestiona y muestra las marchas en su interfaz.
- Es una alternativa a F5/F6 con protocolo propietario y frágil. Solo si se echa de menos ver la marcha en Zwift.

### F15. Cambio de marcha conservando la inercia del Neo — E

- **F15.1** Investigar si el Neo permite cambiar la relación de marchas en su propio modo de simulación. Si fuera así, se obtendría el realismo del modelo físico sin perder la sensación de inercia del modo SIM.

### F16. Pulsómetro Bluetooth hacia Zwift — C

Change `pulso-zwift`. Validado con una banda COOSPO HW9 y Zwift en PC (27-10-2026).

- **F16.1** Conectar con un pulsómetro Bluetooth estándar (servicio Heart Rate) con «Buscar» en su sección de Dispositivos; configurarlo (uno solo), reconectarlo solo y olvidarlo. Opcional: sin pulsómetro configurado, la app no muestra nada de él.
- **F16.2** Reenviar a Zwift el pulso de la banda como una fuente más del dispositivo publicado (por wifi o, con «Solo Bluetooth», por Bluetooth), solo con un pulsómetro configurado. Solo valen lecturas de 1 a 250 bpm; sin banda o sin lectura válida no se envía nada (nunca un 0).
- **F16.3** Rodaje muestra su icono en la barra de conexiones, solo con banda configurada, y nunca el pulso como dato. Diagnóstico muestra el último pulso y su antigüedad, sin su estado, que está en Dispositivos.

### F17. Ventilador Wahoo KICKR Headwind — C

Change `ventilador-headwind`. Protocolo no oficial, **a validar** con el ventilador real.

- **F17.1** Conectar con un ventilador Headwind por Bluetooth (se busca por el nombre `HEADWIND…` con «Buscar» en su sección de Dispositivos), configurarlo (uno solo), reconectarlo solo y olvidarlo. Opcional: sin ventilador configurado, la app no muestra nada de él.
- **F17.2** Tres acciones asignables, sin botón por defecto: «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad». En manual, + y − cambian la velocidad de 10 en 10 % entre 0 y 100 %; en automático no cambian nada y avisan. No tocan la marcha, el Neo ni Zwift.
- **F17.3** En Ajustes, el modo (manual o automático), la velocidad manual y los cuatro umbrales de pulso («desde» cada uno el ventilador entra en su zona; por debajo del primero está parado). Los umbrales se editan solo ahí, conservando el resto de la configuración del ventilador.
- **F17.4** El modo automático lo ejecuta el ventilador con la banda que tiene emparejada por ANT+; la app no decide la velocidad. Lo que se muestra es siempre lo que el ventilador comunica. Rodaje solo muestra su icono en la barra de conexiones y el aviso de las acciones.

## 5. Requisitos no funcionales

| ID | Requisito |
|---|---|
| NF1 | Latencia palanca → cambio de resistencia < 100 ms |
| NF2 | Uso completo durante el rodaje sin tocar el móvil |
| NF3 | Android 9 o superior. Para el modo «Solo Bluetooth» (F8.5) y el mando emulado (F9), el móvil debe poder anunciarse como dispositivo Bluetooth (modo periférico); si no puede, la app dice «No disponible» |
| NF4 | Sesiones de 2 h o más sin cortes, con la pantalla apagada o en segundo plano |
| NF5 | Sin cuentas, sin servidores y sin conexión a internet. La conexión con Zwift usa solo la red local: DirCon escucha únicamente en la dirección de la wifi, no en los datos móviles ni en otras interfaces |
| NF6 | Código propio. No se reutiliza código de otras apps de licencia incompatible |
| NF7 | Interfaz en español y en inglés (change `idioma-es-en`); documentación y trazas en español |

## 6. Fuera de alcance

- Entrenamientos propios, planes o mapas.
- Integración con Strava, Garmin, Peloton u otras plataformas.
- Otros rodillos, bicis o mandos distintos de Neo 1 y Elite Square.
- Otros sensores, y del pulsómetro (F16) mostrar el pulso en Rodaje, las zonas, medias o intervalos RR, y más de una banda (se puede reconsiderar). El ventilador Headwind sí entra (F17); sigue fuera emparejar o cambiar su banda de pulso, sus modos por velocidad, potencia, temperatura, carrera o híbrido, y manejarlo desde Zwift.
- Versión iOS.

## 7. Fases

Las fases, los changes previstos en cada una y su estado están en [`planificacion.md`](planificacion.md).

## 8. Decisiones

### Tomadas

| # | Decisión | Resultado |
|---|---|---|
| 1 | Sentido de las palancas | Lineal: derecha más dura, izquierda más ligera (palanca 1 y 2 iguales). Con plato y piñones, por defecto como los mandos de la marca (F5.8). Invertible (F4.5). |
| 2 | Modelo de resistencia por defecto | Simple. El modelo físico (F6.3) y el de tabla (F6.2) están implementados (change `modelos-tabla-y-fisico`); el predeterminado definitivo se decide tras compararlos en el Neo con Zwift (prueba pendiente). |
| 4 | Capas alternativas (F4.4) | No por ahora: asignación por defecto de Zwift más los adicionales de la app. |
| 5 | Transmisión por defecto | Lineal de 24 marchas. |
| 6 | Recordar la marcha entre sesiones | Ajuste «Recordar la última marcha», activado por defecto. |
| 7 | Plataforma principal de Zwift | PC. Las pruebas del puente (fase 2) y del mando emulado (fase 4) se hacen primero en PC. |
| 8 | Conexión con Zwift | Wifi en la red local como principal; Bluetooth como alternativa (F8.5), implementada en el change `ftms-bluetooth`. |
| 9 | Pendiente por marcha con una transmisión de marca (abierta 3) | Proporcional al logaritmo de la relación, 0 % en el centro geométrico del desarrollo y normalizada para que de la más ligera a la más dura haya lo mismo que de la 1 a la 24 de la lineal (−11,5 % a +11,5 % con 1 % por marcha). Dos marchas con la misma relación dan la misma pendiente (change `transmisiones-de-marca`). |
| 10 | Desarrollos de fábrica (abierta 5) | Por ahora, solo el del usuario: Shimano 105 2×12, 52/36 · 11–36. Los de SRAM y Campagnolo se confirmarán en su change. |
| 11 | Botones con una transmisión de marca | Una asignación por transmisión. La de la Shimano reproduce el STI: D1 piñón más grande, D2 más pequeño, I1 plato grande, I2 pequeño (validado en la bici del usuario). |
| 12 | Velocidad como dato de rodaje (abierta 4) | Fuera de los datos, junto con la distancia, hasta el modelo físico (change `modelos-tabla-y-fisico`): con marchas virtuales, la velocidad del Neo depende solo de la cadencia y no coincide con la de Zwift (change `rodaje-completo-y-ajustes`). |
| 13 | Valores por defecto de «Al rodar» (abierta 6) | Datos potencia y cadencia, botones − y + ocultos, mostrar la acción y vibrar activados (change `rodaje-completo-y-ajustes`). |

### Abiertas

1. ~~¿Mantener el Square emparejado en Zwift para la dirección hasta la fase 4, o adelantar F9?~~ **Resuelta** (27-10-2026): con el mando emulado (change `mando-zwift-emulado`) el Square ya no se empareja en Zwift; con «Mando de Zwift» desactivado se puede seguir haciendo.
2. ~~Correspondencia entre las palancas 1 y 2 del Square y los dos botones de cada mando de marca (F5.8): cuál es el «grande» y cuál el «pequeño».~~ **Resuelta** (25-10-2026, con el Square en la mano): la palanca 1 es la grande y la 2 la pequeña, en los dos lados.
3. ~~Con el modelo simple y una transmisión de marca, cuánto cambia la pendiente en cada marcha.~~ **Resuelta** (26-10-2026): decisión tomada 9.
4. ~~Velocidad como dato de rodaje (F11.3).~~ **Resuelta** (26-10-2026): decisión tomada 12.
5. ~~Platos y cassettes de fábrica de cada marca.~~ **Resuelta en parte** (26-10-2026): decisión tomada 10; quedan los de SRAM y Campagnolo para su change.
6. ~~Valores por defecto de «Al rodar» (F12.7).~~ **Resuelta** (26-10-2026): decisión tomada 13.
7. ~~Nombre de la app («Square Shift» era provisional).~~ **Resuelta** (27-10-2026): «Smart Bike», con el paquete `app.smartbike` (change `renombrar-smart-bike`). «Square» queda como el nombre del mando Elite que la app soporta.
8. Modelo simple (pendiente de revisar con la prueba de modelos del change `modelos-tabla-y-fisico`): incremento de 1 % por marcha y la 12 como marcha neutra (0 % sin Zwift; las ligeras restan pendiente, hasta −11 %, pero con el modo de bajadas por defecto no bajan de 0 % en llano: F6.5). Los dejó el usuario de forma provisional tras la primera prueba con el equipo real (25-10-2026); se revisan al comparar modelos en la fase 5.

## Anexo A. Asignación de botones por defecto (propuesta)

**Principio:** la configuración por defecto reproduce las funciones de un **mando oficial de Zwift**, con cada botón del Square en el sitio equivalente:
- Cruceta en la mano izquierda.
- A/B/Y/Z en la mano derecha.
- Palancas para cambiar.
- Manetas para frenar.

Así se rueda en Zwift como con un Zwift Ride, sin aprender nada nuevo. Las funciones **adicionales** de la app usan solo los botones que Zwift no necesita, o gestos y combinaciones que no interfieren con él.

### A.1 Funciones de Zwift (por defecto)

| Botón del Square | Función en Zwift | Cómo se resuelve |
|---|---|---|
| Palancas derechas (1 y 2) | Endurecer marcha (mantener = repetir) | Marcha local de la app (F5), al instante. Con una transmisión de marca, como sus mandos (F5.8) |
| Palancas izquierdas (1 y 2) | Suavizar marcha (mantener = repetir) | Marcha local de la app (F5), al instante. Con una transmisión de marca, como sus mandos (F5.8) |
| X / Círculo (mantener) | Desplazarse a izquierda / derecha | Mando emulado (F9.4): «Girar a la izquierda / derecha», palanca izquierda o derecha hacia fuera |
| Manetas de freno (mantener) | Frenar (las dos, más fuerte) | Mando emulado (F9.5): «Freno izquierdo / derecho», palancas hacia dentro |
| Cruceta ↑ | Barra de acciones / navegar | Mando emulado: «Barra de acciones» |
| Cruceta ↓ | Cambio de sentido / navegar | Mando emulado: «Cambio de sentido» |
| Cruceta ← / → | Navegar menús y elegir desvío en los cruces | Mando emulado: «Izquierda» / «Derecha» |
| A | Seleccionar | Mando emulado: «Seleccionar» |
| B | Atrás | Mando emulado: «Atrás» |
| Y | Usar powerup | Mando emulado: «Usar powerup» |
| Z | Ride On | Mando emulado: «Ride On» |

Cada función de Zwift es una acción asignable (F4.2), con «pulsar» en su botón por defecto: llega a Zwift **sin retardo**, pulsada mientras se mantiene el botón y suelta al soltarlo. Con corta, larga o doble llega como una pulsación breve. Un botón reasignado deja de llegar a Zwift. Las configuraciones guardadas antes del mando reciben estas reglas en las entradas libres.

Las marchas son la única función de Zwift que resuelve la app y no Zwift. Con el puente FTMS, Zwift no hace cambio virtual (ver F14 para la alternativa).

### A.2 Funciones adicionales de la app

Usan los botones libres (**Cuadrado, Triángulo, Campagnolo izquierdo y derecho**) y combinaciones con los Campagnolo como **modificadores**. Una combinación con modificador la consume la app y **no** se envía a Zwift.

| Botón / gesto | Acción |
|---|---|
| Campagnolo izquierdo + palanca izquierda 1 | −3 marchas |
| Campagnolo izquierdo + palanca izquierda 2 | Marcha más ligera |
| Campagnolo derecho + palanca derecha 1 | +3 marchas |
| Campagnolo derecho + palanca derecha 2 | Marcha más dura |
| Cuadrado (corta) | Volver a la marcha de referencia |
| Cuadrado (larga) | Fijar la marcha actual como referencia |
| Triángulo (larga) | Alternar SIM ⇄ ERG manual |
| Triángulo (corta) | Cambiar de modelo de resistencia |
| Campagnolo izquierdo + cruceta ← / → | Pantalla de la app anterior / siguiente |

Todo es reasignable (F4). Los adicionales son una propuesta de partida.

Campagnolo izquierdo + cruceta ← / → recorre las pestañas de la barra inferior en orden circular, también desde Rodaje (change `rodaje-completo-y-ajustes`); «Pantalla anterior» y «Pantalla siguiente» son asignables, y las configuraciones guardadas reciben la regla si la entrada está libre.

Ya funcionan (change `gestos-y-asignacion`): las cuatro combinaciones con Campagnolo y Cuadrado corta y larga, con la autorrepetición de las palancas. Triángulo larga, desde el change `erg-manual`. La propuesta de «ERG ±10 W» con Campagnolo derecho + cruceta ↑ / ↓ se descartó al validar la maqueta: con el Square emparejado en Zwift, ↓ también es «Cambio de sentido»; en ERG ajustan las palancas, y «+10 W» y «−10 W» quedan asignables sin botón.

Con la Shimano 105, la asignación es otra (change `transmisiones-de-marca`): palanca D1 −1 marcha (piñón más grande), D2 +1 marcha, I1 plato más grande, I2 plato más pequeño; Campagnolo derecho + D1 −3 marchas y + D2 +3 marchas; Campagnolo izquierdo + I1 marcha más dura y + I2 marcha más ligera; Cuadrado igual. Cada transmisión tiene su asignación, y los tiempos de los gestos son comunes. Triángulo larga llega con el ERG manual (change `erg-manual`), cambiar de modelo con `modelos-tabla-y-fisico`, y la pantalla anterior o siguiente con la barra inferior (`rodaje-completo-y-ajustes`); cada uno añade su regla a la asignación por defecto.

### A.3 Sin el mando emulado (fases 1–3, o sin un Square configurado como mando de la app)

- Las palancas ya funcionan como marchas desde la fase 1.
- Si el Square se empareja también en Zwift, Zwift gestiona directamente las funciones que el Square soporte de forma nativa (al menos la dirección con X/Círculo). Las demás funciones de Zwift (freno, menús, powerup, Ride On) llegan con F9.
- Los adicionales de A.2 funcionan desde que exista F3/F4 (fase 3).
- Con el Square emparejado también en Zwift, un botón reasignado en la app sigue llegando a Zwift: antes la app avisaba al asignarlo con el mando desactivado; desde el change `pestana-conexion` no hay interruptor ni aviso. Con el mando emulado (F9) el Square no se empareja en Zwift y no pasa.
- Por eso Campagnolo izquierdo + cruceta ← / → (pantalla anterior o siguiente) también llega a Zwift como cruceta y puede mover sus menús. Falta comprobar qué hace Zwift con ella: la prueba quedó pendiente al archivar `rodaje-completo-y-ajustes`.
