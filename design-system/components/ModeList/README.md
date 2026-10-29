# ModeList

Cómo se elige, en Conexión, de qué forma publica la app sus servicios para que la app de entrenamiento se conecte a ellos. Un solo modo a la vez, siempre implementado. No se elige plataforma: las apps de entrenamiento con las que se ha probado cada modo van como etiquetas de compatibilidad.

**Piezas**
- `fieldset` con `legend` «Modo» y un `sq-list` de `sq-opt` (radio con descripción de `SettingsList`). El elegido lleva el anillo `gear` y el radio con aro y punto `gear`, así que se distingue sin depender del color.
- Nombre del modo por su transporte: «Wifi + Bluetooth», «Solo Bluetooth», «Solo wifi (modo Zwift Ride)».
- Primera línea de ayuda (`sq-caption`): por dónde va cada servicio, en palabras («Rodillo y pulso por wifi; mando por Bluetooth»).
- Segunda línea (`sq-caption`): el detalle técnico para quien empareja (FTMS, DirCon) y lo que hay que saber del modo («no necesita wifi», «la app de entrenamiento mostrará su propia marcha»).
- Marca de recomendado: `sq-tag` «Recomendado» a continuación del nombre.
- Compatibilidad: `sq-compat` con un `sq-tag` por app probada («**Zwift:** validado», «**Zwift:** validado en parte») y, en los modos con FTMS, «**Otras apps con FTMS:** a validar». Se lee por el texto, nunca por el color.

**Uso**
- Solo se ofrecen modos implementados; lo que no existe no se enseña desactivado.
- Solo se etiqueta lo probado con el equipo real. Lo no probado no aparece, salvo la etiqueta genérica «a validar».
- Lo que depende del modo («Nombre ante la app») va debajo, en su propia sección.
- Objetivos táctiles de `touch-min` (48 dp); la fila entera es el objetivo.

**No**
- No usar casillas ni interruptores para los modos: son excluyentes.
- No elegir plataforma ni nombrar una app como compatible sin haberla probado.
