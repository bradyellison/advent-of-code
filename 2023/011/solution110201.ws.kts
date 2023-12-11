import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val galaxy = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .map { line -> line.asSequence().toList() }
            .toList()
            .let { Galaxy(it) }

    println("Input:       ${inputPrefix}")
    //println(galaxy.prettyPrint())
    for (emptyDistance in listOf(2, 10, 100, 1000000)) {
        val galaxyPairAndDistance = galaxy.galaxyToPosition.keys.toList().combinations()
                .map {
                    val startPos = galaxy.galaxyToPosition[it.first]!!
                    val endPos = galaxy.galaxyToPosition[it.second]!!
                    var distance = 0L

                    // walk horizontally until in column
                    // walk vertically until in row
                    val rowIndexes = if (startPos.first > endPos.first) {
                        endPos.first..startPos.first
                    } else {
                        startPos.first..endPos.first
                    }
                    val colIndexes = if (startPos.second > endPos.second) {
                        endPos.second..startPos.second
                    } else {
                        startPos.second..endPos.second
                    }
                    for (row in rowIndexes) {
                        if (galaxy.galaxy[row][colIndexes.first] < 0) {
                            distance += emptyDistance
                        } else {
                            distance++
                        }
                    }
                    for (col in colIndexes) {
                        if (galaxy.galaxy[rowIndexes.last][col] < 0) {
                            distance += emptyDistance
                        } else {
                            distance++
                        }
                    }
                    distance -= 2

                    it to (distance)
                }

        val result = galaxyPairAndDistance.map { it.second }.sum()
        val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

        //println("Timing:      ${runMillis}")
        println("Empty Dist:  ${emptyDistance}")
        println("Result:      ${result}")
    }
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
        val verticallyExpanded = rawGalaxy.map {
            if (it.all { it == '.' }) {
                List(it.size) { '*' }
            } else {
                it
            }
        }
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
                    rowVals.add(-1)
                } else {
                    val expandedChar = verticallyExpanded[row][col]
                    if (expandedChar == '#') {
                        rowVals.add(galaxyNum++)
                    } else if (expandedChar == '*') {
                        rowVals.add(-1)
                    } else {
                        rowVals.add(0)
                    }
                }
            }
        }
        expanded
    }

    var galaxyToPosition = galaxy
            .flatMapIndexed { rowIndex, row ->
                row.mapIndexedNotNull { colIndex, galaxy ->
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
        builder.append(galaxy.map { it.map { if (it == 0) "(..)" else if (it < 0) "(**)" else "(${"%2d".format(it)})" }.joinToString("") }.joinToString("\n", postfix = "\n"))
        return builder.toString()
    }
}
