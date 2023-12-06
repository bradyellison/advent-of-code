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
                digitRuns.findAll(valuesRaw)
                        .map { it.value.toInt() }
                        .let { values ->
                            when (kind) {
                                "Time" -> values.map { Time(it) }
                                "Distance" -> values.map { Distance(it) }
                                else -> { throw UnsupportedOperationException() }
                            }
                        }
            }
            .zipWithNext()
            .single()
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
            .map {
                val time = it.time.value.toDouble()
                val distance = it.distance.value.toDouble()
                val inner = sqrt(((time * time) - (4 * distance)))
                val max = (((time + inner) / 2.0))
                val min = (((time - inner) / 2.0))
                val maxFloor = floor(max)
                val minCeil = ceil(min)
                val start = if (max != maxFloor) maxFloor else maxFloor - 1
                val end = if (min != minCeil) minCeil else minCeil + 1
                (start - end + 1).toInt()
            }

    val result = values.fold(1) { prod, current -> prod * current }

    println("Input:       ${inputPrefix}")
    println("Result:      ${result}")
}


@JvmInline value class Time(val value: Int)
@JvmInline value class Distance(val value: Int)
data class TimeAndDistance(val time: Time, val distance: Distance)