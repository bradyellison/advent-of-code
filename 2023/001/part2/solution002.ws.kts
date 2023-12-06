import java.io.File
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.toList

//val inputPrefix = "sample"
val inputPrefix = "real"

data class Num(val index: Int, val value: Int)

val zero = "0|zero".toRegex(RegexOption.IGNORE_CASE)
val one = "1|one".toRegex(RegexOption.IGNORE_CASE)
val two = "2|two".toRegex(RegexOption.IGNORE_CASE)
val three = "3|three".toRegex(RegexOption.IGNORE_CASE)
val four = "4|four".toRegex(RegexOption.IGNORE_CASE)
val five = "5|five".toRegex(RegexOption.IGNORE_CASE)
val six = "6|six".toRegex(RegexOption.IGNORE_CASE)
val seven = "7|seven".toRegex(RegexOption.IGNORE_CASE)
val eight = "8|eight".toRegex(RegexOption.IGNORE_CASE)
val nine = "9|nine".toRegex(RegexOption.IGNORE_CASE)

val sum = File("""${inputPrefix}.input001.txt""")
        .bufferedReader()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .peek { print("${it} - ") }
        .mapToInt { line ->
            val nums = sequenceOf(
                    extract(line, zero, 0),
                    extract(line, one, 1),
                    extract(line, two, 2),
                    extract(line, three, 3),
                    extract(line, four, 4),
                    extract(line, five, 5),
                    extract(line, six, 6),
                    extract(line, seven, 7),
                    extract(line, eight, 8),
                    extract(line, nine, 9))
                    .flatten()
            nums.minBy { it.index }.value * 10 + nums.maxBy { it.index }.value
        }
        .peek { println(it) }
        .sum()

println()
println(sum)

fun extract(line: String, toCheck: Regex, value: Int): Sequence<Solution002_ws.Num> {
    val matches = toCheck.findAll(line)
    return if (matches.none()) {
        emptySequence()
    } else {
        sequenceOf(
                Solution002_ws.Num(matches.first().range.first, value),
                Solution002_ws.Num(matches.last().range.last, value))
    }
}