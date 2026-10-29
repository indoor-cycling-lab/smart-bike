# Spec Delta

## Purpose

Pantalla de Ajustes de la app: qué secciones tiene, cómo se llega a ella y cómo se guardan y aplican los ajustes. Empieza con la sección Rodillo y el ajuste «Bajadas».

## ADDED Requirements

### Requirement: Ajustes desde Diagnóstico
Mientras no exista la navegación inferior, Ajustes SHALL abrirse desde Diagnóstico, con un botón de volver a Diagnóstico, y SHALL mostrar solo la sección Rodillo con el ajuste «Bajadas».

#### Scenario: Ida y vuelta
- **WHEN** en Diagnóstico se toca «Ajustes», luego volver
- **THEN** se abre Ajustes con la sección Rodillo y después se vuelve a Diagnóstico, sin terminar la sesión

#### Scenario: Atrás del sistema
- **WHEN** Ajustes está abierta y se pulsa Atrás
- **THEN** se vuelve a Diagnóstico

### Requirement: Ajuste «Bajadas»
La sección Rodillo SHALL ofrecer el ajuste «Bajadas» con tres opciones excluyentes, cada una con una ayuda de una línea: «Sin protección», «La marcha no crea bajadas» y «Nunca empujar». La opción elegida SHALL distinguirse por algo más que el color.

#### Scenario: Valor por defecto
- **WHEN** se abre Ajustes por primera vez
- **THEN** está elegida «La marcha no crea bajadas»

#### Scenario: Elegir otra opción
- **WHEN** se toca «Nunca empujar»
- **THEN** queda elegida «Nunca empujar» y las otras dos dejan de estarlo

#### Scenario: Objetivo táctil
- **WHEN** se toca cualquier punto de la fila de una opción, incluida su ayuda
- **THEN** se elige esa opción

### Requirement: Ajuste aplicado al momento
Al cambiar el ajuste «Bajadas», la app SHALL aplicarlo a la pendiente que envía al Neo sin reiniciar la sesión ni cambiar la marcha, y SHALL enviar la pendiente nueva si cambia.

#### Scenario: Cambiar de modo rodando
- **WHEN** se rueda sin Zwift en la marcha 5 con «Sin protección» (el Neo simula −7 %) y se elige «La marcha no crea bajadas»
- **THEN** el Neo pasa a simular 0 % y la marcha sigue siendo la 5

#### Scenario: Sin Neo conectado
- **WHEN** el Neo no está conectado y se cambia el ajuste
- **THEN** el ajuste queda elegido y el Neo recibe la pendiente con el modo nuevo en cuanto se conecta

### Requirement: Ajustes guardados
Los ajustes SHALL guardarse en el móvil al cambiarlos y SHALL seguir vigentes al volver a abrir la app. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto.

#### Scenario: Volver a abrir la app
- **WHEN** se elige «Nunca empujar» y se cierra y se vuelve a abrir la app
- **THEN** sigue elegida «Nunca empujar» y se aplica desde el primer envío al Neo

#### Scenario: Configuración anterior al ajuste
- **WHEN** se abre la app con una configuración guardada antes de que existiera el ajuste «Bajadas»
- **THEN** se conservan los demás ajustes y «Bajadas» está en «La marcha no crea bajadas»
