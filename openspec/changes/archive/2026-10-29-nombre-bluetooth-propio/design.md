# Design

## Context

- El anuncio BLE lo monta `Advertiser` (`ble/Advertiser.kt`). En la respuesta de escaneo solo lleva `setIncludeDeviceName(true)`, es decir, el nombre del adaptador. `AdvertiseData` no permite meter un *Complete Local Name* propio, y la característica GATT *Device Name* (`0x2A00`) la sirve la pila del sistema con ese mismo nombre.
- Dos `GattServerHost` comparten un único `Advertiser` (`AppGraph`): el del controlador aparte (`MandoZwiftBluetooth`, «Wifi + Bluetooth») y el del rodillo (`TransporteBluetooth`, «Solo Bluetooth»). Los dos se anuncian en `onServiceAdded`, tras añadir los servicios.
- Lo que se publica lo decide `Publicacion.de(modo, configurados)` en `:core` (`ServiciosPublicados`). La `Sesion` llama a `PuentePlataforma.usar(servicios, identidad)` cuando cambian la configuración o los configurados.
- `Configuracion.identidadEfectiva` devuelve «Smart Bike» cuando el modo no deja elegir nombre (`ModoPublicado.nombreElegible`, falso en «Solo Bluetooth»).
- La sesión dura desde que se abre la app hasta Atrás desde Rodaje o «Terminar» (arquitectura.md § 4.4).
- `AdaptadorBluetooth.nombre()` ya lee el nombre del adaptador para las ayudas de Conexión y para Diagnóstico.

## Goals / Non-Goals

**Goals:**
- Que el renombrado dure solo mientras haya algo publicado por Bluetooth y que el nombre original se recupere siempre, también tras un cierre inesperado.
- Que el anuncio salga ya con el nombre nuevo, no con el anterior.
- Que la decisión de qué nombre toca sea una función pura de `:core`, con sus tests.

**Non-Goals:**
- Cambiar el contenido del anuncio (servicios, datos de fabricante) o la identidad DirCon.
- Detectar en tiempo real que el usuario renombra el móvil en los ajustes de Android durante la sesión. Solo se tiene en cuenta al recuperar el nombre (decisión 4).

## Decisions

### 1. Renombrar con `BluetoothAdapter.setName`, no anunciar otro nombre

Es la única vía pública. `setName` necesita `BLUETOOTH_CONNECT` en Android 12 o superior y `BLUETOOTH_ADMIN` hasta Android 11; los dos ya están en el manifiesto y se piden con «Buscar». Devuelve `false` con el Bluetooth apagado. El cambio es asíncrono y se confirma con `BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED`.

Alternativas descartadas:
- Meter el nombre en los datos de fabricante o de servicio: Zwift no los muestra como nombre.
- Anunciar sin nombre (`setIncludeDeviceName(false)`): Zwift mostraría un dispositivo sin nombre o uno genérico, y no el elegido.

### 2. `RenombradorBluetooth` en `ble/`, con un `StateFlow` de estado

Una clase nueva envuelve `setName`, el receptor de `ACTION_LOCAL_NAME_CHANGED` y la persistencia del nombre original:

- `aplicar(deseado: String?)` es una función `suspend`. Con un nombre, si el adaptador ya se llama así, no hace nada. Si no, guarda el original (decisión 4), llama a `setName` y espera la confirmación con un tiempo máximo de 3 s. Con `null`, recupera el original si lo hay.
- `estado: StateFlow<EstadoNombre>`: `Propio` (sin renombrar), `Renombrado(nombre)` o `NoSePudo(motivo)`. Lo leen Conexión (aviso y ayudas) y Diagnóstico.

Se separa de `Advertiser` porque el nombre es un estado del adaptador compartido por los dos servidores GATT, no de cada anuncio.

### 3. Quién decide el nombre: una función pura y la `Sesion`

En `:core`, `NombreBluetooth.deseado(config, servicios, identidad): String?` devuelve el nombre si se cumplen las tres condiciones: el ajuste está activado, `servicios` publica algo por Bluetooth (modo `FTMS_BLE`, o `ServiciosPublicados.mando` aparte por Bluetooth) y hay identidad. En otro caso, devuelve `null`. El nombre es `NombreAnunciado.de(identidad, simularKickr)`, el mismo que por wifi.

La `Sesion`, en el mismo punto en que llama a `PuentePlataforma.usar`, calcula el nombre deseado y llama a `renombrador.aplicar(...)` **antes** de `usar`. Así, cuando el `GattServerHost` termina de añadir servicios y se anuncia, el adaptador ya tiene el nombre nuevo. Si el nombre cambia con el servidor ya anunciado (se marca la casilla o se cambia el nombre rodando), la `Sesion` le pide al puente que se vuelva a anunciar (`Advertiser.iniciar` con el mismo `Anuncio`). Esa reanunciación tiene el mismo efecto que un cambio de modo: Zwift tiene que buscar de nuevo, como dice la ayuda.

Al terminar, `Sesion.terminar()` llama a `aplicar(null)` después de parar los servidores.

La identidad efectiva cambia: `identidadEfectiva` admite el nombre elegido también en «Solo Bluetooth» cuando el ajuste está activado. La identidad se busca con la clave (modo, nombre) de siempre, así que el sufijo de «KICKR BIKE PRO» por Bluetooth es estable y distinto del de wifi.

Alternativa descartada: renombrar dentro de `GattServerHost` justo antes de anunciarse. Mezclaría el nombre global con cada servidor, y con dos servidores habría que decidir cuál lo devuelve.

### 4. Nombre original en `estado.json` y recuperación al arrancar

Antes del primer `setName`, se guarda en `EstadoGuardado` el campo `nombreBluetoothOriginal` (el nombre del móvil) junto con `nombreBluetoothPuesto` (el que pone la app). Son opcionales y no cambian la versión, como `pulsometro`. Al recuperar el nombre:

- Si el adaptador sigue llamándose `nombreBluetoothPuesto`, se llama a `setName(original)`.
- Si se llama de otra forma, el usuario lo ha cambiado: no se toca.
- En los dos casos, se borran los dos campos.

`AppGraph`, al crearse (al abrir la app y antes de `iniciarSesion`), hace esta recuperación si quedaron campos guardados. Si después la sesión vuelve a necesitar el nombre propio, lo pone de nuevo. Es un renombrado más, pero evita tener un caso especial.

No se exporta: `estado.json` no entra en la copia de seguridad (F12.6).

### 5. El ajuste va en `AjustesConexion`

`AjustesConexion` gana `nombrePorBluetooth: Boolean = false`. No cambia la versión de `Configuracion` y no hay migración: un documento sin el campo lo lee desmarcado, como `bajadas`. Se exporta e importa con el resto.

### 6. Anunciarse aunque falle el renombrado

Si `aplicar` acaba en `NoSePudo` (porque `setName` devuelve `false`, salta una `SecurityException` o pasan 3 s sin confirmación), la `Sesion` sigue y publica igual: la app se anuncia con el nombre del móvil. Al encenderse el Bluetooth (`EstadoBluetooth.encendido`), la `Sesion` ya vuelve a montar la publicación; antes vuelve a llamar a `aplicar`. Cada intento y su resultado van al registro (`Origen.Plataforma`).

### 7. Interfaz: casilla `sq-check` en la sección «Nombre ante la app»

No hace falta ningún componente nuevo. La casilla usa `sq-check`, como las de Ajustes, y el aviso de fallo usa `Notice` en su variante de alerta, con icono y texto. En la Etapa 1, la maqueta decide la disposición exacta: la casilla bajo el selector en «Wifi + Bluetooth», y la casilla primero y el selector debajo en «Solo Bluetooth».

## Risks / Trade-offs

- **[Android o OxygenOS rechazan o ignoran `setName` en versiones recientes] → Mitigación:** la decisión 6 mantiene la publicación. Primera prueba en el OnePlus 8 antes de la Etapa 2: marcar la casilla, comprobar con otro móvil o con Zwift que el nombre cambia y que vuelve al terminar. **A validar.**
- **[El anuncio sale con el nombre anterior porque el adaptador aún no lo ha aplicado] → Mitigación:** se espera a `ACTION_LOCAL_NAME_CHANGED` antes de publicar (decisión 3). Validación: en el emparejamiento de Zwift, el nombre debe ser el nuevo desde el primer momento, sin reiniciar la búsqueda. **A validar.**
- **[Zwift guarda el nombre de un dispositivo ya visto] → Mitigación:** por Bluetooth, Android se anuncia con una dirección aleatoria por sesión, así que Zwift lo ve como nuevo. La ayuda ya avisa de que hay que emparejar de nuevo. **A validar** con Zwift en PC.
- **[«KICKR BIKE PRO» por Bluetooth hace que Zwift lo trate como un rodillo Wahoo y busque servicios propios de Wahoo] → Mitigación:** es una opción de prueba, igual que por wifi. Se valida con Zwift en «Solo Bluetooth» y, si falla, se documenta en la compatibilidad. **A validar.**
- **[En «Wifi + Bluetooth», el rodillo (wifi) y el controlador (Bluetooth) se llaman igual] → Mitigación:** salen en listas distintas de Zwift (rodillo y mandos), y la ayuda del controlador lo dice. **A validar** que Zwift no los confunde.
- **[El móvil se queda renombrado si la app no vuelve a abrirse tras un cierre forzado]:** es una consecuencia aceptada. La ayuda de la casilla lo menciona de forma implícita («al terminar recupera su nombre»), y abrir la app lo arregla.
- **[Otros dispositivos ven el móvil con otro nombre durante la sesión, por ejemplo los auriculares emparejados]:** es la razón de que sea opcional y de que esté desmarcada por defecto, con la ayuda que lo dice.

## Migration Plan

No hay migración: el campo nuevo de la configuración y los de `estado.json` son opcionales. Para volver atrás basta con desmarcar la casilla. Al bajar a una versión anterior de la app, el campo se ignora (`ignoreUnknownKeys`). Si el móvil quedara renombrado en ese momento, habría que cambiarle el nombre a mano.
