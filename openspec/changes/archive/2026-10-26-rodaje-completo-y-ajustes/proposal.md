## Why

Hoy la app solo tiene Rodaje como pantalla principal. Botones, Ajustes (solo con «Bajadas») y Marchas se abren desde Diagnóstico de forma provisional, Rodaje enseña siempre potencia y cadencia, y no hay forma de olvidar un dispositivo ni de cambiar la pendiente sin Zwift. Con las marchas, los gestos, las transmisiones y el ERG manual ya archivados (changes 7, 7b, 8 y 9), falta la estructura final de la app: la barra inferior, Ajustes completos y la pantalla de rodaje a gusto del usuario (F11 completa y el resto de F12).

Es el change 10 de `docs/planificacion.md` (fase 3), partido en dos al proponerlo por decisión del usuario: este lleva la navegación, Ajustes, Dispositivos y «Al rodar»; el 10b se queda con exportar e importar la configuración, la batería del Square y el aviso de giro.

## What Changes

- **Barra inferior** con Rodar, Dispositivos, Botones y Ajustes. Rodaje sigue siendo inmersiva, sin barra; las otras tres pestañas la muestran y no tienen botón de volver. Atrás desde una pestaña lleva a Rodaje, y Atrás desde Rodaje sigue terminando la sesión.
- **BREAKING** respecto a `ride-screen` y `diagnostics`: los chips de Rodaje abren **Dispositivos** (con la barra inferior) en lugar de Diagnóstico. Diagnóstico pasa a abrirse desde Ajustes, con volver a Ajustes, y deja de enlazar a Botones y Ajustes.
- **Pantalla anterior o siguiente con el Square (anexo A.2):** Campag. I + cruceta ← / → recorre las pestañas en orden circular, también desde Rodaje. Son dos acciones asignables nuevas, «Pantalla anterior» y «Pantalla siguiente», con esa regla en la asignación por defecto de la lineal y de la Shimano; las configuraciones guardadas la reciben si la entrada está libre.
- **Dispositivos (F12.1, F1.4):** Neo y Square con su estado y su ayuda para despertarlos, Zwift con su estado y cómo emparejarlo, «Buscar dispositivos» y «Olvidar» para cada dispositivo recordado.
- **Ajustes completos (F12):** entradas Marchas y Diagnóstico; «Al rodar»; «Ciclista» con el peso; «Rodillo» con «Bajadas» y «Pendiente sin Zwift» (F7.1, F12.5).
- **«Al rodar» (F12.7), con los valores por defecto de la decisión abierta 6, que este change cierra:**
  - «Datos en pantalla», con su pantalla: de 1 a 6 datos entre potencia, cadencia, tiempo, W/kg, potencia media y potencia máxima, ordenables, con vista previa (por defecto, potencia y cadencia).
  - «Botones − y + en pantalla» (desactivado): botones táctiles en Rodaje que hacen lo mismo que +1 y −1 marcha, o ±10 W en ERG (F11.8).
  - «Mostrar la acción» (activado): con él desactivado, Rodaje no muestra el aviso de la acción (F11.6); los topes y la ayuda para conectar se siguen viendo.
  - «Vibrar» (activado): con él desactivado, el móvil no vibra al cambiar, en los topes ni al reconocer un gesto (F3.6).
- **Datos de rodaje (F11.3):** la rejilla de Rodaje muestra los datos elegidos en su orden, con el tamaño ajustado al número. La potencia pasa a ser la media de 3 s. Tiempo, potencia media y máxima son de la sesión y solo cuentan mientras se pedalea; W/kg usa el peso del ciclista.
- **Decisión abierta 4, cerrada:** velocidad y distancia salen de la lista de datos. Con marchas virtuales, la velocidad del Neo depende solo de la cadencia y no coincide con la de Zwift; vuelven con el modelo físico (change 15), que la calcula.
- **Configuración versión 4:** datos en pantalla, botones − y +, peso del ciclista y la regla de pantalla anterior o siguiente.
- **Maqueta primero:** barra inferior, Rodaje sin velocidad ni distancia y con los botones − y +, Dispositivos, Botones y Ajustes como pestañas, Datos en pantalla y Diagnóstico desde Ajustes. Las valida el usuario antes de la app.
- **Prueba con el equipo real:** rodar con 1, 2 y 6 datos en las dos orientaciones, los botones − y + con el Neo, «Mostrar la acción» y «Vibrar» desactivados, olvidar y volver a buscar el Square, y recorrer las pestañas con el Square.

Fuera de alcance:

- Exportar e importar la configuración (F12.6), la batería del Square y el aviso de giro: change 10b.
- Velocidad y distancia (decisión abierta 4), peso de la bici y potencia mínima: change 15, con el modelo físico.
- Ajustes de la maqueta sin función en `funcional.md`: paso ERG configurable, tema, «Mantener la pantalla encendida» (Rodaje la mantiene siempre, F11.7) y aviso de cadencia baja en ERG. Salen de la maqueta.
- Elegir wifi o Bluetooth hacia Zwift en Dispositivos (change 11) y la sección del mando de Zwift (change 13).
- Modelo de resistencia y dificultad en Ajustes (change 15), y capas (F4.4, decisión 4).
- Reiniciar los datos de la sesión sin terminarla.

## Capabilities

### New Capabilities

- `app-navigation`: la barra inferior y sus pestañas, qué pantallas son secundarias y adónde vuelven, Atrás del sistema y la pantalla anterior o siguiente con el Square.

### Modified Capabilities

- `ride-screen`: datos configurables y su tamaño, potencia media de 3 s y datos de la sesión, botones − y +, aviso de la acción desactivable, y los chips abren Dispositivos (sustituye a «Diagnóstico desde los chips»).
- `settings-screen`: Ajustes como pestaña con todas sus secciones (sustituye a «Ajustes desde Diagnóstico»), «Al rodar», peso del ciclista, «Pendiente sin Zwift» y la pantalla Datos en pantalla.
- `device-connection`: pantalla de Dispositivos y olvidar un dispositivo recordado.
- `button-mapping`: Botones como pestaña (sustituye a «Botones desde Diagnóstico»), las acciones de pantalla anterior y siguiente, y su regla en la asignación por defecto.
- `button-gestures`: la vibración por gesto respeta «Vibrar».
- `virtual-gears`: la vibración en cada cambio y en los topes respeta «Vibrar».
- `trainer-resistance`: la pendiente sin Zwift es la de Ajustes, no siempre 0 %.
- `diagnostics`: Diagnóstico se abre desde Ajustes y vuelve a Ajustes, sin enlaces a Botones ni a Ajustes.
- `screen-mockups`: navegación, Rodaje, Dispositivos, Botones, Ajustes y Datos en pantalla según este change, sin los estados abiertos desde Diagnóstico.

## Impact

- **Código `:nucleo`:** `Configuracion` versión 4 con su migración (`datos`, `botonesEnPantalla`, `pesoCiclistaKg`); acciones `PantallaAnterior` y `PantallaSiguiente` en `Asignacion` con su regla por defecto; cálculo de los datos de la sesión (media de 3 s, tiempo pedaleando, media y máxima) como dominio puro.
- **Código `:app`:** navegación con barra inferior en `MainActivity`; pantallas nuevas `ui/dispositivos` y `ui/datos`; `ui/ajustes` completa; Botones y Diagnóstico sin los accesos provisionales; Rodaje con la rejilla configurable y los botones − y +; `Sesion` con olvidar, los datos de la sesión, la vibración condicionada y el evento de navegación; `Almacen` olvidando dispositivos en `estado.json`. Sin dependencias nuevas.
- **Maquetas y sistema de diseño:** `maquetas/rodaje.html`, `rodaje-vertical.html`, `dispositivos.html`, `botones.html`, `ajustes.html`, `datos.html`, `diagnostico.html`, `marchas.html` e `index.html`. Se reutilizan `BottomNav`, `MetricGrid`, `SettingsList`, `Stepper`, `ConnectionChip` y `Button`; si la fila ordenable de Datos necesita una variante, se añade antes a `design-system/`.
- **Documentos de `docs/`:**
  - `funcional.md`: F11.3 sin velocidad ni distancia; F12.1, F12.7 y la parte de F12.5 cumplidas; decisiones abiertas 4 y 6 resueltas; anexo A.2, pantalla anterior o siguiente ya asignada.
  - `arquitectura.md`: § 5.3, las acciones de App; § 9.2, la barra inferior, los chips a Dispositivos y Diagnóstico desde Ajustes; § 10, la versión 4 de `configuracion.json` y olvidar en `estado.json`.
  - `planificacion.md`: fila del 10 con su alcance reducido, fila nueva del 10b, y velocidad, distancia, peso de la bici y potencia mínima en el 15.
  - `desarrollo.md`: ninguno.
