
val seq = listOf(10, 20, 30)

for (target in sequenceOf(0, 5, 10, 15, 20, 25, 30, 35)) {
    val raw = seq.binarySearch(target)
    val alt = if (raw < 0) {
        -(raw + 2)
    } else {
        raw
    }
    println("$target -> ($raw, $alt)")
}
