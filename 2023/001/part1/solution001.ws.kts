import java.io.File

//val inputPrefix = "sample"
val inputPrefix = "real"
val notDigit = "[^0-9]".toRegex()

println("hello, world!")
val sum = File("""${inputPrefix}.input001.txt""")
        .bufferedReader()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { notDigit.replace(it, "") }
        .map { "${it.first()}${it.last()}" }
        .mapToInt { it.toInt() }
        .peek { println(it) }
        .sum()

println()
println(sum)
