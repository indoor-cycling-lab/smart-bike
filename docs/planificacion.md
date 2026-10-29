# Planificación

> Estado: **borrador para revisión**. El orden de las fases 3–5 puede cambiar según lo que más se eche de menos al rodar.

Este documento ordena el trabajo: las fases, los changes de OpenSpec previstos en cada una, sus dependencias y su estado. Qué hace la app está en [`funcional.md`](funcional.md) (F1…F15, NF1…NF7) y cómo está construida, en [`arquitectura.md`](arquitectura.md). El contenido de cada change (propuesta, diseño, specs y tareas) vive solo en `openspec/changes/`; aquí se resume en una fila.

## Cómo se mantiene

- Al crear un change con `openspec new change`, su fila pasa a **En curso** con el nombre real. Si no estaba previsto, se añade en su fase.
- Al archivarlo, pasa a **Archivado**.
- Si un change se parte, se fusiona o se descarta, se refleja aquí en el mismo change que lo decide.

El repositorio se publicó el 29-10-2026 con un único commit inicial (`publicar-repo-publico`, change 20): el historial de commits anterior se conserva en una copia privada, y las decisiones de cada change, en `openspec/changes/archive/`. Las direcciones, IPs y nombres de dispositivos que aparecen en las notas históricas de los changes archivados son valores de ejemplo, no los del equipo real.

| Estado | Significado |
|---|---|
| Previsto | Planificado; aún no existe en OpenSpec |
| En curso | Existe en `openspec/changes/` |
| Archivado | Terminado y archivado; sus specs están en `openspec/specs/` |
| Descartado | No se hará; se indica el motivo |

## Fases

| Fase | Objetivo | Funcionalidades | Resultado verificable |
|---|---|---|---|
| **0. Diagnóstico** | Validar con el equipo real los supuestos técnicos | F1.1, F2.1, F13.1, F13.2 | Se ven todos los botones del Square y el Neo cambia de resistencia al enviarle una pendiente o una potencia |
| **1. Marchas sin Zwift** | Rodar con marchas usando el Square | F1, F2, F5.1–F5.6, F6.1, F7.1, F11 básica | Rodar con el Neo y el Square, cambiando marchas, sin Zwift |
| **2. Puente Zwift** | Sustituir la app actual en el uso diario | F8 por wifi, F13.3 | Rodar en Zwift con marchas desde el Square, con la app conectada por wifi (Square emparejado en Zwift para la dirección) |
| **3. Experiencia** | Interfaz y botones a gusto | F3, F4, F5.7–F5.8, F7.2–F7.3, F11 completa, F12 | Gestos, asignación configurable y pantalla de rodaje final |
| **4. Mando Zwift** | Un solo dispositivo ante Zwift | F8.5 por Bluetooth, F9 (prototipo aislado primero), F10 si F9 falla | Navegar y girar en Zwift sin emparejar el Square |
| **5. Modelo físico** | Marchas más realistas | F6.2–F6.5 | Comparar modelos en el Neo y elegir el predeterminado |
| **6. Experimentos** | Opcionales | F13.4, F14, F15 | Según resultado |

**Primer hito útil:** final de la fase 2. Las fases 3–5 pueden reordenarse según lo que más se eche de menos.

## Changes

| # | Change | Fase | Funcionalidades | Depende de | Estado |
|---|---|---|---|---|---|
| 1 | `base-sistema-diseno-y-maquetas` | — | Sistema de diseño, guía y maquetas | — | Archivado |
| 2 | `tema-compose-desde-tokens` | — | Ninguna funcional: `generar.py` genera `Tokens.kt`; esqueleto del proyecto Android | 1 | Archivado |
| 3 | `diagnostico-square-neo` | 0 | F1.1, F2.1, F2.2, F13.1, F13.2 | 2 | Archivado |
| 3b | `trafico-bajo-demanda` | 0 (ajuste) | F13.1: tráfico de Diagnóstico oculto al abrir, con la casilla «Tráfico en directo» | 3 | Archivado |
| 4 | `marchas-sin-zwift` | 1 | F5.1–F5.6, F6.1, F7.1, F11 básica | 3 | Archivado |
| 4b | `rodaje-vertical` | 1 | F11 (pantalla de rodaje en vertical, adelantada del 10) | 4 | Archivado |
| 5 | `conexion-automatica-en-segundo-plano` | 1 | F1.2–F1.5 | 3 | Archivado |
| 6 | `puente-zwift-wifi` | 2 | F8.1–F8.5 por wifi (DirCon), F13.3 | 4, 5 | Archivado (con pruebas pendientes) |
| 6b | `comprobar-anuncio-zwift` | 2 (ajuste) | F8.5, F1.4: comprobar que el anuncio mDNS responde en la red y, si no, mostrar Zwift «No visible» con qué hacer | 6 | Archivado (con pruebas pendientes) |
| 7 | `gestos-y-asignacion` | 3 | F3, F4, F12.4 | 4 | Archivado (con pruebas pendientes) |
| 7b | `proteccion-bajadas` | 5 (adelantado) | F6.5, F12.5 (ajuste «Bajadas») | 7 | Archivado (con prueba pendiente) |
| 8 | `transmisiones-de-marca` | 3 | F5.7 y F5.8 con la Shimano 105 del usuario, F12.3 (transmisión, modo, referencia, recordar) | 4 | Archivado (con prueba pendiente) |
| 8b | `transmisiones-otras-marcas` | 3 | F5.7 y F5.8: SRAM, Campagnolo y «A medida» | 8 | Previsto |
| 9 | `erg-manual` | 3 | F7.2, F7.3 | 4 | Archivado (con prueba pendiente) |
| 10 | `rodaje-completo-y-ajustes` | 3 | F11 completa (sin velocidad ni distancia), F12.1, F12.5 (pendiente sin Zwift), F12.7 y la barra inferior | 7, 7b, 8 | Archivado (con prueba pendiente) |
| 10c | `chips-conexion-compactos` | 3 | F11.5: chips de Rodaje sin el estado en palabra y en una línea en vertical | 10 | Archivado |
| 10d | `rejilla-datos-fija` | 3 | F11.3 y F11.8: área de datos fija en Rodaje, cifra estable y botones − y + sin cambiar los datos | 10c | Archivado |
| 10e | `acceso-ajustes-y-tema` | 3 | F11.9 (icono de Ajustes en Rodaje) y F12.8 (tema oscuro o claro en Ajustes) | 10d | Archivado |
| 10f | `barra-conexiones-iconos` | 3 | F1.4 y F11.5: barra de iconos en Rodaje en lugar de los chips, con el rodillo y el mando de Zwift por separado y sitio para más enlaces | 10e, 6b, 13 | Archivado |
| 10g | `orientacion-pantalla` | 3 | F12.8 y F11: orientación de la app en Ajustes (según el móvil, vertical u horizontal fijos) | 10e | Archivado |
| 10h | `barra-estado-rodaje` | 3 | F11.10 y F1.4: hora, wifi y batería del móvil en Rodaje, colocados según la orientación y la cámara | 10f | Archivado (con prueba pendiente; la retira el 30) |
| 10i | `identidad-app` | 3 | Ninguna funcional: icono de la app (piñonera, adaptativo y monocromo), splash animada e icono propio de «Botones» | 10e | Archivado |
| 10b | `exportar-bateria-y-giro` | 3 | F12.6, batería del Square y aviso de giro | 10 | Archivado (queda anotar la validación de la batería) |
| 10j | `pulso-zwift` | 3 | F16 (nueva): pulsómetro Bluetooth recogido por el móvil y reenviado a Zwift por DirCon, con su icono en Rodaje | 10f, 6 | Archivado |
| 10k | `ventilador-headwind` | 3 | F17 (nueva): ventilador Wahoo KICKR Headwind por Bluetooth: modo manual o automático y velocidad desde los botones del Square, y los umbrales de pulso de las zonas en Ajustes | 10f, 7 | Archivado |
| 11 | `ftms-bluetooth` | 4 | F8.5 (Bluetooth como alternativa), NF3: «Rodillo por Bluetooth (FTMS)» como un modo más de la pestaña «Plataformas», con el mando de Zwift dentro del mismo dispositivo (mismo servidor y anuncio, validado con Zwift en PC) y el nombre Bluetooth del móvil (antes `puente-zwift-bluetooth`; no es de Zwift). Las otras plataformas quedan fuera hasta que el 24 y el 11 estén implementados | 6, 24 | Archivado (con pruebas pendientes) |
| 12 | `prototipo-mando-zwift` | 4 | F9 (prototipo aislado, sin integrar) | 6 | Archivado (con pruebas pendientes) |
| 13 | `mando-zwift-emulado` | 4 | F9 | 7, 12 (lleva su propio servidor GATT y anuncio BLE, solo para el mando) | Archivado (con pruebas pendientes) |
| 13b | `mando-zwift-dircon` | 4 | F9 por wifi: rodillo y mando en un solo dispositivo DirCon, con la pendiente del protocolo de marchas virtuales de Zwift; elección del transporte del mando (F8.5, F12.1) | 13 | Archivado (validado en parte: conexión, watts y giro; resto a validar) |
| 13d | `simular-kickr-bike` | 4 | F9, F12.1: ajuste para anunciar el rodillo ante Zwift por DirCon como «KICKR BIKE PRO» (nombre e identidad propios) y probar si Zwift admite así las marchas virtuales | 13b | Archivado (validado en parte: el nombre; el resto a validar) |
| 13c | `marchas-de-zwift` | 4 | F9.3 (variante): modo opcional «Marchas de Zwift»: las manetas del Square cambian la marcha de Zwift como las de una Zwift Ride, la pantalla muestra la marcha de Zwift y el Neo aplica su relación con el modelo físico | 13b | **Descartado** (28-10-2026, decidido en el change 23): la app implementa sus propias marchas virtuales y no se acopla a un protocolo de Zwift que no es público; lo que se emula del protocolo de Zwift es solo lo necesario para conectar rodillo y mando/Square por wifi |
| 13e | `completar-protocolo-dircon-zwift` | 4 | F9 por wifi: páginas 1024 y 771 con datos, serie, firmware y hardware en la información del dispositivo, tomados de la captura de BikeControl con Zwift; probados con el equipo real y sin variantes (Click v2 y el saludo `02 00` se descartaron) | 13b, 13d | Archivado |
| 14 | `teclado-hid` | 4 | F10, solo si 12 descarta F9 | 7, 12 | Descartado: el 12 validó el mando emulado por Bluetooth |
| 15 | `modelos-tabla-y-fisico` | 5 | F6.2–F6.4, velocidad y distancia en Rodaje (F11.3), potencia mínima y peso de la bici (F12.5) | 6 | Archivado (con pruebas pendientes) |
| 16 | `registro-sesiones` | 6 | F13.4: grabar la sesión (solo sucesos, con la opción de depuración activada) en archivos de texto de la app, con espacio libre mínimo, y gestionarlas desde Ajustes: listar con tamaño, abrir, compartir y borrar | 3 | Archivado |
| 17 | `experimentos-zwift-y-neo` | 6 | F14, F15 | 6, 15 | Previsto |
| 18 | `renombrar-smart-bike` | — | Ninguna funcional: el proyecto pasa de «Square Shift» a «Smart Bike» (paquete `app.smartbike`, nombre anunciado a Zwift) | — | Archivado |
| 19 | `anadir-linter-kotlin` | — | Ninguna funcional: ktlint para el Kotlin de `:app` y `:core` (`ktlintCheck` y `ktlintFormat`, `.editorconfig`), con el código existente formateado una vez | 18 | Archivado |
| 20 | `publicar-repo-publico` | — | Ninguna funcional: sanear los datos personales del árbol y publicar el repositorio con un historial nuevo de un solo commit, conservando el actual como copia privada. Tras `preparar-release` (33), también: sustituir la dirección Bluetooth real de `ConexionFtmsTest` (A9 de la auditoría; ya en el inventario del change) y, en el repositorio público, crear el entorno `release` limitado a etiquetas `v*`, volver a dar de alta los secrets de firma y `HUELLA_CERTIFICADO` y añadir `environment: release` a `release.yml` (`desarrollo.md` § 7). Publicado el 29-10-2026 como `indoor-cycling-lab/smart-bike`, con el anterior renombrado a `smart-bike-historial` (privado); el saneado incluyó también el número de serie del rodillo dentro de las tramas FE-C de las capturas y las capturas de la web | 18 | Archivado |
| 21 | `pagina-web-publica` | — | Ninguna funcional: web pública en GitHub Pages (español e inglés) con capturas de las maquetas, publicada con una GitHub Action | 20 | Archivado (publicada el 29-10-2026) |
| 22 | `identidad-bici-rodillo` | — | Ninguna funcional: nuevo icono y splash de Smart Bike (bici de perfil con la rueda trasera ámbar como rodillo, tres alternativas en la maqueta) y splash con la rueda girando cada vez más rápido | 18 | Archivado |
| 23 | `pestana-plataformas` | 4 | F8.5, F9, F12.1, F12.4, F12.9: Botones pasa a Ajustes; la nueva pestaña «Plataformas» elige plataforma (Zwift), modo de conexión (rodillo por wifi o Zwift Ride, con el mando dentro de DirCon) y nombre ante la app, con un solo modo compatible a la vez; Rodaje sustituye los iconos de Zwift y del mando por el estado de la plataforma en palabras; `Configuracion` v8 con migración; descarta el 13c | 13b, 13d | Archivado (con pruebas pendientes) |
| 24 | `generalizar-plataformas` | 4 | Interno, sin protocolo nuevo ni cambio visible: quita el nombre «Zwift» de lo genérico (dominio, sesión, paquete `zwift/` → `plataforma/` y `plataforma/zwift/`), parte la conexión en `ConexionFtmsWifi` y un mando opcional, saca el puente de `Sesion` a `PuentePlataforma`, guarda los ajustes por plataforma (`Configuracion` v9) y las identidades en un mapa por (plataforma, modo, nombre), sin migración, y sustituye `zwift-bridge` por `platform-bridge` y `zwift-ride` | 23 | Implementado y validado con Zwift (28-10-2026) |
| 25 | `rodaje-pantallas-bajas` | 4 | F11.3 y F11.8: en Rodaje vertical con menos de 760 dp de alto, modo compacto y, con los botones − y +, solo los 4 primeros datos (aviso en «Datos en pantalla»); arregla la marcha que desaparece a 360 × 640 | 23, 10d | Archivado |
| 26 | `pestana-conexion` | 4 | F8.5, F9, F12.9, F16.2: «Plataformas» pasa a «Conexión» (estado por servicio —rodillo, mando, pulso— y tres modos por transporte: «Wifi + Bluetooth», «Solo Bluetooth», «Solo wifi (modo Zwift Ride)», con compatibilidad por modo), sin elección ni guardado por plataforma; se publica lo configurado en Dispositivos (sin interruptor del mando); Dispositivos con una búsqueda por tipo; barra Rodar · Conexión · Dispositivos · Ajustes | 11, 24 | Archivado (con pruebas pendientes) |
| 27 | `renombrar-controlador` | 4 | F1.4, F9, F12.9: «Mando» pasa a «Controlador» en Dispositivos (tipo genérico, hoy el Elite Square) con un icono nuevo de manillar de bici de carretera en el Square y en su estado de la barra de Rodaje; el servicio que publica la app pasa a «Controlador Zwift» con el icono de Zwift; se retira el icono del mando de consola | 10f, 26 | Archivado (con una prueba pendiente) |
| 28 | `simplificar-diagnostico-conexiones` | 4 | F13.1, F13.3: la sección «Conexiones» de Diagnóstico pasa a «Datos de conexión», solo con los datos que no se ven en Conexión ni en Dispositivos (dirección de la app de entrenamiento, nombre anunciado, simulación de Zwift, batería del Square, último pulso, modo y notificación del ventilador); sin filas de estado, avisos ni «Buscar dispositivos»; la simulación de Zwift pasa a Simulación y el aviso de batería baja del Square a Dispositivos, que gana en la maqueta los estados Neo no encontrado y sin permiso | 26, 27 | Archivado |
| 29 | `teclas-con-simbolos-dibujados` | 4 | F1.4, F13.1, F7.2: las teclas del Square dibujan △ ○ □ ✕ y las cuatro flechas como iconos vectoriales del mismo tamaño óptico, en vez de texto en fuentes de reserva (flechas desalineadas y símbolos de distinta proporción); letras y nombres siguen como texto | 10f | Archivado |
| 30 | `quitar-barra-estado-rodaje` | 4 | F11.10 (retirada) y F1.4: Rodaje deja de ocultar las barras del sistema y se elimina la réplica de la hora, el wifi y la batería (`DeviceStatus`, `AdaptadorEstadoMovil`, `colocarFranja`); en vertical las conexiones suben a la fila de la plataforma y Ajustes baja a la fila del modo; los iconos de la barra de conexiones se agrupan a 4 dp | 10h, 10f | Archivado |
| 31 | `nombre-bluetooth-propio` | 4 | F8.5, F9.2, F12.9: casilla opcional «Usar el nombre también por Bluetooth» en Conexión que renombra el Bluetooth del móvil con el nombre ante la app mientras la app publica por Bluetooth («Solo Bluetooth» o el controlador en «Wifi + Bluetooth») y recupera el original al terminar, también tras un cierre inesperado | 26, 13d | Archivado |
| 32 | `endurecer-dircon-ble` | 4 | NF5, NF1, F8.5, F9: endurecer el puente según la auditoría de producción: DirCon solo en la wifi, sustitución de cliente con condiciones (misma IP o 15 s de silencio), detección de la plataforma que deja de responder, lector sin coste cuadrático y límite de ritmo; servidor GATT en el hilo de control, notificaciones por dispositivo suscrito y escrituras no admitidas con respuesta; cancelar un intento de conexión sin dejar dos en la cola de Nordic (con `cancelPendingConnection()`, no con `close()`, que deja el gestor inservible); dirección inválida y permisos sin cerrar la app; borrar las variantes del prototipo. Implementado y probado en el OnePlus 8 con Zwift en PC el 29-10-2026 (queda sin probar 10 min seguidos y la escucha con datos móviles) | 26, 31 | Archivado |
| 33 | `preparar-release` | 4 | F13.4, NF5: preparar la distribución según la auditoría de producción: release con R8 y `shrinkResources` (sin ofuscar), firma con clave propia leída del entorno, versión desde la etiqueta `vX.Y.Z` y workflow que publica la Release de GitHub con el APK firmado, su SHA-256 y la huella del certificado; copia de seguridad de Android solo con la configuración; se conservan las 20 sesiones grabadas más recientes. Mientras el repositorio sea privado (plan gratuito, sin entornos), los secrets de firma son del repositorio; `publicar-repo-publico` debe crear en el público el entorno `release` limitado a etiquetas `v*`, volver a dar de alta los secrets y `HUELLA_CERTIFICADO`, y añadir `environment: release` al workflow (`desarrollo.md` § 7). Implementado y probado en el OnePlus 8 el 29-10-2026: release con el Neo, el Square y Zwift por wifi y Bluetooth, copia y restauración con `bmgr`, y primera Release `v0.1.0` instalada como actualización conservando la configuración | 32 | Archivado |
| 34 | `idioma-es-en` | 4 | NF7, F12.8: interfaz en español e inglés; ajuste «Idioma · Language» en Ajustes y, en el primer arranque, español si el móvil está en español e inglés en cualquier otro caso, con elección posterior del usuario; formatos de números y fechas según el idioma | — | Archivado (con pruebas pendientes) |
| 35 | `modo-tactil-rodaje` | 4 | F7.3, F11.5, F11.8, F12.7: el indicador de modo de Rodaje se toca y alterna Libre o SIM ⇄ ERG manual con la misma acción que el Square, con un ajuste propio, «Cambiar de modo tocando» (desactivado por defecto, independiente de los botones − y +); sin confirmación, porque es reversible. Permite rodar en ERG manual sin Square. Es previo a `guia-inicio` | 9, 10, 39 | Archivado |
| 36 | `guia-inicio` | 4 | F1.4, F11.12: guía de inicio en Rodaje, una hoja inferior de tres páginas (qué es Smart Bike, pasos para empezar —empareja los dispositivos, configura la conexión y ajusta la app— y cómo conectar la app de ciclismo indoor según el modo de conexión) que se abre sola en el primer uso, se minimiza a una tira del pie, se cierra con una equis o «No volver a mostrar» (recordado en `estado.json`), desaparece sola con los dispositivos emparejados y una app conectada y se reabre desde Ajustes; no aparece a quien actualiza con dispositivos configurados | 35, 34 | Archivado |
| 35 | `confirmar-salida-rodaje` | 4 | F11: en Rodaje, Atrás ya no termina la sesión a la primera; pide pulsarlo otra vez en 3 s con un aviso flotante («Para cerrar la aplicación, pulsa Atrás otra vez») y muestra «Cerrando…» al salir; componente `ExitNotice`; «Terminar» de la notificación no cambia | 10, 34 | Archivado |
| 37 | `ordenar-grupos-botones` | 4 | F4, F12: en Botones, «Acciones de la app» pasa a ir antes que «Funciones de Zwift», para que lo que más se reconfigura quede en la primera pantalla; solo cambia el orden (maqueta, app y captura de la web) | 10 | Archivado |
| 38 | `radio-elegido-dorado` | 4 | F12, F9: en las listas de opciones excluyentes (Conexión, Ajustes) el radio de la opción elegida dibuja el aro y el punto en el acento dorado, igual que el anillo de la fila, en lugar de aro blanco y punto dorado; solo cambia el color (maqueta y app) | 10 | Archivado |
| 39 | `modo-libre-y-pendiente` | 4 | F6, F11.3, F11.5, F11.8, F12.7: sin app de entrenamiento enviando pendiente, el modo de Rodaje se llama «LIBRE» (pendiente 0 %, solo se cambia de marcha) y con ella sigue «SIM»; nuevo dato opcional «Pendiente» (la de la simulación, sin la base ni lo enviado al Neo); los botones − y + se muestran siempre con el Square sin conectar y, con él conectado, según la casilla. Va después de `guia-inicio` (que ajusta en sus textos) y antes de `modo-tactil-rodaje`, que toma de él el nombre del modo y la regla de los botones | 9, 10, 36 | Archivado |
| 40 | `guia-modos-de-uso` | 4 | F11.12: la guía de inicio gana una página nueva, la tercera, «Cómo puedes usar la app», que explica los modos de uso con los nombres del indicador (LIBRE, SIM y ERG, manual o de la app) y pasa de tres a cuatro páginas; sin tutorial de botones | 36, 39 | Archivado |
| 41 | `capturas-web-en-ingles` | — | Ninguna funcional: la web pública (`site/en/`) enseña la app en inglés. Las maquetas se pueden ver en inglés con `idioma=en` en el hash (diccionario generado de `strings.xml` más uno manual, sin tocar su HTML), `capturas.py` fotografía cada captura en los dos idiomas (48 imágenes: idioma × tema) y falla si queda texto visible sin traducir, y `generar.py` enlaza la de cada idioma. `maquetas/en.json` admite contextos para un mismo texto con dos sentidos («Pendiente»). Solo los 12 estados fotografiados; las maquetas siguen validándose en español. Conviene que entre antes de publicar la web | 21 | Archivado |

## Notas por change

Qué construye cada uno (componentes de `arquitectura.md` § 4.3) y cómo se prueba con el equipo real. Los detalles se fijan en la propuesta de cada change.

### 1. `base-sistema-diseno-y-maquetas`

Tokens, componentes `sq-*`, guía de estilo y maquetas sin conexión, con comprobación de contraste. Validado por el usuario y archivado. Sus specs están en `openspec/specs/` (`design-tokens`, `ui-components`, `style-guide` y `screen-mockups`).

### 2. `tema-compose-desde-tokens`

- Esqueleto del proyecto Android (Gradle Wrapper, catálogo de versiones, `:core` vacío y `:app` con el `applicationId` `jmlopezdona.squareshift` y `.debug` en depuración), adelantado del 3 porque el tema necesita un módulo donde compilarse.
- `generar.py` emite `Tokens.kt`, las fuentes TTF de `res/font` y el color de la ventana; `SqTema` con `CompositionLocal`; `TokensSincronizadosTest` detecta si `Tokens.kt` está desactualizado (`arquitectura.md` § 9.1).
- Muestra del tema solo en depuración, validada por el usuario en el OnePlus 8 frente a la guía de estilo.
- Actualiza `AGENTS.md` para incluir los derivados de la app entre los ficheros generados.
- Es el único change sin porción vertical con el equipo: va primero porque toda la UI depende de él.
- Validado por el usuario y archivado. Su spec está en `openspec/specs/app-theme`.

### 3. `diagnostico-square-neo`

- Parte del esqueleto del change 2: añade `AppGraph`, permisos, navegación y los iconos de `icons.svg` como *vector drawables*, y sustituye el arranque provisional de `MainActivity` por Diagnóstico.
- `SquareClient`, `NeoClient` (Nordic), `SquareDecoder`, `FecCodec`, `RegistroSesion` y la pantalla de Diagnóstico mínima según `maquetas/diagnostico.html`.
- Valida con el equipo real lo marcado **a validar** en `arquitectura.md` § 7.1 y § 7.2: la característica `0x43` con pulsaciones rápidas, qué palanca es cada bit, la frecuencia de notificaciones y el checksum de la página 0x33.
- Deja grabadas trazas del Square y del Neo para los tests de los changes siguientes.
- Resultado de la validación: los botones se leen de `0x45` (contadores; `0x43` perdía la mitad de las pulsaciones rápidas), `EventosBoton` y F2.2 se adelantan del change 4, la palanca 1 es la grande, el checksum de la 0x33 es XOR y el Neo se anuncia como «Tacx Neo …». Queda abierto que el Square y el Neo imponen un intervalo de conexión de 187,5 ms (NF1).
- Archivado. Sus specs están en `openspec/specs/` (`device-connection`, `square-buttons`, `trainer-manual-control` y `diagnostics`, más los cambios en `app-theme`, `screen-mockups` y `ui-components`). Las pruebas de conexión perdida y de combinaciones pasan a los changes 5 y 7, y la de ERG a dos cadencias al 9.
- El despliegue en el OnePlus 8 con la skill `desplegar-movil` ya funciona desde el change 2; aquí se usa para las sesiones de validación guiada (`desarrollo.md` § 3).

### 3b. `trafico-bajo-demanda`

- Propuesto por el usuario (26-10-2026): Diagnóstico iba a tirones con el tráfico en directo. Antes del change, el commit `7eadf56` aligeró el volcado: lista perezosa, filas formadas fuera del hilo principal y hexadecimal sin `String.format`.
- El usuario eligió ocultar el tráfico al abrir en lugar de arrancar en pausa. La casilla «Tráfico en directo» reutiliza la fila con casilla de Ajustes; en la maqueta, la oculta una regla de `revision.css`.
- Resultado en el OnePlus 8 (compilación de depuración, solo el Neo): 10,8 % de fotogramas con tirón con el tráfico oculto y 10,7 % con él en directo. Tras `7eadf56`, el volcado ya no es lo que frena la pantalla; queda sin medir una compilación de publicación y el coste de recomponer toda la pantalla con cada estado de la sesión.
- Archivado. Cambia las specs `diagnostics` y `screen-mockups`.

### 4. `marchas-sin-zwift`

- Gesto `Press` (sobre `EventosBoton`, adelantado al change 3), `Transmision` lineal, `Cambio` con topes, modelo simple, `ControlRodillo` en `SimLibre`, `Sesion`, `RideService` y la pantalla de rodaje básica.
- Prueba: rodar con el Neo y el Square cambiando marchas; medir la latencia de la palanca (NF1) en Diagnóstico.
- Debe resolver o acotar el intervalo de conexión de 187,5 ms que imponen el Square y el Neo (`arquitectura.md` § 6.3), que con lo validado en el 3 deja la latencia por encima de NF1.
- Hecho y validado con el equipo real (25-10-2026):
  - Saltos con combinaciones fijas del anexo A.2 (Campagnolo + palanca) y □ a la referencia, sin la asignación configurable.
  - Valores por defecto fijos, sin pantalla de Ajustes ni de Marchas.
  - Rodaje al abrir la app y Diagnóstico desde los chips.
  - Latencia: escritura sin respuesta en el Neo y prioridad alta sostenida solo en el Square; de la notificación a la entrega, mediana de 62 ms (antes, 414 ms hasta la confirmación). NF1 aceptado con la percepción del usuario; los intervalos del Square y del Neo quedan como límite conocido (`arquitectura.md` § 6.3).
- Arreglado de paso en el generador del tema (change 2): las cifras grandes no respetaban un interlineado menor que su alto natural en Compose.
- Archivado. Sus specs están en `openspec/specs/` (`virtual-gears`, `trainer-resistance`, `ride-screen` y `ride-session`, más los cambios en `device-connection`, `diagnostics`, `trainer-manual-control` y `screen-mockups`).
- Pasa al change 5 la prueba de apagar el Square a mitad de sesión sin perder la marcha, y queda para él medir el efecto de la prioridad sostenida en la batería del Square en sesiones largas. El 1 % por marcha y la 12 como neutra son provisionales hasta el change 15 (decisión abierta 8 de `funcional.md`).

### 4b. `rodaje-vertical`

- Adelantado del change 10 porque el soporte del usuario pone el móvil en vertical: Rodaje sigue la orientación del móvil y tiene disposición vertical con lo que hace la app tras el 4 (lineal de 24 marchas, potencia y cadencia, sin Zwift).
- Archivado. Sus cambios están en las specs `ride-screen` (orientación del móvil y disposición en vertical) y `screen-mockups` (estados lineales sin Zwift en vertical) de `openspec/specs/`.
- Validado en el soporte en vertical con el Neo y el Square: se lee todo desde la bici, con topes y reconexión desde Diagnóstico. El hueco de la etiqueta de tope queda siempre reservado (`sq-limit is-reserva`), para que no mueva nada; las disposiciones con plato y cassette del change 8 tienen que hacer lo mismo.

### 5. `conexion-automatica-en-segundo-plano`

- Dispositivos configurados en `estado.json`, conexión al abrir, máquina de estados de reconexión, estado de las conexiones en pantalla y notificación del servicio.
- Prueba: sesión de 2 h con la pantalla apagada (NF4); apagar y encender el Square y el Neo a mitad de sesión sin perder la marcha (la del Square quedó pendiente en el 4); batería del Square con la prioridad alta sostenida.
- Incluye la prueba que quedó pendiente en el 3: apagar el Square con un botón mantenido y comprobar que se libera y que el Neo sigue conectado.
- Archivado. Validado con el Neo y el Square: conexión al abrir sin tocar nada y reconexión sola en menos de 10 s desde que el dispositivo se despierta, con la pantalla encendida y apagada, sin perder la marcha. Las direcciones no cambian al reiniciarlos. La prueba de 2 h con la pantalla apagada (NF4) y la batería del Square con la prioridad alta sostenida pasaron al 6.

### 6. `puente-zwift-wifi`

- `ServidorGatt`, `TransporteDircon` (antes `TransporteZwift`), `DirconCodec`, `DirconServer`, `FtmsCodec`, `PuenteFtms` (antes `ZwiftBridge`), modos `SimZwift` y `ErgZwift`, y lo que envía Zwift en Diagnóstico (F13.3).
- Empieza validando DirCon con Zwift en PC (`arquitectura.md` § 7.3): anuncio mDNS, cabecera y mensajes. Si DirCon no funciona, se para y se replantea con el usuario si se adelanta el 11.
- Prueba: emparejar en Zwift para PC (decisión 7 de `funcional.md`) el rodillo, la potencia y la cadencia por wifi; rodar una ruta con subidas y un entrenamiento en ERG. Decide cuánto se mantiene el último terreno tras una desconexión (`arquitectura.md` § 15).
- Incluye la prueba de 2 h que quedó pendiente en el 5 (NF4, spec `ride-session`, «Sesión larga con la pantalla apagada»): pantalla apagada, sin cargador y con la optimización de batería por defecto; apagar y encender el Square a la hora; revisar en el registro que no hay cortes del servicio, que la reconexión es sola y la batería del Square (lecturas periódicas, cada 10 min) al principio y al final. Si el sistema corta el servicio, se decide con el usuario la exención de batería (`arquitectura.md` § 15, 3), con maqueta, antes de implementarla.
- Hecho y validado con Zwift en PC (26-10-2026):
  - DirCon funcionó a la primera: «Square Shift» aparece por wifi como potencia, cadencia y rodillo controlable, y se empareja en las tres.
  - En una ruta con subidas, la resistencia sigue al terreno y a las marchas; latencia de la palanca de 30 ms; potencia y cadencia en Zwift sin cortes a 4 Hz (decisión 2 de `arquitectura.md` § 15, resuelta).
  - El Square emparejado a la vez en Zwift gira con X y Círculo (decisión 1 de `funcional.md`: funciona).
  - Chip de Zwift en Rodaje, modo «ERG APP» con la potencia objetivo, fila de Zwift y simulación con lo que pide Zwift en Diagnóstico. Sistema de diseño: token `erg-soft`, fila de chips de dos líneas en vertical e icono `landscape` para Zwift.
- **Archivado con pruebas pendientes**, por decisión del usuario, para retomarlas después sin trasladarlas a otro change (tareas sin marcar en `openspec/changes/archive/2026-10-26-puente-zwift-wifi/tasks.md`):
  - Entrenamiento ERG de Zwift (F8.3): potencia mantenida a dos cadencias, palancas ±10 W, □ y vuelta a la marcha; y la captura de Rodaje en ERG.
  - Desconexiones: cerrar Zwift en una subida, cortar el wifi del móvil 10 s y 1 min (los 30 s de espera, decisión 1 de `arquitectura.md` § 15).
  - Sesión de 2 h con la pantalla apagada rodando en Zwift (NF4) y, con ella, la exención de batería (§ 15, 3) y las lecturas de la batería del Square para el umbral del change 10.
  - Opcional: Zwift en Apple TV (F8.4).

### 6b. `comprobar-anuncio-zwift`

- Nace de lo observado el 27-10-2026 en el OnePlus 8: Android daba el anuncio mDNS por registrado, pero no respondía en la red, y Zwift no veía el rodillo; apagar y encender el wifi lo arregló. Al usuario le pasaba también con qdomyos-zwift.
- La app comprueba su propio anuncio con una consulta mDNS mientras espera a Zwift y, si no responde tras volver a anunciarse, muestra Zwift «No visible» con qué hacer. Pasa por maqueta (Dispositivos y Rodaje).
- Prueba: la sonda en el caso sano en el OnePlus 8, sin falsos avisos en sesiones normales con Zwift en PC y el camino del aviso con un nombre falso en la build de depuración. El fallo real no se puede provocar: se confirma cuando se repita.
- Hecho y validado en el OnePlus 8 (27-10-2026): la sonda multicast del propio móvil recibe respuesta en 2–5 ms; con el nombre falso, «No visible» a los 7 s (tras volver a anunciarse una vez), con Rodaje y Dispositivos como en la maqueta, y vuelta a «Esperando» en la siguiente comprobación al quitarlo.
- **Archivado con pruebas pendientes**, por decisión del usuario (tareas sin marcar en `openspec/changes/archive/2026-10-27-comprobar-anuncio-zwift/tasks.md`):
  - Tres sesiones normales con Zwift en PC sin ningún «No visible» y sin comprobaciones con Zwift conectado.
  - Confirmar la detección con el fallo real cuando se repita (desde el Mac con `dns-sd`) y si alguna vez basta con volver a anunciarse.

### 7. `gestos-y-asignacion`

- Reconocedor de gestos completo, `Asignacion` configurable con la asignación por defecto del anexo A de `funcional.md`, captura de botones, vibración por gesto y pantallas de botones y asignación.
- Prueba: cada gesto del anexo A con el Square real; las palancas siguen respondiendo sin espera.
- Graba la traza de combinaciones (Campagnolo + palanca, freno + palanca) que quedó pendiente en el 3.
- La combinación que apaga el Square (los dos Campagnolo con las palancas I1 y D1, pulsados a la vez) no debe cambiar la marcha. Hoy se reconoce como «−1» y «+3», y apagar el Square sube dos marchas (visto en el 5).
- Hecho: traza `square-combinaciones.txt`; apagar el Square deshace los cambios de la ráfaga (los cuatro botones llegan pulsados ~4,6 s antes del corte). La larga y el retardo de repetición quedan en 800 ms por las duraciones medidas. Botones se abre desde Diagnóstico hasta el 10. Pruebas pendientes (tareas 8.1–8.4, archivado sin ellas por decisión del usuario): latencia de la palanca con el Neo (NF1); invertir palancas y restaurar en el móvil; vibración por gesto con la pantalla apagada; apagar el Square con el Neo conectado (sigue con la resistencia de antes); en Zwift, palancas mantenidas con el Square emparejado y repetición en ERG (+10 W por repetición).

### 7b. `proteccion-bajadas`

- Adelanta F6.5 del change 15. El motor del Neo empuja el volante con pendiente negativa, y con el modelo simple las marchas ligeras la dan incluso en llano.
- Ajuste «Bajadas» con tres modos («Sin protección», «La marcha no crea bajadas», por defecto, y «Nunca empujar»), aplicado en `ControlRodillo` a todos los modelos, con y sin Zwift.
- Pantalla Ajustes mínima, solo con la sección Rodillo, abierta desde Diagnóstico como Botones; el change 10 la completa.
- Se implementa después de archivar el 7, que toca los mismos ficheros.
- Prueba: medir a partir de qué pendiente negativa empuja el Neo y comprobar los tres modos con y sin Zwift.
- Hecho y validado sin Zwift (26-10-2026):
  - El motor del Neo empieza a empujar a −1,5 % (a −1,0 % el volante aún se frena solo).
  - Marcha 1 con el modo por defecto: sin empuje, y Diagnóstico enseña «Limitada por Bajadas»; al cambiar a «Sin protección» con la sesión en marcha, vuelve el empuje sin cambiar la marcha; el modo se conserva al reabrir la app.
  - De paso, los valores de texto de Simulación (modo, modelo, bajadas) pasan a la letra de la maqueta (`sq-metric-val text`) en lugar de la de las cifras.
- **Archivado con prueba pendiente**, por decisión del usuario: la prueba con Zwift (tarea 6.3 sin marcar en `openspec/changes/archive/2026-10-26-proteccion-bajadas/tasks.md`): en una bajada, con «La marcha no crea bajadas», bajar a la 1 no aumenta el empuje; con «Nunca empujar», no empuja y las marchas duras siguen endureciendo. Sus specs están en `openspec/specs/` (`settings-screen`, nueva, y cambios en `trainer-resistance`, `zwift-bridge`, `diagnostics` y `screen-mockups`).
- Queda para un change posterior el suelo configurable en «Nunca empujar» (por ejemplo −1,0 %, justo antes de que empuje), si se echa en falta diferencia entre marchas en las bajadas. El change 10 lleva Ajustes a la barra inferior con el resto de secciones.

### 8. `transmisiones-de-marca`

- Reducido en la exploración a la transmisión del usuario: lineal y Shimano 105 2×12 (52/36 · 11–36), con los modos «Automático» (por defecto, como el Di2 en Synchro Shift, con histéresis) y «Manual» (como sus mandos).
- Pendiente proporcional a la relación, normalizada al recorrido de la lineal de 24 (decisión 3 de `funcional.md`, ya tomada), y una asignación de botones por transmisión con las acciones de plato.
- Pantalla de Marchas desde Ajustes; Rodaje con plato × piñón, `ChainringIndicator` y `CassetteView`; Diagnóstico con la relación. `configuracion.json` pasa a la versión 2, con migración.
- Validado con el equipo real (26-10-2026): la secuencia del automático (15 marchas en cada sentido), el plato a mano, los topes, cambiar de transmisión rodando, la persistencia y la correspondencia de las palancas con el STI del usuario. Archivado con la prueba en Zwift pendiente, por decisión del usuario (tarea 8.4 sin marcar en `openspec/changes/archive/2026-10-26-transmisiones-de-marca/tasks.md`): una ruta con la Shimano, viendo en Diagnóstico la pendiente de Zwift más el ajuste por relación.
- Quedan para el 8b: SRAM (con «las dos palancas a la vez», que choca con NF1 si hay que esperar a la segunda), Campagnolo y «A medida» con su pantalla de edición. Para el 10: número de marchas de la lineal (F5.5); para el 15: dificultad, elegir el modelo y «Pendiente por marcha» en Marchas.

### 8b. `transmisiones-otras-marcas`

- Desarrollos de fábrica de SRAM y Campagnolo (decisión 5 de `funcional.md`, confirmarlos antes) y transmisión «A medida» con su pantalla de edición, sobre `Transmision.Platos` y `Desarrollos` del 8.
- Modo de SRAM «las dos palancas a la vez cambian de plato»: decidir cómo detectar la pulsación doble sin pasar de 100 ms (NF1), y si un AXS real cambia al pulsar o al soltar.

### 9. `erg-manual`

- Modo `ErgManual`, ajuste ±W y cambio SIM ⇄ ERG desde el Square.
- Hecho: △ larga alterna en todas las transmisiones; en ERG manual las palancas ajustan ±10 W por marcha como en ERG de Zwift y □ vuelve a la potencia de entrada; «+10 W» y «−10 W» son asignables sin botón por defecto (la cruceta del anexo A.2 se descartó: con el Square emparejado en Zwift, ↓ es «Cambio de sentido»). La potencia de entrada es la última usada (150 W la primera vez). En una ruta de Zwift manda el ERG manual; un entrenamiento de Zwift lo termina. Las configuraciones guardadas reciben △ larga al leerse (versión 3), solo si está libre.
- Al validar la maqueta, el usuario pidió centrar en horizontal la marcha, la potencia objetivo y los datos de Rodaje, en las dos orientaciones y en todos los modos.
- Probado en el OnePlus 8 con el Neo y el Square (26-10-2026): entrar y salir con △ larga, palancas, □, topes y potencia recordada. La prueba de dos cadencias se hizo a 150 W en lugar de 200 W: el Neo mantiene la potencia según el usuario, sin medir el ±5 % porque el registro aún no se exporta (change 16).
- Prueba pendiente (tarea 6.4, archivado sin ella por decisión del usuario): en Zwift, ERG manual sobre una ruta y vuelta con el terreno vigente, un entrenamiento que lo interrumpe y △ larga durante el entrenamiento.
- Repite la prueba de ERG que quedó a medias en el 3: potencia mantenida a ~70 y ~95 rpm, 30 s cada una (la página 0x31 ya está validada; faltaba ver que el Neo mantiene la potencia al cambiar de cadencia). Hecha a 150 W.

### 10. `rodaje-completo-y-ajustes`

- Datos configurables, avisos de acción, botones − y + opcionales, pantalla de rodaje final (horizontal y vertical; la vertical básica se adelantó al 4b), ajustes completos y exportar e importar la configuración.
- Barra inferior: Botones (hoy abierta desde Diagnóstico, change 7) pasa a ser una sección, sin botón de volver, como en `maquetas/botones.html#estado=lista`. Lo mismo Ajustes (hoy solo Rodillo con «Bajadas», abierta desde Diagnóstico, change 7b), que se completa como en `maquetas/ajustes.html#estado=inicio`. Pantalla anterior o siguiente con Campagnolo izquierdo + cruceta ←/→ (anexo A.2). La vibración (F12.7) se puede desactivar.
- Necesita antes las decisiones 4 y 6 de `funcional.md` (velocidad y valores por defecto de «Al rodar»).
- Al proponerlo (26-10-2026), el usuario decidió:
  - Partirlo: el 10 lleva la barra inferior, Dispositivos, Ajustes, «Al rodar» y la pantalla anterior o siguiente; exportar e importar, la batería del Square y el aviso de giro pasan al 10b.
  - Decisión 4: velocidad y distancia fuera de los datos hasta el modelo físico (15).
  - Ajustes solo con lo que tiene función: el peso de la bici y la potencia mínima pasan al 15; el paso ERG, el tema, «Mantener la pantalla encendida» y el aviso de cadencia baja salen de la maqueta.
- Al validar la maqueta (26-10-2026), el usuario aprobó que los chips de Rodaje abran Dispositivos y que Diagnóstico pase a Ajustes, la vertical compactada con los botones − y + y el estado ERG sin intervalo ni aviso de cadencia baja.
- Hecho:
  - Barra inferior (Rodar, Dispositivos, Botones, Ajustes) sin transiciones, Atrás de una pestaña a Rodaje y pantalla anterior o siguiente con Campag. I + cruceta ← / → (acciones asignables nuevas).
  - Dispositivos con «Buscar dispositivos» y «Olvidar»; Ajustes completa (Marchas, Diagnóstico, «Al rodar», peso del ciclista, «Bajadas» y «Pendiente sin Zwift»); Datos en pantalla con lista ordenable accesible.
  - Rodaje con los datos elegidos (potencia de 3 s, tiempo pedaleando, W/kg, media y máxima), botones − y + y aviso de acción desactivable; «Vibrar» desactivable.
  - `configuracion.json` versión 4 con migración; `DatosSesion` en `:core`. Decisiones abiertas 4 y 6 de `funcional.md`, resueltas.
  - Visto en el OnePlus 8: con la letra del sistema grande, las cifras de seis datos en vertical se cortaban; `MetricGrid` baja ahora el tamaño hasta que caben. También se corrigió que la pendiente sin Zwift guardada no se aplicaba al abrir.
- Validado por el usuario con el equipo real (26-10-2026): capturas frente a la maqueta, rodar sin Zwift con datos, botones y ajustes, olvidar y volver a buscar, recorrer las pestañas con el Square y conservar los ajustes al reabrir.
- **Archivado con prueba pendiente**, por decisión del usuario (tarea 6.4 sin marcar en `openspec/changes/archive/2026-10-26-rodaje-completo-y-ajustes/tasks.md`): en Zwift, qué hace Zwift con Campag. I + cruceta ← / → con el Square emparejado, y cambiar «Pendiente sin Zwift» en una ruta sin que cambie la resistencia.
- Sus specs están en `openspec/specs/` (`app-navigation`, nueva, y cambios en `ride-screen`, `settings-screen`, `device-connection`, `button-mapping`, `button-gestures`, `virtual-gears`, `trainer-resistance`, `diagnostics` y `screen-mockups`).

### 10b. `exportar-bateria-y-giro`

- Exportar e importar la configuración (F12.6), la batería del Square y el aviso de giro, separados del 10 al proponerlo. Ya no tiene dependencias pendientes: el 10 está archivado.
- Exportar e importar: el documento es `configuracion.json` versión 4 (`arquitectura.md` § 10), que incluye el tema desde el 10e; al importar se valida entero antes de aplicarlo. La entrada va en Ajustes, que pasa por maqueta.
- Aviso de giro en Rodaje: mientras se mantiene X o Círculo (girar en Zwift), indicarlo en pantalla. Propuesto por el usuario al validar el change 6, donde el Square sigue emparejado en Zwift para girar. Pasa por maqueta. Con el mando emulado (13), el giro llega por la app como `Zwift(GIRAR_IZQUIERDA)` y `Zwift(GIRAR_DERECHA)` en cualquier botón, así que el aviso puede salir de esas funciones pulsadas, no de X y Círculo.
- Batería del Square, que desde el 5 solo va al registro: nivel en la fila del Square de Diagnóstico y aviso «Batería del Square baja» cuando baje de un umbral, en lugar de mostrar el porcentaje en Rodaje. El valor de `0x2A19` es grueso: da 100 % con una pila de dos años y no vale justo después de encender el Square (`arquitectura.md` § 7.1). Se ignoran las lecturas de los primeros minutos tras un encendido, y el umbral se decide con las lecturas periódicas de la prueba de 2 h del 6. Propuesta de partida para el aviso: en el chip del Square, con el estado en palabra («Square batería baja», como «Square sin conexión») y una variante de `ConnectionChip` para el aviso, que no ocupa sitio nuevo ni compite con el aviso de acción del pie. Pasa por maqueta (Diagnóstico y el chip en Rodaje).
- Maqueta validada por el usuario (27-10-2026). Cambio al validarla: la marca de batería baja pasó de un hueco con «!» que cortaba la rueda del icono a una insignia en la esquina superior izquierda, con el icono entero.
- Hecho: `Configuracion.validar`, `CopiaArchivo`, la sección «Copia de seguridad», `BateriaSquare`/`FiltroBateria`, `EstadoChip.Baja`, el nivel en Diagnóstico, `EstadoMandoZwift.giro` y `TurnIndicator`. El umbral de batería baja (20 %) es provisional hasta la prueba de 2 h del change 6. El aviso de giro no sale en ERG (la maqueta no lo llevaba) ni con el mando desactivado.
- Prueba con el equipo (27-10-2026): exportar e importar la configuración, probado por el usuario en el OnePlus 8 y dado por bueno. El aviso de giro, validado por el usuario con el equipo real: aparecen las flechas de dirección.
- **Archivado con prueba pendiente**, por decisión del usuario (tarea 5.1b sin marcar en `openspec/changes/archive/2026-10-27-exportar-bateria-y-giro/tasks.md`): anotar la validación de la batería del Square, es decir, su nivel a los 10 min en Diagnóstico y la primera lectura ignorada en el registro. Con ello se fija el umbral de batería baja (20 %, provisional).
- Sus specs están en `openspec/specs/` (`settings-backup` y `square-battery`, nuevas, y cambios en `ride-screen`, `settings-screen` y `diagnostics`).

### 10c. `chips-conexion-compactos`

- Adelantado a petición del usuario al revisar la pantalla de rodaje en vertical: los chips de Rodaje dejan el estado en palabra («conectando», «esperando», «sin conexión») y muestran solo el nombre y la forma del punto, en horizontal y en vertical. La palabra queda en la descripción accesible y en Dispositivos.
- En vertical, la fila de chips ocupa una línea en lugar de reservar dos, y la rejilla de datos gana ese alto.
- Prueba con el equipo: en el OnePlus 8 en vertical, con el Neo apagado, el Square dormido y Zwift cerrado, la fila cabe en una línea y nada se mueve al conectarlos.

### 10d. `rejilla-datos-fija`

- Adelantado a petición del usuario al revisar Rodaje en vertical: la rejilla de datos cambiaba de área, y a veces de cifra, al activar los botones − y +.
- La rejilla ocupa un área fija (40 % del alto útil en vertical, 40 % del ancho útil en horizontal) y la marcha se reduce para caber en el resto. Los botones toman primero la holgura de la rejilla sin bajar su cifra. La cifra se mide con el valor más largo de cada dato, así que no cambia mientras se rueda. Con un número impar de datos, el último va centrado.
- Prueba con el equipo: en el OnePlus 8, activar y desactivar los botones con 2, 5 y 6 datos y rodar pasando de 999 a 1000 W sin que la cifra se mueva.

### 10e. `acceso-ajustes-y-tema`

- Propuesto por el usuario (26-10-2026): un icono de Ajustes en la esquina superior derecha de Rodaje, que hoy no tiene barra inferior, y el ajuste «Tema» («Oscuro», por defecto, o «Claro») en una sección nueva «Pantalla» de Ajustes.
- Al proponerlo, el usuario decidió: el icono solo en Rodaje; dos opciones, sin seguir el tema del sistema; y el tema elegido se aplica a toda la app, Rodaje incluida.
- Pasa por maqueta: el icono en todos los estados de rodaje (con la línea de chips a 360 dp en vertical) y Rodaje revisado en claro por primera vez.
- Prueba con el equipo: en el OnePlus 8, rodando con el Neo y el Square, abrir Ajustes con el icono, cambiar a «Claro» y volver con la misma marcha; leer Rodaje en claro a 1–2 m; girar el móvil y reabrir la app en claro.
- Maqueta validada por el usuario (26-10-2026). El icono mide 32 px, como los chips, con la zona táctil de 48 por fuera, así que la línea de los chips no crece; a 360 px cabe con 55 px de holgura. De paso se corrigió la vista previa de `RideScreen`, que superponía la zona ERG desde el 10d.
- Hecho: el icono en Rodaje abre Ajustes como pestaña; sección «Pantalla» con «Tema»; `tema` en `configuracion.json` sin subir la versión; el tema se aplica en la raíz (`SqTemaElegido`) con el fondo de la ventana y las barras del sistema. Visto en el OnePlus 8 sin el equipo: el icono abre Ajustes, «Claro» se aplica al momento (barra de estado con iconos oscuros) y se conserva al reabrir la app; la pantalla de inicio del sistema sigue en oscuro un instante, como se aceptó.
- Validado por el usuario con el equipo real (26-10-2026) y archivado. Sus specs están en `openspec/specs/` (cambios en `ride-screen`, `settings-screen`, `app-theme` y `screen-mockups`).


### 10f. `barra-conexiones-iconos`

- Propuesto por el usuario (27-10-2026): los chips con nombre de Rodaje no caben si llegan más enlaces (banda de pulso, ventilador), y el chip «Zwift» no distingue el rodillo del mando. Se sustituyen por una barra de iconos, como la barra de estado de un móvil.
- Decidido al explorarlo: un icono por enlace (Neo `bolt`, Square `directions_bike`, Zwift una «Z» propia en un recuadro, mando `sports_esports`); el estado por la forma (normal, atenuado con marca girando, tachado); el rodillo y el mando por separado; solo los enlaces en uso; toda la barra abre Dispositivos. En horizontal, en la línea del modo; en vertical, encima, como hoy.
- Pasa por maqueta: la barra en todos los estados de rodaje, en los dos temas y a 360 dp, y los iconos en Dispositivos y Diagnóstico como leyenda.
- Prueba con el equipo: en el OnePlus 8, en las dos orientaciones, conectar y desconectar el Neo, el Square y Zwift en PC, el mando esperando, sin wifi y con «Mando de Zwift» desactivado.
- Archivado (27-10-2026) con la prueba con el equipo (tarea 2.6) sin hacer: queda pendiente validar en el OnePlus 8, a 1–2 m, la marca de espera a 18 dp (si no se distingue, subir el icono a 20 dp) y el texto largo junto a Ajustes en vertical.
- Los changes que añadan el pulso o el ventilador solo suman un enlace a la lista (`Enlace` y `RodajeUiState.enlaces`); sus iconos (`favorite`, `mode_fan`) ya están en el sprite.

### 10g. `orientacion-pantalla`

- Propuesto por el usuario (27-10-2026): fijar la app en vertical o en horizontal sin depender del bloqueo de rotación del sistema, que afecta a todas las apps.
- Ajuste «Orientación» en la sección «Pantalla» de Ajustes, debajo de «Tema»: «Según el móvil» (por defecto, lo de hoy), «Vertical» y «Horizontal». Se aplica a toda la app y al momento, y se guarda en `configuracion.json` sin subir la versión.
- Pasa por maqueta: la opción en `ajustes.html`, con un estado por opción, en los dos temas.
- Prueba con el equipo: en el OnePlus 8 en el soporte, fijar cada orientación rodando y girar el móvil, con el bloqueo del sistema en la contraria, y cerrar y abrir la app.
- Hecho y archivado (27-10-2026): `OrientacionApp` en `Configuracion` (sin subir la versión), `OrientacionElegida` en `MainActivity` y «Orientación» en Ajustes. El usuario validó la prueba en el OnePlus 8 en el soporte; no anotó si admite vertical boca abajo ni si se ve un instante Rodaje sin girar al abrir la app con el móvil en la orientación contraria (decisiones 2 y 4 del `design.md`): si aparece, aplicar la alternativa de la decisión 4. El test de navegación no gira la actividad (elige la orientación que ya tiene el dispositivo); que la marcha y la sesión sobreviven al giro lo cubre la prueba en el móvil.

### 10h. `barra-estado-rodaje`

- Propuesto por el usuario (27-10-2026): Rodaje oculta las barras del sistema y no se ven la hora, el wifi ni la batería del móvil. El wifi importa porque el puente con Zwift va por DirCon, y la batería, porque se rueda con la pantalla siempre encendida.
- Decidido al explorarlo: solo hora (`hh:mm`, 24 h), wifi (hay o no hay) y batería (icono con el nivel, porcentaje y rayo al cargar); no se devuelve la barra del sistema. En vertical, en la franja superior a la izquierda, junto a la cámara (a su derecha si está a la izquierda, a su izquierda si está en el centro); en horizontal, el conjunto de la hora, el wifi y la batería centrado en la línea. Sin separador: los separa la posición.
- Pasa por maqueta: el grupo en los dos tamaños y orientaciones, en los dos temas, con los estados sin wifi, batería baja, cargando y cámara en el centro.
- Prueba con el equipo: en el OnePlus 8 en el soporte, ver la hora cambiar al minuto, apagar el wifi, conectar el cargador y leer el grupo a 1–2 m sin que tape la cámara. La cámara en el centro no se puede probar con ese móvil: queda «a validar» y se cubre con tests de la colocación.
- Maqueta validada por el usuario (27-10-2026). Cambios al validarla: el icono de batería es el horizontal `battery_android_*` de Material (el del móvil), con el rayo al cargar; y el wifi se alineaba más bajo que la hora y la batería (el `svg` en línea se asentaba en la línea base).
- Ajuste del usuario al verlo en el móvil (27-10-2026): los iconos de la barra de conexiones (10f), el wifi y la batería bajan de 18 a 14 dp, todos del mismo tamaño (`--ic` en `components.css`, con la marca de espera y el tachado escalados), y se reducen los huecos entre hora, wifi, batería y porcentaje (6 y 4 dp). Se probó la batería vertical, que apenas ahorraba sitio y quedaba muy fina: se queda la horizontal, recortada a su ancho.
- Hecho: `DeviceStatus` (`sq-devstatus`), `AdaptadorEstadoMovil`, `Bateria`, `EstadoMovil` y `colocarFranja` en `:core`, y la franja vertical y la línea horizontal de `Rodaje`. Visto en el OnePlus 8 en vertical: «11:23», wifi y batería al 100 % con el rayo, a la derecha de la cámara. Tests: `:core`, unitarios y 177 de instrumentación en verde.
- Archivado (27-10-2026) con la prueba con el equipo (tarea 2.8) sin hacer: queda pendiente validar en el OnePlus 8 en el soporte, en vertical y en horizontal, que la hora cambia en el minuto exacto, que el wifi y la Z de Zwift se tachan a la vez al apagar el wifi, que el rayo sigue al cargador, que los iconos de 14 dp y el porcentaje se leen a 1–2 m, y que el conjunto queda centrado en horizontal. La cámara en el centro no se puede probar con ese móvil: queda a validar y cubierta por los tests de `colocarFranja`. Sus specs están en `openspec/specs/` (cambios en `ride-screen`, `ui-components`, `screen-mockups` y `style-guide`).

### 10j. `pulso-zwift`

- Propuesto por el usuario (27-10-2026): rueda con un pulsómetro de brazo COOSPO HW9 (Bluetooth y ANT+); su ANT+ la usa el ventilador Wahoo KICKR Headwind, y Zwift en PC no recibe el pulso.
- Decidido al explorarlo: el móvil recoge el pulso por Bluetooth estándar (`180D`/`2A37`) y lo ofrece a Zwift como una fuente más del mismo dispositivo DirCon; el ventilador sigue con su ANT+ directa y la app no lo toca. La banda es opcional y se busca aparte de «Buscar dispositivos»; sin pulso no se envía nada a Zwift (nunca un 0).
- Pasa por maqueta: el icono del pulso en la barra de Rodaje (con cinco enlaces a 360 dp), la fila «Pulsómetro» en Dispositivos y en Diagnóstico. Sin pulso en la rejilla de Rodaje.
- Prueba con el equipo: en el OnePlus 8 con la HW9, el Neo y Zwift en PC; validado el 27-10-2026: Zwift lista «Square Shift» como pulso por DirCon a la primera, junto a la potencia, la cadencia y la resistencia, y los bpm llegan; la banda se encuentra, se recuerda y se reconecta sola, y el ventilador sigue al pulso. Sin probar: 20 min rodando con pulso sin cortes (e) y cuántas conexiones Bluetooth admite la HW9 (c: no hace falta, solo el móvil se conecta a ella y Zwift recibe el pulso por wifi).
- Fuera de alcance: el pulso como dato de Rodaje, zonas y medias, el ventilador y ANT+ en el móvil.

### 10k. `ventilador-headwind`

- Propuesto por el usuario (27-10-2026): controlar el ventilador Wahoo KICKR Headwind desde Square Shift, sin la app de Wahoo, para cambiar entre velocidad manual y automática por pulso, subir y bajar la velocidad desde el Square y ajustar los umbrales de pulso de las zonas.
- Decidido al explorarlo: el móvil habla con el control Bluetooth del ventilador (protocolo no oficial, de la ingeniería inversa de la app de Wahoo que publican otros proyectos; se reimplementa, sin copiar código, y queda a validar). El modo automático lo ejecuta el ventilador con su banda por ANT+: la app solo lo configura, no hace nada con el pulso y este change no depende de `pulso-zwift`.
- Tres acciones nuevas asignables en Botones, sin botón por defecto: alternar manual ⇄ automático y + y − velocidad (pasos de 10 %). En automático la velocidad no se toca y se avisa. Los umbrales (techos de las cuatro zonas) se editan solo en Ajustes, en la sección «Ventilador».
- Pasa por maqueta: la sección de Ajustes, la fila de Dispositivos y de Diagnóstico, las acciones de Botones y el sexto icono de la barra de Rodaje.
- Prueba con el equipo: en el OnePlus 8 con el Headwind y la app de Wahoo cerrada; a validar que lo ve por su nombre, los comandos, que la configuración se escribe sin estropear el resto de campos y que el automático sigue a la banda con la app conectada.
- Fuera de alcance: emparejar o cambiar la banda del ventilador, los demás modos por sensor, ANT+ en el móvil y el ventilador como dato de Rodaje.
- Resultado de la prueba (27-10-2026, OnePlus 8, Headwind `HEADWIND 1234`): todo bien. El protocolo coincide salvo la configuración, que son 8 bytes tras el `ST` (no 9 o 18); `04 04` reanuda la última velocidad manual; el 0 % en manual lo para; los botones del Square, el automático con la HW9 y 20 min con Zwift sin incidencias. Detalle en `arquitectura.md` § 7.5.

### 10i. `identidad-app`

- Propuesto por el usuario (27-10-2026): la app no tiene icono ni splash, y la pestaña Botones comparte `sports_esports` con el mando de Zwift de la barra de conexiones.
- Decidido al explorarlo con bocetos: icono de la app, una piñonera de perfil con el piñón activo en ámbar (capa monocroma para Android 13+); splash del sistema (`androidx.core:splashscreen`) animada con el piñón saltando de barra, sin pantalla propia; icono `i-botones` (triángulo, cuadrado relleno, círculo y cruz en 2×2). El G3 en color queda para una futura bienvenida o «Acerca de», fuera de este change. Distinto del icono de la organización `indoor-cycling-lab`.
- Pasa por maqueta: `maquetas/identidad.html` (icono con máscaras y en monocromo, splash, barra inferior) y los componentes `AppIcon` y `Splash` en la guía.
- Prueba con el equipo: en el OnePlus 8 (Android 13), reinstalar para refrescar el icono, ver el launcher con y sin iconos temáticos, abrir en frío con tema oscuro y claro y ver la barra inferior.
- Sin porción vertical con el equipo: es identidad visual, como el change 2.
- Hecho y probado en el OnePlus 8 (Android 13, 27-10-2026): icono adaptativo y con iconos temáticos, splash en frío con tema oscuro y claro (sigue oscura), y la pestaña Botones con su icono distinto del mando de Zwift. Variante monocroma elegida al validar la maqueta: **B** (barras inactivas en contorno). Dibujo reducido para caber en la zona segura (barras de 17 a 48 dp de alto y 5 dp de ancho). `androidx.core:splashscreen` 1.2.0. El arranque sin animación (Android 9 a 11) no se puede probar en ese móvil: queda cubierto por el estado base del vector.
- Sin diferencias con `maquetas/identidad.html` anotadas en la prueba.

### 11. `ftms-bluetooth`

- Parte de `generalizar-plataformas` (24): el modo «Rodillo por Bluetooth (FTMS)» es una fila más de la tabla de compatibilidad de Zwift (`Enlace.BLUETOOTH`, `nombreElegible = false`, `Validacion.EN_PARTE`, sin recomendar). `ConexionFtmsWifi` pasa a `ConexionFtms` (modo y transporte), `TransporteBluetooth` pone las condiciones del periférico (Bluetooth, permisos, modo periférico) y `PuentePlataforma` deja de suponer el wifi. Segundo `GattServerHost` con el anuncio de FTMS, Cycling Power y `FC82`.
- Decidido al explorarlo: el nombre es el Bluetooth del móvil (lo único que Android deja anunciar). El mando se probó como experimento (tarea 6.6) y **funciona dentro del mismo servidor GATT**: Zwift lista el móvil como mando, hace el handshake y los botones del Square mueven Zwift, así que el modo lleva el mando dentro, siempre activo y sin interruptor (`MandoZwiftEnServidor`). Maqueta validada dos veces por el usuario (sin mando y con el mando dentro).
- Validado con el OnePlus 8, el Neo y Zwift en PC (28-10-2026): aparece como rodillo, potencia, cadencia y mando, Zwift controla el rodillo por FTMS, telemetría a ~4 Hz, palanca de 62 y 81 ms, Zwift cerrado (30 s y pendiente base), Bluetooth apagado y encendido, y cambio de modo en los dos sentidos sin tocar la marcha. La prueba destapó dos fallos ya corregidos: `Advertiser` no volvía a anunciarse tras apagar y encender el Bluetooth (error 3, también del mando aparte) y «Terminar» se colgaba con un intento de conexión del Square pendiente.
- **Limitación aceptada por el usuario:** Zwift no reconoce el dispositivo entre sesiones por Bluetooth (hay que quitar el anterior y buscarlo de nuevo); el modo por wifi sigue siendo el recomendado. Una dirección estable (Android 14, dirección pública) queda para otro change.
- Pruebas pendientes (tareas sin marcar en `openspec/changes/ftms-bluetooth/tasks.md`): una subida real con cambios de piñón y un entrenamiento ERG (6.2), Zwift en Apple TV (6.5, que no se hará: el usuario no tiene Apple TV, así que sigue sin validar) y la reconexión de Zwift tras cada cambio de modo y tras apagar y encender el Bluetooth con el mando dentro (7.5).

### 12. `prototipo-mando-zwift`

- App o pantalla aislada con solo el servicio de mando Zwift y botones en pantalla. Valida el handshake, la máscara de botones, el freno y el reconocimiento como mando (`arquitectura.md` § 7.4).
- Prueba el mando primero por DirCon y, si Zwift no lo acepta, por Bluetooth (`arquitectura.md` § 15, 4).
- Termina con una decisión: seguir con el 13 o pasar al 14. Si hace falta reutilizar `ble`, se extrae a su propio módulo aquí.
- Hecho como pantalla de prueba solo de depuración, «Prueba del mando» (`app/src/debug/`), sin maqueta, por decisión del usuario; no hizo falta extraer `ble`. `MandoZwiftCodec` queda en `:core` para el 13, y `DirconServer` admite otro nombre de anuncio.
- Resultado con Zwift en PC (26 y 27-10-2026; `arquitectura.md` § 7.4):
  - Por DirCon, Zwift hace el handshake pero no trata el servicio como mando.
  - Por Bluetooth funcionan los menús, A, B, Z (Ride On), el freno y la dirección, con las palancas analógicas a 0 al soltarlas.
  - Zwift ve dos dispositivos: el rodillo por wifi y el mando por Bluetooth.
  - Se sigue con el 13 y se descarta el 14.
- Pruebas pendientes al archivar: 10 min sin pulsar nada, cerrar Zwift con algo mantenido, Y y ↑/B en rodaje, y qué hace falta para que Zwift lo reconozca, los datos de fabricante o la respuesta a la información. Se hacen en el 13 con los botones del Square.

### 13. `mando-zwift-emulado`

- `MandoZwift` sobre `MandoZwiftCodec` (del 12), publicado como periférico BLE con el servidor GATT y el anuncio del 11 aunque el puente vaya por wifi; las acciones `Zwift(…)` de la asignación.
- Lo aprendido en el 12 (`arquitectura.md` § 7.4):
  - X y Círculo van como palancas hacia fuera y las manetas como palancas hacia dentro, siempre con las palancas sueltas a 0.
  - Las palancas del Square no se reenvían como botones de cambio.
  - El servidor GATT tiene que distinguir a Zwift de los enlaces centrales con el Neo y el Square.
- Las funciones de Zwift de Botones, hoy informativas, pasan a ser acciones asignables, y un botón reasignado deja de llegar a Zwift (con el Square emparejado en Zwift llegaba igual; la app solo avisaba). Necesita reenviar pulsar y soltar (`Hold`), que el reconocedor del change 7 no produce.
- Prueba: navegar, girar, frenar, powerup y Ride On en Zwift sin emparejar el Square.

- Hecho (27-10-2026): primero la prueba por DirCon (sección 0, decidido el Bluetooth); después maqueta validada por el usuario, `FuncionZwift` y `Accion.Zwift` asignables con las del anexo A.1 por defecto, `EstadoMandoZwift`, `Configuracion` versión 5 con «Mando de Zwift», `GattServerHost`, `Advertiser` y `MandoZwift`, el pulsar y soltar en la `Sesion` (sin `Hold` en el reconocedor) y las pantallas. Se retiró la pantalla de prueba del 12.
- Validado por el usuario con el equipo real (OnePlus 8, Zwift en PC, Square desemparejado en Zwift): emparejar rodillo y mando, todas las funciones desde el Square, las palancas sin aviso de marchas virtuales, un botón reasignado que deja de llegar a Zwift, apagar y encender el Square rodando y desactivar y activar el ajuste rodando. Archivado.
- Pruebas pendientes al archivar: 10 min sin pulsar nada y luego A, cerrar Zwift con ○ mantenido y apagar el Square con un freno mantenido (cubiertas por tests).
- **Reutilización con el 13b.** La lógica del mando no depende del Bluetooth: `MandoZwift` monta el servicio sobre un `ServidorGatt` y envía por un `TransporteZwift`; `GattServerHost` y `Advertiser` son solo el transporte. Las acciones `Zwift(…)`, `EstadoMandoZwift`, el pulsar y soltar de la `Sesion`, el ajuste y las pantallas no saben por dónde va el mando. Así el 13b cambia el transporte sin tocar nada de eso.

### 13b. `mando-zwift-dircon`

Alternativa complementaria al 13, propuesta por el usuario (27-10-2026): **todo por wifi**, el rodillo y el mando en el mismo dispositivo DirCon de la sesión, sin servidor GATT ni anuncio Bluetooth. Parte de la prueba de la sección 0 del 13 (`arquitectura.md` § 7.4):

- Zwift reconoce el mando por DirCon con el servicio `FC82` y una `DevInfoPage` completa, pero solo admite un dispositivo DirCon por IP: el mando tiene que ir dentro del rodillo.
- Dentro del rodillo, Zwift activa sus marchas virtuales («GEAR» en el HUD) y envía `SET` (`0x3F`) con `TrainerSimulationParam`: pendiente, CW, Crr, masas y la relación de su marcha virtual.
- Idea: tomar de ahí la pendiente (y los parámetros de simulación) para el modelo de marchas de la app, con una implementación propia del protocolo a partir de su descripción, e ignorar la marcha de Zwift. Las marchas siguen siendo de la app (F9.3).

**Reutilización (lo que no se reescribe):**

- Del 13, todo lo que no es transporte: `MandoZwift` (servicio `FC82`, handshake, páginas de información, botones, *keepalive*), `MandoZwiftCodec`, `EstadoMandoZwift`, las acciones `Zwift(…)`, el pulsar y soltar de la `Sesion`, el ajuste «Mando de Zwift» y las pantallas. El 13b solo monta el servicio del mando en el `ServidorGatt` del `ZwiftBridge` en lugar del de `GattServerHost`.
- Del 6: `DirconServer`, `ServidorGatt` y `ZwiftBridge` tal cual; el puente gana el servicio del mando y un receptor de `SET` que entrega la simulación al mismo camino que hoy siguen los comandos FTMS (`ControlRodillo`), para que el modelo de marchas no sepa de dónde viene la pendiente.
- Codificación nueva solo donde haga falta: el `Set`/`SET_RESPONSE` y `TrainerSimulationParam` en `:core` (`protocolo/`), puros y con vectores sacados de la prueba del 13.

**Etapas:**

1. Prueba, como la sección 0 del 13, con el Neo de verdad: responder `SET_RESPONSE`, anotar la pendiente durante unos minutos de ruta y compararla con la de Zwift, ver si Zwift sigue escribiendo en FTMS, qué hace el HUD con su marcha fija y cómo van los entrenamientos ERG.
2. Solo si sale bien, diseño definitivo: elección del transporte del mando («Bluetooth» o «todo por wifi») en Ajustes, que encaja con F8.5 y F12.1 del change 11, o sustitución del Bluetooth.

**Riesgos:** el protocolo es privado de Zwift (público solo a través del protobuf de BikeControl) y puede cambiar; el HUD muestra una marcha que no es la de la app; Zwift podría tratar distinto un rodillo con marchas virtuales en ERG o en carreras. Sin código de BikeControl ni de qdomyos (NF6).

### 13c. `marchas-de-zwift`

**Descartado (28-10-2026, decidido en el change 23 `pestana-plataformas`).** La app implementa sus propias marchas virtuales y no se acopla a un protocolo de Zwift que no es público; lo que se emula del protocolo de Zwift es solo lo necesario para conectar rodillo y mando/Square por wifi. Lo que sigue queda como referencia de lo que se estudió.

Anotado a petición del usuario (27-10-2026), como change aparte tras el 13b. Alternativa a las marchas de la app cuando el mando va por wifi dentro del rodillo (13b): en vez de ignorar la marcha virtual de Zwift, se sigue.

- Las palancas del Square se reenvían a Zwift como los botones de cambio de una Zwift Ride (`CAMBIO_SUBIR` y `CAMBIO_BAJAR`, ya en `MandoZwiftCodec`); Zwift cambia de marcha y manda la relación nueva en un `SET`; la app la aplica al Neo y muestra esa marcha en Rodaje.
- **Tabla de Zwift (1×24, «Mixed Terrain»)**, de Zwift Insider: 0,75; 0,87; 0,99; 1,11; 1,23; 1,38; 1,53; 1,68; 1,86; 2,04; 2,22; 2,40; 2,61; 2,82; 3,03; 3,24; 3,49; 3,74; 3,99; 4,24; 4,54; 4,84; 5,14; 5,49. La captura del 13 la confirma en un punto: relación 1,68 (`16800`) con «GEAR 8» en el HUD. La marcha se obtiene buscando la relación más cercana.
- **A validar:** que el tope sea 5,49 y no 5,59 (otra fuente); qué relaciones manda Zwift con los otros rangos (Flat, All-Arounder, Climbing) o si detecta el desarrollo físico; y la latencia de ida y vuelta (palanca → Zwift → `SET` → Neo) frente a NF1.
- **Alcance previsto:** solo el modelo físico (los modelos simple y de tabla, las transmisiones de marca y los platos dependen de las marchas de la app); ajuste en Ajustes «Marchas de la app» o «Marchas de Zwift», solo con el mando por wifi. Cambia F9.3 y pasa por maqueta.
- La prueba del 13b mira de paso (pregunta 8 de su `design.md`) si Zwift manda la relación nueva al enviarle los botones de cambio.

### 23. `pestana-plataformas`

- Propuesto por el usuario (28-10-2026): separar lo que la app consume (Dispositivos) de lo que simula y publica (nueva pestaña «Plataformas»), con Botones dentro de Ajustes y un solo modo compatible a la vez. Tras la exploración: plataforma → modos como tabla de compatibilidad en `:core` (`Compatibilidad`), Zwift Ride con el mando siempre dentro de DirCon y «Nombre ante la app» en lugar del interruptor de KICKR BIKE.
- Por el camino, y a petición del usuario sobre la maqueta: la barra de conexiones de Rodaje queda solo con dispositivos (el Square con icono de mando); el estado de la plataforma va en palabras junto a Ajustes con el modo debajo («Conectado a Zwift · sin mando» si el mando por Bluetooth espera); en ERG se quita la línea explicativa de las palancas.
- `Configuracion` v8 con migración desde la v7. Se descarta el 13c (`marchas-de-zwift`).
- Hecho y comprobado en el OnePlus 8 (28-10-2026): la migración conserva Zwift Ride y el nombre KICKR BIKE PRO, y Rodaje y Plataformas se ven como en la maqueta. Pruebas unitarias en verde.
- **Archivado con pruebas pendientes**, por decisión del usuario (tarea 2.10 sin marcar en `openspec/changes/archive/2026-10-28-pestana-plataformas/tasks.md`): con Zwift en PC, que reconozca el dispositivo sin emparejar de nuevo tras actualizar; cambiar de modo y de nombre rodando; «Conectado a Zwift · sin mando»; abrir Plataformas desde el estado de Rodaje; «No visible» con el nombre falso; recorrer las cuatro pestañas con «Pantalla anterior/siguiente». Quedan también por ejecutar los tests de instrumentación (adaptados y compilados, sin lanzar porque `connectedDebugAndroidTest` borra los datos de la app).
- **Prueba con Zwift en el PC (28-10-2026, parcial).** El cambio de modo funciona: el anuncio mDNS se sustituye sin duplicados (un solo servicio en cada momento) y Zwift sincroniza el `KICKR BIKE PRO` de Zwift Ride. Quedan sin probar el resto de puntos de la tarea 2.10. Dos hallazgos:
  - **Cambio de modo en caliente con Zwift en el emparejado:** Zwift abre de 30 a 60 conexiones DirCon por segundo y `DirconServer`, que solo atiende a un cliente y sustituye al anterior, las corta en pleno descubrimiento. Zwift reintenta y se forma un bucle que acaba estabilizándose solo tras un rato (duración sin medir). Solo ocurre al cambiar en caliente; con Zwift cerrado o abierto después del cambio no se da. Limitación conocida, sin change. Mitigación barata si molesta: un texto al cambiar de modo («Zwift puede tardar en reconocer el cambio; ciérralo y ábrelo»). Arreglo de fondo: que el servidor DirCon acepte varios clientes a la vez, con las suscripciones por cliente (hoy en `ServidorGatt`).
  - **Zwift lista el KICKR también en «controlador» con el modo FTMS**, aunque el anuncio no lleva `FC82`, y no responde al Square; el mando que funciona es el del OnePlus por Bluetooth. A validar si es comportamiento de Zwift (no se ha leído su registro: el PC no tenía registros de la sesión).
- El change 11 (`puente-zwift-bluetooth`) hereda la pestaña: añade filas a la tabla de compatibilidad y a sus plataformas.

### 24. `generalizar-plataformas`

- Decidido con el usuario tras el 23 (28-10-2026): el siguiente paso es dejar de particularizar en Zwift por dentro, antes de añadir ninguna otra plataforma. Refactor interno completo: lo que funciona queda igual y no hay cambio visible. Como la app todavía no la usa nadie más que el autor, **no hay migración** de la configuración ni del `estado.json`: se leen con los valores por defecto (se conserva lo que no es de la plataforma) y hay que emparejar de nuevo en Zwift una vez.
- **Qué incluye** (detalle en `openspec/changes/generalizar-plataformas/design.md`):
  - Dominio: `ModoConexion` solo de transporte y perfil (`FTMS_WIFI`, `ZWIFT_RIDE`; `FTMS_BLUETOOTH` lo añade el 11), `MandoEnModo` (`NINGUNO`, `BLUETOOTH_OPCIONAL`, `DENTRO_DEL_MODO`) en la tabla `Compatibilidad`, `AjustesPlataforma` y `Configuracion.plataformas` por plataforma (v9), e identidades en un mapa con clave (plataforma, modo, nombre).
  - Conexión: `ConexionPlataforma` y `MandoEmulado?`; `ConexionZwiftFtms` se parte en `ConexionFtmsWifi` (válida para cualquier plataforma) y `MandoZwiftBluetooth`; `ConexionZwiftRide` se queda como propia de Zwift; `Accion.Zwift` pasa a `Accion.Mando(FuncionMando)`.
  - Sesión: el puente sale de `Sesion` a `PuentePlataforma`.
  - Paquetes y nombres: lo genérico en `plataforma/` y lo propio de Zwift (`FC82`, `MandoZwift`, `SimulacionZwift`, `ConexionZwiftRide`) en `plataforma/zwift/`; textos con el nombre de la plataforma elegida.
- **Specs:** `zwift-bridge` se retira y sus requisitos pasan a `platform-bridge` (genérica) y `zwift-ride` (modo Ride); `zwift-controller` queda solo con lo propio del mando de Zwift; `platform-settings` y `ride-screen` se ajustan. Las funciones asignables y los gestos ambiguos se quedan en `zwift-controller`: describen cómo llega cada función a Zwift.
- **Fuera de alcance:** ningún protocolo nuevo, FTMS por Bluetooth (el 11), otras plataformas, y que `DirconServer` atienda a varios clientes (limitación anotada en el 23).
- **Orden:** 24 antes del 11. El mapa por plataforma y la versión 9 de `Configuracion` entran aquí, no en el 11.

### 25. `rodaje-pantallas-bajas`

- Detectado al revisar los tests de instrumentación (28-10-2026): `RodajeTest.todo_visible_en_un_movil_pequeno_con_botones` fallaba desde `pestana-plataformas` (la fila del estado de la plataforma). A 360 × 640 con 6 datos y botones la marcha se reduce a 0 dp, también en la maqueta.
- Decidido por el usuario (28-10-2026), sobre medidas de la maqueta: en vertical con menos de 760 dp de alto, modo compacto (espacio entre bloques de 16 a 8 dp y cassette de 56 a 40 dp) y, con los botones − y + activados, como máximo 4 datos (o 6 sin botones); la marcha pasa de 0,36 a 0,72. Los datos sobrantes se ocultan sin tocar la configuración, y «Datos en pantalla» avisa (no bloquea).
- Umbral de la pantalla baja: 760 dp (no 700, como se propuso): al medir, sin la regla la marcha con 6 datos y botones es 0,25 a 700 dp y 0,35 a 720; con ella, 0,61-0,82 de 640 a 799 dp. El OnePlus 8 mide 764 dp en inmersivo (medido), 4 dp por encima del umbral, así que no activa el modo compacto.
- Descartado: encoger la cifra de los datos a 36 px para que quepan 6 con botones, e impedir la combinación en Ajustes.
- Pasa por maqueta: un tamaño 360 × 640 en `maquetas/rodaje-vertical.html` y el aviso en `maquetas/datos.html`.

### 26. `pestana-conexion`

- Propuesto por el usuario (28-10-2026): con más plataformas (MyWhoosh, Rouvy…) la pestaña «Plataformas» se volvería confusa, cuando casi todas usan conexiones estándar. Se reorganiza por lo que la app publica y su estado. La app solo publica servicios: es la app de entrenamiento la que se conecta a la smart bike.
- Decidido al explorarlo: sin elección ni guardado por plataforma (la plataforma queda como etiqueta de compatibilidad de cada modo, solo con lo probado); tres modos nombrados por transporte; el mando sin interruptor; «configurado = se publica» (rodillo, Square y pulsómetro configurados; sin rodillo no se publica nada; deshace la decisión 4 de `pulso-zwift`); estado por servicio según la suscripción; la pestaña se llama «Conexión» (nombre fácil de cambiar) con el estado arriba; Dispositivos con una sección y «Buscar»/«Olvidar» por tipo; sin migración de la conexión ni de las identidades (hay que emparejar de nuevo una vez).
- Pasa por maqueta: `maquetas/conexion.html` (antes `plataformas.html`), `dispositivos.html`, el estado de `rodaje.html`, Botones y la barra inferior.
- A validar con el equipo real (decisión 8 del `design.md`): cuándo se suscribe Zwift a cada servicio, si ve el pulso añadido a un dispositivo ya emparejado, si Zwift Ride funciona sin el mando dentro, y el experimento con otras plataformas.
- Fuera de alcance: elegir dispositivo de una lista, otros rodillos y mandos, modos nuevos y la app como puente sin rodillo.
- Validados por el usuario los tres modos con el OnePlus 8, el Neo, el Square, la HW9 y Zwift en PC (28-10-2026):
  - «Wifi + Bluetooth»: con solo el rodillo emparejado, Rodaje dice «Conectado · sin mando».
  - «Solo Bluetooth»: con solo el rodillo, Zwift usa FTMS. Al emparejar también el mando, lo trata como un Zwift Ride: muestra su marcha y manda la pendiente por el mando. Al principio marcaba 0 W; se arregló atendiendo los dos caminos (decisión 9 del `design.md`), y ahora en los dos ve la potencia y el Neo se endurece en las subidas.
  - «Solo wifi (modo Zwift Ride)».
- V1, en parte: con el dispositivo ya configurado, Zwift se suscribe a todo al conectar (Indoor Bike Data, Cycling Power, `2A37` y `FC82`).
- Pruebas pendientes al archivar (tareas sin marcar en `openspec/changes/archive/2026-10-28-pestana-conexion/tasks.md`):
  - V1 al emparejar por primera vez: cuándo se suscribe Zwift a cada servicio (2.1).
  - V2: si Zwift ve el pulso al configurar la banda con el dispositivo ya emparejado (4.3).
  - V4: MyWhoosh o Rouvy en «Wifi + Bluetooth» y «Solo Bluetooth» (2.2), y llevar lo que salga a `probadoCon` (3.2).
  - De principio a fin (7.1): olvidar y configurar la banda y el Square rodando, cambiar de modo rodando y latencia de la palanca.
  - Los tests de instrumentación desinstalan la app: tras ellos hay que volver a configurar los dispositivos y emparejar en Zwift.

### 27. `renombrar-controlador`

- Propuesto por el usuario (28-10-2026): el Square se llama «Mando» y lleva un mando de consola, igual que el servicio que la app publica; Dispositivos busca controladores en general y el gamepad no dice nada de unas palancas en un manillar de carretera.
- Decidido al explorarlo:
  - Dispositivos: «Controlador», genérico (hoy el Elite Square; mañana Zwift Play u otros), con un icono de manillar de carretera dibujado a medida (`i-manillar`), el mismo del estado del Square en la barra de Rodaje.
  - Conexión: el servicio publicado se llama «Controlador Zwift» y lleva el icono de Zwift, porque emula el controlador de Zwift; si otras plataformas lo admiten, se verá con la compatibilidad por modo.
  - «Control» se descartó por chocar con «Control manual del Neo»; «Palancas», porque el Square tiene más botones.
  - Se retira `i-sports_esports`.
- Manillar: el usuario eligió la propuesta frontal y, con una foto de referencia, se redibujó con bajadas en curva, puntas cortas y un puente corto (con bajadas rectas y labios largos parecía una «B»). La barra de Rodaje sigue diciendo «Square» en su descripción accesible (a confirmar al validar).
- Pasa por maqueta: el manillar a 24 y 14 dp en los cuatro estados de la barra (conectado, esperando, sin conexión y batería baja), Dispositivos, Conexión y Rodaje.
- Fuera de alcance: el refactor de Diagnóstico para ser agnóstico a la plataforma (otro change, sin planificar; solo siguen a `Enlace` sus iconos), los identificadores de dominio (`Enlace.Mando`, `MandoZwift*`) y los otros controladores.
- Validado por el usuario en la maqueta (28-10-2026): el manillar frontal redibujado a partir de su foto, la barra de Rodaje con «Controlador» en su descripción accesible y los dos iconos de Zwift repetidos en Diagnóstico.
- Probado en el OnePlus 8 (28-10-2026): el manillar se lee a 14 dp con el Square conectado, reconectando (apagado y encendido) y sin conexión; en reconectando el hueco recorta la punta del gancho derecho y sigue reconociéndose. Dispositivos dice «Controlador» con el manillar y Conexión «Controlador Zwift» con el icono de Zwift; allí el nombre ocupa dos líneas con «No se ofrece» y el usuario decidió dejarlo.
- Zwift en PC llama «Mandos» a la lista de su pantalla de emparejamiento (28-10-2026), así que las ayudas de Conexión conservan «lista de mandos» y «protocolo de los mandos de Zwift».
- Pendiente al archivar: el manillar con la batería baja en el móvil real (no se puede forzar sin vaciar el Square); `Diagnóstico` conserva «Square» y «Mando de Zwift» hasta su refactor agnóstico, que no está planificado.

### Otras plataformas (MyWhoosh, Rouvy…)

- Con el 24 y el 11 implementados, ya se puede probar cada una. La hipótesis del usuario es que, salvo Zwift, se ajustan más a los estándares (FTMS y Cycling Power, por Bluetooth o por wifi), y se validará con cada una: qué servicios y nombre piden, cuántas conexiones admiten y si descubren la app por wifi (DirCon) o solo por Bluetooth. Nada de esto se da por confirmado hasta probarlo con el equipo real.
- Un experimento barato, sin código, que se puede hacer ya con la app actual: ver si alguna descubre la app por wifi y controla la resistencia; lo que salga decide la urgencia del 11.
- Desde el 26 (`pestana-conexion`) se prueban con los modos «Wifi + Bluetooth» y «Solo Bluetooth», sin elegir plataforma. Lo que se compruebe va a la compatibilidad de cada modo (`ModoPublicado.probadoCon`), que hoy solo tiene Zwift.

### 14. `teclado-hid`

- Solo si el 12 descarta el mando emulado: el móvil como teclado Bluetooth con atajos de Zwift.
- Descartado: el 12 validó el mando emulado por Bluetooth.

### 15. `modelos-tabla-y-fisico`

- Modelos por tabla y físico, protecciones de `ControlRodillo` y dificultad global. Las bajadas (F6.5) se adelantaron al 7b; el modo elegido se aplica también a estos modelos.
- Del 10 (decisión tomada 12 de `funcional.md`): velocidad y distancia como datos de Rodaje, con la velocidad que calcula el modelo físico; peso de la bici (el del ciclista ya está en Ajustes) y potencia mínima, en Ajustes. `DatoRodaje` crece con los nuevos datos; una versión anterior de la app los descarta al leer.
- Implementado: `ModeloTabla`, `ModeloFisico` y `Recorrido` en `:core`, protecciones y dificultad en `ControlRodillo`, la acción «Siguiente modelo» y las pantallas (Rodillo y Ciclista en Ajustes, «Tabla de marchas», Datos en pantalla con ocho datos, Rodaje y Diagnóstico). Constantes del físico provisionales (`arquitectura.md` § 15, 5).
- Pruebas con el equipo real **pendientes** (las 4 del `design.md` del change): potencia del físico mientras cambia la cadencia y latencia de la palanca, parar y arrancar, subida/llano/bajada en los tres modelos con Zwift, y la velocidad contra la de Zwift.
- Prueba: comparar los modelos en el Neo con Zwift y elegir el predeterminado (decisión 2 de `funcional.md`); revisar el incremento por marcha y la marcha neutra del modelo simple (decisión abierta 8).

### 16. `registro-sesiones`

- Implementado (29-10-2026): maqueta validada por el usuario (`maquetas/sesiones.html`, sección «Depuración» de `maquetas/ajustes.html` y el componente `SessionRow`), `Configuracion` en la versión 11, `ArchivoSesion` en `:core`, sumidero en `RegistroSesion`, `GrabadorSesion`, `RepositorioSesiones`, `FileProvider` y las pantallas. Comprobado en el OnePlus 8: la app arranca, Ajustes muestra «Depuración» y «Sesiones grabadas» abre vacía. La cabecera no lleva «plataforma»: el change `pestana-conexion` la quitó de la configuración.
- Validado (29-10-2026) en el OnePlus 8: sin Zwift, con el agente (`adb` y toques en la app): activar sin espacio (el móvil tenía el 5,2 % libre, así que con el mínimo en 10 % no graba y lo dice), grabar con cabecera y sucesos con hora y sin tramas, cierre forzado con el archivo conservado, lista con la activa sin borrar, abrir (el sistema ofrece las apps de texto), cancelar y confirmar el borrado de una y de todas (la que se graba se conserva) y la parada por poco espacio con su línea final; y con el Neo y el Square, por el usuario. Observación: una búsqueda de dispositivos deja en el archivo las direcciones MAC de los dispositivos Bluetooth ajenos de alrededor; el aviso de la pantalla lo cubre y no se anonimiza (fuera de alcance del change).
- Guardar el registro de la sesión en archivos de texto de la app, solo con «Grabar sesiones» activado en Ajustes (apagado por defecto), con los sucesos y una cabecera, escritura continua y parada por espacio libre mínimo (10 % por defecto). «Sesiones grabadas», secundaria de Ajustes, lista las sesiones con su tamaño y permite abrirlas, compartirlas y borrarlas (una o todas). Sin tramas crudas, sin anonimizar y sin tope automático.

### 17. `experimentos-zwift-y-neo`

- F14 (marchas visibles en Zwift) y F15 (página FE-C 0x37). Probablemente se parta en dos al proponerlo.

### 18. `renombrar-smart-bike`

- Propuesto por el usuario (27-10-2026): el proyecto ha crecido hasta parecerse a una smartbike y el nombre «Square Shift» deja una marca de Elite en el título. Pasa a llamarse «Smart Bike» (`smart-bike` en el repositorio y en Gradle), con el paquete y el `applicationId` `app.smartbike` (`app.smartbike.debug` en depuración). «Square» queda como el nombre del mando Elite que la app soporta; el prefijo `Sq` del sistema de diseño se queda.
- Sin migración: es otra app para Android y su configuración era la de fábrica; hubo que emparejar «Smart Bike» en Zwift de nuevo. Sin maqueta: solo cambian textos.
- Las notas de los changes anteriores conservan el nombre que tenía la app entonces («Square Shift», `jmlopezdona.squareshift`): son el registro de lo que se hizo y se validó.
- Hecho y validado en el OnePlus 8 (27-10-2026): la app nueva se anuncia como «Smart Bike», Zwift la lista y se empareja como rodillo, potencia, cadencia y mando, el cambio de marcha funciona, «Simular un KICKR BIKE» sigue funcionando en los dos sentidos y la copia de seguridad propone `smart-bike-configuracion-<fecha>.json` y se importa.
- Quedan fuera: renombrar el repositorio en GitHub y la carpeta local, que hace el usuario al terminar.

### 19. `anadir-linter-kotlin`

- Propuesto por el usuario (28-10-2026): el estilo del Kotlin dependía de quién escribía cada fichero y nada lo comprobaba.
- ktlint 1.8.0 con el plugin de Gradle 14.2.0 (funciona con Gradle 9.8.2, AGP 9.4.1 y la caché de configuración), estilo `intellij_idea`, imports en orden lexicográfico simple y sin límite de línea. `Tokens.kt` y `build/` quedan fuera.
- Medición: 1161 infracciones en 148 ficheros; `ktlintFormat` corrigió 1025. De las 136 restantes, 131 eran composables en PascalCase (se mantiene la regla con `ktlint_function_naming_ignore_when_annotated_with = Composable`) y 5 se arreglaron a mano. Sin reglas desactivadas ni baseline. El formateo va en su propio commit, anotado en `.git-blame-ignore-revs`.
- Comprobación en el móvil: 239 tests y 8 fallos, los mismos 8 sin el formateo (previos, de `ftms-bluetooth`); el usuario dio el formateo por bueno y los arreglos quedan fuera de este change.

### 21. `pagina-web-publica`

- Propuesto por el usuario (27-10-2026): una web pública en GitHub Pages que explique qué es el proyecto y sus funcionalidades con capturas. Decisiones del usuario: capturas de las maquetas (datos ilustrativos), español e inglés, y la dirección por defecto `indoor-cycling-lab.github.io/smart-bike`.
- Hecho: `site/` (HTML generado por `scripts/sitio/generar.py` desde `site/contenido/{es,en}.json`, estilos solo con tokens, capturas en `site/img/` de `scripts/sitio/capturas.py`), la vista `#vista=captura` de las maquetas y la Action `.github/workflows/pages.yml`. Al revisar las capturas apareció un desfase en la barra de conexiones de Rodaje (el SVG de cada icono quedaba 7 px más abajo que su caja y el arco de «esperando» caía arriba): corregido en `components.css`, en las dos orientaciones.
- Maqueta validada por el usuario (28-10-2026), con los textos y las capturas de cada sección.
- **Archivado con la publicación pendiente**, por decisión del usuario (tareas 2.2, 2.3 y 2.4 sin marcar en `openspec/changes/archive/2026-10-28-pagina-web-publica/tasks.md`): depende del 20. Cuando el repositorio sea público, el usuario activa Pages («Source: GitHub Actions»), se lanza la Action, se comprueba la dirección pública (los dos idiomas, claro y oscuro, el móvil, y que `/docs/` no se publica) y se pone la web como «Website» en la descripción del repositorio. La Action solo se dispara con cambios en `site/` o a mano.

### 22. `identidad-bici-rodillo`

- Propuesto por el usuario (28-10-2026): el icono de la piñonera (change 10i) sigue válido pero no dice «bici» ni «rodillo» tras el cambio de nombre. Nuevo icono: bici de perfil, rueda trasera ámbar (el rodillo) y delantera gris; tres alternativas en la maqueta (D1 anillo, D2 disco macizo, D3 anillo con tambor) y la elección del usuario al validarla.
- Splash: la rueda trasera gira acelerando de menos a más rápido en como mucho 1 s (`rotation` del grupo de la `animated-vector`), sin efecto de rueda de carro.
- Maqueta validada por el usuario (28-10-2026): eligió D3 (anillo ámbar con radios y tambor gris, «el toque indoor»); D1 y D2 se descartaron. El cuadro termina antes de la rueda trasera porque `ink` sobre ámbar solo da 1,6:1. Se eliminaron la piñonera y sus variantes monocromas; la web (logo e icono de pestaña) usa la misma fuente.
- Validado por el usuario en el OnePlus 8 (28-10-2026): icono en el launcher y con iconos temáticos, y splash en frío con el giro acelerado, también con el tema «Claro».
- Fuera de alcance: icono de la notificación, icono «Botones», logotipo con texto.

## Decisiones pendientes que bloquean changes

| Decisión | Dónde | Bloquea |
|---|---|---|
| Square emparejado en Zwift hasta la fase 4 o adelantar F9 | `funcional.md` § 8, abierta 1 | Orden de las fases 2–4. Validado en el 6 que el giro funciona con el Square emparejado |
| ~~Palanca 1 y 2 de cada lado~~ | `funcional.md` § 8, abierta 2 | Resuelta en el 3: la 1 es la grande y la 2 la pequeña |
| ~~Pendiente por marcha con transmisión de marca~~ | `funcional.md` § 8, abierta 3 | Resuelta en el 8: proporcional a la relación |
| ~~Velocidad como dato de rodaje~~ | `funcional.md` § 8, abierta 4 | Resuelta en el 10: fuera hasta el modelo físico (15) |
| Platos y cassettes de fábrica | `funcional.md` § 8, abierta 5 | Resuelta en el 8 para la Shimano 105; SRAM y Campagnolo, en el 8b |
| ~~Valores por defecto de «Al rodar»~~ | `funcional.md` § 8, abierta 6 | Resuelta en el 10 |
| Incremento por marcha y marcha neutra del modelo simple; modelo predeterminado | `funcional.md` § 8, abierta 8 y decisión 2 | Prueba con el equipo real del 15 (implementado) |
| Último terreno de Zwift tras desconexión | `arquitectura.md` § 15, 1 | 6: 30 s provisionales, pendiente de la prueba de desconexión |
| ~~Frecuencia de telemetría hacia Zwift~~ | `arquitectura.md` § 15, 2 | Resuelta en el 6: 4 Hz |
| Exención de optimización de batería | `arquitectura.md` § 15, 3 | Prueba de 2 h pendiente del 6 |
| ~~Mando emulado por DirCon o solo por Bluetooth~~ | `arquitectura.md` § 15, 4 | Resuelta en el 12: solo por Bluetooth |

### 28. `simplificar-diagnostico-conexiones`

- Propuesto por el usuario (28-10-2026): Diagnóstico repetía el estado que ya muestran Conexión y Dispositivos.
- Decidido al proponerlo: se queda una sección corta «Datos de conexión» con una fila por dato (no por dispositivo), sin estado en palabra, avisos ni acciones; «baja» se lee en la fila de batería. Se conservan los identificadores de estado de la maqueta para no romper enlaces ni capturas.
- Decidido por el usuario al revisar la maqueta (28-10-2026): el aviso de batería baja del Square (el único de la app) pasa a Dispositivos, bajo el Controlador; la «Simulación de Zwift» pasa a la sección Simulación, porque no es un dato de conexión.
- Al preguntar el usuario si Dispositivos avisa de que falta el permiso de Bluetooth: la app sí (comparte los avisos de error con Diagnóstico), la maqueta no. Se añaden a la maqueta los estados «Neo no encontrado» y «sin permiso».
- Decidido por el usuario (28-10-2026): en Diagnóstico, «rodillo» en vez de «Neo» en los textos visibles (el registro de sesión y las demás pantallas no cambian).
- Pasa por maqueta: Diagnóstico en todos sus estados y Dispositivos con los estados nuevos.
- Maqueta validada por el usuario (28-10-2026). App hecha (28-10-2026): Diagnóstico sin estado ni avisos, «Datos de conexión» y la simulación de Zwift en Simulación, «rodillo» en vez de «Neo», y el aviso de batería baja en Dispositivos (`avisosDeConexion` y las acciones se mueven a `ui/dispositivos/AvisosConexion.kt`). Pasan los tests unitarios, `ktlintCheck` y los 36 de instrumentación de Diagnóstico y Dispositivos en el OnePlus 8.
- Validado por el usuario en el OnePlus 8 (29-10-2026): Diagnóstico con «Datos de conexión» sin estado ni avisos, la simulación de Zwift en Simulación, «rodillo» en los textos y el aviso de batería baja en Dispositivos. En la prueba se vio que la entrada de Ajustes seguía diciendo «Conexiones»: ahora dice «Datos de conexión» (app, maqueta y capturas de la web).
- Fuera de alcance: cambiar Conexión, Dispositivos o las demás secciones de Diagnóstico, el tráfico y el registro, y el refactor de Diagnóstico agnóstico a la plataforma.

### 29. `teclas-con-simbolos-dibujados`

- Propuesto por el usuario (28-10-2026), al ver la maqueta de Diagnóstico del 28: las flechas no están alineadas en la tecla y △ ○ □ ✕ no tienen la misma proporción, también en la app.
- Causa: la fuente `numeric` no trae esos glifos y el sistema usa una de reserva por glifo, cada una con su tamaño y su línea base; en el móvil depende además de la fuente del sistema. No se arregla con CSS ni con Compose.
- Decidido al proponerlo: ocho iconos de `icons.svg` de un solo trazado relleno, con las formas de `i-botones`, y `SquareKey` los elige por el glifo del texto sin cambiar de firma. Las letras y los nombres siguen como texto.
- Pasa por maqueta: guía de SquareKey, Diagnóstico, Botones y Asignar acción.
- Fuera de alcance: los glifos dentro de frases, el tamaño o el diseño de las demás teclas y los textos de `strings.xml`.
- Orden con el 28: no depende de él; los dos tocan `maquetas/diagnostico.html` en secciones distintas.
- Resultado (29-10-2026): ocho iconos `i-tecla_*` y `SquareKey`/`CapturePanel` los dibujan por el glifo. Probado en el OnePlus 8 (Diagnóstico, Botones y Asignar acción); el usuario lo da por validado. Siete tests de instrumentación fallan por causas ajenas al change (textos «Controlador» de la barra de conexiones del change 27 y `AjustesTest`), sin investigar.

### 30. `quitar-barra-estado-rodaje`

- Propuesto por el usuario (29-10-2026): la app replica en Rodaje la hora, el wifi y la batería de la barra de estado; quiere quitar la réplica y eliminar el código muerto.
- Decidido al explorarlo: no ocultar las barras del sistema (el sistema pinta lo que antes se replicaba) y, en vertical, las conexiones pasan a la derecha de la fila del estado de la publicación, donde estaba Ajustes, y Ajustes baja al final de la fila SIM/ERG. En horizontal, solo sin el grupo central y con 12 dp entre la barra y Ajustes. Los iconos de la barra de conexiones se agrupan: de 10 a 4 dp entre ellos.
- Retira la parte visible del 10h y su prueba con el equipo pendiente (tarea 2.8). Mantiene `AdaptadorWifi`, `ACCESS_NETWORK_STATE`, el «Sin wifi» del estado de la publicación y la Z de Zwift tachada.
- Pasa por maqueta: Rodaje vertical y horizontal (estados nuevos «peor caso» a 360 dp y «enlaces mezclados»; se quitan batería baja, cargando y cámara en el centro) y la guía de `ConnectionBar`. Validada por el usuario (29-10-2026).
- Decidido en la maqueta: si el estado de la publicación no cabe junto a las conexiones, pasa a dos líneas dentro de su objetivo táctil de 48 dp, sin alargar la fila.
- Hecho: se borran `DeviceStatus` y `sq-devstatus`, `AdaptadorEstadoMovil`, `EstadoMovil`, `Bateria` y `colocarFranja` con sus tests, los iconos de batería y el de wifi; `RodajeRuta` ya no oculta las barras. Se corrigen de paso tres tests de instrumentación que esperaban «Square» donde la descripción ya dice «Controlador» (change 27). Tests: `:core`, unitarios y 224 de instrumentación en verde en el OnePlus 8.
- Prueba con el equipo: visto en el OnePlus 8 en vertical (barra de estado de Android arriba, estado y conexiones en una fila, Ajustes junto a SIM) y probado por el usuario en horizontal.
- Ajuste del usuario al verlo en el móvil (29-10-2026): menos hueco entre la barra del sistema y la primera fila, en vertical y en horizontal. Sin relleno arriba en Rodaje (el chip queda a 8 dp de la barra en vez de 24); `RodajeTest` y ktlint en verde.
- Cambio de disposición pedido por el usuario en horizontal (29-10-2026), validado en la maqueta: el modo con la transmisión sube a la línea superior (izquierda), el estado de la publicación queda centrado en la pantalla y las conexiones con Ajustes a la derecha; la marcha y los datos ganan los 44 dp que reservaba la esquina. Hecho en la app (`BarraSuperior` como `Layout` de tres columnas); `RodajeTest` (72) y ktlint en verde. Probado por el usuario en el OnePlus 8 en horizontal.
- Archivado (29-10-2026): specs sincronizadas en `openspec/specs/` (`ride-screen`, `ui-components`, `screen-mockups` y `style-guide`). Sin pruebas pendientes.

### 34. `idioma-es-en`

- Pedido por el usuario (29-10-2026): la interfaz solo estaba en español; ahora en español e inglés, con un ajuste en Ajustes.
- Decidido al explorarlo: el selector tiene solo «Español» y «English» (no «Automático»): el primer arranque detecta el idioma del móvil (español si es español, inglés en cualquier otro caso), lo guarda y desde entonces la elección es del usuario («la gente no cambia habitualmente el idioma del móvil»). Documentación, specs, trazas y registro de Diagnóstico siguen en español.
- Decidido en el diseño: el idioma se aplica con un contexto localizado dentro de la composición, sin `AppCompatDelegate` ni recrear la actividad. Un archivo de copia anterior al ajuste conserva el idioma de la app.
- Pasa por maqueta: el ajuste «Idioma · Language» en `maquetas/ajustes.html` (estado `ingles`). Validada por el usuario (29-10-2026).
- Hecho: `IdiomaApp` y `Configuracion.idioma` (versión 12), `fijarIdiomaInicial`, `IdiomaElegido`, la notificación de `RideService`, formatos con `Locale`, `values-en/strings.xml` (611 entradas, traducidas a partir del glosario del diseño) y `TextosInglesTest`. Probado en el OnePlus 8: primer arranque con el móvil en español (guarda `es`), cambio a English rodando y pantallas en inglés.
- Archivado (29-10-2026), con cuatro tareas sin marcar: 3.2 (ver la notificación de la sesión en los dos idiomas), 4.1 (revisión de `values-en/strings.xml` por el usuario; términos a mirar: «shifter L/R», «Easy/Hard», «limit», «Heart rate monitor») y 6.1 y 6.2 (primer arranque con el móvil en inglés y todas las pantallas a 360 dp con la fuente al 130 %). Tests: `:core`, unitarios y 247 de instrumentación en verde en el OnePlus 8 tras corregir `AjustesTest.depuracion_al_final_con_los_valores_por_defecto`, que fallaba desde antes (el valor «10 %» quedaba fuera de la ventana tras `performScrollTo`).
- Conocido y no tocado: en la fila «Espacio libre mínimo» de Depuración el selector −/+ deja unos 80 dp a la etiqueta y esta se parte en tres líneas (ya era así).
