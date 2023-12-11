import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.streams.asSequence

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val galaxy = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .map { line -> line.asSequence().toList() }
            .toList()
            .let { Galaxy(it) }

    println(galaxy.prettyPrint())

    val galaxyPairAndDistance = galaxy.galaxyToPosition.keys.toList().combinations()
            .map {
                val startPos = galaxy.galaxyToPosition[it.first]!!
                val endPos = galaxy.galaxyToPosition[it.second]!!
                it to (abs(startPos.first - endPos.first) + abs(startPos.second - endPos.second))
            }

    val result = galaxyPairAndDistance.map { it.second }.sum()
    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}

fun <T> Iterable<T>.combinations() = sequence {
    var drop = 1
    for (i in this@combinations.iterator()) {
        yieldAll(this@combinations.asSequence().drop(drop++).map { i to it })
    }
}

// Position is row, column
data class Galaxy(val rawGalaxy: List<List<Char>>) {
    val galaxy = let {
        val verticallyExpanded = rawGalaxy.flatMap {
            if (it.all { it == '.' }) {
                sequenceOf(it, it)
            } else {
                sequenceOf(it)
            }
        }.toMutableList()
        val horizontalCount = verticallyExpanded.first().size

        val expandIndexes = mutableSetOf<Int>()
        for (col in 0 until horizontalCount) {
            if (rawGalaxy.map { it[col] }.all { it == '.' }) {
                expandIndexes.add(col)
            }
        }
        var galaxyNum = 1
        val expanded = List(verticallyExpanded.size) { mutableListOf<Int>() }
        for (row in (0 until verticallyExpanded.size)) {
            val rowVals = expanded[row]
            for (col in (0 until horizontalCount)) {
                if (col in expandIndexes) {
                    rowVals.addAll(sequenceOf(0, 0))
                } else {
                    if (verticallyExpanded[row][col] == '#') {
                        rowVals.add(galaxyNum++)
                    } else {
                        rowVals.add(0)
                    }
                }
            }
        }
        expanded
    }

    var galaxyToPosition = galaxy
            .flatMapIndexed { colIndex, row ->
                row.mapIndexedNotNull { rowIndex, galaxy ->
                    when {
                        (galaxy > 0) -> galaxy to (rowIndex to colIndex)
                        else -> null
                    }
                }
            }
            .toMap()

    fun prettyPrint(): String {
        val builder = StringBuilder()
        // builder.append(rawGalaxy.map { it.joinToString("") }.joinToString("\n", postfix = "\n"))
        builder.append(galaxy.map { it.map { if (it == 0) "." else "$it" }.joinToString("") }.joinToString("\n", postfix = "\n"))
        return builder.toString()
    }
}
