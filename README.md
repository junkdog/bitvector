[![Build Status](https://travis-ci.org/junkdog/transducers-kotlin.svg)](https://travis-ci.org/junkdog/bitvector)

## BitVector

- Uncompressed, dynamically resizeable bitset, similar to `java.util.BitSet`
- Compatible with JavaScript and JVM backends



## The gist

#### Constructing
```kotlin
val bv: BitVector = bitsOf(1, 2, 56, 64, 128, 129, 130, 131, 420)
```


#### individual bits
```kotlin
val bv = BitVector()
bv[142] = true // or bv.set(14)
assert(142 in bv)

bv.clear(142)  // or bv.set(14, false)
assert(142 !in bv)
```


#### or, and, andNot, xor
As with `java.util.BitSet`, these operations mutate the callee. Do a `copy()` if the original BitVector needs to be around.

These functions are not infix functions, as such syntax would suggest a value copy.
  
```kotlin
val a = bitsOf(0, 1, 2, 3, 120,                130)
val b = bitsOf(0, 1, 2,    120, 121, 122, 123, 130)

assert(a.and(b) == bitsOf(0, 1, 2, 120, 130))

// caveat: bitvector was mutated above
assert(a == bitsOf(0, 1, 2, 120, 130))
```


#### vs other BitVectors
```kotlin
// 1, 4 contained in other 
bitsOf(1, 4) in bitsOf(0, 1, 4, 5, 6) 
```

#### Looping over bits: fast way
```kotlin
bitsOf(*bits).forEachBit { println("bit $it says hi") }
```


#### Looping over bits: Iterable<Int>
```kotlin
// can be combined with filter/map etc
bitsOf(*bits).forEach { println("bit $it says hi") }
```


## Benchmarks / iteration