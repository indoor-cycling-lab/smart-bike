# Spec Delta

## MODIFIED Requirements

### Requirement: Bluetooth apagado
Si el Bluetooth del móvil está apagado, la app SHALL decirlo y ofrecer activarlo; al apagarse con dispositivos conectados o reconectándose, SHALL mostrarlos sin conexión y, al volver a encenderse, SHALL conectarse sola a los configurados.

#### Scenario: Bluetooth apagado al buscar
- **WHEN** el Bluetooth está apagado y el usuario toca «Buscar» en cualquier sección
- **THEN** aparece el aviso de error «El Bluetooth está apagado. Actívalo para buscar dispositivos.» con el botón «Activar Bluetooth», y no se busca nada

#### Scenario: Bluetooth apagado durante la sesión
- **WHEN** el Neo y el Square están conectados y el usuario apaga el Bluetooth
- **THEN** las dos filas dicen «Sin conexión» y aparece el aviso de Bluetooth apagado

#### Scenario: Bluetooth encendido de nuevo
- **WHEN** el Bluetooth se apagó durante la sesión con la marcha 14 y el usuario lo vuelve a encender
- **THEN** el aviso desaparece, el Neo y el Square se conectan solos y el Neo recibe la resistencia de la 14

#### Scenario: Bluetooth apagado con el Square dormido
- **WHEN** el Square está configurado pero dormido, la app espera a que se despierte para conectarse y el usuario apaga y vuelve a encender el Bluetooth
- **THEN** la fila del Square vuelve a «Conectando» y, al pulsar un botón del Square, se conecta sin reiniciar la app ni buscarlo otra vez

## ADDED Requirements

### Requirement: Dispositivo configurado inválido
Si un dispositivo configurado tiene una dirección que el móvil no puede usar (por ejemplo, tras importar una configuración dañada), la app SHALL seguir abierta, SHALL mostrarlo sin conexión y SHALL permitir buscarlo de nuevo u olvidarlo.

#### Scenario: Dirección inválida al abrir
- **WHEN** el Neo configurado tiene una dirección inválida y se abre la app
- **THEN** la app abre sin cerrarse, la fila del rodillo dice «Sin conexión», el registro anota la dirección inválida y el Square se conecta con normalidad

#### Scenario: Buscarlo de nuevo
- **WHEN** el rodillo configurado tiene una dirección inválida y el usuario toca «Buscar» con el Neo despierto
- **THEN** la app encuentra el Neo, lo configura con su dirección real y se conecta
