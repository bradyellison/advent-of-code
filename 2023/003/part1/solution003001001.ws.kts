import java.io.File
import kotlin.math.max
import kotlin.streams.asSequence

for (inputPrefix in sequenceOf("sample", "real")) {

    val rawSchematic = File("""${inputPrefix}.input001.txt""")
            .bufferedReader()
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toCharArray().toList() }
            .toList()


    val engineSchematic = EngineSchematic(rawSchematic)

    for (y in -1 until engineSchematic.height + 1) {
        for (x in -1 until engineSchematic.width + 1) {
            print(engineSchematic.safeAt(x, y))
        }
        println()
    }

    val numbers = mutableListOf<Int>()
    for (y in 0..<engineSchematic.height) {
        var isNumber = true
        var shouldInclude = false
        var num = 0
        for (x in 0..engineSchematic.width) { // Include last, simplifies things
            val position = engineSchematic.at(x, y)
            // print("$isNumber, $shouldInclude, $num, ${position.value}")
            if (position.isNumber) {
                if (isNumber) {
                    // continue
                    num = num * 10 + position.value
                } else {
                    // start
                    num = position.value
                    isNumber = true
                }
                shouldInclude = shouldInclude || position.hasSymbol
            } else if (isNumber) { // isNumber but not positional Number
                if (shouldInclude) {
                    numbers.add(num)
                }
                isNumber = false
                shouldInclude = false
                num = 0
            }
        }
        // println()
    }

    var result = numbers.sum()

    println("Numbers: ${numbers}")
    println("Input:   ${inputPrefix}")
    println("Result:  ${result}")
}

data class EngineSchematic(val board: List<List<Char>>) {
    companion object {
        val notSymbols = setOf('.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    }

    val width = board.first().size
    val height = board.size

    fun rawAt(x: Int, y: Int) = board[y][x]
    fun safeAt(x: Int, y: Int) = try { rawAt(x, y) } catch (e: IndexOutOfBoundsException) { '.' }
    fun at(x: Int, y: Int): EnginePosition {
        val maybeNum = safeAt(x, y) - '0'
        val num = if (maybeNum in 0..9) maybeNum else -1
        var hasSymbol = false;
        if (num >= 0) {
            for (subX in x-1..x+1) {
                for (subY in y-1..y+1) {
                    if (subX != x || subY != y) {
                        if (safeAt(subX, subY) !in notSymbols) {
                            hasSymbol = true
                        }
                    }
                }
            }
        }
        return EnginePosition(this, x, y, num, hasSymbol)
    }
}

data class EnginePosition(val board: EngineSchematic, val x: Int, val y: Int, val value: Int, val hasSymbol: Boolean) {
    val isNumber = value >= 0
}

