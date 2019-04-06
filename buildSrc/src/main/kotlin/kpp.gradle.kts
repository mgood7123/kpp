import preprocessor.base.globalVariables
import preprocessor.utils.Sync

tasks {
    register("KOTLIN_PRE_PROCESSOR") {
        group = "kotlin pre processor"
        description = "kotlin pre processor"
        doLast {
            println("starting KOTLIN_PRE_PROCESSOR")
            globalVariables.initGlobals(rootDir, projectDir)
            Sync().findSourceFilesOrNull(globalVariables.projectDirectory)
            println("KOTLIN_PRE_PROCESSOR finished")
        }
    }
}