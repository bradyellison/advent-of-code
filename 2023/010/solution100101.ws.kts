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

for (inputPrefix in sequenceOf("sample", "sample2", "real")) {
    val start = System.nanoTime()
    val board = File("../${inputPrefix}.input001.txt")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .map { line -> line.asSequence().map { toPipe[it]!! }.toList() }
            .toList()
            .let { Board(it) }

    var loopLength = 0
    val visited = mutableSetOf<Pair<Int, Int>>()
    do {
        board.move(visited)
        visited.add(board.current)
        loopLength++
    } while (board.current != board.start)

    val result = loopLength / 2

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

data class Pipe(val north: Boolean, val east: Boolean, val south: Boolean, val west: Boolean)

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

    private fun pipeAt(pos: Pair<Int, Int>) = pipes.getOrNull(pos.first)?.getOrNull(pos.second)

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
        current = possibleMoves.filter { it !in visited }.first()
    }

}
