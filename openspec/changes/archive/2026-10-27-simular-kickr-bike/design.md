# Design

## Context

Punto de partida, tras `mando-zwift-dircon` (13b):

- Hay dos modos de conexión con Zwift, `ConexionZwiftFtms` y `ConexionZwiftRide`, detrás de la interfaz `ConexionZwift`. Cada uno se anuncia por DirCon con su identidad (`puenteZwift` y `puenteZwiftRide` en `estado.json`: `mac` y `serie`) y con el nombre fijo «Square Shift» (`DirconServer.NOMBRE`). La `Sesion` publica la conexión del modo elegido y la sustituye al cambiar el ajuste, soltando antes las funciones de Zwift y manteniendo lo último que pidió Zwift 30 s (decisión 6 del 13b).
- Zwift recuerda cada dispositivo DirCon por `mac-address` y `serial-number` (hallazgo de identidad del 13b): con una identidad ya emparejada de otra forma, el puente se quedaba en «Conectando…».
- El anuncio mDNS es `_wahoo-fitness-tnp._tcp` con TXT `ble-service-uuids`, `mac-address` y `serial-number`. El nombre de instancia es lo único que hoy dice «Square Shift» al emparejar.
- La pregunta 8 del 13b (declarar capacidad de rodillo en la información del dispositivo no quita el aviso de marchas virtuales) dejó como hipótesis sin confirmar que Zwift reconoce los rodillos aptos por una lista de dispositivos. Otra app se anuncia con instancia `KICKR BIKE PRO …`; su análisis solo prueba que es lo que hace, no que sea la causa.

Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change.

## Goals / Non-Goals

**Goals:**

- Un interruptor en Ajustes que cambie el nombre de instancia y la identidad con que se anuncia el rodillo por DirCon, en los dos modos, y que se pueda activar y desactivar con la sesión abierta.
- Poder responder con el equipo real a «¿cambia Zwift su trato con el nombre de un KICKR BIKE?» sin recompilar.
- Volver al estado de siempre al desactivarlo, sin perder los emparejamientos que ya existían.

**Non-Goals:**

- Cambiar el TXT (`ble-service-uuids`), los servicios, Device Information (`0x180A`), la página 0 del protocolo de Zwift o el protocolo. Solo el nombre de instancia y la identidad. Lo descartado y el orden para retomarlo están en «Descartado por ahora».
- Nombre o identidad editables a mano, o varios modelos.
- Garantizar que Zwift acepte las marchas virtuales: este change es el medio de probarlo.

## Decisions

### 1. Un ajuste booleano en `Configuracion` versión 7

`simularKickrBike: Boolean = false` en `Configuracion`, junto a `conexionZwift` y `mandoZwift`. La migración desde la versión 6 solo añade el campo con el valor por defecto. Exportar e importar (F12.6) lo llevan sin cambios, y una versión 7 en una app de la 6 se rechaza como más nueva, como ya hace el resto. Es un ajuste de la configuración (preferencia), no del estado: la identidad no viaja.

El número de versión asume que `mando-zwift-dircon` ya dejó la 6 en la rama. Si se implementara antes, la versión sería la que corresponda en ese momento.

### 2. Cuatro identidades guardadas, elegidas por modo y ajuste

La identidad que usa cada modo pasa a depender del ajuste. `EstadoGuardado` gana dos identidades más, las de la simulación (una por modo), generadas la primera vez que se necesitan, igual que `puenteZwift` y `puenteZwiftRide`:

| | Ajuste desactivado | Ajuste activado |
|---|---|---|
| `ConexionZwiftFtms` | `puenteZwift` | `puenteKickr` |
| `ConexionZwiftRide` | `puenteZwiftRide` | `puenteKickrRide` |

`ConexionZwift` ya expone `identidad(guardado)` y `conIdentidad(guardado, identidad)`; ganan el parámetro del ajuste, de modo que la `Sesion` no sabe qué campo se usa. Ninguna rama `if (simular)` en la `Sesion`: la decisión vive en cada implementación (el 13b la fijó como principio, decisión 1).

**Por qué identidad aparte y no solo otro nombre.** Zwift recuerda por `mac` y `serie`, no por nombre (hallazgo del 13b). Cambiar solo el nombre con la misma identidad dejaría a Zwift con el emparejamiento anterior y podría no volver a clasificar el dispositivo, con lo que la prueba no diría nada. Con una identidad nueva Zwift lo trata como un dispositivo que no conoce, que es lo que se quiere comprobar. Además, desactivar el ajuste recupera la identidad de siempre y Zwift la reconoce. Alternativa descartada: una identidad única regenerada al cambiar el ajuste, que obligaría a emparejar de nuevo «Square Shift» cada vez que se desactiva.

**Estado de los emparejamientos.** `estado.json` es del móvil, no se exporta con la configuración; un móvil nuevo genera las suyas.

### 3. El nombre de instancia lo da la identidad

`DirconServer` deja de usar `NOMBRE` como valor fijo: el nombre de instancia pasa a ser un argumento de `publicar` (junto a la identidad). `ConexionZwift` lo calcula:

- Ajuste desactivado: «Square Shift» (`DirconServer.NOMBRE`), como hasta ahora.
- Ajuste activado: `KICKR BIKE PRO` + espacio + los cuatro últimos caracteres hexadecimales de la `mac` de la identidad de la simulación, en mayúsculas (p. ej. `50-50-25-6C-66-9C` → `KICKR BIKE PRO 669C`). Como la `mac` es estable, el nombre es estable entre sesiones y distinto en cada modo. La función que lo compone es pura y está en `:nucleo` (`NombreAnunciado`), con tests.

El nombre de instancia mDNS no distingue por sí solo el dispositivo para Zwift (decisión 7 del 13b: Zwift distingue por identidad), así que los cuatro caracteres sirven para el usuario y para parecerse al formato real de los nombres; no son identidad.

**Modelo fijo.** Un único modelo, `KICKR BIKE PRO`, porque es el que se analizó. Hacerlo elegible queda para si la prueba apunta a otro.

### 4. Lo que no cambia

El TXT sigue anunciando los servicios que de verdad se ofrecen (`ble-service-uuids` lo calcula el servidor GATT), la información del dispositivo sigue diciendo «Square Shift» y el comportamiento es el mismo. No se declara nada que no se ofrezca: lo único que cambia es cómo se llama la app y con qué identidad se anuncia. Si la prueba demostrara que Zwift mira también el fabricante, el modelo o la página 0, sería otro change (ver «Descartado por ahora»).

### 5. Ciclo de vida: reutilizar el cambio de modo

Cambiar el ajuste con la sesión abierta hace lo mismo que cambiar de modo (decisión 6 del 13b): se sueltan las funciones de Zwift, se retira la conexión actual (anuncio incluido), se publica de nuevo con la identidad y el nombre del ajuste, y se mantiene lo último que pidió Zwift los 30 s de siempre, sin tocar la marcha ni lo que simula el Neo. La `Sesion` observa `simularKickrBike` junto a `conexionZwift` y reconstruye con el mismo camino. Sin wifi el ajuste se guarda y se aplica al anunciarse. El cambio no toca el camino caliente de la palanca (NF1); un test de la `Sesion` lo comprueba.

### 6. Interfaz y maqueta

Pasa por maqueta (AGENTS.md). Sin componentes nuevos:

- **Ajustes, «Zwift»:** el interruptor «Simular un KICKR BIKE», debajo de «Conexión con Zwift» y «Mando de Zwift», con el mismo patrón de interruptor con ayuda. La ayuda dice que Zwift verá un «KICKR BIKE PRO» en vez de «Square Shift», que es una prueba, que hay que emparejar de nuevo y que el emparejamiento de «Square Shift» se conserva.
- **Dispositivos:** la ayuda de emparejar nombra el dispositivo que se anuncia (decisión 3). La cadena lleva el nombre como argumento.
- **Diagnóstico:** el nombre anunciado bajo la fila de Zwift de Conexiones; el registro ya anuncia «Anunciado «nombre» en el puerto…» y recoge el nombre nuevo sin más.
- **Rodaje:** sin cambios.

## Risks / Trade-offs

- **Hacerse pasar por un producto de otra marca.** Es una prueba explícita y desactivada por defecto, y la ayuda lo dice. No cambia fabricante ni modelo en Device Information, ni los servicios del TXT → no se declara nada que no se ofrezca.
- **Puede no servir de nada.** Si Zwift sigue avisando, la hipótesis del nombre cae y el ajuste se puede retirar o dejar como herramienta de diagnóstico → es una prueba barata para decidir el 13c, no un compromiso de funcionalidad.
- **Puede que Zwift trate distinto al «KICKR BIKE» y lo rompa** (esperar protocolos que no implementamos: otros servicios, otras páginas) → el ajuste está desactivado por defecto, vuelve atrás al instante con el emparejamiento anterior intacto y, como el modo de siempre, los emparejamientos de «Square Shift» no se tocan.
- **Cuatro identidades guardadas.** Más estado en `estado.json`, pero simple y sin ramas en la `Sesion` → alternativas descartadas en la decisión 2.
- **Un cambio de modo y del ajuste a la vez.** Se aplican como una sola reconstrucción para no anunciar dos veces seguidas → la `Sesion` reconstruye por el par `(modo, ajuste)`.

## Migration Plan

- `configuracion.json` pasa a la versión 7 al leerse, con el ajuste desactivado. Quien no lo toque no nota nada.
- `estado.json` gana `puenteKickr` y `puenteKickrRide`, que se generan al activar el ajuste por primera vez en cada modo.
- Para volver atrás sin desinstalar, basta con desactivar el ajuste: Zwift reconoce el dispositivo de siempre.

## Validación con el equipo real

Todo lo de Zwift queda «a validar» hasta la prueba (AGENTS.md). Con Zwift en PC, el Neo y el Square, en cada modo:

1. Activar el ajuste: Zwift lista «KICKR BIKE PRO xxxx» y no «Square Shift», se empareja como potencia, cadencia, rodillo controlable y, en el modo todo por wifi, mando.
2. **Pregunta central:** con «Todo por wifi», pulsar en Zwift su botón de cambiar de marcha y mirar si desaparece el aviso de que el rodillo no admite marchas virtuales, si Zwift pide las páginas de rodillo (512, 528, 544) o cambia su comportamiento. Anotarlo en «Resultados de la prueba» de este documento y en `arquitectura.md` § 7.4.
3. Desactivar el ajuste: vuelve «Square Shift», reconocido sin emparejar de nuevo, con el comportamiento de antes.
4. Activar rodando: Zwift pierde la conexión, la marcha no cambia y el Neo mantiene lo último que pidió Zwift los 30 s.
5. Latencia de la palanca (NF1) con el ajuste activado.

## Descartado por ahora (para retomar si la prueba no basta)

Este change solo cambia el **nombre de instancia mDNS** y la **identidad** (`mac` y serie). Se deja fuera a propósito, y anotado para retomarlo, lo siguiente que también podría influir en que Zwift reconozca el dispositivo como un KICKR BIKE:

| Qué | Dónde está hoy | Por qué no se hace ya |
|---|---|---|
| Fabricante y modelo del servicio estándar Device Information (`0x180A`) | «Square Shift» (`ZwiftBridge`) | BikeControl, que se anuncia como KICKR BIKE PRO, no sirve `0x180A` (su descubrimiento DirCon devuelve solo `FC82`), así que Zwift no depende de él en ese caso. Poner «Wahoo» como fabricante es hacerse pasar por otra marca con más intención que un nombre de instancia |
| Página 0 del protocolo de Zwift (`PAGE_DEV_INFO`: nombre, `productId`, capacidades) | La que contesta el mando en el handshake del `FC82` (13b) | Declarar capacidad de rodillo ahí ya se probó en el 13b y no quitó el aviso, pero no se probó el nombre ni el `productId`. Es donde Zwift se identifica con sus propios dispositivos, así que es el candidato más probable tras el nombre |
| TXT con solo `FC82` | `ble-service-uuids` con todos los servicios que se ofrecen | Quitar servicios del anuncio cambiaría lo que Zwift ve como rodillo; no se puede probar sin romper la potencia y la cadencia por FTMS |

**Orden para retomarlo** si con el nombre y la identidad el aviso de marchas virtuales sigue (pregunta central de «Validación con el equipo real»), de uno en uno y no varios a la vez, para saber cuál importa:

1. Nombre y `productId` de la página 0.
2. Fabricante y modelo de `0x180A`.
3. El TXT con solo `FC82` (solo en el modo «Todo por wifi», donde la potencia y la cadencia ya van por el protocolo de Zwift).

Cada paso sería un change propio con su propio análisis (y la decisión de si conviene hacerse pasar por ese fabricante).

## Resultados de la prueba

**27-10-2026, OnePlus 8 y Zwift en PC.** Con el ajuste activado, Zwift lista el rodillo como «KICKR BIKE PRO …» y no como «Square Shift». **A validar con el equipo real:** si con «Todo por wifi» desaparece el aviso de marchas virtuales y si Zwift pide las páginas de rodillo (512, 528, 544); desactivar el ajuste y volver al dispositivo de siempre sin emparejar de nuevo; activar rodando; y la latencia de la palanca (NF1). Mientras no se pruebe el aviso, no se abre el siguiente change de «Descartado por ahora».

## Open Questions

- ¿Dejar el ajuste como herramienta de diagnóstico si no sirve para las marchas virtuales, o retirarlo? Se decide con el resultado de la prueba.
