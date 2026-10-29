# Design

## Context

Ver `proposal.md` para el porqué. La guía de inicio (`guia-inicio`, archivado) es una hoja de **tres páginas** (`GuideSheet.kt`: `PaginaQueEs`, `PaginaPasos`, `PaginaConectar`), con la página actual en `GuiaPantalla.pagina` (de `PRIMERA` a `ULTIMA = 3`, en memoria) y el texto del indicador en `guia_pagina` («%1$d de %2$d»). La spec `getting-started-guide` nombra las páginas por su número (`Página 1, qué es`…) y cita «1 de 3» en el nombre accesible. Los nombres de los modos que ve el ciclista salen del indicador de modo de Rodaje; con `modo-libre-y-pendiente` ese indicador distingue «LIBRE» de «SIM».

## Goals / Non-Goals

**Goals:**
- Una página nueva, tras los pasos, que diga las formas de rodar con los nombres reales del indicador y enlace con la conexión opcional.
- Tocar lo mínimo del resto de la guía: renumerar, no rehacer.

**Non-Goals:**
- Un tutorial, o hacer interactiva la página de modos.

## Decisions

**1. La página de modos es la tercera, entre los pasos y la conexión.** El orden va de las piezas a su uso: de qué se compone (1), qué hacer ya (2), cómo se puede usar (3), cómo conectar con la app (4). Cuando se llega a los modos ya se sabe qué son el rodillo, el controlador y la app, y los modos explican **cuándo hace falta la app**: LIBRE y ERG MANUAL no la necesitan; SIM y ERG APP sí, y la página termina remitiendo a la siguiente. Así la página 4 deja de leerse como un paso obligatorio: es **opcional** y lleva la marca «Opcional» (la misma de la página 2). Las páginas 1 y 2 conservan su número, y la hoja sigue abriéndose en la 1 en el primer uso y desde «Guía de inicio» de Ajustes. Alternativa descartada: ponerla la primera; obliga a explicar los modos antes de saber qué piezas intervienen y renumera todo. Alternativa descartada: fundirla con «qué es»; esa página ya cabe justa en 360 × 640 (medido en su maqueta). Alternativa descartada: ponerla la última; se pierde el puente hacia la conexión.

**2. `GuiaPantalla.ULTIMA` pasa a 4 y el `when` de `GuideSheet` inserta la página nueva, sin cambiar la lógica.** `irA`, `siguiente` y `anterior` ya acotan entre `PRIMERA` y `ULTIMA`; solo cambian la constante y el `when`, que gana `3 -> PaginaModos` y mueve `PaginaConectar` a `4`. Los controles que dependen de «la última página» (ocultar «Siguiente») ya usan `ULTIMA`. El acceso desde los pasos (p. ej. «Conecta tu app» lleva a Conexión, no a una página de la guía) no cambia. Alternativa descartada: guardar la página como identificador y no como índice; la página no se persiste y no aporta.

**3. Los nombres de los modos salen de las cadenas del indicador.** La página usa los recursos del indicador de modo (`modo_libre`, `modo_sim`, `modo_erg_manual`, `modo_erg_zwift`, de `modo-libre-y-pendiente`) dentro de una frase, así que renombrar un modo cambia la guía a la vez y `TextosInglesTest` cubre el inglés. Las frases de cada modo son cadenas propias de la guía (`guia_p3_*`; las de «conectar», hoy `guia_p3_*`, pasan a `guia_p4_*`, y `guia_p1_*` y `guia_p2_*` no cambian). Alternativa descartada: copiar los nombres en el texto; es lo que provoca que la guía diga «SIM» cuando el indicador dice «LIBRE».

**4. Sin tutorial de botones ni cambio de modo.** La página describe, no enseña: cómo se pasa de un modo a otro depende de la asignación de botones del usuario (y, con `modo-tactil-rodaje`, de una casilla), y eso cambia con el tiempo; el texto se quedaría viejo. Los cuatro textos son frases cortas: qué es el modo, cuándo se está en él y quién fija la resistencia o la potencia.

**5. La marca «Opcional» de la página 4 es solo una marca.** Lleva el mismo distintivo «Opcional» que el controlador y la app de la página 2, y la página de modos remite a ella con una frase («SIM y ERG APP necesitan tu app: conéctala en la página siguiente»). No cambia cuándo termina la guía ni qué dice la tira minimizada: siguen pidiendo una app conectada (ver «Fuera de alcance» de `proposal.md`). Alternativa descartada: terminar la guía con los dispositivos y sacar la app de los pasos pendientes; es una decisión sobre el flujo que el usuario no ha pedido por ahora.

**6. La maqueta se rehace por números, no por contenido.** Los estados de la guía de las maquetas de rodaje (`#estado=guia-…`) siguen existiendo; las páginas 1 y 2 solo cambian el indicador, se añade el de la página 3 y el de conectar pasa a ser el 4, con la marca «Opcional». Para no dejar en la spec escenarios con el mismo nombre y otro contenido, los escenarios «Página 3» y «Página 4» del requisito de maquetas conservan su nombre pero cambian de contenido (modos y conectar); «Página 1» y «Página 2» solo cambian el indicador a «n de 4». Es lo que permite al archivado reemplazar el bloque sin descartar escenarios.

**7. Cómo se escribe el delta.** La spec `getting-started-guide` ya está en `openspec/specs/` (el change `guia-inicio` está archivado). El delta usa `RENAMED` solo para la página con número que se mueve (`Página 3, conectar…` → `Página 4, conectar…`) y `MODIFIED` para los requisitos que citan el número de páginas o la página de conexión (con el texto completo); los demás no se tocan. Los `Purpose` no se tocan por delta: hay que editar «tres páginas» en `openspec/specs/getting-started-guide/spec.md` al archivar.

## Risks / Trade-offs

- [La guía queda más larga: cuatro páginas] → la nueva es solo lectura y corta, y la última es opcional; «Siguiente», minimizar y «No volver a mostrar» no cambian.
- [«Opcional» en la página 4 pero la guía sigue pidiendo una app para terminar] → es una incoherencia asumida por ahora (decisión 5); si en la prueba real confunde, se abre otro change para el fin de la guía.
- [«LIBRE» no existe en la app hasta `modo-libre-y-pendiente`] → este change se aplica después; sus tareas lo comprueban al empezar.
- [La página de modos no cabe en 360 × 640 con la hoja y los textos en inglés, más largos] → la maqueta valida las dos orientaciones y los dos idiomas; si no cabe, se acortan las frases, no se añade desplazamiento.
- [Los nombres de los modos cambian de nuevo en el futuro] → salen de las cadenas del indicador (decisión 3).
- [Quien ya vio la guía antigua no ve la nueva página] → sí la ve al reabrir «Guía de inicio» desde Ajustes; no se fuerza nada a quien la descartó.

## Migration Plan

Sin migración: lo que recuerda la guía (`estado.json`) no cambia y la página no se persiste.

Prueba con el equipo real (OnePlus 8): ver arriba, en `proposal.md` (Impact).

## Open Questions

- El texto exacto de las cuatro frases y su orden dentro de la página (LIBRE, SIM, ERG o de menos a más complejo): se fija en la maqueta, sin cambiar las specs.
