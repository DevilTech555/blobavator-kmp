package com.navbyte.blobavatar.core

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.PI
import kotlin.math.sign
import kotlin.math.sin

/**
 * Deterministic elapsed-time motion for generation-2 blobatars.
 *
 * Kotlin Multiplatform port of `packages/blobatar/src/motion.ts` and `motion.dart` at blobatar 2.4.0.
 */
data class MotionSeeds(
    val phase: Int,
    val bob: Int,
    val blink: Int,
    val blinkPhase: Int,
    val saccade: Int,
    val saccadePhase: Int,
    val lookX: Double,
    val lookY: Double,
    val lookMagnitudeX: Double,
    val lookMagnitudeY: Double
)

data class MotionWrap(
    val magnitudeX: Double,
    val side: Double,
    val scaleY: Double,
    val rotation: Double
)

data class MotionFrame(
    val shake: Pair<Double, Double>,
    val breathe: Pair<Double, Double>,
    val bob: Double,
    val saccade: Pair<Double, Double>,
    val thinkingPhase: Double,
    val blink: Double,
    val wrap: MotionWrap
)

private fun round2(value: Double): Double =
    jsRound(value * 100.0) / 100.0

fun motionSeeds(traits: Traits): MotionSeeds {
    val blink = jsRound(traits.numIn("motion.blink", 3500.0, 6500.0)).toInt()
    val saccade = jsRound(traits.numIn("motion.saccade", 4200.0, 7600.0)).toInt()
    val lookX = round2(traits.numIn("motion.lookX", 1.0, 2.2))
    val lookY = round2(traits.numIn("motion.lookY", 0.8, 1.7))

    return MotionSeeds(
        phase = jsRound(traits.numIn("motion.phase", 0.0, 2800.0)).toInt(),
        bob = jsRound(traits.numIn("motion.bob", 0.0, 3400.0)).toInt(),
        blink = blink,
        blinkPhase = jsRound(traits.numIn("motion.blinkPhase", 0.0, blink.toDouble())).toInt(),
        saccade = saccade,
        saccadePhase = jsRound(traits.numIn("motion.saccadePhase", 0.0, saccade.toDouble())).toInt(),
        lookX = lookX * (if (traits.boolIn("motion.lookXFlip")) -1.0 else 1.0),
        lookY = lookY * (if (traits.boolIn("motion.lookYFlip")) -1.0 else 1.0),
        lookMagnitudeX = lookX,
        lookMagnitudeY = lookY
    )
}

fun motionSeedsFor(
    name: String,
    normalize: Boolean = true,
    traits: TraitOverrides? = null
): MotionSeeds = motionSeeds(traitsFor(name, normalize = normalize, overrides = traits))

private fun cycle(time: Double, phase: Double, period: Double): Double {
    val value = (time + phase) / period
    return value - floor(value)
}

private fun alternate(time: Double, phase: Double, period: Double): Double {
    val value = (time + phase) / period
    val iteration = floor(value).toLong()
    val fraction = value - floor(value)
    return if (iteration % 2L != 0L) 1.0 - fraction else fraction
}

private fun stops(value: Double, table: Array<DoubleArray>, column: Int): Double {
    for (index in table.size - 1 downTo 0) {
        val row = table[index]
        if (value < row[0]) continue
        if (index + 1 == table.size) return row[column]
        val next = table[index + 1]
        val span = next[0] - row[0]
        if (span <= 0.0) return row[column]
        return row[column] + (next[column] - row[column]) * ((value - row[0]) / span)
    }
    return table[0][column]
}

private val saccadeStops = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.15, 0.0, 0.0),
    doubleArrayOf(0.165, -0.8, -0.9),
    doubleArrayOf(0.31, -0.8, -0.9),
    doubleArrayOf(0.325, 1.0, 0.1),
    doubleArrayOf(0.47, 1.0, 0.1),
    doubleArrayOf(0.485, -0.15, 0.85),
    doubleArrayOf(0.63, -0.15, 0.85),
    doubleArrayOf(0.645, 0.75, -0.8),
    doubleArrayOf(0.79, 0.75, -0.8),
    doubleArrayOf(0.805, -1.0, -0.15),
    doubleArrayOf(0.985, -1.0, -0.15),
    doubleArrayOf(1.0, 0.0, 0.0)
)

private val wrapStops = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.15, 0.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.165, -0.0176, 0.008, -0.027, 0.648),
    doubleArrayOf(0.31, -0.0176, 0.008, -0.027, 0.648),
    doubleArrayOf(0.325, -0.022, -0.01, -0.003, 0.09),
    doubleArrayOf(0.47, -0.022, -0.01, -0.003, 0.09),
    doubleArrayOf(0.485, -0.0033, 0.0015, -0.0255, -0.115),
    doubleArrayOf(0.63, -0.0033, 0.0015, -0.0255, -0.115),
    doubleArrayOf(0.645, -0.0165, -0.0075, -0.024, -0.54),
    doubleArrayOf(0.79, -0.0165, -0.0075, -0.024, -0.54),
    doubleArrayOf(0.805, -0.022, 0.01, -0.0045, 0.135),
    doubleArrayOf(0.985, -0.022, 0.01, -0.0045, 0.135),
    doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0)
)

private val shakeStops = arrayOf(
    doubleArrayOf(0.0, 0.62, -0.34),
    doubleArrayOf(0.25, -0.7, 0.22),
    doubleArrayOf(0.5, 0.38, 0.66),
    doubleArrayOf(0.75, -0.44, -0.6),
    doubleArrayOf(1.0, 0.62, -0.34)
)

const val breatheMilliseconds: Double = 2800.0
const val bobMilliseconds: Double = 3400.0
const val thinkingMilliseconds: Double = 900.0
const val shakeMilliseconds: Double = 112.0
const val expressionEnterMilliseconds: Int = 300
const val expressionExitMilliseconds: Int = 400
const val ambientRampMilliseconds: Int = 400
const val hoverEnterMilliseconds: Int = 220
const val hoverExitMilliseconds: Int = 160

fun cubicBezier(
    x: Double,
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double
): Double {
    val cx = 3.0 * x1
    val bx = 3.0 * (x2 - x1) - cx
    val ax = 1.0 - cx - bx
    val cy = 3.0 * y1
    val by = 3.0 * (y2 - y1) - cy
    val ay = 1.0 - cy - by
    var parameter = x
    for (iteration in 0 until 8) {
        val error = ((ax * parameter + bx) * parameter + cx) * parameter - x
        if (abs(error) < 1e-5) break
        val derivative = (3.0 * ax * parameter + 2.0 * bx) * parameter + cx
        if (abs(derivative) < 1e-6) break
        parameter -= error / derivative
    }
    return ((ay * parameter + by) * parameter + cy) * parameter
}

fun easeInOut(value: Double): Double = cubicBezier(value, 0.42, 0.0, 0.58, 1.0)
fun easeIn(value: Double): Double = cubicBezier(value, 0.42, 0.0, 1.0, 1.0)
fun easeOut(value: Double): Double = cubicBezier(value, 0.0, 0.0, 0.58, 1.0)
fun expressionEnterEase(value: Double): Double = cubicBezier(value, 0.45, 0.05, 0.5, 1.0)
fun hoverEase(value: Double): Double = cubicBezier(value, 0.23, 1.0, 0.32, 1.0)

fun motionAt(
    seeds: MotionSeeds,
    elapsedMilliseconds: Double,
    amplitude: Double,
    shake: Double = 0.0
): MotionFrame {
    val breathe = easeInOut(
        alternate(elapsedMilliseconds, seeds.phase.toDouble(), breatheMilliseconds)
    )
    val bob = easeInOut(
        alternate(elapsedMilliseconds, seeds.bob.toDouble(), bobMilliseconds)
    )
    val saccade = cycle(
        elapsedMilliseconds,
        seeds.saccadePhase.toDouble(),
        seeds.saccade.toDouble()
    )
    val shakeCycle = cycle(elapsedMilliseconds, 0.0, shakeMilliseconds)
    val thinkingCycle = cycle(elapsedMilliseconds, 0.0, thinkingMilliseconds)
    val thinkingPhase = if (thinkingCycle < 0.5) {
        1.0 - 2.0 * easeInOut(thinkingCycle * 2.0)
    } else {
        -1.0 + 2.0 * easeInOut(thinkingCycle * 2.0 - 1.0)
    }
    val blinkCycle = cycle(
        elapsedMilliseconds,
        seeds.blinkPhase.toDouble(),
        seeds.blink.toDouble()
    )
    val blink = when {
        blinkCycle < 0.972 -> 1.0
        blinkCycle < 0.986 -> 1.0 - 0.92 * amplitude * easeIn((blinkCycle - 0.972) / 0.014)
        else -> 1.0 - 0.92 * amplitude * (1.0 - easeOut((blinkCycle - 0.986) / 0.014))
    }

    return MotionFrame(
        shake = Pair(
            stops(shakeCycle, shakeStops, 1) * shake,
            stops(shakeCycle, shakeStops, 2) * shake
        ),
        breathe = Pair(
            1.0 + 0.022 * amplitude * breathe,
            1.0 - 0.018 * amplitude * breathe
        ),
        bob = -1.1 * amplitude * bob,
        saccade = Pair(
            stops(saccade, saccadeStops, 1) * seeds.lookX * amplitude,
            stops(saccade, saccadeStops, 2) * seeds.lookY * amplitude
        ),
        thinkingPhase = thinkingPhase,
        blink = blink,
        wrap = MotionWrap(
            magnitudeX = stops(saccade, wrapStops, 1) * seeds.lookMagnitudeX * amplitude,
            side = stops(saccade, wrapStops, 2) * seeds.lookX * amplitude,
            scaleY = stops(saccade, wrapStops, 3) * seeds.lookMagnitudeY * amplitude,
            rotation = stops(saccade, wrapStops, 4) * seeds.lookX * seeds.lookY * amplitude
        )
    )
}

fun lerpPose(from: Pose?, to: Pose?, progress: Double): Pose {
    val a = from ?: identityPose
    val b = to ?: identityPose
    fun lerp(start: Double, end: Double): Double = start * (1.0 - progress) + end * progress
    return Pose(
        esx = lerp(a.esx, b.esx),
        esy = lerp(a.esy, b.esy),
        tilt = lerp(a.tilt, b.tilt),
        edy = lerp(a.edy, b.edy),
        edx = lerp(a.edx, b.edx),
        esx2 = lerp(a.esx2, b.esx2),
        esy2 = lerp(a.esy2, b.esy2),
        tilt2 = lerp(a.tilt2, b.tilt2),
        edy2 = lerp(a.edy2, b.edy2),
        lock = lerp(a.lock, b.lock),
        heat = lerp(a.heat, b.heat),
        shake = lerp(a.shake, b.shake),
        rock = lerp(a.rock, b.rock),
        bdy = lerp(a.bdy, b.bdy)
    )
}

/**
 * Procedural motion dynamics specific to individual emotional expressions.
 */
data class EmotionDynamics(
    val dx: Double = 0.0,
    val dy: Double = 0.0,
    val scaleX: Double = 1.0,
    val scaleY: Double = 1.0,
    val rotation: Double = 0.0
)

val identityEmotionDynamics = EmotionDynamics()

/**
 * Calculates expressive procedural motion for each of the 14 emotions based on elapsed time.
 * Enabled via the `animateEmotions` flag in BlobavatarOptions.
 */
fun emotionDynamicsAt(
    expression: Expression?,
    elapsedMilliseconds: Double,
    enabled: Boolean = true
): EmotionDynamics {
    if (!enabled || expression == null) return identityEmotionDynamics

    // Always keep positive normalized cycle in [0.0, 1.0)
    fun cycle(periodSeconds: Double, phaseSeconds: Double = 0.0): Double {
        val value = (elapsedMilliseconds / 1000.0 + phaseSeconds) / periodSeconds
        return value - floor(value)
    }

    return when (expression.name) {
        "happy" -> {
            // Joyful energetic bounce with squash & stretch
            val c = cycle(0.75)
            val hop = sin(c * PI)
            val dy = -hop * 8.5
            val squash = (1.0 - hop) * 0.08
            EmotionDynamics(
                dy = dy,
                scaleX = 1.0 + squash,
                scaleY = 1.0 - squash * 0.7,
                rotation = sin(cycle(1.5) * 2 * PI) * 4.0
            )
        }
        "love" -> {
            // Pronounced double heartbeat pulse + romantic floating sway
            val c = cycle(1.15)
            val pulse = when {
                c < 0.14 -> sin(c / 0.14 * PI) * 0.13
                c in 0.18..0.32 -> sin((c - 0.18) / 0.14 * PI) * 0.08
                else -> 0.0
            }
            EmotionDynamics(
                dy = sin(cycle(2.3) * 2 * PI) * 3.5,
                scaleX = 1.0 + pulse,
                scaleY = 1.0 + pulse,
                rotation = sin(cycle(2.3) * 2 * PI) * 4.5
            )
        }
        "sad" -> {
            // Heavy melancholy slump & periodic sobbing heave
            val c = cycle(2.8)
            val sob = if (c in 0.75..0.92) sin((c - 0.75) / 0.17 * PI) * 2.5 else 0.0
            EmotionDynamics(
                dy = 4.0 + sin(cycle(2.8) * 2 * PI) * 2.0 - sob,
                scaleX = 0.96,
                scaleY = 1.04,
                rotation = sin(cycle(2.8) * 2 * PI) * 2.5
            )
        }
        "mad" -> {
            // Intense furious vibration & rage huffing
            val c = cycle(0.6)
            val huff = sin(c * 2 * PI) * 0.07
            val jitterX = sin(cycle(0.04) * 2 * PI) * 3.2
            val jitterY = cos(cycle(0.035) * 2 * PI) * 2.4
            EmotionDynamics(
                dx = jitterX,
                dy = jitterY,
                scaleX = 1.0 + huff,
                scaleY = 1.0 - huff * 0.6,
                rotation = sin(cycle(0.06) * 2 * PI) * 3.5
            )
        }
        "surprised" -> {
            // Startled upward jump & suspended shock jitter
            val c = cycle(1.6)
            val hop = if (c < 0.25) sin(c / 0.25 * PI) * 3.0 else 0.0
            val jitter = sin(cycle(0.08) * 2 * PI) * 1.5
            EmotionDynamics(
                dy = -6.5 - hop + jitter,
                scaleX = 0.94,
                scaleY = 1.07,
                rotation = sin(cycle(0.3) * 2 * PI) * 2.0
            )
        }
        "wink" -> {
            // Playful cheek squish & cheeky head tilt
            val c = cycle(1.4)
            val tilt = sin(c * 2 * PI) * 7.5
            val squish = sin(c * 2 * PI) * 0.06
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 2.5,
                dy = cos(c * 4 * PI) * 1.5,
                scaleX = 1.0 + squish,
                scaleY = 1.0 - squish,
                rotation = tilt
            )
        }
        "sleepy" -> {
            // Drowsy progressive nod-off & sudden wake-up catch
            val c = cycle(2.6)
            val nod = if (c < 0.80) {
                val p = c / 0.80
                p * p * 8.0
            } else {
                8.0 * (1.0 - (c - 0.80) / 0.20)
            }
            EmotionDynamics(
                dy = nod,
                scaleX = 1.03,
                scaleY = 0.96,
                rotation = sin(cycle(2.6) * 2 * PI) * 3.5
            )
        }
        "smug" -> {
            // Cocky arrogant swagger & chin lift oscillation
            val c = cycle(1.8)
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 4.0,
                dy = cos(c * 4 * PI) * 2.2,
                rotation = sin(c * 2 * PI) * 8.0
            )
        }
        "unsure" -> {
            // Puzzled head cocking left & right with hesitant pause
            val c = cycle(2.2)
            val raw = sin(c * 2 * PI)
            val angle = sign(raw) * abs(raw).let { if (it > 0.4) 1.0 else it / 0.4 } * 7.0
            EmotionDynamics(
                dx = sin(c * 4 * PI) * 1.8,
                dy = cos(c * 2 * PI) * 1.4,
                rotation = angle
            )
        }
        "scared" -> {
            // Fast panic shiver & terrified contraction
            val jitterX = sin(cycle(0.025) * 2 * PI) * 3.5
            val jitterY = cos(cycle(0.028) * 2 * PI) * 2.8
            EmotionDynamics(
                dx = jitterX,
                dy = jitterY,
                scaleX = 0.93 + sin(cycle(0.12) * 2 * PI) * 0.03,
                scaleY = 1.05,
                rotation = sin(cycle(0.04) * 2 * PI) * 4.0
            )
        }
        "shy" -> {
            // Bashful rocking & timid sinking/peeking
            val c = cycle(2.0)
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 4.5,
                dy = 3.0 + abs(cos(c * 2 * PI)) * 2.5,
                scaleX = 0.95,
                rotation = sin(c * 2 * PI) * 5.5
            )
        }
        "sick" -> {
            // Queasy rolling stomach heave & dizzy wobble
            val c = cycle(1.8)
            val roll = sin(c * 2 * PI) * 8.0
            val heave = sin(c * 4 * PI) * 3.5
            EmotionDynamics(
                dx = cos(c * 2 * PI) * 2.5,
                dy = heave,
                scaleX = 1.0 + sin(c * 2 * PI) * 0.05,
                rotation = roll
            )
        }
        "thinking" -> {
            // Pondering chin rocking & contemplative slow float
            val c = cycle(2.2)
            EmotionDynamics(
                dx = cos(c * 2 * PI) * 2.2,
                dy = -3.0 + sin(c * 4 * PI) * 2.0,
                rotation = sin(c * 2 * PI) * 6.0
            )
        }
        "excited" -> {
            // High-energy bounce hop with dynamic squash and stretch
            val c = cycle(0.5)
            val hop = sin(c * PI)
            val dy = -hop * 9.5
            val squash = (1.0 - hop) * 0.12
            EmotionDynamics(
                dy = dy,
                scaleX = 1.0 + squash,
                scaleY = 1.0 - squash * 0.75,
                rotation = sin(cycle(1.0) * 2 * PI) * 5.0
            )
        }
        "cool" -> {
            // Chill relaxed swagger & confident head bob
            val c = cycle(1.6)
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 3.5,
                dy = abs(cos(c * 2 * PI)) * 1.8 - 1.0,
                rotation = -sin(c * 2 * PI) * 4.5
            )
        }
        "dizzy" -> {
            // Spiral orbital wobble & disoriented rolling tilt
            val c = cycle(1.3)
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 5.0,
                dy = cos(c * 2 * PI) * 3.5,
                scaleX = 1.0 + sin(c * 4 * PI) * 0.05,
                scaleY = 1.0 - sin(c * 4 * PI) * 0.05,
                rotation = sin(c * 2 * PI) * 12.0
            )
        }
        "zen" -> {
            // Calming deep breath & serene levitation float
            val c = cycle(3.2)
            val breathe = sin(c * 2 * PI)
            EmotionDynamics(
                dy = -3.0 + breathe * 4.0,
                scaleX = 1.0 + breathe * 0.05,
                scaleY = 1.0 + breathe * 0.05,
                rotation = sin(c * 2 * PI) * 1.5
            )
        }
        "mischievous" -> {
            // Devious angled swagger & mischievous chuckle vibration
            val c = cycle(1.5)
            val chuckle = if (c in 0.6..0.95) sin(cycle(0.08) * 2 * PI) * 1.5 else 0.0
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 2.5 + chuckle,
                dy = cos(c * 2 * PI) * 1.8,
                rotation = 3.0 + sin(c * 2 * PI) * 4.5
            )
        }
        "mindblown" -> {
            // Stunned shock recoil, popping scale & freeze jitter
            val c = cycle(1.8)
            val shock = if (c < 0.3) sin(c / 0.3 * PI) * 0.15 else 0.0
            val jitter = sin(cycle(0.04) * 2 * PI) * 2.2
            EmotionDynamics(
                dx = jitter,
                dy = -5.0 - shock * 18.0,
                scaleX = 1.0 + shock,
                scaleY = 1.0 + shock,
                rotation = sin(cycle(0.08) * 2 * PI) * 2.5
            )
        }
        "crying" -> {
            // Dramatic sobbing heave & shuddering slump
            val c = cycle(1.2)
            val sob = if (c < 0.6) sin(c / 0.6 * PI) * 4.5 else 0.0
            val shudder = sin(cycle(0.07) * 2 * PI) * 1.6
            EmotionDynamics(
                dy = 3.5 + sob + shudder,
                scaleX = 1.02,
                scaleY = 0.97 - (sob / 4.5) * 0.06,
                rotation = sin(c * 2 * PI) * 3.0
            )
        }
        "silly" -> {
            // Wobbly goofy bob & eccentric zig-zag head tilt
            val c = cycle(1.1)
            EmotionDynamics(
                dx = sin(c * 2 * PI) * 4.0,
                dy = sin(c * 4 * PI) * 3.0,
                scaleX = 1.0 + cos(c * 2 * PI) * 0.08,
                scaleY = 1.0 - cos(c * 2 * PI) * 0.08,
                rotation = sin(c * 2 * PI) * 11.0
            )
        }
        "bored" -> {
            // Slow unimpressed droop & heavy sigh slump
            val c = cycle(3.0)
            val sigh = if (c in 0.4..0.85) sin((c - 0.4) / 0.45 * PI) * 3.0 else 0.0
            EmotionDynamics(
                dy = 2.0 + sigh,
                scaleX = 1.03,
                scaleY = 0.96,
                rotation = sin(c * 2 * PI) * 2.0
            )
        }
        "nervous" -> {
            // High-frequency anxious chattering shiver
            val jitterX = sin(cycle(0.03) * 2 * PI) * 2.8
            val jitterY = cos(cycle(0.033) * 2 * PI) * 2.2
            EmotionDynamics(
                dx = jitterX,
                dy = jitterY,
                rotation = sin(cycle(0.05) * 2 * PI) * 2.0
            )
        }
        "idle" -> {
            // Noticeable gentle breathing & float
            val c = cycle(2.4)
            EmotionDynamics(
                dy = sin(c * 2 * PI) * 2.2,
                rotation = sin(c * 2 * PI) * 1.5
            )
        }
        else -> identityEmotionDynamics
    }
}
