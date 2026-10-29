# Spec Delta

## MODIFIED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, «Botones», con la transmisión de la asignación, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración»; y la sección «Depuración», con «Grabar sesiones», «Espacio libre mínimo» y la entrada «Sesiones grabadas». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible. Ajustes SHALL NOT tener sección «Zwift»: lo que publica la app está en Plataformas.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Botones», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista, Rodillo, Copia de seguridad y Depuración, sin sección «Zwift» y sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

#### Scenario: Entrada de Botones
- **WHEN** la transmisión es la Shimano y se abre Ajustes
- **THEN** la entrada «Botones» dice que es la asignación de «Shimano 105», lleva el icono propio de los cuatro símbolos y al tocarla se abre Botones con un botón de volver a Ajustes

#### Scenario: Abrir Diagnóstico
- **WHEN** se toca «Diagnóstico» en Ajustes
- **THEN** se abre Diagnóstico con un botón de volver a Ajustes

#### Scenario: Pantalla pequeña
- **WHEN** el móvil está en vertical a 360 dp de ancho
- **THEN** las secciones se recorren desplazando la pantalla y la barra inferior sigue visible al pie

#### Scenario: Ajustes en horizontal
- **WHEN** «Horizontal» está elegido y se abre Ajustes
- **THEN** todas las secciones, «Orientación» incluida, se recorren desplazando la pantalla y la barra inferior sigue visible al pie

#### Scenario: Copia de seguridad al final
- **WHEN** se recorre Ajustes hasta el final
- **THEN** la sección «Copia de seguridad» va después de «Rodillo», con «Exportar configuración» e «Importar configuración», cada una con un objetivo táctil de al menos 48 dp, y solo la sección «Depuración» va después

## ADDED Requirements

### Requirement: Depuración en Ajustes
La sección «Depuración» de Ajustes SHALL mostrar el interruptor «Grabar sesiones», apagado por defecto, y debajo «Espacio libre mínimo» con un stepper de 5 % a 50 % de 5 en 5 (10 % por defecto) y su ayuda. Debajo SHALL mostrar el estado de la grabación («Grabando», «Detenida por poco espacio», «Detenida por un error» o nada si está apagada) y la entrada «Sesiones grabadas», que resume cuántas hay y cuánto ocupan y abre la pantalla del mismo nombre con un botón de volver a Ajustes.

#### Scenario: Valores por defecto
- **WHEN** se abre Ajustes por primera vez y se llega a «Depuración»
- **THEN** «Grabar sesiones» está apagado, «Espacio libre mínimo» dice 10 % y «Sesiones grabadas» dice que no hay ninguna

#### Scenario: Estado de la grabación
- **WHEN** hay una grabación en curso y se mira «Depuración»
- **THEN** dice «Grabando» y la entrada «Sesiones grabadas» cuenta la sesión en curso

#### Scenario: Entrada de Sesiones grabadas
- **WHEN** hay tres sesiones que ocupan 41 KB y se mira «Depuración»
- **THEN** la entrada «Sesiones grabadas» dice «3 sesiones · 41 KB» y al tocarla se abre la pantalla con un botón de volver a Ajustes

### Requirement: Ajustes de depuración guardados
«Grabar sesiones» y «Espacio libre mínimo» SHALL aplicarse al momento y guardarse con la configuración. Una configuración guardada antes de estos ajustes SHALL leerse con la grabación apagada y el mínimo en 10 %, conservando el resto.

#### Scenario: Activar y guardar
- **WHEN** se activa «Grabar sesiones», se sube el mínimo a 20 % y se reabre la app
- **THEN** «Grabar sesiones» sigue activado y el mínimo dice 20 %

#### Scenario: Configuración anterior
- **WHEN** se abre un `configuracion.json` guardado antes de estos ajustes
- **THEN** «Grabar sesiones» está apagado y «Espacio libre mínimo» dice 10 %, y el resto de los ajustes se conservan
