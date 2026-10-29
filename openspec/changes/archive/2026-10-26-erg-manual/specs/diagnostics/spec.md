# Spec Delta

## MODIFIED Requirements

### Requirement: Simulación
Diagnóstico SHALL mostrar en la sección Simulación el modo de control, la marcha puesta (con la Shimano, plato × piñón con su relación), el modelo («Simple»), el modo de bajadas, la pendiente base o la de Zwift con su terreno, en ERG de Zwift la potencia de Zwift y el ajuste de las palancas, en ERG manual la potencia fijada y la de entrada, el ajuste por la marcha, la última pendiente o potencia enviada al Neo, el último mensaje de Zwift con su hora y la latencia de la última palanca, y SHALL actualizarlos con cada cambio. Cuando el modo de bajadas limita la pendiente, SHALL indicarlo con texto, junto con la pendiente que habría correspondido sin el límite.

#### Scenario: Marcha dura
- **WHEN** la marcha es la 17, el Neo está conectado y Zwift no
- **THEN** la sección dice modo SIM, marcha 17 de 24, modelo Simple, bajadas «La marcha no crea bajadas», pendiente base 0,0 %, ajuste +5,0 % y enviada al Neo +5,0 %, sin indicación de límite

#### Scenario: Ruta de Zwift
- **WHEN** Zwift envía una pendiente de 4,0 % con Crr 0,0040, la marcha es la 14 y el Neo está conectado
- **THEN** la sección dice modo SIM · Zwift, pendiente Zwift 4,0 %, el terreno con Crr 0,0040, ajuste +2,0 %, enviada al Neo +6,0 % y «Simulación» como último mensaje de Zwift con su hora

#### Scenario: Pendiente limitada
- **WHEN** Zwift envía una pendiente de −7,0 %, la marcha es la 1 y el modo de bajadas es «La marcha no crea bajadas»
- **THEN** la sección dice pendiente Zwift −7,0 %, ajuste −11,0 %, enviada al Neo −7,0 % y «Limitada por Bajadas: sin límite sería −18,0 %»

#### Scenario: Límite que deja de actuar
- **WHEN** la pendiente está limitada y se sube de marcha hasta que la suma supera el límite
- **THEN** desaparece la indicación de límite y la enviada al Neo coincide con la suma

#### Scenario: Entrenamiento de Zwift
- **WHEN** Zwift pide 230 W y el ajuste de las palancas es +10 W
- **THEN** la sección dice modo ERG · Zwift, potencia Zwift 230 W, ajuste de las palancas +10 W y enviada al Neo 240 W, y la marcha sigue visible

#### Scenario: ERG manual
- **WHEN** se entró en ERG manual a 200 W, se ha subido a 220 W y el Neo está conectado
- **THEN** la sección dice modo ERG · manual, potencia fijada 220 W, entrada 200 W y enviada al Neo 220 W, y la marcha sigue visible

#### Scenario: ERG manual sobre una ruta de Zwift
- **WHEN** en ERG manual sobre una ruta de Zwift, Zwift envía una pendiente de 3,0 %
- **THEN** la sección sigue diciendo modo ERG · manual, el último mensaje de Zwift es «Simulación» con su hora y la enviada al Neo no cambia

#### Scenario: Zwift desconectado
- **WHEN** se cierra Zwift rodando en una subida
- **THEN** durante 30 s la sección dice que se mantiene lo último de Zwift y después vuelve a modo SIM con la pendiente base

#### Scenario: Neo sin conexión
- **WHEN** el Neo no está conectado
- **THEN** la marcha, el modelo, el modo de bajadas, la pendiente base o la de Zwift y el ajuste siguen visibles, «Enviada al Neo» muestra «—» y no hay indicación de límite

#### Scenario: Shimano
- **WHEN** la transmisión es la Shimano, la marcha la 52×17, el Neo está conectado, Zwift no y «Bajadas» está en «Sin protección»
- **THEN** la sección dice marcha 52×17 · relación 3,06, modelo Simple, pendiente base 0,0 %, ajuste +5,1 % y enviada al Neo +5,1 %
