# Proposal

## Why

La pestaña «Plataformas» se organiza por plataforma (hoy solo Zwift). Sin embargo, lo que el usuario elige en ella son protocolos casi estándar: FTMS por wifi (DirCon), FTMS por Bluetooth y el modo Zwift Ride. Si añadimos MyWhoosh, Rouvy y las demás como opciones aparte, la pantalla se llena de combinaciones repetidas. Además, la pantalla no muestra lo que de verdad le importa al usuario: qué servicios publica la app (rodillo, mando y pulso), por dónde y si la app de entrenamiento se ha conectado a cada uno. Ahora que el 24 (`generalizar-plataformas`) y el 11 (`ftms-bluetooth`) están hechos, y antes de probar otras plataformas, es el momento de reorganizarla.

## What Changes

- **BREAKING** La pestaña «Plataformas» pasa a llamarse **«Conexión»** y explica cómo se conecta la app de entrenamiento (Zwift, MyWhoosh, Rouvy…) a la smart bike: la app solo publica servicios, y es la app de entrenamiento la que se conecta a ellos. De arriba abajo muestra los avisos, el **estado** de cada servicio, el **modo** y «Nombre ante la app».
- **BREAKING** Se elimina la elección de plataforma y el guardado de ajustes por plataforma. La plataforma pasa a ser una etiqueta de compatibilidad de cada modo, solo con lo probado: «Zwift: validado», «validado en parte»… No hay migración: se conserva lo que no es de conexión y hay que volver a emparejar una vez.
- Una única elección, el modo, con tres opciones nombradas por transporte:
  - «Wifi + Bluetooth», la recomendada: rodillo y pulso por wifi (DirCon, FTMS) y mando por Bluetooth aparte.
  - «Solo Bluetooth»: rodillo, pulso y mando por Bluetooth (FTMS), en el mismo dispositivo.
  - «Solo wifi (modo Zwift Ride)»: todo por wifi (DirCon, protocolo de Zwift).
- «Nombre ante la app» solo aparece en los modos con wifi.
- **BREAKING** Desaparece el interruptor «Mando de Zwift» y, con él, el estado «Desactivado» del mando.
- Nueva regla de publicación: **se publica lo configurado** (el dispositivo recordado en Dispositivos), y la conexión solo cambia el estado.
  - El rodillo se publica si hay un rodillo recordado; sin él no se publica nada.
  - El mando se publica si hay un Square recordado.
  - El pulso se publica si hay un pulsómetro recordado.
  - Esto deshace la decisión de `pulso-zwift` de ofrecer siempre el pulso.
- Estado por servicio según lo que la app de entrenamiento usa de verdad (su suscripción o el saludo del mando):
  - Rodillo: Sin wifi, Sin Bluetooth, No disponible, No visible, Esperando o Conectado.
  - Mando: No disponible, Esperando o Conectado.
  - Pulso: Sin lectura, Esperando o Enviando.
  - Cualquiera de los tres dice «No se ofrece» cuando falta su dispositivo, con un enlace a Dispositivos.
- Dispositivos tiene una sección y una búsqueda por tipo de dispositivo: Rodillo, Mando, Pulsómetro y Ventilador.
  - Los botones dicen «Buscar» y «Olvidar», con una descripción accesible que nombra el dispositivo.
  - **BREAKING** Desaparece la búsqueda conjunta «Buscar dispositivos».
  - Solo se admiten el Tacx Neo y el Elite Square, y la app se conecta al primero que encuentra.
- La barra inferior pasa a ser **Rodar · Conexión · Dispositivos · Ajustes**, y las acciones «Pantalla anterior» y «Pantalla siguiente» siguen ese orden.
- Rodaje muestra el estado sin nombrar ninguna plataforma: «Conectado», con «· sin mando» o «· sin pulso» solo si falta algo configurado, sin decir por dónde va (eso está en Conexión). Al tocarlo se abre Conexión.
- **BREAKING** Desaparece «Pendiente sin Zwift» de Ajustes (decidido por el usuario al implementar): sin app de entrenamiento, el terreno es siempre llano (0 %) y la dureza la da la marcha.
- Se dejan de nombrar «Zwift» y «Mando de Zwift» donde la app no sabe quién se ha conectado. El mando se llama «Mando» y «(protocolo de Zwift)» aparece solo en la ayuda y en la compatibilidad.

**Fuera de alcance:**

- Elegir un dispositivo de una lista cuando hay varios.
- Admitir otros rodillos (FTMS genérico) u otros mandos (Zwift Click/Play…).
- Añadir modos de conexión nuevos.
- Publicar el pulso aparte del rodillo.
- Usar la app solo como puente de mando y pulso, sin rodillo.
- Que DirCon atienda a varios clientes.
- Conservar las identidades anteriores para no volver a emparejar.

Las pruebas con otras plataformas (MyWhoosh, Rouvy) son un experimento sin código que alimenta las etiquetas de compatibilidad. No se presenta como confirmado nada que no se haya probado.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `platform-settings`: la pestaña pasa a ser «Conexión», sin plataforma ni guardado por plataforma, con tres modos nombrados por transporte, un estado por servicio, «No se ofrece» y compatibilidad por modo. Desaparece el interruptor del mando.
- `platform-bridge`: lo publicado depende de los dispositivos recordados, el pulso solo se publica con pulsómetro, los modos tienen nombres nuevos, la identidad depende solo del modo y del nombre, y el cambio de modo ya no tiene mando desactivado.
- `ftms-bluetooth`: el mando dentro del dispositivo depende de que haya un Square recordado, y el rodillo solo se publica con un rodillo recordado. Nombres nuevos.
- `zwift-controller`: el mando por Bluetooth aparte se publica si hay un Square recordado, sin interruptor.
- `zwift-ride`: el mando dentro de DirCon depende del Square recordado (a validar, con alternativa) y desaparece el escenario del interruptor.
- `device-connection`: una sección y una búsqueda por tipo, «Buscar» y «Olvidar», y sin «Buscar dispositivos».
- `app-navigation`: barra inferior Rodar · Conexión · Dispositivos · Ajustes, y el orden de «Pantalla anterior/siguiente».
- `ride-screen`: el estado de la publicación en Rodaje, sin nombre de plataforma, y la flecha de giro sin depender del interruptor.
- `button-mapping`: sin estados ni avisos de «mando desactivado».
- `settings-backup`: la copia lleva el modo de conexión y el nombre ante la app, no «Mando de Zwift».
- `settings-screen`: lo que se guarda pasa a ser «Conexión», sin «Mando de Zwift», y se quita «Pendiente sin Zwift».
- `trainer-resistance`: sin app de entrenamiento, el terreno es siempre llano.
- `resistance-models`: el físico sin app de entrenamiento usa terreno llano.
- `ui-components`: la navegación inferior con «Conexión» y el icono de la pestaña.
- `screen-mockups`: maquetas de Conexión y Dispositivos, la barra inferior y los estados de Rodaje sin «Zwift».

## Impact

- **`:core`, dominio:**
  - Desaparecen `Plataforma` y `Configuracion.plataformas` por plataforma.
  - `AjustesPlataforma` pierde `mando`.
  - `MandoEnModo` pierde `BLUETOOTH_OPCIONAL`.
  - `Compatibilidad` pasa a ser modo → [(plataforma, validación)].
  - `Configuracion` sube de versión, sin migración de la parte de conexión.
- **`:app`:**
  - `PuentePlataforma`, `ConexionFtms`, `ConexionZwiftRide`, `MandoZwiftBluetooth` y `PuenteFtms` publican los servicios según los dispositivos recordados.
  - Hay un estado por servicio a partir de las suscripciones.
  - Las identidades se guardan por (modo, nombre).
  - `ui/plataformas/` pasa a ser la pantalla Conexión, y cambian Dispositivos, la navegación, Rodaje (estado) y Botones.
- **`design-system/`:** el icono y el texto de la pestaña, y quizá una fila de servicio, si no basta con los componentes actuales.
- **`maquetas/`:** `plataformas.html` (pasa a Conexión), `dispositivos.html`, `rodaje.html`, `rodaje-vertical.html`, `botones.html`, `diagnostico.html` (textos), la barra inferior y `index.html`.
- **`site/`:** regenerar las capturas.
- **Equipo real:** cuándo se suscribe Zwift a cada servicio, si Zwift ve el pulso añadido a un dispositivo ya emparejado, y si Zwift Ride funciona sin el mando dentro.
- **Documentos de `docs/`:**
  - `funcional.md`: F8.5, F9, F12.9, F16.2 y los anexos A.3.
  - `arquitectura.md`: plataforma, compatibilidad, publicación según lo configurado y estado por servicio.
  - `planificacion.md`: fila nueva 26 y el apartado «Otras plataformas».
  - `desarrollo.md`: ninguno, salvo los tests de navegación, si cambian los comandos.
