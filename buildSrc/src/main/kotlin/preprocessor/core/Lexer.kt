package preprocessor.core

import java.nio.ByteBuffer
import preprocessor.utils.conversion.stringToDeque
import java.util.*

/**
 * a minimal Lexer implementation
 *
 * we traditionally process the input file character by character
 *
 * append each processed character to a buffer
 *
 * then return that buffer when a specific delimiter is found
 *
 * @param stm the byte buffer to read from, set up via
 * [fileToByteBuffer][preprocessor.utils.conversion.fileToByteBuffer]
 * @param delimiter the delimiters to split the input by
 * @see Parser
 * @see Lexer.lex
 */
@Suppress("unused")
class Lexer(stm: ByteBuffer, delimiter: String) {
    private val f: ByteBuffer = stm
    private val delimiters: String = delimiter
    private val d: ArrayDeque<String> = stringToDeque(delimiters)
    /**
     * this is the current line the Lexer of on
     *
     */
    var currentLine: String? = null

    /**
     *
     */
    inner class InternalLineInfo {
        /**
         *
         */
        inner class Default {
            /**
             *
             */
            var column: Int = 1
            /**
             *
             */
            var line: Int = 1
        }

        /**
         *
         */
        var column: Int = Default().column
        /**
         *
         */
        var line: Int = Default().line
    }

    private var lineInfo: InternalLineInfo = InternalLineInfo()

    /**
     * returns an exact duplicate of the current [Lexer]
     */
    fun clone(): Lexer {
        return Lexer(this.f.duplicate(), this.delimiters)
    }

    /**
     * returns a duplicate of this [Lexer] with the specified delimiters
     */
    fun clone(newDelimiters: String): Lexer {
        return Lexer(this.f.duplicate(), newDelimiters)
    }

    /**
     * sets the variable [currentLine] to the
     * [buffer][ByteBuffer] (converted to a
     * [String]) when a specific
     * [delimiter][Lexer] is found
     *
     * sets the variable [currentLine] to
     * **null** upon EOF
     */
    fun lex() {
        /*
        in order to make a Lexer, we traditionally process the input file
        character by character, appending each to a buffer, then returning
        that buffer when a specific delimiter is found
        */
        val isDelimiter = false
        if (f.remaining() == 0) {
            currentLine = null
            return
        }
        val b = StringBuffer()
        while (f.remaining() != 0 && !isDelimiter) {
            val s = f.get().toChar().toString()
            b.append(s)
            if (s == "\n") {
                lineInfo.column = lineInfo.Default().column
                lineInfo.line++
            } else lineInfo.column++
            if (d.contains(s)) break
        }
        currentLine = b.toString()
        return
    }
}
