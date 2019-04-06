package preprocessor.core

import java.nio.ByteBuffer
import preprocessor.utils.conversion.stringToDeque

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

    fun clone() : lexer {
        return lexer(this.f.duplicate(), this.delimiters)
    }

    fun clone(newdelimiters : String) : lexer {
        return lexer(this.f.duplicate(), newdelimiters)
    }
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
