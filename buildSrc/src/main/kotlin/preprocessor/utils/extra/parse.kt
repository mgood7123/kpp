package preprocessor.utils.extra

import preprocessor.core.Macro
import preprocessor.core.Lexer
import preprocessor.core.Parser
import preprocessor.core.parserPrep
import java.util.ArrayList

/**
 * parses a line
 * @param lex the current [Lexer]
 * @param MACRO the [Macro] list
 */
fun parse(lex: Lexer, MACRO: ArrayList<Macro>): String {
    return expand(
        lex,
        Parser(parserPrep(lex.currentLine as String)),
        MACRO
    )
}
