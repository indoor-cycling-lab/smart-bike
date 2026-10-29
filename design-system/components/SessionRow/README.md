# SessionRow

Una sesión grabada en la lista de «Sesiones grabadas»: cuándo empezó, cuánto ocupa y las acciones de abrir, compartir y borrar.

**Recibe:** fecha y hora de inicio, tamaño, estado (cerrada, grabándose, detenida por poco espacio o por un error) y si se puede borrar.

**Piezas**
- `sq-ses`: la fila, dentro de un `sq-list`. Texto a la izquierda (`sq-ses__text`) y las acciones a la derecha (`sq-ses__acts`).
- Nombre (`sq-ses__name`, 600): la fecha y la hora de inicio, «29 oct 2026 · 18:42».
- Ayuda (`caption`): el tamaño, y el estado si no es una sesión cerrada («Grabando · 12 KB», «Detenida por poco espacio · 8 KB»). El estado va siempre en texto.
- Acciones (`sq-ses__act`, de `touch-min`, solo icono): `open_in_new` («Abrir»), `share` («Compartir») y `delete` («Borrar»). Cada una lleva `aria-label` con el nombre de la acción y el de la sesión.

**Uso**
- La sesión que se está grabando no ofrece «Borrar»: se quita el botón, no se desactiva, para que no parezca que se podrá borrar después. El hueco se conserva (`sq-ses__acts` mide siempre tres `touch-min`) y abrir y compartir quedan alineados con las demás filas.
- Para borrar se pide confirmación en línea debajo de la fila (`Notice` y `sq-actions` con «Borrar» y «Cancelar»), como la restauración en Botones.
- Abrir y compartir siguen disponibles aunque la sesión se esté grabando.

**No**
- No transmitir el estado solo con color ni con un icono sin texto.
- No añadir más de tres acciones a la fila.
