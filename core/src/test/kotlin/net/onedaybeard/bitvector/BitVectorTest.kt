package net.onedaybeard.bitvector

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail


class BitVectorTest {
    @Test
    fun `test equality`() {
        bitsOf(
            1, 2, 4, 1230, 1323, 1324
        ) assertEquals bitsOf(1, 2, 4, 1230, 1323, 1324)

        val bv = bitsOf(0, 1, 2, 120, 420)
        bv[120] = false

        bitsOf(0, 1, 2, 420) assertEquals bv
    }

    @Test
    fun `get and set`() {
        val bv = BitVector()
        bv[0] = true
        bv[2] = true
        bv.unsafeSet(14)
        bv[49] = true

        assertTrue(bv[0])
        assertTrue(1 !in bv)
        assertTrue(2 in bv)
        assertTrue(14 in bv)
        assertTrue(bv.unsafeGet(49))
        assertFalse(bv[128])

        bv.cardinality() assertEquals 4
    }

    @Test
    fun `pushing bits to function | faster iterator alternative`() {
        val bv = bitsOf(1, 2, 56, 64, 128, 129, 130, 131, 420)
        val other = BitVector()

        bv.forEachBit { other.set(it) }
        other assertEquals bv
    }

    @Test
    fun `fill then clear`() {
        val bv = bitsOf(23, 4, 5, 123, 467, 10)
        assertFalse(bv.isEmpty)
        bv.clear()
        assertTrue(bv.isEmpty)

        for (bit in bv)
            fail()
    }

    @Test
    fun `fundamental bitwise checks`() {
        val bits: BitVector = bitsOf(0, 1, 2, 3, 7, 8, 9)

        assertTrue((bitsOf(1, 8, 9) in bits))
        assertTrue(bits !in bitsOf(1, 8, 9))
        assertTrue(bits.intersects(bitsOf(3, 4, 5, 6, 7)))

        assertFalse(bitsOf(100) in bits)
        assertFalse(100 in bits)
    }

    @Test
    fun `bitwise_operations`() {
        val a = bitsOf(0, 1, 2, 3, 120,                130)
        val b = bitsOf(0, 1, 2,    120, 121, 122, 123, 130)

        a.copy().apply {
            and(b)
        } assertEquals bitsOf(0, 1, 2, 120, 130)

        a.copy().apply {
            andNot(b)
        } assertEquals bitsOf(3)

        a.copy().apply {
            or(b)
        } assertEquals bitsOf(0,
                              1,
                              2,
                              3,
                              120,
                              121,
                              122,
                              123,
                              130)

        a.copy().apply {
            xor(b)
        } assertEquals bitsOf(3, 121, 122, 123)
    }
}


infix fun <T> T.assertEquals(expected: T) = assertEquals(expected, this)