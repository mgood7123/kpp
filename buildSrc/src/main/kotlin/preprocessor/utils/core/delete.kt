package preprocessor.utils.core

import java.io.File

/**
 * deletes **src**
 *
 * [abort]s on failure
 */
fun delete(src: File) {
    if (!src.exists()) {
        println("deletion of ${src.path} failed: file or directory does not exist")
    }
    if (!src.delete()) {
        abort("deletion of \"${src.path}\" failed")
    }
}
