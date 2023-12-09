import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.streams.asSequence

// real solution in 0.015622 ms (2023-12-08 -- 2019 intel i9)

@JvmInline
value class Fork(val value: Int) {
    constructor(first: Short, second: Short) : this(first.toInt() shl Short.SIZE_BITS or second.toInt())

    fun first(): Int = (value shr Short.SIZE_BITS)
    fun second(): Int = (value and 0xFFFF)
}

val nonWordRun = "\\W+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val (rawInstructionSequence, rawPositionAndFork) = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .let {
                val iter = it.iterator()
                iter.next() to iter.asSequence()
            }

    val instructionSequence = rawInstructionSequence.asSequence()
            .map {
                when (it) {
                    'R' -> false
                    'L' -> true
                    else -> throw IllegalArgumentException()
                }
            }.toList()

    val symbolTable = mutableMapOf<String, Int>()
    val symbolCounter = AtomicInteger(0)
    val positionToForkMap = rawPositionAndFork
            .flatMap { line ->
                line.splitToSequence(nonWordRun)
                        .map { symbolTable.computeIfAbsent(it) { symbolCounter.getAndIncrement() } }
                        .take(3)
            }
            .chunked(3) { (from, left, right) -> from to Fork(left.toShort(), right.toShort()) }
            .toMap()
    val positionToFork = IntArray(positionToForkMap.size) { i -> positionToForkMap[i]!!.value }

    val endSymbol = symbolTable["ZZZ"]!!
    val startSymbol = symbolTable["AAA"]!!

    val instructions = instructionSequence.toTypedArray()
    val insCount = instructions.size
    var runMillis = Double.MAX_VALUE
    for (i in 0..10000) {
        val start = System.nanoTime()
        var insIndex = 0;
        var currentSymbol = startSymbol
        var moveCount = 0
        while (currentSymbol != endSymbol) {
            currentSymbol = if (instructions[insIndex++]) {
                positionToFork[currentSymbol] shr Short.SIZE_BITS
            } else {
                positionToFork[currentSymbol] and 0xFFFF
            }
            moveCount++
            if (insIndex >= insCount) {
                insIndex = 0
            }
        }

        val result = moveCount
        runMillis = min(runMillis, (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble())

        if (i == 10000) {
            println("Input:       ${inputPrefix}")
            println("Timing:      ${runMillis}")
            println("Result:      ${result}")
        }
    }
}

