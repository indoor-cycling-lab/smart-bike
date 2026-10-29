## Why

Rodaje oculta las barras del sistema, así que mientras se rueda no se ven la hora, el wifi ni la batería del móvil. Con la pantalla siempre encendida y el móvil en el soporte, quedarse sin batería a mitad de ruta es un riesgo real y no hay forma de saberlo sin deslizar desde el borde. El wifi importa más aún: es el transporte del puente con Zwift (DirCon, F8.5), y un wifi caído es lo primero que se comprueba cuando Zwift sale «No visible» (F1.4). La hora, además, es lo que se mira en una sesión larga. Hace falta devolver esos tres datos a Rodaje sin restar altura a la marcha ni a los datos (F11).

## What Changes

- **Indicadores del móvil en Rodaje**, a la vista aunque las barras del sistema sigan ocultas:
  - **Hora** en formato `hh:mm` de 24 horas, sin segundos, del reloj del sistema.
  - **Wifi**: un icono que dice si el móvil tiene wifi. Normal si lo hay; tachado, en el color de alerta, si no.
  - **Batería**: un icono con el nivel (un contorno con el relleno proporcional a la carga, como el del sistema) y el porcentaje al lado. Con el móvil cargando lleva un rayo. Por debajo del 15 % el icono pasa al color de alerta y el relleno queda en una franja mínima.
  - El estado se lee por la forma y el número, nunca solo por el color.
- **Disposición en vertical.** En la franja superior (la de la cámara y el estado del móvil), el grupo hora, wifi y batería va a la izquierda, con aire, y los enlaces de la app siguen a la derecha. El grupo se coloca según dónde esté la cámara:
  - con la cámara a la izquierda (como el OnePlus 8), a su derecha;
  - con la cámara en el centro, a su izquierda;
  - sin recorte en la franja, desde el margen normal.
  
  Nunca ocupa el hueco de la cámara.
- **Disposición en horizontal.** En la línea superior, el conjunto de la hora, el wifi y la batería va centrado. El modo con la transmisión sigue a la izquierda y las conexiones con Ajustes, a la derecha.
- **Sin separador.** Lo del móvil y lo de la app se distinguen por la posición. Los indicadores del móvil son solo informativos y quedan fuera del objetivo táctil de la barra de conexiones: tocar la hora, el wifi o la batería no abre nada.
- **Descripción accesible propia**, aparte de la de las conexiones: «14:32, wifi conectado, batería 82 %, cargando».
- Rodaje sigue sin barras del sistema y con la pantalla encendida.

Fuera de alcance:
- Mostrar la barra de estado del sistema, con sus notificaciones, en Rodaje.
- Intensidad de la señal del wifi, nombre de la red o datos móviles: solo hay o no hay wifi.
- Los segundos, la fecha y el formato de 12 horas, y un ajuste para ocultar o elegir los indicadores.
- Un aviso emergente o sonoro con la batería baja: solo cambia el icono.
- La batería del Neo, del Square o de otros accesorios (change 10b, batería del Square).
- Mostrar estos indicadores fuera de Rodaje: el resto de pantallas ya tienen la barra de estado del sistema.
- Cambiar la barra de conexiones (10f) o sus estados.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `ride-screen`: requisitos nuevos «Hora, wifi y batería del móvil en Rodaje», «Colocación vertical de los indicadores del móvil», «Colocación horizontal de los indicadores del móvil» e «Indicadores del móvil solo informativos»; «Disposición en vertical» incluye el grupo del móvil a la izquierda de la franja superior. Rodaje sigue sin las barras del sistema y con la pantalla encendida, así que esos requisitos no cambian.
- `ui-components`: requisito nuevo «Indicadores de estado del móvil».
- `screen-mockups`: requisito nuevo «Indicadores del móvil en las maquetas de rodaje», con estados para sin wifi, batería baja, cargando y cámara en el centro.
- `style-guide`: «Iconos en la pantalla de rodaje» admite también los iconos de wifi y batería; requisito nuevo «Indicadores del móvil en la guía».

## Impact

- Funcionalidades: F11 (pantalla de rodaje) y F1.4 (el wifi se ve de un vistazo). `docs/funcional.md` gana la nota correspondiente.
- Sistema de diseño:
  - Componente nuevo `DeviceStatus` (`sq-devstatus`), con `README.md` y `preview.html`.
  - Iconos nuevos en `icons.svg`: `wifi`, `wifi_off`, la batería y sus variantes. Son iconos de Material, y la batería se compone con el nivel.
  - Pautas de iconografía en `design-system/README.md` y de `RideScreen`.
  - Sin tokens nuevos.
- Maquetas: `rodaje.html`, `rodaje-vertical.html` (todos sus estados más los nuevos) e `index.html`.
- App:
  - Composable `DeviceStatus` en `ui/componentes`.
  - `RodajeUiState` con la hora, el wifi y la batería.
  - Un lector de la batería del móvil (`BatteryManager`), un reloj por minutos y el wifi de `AdaptadorWifi` (`EstadoWifi.direccion`).
  - `Rodaje.kt`: la franja superior y la línea superior en horizontal.
  - Lectura del recorte de pantalla (`WindowInsets.displayCutout`) para colocar el grupo en vertical.
  - Drawables generados y cadenas nuevas.
- Prueba con el equipo real: en el OnePlus 8 en el soporte, en vertical y en horizontal, rodando con el Neo y el Square: ver la hora y su cambio al minuto, apagar y encender el wifi, conectar y desconectar el cargador, y comprobar a 1–2 m que el grupo se lee y no tapa la cámara. La cámara centrada no se puede probar con ese móvil: queda «a validar» y se cubre con un test de la colocación.
- Documentos de `docs/`:
  - `funcional.md`: nota en F11 y F1.4.
  - `arquitectura.md`: componentes de UI, el lector de batería y el reloj, y el recorte en Rodaje.
  - `planificacion.md`: fila nueva 10h y sus notas.
  - `desarrollo.md`: se revisa al final; no se esperan cambios.
