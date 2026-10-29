## MODIFIED Requirements

### Requirement: Lista de modos de conexión
El sistema de diseño SHALL ofrecer una lista de modos de elección única, cada modo con su nombre, su descripción de por dónde va cada servicio, su detalle técnico, sus etiquetas de compatibilidad y las marcas de recomendado y de elegido, esta última por algo más que el color. La marca de elegido SHALL ser el anillo del acento de marca en la fila y un radio con el aro y el punto central de ese mismo acento, sin otro color añadido; los modos sin elegir SHALL llevar el radio vacío con el aro neutro. La guía SHALL mostrarla con modos sin elegir, elegido y recomendado.

#### Scenario: Modo elegido
- **WHEN** un modo está elegido
- **THEN** se distingue por el anillo de la fila y por el radio con aro y punto del acento de marca, además del color, con el contraste de 4,5:1 para el texto y de 3:1 para el radio sobre la fila en los dos temas, y su fila tiene al menos 48 dp de alto

#### Scenario: Modo sin elegir
- **WHEN** un modo no está elegido
- **THEN** su radio es un aro neutro sin punto y su fila no lleva anillo

#### Scenario: Modo elegido en escala de grises
- **WHEN** la lista se ve en escala de grises
- **THEN** el modo elegido se sigue distinguiendo por el anillo de la fila y por el punto del radio

#### Scenario: Modo recomendado
- **WHEN** un modo es el recomendado y no está elegido
- **THEN** lleva la marca «Recomendado» en texto

#### Scenario: Etiquetas de compatibilidad
- **WHEN** un modo tiene «Zwift: validado» y «Otras apps con FTMS: a validar»
- **THEN** las dos etiquetas se distinguen por el texto, no solo por el color, y lo «a validar» no se presenta como confirmado
