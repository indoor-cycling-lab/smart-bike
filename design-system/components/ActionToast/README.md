# ActionToast

Aviso breve de lo que acaba de hacer la app al pulsar el Square: «+1 marcha», «Plato 50», «+10 W», «Modo ERG manual». Solo la acción: el botón y el gesto que la produjeron se ven en Diagnóstico.

**Recibe:** el texto de la acción.

**Uso**
- Abajo en el centro de la pantalla de rodaje. Visible 1,5 s y se desvanece; una acción nueva sustituye a la anterior sin apilarse.
- Opcional: se activa en Ajustes > Al rodar > «Mostrar la acción» (activado por defecto).
- Texto corto, sin verbo cuando basta: «+3 marchas», «Marcha de referencia», «Plato 34». Cifras con signo y coma decimal.
- Un tope no usa este aviso: lo muestra `GearDisplay` con su etiqueta de tope.

**No**
- No mostrar el botón ni el gesto (`Palanca D1 · pulsar`): eso es información de depuración y va en Diagnóstico.
- No usarlo para errores de conexión: eso es de `ConnectionChip`.
