## Purpose
Identidad visual de la app en el móvil: el icono del launcher (adaptativo y monocromo), la splash animada del arranque y que sus recursos salgan de una única fuente del sistema de diseño y se mantengan sincronizados.

## ADDED Requirements

### Requirement: Icono de la app en el launcher
La app SHALL tener un icono propio en el launcher: una piñonera vista de perfil, con el piñón activo en el color de marca y el resto en gris, sobre el fondo oscuro de Rodaje. El icono SHALL ser adaptativo, verse entero con cualquier máscara de Android (círculo, cuadrado redondeado) y ser el mismo con el tema de la app en oscuro o en claro.

#### Scenario: Icono en el launcher
- **WHEN** se abre el launcher del móvil
- **THEN** la app aparece con el icono de la piñonera, con un solo piñón en ámbar, y no con el icono genérico de Android

#### Scenario: Máscaras del launcher
- **WHEN** el launcher recorta el icono en círculo, en cuadrado redondeado o en otra forma
- **THEN** las barras se ven enteras, sin cortarse por las esquinas

#### Scenario: Tema de la app claro
- **WHEN** en Ajustes el tema elegido es «Claro»
- **THEN** el icono del launcher no cambia

### Requirement: Icono monocromo
La app SHALL ofrecer una versión monocroma del icono para los iconos temáticos de Android 13 o posterior, en la que el piñón activo se distinga de los demás por la forma y no solo por el color.

#### Scenario: Iconos temáticos activados
- **WHEN** el móvil con Android 13 o posterior tiene activados los iconos temáticos
- **THEN** el icono de la app se ve en el color del tema del móvil y se distingue cuál es el piñón activo

### Requirement: Legibilidad del icono en pequeño
Las barras del icono y el piñón activo SHALL distinguirse entre sí y del fondo con al menos 3:1 de contraste, y el icono SHALL reconocerse a 48 dp.

#### Scenario: Icono a 48 dp
- **WHEN** el icono se ve a 48 dp
- **THEN** se cuentan las siete barras y se identifica el piñón activo

### Requirement: Splash animada
Al abrir la app, el arranque SHALL mostrar el icono de la app sobre el fondo oscuro de Rodaje, sin textos. En Android 12 o posterior SHALL animarse como mucho 1 s: el piñón ámbar salta de barra en barra y se queda en la posición del icono. En versiones anteriores, el icono se ve quieto. La splash SHALL ser oscura también con el tema «Claro» elegido.

#### Scenario: Arranque en frío en Android 12 o posterior
- **WHEN** se abre la app desde el launcher con la app cerrada
- **THEN** se ve el icono sobre el fondo oscuro con el piñón ámbar saltando de barra en barra durante como mucho 1 s, y a continuación aparece Rodaje

#### Scenario: Arranque en Android 11 o anterior
- **WHEN** se abre la app desde el launcher con la app cerrada
- **THEN** se ve el icono quieto, con el piñón activo en su posición, sobre el fondo oscuro, y a continuación aparece Rodaje

#### Scenario: Animaciones del sistema desactivadas
- **WHEN** el móvil tiene las animaciones desactivadas
- **THEN** se ve directamente el icono con el piñón activo en su posición, sin movimiento

#### Scenario: Splash sin parpadeo claro
- **WHEN** se abre la app con el tema «Claro» elegido
- **THEN** el arranque no muestra ningún fondo claro antes de Rodaje

### Requirement: Arranque sin retraso añadido
La splash SHALL durar solo lo que tarda la app en abrir Rodaje, sin esperas añadidas para terminar la animación, y SHALL NOT bloquear la conexión con el Neo y el Square, que arranca igual que antes.

#### Scenario: Rodaje listo antes de que acabe la animación
- **WHEN** Rodaje está listo antes de que termine la animación
- **THEN** se muestra Rodaje sin esperar a que acabe

### Requirement: Recursos de identidad desde una única fuente
El icono, la splash y el icono de «Botones» de la app SHALL regenerarse con el mismo comando que los demás derivados del sistema de diseño, a partir de una única fuente, y la comprobación de la app SHALL fallar si no coinciden con ella.

#### Scenario: Fuente cambiada sin regenerar
- **WHEN** se cambia el dibujo del icono en el sistema de diseño y se ejecutan los tests de la app sin regenerar
- **THEN** los tests fallan, nombran el recurso desactualizado e indican el comando de regeneración

#### Scenario: Icono igual que en la guía
- **WHEN** se compara el icono de la app con el de la guía de estilo
- **THEN** tienen la misma forma y los mismos colores
