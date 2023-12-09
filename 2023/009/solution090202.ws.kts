import java.io.File
import kotlin.streams.asSequence

val digitRun = "[-0-9]+".toRegex()

fun part1(rows: Sequence<List<Int>>): Int = rows
        .map { it.last() }
        .sum()

fun part2(rows: Sequence<List<Int>>): Int = rows
        .map { it.first() }.toList().asReversed()
        .reduce { a, b -> b - a }

for (inputPrefix in sequenceOf("sample", "real")) {
    println("Input:       $inputPrefix")
    for ((partName, part) in sequenceOf("part1" to ::part1, "part2" to ::part2)) {
        // Parse file
        val file = File("../${inputPrefix}.input001.txt")
        val lines = file.bufferedReader().lines().parallel().map { it.trim() }.filter { it.isNotEmpty() }
        val starting = lines.map { digitRun.findAll(it).map { it.value.toInt() }.toList() }

        // Compute result
        val perLineResult = starting.mapToInt {
            generateSequence(it) { current -> current.zipWithNext { a, b -> b - a } }
                    .takeWhile { it.last() != 0 }
                    .let(part)
        }
        val result = perLineResult.sum()

        println("Part: $partName, Result: $result")
    }
    println()
}
