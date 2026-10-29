# HexLog

Registro en crudo de lo que entra y sale por Bluetooth, para la pantalla de diagnóstico.

**Recibe:** lista de tramas con hora (con milésimas), origen (`Square`, `Neo`, `Zwift`, `Mando`), dirección, característica y bytes.

**Uso**
- Fuente `mono`, columnas fijas: hora (`12:04:31.208`), origen, característica (UUID corto: `0x43`, `fec3`, `2A63`) y bytes. Hora, origen y característica en `ink-muted`; lo que la app envía («→ Neo») marca el origen en `gear`.
- Los bytes que cambian respecto a la trama anterior de la misma característica se resaltan en `gear`.
- Alto máximo de 12 filas. Desplazamiento horizontal y vertical dentro del bloque, nunca de la pantalla.
- Las más recientes arriba.
- Sin tramas, el bloque dice «Sin tráfico todavía».
- Botones «Pausar» (que pasa a «Reanudar»), «Limpiar» y «Compartir registro», como secundarios.
