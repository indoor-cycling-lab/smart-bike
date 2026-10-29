# Proposal

## Why

En la pantalla de Botones (Ajustes → Botones) el primer grupo es «Funciones de Zwift», con 12 filas, y «Acciones de la app» queda debajo. Lo que el ciclista reconfigura de verdad son las acciones de la app (marchas, ERG, acciones de pantalla), y es la asignación que depende de la transmisión elegida. Las funciones de Zwift salen por defecto como en un mando oficial, son iguales en las dos transmisiones y sin Square configurado ni siquiera llegan a la plataforma. Con el orden actual, «+1 marcha» y «−1 marcha» quedan fuera de la primera pantalla y hay que desplazarse para llegar a lo principal. Ningún requisito ni decisión archivada justifica ese orden: es consecuencia de haber añadido las funciones de Zwift al final del change `mando-zwift-emulado`.

## What Changes

- En Botones, el grupo «Acciones de la app» pasa a ir **antes** que «Funciones de Zwift». El orden de la pantalla queda: Acciones de la app, Funciones de Zwift, Gestos y «Restaurar por defecto».
- Contenido, textos de ayuda, filas y comportamiento de cada grupo no cambian; solo su orden.
- La captura «botones» de la web pública pasa a mostrar el grupo de acciones de la app en primer lugar; se ajusta su texto alternativo, que hoy dice que la captura muestra las funciones de Zwift.

Fuera de alcance:
- Cambiar el orden de las filas dentro de cada grupo.
- Mover «Gestos» o «Restaurar por defecto».
- Cambiar nombres, textos o ayudas de los grupos, o la pantalla Asignar acción.
- Cualquier cambio de comportamiento: qué se asigna, cómo se guarda o cómo llega a Zwift.

Es una porción vertical completa: se ve en la maqueta, en la app del OnePlus 8 y en la captura de la web, sin tocar el protocolo ni el equipo (Neo, Square, Zwift).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `button-mapping`: el requisito «Pantalla de Botones» fija el orden de los grupos (acciones de la app primero, funciones de Zwift después).

## Impact

- Maqueta: `maquetas/botones.html` (sección de Zwift tras la de la app, en todos los estados) y, si hace falta, `maquetas/index.html`.
- App: `ui/botones/Botones.kt` (orden de `AccionesApp` y `FuncionesZwift` en la columna) y su test de UI si lo hay.
- Web: `site/capturas.json` (texto alternativo en `es` y `en`) y regenerar `site/img/` con `scripts/sitio/capturas.py`.
- Specs: delta de `button-mapping`.
- Documentos de `docs/`: `planificacion.md` (fila nueva «En curso», luego «Archivado»); `funcional.md`, `arquitectura.md` y `desarrollo.md` se revisan y, salvo que mencionen el orden, no cambian.
- Es un cambio de disposición, así que pasa por maqueta y validación del usuario antes de tocar la app (AGENTS.md). Fase 4, sin dependencias de otros changes.
