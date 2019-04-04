package preprocessor.extra

import preprocessor.core.*
import preprocessor.utils.fileToByteBuffer
import java.io.File
import java.util.ArrayList

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
    val lex = lexer(fileToByteBuffer(File(src)), globalVariables.tokensNewLine)
    val lex2 = lex.clone()
    lex2.lex()
    lex.lex()
    println("lex.currentLine is ${lex.current_line}")
    println("lex2.currentLine is ${lex2.current_line}")
    while (lex.current_line != null) {
        val out = parse(lex, MACRO)
        var input = lex.current_line as String
        if (input[input.length-1] == '\n') {
            input = input.dropLast(1)
        }
        println("\ninput = $input")
        println("output = $out\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        if (globalVariables.firstLine) {
            DESTPRE.writeText(out + "\n")
            globalVariables.firstLine = false
        }
        else DESTPRE.appendText(out + "\n")
        lex.lex()
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
        val i = macroExists(token, type, index, MACRO)
        if (globalVariables.currentMacroExists) {
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
        val i = macroExists(token, type, index, MACRO)
        if (globalVariables.currentMacroExists) {
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
        val b = Balanced()
        MACRO[index].Macros[macro_index].Arguments =
            extract_arguments(b.extract_text(t).drop(1).dropLast(1), MACRO)
        MACRO[index].Macros[macro_index].replacementList = t.substring(b.end[0]+1).trimStart()
    }
    println("Type       = ${MACRO[index].Macros[macro_index].Type}")
    println("Token      = ${MACRO[index].Macros[macro_index].Token}")
    if (MACRO[index].Macros[macro_index].Arguments != null)
        println("Arguments  = ${MACRO[index].Macros[macro_index].Arguments}")
    println("replacementList      = ${MACRO[index].Macros[macro_index].replacementList}")
    macroList(index, MACRO)
    // definition names do not expand
    // definition values do expand
}
