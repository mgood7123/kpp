package preprocessor.utils.extra

import preprocessor.core.Macro
import preprocessor.core.lexer
import preprocessor.core.parser
import preprocessor.core.parserPrep
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
