import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

for (inputPrefix in sequenceOf("sample", "real")) {
    val start = System.nanoTime()
    val values = File("""${inputPrefix}.input001.txt""")
            .bufferedReader().lines().asSequence().map { it.trim() }.filter { it.isNotEmpty() }
            .map {
                it.splitToSequence(' ').take(2)
                        .map { it.trim() }
                        .zipWithNext { faces, bid -> Hand.fromFaces(faces) to bid.toInt() }
                        .single()
            }
            .sortedBy { it.first }
            .withIndex()
            .map { (index, handBid) -> (index + 1) to handBid.second }
            .map { (rank, bid) -> rank * bid }

    val result = values.sum()
    val runMillis = (System.nanoTime() - start) / (TimeUnit.MILLISECONDS.toNanos(1)).toDouble()

    println("Input:       ${inputPrefix}")
    println("Timing:      ${runMillis}")
    println("Result:      ${result}")
}

@JvmInline
value class Card(val rank: Int) : Comparable<Card> {
    companion object {
        private val ranks = listOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')
                .withIndex()
                .associate { (index, face) -> face to index }
        private val ranksReverse = ranks.entries.associateBy({ it.value }) { it.key }

        val joker = fromFace('J')

        fun fromFace(face: Char) = Card(ranks[face]!!)

        fun fromFaces(faces: String) =
                faces.asSequence()
                        .map { fromFace(it) }
                        .toList()
    }

    override fun compareTo(other: Card) = rank.compareTo(other.rank)

    override fun toString(): String {
        return "${ranksReverse[rank]!!}"
    }
}

data class Hand(val card1: Card, val card2: Card, val card3: Card, val card4: Card, val card5: Card) : Comparable<Hand> {
    companion object {
        fun fromFaces(faces: String) = Card.fromFaces(faces)
                .let { (card1, card2, card3, card4, card5) -> Hand(card1, card2, card3, card4, card5) }
    }

    val cardsByRank = sequenceOf(card1, card2, card3, card4, card5).sortedBy { it.rank }.toList()
    val cardOccurrence = cardsByRank
            .partition { it == Card.joker }
            .let { (jokers, other) ->
                if (other.isEmpty()) {
                    mapOf(Card.joker to jokers.size)
                } else {
                    val otherOccurrences = other.groupingBy { it }.eachCount().toMutableMap()
                    val maxOccurrence = otherOccurrences.entries.maxBy { it.value }
                    maxOccurrence.setValue(maxOccurrence.value + jokers.size)
                    otherOccurrences
                }
            }


    val fiveOfAKind = cardOccurrence.size == 1
    val fourOfAKind = cardOccurrence.size == 2 && cardOccurrence.any { it.value == 4 }
    val fullHouse = cardOccurrence.size == 2 && cardOccurrence.any { it.value == 3 } && cardOccurrence.any { it.value == 2 }
    val threeOfAKind = cardOccurrence.size == 3 && cardOccurrence.any { it.value == 3 }
    val twoPair = cardOccurrence.size == 3 && cardOccurrence.count { it.value == 2 } == 2
    val onePair = cardOccurrence.size == 4 && cardOccurrence.count { it.value == 2 } == 1
    val highCard = cardOccurrence.size == 5

    val rank = when {
        fiveOfAKind -> 0
        fourOfAKind -> 1
        fullHouse -> 2
        threeOfAKind -> 3
        twoPair -> 4
        onePair -> 5
        highCard -> 6
        else -> throw IllegalStateException()
    }

    override fun compareTo(other: Hand): Int {
        return when {
            this == other -> 0
            rank < other.rank -> 1
            rank > other.rank -> -1
            // If ranks are equal, compare cards ranks in-order
            card1 != other.card1 -> card1.compareTo(other.card1)
            card2 != other.card2 -> card2.compareTo(other.card2)
            card3 != other.card3 -> card3.compareTo(other.card3)
            card4 != other.card4 -> card4.compareTo(other.card4)
            card5 != other.card5 -> card5.compareTo(other.card5)
            else -> 0
        }
    }

    override fun toString(): String {
        return "$card1$card2$card3$card4$card5 (${cardsByRank.joinToString("") { it.toString() }}, ${cardOccurrence})"
    }
}