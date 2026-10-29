# Design

## Context

La pantalla de Botones (`ui/botones/Botones.kt`) compone cuatro bloques en una `Column` con scroll: `FuncionesZwift`, `AccionesApp`, `Gestos` y `Restaurar`. Cada bloque es un composable independiente que recibe solo `BotonesUiState` y `AccionesBotones`, así que el orden es solo el de las llamadas. La maqueta (`maquetas/botones.html`) tiene las mismas secciones en el mismo orden, con textos y filas que dependen del estado (`data-mock-state`: `lista`, `sin-square`, `modificada`, `shimano`, `restaurar`). Ver proposal.md para la motivación.

## Goals / Non-Goals

**Goals:**
- Que «Acciones de la app» sea el primer grupo de la pantalla, en la maqueta y en la app, sin cambiar nada más.

**Non-Goals:**
- Cambiar el modelo de estado, los textos o los componentes.
- Reordenar filas dentro de un grupo.

## Decisions

1. **Solo se intercambian dos bloques.** En `Botones.kt` se llama a `AccionesApp` antes que a `FuncionesZwift`. Alternativa descartada: fusionar los dos grupos en una sola lista con la insignia de capa (`ZWIFT` / `APP`) de cada fila; mezclaría 12 funciones fijas con las acciones propias de la transmisión y perdería las ayudas de cada grupo.
2. **Los tests no cambian por el orden.** `BotonesTest` localiza todo con `performScrollTo()`, así que no depende de la posición. El orden se comprueba con un test nuevo que compara la posición vertical de los dos títulos.
3. **La captura de la web se regenera.** `site/capturas.json` conserva el estado `lista` de `botones`, que ahora enseña las acciones de la app arriba; se corrige el texto alternativo (`es` y `en`), que hoy habla de las funciones de Zwift.
4. **Sin migración ni cambios de datos.** No se toca `Configuracion` ni `estado.json`.

## Risks / Trade-offs

- [Quien ya se había acostumbrado a ver Zwift primero] → Es una pantalla de configuración que se visita poco; los títulos de los grupos no cambian.
- [El texto de ayuda de «Acciones de la app» menciona la asignación de la transmisión y ahora queda en la primera pantalla; puede crecer] → Se revisa en la maqueta a 390 × 844 y con la Shimano; no se espera cambio de textos.

## Validación con el equipo real

No hay comportamiento BLE «a validar». Se comprueba a ojo en el OnePlus 8 con la skill `desplegar-movil`: Ajustes → Botones muestra «Acciones de la app» primero.
