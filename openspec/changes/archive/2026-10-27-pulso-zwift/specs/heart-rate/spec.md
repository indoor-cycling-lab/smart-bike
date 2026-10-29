# Spec Delta

## Purpose

Recoger el pulso de un pulsómetro Bluetooth estándar y decidir cuándo hay un pulso válido y cuándo no, de modo que el resto de la app trabaje con un valor fiable o con ninguno, sin inventar ceros.

## ADDED Requirements

### Requirement: Pulsómetro opcional
La app SHALL poder conectarse a un pulsómetro Bluetooth que ofrezca el servicio estándar de frecuencia cardiaca, y SHALL funcionar igual que hasta ahora, sin filas, iconos ni avisos de pulso, mientras no haya un pulsómetro recordado.

#### Scenario: Sin pulsómetro
- **WHEN** el usuario nunca ha buscado un pulsómetro
- **THEN** Rodaje, Dispositivos y Diagnóstico no muestran ningún aviso de pulso ni error de pulso, y las conexiones del Neo, el Square y Zwift se comportan como siempre

#### Scenario: Banda sin servicio de pulso
- **WHEN** el dispositivo al que se conecta la app no ofrece el servicio de frecuencia cardiaca
- **THEN** la app lo da por no válido, no lo recuerda y lo explica en Dispositivos

### Requirement: Pulso válido
La app SHALL tomar como pulso la frecuencia cardiaca en latidos por minuto que comunica la banda, entera y entre 1 y 250. Un valor de 0, fuera de ese rango o ilegible SHALL ignorarse. Si pasan 5 s sin una lectura válida, la app SHALL considerar que no hay pulso, y SHALL volver a tenerlo con la siguiente lectura válida. La app SHALL NOT filtrar, promediar ni corregir el valor.

#### Scenario: Lectura normal
- **WHEN** la banda comunica 132 bpm
- **THEN** el pulso es 132 bpm

#### Scenario: Banda sin contacto
- **WHEN** la banda comunica 0 bpm porque no toca la piel
- **THEN** esa lectura se ignora y, a los 5 s sin otra válida, no hay pulso

#### Scenario: Lectura imposible
- **WHEN** la banda comunica 255 bpm
- **THEN** esa lectura se ignora y se mantiene el pulso anterior hasta los 5 s

#### Scenario: Banda que deja de dar lecturas
- **WHEN** la conexión sigue abierta pero la banda no envía nada durante 5 s
- **THEN** no hay pulso, y vuelve a haberlo con la siguiente lectura válida

#### Scenario: Banda con lecturas de 16 bits
- **WHEN** la banda comunica el pulso en formato de 16 bits, como admite el estándar
- **THEN** el pulso es el mismo que con el formato de 8 bits

### Requirement: Pérdida del pulsómetro
Si se pierde la conexión con el pulsómetro, la app SHALL considerar que no hay pulso desde ese momento, SHALL intentar recuperar la conexión sola, sin límite de intentos mientras dure la sesión y también con la pantalla apagada, y SHALL conservar el resto de conexiones y la marcha puesta.

#### Scenario: Banda fuera de alcance
- **WHEN** el pulsómetro está conectado y se aleja o se apaga
- **THEN** no hay pulso, su estado pasa a reconectando, y el Neo, el Square y Zwift siguen conectados con la marcha puesta

#### Scenario: Banda de vuelta
- **WHEN** el pulsómetro se reconectaba y vuelve a estar a mano
- **THEN** se reconecta solo, sin tocar el móvil, y vuelve a haber pulso con la primera lectura válida

#### Scenario: Banda sin batería
- **WHEN** la banda se queda sin batería y se carga y se enciende al cabo de 30 minutos, con la pantalla apagada todo el tiempo
- **THEN** la app se reconecta sola
