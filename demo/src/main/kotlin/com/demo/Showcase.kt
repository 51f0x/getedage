package com.demo


public class Showcase {

    fun doSomeWork(a: Int, b: Int): Int {
        if (a > 19) {
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

}



fun main(vararg args: Int): Int {

    if (args.size < 2) {
        println("Please provide at least two integer arguments.")
        return -1
    }

    val num1 = args[0]
    val num2 = args[1]

    var result = -1

    val showcase = Showcase()

    for (i in 1..10) {
        
        var a = showcase.doSomeWork(i,num1)

        var b = showcase.doSomeOtherWork(a, num2)

        var c = showcase.doSomeMoreWork(b)

        result = c

    }

    println("Result: $result")

    return result
}

