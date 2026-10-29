# Design

## Context

Estado de partida, tras `mando-zwift-dircon`, `simular-kickr-bike` y `completar-protocolo-dircon-zwift`:

- Lo que la app publica ante Zwift son tres ajustes sueltos de `Configuracion` (versión 7): `conexionZwift` (`ModoConexionZwift`: `RODILLO_Y_MANDO_BLUETOOTH` y `TODO_WIFI`), `mandoZwift` (booleano) y `simularKickrBike` (booleano), con sus filas al final de Ajustes. Dos implementaciones de `ConexionZwift` (`ConexionZwiftFtms` y `ConexionZwiftRide`) y las identidades por modo y por nombre viven en `estado.json` (`puenteZwift`, `puenteZwiftRide` y las de «KICKR BIKE»).
- La barra inferior (`Pestana`: Rodar, Dispositivos, Botones, Ajustes) y las rutas están en `ui/navegacion/Navegacion.kt`; Asignar acción se cuenta como de Botones y Marchas, Datos, Tabla y Diagnóstico, como de Ajustes.
- Dispositivos muestra, además del Neo y el Square, las filas de Zwift y del mando con sus estados (incluido «No visible»); la barra de conexiones de Rodaje abre Dispositivos al tocarla.
- El mando por wifi (Zwift Ride) hoy se puede desactivar con `mandoZwift`; así lo recogen `zwift-controller` y `button-mapping`.

Motivo y alcance: `proposal.md`. Comportamiento: las specs de este change, sobre todo `platform-settings`.

## Goals / Non-Goals

**Goals:**

- Que lo que la app publica tenga un único sitio, con un modelo (plataforma → modos compatibles) en el que añadir FTMS por Bluetooth u otra plataforma sea añadir filas a una tabla y no rehacer la pantalla.
- Que la configuración no pueda quedar en una combinación inválida, ni al elegir en la UI ni al importar una copia o migrar.
- Que migrar no obligue a emparejar de nuevo en Zwift: mismas identidades, mismos modos.

**Non-Goals:**

- Ningún protocolo nuevo ni cambio de los existentes (DirCon, mando, identidades, marchas).
- FTMS por Bluetooth y otras plataformas (change 11).
- Rediseñar Rodaje más allá del destino al tocar los iconos de Zwift y del mando.

## Decisions

### 1. El modelo: plataforma → modos, no interruptores

`:core` gana `Plataforma` (hoy solo `ZWIFT`, serializada `zwift`) y una tabla de compatibilidad en código: para cada plataforma, la lista de modos que admite, el recomendado y, por modo, si lleva mando (`NINGUNO`, `OPCIONAL_BLUETOOTH` o `DENTRO_DE_DIRCON`) y si está validado. La pantalla, la normalización de `Configuracion` y los tests la leen; no hay lógica de compatibilidad repartida.

| Plataforma | Modo | Mando | Estado |
|---|---|---|---|
| Zwift | Rodillo por wifi (FTMS), recomendado | Opcional, por Bluetooth | Validado en Zwift en PC |
| Zwift | Todo por wifi (modo Zwift Ride) | Siempre dentro de DirCon | Validado en parte (ver `zwift-controller`) |

Alternativas descartadas:

- *Lista de interruptores independientes* (wifi, Bluetooth, Ride, mando): permite combinaciones que no existen o chocan (Ride con un mando Bluetooth aparte, dos anuncios con la misma identidad) y obliga al usuario a acertar. Ya se decidió no publicar más de un modo a la vez.
- *Solo por modo de conexión, sin plataforma*: es lo que la app implementa, pero el mando es de Zwift y el usuario piensa en «rodar en X». La plataforma se guarda porque un mismo modo (FTMS por Bluetooth, en el change 11) servirá para varias.

El nombre del primer modo cambia de «Rodillo por wifi y mando por Bluetooth» a **«Rodillo por wifi (FTMS)»**, porque el mando ya es un interruptor. Las specs antiguas que dicen «el modo de siempre» se refieren a él; el enum `ModoConexionZwift` y sus nombres serializados no cambian (`rodillo-y-mando-bluetooth`, `todo-wifi`), para no tocar `estado.json`, los tests ni las copias de seguridad. Cuando el change 11 añada un modo no ligado a Zwift se generaliza el tipo.

### 2. Configuración v8 y migración

`Configuracion.VERSION` pasa de 7 a **8**:

- Nuevo `plataforma` (`zwift`), nuevo `identidad` (`smart-bike` o `kickr-bike-pro`) y se retira `simularKickrBike`.
- `conexionZwift` y `mandoZwift` conservan nombre y valores.
- Migración v7→v8: `identidad = KICKR_BIKE_PRO` si `simularKickrBike` era `true`, `SMART_BIKE` si no; `plataforma = ZWIFT`. La migración vive donde las anteriores (`Configuracion.deJson`) y la usa también la importación de copias (`settings-backup`), sin cambios allí.
- **El mando en Zwift Ride**: `mandoZwift` se conserva tal cual pero Ride lo **ignora** (siempre ofrece el mando). Así, quien tenía Ride con el mando desactivado conserva su modo y su emparejamiento, y al volver al modo de rodillo por wifi recupera el interruptor como lo dejó. Es el único cambio de comportamiento observable de la migración (propuesta, **BREAKING**).
- `normalizada()` fuerza que `conexionZwift` esté entre los modos de la plataforma (si no, el recomendado).

Las identidades de `estado.json` no cambian: la clave es (modo, nombre), y el nombre ahora sale de `identidad` en lugar del booleano.

### 3. Aplicar los cambios con la sesión abierta

La `Sesion` ya cambia de modo y de nombre al momento (`cambiarConexionZwift` y el nombre anunciado). Su lógica no cambia en lo esencial: lee `identidad` donde leía `simularKickrBike` (`usarConexionZwift`) y calcula el mando activo como «siempre, en Ride; `mandoZwift`, en rodillo por wifi» donde hoy lee `mandoZwift` directamente (el filtro de botones y `mandoActivado`). El requisito de mantener durante 30 s lo último que pidió Zwift ya está resuelto en las specs de `zwift-bridge`; este change no lo toca.

### 4. Navegación

`Pestana` pasa a Rodar, Dispositivos, **Plataformas**, Ajustes; `Rutas.PLATAFORMAS` nueva. `Pestana.de` manda `BOTONES` y `ASIGNAR` a Ajustes; `vecina` no cambia (recorre el enum). Atrás desde Botones vuelve a Ajustes como las demás secundarias, y desde Asignar acción, a Botones. La entrada «Botones» de Ajustes lleva el icono propio de los cuatro símbolos (hoy el de la pestaña) y la transmisión de la asignación.

### 5. Plataformas, Dispositivos y Rodaje

- `ui/plataformas` nueva (`UiState`, `ViewModel`, composable) con la misma estructura que Ajustes y reutilizando sus filas de elección y de interruptor. El estado de Zwift y del mando, que hoy calcula `DispositivosUiState`, se mueve a `PlataformasUiState`; la `Sesion` ya lo expone.
- Dispositivos queda con el Neo, el Square, el ventilador y el pulsómetro. «No visible» sale de allí (spec `zwift-bridge`).
- En Rodaje, la barra de conexiones se queda solo con los dispositivos a los que se conecta la app (Neo, Square y, si están recordados, pulsómetro y ventilador) y sigue siendo un único objetivo táctil que abre Dispositivos. Los iconos de Zwift y del mando salen de ella. El Square pasa a llevar el icono de un mando de consola (`i-sports_esports`) y se deja de usar el del ciclista en la barra; el icono `i-directions_bike` se retira del sprite y de la app en la etapa 2.
- El estado de la plataforma va aparte, en un `PlatformBadge` (chip `sq-conn` de una línea) a la izquierda de la fila del icono de Ajustes (en horizontal, a la izquierda de la fila superior), con el modo y la transmisión en la fila de debajo (en horizontal, en la esquina superior izquierda de la zona principal, de modo que los datos y los botones empiezan bajo la fila superior), que lleva el nombre de la plataforma elegida: «Conectado a Zwift», «Esperando conexión a Zwift…» (atenuado), «Zwift no visible» o «Sin wifi para Zwift». Con el modo de rodillo por wifi y el mando activo, si el rodillo está conectado y el mando no, dice «Conectado a Zwift · sin mando». Tocarlo abre Plataformas. No hay opción de «sin plataforma»: con una sola plataforma se muestra esa.
- La generalización del nombre «Zwift» a otras plataformas sale sola del nombre de la plataforma elegida.

### 6. Selector de plataforma

Con una sola plataforma, la pantalla muestra «Plataforma: Zwift» como cabecera y **no** un selector con una opción. El componente de selector (tarjetas o segmentos, a decidir en la maqueta) entra en el sistema de diseño y se enseña en la guía con una y con varias plataformas, para que el change 11 no tenga que rediseñarlo. La maqueta de la app muestra solo lo que la app tendrá.

### 7. Acciones de Zwift sin mando

Las filas de «Funciones de Zwift» de Botones **se mantienen siempre** y solo cambia su aviso («no llegan a Zwift por la app»), como hoy con el mando desactivado: ocultarlas haría perder la asignación al cambiar de modo y de vuelta. Con el modelo de este change no hay ningún modo sin mando salvo «rodillo por wifi con el interruptor apagado», así que el aviso actual basta; una plataforma futura sin mando lo reutilizará.

### 8. Icono de la pestaña Plataformas

Un icono nuevo que sugiere emitir o publicar, de Material Symbols en el mismo peso que los demás (candidatos: `sensors`, `cell_tower`, `cast`), distinto de Bluetooth, wifi y el mando de Zwift. Se elige en la maqueta, se copia a `design-system/icons.svg` y se regenera (`ic_*.xml` y `IconosSincronizadosTest`). Por defecto, `sensors`.

### 9. Descartar el 13c

`marchas-de-zwift` pasa a «Descartado» en `planificacion.md` (fila y nota) con el motivo del usuario: la app implementa sus propias marchas virtuales y no se acopla a un protocolo de Zwift no público; lo que se emula del protocolo de Zwift es lo necesario para conectar rodillo y mando/Square por wifi. `funcional.md` lo recoge en F9.3 y `arquitectura.md` en la sección del modo Zwift Ride.

## Risks / Trade-offs

- [Migrar cambia sin querer la identidad y Zwift pide emparejar de nuevo] → la clave de identidad (modo, nombre) no cambia y la migración se prueba con una configuración v7 de cada combinación; prueba con Zwift en PC tras instalar sobre una v7 emparejada.
- [Quien usaba Zwift Ride con el mando desactivado ve ahora los botones llegando a Zwift] → es la decisión tomada; la migración lo conserva en el modo y el valor guardado vuelve al cambiar a rodillo por wifi. Se anota en `funcional.md`.
- [El estado de la plataforma, siempre visible, resulta ruidoso al rodar sin Zwift] → va en tono atenuado cuando solo espera y no cambia el área de datos; se revisa en la maqueta y en el móvil.
- [Mover las filas de Zwift de Dispositivos descoloca a quien las busca allí] → la ayuda y el estado completos están en Plataformas, y el icono de Zwift en Rodaje lleva directo.
- [La tabla de compatibilidad promete lo no validado] → cada fila lleva el estado «validado» o «a validar» y la pantalla solo ofrece lo implementado; lo «a validar» de las specs sigue marcado.
- [Una pestaña con un solo modo parece vacía] → son dos modos y el mando/nombre, sin selector de plataforma hasta que haya más de una; se valida en la maqueta.

## Migration Plan

1. Etapa 1: sistema de diseño y maquetas; puerta de validación del usuario.
2. Etapa 2: `Configuracion` v8 y migración con tests; navegación; pantalla Plataformas; Dispositivos, Ajustes, Botones y Rodaje; Diagnóstico.
3. Prueba en el OnePlus 8 con Zwift en PC: actualizar sobre una instalación con la v7 y comprobar que Zwift reconoce el dispositivo sin emparejar.
4. Retroceso: la app anterior no entiende una configuración v8 (la importación la rechaza como «versión más nueva», `settings-backup`); la vía de vuelta es exportar la copia de seguridad antes de actualizar.

## Open Questions

- Forma del selector de plataforma en la guía (tarjetas o segmentos) e icono final de la pestaña: se resuelven en la maqueta con el usuario, sin cambiar las specs.
