package preprocessor.core

import preprocessor.utils.extra.Balanced
import preprocessor.utils.extra.extractArguments
import preprocessor.base.globalVariables
import preprocessor.utils.extra.parse
import preprocessor.utils.conversion.fileToByteBuffer
import preprocessor.utils.core.realloc
import java.io.File
import java.util.ArrayList

/**
 * pre-processes a file **src**
 *
 * the result is saved in "$src${globalVariables.preprocessedExtension}$extension$extension"
 *
 * @param src the file to be modified
 * @param extension the extention of file specified in **src**
 * @param MACROM a [Macro] array
 */
fun process(src : String, extension : String, MACROM : ArrayList<Macro>) {
    val DESTPRE: File = File("$src${globalVariables.preprocessedExtension}.$extension")
    var MACRO = MACROM
    if (MACRO[0].fileName != null) {
        MACRO = MACRO[0].realloc(MACRO, MACRO[0].size+1)
    }
    MACRO[0].fileName = src.substringAfterLast('/')
    println("registered macro definition for ${MACRO[0].fileName} at index ${MACRO[0].size}")
    println("processing ${MACRO[0].fileName} -> ${DESTPRE.name}")
    DESTPRE.createNewFile()
    val lex = Lexer(fileToByteBuffer(File(src)), globalVariables.tokensNewLine)
    val lex2 = lex.clone()
    lex2.lex()
    lex.lex()
    println("lex.currentLine is ${lex.currentLine}")
    println("lex2.currentLine is ${lex2.currentLine}")
    while (lex.currentLine != null) {
        val out = parse(lex, MACRO)
        var input = lex.currentLine as String
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
    var macro_index = MACRO[index].macros[0].size
    // to include the ability to redefine existing definitions, we must save to local variables first
    val full_macro : String = line.trimStart().drop(1).trimStart()
    var type : String = ""
    // determine Token type
    if (full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart().equals(full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()?.substringBefore('('))!!)
        type = Macro().Directives().Define().Types().Object
    else
        type = Macro().Directives().Define().Types().Function
    var token : String = ""
    if (type.equals(Macro().Directives().Define().Types().Object)) {
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
            if (MACRO[index].macros[macro_index].fullMacro != null) {
                MACRO[index].macros = MACRO[index].macros[0].realloc(
                    MACRO[index].macros,
                    MACRO[index].macros[0].size + 1
                )
            }
            macro_index = MACRO[index].macros[0].size
        }
        MACRO[index].macros[macro_index].fullMacro = line.trimStart().trimEnd()
        MACRO[index].macros[macro_index].token = token
        MACRO[index].macros[macro_index].type = type
        if (!empty) {
            MACRO[index].macros[macro_index].replacementList =
                full_macro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        }
        else MACRO[index].macros[macro_index].replacementList = ""
    } else {
        // function
        token =
            full_macro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macroExists(token, type, index, MACRO)
        if (globalVariables.currentMacroExists) {
            macro_index = i
        }
        else {
            if (MACRO[index].macros[macro_index].fullMacro != null) {
                MACRO[index].macros = MACRO[index].macros[0].realloc(
                    MACRO[index].macros,
                    MACRO[index].macros[0].size + 1
                )
            }
            macro_index = MACRO[index].macros[0].size
        }
        MACRO[index].macros[macro_index].fullMacro = line.trimStart().trimEnd()
        MACRO[index].macros[macro_index].token = token
        MACRO[index].macros[macro_index].type = type
        // obtain the function arguments
        val t = MACRO[index].macros[macro_index].fullMacro?.substringAfter(' ')!!
        val b = Balanced()
        MACRO[index].macros[macro_index].arguments =
            extractArguments(b.extractText(t).drop(1).dropLast(1))
        MACRO[index].macros[macro_index].replacementList = t.substring(b.end[0]+1).trimStart()
    }
    println("Type       = ${MACRO[index].macros[macro_index].type}")
    println("Token      = ${MACRO[index].macros[macro_index].token}")
    if (MACRO[index].macros[macro_index].arguments != null)
        println("Arguments  = ${MACRO[index].macros[macro_index].arguments}")
    println("replacementList      = ${MACRO[index].macros[macro_index].replacementList}")
    macroList(index, MACRO)
    // definition names do not expand
    // definition values do expand
}