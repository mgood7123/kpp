package preprocessor.globals

import preprocessor.core.Macro
import java.io.File

class Globals {
    var INITPROJECTDIR: File? = null
    var INITROOTDIR: File? = null

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

    /**
     * the directory that **kpp** is contained in
     */
    var kppDir : String? = null

    fun initKppDir() {
        kppDir = INITROOTDIR.toString() + "/kpp"
    }

    /**
     * the default [macro][Macro] list
     */
    var kppMacroList = arrayListOf(Macro())

    /**
     * specifies if the function [cp] should produce verbose output or not
     */
    val globalCpVerbose = false

    var currentFileContainsPreprocesser = false
    var currentFileIsCashed = false
    var cachedFileContainsPreprocesser = false
    var firstLine = true
    var currentMacroExists = false
    var preprocessedExtension = ".preprocessed"
}