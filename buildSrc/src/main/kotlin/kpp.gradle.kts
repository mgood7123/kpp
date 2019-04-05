import preprocessor.base.*
import preprocessor.extra.globalVariables

tasks {
    register("KOTLIN_PRE_PROCESSOR") {
        group = "kotlin pre processor"
        description = "kotlin pre processor"
        doLast {
            globalVariables.INITPROJECTDIR = projectDir
            globalVariables.INITROOTDIR = rootDir
            println("starting KOTLIN_PRE_PROCESSOR")
            find_source_files(globalVariables.INITPROJECTDIR.toString(), "kpp")
            println("KOTLIN_PRE_PROCESSOR finished")
        }
    }
}