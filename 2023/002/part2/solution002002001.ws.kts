import java.io.File
import java.lang.String
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asSequence

//val inputPrefix = "sample"
val inputPrefix = "real"


val games = File("""${inputPrefix}.input001.txt""")
        .bufferedReader()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.splitToSequence(':').map { it.trim() } }
        .map {
            val gameIndicator = it.first()
            val gameOutcomes = it.last()
            val gameNum = Integer.parseInt(gameIndicator.splitToSequence(' ').last().trim())
            val power = gameOutcomes.splitToSequence(';')
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
                    .values
                    .fold(1) { current, count -> current * count }

            gameNum to power
        }
        .toList()
println(games)

val result = games
        .asSequence()
        .map { it.second }
        .sum()

println(result)