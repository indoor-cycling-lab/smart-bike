# Design

## Context

Hoy «Conexiones» de Diagnóstico mezcla tres cosas: estado de conexión (repetido en Conexión y Dispositivos), acciones (buscar, activar Bluetooth, abrir ajustes) y datos finos que solo están ahí (IP de Zwift, nombre anunciado, batería, último pulso, notificación del ventilador, simulación de Zwift). Ver proposal.md. Las otras secciones (botones del Square, simulación, tráfico, control manual) leen sus propios flujos y no dependen de la sección que se rehace; algunas muestran avisos propios («Conecta el Square para ver sus botones») que se conservan.

## Goals / Non-Goals

**Goals:**
- Una sola fuente de estado por dispositivo: Conexión y Dispositivos. Diagnóstico solo aporta datos.
- Que la sección siga sirviendo para depurar en pleno rodaje sin saltar entre pestañas: los datos finos se ven juntos.

**Non-Goals:**
- Quitar datos que hoy se registran en el registro de sesión; solo cambia lo que se pinta.
- Rediseñar las demás secciones.

## Decisions

0. **Datos agnósticos de plataforma.** La app solo publica servicios y se conecta quien quiera (Zwift, Rouvy, MyWhoosh…). La fila de origen se llama «App de entrenamiento» y el nombre, «Nombre anunciado»; solo la simulación de marchas virtuales lleva «Zwift», porque es un dato de su protocolo. Alternativa: una fila por plataforma; se descarta porque no escala y porque la app no sabe qué app es la que conecta.

1. **Una fila por dato, no por dispositivo.** Cada fila lleva el dato y su antigüedad cuando importa («128 bpm · hace 2 s»), sin la palabra de estado ni el punto de color. Alternativa: dejar una versión compacta del estado (una línea por dispositivo). Se descarta: sigue duplicando y obliga a mantener los avisos.
2. **Sin estado en palabra, el dato ausente se dice con «—»**. Si no hay Square recordado, no hay fila de batería; si el dato no ha llegado, «—» o el texto que ya existía («Batería: sin lectura todavía»). Así la ausencia de conexión se lee sin repetir «Sin conexión».
3. **«baja» se queda en la fila de batería** (texto, no solo color) y se retira el aviso de la sección; el aviso de batería baja lo cubre la spec `square-battery` fuera de Diagnóstico. Si no hay otro aviso que la cubra, se decide al validar la maqueta (pregunta abierta).
4. **Los avisos de Bluetooth apagado, sin permiso, sin wifi y reconectando** salen de Diagnóstico; ya los tienen Dispositivos y Conexión (`device-connection`, `platform-bridge`). Se comprueba en la maqueta de cada una antes de quitarlos.
5. **Estados de la maqueta.** Se conservan los identificadores de estado que otras secciones necesitan (`conectado`, `neo-no-encontrado` para los botones de envío deshabilitados, `bluetooth-apagado`, `sin-permiso`, `buscando`, `conectando`, `rodando`, etc.) para no romper los enlaces ni las capturas de la web; solo cambia lo que cada uno pinta en la primera sección.
6. **Sin código nuevo en `:core`.** Los datos ya existen en el estado de la sesión; el cambio es de presentación.

## Risks / Trade-offs

- [Se pierde la vista conjunta de estado al depurar] → los datos finos y el registro de sesión siguen en Diagnóstico; el estado está a un toque en la barra inferior.
- [Algún aviso de Diagnóstico no tiene equivalente en Conexión o Dispositivos] → se comprueba en la maqueta y se anota en la planificación si falta, sin ampliar este change.
- [Enlaces y capturas de la web que apuntan a estados de Diagnóstico] → se conservan los identificadores de estado y se regeneran las capturas.

## Open Questions

Ninguna abierta.

## Decisiones del usuario al validar la maqueta (28-10-2026)

- **Aviso de batería baja → Dispositivos (opción b).** Antes: comparadas las maquetas (tarea 1.2): el único sitio que hoy muestra «Batería del Square baja (15 %). Revisa la pila antes de rodar.» es Diagnóstico; ni Dispositivos ni Conexión ni Rodaje tienen un aviso equivalente, aunque la spec `square-battery` pide avisarla. Quitarlo de Diagnóstico sin otro destino lo dejaría sin aviso en toda la app. Decidido: se mueve a Dispositivos, bajo el Controlador, como `sq-notice` informativo con icono y texto; desaparece al subir del umbral o al desconectarse el Square. Diagnóstico solo dice «15 % · baja» en la fila. Hoy Dispositivos filtra los avisos y solo deja pasar los de error (`it.error`), así que el informativo de batería hay que dejarlo pasar, solo el de batería y solo con el Square conectado.
- **«Rodillo» en Diagnóstico.** Los textos visibles de Diagnóstico llaman «rodillo» al Neo, como Dispositivos y Conexión; «Tacx Neo» queda solo como modelo. Se mantienen los identificadores de estado de la maqueta (`neo-no-encontrado`) y los nombres de los escenarios de las specs, que no se pueden renombrar al modificarlos. El registro de sesión conserva «Neo» como origen: es un dato para analizar, no un texto de la interfaz.
- **Simulación de Zwift → sección Simulación.** No es un dato de conexión; conserva su nombre (es del protocolo de Zwift) y solo sale con Zwift por wifi y si ha enviado alguna.

## Avisos que salen de Diagnóstico (tarea 1.2)

| Aviso | ¿Lo cubre otra pantalla en la maqueta? |
|---|---|
| Bluetooth apagado | Sí: Dispositivos y Conexión |
| Reconectando (Square, banda, ventilador) | Sí: Dispositivos (estados «reconectando» y «no encontrado») |
| Pulsómetro, Square y ventilador no encontrados | Sí: Dispositivos |
| Sin wifi / la app de entrenamiento no encuentra la app | Sí: Conexión |
| Neo no encontrado | En la app sí (Dispositivos); en la maqueta no: se añade el estado |
| Sin permiso de Bluetooth | En la app sí (Dispositivos, con «Abrir ajustes»); en la maqueta no: se añade el estado |
| Batería baja | No, ni en la app ni en la maqueta: se añade a Dispositivos en este change |

Neo no encontrado y sin permiso no son huecos de la app: Dispositivos ya los muestra (usa `avisosDeConexion` y deja pasar los de error). Son huecos de la maqueta de Dispositivos frente a su spec. Al preguntar el usuario por el de permiso, se cubren en este change (estados nuevos en la maqueta; sin cambios de spec, que ya los pide).
