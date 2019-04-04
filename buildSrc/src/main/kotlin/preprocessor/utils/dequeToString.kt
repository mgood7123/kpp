package preprocessor.utils

import java.util.*

/**
 * converts a [ArrayDeque] into a [String]
 * @see stringToDeque
 * @return the resulting conversion
 */
fun dequeToString(d : ArrayDeque<String>) : String {
    val result = StringBuffer()
    val dq = d.iterator()
    while(dq.hasNext()) {
        result.append(dq.next())
    }
    return result.toString()
}
