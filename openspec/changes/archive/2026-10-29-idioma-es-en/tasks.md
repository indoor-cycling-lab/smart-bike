# Tasks

## 1. Maqueta

- [x] 1.1 En `maquetas/ajustes.html`, añadir a la sección «Pantalla», encima de «Tema», el ajuste «Idioma · Language» con las opciones «Español» (elegida) y «English», con el mismo grupo de opciones que «Tema» (sin componente nuevo), y el estado `ingles` con «English» elegido (`data-mock-states`, `data-mock-state`). Verificar abriendo `maquetas/ajustes.html` y `maquetas/ajustes.html#estado=ingles` en los dos temas, y que no cambia ningún otro estado.
- [x] 1.2 Comprobar que el grupo de opciones usado es el mismo de «Tema» y «Orientación» en la guía (`design-system/styleguide/index.html`) y que no hace falta nada nuevo en `design-system/`. Verificar con `python3 scripts/design-system/generar.py` sin cambios en `tokens.css` ni en la guía (`git status`).
- [x] 1.3 Actualizar `maquetas/index.html` con el estado nuevo y comprobar que todos los enlaces de la lista abren un estado que existe.
- [x] 1.4 Validación de la maqueta por el usuario: dar los enlaces exactos (`maquetas/ajustes.html` y `maquetas/ajustes.html#estado=ingles`) y esperar la aprobación explícita antes de seguir con «App». Si pide cambios, ajustar la maqueta, `design.md` y las specs, y volver a pedir validación.

## 2. Idioma en el dominio y en el almacén

- [x] 2.1 En `:core`, `IdiomaApp` (`ES`, `EN`) con `IdiomaApp.deLenguaje(codigo)` y `Configuracion.idioma: IdiomaApp?` (opcional, `null` sin decidir), `VERSION` a 12, normalización y exportación/importación (un archivo sin `idioma` conserva el de la app). Tests en `:core`: detección (`es`, `es-MX`, `en`, `fr`, vacío), documento anterior sin `idioma`, ida y vuelta de la copia y compatibilidad de un documento con `idioma` leído sin él. Verificar con `./gradlew :core:test`.
- [x] 2.2 En `AppGraph`, `idioma: StateFlow<IdiomaApp?>` (`null` hasta la primera lectura): si la configuración no trae idioma, lo detecta con `Locale.getDefault().language`, lo guarda y lo emite. Test unitario con un `Almacen` falso: instalación limpia con móvil `es` y `fr`, y configuración anterior sin idioma. Verificar con `./gradlew :app:testDebugUnitTest`.
- [x] 2.3 En `AjustesViewModel` y `AjustesUiState`, `idioma(i: IdiomaApp)` y el campo en el estado, igual que `tema`. Test del `ViewModel`: elegir un idioma lo guarda y lo emite. Verificar con `./gradlew :app:testDebugUnitTest`.

## 3. Aplicar el idioma

- [x] 3.1 Helper `Context.conIdioma(IdiomaApp)` (`createConfigurationContext` con el `Locale`) y `IdiomaElegido` (composable) que provee `LocalContext` y `LocalConfiguration` con un `ContextWrapper` de la actividad, recordado por idioma; `MainActivity` lo aplica sobre el contenido junto a `SqTemaElegido` y no pinta nada hasta conocer el idioma. Test de instrumentación: con `ES` y con `EN` la misma pantalla de prueba dice «Ajustes»/«Settings», un plural dice «+1 marcha»/«+1 gear», y `LocalContext.current.actividad()` sigue devolviendo la actividad. Verificar con los tests de instrumentación en el OnePlus 8.
- [ ] 3.2 `RideService`: usar `conIdioma` para el canal y la notificación, recomponerla al cambiar el idioma (clave de `distinctUntilChanged`) y recrear el canal; sin idioma leído, el del sistema. Test unitario de la clave y del texto de la notificación en los dos idiomas. Verificar en el OnePlus 8 viendo la notificación en español y en inglés al cambiar el idioma rodando.
- [x] 3.3 Pasar el `Locale` del idioma a los formatos: `RodajeUiState`, `Tabla.kt`, `Diagnostico.kt` y `TextosSesiones.kt` (meses con `DateTimeFormatter` y el locale, tamaño con separador decimal del locale). Tests: cada formato en `es` y `en` según los escenarios de `app-language` («24,5 km/h»/«24.5 km/h», «29 oct 2026 · 18:42»/«29 Oct 2026 · 18:42», «1,2 MB»/«1.2 MB», «−3,5 %»/«−3.5 %»), comprobando el punto tras el mes en el JDK de Android. Verificar con `./gradlew :app:testDebugUnitTest` y `grep -rn 'forLanguageTag("es")' app/src/main` = 0.

## 4. Textos en inglés

- [ ] 4.1 Fijar el glosario de `design.md` (decisión 6) y crear `values-en/strings.xml` con la traducción de todas las cadenas y plurales de `strings.xml`, marcando `translatable="false"` el nombre de la app y las dos opciones «Español»/«English». Test unitario que compara las claves y los marcadores de formato de `values/` y `values-en/` y que falla si falta o sobra alguna. Verificar con `./gradlew :app:testDebugUnitTest`; el usuario revisa el archivo entero.
- [x] 4.2 Añadir las cadenas del ajuste «Idioma · Language» y del aviso de cambio a los dos archivos. Verificar con el test de 4.1.

## 5. Ajustes

- [x] 5.1 En `Ajustes.kt`, el ajuste «Idioma · Language» en la sección «Pantalla» encima de «Tema», con el mismo grupo de opciones y la marca de selección, reproduciendo la maqueta aprobada. Tests de instrumentación (`AjustesTest`): valor por defecto según el idioma del arranque, elegir «English» cambia Ajustes a inglés al momento, la opción elegida lleva la marca y el lector anuncia cuál, el objetivo táctil es toda la fila, y el orden «Idioma · Language», «Tema», «Orientación». Verificar con `./gradlew :app:connectedDebugAndroidTest --tests '*AjustesTest'`.
- [x] 5.2 Aplicar el cambio rodando sin tocar la sesión: test (unitario o de instrumentación) con una sesión simulada en la marcha 14 con Neo y Square conectados que cambie el idioma y compruebe que la marcha, los enlaces y la resistencia enviada no cambian. Verificar con el test.
- [x] 5.3 Copia de seguridad: exportar escribe el idioma e importar lo aplica al momento (archivo con `EN`) o conserva el actual (archivo anterior al ajuste). Tests de `CopiaConfiguracion` para los dos casos. Verificar con `./gradlew :app:testDebugUnitTest`.
- [x] 5.4 Fijar el idioma en los tests de instrumentación que parten de una instalación limpia (decisión 9) para que no dependan del móvil. Verificar con la batería completa de instrumentación en el OnePlus 8.

## 6. Prueba con el equipo y ajuste de textos

- [ ] 6.1 Instalar con `desplegar-movil` en el OnePlus 8 y recorrer todas las pantallas (Rodaje en vertical y horizontal, Conexión, Dispositivos, Ajustes, Marchas, Datos, Tabla, Diagnóstico y Sesiones) en inglés a 360 dp y con la fuente al 130 %: ningún texto cortado ni objetivo táctil tapado. Corregir las traducciones o los anchos que fallen y anotar el resultado en `docs/planificacion.md`.
- [ ] 6.2 Probar el primer arranque desinstalando e instalando con el móvil en español y en inglés, y el cambio de idioma rodando con el Neo y el Square conectados (sin perder la conexión ni la marcha). Anotar lo que no se pueda probar como pendiente.
- [x] 6.3 Comprobar con `./gradlew ktlintCheck` y `./gradlew :app:testDebugUnitTest` y la instrumentación completa que todo pasa.

## 7. Documentación

- [x] 7.1 Actualizar la documentación: `docs/funcional.md` (NF7 cumplida y F12.8 con el idioma), `docs/arquitectura.md` (NF7 y la decisión de cómo se aplica el idioma), `docs/planificacion.md` (fila del change «En curso» al empezar y «Archivado» al archivarlo, con la prueba pendiente si la hay), `docs/desarrollo.md` (cómo añadir un texto: en los dos `strings.xml`, con el test de claves), y `AGENTS.md` (los textos de la UI van en español y en inglés). Verificar con `grep -rn "NF7\|solo en español" docs AGENTS.md` y leyendo la fila del change en `docs/planificacion.md`.
