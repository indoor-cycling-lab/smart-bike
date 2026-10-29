# Spec Delta

## Purpose

Define cómo la app traduce la marcha, el terreno y la cadencia en resistencia del Neo según el modelo elegido (simple, por tabla o físico), con la dificultad global, las protecciones del modelo físico y la velocidad y distancia que este calcula.

## ADDED Requirements

### Requirement: Tres modelos de resistencia
La app SHALL ofrecer tres modelos de resistencia: «Simple», que es el de por defecto, «Por tabla» y «Físico». El modelo elegido SHALL valer con y sin Zwift, en SIM, y SHALL aplicarse al momento al cambiarlo, sin cambiar la marcha ni reiniciar la sesión. No SHALL afectar a la ERG de Zwift, a la ERG manual ni al control manual de Diagnóstico.

#### Scenario: Modelo por defecto
- **WHEN** se abre la app por primera vez
- **THEN** el modelo es «Simple» y la resistencia es la de antes de este change

#### Scenario: Cambiar de modelo rodando
- **WHEN** se rueda sin Zwift en la marcha 17 y se elige «Por tabla» sin haber editado la tabla
- **THEN** el Neo sigue simulando +5 % y la marcha sigue siendo la 17

#### Scenario: En ERG
- **WHEN** se está en ERG manual a 200 W y se elige «Físico»
- **THEN** el Neo sigue en 200 W, y al volver a SIM se aplica el físico

#### Scenario: Sin Neo conectado
- **WHEN** el Neo no está conectado y se elige «Físico»
- **THEN** el modelo queda elegido y el Neo recibe su potencia en cuanto se conecta

### Requirement: Modelo por tabla
Con «Por tabla», la app SHALL simular en el Neo la pendiente del terreno más el ajuste propio de la marcha puesta, tomado de la tabla de la transmisión elegida. Mientras no se edite, la tabla SHALL dar a cada marcha el mismo ajuste que el modelo simple.

#### Scenario: Tabla sin editar
- **WHEN** se elige «Por tabla» con la transmisión lineal, sin Zwift, en la marcha 17
- **THEN** el Neo simula +5 %, igual que con «Simple»

#### Scenario: Marcha editada
- **WHEN** en la tabla de la lineal la marcha 17 pasa a +7,0 % y se rueda sin Zwift en la 17 con la pendiente sin Zwift en 0 %
- **THEN** el Neo simula +7 % y las demás marchas no cambian

#### Scenario: Con Zwift
- **WHEN** Zwift manda una pendiente de +3 % y la marcha tiene +2,0 % en la tabla
- **THEN** el Neo simula +5 %

#### Scenario: Una tabla por transmisión
- **WHEN** se edita la tabla de la lineal y se cambia a la Shimano 105
- **THEN** la Shimano usa su propia tabla, sin editar, y la de la lineal se conserva al volver

#### Scenario: Protección en bajadas
- **WHEN** la tabla da −8 % a la marcha puesta, el terreno es 0 % y «Bajadas» está en «La marcha no crea bajadas»
- **THEN** el Neo simula 0 %

### Requirement: Modelo físico
Con «Físico», la app SHALL calcular la potencia que costaría pedalear a la velocidad que dan la cadencia y la relación de la marcha, con el peso del ciclista y de la bici y el terreno (pendiente, rodadura y viento) y SHALL fijarla en el Neo. A igual cadencia, una marcha más dura SHALL pedir más potencia, y a igual marcha, una pendiente mayor también.

#### Scenario: Marcha más dura
- **WHEN** se pedalea en llano a 90 rpm y se pasa a una marcha más dura
- **THEN** la potencia que se fija en el Neo sube

#### Scenario: Cadencia
- **WHEN** se mantiene la marcha y la cadencia pasa de 70 a 90 rpm en llano
- **THEN** la potencia que se fija en el Neo sube con la cadencia, no se queda fija

#### Scenario: Subida
- **WHEN** Zwift manda una pendiente de +6 % y se mantiene la cadencia
- **THEN** la potencia fijada es mayor que con 0 %

#### Scenario: Pesos
- **WHEN** sube el peso del ciclista o el de la bici
- **THEN** la potencia en subida sube y en llano casi no cambia

#### Scenario: Sin Zwift
- **WHEN** se rueda sin Zwift con el físico y la «Pendiente sin Zwift» es 2,0 %
- **THEN** el cálculo usa una pendiente de +2 % con la rodadura y el aire por defecto

#### Scenario: Transmisión lineal
- **WHEN** se elige el físico con la transmisión lineal
- **THEN** las 24 marchas se reparten de la más ligera a la más dura en un recorrido equivalente al de la Shimano 105, y cada una pide más potencia que la anterior a igual cadencia

### Requirement: Protecciones del modelo físico
Con «Físico», la app SHALL suavizar la cadencia y la pendiente, SHALL NOT bajar de la potencia mínima de Ajustes, SHALL aliviar la potencia cuando la cadencia es baja y SHALL limitar la rapidez con la que cambia la potencia, salvo al cambiar de marcha, que SHALL aplicarse al instante.

#### Scenario: Pendiente con zigzag
- **WHEN** Zwift manda una pendiente que oscila unas centésimas cada pocos segundos
- **THEN** la potencia fijada en el Neo no oscila de forma apreciable

#### Scenario: Potencia mínima en bajada
- **WHEN** la pendiente es −5 %, la potencia mínima es 30 W y se pedalea a cadencia normal
- **THEN** el Neo recibe 30 W

#### Scenario: Cadencia baja
- **WHEN** la cadencia cae de 80 rpm a 20 rpm
- **THEN** la potencia baja hacia la mínima en vez de mantenerse, y a 0 rpm es la mínima

#### Scenario: Cambio gradual
- **WHEN** la cadencia sube de golpe de 60 a 100 rpm
- **THEN** la potencia fijada sube en pocos segundos, no de un salto

#### Scenario: Cambio de marcha inmediato
- **WHEN** se pulsa la palanca para una marcha más dura rodando a 90 rpm
- **THEN** la potencia nueva llega al Neo sin esperar a la rampa y la latencia de la palanca sigue por debajo de 100 ms

#### Scenario: Sin reenvíos inútiles
- **WHEN** la potencia calculada cambia menos de 2 W
- **THEN** no se envía nada nuevo al Neo

### Requirement: Dificultad global
La app SHALL aplicar el ajuste «Dificultad», de 50 % a 150 % con 100 % por defecto, a los tres modelos: con «Simple» y «Por tabla» multiplica la suma de la pendiente del terreno y el ajuste de la marcha, antes del límite de «Bajadas», y con «Físico» multiplica la potencia calculada. No SHALL afectar a la ERG ni al control manual.

#### Scenario: Valor por defecto
- **WHEN** la dificultad es 100 %
- **THEN** la resistencia es la que da el modelo sin escalar

#### Scenario: Más dura con el simple
- **WHEN** la dificultad es 150 %, se rueda sin Zwift en la marcha 17 y la pendiente sin Zwift es 0 %
- **THEN** el Neo simula +7,5 %

#### Scenario: Más suave con el físico
- **WHEN** la dificultad es 50 % con el físico
- **THEN** la potencia fijada en el Neo es la mitad de la calculada, sin bajar de la potencia mínima

#### Scenario: En ERG
- **WHEN** la dificultad es 150 % y se rueda en ERG manual a 200 W
- **THEN** el Neo sigue en 200 W

### Requirement: Velocidad y distancia calculadas
Con «Físico» y el Neo conectado, la app SHALL calcular la velocidad a partir de la cadencia y la relación de la marcha, y SHALL acumular la distancia mientras se pedalea. Con «Simple» o «Por tabla», o sin el Neo, la velocidad SHALL quedar sin valor. La distancia SHALL empezar en cero con cada sesión y conservar su valor sin avanzar cuando no se calcula la velocidad.

#### Scenario: Velocidad
- **WHEN** se pedalea a 90 rpm con el físico en una marcha de relación 2,0
- **THEN** la velocidad es de unos 22,6 km/h

#### Scenario: Distancia
- **WHEN** se pedalea 10 min a 24 km/h con el físico
- **THEN** la distancia es de unos 4 km

#### Scenario: Paradas
- **WHEN** se deja de pedalear
- **THEN** la velocidad es 0 y la distancia no avanza

#### Scenario: Otro modelo
- **WHEN** se cambia a «Simple» tras recorrer 4,0 km con el físico
- **THEN** la velocidad no tiene valor, la distancia sigue en 4,0 km y no avanza

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo
- **THEN** la velocidad no tiene valor y la distancia conserva su valor

### Requirement: Alternar de modelo con un botón
La acción «Siguiente modelo» SHALL pasar de «Simple» a «Por tabla», de «Por tabla» a «Físico» y de «Físico» a «Simple», aplicarse al momento y avisar con el nombre del modelo nuevo. El modelo SHALL quedar guardado como si se hubiera elegido en Ajustes.

#### Scenario: Recorrer los modelos
- **WHEN** se pulsa el botón asignado a «Siguiente modelo» tres veces desde «Simple»
- **THEN** el aviso dice «Modelo: Por tabla», «Modelo: Físico» y «Modelo: Simple», en ese orden

#### Scenario: Guardado
- **WHEN** se pasa a «Físico» con el botón y se cierra y se vuelve a abrir la app
- **THEN** el modelo sigue siendo «Físico»

#### Scenario: Aviso desactivado
- **WHEN** «Mostrar la acción» está desactivado y se pulsa el botón
- **THEN** el modelo cambia sin aviso en pantalla

### Requirement: Configuración anterior a los modelos
Si la configuración guardada es anterior a este change, la app SHALL conservar los demás ajustes y empezar con «Simple», dificultad 100 %, potencia mínima 30 W, peso de la bici de 8 kg y tablas sin editar. Los valores guardados fuera de sus límites SHALL recortarse.

#### Scenario: Configuración anterior
- **WHEN** se abre la app con una configuración guardada antes de este change
- **THEN** se conservan los demás ajustes y el modelo es «Simple»

#### Scenario: Tabla con el número equivocado de marchas
- **WHEN** la tabla guardada de una transmisión no tiene una entrada por marcha
- **THEN** esa transmisión usa la tabla sin editar y las demás se conservan

#### Scenario: Valor fuera de límites
- **WHEN** la dificultad guardada es 300 %
- **THEN** la app usa 150 %
