import java.io.*
import java.nio.file.Files.exists
import java.util.*

val abort_on_complete = true

fun abortk(e : String = "Aborted") {
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

tasks.register("KOTLIN_PRE_PROCESSOR") {
    println("starting KOTLIN_PRE_PROCESSOR")
//    Macro().test()
    find_source_files(projectDir.toString(), "kt")
    println("KOTLIN_PRE_PROCESSOR finished")
    if (abort_on_complete) abortk()
}

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
            process(src, dest)
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
            process(src, dest)
            if (cp(dest + ".preprocessed.kt", source, true)) { // copy back to source
                println("${dest.substringAfterLast('/')}.preprocessed.kt (in kpp/src) copied back to original source")
            }
            else throw GradleException("failed to copy ${dest}.preprocessed.kt (in kpp/src) to $source")
        }
    }
}

fun process(orig : File, src : String) {
    val lines: List<String> = file(src).readLines()
    val DESTPRE: File = file(src + ".preprocessed.kt")
    if (kpp_MACRO_LIST[0].FileName != null) {
        kpp_MACRO_LIST = kpp_MACRO_LIST[0].realloc(kpp_MACRO_LIST, kpp_MACRO_LIST[0].size+1)
    }
    kpp_MACRO_LIST[0].FileName = orig.name
    println("registered macro definition for ${kpp_MACRO_LIST[0].FileName} at index ${kpp_MACRO_LIST[0].size}")
    println("processing ${kpp_MACRO_LIST[0].FileName} -> ${DESTPRE.name}")
    DESTPRE.createNewFile()
    lines.forEach { line ->
        preprocess(orig, DESTPRE, line)
    }
}

fun test_file(src : File) {
    val lines: List<String> = src.readLines()
    lines.forEach {
            line -> check_if_preprocessor_is_needed(line)
    }
}

fun check_if_preprocessor_is_needed(line: String) {
    if (line.trimStart().startsWith('#')) current_file_contains_preprocesser = true
}

fun test_cache_file(src : File) {
    val lines: List<String> = src.readLines()
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
fun preprocess(src : File, dest : File, line: String) {
    val index = kpp_MACRO_LIST[0].size - 1
    if (line.trimStart().startsWith('#')) {
        val directive = line.trimStart().drop(1).trimStart().substringBefore(' ')
        println("${src.name}: preprocessor directive: $directive")
        println("${src.name}: preprocessor line: ${line.trimStart()}")
        if (directive.equals(Macro().Directives().Definition().value)) {
            var macro_index = kpp_MACRO_LIST[index].Macros[0].size
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
                val i = macro_exists(token, type, index)
                if (currentmacroexists) {
                    macro_index = i
                }
                else {
                    if (kpp_MACRO_LIST[index].Macros[macro_index].FullMacro != null) {
                        kpp_MACRO_LIST[index].Macros = kpp_MACRO_LIST[index].Macros[0].realloc(
                            kpp_MACRO_LIST[index].Macros,
                            kpp_MACRO_LIST[index].Macros[0].size + 1
                        )
                    }
                    macro_index = kpp_MACRO_LIST[index].Macros[0].size
                }
                kpp_MACRO_LIST[index].Macros[macro_index].FullMacro = line.trimStart().trimEnd()
                kpp_MACRO_LIST[index].Macros[macro_index].Token = token
                kpp_MACRO_LIST[index].Macros[macro_index].Type = type
                kpp_MACRO_LIST[index].Macros[macro_index].Value =
                    macro_expand(full_macro.substringAfter(' ').trimStart().substringAfter(' ').trimStart()!!, index).trimStart()
            } else {
                // function
                token =
                    full_macro.substringAfter(' ').substringBefore('(').trimStart()
                val i = macro_exists(token, type, index)
                if (currentmacroexists) {
                    macro_index = i
                }
                else {
                    if (kpp_MACRO_LIST[index].Macros[macro_index].FullMacro != null) {
                        kpp_MACRO_LIST[index].Macros = kpp_MACRO_LIST[index].Macros[0].realloc(
                            kpp_MACRO_LIST[index].Macros,
                            kpp_MACRO_LIST[index].Macros[0].size + 1
                        )
                    }
                    macro_index = kpp_MACRO_LIST[index].Macros[0].size
                }
                kpp_MACRO_LIST[index].Macros[macro_index].FullMacro = line.trimStart().trimEnd()
                kpp_MACRO_LIST[index].Macros[macro_index].Token = token
                kpp_MACRO_LIST[index].Macros[macro_index].Type = type
                // obtain the function arguments
                val t = kpp_MACRO_LIST[index].Macros[macro_index].FullMacro?.substringAfter(' ')!!
                val b = balanced()
                kpp_MACRO_LIST[index].Macros[macro_index].Arguments = extract_arguments(b.extract_text(t).drop(1).dropLast(1))
                kpp_MACRO_LIST[index].Macros[macro_index].Value =
                    macro_expand(t.substring(b.end[0]+1), index).trimStart()
            }
            println("Type       = ${kpp_MACRO_LIST[index].Macros[macro_index].Type}")
            println("Token      = ${kpp_MACRO_LIST[index].Macros[macro_index].Token}")
            if (kpp_MACRO_LIST[index].Macros[macro_index].Arguments != null)
                println("Arguments  = ${kpp_MACRO_LIST[index].Macros[macro_index].Arguments}")
            println("Value      = ${kpp_MACRO_LIST[index].Macros[macro_index].Value}")
            macro_list(index)
            // definition names do not expand
            // definition values do expand
        }
    }
    else {
        if (first_line) {
            dest.writeText(macro_expand(line, index) + "\n")
            first_line = false
        }
        else dest.appendText(macro_expand(line, index) + "\n")
    }
}

fun extract_arguments(arg : String)  : ArrayList<String>? {
    fun filterSplit(arg : String, ex : balanced, b : balanced.balanceList) : ArrayList<String> {
        var Arguments : ArrayList<String> = arrayListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    Arguments.add(arg)
                    println(Arguments[0])
                } else {
                    Arguments.add(arg.substring(0, ex.splitterLocation[0]).trimStart())
                    println(Arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        Arguments.add(arg.substring(ex.splitterLocation[i]+1, ex.splitterLocation[i+1]).trimStart())
                        println(Arguments[i])
                        i++
                    }
                    Arguments.add(arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart())
                    println(Arguments[i])
                }
            }
            else {
                ex.info()
                abortk("unbalanced code")
            }
        }
        else if (ex.containsR(arg, b)) {
            // unbalanced
            abortk("unbalanced code")
        }
        else {
            var a : MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                Arguments.add(a.get(i).trimStart().trimEnd())
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

fun macro_expand(str : String, index : Int) : String{
    if (kpp_MACRO_LIST[index].Macros[0].FullMacro == null) return str
    println("expanding line '$str'")
    var newstr = ""
    val tokens = " ().,->"
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
            kpp_MACRO_LIST[index].Macros.forEach {
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
                                println("tokenization : $strt_list")
                                println("need to skip ${strt_list.lastIndex-1} tokens")
                                val mi = macro_exists(st_list[i].toString(), Macro().Directives().Definition().Types().FUNCTION, index)
                                if (currentmacroexists) {
                                    println("${kpp_MACRO_LIST[index].Macros[mi].Token} of type ${kpp_MACRO_LIST[index].Macros[mi].Type} has value ${kpp_MACRO_LIST[index].Macros[mi].Value}")
                                    if (it.Type.equals(Macro().Directives().Definition().Types().FUNCTION)) {
                                        replacement = kpp_MACRO_LIST[index].Macros[mi].Value!!
                                        println("args = ${kpp_MACRO_LIST[index].Macros[mi].Arguments}")
                                        println("replacement = $replacement")
                                    }
                                }
//                                if (it.Type.equals(Macro().Directives().Definition().Types().FUNCTION)) {
//                                    replacement = it.Value!!
//                                }
//                                else abortk("fatal error: type is not function")
                                i+=strt_list.lastIndex
                            }
                        }
                        else {
                            println("${st_list[i].toString()} is an object")
                            val mi = macro_exists(st_list[i].toString(), Macro().Directives().Definition().Types().OBJECT, index)
                            if (currentmacroexists) {
                                println("${kpp_MACRO_LIST[index].Macros[mi].Token} of type ${kpp_MACRO_LIST[index].Macros[mi].Type} has value ${kpp_MACRO_LIST[index].Macros[mi].Value}")
                                if (it.Type.equals(Macro().Directives().Definition().Types().OBJECT)) {
                                    replacement = kpp_MACRO_LIST[index].Macros[mi].Value!!
                                }
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
fun macro_exists(token : String, type : String, index : Int) : Int {
    // used to detect existing definitions for #define
    currentmacroexists = false
    // if empty return 0 and do not set currentmacroexists
    if (kpp_MACRO_LIST[index].Macros[0].FullMacro == null) return 0
    var i = 0
    while (i <= kpp_MACRO_LIST[index].Macros.lastIndex) {
        if (kpp_MACRO_LIST[index].Macros[i].Token.equals(token) && kpp_MACRO_LIST[index].Macros[i].Type.equals(type)) {
            println("token and type matches existing definition ${kpp_MACRO_LIST[index].Macros[i].Token} type ${kpp_MACRO_LIST[index].Macros[i].Type}")
            currentmacroexists = true
            break
        }
        else println("token $token or type $type does not match current definition token ${kpp_MACRO_LIST[index].Macros[i].Token} type ${kpp_MACRO_LIST[index].Macros[i].Type}")
        i++
    }
    return i
}

fun macro_list(index : Int) {
    if (kpp_MACRO_LIST[index].Macros[0].FullMacro == null) return
    println("LISTING MACROS")
    var i = 0
    while (i <= kpp_MACRO_LIST[index].Macros.lastIndex) {
        println("[$i].FullMacro  = ${kpp_MACRO_LIST[index].Macros[i].FullMacro}")
        println("[$i].Type       = ${kpp_MACRO_LIST[index].Macros[i].Type}")
        println("[$i].Token      = ${kpp_MACRO_LIST[index].Macros[i].Token}")
        if (kpp_MACRO_LIST[index].Macros[i].Arguments != null)
            println("[$i].Arguments  = ${kpp_MACRO_LIST[index].Macros[i].Arguments}")
        println("[$i].Value      = ${kpp_MACRO_LIST[index].Macros[i].Value}")
        i++
    }
    println("LISTED MACROS")
}