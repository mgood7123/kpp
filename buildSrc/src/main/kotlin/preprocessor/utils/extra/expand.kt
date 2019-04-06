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
 * @param TS the current [parser]
 * @param MACRO the current [Macro]
 * @param ARG the current argument list in an expanding function
 * @param blacklist the current list of macro's which should not be expanded
 */
fun expand(lex : lexer, TS : parser, MACRO : ArrayList<Macro>, ARG : ArrayList<String>? = null, blacklist : MutableList<String> = mutableListOf()) : String {
    println("expanding '${lex.current_line}'")
    println("blacklist = $blacklist")
    println("ARG = ${ARG}")
    val expansion = StringBuffer()
    var itterations = 0
    var maxItterations = 100
    while (itterations <= maxItterations && TS.peek() != null) {
        val space = TS.isSequenceOneOrMany(" ")
        val newline = TS.isSequenceOnce("\n")
        val directive = TS.isSequenceOnce("#")
        val define = TS.isSequenceOnce("define")
        val comment = TS.isSequenceOnce("//")
        val blockcommentstart = TS.isSequenceOnce("/*")
        val blockcommentend = TS.isSequenceOnce("*/")
        val comma = TS.isSequenceOnce(",")
        val emptyparens = TS.isSequenceOnce("()")
        val leftparenthesis = TS.isSequenceOnce("(")
        val rightparenthesis = TS.isSequenceOnce(")")
        val leftbrace = TS.isSequenceOnce("[")
        val rightbrace = TS.isSequenceOnce("]")
        val leftbracket = TS.isSequenceOnce("{")
        val rightbracket = TS.isSequenceOnce("}")
        if (comment.peek()) {
            println("clearing comment token '${TS.toString()}'")
            TS.clear()
        }
        else if(blockcommentstart.peek()) {
            var depthblockcomment = 0
            blockcommentstart.pop() // pop the first /*
            depthblockcomment++
            var itterations = 0
            var maxItterations = 1000
            while (itterations <= maxItterations) {
                if (TS.peek() == null) {
                    lex.lex()
                    if (lex.current_line == null) abort("no more lines when expecting more lines, unterminated block commment")
                    TS.tokenList = parserPrep(lex.current_line as String)
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
        }
        else if (emptyparens.peek()) {
            println("popping empty parenthesis token '${emptyparens.toString()}'")
            expansion.append(emptyparens.toString())
            emptyparens.pop()
        }
        else if (newline.peek()) {
            println("popping newline token '${newline.toString()}'")
            newline.pop()
        }
        else if ((space.peek() && TS.lineinfo.column == 1) || (TS.lineinfo.column == 1 && directive.peek())) {
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
                println("popping space token '${space.toString()}'")
                space.pop()
                expansion.append(" ")
            }
            if (directive.peek()) {
                println("popping directive token '${directive.toString()}'")
                directive.pop()
                if (space.peek()) {
                    // case 1, space at start of file followed by define
                    println("popping space token '${space.toString()}'")
                    space.pop()
                }
                if (define.peek()) {
                    // case 2, define at start of line
                    println("popping define statement '${TS.toString()}'")
                    preprocessor.core.processDefine("#" + TS.toString(), MACRO)
                    TS.clear()
                }
            }
        }
        else {
            val index = MACRO[0].size - 1
            val ss = TS.peek()
            val name : String
            if (ss == null) abort("somthing is wrong")
            name = ss as String
            println("popping normal token '$name'")
            /*
            kotlin supports new line statements but functions MUST not contain
            a new line between the identifier and the left parenthesis
             */
            var isalnum : Boolean = name.matches("[A-Za-z0-9]*".toRegex())
            var macrofunctionexists : Boolean = false
            var macrofunctionindex = 0
            var macroobjectexists : Boolean = false
            var macroobjectindex = 0
            var macrofunctionexistsAsArgument : Boolean = false
            var macroobjectexistsAsArgument : Boolean = false
            if (isalnum) {
                macrofunctionindex = macroExists(
                    name,
                    Macro().Directives().Define().Types().FUNCTION,
                    index,
                    MACRO
                )
                if (globalVariables.currentMacroExists) {
                    macrofunctionexists = true
                }
                else {
                    macroobjectindex = macroExists(
                        name,
                        Macro().Directives().Define().Types().OBJECT,
                        index,
                        MACRO
                    )
                    if (globalVariables.currentMacroExists) {
                        macroobjectexists = true
                    }
                }
            }
            if (macroobjectexists || macrofunctionexists) {
                var isfunction: Boolean = false

                println("looking ahead")
                val TSA = TS.clone()
                val TSAspace = TSA.isSequenceOneOrMany(" ")
                val TSAleftparen = TSA.isSequenceOnce("(")
                TSA.pop() // pop the function name
                if (TSAspace.peek()) TSAspace.pop() // pop any spaces in between
                if (TSAleftparen.peek()) isfunction = true

                var skip : Boolean = false
                if (blacklist.contains(name)) skip = true
                if (isfunction) {
                    if (macrofunctionexists && skip == false) {
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
                        var maxItterations = 100
                        var argc = 0
                        var argv: MutableList<String> = mutableListOf()
                        argv.add("")
                        while (itterations <= maxItterations) {
                            if (newline.peek()) {
                                newline.pop()
                                val l = TS.peek()
                                if (l == null) {
                                    println("ran out of tokens, grabbing more tokens from the next line")
                                    lex.lex()
                                    if (lex.current_line == null) abort("no more lines when expecting more lines")
                                    TS.tokenList = parserPrep(lex.current_line as String)
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
                        println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].replacementList}")
                        println("macro  args = ${MACRO[index].Macros[macroTypeDependantIndex].Arguments}")
                        println("target args = $argv")
                        if (MACRO[index].Macros[macroTypeDependantIndex].replacementList != null) {
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].replacementList as String),
                                globalVariables.tokensNewLine
                            )
                            lex.lex()
                            if (lex.current_line != null) {
                                val parser =
                                    parser(parserPrep(lex.current_line as String))
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
                                    val lex = lexer(
                                        stringToByteBuffer(argv[i]),
                                        globalVariables.tokensNewLine
                                    )
                                    lex.lex()
                                    if (lex.current_line != null) {
                                        val parser =
                                            parser(parserPrep(lex.current_line as String))
                                        val e = expand(lex, parser, MACRO)
                                        println("macro expansion '${argv[i]}' returned $e")
                                        argv[i] = e
                                    }
                                    i++
                                }
                                println("expanded arguments: $argc arguments expanded")
                                val associated_arguments = toMacro(
                                    MACRO[index].Macros[macroTypeDependantIndex].Arguments,
                                    argv as List<String>
                                )
                                println("blacklisting $name")
                                blacklist.add(name)
                                println("MACRO[index].Macros[macroTypeDependantIndex].Arguments = ${MACRO[index].Macros[macroTypeDependantIndex].Arguments}")
                                val e = expand(
                                    lex,
                                    parser,
                                    associated_arguments,
                                    MACRO[index].Macros[macroTypeDependantIndex].Arguments,
                                    blacklist
                                )
                                println("current expansion is $expansion")
                                println("macro FUNCTION expansion $name returned $e")
                                val lex2 = lexer(stringToByteBuffer(e), globalVariables.tokensNewLine)
                                lex2.lex()
                                if (lex2.current_line != null) {
                                    val parser =
                                        parser(parserPrep(lex2.current_line as String))
                                    val e2 = expand(
                                        lex2,
                                        parser,
                                        MACRO,
                                        MACRO[index].Macros[macroTypeDependantIndex].Arguments,
                                        blacklist
                                    )
                                    println("current expansion is $expansion")
                                    println("macro FUNCTION expansion '$e' returned $e2")
                                    expansion.append(e2)
                                    println("current expansion is $expansion")
                                }
                            }
                        }
                    }
                    else if (macrofunctionexists && skip == true) {
                        println("'$name' is a function but it is currently being expanded")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    }
                    else {
                        println("'$name' is a function but no associated macro exists")
                        expansion.append(name)
                        TS.pop() // pop the macro name
                    }
                }
                else {
                    println("'$name' is an object")
                    TS.pop() // pop the macro name
                    if (macroobjectexists) {
                        if (skip == true) {
                            println("but it is currently being expanded")
                            expansion.append(name)
                        }
                        else {
                            val macroTypeDependantIndex = macroobjectindex
                            println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].replacementList}")
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].replacementList as String),
                                globalVariables.tokensNewLine
                            )
                            lex.lex()
                            if (lex.current_line != null) {
                                if (ARG != null) {
                                    println("ARG = ${ARG}")
                                    if (!ARG.contains(name)) {
                                        println("blacklisting $name")
                                        blacklist.add(name)
                                    } else {
                                        println("$name is an argument")
                                    }
                                }
                                else {
                                    println("warning: ARG is null")
                                    println("blacklisting $name")
                                    blacklist.add(name)
                                }
                                val parser =
                                    parser(parserPrep(lex.current_line as String))
                                val e = expand(lex, parser, MACRO, ARG, blacklist)
                                println("macro OBJECT expansion $name returned $e")
                                expansion.append(e)
                            }
                        }
                    }
                    else {
                        println("but does not exist as a macro")
                        expansion.append(name)
                    }
                }
            }
            else expansion.append(TS.pop())
        }
        itterations++
    }
    if (itterations > maxItterations) println("itterations expired")
    println("expansion = $expansion")
    return expansion.toString()
}
