package net.onedaybeard.bitvector

// ref https://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
internal fun bitCount(bits: Int): Int {
    var v = bits

    v -= (v ushr 1 and 0x55555555)
    v = (v and 0x33333333) + (v ushr 2 and 0x33333333)
    return (v + (v ushr 4) and 0xf0f0f0f) * 0x1010101 ushr 24
}


// ref https://graphics.stanford.edu/~seander/bithacks.html#IntegerLog
internal fun leadingZeros(bits: Int): Int {
    if (0 == bits) return 32

    var v = bits
    var r = 0

    val f = { shift: Int ->
        v = v ushr shift
        r = r or shift
    }

    if (((v ushr 16) and 0xffff) > 0) f(16)
    if ((v and 0x0000ff00) > 0) f(8)
    if ((v and 0x000000f0) > 0) f(4)
    if ((v and 0x0000000c) > 0) f(2)
    if ((v and 0x00000002) > 0) f(1)

    return 31 - r
}