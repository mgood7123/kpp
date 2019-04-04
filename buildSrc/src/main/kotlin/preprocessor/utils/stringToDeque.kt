package preprocessor.utils

import java.util.*

/**
 * converts a [String] into a [ArrayDeque]
 * @see dequeToString
 * @return the resulting conversion
 */
fun stringToDeque(str : String) : ArrayDeque<String> {
    var deq = ArrayDeque<String>()
    var i = 0
    while (i < str.length) deq.addLast(str[i++].toChar().toString())
    return deq
}
