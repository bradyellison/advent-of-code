import java.io.File

//val inputPrefix = "sample"
val inputPrefix = "real"

val notDigit = "[^0-9]".toRegex()
val zero = "zero".toRegex(RegexOption.IGNORE_CASE)
val one = "one".toRegex(RegexOption.IGNORE_CASE)
val two = "two".toRegex(RegexOption.IGNORE_CASE)
val three = "three".toRegex(RegexOption.IGNORE_CASE)
val four = "four".toRegex(RegexOption.IGNORE_CASE)
val five = "five".toRegex(RegexOption.IGNORE_CASE)
val six = "six".toRegex(RegexOption.IGNORE_CASE)
val seven = "seven".toRegex(RegexOption.IGNORE_CASE)
val eight = "eight".toRegex(RegexOption.IGNORE_CASE)
val nine = "nine".toRegex(RegexOption.IGNORE_CASE)

val sum = File("""${inputPrefix}.input001.txt""")
        .bufferedReader()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .peek{ print("${it} - ") }
        .map { zero.replace(it, "0zero") }
        .map { one.replace(it, "o1e") }
        .map { two.replace(it, "t2o") }
        .map { three.replace(it, "t3e") }
        .map { four.replace(it, "f4r") }
        .map { five.replace(it, "f5e") }
        .map { six.replace(it, "s6x") }
        .map { seven.replace(it, "s7n") }
        .map { eight.replace(it, "e8t") }
        .map { nine.replace(it, "n9e") }
        .map { notDigit.replace(it, "") }
        .map { "${it.first()}${it.last()}" }
        .mapToInt { it.toInt() }
        .peek { println(it) }
        .sum()

println()
println(sum)
