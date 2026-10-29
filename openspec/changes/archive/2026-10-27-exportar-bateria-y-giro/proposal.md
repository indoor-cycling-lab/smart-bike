# Proposal

## Why

Tres cosas quedaron fuera del change 10 y siguen pendientes (`docs/planificacion.md`, fila 10b): la configuración no se puede sacar del móvil ni volver a ponerla (F12.6), de modo que cambiar de móvil o reinstalar la app obliga a rehacer todos los ajustes; la batería del Square solo se escribe en el registro, así que el usuario se entera de que está baja cuando el Square se apaga a mitad de una ruta; y al girar en Zwift con X o Círculo (hoy `Girar` del mando emulado) no hay ninguna señal en la pantalla de que el giro está pulsado. Ya no hay dependencias pendientes, y los tres cambios son pequeños y se prueban con el equipo real en la misma sesión.

## What Changes

- **Exportar e importar la configuración (F12.6).** Nueva sección «Copia de seguridad» al final de Ajustes, con «Exportar configuración» e «Importar configuración». Se exporta el documento de configuración a un archivo JSON que elige el usuario con el selector de archivos del sistema; al importar se valida el archivo entero antes de aplicar nada, se pide confirmación y se explica el motivo si no es válido. Los dispositivos recordados (dependen del móvil) no se exportan.
- **Batería del Square.** La app guarda el último nivel válido (las lecturas periódicas, no la de justo después de conectar), lo muestra en la fila del Square de Diagnóstico y, por debajo del umbral, avisa en Rodaje con una variante del icono del Square en la barra de conexiones («Square, batería baja» en su descripción accesible) y un aviso informativo en Diagnóstico. No se muestra el porcentaje en Rodaje. El umbral es provisional (20 %).
- **Aviso de giro.** Mientras se mantiene el giro a la izquierda o a la derecha del mando de Zwift (X o Círculo con la asignación por defecto, o cualquier botón al que se asigne), Rodaje lo indica con una flecha a cada lado de la marcha, sin mover nada y sin tapar la marcha.
- Pasa por maqueta: «Copia de seguridad» en Ajustes (con sus estados de confirmación, éxito y error), el indicador de giro en Rodaje, el icono del Square con batería baja y la fila del Square en Diagnóstico.

**Fuera de alcance**
- Exportar o importar los dispositivos recordados, la última marcha o el registro de sesión (el registro es el change 16, F13.4).
- Copia automática, nube o compartir el archivo con otras apps como flujo propio: se usa solo el selector de archivos del sistema.
- Mostrar el porcentaje de la batería del Square en Rodaje, o avisar de la batería del Neo.
- Decidir el umbral definitivo de batería: queda provisional hasta la prueba de 2 h del change 6 (`planificacion.md`, decisión pendiente).
- El aviso de giro cuando el Square llega a Zwift emparejado directamente (con «Mando de Zwift» desactivado): la app no envía el giro, así que no lo indica.
- Indicar el freno de Zwift (las manetas): solo el giro.

## Capabilities

### New Capabilities
- `settings-backup`: exportar la configuración a un archivo e importarla de vuelta, con validación completa, confirmación y mensajes de error.
- `square-battery`: nivel de batería del Square (qué lecturas valen, cuándo se considera baja y qué se avisa), de forma que el usuario se entere antes de que se apague.

### Modified Capabilities
- `settings-screen`: la sección «Copia de seguridad» y su lugar en el orden de Ajustes.
- `ride-screen`: el aviso de giro y el icono del Square con batería baja en la barra de conexiones.
- `diagnostics`: el nivel de batería y el aviso de batería baja en la fila del Square de Conexiones.

## Impact

- **Código:** `:nucleo` (lectura estricta del documento de configuración, umbral de batería, estado del giro); `:app` (`SquareClient` y `Sesion` para el nivel de batería y el giro, `Almacen` para exportar e importar, `Ajustes`, `Rodaje`, `ConnectionBar`, `Diagnostico`). Sin permisos nuevos: el selector de archivos del sistema no los pide.
- **Sistema de diseño y maquetas:** componente nuevo `TurnIndicator` y estado «batería baja» de `ConnectionBar` en `design-system/`; cambios en `maquetas/ajustes.html`, `rodaje.html`, `rodaje-vertical.html`, `diagnostico.html` y el índice. Sin tokens ni colores nuevos.
- **Documentos de `docs/`:** `funcional.md` (F12.6, F11, F1.4, batería del Square), `arquitectura.md` (§ 7.1 batería, § 10 exportar e importar, § 9.2 Rodaje, componentes), `planificacion.md` (fila 10b y sus notas) y `desarrollo.md` si cambia el flujo de pruebas.
- **Equipo real:** la lectura periódica de la batería (cada 10 min) y el giro con Zwift se prueban en el OnePlus 8 con el Square y Zwift en PC.
