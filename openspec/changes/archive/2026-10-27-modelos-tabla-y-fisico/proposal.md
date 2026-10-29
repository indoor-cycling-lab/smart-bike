# Proposal

## Why

Hoy la marcha solo suma un porcentaje fijo a la pendiente (modelo simple, F6.1), con el mismo paso en todas las marchas y sin relación con la velocidad. Tras la primera prueba con el equipo real (decisión abierta 8 de `funcional.md`) falta poder afinar el reparto de las marchas (F6.2), probar un modelo en el que la marcha cambie la potencia como en una bici real (F6.3), ajustar la dureza global (F6.4) y comparar los tres en el Neo con Zwift para elegir el predeterminado (decisión 2). El modelo físico también permite mostrar velocidad y distancia coherentes con la marcha (decisión 12), que se dejaron fuera del change `rodaje-completo-y-ajustes`.

## What Changes

- **Elegir el modelo de resistencia** en Ajustes → Rodillo: «Simple» (por defecto, sin cambios), «Por tabla» y «Físico». El cambio se aplica en caliente y también se puede alternar con una acción nueva, «Siguiente modelo», sin botón por defecto (la arquitectura ya la prevé en § 5.4).
- **Modelo por tabla (F6.2):** cada marcha tiene su propio ajuste de pendiente, editable en una pantalla nueva «Tabla de marchas» (una tabla por transmisión), con restablecer al reparto del modelo simple.
- **Modelo físico (F6.3):** con la cadencia, la relación de la marcha, los pesos y el terreno de Zwift (pendiente, rodadura y viento), calcula la potencia que costaría pedalear y la fija en el Neo. Con protecciones: suavizado de cadencia y pendiente, potencia mínima, alivio con cadencia baja y cambios de potencia con rampa, salvo al cambiar de marcha.
- **Dificultad global (F6.4):** ajuste de 50 % a 150 % (100 % por defecto) que endurece o suaviza los tres modelos.
- **Ajustes nuevos:** «Modelo de resistencia», «Dificultad», «Potencia mínima» (F12.5) en Rodillo, y «Peso de la bici» en Ciclista.
- **Velocidad y distancia en Rodaje:** dos datos nuevos de «Datos en pantalla» (de 8 posibles, máximo 6 a la vez), con la velocidad que calcula el modelo físico. Con los otros modelos muestran «—». **BREAKING (menor):** `DatoRodaje` crece; una versión anterior de la app descarta los datos nuevos al leer la configuración.
- **Diagnóstico** muestra el modelo en uso y, con el físico, la potencia calculada y la velocidad.
- **Maqueta primero** (AGENTS.md): el change pasa por la maqueta de Ajustes, «Tabla de marchas», Datos en pantalla y Rodaje antes de tocar la app.

Fuera de alcance:

- El ERG de Zwift y el ERG manual no cambian: no pasan por el modelo ni por la dificultad.
- Calibrar el modelo físico con una ruta real o enviar a Zwift la velocidad calculada (la velocidad que se envía hoy por el puente no cambia). Eso es de `experimentos-zwift-y-neo` (17).
- Tabla editable por tramos, importar o exportar tablas, curvas de aire distintas por postura o tipo de rueda.
- Elegir el modelo predeterminado definitivo: lo decide el usuario tras la prueba con el equipo real; este change deja «Simple» como defecto.
- Cambios en la protección de bajadas del 7b salvo su aplicación a los nuevos modelos.

## Capabilities

### New Capabilities
- `resistance-models`: cómo cada modelo (simple, por tabla y físico) traduce marcha, terreno y cadencia en resistencia; la dificultad global; las protecciones del físico; velocidad y distancia calculadas; y la acción de alternar de modelo.

### Modified Capabilities
- `settings-screen`: modelo de resistencia, dificultad y potencia mínima en Rodillo, peso de la bici en Ciclista, pantalla «Tabla de marchas», y Datos en pantalla con 8 datos posibles (máximo 6).
- `ride-screen`: velocidad y distancia como datos de Rodaje, con su comportamiento sin modelo físico o sin Neo.
- `button-mapping`: acción asignable «Siguiente modelo».
- `diagnostics`: modelo en uso, potencia calculada y velocidad en la simulación.
- `screen-mockups`: maquetas de Ajustes, «Tabla de marchas», Datos en pantalla y Rodaje con los nuevos elementos.

## Impact

- **Código:** `:nucleo` (`dominio/Resistencia.kt` con `ModeloTabla` y `ModeloFisico`; `ControlRodillo` con protecciones, dificultad y entrada de cadencia; `Configuracion` con `modelo`, `dificultadPct`, `potenciaMinimaW`, `pesoBiciKg`, `tablas` y los `DatoRodaje` nuevos; `Accion.SiguienteModelo`); `:app` (`Sesion`, ajustes, pantalla «Tabla de marchas», datos y Rodaje, Diagnóstico).
- **Sistema de diseño:** posible componente para editar la tabla (fila con stepper por marcha); se decide en el `design.md` y se da de alta en `design-system/`.
- **Dependencias nuevas:** ninguna.
- **Hardware:** se valida con el Neo y Zwift: el físico depende de que el Neo mantenga la potencia pedida en modo 0x31 mientras cambia la cadencia (a validar).
- **Documentación:** cambian `funcional.md` (F6.2–F6.4, F11.3, F12.5, decisiones 2, 12 y abierta 8), `arquitectura.md` (§ 5.4, 5.5, 10, 15), `planificacion.md` (fila 15 y decisiones pendientes) y `desarrollo.md` solo si cambia la forma de probar.
