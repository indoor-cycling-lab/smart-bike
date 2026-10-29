# AppBar

Cabecera de las pantallas de ajustes: el título de la pantalla y, en las secundarias, el botón de volver.

**Uso**
- Título en `title` (24 sp, 600). Una por pantalla.
- Las pantallas de una pestaña (Conexión, Dispositivos, Ajustes) no llevan volver; las secundarias (Botones, Marchas, Datos en pantalla, Diagnóstico, Asignar acción) sí, con `arrow_back` y `aria-label` «Volver a …».
- Botón de volver de `touch-min`, alineado ópticamente con el margen.
- La pantalla de rodaje no tiene AppBar.

**No**
- No añadir acciones con icono en la barra: las acciones van como `Button` al pie de la pantalla.
