# Spec Delta

## Purpose

Define los valores de diseño con nombre (color, tipografía, espaciado, radios y tamaños táctiles) que comparten todas las pantallas de la app, y cómo se resuelven en los temas rodaje (oscuro) y claro.

## ADDED Requirements

### Requirement: Tokens con nombre como única fuente de estilo
El sistema de diseño SHALL definir todos sus valores visuales como tokens con nombre por función (p. ej. `bg`, `surface`, `ink`, `gear`, `space-4`, `touch-ride`) en una única fuente, y los componentes SHALL usar solo esos tokens, sin valores literales de color, tamaño o espaciado.

#### Scenario: Componente sin valores literales de color
- **WHEN** se revisan los estilos de cualquier componente
- **THEN** sus colores proceden de tokens y no de valores hexadecimales escritos en el componente

#### Scenario: Un cambio de token se propaga
- **WHEN** se cambia el valor del token `gear` en la fuente de tokens y se regeneran los derivados
- **THEN** la marcha, la escalera, el plato activo y el botón principal reflejan el nuevo valor sin editar sus estilos

### Requirement: Dos temas con rodaje por defecto
Los tokens de color SHALL tener un valor para el tema `rodaje` (oscuro, por defecto) y otro para el tema `claro`, y el tema claro SHALL poder aplicarse a toda una pantalla o a un contenedor.

#### Scenario: Pantalla sin preferencia de tema
- **WHEN** se abre una pantalla sin tema elegido
- **THEN** se muestra con el tema rodaje

#### Scenario: Tema claro en un contenedor
- **WHEN** un contenedor se marca con el tema claro dentro de una página en tema rodaje
- **THEN** ese contenedor y su contenido usan los valores claros y el resto de la página sigue en rodaje

### Requirement: Un color por significado
La paleta SHALL tener un único color de marca (`gear`, ámbar) reservado a la marcha, a la acción principal y al estado elegido o activo de los controles, y colores con un significado fijo: `sim` y `erg` para el modo, `ok` y `danger` para conexión y alertas, `climb` y `descent` para pendiente (solo en Diagnóstico).

#### Scenario: Marca solo en la marcha y la acción principal
- **WHEN** se revisa una pantalla cualquiera
- **THEN** el ámbar aparece solo en la marcha (cifra, plato, piñón, escalera o aviso de tope), la acción principal, el foco, la pestaña activa, los controles elegidos o pulsados (casilla, opción, segmento, tecla pulsada, captura) y lo que resuelve la app (etiqueta APP, aviso informativo, lo enviado al Neo en Diagnóstico)

#### Scenario: Pendiente fuera de rodaje
- **WHEN** se revisa la pantalla de rodaje
- **THEN** no aparece ningún color de pendiente (`climb`, `descent`)

### Requirement: Contraste accesible en ambos temas
Los pares de texto y fondo definidos por los tokens SHALL cumplir 4,5:1 para texto y 3:1 para bordes de controles y elementos gráficos, en los dos temas, y SHALL existir una comprobación automática que falle si algún par no lo cumple.

#### Scenario: Texto secundario sobre superficie
- **WHEN** se mide `ink-muted` sobre `bg`, `surface` y `surface-2` en rodaje y en claro
- **THEN** todos los ratios son al menos 4,5:1

#### Scenario: Texto sobre el relleno de marca
- **WHEN** se mide `on-gear-fill` sobre `gear-fill`
- **THEN** el ratio es al menos 4,5:1

#### Scenario: Comprobación tras cambiar un color
- **WHEN** se cambia un color de forma que un par queda por debajo de su mínimo y se ejecuta la comprobación de contraste
- **THEN** la comprobación falla e indica el par y su ratio

### Requirement: Escala tipográfica con cifras legibles a distancia
La tipografía SHALL definir una familia condensada con numerales tabulares para cifras, una familia de interfaz y una monoespaciada para diagnóstico, con estilos con nombre, incluidos `gear-hero` (marcha lineal) y `gear-ratio-hero` (plato × piñón).

#### Scenario: Cifras que no bailan
- **WHEN** la potencia pasa de 99 a 100 W o la marcha de 9 a 10
- **THEN** las cifras mantienen su ancho por dígito y el resto de la pantalla no se desplaza

### Requirement: Fuentes disponibles sin conexión
Las fuentes y la fuente de iconos SHALL distribuirse con el sistema de diseño, con su licencia, y SHALL NOT cargarse de servicios externos.

#### Scenario: Guía sin conexión
- **WHEN** se abre la guía de estilo desde un servidor local sin conexión a internet
- **THEN** los textos se ven con sus fuentes y los iconos con sus glifos, no con sustitutos

### Requirement: Tamaños táctiles para rodaje y ajustes
Los tokens SHALL definir un objetivo táctil mínimo de 48 dp (`touch-min`) para ajustes y de 72 dp (`touch-ride`) para los controles de la pantalla de rodaje.

#### Scenario: Botones de rodaje
- **WHEN** se muestran los botones − y + en la pantalla de rodaje
- **THEN** cada uno mide al menos 72 × 72 dp
