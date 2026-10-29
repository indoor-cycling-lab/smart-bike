## 1. Maqueta

- [x] 1.1 Cambiar `.sq-opt input:checked` en `design-system/components.css`: aro y punto en `var(--gear)`
- [x] 1.2 Actualizar la descripción del radio en `design-system/components/SettingsList/README.md` y `ModeList/README.md` (aro y punto `gear`, no aro `ink` y punto `gear-fill`)
- [x] 1.3 Regenerar con `python3 scripts/design-system/generar.py` y comprobar el contraste en los dos temas (sin pares nuevos)
- [x] 1.4 Revisar que `maquetas/conexion.html`, `ajustes.html` y `marchas.html` muestran la opción elegida con el cambio, sin tocar su estructura
- [x] 1.5 Validación de la maqueta por el usuario: enlaces `maquetas/conexion.html` y `maquetas/ajustes.html` y las secciones SettingsList y ModeList de la guía, en tema claro y oscuro (visto ya en una primera versión; falta la aprobación explícita)

## 2. App

- [x] 2.1 En `SettingsOpcion` (`SettingsList.kt`), aro `c.gear` al elegir y punto `c.gear`, y comprobar que la opción deshabilitada elegida mantiene aro y punto atenuados
- [x] 2.2 Ejecutar los tests de Ajustes y Conexión que seleccionan opciones (`./gradlew :app:testDebugUnitTest`) y `./gradlew ktlintCheck`
- [x] 2.3 Regenerar las capturas de la web con `python3 scripts/sitio/capturas.py` y revisar las que muestran una opción elegida
- [x] 2.4 Probar en el OnePlus 8, en tema claro y oscuro, Conexión (modos) y Ajustes (tema, idioma, orientación y transmisión)

## 3. Documentación

- [x] 3.1 Actualizar la documentación: revisar `docs/funcional.md`, `docs/arquitectura.md`, `docs/planificacion.md` y `docs/desarrollo.md`; la fila del change (38) y su nota en `planificacion.md` se actualizan siempre
