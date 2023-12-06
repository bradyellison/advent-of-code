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

    var gearRatios = mutableListOf<Int>()
    for (y in -1 until engineSchematic.height + 1) {
        for (x in -1 until engineSchematic.width + 1) {
            if (engineSchematic.isGear(x, y)) {
                // look around for numbers
                val nums = mutableListOf<Int>()
                for (dy in y-1..y+1) {
                    var dx = x - 1
                    while( dx <= x + 1) {
                        val (num, delta) = engineSchematic.extractNum(dx, dy)
                        dx += delta
                        if (num >= 0) {
                            nums.add(num)
                        }
                    }
                }
                if (nums.size > 1) {
                    val gearRatio = nums.fold(1) { total, num -> total * num }
                    gearRatios.add(gearRatio)
                }
            } else if (engineSchematic.isNum(x, y)) {
                print('#')
            } else {
                print('.')
            }
        }
        println()
    }

    var result = gearRatios.sum()

    println("Numbers: ${gearRatios}")
    println("Input:   ${inputPrefix}")
    println("Result:  ${result}")
}

data class EngineSchematic(val board: List<List<Char>>) {
    companion object {
        val notSymbols = setOf('.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val nums = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    }

    val width = board.first().size
    val height = board.size

    fun rawAt(x: Int, y: Int) = board[y][x]
    fun safeAt(x: Int, y: Int) = try { rawAt(x, y) } catch (e: IndexOutOfBoundsException) { '.' }
    fun isGear(x: Int, y: Int) = safeAt(x, y) == '*'
    fun isNum(x: Int, y: Int) = safeAt(x, y) in nums
    fun extractNum(x: Int, y: Int): Pair<Int, Int> {
        if (!isNum(x, y)) {
            return Pair(-1, 1)
        }

        // roll-left
        var delta = 0
        while (isNum(x + delta, y)) {
            delta--
        }
        delta++
        // roll-right
        var num = 0
        while (isNum(x + delta, y)) {
            num = (num * 10) + (rawAt(x + delta, y) - '0')
            delta++
        }
        return Pair(num, delta)
    }
    fun atOld(x: Int, y: Int): EnginePosition {
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

