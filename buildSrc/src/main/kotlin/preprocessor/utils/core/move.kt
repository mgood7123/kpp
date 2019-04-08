package preprocessor.utils.core

import java.io.File
import java.io.IOException

/**
 * moves one file to another, optionally overwriting it
 * @return true if the operation succeeds, otherwise false
 * @see cp
 */
fun mv(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
    return try {
        File(src).copyTo(File(dest), overwrite)
        if (verbose) println("$src -> $dest")
        delete(File(src))
        true
    } catch (e: IOException) {
        println("failed to move $src to $dest")
        false
    }
}
