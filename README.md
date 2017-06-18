[![Build Status](https://travis-ci.org/junkdog/transducers-kotlin.svg)](https://travis-ci.org/junkdog/bitvector)

## BitVector

- Uncompressed, dynamically sizeable bitset, similar to `java.util.BitSet`
- Comaptible with JavaScript and JVM


```kotlin
val bv: BitVector = bitsOf(1, 2, 56, 64, 128, 129, 130, 131, 420)
bv.forEachBit(::println)

```
