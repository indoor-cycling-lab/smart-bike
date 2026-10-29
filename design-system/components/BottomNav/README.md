# BottomNav

Navegación principal entre las cuatro pestañas: Rodar, Conexión, Dispositivos y Ajustes.

**Uso**
- Al pie de las pantallas de pestaña, en `surface` con separador `line`.
- Iconos: Rodar `i-pedal_bike`, Conexión `i-sensors` (publicar una señal), Dispositivos `i-bluetooth` y Ajustes `i-tune`. Conexión no usa el icono de Bluetooth, el de wifi ni el manillar del controlador (`i-manillar`) de la barra de conexiones. Botones ya no es pestaña: su entrada de Ajustes lleva el icono propio de los cuatro símbolos (`i-botones`).
- Pestaña activa en `gear` (icono y texto) y con `aria-current="page"`; las demás en `ink-muted`. Siempre icono y texto.
- La llevan Conexión, Dispositivos y Ajustes, que no tienen botón de volver.
- No aparece en la pantalla de rodaje (inmersiva) ni en las pantallas secundarias (Botones, Marchas, Datos en pantalla, Diagnóstico, Asignar acción), que tienen volver a su origen.
- Atrás en una pestaña vuelve a Rodaje.
- Con el Square, Campag. I + ← / → cambia de pestaña.

**No**
- No añadir más de cuatro pestañas: Botones, Marchas y Diagnóstico se abren desde Ajustes.
