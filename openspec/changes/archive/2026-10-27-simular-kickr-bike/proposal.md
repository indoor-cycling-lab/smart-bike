# Proposal

## Why

En la prueba de `mando-zwift-dircon` (27-10-2026, pregunta 8) Zwift trató el modo «Todo por wifi» como un rodillo con marchas virtuales, pero **avisa de que el rodillo no admite marchas virtuales** cuando se le mandan sus botones de cambio, incluso declarando capacidad de rodillo en la información del dispositivo. La hipótesis anotada es que Zwift reconoce los rodillos aptos por una lista de dispositivos («Zwift Ready»), no por lo que declaran. Otra app de código abierto se anuncia por DirCon con nombre de instancia `KICKR BIKE PRO …` (un modelo que Zwift sí conoce); su análisis no prueba que el nombre sea la causa, pero es la prueba más barata que tenemos y hoy solo se puede hacer recompilando.

El usuario quiere poder activar esa simulación desde Ajustes para probarla con el equipo real, sin tocar el modo por defecto. Su resultado decide si el change `marchas-de-zwift` (13c) es viable.

## What Changes

- **Ajuste «Simular un KICKR BIKE» en la sección «Zwift» de Ajustes**, desactivado por defecto. Con él activado, la app se anuncia por DirCon con el nombre de instancia `KICKR BIKE PRO` más cuatro caracteres hexadecimales estables (p. ej. `KICKR BIKE PRO 669C`) en vez de «Square Shift».
- **Aplica a los dos modos de conexión con Zwift** (rodillo por wifi con mando por Bluetooth, y todo por wifi). Solo cambia el nombre y la identidad con la que se anuncia el rodillo por DirCon; los servicios que se ofrecen, los datos y el comportamiento no cambian.
- **Identidad propia mientras esté activado.** Zwift recuerda los dispositivos por `mac-address` y `serial-number` (hallazgo de `mando-zwift-dircon`): al activar el ajuste, cada modo se anuncia con una identidad distinta de la de siempre, para que Zwift lo trate como un dispositivo nuevo y no arrastre el emparejamiento anterior. Al desactivarlo, vuelve la de antes.
- **Cambiarlo reanuncia.** Como al cambiar de modo, la app se retira y se vuelve a anunciar con lo nuevo, mantiene lo último que pidió Zwift durante los 30 s de siempre y no toca la marcha ni lo que simula el Neo. Zwift tiene que reconectar y emparejar de nuevo.
- **Dispositivos** nombra en su ayuda el dispositivo que hay que elegir en Zwift (hoy «Square Shift»; con el ajuste, el nombre simulado).
- **Diagnóstico** muestra el nombre con el que se anuncia la app, para comprobar en el registro y en pantalla qué ve Zwift.
- Pasa por maqueta: Ajustes (el interruptor y su ayuda), Dispositivos (el nombre en la ayuda) y Diagnóstico (el nombre anunciado).

**Fuera de alcance**
- Cambiar por defecto el nombre o la identidad: el ajuste nace desactivado y solo es una prueba.
- Cambiar los servicios que se anuncian en el TXT (`ble-service-uuids`) o los datos de Device Information (fabricante y modelo): solo cambia el nombre de instancia y la identidad. Si la prueba apunta a que hacen falta, es otro change (`design.md`, «Descartado por ahora», lo deja anotado con su orden).
- Elegir otros modelos o editar el nombre y la identidad a mano: un único modelo fijo (`KICKR BIKE PRO`).
- Que Zwift acepte las marchas virtuales (13c): este change solo da el medio de probar si el nombre influye; no promete el resultado.
- Hacerse pasar por un dispositivo de otra marca sin que el usuario lo pida: el ajuste es explícito y su ayuda dice que es una prueba.
- Copiar código de BikeControl o de qdomyos-zwift (NF6).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities
- `zwift-bridge`: nombre e identidad con los que la app se anuncia ante Zwift cuando se simula un KICKR BIKE, y cómo cambia con la sesión abierta.
- `settings-screen`: el interruptor «Simular un KICKR BIKE» en la sección «Zwift».
- `device-connection`: la ayuda del emparejamiento nombra el dispositivo que se anuncia.
- `diagnostics`: el nombre anunciado a Zwift.
- `screen-mockups`: los estados nuevos de las maquetas de Ajustes, Dispositivos y Diagnóstico.

## Impact

- **Dependencias entre changes:** parte de `mando-zwift-dircon` (13b), que añade la elección de modo, la identidad propia de cada modo y `Configuracion` versión 6. Se implementa después de que esos cambios estén en la rama, o junto a ellos.
- **Código:** `:nucleo` (`dominio/`: `simularKickrBike` en `Configuracion` versión 7 con migración desde la 6, y las identidades de la simulación en `EstadoGuardado`); `:app` (`zwift/`: el nombre de instancia deja de ser fijo en `DirconServer` y lo da la identidad; `ConexionZwift` expone qué identidad usa según el ajuste; `Sesion` y `AppGraph` reanuncian al cambiar; `Ajustes`, `Dispositivos` y `Diagnostico`).
- **Sistema de diseño y maquetas:** ningún componente ni token nuevos; cambios en `maquetas/ajustes.html`, `maquetas/dispositivos.html`, `maquetas/diagnostico.html` y `maquetas/index.html`.
- **Documentos de `docs/`:** `funcional.md` (F9, F12.1), `arquitectura.md` (§ 7.3, § 7.4 y la identidad ante Zwift), `planificacion.md` (fila del change), `desarrollo.md` solo si cambia la prueba con el equipo.
- **Equipo real:** hay que probar con Zwift en PC y el Neo si, con el nombre de KICKR BIKE, desaparece el aviso de marchas virtuales, cómo lo lista Zwift en el emparejamiento y que desactivar el ajuste devuelve el comportamiento de antes. Todo lo de Zwift queda «a validar» hasta entonces.
