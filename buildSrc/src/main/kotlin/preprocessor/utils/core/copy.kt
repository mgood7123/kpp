package preprocessor.utils.core

import java.io.File
import java.io.IOException

/**
 * copy one file to another, optionally overwriting it
 * @return true if the operation succeeds, otherwise false
 * @see mv
 */
fun cp(src: String, dest: String, verbose: Boolean = false, overwrite: Boolean = false): Boolean {
    return try {
        File(src).copyTo(File(dest), overwrite)
        if (verbose) println("$src -> $dest")
        true
    } catch (e: IOException) {
        println("failed to copy file $src to $dest")
        false
    }
}
