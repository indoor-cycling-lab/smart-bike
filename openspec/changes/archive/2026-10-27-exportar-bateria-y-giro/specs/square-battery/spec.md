# Spec Delta

## Purpose

Avisa al usuario de que la batería del Square está baja antes de que se apague a mitad de una ruta, sin llenar Rodaje de datos y sin fiarse de las lecturas que da el Square justo después de encenderlo.

## ADDED Requirements

### Requirement: Nivel de batería del Square
La app SHALL leer la batería del Square al conectar y cada 10 minutos mientras esté conectado. La lectura de justo después de conectar SHALL NOT contar como nivel, porque el Square da valores sin sentido al encenderse; solo cuentan las lecturas periódicas. La app SHALL conservar el último nivel válido mientras no llegue otro y mientras el Square siga recordado. Una lectura que falla o fuera de 0–100 % SHALL ignorarse.

#### Scenario: Primer nivel
- **WHEN** el Square se conecta y pasan 10 minutos
- **THEN** la app tiene un nivel, el de la lectura periódica

#### Scenario: Lectura al conectar
- **WHEN** el Square se conecta y da 35 % en la primera lectura
- **THEN** ese 35 % no se toma como nivel

#### Scenario: Reconexión
- **WHEN** el Square se desconecta y vuelve a conectarse
- **THEN** el último nivel válido se mantiene hasta que llegue la siguiente lectura periódica

#### Scenario: Olvidar el Square
- **WHEN** se olvida el Square en Dispositivos
- **THEN** la app deja de tener nivel de su batería

#### Scenario: Square sin servicio de batería
- **WHEN** el Square no ofrece el servicio de batería
- **THEN** no hay nivel y no hay aviso, y el resto funciona igual

### Requirement: Batería baja
La app SHALL considerar baja la batería del Square cuando su último nivel válido sea de 20 % o menos. El umbral es provisional hasta la prueba de 2 h con el equipo real. La batería baja SHALL avisarse mientras el Square esté conectado y SHALL dejar de avisarse cuando llegue un nivel válido por encima del umbral. La batería baja SHALL NOT cambiar el funcionamiento de los botones ni de las conexiones.

#### Scenario: Cruzar el umbral
- **WHEN** el último nivel válido pasa de 25 % a 20 %
- **THEN** la batería se considera baja

#### Scenario: Justo por encima
- **WHEN** el último nivel válido es 21 %
- **THEN** la batería no se considera baja

#### Scenario: Sustituir la pila
- **WHEN** la batería está baja y, tras cambiar la pila, llega una lectura periódica de 100 %
- **THEN** deja de considerarse baja

#### Scenario: Square desconectado
- **WHEN** la batería está baja y el Square pierde la conexión
- **THEN** se muestra el estado de conexión del Square, no el aviso de batería

#### Scenario: Se sigue rodando
- **WHEN** la batería está baja
- **THEN** los botones, las marchas y las conexiones funcionan igual
