# Button

Botón de acción. Tres variantes: principal, secundario y de rodaje.

**Recibe:** texto (o glifo en la variante de rodaje), variante y si está deshabilitado.

**Uso**
- Principal (`gear-fill` con `on-gear-fill`): una sola por pantalla, para la acción que avanza («Empezar a rodar», «Guardar»).
- Secundario (borde `line-strong`): el resto.
- De rodaje (`touch-ride`, 72dp): solo en la pantalla de rodaje, como respaldo táctil de las marchas. El Square es el control principal.
- Si navega, es un enlace (`a.sq-btn`) con el mismo aspecto: texto centrado y sin subrayado.
- Altura mínima `touch-min`. Foco con anillo `focus` de `focus-width`.
- El texto dice lo que pasa, en infinitivo: «Buscar dispositivos», no «Buscar».
