package preprocessor.utils.core

/**
 * returns the basename of a string, if the string is **null* then returns **null**
 */
fun basename(s : Any?) : String? {
    if (s == null || !s.toString().contains('/')) {
        return null
    }
    else return s.toString().substringAfterLast('/')
}
