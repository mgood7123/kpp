package preprocessor.utils.core

import java.io.File

/**
 * returns true if **src** is an empty directory, otherwise returns false
 */
fun empty(src: File): Boolean {
    val files = src.listFiles() ?: return true
    if (files.isEmpty()) return true
    files.forEach {
        if (it.isDirectory) return@empty empty(it)
        else if (it.isFile) return@empty false
        return@empty true
    }
    return false
}
