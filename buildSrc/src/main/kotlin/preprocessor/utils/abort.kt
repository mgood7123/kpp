package preprocessor.utils

import org.gradle.api.GradleException

/**
 * a wrapper for GradleException, default message is **Aborted**
 */
fun abort(e : String = "Aborted") : Nothing {
    println("Aborting with error: $e")
    throw GradleException(e)
}
