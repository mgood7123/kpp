package preprocessor.core

import preprocessor.extra.expand
import java.util.ArrayList

/**
 * parses a line
 * @param lex the current [lexer]
 * @param MACRO the [Macro] list
 */
fun parse(lex : lexer, MACRO : ArrayList<Macro>) : String {
    return expand(
        lex,
        parser(parserPrep(lex.current_line as String)),
        MACRO
    )
}
