## MODIFIED Requirements

### Requirement: Icono de la app en el launcher
La app SHALL tener un icono propio en el launcher: una bici vista de perfil, con la rueda trasera en el color de marca como símbolo del rodillo y un tambor gris debajo, la rueda delantera en gris y el cuadro en el color del texto, sobre el fondo oscuro de Rodaje. El icono SHALL ser adaptativo, verse entero con cualquier máscara de Android (círculo, cuadrado redondeado) y ser el mismo con el tema de la app en oscuro o en claro.

#### Scenario: Icono en el launcher
- **WHEN** se abre el launcher del móvil
- **THEN** la app aparece con el icono de la bici, con la rueda trasera en ámbar sobre un tambor gris y la delantera en gris, y no con el icono genérico de Android

#### Scenario: Máscaras del launcher
- **WHEN** el launcher recorta el icono en círculo, en cuadrado redondeado o en otra forma
- **THEN** las dos ruedas, el tambor y el cuadro se ven enteros, sin cortarse por las esquinas

#### Scenario: Tema de la app claro
- **WHEN** en Ajustes el tema elegido es «Claro»
- **THEN** el icono del launcher no cambia

### Requirement: Icono monocromo
La app SHALL ofrecer una versión monocroma del icono para los iconos temáticos de Android 13 o posterior, en la que la rueda trasera se distinga de la delantera por la forma y no solo por el color.

#### Scenario: Iconos temáticos activados
- **WHEN** el móvil con Android 13 o posterior tiene activados los iconos temáticos
- **THEN** el icono de la app se ve en el color del tema del móvil y se distingue cuál es la rueda trasera

### Requirement: Legibilidad del icono en pequeño
La rueda trasera, la delantera y el cuadro SHALL distinguirse entre sí y del fondo con al menos 3:1 de contraste, y el icono SHALL reconocerse como una bici a 48 dp.

#### Scenario: Icono a 48 dp
- **WHEN** el icono se ve a 48 dp
- **THEN** se reconocen las dos ruedas y el cuadro, y se identifica la rueda trasera

### Requirement: Splash animada
Al abrir la app, el arranque SHALL mostrar el icono de la app sobre el fondo oscuro de Rodaje, sin textos. En Android 12 o posterior SHALL animarse como mucho 1 s: la rueda trasera gira acelerando, de lento a rápido, y termina en una posición equivalente a la del icono. En versiones anteriores, el icono se ve quieto. La splash SHALL ser oscura también con el tema «Claro» elegido.

#### Scenario: Arranque en frío en Android 12 o posterior
- **WHEN** se abre la app desde el launcher con la app cerrada
- **THEN** se ve el icono sobre el fondo oscuro con la rueda trasera ámbar girando cada vez más rápido durante como mucho 1 s, y a continuación aparece Rodaje

#### Scenario: Arranque en Android 11 o anterior
- **WHEN** se abre la app desde el launcher con la app cerrada
- **THEN** se ve el icono quieto, con la rueda trasera en su posición, sobre el fondo oscuro, y a continuación aparece Rodaje

#### Scenario: Animaciones del sistema desactivadas
- **WHEN** el móvil tiene las animaciones desactivadas
- **THEN** se ve directamente el icono con la rueda trasera en su posición, sin movimiento

#### Scenario: Splash sin parpadeo claro
- **WHEN** se abre la app con el tema «Claro» elegido
- **THEN** el arranque no muestra ningún fondo claro antes de Rodaje

#### Scenario: Giro legible
- **WHEN** la rueda trasera gira en la splash
- **THEN** se ve girar en un solo sentido, sin parecer que se para ni que va hacia atrás al ir más rápido
