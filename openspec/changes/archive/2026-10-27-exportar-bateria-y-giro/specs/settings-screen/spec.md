# Spec Delta

## MODIFIED Requirements

### Requirement: Ajustes como pestaña
Ajustes SHALL abrirse desde su pestaña de la barra inferior, sin botón de volver, y SHALL mostrar en este orden: las entradas «Marchas», con la transmisión elegida y su modo, y «Diagnóstico»; la sección «Al rodar»; la sección «Pantalla», con «Tema» y «Orientación»; la sección «Ciclista»; la sección «Rodillo», con «Bajadas» y «Pendiente sin Zwift»; la sección «Zwift», con «Mando de Zwift»; y la sección «Copia de seguridad», con «Exportar configuración» e «Importar configuración». Todo SHALL verse desplazando la pantalla, con la barra inferior siempre visible.

#### Scenario: Abrir Ajustes
- **WHEN** se toca «Ajustes» en la barra inferior
- **THEN** se ve Ajustes con «Marchas», «Diagnóstico» y las secciones Al rodar, Pantalla, Ciclista, Rodillo, Zwift y Copia de seguridad, sin botón de volver

#### Scenario: Entrada de Marchas
- **WHEN** la transmisión es la Shimano en automático y se abre Ajustes
- **THEN** la entrada «Marchas» dice «Shimano 105 · automático»

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
- **THEN** la sección «Copia de seguridad» va después de «Zwift», con «Exportar configuración» e «Importar configuración», cada una con un objetivo táctil de al menos 48 dp
