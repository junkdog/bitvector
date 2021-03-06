package net.onedaybeard.bitvector

fun bitsOf(vararg bits: Int) : BitVector {
    return BitVector().apply { bits.forEach{ set(it) } }
}

operator fun Int.contains(bv: BitVector) : Boolean = bv[this]

internal inline fun Int.toWordIdx() = this ushr 5
internal inline fun Int.bitCapacity() = this shl 5
