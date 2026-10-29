# Spec Delta

## ADDED Requirements

### Requirement: Confirmar la salida con Atrás
En Rodaje, el primer Atrás SHALL mostrar durante 3 s un aviso de varias líneas con el texto «Para cerrar la aplicación, pulsa Atrás otra vez». Un segundo Atrás dentro de esos 3 s SHALL terminar la sesión (spec `ride-session`) y cambiar el aviso a «Cerrando…» hasta que la app se cierre. Pasados los 3 s sin segundo Atrás, el aviso SHALL desaparecer y el siguiente Atrás SHALL volver a pedir confirmación.

#### Scenario: Primer Atrás
- **WHEN** se rueda en la marcha 14 y se pulsa Atrás una vez
- **THEN** se ve «Para cerrar la aplicación, pulsa Atrás otra vez», la marcha sigue en la 14 y las conexiones siguen activas

#### Scenario: Segundo Atrás a tiempo
- **WHEN** se ve el aviso de salida y, antes de 3 s, se pulsa Atrás de nuevo
- **THEN** el aviso pasa a «Cerrando…» y la app se cierra al terminar la sesión

#### Scenario: Segundo Atrás tarde
- **WHEN** se pulsa Atrás una vez, pasan más de 3 s y se pulsa Atrás de nuevo
- **THEN** el aviso desaparece a los 3 s y la segunda pulsación vuelve a mostrar «Para cerrar la aplicación, pulsa Atrás otra vez», sin terminar la sesión

#### Scenario: Atrás mientras se cierra
- **WHEN** se ve «Cerrando…» y se pulsa Atrás otra vez
- **THEN** no cambia nada y la sesión termina una sola vez

#### Scenario: Otra pantalla entre medias
- **WHEN** se pulsa Atrás en Rodaje, se abre Ajustes desde el icono antes de 3 s y se vuelve a Rodaje
- **THEN** no se ve el aviso de salida, y un Atrás en Rodaje vuelve a pedir confirmación

#### Scenario: Neo o Square desconectados
- **WHEN** el Neo está desconectado y el Square reconectando, y se pulsa Atrás dos veces seguidas
- **THEN** el aviso se comporta igual y la sesión termina sin esperar a ningún dispositivo

### Requirement: Aviso de salida sin mover la pantalla
El aviso de salida SHALL mostrarse flotando, translúcido y centrado en la pantalla de Rodaje, en horizontal y en vertical, sin mover ni cambiar de tamaño ningún otro elemento, de forma que lo que tapa se siga viendo. SHALL leerse entero, en una línea desde 360 dp de ancho y en varias si la pantalla es más estrecha. SHALL NOT depender solo del color y SHALL anunciarse al lector de pantalla. El aviso de acción SHALL poder verse a la vez sin pisarse.

#### Scenario: Sin movimiento
- **WHEN** aparece o desaparece el aviso de salida
- **THEN** la marcha, los datos, la barra de conexiones y los botones − y + no cambian de sitio ni de tamaño

#### Scenario: Texto completo en vertical
- **WHEN** se muestra «Para cerrar la aplicación, pulsa Atrás otra vez» en un móvil de 360 dp de ancho en vertical
- **THEN** el texto se lee entero en una línea, y en un móvil más estrecho se parte en dos sin salirse de la pantalla

#### Scenario: Centrado y translúcido
- **WHEN** se muestra el aviso de salida en horizontal o en vertical
- **THEN** está en el centro de la pantalla y la marcha que queda detrás se sigue viendo

#### Scenario: Lector de pantalla
- **WHEN** se pulsa Atrás con el lector de pantalla activado
- **THEN** el lector dice «Para cerrar la aplicación, pulsa Atrás otra vez» y, al cerrar, «Cerrando»

#### Scenario: Con un aviso de acción
- **WHEN** se ve «+1 marcha» en el pie y se pulsa Atrás
- **THEN** se ven los dos avisos, el de salida en el centro y el de acción en el pie, sin taparse entre sí

#### Scenario: Aviso de acción desactivado
- **WHEN** «Mostrar la acción» está desactivado en Ajustes y se pulsa Atrás
- **THEN** el aviso de salida se ve igual: no depende de ese ajuste
