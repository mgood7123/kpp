package preprocessor.utils.core

import java.io.File

/**
 * deletes **src** and all sub directories
 *
 * [abort]s on failure
 */
fun deleteRecursive(src : File) {
    if (!src.exists()) {
        println("deletion of ${src.path} failed: file or directory does not exist")
    }
    if (!src.deleteRecursively()) {
        abort("deletion of \"${src.path}\" failed")
    }
}
