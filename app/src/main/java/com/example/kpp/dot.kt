package com.example.kpp

import java.io.*
import java.nio.*
import java.nio.file.*

fun t() {
    val functions = mutableMapOf<String, (String) -> Unit>()
    functions["first"] = { v: String ->
        println("invoked with paramater 'v' with value '$v'")
    }
    println(functions)
    functions["first"]?.invoke("a")
    val chars: List<Char> = listOf('a', '+', '1')
    println("$chars")
}