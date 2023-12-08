import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.asSequence

val nonWordRun = "\\W+".toRegex()
val rightMask = 0xFFFFFFFFL

for (inputPrefix in sequenceOf("sample", "sample2", "real")) {
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
                    'R' -> { fork: Long -> (fork and rightMask).toInt() }
                    'L' -> { fork: Long -> (fork shr 32).toInt() }
                    else -> throw IllegalArgumentException()
                }
            }.toList()
            .let { { generateSequence { it }.flatMap { it.asSequence() } } }

    val symbolTable = mutableMapOf<String, Int>()
    val symbolCounter = AtomicInteger(0)
    val positionToForkMap = rawPositionAndFork
            .flatMap { line ->
                line.splitToSequence(nonWordRun)
                        .map { symbolTable.computeIfAbsent(it) { symbolCounter.getAndIncrement() } }
                        .take(3)
            }
            .chunked(3) { (from, left, right) -> from to ((left.toLong() shl 32) or (right.toLong())) }
            .toMap()
    val positionToFork = LongArray(positionToForkMap.size) { i -> positionToForkMap[i]!! }

    val startSymbols = symbolTable.entries
            .filter { it.key.endsWith('A') }
            .map { it.value }
            .toIntArray()
    val endSymbols = symbolTable.entries
            .filter { it.key.endsWith('Z') }
            .map { it.value }
            .toIntArray()
    val moveCounts = startSymbols
            .map { startSymbol ->
                instructionSequence().runningFold(startSymbol) { currentSymbol, instruction ->
                    instruction(positionToFork[currentSymbol])
                }
                        .takeWhile { it !in endSymbols }
                        .count().toBigInteger()
            }
            .reduce { a, b -> a * b / a.gcd(b) }

    val result = moveCounts
    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}
