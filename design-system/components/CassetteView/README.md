# CassetteView

El cassette visto de perfil: una barra por piñón, con altura proporcional a sus dientes, y el piñón activo resaltado. Sustituye a `GearLadder` cuando la transmisión es física.

**Recibe:** dientes de cada piñón y el piñón activo.

**Uso**
- Debajo de `GearDisplay`, de ancho completo en la zona de marcha. Altura 48dp (56dp en vertical).
- Piñón mayor (más ligero) a la izquierda y menor (más duro) a la derecha, igual que `GearLadder`.
- Altura de cada barra: 28 % + 72 % × dientes / dientes del mayor, para que el piñón pequeño siga siendo visible. En HTML se pasa como la variable `--cog` de cada `sq-cog` (`<i class="sq-cog" style="--cog: 64%"></i>`): es un dato del cassette elegido, no un estilo, y es el único atributo `style` que admiten las maquetas.
- Piñón activo en `gear-fill`; los demás en `surface-2`. Bajo cada barra, sus dientes en `numeric` 13sp, el activo en `gear`.
- Para lectores de pantalla, un único `role="img"` con «Cassette 11 a 36, piñón 17».

**No**
- No dibujar dientes ni perspectiva: es un indicador, no una ilustración.
- No hacerlo interactivo en rodaje.
