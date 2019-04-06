package preprocessor.core

import preprocessor.base.globalVariables
import preprocessor.utils.core.abort
import java.util.ArrayList

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
 * checks if the desired macro exists in the [Macro] list
 */
fun macroExists(token : String, type : String, index : Int, MACRO : ArrayList<Macro>) : Int {
    // used to detect existing definitions for #define
    globalVariables.currentMacroExists = false
    // if empty return 0 and do not set globalVariables.currentMacroExists
    if (MACRO[index].Macros[0].FullMacro == null) return 0
    var i = 0
    while (i <= MACRO[index].Macros.lastIndex) {
        if (MACRO[index].Macros[i].Token.equals(token) && MACRO[index].Macros[i].Type.equals(type)) {
            println("token and type matches existing definition ${MACRO[index].Macros[i].Token} type ${MACRO[index].Macros[i].Type}")
            globalVariables.currentMacroExists = true
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
fun macroList(index : Int = 0, MACRO : ArrayList<Macro>) {
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
    macroList(MACRO = associated_arguments)
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
    macroList(MACRO = associated_arguments)
    return associated_arguments
}
