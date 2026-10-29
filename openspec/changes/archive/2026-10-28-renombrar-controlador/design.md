# Design

## Context

Ver `proposal.md` (Why). Punto de partida en el código tras archivar `pestana-conexion`:

- **Un icono para dos enlaces.** `Enlace` (`ui/componentes/ConnectionBar.kt`) asigna `ic_sports_esports` a `Square` y a `Mando`; `Zwift` usa `ic_zwift`. En la barra de Rodaje solo se pintan Neo, Square, Pulso y Ventilador (`RodajeUiState.kt`), así que hoy el icono compartido solo se ve repetido en Diagnóstico.
- **Un texto para dos significados.** `servicio_mando` («Mando») lo usan `Dispositivos.kt` (la fila del Square, con `Enlace.Square`) y `Conexion.kt` (el servicio publicado, con `Enlace.Mando`). `tipo_mando` («mando») nombra el tipo en los botones.
- **Diagnóstico** conserva la fila de la plataforma (`Enlace.Zwift`, `ic_zwift`) y «Mando de Zwift» (`Enlace.Mando`). Su refactor agnóstico es otro change.
- **Iconos del sistema de diseño.** Salen de `design-system/icons.svg`; `python3 scripts/design-system/generar.py` emite `app/src/main/res/drawable/ic_*.xml` y borra los que ya no están en el sprite. `i-zwift` e `i-botones` son propios (un solo `<path>` relleno, `viewBox="0 -960 960 960"`); el resto, de Material Symbols.
- **Barra de conexiones** (`ConnectionBar`): iconos de 14 dp, el estado por la forma. Esperando recorta un hueco circular en la esquina inferior derecha (radio de 7 de 24 unidades, centrado en 19,19) con `BlendMode.Clear` y gira un arco dentro; sin conexión recorta una diagonal y va tachado en `danger`; batería baja añade una insignia redonda con «!» en esa misma esquina.

## Goals / Non-Goals

**Goals:**

- Que el dispositivo y el servicio publicado no compartan ni nombre ni icono.
- Que el manillar se lea a 14 dp en los cuatro estados de la barra sin tocar el comportamiento de `ConnectionBar`.
- Que el cambio sea de nombre e icono: ningún flujo, estado ni protocolo cambia.

**Non-Goals:**

- Cambiar identificadores de dominio o paquetes (`Enlace.Mando`, `MandoZwift*`, `EstadoMando*`).
- Dar a Diagnóstico un modelo agnóstico (otro change).

## Decisions

### 1. Nombre e icono por función, no por dispositivo

| Dónde | Nombre | Icono |
|---|---|---|
| Dispositivos (lo que se busca y empareja) | «Controlador» (genérico) | `i-manillar` |
| Estado del dispositivo en la barra de Rodaje (`Enlace.Square`) | «Controlador» en la descripción accesible | `i-manillar` |
| Conexión (lo que la app publica, `Enlace.Mando`) | «Controlador Zwift» | `i-zwift` |

Dispositivos busca «controladores» en general (hoy solo el Elite Square), así que su sección no nombra el modelo ni la plataforma. El servicio publicado sí nombra a Zwift porque es una emulación del controlador de Zwift; si otra plataforma lo admite, el nombre se revisa con la compatibilidad por modo (`ModoPublicado.probadoCon`). **Alternativas descartadas:** «Control» (choca con «Control manual del Neo» y con `ControlRodillo`), «Mando» para los dos (la confusión actual), un nombre sin plataforma también en Conexión (el usuario quiere que se vea que es la emulación del controlador de Zwift) y «Palancas» (el Square tiene más botones que las palancas).

### 2. Iconos de `Enlace`

`Enlace.Square` pasa a `ic_manillar` y `Enlace.Mando` a `ic_zwift`; `Enlace.Zwift` conserva `ic_zwift`. Los dos enlaces de Zwift comparten icono, y eso solo se ve en Diagnóstico, donde cada fila lleva su nombre. En la barra de Rodaje no hay colisión porque ni Zwift ni el controlador publicado están en ella (spec `ride-screen`). El nombre «Controlador Zwift» sigue distinguiendo la fila en Conexión. **Alternativa descartada:** un icono propio para el controlador publicado (los botones de un Click); el usuario eligió el de Zwift.

### 3. El icono `i-manillar`

Se dibuja a medida, porque Material Symbols no trae un manillar de carretera, como `i-zwift` e `i-botones`: un solo `<path>` relleno en `viewBox="0 -960 960 960"`, con el grosor de los iconos Rounded de peso 400 (alrededor de 80 unidades, algo más de 1 dp a 14 dp) y los extremos redondeados. El generador emite `ic_manillar.xml`, y `IconosSincronizadosTest` comprueba que coincide con su fuente.

El usuario eligió la propuesta **frontal** (la otra era de perfil y se retiró) y, con una foto de referencia de un manillar de carretera, la redibujó: una barra recta, dos bajadas en curva amplia que acaban en una punta redondeada corta y un puente central corto (el usuario pidió recortar el puente y las puntas dos veces). Antes, con bajadas rectas y labios largos, parecía una «B» tumbada.

El dibujo tiene que sobrevivir a lo que la barra recorta o pinta: el hueco de «esperando» en la esquina inferior derecha, la diagonal del tachado y la insignia de batería baja en la superior izquierda. Por eso la masa del manillar va hacia arriba y el centro, y lo que cae en la esquina inferior derecha (la punta de la bajada) es lo que se pierde en «esperando» (ver «Riesgos»). Si el generador no admite un comando de trazado que el dibujo necesite, se amplía con un test, y no se baja a otro formato.

### 4. Textos: qué cambia y qué no

- **Cambia** el nombre del tipo y del servicio, y las frases que hablan del dispositivo o de lo publicado: «Controlador», «Controlador Zwift», «Buscar controlador», «Conectado · sin controlador», «controlador, esperando», «Sin controlador recordado», «Busca el Square en Dispositivos para publicar el controlador», las ayudas de Botones y `dispositivos_intro`.
- **No cambia** «Mando de Zwift» en Diagnóstico, «mando» en `mando_de`, ni las frases que citan la lista de la app de entrenamiento («En la lista de mandos, elige…», «el protocolo de los mandos de Zwift»). Es el término que usa la app de entrenamiento; se comprobó con Zwift en PC (28-10-2026), que llama «Mandos» a su lista, así que se conserva.
- **Claves de `strings.xml`:** se parte `servicio_mando` en `servicio_controlador` (Dispositivos) y `servicio_controlador_zwift` (Conexión), y se renombran `tipo_mando` → `tipo_controlador` y `enlace_mando` → `enlace_controlador`, porque cambia su significado. El resto conserva su clave: solo cambia el texto.
- **Rodaje:** la descripción accesible de la barra llama «Controlador» al Square («Conexiones: Neo, conectado; Controlador, sin conexión»), como Dispositivos; lo decidió el usuario al validar la maqueta. El nombre «Square» se conserva en Diagnóstico, que no se toca. Para eso `Rodaje.kt` usa `enlace_controlador_square` en lugar de `square`.

### 5. Specs

Solo cambian las capacidades de la lista de `proposal.md`. Los requisitos de `zwift-controller`, `ftms-bluetooth`, `zwift-ride`, `platform-bridge` y `diagnostics` hablan del protocolo y de la emulación, no de la etiqueta de la UI, y conservan «mando» como término técnico; la tarea de revisión busca en ellos cualquier texto visible que sí deba cambiar y añade su delta.

### 6. Maqueta primero

El change cambia iconos y disposición de la barra, así que sigue «Cambios de UI: maqueta primero» (`AGENTS.md`): se dibuja `i-manillar` en `icons.svg`, se muestra en la guía a 24 y a 14 dp en los cuatro estados de la barra y en los dos temas, se actualizan las maquetas y se espera la aprobación explícita antes de tocar la app. La verificación a 14 dp con los estados por forma se hace además en el OnePlus 8 con la skill `desplegar-movil`, porque la maqueta no sustituye a la densidad real del móvil.

## Risks / Trade-offs

- **El manillar no se lee a 14 dp, o el hueco y la insignia de la esquina lo rompen** → Se dibujaron dos propuestas, se mostraron a 14 dp en los cuatro estados de la barra y el usuario eligió y recortó una; si aun así no se lee en el móvil, se vuelve a dibujar con él; mantener lo esencial (la barra, el puente y una bajada entera) fuera del hueco de la esquina inferior derecha y de la insignia de la superior izquierda. Si ninguna se lee, volver al usuario con una tercera antes de la app.
- **El Square se llama «Controlador» en Dispositivos y en la barra de Rodaje, pero «Square» en Diagnóstico** → Lo acepta el usuario hasta el refactor de Diagnóstico, en otro change.
- **Diagnóstico repite `i-zwift` en dos filas** → Cada fila lleva su nombre; se corrige al refactorizar Diagnóstico, en otro change.
- **«Controlador Zwift» no se corresponde con lo que Zwift llama en su lista** → Se comprueba con el equipo real; es un cambio de solo texto.
- **Textos y tests que citan «Mando» por todo el repositorio** → Búsqueda con `grep` de las claves y los literales (`Mando`, `mando`, `ic_sports_esports`) en `app/src`, `maquetas/`, `design-system/`, `site/` y `docs/`, y los tests de iconos y de textos del grupo 3 detectan lo que quede.
- **«Controlador» es también «driver»** → El contexto (el manillar al lado, Elite Square debajo) lo deja claro.

## Migration Plan

Sin migración: no se guarda ningún nombre ni icono (`estado.json` y `configuracion.json` no cambian). Para volver atrás basta revertir el change; `ic_sports_esports` se restaura con el sprite. El icono del launcher no se toca.
