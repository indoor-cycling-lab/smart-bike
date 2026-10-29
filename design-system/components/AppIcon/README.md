# AppIcon

Icono de la app en el launcher: una bici de perfil sobre su rodillo. La rueda trasera, en `gear-fill` (ámbar), es el rodillo y gira en la splash; lleva debajo su tambor en `line-strong`. La rueda delantera va en `line-strong`, el cuadro en `ink` y el fondo es el `bg` de Rodaje.

**Fuente:** `design-system/identidad/icono-app.svg` (lienzo de 108 × 108, como el icono adaptativo de Android) y su capa monocroma `icono-app-monocromo.svg`. Sin colores: los ponen los tokens. De ahí salen los recursos de la app (`generar.py`) y el logo de la web.

**Uso**
- El icono es el mismo con el tema oscuro y con el claro: su fondo es siempre el `bg` de Rodaje. Quien lo muestre lo envuelve en `data-theme="rodaje"`.
- Android recorta el lienzo a los 72 dp centrales con la máscara del launcher (círculo, cuadrado redondeado, otras). Todas las formas caben en la **zona segura**, un círculo de 66 dp de diámetro centrado; `generar.py` falla si alguna se sale.
- Capa monocroma (Android 13 o posterior): un solo color, el del tema del móvil. La rueda trasera se distingue por la forma (anillo grueso con radios frente al anillo fino de la delantera), nunca por el color.
- Contraste de los elementos gráficos: 3:1 como mínimo entre la rueda trasera, el cuadro y el fondo (`contraste.py`). El cuadro termina antes del borde de la rueda trasera, con un hueco del color del fondo, porque `ink` sobre ámbar solo da 1,6:1.
- Clases: `sq-appicon__fondo`, `__cuadro`, `__rueda-delantera`, `__rueda-trasera`, `__radio`, `__buje` y `__tambor` en el SVG; `sq-appmask` (con `--circulo`, `--cuadrado`, `--zona` para marcar la zona segura, `--sm` para 48 dp y `--mono`) para ver el recorte.

**No**
- No añadir texto ni más ruedas al icono.
- No usar el icono dentro de la app como adorno: solo está en el launcher y en la splash.
