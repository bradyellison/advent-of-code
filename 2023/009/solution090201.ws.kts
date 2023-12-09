import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

val digitRun = "[-0-9]+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val result = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .map { digitRun.findAll(it).map { it.value.toInt() }.toList() }
            .map {
                generateSequence(it) { current ->
                    current.zipWithNext { a, b -> b - a }
                }
                        .takeWhile { it.last() != 0 }
                        .map { it.first() }
                        .toList().asReversed()
                        .reduce { a, b -> b - a }
            }
            .sum()

    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}
