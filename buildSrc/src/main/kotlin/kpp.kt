import org.gradle.api.GradleException
import java.io.*
import java.nio.file.Files.*
import java.util.*

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

var INITPROJECTDIR : File? = null
var INITROOTDIR : File? = null

class kpp : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {

        tasks {
            register("KOTLIN_PRE_PROCESSOR") {
                group = "kotlin pre processor"
                description = "kotlin pre processor"
                doLast {
                    INITPROJECTDIR = projectDir
                    INITROOTDIR = rootDir
                    println("starting KOTLIN_PRE_PROCESSOR")
                    find_source_files(INITPROJECTDIR.toString(), "kt")
                    println("KOTLIN_PRE_PROCESSOR finished")
                }
            }
        }
    }
}

//tasks.register("KOTLIN_PRE_PROCESSOR") {
//    println("starting KOTLIN_PRE_PROCESSOR")
//    find_source_files(projectDir.toString(), "kt")
//    println("KOTLIN_PRE_PROCESSOR finished")
//    if (abort_on_complete) abort()
//}


val abort_on_complete = true
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
val mtokens = " ().,->{}[]"

/**
 * a wrapper for GradleException, default message is **Aborted**
 */
fun abort(e : String = "Aborted") : Nothing {
    println("Aborting with error: $e")
    throw GradleException(e)
}

/**
 * the directory that **kpp** is contained in
 */
var kpp_DIR : String? = null
fun init_kppDIR() {
    kpp_DIR = INITROOTDIR.toString() + "/kpp"
}
/**
 * the main [macro][Macro] list
 */
var kpp_MACRO_LIST = arrayListOf(Macro())


/**
 *
 * This class is used to house all macro definitions
 *
 * @sample tests.generalUsage
 *
 * @see Directives
 * @see Define
 *
 */
class Macro() {
    /**
     * this class is used to obtain predefined values such as directive names and types
     *
     * all preprocessor directives start with a #
     * @see Define
     */
    inner class Directives() {
        /**
         * contains predefined [values][Define.value] and [types][Define.Types] for the [#define][Define.value] directive
         * @see Directives
         */
        inner class Define() {
            /**
             * the ***#define*** directive associates an identifier to a replacement list
             * the default value of this is **define**
             * @see Types
             */
            val value : String = "define"

            /**
             * the valid types of a definition directive macro
             * @see OBJECT
             * @see FUNCTION
             */
            inner class Types() {
                /**
                 * the Object type denotes the standard macro definition, in which all text is matched with
                 *
                 * @see Types
                 * @see FUNCTION
                 * @sample ObjectSample
                 * @sample ObjectUsage
                 */
                val OBJECT : String = "object"
                /**
                 * the Function type denotes the Funtion macro definition, in which all text that is followed by parentheses is matched with
                 *
                 * @see Types
                 * @see OBJECT
                 * @sample FunctionSample
                 * @sample FunctionUsage
                 */
                val FUNCTION : String = "function"

                private fun ObjectSample() {
                    /* ignore this block comment

                    #define object value
                    object
                    object(object).object[object]

                    ignore this block comment */
                }
                private fun ObjectUsage() {
                    var c = arrayListOf(Macro())
                    c[0].FileName = "test"
                    c[0].Macros[0].FullMacro = "A B00"
                    c[0].Macros[0].Token = "A"
                    c[0].Macros[0].replacementList = "B00"
                    c[0].Macros[0].Type =
                        Macro().Directives().Define().Types().OBJECT
                    println(c[0].Macros[0].Type)
                }
                private fun FunctionSample() {
                    /* ignore this block comment

                    #define object value
                    #define function value
                    object
                    function(object).object[function(function()]

                    ignore this block comment */
                }
                private fun FunctionUsage() {
                    var c = arrayListOf(Macro())
                    c[0].FileName = "test"
                    c[0].Macros[0].FullMacro = "A() B00"
                    c[0].Macros[0].Token = "A"
                    c[0].Macros[0].replacementList = "B00"
                    c[0].Macros[0].Type =
                        Macro().Directives().Define().Types().FUNCTION
                    println(c[0].Macros[0].Type)
                }
            }
        }
    }

    /**
     * the internals of the Macro class
     *
     * this is where all macros are kept, this is managed via [realloc]
     * @sample tests.generalUsage
     */
    inner class macroInternal() {
        /**
         * the current size of the [macro][macroInternal] list
         *
         * can be used to obtain the last added macro
         *
         * @sample tests.sizem
         */
        var size : Int = 0
        /**
         * the full macro definition
         *
         * contains the full line at which the definition appears
         */
        var FullMacro: String? = null
        /**
         * contains the definition **identifier**
         */
        var Token: String? = null
        /**
         * contains the definition **type**
         */
        var Type: String? = null
        /**
         * contains the definition **arguments**,
         * valid only for
         * [Function][Macro.Directives.Define.Types.FUNCTION]
         * type definitions
         *
         * this defaults to **null**
         *
         */
        var Arguments : ArrayList<String>? = null
        /**
         * this contains the definition replacement list
         */
        var replacementList: String? = null

        /**
         * this adds a new macro to the [macro][macroInternal] list
         *
         * implementation of the C function **realloc**
         *
         * allocation is recorded in [size][Macro.macroInternal.size] paramater
         *
         * @param m [internal macro list][macroInternal]
         * @param NewSize the new [size][macroInternal.size] to allocate the [internal macro list][macroInternal] to
         * @see <a href="http://man7.org/linux/man-pages/man3/realloc.3p.html">realloc (C function)</a>
         * @sample tests.reallocUsageInternal
         * @return the given [macro][macroInternal] list, allocated to **newSize**
         */
        fun realloc(m : ArrayList<macroInternal>, newSize : Int) : ArrayList<macroInternal> {
            m.add(macroInternal())
            m[0].size = newSize
            return m
        }
    }
    /**
     * the current size of the [macro][Macro] list
     *
     * can be used to obtain the last added [macro group][macroInternal]
     *
     * @sample tests.size
     */
    var size : Int = 0
    /**
     * the name of the file containing this [macro][Macro] list
     */
    var FileName: String? = null
    /**
     * the [macro][macroInternal] list
     */
    var Macros: ArrayList<macroInternal>

    init {
        this.size = 1
        Macros = arrayListOf(macroInternal())
    }
    /**
     * this adds a new macro to the [macro][Macro] list
     *
     * implementation of the C function realloc
     *
     * allocation is recorded in [size][Macro.size] paramater
     *
     * @param m [macro list][Macro]
     * @param NewSize the new [size][Macro.size] to allocate the [macro list][Macro] to
     * @see <a href="http://man7.org/linux/man-pages/man3/realloc.3p.html">realloc (C function)</a>
     * @sample tests.reallocUsage
     * @return the given macro list, allocated to newSize
     */
    fun realloc(m : ArrayList<Macro>, NewSize : Int) : ArrayList<Macro> {
        m.add(Macro())
        m[0].size = NewSize
        return m
    }

    private class tests {
        fun generalUsage() {
            var c = arrayListOf(Macro())
            c[0].FileName = "test"
            c[0].Macros[0].FullMacro = "A B"
            c[0].Macros[0].Token = "A"
            c[0].Macros[0].replacementList = "B00"
            println(c[0].Macros[0].replacementList)
            c = c[0].realloc(c, c[0].size + 1)
            c[1].FileName = "test"
            c[1].Macros[0].FullMacro = "A B"
            c[1].Macros[0].Token = "A"
            c[1].Macros[0].replacementList = "B10"
            println(c[1].Macros[0].replacementList)
            c[1].Macros = c[1].Macros[0].realloc(c[1].Macros, c[1].Macros[0].size + 1)
            c[1].FileName = "test"
            c[1].Macros[1].FullMacro = "A B"
            c[1].Macros[1].Token = "A"
            c[1].Macros[1].replacementList = "B11"
            println(c[1].Macros[1].replacementList)
            c[1].Macros = c[1].Macros[0].realloc(c[1].Macros, c[1].Macros[0].size + 1)
            c[1].FileName = "test"
            c[1].Macros[2].FullMacro = "A B"
            c[1].Macros[2].Token = "A"
            c[1].Macros[2].replacementList = "B12"
            println(c[1].Macros[2].replacementList)
        }
        fun reallocUsage() {
            var c = arrayListOf(Macro())
            // allocate a new index
            c = c[0].realloc(c, c[0].size + 1)
            // assign some values
            c[0].FileName = "test"
            c[1].FileName = "test"
        }
        fun reallocUsageInternal() {
            var c = arrayListOf(Macro())
            // allocate a new index
            c[0].Macros = c[0].Macros[0].realloc(c[0].Macros, c[0].Macros[0].size + 1)
            // assign some values
            c[0].Macros[0].FullMacro = "A A"
            c[0].Macros[1].FullMacro = "A B"
        }
        fun size() {
            var c = arrayListOf(Macro())
            // allocate a new macro
            c[0].Macros = c[0].Macros[0].realloc(c[0].Macros, c[0].Macros[0].size + 1)
            c[0].Macros[1].FullMacro = "A B"
            println(c[0].Macros[1].replacementList)
            // obtain base index
            val index = c[0].size - 1
            // obtain last macro index
            val macroIndex = c[0].Macros[0].size - 1
            if (c[index].Macros[macroIndex].FullMacro.equals(c[0].Macros[1].FullMacro))
                println("index matches")
        }
        fun sizem() {
            var c = arrayListOf(Macro())
            c[0].FileName = "test1"
            c = c[0].realloc(c, c[0].size + 1)
            c[1].FileName = "test2"
            val index = c[0].size - 1
            if (c[index].FileName.equals(c[1].FileName))
                println("index matches")
        }
    }
}

/**
 * self exlanatory
 *
 * this function finds and processes all source files in the directory **Dir** with the extention **ext**
 * @param Dir the directory to search in
 * @param ext the extention that each file must end
 * @sample find_source_files_sample
 */
fun find_source_files(Dir : String, ext : String) {
    var srcDir = File(Dir)
    srcDir?.listFiles().forEach {
        if (it.isDirectory) find_source_files(it.toString(), ext)
        if (it.isFile) {
            if (it.extension.equals(ext)) {
                initiate(it, ext)
            }
        }
    }
}

fun find_source_files_sample() {
    // find all source files with kotlin extention
    find_source_files(INITPROJECTDIR.toString(), "kt")
}

val globalCpVerbose = false

/**
 * copy one file to another, optionally overwriting it
 * @return true if the operation suceeds, otherwise false
 */
fun cp(src : String, dest : String, verbose : Boolean = false, overwrite : Boolean = false) : Boolean {
    try {
        File(src).copyTo(File(dest), overwrite)
        if (verbose) println("$src -> $dest")
        return true
    } catch (e: IOException) {
        println("failed to copy file $src")
        return false
    }
    return false
}

var current_file_contains_preprocesser : Boolean = false
var current_file_is_cashed = false
var cached_file_contains_preprocesser = false
var first_line : Boolean = true

/**
 * initializes the file **src** to be pre-processed
 */
fun initiate(src : File, ext : String) {
    init_kppDIR()
    val source = src.toString()
    val dest = kpp_DIR + src.toString().removePrefix(INITPROJECTDIR.toString())
    current_file_contains_preprocesser = false
    cached_file_contains_preprocesser = false
    current_file_is_cashed = false
    first_line = true

    if (File(dest).exists()) {
        test_cache_File(File(dest))
        if (!cached_file_contains_preprocesser) {
            // if dest no longer contains preprocessing info, copy it back to src, overwriting the current src, and use src
            if (cp(dest, source, globalCpVerbose, true)) { // copy dest to source
                println("${dest.substringAfterLast('/')} (in kpp/src) moved back to original source")
                File(dest).delete()
                if (File(dest + ".preprocessed.kt").exists()) File(dest + ".preprocessed.kt").delete()
            }
            else abort("failed to move ${dest} (in kpp/src) to $source")
        }
        else {
            // if dest already exist use dest
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(dest, ext, kpp_MACRO_LIST)
            if (cp(dest + ".preprocessed.kt", source, globalCpVerbose, true)) {
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else abort("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
    else {
        test_File(src)
        if (!current_file_contains_preprocesser) {
            // if src does not contain preprocessing info, use src
        } else {
            // if src contains preprocessing info, copy it to dest and use dest
            if (cp(source, dest, globalCpVerbose, true)) { // copy if dest does not exist
                println("original ${dest.substringAfterLast('/')} added to kpp/src")
            }
            else abort("failed to copy $source to $dest")
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(dest, ext, kpp_MACRO_LIST)
            if (cp(dest + ".preprocessed.kt", source, globalCpVerbose, true)) { // copy back to source
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else abort("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
}

/**
 * test if file **src** contains any preprocessor directives
 */
fun test_File(src : File) {
    val lines: List<String> = readAllLines(src.toPath())
    lines.forEach {
            line -> check_if_preprocessor_is_needed(line)
    }
}

fun check_if_preprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) current_file_contains_preprocesser = true
}

/**
 * test if file **src** contains any preprocessor directives
 */
fun test_cache_File(src : File) {
    val lines: List<String> = readAllLines(src.toPath())
    lines.forEach {
            line -> check_if_cachepreprocessor_is_needed(line)
    }
}

fun check_if_cachepreprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) cached_file_contains_preprocesser = true
}

var currentmacroexists = false

/**
 *
 * a class for detecting balanced brackets
 *
 * cant be bothered documenting this
 *
 * modified from the original rosetta code in the **See Also**
 *
 * @see <a href="https://rosettacode.org/wiki/Balanced_brackets#Kotlin">Balanced Brackets</a>
 */
class balanced {
    class balanceList() {
        var l : MutableList<Char> = mutableListOf()
        var r : MutableList<Char> = mutableListOf()
        fun addPair( l : Char, r : Char) {
            this.l.add(l)
            this.r.add(r)
        }
    }
    var start : MutableList<Int> = mutableListOf()
    var end : MutableList<Int> = mutableListOf()
    var index = 0
    var countLeft = 0  // number of left brackets so far unmatched
    var splitterCount = 0
    var splitterLocation : MutableList<Int> = mutableListOf()
    var lastRegisteredLBalancer = ' '
    var lastRegisteredRBalancer = ' '
    var lastCheckString = ""
    fun isBalanced(s: String, balancerLeft : Char, balancerRight : Char): Boolean {
        lastCheckString = s
        lastRegisteredLBalancer = balancerLeft
        lastRegisteredRBalancer = balancerRight
        start
        end
        if (s.isEmpty()) return true
        for (c in s) {
            if (c == lastRegisteredLBalancer) {
                countLeft++
                if (countLeft == 1) start.add(index)
            }
            else if (c == lastRegisteredRBalancer) {
                if (countLeft == 1) end.add(index+1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    fun is_balancerR(c : Char, balance : balanceList) : Boolean {
        balance.r.forEach {
            if (c == it) return true
        }
        return false
    }
    fun is_balancerL(c : Char, balance : balanceList) : Boolean {
        balance.l.forEach {
            if (c == it) return true
        }
        return false
    }

    fun containsL(c : String, balance : balanceList) : Boolean {
        balance.l.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    fun containsR(c : String, balance : balanceList) : Boolean {
        balance.r.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    fun isBalancedSplit(s: String, balancer : balanceList, Splitter : Char): Boolean {
        lastCheckString = s
        lastRegisteredLBalancer = balancer.l[balancer.l.lastIndex]!!
        lastRegisteredRBalancer = balancer.r[balancer.r.lastIndex]!!
        if (s.isEmpty()) return true
        for (c in s) {
            if (countLeft == 0) if (c == Splitter) {
                splitterCount++
                splitterLocation.add(index)
            }
            if (is_balancerL(c, balancer)) {
                countLeft++
                if (countLeft == 1) start.add(index)
            }
            else if (is_balancerR(c, balancer)) {
                if (countLeft == 1) end.add(index+1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    fun extract_text(text : String) : String {
        if (isBalanced(text, '(', ')')) {
            println("text : " + text.substring(start[0], end[0]))
            return text.substring(start[0], end[0])
        }
        return text
    }

    fun info() {
        println("last check string  = $lastCheckString")
        println("left balancer      = $lastRegisteredLBalancer")
        println("right balancer     = $lastRegisteredRBalancer")
        println("start index        = $start")
        println("end index          = $end")
        println("curent index       = $index")
        println("unmatched brackets = $countLeft")
        println("splitter count     = $splitterCount")
        println("splitter location  = $splitterLocation")
    }
}

/**
 * adds each **line** to the given [MACRO][Macro] list
 *
 * assumes each **line** is a valid **#define** directive
 */
fun processDefine(line: String, MACRO : ArrayList<Macro>) {
    val index = MACRO[0].size - 1
    var macro_index = MACRO[index].Macros[0].size
    // to include the ability to redefine existing definitions, we must save to local variables first
    val full_macro : String = line.trimStart().drop(1).trimStart()
    var type : String = ""
    // determine Token type
    if (full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart().equals(full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()?.substringBefore('('))!!)
        type = Macro().Directives().Define().Types().OBJECT
    else
        type = Macro().Directives().Define().Types().FUNCTION
    var token : String = ""
    if (type.equals(Macro().Directives().Define().Types().OBJECT)) {
        var empty = false
        // object
        token =
            full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
        if (token[token.length-1] == '\n') {
            token = token.dropLast(1)
            empty = true
        }
        val i = macro_exists(token, type, index, MACRO)
        if (currentmacroexists) {
            macro_index = i
        }
        else {
            if (MACRO[index].Macros[macro_index].FullMacro != null) {
                MACRO[index].Macros = MACRO[index].Macros[0].realloc(
                    MACRO[index].Macros,
                    MACRO[index].Macros[0].size + 1
                )
            }
            macro_index = MACRO[index].Macros[0].size
        }
        MACRO[index].Macros[macro_index].FullMacro = line.trimStart().trimEnd()
        MACRO[index].Macros[macro_index].Token = token
        MACRO[index].Macros[macro_index].Type = type
        if (!empty) {
            MACRO[index].Macros[macro_index].replacementList =
                full_macro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        }
        else MACRO[index].Macros[macro_index].replacementList = ""
    } else {
        // function
        token =
            full_macro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macro_exists(token, type, index, MACRO)
        if (currentmacroexists) {
            macro_index = i
        }
        else {
            if (MACRO[index].Macros[macro_index].FullMacro != null) {
                MACRO[index].Macros = MACRO[index].Macros[0].realloc(
                    MACRO[index].Macros,
                    MACRO[index].Macros[0].size + 1
                )
            }
            macro_index = MACRO[index].Macros[0].size
        }
        MACRO[index].Macros[macro_index].FullMacro = line.trimStart().trimEnd()
        MACRO[index].Macros[macro_index].Token = token
        MACRO[index].Macros[macro_index].Type = type
        // obtain the function arguments
        val t = MACRO[index].Macros[macro_index].FullMacro?.substringAfter(' ')!!
        val b = balanced()
        MACRO[index].Macros[macro_index].Arguments = extract_arguments(b.extract_text(t).drop(1).dropLast(1), MACRO)
        MACRO[index].Macros[macro_index].replacementList = t.substring(b.end[0]+1).trimStart()
    }
    println("Type       = ${MACRO[index].Macros[macro_index].Type}")
    println("Token      = ${MACRO[index].Macros[macro_index].Token}")
    if (MACRO[index].Macros[macro_index].Arguments != null)
        println("Arguments  = ${MACRO[index].Macros[macro_index].Arguments}")
    println("replacementList      = ${MACRO[index].Macros[macro_index].replacementList}")
    macro_list(index, MACRO)
    // definition names do not expand
    // definition values do expand
}

/**
 * converts a [String] into a [ArrayDeque]
 * @see dequeToString
 * @return the resulting conversion
 */
fun stringToDeque(str : String) : ArrayDeque<String> {
    var deq = ArrayDeque<String>()
    var i = 0
    while (i < str.length) deq.addLast(str[i++].toChar().toString())
    return deq
}

/**
 * converts a [ArrayDeque] into a [String]
 * @see stringToDeque
 * @return the resulting conversion
 */
fun dequeToString(d : ArrayDeque<String>) : String {
    val result = StringBuffer()
    val dq = d.iterator()
    while(dq.hasNext()) {
        result.append(dq.next())
    }
    return result.toString()
}

/**
 * converts a [File] into a [ByteBuffer]
 * @return the resulting conversion
 * @see stringToByteBuffer
 */
fun fileToByteBuffer(f : File) : ByteBuffer {
    val file = RandomAccessFile(f, "r")
    val fileChannel = file.getChannel()

    var i = 0
    var buffer = ByteBuffer.allocate(fileChannel.size().toInt())
    fileChannel.read(buffer)
    buffer.flip()
    return buffer
}

/**
 * converts a [String] into a [ByteBuffer]
 * @return the resulting conversion
 * @see fileToByteBuffer
 */
fun stringToByteBuffer(f : String) : ByteBuffer {
    return ByteBuffer.wrap(f.toByteArray())
}

/**
 * a minimal lexer implementation
 *
 * we traditionally process the input file character by character
 *
 * append each processed character to a buffer
 *
 * then return that buffer when a specific delimiter is found
 *
 * @param stm the byte buffer to read from, set up via [fileToByteBuffer]
 * @param delimiter the delimiters to split the input by
 * @see parser
 * @see lexer.lex
 */
class lexer(stm : ByteBuffer, delimiter : String) {
    val f = stm
    val delimiters = delimiter
    val d = stringToDeque(delimiters)
    /**
     * this is the current line the lexer of on
     *
     */
    var current_line : String? = null

    inner class internallineinfo {
        inner class default {
            var column = 1
            var line = 1
        }
        var column = default().column
        var line = default().line
    }

    var lineinfo = internallineinfo()

    // for some reason, a ByteBuffer cannot be instanced inside a class inside a
    // class/function, this is related to DSL kotlin

/*
    fun clone() : lexer {
        return lexerClone(this)
    }

    fun clone(newdelimiters : String) : lexer {
        return lexerClone(this, newdelimiters)
    }
*/
    /**
     * sets the variable [current_line] to the
     * [buffer][ByteBuffer] (converted to a
     * [String]) when a specific
     * [delimiter][lexer] is found
     *
     * sets the variable [current_line] to
     * **null** upon EOF
     */
    fun lex() {
        /*
        in order to make a lexer, we traditionally process the input file
        character by character, appending each to a buffer, then returning
        that buffer when a specific delimiter is found
        */
        var isdelim = false
        if (f.remaining() == 0) {
            current_line = null
            return
        }
        var b = StringBuffer()
        while(f.remaining() != 0 && !isdelim) {
            var s = f.get().toChar().toString()
            b.append(s)
            if (s == "\n") {
                lineinfo.column = lineinfo.default().column
                lineinfo.line++
            }
            else lineinfo.column++
            if (d.contains(s)) break
        }
        current_line = b.toString()
        return
    }
}

/**
 * prepares the input **line** for consumption by the parser
 * @return an [ArrayDeque] which is split by the global variable **tokens**
 *
 * this [ArrayDeque] preserves the tokens in which it is split by
 * @see parser
 */
fun parserPrep(line: String) : ArrayDeque<String> {
    val st = StringTokenizer(line, tokens, true)
    var dq = ArrayDeque<String>()
    while (st.hasMoreTokens()) {
        dq.addLast(st.nextToken())
    }
    return dq
}

/**
 * a minimal parser implementiation
 * @param tokens a list of tokens to split by
 * @see tokens
 * @see lexer
 */
class parser(tokens : ArrayDeque<String>) {
    /**
     * a line represented as an [ArrayDeque] as returned by [parserPrep]
     * @see tokens
     */
    var tokenList = tokens

    inner class internallineinfo {
        inner class default {
            var column = 1
            var line = 1
        }
        var column = default().column
        var line = default().line
    }

    var lineinfo = internallineinfo()
    /**
     * @return a new independant instance of the current [parser]
     */
    fun clone() : parser {
        val result = StringBuffer()
        tokenList.forEach {
            result.append(it!!)
        }
        return parser(parserPrep(result.toString()))
    }

    /**
     * wrapper for [ArrayDeque.peek]
     * @return [tokenList].[peek()][ArrayDeque.peek]
     */
    fun peek() : String? {
        return tokenList.peek()
    }

    /**
     * wrapper for [ArrayDeque.pop]
     * @return [tokenList].[pop()][ArrayDeque.pop] on success
     *
     * **null** on failure
     *
     * [aborts][abort] if the [tokenList] is corrupted
     */
    fun pop() : String? {
        if (tokenList.peek() == null) {
            try {
                tokenList.pop()
            }
            catch (e: java.util.NoSuchElementException) {
                return null
            }
            abort("token list is corrupted, or the desired exception 'java.util.NoSuchElementException' was not caught")
        }
        val returnValue = tokenList.pop()
        if (returnValue == null) abort("token list is corrupted")
        var i = 0
        while (i < returnValue.length) {
            if (returnValue[i] == '\n') {
                lineinfo.column = lineinfo.default().column
                lineinfo.line++
            }
            else lineinfo.column++
            i++
        }
        return returnValue
    }

    /**
     * wrapper for [ArrayDeque.clear]
     * @return [tokenList].[clear()][ArrayDeque.clear]
     */
    fun clear() {
        tokenList.clear()
    }

    /**
     * @return the current [tokens] left, converted into a [String]
     * @see dequeToString
     */
    override fun toString() : String {
        return dequeToString(tokenList)
    }

    /**
     * @return the current [tokens] left, directly returns [tokenList].[toString()][ArrayDeque.toString]
     */
    fun toStringAsArray() : String {
        return tokenList.toString()
    }

    /**
     * matches a sequence of **str** zero or more times
     * @see isSequenceOnceOrMany
     * @see isSequenceOnce
     */
    inner class isSequenceZeroOrMany(str : String) {
        val sg = str
        val seq = isSequenceOneOrMany(sg)

        /**
         * returns the value of [isSequenceOneOrMany.toString]
         *
         * the original [tokenList] is [cloned][parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the result of [isSequenceOneOrMany.toString], which can be an empty string
         * @see isSequenceOneOrMany
         * @see peek
         * @see pop
         */
        override fun toString() : String {
            return seq.toString()
        }

        /**
         * this function always returns true
         * @see toString
         * @see pop
         */
        fun peek() : Boolean {
            return true
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * **this relies on the safety of [isSequenceOneOrMany.pop]**
         *
         * this function always returns true
         * @see isSequenceOneOrMany
         * @see toString
         * @see peek
         */
        fun pop() : Boolean {
            while(seq.peek()) seq.pop()
            return true
        }
    }

    /**
     * matches a sequence of **str** one or more times
     * @see isSequenceZeroOrMany
     * @see isSequenceOnce
     */
    inner class isSequenceOneOrMany(str : String) {
        val sg = str

        /**
         * returns the accumalative value of [isSequenceOnce.toString]
         *
         * the original [tokenList] is [cloned][parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return the accumalative result of [isSequenceOnce.toString], which can be an empty string
         * @see isSequenceOnce
         * @see peek
         * @see pop
         */
        override fun toString() : String {
            val o = clone().isSequenceOnce(sg)
            val result = StringBuffer()
            while(o.peek()) {
                result.append(o.toString())
                o.pop()
            }
            return result.toString()
        }

        /**
         *
         * the original [tokenList] is [cloned][parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return true if [sg] matches at least once
         * @see isSequenceOnce
         * @see toString
         * @see pop
         */
        fun peek() : Boolean {
            val o = clone().isSequenceOnce(sg)
            var matches = 0
            while(o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (parser.peek()) parser.pop()*** to avoid accidental modifications, where **parser** is an instance of [isSequenceOneOrMany]
         *
         * @return true if [sg] matches at least once
         * @see isSequenceOnce
         * @see isSequenceOnce.pop
         * @see toString
         * @see peek
         */
        fun pop() : Boolean {
            val o = isSequenceOnce(sg)
            var matches = 0
            while(o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }
    }

    /**
     * matches a sequence of **str**
     *
     * this is the base class of which all parser sequence classes use
     *
     * based on [ArrayDeque] functions [toString][ArrayDeque.toString], [peek][ArrayDeque.peek], and [pop][ArrayDeque.pop]
     *
     * @param str a string to match
     * @see isSequenceZeroOrMany
     * @see isSequenceOneOrMany
     */
    inner class isSequenceOnce(str : String) {
        /**
         * the string to match
         */
        val sg = str

        /**
         * returns the accumalative value of [parser.peek] and [parser.pop]
         *
         * the original [tokenList] is [cloned][parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return "" if either [tokenList] or [str][sg] is empty
         *
         * otherwise the result of [parser.peek], which can never be an empty string
         * @see peek
         * @see pop
         */
        override fun toString() : String {
            var tmp = tokenList.clone()
            var s = parserPrep(sg)
            if (s.peek() == null || tmp.peek() == null) return ""
            val result = StringBuffer()
            while(tmp.peek() != null && s.peek() != null) {
                val x = tmp.peek()
                if (tmp.pop().equals(s.pop())) result.append(x)
            }
            return result.toString()
        }

        /**
         * the original [tokenList] is [cloned][parser.clone] to prevent modifications to the original [list][tokenList]
         *
         * @return false if [str][sg] does not match
         *
         * otherwise returns true
         * @see toString
         * @see pop
         */
        fun peek() : Boolean {
            var tmp = tokenList.clone()
            var s = parserPrep(sg)
            if (s.peek() == null || tmp.peek() == null) return false
            val expected = s.size
            var matches = 0
            while(tmp.peek() != null && s.peek() != null) if (tmp.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        /**
         *
         * **WARNING**: this **CAN** modify [tokenList]
         *
         * it is recommended to use ***if (parser.peek()) parser.pop()*** to avoid accidental modifications, where **parser** is an instance of [isSequenceOnce]
         *
         * @return false if [str][sg] does not match
         *
         * otherwise returns true
         * @see toString
         * @see peek
         */
        fun pop() : Boolean {
            var s = parserPrep(sg)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while(this@parser.peek() != null && s.peek() != null) if (this@parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }
    }
}

/**
 * pre-processes a file **src**
 *
 * the result is saved in "$src.preprocessed.$ext"
 *
 * @param src the file to be modified
 * @param ext the extention of file specified in **src**
 * @param MACROM a [Macro] array
 */
fun process(src : String, ext : String, MACROM : ArrayList<Macro>) {
    val DESTPRE: File = File("$src.preprocessed.$ext")
    var MACRO = MACROM
    if (MACRO[0].FileName != null) {
        MACRO = MACRO[0].realloc(MACRO, MACRO[0].size+1)
    }
    MACRO[0].FileName = src.substringAfterLast('/')
    println("registered macro definition for ${MACRO[0].FileName} at index ${MACRO[0].size}")
    println("processing ${MACRO[0].FileName} -> ${DESTPRE.name}")
    DESTPRE.createNewFile()
    val lex = lexer(fileToByteBuffer(File(src)), tokensNewLine)
    lex.lex()
    while (lex.current_line != null) {
        val out = parse(lex, MACRO)
        var input = lex.current_line as String
        if (input[input.length-1] == '\n') {
            input = input.dropLast(1)
        }
        println("\ninput = $input")
        println("output = $out\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        if (first_line) {
            DESTPRE.writeText(out + "\n")
            first_line = false
        }
        else DESTPRE.appendText(out + "\n")
        lex.lex()
    }
}

/**
 * parses a line
 * @param lex the current [lexer]
 * @param MACRO the [Macro] list
 */
fun parse(lex : lexer, MACRO : ArrayList<Macro>) : String {
    return expand(lex, parser(parserPrep(lex.current_line as String)), MACRO)
}

/**
 * expands a line
 * @return the expanded line
 * @param lex this is used for multi-line processing
 * @param TS the current [parser]
 * @param MACRO the current [Macro]
 * @param ARG the current argument list in an expanding function
 * @param blacklist the current list of macro's which should not be expanded
 */
fun expand(lex : lexer, TS : parser, MACRO : ArrayList<Macro>, ARG : ArrayList<String>? = null, blacklist : MutableList<String> = mutableListOf()) : String {
    println("expanding '${lex.current_line}'")
    println("blacklist = $blacklist")
    println("ARG = ${ARG}")
    val expansion = StringBuffer()
    var itterations = 0
    var maxItterations = 100
    while (itterations <= maxItterations && TS.peek() != null) {
        val space = TS.isSequenceOneOrMany(" ")
        val newline = TS.isSequenceOnce("\n")
        val directive = TS.isSequenceOnce("#")
        val define = TS.isSequenceOnce("define")
        val comment = TS.isSequenceOnce("//")
        val blockcommentstart = TS.isSequenceOnce("/*")
        val blockcommentend = TS.isSequenceOnce("*/")
        val comma = TS.isSequenceOnce(",")
        val emptyparens = TS.isSequenceOnce("()")
        val leftparenthesis = TS.isSequenceOnce("(")
        val rightparenthesis = TS.isSequenceOnce(")")
        val leftbrace = TS.isSequenceOnce("[")
        val rightbrace = TS.isSequenceOnce("]")
        val leftbracket = TS.isSequenceOnce("{")
        val rightbracket = TS.isSequenceOnce("}")
        if (comment.peek()) {
            println("clearing comment token '${TS.toString()}'")
            TS.clear()
        }
        else if(blockcommentstart.peek()) {
            var depthblockcomment = 0
            blockcommentstart.pop() // pop the first /*
            depthblockcomment++
            var itterations = 0
            var maxItterations = 1000
            while (itterations <= maxItterations) {
                if (TS.peek() == null) {
                    lex.lex()
                    if (lex.current_line == null) abort("no more lines when expecting more lines, unterminated block commment")
                    TS.tokenList = parserPrep(lex.current_line as String)
                }
                if (newline.peek()) newline.pop()
                else if (blockcommentstart.peek()) {
                    depthblockcomment++
                    blockcommentstart.pop()
                } else if (blockcommentend.peek()) {
                    depthblockcomment--
                    blockcommentend.pop()
                    if (depthblockcomment == 0) {
                        break
                    }
                } else TS.pop()
                itterations++
            }
            if (itterations > maxItterations) abort("itterations expired")
        }
        else if (emptyparens.peek()) {
            println("popping empty parenthesis token '${emptyparens.toString()}'")
            expansion.append(emptyparens.toString())
            emptyparens.pop()
        }
        else if (newline.peek()) {
            println("popping newline token '${newline.toString()}'")
            newline.pop()
        }
        else if ((space.peek() && TS.lineinfo.column == 1) || (TS.lineinfo.column == 1 && directive.peek())) {
            /*
            5
Constraints
The only white-space characters that shall appear between preprocessing tokens within a prepro-
cessing directive (from just after the introducing # preprocessing token through just before the
terminating new-line character) are space and horizontal-tab (including spaces that have replaced
comments or possibly other white-space characters in translation phase 3).

             */
            if (space.peek()) {
                // case 1, space at start of file followed by define
                println("popping space token '${space.toString()}'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                println("popping directive token '${directive.toString()}'")
                directive.pop()
                if (space.peek()) {
                    // case 1, space at start of file followed by define
                    println("popping space token '${space.toString()}'")
                    space.pop()
                }
                if (define.peek()) {
                    // case 2, define at start of line
                    println("popping define statement '${TS.toString()}'")
                    processDefine("#" + TS.toString(), MACRO)
                    TS.clear()
                }
            }
        }
        else {
            val index = MACRO[0].size - 1
            val ss = TS.peek()
            val name : String
            if (ss == null) abort("somthing is wrong")
            name = ss as String
            println("popping normal token '$name'")
            /*
            kotlin supports new line statements but functions MUST not contain
            a new line between the identifier and the left parenthesis
             */
            var isalnum : Boolean = name.matches("[A-Za-z0-9]*".toRegex())
            var macrofunctionexists : Boolean = false
            var macrofunctionindex = 0
            var macroobjectexists : Boolean = false
            var macroobjectindex = 0
            var macrofunctionexistsAsArgument : Boolean = false
            var macroobjectexistsAsArgument : Boolean = false
            if (isalnum) {
                macrofunctionindex = macro_exists(name, Macro().Directives().Define().Types().FUNCTION, index, MACRO)
                if (currentmacroexists) {
                    macrofunctionexists = true
                }
                else {
                    macroobjectindex = macro_exists(name, Macro().Directives().Define().Types().OBJECT, index, MACRO)
                    if (currentmacroexists) {
                        macroobjectexists = true
                    }
                }
            }
            if (macroobjectexists || macrofunctionexists) {
                var isfunction: Boolean = false

                println("looking ahead")
                val TSA = TS.clone()
                val TSAspace = TSA.isSequenceOneOrMany(" ")
                val TSAleftparen = TSA.isSequenceOnce("(")
                TSA.pop() // pop the function name
                if (TSAspace.peek()) TSAspace.pop() // pop any spaces in between
                if (TSAleftparen.peek()) isfunction = true

                var skip : Boolean = false
                if (blacklist.contains(name)) skip = true
                if (isfunction) {
                    if (macrofunctionexists && skip == false) {
                        println("'${TS.peek()}' is a function")
                        // we know that this is a function, proceed to attempt to extract all arguments
                        var depthparenthesis = 0
                        var depthbrace = 0
                        var depthbracket = 0
                        TS.pop() // pop the function name
                        if (space.peek()) space.pop() // pop any spaces in between
                        TS.pop() // pop the first (
                        depthparenthesis++
                        var itterations = 0
                        var maxItterations = 100
                        var argc = 0
                        var argv: MutableList<String> = mutableListOf()
                        argv.add("")
                        while (itterations <= maxItterations) {
                            if (newline.peek()) {
                                newline.pop()
                                val l = TS.peek()
                                if (l == null) {
                                    println("ran out of tokens, grabbing more tokens from the next line")
                                    lex.lex()
                                    if (lex.current_line == null) abort("no more lines when expecting more lines")
                                    TS.tokenList = parserPrep(lex.current_line as String)
                                }
                            }
                            println("popping '${TS.peek()}'")
                            if (leftparenthesis.peek()) {
                                depthparenthesis++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (leftbrace.peek()) {
                                depthbrace++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (leftbracket.peek()) {
                                depthbracket++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightparenthesis.peek()) {
                                depthparenthesis--
                                if (depthparenthesis == 0) {
                                    TS.pop()
                                    break
                                }
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightbrace.peek()) {
                                depthbrace--
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightbracket.peek()) {
                                depthbracket--
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (comma.peek()) {
                                if (depthparenthesis == 1) {
                                    argc++
                                    argv.add("")
                                    comma.pop()
                                } else argv[argc] = argv[argc].plus(TS.pop())
                            } else argv[argc] = argv[argc].plus(TS.pop())
                            itterations++
                        }
                        if (itterations > maxItterations) println("itterations expired")
                        argc++
                        println("argc = $argc")
                        println("argv = $argv")
                        val macroTypeDependantIndex = macrofunctionindex
                        println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].replacementList}")
                        println("macro  args = ${MACRO[index].Macros[macroTypeDependantIndex].Arguments}")
                        println("target args = $argv")
                        if (MACRO[index].Macros[macroTypeDependantIndex].replacementList != null) {
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].replacementList as String),
                                tokensNewLine
                            )
                            lex.lex()
                            if (lex.current_line != null) {
                                val parser = parser(parserPrep(lex.current_line as String))
                                /*
                                1
After the arguments for the invocation of a function-like macro have been identified, argument
substitution takes place. A parameter in the replacement list, unless preceded by a # or ## prepro-
cessing token or followed by a ## preprocessing token (see below), is replaced by the corresponding
argument after all macros contained therein have been expanded. Before being substituted, each
argument’s preprocessing tokens are completely macro replaced as if they formed the rest of the
preprocessing file; no other preprocessing tokens are available.
*/
                                var i = 0
                                println("expanding arguments: $argc arguments to expand")
                                while (i < argc) {
                                    // expand each argument
                                    val lex = lexer(stringToByteBuffer(argv[i]), tokensNewLine)
                                    lex.lex()
                                    if (lex.current_line != null) {
                                        val parser = parser(parserPrep(lex.current_line as String))
                                        val e = expand(lex, parser, MACRO)
                                        println("macro expansion '${argv[i]}' returned $e")
                                        argv[i] = e
                                    }
                                    i++
                                }
                                println("expanded arguments: $argc arguments expanded")
                                val associated_arguments = toMacro(
                                    MACRO[index].Macros[macroTypeDependantIndex].Arguments,
                                    argv as List<String>
                                )
                                println("blacklisting $name")
                                blacklist.add(name)
                                println("MACRO[index].Macros[macroTypeDependantIndex].Arguments = ${MACRO[index].Macros[macroTypeDependantIndex].Arguments}")
                                val e = expand(lex, parser, associated_arguments, MACRO[index].Macros[macroTypeDependantIndex].Arguments, blacklist)
                                println("current expansion is $expansion")
                                println("macro FUNCTION expansion $name returned $e")
                                val lex2 = lexer(stringToByteBuffer(e), tokensNewLine)
                                lex2.lex()
                                if (lex2.current_line != null) {
                                    val parser = parser(parserPrep(lex2.current_line as String))
                                    val e2 = expand(lex2, parser, MACRO, MACRO[index].Macros[macroTypeDependantIndex].Arguments, blacklist)
                                    println("current expansion is $expansion")
                                    println("macro FUNCTION expansion '$e' returned $e2")
                                    expansion.append(e2)
                                    println("current expansion is $expansion")
                                }
                            }
                        }
                    }
                    else if (macrofunctionexists && skip == true) {
                        println("'$name' is a function but it is currently being expanded")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    }
                    else {
                        println("'$name' is a function but no associated macro exists")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    }
                }
                else {
                    println("'$name' is an object")
                    TS.pop() // pop the macro name
                    if (macroobjectexists) {
                        if (skip == true) {
                            println("but it is currently being expanded")
                            expansion.append(name)
                        }
                        else {
                            val macroTypeDependantIndex = macroobjectindex
                            println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].replacementList}")
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].replacementList as String),
                                tokensNewLine
                            )
                            lex.lex()
                            if (lex.current_line != null) {
                                if (ARG != null) {
                                    println("ARG = ${ARG}")
                                    if (!ARG.contains(name)) {
                                        println("blacklisting $name")
                                        blacklist.add(name)
                                    } else {
                                        println("$name is an argument")
                                    }
                                }
                                else {
                                    println("warning: ARG is null")
                                    println("blacklisting $name")
                                    blacklist.add(name)
                                }
                                val parser = parser(parserPrep(lex.current_line as String))
                                val e = expand(lex, parser, MACRO, ARG, blacklist)
                                println("macro OBJECT expansion $name returned $e")
                                expansion.append(e)
                            }
                        }
                    }
                    else {
                        println("but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            }
            else expansion.append(TS.pop())
        }
        itterations++
    }
    if (itterations > maxItterations) println("itterations expired")
    println("expansion = $expansion")
    return expansion.toString()
}

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of paramaters
 */
fun extract_arguments(arg : String, MACRO : ArrayList<Macro>)  : ArrayList<String>? {
    fun filterSplit(arg : String, ex : balanced, b : balanced.balanceList) : ArrayList<String> {
        var Arguments : ArrayList<String> = arrayListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    Arguments.add(arg)
                    println(Arguments[0])
                } else {
                    var s : String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    Arguments.add(s)
                    println(Arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i]+1, ex.splitterLocation[i+1]).trimStart()
                        Arguments.add(s)
                        println(Arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    Arguments.add(s)
                    println(Arguments[i])
                }
            }
            else {
                ex.info()
                abort("unbalanced code")
            }
        }
        else if (ex.containsR(arg, b)) {
            // unbalanced
            abort("unbalanced code")
        }
        else {
            var a : MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                var s : String = a.get(i).trimStart().trimEnd()
                Arguments.add(s)
                i++
            }
        }
        println("Arguments List = $Arguments")
        return Arguments as ArrayList
    }
    println("extracting arguments for $arg")
    // first, determine the positions of all tokens
    var balance = balanced.balanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = balanced()
    return filterSplit(arg, ex, balance)
}

/**
 * checks if the desired macro exists in the [Macro] list
 */
fun macro_exists(token : String, type : String, index : Int, MACRO : ArrayList<Macro>) : Int {
    // used to detect existing definitions for #define
    currentmacroexists = false
    // if empty return 0 and do not set currentmacroexists
    if (MACRO[index].Macros[0].FullMacro == null) return 0
    var i = 0
    while (i <= MACRO[index].Macros.lastIndex) {
        if (MACRO[index].Macros[i].Token.equals(token) && MACRO[index].Macros[i].Type.equals(type)) {
            println("token and type matches existing definition ${MACRO[index].Macros[i].Token} type ${MACRO[index].Macros[i].Type}")
            currentmacroexists = true
            println("returning $i")
            return i
        }
        else println("token $token or type $type does not match current definition token ${MACRO[index].Macros[i].Token} type ${MACRO[index].Macros[i].Type}")
        i++
    }
    return i
}

/**
 * lists the current macros in a [Macro] list
 */
fun macro_list(index : Int = 0, MACRO : ArrayList<Macro>) {
    if (MACRO[index].Macros[0].FullMacro == null) return
    println("LISTING MACROS")
    var i = 0
    while (i <= MACRO[index].Macros.lastIndex) {
        println("[$i].FullMacro  = ${MACRO[index].Macros[i].FullMacro}")
        println("[$i].Type       = ${MACRO[index].Macros[i].Type}")
        println("[$i].Token      = ${MACRO[index].Macros[i].Token}")
        if (MACRO[index].Macros[i].Arguments != null)
            println("[$i].Arguments  = ${MACRO[index].Macros[i].Arguments}")
        println("[$i].replacementList      = ${MACRO[index].Macros[i].replacementList}")
        i++
    }
    println("LISTED MACROS")
}

/**
 * converts a pair of [ArrayList]'s into a [Macro] array
 */
fun toMacro(macro_arguments : ArrayList<String>?, actual_arguments : ArrayList<String>?) : ArrayList<Macro> {
    println("${macro_arguments!!.size} == ${actual_arguments!!.size} is ${macro_arguments!!.size == actual_arguments!!.size}")
    if ((macro_arguments!!.size == actual_arguments!!.size) == false) {
        abort("size mismatch: expected ${macro_arguments!!.size}, got ${actual_arguments
        !!.size}")
    }
    var associated_arguments = arrayListOf(Macro())
    var i = 0
    associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
    associated_arguments[0].Macros[i].Type = Macro().Directives().Define().Types().OBJECT
    associated_arguments[0].Macros[i].Token = macro_arguments[i]
    associated_arguments[0].Macros[i].replacementList = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        associated_arguments[0].Macros = associated_arguments[0].Macros[0].realloc(associated_arguments[0].Macros, associated_arguments[0].Macros[0].size+1)
        associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
        associated_arguments[0].Macros[i].Type = Macro().Directives().Define().Types().OBJECT
        associated_arguments[0].Macros[i].Token = macro_arguments[i]
        associated_arguments[0].Macros[i].replacementList = actual_arguments[i]
        i++
    }
    macro_list(MACRO = associated_arguments)
    return associated_arguments
}

/**
 * converts an [ArrayList] and a [List] into a [Macro] array
 */
fun toMacro(macro_arguments : ArrayList<String>?, actual_arguments : List<String>?) : ArrayList<Macro> {
    println("${macro_arguments!!.size} == ${actual_arguments!!.size} is ${macro_arguments!!.size == actual_arguments!!.size}")
    if ((macro_arguments!!.size == actual_arguments!!.size) == false) {
        abort("size mismatch: expected ${macro_arguments!!.size}, got ${actual_arguments
        !!.size}")
    }
    var associated_arguments = arrayListOf(Macro())
    var i = 0
    associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
    associated_arguments[0].Macros[i].Type = Macro().Directives().Define().Types().OBJECT
    associated_arguments[0].Macros[i].Token = macro_arguments[i]
    associated_arguments[0].Macros[i].replacementList = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        associated_arguments[0].Macros = associated_arguments[0].Macros[0].realloc(associated_arguments[0].Macros, associated_arguments[0].Macros[0].size+1)
        associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
        associated_arguments[0].Macros[i].Type = Macro().Directives().Define().Types().OBJECT
        associated_arguments[0].Macros[i].Token = macro_arguments[i]
        associated_arguments[0].Macros[i].replacementList = actual_arguments[i]
        i++
    }
    macro_list(MACRO = associated_arguments)
    return associated_arguments
}
