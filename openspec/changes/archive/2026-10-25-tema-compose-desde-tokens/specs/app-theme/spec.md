# Spec Delta

## Purpose

Lleva los tokens del sistema de diseño a la app Android para que sus pantallas se vean igual que la guía de estilo y las maquetas en los dos temas, y detecta cuándo los derivados de la app se han quedado atrás respecto a la fuente de tokens.

## ADDED Requirements

### Requirement: Valores de la app iguales a los de la guía
La app SHALL usar, para cada token de color, tipografía, espaciado, radio y tamaño táctil, el mismo valor que la fuente de tokens del sistema de diseño, con los píxeles de la fuente interpretados como dp en tamaños y como sp en textos.

#### Scenario: Color de la marcha
- **WHEN** se muestra un elemento con el token `gear` en tema rodaje
- **THEN** su color es el mismo `#ffb21f` que la guía de estilo muestra para `gear` en rodaje

#### Scenario: Tamaño táctil de rodaje
- **WHEN** se mide en la app el token `touch-ride`
- **THEN** mide 72 dp

#### Scenario: Estilo tipográfico
- **WHEN** se muestra un texto con el estilo `metric-xl`
- **THEN** usa la familia condensada, 72 sp de tamaño, 72 sp de interlineado y peso 600

### Requirement: Tema rodaje por defecto y tema claro por contenedor
La app SHALL mostrarse en tema rodaje cuando no se elige tema, y SHALL poder aplicar el tema claro a una pantalla entera o a una parte de ella sin afectar al resto.

#### Scenario: Arranque sin preferencia
- **WHEN** se abre la app
- **THEN** el fondo es `bg` de rodaje (`#0f1216`) y el texto principal es `ink` de rodaje

#### Scenario: Parte de la pantalla en claro
- **WHEN** una zona de una pantalla en rodaje se muestra con el tema claro
- **THEN** esa zona usa los valores claros de cada token y el resto de la pantalla sigue en rodaje

#### Scenario: Token sin valor propio en claro
- **WHEN** se usa `focus`, que remite a `gear`, dentro de una zona en tema claro
- **THEN** su color es el de `gear` en claro, no el de rodaje

### Requirement: Fuentes empaquetadas en la app
La app SHALL mostrar los textos con las fuentes del sistema de diseño (condensada para cifras, de interfaz y monoespaciada) empaquetadas en la propia app, sin descargarlas, y las cifras SHALL tener ancho tabular.

#### Scenario: Móvil sin conexión
- **WHEN** se abre la app con el móvil sin red
- **THEN** las cifras se ven en la familia condensada y los textos de interfaz en su familia, no en la fuente del sistema

#### Scenario: Cifras que no bailan
- **WHEN** una cifra con un estilo de cifras pasa de 99 a 100 o de 9 a 10
- **THEN** cada dígito mantiene su ancho

### Requirement: Derivados de la app sincronizados con la fuente de tokens
La comprobación de la app SHALL fallar si los valores de tema de la app no coinciden con la fuente de tokens, e indicar que hay que regenerar los derivados; los derivados SHALL regenerarse con el mismo comando que la hoja de tokens y la guía, y SHALL indicar que no se editan a mano.

#### Scenario: Token cambiado sin regenerar
- **WHEN** se cambia el valor de un token en la fuente y se ejecutan los tests de la app sin regenerar
- **THEN** los tests fallan, nombran el desajuste e indican el comando de regeneración

#### Scenario: Token cambiado y regenerado
- **WHEN** se cambia el valor de un token, se ejecuta el generador y se ejecutan los tests de la app
- **THEN** los tests pasan y la app muestra el nuevo valor sin editar su código a mano

#### Scenario: Contraste insuficiente
- **WHEN** se cambia un color de forma que un par queda por debajo de su mínimo y se ejecuta el generador
- **THEN** el generador falla con el par y su ratio, igual que para la guía, y la app no recibe un valor que no haya pasado la comprobación de contraste

### Requirement: Muestra del tema en la compilación de depuración
La compilación de depuración SHALL incluir una pantalla que muestre cada token de color con su nombre y cada estilo tipográfico con su nombre, tamaño y ejemplo, en tema rodaje y en tema claro, y la compilación de distribución SHALL NOT incluirla.

#### Scenario: Comparar con la guía
- **WHEN** se abre la muestra del tema en el móvil y la sección de color y tipografía de la guía de estilo en el ordenador
- **THEN** cada token y estilo aparece en las dos con el mismo nombre, color y aspecto, en los dos temas

#### Scenario: Compilación de distribución
- **WHEN** se instala la compilación de distribución
- **THEN** la muestra del tema no aparece ni se puede abrir

#### Scenario: Ejemplo más ancho que la pantalla
- **WHEN** un ejemplo tipográfico grande (`gear-hero`, `gear-ratio-hero`) no cabe en el ancho del móvil en vertical
- **THEN** la muestra se puede desplazar para verlo entero, sin recortar ni reducir el tamaño del estilo
