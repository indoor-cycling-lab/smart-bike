#!/usr/bin/env python3
"""Guardián de adb para agentes (hook PreToolUse).

El OnePlus 8 de pruebas es el móvil personal del usuario. Este hook deja pasar
solo los comandos adb que tocan la app del proyecto y bloquea el resto, incluso
en modo bypass. Las herramientas MCP con acceso por adb se bloquean todas: los
agentes usan adb desde Bash, donde aplica esta lista.

Política completa y motivos: docs/desarrollo.md, «Restricciones de adb».
"""

import json
import os
import re
import shlex
import sys

# applicationId de la app; cubre también las variantes .debug y .debug.test por prefijo.
PAQUETE = "app.smartbike"

SEPARADORES = {"&&", "||", ";", "|", "&", "(", ")", "\n", "|&", ";;"}
PREFIJOS = {"sudo", "exec", "command", "time", "nohup", "env", "xargs", "builtin"}
SHELLS = {"sh", "bash", "zsh", "dash"}

# Opciones globales de adb que llevan un valor detrás.
OPCIONES_CON_VALOR = {"-s", "-t", "-H", "-P", "-L"}


def es_paquete(nombre):
    return nombre == PAQUETE or nombre.startswith(PAQUETE + ".")


def denegar(motivo):
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": (
                "adb restringido (móvil personal): " + motivo
                + ". Ver docs/desarrollo.md, «Restricciones de adb»; "
                "si hace falta, pide al usuario que lo ejecute con `! <comando>`."
            ),
        }
    }))
    sys.exit(0)


def tokens(comando):
    lexer = shlex.shlex(comando, posix=True, punctuation_chars=";&|()")
    lexer.whitespace_split = True
    lexer.commenters = ""
    return list(lexer)


def segmentos(toks):
    actual = []
    for t in toks:
        if t in SEPARADORES or set(t) <= set(";&|()"):
            if actual:
                yield actual
            actual = []
        else:
            actual.append(t)
    if actual:
        yield actual


def es_asignacion(t):
    return re.match(r"^[A-Za-z_][A-Za-z0-9_]*=", t) is not None


def invocaciones(comando, profundidad=0):
    """Devuelve las listas de argumentos de cada adb invocado en el comando."""
    if profundidad > 3:
        denegar("anidamiento de shells demasiado profundo")
    texto = comando.replace("`", " ; ").replace("$(", " ( ")
    encontradas = []
    for seg in segmentos(tokens(texto)):
        i = 0
        while i < len(seg) and (es_asignacion(seg[i]) or seg[i].lstrip("$") in PREFIJOS):
            i += 1
        if i >= len(seg):
            continue
        prog = os.path.basename(seg[i].lstrip("$"))
        args = seg[i + 1:]
        if prog == "adb":
            encontradas.append(args)
        elif prog in SHELLS and "-c" in args:
            idx = args.index("-c")
            if idx + 1 < len(args):
                encontradas += invocaciones(args[idx + 1], profundidad + 1)
        elif prog == "eval":
            encontradas += invocaciones(" ".join(args), profundidad + 1)
    return encontradas


def ruta_apk_valida(ruta):
    proyecto = os.environ.get("CLAUDE_PROJECT_DIR", os.getcwd())
    absoluta = os.path.realpath(os.path.join(proyecto, ruta))
    return absoluta.startswith(os.path.realpath(proyecto) + os.sep) and \
        "/build/outputs/apk/" in absoluta and absoluta.endswith(".apk")


def validar_shell(args):
    if not args:
        denegar("`adb shell` interactivo no permitido")
    if len(args) == 1 and " " in args[0]:
        args = shlex.split(args[0])
    cmd, resto = args[0], args[1:]
    texto = " ".join(resto)

    if cmd == "am":
        if resto and resto[0] in {"start", "start-activity", "force-stop", "instrument", "kill"}:
            if any(es_paquete(p.split("/")[0]) for p in resto):
                return
        denegar("`am` solo con la app del proyecto")
    if cmd == "pm":
        if resto and resto[0] in {"grant", "revoke", "clear", "path", "dump"} and \
                len(resto) > 1 and es_paquete(resto[1]):
            return
        if resto[:2] == ["list", "packages"] and len(resto) == 3 and resto[2].startswith(PAQUETE):
            return
        denegar("`pm` solo con la app del proyecto (sin desinstalar ni listar otras)")
    if cmd == "run-as":
        if resto and es_paquete(resto[0]):
            return
        denegar("`run-as` solo con la app del proyecto")
    if cmd == "pidof":
        if resto and es_paquete(resto[-1]):
            return
        denegar("`pidof` solo con la app del proyecto")
    if cmd == "dumpsys":
        if resto and resto[0] in {"bluetooth_manager", "battery", "deviceidle", "power", "display"}:
            return
        if resto and resto[0] in {"package", "meminfo", "gfxinfo", "activity"} and \
                any(es_paquete(p) for p in resto):
            return
        denegar("`dumpsys` solo de Bluetooth, batería y energía, o de la app del proyecto")
    if cmd == "screencap":
        denegar("usa `adb exec-out screencap -p > fichero.png`")
    if cmd in {"getprop", "wm"} and (cmd == "getprop" or (resto and resto[0] in {"size", "density"})):
        return
    if cmd == "settings" and resto and resto[0] == "get":
        return
    if cmd == "input" and resto and resto[0] in {"tap", "swipe", "keyevent"}:
        return
    if cmd == "uiautomator" and resto and resto[0] == "dump":
        return
    if cmd == "ls" and texto.startswith("/sdcard/Android/data/" + PAQUETE):
        return
    if cmd == "cat" and texto.startswith("/data/local/tmp/"):
        return
    denegar("`adb shell " + cmd + "` fuera de la lista permitida")


def validar(args):
    i = 0
    while i < len(args) and args[i].startswith("-"):
        i += 2 if args[i] in OPCIONES_CON_VALOR else 1
    if i >= len(args):
        return
    sub, resto = args[i], args[i + 1:]

    if sub in {"devices", "version", "get-state", "get-serialno", "wait-for-device",
               "start-server", "kill-server", "disconnect", "help"}:
        return
    if sub == "mdns" and resto and resto[0] in {"check", "services"}:
        return
    if sub == "connect":
        return
    if sub == "pair":
        denegar("la vinculación la hace el usuario: `! adb pair <ip>:<puerto>`")
    if sub == "install":
        apks = [a for a in resto if not a.startswith("-")]
        if apks and all(ruta_apk_valida(a) for a in apks):
            return
        denegar("solo se instalan APK de */build/outputs/apk/ del proyecto")
    if sub == "uninstall":
        if resto and all(es_paquete(p) for p in resto if not p.startswith("-")):
            return
        denegar("solo se desinstala la app del proyecto")
    if sub == "logcat":
        if any(a == "-c" for a in resto) or any(a.startswith("--pid") for a in resto) or "-s" in resto:
            return
        denegar("`logcat` necesita `--pid=<pid de la app>` o `-s <etiquetas>`; el registro completo incluye otras apps")
    if sub == "exec-out":
        if resto[:2] == ["screencap", "-p"] or resto[:1] == ["screencap -p"]:
            return
        if resto and resto[0] == "run-as" and len(resto) > 1 and es_paquete(resto[1]):
            return
        denegar("`exec-out` solo para `screencap -p` o `run-as` de la app del proyecto")
    if sub == "shell":
        validar_shell(resto)
        return
    if sub == "pull":
        if resto and (resto[0].startswith("/sdcard/Android/data/" + PAQUETE)
                      or resto[0].startswith("/data/local/tmp/")):
            return
        denegar("`pull` solo desde los datos de la app o /data/local/tmp/")
    if sub == "push":
        if len(resto) >= 2 and resto[-1].startswith("/data/local/tmp/"):
            return
        denegar("`push` solo a /data/local/tmp/")
    denegar("`adb " + sub + "` fuera de la lista permitida")


def main():
    try:
        evento = json.load(sys.stdin)
    except Exception:
        sys.exit(0)
    herramienta = evento.get("tool_name", "")

    if herramienta.startswith("mcp__") and "adb" in herramienta:
        denegar("las herramientas MCP con adb no están permitidas; usa adb desde Bash")
    if herramienta != "Bash":
        sys.exit(0)

    comando = evento.get("tool_input", {}).get("command", "")
    if not re.search(r"\badb\b", comando):
        sys.exit(0)
    try:
        lista = invocaciones(comando)
    except ValueError:
        denegar("no se pudo analizar el comando (comillas sin cerrar)")
    for args in lista:
        validar(args)
    sys.exit(0)


if __name__ == "__main__":
    main()
