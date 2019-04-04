package preprocessor.core

import preprocessor.extra.globalVariables
import preprocessor.utils.abort
import preprocessor.utils.dequeToString
import java.util.*

/**
 * prepares the input **line** for consumption by the parser
 * @return an [ArrayDeque] which is split by the global variable **tokens**
 *
 * this [ArrayDeque] preserves the tokens in which it is split by
 * @see parser
 */
fun parserPrep(line: String) : ArrayDeque<String> {
    val st = StringTokenizer(line, globalVariables.tokens, true)
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
