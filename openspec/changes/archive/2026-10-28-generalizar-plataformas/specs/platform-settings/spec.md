# Spec Delta

## MODIFIED Requirements

### Requirement: Zwift no visible en Plataformas
Cuando la app muestra Zwift como «No visible» (spec `platform-bridge`), Plataformas SHALL mostrar ese estado con el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo, y SHALL quitarlo en cuanto el anuncio vuelva a responder. Dispositivos SHALL NOT mostrar este estado.

#### Scenario: Anuncio que no responde
- **WHEN** el anuncio no responde en la red ni tras volver a anunciarse
- **THEN** Plataformas muestra Zwift como «No visible» con el aviso, y las marchas siguen funcionando como sin Zwift

#### Scenario: Vuelve a responder
- **WHEN** Zwift está «No visible» y el anuncio vuelve a responder
- **THEN** Plataformas pasa a «Esperando» y el aviso desaparece sin tocar nada

### Requirement: Tabla de compatibilidad
Cada plataforma y cada modo de conexión que la app ofrece SHALL tener documentado qué está validado con el equipo real y qué queda «a validar», y la pantalla SHALL NOT presentar como confirmado lo que no lo está. Un modo SHALL ofrecerse solo si está implementado.

#### Scenario: Modos de Zwift
- **WHEN** se consulta la compatibilidad de Zwift
- **THEN** «Rodillo por wifi (FTMS)» y «Todo por wifi (modo Zwift Ride)» constan como validados en Zwift en PC en lo que recogen las specs `platform-bridge`, `zwift-ride` y `zwift-controller`, y lo marcado «a validar» ahí sigue marcado

#### Scenario: Modo sin implementar
- **WHEN** una plataforma o un modo (por ejemplo FTMS por Bluetooth) no está implementado
- **THEN** no aparece en la pantalla

## REMOVED Requirements

### Requirement: Ajustes de Zwift guardados con migración
**Reason**: La app todavía no la usa nadie, así que no se migra la configuración anterior; los ajustes pasan a guardarse por plataforma.
**Migration**: Pasa al requisito «Ajustes guardados por plataforma».

## ADDED Requirements

### Requirement: Ajustes guardados por plataforma
La plataforma elegida y, de cada plataforma, su modo, su mando y su nombre ante la app SHALL guardarse con los demás ajustes y seguir vigentes al reabrir la app. Al volver a elegir una plataforma, SHALL recuperarse lo que tenía. Si lo guardado no se puede leer, la app SHALL empezar con los valores por defecto. Una copia de seguridad SHALL exportarse e importarse con estos ajustes.

#### Scenario: Valores guardados
- **WHEN** se elige «Todo por wifi (modo Zwift Ride)» y «KICKR BIKE PRO» y se cierra y se vuelve a abrir la app
- **THEN** la plataforma es Zwift, el modo es Zwift Ride y el nombre es «KICKR BIKE PRO»

#### Scenario: Cada plataforma recuerda lo suyo
- **WHEN** hay más de una plataforma, se deja una con un modo y un nombre, se elige otra y se vuelve a la primera
- **THEN** la primera recupera el modo, el mando y el nombre que tenía, y la otra los suyos

#### Scenario: Lo guardado no se puede leer
- **WHEN** la configuración guardada no se puede leer
- **THEN** la app empieza con los valores por defecto: Zwift, «Rodillo por wifi (FTMS)», «Mando de Zwift» activado y «Smart Bike», y hay que emparejar de nuevo en Zwift

#### Scenario: Configuración de la versión anterior
- **WHEN** se abre la app, o se importa una copia, con una configuración guardada antes de que los ajustes fueran por plataforma
- **THEN** se conservan los ajustes que no son de la plataforma (marchas, botones, pantalla…), la plataforma, el modo, el mando y el nombre toman los valores por defecto, y hay que emparejar de nuevo en Zwift

#### Scenario: Exportar e importar
- **WHEN** se exporta la configuración con «KICKR BIKE PRO» elegido y se importa en otro móvil
- **THEN** el nombre queda elegido y el móvil nuevo usa su propia identidad, no la del móvil anterior
