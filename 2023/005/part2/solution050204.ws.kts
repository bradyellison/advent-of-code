import java.io.File
import kotlin.streams.asSequence
import kotlin.streams.asStream

val digitRuns = "[0-9]+".toRegex()

// Would be nice to make the find-range use a binary search where we bias "down" and then check
// for in-range, this would likely short-circuit often because I assume there are lots of un-mapped
// values.

for (inputPrefix in sequenceOf("sample", "real")) {
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
//                println("seed: $seedRange")
//                print("  ")
                for (mapping in locationPath) {
                    val start = mapped
                    // a biasing binary search would really help
                    var mappedRange: MappedRange?
                    // Trying to throw out guess if its clearly out of range
                    if (mapped < mapping.second.first().fromRange.first || mapped > mapping.second.last().fromRange.last) {
                        mappedRange = null
                    } else {
                        mappedRange = mapping.second.find { mappedRange ->
                            mapped in mappedRange.fromRange
                        }
                    }
                    if (mappedRange != null) {
                        mapped = mappedRange.toStart + (mapped - mappedRange.fromStart)
                    }
                    //            mapped = mapping.second[mapped] ?: (mapped)
//                    print("($start -> $mapped (d: ${mappedRange})) ~ ")
                }
//                println()
                mapped
            }
            .min()

//    for ((key, value) in maps.entries) {
//        println("$key : $value")
//    }
    println("Path:        ${locationPath.map { it.first }.toList()}")
//    println("Seed to Loc: ${seedToLocation}")
    println("Input:       ${inputPrefix}")
    println("Result:      ${result}")
}

data class MappedRange(val fromStart: Long, val toStart: Long, val count: Long) {
    val fromRange = fromStart until (fromStart + count)
}