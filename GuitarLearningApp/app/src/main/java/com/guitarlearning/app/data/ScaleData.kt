package com.guitarlearning.app.data

/**
 * Represents one position/box of a scale on the fretboard.
 *
 * @param notes List of (string, fret) pairs. String 1 = high e, String 6 = low E.
 * @param rootFrets The fret positions on each string that are the root note of the scale.
 */
data class Scale(
    val name: String,
    val type: ScaleType,
    val description: String,
    val rootNote: String,
    val startFret: Int,
    val notes: List<Pair<Int, Int>>,   // (stringNumber 1..6, fret)
    val rootFrets: List<Pair<Int, Int>> // (stringNumber, fret) of root notes
)

enum class ScaleType(val label: String) {
    PENTATONIC_MINOR("Minor Pentatonic"),
    PENTATONIC_MAJOR("Major Pentatonic"),
    NATURAL_MINOR("Natural Minor (Aeolian)"),
    MAJOR("Major (Ionian)"),
    BLUES("Blues Scale"),
    DORIAN("Dorian Mode")
}

object ScaleRepository {

    val scales: List<Scale> = listOf(

        // Am Pentatonic — Position 1 (box shape at fret 5)
        Scale(
            name = "Am Pentatonic – Box 1",
            type = ScaleType.PENTATONIC_MINOR,
            description = "The most used scale in rock and blues. Play this over any Am, C, or G chord progression.",
            rootNote = "A",
            startFret = 4,
            notes = listOf(
                6 to 5, 6 to 8,
                5 to 5, 5 to 7,
                4 to 5, 4 to 7,
                3 to 5, 3 to 7,
                2 to 5, 2 to 8,
                1 to 5, 1 to 8
            ),
            rootFrets = listOf(6 to 5, 4 to 5, 1 to 5)
        ),

        // C Major Pentatonic — Position 1
        Scale(
            name = "C Major Pentatonic – Box 1",
            type = ScaleType.PENTATONIC_MAJOR,
            description = "The happy, country-flavoured pentatonic. Great over C, G, and F chord progressions.",
            rootNote = "C",
            startFret = 4,
            notes = listOf(
                6 to 8, 6 to 10,
                5 to 7, 5 to 10,
                4 to 7, 4 to 9,
                3 to 7, 3 to 9,
                2 to 8, 2 to 10,
                1 to 7, 1 to 10
            ),
            rootFrets = listOf(5 to 10, 3 to 9, 1 to 8)
        ),

        // A Blues Scale
        Scale(
            name = "A Blues Scale",
            type = ScaleType.BLUES,
            description = "Minor pentatonic + the 'blue note' (b5). The extra note adds tension and expression.",
            rootNote = "A",
            startFret = 4,
            notes = listOf(
                6 to 5, 6 to 8,
                5 to 5, 5 to 6, 5 to 7,
                4 to 5, 4 to 6, 4 to 7,
                3 to 5, 3 to 7,
                2 to 5, 2 to 8,
                1 to 5, 1 to 8
            ),
            rootFrets = listOf(6 to 5, 4 to 5, 1 to 5)
        ),

        // A Natural Minor (Aeolian)
        Scale(
            name = "A Natural Minor",
            type = ScaleType.NATURAL_MINOR,
            description = "The full 7-note minor scale. Forms the basis of most minor key songs.",
            rootNote = "A",
            startFret = 4,
            notes = listOf(
                6 to 5, 6 to 7, 6 to 8,
                5 to 5, 5 to 7,
                4 to 5, 4 to 7,
                3 to 5, 3 to 6, 3 to 7,
                2 to 5, 2 to 6, 2 to 8,
                1 to 5, 1 to 7, 1 to 8
            ),
            rootFrets = listOf(6 to 5, 4 to 5, 2 to 5)
        ),

        // G Major
        Scale(
            name = "G Major Scale",
            type = ScaleType.MAJOR,
            description = "The most commonly taught major scale. Gives the classic 'do-re-mi' sound.",
            rootNote = "G",
            startFret = 2,
            notes = listOf(
                6 to 3, 6 to 5,
                5 to 2, 5 to 3, 5 to 5,
                4 to 2, 4 to 4, 4 to 5,
                3 to 2, 3 to 4, 3 to 5,
                2 to 3, 2 to 5,
                1 to 2, 1 to 3, 1 to 5
            ),
            rootFrets = listOf(6 to 3, 4 to 5, 2 to 3)
        ),

        // A Dorian
        Scale(
            name = "A Dorian",
            type = ScaleType.DORIAN,
            description = "Minor-flavoured mode with a raised 6th. Used in jazz, funk, and Santana-style rock.",
            rootNote = "A",
            startFret = 4,
            notes = listOf(
                6 to 5, 6 to 7, 6 to 8,
                5 to 5, 5 to 7,
                4 to 5, 4 to 7,
                3 to 4, 3 to 5, 3 to 7,
                2 to 5, 2 to 7,
                1 to 5, 1 to 7, 1 to 8
            ),
            rootFrets = listOf(6 to 5, 4 to 5, 2 to 5)
        )
    )
}
