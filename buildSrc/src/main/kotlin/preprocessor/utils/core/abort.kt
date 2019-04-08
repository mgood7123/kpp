package preprocessor.utils.core

import org.gradle.api.GradleException

/**
 * a wrapper for GradleException, Default message is **Aborted**
 */
fun abort(e: String = "Aborted"): Nothing {
    println("Aborting with error: $e")
    throw GradleException(e)
}
