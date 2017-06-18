package net.onedaybeard.bitvector

import java.util.*


internal const val WORD_SIZE: Int = 32

/**
 *
 * Performance optimized bitset implementation. Certain operations are
 * prefixed with `unsafe`; these methods perform no validation.
 */
class BitVector : Iterable<Int> {
    var words = IntArray(1)

    constructor()

    /**
     * Creates a bit set whose initial size is large enough to
     * explicitly represent bits with indices in the range 0 through
     * `nbits - 1`.
     *
     * @param nbits the initial size of the bit set
     */
    constructor(nbits: Int) {
        checkCapacity(nbits.toWordIdx())
    }

    /** Creates a bit set based off another bit vector. */
    constructor(copyFrom: BitVector) {
        words = Arrays.copyOf(copyFrom.words, copyFrom.words.size)
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
        words.fill(0)
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
                return word.bitCapacity() + WORD_SIZE - java.lang.Integer.numberOfLeadingZeros(bitsAtWord)
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
        val commonWords = Math.min(words.size, other.words.size)
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
        val commonWords = Math.min(words.size, other.words.size)
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
        val commonWords = Math.min(words.size, other.words.size)
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
        val commonWords = Math.min(words.size, other.words.size)

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
        val s = Math.min(bits.size, otherBits.size)
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
        val s = Math.min(bitsLength, otherBitsLength)
        while (s > i) {
            if (bits[i] and otherBits[i] != otherBits[i]) {
                return false
            }
            i++
        }
        return true
    }

    fun cardinality(): Int {
        var count = 0
        for (i in words.indices)
            count += java.lang.Integer.bitCount(words[i])

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
        if (javaClass != obj.javaClass)
            return false

        val other = obj as BitVector?
        val otherBits = other!!.words

        val commonWords = Math.min(words.size, otherBits.size)
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
        val end = Math.min(128, cardinality)
        var count = 0

        val sb = StringBuilder()
        sb.append("BitVector[").append(cardinality)
        if (cardinality > 0) {
            sb.append(": {")

            val taken = take(128)
            sb.append(taken.joinToString(separator = ", "))

            if (cardinality > end)
                sb.append(" ...")

            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun forEachBit(f: (Int) -> Unit): Unit {
        var offset = 0
        for (index in words.indices) {
            var bitset = words[index]
            while (bitset != 0) {
                val t = bitset and -bitset
                f(offset + java.lang.Integer.bitCount(t - 1))
                bitset = bitset xor t
            }

            offset += WORD_SIZE
        }
    }
}
