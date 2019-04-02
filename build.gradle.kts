import java.io.*
import java.nio.file.Files.*
import java.util.*

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//import com.github.h0tk3y.betterParse.combinators.*
//import com.github.h0tk3y.betterParse.grammar.Grammar
//import com.github.h0tk3y.betterParse.grammar.parseToEnd
//import com.github.h0tk3y.betterParse.grammar.parser
//import com.github.h0tk3y.betterParse.parser.Parser

class invokers() {
    inner class information {
        var name : String = ""
        var paramater : String = ""
    }

    val info : MutableList<information> = mutableListOf()
    val function : (String) -> Unit = { v : String -> println("invoked with paramater 'v' with value '$v'") }

    fun add(name : String, paramater : String) {
        this.info.add(information())
        this.info[this.info.size-1].name = name
        this.info[this.info.size-1].paramater = paramater
    }
    fun print() {
        this.info.forEach({println("it.name = ${it.name}")})
        this.info.forEach({println("it.paramater = ${it.paramater}")})
    }

    fun call(name : String) {
        for (information in info) {
            if (information.name.equals(name)) {
                println("name exists: $name")
                function(information.paramater)
            }
        }
    }
}

fun t(){
    val functionList = invokers()
    functionList.add("h", "HAI")
    functionList.print()
    functionList.call("h")
}

tasks.register("KOTLIN_PRE_PROCESSOR") {
    println("starting KOTLIN_PRE_PROCESSOR")
    t()
//    val tokens = tokenizeKotlinCode("val x = foo() + 10;")
//    val parseTree = parseKotlinCode(tokens)
    // or just `val parseTree = parseKotlinCode("val x = foo() + 10;")`

//    println(parseTree)
//    abort()
//    Macro().test()
    find_source_files(projectDir.toString(), "kt")
    println("KOTLIN_PRE_PROCESSOR finished")
    if (abort_on_complete) abort()
}


val abort_on_complete = true
val tokensSpace = " \t"
val tokensNewLine = "\n"
val tokensExtra = "/*#().,->{}[]"
val tokens = tokensSpace + tokensNewLine + tokensExtra
val mtokens = " ().,->{}[]"

fun abort(e : String = "Aborted") {
    throw GradleException(e)
}

val kpp_DIR : String = rootDir.toString() + "/kpp"

var kpp_MACRO_LIST = arrayListOf(Macro())

class Macro() {
    inner class Directives() {
        inner class Definition() {
            val value : String = "define"
            inner class Types() {
                val OBJECT : String = "object"
                val FUNCTION : String = "function"
            }
        }
    }
    inner class Macro_internal() {
        var size : Int = 0
        var FullMacro: String? = null
        var Token: String? = null
        var Type: String? = null
        var Arguments : ArrayList<String>? = null
        var Value: String? = null
        fun realloc(m : ArrayList<Macro_internal>, sz : Int) : ArrayList<Macro_internal> {
            m.add(Macro_internal())
            m[0].size = sz
            return m
        }
    }
    var size : Int = 0
    var FileName: String? = null
    var Macros: ArrayList<Macro_internal>
    init {
        this.size = 1
        Macros = arrayListOf(Macro_internal())
    }
    fun realloc(m : ArrayList<Macro>, sz : Int) : ArrayList<Macro> {
        m.add(Macro())
        m[0].size = sz
        return m
    }

    fun test() {
        var c = arrayListOf(Macro())
        c[0].FileName = "test"
        c[0].Macros[0].FullMacro = "A B"
        c[0].Macros[0].Token = "A"
        c[0].Macros[0].Value = "B00"
        println(c[0].Macros[0].Value)
        c = c[0].realloc(c, c[0].size+1)
        c[1].FileName = "test"
        c[1].Macros[0].FullMacro = "A B"
        c[1].Macros[0].Token = "A"
        c[1].Macros[0].Value = "B10"
        println(c[1].Macros[0].Value)
        c[1].Macros = c[1].Macros[0].realloc(c[1].Macros, c[1].Macros[0].size+1)
        c[1].FileName = "test"
        c[1].Macros[1].FullMacro = "A B"
        c[1].Macros[1].Token = "A"
        c[1].Macros[1].Value = "B11"
        println(c[1].Macros[1].Value)
        c[1].Macros = c[1].Macros[0].realloc(c[1].Macros, c[1].Macros[0].size+1)
        c[1].FileName = "test"
        c[1].Macros[2].FullMacro = "A B"
        c[1].Macros[2].Token = "A"
        c[1].Macros[2].Value = "B12"
        println(c[1].Macros[2].Value)
    }
}

/*

#include  <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <assert.h>

struct Macros {
    long Size;
    char * FullMacro;
    char * Name;
    char * Value;
};

struct Macro {
    long Size;
    char * FileName;
    struct Macros * Macros; // cant remember is a internal struct is accessible without a declaration
} * Macro;

#define mallocstruct(c, s) {\
    c = malloc(sizeof(struct s)); \
    c->Size = sizeof(struct s); \
}

#define reallocstruct(c, s) {\
    c = realloc(c, c->Size+sizeof(struct s)); \
    c->Size = c->Size+sizeof(struct s); \
}

int main(void)
{
    struct Macro * c = NULL;
    mallocstruct(c, Macro);
    mallocstruct(c->Macros, Macros);

    c[0].Macros->Value = "B00";

    reallocstruct(c, Macro);
    mallocstruct(c[1].Macros, Macros);
    c[1].Macros->Value = "B10";

    reallocstruct(c[1].Macros, Macros);
    c[1].Macros[1].Value = "B11";

    reallocstruct(c[1].Macros, Macros);
    c[1].Macros[2].Value = "B12";

    puts(c[0].Macros->Value);
    puts(c[1].Macros->Value);
    puts(c[1].Macros[1].Value);
    puts(c[1].Macros[2].Value);
}
*/

fun find_source_files(Dir : String, ext : String) {
    var srcDir = file(Dir)
    srcDir?.listFiles().forEach {
        if (it.isDirectory) find_source_files(it.toString(), ext)
        if (it.isFile) {
            if (it.extension.equals(ext)) {
                initiate(it)
            }
        }
    }
}

fun cp(src : String, dest : String, overwrite : Boolean = false) : Boolean {
    try {
        file(src).copyTo(file(dest), overwrite)
//        println("$src -> $dest")
        return true
    } catch (e: IOException) {
        println("failed to copy file $src")
        return false
    }
    return false
}

var current_file_contains_preprocesser : Boolean = false
var current_file_is_cashed = false
var cached_file_contains_preprocesser = false
var first_line : Boolean = true

fun initiate(src : File) {
    val source = src.toString()
    val dest = kpp_DIR + src.toString().removePrefix(projectDir.toString())
    current_file_contains_preprocesser = false
    cached_file_contains_preprocesser = false
    current_file_is_cashed = false
    first_line = true

    if (file(dest).exists()) {
        test_cache_file(file(dest))
        if (!cached_file_contains_preprocesser) {
            // if dest no longer contains preprocessing info, copy it back to src, overwriting the current src, and use src
            if (cp(dest, source, true)) { // copy dest to source
                println("${dest.substringAfterLast('/')} (in kpp/src) moved back to original source")
                file(dest).delete()
                if (file(dest + ".preprocessed.kt").exists()) file(dest + ".preprocessed.kt").delete()
            }
            else throw GradleException("failed to move ${dest} (in kpp/src) to $source")
        }
        else {
            // if dest already exist use dest
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(src, dest, kpp_MACRO_LIST)
            if (cp(dest + ".preprocessed.kt", source, true)) {
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else throw GradleException("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
    else {
        test_file(src)
        if (!current_file_contains_preprocesser) {
            // if src does not contain preprocessing info, use src
        } else {
            // if src contains preprocessing info, copy it to dest and use dest
            if (cp(source, dest, true)) { // copy if dest does not exist
                println("original ${dest.substringAfterLast('/')} added to kpp/src")
            }
            else throw GradleException("failed to copy $source to $dest")
            println("using ${dest.substringAfterLast('/')} in kpp/src")
            process(src, dest, kpp_MACRO_LIST)
            if (cp(dest + ".preprocessed.kt", source, true)) { // copy back to source
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else throw GradleException("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
}

fun test_file(src : File) {
    val lines: List<String> = readAllLines(src.toPath())
    lines.forEach {
            line -> check_if_preprocessor_is_needed(line)
    }
}

fun check_if_preprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) current_file_contains_preprocesser = true
}

fun test_cache_file(src : File) {
    val lines: List<String> = readAllLines(src.toPath())
    lines.forEach {
            line -> check_if_cachepreprocessor_is_needed(line)
    }
}

fun check_if_cachepreprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) cached_file_contains_preprocesser = true
}

var currentmacroexists = false

/*
Inner classes
        A class may be marked as inner to be able to access members of outer class. Inner classes carry
a reference to an object of an outer class:
class Outer {
    private val bar: Int = 1
    inner class Inner {
        fun foo() = bar
    }
}
val demo = Outer().Inner().foo() // == 1
See Quali ed this expressions to learn about disambiguation of this in inner classes.
*/

class balanced {
    class balanceList() {
        var l : MutableList<Char> = mutableListOf()
        var r : MutableList<Char> = mutableListOf()
        fun addPair( l : Char, r : Char) {
            this.l.add(l)
            this.r.add(r)
        }
    }
    var start : MutableList<Int> = mutableListOf()
    var end : MutableList<Int> = mutableListOf()
    var index = 0
    var countLeft = 0  // number of left brackets so far unmatched
    var splitterCount = 0
    var splitterLocation : MutableList<Int> = mutableListOf()
    var lastRegisteredLBalancer = ' '
    var lastRegisteredRBalancer = ' '
    var lastCheckString = ""
    fun isBalanced(s: String, balancerLeft : Char, balancerRight : Char): Boolean {
        lastCheckString = s
        lastRegisteredLBalancer = balancerLeft
        lastRegisteredRBalancer = balancerRight
        start
        end
        if (s.isEmpty()) return true
        for (c in s) {
            if (c == lastRegisteredLBalancer) {
                countLeft++
                if (countLeft == 1) start.add(index)
            }
            else if (c == lastRegisteredRBalancer) {
                if (countLeft == 1) end.add(index+1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    fun is_balancerR(c : Char, balance : balanceList) : Boolean {
        balance.r.forEach {
            if (c == it) return true
        }
        return false
    }
    fun is_balancerL(c : Char, balance : balanceList) : Boolean {
        balance.l.forEach {
            if (c == it) return true
        }
        return false
    }

    fun containsL(c : String, balance : balanceList) : Boolean {
        balance.l.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    fun containsR(c : String, balance : balanceList) : Boolean {
        balance.r.forEach {
            if (c.contains(it)) return true
        }
        return false
    }

    fun isBalancedSplit(s: String, balancer : balanceList, Splitter : Char): Boolean {
        lastCheckString = s
        lastRegisteredLBalancer = balancer.l[balancer.l.lastIndex]!!
        lastRegisteredRBalancer = balancer.r[balancer.r.lastIndex]!!
        if (s.isEmpty()) return true
        for (c in s) {
            if (countLeft == 0) if (c == Splitter) {
                splitterCount++
                splitterLocation.add(index)
            }
            if (is_balancerL(c, balancer)) {
                countLeft++
                if (countLeft == 1) start.add(index)
            }
            else if (is_balancerR(c, balancer)) {
                if (countLeft == 1) end.add(index+1)
                if (countLeft > 0) countLeft--
                else return false
            }
            index++
        }
        return countLeft == 0
    }

    fun extract_text(text : String) : String {
        if (isBalanced(text, '(', ')')) {
            println("text : " + text.substring(start[0], end[0]))
            return text.substring(start[0], end[0])
        }
        return text
    }

    fun info() {
        println("last check string  = $lastCheckString")
        println("left balancer      = $lastRegisteredLBalancer")
        println("right balancer     = $lastRegisteredRBalancer")
        println("start index        = $start")
        println("end index          = $end")
        println("curent index       = $index")
        println("unmatched brackets = $countLeft")
        println("splitter count     = $splitterCount")
        println("splitter location  = $splitterLocation")
    }
}

fun preprocess(src : File, dest : File, line: String, MACRO : ArrayList<Macro>) {
    val index = MACRO[0].size - 1
    if (line.trimStart().startsWith('#')) {
        val directive = line.trimStart().drop(1).trimStart().substringBefore(' ')
        println("${src.name}: preprocessor directive: $directive")
        println("${src.name}: preprocessor line: ${line.trimStart()}")
        if (directive.equals(Macro().Directives().Definition().value)) {
            var macro_index = MACRO[index].Macros[0].size
            // to include the ability to redefine existing definitions, we must save to local variables first
            val full_macro : String = line.trimStart().drop(1).trimStart()
            var type : String? = null
            // determine Token type
            if (full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart().equals(full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()?.substringBefore('('))!!)
                type = Macro().Directives().Definition().Types().OBJECT
            else
                type = Macro().Directives().Definition().Types().FUNCTION
            var token : String? = null
            if (type.equals(Macro().Directives().Definition().Types().OBJECT)) {
                // object
                token =
                    full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
                val i = macro_exists(token, type, index, MACRO)
                if (currentmacroexists) {
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
                MACRO[index].Macros[macro_index].Value =
                    full_macro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
            } else {
                // function
                token =
                    full_macro.substringAfter(' ').substringBefore('(').trimStart()
                val i = macro_exists(token, type, index, MACRO)
                if (currentmacroexists) {
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
                val b = balanced()
                MACRO[index].Macros[macro_index].Arguments = extract_arguments(b.extract_text(t).drop(1).dropLast(1), MACRO)
                MACRO[index].Macros[macro_index].Value = t.substring(b.end[0]+1).trimStart()
            }
            println("Type       = ${MACRO[index].Macros[macro_index].Type}")
            println("Token      = ${MACRO[index].Macros[macro_index].Token}")
            if (MACRO[index].Macros[macro_index].Arguments != null)
                println("Arguments  = ${MACRO[index].Macros[macro_index].Arguments}")
            println("Value      = ${MACRO[index].Macros[macro_index].Value}")
            macro_list(index, MACRO)
            // definition names do not expand
            // definition values do expand
        }
    }
    else {
        if (first_line) {
            dest.writeText(MacroExpansionEngine(line, index, MACRO) + "\n")
            first_line = false
        }
        else dest.appendText(MacroExpansionEngine(line, index, MACRO) + "\n")
    }
}

fun processDefine(line: String, MACRO : ArrayList<Macro>) {
    val index = MACRO[0].size - 1
    var macro_index = MACRO[index].Macros[0].size
    // to include the ability to redefine existing definitions, we must save to local variables first
    val full_macro : String = line.trimStart().drop(1).trimStart()
    var type : String = ""
    // determine Token type
    if (full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart().equals(full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()?.substringBefore('('))!!)
        type = Macro().Directives().Definition().Types().OBJECT
    else
        type = Macro().Directives().Definition().Types().FUNCTION
    var token : String = ""
    if (type.equals(Macro().Directives().Definition().Types().OBJECT)) {
        var empty = false
        // object
        token =
            full_macro.substringAfter(' ').trimStart().substringBefore(' ').trimStart()
        if (token[token.length-1] == '\n') {
            token = token.dropLast(1)
            empty = true
        }
        val i = macro_exists(token, type, index, MACRO)
        if (currentmacroexists) {
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
            MACRO[index].Macros[macro_index].Value =
                full_macro.substringAfter(' ').trimStart().substringAfter(' ').trimStart().trimStart()
        }
        else MACRO[index].Macros[macro_index].Value = ""
    } else {
        // function
        token =
            full_macro.substringAfter(' ').substringBefore('(').trimStart()
        val i = macro_exists(token, type, index, MACRO)
        if (currentmacroexists) {
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
        val b = balanced()
        MACRO[index].Macros[macro_index].Arguments = extract_arguments(b.extract_text(t).drop(1).dropLast(1), MACRO)
        MACRO[index].Macros[macro_index].Value = t.substring(b.end[0]+1).trimStart()
    }
    println("Type       = ${MACRO[index].Macros[macro_index].Type}")
    println("Token      = ${MACRO[index].Macros[macro_index].Token}")
    if (MACRO[index].Macros[macro_index].Arguments != null)
        println("Arguments  = ${MACRO[index].Macros[macro_index].Arguments}")
    println("Value      = ${MACRO[index].Macros[macro_index].Value}")
    macro_list(index, MACRO)
    // definition names do not expand
    // definition values do expand
}

fun stringToDeque(str : String) : ArrayDeque<String> {
    var deq = ArrayDeque<String>()
    var i = 0
    while (i < str.length) deq.addLast(str[i++].toChar().toString())
    return deq
}

fun dequeToString(d : ArrayDeque<String>) : String {
    val result = StringBuffer()
    val dq = d.iterator()
    while(dq.hasNext()) {
        result.append(dq.next())
    }
    return result.toString()
}

fun fileToByteBuffer(f : File) : ByteBuffer {
    val file = RandomAccessFile(f, "r")
    val fileChannel = file.getChannel()

    var i = 0
    var buffer = ByteBuffer.allocate(fileChannel.size().toInt())
    fileChannel.read(buffer)
    buffer.flip()
    return buffer
}

fun stringToByteBuffer(f : String) : ByteBuffer {
    return ByteBuffer.wrap(f.toByteArray())
}

class lexer(stm : ByteBuffer, delimiter : String) {
    val f = stm
    val delimiters = delimiter
    val d = stringToDeque(delimiters)
    var current_line = ""

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

/*
    fun clone() : lexer {
        return lexerClone(this)
    }

    fun clone(newdelimiters : String) : lexer {
        return lexerClone(this, newdelimiters)
    }
*/

    fun lex() : String? {
        /*
        in order to make a lexer, we traditionally process the input file
        character by character, appending each to a buffer, then returning
        that buffer when a specific delimiter is found
        */
        var isdelim = false
        if (f.remaining() == 0) return null
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
        return current_line
    }
}

fun parserPrep(line: String) : ArrayDeque<String> {
    val st = StringTokenizer(line, tokens, true)
    var dq = ArrayDeque<String>()
    while (st.hasMoreTokens()) {
        dq.addLast(st.nextToken())
    }
    return dq
}

class parser(tokens : ArrayDeque<String>) {
    var tokenList = tokens

    inner class internallineinfo {
        inner class default {
            var column = 1
            var line = 1
        }
        var column = default().column
        var line = default().line
    }

    var lineinfo = internallineinfo()

    fun clone() : parser {
        val result = StringBuffer()
        tokenList.forEach {
            result.append(it!!)
        }
        return parser(parserPrep(result.toString()))
    }

    fun peek() : String? {
        return tokenList.peek()
    }

    fun pop() : String? {
        val peekValue = tokenList.peek()
        if (peekValue == null) {
            if (tokenList.pop() != null) abort("token list is corrupted")
            return null
        }
        val returnValue = tokenList.pop()
        if (returnValue == null) abort("token list is corrupted")
        var i = 0
        while (i < returnValue.length) {
            if (returnValue[i] == '\n') {
                lineinfo.column = lineinfo.default().column
                lineinfo.line++
            }
            else lineinfo.column++
            i++
        }
        return returnValue
    }

    fun clear() {
        tokenList.clear()
    }

    override fun toString() : String {
        return dequeToString(tokenList)
    }

    fun toStringAsArray() : String {
        return tokenList.toString()
    }

    inner class isSequenceZeroOrMany(str : String) {
        val sg = str
        val seq = isSequenceOneOrMany(sg)

        override fun toString() : String {
            return this@parser.isSequenceOneOrMany(sg).toString()
        }

        fun peek() : Boolean {
            return true
        }

        fun pop() : Boolean {
            while(seq.peek()) seq.pop()
            return true
        }
    }

    inner class isSequenceOneOrMany(str : String) {
        val sg = str

        override fun toString() : String {
            val o = clone().isSequenceOnce(sg)
            val result = StringBuffer()
            while(o.peek()) {
                result.append(o.toString())
                o.pop()
            }
            return result.toString()
        }

        fun peek() : Boolean {
            val o = clone().isSequenceOnce(sg)
            var matches = 0
            while(o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }

        fun pop() : Boolean {
            val o = isSequenceOnce(sg)
            var matches = 0
            while(o.peek()) {
                matches++
                o.pop()
            }
            if (matches != 0) return true
            return false
        }
    }

    inner class isSequenceOnce(str : String) {
        val sg = str

        override fun toString() : String {
            var tmp = tokenList.clone()
            var s = parserPrep(sg)
            if (s.peek() == null || tmp.peek() == null) return ""
            val result = StringBuffer()
            while(tmp.peek() != null && s.peek() != null) {
                val x = tmp.peek()
                if (tmp.pop().equals(s.pop())) result.append(x)
            }
            return result.toString()
        }

        fun peek() : Boolean {
            var tmp = tokenList.clone()
            var s = parserPrep(sg)
            if (s.peek() == null || tmp.peek() == null) return false
            val expected = s.size
            var matches = 0
            while(tmp.peek() != null && s.peek() != null) if (tmp.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }

        fun pop() : Boolean {
            var s = parserPrep(sg)
            if (s.peek() == null) return false
            val expected = s.size
            var matches = 0
            while(this@parser.peek() != null && s.peek() != null) if (this@parser.pop().equals(s.pop())) matches++
            if (matches == expected) return true
            return false
        }
    }
}

fun process(orig : File, src : String, MACROM : ArrayList<Macro>) {
    val DESTPRE: File = file(src + ".preprocessed.kt")
    var MACRO = MACROM
    if (MACRO[0].FileName != null) {
        MACRO = MACRO[0].realloc(MACRO, MACRO[0].size+1)
    }
    MACRO[0].FileName = orig.name
    println("registered macro definition for ${MACRO[0].FileName} at index ${MACRO[0].size}")
    println("processing ${MACRO[0].FileName} -> ${DESTPRE.name}")
    DESTPRE.createNewFile()
//    println("cloning")
//    val la = lexer(lex.f.duplicate(), lex.delimiters)
//    println("clone made")
    val lex = lexer(fileToByteBuffer(File(src)), tokensNewLine)
    var line = lex.lex()
    while (line != null) {
        val out = parse(lex, line, MACRO)
        var input = line
        if (input[input.length-1] == '\n') {
            input = input.dropLast(1)
        }
        println("\ninput = $input")
        println("output = $out\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
        if (first_line) {
            DESTPRE.writeText(out + "\n")
            first_line = false
        }
        else DESTPRE.appendText(out + "\n")
        line = lex.lex()
    }
}

/*
fun containsDirectives(line : String) : Boolean {
    if (line.trimStart().startsWith('#')) return true
    return false
}

fun processDirectives(line : String, MACRO : ArrayList<Macro>) {
    if (line.trimStart().startsWith('#')) {
        val directive = line.trimStart().drop(1).trimStart().substringBefore(' ')
        if (directive.equals(Macro().Directives().Definition().value)) processDefine(line, MACRO)
    }
}
*/

fun parse(lex : lexer, line: String, MACRO : ArrayList<Macro>) : String {
    return expand(lex, parser(parserPrep(line)), MACRO)
}

fun expand(lex : lexer, TS : parser, MACRO : ArrayList<Macro>, blacklist : MutableList<String> = mutableListOf()) : String {
    println("expanding '${lex.current_line}'")
    println("blacklist = $blacklist")
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
            var maxItterations = 100
            while (itterations <= maxItterations) {
                while (newline.peek()) {
                    // if next line grabbed happens to be a new line, pop it, and grab the next line
                    newline.pop()
                    val l = TS.peek()
                    if (l == null) {
                        println("ran out of tokens, grabbing more tokens from the next line")
                        val line = lex.lex()
                        if (line == null) abort("no more lines when expecting more lines")
                        TS.tokenList = parserPrep(line as String)
                    }
                }
                if (blockcommentstart.peek()) {
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
            if (itterations > maxItterations) println("itterations expired")
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
                    processDefine("#" + TS.toString(), MACRO)
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
            if (isalnum) {
                macrofunctionindex = macro_exists(name, Macro().Directives().Definition().Types().FUNCTION, index, MACRO)
                if (currentmacroexists) {
                    macrofunctionexists = true
                }
                else {
                    macroobjectindex = macro_exists(name, Macro().Directives().Definition().Types().OBJECT, index, MACRO)
                    if (currentmacroexists) {
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
                                    val line = lex.lex()
                                    if (line == null) abort("no more lines when expecting more lines")
                                    TS.tokenList = parserPrep(line as String)
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
                        println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].Value}")
                        println("macro  args = ${MACRO[index].Macros[macroTypeDependantIndex].Arguments}")
                        println("target args = $argv")
                        if (MACRO[index].Macros[macroTypeDependantIndex].Value != null) {
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].Value as String),
                                tokensNewLine
                            )
                            val line = lex.lex()
                            if (line != null) {
                                val parser = parser(parserPrep(line as String))
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
//                                println("expanding arguments: $argc arguments to expand")
//                                while (i < argc) {
//                                    // expand each argument
//                                    val lex = lexer(stringToByteBuffer(argv[i]), tokensNewLine)
//                                    val line = lex.lex()
//                                    if (line != null) {
//                                        val parser = parser(parserPrep(line as String))
//                                        val e = expand(lex, parser, MACRO)
//                                        println("macro expansion '${argv[i]}' returned $e")
//                                        argv[i] = e
//                                    }
//                                    i++
//                                }
//                                println("expanded arguments: $argc arguments expanded")
                                val associated_arguments = toMacro(
                                    MACRO[index].Macros[macroTypeDependantIndex].Arguments,
                                    argv as List<String>
                                )
                                println("blacklisting $name")
                                blacklist.add(name)
                                val e = expand(lex, parser, associated_arguments, blacklist)
                                println("current expansion is $expansion")
                                println("macro FUNCTION expansion $name returned $e")
                                val lex2 = lexer(stringToByteBuffer(e), tokensNewLine)
                                val line2 = lex2.lex()
                                if (line2 != null) {
                                    val parser = parser(parserPrep(line2 as String))
                                    val e2 = expand(lex2, parser, MACRO, blacklist)
                                    println("current expansion is $expansion")
                                    println("macro FUNCTION expansion '$e' returned $e2")
                                    expansion.append(e2)
                                    println("current expansion is $expansion")
                                }
//                                expansion.append(e)
//                                val e = expand(lex, parser, associated_arguments, blacklist)
//                                println("macro expansion $name returned $e")
//                                expand(lex, parser, associated_arguments)
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
                            println("${MACRO[index].Macros[macroTypeDependantIndex].Token} of type ${MACRO[index].Macros[macroTypeDependantIndex].Type} has value ${MACRO[index].Macros[macroTypeDependantIndex].Value}")
                            val lex = lexer(
                                stringToByteBuffer(MACRO[index].Macros[macroTypeDependantIndex].Value as String),
                                tokensNewLine
                            )
                            val line = lex.lex()
                            if (line != null) {
                                println("blacklisting $name")
                                blacklist.add(name)
                                val parser = parser(parserPrep(line as String))
                                val e = expand(lex, parser, MACRO, blacklist)
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

fun extract_arguments(arg : String, MACRO : ArrayList<Macro>, expand_arguments : Boolean = false)  : ArrayList<String>? {
    fun filterSplit(arg : String, ex : balanced, b : balanced.balanceList) : ArrayList<String> {
        var Arguments : ArrayList<String> = arrayListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    Arguments.add(arg)
                    println(Arguments[0])
                } else {
                    var s : String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    if (expand_arguments) s = MacroExpansionEngine(s, 0, MACRO)
                    Arguments.add(s)
                    println(Arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i]+1, ex.splitterLocation[i+1]).trimStart()
                        if (expand_arguments) s = MacroExpansionEngine(s, 0, MACRO)
                        Arguments.add(s)
                        println(Arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    if (expand_arguments) s = MacroExpansionEngine(s, 0, MACRO)
                    Arguments.add(s)
                    println(Arguments[i])
                }
            }
            else {
                ex.info()
                abort("unbalanced code")
            }
        }
        else if (ex.containsR(arg, b)) {
            // unbalanced
            abort("unbalanced code")
        }
        else {
            var a : MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                var s : String = a.get(i).trimStart().trimEnd()
                if (expand_arguments) s = MacroExpansionEngine(s, 0, MACRO)
                Arguments.add(s)
                i++
            }
        }
        println("Arguments List = $Arguments")
        return Arguments as ArrayList
    }
    println("extracting arguments for $arg")
    // first, determine the positions of all tokens
    var balance = balanced.balanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = balanced()
    return filterSplit(arg, ex, balance)
}

fun MacroExpansionEngine(str : String, index : Int, MACRO : ArrayList<Macro>) : String{
    if (MACRO[index].Macros[0].FullMacro == null) return str
    println("expanding line '$str'")
    var newstr = ""
    val tokens = mtokens
    val token_list = tokens.toList()
    println("tokens to ignore : $token_list")
    val st = StringTokenizer(str, tokens, true)
    val st_list = st.toList()
    println("tokenization : $st_list")
    var i = 0
    while (i <= st_list.lastIndex) {
        var ii = 0
        var skip = false
        var replacement = st_list[i].toString()
        while (ii <= token_list.lastIndex) {
            // check to see if st_list[i] equals any of the tokens in token_list
            if (st_list[i].toString().equals(token_list[ii].toString())) {
                skip = true
                break
            }
            ii++
        }
        if (!skip) {
            var skip_lower = false
            MACRO[index].Macros.forEach {
                // this will fail if the macro is defined as empty
                if (it.Value == null) skip_lower = true
                // if token matches any macros we stop searching through the macro list
                if (!skip_lower) {
                    if (it.Token?.equals(st_list[i].toString()) == true) {
                        val original_index = i
                        println("${it.Token} matches ${st_list[i].toString()}")
                        // once a token is matched, we match its type to ensure we are expanding the correct token
                        println("token list     = ${st_list[i].toString()}")
                        // the tricky part here is that all punctuation is included in the token list, this is required to correctly detect functions and possible syntax errors
                        var isvalid = false
                        var iiii = i+1
                        var dobreak = false
                        println("determining if ${st_list[i].toString()} is a function or an object")
//                        println("iiii = $iiii\ni = $i")
                        while (iiii <= st_list.lastIndex) {
                            if (dobreak) break
                            var iii = 0
                            var c = 0
                            var skipa = false
//                            println("st_list[$iiii].toString() = ${st_list[iiii].toString()}")
                            while (iii <= token_list.lastIndex) {
                                // attempt to find next valid index that does not match puncuation
                                // however it is important that if '(' is found first we break the loop
                                // to prevent '(' being matched further along the index
//                                println("token_list[$iii].toString() = ${token_list[iii].toString()}")
                                if (st_list[iiii].toString().equals("(") == true) {
                                    isvalid = true
                                    dobreak = true
                                    break
                                } else {
                                    var a = 0
                                    while (a <= token_list.lastIndex) {
                                        // check to see if st_list[iiii] equals any of the tokens in token_list
                                        if (st_list[iiii].toString().equals(token_list[a].toString())) {
                                            skipa = true
                                            break
                                        }
                                        a++
                                    }
                                }
                                iii++
                            }
                            if (!skipa) if (!isvalid) dobreak = true
//                            println("skipa   = $skipa")
//                            println("isvalid = $isvalid")
//                            println("dobreak = $dobreak")
                            iiii++
                        }
                        if (isvalid) {
                            // found function
                            println("${st_list[i].toString()} is a function")
                            println("token list [${iiii-1}] = ${st_list[iiii-1].toString()}")
                            println("${it.Token} of type ${it.Type} has value ${it.Value}")
                            // in order to replace a function, one method would be to
                            // reconstruct the list as a full string, starting from the current
                            // index, to the last index, then use the balanced class to scan the
                            // string for the start and end of the function

                            // NOTE: the C pre-processor will fully expand each argument
                            // with the exception of self-referencing macro's
                            var rebuiltstring = ""
                            var i2 = original_index
                            while (i2 <= st_list.lastIndex) {
                                rebuiltstring = rebuiltstring.plus(st_list[i2].toString())
                                i2++
                            }
                            println("rebuilt string = $rebuiltstring")
                            // first, determine the positions of all tokens
                            var balance = balanced.balanceList()
                            balance.addPair('(', ')')
                            val ex = balanced()
                            if (ex.isBalancedSplit(rebuiltstring, balance, ',')) {
                                ex.info()
                                val str = rebuiltstring.substring(0, ex.end[0])
                                val strt = StringTokenizer(str, tokens, true)
                                val strt_list = strt.toList()
                                println("extracted    : $str")
                                val str_arguments = extract_arguments(str.substring(ex.start[0]+1, ex.end[0]-1), MACRO, true)
                                println("tokenization : $strt_list")
                                println("need to skip ${strt_list.lastIndex-1} tokens")
                                val mi = macro_exists(st_list[i].toString(), Macro().Directives().Definition().Types().FUNCTION, index, MACRO)
                                println("mi = $mi")
                                if (currentmacroexists) {
                                    println("${MACRO[index].Macros[mi].Token} of type ${MACRO[index].Macros[mi].Type} has value ${MACRO[index].Macros[mi].Value}")
                                    println("macro  args = ${MACRO[index].Macros[mi].Arguments}")
                                    println("target args = $str_arguments")
                                    replacement = macro_substitution(MACRO[index].Macros[mi].Value!!, MACRO[index].Macros[mi].Arguments, str_arguments, MACRO)
                                    println("replacement = $replacement")
                                }
                                i+=strt_list.lastIndex
                            }
                        }
                        else {
                            println("${st_list[i].toString()} is an object")
                            val mi = macro_exists(st_list[i].toString(), Macro().Directives().Definition().Types().OBJECT, index, MACRO)
                            if (currentmacroexists) {
                                println("${MACRO[index].Macros[mi].Token} of type ${MACRO[index].Macros[mi].Type} has value ${MACRO[index].Macros[mi].Value}")
                                replacement = MacroExpansionEngine(MACRO[index].Macros[mi].Value!!, index, MACRO)
                            }
                        }
                        skip_lower = true
                    }
                    else println("${it.Token} of type ${it.Type} does not match ${st_list[i].toString()}")
                }
            }
        }
        newstr = newstr.plus(replacement)
        i++
    }
    println("expanded string : $newstr")
    return newstr
}

fun macro_exists(token : String, type : String, index : Int, MACRO : ArrayList<Macro>) : Int {
    // used to detect existing definitions for #define
    currentmacroexists = false
    // if empty return 0 and do not set currentmacroexists
    if (MACRO[index].Macros[0].FullMacro == null) return 0
    var i = 0
    while (i <= MACRO[index].Macros.lastIndex) {
        if (MACRO[index].Macros[i].Token.equals(token) && MACRO[index].Macros[i].Type.equals(type)) {
            println("token and type matches existing definition ${MACRO[index].Macros[i].Token} type ${MACRO[index].Macros[i].Type}")
            currentmacroexists = true
            println("returning $i")
            return i
        }
        else println("token $token or type $type does not match current definition token ${MACRO[index].Macros[i].Token} type ${MACRO[index].Macros[i].Type}")
        i++
    }
    return i
}

fun macro_list(index : Int = 0, MACRO : ArrayList<Macro>) {
    if (MACRO[index].Macros[0].FullMacro == null) return
    println("LISTING MACROS")
    var i = 0
    while (i <= MACRO[index].Macros.lastIndex) {
        println("[$i].FullMacro  = ${MACRO[index].Macros[i].FullMacro}")
        println("[$i].Type       = ${MACRO[index].Macros[i].Type}")
        println("[$i].Token      = ${MACRO[index].Macros[i].Token}")
        if (MACRO[index].Macros[i].Arguments != null)
            println("[$i].Arguments  = ${MACRO[index].Macros[i].Arguments}")
        println("[$i].Value      = ${MACRO[index].Macros[i].Value}")
        i++
    }
    println("LISTED MACROS")
}

fun toMacro(macro_arguments : ArrayList<String>?, actual_arguments : ArrayList<String>?) : ArrayList<Macro> {
    println("${macro_arguments!!.size} == ${actual_arguments!!.size} is ${macro_arguments!!.size == actual_arguments!!.size}")
    if ((macro_arguments!!.size == actual_arguments!!.size) == false) {
        abort("size mismatch: expected ${macro_arguments!!.size}, got ${actual_arguments
        !!.size}")
    }
    var associated_arguments = arrayListOf(Macro())
    var i = 0
    associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
    associated_arguments[0].Macros[i].Type = Macro().Directives().Definition().Types().OBJECT
    associated_arguments[0].Macros[i].Token = macro_arguments[i]
    associated_arguments[0].Macros[i].Value = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        associated_arguments[0].Macros = associated_arguments[0].Macros[0].realloc(associated_arguments[0].Macros, associated_arguments[0].Macros[0].size+1)
        associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
        associated_arguments[0].Macros[i].Type = Macro().Directives().Definition().Types().OBJECT
        associated_arguments[0].Macros[i].Token = macro_arguments[i]
        associated_arguments[0].Macros[i].Value = actual_arguments[i]
        i++
    }
    macro_list(MACRO = associated_arguments)
    return associated_arguments
}

fun toMacro(macro_arguments : ArrayList<String>?, actual_arguments : List<String>?) : ArrayList<Macro> {
    println("${macro_arguments!!.size} == ${actual_arguments!!.size} is ${macro_arguments!!.size == actual_arguments!!.size}")
    if ((macro_arguments!!.size == actual_arguments!!.size) == false) {
        abort("size mismatch: expected ${macro_arguments!!.size}, got ${actual_arguments
        !!.size}")
    }
    var associated_arguments = arrayListOf(Macro())
    var i = 0
    associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
    associated_arguments[0].Macros[i].Type = Macro().Directives().Definition().Types().OBJECT
    associated_arguments[0].Macros[i].Token = macro_arguments[i]
    associated_arguments[0].Macros[i].Value = actual_arguments[i]
    i++
    while (i <= macro_arguments.lastIndex) {
        associated_arguments[0].Macros = associated_arguments[0].Macros[0].realloc(associated_arguments[0].Macros, associated_arguments[0].Macros[0].size+1)
        associated_arguments[0].Macros[i].FullMacro = "define ${macro_arguments[i]} ${actual_arguments[i]}"
        associated_arguments[0].Macros[i].Type = Macro().Directives().Definition().Types().OBJECT
        associated_arguments[0].Macros[i].Token = macro_arguments[i]
        associated_arguments[0].Macros[i].Value = actual_arguments[i]
        i++
    }
    macro_list(MACRO = associated_arguments)
    return associated_arguments
}

fun macro_substitution(str : String, macro_arguments : ArrayList<String>?, actual_arguments : ArrayList<String>?, MACRO : ArrayList<Macro>) : String {
    println("substituting $str")
    val associated_arguments = toMacro(macro_arguments, actual_arguments)
    var E = MacroExpansionEngine(str, 0, associated_arguments)
    println("E = $E")
    E = MacroExpansionEngine(E, 0, MACRO)
    return E
}