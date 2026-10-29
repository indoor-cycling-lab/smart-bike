# Stepper

Ajuste numérico con botones − y +. Es el control por defecto para valores con unidad: pesos, porcentajes, vatios, número de marchas.

**Recibe:** nombre, ayuda opcional, valor, unidad, paso, mínimo y máximo.

**Uso**
- Valor en `metric-md` y tabular; unidad en `ink-muted`.
- Mantener − o + repite, igual que las palancas del Square.
- En los límites, el botón que los superaría se deshabilita (`disabled`: atenuado, sin respuesta y anunciado como no disponible); el otro sigue activo. El valor no se atenúa.
- Decimales con coma.

**No**
- No usar deslizadores para valores que hay que fijar con precisión.
