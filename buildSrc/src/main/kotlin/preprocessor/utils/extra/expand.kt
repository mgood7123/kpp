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
 * @param TS the current [Parser]
 * @param macro the current [Macro]
 * @param ARG the current argument list in an expanding function
 * @param blacklist the current list of macro's which should not be expanded
 */
fun expand(
    lex: Lexer,
    TS: Parser,
    macro: ArrayList<Macro>,
    ARG: ArrayList<String>? = null,
    blacklist: MutableList<String> = mutableListOf()
): String {
    println("expanding '${lex.currentLine}'")
    println("blacklist = $blacklist")
    println("ARG = $ARG")
    val expansion = StringBuffer()
    var itterations = 0
    val maxItterations = 100
    while (itterations <= maxItterations && TS.peek() != null) {
        val space = TS.IsSequenceOneOrMany(" ")
        val newline = TS.IsSequenceOnce("\n")
        val directive = TS.IsSequenceOnce("#")
        val define = TS.IsSequenceOnce(Macro().Directives().Define().value)
        val comment = TS.IsSequenceOnce("//")
        val blockcommentstart = TS.IsSequenceOnce("/*")
        val blockcommentend = TS.IsSequenceOnce("*/")
        val comma = TS.IsSequenceOnce(",")
        val emptyparens = TS.IsSequenceOnce("()")
        val leftparenthesis = TS.IsSequenceOnce("(")
        val rightparenthesis = TS.IsSequenceOnce(")")
        val leftbrace = TS.IsSequenceOnce("[")
        val rightbrace = TS.IsSequenceOnce("]")
        val leftbracket = TS.IsSequenceOnce("{")
        val rightbracket = TS.IsSequenceOnce("}")
        if (comment.peek()) {
            println("clearing comment token '$TS'")
            TS.clear()
        } else if (blockcommentstart.peek()) {
            var depthblockcomment = 0
            blockcommentstart.pop() // pop the first /*
            depthblockcomment++
            var itterations = 0
            val maxItterations = 1000
            while (itterations <= maxItterations) {
                if (TS.peek() == null) {
                    lex.lex()
                    // Line is longer than allowed by code style (> 120 columns)
                    if (lex.currentLine == null) abort(
                        "no more lines when expecting more lines, unterminated block commment"
                    )
                    TS.tokenList = parserPrep(lex.currentLine as String)
                }
                if (newline.peek()) newline.pop()
                else if (blockcommentstart.peek()) {
                    depthblockcomment++
                    blockcommentstart.pop()
                } else if (blockcommentend.peek()) {
                    depthblockcomment--
                    blockcommentend.pop()
                    if (depthblockcomment == 0) {
                        break
                    }
                } else TS.pop()
                itterations++
            }
            if (itterations > maxItterations) abort("itterations expired")
        } else if (emptyparens.peek()) {
            println("popping empty parenthesis token '$emptyparens'")
            expansion.append(emptyparens.toString())
            emptyparens.pop()
        } else if (newline.peek()) {
            println("popping newline token '$newline'")
            newline.pop()
        } else if ((space.peek() && TS.lineInfo.column == 1) || (TS.lineInfo.column == 1 && directive.peek())) {
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
                    println("popping ${Macro().Directives().Define().value} statement '$TS'")
                    processDefine("#$TS", macro)
                    TS.clear()
                }
            }
        } else {
            val index = macro.size - 1
            val ss = TS.peek()
            val name: String
            if (ss == null) abort("somthing is wrong")
            name = ss
            println("popping normal token '$name'")
            /*
            kotlin supports new line statements but functions MUST not contain
            a new line between the identifier and the left parenthesis
             */
            val isalnum: Boolean = name.matches("[A-Za-z0-9]*".toRegex())
            var macrofunctionexists = false
            var macrofunctionindex = 0
            var macroobjectexists = false
            var macroobjectindex = 0
            if (isalnum) {
                macrofunctionindex = macroExists(
                    name,
                    Macro().Directives().Define().Types().Function,
                    index,
                    macro
                )
                if (globalVariables.currentMacroExists) {
                    macrofunctionexists = true
                } else {
                    macroobjectindex = macroExists(
                        name,
                        Macro().Directives().Define().Types().Object,
                        index,
                        macro
                    )
                    if (globalVariables.currentMacroExists) {
                        macroobjectexists = true
                    }
                }
            }
            if (macroobjectexists || macrofunctionexists) {
                var isfunction = false

                println("looking ahead")
                val tSA = TS.clone()
                val tSAspace = tSA.IsSequenceOneOrMany(" ")
                val tSAleftparen = tSA.IsSequenceOnce("(")
                tSA.pop() // pop the function name
                if (tSAspace.peek()) tSAspace.pop() // pop any spaces in between
                if (tSAleftparen.peek()) isfunction = true

                var skip = false
                if (blacklist.contains(name)) skip = true
                if (isfunction) {
                    if (macrofunctionexists && !skip) {
                        println("'${TS.peek()}' is a function")
                        // we know that this is a function, proceed to attempt to extract all arguments
                        var depthparenthesis = 0
                        var depthbrace = 0
                        var depthbracket = 0
                        TS.pop() // pop the function name
                        if (space.peek()) space.pop() // pop any spaces in between
                        TS.pop() // pop the first (
                        depthparenthesis++
                        var itterations = 0
                        val maxItterations = 100
                        var argc = 0
                        val argv: MutableList<String> = mutableListOf()
                        argv.add("")
                        while (itterations <= maxItterations) {
                            if (newline.peek()) {
                                newline.pop()
                                val l = TS.peek()
                                if (l == null) {
                                    println("ran out of tokens, grabbing more tokens from the next line")
                                    lex.lex()
                                    if (lex.currentLine == null) abort("no more lines when expecting more lines")
                                    TS.tokenList = parserPrep(lex.currentLine as String)
                                }
                            }
                            println("popping '${TS.peek()}'")
                            if (leftparenthesis.peek()) {
                                depthparenthesis++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (leftbrace.peek()) {
                                depthbrace++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (leftbracket.peek()) {
                                depthbracket++
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightparenthesis.peek()) {
                                depthparenthesis--
                                if (depthparenthesis == 0) {
                                    TS.pop()
                                    break
                                }
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightbrace.peek()) {
                                depthbrace--
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (rightbracket.peek()) {
                                depthbracket--
                                argv[argc] = argv[argc].plus(TS.pop())
                            } else if (comma.peek()) {
                                if (depthparenthesis == 1) {
                                    argc++
                                    argv.add("")
                                    comma.pop()
                                } else argv[argc] = argv[argc].plus(TS.pop())
                            } else argv[argc] = argv[argc].plus(TS.pop())
                            itterations++
                        }
                        if (itterations > maxItterations) println("itterations expired")
                        argc++
                        println("argc = $argc")
                        println("argv = $argv")
                        val macroTypeDependantIndex = macrofunctionindex
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
                    } else if (macrofunctionexists && skip) {
                        println("'$name' is a function but it is currently being expanded")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    } else {
                        println("'$name' is a function but no associated macro exists")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    }
                } else {
                    println("'$name' is an object")
                    TS.pop() // pop the macro name
                    if (macroobjectexists) {
                        if (skip) {
                            println("but it is currently being expanded")
                            expansion.append(name)
                        } else {
                            val macroTypeDependantIndex = macroobjectindex
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
            } else expansion.append(TS.pop())
        }
        itterations++
    }
    if (itterations > maxItterations) println("itterations expired")
    println("expansion = $expansion")
    return expansion.toString()
}
