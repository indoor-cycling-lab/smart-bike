## ADDED Requirements

### Requirement: Icono de Conexión
La pestaña Conexión de la navegación inferior SHALL usar un icono propio que sugiera emitir o publicar una señal, de un solo color, que tome el de la pestaña (activa o no), distinto del icono de Bluetooth de Dispositivos, del de wifi y del del mando, y reconocible a 24 dp en los dos temas.

#### Scenario: Pestaña Conexión
- **WHEN** se abre cualquier pantalla con la barra inferior
- **THEN** la pestaña Conexión muestra su icono propio, distinto del de Dispositivos

#### Scenario: Pestaña activa en los dos temas
- **WHEN** la pestaña Conexión está activa en tema rodaje y en tema claro
- **THEN** el icono se ve en el color de marca de la pestaña, con el contraste del texto de la pestaña

### Requirement: Estado de la publicación
El estado de la publicación en Rodaje SHALL ser un chip de una línea con el estado del rodillo y lo que falta en palabra y forma (punto lleno conectado, aro que gira esperando en tono atenuado, aro vacío con borde de alerta si no se puede publicar), sin nombre de plataforma ni transporte, a la izquierda de la fila de Ajustes, con un objetivo táctil de al menos 48 dp de alto sin alargar la línea. La guía SHALL mostrar sus estados en los dos temas.

#### Scenario: Estados sin color
- **WHEN** el estado se ve en escala de grises
- **THEN** conectado, esperando y sin publicar se distinguen por la forma y por la palabra

#### Scenario: Contraste en los dos temas
- **WHEN** se ve el estado en tema rodaje y en tema claro
- **THEN** el texto se lee con al menos 4,5:1 y el borde de alerta con al menos 3:1

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla llega al estado con el rodillo conectado y el mando esperando
- **THEN** lo lee como un solo elemento: «Conexión: rodillo, conectado; mando, esperando»

#### Scenario: Texto más largo
- **WHEN** el estado dice «Conectado · sin mando ni pulso» en un móvil de 360 dp de ancho en vertical
- **THEN** cabe en una línea sin empujar el botón de Ajustes ni cambiar el alto de la fila

### Requirement: Lista de modos de conexión
El sistema de diseño SHALL ofrecer una lista de modos de elección única, cada modo con su nombre, su descripción de por dónde va cada servicio, su detalle técnico, sus etiquetas de compatibilidad y las marcas de recomendado y de elegido, esta última por algo más que el color. La guía SHALL mostrarla con modos sin elegir, elegido y recomendado.

#### Scenario: Modo elegido
- **WHEN** un modo está elegido
- **THEN** se distingue por la marca de selección además del color, con el contraste de texto de 4,5:1 en los dos temas, y su fila tiene al menos 48 dp de alto

#### Scenario: Modo recomendado
- **WHEN** un modo es el recomendado y no está elegido
- **THEN** lleva la marca «Recomendado» en texto

#### Scenario: Etiquetas de compatibilidad
- **WHEN** un modo tiene «Zwift: validado» y «Otras apps con FTMS: a validar»
- **THEN** las dos etiquetas se distinguen por el texto, no solo por el color, y lo «a validar» no se presenta como confirmado

### Requirement: Fila de servicio publicado
El sistema de diseño SHALL ofrecer una fila de servicio con su icono, su nombre, por dónde se publica, su estado en palabra y forma (conectado, esperando, sin conexión, no disponible, no se ofrece) y una ayuda debajo, que puede llevar a otra pantalla. La guía SHALL mostrar todos sus estados en los dos temas.

#### Scenario: Estados sin color
- **WHEN** la fila se ve en escala de grises
- **THEN** cada estado se distingue por la palabra y la forma

#### Scenario: No se ofrece
- **WHEN** la fila está en «No se ofrece» con la ayuda que lleva a Dispositivos
- **THEN** el estado se ve atenuado, distinto de «Sin conexión», y la ayuda tiene un objetivo táctil de al menos 48 dp

## MODIFIED Requirements

### Requirement: Icono de Zwift
Zwift SHALL tener una «Z» en un recuadro redondeado, que SHALL NOT imitar el logotipo de Zwift, y se usa en Diagnóstico, no en la barra de Rodaje.

#### Scenario: Icono de Zwift fuera de la barra
- **WHEN** se abre la barra de conexiones de Rodaje
- **THEN** no aparece la «Z» de Zwift

### Requirement: Navegación inferior
La navegación inferior SHALL ofrecer las pestañas Rodar, Conexión, Dispositivos y Ajustes con icono y texto, marcar la activa por color y por estado accesible, y SHALL NOT aparecer en la pantalla de rodaje ni en pantallas secundarias.

#### Scenario: Pestaña activa
- **WHEN** se está en Ajustes
- **THEN** la pestaña Ajustes aparece en el color de marca y se anuncia como página actual

## REMOVED Requirements

### Requirement: Icono de Plataformas
**Reason**: La pestaña pasa a llamarse «Conexión».
**Migration**: Ver «Icono de Conexión»; se conserva el mismo icono.

### Requirement: Estado de la plataforma
**Reason**: El estado de Rodaje deja de nombrar la plataforma.
**Migration**: Ver «Estado de la publicación».

### Requirement: Selector de plataforma y de modo
**Reason**: Ya no se elige plataforma.
**Migration**: Ver «Lista de modos de conexión».
