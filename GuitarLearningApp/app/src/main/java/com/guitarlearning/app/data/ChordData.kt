package com.guitarlearning.app.data

/**
 * Represents a single guitar chord.
 *
 * @param name Display name, e.g. "Am"
 * @param fullName Full name, e.g. "A minor"
 * @param frets Fret number for each string (low E → high e, 6 values).
 *   -1 = muted (X), 0 = open string.
 * @param fingers Which finger to use on each string (0 = none/open/muted, 1-4 = index→pinky).
 * @param startFret The fret the diagram starts at (1 for open chords).
 * @param barre If non-null, the fret that has a barre and the strings it covers.
 * @param category Chord type for filtering.
 */
data class Chord(
    val name: String,
    val fullName: String,
    val frets: List<Int>,       // 6 values: strings 6..1 (low E to high e)
    val fingers: List<Int>,     // 6 values matching frets
    val startFret: Int = 1,
    val barre: Barre? = null,
    val category: ChordCategory = ChordCategory.OPEN,
    val tip: String = ""
)

data class Barre(val fret: Int, val fromString: Int, val toString: Int)

enum class ChordCategory(val label: String) {
    OPEN("Open Chords"),
    BARRE("Barre Chords"),
    POWER("Power Chords"),
    SEVENTH("7th Chords"),
    MINOR("Minor Chords")
}

object ChordRepository {

    val all: List<Chord> = listOf(
        // ── Open Major Chords ──────────────────────────────────────────────
        Chord("E", "E Major",
            frets  = listOf(0, 2, 2, 1, 0, 0),
            fingers= listOf(0, 2, 3, 1, 0, 0),
            category = ChordCategory.OPEN,
            tip = "The most fundamental chord. Keep your fingers arched so open strings ring clearly."),
        Chord("A", "A Major",
            frets  = listOf(-1, 0, 2, 2, 2, 0),
            fingers= listOf(0,  0, 1, 2, 3, 0),
            category = ChordCategory.OPEN,
            tip = "Try to fit fingers 1-2-3 in the same fret slot on strings 2-3-4."),
        Chord("D", "D Major",
            frets  = listOf(-1, -1, 0, 2, 3, 2),
            fingers= listOf(0,   0, 0, 1, 3, 2),
            category = ChordCategory.OPEN,
            tip = "Only strum the top 4 strings. Keep your wrist relaxed."),
        Chord("G", "G Major",
            frets  = listOf(3, 2, 0, 0, 0, 3),
            fingers= listOf(2, 1, 0, 0, 0, 3),
            category = ChordCategory.OPEN,
            tip = "Classic chord. Stretch fingers 2 and 4 to cover the low and high strings."),
        Chord("C", "C Major",
            frets  = listOf(-1, 3, 2, 0, 1, 0),
            fingers= listOf(0,  3, 2, 0, 1, 0),
            category = ChordCategory.OPEN,
            tip = "A gateway chord. Focus on keeping string 2 (B) clean."),

        // ── Open Minor Chords ──────────────────────────────────────────────
        Chord("Am", "A Minor",
            frets  = listOf(-1, 0, 2, 2, 1, 0),
            fingers= listOf(0,  0, 2, 3, 1, 0),
            category = ChordCategory.MINOR,
            tip = "E-shape moved up one string. Avoid picking the low-E string."),
        Chord("Em", "E Minor",
            frets  = listOf(0, 2, 2, 0, 0, 0),
            fingers= listOf(0, 2, 3, 0, 0, 0),
            category = ChordCategory.MINOR,
            tip = "One of the easiest chords — only 2 fingers needed!"),
        Chord("Dm", "D Minor",
            frets  = listOf(-1, -1, 0, 2, 3, 1),
            fingers= listOf(0,   0, 0, 2, 3, 1),
            category = ChordCategory.MINOR,
            tip = "Notice how it's almost like D Major but finger 1 drops one fret."),

        // ── Barre Chords ───────────────────────────────────────────────────
        Chord("Bm", "B Minor",
            frets  = listOf(2, 2, 4, 4, 3, 2),
            fingers= listOf(1, 1, 3, 4, 2, 1),
            startFret = 2,
            barre  = Barre(fret = 2, fromString = 1, toString = 6),
            category = ChordCategory.BARRE,
            tip = "Your first barre chord! Press the index finger firmly across all 6 strings at fret 2."),
        Chord("F", "F Major",
            frets  = listOf(1, 1, 2, 3, 3, 1),
            fingers= listOf(1, 1, 2, 3, 4, 1),
            startFret = 1,
            barre  = Barre(fret = 1, fromString = 1, toString = 6),
            category = ChordCategory.BARRE,
            tip = "The notorious F chord! Keep your index knuckle slightly curved and use thumb pressure."),

        // ── Power Chords ───────────────────────────────────────────────────
        Chord("E5", "E Power Chord",
            frets  = listOf(0, 2, 2, -1, -1, -1),
            fingers= listOf(0, 1, 2, 0,  0,  0),
            category = ChordCategory.POWER,
            tip = "Two-finger power chord. Great for rock and metal rhythm playing."),
        Chord("A5", "A Power Chord",
            frets  = listOf(-1, 0, 2, 2, -1, -1),
            fingers= listOf(0,  0, 1, 2,  0,  0),
            category = ChordCategory.POWER,
            tip = "Move this shape up and down the neck to play any power chord on the A string."),
        Chord("D5", "D Power Chord",
            frets  = listOf(-1, -1, 0, 2, 2, -1),
            fingers= listOf(0,   0, 0, 1, 2,  0),
            category = ChordCategory.POWER,
            tip = "Power chords are neither major nor minor — perfect for distorted guitar."),

        // ── 7th Chords ─────────────────────────────────────────────────────
        Chord("G7", "G Dominant 7th",
            frets  = listOf(3, 2, 0, 0, 0, 1),
            fingers= listOf(3, 2, 0, 0, 0, 1),
            category = ChordCategory.SEVENTH,
            tip = "Adds a bluesy flavour to the G chord. Common in blues and jazz."),
        Chord("C7", "C Dominant 7th",
            frets  = listOf(-1, 3, 2, 3, 1, 0),
            fingers= listOf(0,  3, 2, 4, 1, 0),
            category = ChordCategory.SEVENTH,
            tip = "Often used as a transition to F major in blues progressions."),
        Chord("D7", "D Dominant 7th",
            frets  = listOf(-1, -1, 0, 2, 1, 2),
            fingers= listOf(0,   0, 0, 2, 1, 3),
            category = ChordCategory.SEVENTH,
            tip = "Great-sounding open chord used heavily in folk and blues."),
        Chord("E7", "E Dominant 7th",
            frets  = listOf(0, 2, 0, 1, 0, 0),
            fingers= listOf(0, 2, 0, 1, 0, 0),
            category = ChordCategory.SEVENTH,
            tip = "E Major with the 3rd string open — lifts one finger off the E chord."),
    )

    val categories: List<ChordCategory> = ChordCategory.entries

    fun byCategory(cat: ChordCategory): List<Chord> = all.filter { it.category == cat }
}
