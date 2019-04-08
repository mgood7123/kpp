package preprocessor.utils.conversion

import java.util.*

/**
 * converts a [String] into a [ArrayDeque]
 * @see dequeToString
 * @return the resulting conversion
 */
fun stringToDeque(str: String): ArrayDeque<String> {
    val deq = ArrayDeque<String>()
    var i = 0
    while (i < str.length) deq.addLast(str[i++].toString())
    return deq
}
