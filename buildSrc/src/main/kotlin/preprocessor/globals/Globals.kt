package preprocessor.globals

import preprocessor.core.Macro
import preprocessor.utils.core.basename
import java.io.File

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
     * the default [macro][Macro] list
     */
    var kppMacroList = arrayListOf(Macro())

    /**
     * the directory that **kpp** is contained in
     */
    var kppDir : String? = null
    /**
     * the directory that **kpp** is contained in
     */
    var kppDirAsFile : File? = null
    /**
     * the suffix to give files that have been processed by kpp
     */
    var preprocessedExtension = ".preprocessed"

    /**
     * inits the global variables
     *
     * it is recommended to call this function as `Globals().initGlobals(rootDir, projectDir)`
     *
     * replace `Globals()` with your instance of the `Globals` class
     * @sample globalsSample
     */
    fun initGlobals(rootDir : File, projectDir : File) {
        projectDirectory = projectDir
        projectDirectoryBaseName = basename(projectDirectory)
        rootDirectory = rootDir
        rootDirectoryBaseName = basename(rootDirectory)
        kppDir = rootDirectory.toString() + "/kpp"
        kppDirAsFile = File(kppDir)
    }

    /**
     * specifies if the function [cp] should produce verbose output or not
     */
    val globalCpVerbose = false

    var currentFileContainsPreprocesser = false
    var currentFileIsCashed = false
    var cachedFileContainsPreprocesser = false
    var firstLine = true
    var currentMacroExists = false
    val abortOnComplete = true

    /**
     * `<space> or <tab>`
     * @see tokens
     */
    val tokensSpace = " \t"

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
    val tokensNewLine = "\n"

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
    val tokensExtra = "/*#().,->{}[]"

    /**
     * the default list of tokens
     *
     * **tokens = [tokensSpace] + [tokensNewLine] + [tokensExtra]**
     */
    val tokens = tokensSpace + tokensNewLine + tokensExtra

    /**
     * tokens used in macro expansion
     */
    val mTokens = " ().,->{}[]"
}

private fun globalsSample(rootDir : File, projectDir : File) {
    val globals = Globals()
    globals.initGlobals(rootDir, projectDir)
    //rootDir is usually provided within the task itself
    //projectDir is usually provided within the task itself
}