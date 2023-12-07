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
                        .zipWithNext { faces, bid -> Hand.bestFromFaces(faces) to bid.toInt() }
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

data class Card(val rank: Int, val isJoker: Boolean = false) : Comparable<Card> {
    companion object {
        val faces = listOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')
        private val ranks = faces
                .withIndex()
                .associate { (index, face) -> face to index }
        private val ranksReverse = ranks.entries.associateBy({ it.value }) { it.key }

        fun fromFace(face: Char) = Card(ranks[face]!!)

        fun fromFaces(faces: String) =
                faces.asSequence()
                        .map { fromFace(it) }
                        .toList()
    }

    fun withJoker() = Card(rank, true)

    override fun compareTo(other: Card) = when {
        isJoker && other.isJoker -> 0
        isJoker -> -1
        other.isJoker -> 1
        else -> rank.compareTo(other.rank)
    }

    override fun toString(): String {
        return "${ranksReverse[rank]!!}"
    }
}

data class Hand(val card1: Card, val card2: Card, val card3: Card, val card4: Card, val card5: Card) : Comparable<Hand> {
    companion object {
        private val alternativeFaces = Card.faces.filterNot { it == 'J' }.toList()
        fun bestFromFaces(faces: String) =
                if (faces.contains('J')) {
                    alternativeFaces
                            //                        .map { faces.replace('J', it) }
                            .map { alternative ->
                                faces.asSequence()
                                        .map { face ->
                                            when (face) {
                                                'J' -> Card.fromFace(alternative).withJoker()
                                                else -> Card.fromFace(face)
                                            }
                                        }
                                        .toList()
                                        .let { (card1, card2, card3, card4, card5) -> Hand(card1, card2, card3, card4, card5) }
                                        .let {
                                            //println("trying: $faces as $it")
                                            it
                                        }
                            }
                            .maxBy { it }
                            .let {
                                println("## from $faces selected: $it")
                                it
                            }
                } else {
                    Card.fromFaces(faces)
                            .let { (card1, card2, card3, card4, card5) -> Hand(card1, card2, card3, card4, card5) }
                }
    }

    val cardsByRank = sequenceOf(card1, card2, card3, card4, card5).sortedBy { it.rank }.toList()
    val cardOccurrence = cardsByRank.groupingBy { it.rank }.eachCount()

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
            rank < other.rank -> 1
            rank > other.rank -> -1
            // If ranks are equal, compare cards ranks in-order
            // This is wasteful, we shouldn't compute compareTo twice
            card1.compareTo(other.card1) != 0 -> card1.compareTo(other.card1)
            card2.compareTo(other.card2) != 0 -> card2.compareTo(other.card2)
            card3.compareTo(other.card3) != 0 -> card3.compareTo(other.card3)
            card4.compareTo(other.card4) != 0 -> card4.compareTo(other.card4)
            card5.compareTo(other.card5) != 0 -> card5.compareTo(other.card5)
            else -> 0
        }
    }

    override fun toString(): String {
        return "$card1$card2$card3$card4$card5~$rank (${cardsByRank.joinToString("") { it.toString() }}, ${cardOccurrence})"
    }
}
