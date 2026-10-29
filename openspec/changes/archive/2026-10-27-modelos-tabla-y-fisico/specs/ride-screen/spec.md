# Spec Delta

## ADDED Requirements

### Requirement: Velocidad y distancia en Rodaje
Rodaje SHALL poder mostrar «Velocidad», en km/h con un decimal, y «Distancia», en km con dos decimales, como datos de la rejilla cuando estén elegidos en «Datos en pantalla», con el mismo tamaño de cifra que los demás. La velocidad SHALL tener valor solo con el modelo físico y el Neo conectado y, si no, mostrarse como «—»; la distancia SHALL conservar su valor cuando no avanza.

#### Scenario: Con el físico
- **WHEN** se pedalea con el modelo físico, a unos 24 km/h, con velocidad y distancia elegidas
- **THEN** Rodaje muestra «24,0» km/h y una distancia que crece mientras se pedalea

#### Scenario: Con otro modelo
- **WHEN** el modelo es «Simple» y la velocidad está elegida
- **THEN** la velocidad muestra «—» y la distancia conserva su valor sin avanzar

#### Scenario: Neo desconectado
- **WHEN** se pierde la conexión con el Neo con el físico y 3,20 km recorridos
- **THEN** la velocidad muestra «—» y la distancia sigue en «3,20»

#### Scenario: Parado
- **WHEN** se deja de pedalear con el físico
- **THEN** la velocidad muestra «0,0» y la distancia no avanza

#### Scenario: Seis datos con los nuevos
- **WHEN** están elegidos potencia, cadencia, tiempo, W/kg, velocidad y distancia
- **THEN** Rodaje los muestra todos sin desplazamiento ni cambio de tamaño de la cifra

#### Scenario: Sin mostrar el modelo
- **WHEN** se rueda con cualquier modelo
- **THEN** Rodaje no muestra el nombre del modelo ni la potencia que calcula, y estos solo se ven en Diagnóstico

#### Scenario: Sesión nueva
- **WHEN** se termina la sesión y se vuelve a abrir la app
- **THEN** la distancia empieza en cero
