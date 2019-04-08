@file:Suppress("unused")

package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import java.util.ArrayList

/**
 *
 * This class is used to house all macro definitions
 *
 * @sample Tests.generalUsage
 *
 * @see Directives
 * @see Directives.Define
 *
 */
class Macro {
    /**
     * this class is used to obtain predefined values such as directive names and types
     *
     * all preprocessor directives start with a #
     * @see Define
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inner class Directives {
        /**
         * contains predefined [values][Define.value] and [types][Define.Types] for the
         * [#define][Define.value] directive
         * @see Directives
         */
        inner class Define {
            /**
             * the ***#define*** directive associates an identifier to a replacement list
             * the Default value of this is **define**
             * @see Types
             */
            val value: String = "define"

            /**
             * the valid types of a definition directive macro
             * @see Object
             * @see Function
             */
            inner class Types {
                /**
                 * the Object type denotes the standard macro definition, in which all text is matched with
                 *
                 * making **Object** lowercase conflists with the top level declaration **object**
                 *
                 * @see Types
                 * @see Function
                 * @sample objectSample
                 * @sample objectUsage
                 */
                @Suppress("PropertyName")
                val Object: String = "object"
                /**
                 * the Function type denotes the Funtion macro definition, in which all text that is followed by
                 * parentheses is matched with
                 *
                 * making **Function** lowercase must mean [Object] must also be lowercase
                 * to maintain naming pairs (as **Object, function**, and **object, Function**
                 * just looks weird)
                 *
                 * unfortunately this is impossible as it would conflict with the top level
                 * declaration **object**
                 *
                 * @see Types
                 * @see Object
                 * @sample functionSample
                 * @sample functionUsage
                 */
                @Suppress("PropertyName")
                val Function: String = "function"

                private fun objectSample() {
                    /* ignore this block comment

                    #define object value
                    object
                    object(object).object[object]

                    ignore this block comment */
                }

                private fun objectUsage() {
                    val c = arrayListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A B00"
                    c[0].macros[0].token = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Object
                    println(c[0].macros[0].type)
                }

                private fun functionSample() {
                    /* ignore this block comment

                    #define object value
                    #define function value
                    object
                    function(object).object[function(function()]

                    ignore this block comment */
                }

                private fun functionUsage() {
                    val c = arrayListOf(Macro())
                    c[0].fileName = "test"
                    c[0].macros[0].fullMacro = "A() B00"
                    c[0].macros[0].token = "A"
                    c[0].macros[0].replacementList = "B00"
                    c[0].macros[0].type =
                        Macro().Directives().Define().Types().Function
                    println(c[0].macros[0].type)
                }
            }
        }
    }

    /**
     * the internals of the Macro class
     *
     * this is where all macros are kept, this is managed via [realloc]
     * @sample Tests.generalUsage
     */
    inner class MacroInternal {
        /**
         * the current size of the [macro][MacroInternal] list
         *
         * can be used to obtain the last added macro
         *
         * @sample Tests.sizem
         */
        var size: Int = 0
        /**
         * the full macro definition
         *
         * contains the full line at which the definition appears
         */
        var fullMacro: String? = null
        /**
         * contains the definition **identifier**
         */
        var token: String? = null
        /**
         * contains the definition **type**
         */
        var type: String? = null
        /**
         * contains the definition **arguments**,
         * valid only for
         * [Function][Macro.Directives.Define.Types.Function]
         * type definitions
         *
         * this defaults to **null**
         *
         */
        var arguments: ArrayList<String>? = null
        /**
         * this contains the definition replacement list
         */
        var replacementList: String? = null

        /**
         * this adds a new macro to the [macro][MacroInternal] list
         *
         * implementation of the C function **realloc**
         *
         * allocation is recorded in [size][Macro.MacroInternal.size] paramater
         *
         * @param m [internal macro list][MacroInternal]
         * @param newSize the new [size][MacroInternal.size] to allocate the [internal macro list][MacroInternal] to
         * @see <a href="http://man7.org/linux/man-pages/man3/realloc.3p.html">realloc (C function)</a>
         * @sample Tests.reallocUsageInternal
         */
        fun realloc(m: ArrayList<MacroInternal>, newSize: Int) {
            m.add(MacroInternal())
            m[0].size = newSize
        }
    }

    /**
     * the current size of the [macro][Macro] list
     *
     * can be used to obtain the last added [macro group][MacroInternal]
     *
     * @sample Tests.size
     */
    var size: Int = 0
    /**
     * the name of the file containing this [macro][Macro] list
     */
    var fileName: String? = null
    /**
     * the [macro][MacroInternal] list
     */
    var macros: ArrayList<MacroInternal>

    init {
        this.size = 1
        macros = arrayListOf(MacroInternal())
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
     * @sample Tests.reallocUsage
     */
    fun realloc(m: ArrayList<Macro>, NewSize: Int) {
        m.add(Macro())
        m[0].size = NewSize
    }

    private class Tests {
        fun generalUsage() {
            val c = arrayListOf(Macro())
            c[0].fileName = "test"
            c[0].macros[0].fullMacro = "A B"
            c[0].macros[0].token = "A"
            c[0].macros[0].replacementList = "B00"
            println(c[0].macros[0].replacementList)
            c[0].realloc(c, c[0].size + 1)
            c[1].fileName = "test"
            c[1].macros[0].fullMacro = "A B"
            c[1].macros[0].token = "A"
            c[1].macros[0].replacementList = "B10"
            println(c[1].macros[0].replacementList)
            c[1].macros[0].realloc(c[1].macros, c[1].macros[0].size + 1)
            c[1].fileName = "test"
            c[1].macros[1].fullMacro = "A B"
            c[1].macros[1].token = "A"
            c[1].macros[1].replacementList = "B11"
            println(c[1].macros[1].replacementList)
            c[1].macros[0].realloc(c[1].macros, c[1].macros[0].size + 1)
            c[1].fileName = "test"
            c[1].macros[2].fullMacro = "A B"
            c[1].macros[2].token = "A"
            c[1].macros[2].replacementList = "B12"
            println(c[1].macros[2].replacementList)
        }

        fun reallocUsage() {
            val c = arrayListOf(Macro())
            // allocate a new index
            c[0].realloc(c, c[0].size + 1)
            // assign some values
            c[0].fileName = "test"
            c[1].fileName = "test"
        }

        fun reallocUsageInternal() {
            val c = arrayListOf(Macro())
            // allocate a new index
            c[0].macros[0].realloc(c[0].macros, c[0].macros[0].size + 1)
            // assign some values
            c[0].macros[0].fullMacro = "A A"
            c[0].macros[1].fullMacro = "A B"
        }

        fun size() {
            val c = arrayListOf(Macro())
            // allocate a new macro
            c[0].macros[0].realloc(c[0].macros, c[0].macros[0].size + 1)
            c[0].macros[1].fullMacro = "A B"
            println(c[0].macros[1].replacementList)
            // obtain base index
            val index = c[0].size - 1
            // obtain last macro index
            val macroIndex = c[0].macros[0].size - 1
            if (c[index].macros[macroIndex].fullMacro.equals(c[0].macros[1].fullMacro))
                println("index matches")
        }

        fun sizem() {
            var c = arrayListOf(Macro())
            c[0].fileName = "test1"
            c[0].realloc(c, c[0].size + 1)
            c[1].fileName = "test2"
            val index = c[0].size - 1
            if (c[index].fileName.equals(c[1].fileName))
                println("index matches")
        }
    }
}

/**
 * checks if the desired macro exists in the [Macro] list
 */
fun macroExists(token: String, type: String, index: Int, macro: ArrayList<Macro>): Int {
    // used to detect existing definitions for #define
    globalVariables.currentMacroExists = false
    // if empty return 0 and do not set globalVariables.currentMacroExists
    if (macro[index].macros[0].fullMacro == null) return 0
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        if (macro[index].macros[i].token.equals(token) && macro[index].macros[i].type.equals(type)) {
            // Line is longer than allowed by code style (> 120 columns)
            println(
                "token and type matches existing definition ${macro[index].macros[i].token} type " +
                        "${macro[index].macros[i].type}"
            )
            globalVariables.currentMacroExists = true
            println("returning $i")
            return i
        }
        // Line is longer than allowed by code style (> 120 columns)
        else println(
            "token $token or type $type does not match current definition token " +
                    "${macro[index].macros[i].token} type ${macro[index].macros[i].type}"
        )
        i++
    }
    return i
}

/**
 * lists the current macros in a [Macro] list
 */
fun macroList(index: Int = 0, macro: ArrayList<Macro>) {
    if (macro[index].macros[0].fullMacro == null) return
    println("LISTING macros")
    var i = 0
    while (i <= macro[index].macros.lastIndex) {
        println("[$i].fullMacro  = ${macro[index].macros[i].fullMacro}")
        println("[$i].type       = ${macro[index].macros[i].type}")
        println("[$i].token      = ${macro[index].macros[i].token}")
        if (macro[index].macros[i].arguments != null)
            println("[$i].arguments  = ${macro[index].macros[i].arguments}")
        println("[$i].replacementList      = ${macro[index].macros[i].replacementList}")
        i++
    }
    println("LISTED macros")
}

/**
 * lists the current macros in a [Macro] list
 *
 * this version lists ALL [Macro]s in the current [Macro] list in all available file index's
 */
fun macroList(macro: ArrayList<Macro>) {
    if (macro.size == 0) {
        println("macro list is empty")
        return
    }
    var i = 0
    while (i < macro.size) {
        if (macro[i].fileName == null) {
            println("macro list for index $i is incomplete")
            break
        }
        println("LISTING macros for file ${macro[i].fileName}")
        macroList(i, macro)
        println("LISTED macros for file ${macro[i].fileName}")
        i++
    }
}

/**
 * converts a pair of [ArrayList]'s into a [Macro] array
 */
fun toMacro(macro_arguments: ArrayList<String>?, actual_arguments: ArrayList<String>?): ArrayList<Macro> {
    // Line is longer than allowed by code style (> 120 columns)
    println(
        "${macro_arguments!!.size} == ${actual_arguments!!.size} is " +
                "${macro_arguments.size == actual_arguments.size}"
    )
    if (macro_arguments.size != actual_arguments.size) {
        abort("size mismatch: expected ${macro_arguments.size}, got ${actual_arguments.size}")
    }
    val associatedArguments = arrayListOf(Macro())
    var i = 0
    associatedArguments[0].macros[i].fullMacro =
        "${Macro().Directives().Define().value} ${macro_arguments[i]} ${actual_arguments[i]}"
    associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
    associatedArguments[0].macros[i].token = macro_arguments[i]
    associatedArguments[0].macros[i].replacementList = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        associatedArguments[0].macros[0].realloc(
            associatedArguments[0].macros,
            associatedArguments[0].macros[0].size + 1
        )
        associatedArguments[0].macros[i].fullMacro =
            "${Macro().Directives().Define().value} ${macro_arguments[i]} ${actual_arguments[i]}"
        associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
        associatedArguments[0].macros[i].token = macro_arguments[i]
        associatedArguments[0].macros[i].replacementList = actual_arguments[i]
        i++
    }
    macroList(macro = associatedArguments)
    return associatedArguments
}

/**
 * converts an [ArrayList] and a [List] into a [Macro] array
 */
fun toMacro(macro_arguments: ArrayList<String>?, actual_arguments: List<String>?): ArrayList<Macro> {
    // Line is longer than allowed by code style (> 120 columns)
    println(
        "${macro_arguments!!.size} == ${actual_arguments!!.size} is " +
                "${macro_arguments.size == actual_arguments.size}"
    )
    if (macro_arguments.size != actual_arguments.size) {
        abort("size mismatch: expected ${macro_arguments.size}, got ${actual_arguments.size}")
    }
    val associatedArguments = arrayListOf(Macro())
    var i = 0
    associatedArguments[0].macros[i].fullMacro =
        "${Macro().Directives().Define().value} ${macro_arguments[i]} ${actual_arguments[i]}"
    associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
    associatedArguments[0].macros[i].token = macro_arguments[i]
    associatedArguments[0].macros[i].replacementList = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        // Line is longer than allowed by code style (> 120 columns)
        associatedArguments[0].macros[0].realloc(
            associatedArguments[0].macros,
            associatedArguments[0].macros[0].size + 1
        )
        associatedArguments[0].macros[i].fullMacro =
            "${Macro().Directives().Define().value} ${macro_arguments[i]} ${actual_arguments[i]}"
        associatedArguments[0].macros[i].type = Macro().Directives().Define().Types().Object
        associatedArguments[0].macros[i].token = macro_arguments[i]
        associatedArguments[0].macros[i].replacementList = actual_arguments[i]
        i++
    }
    macroList(macro = associatedArguments)
    return associatedArguments
}
