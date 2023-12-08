import Solution080101_ws.Fork
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.asSequence

typealias Fork = Pair<Int, Int>

val nonWordRun = "\\W+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val (rawInstructionSequence, rawPositionAndFork) = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .let {
                val iter = it.iterator()
                iter.next() to iter.asSequence()
            }

    val instructionSequence = rawInstructionSequence.asSequence()
            .map {
                when (it) {
                    'R' -> Fork::second
                    'L' -> Fork::first
                    else -> throw IllegalArgumentException()
                }
            }.toList()
            .let { generateSequence { it }.flatMap { it.asSequence() } }

    val symbolTable = mutableMapOf<String, Int>()
    val symbolCounter = AtomicInteger(0)
    val positionToFork = rawPositionAndFork
            .flatMap { line ->
                line.splitToSequence(nonWordRun)
                        .map { symbolTable.computeIfAbsent(it) { symbolCounter.getAndIncrement() } }
                        .take(3)
            }
            .chunked(3) { (from, left, right) -> from to Fork(left, right) }
            .toMap()

    val endSymbol = symbolTable["ZZZ"]
    val moveCount = instructionSequence
            .runningFold(symbolTable["AAA"]) { currentSymbol, instruction -> instruction(positionToFork[currentSymbol]!!) }
            .takeWhile { it != endSymbol }
            .count()

    val result = moveCount
    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}

