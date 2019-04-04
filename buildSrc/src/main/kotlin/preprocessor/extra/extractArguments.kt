package preprocessor.extra

import preprocessor.core.Macro
import preprocessor.utils.abort
import java.util.ArrayList

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of paramaters
 */
fun extract_arguments(arg : String, MACRO : ArrayList<Macro>)  : ArrayList<String>? {
    fun filterSplit(arg : String, ex : Balanced, b : Balanced.BalanceList) : ArrayList<String> {
        var Arguments : ArrayList<String> = arrayListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    Arguments.add(arg)
                    println(Arguments[0])
                } else {
                    var s : String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    Arguments.add(s)
                    println(Arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i]+1, ex.splitterLocation[i+1]).trimStart()
                        Arguments.add(s)
                        println(Arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    Arguments.add(s)
                    println(Arguments[i])
                }
            }
            else {
                ex.info()
                abort("unBalanced code")
            }
        }
        else if (ex.containsR(arg, b)) {
            // unBalanced
            abort("unBalanced code")
        }
        else {
            var a : MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                var s : String = a.get(i).trimStart().trimEnd()
                Arguments.add(s)
                i++
            }
        }
        println("Arguments List = $Arguments")
        return Arguments as ArrayList
    }
    println("extracting arguments for $arg")
    // first, determine the positions of all tokens
    var balance = Balanced.BalanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = Balanced()
    return filterSplit(arg, ex, balance)
}
