import java.io.File
import java.lang.IllegalStateException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.streams.asSequence

val digitRuns = "[0-9]+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val values = File("""${inputPrefix}.input001.txt""")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .take(2)
            .map { line ->
                val (kind, valuesRaw) = line.splitToSequence(':').take(2).zipWithNext().single()
                digitRuns.findAll(valuesRaw.replace(" ", ""))
                        .map { it.value }
                        .map { it.toLong() }
                        .let { values ->
                            when (kind) {
                                "Time" -> values.map { Time(it) }
                                "Distance" -> values.map { Distance(it) }
                                else -> { throw UnsupportedOperationException() }
                            }
                        }
            }
            .zipWithNext().single()
            .let { (al, bl) -> al.zip(bl) }
            .map { (a, b) ->
                when {
                    a is Time && b is Distance -> TimeAndDistance(a, b)
                    a is Distance && b is Time -> TimeAndDistance(b, a)
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
            .map { it.winningCount() }

    val result = values.fold(1L) { prod, current -> prod * current }

    println("Input:       ${inputPrefix}")
    println("Result:      ${result}")
}


@JvmInline value class Time(val value: Long)
@JvmInline value class Distance(val value: Long)
data class TimeAndDistance(val time: Time, val distance: Distance) {
    fun winningCount(): Long {
        /*
                t = race time
                h = hold time
                l = travel time
                t-h = l

                d = distance travelled
                h*(1 mm/ms) * l = d
                h * (t - h) = d
                h*t - h*h = d

                solve for h:
                h = 1/2 (t + sqrt(t^2 - 4 d))
                h = 1/2 (t - sqrt(t^2 - 4 d))
                 */
        val time = time.value.toDouble()
        val distance = distance.value.toDouble()
        val inner = sqrt(((time * time) - (4 * distance)))
        val max = (((time + inner) / 2.0))
        val min = (((time - inner) / 2.0))
        val maxFloor = floor(max)
        val minCeil = ceil(min)
        val start = if (max != maxFloor) maxFloor else maxFloor - 1
        val end = if (min != minCeil) minCeil else minCeil + 1
        return (start - end + 1).toLong()
    }
}