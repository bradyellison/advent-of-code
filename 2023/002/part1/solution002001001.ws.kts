import java.io.File
import kotlin.math.max
import kotlin.streams.asSequence

//val inputPrefix = "sample"
val inputPrefix = "real"

// only 12 red cubes, 13 green cubes, and 14 blue cubes
val rules = mapOf(
        "red" to 12,
        "green" to 13,
        "blue" to 14
)

val games: Sequence<Pair<Int, List<Pair<String, Int>>>> = File("""${inputPrefix}.input001.txt""")
        .bufferedReader()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.splitToSequence(':').map { it.trim() } }
        .map {
            val gameIndicator = it.first()
            val gameOutcomes = it.last()
            val gameNum = Integer.parseInt(gameIndicator.splitToSequence(' ').last().trim())
            val colorsAndCounts = gameOutcomes.splitToSequence(';')
                    .map { it.trim() }
                    .flatMap { gameOutcome ->
                        gameOutcome.splitToSequence(',')
                                .map { it.trim() }
                                .map { colorCount ->
                                    val (countPart, color) = colorCount.split(' ')
                                    val ret = color.trim() to Integer.parseInt(countPart.trim())
                                    ret
                                }
                    }
                    .groupingBy { it.first }
                    .fold(0) { current, (_, count) -> max(current, count) }
                    .toList()

            gameNum to colorsAndCounts
        }
        .asSequence()

val result = games.filter { (_, colorAndCounts) ->
    colorAndCounts.all { (color, count) -> rules.getOrDefault(color, 0) >= count }
}
        .fold(0) { tot, (gameNum, _) -> tot + gameNum }

println(result)
