package net.onedaybeard.bitvector

import org.junit.Test
import java.lang.Integer.numberOfLeadingZeros
import kotlin.test.assertEquals

class PlatformTest {
    @Test
    fun `count leading zeroes of int`() {
        listOf(1 shl 31,
               420,
               -1,
               1,
               0,
               0x0ffffff,
               0x1000000,
               -12445,
               65536,
               65535).forEach { n ->

            assertEquals(numberOfLeadingZeros(n),
                         leadingZeros(n))
        }
    }
}

