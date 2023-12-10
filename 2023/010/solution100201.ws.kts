import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

val toPipe = mapOf(
        '|' to Pipe(north = true, east = false, south = true, west = false),
        '-' to Pipe(north = false, east = true, south = false, west = true),
        'L' to Pipe(north = true, east = true, south = false, west = false),
        'J' to Pipe(north = true, east = false, south = false, west = true),
        '7' to Pipe(north = false, east = false, south = true, west = true),
        'F' to Pipe(north = false, east = true, south = true, west = false),
        '.' to Pipe(north = false, east = false, south = false, west = false),
        'S' to Pipe(north = true, east = true, south = true, west = true),
)

val inputToBoxChar = mapOf(
        '|' to '│',
        '-' to '─',
        'L' to '╰',
        'J' to '╯',
        '7' to '╮',
        'F' to '╭',
        '.' to ' ',
        'S' to '○',
)

for (inputPrefix in sequenceOf("sample", "sample2", "sample3", "sample4", "sample5", "real")) {
    val start = System.nanoTime()
    val rawPipes = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .toList()
    val pipes = rawPipes
            .map { line -> line.asSequence().map { toPipe[it]!! }.toList() }
            .toList()
    val board = Board(pipes)

    var loopLength = 0
    val visited = mutableSetOf<Pair<Int, Int>>()
    do {
        visited.add(board.current)
        board.move(visited)
        loopLength++
    } while (board.current != board.start)

    val rowCount = board.pipes.size
    val columnCount = board.pipes.first().size

    val inner = List(rowCount) { MutableList(columnCount) { 0 } }
    for (row in 0 until rowCount) {
        val colCnt = (0 until columnCount).iterator()
        var inside = false
        var col = -1
        while (colCnt.hasNext()) {
            // scan until loop
            while (colCnt.hasNext()) {
                col = colCnt.nextInt()
                val pos = row to col
                if (pos in visited) {
                    break
                }
                inner[row][col] = if (inside) 1 else -1
            }
            // scan to entrance/exit
            var northSeen = 0
            var southSeen = 0
            while (colCnt.hasNext()) {
                val pos = row to col
                val pipe = board.pipeAtWithStart(pos)
                if (pipe.north) {
                    northSeen++
                }
                if (pipe.south) {
                    southSeen++
                }
                if ((northSeen % 2 == 1) and (southSeen % 2 == 1)) {
                    inside = !inside
                    break
                }
                if ((northSeen >= 2) or (southSeen >= 2)) {
                    break
                }
                col = colCnt.nextInt()
            }
        }
    }

    // right: 303

    var count = 0
    for (row in 0 until rowCount) {
        rawPipes[row].asSequence().forEachIndexed { col, char ->
            val innerVal = inner[row][col]
            if (innerVal > 0) {
                print('●')
            } else if (innerVal < 0) {
                print(' ')
            } else if (row to col in visited) {
                print(inputToBoxChar[char])
            } else {
                print(' ')
            }
        }
        println()
    }
//    println()

    val result = count

    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}

/*
| is a vertical pipe connecting north and south.
- is a horizontal pipe connecting east and west.
L is a 90-degree bend connecting north and east.
J is a 90-degree bend connecting north and west.
7 is a 90-degree bend connecting south and west.
F is a 90-degree bend connecting south and east.
. is ground; there is no pipe in this tile.
S is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.
 */

data class Pipe(val north: Boolean, val east: Boolean, val south: Boolean, val west: Boolean) {
    val vertical = north and south
    val horizontal = east and west
    val turn = (north xor south) and (east xor west)
}

// Position is row, column
data class Board(val pipes: List<List<Pipe>>) {
    val start = pipes
            .mapIndexed { rowIndex, row -> rowIndex to row.indexOfFirst { it.north && it.east && it.south && it.west } }
            .single { it.second >= 0 }

    var current = start

    val startEquivalent = let {
        // (row, column)
        val pos = start
        val north = pipeAt(pos.first - 1 to pos.second)?.south ?: false
        val east = pipeAt(pos.first to pos.second + 1)?.west ?: false
        val south = pipeAt(pos.first + 1 to pos.second)?.north ?: false
        val west = pipeAt(pos.first to pos.second - 1)?.east ?: false
        Pipe(north, east, south, west)
    }

    fun pipeAt(pos: Pair<Int, Int>) = pipes.getOrNull(pos.first)?.getOrNull(pos.second)

    fun pipeAtWithStart(pos: Pair<Int, Int>) = if (pos == start) { startEquivalent } else { pipeAt(pos)!! }

    fun move(visited: Set<Pair<Int, Int>>) {
        val currentPipe = if (current == start) {
            startEquivalent
        } else {
            pipeAt(current)!!
        }

        val possibleMoves = mutableListOf<Pair<Int, Int>>()
        if (currentPipe.north) {
            possibleMoves.add(current.first - 1 to current.second)
        }
        if (currentPipe.east) {
            possibleMoves.add(current.first to current.second + 1)
        }
        if (currentPipe.south) {
            possibleMoves.add(current.first + 1 to current.second)
        }
        if (currentPipe.west) {
            possibleMoves.add(current.first to current.second - 1)
        }
        current = possibleMoves.filter { it !in visited }.firstOrNull() ?: start
    }

}
