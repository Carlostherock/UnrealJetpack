package com.guitarlearning.app.data

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val level: LessonLevel,
    val durationMinutes: Int,
    val sections: List<LessonSection>
)

data class LessonSection(
    val heading: String,
    val body: String
)

enum class LessonLevel(val label: String, val color: Long) {
    BEGINNER("Beginner", 0xFF4CAF50),
    INTERMEDIATE("Intermediate", 0xFFFF9800),
    ADVANCED("Advanced", 0xFFF44336)
}

object LessonRepository {

    val lessons: List<Lesson> = listOf(

        // ── Beginner ───────────────────────────────────────────────────────
        Lesson(
            id = 1,
            title = "Parts of the Guitar",
            subtitle = "Get to know your instrument",
            level = LessonLevel.BEGINNER,
            durationMinutes = 5,
            sections = listOf(
                LessonSection("Anatomy", """
The guitar has three main parts:

• Headstock — holds the tuning pegs (machine heads) that you turn to tune the strings.
• Neck — the long piece where you press the strings. The metal strips are called FRETS.
• Body — the large part that amplifies the vibration (acoustic) or carries the pickups (electric).

The 6 strings are numbered 1 (thinnest, high e) to 6 (thickest, low E).
Standard tuning from low to high: E – A – D – G – B – e
                """.trimIndent()),
                LessonSection("How sound is made", """
When you pluck a string it vibrates. Pressing the string behind a fret shortens its vibrating length, making the pitch higher. The further up the neck you go, the higher the note.
                """.trimIndent()),
                LessonSection("Holding the guitar", """
Sit comfortably with the guitar resting on your right thigh (if right-handed). The neck should point slightly upward at about a 45° angle. Keep your back straight.

Fretting hand: Thumb lightly behind the neck, fingers curved like holding a ball.
Picking hand: Rest your forearm on the body edge; strum across the sound hole.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 2,
            title = "Reading Chord Diagrams",
            subtitle = "Understand the chord chart",
            level = LessonLevel.BEGINNER,
            durationMinutes = 8,
            sections = listOf(
                LessonSection("The Grid", """
A chord diagram is a grid that represents the guitar fretboard viewed from the front.

  ← Low E  High e →
  6  5  4  3  2  1   ← string numbers
  |  |  |  |  |  |
——+——+——+——+——+——   ← nut (top = fret 1)
  |  |  |  |  |  |
——+——+——+——+——+——   ← fret 1
  |  |  |  |  |  |
——+——+——+——+——+——   ← fret 2

Dots show where to press your fingers.
                """.trimIndent()),
                LessonSection("Symbols", """
• X above a string  →  Mute / don't play that string.
• O above a string  →  Play the string open (unfretted).
• Numbers inside dots  →  Which finger to use (1=index, 2=middle, 3=ring, 4=pinky).
• A thick bar across strings  →  Barre chord — one finger presses multiple strings.
                """.trimIndent()),
                LessonSection("Try it!", """
Open the Chords section of this app and look at the Em chord (E minor).

You'll see two dots on the 5th and 6th strings at fret 2 — that's all! Put your middle and ring fingers there and strum all 6 strings.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 3,
            title = "Your First 3 Chords",
            subtitle = "Em, Am, and D",
            level = LessonLevel.BEGINNER,
            durationMinutes = 15,
            sections = listOf(
                LessonSection("Why these three?", """
Em, Am, and D are among the easiest open chords. Together they let you play dozens of songs. Mastering transitions between them is your first real milestone.
                """.trimIndent()),
                LessonSection("E Minor (Em)", """
Finger placement:
  String 5 (A), fret 2  →  Middle finger (2)
  String 4 (D), fret 2  →  Ring finger   (3)
  All other strings: open

Strum all 6 strings. It should sound full and sad. Practice pressing and releasing until every note rings cleanly.
                """.trimIndent()),
                LessonSection("A Minor (Am)", """
Finger placement:
  String 4 (D), fret 2  →  Middle finger (2)
  String 3 (G), fret 2  →  Ring finger   (3)
  String 2 (B), fret 1  →  Index finger  (1)
  Strings 1 and 5: open.  String 6: muted (X)

Strum strings 5 through 1.
                """.trimIndent()),
                LessonSection("D Major", """
Finger placement:
  String 3 (G), fret 2  →  Index finger  (1)
  String 1 (e), fret 2  →  Middle finger (2)
  String 2 (B), fret 3  →  Ring finger   (3)
  Strings 4 open.  Strings 5-6: muted (X)

Only strum strings 4 through 1.
                """.trimIndent()),
                LessonSection("Practice routine", """
1. Play Em for 4 counts (4 strums).
2. Switch to Am — aim for under 2 seconds to change.
3. Play Am for 4 counts.
4. Switch to D.
5. Repeat.

Do this for 10 minutes every day. Speed comes with repetition!
                """.trimIndent())
            )
        ),

        Lesson(
            id = 4,
            title = "Basic Strumming Patterns",
            subtitle = "Develop your rhythm",
            level = LessonLevel.BEGINNER,
            durationMinutes = 12,
            sections = listOf(
                LessonSection("Downstrokes first", """
Hold an Em chord. Strum downward across the strings in a steady pulse:

  ↓  ↓  ↓  ↓
  1  2  3  4

Count aloud. Keep the tempo even — slow and steady beats fast and sloppy.
                """.trimIndent()),
                LessonSection("Adding upstrokes", """
Once downstrokes feel natural, add upstrokes on the 'and' beats:

  ↓    ↑    ↓    ↑    ↓    ↑    ↓    ↑
  1   and   2   and   3   and   4   and

Your hand should move like a pendulum. Even when you don't hit the strings on the 'and', keep the motion going.
                """.trimIndent()),
                LessonSection("The most common pattern", """
This pattern works for hundreds of pop/rock songs:

  ↓  ↓  ↑  ↓  ↑
  1  2 +  3 +

Miss strings 2 & 4 on the first and third beats, and you'll land naturally on this feel. Slow it right down with a metronome first.
                """.trimIndent()),
                LessonSection("Metronome tip", """
Start at 60 BPM. When you can play the pattern cleanly 4 times in a row without mistakes, bump up the tempo by 5 BPM. This is called 'chunking' and it's the fastest way to build speed.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 5,
            title = "The CAGED System",
            subtitle = "Unlock the whole fretboard",
            level = LessonLevel.BEGINNER,
            durationMinutes = 20,
            sections = listOf(
                LessonSection("What is CAGED?", """
CAGED stands for the five open chord shapes:
  C – A – G – E – D

Every major chord on the guitar can be played using one of these five shapes, moved up the neck with a barre. Learning all five lets you play any chord anywhere on the fretboard.
                """.trimIndent()),
                LessonSection("The E shape", """
You already know E major. Now barre across all strings at fret 2 and you have F# major. Barre at fret 3 → G major. The shape travels up the neck!

  Open E = E Major
  Barre fret 1 = F Major
  Barre fret 2 = F# Major
  Barre fret 3 = G Major  …and so on.
                """.trimIndent()),
                LessonSection("The A shape", """
Similarly, barre the A-shape:
  Open A = A Major
  Barre fret 2 = B Major
  Barre fret 3 = C Major  …

The tricky part: you need to barre 5 strings while your other fingers form the A shape. Many players use the ring finger to barre strings 2-3-4 at the same fret instead.
                """.trimIndent()),
                LessonSection("Practice goal", """
This week: take one chord (try G major) and find it as both an E-shape barre and a D-shape barre chord somewhere higher on the neck. Look it up, play it, and see that it's the same notes!
                """.trimIndent())
            )
        ),

        // ── Intermediate ───────────────────────────────────────────────────
        Lesson(
            id = 6,
            title = "Pentatonic Scale — Position 1",
            subtitle = "The foundation of lead guitar",
            level = LessonLevel.INTERMEDIATE,
            durationMinutes = 20,
            sections = listOf(
                LessonSection("Why pentatonic?", """
The minor pentatonic scale is the most used scale in rock, blues, and pop lead guitar. It has only 5 notes, which makes it easy to learn and hard to make sound bad over most chord progressions.
                """.trimIndent()),
                LessonSection("A minor pentatonic — Position 1", """
Root note at the 5th fret of the low-E string (A).

String 6: frets 5, 8
String 5: frets 5, 7
String 4: frets 5, 7
String 3: frets 5, 7
String 2: frets 5, 8
String 1: frets 5, 8

Play each note slowly, up then down. Use alternate picking (down-up-down-up).
                """.trimIndent()),
                LessonSection("Pattern tip", """
Notice the shape: it's always either 3 frets apart (minor 3rd) or 2 frets apart (whole step). This gives the scale its characteristic 'box' shape on the fretboard.

The shape doesn't change — only the root fret moves. Slide it to fret 7 and you're playing B minor pentatonic.
                """.trimIndent()),
                LessonSection("Lick to try", """
A classic blues lick in Am:

  String 1: 8–5  (slide or pull-off)
  String 2: 8–5
  String 3: 7–5
  String 4: 5–7  (hammer-on)

Play it slowly first. Add vibrato on the last note for expression.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 7,
            title = "Barre Chords Masterclass",
            subtitle = "Conquer the F chord and beyond",
            level = LessonLevel.INTERMEDIATE,
            durationMinutes = 25,
            sections = listOf(
                LessonSection("Why barre chords matter", """
Barre chords are movable — learn one shape and you can play 12 different chords just by sliding up the neck. They're also your gateway to playing songs in any key.
                """.trimIndent()),
                LessonSection("Getting the barre right", """
Common problems and fixes:

1. Buzzing strings → Roll your index finger slightly toward the nut side. The bony ridge on the side of your finger presses cleaner than the flesh.

2. Muted strings → Check that no finger-tip is touching an adjacent string it shouldn't.

3. Wrist pain → Bring your elbow closer to your body. Your thumb should sit behind the middle finger of your fretting hand, not poking over the top of the neck.
                """.trimIndent()),
                LessonSection("Building barre strength", """
Exercise: Place a full barre at fret 5. Strum all 6 strings once. Lift your finger completely. Re-barre. Repeat 20 times. This builds independence between barre pressure and the rest of your hand.

Start at a high fret (7-9) where the frets are closer together and tension is lower, then work your way down toward fret 1 (the hardest spot).
                """.trimIndent()),
                LessonSection("E-shape and A-shape", """
Two main barre families:

E-shape (6-string root):  Based on open E. Root note is on string 6.
  Barre fret 1 = F major, fret 3 = G major, fret 5 = A major…

A-shape (5-string root):  Based on open A. Root note is on string 5.
  Barre fret 2 = B major, fret 3 = C major, fret 5 = D major…

Know these two and you can play any major chord on the guitar.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 8,
            title = "12-Bar Blues",
            subtitle = "Play the most important chord progression",
            level = LessonLevel.INTERMEDIATE,
            durationMinutes = 18,
            sections = listOf(
                LessonSection("The progression", """
The 12-bar blues in the key of A uses three chords: A7, D7, and E7.

Bar:   1    2    3    4    5    6    7    8    9   10   11   12
Chord: A7 | A7 | A7 | A7 | D7 | D7 | A7 | A7 | E7 | D7 | A7 | E7 |

That last E7 in bar 12 is called the 'turnaround' — it leads you back to bar 1.
                """.trimIndent()),
                LessonSection("Quick-change variation", """
A common variation plays D7 in bar 2 instead of A7:

Bar 1 = A7, Bar 2 = D7 (quick change!), Bars 3-4 = A7…

Listen to 'Johnny B. Goode' or 'Hound Dog' — both use this quick-change pattern.
                """.trimIndent()),
                LessonSection("Blues shuffle rhythm", """
Instead of straight strumming, blues guitarists play a shuffle rhythm based on 6th intervals:

On string 6 (low E open = E):
  Fret 2 + open, Fret 4 + open, Fret 2 + open…

This alternating two-note pattern gives the music its 'groove'. Try it on the A string too.
                """.trimIndent())
            )
        ),

        // ── Advanced ───────────────────────────────────────────────────────
        Lesson(
            id = 9,
            title = "Fingerpicking Fundamentals",
            subtitle = "Develop independence between fingers",
            level = LessonLevel.ADVANCED,
            durationMinutes = 30,
            sections = listOf(
                LessonSection("Hand position", """
Rest your thumb on string 6 (or 5 for A-root patterns). Assign:
  p (pulgar)  = thumb  → strings 4, 5, 6
  i (indice)  = index  → string 3
  m (medio)   = middle → string 2
  a (anular)  = ring   → string 1

Keep fingers slightly curved. Let them pluck through the string, not scrape it.
                """.trimIndent()),
                LessonSection("Travis picking pattern", """
Hold a C major chord. The thumb alternates bass notes while fingers fill in melody.

Beat 1: p (string 5) + a (string 1) together
Beat 2: i (string 3)
Beat 3: p (string 4) + m (string 2) together
Beat 4: i (string 3)

Repeat. Let every note sustain into the next — don't lift fingers prematurely.
                """.trimIndent()),
                LessonSection("Independence exercise", """
The trickiest part is making the thumb independent of the fingers. Practice:

1. Tap your thumb on a table in a steady beat.
2. While tapping, tap your index finger on a different rhythm.
3. Gradually make the rhythms more complex.

Transfer this coordination to the guitar. True independence takes months — be patient.
                """.trimIndent())
            )
        ),

        Lesson(
            id = 10,
            title = "Music Theory for Guitarists",
            subtitle = "Understand scales, keys, and harmony",
            level = LessonLevel.ADVANCED,
            durationMinutes = 35,
            sections = listOf(
                LessonSection("Intervals", """
An interval is the distance between two notes.
  Unison (0 semitones), Minor 2nd (1), Major 2nd (2), Minor 3rd (3), Major 3rd (4),
  Perfect 4th (5), Tritone (6), Perfect 5th (7), Minor 6th (8), Major 6th (9),
  Minor 7th (10), Major 7th (11), Octave (12).

On the guitar, one fret = one semitone.
                """.trimIndent()),
                LessonSection("Major scale formula", """
Major scale = W W H W W W H  (W=whole step / 2 frets, H=half step / 1 fret)

Starting on A (fret 5, string 6):
  A  B  C# D  E  F# G# A
  5  7  9  10 12 14 16 17

Every other scale (minor, pentatonic, modes) is derived from this by lowering or removing notes.
                """.trimIndent()),
                LessonSection("Keys and the Circle of Fifths", """
The Circle of Fifths shows which chords belong to a key:

Key of G major: G Am Bm C D Em F#dim

Formula for any major key: I ii iii IV V vi vii°
  • I, IV, V are major
  • ii, iii, vi are minor
  • vii° is diminished

The V chord has a strong pull back to I — that's tension and resolution, the engine of all harmony.
                """.trimIndent()),
                LessonSection("Modes overview", """
Modes are rotations of the major scale starting on different degrees:
  1st  →  Ionian   (= major scale, bright)
  2nd  →  Dorian   (minor, jazzy — used in 'Oye Como Va')
  3rd  →  Phrygian (minor, Spanish/metal feel)
  4th  →  Lydian   (major, dreamy — John Williams loves it)
  5th  →  Mixolydian (major but bluesy — most rock)
  6th  →  Aeolian  (= natural minor scale)
  7th  →  Locrian  (diminished, rarely used)

Don't memorise all at once. Start with Dorian and Mixolydian — they're used constantly.
                """.trimIndent())
            )
        )
    )
}
