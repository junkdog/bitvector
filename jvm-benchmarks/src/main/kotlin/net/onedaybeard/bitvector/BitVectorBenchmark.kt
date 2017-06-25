package net.onedaybeard.bitvector

import com.artemis.utils.ConverterUtil
import com.artemis.utils.IntBag
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Thread)
open class BitVectorBenchmark {
    private var bitSet: BitSet? = null
    private var bv: BitVector? = null

    @Param(".01", ".05", ".10", ".20", ".40", ".50", ".60", ".70", ".80", ".95")
    internal var fillRate: Float = 0.toFloat()

    internal var maxCount = 4096

    private val ids = IntBag()

    @Setup
    fun setup() {
        bitSet = BitSet()
        //		bits = new Bits();
        bv = BitVector()

        val rng = Random(123456)

        var i = 0
        while (maxCount > i) {
            if (rng.nextFloat() <= fillRate) {
                bitSet!!.set(i)
                //				bits.set(i);
                bv!!.set(i)
            }
            i++
        }
    }

    @Benchmark
    fun bitset_foreach_intbag(): IntBag {
        return ConverterUtil.toIntBag(bitSet, ids)
    }

    @Benchmark
    fun bitvector_foreach_bit_intbag(): IntBag {
        val out = ids
        out.setSize(0)

        bv!!.forEachBit(out::add)

        return out
    }

    @Benchmark
    fun bitvector_foreach_intbag(): IntBag {
        val out = ids
        out.setSize(0)

        bv!!.forEach(out::add)

        return out
    }

    fun main(args: Array<String>) {
        Runner(
            OptionsBuilder()
                // include(".*bit(set|vector).*")
                .include(".*bitvector.*to.*")
                .build()
              ).run()
    }
}
