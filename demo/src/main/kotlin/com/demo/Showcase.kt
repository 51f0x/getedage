package com.demo


fun main(vararg args: Int): Int {

    if (args.size < 2) {
        println("Please provide at least two integer arguments.")
        return -1
    }

    val num1 = args[0]
    val num2 = args[1]

    var result = -1

    for (i in 1..10) {
        
        var a = doSomeWork(i,num1)

        var b = doSomeOtherWork(a, num2)

        var c = doSomeMoreWork(b)

        result = c

    }

    println("Result: $result")

    return result
}

fun doSomeWork(a: Int, b: Int): Int {
    if (a > b) {
        return a + b
    } else  if (b == 0) {
        return 0
    } else {
        return a - b
    }
}

fun doSomeOtherWork(a: Int, b: Int): Int {
    return a * 2 - b
}   

fun doSomeMoreWork(a: Int): Int {
    return a * 3
}