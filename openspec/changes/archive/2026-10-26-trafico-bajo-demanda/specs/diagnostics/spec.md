## ADDED Requirements

### Requirement: Tráfico en directo bajo demanda
La sección Tráfico de Diagnóstico SHALL empezar con la casilla «Tráfico en directo» desmarcada cada vez que se abre la pantalla, y sin el volcado ni «Pausar» y «Limpiar» mientras siga desmarcada. Al marcarla SHALL aparecer el volcado con las tramas más recientes, también las llegadas con la casilla desmarcada. Al desmarcarla SHALL ocultarse de nuevo y anularse la pausa. La casilla SHALL conservar su valor al girar el móvil, y ocultar el tráfico SHALL NOT detener el registro de sesión.

#### Scenario: Al abrir Diagnóstico
- **WHEN** el usuario abre Diagnóstico desde Ajustes con el Neo y el Square conectados
- **THEN** la sección Tráfico muestra la casilla «Tráfico en directo» desmarcada, sin volcado ni «Pausar» y «Limpiar»

#### Scenario: Activar el tráfico
- **WHEN** con la casilla desmarcada se pulsa la palanca D1 y después se marca la casilla
- **THEN** aparece el volcado con la fila «Square» de esa pulsación entre las más recientes, y «Pausar» y «Limpiar» están disponibles

#### Scenario: Ocultar el tráfico
- **WHEN** con el tráfico en directo y en pausa el usuario desmarca la casilla, pulsa botones del Square y la vuelve a marcar
- **THEN** el volcado desaparece al desmarcarla y, al marcarla otra vez, muestra en directo las tramas de esas pulsaciones y el botón dice «Pausar»

#### Scenario: Volver a Diagnóstico
- **WHEN** con el tráfico en directo el usuario vuelve a Ajustes y abre Diagnóstico de nuevo
- **THEN** la casilla «Tráfico en directo» está desmarcada y el volcado no se ve

#### Scenario: Girar el móvil
- **WHEN** con el tráfico en directo el usuario gira el móvil
- **THEN** la casilla sigue marcada y el volcado sigue visible

#### Scenario: El registro sigue grabando
- **WHEN** con el tráfico oculto se pedalea con el Neo conectado y se lee el registro del sistema de la compilación de depuración
- **THEN** cada trama del Neo aparece en el registro como con el tráfico visible

#### Scenario: Sin dispositivos
- **WHEN** sin el Neo ni el Square conectados y sin tramas registradas se marca la casilla
- **THEN** el volcado dice «Sin tráfico todavía»

#### Scenario: Conexión perdida con el tráfico visible
- **WHEN** con el tráfico en directo se pierde la conexión con el Square
- **THEN** las filas del Square ya mostradas siguen en el volcado, no llegan filas nuevas suyas y las del Neo siguen apareciendo

#### Scenario: Accesibilidad
- **WHEN** TalkBack recorre la sección Tráfico
- **THEN** anuncia «Tráfico en directo» como casilla con su estado, y toda la fila, de 48 dp de alto como mínimo, la cambia

## MODIFIED Requirements

### Requirement: Tráfico en crudo
Con el tráfico en directo activado, Diagnóstico SHALL mostrar las tramas recibidas del Square, del Neo y de Zwift, y las enviadas al Neo y a Zwift, las más recientes arriba, con hora (con milésimas), origen, característica y bytes en hexadecimal; lo que envía la app SHALL distinguirse por el origen «→ Neo» o «→ Zwift» y no solo por el color.

#### Scenario: Notificación de botones
- **WHEN** se pulsa la palanca D1
- **THEN** aparece una fila con la hora, el origen «Square», la característica de botones y los bytes recibidos, con resaltados los que cambian respecto a la trama anterior de la misma característica

#### Scenario: Telemetría del Neo
- **WHEN** se pedalea con el Neo conectado
- **THEN** aparecen filas «Neo» de potencia, cadencia y respuestas FE-C, cada una con su característica

#### Scenario: Terreno de Zwift
- **WHEN** Zwift envía una pendiente nueva
- **THEN** aparece una fila «Zwift» con el punto de control de FTMS (`2AD9`) y sus bytes, seguida de la respuesta «→ Zwift» y del comando «→ Neo» si cambia lo que se envía al Neo

#### Scenario: Telemetría hacia Zwift
- **WHEN** se pedalea con Zwift conectado
- **THEN** aparecen filas «→ Zwift» con los datos de la bici (`2AD2`) y la potencia (`2A63`)

### Requirement: Pausar y limpiar el tráfico
Con el tráfico en directo activado, Diagnóstico SHALL permitir pausar la lista de tráfico, para leerla sin que se mueva, y limpiarla; pausar SHALL NOT detener el registro de sesión.

#### Scenario: Pausar
- **WHEN** el usuario toca «Pausar» y sigue pulsando botones
- **THEN** la lista no cambia, el botón dice «Reanudar» y al tocarlo aparecen también las tramas llegadas durante la pausa

#### Scenario: Limpiar
- **WHEN** el usuario toca «Limpiar»
- **THEN** la lista queda vacía y dice «Sin tráfico todavía»
