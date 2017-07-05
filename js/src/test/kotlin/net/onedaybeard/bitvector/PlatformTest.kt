package net.onedaybeard.bitvector

import org.junit.Test
import kotlin.test.assertEquals

class PlatformTest {
    val numbersToTest = listOf(1 shl 31,
                               420,
                               1,
                               0,
                               -1, // ~0
                               Integer.MAX_VALUE,
                               Integer.MIN_VALUE,
                               0x0ffffff,
                               0x1000000,
                               -12445,
                               65536,
                               65535)

    @Test
    fun `count leading zeroes in int`() {
        assertImplEquals(Integer::numberOfLeadingZeros, ::leadingZeros)
    }

    @Test
    fun `count set bits in int`() {
        assertImplEquals(Integer::bitCount, ::bitCount)
    }

    private fun assertImplEquals(expected: (Int) -> Int,
                                 actual: (Int) -> Int) {

        for (n in numbersToTest) {
            assertEquals(expected(n), actual(n))
        }
    }
}
