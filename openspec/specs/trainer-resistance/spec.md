# trainer-resistance Specification

## Purpose
Traducir la marcha puesta en resistencia del Tacx Neo cuando se rueda sin Zwift: qué pendiente se envía con cada marcha, cuándo se envía y en cuánto tiempo llega desde la palanca.

## Requirements

### Requirement: Pendiente según la marcha sin Zwift
Sin Zwift y con la transmisión lineal, la app SHALL simular en el Neo un terreno llano (0 %) más 1 % por cada marcha por encima de la 12 (menos 1 % por cada marcha por debajo), en modo Libre, con el límite inferior que fije el ajuste «Bajadas». La pendiente del terreno sin app de entrenamiento SHALL NOT poder cambiarse en Ajustes.

#### Scenario: Marcha de referencia
- **WHEN** la marcha es la 12 y el Neo está conectado
- **THEN** el Neo simula una pendiente de 0 %

#### Scenario: Marcha dura
- **WHEN** la marcha pasa a la 17
- **THEN** el Neo simula una pendiente de +5 % y la resistencia aumenta de forma perceptible

#### Scenario: Extremos
- **WHEN** la marcha es la 1 o la 24 con el ajuste «Bajadas» por defecto
- **THEN** el Neo simula 0 % o +12 %, respectivamente

#### Scenario: Extremos sin protección
- **WHEN** la marcha es la 1 o la 24 con «Bajadas» en «Sin protección»
- **THEN** el Neo simula −11 % o +12 %, respectivamente

#### Scenario: Pendiente sin Zwift de 3 %
- **WHEN** una configuración anterior tenía «Pendiente sin Zwift» en 3,0 % y la marcha es la 14
- **THEN** el Neo simula +2 %: el terreno sin app de entrenamiento es siempre llano

#### Scenario: Pendiente sin Zwift negativa
- **WHEN** la marcha es la 10 y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %: la marcha no baja la pendiente por debajo de la del terreno

### Requirement: Envío inmediato en cada cambio
La app SHALL enviar la pendiente nueva al Neo en cuanto cambia la marcha, sin esperar a ningún envío periódico, y SHALL NOT reenviarla si no cambia.

#### Scenario: Tope
- **WHEN** la marcha es la 24 y se pulsa la palanca D1
- **THEN** no se envía nada nuevo al Neo

#### Scenario: Volver a la referencia
- **WHEN** la marcha es la 12 y se pulsa □
- **THEN** no se envía nada nuevo al Neo

### Requirement: Sin envíos acumulados con cambios rápidos
Con varios cambios de marcha seguidos más rápidos de lo que el Neo confirma las escrituras, la app SHALL enviar siempre la pendiente de la marcha más reciente y SHALL descartar las intermedias que aún no se hayan enviado.

#### Scenario: Cinco cambios rápidos
- **WHEN** se pulsa la palanca D1 cinco veces lo más rápido posible desde la 12
- **THEN** la última pendiente que recibe el Neo es +5 % y llega sin esperar a que se envíen las cinco intermedias

### Requirement: Reenvío al conectar el Neo
Al conectarse o volver a conectarse el Neo, la app SHALL enviarle la pendiente de la marcha puesta sin que el usuario haga nada.

#### Scenario: Neo reconectado
- **WHEN** la marcha es la 17, se pierde la conexión con el Neo y se vuelve a conectar desde Diagnóstico
- **THEN** el Neo vuelve a simular +5 % en cuanto está conectado

### Requirement: Latencia de la palanca
La app SHALL medir, en cada cambio de marcha con una palanca, el tiempo desde que llega la pulsación hasta que el comando se entrega al Neo, y SHALL mostrar el último valor en Diagnóstico. El objetivo es que la resistencia cambie en menos de 100 ms desde la pulsación (NF1); el cumplimiento SHALL validarse con el equipo real antes de darse por bueno.

#### Scenario: Medir un cambio
- **WHEN** se pulsa la palanca D1 con el Neo y el Square conectados
- **THEN** Diagnóstico muestra la latencia de esa palanca en milisegundos y el registro de sesión guarda la hora de llegada de la pulsación, del envío y de la entrega al Neo

#### Scenario: Sin cambios todavía
- **WHEN** aún no se ha cambiado de marcha con una palanca en la sesión
- **THEN** la latencia de la palanca se muestra como «—»

### Requirement: Protección en bajadas
En modo Libre y en modo SIM, la app SHALL calcular la pendiente del terreno más el ajuste de la marcha y SHALL aplicarle el límite inferior del ajuste «Bajadas»: ninguno con «Sin protección»; la pendiente del terreno si es negativa, o 0 % si no lo es, con «La marcha no crea bajadas»; y 0 % con «Nunca empujar». El límite SHALL NOT afectar a la ERG ni al control manual de Diagnóstico.

#### Scenario: Llano en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es 0 %, la marcha es la 1 y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 % y el motor no empuja el volante

#### Scenario: Subida en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es +4 % y la marcha es la 1, con «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %

#### Scenario: Bajada en marcha ligera, por defecto
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 1, con «La marcha no crea bajadas»
- **THEN** el Neo simula −7 %

#### Scenario: Bajada en marcha dura, por defecto
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 24, con «La marcha no crea bajadas»
- **THEN** el Neo simula +5 %

#### Scenario: Nunca empujar en una bajada
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 12, con «Nunca empujar»
- **THEN** el Neo simula 0 %

#### Scenario: Nunca empujar no endurece
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 22, con «Nunca empujar»
- **THEN** el Neo simula +3 %

#### Scenario: Sin protección
- **WHEN** la pendiente del terreno es −7 % y la marcha es la 1, con «Sin protección»
- **THEN** el Neo simula −18 %

#### Scenario: Marchas que dan la misma pendiente
- **WHEN** la pendiente del terreno es 0 %, «Bajadas» está en «La marcha no crea bajadas» y se pasa de la marcha 3 a la 4
- **THEN** la marcha cambia en pantalla y no se envía nada nuevo al Neo, porque la pendiente sigue siendo 0 %

#### Scenario: Control manual
- **WHEN** en Diagnóstico se elige −5,0 % y se toca «Enviar pend.», con cualquier modo de «Bajadas»
- **THEN** el Neo simula −5 %

#### Scenario: Prueba del empuje del motor
- **WHEN** se envían desde el control manual pendientes de −0,5 % a −5 % en pasos de 0,5 % y, en cada una, se pedalea unos segundos y se deja de pedalear
- **THEN** se anota a partir de qué pendiente el motor mantiene o acelera el volante sin pedalear; el resultado SHALL validarse con el equipo real antes de decidir un suelo distinto de 0 %

### Requirement: Pendiente según la relación con la Shimano
Con la Shimano, la app SHALL sumar a la pendiente del terreno un ajuste proporcional al logaritmo de la relación (plato / piñón): 0 % en el centro geométrico del desarrollo y, con 1 % por marcha, −11,5 % en la 36×36 y +11,5 % en la 52×11, el mismo recorrido que de la 1 a la 24 de la lineal. Dos marchas con la misma relación SHALL dar la misma pendiente. El límite del ajuste «Bajadas» SHALL aplicarse igual que con la lineal.

#### Scenario: Marcha casi neutra
- **WHEN** se rueda sin Zwift con la Shimano en la 36×17 y «Bajadas» en «Sin protección»
- **THEN** el Neo simula unos −0,4 %

#### Scenario: Plato grande
- **WHEN** se rueda sin Zwift con la Shimano en la 52×17
- **THEN** el Neo simula unos +5,1 %

#### Scenario: Extremos sin protección
- **WHEN** la marcha es la 36×36 o la 52×11 con «Bajadas» en «Sin protección» y sin Zwift
- **THEN** el Neo simula unos −11,5 % o +11,5 %, respectivamente

#### Scenario: Relaciones casi iguales
- **WHEN** se pasa de la 36×19 a la 52×28 con «Sin protección» y sin Zwift
- **THEN** la pendiente cambia menos de 0,5 % (de unos −2,0 % a unos −2,3 %)

#### Scenario: Cambio de plato en automático
- **WHEN** se rueda sin Zwift con «Sin protección» en la 36×15 en automático y se pide «+1 marcha»
- **THEN** la marcha pasa a la 52×19 y el Neo pasa de unos +1,5 % a unos +3,4 %, un salto parecido al de un piñón

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de 4,0 % y la marcha es la 52×17
- **THEN** el Neo simula unos +9,1 %

#### Scenario: Prueba con el equipo real
- **WHEN** el usuario rueda con la Shimano en automático de la 36×36 a la 52×11 y vuelve
- **THEN** cada marcha se nota más dura o más ligera que la anterior y el cambio de plato no se nota más que un cambio de piñón; si no es así, se ajusta antes de archivar
