package net.onedaybeard.bitvector

internal class BitVectorIt(val bv: BitVector) : IntIterator() {
    var remaining = bv.cardinality()
    var wordIdx = 0
    var word = if (remaining > 0) bv.words[0] else 0

    override fun hasNext() = remaining > 0

    override fun nextInt(): Int {
        while (true) {
            if (word != 0) {
                val t = word and -word
                val nextBit = wordIdx.bitCapacity() + java.lang.Integer.bitCount(t - 1)
                word = word xor t

                remaining--

                return nextBit
            } else {
                word = bv.words[++wordIdx]
            }
        }
    }
}