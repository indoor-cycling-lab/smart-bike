# Proposal

## Why

El usuario rueda con un ventilador Wahoo KICKR Headwind y no quiere seguir usando la app de Wahoo: hoy, para cambiar entre velocidad manual y automática por pulso, o para ajustar los umbrales de pulso de las zonas automáticas, tiene que cerrar Square Shift y abrir otra app, y con el ventilador ya no se puede subir o bajar el aire sin soltar el manillar. El Headwind ofrece por Bluetooth un servicio de control propio (modo, velocidad y configuración de zonas), así que Square Shift puede ser su único mando: el modo automático lo ejecuta el propio ventilador con la banda de pulso que ya tiene emparejada por ANT+, y la app solo lo configura y lo maneja desde los botones del Square.

## What Changes

- **Ventilador Bluetooth.** La app busca por nombre (`HEADWIND …`), se conecta, lo recuerda (uno solo) y se reconecta sola, como el Square. Sin ventilador recordado, la app se comporta como hasta ahora y no muestra nada nuevo. El estado del ventilador (modo y velocidad) sale de lo que él mismo notifica, no de lo último que la app le envió.
- **Tres acciones nuevas en Botones**, sin botón por defecto: «Ventilador: manual ⇄ automático», «Ventilador: + velocidad» y «Ventilador: − velocidad». En manual, + y − cambian la velocidad de 10 en 10 % entre 0 y 100 %. En automático no cambian nada y avisan «Ventilador en automático». Sin ventilador conectado no hacen nada y lo dicen.
- **Ajustes.** Sección «Ventilador», solo con ventilador recordado: modo (manual o automático), velocidad manual y los umbrales de las cuatro zonas de pulso del modo automático, cada uno con su paso a paso. Los umbrales se editan solo aquí, nunca con los botones del Square.
- **Dispositivos.** Fila «Ventilador» con su estado y «Buscar ventilador», aparte de «Buscar dispositivos» (como el pulsómetro del change `pulso-zwift`, para que quien no tiene ventilador no vea nunca un error), y «Olvidar» cuando hay uno recordado.
- **Rodaje.** Un icono más en la barra de conexiones (`mode_fan`, ya en el sprite), solo con ventilador recordado, y un aviso de la acción como el de «Modelo: Por tabla». Rodaje no muestra la velocidad del ventilador ni controles para él.
- **Diagnóstico.** Fila del ventilador en Conexiones con el modo y la velocidad notificados, y su tráfico en el registro.
- Pasa por maqueta: la sección «Ventilador» de Ajustes (todos sus estados), la fila de Dispositivos, la fila de Diagnóstico y el icono de la barra de Rodaje.

**Fuera de alcance**
- Emparejar o cambiar la banda de pulso del ventilador, y cualquier cosa de sensores emparejados (códigos `09`–`0B` del protocolo). Si algún día hay que cambiar de banda o actualizar el firmware, se hará con la app de Wahoo.
- Los modos del ventilador por velocidad, potencia, temperatura CORE, carrera o híbrido: la app solo maneja manual y pulso. Si el ventilador está en otro modo, la app lo dice y deja pasarlo a manual o a pulso.
- Ejecutar la regla del modo automático en la app: lo hace el ventilador. El pulso que recoge `pulso-zwift` no se usa aquí, y este change no depende de él.
- ANT+ en el móvil.
- Controlar el ventilador desde Zwift o enviarle la velocidad o la potencia del rodillo.
- Mostrar la velocidad del ventilador como dato de Rodaje o en «Datos en pantalla».
- Más de un ventilador, o elegir entre varios a la vista: se conecta al primero que encuentra.
- Cambiar los umbrales con los botones del Square.

## Capabilities

### New Capabilities
- `fan-control`: conexión con el ventilador Headwind y qué se puede hacer con él (modo manual o automático, velocidad manual y umbrales de las zonas de pulso), qué hacen las acciones de los botones según el modo y qué ocurre sin ventilador o al perder la conexión.

### Modified Capabilities
- `button-mapping`: tres acciones asignables nuevas, sin botón por defecto.
- `settings-screen`: la sección «Ventilador» de Ajustes.
- `device-connection`: búsqueda, recuerdo, reconexión, estado y olvido del ventilador, que es opcional.
- `ride-screen`: el icono del ventilador en la barra de conexiones y el aviso de sus acciones.
- `diagnostics`: la fila del ventilador en Conexiones.

## Impact

- **Código:** `:nucleo` (codec del protocolo del ventilador, estado y reglas de las acciones, `Accion` con tres casos nuevos, `DispositivoRecordado` del ventilador en `EstadoGuardado`); `:app` (`VentiladorClient` sobre `ClienteBle`, búsqueda por prefijo `HEADWIND` en `BleScanner`, `Sesion`, `Enlace.Ventilador` en `ConnectionBar`, secciones de `Ajustes`, `Dispositivos` y `Diagnostico`, textos de las acciones). Sin dependencias nuevas ni permisos nuevos: los de Bluetooth ya están.
- **Sistema de diseño y maquetas:** icono del ventilador en `ConnectionBar` y, si hace falta, una variante del selector de modo; cambios en `maquetas/ajustes.html`, `dispositivos.html`, `diagnostico.html`, `rodaje.html`, `rodaje-vertical.html`, `botones.html` y el índice.
- **Documentos de `docs/`:** `funcional.md` (nueva F17 «Ventilador» y § 6 «Fuera de alcance»), `arquitectura.md` (§ 4.3 componentes, § 7 cliente del ventilador y protocolo, lo validado y lo «a validar»), `planificacion.md` (fila 10k «En curso», sin dependencia de `pulso-zwift`) y `desarrollo.md` si cambia el flujo de pruebas.
- **Protocolo:** el del Headwind no es oficial; sale de la ingeniería inversa de la app de Wahoo publicada por otros proyectos (verificada en un solo ventilador, firmware 2.0.43). Se reimplementa a partir de su descripción, sin copiar código, y todo queda «a validar» con el ventilador real.
- **Equipo real:** se prueba en el OnePlus 8 con el Headwind, la app de Wahoo cerrada del todo, el Square y, para el modo automático, la banda HW9 por ANT+ ya emparejada con el ventilador: que la app lo ve por su nombre, que los comandos de modo, velocidad y zonas funcionan, que la configuración se escribe sin estropear el resto de sus campos y que el modo automático sigue a la banda mientras la app está conectada.
