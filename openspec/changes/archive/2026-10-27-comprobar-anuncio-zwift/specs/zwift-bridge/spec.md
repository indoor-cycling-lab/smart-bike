## ADDED Requirements

### Requirement: Anuncio comprobado en la red
Mientras la app se anuncia en la red wifi y Zwift no está conectado, la app SHALL comprobar que su anuncio responde en esa red, al anunciarse y después cada minuto. SHALL dejar de comprobarlo mientras Zwift esté conectado. Si no responde, SHALL volver a anunciarse una vez y comprobarlo de nuevo antes de avisar. El registro de la sesión SHALL guardar el resultado de cada comprobación.

#### Scenario: Anuncio que responde
- **WHEN** se abre la sesión con wifi y el anuncio responde en la red
- **THEN** Zwift sigue en «Esperando», no aparece ningún aviso y el registro de la sesión dice que el anuncio responde

#### Scenario: Basta con volver a anunciarse
- **WHEN** la primera comprobación falla y, tras volver a anunciarse, el anuncio responde
- **THEN** Zwift sigue en «Esperando», sin aviso, y el registro de la sesión dice que hizo falta volver a anunciarse

#### Scenario: Zwift conectado
- **WHEN** Zwift está conectado a la app
- **THEN** la app no comprueba el anuncio y nunca muestra «No visible», porque la conexión ya demuestra que Zwift la encontró

#### Scenario: Sin wifi
- **WHEN** el móvil no tiene wifi
- **THEN** la app no comprueba el anuncio y Zwift dice «Sin conexión» con la ayuda de conectarse a la misma red, no «No visible»

### Requirement: Zwift no visible
Si el anuncio sigue sin responder después de volver a anunciarse, la app SHALL mostrar Zwift como «No visible» y decir qué hacer: apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciar el móvil. Las marchas SHALL seguir funcionando como sin Zwift. En cuanto el anuncio vuelva a responder, la app SHALL volver a «Esperando» sin que el usuario haga nada más.

#### Scenario: Anuncio que no responde
- **WHEN** se abre la sesión con wifi, Android da el anuncio por registrado y el anuncio no responde en la red, ni siquiera tras volver a anunciarse
- **THEN** en unos 15 s Dispositivos muestra Zwift como «No visible», con el aviso de apagar y volver a encender el wifi del móvil y, si sigue igual, reiniciarlo; las marchas siguen funcionando con la pendiente base

#### Scenario: Reiniciar el wifi
- **WHEN** Zwift está «No visible» y el usuario apaga y vuelve a encender el wifi del móvil
- **THEN** al volver el wifi, la app se anuncia de nuevo, lo comprueba y, si responde, Zwift vuelve a «Esperando» y el aviso desaparece sin tocar la app

#### Scenario: El anuncio deja de responder más tarde
- **WHEN** el anuncio respondía al abrir la sesión, Zwift no se ha conectado y, más tarde, deja de responder
- **THEN** antes de 2 minutos Zwift pasa a «No visible» con el mismo aviso
