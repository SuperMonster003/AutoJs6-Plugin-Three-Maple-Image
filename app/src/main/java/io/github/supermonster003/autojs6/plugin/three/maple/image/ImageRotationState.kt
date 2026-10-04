package io.github.supermonster003.autojs6.plugin.three.maple.image

/** Pure quarter-turn normalization shared by the view and local unit tests. */
internal object ImageRotationState {

    const val ORIGINAL_QUARTER_TURNS = 0
    const val QUARTER_TURN_DEGREES = 90
    const val QUARTER_TURNS_PER_CIRCLE = 4

    fun clockwise(currentQuarterTurns: Int): Int = normalize(currentQuarterTurns + 1)

    fun normalize(quarterTurns: Int): Int =
        ((quarterTurns % QUARTER_TURNS_PER_CIRCLE) + QUARTER_TURNS_PER_CIRCLE) %
            QUARTER_TURNS_PER_CIRCLE

    fun degrees(quarterTurns: Int): Int =
        normalize(quarterTurns) * QUARTER_TURN_DEGREES

    fun swapsDimensions(quarterTurns: Int): Boolean = normalize(quarterTurns) % 2 != 0
}
