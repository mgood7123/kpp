package preprocessor.globals

import preprocessor.core.Macro
import preprocessor.utils.core.basename
import java.io.File

/**
 * the globals class contains all global variables used by this library
 */
@Suppress("MemberVisibilityCanBePrivate")
class Globals {
    /**
     * the current project directory that this task has been called from
     * @see projectDirectoryBaseName
     * @see rootDirectory
     */
    var projectDirectory: File? = null
    /**
     * the basename of [projectDirectory]
     * @see rootDirectoryBaseName
     */
    var projectDirectoryBaseName: String? = null
    /**
     * the root project directory
     * @see rootDirectoryBaseName
     * @see projectDirectory
     */
    var rootDirectory: File? = null
    /**
     * the basename of [rootDirectory]
     * @see projectDirectoryBaseName
     */
    var rootDirectoryBaseName: String? = null

    /**
     * the Default [macro][Macro] list
     */
    var kppMacroList: ArrayList<Macro> = arrayListOf(Macro())

    /**
     * the directory that **kpp** is contained in
     */
    var kppDir: String? = null
    /**
     * the directory that **kpp** is contained in
     */
    var kppDirAsFile: File? = null
    /**
     * the suffix to give files that have been processed by kpp
     */
    var preprocessedExtension: String = ".preprocessed"

    /**
     * inits the global variables
     *
     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
     *
     * replace `Globals()` with your instance of the `Globals` class
     * @sample globalsSample
     */
    fun initGlobals(rootDir: File, projectDir: File) {
        projectDirectory = projectDir
        projectDirectoryBaseName = basename(projectDirectory)
        rootDirectory = rootDir
        rootDirectoryBaseName = basename(rootDirectory)
        kppDir = rootDirectory.toString() + "/kpp"
        kppDirAsFile = File(kppDir)
    }

    /**
     * this is used by [testFile][preprocessor.utils.Sync.testFile]
     */
    var currentFileContainsPreprocesser: Boolean = false
    /**
     *
     *//*
    TODO: implement file cache
    var currentFileIsCashed: Boolean = false
    var cachedFileContainsPreprocesser: Boolean = false
     */
    var firstLine: Boolean = true
    /**
     *
     */
    var currentMacroExists: Boolean = false
    /**
     *
     */
    var abortOnComplete: Boolean = true

    /**
     * `<space> or <tab>`
     * @see tokens
     */
    val tokensSpace: String = " \t"

    /**
     * `<newline>`
     *
     * (
     *
     * \n or
     *
     * "new
     *
     * line"
     *
     * )
     * @see tokens
     */
    val tokensNewLine: String = "\n"

    /**
     * ```
     * /
     * *
     * #
     * (
     * )
     * .
     * ,
     * -
     * >
     * {
     * }
     * [
     * ]
     * ```
     * @see tokens
     */
    val tokensExtra: String = "/*#().,->{}[]"
    /**
     * ```
     * +
     * -
     * *
     * /
     * ```
     * @see tokens
     */
    val tokensMath: String = "+-*/"

    /**
     * the Default list of tokens
     *
     * this is used in general tokenization and [Macro] expansion
     *
     * **tokens = [tokensSpace] + [tokensNewLine] + [tokensExtra] + [tokensMath]**
     */
    val tokens: String = tokensSpace + tokensNewLine + tokensExtra + tokensMath
}

private fun globalsSample(rootDir: File, projectDir: File) {
    val globals = Globals()
    globals.initGlobals(rootDir, projectDir)
    //rootDir is usually provided within the task itself
    //projectDir is usually provided within the task itself
}