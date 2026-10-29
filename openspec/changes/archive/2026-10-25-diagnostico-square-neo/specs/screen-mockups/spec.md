# Spec Delta

## MODIFIED Requirements

### Requirement: Diagnóstico
La maqueta de Diagnóstico SHALL mostrar las conexiones del Neo y del Square con la acción de buscar, los botones del Square con los pulsados resaltados y la máscara, el último gesto con su acción, la simulación (marcha, modelo, pendiente de Zwift, ajuste, lo enviado al Neo), el tráfico con pausar y limpiar, y el control manual del Neo con el último envío. SHALL tener los estados todo conectado (por defecto), buscando, Neo no encontrado, Square con conexión perdida, Bluetooth apagado, sin permiso, límites del control manual y rodando.

#### Scenario: Último gesto
- **WHEN** se elige el estado rodando
- **THEN** se ve «Campag. D + Palanca D1 · pulsar · +3 marchas» y la sección Simulación

#### Scenario: Sin rodaje en curso
- **WHEN** se abre la maqueta de Diagnóstico sin estado
- **THEN** el Neo y el Square aparecen conectados, «Último botón» muestra «Campag. D + Palanca D1» sin gesto ni acción, y no aparece la sección Simulación

#### Scenario: Dispositivo no encontrado
- **WHEN** se elige el estado Neo no encontrado
- **THEN** la fila del Neo dice «No encontrado», hay un aviso de error que dice qué hacer y los botones de envío al Neo están deshabilitados con su explicación

#### Scenario: Conexión perdida
- **WHEN** se elige el estado Square con conexión perdida
- **THEN** la fila del Square dice «Conexión perdida», ninguna tecla se ve pulsada, la máscara dice «—» y el Neo sigue conectado

#### Scenario: Bluetooth y permisos
- **WHEN** se eligen los estados Bluetooth apagado y sin permiso
- **THEN** cada uno muestra su aviso de error con la acción que lo resuelve («Activar Bluetooth», «Abrir ajustes»)

#### Scenario: Límites del control manual
- **WHEN** se elige el estado de límites
- **THEN** la pendiente está en +20,0 % con su «+» deshabilitado y la potencia en 50 W con su «−» deshabilitado
