# Spec Delta

## ADDED Requirements

### Requirement: Ventilador en Conexiones
La sección Conexiones de Diagnóstico SHALL añadir una fila del ventilador solo si hay uno recordado, con su estado, su modo y su velocidad tal como los comunica el ventilador y cuánto hace de su última notificación, o «—» si no hay ninguna. Al reconectar SHALL mostrar un aviso informativo, no de error. El tráfico del ventilador, lo enviado y lo recibido, SHALL verse en el tráfico en crudo y en el registro de sesión con su propio origen, igual que el de los demás dispositivos.

#### Scenario: Sin ventilador recordado
- **WHEN** no hay ventilador recordado y se abre Diagnóstico
- **THEN** Conexiones no muestra ninguna fila del ventilador

#### Scenario: Ventilador conectado
- **WHEN** el ventilador está conectado en manual al 40 %
- **THEN** su fila dice «Conectado · Manual · 40 %» y hace cuánto fue la última notificación

#### Scenario: Ventilador reconectando
- **WHEN** el ventilador recordado se ha desconectado
- **THEN** su fila dice «Reconectando» con el último modo y velocidad conocidos y su antigüedad, y un aviso informativo

#### Scenario: Tráfico del ventilador
- **WHEN** se pulsa «Ventilador: + velocidad» con «Tráfico en directo» activada
- **THEN** el tráfico muestra la orden enviada al ventilador y su respuesta, con el origen del ventilador
