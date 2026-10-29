# AGENTS.md

Instrucciones para agentes de IA que trabajan en este repositorio. La documentación para personas está en [`README.md`](README.md).

## Antes de empezar

- Lee [`docs/funcional.md`](docs/funcional.md) antes de proponer o implementar cualquier cambio. Referencia sus identificadores (F1…F15, NF1…NF7).
- Lee [`docs/arquitectura.md`](docs/arquitectura.md) antes de cambiar la estructura de la app: módulos, componentes, flujo de datos y lo pendiente de validar con el equipo real. Si un change se aparta de ella, actualízala en el mismo change.
- Consulta [`docs/planificacion.md`](docs/planificacion.md) antes de proponer un change: parte de su fila (fase, funcionalidades, dependencias) y respeta el orden previsto, o explica por qué cambia.
- La configuración de OpenSpec, con el contexto del proyecto y las reglas para cada artefacto, está en [`openspec/config.yaml`](openspec/config.yaml). Respétala al escribir propuestas, diseños, specs y tareas.
- `docs/referencias/` es material de trabajo (protocolos BLE, análisis de otras apps). No formará parte del repositorio final: no lo enlaces desde código, specs ni documentación definitiva; incorpora a las specs lo que haga falta.

## Idioma

- Escribe en español la documentación, las specs, los comentarios y los mensajes de commit, con ortografía completa (tildes y eñes). Los textos de la UI están en español (`values/strings.xml`) y en inglés (`values-en/strings.xml`): cada cadena nueva va en los dos, y `TextosInglesTest` falla si falta alguna o cambian sus marcadores de formato. Las trazas, el registro de Diagnóstico y los nombres de archivo siguen en español.
- La web pública (`site/`) es la excepción: está en español e inglés (`site/contenido/es.json` y `en.json`, con las mismas claves). El resto del repositorio sigue en español.
- En el código, nombra el dominio en español sin tildes (`marcha`, `plato`, `pinon`, `transmision`) y la infraestructura genérica en inglés (`BleClient`, `retry`). Los composables se llaman como los componentes del sistema de diseño (`GearDisplay`, `MetricGrid`).

## Flujo de trabajo con OpenSpec

Encauza todo cambio funcional o de arquitectura por OpenSpec:

1. `/openspec-explore`: pensar el cambio, sin escribir código.
2. `/openspec-propose`: crear el cambio (`proposal.md`, `design.md`, specs delta, `tasks.md`).
3. `/openspec-apply-change`: implementar tarea a tarea, marcando cada una al terminarla.
4. `/openspec-archive-change`: archivar y sincronizar las specs en `openspec/specs/`.

Reglas:

- Propón porciones verticales que se puedan probar de principio a fin con el equipo real (Neo, Square, Zwift), no una capa entera de golpe.
- En las specs, describe comportamiento observable. Las decisiones técnicas van en el `design.md` del cambio.
- Si al implementar descubres que la spec o el diseño no se sostienen, para y actualiza los artefactos (`/openspec-update-change`) antes de seguir.
- Los cambios sin impacto funcional (erratas, refactors locales, dependencias) no necesitan change.
- No crees directorios de cambios a mano: usa siempre `openspec new change`.
- Mantén la documentación al día con cada change. Al crearlo, márcalo «En curso» en `docs/planificacion.md`. Antes de archivarlo, revisa `docs/funcional.md`, `docs/arquitectura.md`, `docs/planificacion.md` y `docs/desarrollo.md` y actualiza lo que haya cambiado (tarea «Actualizar la documentación», ver `openspec/config.yaml`). Al archivarlo, márcalo «Archivado».

## UI y sistema de diseño

- `design-system/` es la única fuente de estilos: tokens (`tokens.json`), componentes (`components.css`, prefijo `sq-`) y pautas (`README.md` general y `components/<Componente>/README.md`).
- `tokens.css` y `styleguide/index.html` se generan: cambia `tokens.json` o `components/` y ejecuta `python3 scripts/design-system/generar.py`. No los edites a mano.
- El mismo comando genera los derivados de la app: `app/src/main/kotlin/app/smartbike/ui/tema/Tokens.kt`, las fuentes de `app/src/main/res/font/` (copiadas de los TTF de `design-system/fonts/`), `app/src/main/res/values/color_ventana.xml` y los iconos `app/src/main/res/drawable/ic_*.xml` (de `design-system/icons.svg`, incluido `ic_botones`), más los recursos de identidad `drawable/identidad_*.xml` y `mipmap-anydpi/ic_launcher*.xml` (de `design-system/identidad/`). Tampoco se editan a mano. Solo se escriben si el contraste pasa, y `TokensSincronizadosTest`, `IconosSincronizadosTest` e `IdentidadSincronizadaTest` (`./gradlew :app:testDebugUnitTest`) fallan si `Tokens.kt`, los iconos o los recursos de identidad no coinciden con sus fuentes. Al añadir un peso de fuente, añade su TTF junto al WOFF2; al añadir un icono, cópialo en `icons.svg` y regenera.
- La web pública (`site/`) también se genera: `python3 scripts/sitio/generar.py` crea `site/es/`, `site/en/` y `site/assets/`, y `python3 scripts/sitio/capturas.py` crea `site/img/` a partir de las maquetas. Solo se editan a mano `site/contenido/`, `site/capturas.json` y `site/sitio.css` (este, solo con tokens). Tras un change de UI, regenera las capturas, que salen en español y en inglés: un texto nuevo de una maqueta fotografiada lleva su inglés (`maquetas/en.json`, `python3 scripts/sitio/diccionario.py`) o `capturas.py` falla. Detalles en [`docs/desarrollo.md`](docs/desarrollo.md).
- El generador comprueba al final el contraste de los tokens en los dos temas (`scripts/design-system/contraste.py`) y falla si algún par no llega a su mínimo. No des por bueno un cambio de color con el generador en error; al añadir un token de color, añade sus pares a `contraste.py`.
- Si una pantalla necesita un componente o una variante que no existe, añádelo primero a `design-system/` (estilos en `components.css`, carpeta con `README.md` y `preview.html`) y regenera la guía, que lo muestra en los dos temas.
- Toma las maquetas de `maquetas/` como referencia de disposición, textos y estados. Los composables reproducen esa estructura.
- Accesibilidad: objetivos táctiles de `touch-min` (48 dp) y `touch-ride` (72 dp) en rodaje; nada se transmite solo con color; contraste de texto 4,5:1 en los dos temas.
- La pantalla de rodaje muestra la marcha, nunca el modelo de resistencia (pendiente base, ajuste por marcha, lo enviado al Neo): eso va en Diagnóstico. La pendiente de la simulación (la que envía la app) solo aparece como dato opcional «Pendiente».

## Cambios de UI: maqueta primero

El usuario valida la UI/UX sobre la maqueta antes de que se implemente en la app. Un cambio con impacto visual se organiza en dos etapas dentro del mismo change (no confundir con las fases del proyecto de `docs/planificacion.md`):

1. **Etapa 1, maqueta.** Añade o modifica los componentes en `design-system/` y regenera la guía. Actualiza `maquetas/` con todos los estados afectados (selector de estado de `revision.js`, `data-mock-state`) y el índice `maquetas/index.html`. Termina con la tarea «Validación de la maqueta por el usuario».
2. **Puerta.** Detente. Da al usuario los enlaces exactos a revisar (p. ej. `maquetas/rodaje.html#estado=sram-4` y la sección de la guía) y espera. Marca la tarea de validación solo cuando el usuario apruebe de forma explícita. Si pide cambios, ajusta la maqueta, el `design.md` y las specs, y vuelve a pedir validación.
3. **Etapa 2, app.** Implementa en la app reproduciendo la maqueta aprobada, con sus tests.

Pasa por maqueta:

| Tipo de cambio | ¿Maqueta? |
|---|---|
| Pantalla, estado o componente nuevo | Sí |
| Cambio de disposición, jerarquía o flujo | Sí |
| Token nuevo o cambio de color | Sí, en el sistema de diseño y la guía, comprobando el contraste en los dos temas |
| Textos, erratas o ajustes finos ya cubiertos por tokens | No; se valida en la app |
| Bug de la app que se aparta de la maqueta | No; la maqueta ya es la referencia |

Para revisar: `python3 -m http.server 8000` desde la raíz y abrir `http://localhost:8000/maquetas/` y `http://localhost:8000/design-system/styleguide/`. En el móvil, misma red e IP del ordenador.

## Estilo del código Kotlin

- Antes de dar por terminada una tarea de código, ejecuta `./gradlew ktlintCheck` junto a los tests. Si falla, `./gradlew ktlintFormat` corrige la mayoría de avisos; arregla a mano los que queden.
- La configuración está en `.editorconfig` y `Tokens.kt` queda fuera (lo escribe el generador). No desactives una regla ni añadas `@Suppress` para que pase sin una decisión explícita. Detalles en [`docs/desarrollo.md`](docs/desarrollo.md).

## Hardware y BLE

- Todo lo marcado «a validar» en `docs/referencias/` necesita prueba con el equipo real antes de darse por bueno. No lo presentes como confirmado.
- No copies código de qdomyos-zwift (GPL-3.0) ni de BikeControl (no comercial): reimplementa a partir de la documentación de protocolos.
- Latencia de las palancas de cambio por debajo de 100 ms de extremo a extremo (NF1).

## Desplegar y probar en el móvil

- El desarrollo se hace en el Mac; las pruebas, en el OnePlus 8 del usuario por depuración inalámbrica. Entorno, comandos y reparto de tareas: [`docs/desarrollo.md`](docs/desarrollo.md).
- Usa la skill `desplegar-movil` para conectar, instalar, abrir la app y leer su registro.
- **Es el móvil personal del usuario.** Solo puedes usar `adb` sobre la app del proyecto; el hook `.claude/hooks/adb_guard.py` bloquea el resto. Si algo se bloquea, no busques otro camino: pídeselo al usuario (`! <comando>`). No modifiques el hook ni `.claude/settings.json` sin que el usuario lo pida.
- La vinculación del móvil (`adb pair`), pedalear, pulsar el Square y emparejar en Zwift los hace el usuario. Dile exactamente qué hacer y espera.
