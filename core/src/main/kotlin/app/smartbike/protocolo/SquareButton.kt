package app.smartbike.protocolo

/**
 * Los 20 botones del Elite Square, ordenados por bit de menor a mayor. [mascara] es su bit en la máscara de `0x43`,
 * que también representa el conjunto de botones pulsados; [contador] es la posición de su contador de 4 bits en
 * `0x45` (validadas las dos con el Square real).
 */
enum class SquareButton(val mascara: UInt, val contador: Int) {
    PalancaIzquierda2(0x0000_0001u, 9),
    PalancaIzquierda1(0x0000_0002u, 8),
    CrucetaIzquierda(0x0000_0100u, 1),
    CrucetaArriba(0x0000_0200u, 0),
    CrucetaDerecha(0x0000_0400u, 3),
    CrucetaAbajo(0x0000_0800u, 2),
    Cuadrado(0x0000_1000u, 5),
    Equis(0x0000_2000u, 4),
    FrenoIzquierdo(0x0000_4000u, 7),
    CampagnoloIzquierdo(0x0000_8000u, 6),
    PalancaDerecha2(0x0001_0000u, 19),
    PalancaDerecha1(0x0002_0000u, 18),
    A(0x0100_0000u, 11),
    Y(0x0200_0000u, 10),
    Z(0x0400_0000u, 13),
    B(0x0800_0000u, 12),
    Triangulo(0x1000_0000u, 15),
    Circulo(0x2000_0000u, 14),
    FrenoDerecho(0x4000_0000u, 17),
    CampagnoloDerecho(0x8000_0000u, 16),
    ;

    /** Los Campagnolo hacen de modificadores en las combinaciones (funcional.md, anexo A.2). */
    val esModificador: Boolean get() = this == CampagnoloIzquierdo || this == CampagnoloDerecho

    companion object {
        /** Botones pulsados en una máscara, en orden de bit. Los bits que no son de ningún botón se ignoran. */
        fun pulsados(mascara: UInt): List<SquareButton> = entries.filter { mascara and it.mascara != 0u }
    }
}
