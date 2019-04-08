package preprocessor.utils.extra

import preprocessor.core.Macro
import preprocessor.utils.core.abort
import java.util.ArrayList

/**
 * extracts the arguments of a function and puts them into an array
 *
 * @returns an array of paramaters
 */
fun extractArguments(arg: String): ArrayList<String>? {
    fun filterSplit(arg: String, ex: Balanced, b: Balanced.BalanceList): ArrayList<String> {
        val arguments: ArrayList<String> = arrayListOf()
        if (ex.containsL(arg, b)) {
            if (ex.isBalancedSplit(arg, b, ',')) {
                ex.info()
                if (ex.splitterCount == 0) {
                    arguments.add(arg)
                    println(arguments[0])
                } else {
                    var s: String = arg.substring(0, ex.splitterLocation[0]).trimStart()
                    arguments.add(s)
                    println(arguments[0])
                    var i = 0
                    while (i < ex.splitterLocation.lastIndex) {
                        s = arg.substring(ex.splitterLocation[i] + 1, ex.splitterLocation[i + 1]).trimStart()
                        arguments.add(s)
                        println(arguments[i])
                        i++
                    }
                    s = arg.substring(ex.splitterLocation[i] + 1, ex.index).trimStart()
                    arguments.add(s)
                    println(arguments[i])
                }
            } else {
                ex.info()
                abort("unBalanced code")
            }
        } else if (ex.containsR(arg, b)) {
            // unBalanced
            abort("unBalanced code")
        } else {
            val a: MutableList<String> = arg.split(',').toMutableList()
            // next, remove whitespaces from the start and end of each index string
            var i = 0
            while (i <= a.lastIndex) {
                val s: String = a[i].trimStart().trimEnd()
                arguments.add(s)
                i++
            }
        }
        println("arguments List = $arguments")
        return arguments
    }
    println("extracting arguments for $arg")
    // first, determine the positions of all tokens
    val balance = Balanced.BalanceList()
    balance.addPair('(', ')')
    balance.addPair('{', '}')
    balance.addPair('[', ']')
    val ex = Balanced()
    return filterSplit(arg, ex, balance)
}
