import java.io.File

val digitRuns = "[0-9]+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val cardValues = File("""${inputPrefix}.input001.txt""")
            .bufferedReader()
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val (cardIdRaw, unparsedState) = line.splitToSequence(':').toList()
                val cardId = Integer.parseInt(cardIdRaw.splitToSequence(' ').last())
                val (winsRaw, received) = unparsedState.splitToSequence('|').map(::extractNums).toList()
                val wins = winsRaw.toSet()
                val matches = received.count { wins.contains(it) }
                val value = if (matches > 0) {
                    1 shl (matches - 1)
                } else {
                    0
                }
                Pair(cardId, value)
            }
            .toList()


    val result = cardValues
            .map { it.second }
            .sum()
    println("Card Vals: ${cardValues}")
    println("Input:     ${inputPrefix}")
    println("Result:    ${result}")
}

fun extractNums(nums: String) = digitRuns.findAll(nums)
        .map { it.value }
        .map { Integer.parseInt(it) }