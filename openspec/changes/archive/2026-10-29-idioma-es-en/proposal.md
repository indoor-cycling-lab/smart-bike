# Proposal

## Why

La interfaz de la app solo existe en español (NF7 prevé «opcionalmente en inglés»). Quien no lee español no puede usarla, y la publicación del repositorio (change 20) la abre a un público que no tiene por qué leerlo. Los textos ya están en `strings.xml`, así que el coste es la traducción y unos pocos formatos fijados a español, no una reestructuración.

## What Changes

- Nuevo ajuste «Idioma» en la sección «Pantalla» de Ajustes, con dos opciones excluyentes: «Español» y «English». Cada opción se escribe siempre en su propio idioma. Se aplica al momento a toda la app, también a Rodaje, sin terminar la sesión, cambiar la marcha ni cortar ninguna conexión.
- Primer arranque: si el idioma del móvil es el español, la app se muestra en español; en cualquier otro caso, en inglés. El resultado se guarda y desde ese momento la elección es del usuario: la app ya no sigue al idioma del móvil.
- Una configuración guardada antes de este change, sin idioma, recibe la misma detección. Una copia de seguridad con idioma lo conserva al importarla; una anterior al ajuste se trata como un primer arranque.
- La interfaz se traduce al inglés (`values-en/strings.xml`): pantallas, avisos, diálogos, descripciones accesibles y la notificación de la sesión en primer plano.
- Los formatos de lo que se ve siguen al idioma elegido: separador decimal («12,5 km/h» / «12.5 km/h»), la fecha de las sesiones grabadas («29 oct 2026 · 18:42» / «29 Oct 2026 · 18:42») y el tamaño de archivo. Hoy están fijados a español en el código.
- Se actualizan `AGENTS.md` (la regla «textos de la UI en español» pasa a «en español, con la traducción al inglés en `values-en`») y NF7.

## Capabilities

### New Capabilities

- `app-language`: idioma de la interfaz: lenguas admitidas, detección en el primer arranque, elección persistente del usuario, aplicación al momento y formatos que siguen al idioma. Es una capacidad propia porque el comportamiento cruza Ajustes, la notificación, la copia de seguridad y todas las pantallas.

### Modified Capabilities

- `settings-screen`: el ajuste «Idioma» en la sección «Pantalla» y el orden de las secciones.
- `settings-backup`: el idioma entra en lo que se exporta e importa.
- `screen-mockups`: la maqueta de Ajustes con el ajuste «Idioma» y su estado en inglés.

## Impact

- **Fuera de alcance**:
  - La documentación (`docs/`, specs, `design.md`) y los comentarios del código siguen en español.
  - Las líneas del registro de Diagnóstico (`registro.suceso(...)`) y sus trazas siguen en español: es material técnico que leen personas y agentes con ayuda de la IA.
  - Los nombres y formatos de archivo de las sesiones (`sesion-yyyyMMdd-HHmmss`) y el contenido de los archivos exportados.
  - La web pública, que ya es bilingüe, y sus capturas (seguimiento aparte).
  - Más idiomas que español e inglés.
  - El idioma del sistema de las pantallas de Android (selector de archivos, permisos).
  - Traducir las maquetas: siguen en español, salvo el control del ajuste «Idioma».
- **Código**:
  - `:core`: `Configuracion` (campo `idioma`, versión, migración y normalización) y el tipo `IdiomaApp`.
  - `:app`: `AppGraph`, `MainActivity`, `RideService`, `AjustesViewModel` y `AjustesUiState`, `Ajustes.kt`, y los formatos de `Tabla.kt`, `RodajeUiState.kt`, `Diagnostico.kt` y `TextosSesiones.kt`.
  - `strings.xml` y un nuevo `values-en/strings.xml`.
  - Tests unitarios y de instrumentación.
- **Sistema de diseño y maquetas**: `maquetas/ajustes.html` e índice. Sin tokens ni componentes nuevos: el ajuste reutiliza el grupo de opciones de «Tema».
- **Documentos**: `docs/funcional.md` (NF7 cumplida y F12.8, que suma el idioma), `docs/arquitectura.md` (NF7 y la decisión de cómo se aplica el idioma), `docs/planificacion.md` (fila del change) y `AGENTS.md`. `docs/desarrollo.md` solo si describe cómo añadir textos.
- **Orden**: no depende de ningún change abierto. Conviene antes de `publicar-repo-publico` para que el repositorio salga ya bilingüe.
- **Prueba con el equipo**: en el OnePlus 8, con el móvil en español y en inglés, instalar de cero y comprobar el idioma del primer arranque; cambiarlo rodando con el Neo y el Square conectados sin perder la conexión ni la marcha; ver la notificación de la sesión en los dos idiomas; y comprobar que ningún texto inglés se corta en 360 dp.
