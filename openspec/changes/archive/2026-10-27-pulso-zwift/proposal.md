# Proposal

## Why

El usuario rueda con un pulsómetro de brazo COOSPO HW9 (Bluetooth y ANT+). Su ANT+ ya la usa el ventilador Wahoo KICKR Headwind para ajustar el aire con el pulso, pero Zwift en el PC no recibe el pulso de la banda: no hay otra fuente conectada y, con la banda conectada al móvil, Zwift tampoco podría conectarse a ella si la banda solo admite pocas conexiones Bluetooth. La app ya es el único dispositivo que Zwift ve por wifi (DirCon), así que puede recoger el pulso de la banda por Bluetooth y ofrecérselo a Zwift como una fuente más, sin tocar el ventilador, que sigue con su ANT+ directa. `docs/funcional.md` § 6 deja los pulsómetros fuera de alcance «se puede reconsiderar»: este change lo reconsidera.

## What Changes

- **Pulsómetro Bluetooth.** La app se conecta a un pulsómetro Bluetooth estándar (servicio Heart Rate `0x180D`, medida `0x2A37`) y obtiene los latidos por minuto. Es opcional: sin pulsómetro recordado, la app se comporta como hasta ahora y no muestra nada nuevo.
- **Pulso hacia Zwift.** El rodillo «Square Shift» que Zwift ve por wifi se ofrece también como fuente de pulso. Con Zwift conectado y suscrito, recibe los bpm de la banda; sin banda o sin lectura válida no se le envía nada (nunca un 0, que Zwift leería como 0 bpm).
- **Dispositivos.** Fila nueva «Pulsómetro» con su estado, su botón «Buscar pulsómetro» (aparte de «Buscar dispositivos», para que quien no tiene banda no vea nunca un error) y su ayuda para despertarla. La banda se recuerda, una sola, y se reconecta sola al abrir la app y al perder la conexión.
- **Rodaje.** Un icono más en la barra de conexiones (`favorite`, ya en el sprite), solo si hay un pulsómetro recordado, con los mismos estados que los demás enlaces. El pulso no se muestra como dato en la rejilla.
- **Diagnóstico.** Fila del pulsómetro en Conexiones, con el último pulso leído y la hora, y su tráfico en el registro.
- Pasa por maqueta: el icono del pulso en la barra de Rodaje (todos sus estados, los dos temas, 360 dp y las dos orientaciones), la fila «Pulsómetro» en Dispositivos y en Diagnóstico.

**Fuera de alcance**
- El ventilador: la app no lo controla ni lo lee. Sigue con el ANT+ directo de la banda y no depende del móvil.
- ANT+ en el móvil.
- Mostrar el pulso como dato de la rejilla de Rodaje o en «Datos en pantalla» (change aparte, con su maqueta).
- Zonas, pulso medio o máximo, intervalos RR y cualquier tratamiento o filtro del valor: se reenvía lo que mide la banda.
- Más de un pulsómetro, o elegir de una lista entre varios que se vean a la vez: se conecta al primero que encuentra, como con el Neo y el Square.
- Que Zwift reciba el pulso por Bluetooth (alternativa de `puente-zwift-bluetooth`, change 11): solo wifi.
- Cambiar el nombre «Square Shift» o la identidad del puente.

## Capabilities

### New Capabilities
- `heart-rate`: conexión con un pulsómetro Bluetooth estándar, qué lecturas valen y cuándo se considera que no hay pulso, de modo que el resto de la app trabaje con un bpm válido o con ninguno.

### Modified Capabilities
- `zwift-bridge`: la app se ofrece a Zwift también como fuente de pulso y le reenvía los bpm de la banda.
- `device-connection`: búsqueda, recuerdo, reconexión y estado del pulsómetro, que es opcional.
- `ride-screen`: el icono del pulso en la barra de conexiones.
- `diagnostics`: la fila del pulsómetro en Conexiones y su último pulso.

## Impact

- **Código:** `:nucleo` (decodificación de `0x2A37` y de la salida hacia Zwift, estado de «hay pulso», `DispositivoRecordado` del pulsómetro en `EstadoGuardado`); `:app` (`PulsoClient` sobre `ClienteBle`, búsqueda de la banda por servicio en `BleScanner`, `Sesion`, servicio Heart Rate en `ZwiftBridge` y en el anuncio DirCon, `Enlace.Pulso` en `ConnectionBar`, `Dispositivos`, `Diagnostico`). Sin permisos nuevos: los de Bluetooth ya están.
- **Sistema de diseño y maquetas:** icono del pulso en `ConnectionBar` (`design-system/components/ConnectionBar`), sin componente ni token nuevos; cambios en `maquetas/rodaje.html`, `rodaje-vertical.html`, `dispositivos.html`, `diagnostico.html` y el índice.
- **Documentos de `docs/`:** `funcional.md` (nueva F16, § 6 «Fuera de alcance», F1.4), `arquitectura.md` (§ 4.3, § 7 cliente del pulsómetro, § 7.3 servicio ofrecido a Zwift), `planificacion.md` (fila nueva y sus notas) y `desarrollo.md` si cambia el flujo de pruebas.
- **Equipo real:** lo marcado «a validar» se prueba en el OnePlus 8 con la banda HW9, el Neo y Zwift en PC: que Zwift liste «Square Shift» en la lista de pulso y siga los bpm; cuántas conexiones Bluetooth admite la HW9 con el móvil y el PC a la vez; y que el ventilador no cambia con la app abierta.
