## ADDED Requirements

### Requirement: Ajustes desde Rodaje
Rodaje SHALL mostrar en la esquina superior derecha un botón con el icono de Ajustes, el mismo de la pestaña, con objetivo táctil de al menos 48 dp y descripción accesible «Ajustes». En horizontal SHALL ir a la derecha de los chips; en vertical, al final de la línea de los chips, sin que deje de ser una sola línea. Al tocarlo SHALL abrirse Ajustes como pestaña, sin terminar la sesión ni cambiar la marcha.

#### Scenario: Abrir Ajustes en horizontal
- **WHEN** el móvil está en horizontal, se rueda en la marcha 14 y se toca el icono de Ajustes
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada, sin botón de volver, y al tocar «Rodar» se vuelve a Rodaje en la marcha 14

#### Scenario: Abrir Ajustes en vertical
- **WHEN** el móvil está en vertical y se toca el icono de Ajustes, al final de la línea de los chips
- **THEN** se abre Ajustes con la barra inferior y la pestaña Ajustes marcada

#### Scenario: Atrás desde Ajustes abierto con el icono
- **WHEN** se abre Ajustes con el icono de Rodaje y se pulsa Atrás
- **THEN** se vuelve a Rodaje y la sesión sigue en marcha

#### Scenario: Línea de chips en vertical con el icono
- **WHEN** el móvil está en vertical a 360 dp de ancho, con el Neo conectando, el Square sin conexión y Zwift esperando
- **THEN** los tres chips y el icono de Ajustes caben en una sola línea, y al pasar los tres a conectados ni el icono, ni la marcha ni los datos se mueven

#### Scenario: Sin dispositivos
- **WHEN** el Neo y el Square no están conectados
- **THEN** el icono de Ajustes se ve en su sitio y abre Ajustes como siempre, y el pie sigue diciendo «Toca Neo o Square para conectarlos»

#### Scenario: Conexión perdida al abrir Ajustes
- **WHEN** se abre Ajustes con el icono y, con Ajustes abierto, se pierde la conexión con el Square
- **THEN** la app intenta reconectarlo como desde Rodaje y, al volver a Rodaje, el chip «Square» muestra su estado y la marcha es la misma

#### Scenario: Lector de pantalla
- **WHEN** el lector de pantalla recorre la barra superior de Rodaje
- **THEN** después de los chips anuncia «Ajustes», botón

#### Scenario: En ERG
- **WHEN** se está en ERG manual y se toca el icono de Ajustes
- **THEN** se abre Ajustes, y al volver a Rodaje la potencia objetivo es la misma
