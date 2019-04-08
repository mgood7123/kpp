@file:Suppress("unused")

package preprocessor.utils.core

import java.util.AbstractList

/**
 * reallocates a [AbstractList] to the specified size
 * @param v the list to resize
 * @param size the desired size to resize to
 */
fun <E> realloc(v: AbstractList<E>, size: Int) {
    while (v.size != size) if (size > v.size) v.add(listOf<E>() as E) else v.remove(v.last())
}

class a {
    val empty : Int = 0
}

fun ret() {
    val f = arrayListOf<a>()
    realloc(f, 5)
    abort()
}