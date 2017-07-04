[![Build Status](https://travis-ci.org/junkdog/bitvector.svg)](https://travis-ci.org/junkdog/bitvector)

## BitVector

- Uncompressed, dynamically resizeable bitset, similar to `java.util.BitSet`
- [Fast-ish][bench] enumeration of set bits (JS not yet profiled) 
- Compatible with JavaScript and JVM/Android backends
  - 4 byte words, avoids [`Long` emulation][long-emu] in js

 [long-emu]: https://kotlinlang.org/docs/reference/js-to-kotlin-interop.html#representing-kotlin-types-in-javascript 
 [bench]: https://github.com/junkdog/bitvector#jvm-benchmarks--enumerating-set-bits
 
## The gist

#### Constructing
```kotlin
val bv: BitVector = bitsOf(1, 2, 56, 64, 128, 129, 130, 131, 420)
```


#### Individual bits
```kotlin
val bv = BitVector()
bv[142] = true // or bv.set(142)
assert(142 in bv)

bv.clear(142)  // or bv[142] = false
assert(142 !in bv)
```


#### `or`, `and`, `andNot`, `xor`
As with `java.util.BitSet`, these operations mutate the callee. Do a `copy()` if the original BitVector needs to stick around.

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

## Getting Started

For multi-platform projects, `bitvector-jvm` and `bitvector-js` must be configured  

#### Maven: JVM/Android

```xml
<dependency>
	<groupId>net.onedaybeard.bitvector</groupId>
	<artifactId>bitvector-jvm</artifactId>
	<version>0.1.0</version>
</dependency>
```

#### Maven: JavaScript

```xml
<dependency>
	<groupId>net.onedaybeard.bitvector</groupId>
	<artifactId>bitvector-js</artifactId>
	<version>0.1.0</version>
</dependency>
```

#### Gradle: JVM/Android

```groovy
  dependencies { compile "net.onedaybeard.bitvector:bitvector-jvm:0.1.0" }
```

#### Gradle: JavaScript

```groovy
  dependencies { compile "net.onedaybeard.bitvector:bitvector-js:0.1.0" }
```


## JVM Benchmarks / enumerating set bits

Mapping `true` bit positions into integers, inserting each into an `IntBag` (thin wrapper around `int[]`).
 
[artemis-odb](https://github.com/junkdog/artemis-odb)'s `BitVector` was the basis for this implementation. The benchmark setup favors the artemis implementation, as it provides an optimized `toIntBag` method: it serves as a reference for best possible performance.

See [jmh-logs](https://github.com/junkdog/bitvector/tree/master/jmh-logs) for the full logs.

![benchmark.png](http://junkdog.github.io/images/bitvector-jmh.png)

Discrepancy to artemis' `BitVector` is unwelcome. The implementation is for the most part the same, except that this implementation uses `int` for words, instead of `long`. 4 or 8 byte words did not have a significant impact on performance.

The for-loop performs poorly due to all the `Integer` boxing, extra indirection and allocation, compared to `forEachBit`.   

`java.util.BitSet` suffers from not having a way of enumerating all bits at once, instead relying on repeatedly calling `nextSetBit`. 


## Tests

#### Verifying platform-specific implementations
```
mvn clean install -P impltest
```

#### Running JS tests
Build project, `mvn clean install`, then point the browser to `file://$PROJECT_ROOT/js/target/test-classes/index.html`
