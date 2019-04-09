@file:Suppress("unused")

package preprocessor.utils.core

/**
 * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
 *
 * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
 * initialization of a new element
 *
 * uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
 * @param v the list to resize
 * @param size the desired size to resize to
 * @sample reallocTest
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
@Suppress("UNCHECKED_CAST")
fun <E> realloc(v: kotlin.collections.MutableList<E?>, size: Int, isNullable: Boolean = true) {
    val primitive = v[0]!!::class.javaPrimitiveType != null
    val type = v[0]!!::class.javaPrimitiveType
    while (v.size != size) {
        if (size > v.size) {
            v.add(
                if (primitive) {
                    /** copied from
                     * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Primitives.kt
                     * /.../kotlin-stdlib-1.3.21-sources.jar!/kotlin/Boolean.kt
                     *
                     * unsigned integers are experimental: [UByte], [UInt], [ULong], [UShort]
                     */
                    if (isNullable) null as E else when (v[0]!!) {
                        is Byte -> 0
                        is UByte -> 0U
                        is Short -> 0
                        is UShort -> 0U
                        is Int -> 0
                        is UInt -> 0U
                        is Long -> 0L
                        is ULong -> 0UL
                        is Float -> 0.0F
                        is Double -> 0.0
                        is Char -> java.lang.Character.MIN_VALUE // null ('\0') as char
                        is Boolean -> false
                        else -> if (isNullable) null else abort("unknown non-nullable type: $type")
                    } as E
                } else {
                    v[0]!!::class.java.newInstance() as E
                }
            )
        } else {
            v.remove(v.last())
        }
    }
}

/**
 * reallocates a [MutableList][kotlin.collections.MutableList] to the specified size
 *
 * this attempts to handle all types supported by [MutableList][kotlin.collections.MutableList] in the
 * initialization of a new element
 *
 *  uses unsigned integers that are experimental: [UByte], [UInt], [ULong], [UShort]
 * @param v the list to resize
 * @param size the desired size to resize to
 * @sample reallocTest
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
@Suppress("UNCHECKED_CAST")
fun <E> realloc(v: kotlin.collections.MutableList<E>, size: Int): Unit = realloc(
    v = v as kotlin.collections.MutableList<E?>,
    size = size,
    isNullable = false
)

/**
 * @see reallocTest
 */
private class A {
    /**
     * test variable
     */
    var empty: Int = 0
}

/**
 * test function for [realloc]
 */
fun reallocTest() {
    val f = mutableListOf<A>()
    val ff = mutableListOf<Int>()
    val fff = mutableListOf<Double?>()
    f.add(A()); f[0].empty = 5
    ff.add(5)
    fff.add(5.5)
    realloc(f, 5)
    realloc(ff, 5)
    realloc(fff, 5)
    println("f[0].empty = ${f[0].empty}")
    println("f[4].empty = ${f[4].empty}")
    println("ff[0] = ${ff[0]}")
    println("ff[4] = ${ff[4]}")
    println("fff[0] = ${fff[0]}")
    println("fff[4] = ${fff[4]}")
    abort()
}
