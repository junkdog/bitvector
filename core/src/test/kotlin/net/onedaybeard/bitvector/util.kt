package net.onedaybeard.bitvector

import java.util.*

fun BitSet.toBitVector(): BitVector {
    val bv = BitVector()

    var bit = nextSetBit(0)
    bv[bit] = true
    while (bit >= 0) {
        bv[bit] = true
        bit = nextSetBit(bit + 1)
    }

    return bv
}
