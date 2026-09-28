package com.navbyte.blobavatar.core

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Palette construction and OKLCh color mathematics.
 *
 * Kotlin Multiplatform port of `packages/blobatar/src/color.ts` at blobatar 2.4.0.
 *
 * Hue is the only value the seed controls. Lightness and chroma are authored
 * constants, which is what makes every blobatar look like it came from the
 * same designer rather than from a random number generator.
 *
 * Colors are resolved to hex rather than emitted as `oklch()`. Doing the
 * conversion here also means the contrast guarantee is enforced against real
 * sRGB luminance instead of assumed from OKLab lightness, which drifts by up
 * to ~1.4:1 between hues at equal L.
 */
data class Oklch(
    val l: Double,
    val c: Double,
    val h: Double
) {
    fun withL(l: Double): Oklch = Oklch(l, c, h)
}

const val COLOR_BG = "bg"
const val COLOR_HEAD = "head"
const val COLOR_EYE = "eye"

typealias Palette = Map<String, String>

fun jsRound(v: Double): Double = kotlin.math.floor(v + 0.5)

fun cbrt(x: Double): Double =
    if (x >= 0) x.pow(1.0 / 3.0) else -(-x).pow(1.0 / 3.0)

fun hypot2(x: Double, y: Double): Double = sqrt(x * x + y * y)

private fun toLinear(color: Oklch): Triple<Double, Double, Double> {
    val r = color.h * PI / 180.0
    val a = color.c * cos(r)
    val b = color.c * sin(r)

    val l_ = color.l + 0.3963377774 * a + 0.2158037573 * b
    val m_ = color.l - 0.1055613458 * a - 0.0638541728 * b
    val s_ = color.l - 0.0894841775 * a - 1.291485548 * b

    val lCubed = l_ * l_ * l_
    val mCubed = m_ * m_ * m_
    val sCubed = s_ * s_ * s_

    return Triple(
        4.0767416621 * lCubed - 3.3077115913 * mCubed + 0.2309699292 * sCubed,
        -1.2684380046 * lCubed + 2.6097574011 * mCubed - 0.3413193965 * sCubed,
        -0.0041960863 * lCubed - 0.7034186147 * mCubed + 1.707614701 * sCubed
    )
}

private fun inGamut(rgb: Triple<Double, Double, Double>): Boolean =
    rgb.first in -1e-4..(1.0 + 1e-4) &&
    rgb.second in -1e-4..(1.0 + 1e-4) &&
    rgb.third in -1e-4..(1.0 + 1e-4)

private fun resolveGamut(color: Oklch): Triple<Double, Double, Double> {
    var rgb = toLinear(color)
    if (!inGamut(rgb)) {
        var lo = 0.0
        var hi = color.c
        for (i in 0 until 12) {
            val mid = (lo + hi) / 2.0
            if (inGamut(toLinear(Oklch(color.l, mid, color.h)))) {
                lo = mid
            } else {
                hi = mid
            }
        }
        rgb = toLinear(Oklch(color.l, lo, color.h))
    }
    return Triple(
        rgb.first.coerceIn(0.0, 1.0),
        rgb.second.coerceIn(0.0, 1.0),
        rgb.third.coerceIn(0.0, 1.0)
    )
}

private fun luminance(color: Oklch): Double {
    val rgb = resolveGamut(color)
    return 0.2126 * rgb.first + 0.7152 * rgb.second + 0.0722 * rgb.third
}

fun contrast(a: Oklch, b: Oklch): Double {
    val x = luminance(a)
    val y = luminance(b)
    return (max(x, y) + 0.05) / (min(x, y) + 0.05)
}

fun ensureContrast(fg: Oklch, bg: Oklch, minRatio: Double): Oklch {
    if (contrast(fg, bg) >= minRatio) return fg

    val lean = if (fg.l >= bg.l) 1.0 else -1.0
    for (dir in doubleArrayOf(lean, -lean)) {
        var l = fg.l
        for (i in 0 until 60) {
            l = min(1.0, max(0.0, l + dir * 0.02))
            val probe = Oklch(l, fg.c, fg.h)
            if (contrast(probe, bg) >= minRatio) return probe
            if (l == 0.0 || l == 1.0) break
        }
    }

    val black = Oklch(0.0, 0.0, fg.h)
    val white = Oklch(1.0, 0.0, fg.h)
    return if (contrast(black, bg) >= contrast(white, bg)) black else white
}

private fun hex2(v: Int): String {
    val s = v.toString(16)
    return if (s.length < 2) "0$s" else s
}

fun toHex(color: Oklch): String {
    val rgb = resolveGamut(color)
    val out = StringBuilder("#")
    for (v in doubleArrayOf(rgb.first, rgb.second, rgb.third)) {
        val s = if (v <= 0.0031308) 12.92 * v else 1.055 * v.pow(1.0 / 2.4) - 0.055
        out.append(hex2(jsRound(s * 255.0).toInt().coerceIn(0, 255)))
    }
    return out.toString()
}

fun fromHex(hex: String): Oklch {
    val clean = if (hex.startsWith("#")) hex.substring(1) else hex
    val n = clean.toLong(16).toInt()

    fun decode(v: Int): Double {
        val s = v / 255.0
        return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
    }

    val r = decode((n shr 16) and 255)
    val g = decode((n shr 8) and 255)
    val b = decode(n and 255)

    val l = cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
    val m = cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
    val s = cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)

    val a = 1.9779984951 * l - 2.428592205 * m + 0.4505937099 * s
    val bb = 0.0259040371 * l + 0.7827717662 * m - 0.808675766 * s

    return Oklch(
        0.2104542553 * l + 0.793617785 * m - 0.0040720468 * s,
        hypot2(a, bb),
        atan2(bb, a) * 180.0 / PI
    )
}

fun mix(a: Oklch, b: Oklch, t: Double): Oklch {
    fun rad(v: Double): Double = v * PI / 180.0
    val ax = a.c * cos(rad(a.h))
    val ay = a.c * sin(rad(a.h))
    val bx = b.c * cos(rad(b.h))
    val by = b.c * sin(rad(b.h))
    val x = ax + (bx - ax) * t
    val y = ay + (by - ay) * t
    return Oklch(
        a.l + (b.l - a.l) * t,
        hypot2(x, y),
        atan2(y, x) * 180.0 / PI
    )
}

fun mixHex(a: String, b: String, t: Double): String =
    toHex(mix(fromHex(a), fromHex(b), t))

fun fadeHex(a: String, b: String, t: Double): String {
    val cleanA = if (a.startsWith("#")) a.substring(1) else a
    val cleanB = if (b.startsWith("#")) b.substring(1) else b
    val out = StringBuilder("#")
    for (i in 0 until 6 step 2) {
        val from = cleanA.substring(i, i + 2).toInt(16)
        val to = cleanB.substring(i, i + 2).toInt(16)
        val v = jsRound(from + (to - from) * t).toInt().coerceIn(0, 255)
        out.append(hex2(v))
    }
    return out.toString()
}

data class Tint(
    val h: Double,
    val l: Double,
    val pull: Double,
    val c: Double
)

val hot = Tint(27.0, 0.58, 0.6, 0.18)
val rose = Tint(358.0, 0.72, 0.55, 0.16)
val blush = Tint(12.0, 0.84, 0.4, 0.1)
val bile = Tint(142.0, 0.66, 0.6, 0.13)

val tints = listOf(
    "hot" to hot,
    "rose" to rose,
    "blush" to blush,
    "bile" to bile
)

private const val TINT_FLOOR = 4.55

val darkSurface = Oklch(0.145, 0.0, 0.0)
const val surfaceFloor = 1.5

fun tinted(head: String, eye: String, t: Tint): Pair<String, String> {
    val base = fromHex(head)
    val baseEye = fromHex(eye)

    var hotHead = Oklch(
        base.l + (t.l - base.l) * t.pull,
        max(base.c, t.c),
        t.h
    )
    hotHead = ensureContrast(hotHead, darkSurface, surfaceFloor)

    var hotEye = ensureContrast(baseEye, hotHead, TINT_FLOOR)
    val dir = if (hotEye.l >= hotHead.l) 1.0 else -1.0
    val headHex = toHex(hotHead)

    for (pass in 0 until 40) {
        val eyeHex = toHex(hotEye)
        var worst = Double.POSITIVE_INFINITY
        for (i in 0..10) {
            val tt = i / 10.0
            worst = min(
                worst,
                contrast(
                    fromHex(mixHex(eye, eyeHex, tt)),
                    fromHex(mixHex(head, headHex, tt))
                )
            )
        }
        if (worst >= TINT_FLOOR) return Pair(headHex, eyeHex)
        val l = min(1.0, max(0.0, hotEye.l + dir * 0.02))
        if (l == hotEye.l) return Pair(headHex, eyeHex)
        hotEye = hotEye.withL(l)
    }

    return Pair(headHex, toHex(hotEye))
}

private data class Tone(val edge: Double, val l: Double, val c: Double)

private val tones = listOf(
    Tone(0.2, 0.86, 0.085),
    Tone(0.36, 0.9, 0.028),
    Tone(0.62, 0.73, 0.135),
    Tone(0.8, 0.62, 0.165),
    Tone(0.93, 0.87, 0.16),
    Tone(1.0, 0.34, 0.035)
)

private fun toneAt(v: Double): Tone =
    tones.firstOrNull { v < it.edge } ?: tones.first()

private fun rawRamp(hue: Double, tone: Double): Map<String, Oklch> {
    val t = toneAt(tone)
    val head = ensureContrast(Oklch(t.l, t.c, hue), darkSurface, surfaceFloor)
    val eye = if (head.l >= 0.5) Oklch(0.17, 0.02, hue) else Oklch(0.97, 0.012, hue)
    return mapOf(
        COLOR_BG to Oklch(0.965, 0.01, hue),
        COLOR_HEAD to head,
        COLOR_EYE to eye
    )
}

val floors = listOf(
    Triple(COLOR_HEAD, COLOR_BG, 1.25),
    Triple(COLOR_EYE, COLOR_HEAD, 4.5)
)

fun ramp(hue: Double, enforce: Boolean = true, tone: Double = 0.0): Map<String, Oklch> {
    val r = rawRamp(hue, tone).toMutableMap()
    if (enforce) {
        for ((fg, bg, minRatio) in floors) {
            r[fg] = ensureContrast(r[fg]!!, r[bg]!!, minRatio)
        }
    }
    return r
}

fun palette(hue: Double, enforce: Boolean = true, tone: Double = 0.0): Palette {
    val r = ramp(hue, enforce, tone)
    return r.mapValues { toHex(it.value) }
}
