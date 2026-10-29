package app.smartbike.dominio

import app.smartbike.protocolo.SquareButton
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Una función de un mando emulado, de su protocolo (design.md del change `generalizar-plataformas`, decisión 4). Un
 * mando de otro protocolo ignora las funciones que no son suyas. Hoy solo existe [FuncionZwift]; se serializa como ella,
 * para conservar `configuracion.json`, hasta que haya otro protocolo de mando.
 */
@Serializable(with = FuncionMandoSerializer::class)
sealed interface FuncionMando {
    val protocolo: ProtocoloMando
}

object FuncionMandoSerializer : KSerializer<FuncionMando> {
    override val descriptor: SerialDescriptor = FuncionZwift.serializer().descriptor

    override fun serialize(encoder: Encoder, value: FuncionMando) = when (value) {
        is FuncionZwift -> FuncionZwift.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): FuncionMando = FuncionZwift.serializer().deserialize(decoder)
}

/**
 * Una función de un mando oficial de Zwift que la app envía por su mando emulado (F9; design.md del change
 * `mando-zwift-emulado`, decisión 1). Independiente del botón físico: [boton] es solo el de la asignación por defecto
 * (anexo A.1 de `funcional.md`). Los nombres serializados son estables: se guardan en `configuracion.json`.
 */
@Serializable
enum class FuncionZwift(val boton: SquareButton) : FuncionMando {
    @SerialName("arriba")
    ARRIBA(SquareButton.CrucetaArriba),

    @SerialName("abajo")
    ABAJO(SquareButton.CrucetaAbajo),

    @SerialName("izquierda")
    IZQUIERDA(SquareButton.CrucetaIzquierda),

    @SerialName("derecha")
    DERECHA(SquareButton.CrucetaDerecha),

    @SerialName("a")
    A(SquareButton.A),

    @SerialName("b")
    B(SquareButton.B),

    @SerialName("y")
    Y(SquareButton.Y),

    @SerialName("z")
    Z(SquareButton.Z),

    @SerialName("girar-izquierda")
    GIRAR_IZQUIERDA(SquareButton.Equis),

    @SerialName("girar-derecha")
    GIRAR_DERECHA(SquareButton.Circulo),

    @SerialName("freno-izquierdo")
    FRENO_IZQUIERDO(SquareButton.FrenoIzquierdo),

    @SerialName("freno-derecho")
    FRENO_DERECHO(SquareButton.FrenoDerecho),
    ;

    override val protocolo: ProtocoloMando get() = ProtocoloMando.ZWIFT
}
