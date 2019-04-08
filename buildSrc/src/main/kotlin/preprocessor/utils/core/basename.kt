package preprocessor.utils.core

/**
 * returns the basename of a string, if the string is **null* then returns **null**
 */
fun basename(s: Any?): String? {
    return if (s == null || !s.toString().contains('/')) {
        null
    } else s.toString().substringAfterLast('/')
}
