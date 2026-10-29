# Design

## Context

Hoy, en «Todo por wifi (modo Zwift Ride)», `MandoZwift` contesta a `GET` con `MandoZwiftCodec.respuestaInfo(PAGINA_RIDE, id)`: la `DevInfoPage` de un Ride (página 0), `notifications = 0` (página 16) y, a las demás, solo el identificador. `ZwiftBridge` ofrece Device Information con fabricante, modelo y versión de la app (`2A29`, `2A24`, `2A28`). Zwift, según la captura de BikeControl (docs/referencias, «Comparativa con la app»), lee `2A29`, `2A25`, `2A27` y `2A26` y pide las páginas 0, 16, 1024 y 771. Los códecs ya tienen las variantes `RespuestaHandshake` y `RespuestaInfo`, sin otro uso que las pruebas. Ver `proposal.md` para el motivo y el alcance.

Lo observado en la captura es de una sola configuración (BikeControl 7.3.1, Click v2, Neo, Zwift en Windows). No sabemos si Zwift usa algo de esto: es una apuesta barata, no un requisito.

## Goals / Non-Goals

**Goals:**
- Contestar con datos las páginas 1024 y 771, y ofrecer serie, firmware y hardware, sin cambiar lo validado.
- Poder comparar variantes con Zwift real sin añadir ajustes ni interfaz.
- Tener una prueba con resultado claro: lo que mejore pasa a normal, lo demás se descarta.

**Non-Goals:**
- Aceptar las marchas de Zwift (relación virtual), reenviar botones de cambio, o interpretar el campo 6 del mensaje `03` y el tipo `0x06` del anuncio (se desconoce lo que significan).
- Publicar más servicios por DirCon (`00000001-…`, `d273f680-…`). Solo se prueba si lo anterior no basta.

## Decisions

### 1. Las páginas nuevas, tal como se observaron

La página 1024 se contesta con `3c 08 80 08 12 07 0a 05 08 ff 23 10 00` y la 771 con `3c 08 83 06 12 0a 0a 08 08 00 10 64 18 00 20 00`, codificadas con `conLongitud` y varints como el resto de `MandoZwiftCodec`, sin copiar ningún fichero. El significado de la 771 se desconoce, y el campo 2 de la 1024 («ninguna analógica») es el de un Click.

**Riesgo (freno y giro).** La app emula un Ride y manda el freno y el giro por palancas analógicas, no por botones. Si Zwift hace caso a «ninguna analógica», podría ignorarlas. Los botones que usamos (`0x01`…`0x80` con `ENUM_MANUAL`) sí caen dentro de `0x11ff`. Por eso la respuesta de la 1024 es una variante de prueba (decisión 3): *actual* (solo el identificador, lo de hoy), *observada* (la de BikeControl) y, solo si hace falta, *con analógicas* (el campo 2 con las dos palancas; su codificación no se conoce y se descubre probando). Se queda como normal la que no rompa botones, freno ni giro.

*Alternativa descartada:* declarar siempre la 1024 de BikeControl sin probarla. Un fallo de freno o giro es peor que no haber añadido las páginas.

### 2. Device Information con la identidad propia

Se añaden `2A25` (serie), `2A26` (firmware) y `2A27` (hardware). La serie es la `serie` de la `PuenteZwift` con la que se publica el rodillo; el firmware, la versión de la app; el hardware, un texto fijo («1.0»). `ZwiftBridge` se construye antes de conocer la identidad, así que la característica de serie lee la identidad vigente en el momento de la lectura (se fija en `publicar`). Así sirve a los dos modos y al simulador de KICKR BIKE sin duplicar nada, y cada identidad conserva su serie.

*Alternativa descartada:* una serie fija para todo. Zwift recuerda los dispositivos por `mac-address` y `serial-number` (docs/referencias); mezclar identidades confundiría el emparejado.

### 3. Variantes solo en depuración, por extras del Intent

Se sigue el patrón de `EXTRA_SONDA_NOMBRE_FALSO` en `MainActivity` y `AppGraph`: la compilación de depuración lee de los extras del Intent (`am start --es …`) la respuesta al saludo (`pelado` por defecto, `02_00` con el aviso `0x2A` por ASYNC), la información del dispositivo (`ride` por defecto, `click`) y la página 1024 (`actual` por defecto, `observada`, `analogicas`). `MandoZwift` recibe la variante por constructor. Sin extras, todo responde como hoy. En la compilación normal los extras se ignoran, por `BuildConfig.DEBUG` y por no leerse en absoluto.

*Alternativa descartada:* ajustes en la pantalla de Diagnóstico. Obligaría a maqueta y a un ajuste que solo sirve una semana. *Descartada también:* compilar variantes distintas, que obliga a reinstalar entre pruebas.

### 4. Lo que se queda como normal

Solo pasan a la compilación normal las variantes que, con Zwift real y el Neo, mantienen botones, freno, giro y los diez minutos sin pulsar, y en las que Zwift hace algo mejor o igual. Si ninguna mejora, las páginas y las características nuevas se quedan (no cambian lo observable) y el resto de variantes se retiran del código. Lo que se decide se anota en `docs/arquitectura.md` § 7.4.

### 5. Registro

`MandoZwift` ya anota «información N respondida». Se amplía con si la respuesta llevó datos o solo el identificador, y con la variante elegida al arrancar, para poder comparar sesiones leyendo el registro.

## Risks / Trade-offs

- **Zwift no nota nada.** Es probable que las páginas no cambien lo que Zwift hace. Se acepta: el coste es pequeño y el resultado, aunque sea negativo, queda en las notas y evita volver a dudar.
- **Pérdida de freno o giro** con la página 1024 (decisión 1). Se prueba antes de dejarla como normal.
- **Zwift recuerda por serie.** Cambiar la serie del rodillo de un mando ya emparejado puede pedir repetir el emparejamiento. Se avisa en la prueba y se anota; no hay migración.
- **Una sola configuración observada.** Lo que sirva con esta versión de Zwift puede no valer con otra. Los valores se dejan como constantes con nombre, fáciles de cambiar.

## Migration Plan

No hay migración de datos ni ajustes nuevos. Si algo falla con Zwift real, se quitan las dos páginas y las características con un revert, sin tocar el resto.

## Open Questions

- ¿Qué declara la página 1024 de un Ride real respecto a las palancas analógicas? Se descubre probando las variantes; si ninguna sirve, las palancas siguen funcionando con la respuesta actual de la 1024 y se anota como pendiente.

## Resultados de la prueba

### 4.1 Sin variantes (28-10-2026, OnePlus 8, Neo, Square, Zwift)

Con las páginas 771 y las características `2A25`/`2A26`/`2A27` nuevas y la 1024 como hasta ahora (`ACTUAL`): botones (↓ y A en un menú), freno y giro funcionan como antes. Tras 10 minutos sin pulsar, el Square se había apagado por sí solo; al pulsarlo se encendió, se reconectó solo y los controles volvieron a funcionar en Zwift. No hay regresión. El búfer de `logcat` ya había rotado al leerlo, así que no se comprobó en el registro qué páginas pidió Zwift ni las lecturas de serie, firmware y hardware: la prueba siguiente graba los sucesos en segundo plano.

### 4.2 Página 1024 `OBSERVADA` (28-10-2026)

Según el usuario, con la 1024 `OBSERVADA` el menú (↓ y A), el freno y el giro funcionan como sin variantes, repitiendo el emparejamiento desde las opciones de Zwift. El registro confirma que Zwift pidió las páginas 0, 16, 1024 y 771 y que todas se contestaron con datos, y que leyó la serie, el firmware y el hardware sin error. `ANALOGICAS` no hizo falta. Queda por anotar si Zwift hace algo distinto con ella (4.3).

### 4.3 Información de Click v2 (`mando_info click`, con la 1024 `OBSERVADA`) (28-10-2026)

Peor que lo normal: según el usuario, las flechas y los botones no se detectaban al principio y solo empezaron a hacerlo tras probar un rato, y el freno y el giro no funcionaron. El registro muestra que la app contestó igual que en las pruebas buenas (handshake, páginas 0, 16, 1024 y 771 con datos, serie, firmware y hardware), así que lo que cambia el comportamiento de Zwift es el dato de que el mando es un Click v2, que no tiene palancas analógicas. **Se descarta.**

Saludo `RideOn 02 00` con el aviso `0x2A` (con la información de Ride y la 1024 `OBSERVADA`): según el usuario, todo va bien (botones, freno y giro) y no se señala ningún cambio en Zwift. El registro confirma el handshake y el aviso enviado por ASYNC. Es igual que el saludo de siempre, sin mejora observable.

No se probó la combinación del saludo con la información de Click v2: esta última rompe el freno y el giro por sí sola y no hay motivo para esperar que el saludo lo arregle.

### 4.4 Decisión (28-10-2026)

Pasan a normal, sin variantes: las páginas 771 y 1024 (la `OBSERVADA`, con «ninguna analógica») y las características `2A25`, `2A26` y `2A27`. No empeoran botones, freno ni giro. Se retiran del código, de los extras de `adb` y de `docs/desarrollo.md`: la información de Click v2 (rompe el freno y el giro), el saludo `RideOn 02 00` con el aviso `0x2A` (sin mejora observable), la 1024 `ANALOGICAS` (no hizo falta) y los tres extras del Intent. Por eso el requisito «Variantes de prueba solo en depuración» sale de la spec delta de `zwift-controller`. Zwift no cambió nada visible con ninguna variante: lo único observado es que declarar un Click rompe el freno y el giro.

Si hiciera falta repetir la comparativa, los datos están en este documento: aviso `2a 08 03 12 0d 22 0b "RIDE_ON(0)" 00` y `RideOn 02 00`.
