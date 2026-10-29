## 1. Maqueta

- [x] 1.1 En `maquetas/dispositivos.html`, añadir el estado `zwift-no-visible` («Zwift no visible») a `data-mock-states`: Neo y Square «Conectado» con «Olvidar»; arriba, `sq-notice is-danger` con `role="alert"`, icono `i-error` y «Zwift no puede encontrar la app en esta red wifi. Apaga y vuelve a encender el wifi del móvil; si sigue igual, reinícialo.»; fila de Zwift con `sq-conn off` y «No visible», sin la ayuda de emparejar; el mando, «Esperando» como en `zwift-esperando`. Incluir el estado en los `data-mock-state` de los elementos comunes (mando, «Buscar dispositivos»). Comprobar en los dos temas que solo se ve lo de ese estado.
- [x] 1.2 En `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html`, añadir el estado `zwift-no-visible`, igual que el estado sin Zwift, pero con el chip «Zwift» en `off` y `aria-label="Zwift, no visible"`, enlazando con `dispositivos.html#estado=zwift-no-visible`. Comprobar a 360 dp en vertical que los chips caben en una línea y que nada se mueve respecto al estado sin Zwift.
- [x] 1.3 Actualizar `maquetas/index.html` con el estado nuevo de las tres maquetas. Añadir a `design-system/components/ConnectionChip/README.md` el caso «No visible» (Zwift: `off`, anunciado pero sin respuesta en la red). Regenerar la guía (`python3 scripts/design-system/generar.py`).
- [x] 1.4 Validación de la maqueta por el usuario: `maquetas/dispositivos.html#estado=zwift-no-visible`, `maquetas/rodaje.html#estado=zwift-no-visible` y `maquetas/rodaje-vertical.html#estado=zwift-no-visible`, en los dos temas.

## 2. App: la sonda

- [x] 2.1 `:nucleo` `protocolo/ConsultaMdns.kt`: codificar la consulta QU del SRV de una instancia y reconocer en una respuesta el SRV de esa instancia con su puerto, con compresión de nombres y sin distinguir mayúsculas. Tests en `ConsultaMdnsTest` con la respuesta SRV de 155 bytes y la PTR de 296 bytes capturadas del OnePlus 8 (decisión 1), y con respuestas de otro nombre, otro puerto o truncadas, que no cuentan.
- [x] 2.2 `:app` `zwift/SondaMdns.kt`: interfaz y versión UDP, con multicast a `224.0.0.251:5353` por la interfaz del wifi, 3 intentos de 1 s y el resultado con el tiempo de respuesta; la variante unicast a la IP del wifi del propio móvil, elegible para la prueba. Sin `MulticastLock`.
- [x] 2.3 Validar la sonda en el OnePlus 8 (decisión 6.1). Con el anuncio visible desde el Mac (`dns-sd -L "Square Shift" _wahoo-fitness-tnp._tcp local.`), comprobar en el registro que la sonda multicast recibe respuesta y, si no, la unicast. Dejar la que funcione y apuntar el resultado en `design.md`. Si ninguna responde, parar y actualizar el change (`/openspec-update-change`) con el último recurso de la decisión 1.

## 3. App: la comprobación en DirCon

- [x] 3.1 `Anunciador.anunciar` espera el aviso de `NsdManager` y devuelve si se registró (`onRegistrationFailed` cuenta como fallo); adaptar `AnuncioNsd` y el doble de los tests.
- [x] 3.2 `EstadoTransporte.NoVisible`. En `DirconServer`: comprobar al registrarse y cada 60 s sin cliente; ante un fallo, volver a anunciarse una vez y comprobar; si vuelve a fallar, `NoVisible`, que sigue comprobando y vuelve a `Esperando` al responder; sin comprobar con cliente conectado. Apuntar cada resultado en el registro (decisión 5). Tests en `DirconServerTest` con sonda y anunciador dobles y reloj virtual: responde (sin aviso); falla y basta con reanunciar; falla dos veces (`NoVisible` en unos 10 s); se recupera; deja de responder más tarde (`NoVisible` antes de 2 min); cliente conectado (sin comprobaciones); `parar()` corta la comprobación.
- [x] 3.3 Build de depuración: el extra `sonda_nombre_falso` al abrir `MainActivity` hace que la sonda pregunte por un nombre que no existe (decisión 6.3); ausente en la de publicación.

## 4. App: estado y pantallas

- [x] 4.1 `EstadoZwift.NoVisible` y su traducción en `Sesion.zwiftCambia`, con sin wifi por delante. Tests en `SesionTest`: `NoVisible` con wifi; sin wifi gana a `NoVisible`; al volver el wifi se publica de nuevo y pasa a `Esperando`.
- [x] 4.2 Dispositivos: fila de Zwift con `ConnectionChip` `off` y «No visible», el `Notice` de error con su texto y sin la ayuda de emparejar, como la maqueta aprobada. Tests de `DispositivosUiState` y de Compose.
- [x] 4.3 Rodaje: chip «Zwift» en `off` con la descripción «Zwift, no visible» (con el mando activado, «Zwift, no visible; mando, esperando»), que abre Dispositivos. `NoVisible` manda sobre el estado del mando al elegir la forma del chip. Tests de `RodajeUiState` (con y sin mando) y de Compose (no cambia de ancho al pasar de `NoVisible` a esperando).
- [x] 4.4 `./gradlew :nucleo:test :app:testDebugUnitTest` en verde.

## 5. Prueba con el equipo real

- [x] 5.1 En el OnePlus 8, con el extra `sonda_nombre_falso`: el registro muestra el reintento y «No visible» en unos 15 s, Dispositivos y Rodaje se ven como en la maqueta, y al abrir sin el extra y reiniciar el wifi, la vuelta a «Esperando».
- [ ] 5.2 Tres sesiones normales con Zwift en PC: ningún «No visible», el registro dice que el anuncio responde y, con Zwift conectado, no hay comprobaciones.
- [ ] 5.3 Si el fallo real se repite mientras dura el change: confirmar desde el Mac con `dns-sd` que no se ve, que la app muestra «No visible» y que reiniciar el wifi lo quita; apuntar si bastó con volver a anunciarse (decisión 3). Si no se repite, queda «a validar» en `arquitectura.md`.

## 6. Documentación

- [x] 6.1 Actualizar la documentación: `docs/planificacion.md` (fila y nota del change 6b), `docs/arquitectura.md` (§ 4.3 `SondaMdns`, § 7.3 la comprobación del anuncio, lo validado y lo que sigue «a validar»), `docs/funcional.md` (nota en F8) y `docs/desarrollo.md` (comprobar el anuncio desde el Mac con `dns-sd -B` y `dns-sd -L`, y la sonda con `sonda_nombre_falso`). Revisar que no haya nada más que cambie.
