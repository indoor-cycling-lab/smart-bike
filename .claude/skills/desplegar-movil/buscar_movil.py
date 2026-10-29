#!/usr/bin/env python3
"""Encuentra la dirección de la depuración inalámbrica del móvil y la imprime como <ip>:<puerto>.

1. Busca el servicio `_adb-tls-connect._tcp` con `dns-sd`, el mDNS nativo de macOS, que encuentra el móvil aunque
   `adb mdns services` no lo vea.
2. Si el móvil no se anuncia, prueba en paralelo los puertos 30000–49999 de la última IP encontrada (guardada en
   movil.local, junto a este script y fuera del repositorio): el puerto cambia cada vez que se activa la depuración.

No usa adb: la conexión la hace el agente con `adb connect`, para que pase por el hook adb_guard.
Uso: python3 buscar_movil.py
"""
import asyncio
import re
import subprocess
import sys
from pathlib import Path

FICHERO = Path(__file__).with_name("movil.local")
SERVICIO = "_adb-tls-connect._tcp"
PUERTOS = range(30000, 50000)


def dns_sd(*args, segundos=3.0):
    """Salida de `dns-sd <args>` durante unos segundos (dns-sd no termina solo)."""
    p = subprocess.Popen(["dns-sd", *args], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True)
    try:
        salida, _ = p.communicate(timeout=segundos)
    except subprocess.TimeoutExpired:
        p.kill()
        salida, _ = p.communicate()
    return salida


def por_mdns():
    instancias = {m.group(1).strip() for m in re.finditer(r"\sAdd\s+\d+\s+\d+\s+\S+\s+" + re.escape(SERVICIO) + r"\.\s+(.+)", dns_sd("-B", SERVICIO, "local."))}
    direcciones = []
    for instancia in sorted(instancias):
        m = re.search(r"can be reached at (\S+?)\.?:(\d+)", dns_sd("-L", instancia, SERVICIO, "local."))
        if not m:
            continue
        host, puerto = m.groups()
        ip = re.search(r"\s(\d+\.\d+\.\d+\.\d+)\s", dns_sd("-G", "v4", host))
        if ip:
            direcciones.append((ip.group(1), int(puerto)))
    return direcciones


async def abierto(ip, puerto, semaforo):
    async with semaforo:
        try:
            _, w = await asyncio.wait_for(asyncio.open_connection(ip, puerto), 0.5)
            w.close()
            return puerto
        except (OSError, asyncio.TimeoutError):
            return None


async def por_puertos(ip):
    semaforo = asyncio.Semaphore(1000)
    return [(ip, p) for p in await asyncio.gather(*(abierto(ip, p, semaforo) for p in PUERTOS)) if p]


def main():
    direcciones = por_mdns()
    if not direcciones and FICHERO.exists():
        direcciones = asyncio.run(por_puertos(FICHERO.read_text().strip()))
    if not direcciones:
        sys.exit("No se encuentra el móvil: comprueba que está en la misma wifi que el Mac y que la depuración "
                 "inalámbrica está activada.")
    FICHERO.write_text(direcciones[0][0] + "\n")
    for ip, puerto in dict.fromkeys(direcciones):
        print("%s:%d" % (ip, puerto))


if __name__ == "__main__":
    main()
