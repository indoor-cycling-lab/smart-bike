## Context

El transporte DirCon (`DirconServer`, change `puente-zwift-wifi`) abre el servidor TCP en el puerto 36866 y pide el anuncio a `AnuncioNsd`, que registra el servicio con `NsdManager`. Después pasa a `EstadoTransporte.Esperando`, y la `Sesion` muestra `EstadoZwift.Esperando`. Lo que hay hoy y condiciona el diseño:

- `AnuncioNsd` apunta en el registro `onServiceRegistered` y `onRegistrationFailed`, pero nadie espera a esos avisos: el estado pasa a «Esperando» en cuanto se pide el anuncio.
- En el OnePlus 8, el anuncio lo hace el servicio mDNS del sistema con su motor en Java (`dumpsys servicediscovery`: `mUseJavaBackend true`, `MdnsAdvertiser`, sockets por interfaz de `MdnsSocketProvider`), no la app. La app no ve sus sockets ni puede reiniciarlos.
- Android no deja a las apps encender ni apagar el wifi desde la versión 10.
- `arquitectura.md` § 7.3 dice que no se usa `MulticastLock` porque el servicio del sistema responde por la app. Con la pantalla apagada está «a validar».

Lo observado el 27-10-2026 en el OnePlus 8, que este diseño toma como punto de partida:

1. **Con el anuncio roto.** `dumpsys servicediscovery` daba el servicio por registrado y con el probing terminado en `wlan0`. Desde el Mac, `dns-sd -B` y `dns-sd -L` no lo veían. Una consulta PTR con el bit QU enviada a `192.168.0.11:5353` no tuvo respuesta. Antes había errores `EADDRINUSE` al unirse al grupo multicast en `wlan0`.
2. **Tras apagar y encender el wifi.** Desde el Mac, el responder del móvil contesta a consultas con el bit QU desde un puerto cualquiera, tanto unicast a su IP como multicast a `224.0.0.251:5353`: PTR del tipo (296 bytes) y SRV de la instancia (155 bytes). A consultas sin QU desde un puerto distinto del 5353 no contesta, así que no hace las respuestas «legacy unicast» de RFC 6762 § 6.7.

Por tanto, una consulta QU distingue un anuncio sano de uno roto, al menos vista desde otro equipo.

## Goals / Non-Goals

**Goals:**

- Detectar desde la app que su anuncio no responde, sin otro equipo, y decir al usuario qué hacer.
- No dar falsos avisos en el uso normal.
- Que la comprobación sea del transporte DirCon y la `Sesion` solo traduzca su estado, para que el transporte Bluetooth (change 11) no cargue con ella.

**Non-Goals:**

- Reparar el servicio mDNS del sistema.
- Saber si Zwift está en la misma red o si de verdad ve la app.
- `MulticastLock` (ver `proposal.md`).

## Decisions

### 1. La sonda: una consulta mDNS QU a su propio anuncio, enviada por el wifi

La app envía una consulta mDNS con el bit QU que pide el SRV de «Square Shift._wahoo-fitness-tnp._tcp.local». La envía desde un socket UDP de puerto efímero, por multicast a `224.0.0.251:5353` y por la interfaz del wifi (`MulticastSocket.setNetworkInterface`, sin unirse al grupo, así que no hace falta `MulticastLock`). Espera hasta 1 s una respuesta unicast que traiga ese SRV con el puerto en el que escucha el servidor, y lo intenta hasta 3 veces. Si llega, el anuncio responde.

Es la misma pregunta que haría un equipo de la red, y el responder la recibe por el mismo socket de `wlan0` por el que la recibiría de Zwift. Así detecta el fallo observado, en el que ese socket no atendía nada.

- **Validado (27-10-2026, OnePlus 8):** el responder del sistema contesta a la consulta multicast que sale del propio móvil, que el kernel le entrega por bucle local (`IP_MULTICAST_LOOP`), en 2 ms y justo después del registro. Los intentos se reparten entre las dos variantes (multicast, unicast, multicast), 3 en total de 1 s cada uno, y el registro apunta cuál contestó.
- **Respaldo: unicast a la IP del wifi del propio móvil.** Se queda como segundo intento por si un fallo puntual pierde la consulta multicast. Puede que no le llegue al responder, si su socket está atado a `wlan0` y el paquete entra por `lo`; no hace falta validarlo, porque la multicast ya contesta.
- **Descartado: `NsdManager.resolveService` sobre el propio nombre.** El sistema puede contestar desde su caché o desde el anunciador local sin pasar por la red, así que no demostraría nada.
- **Último recurso, si ninguna sonda distingue los dos casos:** sin detección, un texto de ayuda en Dispositivos tras 2 minutos «Esperando». Cambiaría las specs, así que se pararía y se actualizaría el change (`/openspec-update-change`).

La codificación de la consulta y el reconocimiento de la respuesta van en `:nucleo` (`protocolo/ConsultaMdns.kt`), en Kotlin puro y con tests sobre las respuestas capturadas el 27-10. Hay que admitir la compresión de nombres DNS y comparar los nombres sin distinguir mayúsculas. La sonda con sockets va en `:app` (`zwift/SondaMdns.kt`), detrás de una interfaz para el doble de los tests. Sin dependencias nuevas: jmDNS o dnsjava serían excesivos para una pregunta y una respuesta.

### 2. La comprobación es del transporte DirCon

`DirconServer` comprueba su anuncio dentro de su propio trabajo, mientras no haya cliente:

1. `Anunciador.anunciar` pasa a esperar el aviso de `NsdManager` y devuelve si se registró. Un `onRegistrationFailed` cuenta como «no responde».
2. Con el anuncio registrado, se comprueba al momento y después cada 60 s.
3. Si falla, retira el anuncio, lo vuelve a pedir y comprueba otra vez (decisión 3).
4. Si vuelve a fallar, pasa a `EstadoTransporte.NoVisible` y sigue comprobando cada 60 s. Cuando responda, vuelve a `Esperando`.
5. Con un cliente conectado, no comprueba nada. Al irse el cliente, vuelve al paso 2.

`EstadoTransporte` gana `NoVisible` y `EstadoZwift` gana `NoVisible`. `Sesion.zwiftCambia` los traduce, con «sin wifi» por delante como hasta ahora. La pérdida y la vuelta del wifi ya vuelven a publicar el servidor (`wifiCambia`), así que el reinicio del wifi lanza una comprobación nueva sin código adicional.

Los tiempos salen de lo medido: el registro tarda menos de 1 s (probing de 0,85 s), cada comprobación hasta 3 s y volver a anunciarse unos 2 s, así que «No visible» llega en unos 10 s (la spec da 15 s de margen). Una consulta por minuto mientras se espera a Zwift no se nota en la red ni en la batería.

### 3. Volver a anunciarse una vez antes de avisar

Retirar el anuncio y pedirlo de nuevo cuesta unos 2 s y puede bastar. En el volcado del 27-10, retirar el último servicio lleva a `unrequestSocket`, y uno nuevo a `requestSocket`. Si ninguna otra app tiene el socket pedido, el sistema lo recrea. Si otra app lo mantiene (Google Play Services buscaba `_googlecast`), no lo recrea, y entonces avisa al usuario. Solo se reintenta una vez por fallo, para no entrar en un bucle de altas y bajas. **A validar** si alguna vez basta, mirando el registro cuando el fallo se repita.

### 4. El aviso y su texto

- Dispositivos: la fila de Zwift con `ConnectionChip` `off` y «No visible». Arriba, un `Notice` `is-danger` con «Zwift no puede encontrar la app en esta red wifi. Apaga y vuelve a encender el wifi del móvil; si sigue igual, reinícialo.». Se quita la ayuda de emparejar, que no sirve en ese estado. Es el mismo patrón que «Square no encontrado».
- Rodaje: el chip «Zwift» con el anillo vacío y el borde de alerta, como sin wifi, con la descripción «Zwift, no visible». Sin aviso en el pie: rodando sin Zwift no hace falta interrumpir, y el detalle está en Dispositivos.
- Diagnóstico comparte los avisos de conexión con Dispositivos (`avisosDeConexion`), así que muestra el mismo aviso, y su fila de Zwift dice «No visible» con «Apaga y vuelve a encender el wifi del móvil».
- Primero el wifi y después el móvil: es lo que lo arregló el 27-10 y lo que el usuario hacía con qdomyos-zwift.

No hay componentes ni tokens nuevos. Es un estado nuevo, así que pasa por maqueta (AGENTS.md).

### 5. El registro

Cada comprobación deja un suceso de origen Zwift en el registro de la sesión: «Anuncio: responde en N ms», «Anuncio: sin respuesta, se vuelve a anunciar» o «Anuncio: sin respuesta, Zwift no visible». Son unos 60 por hora como mucho, poco frente al tráfico del Neo y del Square, y sirven para decidir la decisión 3 y para reconocer el fallo cuando se repita.

### 6. Cómo se valida con el equipo real

El fallo real no se puede provocar a voluntad. Se valida por partes:

1. **La sonda en el caso sano**, primera tarea de la app. En el OnePlus 8 con el anuncio visible desde el Mac (`dns-sd -L "Square Shift" _wahoo-fitness-tnp._tcp`), la sonda recibe respuesta. Si la variante multicast no la recibe, se prueba la unicast. Si ninguna, se para (decisión 1, último recurso).
2. **Sin falsos avisos.** En tres sesiones normales con Zwift en PC, ningún «No visible», y el registro dice que responde. Zwift conectado: no hay comprobaciones.
3. **El camino del aviso, en el móvil.** Solo en la build de depuración, un extra al abrir la actividad (`adb shell am start … --ez sonda_nombre_falso true`) hace que la sonda pregunte por un nombre que no existe. Se ve el reintento, «No visible» en Dispositivos y en Rodaje, y al quitarlo (abrir sin el extra y reiniciar el wifi), la vuelta a «Esperando». No llega a la build de publicación.
4. **El fallo real, cuando se repita.** Si aparece «No visible», se comprueba desde el Mac con `dns-sd` que de verdad no se ve, y después que reiniciar el wifi lo quita. Se apunta en `arquitectura.md`. Mientras no haya pasado, la detección del fallo real queda «a validar».

## Risks / Trade-offs

- [El responder no contesta a consultas del propio móvil] → Alternativa unicast y, si tampoco, último recurso sin detección y actualización del change (decisión 1).
- [Falso «No visible» por una pérdida puntual de paquetes en el wifi] → 3 intentos por comprobación y un nuevo anuncio antes de avisar. El aviso se quita solo en la siguiente comprobación que responda.
- [Un sistema sano que no contesta a QU en otros móviles o versiones de Android] → Solo se prueba en el OnePlus 8, que es el móvil del proyecto. Si cambia, la validación 1 lo detecta.
- [La sonda responde y aun así Zwift no ve la app] (red con aislamiento de clientes, Zwift en otra red) → Fuera de alcance. El aviso no aparece, como hoy.
- [Volver a anunciarse corta un emparejamiento en curso] → Solo se hace sin cliente conectado y tras una comprobación fallida, cuando Zwift no podía verla de todos modos.
