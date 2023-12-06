import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence
import kotlin.streams.asStream

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val (seedsRaw, mapsRaw) = File("""${inputPrefix}.input001.txt""")
            .bufferedReader()
            .lines()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .fold(mutableListOf<MutableList<String>>()) { groups, line ->
                if (line.contains(':')) {
                    groups.add(mutableListOf(line))
                } else {
                    groups.last().add(line)
                }
                groups
            }
            .partition { !it.first().endsWith("map:") }

    // ideal: seedsRaw, not empty, exactly 1 line, starts with "seeds".
    println("seedsRaw: ${seedsRaw}")
    val seeds = seedsRaw.first().first()
            .splitToSequence(':')
            .last()
            .splitToSequence(' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toLong() }
            .toList()

    val maps = mapsRaw
            .fold(mutableMapOf<Pair<String, String>, List<MappedRange>>()) { map, lines ->
                val (from, to) = lines.first()
                        .splitToSequence(' ')
                        .first()
                        .splitToSequence("-to-")
                        .take(2)
                        .toList()
                val mapping = lines.drop(1)
                        .map { // val (toStart, fromStart, count) =
                            it.splitToSequence(' ')
                                .take(3)
                                .map { it.toLong() }
                                .toList() }
                        .sortedBy { it.component2() }
                        .map { (toStart, fromStart, count) -> MappedRange(fromStart, toStart, count) }
                        .toList()
//                        .fold(mutableListOf<MappedRange>()) { ranges, (toStart, fromStart, count) ->
//                            ranges.add(MappedRange(fromStart, toStart, count))
//                            ranges
//                        }
                map[Pair(from, to)] = mapping
                map
            }

    val mappingsAvailable = maps.keys.toMap()
    val locationPath = mutableListOf<Pair<Pair<String, String>, List<MappedRange>>>()
    var to = "seed"
    while (to != "location") {
        val from = to;
        to = mappingsAvailable[to]!!
        val mappingKey = Pair(from, to)
        val step = maps[mappingKey]!!
        locationPath.add(Pair(mappingKey, step))
    }
    // optimization, pre-compute the fromStart collection
    val locationPathOpti = locationPath.map { it.second to (it.second.map { it.fromStart }).toLongArray() }.toList()

//    val seedToLocation = seeds
    val result = seeds.asSequence()
            .chunked(2) { (seedStart, seedCount) -> seedStart until (seedStart + seedCount) }
            .flatMap {
                println("operating over: $it")
                it
            }
            .asStream()
            .mapToLong { it }
            .parallel()
            .map {
                var mapped = it
                for ((mapping, bin) in locationPathOpti) {
                    val start = mapped
                    // solution #1 but w/ binary search
                    val location = bin.binarySearch(mapped)
                            .let {
                                if (it < 0) {
                                    -(it + 2)
                                } else {
                                    it
                                }
                            }
                    if (location >= 0) {
                        val mappedRange = mapping[location]
                        if (mapped in mappedRange.fromRange) {
                            mapped = mappedRange.toStart + (mapped - mappedRange.fromStart)
                        }
                    }
                }
                mapped
            }
            .min()

//    for ((key, value) in maps.entries) {
//        println("$key : $value")
//    }
    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()
    println("Path:        ${locationPath.map { it.first }.toList()}")
//    println("Seed to Loc: ${seedToLocation}")
    println("Run Millis:  ${runMillis}")
    println("Input:       ${inputPrefix}")
    println("Result:      ${result}")
}

data class MappedRange(val fromStart: Long, val toStart: Long, val count: Long) {
    val fromRange = fromStart until (fromStart + count)
}