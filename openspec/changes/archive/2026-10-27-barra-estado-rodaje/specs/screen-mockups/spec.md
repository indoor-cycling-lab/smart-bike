## ADDED Requirements

### Requirement: Indicadores del móvil en las maquetas de rodaje
Las maquetas de rodaje, en horizontal y en vertical, SHALL mostrar la hora, el wifi y la batería en todos sus estados, en los dos temas, con la colocación de la spec `ride-screen`. SHALL tener estados para el wifi tachado, la batería baja, el móvil cargando y, en vertical, la cámara en el centro, además de la cámara a la izquierda de los demás. Los estados de los indicadores SHALL poder combinarse con los de la ruta sin repetirlos.

#### Scenario: Estado de partida
- **WHEN** se abre `maquetas/rodaje-vertical.html` en el estado sin Zwift
- **THEN** se ven «14:32», el wifi normal y la batería al 82 % a la izquierda de la franja, a la derecha de la cámara, y los iconos del Neo y del Square a la derecha, sin solapamientos a 360 dp

#### Scenario: Cámara en el centro
- **WHEN** se elige el estado con la cámara en el centro en vertical
- **THEN** la hora, el wifi y la batería quedan a la izquierda del recorte y los enlaces a su derecha, y el resto de la pantalla no se mueve

#### Scenario: Sin wifi
- **WHEN** se elige el estado sin wifi, en horizontal y en vertical
- **THEN** el icono de wifi y el de Zwift se ven tachados, el ancho del grupo no cambia y nada más cambia respecto a la ruta de Zwift

#### Scenario: Batería baja y cargando
- **WHEN** se eligen los estados batería baja y móvil cargando, en horizontal y en vertical
- **THEN** la batería baja se ve en el color de alerta con el relleno mínimo y «9 %», el estado cargando lleva el rayo, y ninguno mueve el resto de la pantalla

#### Scenario: Conjunto centrado en horizontal
- **WHEN** se abre `maquetas/rodaje.html` con la Shimano 105 en automático a la anchura horizontal más estrecha que se soporta
- **THEN** la hora, el wifi y la batería están centrados como un conjunto en la línea superior, y el modo con la transmisión y las conexiones no lo tocan

#### Scenario: Dos temas
- **WHEN** se abren las maquetas de rodaje en tema rodaje y en tema claro
- **THEN** la hora, el porcentaje y los iconos se leen con el contraste de la spec `ui-components`
