package preprocessor.utils.extra

import preprocessor.base.globalVariables
import preprocessor.core.*
import preprocessor.utils.core.abort
import preprocessor.utils.conversion.stringToByteBuffer
import java.util.ArrayList

/**
 * expands a line
 * @return the expanded line
 * @param lex this is used for multi-line processing
 * @param tokenSequence the current [Parser]
 * @param macro the current [Macro]
 * @param ARG the current argument list in an expanding function
 * @param blacklist the current list of macro's which should not be expanded
 */
fun expand(
    lex: Lexer,
    tokenSequence: Parser,
    macro: ArrayList<Macro>,
    ARG: ArrayList<String>? = null,
    blacklist: MutableList<String> = mutableListOf()
): String {
    println("expanding '${lex.currentLine}'")
    println("blacklist = $blacklist")
    println("ARG = $ARG")
    val expansion = StringBuffer()
    var iterations = 0
    val maxIterations = 100
    while (iterations <= maxIterations && tokenSequence.peek() != null) {
        val space = tokenSequence.IsSequenceOneOrMany(" ")
        val newline = tokenSequence.IsSequenceOnce("\n")
        val directive = tokenSequence.IsSequenceOnce("#")
        val define = tokenSequence.IsSequenceOnce(Macro().Directives().Define().value)
        val comment = tokenSequence.IsSequenceOnce("//")
        val blockCommentStart = tokenSequence.IsSequenceOnce("/*")
        val blockCommentEnd = tokenSequence.IsSequenceOnce("*/")
        val comma = tokenSequence.IsSequenceOnce(",")
        val emptyParenthesis = tokenSequence.IsSequenceOnce("()")
        val leftParenthesis = tokenSequence.IsSequenceOnce("(")
        val rightParenthesis = tokenSequence.IsSequenceOnce(")")
        val leftBrace = tokenSequence.IsSequenceOnce("[")
        val rightBrace = tokenSequence.IsSequenceOnce("]")
        val leftBracket = tokenSequence.IsSequenceOnce("{")
        val rightBracket = tokenSequence.IsSequenceOnce("}")
        if (comment.peek()) {
            println("clearing comment token '$tokenSequence'")
            tokenSequence.clear()
        } else if (blockCommentStart.peek()) {
            var depthBlockComment = 0
            blockCommentStart.pop() // pop the first /*
            depthBlockComment++
            var iterations = 0
            val maxIterations = 1000
            while (iterations <= maxIterations) {
                if (tokenSequence.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) abort(
                        "no more lines when expecting more lines, unterminated block comment"
                    )
                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) newline.pop()
                else if (blockCommentStart.peek()) {
                    depthBlockComment++
                    blockCommentStart.pop()
                } else if (blockCommentEnd.peek()) {
                    depthBlockComment--
                    blockCommentEnd.pop()
                    if (depthBlockComment == 0) {
                        break
                    }
                } else tokenSequence.pop()
                iterations++
            }
            if (iterations > maxIterations) abort("iterations expired")
        } else if (emptyParenthesis.peek()) {
            println("popping empty parenthesis token '$emptyParenthesis'")
            expansion.append(emptyParenthesis.toString())
            emptyParenthesis.pop()
        } else if (newline.peek()) {
            println("popping newline token '$newline'")
            newline.pop()
        } else if (
            (space.peek() && tokenSequence.lineInfo.column == 1)
            ||
            (tokenSequence.lineInfo.column == 1 && directive.peek())
        ) {
            /*
            5
Constraints
The only white-space characters that shall appear between preprocessing tokens within a prepro-
cessing directive (from just after the introducing # preprocessing token through just before the
terminating new-line character) are space and horizontal-tab (including spaces that have replaced
comments or possibly other white-space characters in translation phase 3).

             */
            if (space.peek()) {
                // case 1, space at start of file followed by define
                println("popping space token '$space'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                println("popping directive token '$directive'")
                directive.pop()
                if (space.peek()) {
                    // case 1, space at start of file followed by define
                    println("popping space token '$space'")
                    space.pop()
                }
                if (define.peek()) {
                    // case 2, define at start of line
                    println("popping ${Macro().Directives().Define().value} statement '$tokenSequence'")
                    processDefine("#$tokenSequence", macro)
                    tokenSequence.clear()
                }
            }
        } else {
            val index = macro.size - 1
            val ss = tokenSequence.peek()
            val name: String
            if (ss == null) abort("something is wrong")
            name = ss
            println("popping normal token '$name'")
            /*
            kotlin supports new line statements but functions MUST not contain
            a new line between the identifier and the left parenthesis
             */
            val isAlphaNumarical: Boolean = name.matches("[A-Za-z0-9]*".toRegex())
            var macroFunctionExists = false
            var macroFunctionIndex = 0
            var macroObjectExists = false
            var macroObjectIndex = 0
            if (isAlphaNumarical) {
                macroFunctionIndex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Function,
                    index,
                    macro
                )
                if (globalVariables.currentMacroExists) {
                    macroFunctionExists = true
                } else {
                    macroObjectIndex = macroExists(
                        name,
                        Macro().Directives().Define().Types().Object,
                        index,
                        macro
                    )
                    if (globalVariables.currentMacroExists) {
                        macroObjectExists = true
                    }
                }
            }
            if (macroObjectExists || macroFunctionExists) {
                var isFunction = false

                println("looking ahead")
                val tsa = tokenSequence.clone()
                val tsaSpace = tsa.IsSequenceOneOrMany(" ")
                val tsaLeftParenthesis = tsa.IsSequenceOnce("(")
                tsa.pop() // pop the function name
                if (tsaSpace.peek()) tsaSpace.pop() // pop any spaces in between
                if (tsaLeftParenthesis.peek()) isFunction = true

                var skip = false
                if (blacklist.contains(name)) skip = true
                if (isFunction) {
                    if (macroFunctionExists && !skip) {
                        println("'${tokenSequence.peek()}' is a function")
                        // we know that this is a function, proceed to attempt to extract all arguments
                        var depthParenthesis = 0
                        var depthBrace = 0
                        var depthBracket = 0
                        tokenSequence.pop() // pop the function name
                        if (space.peek()) space.pop() // pop any spaces in between
                        tokenSequence.pop() // pop the first (
                        depthParenthesis++
                        var iterations = 0
                        val maxIterations = 100
                        var argc = 0
                        val argv: MutableList<String> = mutableListOf()
                        argv.add("")
                        while (iterations <= maxIterations) {
                            if (newline.peek()) {
                                newline.pop()
                                if (tokenSequence.peek() == null) {
                                    println("ran out of tokens, grabbing more tokens from the next line")
                                    lex.lex()
                                    if (lex.currentLine == null) abort("no more lines when expecting more lines")
                                    tokenSequence.tokenList = parserPrep(lex.currentLine as String)
                                }
                            }
                            println("popping '${tokenSequence.peek()}'")
                            if (leftParenthesis.peek()) {
                                depthParenthesis++
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (leftBrace.peek()) {
                                depthBrace++
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (leftBracket.peek()) {
                                depthBracket++
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (rightParenthesis.peek()) {
                                depthParenthesis--
                                if (depthParenthesis == 0) {
                                    tokenSequence.pop()
                                    break
                                }
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (rightBrace.peek()) {
                                depthBrace--
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (rightBracket.peek()) {
                                depthBracket--
                                argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else if (comma.peek()) {
                                if (depthParenthesis == 1) {
                                    argc++
                                    argv.add("")
                                    comma.pop()
                                } else argv[argc] = argv[argc].plus(tokenSequence.pop())
                            } else argv[argc] = argv[argc].plus(tokenSequence.pop())
                            iterations++
                        }
                        if (iterations > maxIterations) println("iterations expired")
                        argc++
                        println("argc = $argc")
                        println("argv = $argv")
                        val macroTypeDependantIndex = macroFunctionIndex
                        // Line is longer than allowed by code style (> 120 columns)
                        println(
                            "${macro[index].macros[macroTypeDependantIndex].token} of type " +
                                    "${macro[index].macros[macroTypeDependantIndex].type} has value " +
                                    "${macro[index].macros[macroTypeDependantIndex].replacementList}"
                        )
                        println("macro  args = ${macro[index].macros[macroTypeDependantIndex].arguments}")
                        println("target args = $argv")
                        if (macro[index].macros[macroTypeDependantIndex].replacementList != null) {
                            // Line is longer than allowed by code style (> 120 columns)
                            val replacementList =
                                macro[index].macros[macroTypeDependantIndex].replacementList
                                        as String
                            val lex = Lexer(
                                stringToByteBuffer(replacementList),
                                globalVariables.tokensNewLine
                            )
                            lex.lex()
                            if (lex.currentLine != null) {
                                val parser =
                                    Parser(parserPrep(lex.currentLine as String))
                                /*
                                1
After the arguments for the invocation of a function-like macro have been identified, argument
substitution takes place. A parameter in the replacement list, unless preceded by a # or ## prepro-
cessing token or followed by a ## preprocessing token (see below), is replaced by the corresponding
argument after all macros contained therein have been expanded. Before being substituted, each
argument’s preprocessing tokens are completely macro replaced as if they formed the rest of the
preprocessing file; no other preprocessing tokens are available.
*/
                                var i = 0
                                println("expanding arguments: $argc arguments to expand")
                                while (i < argc) {
                                    // expand each argument
                                    val lex = Lexer(
                                        stringToByteBuffer(argv[i]),
                                        globalVariables.tokensNewLine
                                    )
                                    lex.lex()
                                    if (lex.currentLine != null) {
                                        val parser =
                                            Parser(parserPrep(lex.currentLine as String))
                                        val e = expand(lex, parser, macro)
                                        println("macro expansion '${argv[i]}' returned $e")
                                        argv[i] = e
                                    }
                                    i++
                                }
                                println("expanded arguments: $argc arguments expanded")
                                val associatedArguments = toMacro(
                                    macro[index].macros[macroTypeDependantIndex].arguments,
                                    argv as List<String>
                                )
                                println("blacklisting $name")
                                blacklist.add(name)
                                // Line is longer than allowed by code style (> 120 columns)
                                println(
                                    "macro[index].macros[macroTypeDependantIndex].arguments = " +
                                            "${macro[index].macros[macroTypeDependantIndex].arguments}"
                                )
                                val e = expand(
                                    lex,
                                    parser,
                                    associatedArguments,
                                    macro[index].macros[macroTypeDependantIndex].arguments,
                                    blacklist
                                )
                                println("current expansion is $expansion")
                                println("macro Function expansion $name returned $e")
                                val lex2 = Lexer(stringToByteBuffer(e), globalVariables.tokensNewLine)
                                lex2.lex()
                                if (lex2.currentLine != null) {
                                    val parser =
                                        Parser(parserPrep(lex2.currentLine as String))
                                    val e2 = expand(
                                        lex2,
                                        parser,
                                        macro,
                                        macro[index].macros[macroTypeDependantIndex].arguments,
                                        blacklist
                                    )
                                    println("current expansion is $expansion")
                                    println("macro Function expansion '$e' returned $e2")
                                    expansion.append(e2)
                                    println("current expansion is $expansion")
                                }
                            }
                        }
                    } else if (macroFunctionExists && skip) {
                        println("'$name' is a function but it is currently being expanded")
                        expansion.append(name)
                        tokenSequence.pop() // pop the macro name
                    } else {
                        println("'$name' is a function but no associated macro exists")
                        expansion.append(name)
                        tokenSequence.pop() // pop the macro name
                    }
                } else {
                    println("'$name' is an object")
                    tokenSequence.pop() // pop the macro name
                    if (macroObjectExists) {
                        if (skip) {
                            println("but it is currently being expanded")
                            expansion.append(name)
                        } else {
                            val macroTypeDependantIndex = macroObjectIndex
                            // Line is longer than allowed by code style (> 120 columns)
                            println(
                                "${macro[index].macros[macroTypeDependantIndex].token} of type " +
                                        "${macro[index].macros[macroTypeDependantIndex].type} has value " +
                                        "${macro[index].macros[macroTypeDependantIndex].replacementList}"
                            )
                            // Line is longer than allowed by code style (> 120 columns)
                            val replacementList =
                                macro[index].macros[macroTypeDependantIndex].replacementList
                                        as String
                            val lex = Lexer(
                                stringToByteBuffer(replacementList),
                                globalVariables.tokensNewLine
                            )
                            lex.lex()
                            if (lex.currentLine != null) {
                                if (ARG != null) {
                                    println("ARG = $ARG")
                                    if (!ARG.contains(name)) {
                                        println("blacklisting $name")
                                        blacklist.add(name)
                                    } else {
                                        println("$name is an argument")
                                    }
                                } else {
                                    println("warning: ARG is null")
                                    println("blacklisting $name")
                                    blacklist.add(name)
                                }
                                val parser =
                                    Parser(parserPrep(lex.currentLine as String))
                                val e = expand(lex, parser, macro, ARG, blacklist)
                                println("macro Object expansion $name returned $e")
                                expansion.append(e)
                            }
                        }
                    } else {
                        println("but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            } else expansion.append(tokenSequence.pop())
        }
        iterations++
    }
    if (iterations > maxIterations) println("iterations expired")
    println("expansion = $expansion")
    return expansion.toString()
}
