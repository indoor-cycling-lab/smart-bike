# Design

## Context

Ver `proposal.md` para el porqué. Hoy:

- Rodaje (`Rodaje.kt`) tiene una disposición fija: la marcha, un área de datos del 40 % del alto o ancho útil (`MetricGrid`, `medirRejilla`) y, si se activan, los botones − y +, que toman de la rejilla «lo que le sobra» hasta su mínimo y, si no basta, de la marcha. Debajo, el pie (`sq-ride__foot`, 40 dp) con los avisos: el de las acciones (`ActionToast`) y el de conexión («Toca las conexiones para conectar el Neo y el Square»).
- `Sesion.estado` ya trae el estado de cada enlace: `neo`, `square` (`EstadoDispositivo`) y `plataforma` (`EstadoPlataforma`). Los dispositivos configurados (con su modelo) viven en `estado.json`, detrás de `Almacen`; el modo de conexión (Wifi + Bluetooth, Solo Bluetooth, Solo wifi) está en los ajustes de la plataforma.
- `estado.json` admite campos opcionales con valor por defecto sin cambiar de versión (precedente: `puenteZwift`), no se exporta y depende del móvil.
- La pantalla de rodaje no muestra el modelo de resistencia ni cosas de configuración; la guía es un añadido temporal.
- La maqueta aprobada (`maquetas/rodaje.html`, `maquetas/rodaje-vertical.html`, `design-system/components/GuideSheet` y `GuideStep`) es la referencia de disposición, textos y estados.

## Goals / Non-Goals

**Goals:**
- Que el estado de cada paso se derive de lo que la app ya sabe, sin lógica de conexión nueva.
- Que la guía se pueda minimizar y descartar, que se recuerde lo que se descarta y que sea reversible.
- Que no cambie nada para quien ya usa la app, y que no tape la marcha mientras se rueda.

**Non-Goals:**
- Un tutorial o una visita guiada de la interfaz.
- Cambiar cuándo conecta la app: la guía solo informa y lleva a las pantallas existentes.
- Cambiar el modo de conexión: se hace en Conexión.

## Decisions

**1. Los pasos son una función pura en `:core`.** `PasosGuia` recibe el estado de los tres enlaces, qué hay configurado y el modo de conexión, y devuelve el estado del paso de los dispositivos y el de la conexión con la app, el paso siguiente por hacer y si ya están hechos. Se prueba sin Android, como el resto del dominio. Estados del paso 1 (Empareja tus dispositivos): pendiente (sin rodillo), conectando, «falta el controlador» (rodillo conectado) y hecho (rodillo y controlador conectados). Estados de la conexión con la app, que se ven en la página 3: pendiente (sin rodillo configurado, no se publica nada), esperando (el rodillo se ofrece y falta emparejarlo en la app) y hecha (una plataforma conectada). Los pasos «Configura la conexión» (opcional: el modo ya viene elegido por defecto, así que no hay nada que «completar») y «Ajusta la app» de la página 2 no tienen estado. Alternativa descartada: calcularlo en el ViewModel; obligaría a pruebas con Android para lo que es una tabla de estados.

**2. Hoja inferior de tres páginas.** La guía es una hoja que sale desde abajo, con asa, indicador de página (puntos y «n de 3») y, en su cabecera, **Minimizar** y **Cerrar**. Las páginas son «qué es» (1), «Para empezar» (2) y «Conecta Smart Bike a tu app de ciclismo indoor» (3). Se navega con «Siguiente» y «Atrás» y deslizando; en la 3 solo hay «Atrás». Alternativas descartadas: una tarjeta fija en Rodaje (no cabe en 360 × 640 ni en horizontal sin aplastar la marcha, medido en la maqueta) y una pantalla de bienvenida bloqueante (estorba a quien ya conoce la app).

**3. Tres formas y un solo espacio en el flujo.** *Abierta*: flota sobre Rodaje, anclada al pie, sin mover nada y sin bloquear (diálogo no modal). *Minimizada*: una tira de 48 dp dentro del pie (`sq-ride__foot`), en lugar del texto de conexión mientras la guía está a la vista, con «Para empezar» y debajo lo siguiente por hacer; sin contadores. *Cerrada*: oculta. Con la hoja abierta la tira sigue bajo ella, oculta pero ocupando su sitio, para que la marcha no se mueva al abrir y cerrar. El único coste en el reparto es que el pie pasa de 40 a 48 dp. Alternativa descartada: una tira propia en el flujo (suma una línea al reparto) o una tarjeta que ceda espacio a la rejilla.

**4. Prioridad de los avisos del pie.** El aviso de acción («+1 marcha», «Plato 52») ocupa el pie mientras dura y la tira vuelve después. El texto de conexión («Toca las conexiones…») no se ve mientras la guía está minimizada: su contenido ya está en el paso 1.

**5. Estado en memoria y en disco.** La página actual y si está abierta o minimizada, y la equis, viven en memoria de la ejecución (la guía se vuelve a ver al abrir la app). «No volver a mostrar» y el fin de la guía se guardan en `estado.json` en un campo opcional `guiaInicio` (`VISIBLE`, `DESCARTADA`, `FINALIZADA`; ausente = aún no evaluada). Alternativa descartada: guardarlo en `configuracion.json`; entraría en la exportación y, al importar la configuración en un móvil nuevo, la guía no se vería en su primer uso, que es justo cuando hace falta.

**6. Cuándo se abre y en qué página.** Con la guía visible, la hoja se abre sola solo mientras no haya ningún dispositivo configurado (primer uso), en la página 1; en los demás arranques aparece minimizada. «Guía de inicio» en Ajustes la abre en la página 1. Tocar la tira la abre en la página en la que estaba.

**7. No aparece a quien actualiza.** Al evaluar por primera vez un `guiaInicio` ausente: con algún dispositivo configurado se guarda `FINALIZADA` (sin guía), y sin ninguno `VISIBLE`. Así una actualización no la enseña, y quien instala de cero la ve. Alternativa descartada: usar solo «hay dispositivos» como regla permanente; la guía reaparecería sola tras «Olvidar» el único dispositivo, que no pide nadie.

**8. Fin de la guía.** Cuando los dispositivos están emparejados y hay una app conectada a la vez (rodillo y controlador conectados, y una plataforma conectada) se guarda `FINALIZADA`, y no reaparece aunque después se pierda la plataforma. Los pasos sin estado de la página 2 no cuentan. La entrada «Guía de inicio» de Ajustes pone `VISIBLE` y abre Rodaje, tanto si se descartó con «No volver a mostrar» como si terminó sola; al reabrirla con todo hecho, la guía no vuelve a finalizarse durante esa ejecución (se ve con los pasos hechos hasta que el usuario la minimice o la cierre) y, si sigue sin descartar, se evalúa de nuevo al abrir la app. Riesgo conocido en «Risks».

**9. Textos de la página 3 desde Conexión.** Los pasos de la página 3 salen del modo de conexión elegido y de las mismas cadenas de ayuda que Conexión usa para ese modo (no se copian). El nombre que se pide elegir en la app sale del ajuste «Nombre ante la app» («Smart Bike» por defecto) y, en Solo Bluetooth, del nombre Bluetooth del móvil. Conexión, donde se cambia el modo, se abre desde el paso «Configura la conexión» de la página 2.

**10. Textos en recursos de cadenas.** Todos los textos de la guía van a `values/strings.xml` y `values-en/strings.xml`, con los ejemplos (Tacx Neo, Elite Square, Zwift) como parámetro del recurso, para que `TextosInglesTest` los compruebe sin tocar la lógica. La frase del controlador («Sin él, cambias de marcha desde la pantalla») es cierta con `modo-tactil-rodaje` y con los botones − y + activados; con ellos desactivados, el texto lo decide la implementación (p. ej. neutro: «Sin él, cambia de marcha desde la pantalla»).

**11. Componentes nuevos en el sistema de diseño.** `GuideSheet` (hoja, asa, cabecera, páginas, pie y tira minimizada, también en horizontal con los elementos en columnas) y `GuideStep` (fila de paso con punto de estado, nombre, ayuda, marca «Opcional» y chevron). Usan solo tokens existentes; el estado se dice con forma y palabra, no solo con color; un paso pendiente usa `ink-muted`, nunca el color de error. Ya están en `design-system/` y la guía los muestra en los dos temas.

## Risks / Trade-offs

- [La guía puede no terminar nunca para quien no usa controlador o app: acaba con los dos pasos hechos y ambos son opcionales] → la tira solo ocupa el pie y no tapa nada; la equis la oculta esa ejecución y, abriendo la hoja, «No volver a mostrar» la oculta para siempre; si se quiere volver a verla, «Guía de inicio» en Ajustes la recupera. Si al probar resulta molesto tener que abrir la hoja para descartarla, se valora que la equis de la tira lo haga con el rodillo ya conectado.
- [La hoja abierta tapa la marcha y los datos en el primer uso] → se abre sola solo en el primer uso, sin bloquear, y se minimiza con un toque.
- [A 640 × 360 en horizontal las páginas 2 y 3 ocupan casi todo el alto] → es el caso más bajo y la hoja se abre a petición; la maqueta lo valida a esa medida.
- [El pie pasa de 40 a 48 dp mientras hay tira, y la marcha cede esa diferencia] → mínimo; al ocultarse la guía, el pie recupera su alto.
- [Un paso «Conectando» o «Esperando» que nunca termina parece un error] → solo dice lo que hace la app, con las mismas palabras de Dispositivos y Conexión; no se añaden avisos.
- [Los textos de la página 3 divergen de los de Conexión] → salen de las mismas cadenas.
- [Dos fuentes de la verdad para «hay dispositivos»: el estado de conexión y `estado.json`] → la función pura recibe ambas y el estado de cada paso sale solo del primero; `estado.json` solo decide la primera evaluación.
- [El texto del controlador promete algo que depende de otro change] → `modo-tactil-rodaje` va antes en `planificacion.md`.

## Migration Plan

Sin migración de datos: el campo `guiaInicio` es opcional y los documentos sin él se leen igual. No requiere reversión; quitar la guía es no leer el campo.

Prueba con el equipo real (OnePlus 8, Neo y Square): instalar de cero (borrar los datos de la app) y ver la hoja abierta en la página 1; recorrer las páginas; conectar el rodillo y el controlador desde su paso; seguir la página 3 en el modo elegido y emparejar en Zwift hasta que desaparezca; minimizarla y comprobar que la tira dice lo siguiente por hacer; instalar encima de una versión anterior con dispositivos configurados y comprobar que no aparece; descartarla y reabrirla desde Ajustes.

## Open Questions

- Ninguna bloqueante. Se valora al probar con el equipo real: si la hoja debe minimizarse sola al empezar a pedalear, y si hace falta otra salida para descartar la guía sin abrir la hoja (ver «Risks»).
