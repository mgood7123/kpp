package preprocessor.core

import preprocessor.utils.extra.Balanced
import preprocessor.utils.extra.extractArguments
import preprocessor.base.globalVariables
import preprocessor.utils.extra.parse
import preprocessor.utils.conversion.fileToByteBuffer
import java.io.File
import java.util.ArrayList

/**
 * pre-processes a file **src**
 *
 * the result is saved in "$src${globalVariables.preprocessedExtension}$extension$extension"
 *
 * @param src the file to be modified
 * @param extension the extention of file specified in **src**
 * @param macroTmp a [Macro] array
 */
fun process(
    src: String,
    extension: String,
    macroTmp: ArrayList<Macro>
) {
    val destinationPreProcessed = File("$src${globalVariables.preprocessedExtension}.$extension")
    // macro needs to be a modifiable value thus cannot be declared as a paramater directly
    var macro = macroTmp
    var index = macro.size - 1
    if (macro[index].fileName != null) {
        index++
        println("reallocating to $index")
        macro = macro[0].realloc(macro, index + 1)
    }
    macro[index].fileName = src.substringAfterLast('/')
    println("registered macro definition for ${macro[index].fileName} at index $index")
    println("processing ${macro[index].fileName} -> ${destinationPreProcessed.name}")
    destinationPreProcessed.createNewFile()
    val lex = Lexer(fileToByteBuffer(File(src)), globalVariables.tokensNewLine)
    lex.lex()
    println("lex.currentLine is ${lex.currentLine}")
    while (lex.currentLine != null) {
        val out = parse(lex, macro)
        var input = lex.currentLine as String
        if (input[input.length - 1] == '\n') {
            input = input.dropLast(1)
        }
        println("\ninput = $input")
        println("output = $out\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        if (globalVariables.firstLine) {
            destinationPreProcessed.writeText(out + "\n")
            globalVariables.firstLine = false
        } else destinationPreProcessed.appendText(out + "\n")
        lex.lex()
    }
}

/**
 * adds each **line** to the given [MACRO][Macro] list
 *
 * assumes each **line** is a valid **#define** directive
 */
fun processDefine(line: String, MACRO: ArrayList<Macro>) {
    val index = MACRO.size - 1
    println("saving macro in to index $index")
    var macroIndex = MACRO[index].macros[0].size
    // to include the ability to redefine existing definitions, we must save to local variables first
    val fullMacro: String = line.trimStart().drop(1).trimStart()
    var type: String
    // determine token type
    // Line is longer than allowed by code style (> 120 columns)
    type = if (fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart() == fullMacro.substringAfter(' ')
            .trimStart()
            .substringBefore(' ')
            .trimStart().substringBefore('(')
    )
        Macro().Directives().Define().Types().Object
    else
        Macro().Directives().Define().Types().Function
    var token: String
    if (type == Macro().Directives().Define().Types().Object) {
        var empty = false
        // object
        token =
            fullMacro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
        if (token[token.length - 1] == '\n') {
            token = token.dropLast(1)
            empty = true
        }
        val i = macroExists(token, type, index, MACRO)
        if (globalVariables.currentMacroExists) {
            macroIndex = i
        } else {
            if (MACRO[index].macros[macroIndex].fullMacro != null) {
                MACRO[index].macros = MACRO[index].macros[0].realloc(
                    MACRO[index].macros,
                    MACRO[index].macros[0].size + 1
                )
            }
            macroIndex = MACRO[index].macros[0].size
        }
        MACRO[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        MACRO[index].macros[macroIndex].token = token
        MACRO[index].macros[macroIndex].type = type
        if (!empty) {
            MACRO[index].macros[macroIndex].replacementList =
                fullMacro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        } else MACRO[index].macros[macroIndex].replacementList = ""
    } else {
        // function
        token =
            fullMacro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macroExists(token, type, index, MACRO)
        if (globalVariables.currentMacroExists) {
            macroIndex = i
        } else {
            if (MACRO[index].macros[macroIndex].fullMacro != null) {
                MACRO[index].macros = MACRO[index].macros[0].realloc(
                    MACRO[index].macros,
                    MACRO[index].macros[0].size + 1
                )
            }
            macroIndex = MACRO[index].macros[0].size
        }
        MACRO[index].macros[macroIndex].fullMacro = line.trimStart().trimEnd()
        MACRO[index].macros[macroIndex].token = token
        MACRO[index].macros[macroIndex].type = type
        // obtain the function arguments
        val t = MACRO[index].macros[macroIndex].fullMacro?.substringAfter(' ')!!
        val b = Balanced()
        MACRO[index].macros[macroIndex].arguments =
            extractArguments(b.extractText(t).drop(1).dropLast(1))
        MACRO[index].macros[macroIndex].replacementList = t.substring(b.end[0] + 1).trimStart()
    }
    println("type       = ${MACRO[index].macros[macroIndex].type}")
    println("token      = ${MACRO[index].macros[macroIndex].token}")
    if (MACRO[index].macros[macroIndex].arguments != null)
        println("arguments  = ${MACRO[index].macros[macroIndex].arguments}")
    println("replacementList      = ${MACRO[index].macros[macroIndex].replacementList}")
    macroList(index, MACRO)
    // definition names do not expand
    // definition values do expand
}
