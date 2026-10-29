# Proposal

## Why

La sección «Conexiones» de Diagnóstico repite lo que ya dicen las pestañas **Conexión** (estado de Zwift y del controlador, con su ayuda) y **Dispositivos** (estado del Neo, el Square, la banda y el ventilador, «Buscar dispositivos» y los avisos de Bluetooth y permisos). Diagnóstico sirve para ver lo que pasa por debajo, no para duplicar el estado. Además, cada cambio de conexión obliga hoy a mantener el mismo texto en tres pantallas (F12.1, F13).

## What Changes

- **«Conexiones» pasa a «Datos de conexión»**, una sección corta con solo los datos que no se ven en otra pantalla:
  - App de entrenamiento (Zwift, Rouvy u otra): la dirección de red desde la que se ha conectado y el nombre con el que la app se anuncia, sin nombrar a ninguna plataforma.
  - Square: el nivel de batería (o «sin lectura todavía», o la última conocida sin conexión), con «baja» si lo está.
  - Pulsómetro, si hay uno recordado: el último pulso y hace cuántos segundos se leyó.
  - Ventilador, si hay uno recordado: modo, velocidad y hace cuánto fue su última notificación.
- **La simulación de Zwift** (pendiente, coeficientes, masas y relación de su marcha) pasa a la sección Simulación de Diagnóstico, junto a lo que simula el Neo: es un dato de simulación, no de conexión.
- **El aviso de batería baja del Square** pasa a Dispositivos, en la sección Controlador (decidido por el usuario al validar la maqueta; era el único aviso de la app). Diagnóstico conserva «15 % · baja» en la fila de batería.
- **La maqueta de Dispositivos** gana los estados que le faltaban frente a su spec: Neo no encontrado, sin permiso de Bluetooth y Square con la batería baja. La app ya muestra los dos primeros.
- **«Rodillo» en vez de «Neo» en los textos de Diagnóstico** (decidido por el usuario al validar la maqueta): «Enviada al rodillo», «Control manual del rodillo», el origen «Rodillo» y «→ Rodillo» en el tráfico, y los textos de ayuda. El modelo «Tacx Neo» solo aparece como nombre de hardware, y el registro de sesión no cambia.
- **Se quitan de Diagnóstico**: las filas de estado del Neo, el Square, Zwift y el controlador; el botón «Buscar dispositivos»; los avisos de Neo no encontrado, Square, banda y ventilador reconectando, Bluetooth apagado, sin permiso, sin wifi y batería baja; y las ayudas de emparejamiento. Todo eso queda en Conexión y Dispositivos.
- **Maqueta de Diagnóstico**: se redibuja la sección y se conservan los estados que otras secciones necesitan (botones del Square, simulación, control manual). Los estados que solo existían para los avisos de conexión se simplifican.
- **Etapas.** Cambio con impacto visual: etapa 1, maqueta y puerta de validación del usuario; etapa 2, la app.

Fuera de alcance:

- Cambiar Conexión, Dispositivos, Rodaje o las demás secciones de Diagnóstico (botones, simulación, tráfico, control manual).
- El tráfico en crudo, el registro de sesión y los datos que se registran: no cambian.
- El resto del refactor de Diagnóstico para ser agnóstico a la plataforma (otro change, sin planificar, citado en el 27). Aquí solo se hacen agnósticas las filas de «Datos de conexión»; la simulación de Zwift conserva su nombre porque es un dato de su protocolo.
- «Neo» en las demás pantallas (Dispositivos, Conexión, Rodaje, notificación) y en el registro de sesión: solo cambia Diagnóstico. Si se quiere el mismo criterio en todas, es otro change.
- Otros avisos informativos de Dispositivos (reconectando) o de Conexión: ya existen en sus maquetas y no cambian.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities
- `diagnostics`: la sección Conexiones se sustituye por «Datos de conexión» (solo datos, sin estado ni acciones); se retiran los requisitos de las filas de estado y los avisos, y los datos de la app de entrenamiento, batería, pulso y ventilador pasan a la sección nueva y la simulación de Zwift a la sección Simulación.
- `screen-mockups`: la maqueta de Diagnóstico refleja la sección nueva y la simulación de Zwift en su sitio; la de Dispositivos suma los estados Neo no encontrado, sin permiso y Square con la batería baja.
- `platform-bridge`: sin wifi, el aviso ya no se explica en Diagnóstico sino en Conexión.
- `trainer-manual-control`: los textos del control manual dicen «rodillo», no «Neo».
- `device-connection`: Dispositivos avisa de la batería baja del Square en la sección Controlador.
- `square-battery`: la batería baja se avisa en Dispositivos y Diagnóstico solo dice «baja» junto al nivel.

## Impact

- **Código.** `:app` `ui/` (pantalla de Diagnóstico y su estado de UI: quitar filas, avisos y la acción de buscar y mover la simulación de Zwift; Dispositivos: el aviso de batería baja, y `avisosDeConexion` y `accionDeConexiones`, hoy en `ui/diagnostico/`, pasan a vivir en Dispositivos porque Diagnóstico deja de usarlos), textos de `strings`, y tests de la pantalla (unitarios y de instrumentación). Sin cambios en `:core` ni en BLE.
- **Maqueta y sistema de diseño.** `maquetas/diagnostico.html`, `maquetas/dispositivos.html` y `maquetas/index.html`; no hay componentes nuevos (se reutilizan `sq-list` y `sq-item`). Se regeneran las capturas de la web si `site/capturas.json` usa Diagnóstico.
- **Documentación.** `docs/funcional.md` (F13.1, F13.3), `docs/planificacion.md` (fila 28); `arquitectura.md` y `desarrollo.md`, solo si algo cambia.
