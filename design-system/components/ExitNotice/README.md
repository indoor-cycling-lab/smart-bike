# ExitNotice

Aviso de que un Atrás más cierra la app: «Para cerrar la aplicación, pulsa Atrás otra vez» durante 3 s y, al pulsarlo de nuevo, «Cerrando…» hasta que la app se cierra. Evita cerrar la sesión de rodaje por un Atrás accidental.

**Recibe:** el texto (una o varias líneas).

**Uso**
- Flota sobre la pantalla de rodaje (`position: absolute` dentro de `sq-ride`): no ocupa sitio, así que al aparecer o desaparecer no se mueve ni cambia de tamaño nada.
- Centrado en la pantalla, donde se mira al rodar. Tapa un instante la marcha, pero al ser translúcido se sigue viendo; el pie queda libre para el aviso de acción.
- Discreto: fondo `surface` al 82 % de opacidad (translúcido), borde de 1 px (`line`), texto de 14 px en peso 500 y ancho máximo el de la pantalla menos 24 px a cada lado: cabe en una línea desde 360 dp de ancho (304 px) y se parte en dos en pantallas más estrechas o con la letra del sistema ampliada.
- Se distingue por su forma y su borde, no solo por el color. Lo que queda detrás se transparenta un poco: el texto debe seguir leyéndose sobre la marcha o los datos; si no, subir la opacidad.
- No es táctil: `pointer-events: none`.
- Accesibilidad: región viva (`role="status"`); «Cerrando…» se anuncia como «Cerrando».
- No depende del ajuste «Mostrar la acción»: es una protección, no un detalle de las palancas.

**No**
- No usarlo para avisar de acciones de las palancas (eso es `ActionToast`) ni de errores de conexión.
- No añadir botones: la confirmación es el segundo Atrás.
