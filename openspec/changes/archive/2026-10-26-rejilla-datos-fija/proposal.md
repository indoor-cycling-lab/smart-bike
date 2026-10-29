## Why

En Rodaje, la rejilla de datos ocupa lo que sobra después de la marcha, el pie y los botones − y +. Al activar los botones cambian su área y, a veces, el tamaño de su cifra, y no siempre en el mismo sentido. En vertical con la Shimano, la compactación que se aplica con los botones deja a los datos *más* alto que sin ellos (unos 336 px frente a 293 px a 390 × 844). En horizontal, la columna de botones le quita a la de datos unos 45 px de ancho, y con valores largos la cifra encoge. Además, el tamaño de la cifra se mide con el valor que se ve en cada momento, así que puede bajar en plena salida al pasar, por ejemplo, de «999 W» a «1000 W». Lo que se lee al rodar debe estar siempre en el mismo sitio y con el mismo tamaño.

## What Changes

- **Área de datos fija.** En vertical, la rejilla ocupa el 40 % del alto útil de la pantalla; en horizontal, el 40 % del ancho útil. No depende de la transmisión, del modo (SIM o ERG) ni del aviso del pie.
- **La marcha es la zona flexible.** Ocupa el resto y su cifra (o plato × piñón, o la potencia objetivo en ERG) se escala para caber, sin pasar de su tamaño actual. Desaparece la compactación fija de vertical con botones (cifra al 76 %, cassette de 40 px, separación reducida): la marcha se reduce solo lo que haga falta.
- **Los botones − y + toman primero el espacio que le sobra a la rejilla.** Sin bajar el tamaño de la cifra de los datos, la rejilla cede su holgura (alto en vertical, ancho en horizontal); lo que falte lo pone la zona de la marcha. Con 1 o 2 filas, la rejilla suele absorber todo el hueco y la marcha no cambia. La cifra de los datos es la misma con y sin botones.
- **La cifra de los datos llena la celda.** Es la mayor que cabe en la celda del área fija, con un máximo de 144 px, y sustituye a la tabla de tamaños por número de datos (que además no coincidía entre la maqueta y la app con 3 a 6 datos en vertical).
- **La cifra se mide con el peor valor de cada dato** («8888 W», «188 rpm», «8:88:88», «88,8»), no con el que se ve. El tamaño se fija al abrir Rodaje o al cambiar los datos y no cambia mientras se rueda.
- **Número impar de datos: el último va centrado** en su fila, ocupando las dos columnas, para que no quede un hueco a su derecha (3 y 5 datos en vertical, 5 en horizontal).
- **Unidad alineada.** La unidad («W», «rpm») deja de alargar el valor. Hoy las filas con unidad quedan más altas que las que no la tienen, y la etiqueta no está a la misma distancia del borde en todas las filas. Este fallo está en la maqueta.

Fuera de alcance:
- Cambiar qué datos se pueden elegir, su orden, sus valores o «Datos en pantalla» en Ajustes.
- Poner 2 datos en una columna en vertical (se valoró y se descarta: siguen en una fila de 2 columnas).
- Cambiar la posición de los botones (columna a la derecha en horizontal, fila al pie en vertical), su tamaño o lo que hacen.
- Cambiar la barra superior, los chips o el pie.
- La miniatura de «Datos en pantalla» (`sq-mgrid--mini`), que sigue con sus tamaños fijos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: «Disposición en vertical» pasa de datos que «ocupan el resto del alto» a un área fija con la marcha como zona flexible. «Datos elegidos en Rodaje» añade el centrado del último dato impar y la cifra estable durante la salida. «Botones − y + en Rodaje» añade que activarlos no cambia la cifra de los datos y que el espacio sale primero de la holgura de la rejilla.
- `ui-components`: «Rejilla de datos sin desplazamiento» pasa de tamaños por escalones a la cifra que llena la celda (máximo 144), medida con el peor valor de cada dato, con el último impar centrado y todas las filas alineadas igual.
- `screen-mockups`: «Pantalla de rodaje» y «Pantalla de rodaje en vertical» reflejan el área fija, los estados con y sin botones de 1 a 6 datos y el centrado del último dato impar.

## Impact

- Sistema de diseño: `design-system/components.css` (`sq-ride`, `sq-ride--v`, `sq-ride__main`, `sq-mgrid`, `.sq-unit` dentro de `sq-metric-val`), `design-system/components/MetricGrid/README.md` y `preview.html`, `design-system/components/RideScreen/README.md` y guía regenerada.
- Maquetas: `maquetas/rodaje.html` y `maquetas/rodaje-vertical.html` (área fija, estados de 1 a 6 datos con y sin botones, último impar centrado); el cálculo de la cifra pasa a un script compartido de las maquetas.
- App: `ui/componentes/MetricGrid.kt` (cifra por celda con el peor valor, último impar centrado, unidad alineada), `ui/rodaje/Rodaje.kt` (área fija, marcha escalable en lugar de `ESCALA_COMPACTA`, reparto del hueco de los botones), `ui/componentes/GearDisplay.kt` y `ErgTarget.kt` (escala para caber), `ui/datos/` (valor más largo de cada dato) y sus tests (`RodajeTest`, `ComponentesTest`).
- Funcionalidades: F11.3 (tamaño de los datos) y F11.8 (botones − y +), en horizontal y en vertical.
- Prueba con el equipo real: en el OnePlus 8, en vertical y en horizontal, activar y desactivar los botones con 2, 5 y 6 datos y ver que los datos no cambian de tamaño; rodar pasando de 999 a 1000 W (o del minuto 59 a la hora) sin que la cifra se mueva.
- Documentos de `docs/`: `planificacion.md` (fila nueva 10d del change), `funcional.md` (nota en F11 sobre el área fija), `arquitectura.md` (la regla de `MetricGrid` de medir la cifra más larga). `desarrollo.md` se revisa al final; no se esperan cambios en él.
