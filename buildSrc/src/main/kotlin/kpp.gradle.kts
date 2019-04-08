import preprocessor.base.globalVariables
import preprocessor.utils.Sync
import preprocessor.utils.core.abort

tasks {
    register("KOTLIN_PRE_PROCESSOR") {
        group = "kotlin pre processor"
        description = "kotlin pre processor"
        doLast {
            preprocessor.utils.core.realloc.ret()
            println("starting KOTLIN_PRE_PROCESSOR")
            globalVariables.abortOnComplete = false
            globalVariables.initGlobals(rootDir, projectDir)
            Sync().findSourceFilesOrNull(globalVariables.projectDirectory)
            println("KOTLIN_PRE_PROCESSOR finished")
            if (globalVariables.abortOnComplete) abort()
        }
    }
}