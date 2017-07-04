package net.onedaybeard.bitvector

const val WORD_SIZE: Int = 32

/**
 * Performance optimized bitset implementation. Certain operations are
 * prefixed with `unsafe`; these methods perform no validation.
 */
class BitVector : Iterable<Int> {
    /** (can be manipulated directly) */
    var words = IntArray(1)

    constructor()

    /** Creates a bit set based off another bit vector. */
    constructor(copyFrom: BitVector) {
        words = IntArray(copyFrom.words.size)
        for (i in 0..words.size - 1)
            words[i] = copyFrom.words[i]
    }

    fun copy()  = BitVector(this)

    /**
     * @param index the index of the bit
     * @return whether the bit is set
     */
    operator fun get(index: Int): Boolean {
        val word = index.toWordIdx()
        return word < words.size && words[word] and (1 shl index) != 0
    }

    /** @param index the index of the bit to set */
    fun set(index: Int) {
        val word = index.toWordIdx()
        checkCapacity(word)
        words[word] = words[word] or (1 shl index)
    }

    /** @param index the index of the bit to set */
    operator fun set(index: Int, value: Boolean) {
        if (value)
            set(index)
        else
            clear(index)
    }

    /**
     * @param index the index of the bit
     * @return whether the bit is set
     */
    fun unsafeGet(index: Int): Boolean {
        return words[index.toWordIdx()] and (1 shl index) != 0
    }

    /** @param index the index of the bit to set */
    fun unsafeSet(index: Int) {
        val word = index.toWordIdx()
        words[word] = words[word] or (1 shl index)
    }

    /** @param index the index of the bit to set */
    fun unsafeSet(index: Int, value: Boolean) {
        if (value)
            unsafeSet(index)
        else
            unsafeClear(index)
    }

    /** @param index the index of the bit to flip */
    fun flip(index: Int) {
        val word = index.toWordIdx()
        checkCapacity(word)
        words[word] = words[word] xor (1 shl index)
    }

    /**
     * Grows the backing array so that it can hold the requested
     * bits. Mostly applicable when relying on the `unsafe` methods,
     * including [unsafeGet] and [unsafeClear].
     *
     * @param bits number of bits to accomodate
     */
    fun ensureCapacity(bits: Int) {
        checkCapacity(bits.toWordIdx())
    }

    private fun checkCapacity(wordIndex: Int) {
        if (wordIndex >= words.size) {
            words = IntArray(wordIndex + 1).also { a ->
                words.forEachIndexed { idx, bits -> a[idx] = bits }
            }
        }
    }

    /**
     * @param index the index of the bit to clear
     */
    fun clear(index: Int) {
        val word = index.toWordIdx()
        if (word >= words.size) return
        words[word] = words[word] and (1 shl index).inv()
    }

    /** @param index the index of the bit to clear */
    fun unsafeClear(index: Int) {
        val word = index.toWordIdx()
        words[word] = words[word] and (1 shl index).inv()
    }

    /** Clears the entire bitset  */
    fun clear() {
        for (i in words.indices)
            words[i] = 0
    }

    /**
     * Returns the "logical size" of this bitset: the index of the
     * highest set bit in the bitset plus one. Returns zero if the
     * bitset contains no set bits.
     *
     * @return the logical size of this bitset
     */
    fun length(): Int {
        val bits = this.words
        for (word in bits.indices.reversed()) {
            val bitsAtWord = bits[word]
            if (bitsAtWord != 0)
                return word.bitCapacity() + WORD_SIZE - leadingZeros(bitsAtWord)
        }

        return 0
    }

    /** @return `true` if this bitset contains no set bits */
    val isEmpty: Boolean
        get() = words.all { it == 0 }

    /**
     * Performs a logical **AND** of this target bit set with the
     * argument bit set. This bit set is modified so that each bit in
     * it has the value true if and only if it both initially had the
     * value true and the corresponding bit in the bit set argument
     * also had the value true.
     *
     * @param other a bit set
     */
    fun and(other: BitVector) {
        val commonWords = minOf(words.size, other.words.size)
        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] and other.words[i]
                i++
            }
        }

        if (words.size > commonWords) {
            var i = commonWords
            val s = words.size
            while (s > i) {
                words[i] = 0
                i++
            }
        }
    }


    /**
     * Clears all of the bits in this bit set whose corresponding
     * bit is set in the specified bit set.
     *
     * @param other a bit set
     */
    fun andNot(other: BitVector) {
        val commonWords = minOf(words.size, other.words.size)
        var i = 0
        while (commonWords > i) {
            words[i] = words[i] and other.words[i].inv()
            i++
        }
    }

    /**
     * Performs a logical **OR** of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value `true` if and only if it either already had the value `true`
     * or the corresponding bit in the bit set argument has the
     * value `true`.
     *
     * @param other a bit set
     */
    fun or(other: BitVector) {
        val commonWords = minOf(words.size, other.words.size)
        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] or other.words[i]
                i++
            }
        }

        if (commonWords < other.words.size) {
            checkCapacity(other.words.size)
            var i = commonWords
            val s = other.words.size
            while (s > i) {
                words[i] = other.words[i]
                i++
            }
        }
    }

    /**
     * Performs a logical **XOR** of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has
     * the value true if and only if one of the following statements holds:
     *
     *  * The bit initially has the value true, and the corresponding bit in
     *    the argument has the value `false`.
     *  * The bit initially has the value false, and the corresponding bit in
     *    the argument has the value `true`.
     *
     * @param other
     */
    fun xor(other: BitVector) {
        val commonWords = minOf(words.size, other.words.size)

        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] xor other.words[i]
                i++
            }
        }

        if (commonWords < other.words.size) {
            checkCapacity(other.words.size)
            var i = commonWords
            val s = other.words.size
            while (s > i) {
                words[i] = other.words[i]
                i++
            }
        }
    }

    /**
     * Returns true if the specified BitVector has any bits set to true
     * that are also set to true in this BitVector.
     *
     * @param other a bit set
     * @return boolean indicating whether this bit set intersects the specified bit set
     */
    fun intersects(other: BitVector): Boolean {
        val bits = this.words
        val otherBits = other.words
        var i = 0
        val s = minOf(bits.size, otherBits.size)
        while (s > i) {
            if (bits[i] and otherBits[i] != 0) {
                return true
            }
            i++
        }
        return false
    }

    /**
     * Returns true if this bit set is a super set of the specified set,
     * i.e. it has all bits set to true that are also set to true
     * in the specified BitVector.
     *
     * @param other a bit set
     * @return boolean indicating whether this bit set is a super set of the specified set
     */
    operator fun contains(other: BitVector): Boolean {
        val bits = this.words
        val otherBits = other.words
        val otherBitsLength = otherBits.size
        val bitsLength = bits.size

        for (i in bitsLength..otherBitsLength - 1) {
            if (otherBits[i] != 0) {
                return false
            }
        }

        var i = 0
        val s = minOf(bitsLength, otherBitsLength)
        while (s > i) {
            if (bits[i] and otherBits[i] != otherBits[i]) {
                return false
            }
            i++
        }
        return true
    }

    /** Returns the count of `true` bits */
    fun cardinality(): Int {
        var count = 0
        for (i in words.indices)
            count += bitCount(words[i])

        return count
    }

    override fun iterator(): IntIterator = BitVectorIt(this)

    override fun hashCode(): Int {
        val word = length().toWordIdx()
        var hash = 0
        var i = 0
        while (word >= i) {
            hash = 127 * hash + words[i]
            i++
        }
        return hash
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (this::class != obj::class)
            return false

        val other = obj as BitVector?
        val otherBits = other!!.words

        val commonWords = minOf(words.size, otherBits.size)
        var i = 0
        while (commonWords > i) {
            if (words[i] != otherBits[i])
                return false
            i++
        }

        if (words.size == otherBits.size)
            return true

        return length() == other.length()
    }

    override fun toString(): String {
        val cardinality = cardinality()
        val end = minOf(128, cardinality)

        if (cardinality > 0) {
            val first = "BitVector[$cardinality: {" + take(128).joinToString(separator = ", ")
            val last = if (cardinality > end) " ...}]" else "}]"

            return first + last
        } else {
            return "BitVector[]"
        }
    }

    /**
     * Enumerates over all `true` bits sequeneially. This function
     * performs better than [forEach] and any other functions
     * from `Iterable<Int>`.
     */
    inline fun forEachBit(f: (Int) -> Unit): Unit {
        val w = words
        val size = w.size
        var index = 0

        while (size > index) {
            var bitset = w[index]
            while (bitset != 0) {
                val t = bitset and -bitset
                bitset = bitset xor t
                f((index shl 5) + bitCount(t - 1))
            }

            index++
        }
    }
}
