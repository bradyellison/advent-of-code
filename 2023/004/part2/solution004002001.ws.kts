import java.io.File

val digitRuns = "[0-9]+".toRegex()

for (inputPrefix in sequenceOf("sample", "real")) {
    val cardMatches = File("""${inputPrefix}.input001.txt""")
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
                Pair(cardId, matches)
            }
            .toList()

    val cardCounts = IntArray(cardMatches.size)
    for ((cardId, matches) in cardMatches) {
        val startIndex = cardId - 1
        cardCounts[startIndex] += 1
        for (i in startIndex + 1 ..< startIndex + 1 + matches )
            cardCounts[i] += cardCounts[startIndex]
    }

    val values = cardCounts.asSequence()
            .toList()

    val result = values.sum()

    println("Card Mtch: ${cardMatches}")
    println("Values:    ${values}")
    println("Input:     ${inputPrefix}")
    println("Result:    ${result}")
}

fun extractNums(nums: String) = digitRuns.findAll(nums)
        .map { it.value }
        .map { Integer.parseInt(it) }