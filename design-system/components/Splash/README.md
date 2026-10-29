# Splash

Arranque de la app: el icono (`AppIcon`) sobre el fondo `bg` de Rodaje, sin textos. Es la splash del sistema (`androidx.core:splashscreen`), no una pantalla propia.

**Uso**
- En Android 12 o posterior, una animación de 1 s como máximo (900 ms): la rueda trasera ámbar gira dos vueltas acelerando, de lenta a rápida, y se queda en la posición del icono. Se anima la rotación de un grupo con el centro de la rueda como pivote.
- El estado base es el final. En Android 9 a 11, o con las animaciones del sistema desactivadas, se ve ese estado, quieto.
- Dos vueltas dejan la rueda en la misma posición que el icono. A la velocidad final (unos 27° por fotograma a 60 Hz) los seis radios, separados 60°, no producen el efecto de rueda de carro (que parezca parar o ir hacia atrás).
- Siempre oscura, también con el tema «Claro» elegido: es la ventana anterior a que se lea la configuración. Quien la muestre la envuelve en `data-theme="rodaje"`.
- Después aparece Rodaje, sin esperar a que acabe la animación: no se retiene la splash (`setKeepOnScreenCondition`) ni se bloquea la conexión con el Neo y el Square.
- En la guía y en la maqueta la animación se repite cada 3 s para poder verla y, con movimiento reducido (`prefers-reduced-motion`), se muestra solo el estado final.

**No**
- No añadir textos, logotipos ni barra de progreso.
- No retener la splash para que acabe la animación.
