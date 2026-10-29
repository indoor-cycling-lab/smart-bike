# Proposal

## Why

El repositorio es privado y se quiere abrir al público como proyecto personal (README y licencia GPL-3.0 ya preparados). Una revisión de privacidad del 27-10-2026 encontró, sin credenciales ni claves, datos personales que no conviene publicar: el correo y el nombre del autor en los 86 commits, identificadores reales de los dispositivos (direcciones Bluetooth del rodillo y del mando, el nombre del rodillo, que lleva parte de su número de serie, y la identidad que la app genera ante Zwift), las direcciones de la red local del móvil y del PC, y detalles del móvil y de las apps instaladas. Hacerlo público tal cual publicaría todo el historial, y esos datos están repartidos por muchos commits. Es más fiable sanear el código actual y publicar con un historial nuevo que intentar limpiar commit a commit.

## What Changes

- Se **sanean los datos personales** del árbol actual sin cambiar el comportamiento de la app: las direcciones y el nombre del rodillo en las capturas de prueba (`core/src/test/resources/trazas/`), en los tests, en la maqueta de Dispositivos y en la documentación; las direcciones de red locales de tests, maquetas y documentos; el nombre real del ventilador en los documentos; y la lista de apps del móvil de `docs/desarrollo.md`. Los valores reales se sustituyen por valores de ejemplo evidentes.
- Los changes archivados (`openspec/changes/archive/`) se tocan solo en esos identificadores concretos y en nada más: son el registro de lo hecho, pero los datos personales no se publican.
- Se **crea un repositorio público nuevo con un único commit inicial** a partir del árbol saneado (solo los ficheros rastreados), con el correo `noreply` de GitHub y sin enlaces a sesiones.
- El repositorio privado actual, con todo su historial, se **conserva como copia de seguridad privada** con otro nombre. El público pasa a ser el repositorio de trabajo.
- Se comprueba el resultado: escaneo de patrones de datos personales sobre el árbol y sobre el historial del repositorio nuevo, y compilación y tests desde un clon limpio.

Queda fuera de alcance:

- Cambios de funcionalidad, de pantallas o de diseño.
- Reescribir el historial del repositorio privado: se deja como está, privado.
- Quitar el nombre del autor (aparece en `LICENSE` y en el README, por decisión del usuario) y la mención de que el proyecto se ha desarrollado con ayuda de IA.
- Valorar los aspectos legales de documentar protocolos no oficiales (ingeniería inversa de Wahoo y Zwift), que es decisión del usuario.
- Configurar integración continua, plantillas de issues o de pull requests y políticas de contribución.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es preparación del repositorio sin efecto observable en la app, así que el change declara `skip_specs: true`.

## Impact

- **Código y tests:** cambian solo cadenas de ejemplo (direcciones, nombres de dispositivo, IPs) en ficheros de test y de recursos de test, sin tocar lógica. `NeoClient.kt` cambia un comentario.
- **Documentos de `docs/`:** cambian `arquitectura.md` (nombres de dispositivos reales), `desarrollo.md` (apps del móvil y comandos de ejemplo), `planificacion.md` (fila del change y nombre del ventilador). `funcional.md`: ninguno. También se revisan `README.md` y `AGENTS.md`.
- **Repositorio:** el actual se renombra y queda privado como copia; se crea uno público con el nombre `smart-bike`. Cambia la URL de clonado del repositorio de trabajo (sigue siendo la que ya figura en el README) y la carpeta local de trabajo.
- **Equipo real:** no aplica; no toca BLE ni Zwift. La comprobación es compilar y pasar los tests unitarios, y que el escaneo no encuentre datos personales.
- **Orden:** depende de `renombrar-smart-bike` (change 18, archivado). Puede hacerse antes o después de `anadir-linter-kotlin`; si va antes, el linter se aplicará ya en el repositorio público.
- **Acciones fuera del repositorio** (renombrar el repo actual, crear y publicar el nuevo, ajustes de privacidad de la cuenta): las confirma y, cuando corresponde, las hace el usuario; ninguna se ejecuta sin su visto bueno expreso.
