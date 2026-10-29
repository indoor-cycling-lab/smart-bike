# Proposal

## Why

En un móvil bajo (360 × 640 dp) con los botones − y + activados y 6 datos, la marcha de Rodaje en vertical se reduce a 0 dp y desaparece: el test `RodajeTest.todo_visible_en_un_movil_pequeno_con_botones` falla y la maqueta reproduce lo mismo. Lo rompió `pestana-plataformas` (28-10-2026) al añadir la fila del estado de la plataforma, y rompe la promesa de `rejilla-datos-fija` de que a 360 × 640 todo se ve sin desplazar. Medido en la maqueta, con 4 datos y botones, o con 6 datos sin botones, la marcha vuelve a verse al 36 % de su tamaño, y al 72 % con un modo compacto. La marcha es lo primero que se lee al rodar, así que no puede desaparecer.

## What Changes

- **Modo compacto en vertical con la pantalla baja.** Cuando Rodaje en vertical mide menos de 760 dp de alto, el espacio entre bloques baja de 16 a 8 dp y el cassette de 56 a 40 dp (la escalera del modo lineal ya mide 28 dp y no cambia). En pantallas más altas no cambia nada.
- **Con la pantalla baja y los botones − y + activados, Rodaje muestra como máximo los 4 primeros datos elegidos.** Si hay 5 o 6, los sobrantes se ocultan en Rodaje; la configuración de «Datos en pantalla» no cambia, y al desactivar los botones, o en una pantalla más alta, vuelven a verse. Sin botones se siguen mostrando hasta 6.
- **Aviso en «Datos en pantalla».** Con la pantalla baja, los botones activados y más de 4 datos elegidos, la pantalla avisa de que Rodaje solo muestra los 4 primeros.
- **Maqueta y sistema de diseño.** Un tamaño de pantalla baja (360 × 640) en las maquetas de Rodaje con sus estados, la variante compacta en `RideScreen`, el aviso en la maqueta de «Datos en pantalla» y la regla de reparto en `maquetas/rodaje.js`. Es un cambio de disposición, así que pasa por validación de la maqueta antes de la app.
- **Tests.** El test de 360 × 640 pasa a comprobar los casos que la regla permite (4 datos con botones, 6 sin ellos) y los datos que se ocultan.

Queda fuera de alcance:

- Rodaje en horizontal y las pantallas con altura de 760 dp o más.
- Cambiar cuántos datos se pueden elegir (siguen siendo 6) o impedir en Ajustes la combinación de botones y más de 4 datos: el aviso informa, no bloquea.
- Reducir los botones − y + (siguen en `touch-ride`, 72 dp) y la cifra de los datos fuera del caso de arriba.
- Los demás tests de instrumentación que fallaban (ya corregidos, aparte).

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: nueva disposición compacta en vertical con la pantalla baja y el límite de 4 datos con los botones activados.
- `settings-screen`: «Datos en pantalla» avisa de los datos que Rodaje oculta en una pantalla baja con los botones activados.

## Impact

- **Código:** `ui/rodaje/Rodaje.kt` y `RodajeUiState.kt` (umbral, espacios, cassette y recorte de datos), y `ui/ajustes/` (aviso en «Datos en pantalla»); `strings.xml` para el aviso.
- **Sistema de diseño y maquetas:** `design-system/components.css` (variante `sq-ride--baja`), `RideScreen/README.md`, guía regenerada; `maquetas/rodaje-vertical.html`, `maquetas/rodaje.js`, `maquetas/revision.css`, `maquetas/datos.html` y `maquetas/index.html`; capturas de la web (`site/img/`) regeneradas.
- **Tests:** `RodajeTest`, `RodajeUiStateTest` y los de Ajustes («Datos en pantalla»).
- **Documentos de `docs/`:** `planificacion.md` (fila 25 y nota del change) y `funcional.md` (F11.3, datos en Rodaje, con su límite en pantallas bajas); `arquitectura.md` y `desarrollo.md`: ninguno.
- **Equipo real:** no toca BLE ni Zwift. Se comprueba en el OnePlus 8 con los tests de instrumentación; en inmersivo mide 764 dp de alto (medido el 28-10-2026), 4 dp por encima del umbral, así que el modo compacto no se activa en él salvo que cambie el tamaño de pantalla o de letra del sistema; se comprueba con los tests de instrumentación y la maqueta.
- **Orden:** parte de `pestana-plataformas` (change 23) y de `rejilla-datos-fija` (10d), ya archivados.
