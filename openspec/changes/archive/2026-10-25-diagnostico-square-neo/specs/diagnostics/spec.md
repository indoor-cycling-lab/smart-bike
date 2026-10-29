# Spec Delta

## Purpose

Pantalla de Diagnóstico de la app y registro de sesión: ver en vivo y en crudo lo que envían el Square y el Neo y lo que la app les manda, para validar el equipo real y analizar problemas.

## ADDED Requirements

### Requirement: Diagnóstico como pantalla inicial
Mientras la app no tenga otras pantallas, SHALL abrirse en Diagnóstico, con sus secciones en este orden: conexiones, botones del Square, tráfico y control manual del Neo; la sección Simulación SHALL NOT aparecer mientras no haya un rodaje en curso.

#### Scenario: Abrir la app
- **WHEN** se abre la app
- **THEN** se ve Diagnóstico con las conexiones del Neo y del Square, sin botón de volver ni sección Simulación

### Requirement: Botones del Square en vivo
Diagnóstico SHALL mostrar los 20 botones del Square como teclas que se ven pulsadas mientras se mantienen, varias a la vez si hace falta, y la máscara de los botones pulsados en hexadecimal.

#### Scenario: Dos botones a la vez
- **WHEN** se mantienen Campag. D y la palanca D1
- **THEN** las dos teclas se ven pulsadas y la máscara dice `0x80020000`

#### Scenario: Square sin conexión
- **WHEN** el Square no está conectado
- **THEN** ninguna tecla se ve pulsada, la máscara dice «—» y la sección dice «Conecta el Square para ver sus botones.»

### Requirement: Última combinación pulsada
Diagnóstico SHALL mostrar los botones que estaban pulsados a la vez en la última pulsación, con el modificador (Campagnolo) primero; mientras no existan los gestos, SHALL NOT mostrar gesto ni acción.

#### Scenario: Combinación con modificador
- **WHEN** se mantiene Campag. D, se pulsa la palanca D1 y se sueltan los dos
- **THEN** en «Último botón» se ve «Campag. D + Palanca D1», y sigue visible después de soltarlos

#### Scenario: Sin pulsaciones todavía
- **WHEN** el Square está conectado y aún no se ha pulsado nada
- **THEN** «Último botón» dice «Ninguno todavía»

### Requirement: Tráfico en crudo
Diagnóstico SHALL mostrar las tramas recibidas del Square y del Neo y las enviadas al Neo, las más recientes arriba, con hora (con milésimas), origen, característica y bytes en hexadecimal; lo que envía la app SHALL distinguirse por el origen «→ Neo» y no solo por el color.

#### Scenario: Notificación de botones
- **WHEN** se pulsa la palanca D1
- **THEN** aparece una fila con la hora, el origen «Square», la característica de botones y los bytes recibidos, con resaltados los que cambian respecto a la trama anterior de la misma característica

#### Scenario: Telemetría del Neo
- **WHEN** se pedalea con el Neo conectado
- **THEN** aparecen filas «Neo» de potencia, cadencia y respuestas FE-C, cada una con su característica

### Requirement: Pausar y limpiar el tráfico
Diagnóstico SHALL permitir pausar la lista de tráfico, para leerla sin que se mueva, y limpiarla; pausar SHALL NOT detener el registro de sesión.

#### Scenario: Pausar
- **WHEN** el usuario toca «Pausar» y sigue pulsando botones
- **THEN** la lista no cambia, el botón dice «Reanudar» y al tocarlo aparecen también las tramas llegadas durante la pausa

#### Scenario: Limpiar
- **WHEN** el usuario toca «Limpiar»
- **THEN** la lista queda vacía y dice «Sin tráfico todavía»

### Requirement: Registro de sesión
La app SHALL registrar con hora monotónica en milisegundos cada trama recibida o enviada al Neo y al Square, los cambios de conexión y los envíos manuales, conservando al menos las últimas 10 000 entradas, y en la compilación de depuración SHALL volcar cada entrada al registro del sistema con una etiqueta propia.

#### Scenario: Capturar una traza
- **WHEN** en la compilación de depuración se pulsan botones del Square y se lee el registro del sistema filtrado por la etiqueta de la app
- **THEN** cada trama aparece con su hora, origen, característica y bytes, en un formato que se puede guardar como traza para los tests

#### Scenario: Registro lleno
- **WHEN** se superan las 10 000 entradas
- **THEN** se descartan las más antiguas y la app sigue registrando sin ralentizarse
