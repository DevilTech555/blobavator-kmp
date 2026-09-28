package com.navbyte.blobavatar.core

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

sealed interface PathSegment

data class MoveTo(val x: Double, val y: Double) : PathSegment
data class LineTo(val x: Double, val y: Double) : PathSegment
data class CubicTo(
    val c1x: Double, val c1y: Double,
    val c2x: Double, val c2y: Double,
    val x: Double, val y: Double
) : PathSegment
data class QuadTo(val cx: Double, val cy: Double, val x: Double, val y: Double) : PathSegment
data class HorizontalLineTo(val x: Double) : PathSegment
data class VerticalLineTo(val y: Double) : PathSegment
data object ClosePath : PathSegment

/**
 * The `r2` formatter: two decimal places, `-0` collapsed to `0`, and no
 * trailing `.0` on whole numbers — byte-identical to JS `String(Number)`.
 */
fun r2(v: Double): String {
    val s = jsRound(v * 100.0) / 100.0
    if (s == 0.0) return "0"
    val tr = s.toLong()
    if (tr.toDouble() == s) return tr.toString()
    return s.toString()
}

/**
 * A structured closed/open path with a faithful string serialization.
 */
data class BlobPath(val segments: List<PathSegment>) {
    /**
     * SVG path data matching TypeScript / web output: compact command letters,
     * single-space-separated coordinates, two-decimal rounding.
     */
    fun toPathData(): String {
        val d = StringBuilder()
        for (seg in segments) {
            when (seg) {
                is MoveTo -> d.append("M${r2(seg.x)} ${r2(seg.y)}")
                is LineTo -> d.append("L${r2(seg.x)} ${r2(seg.y)}")
                is CubicTo -> d.append("C${r2(seg.c1x)} ${r2(seg.c1y)} ${r2(seg.c2x)} ${r2(seg.c2y)} ${r2(seg.x)} ${r2(seg.y)}")
                is QuadTo -> d.append("Q${r2(seg.cx)} ${r2(seg.cy)} ${r2(seg.x)} ${r2(seg.y)}")
                is HorizontalLineTo -> d.append("H${r2(seg.x)}")
                is VerticalLineTo -> d.append("V${r2(seg.y)}")
                is ClosePath -> d.append("Z")
            }
        }
        return d.toString()
    }
}

/**
 * A superellipse: `|x/a|^n + |y/b|^n = 1`.
 */
data class Superellipse(
    val cx: Double,
    val cy: Double,
    val rx: Double,
    val ry: Double,
    val n: Double = 4.0,
    val rot: Double = 0.0
)

/**
 * Approximates each quadrant of a superellipse with one cubic Bezier.
 */
fun superellipse(s: Superellipse): BlobPath {
    val cx = s.cx
    val cy = s.cy
    val rx = s.rx
    val ry = s.ry
    val n = s.n
    val rot = s.rot

    val k = min(1.0, (8.0 * 2.0.pow(-1.0 / n) - 4.0) / 3.0)
    val a = rx
    val b = ry
    val ak = a * k
    val bk = b * k

    val pts = doubleArrayOf(
        a, 0.0,
        a, bk,
        ak, b,
        0.0, b,
        -ak, b,
        -a, bk,
        -a, 0.0,
        -a, -bk,
        -ak, -b,
        0.0, -b,
        ak, -b,
        a, -bk,
        a, 0.0
    )

    val t = rot * PI / 180.0
    val cosT = cos(t)
    val sinT = sin(t)

    fun atX(i: Int): Double {
        val x = pts[i * 2]
        val y = pts[i * 2 + 1]
        return cx + x * cosT - y * sinT
    }

    fun atY(i: Int): Double {
        val x = pts[i * 2]
        val y = pts[i * 2 + 1]
        return cy + x * sinT + y * cosT
    }

    val segments = ArrayList<PathSegment>(6)
    segments.add(MoveTo(atX(0), atY(0)))
    for (i in 1 until 13 step 3) {
        segments.add(
            CubicTo(
                atX(i), atY(i),
                atX(i + 1), atY(i + 1),
                atX(i + 2), atY(i + 2)
            )
        )
    }
    segments.add(ClosePath)
    return BlobPath(segments)
}

/**
 * A quadratic arc, stroked — used only for smiles and frowns.
 */
fun arc(cx: Double, cy: Double, w: Double, depth: Double): BlobPath = BlobPath(
    listOf(
        MoveTo(cx - w, cy),
        QuadTo(cx, cy + depth, cx + w, cy)
    )
)

/**
 * An organic closed curve: radii sampled around a circle, joined by a closed
 * Catmull-Rom spline converted to cubic Beziers.
 */
fun blobPath(
    cx: Double,
    cy: Double,
    rx: Double,
    ry: Double,
    radii: List<Double>,
    rot: Double = 0.0
): BlobPath {
    val n = radii.size
    val t0 = rot * PI / 180.0
    val px = DoubleArray(n)
    val py = DoubleArray(n)
    for (i in 0 until n) {
        val angle = t0 + 2.0 * PI * i / n
        px[i] = cx + rx * radii[i] * cos(angle)
        py[i] = cy + ry * radii[i] * sin(angle)
    }

    fun atIdx(i: Int): Int = ((i % n) + n) % n

    val segments = ArrayList<PathSegment>(n + 2)
    segments.add(MoveTo(px[0], py[0]))

    for (i in 0 until n) {
        val i0 = atIdx(i - 1)
        val i1 = atIdx(i)
        val i2 = atIdx(i + 1)
        val i3 = atIdx(i + 2)

        val x0 = px[i0]
        val y0 = py[i0]
        val x1 = px[i1]
        val y1 = py[i1]
        val x2 = px[i2]
        val y2 = py[i2]
        val x3 = px[i3]
        val y3 = py[i3]

        segments.add(
            CubicTo(
                x1 + (x2 - x0) / 6.0,
                y1 + (y2 - y0) / 6.0,
                x2 - (x3 - x1) / 6.0,
                y2 - (y3 - y1) / 6.0,
                x2,
                y2
            )
        )
    }

    segments.add(ClosePath)
    return BlobPath(segments)
}

/**
 * Regular polygon with rounded corners.
 */
data class Polygon(
    val cx: Double,
    val cy: Double,
    val rx: Double,
    val ry: Double,
    val sides: Int,
    val round: Double = 0.3,
    val rot: Double = 0.0
)

fun polygon(p: Polygon): BlobPath {
    val cx = p.cx
    val cy = p.cy
    val rx = p.rx
    val ry = p.ry
    val sides = p.sides
    val round = p.round
    val rot = p.rot

    val k = if (round > 0.0) (if (round < 1.0) round / 2.0 else 0.5) else 0.0
    val t0 = rot * PI / 180.0 - PI / 2.0

    val vx = DoubleArray(sides)
    val vy = DoubleArray(sides)
    for (i in 0 until sides) {
        val angle = t0 + 2.0 * PI * i / sides
        vx[i] = cx + rx * cos(angle)
        vy[i] = cy + ry * sin(angle)
    }

    fun atIdx(i: Int): Int = ((i % sides) + sides) % sides

    fun cutX(i: Int, j: Int): Double {
        val iIdx = atIdx(i)
        val jIdx = atIdx(j)
        return vx[iIdx] + (vx[jIdx] - vx[iIdx]) * k
    }

    fun cutY(i: Int, j: Int): Double {
        val iIdx = atIdx(i)
        val jIdx = atIdx(j)
        return vy[iIdx] + (vy[jIdx] - vy[iIdx]) * k
    }

    val segments = ArrayList<PathSegment>()
    segments.add(MoveTo(cutX(0, -1), cutY(0, -1)))

    for (i in 0 until sides) {
        val iIdx = atIdx(i)
        val vX = vx[iIdx]
        val vY = vy[iIdx]
        val ex = cutX(i, i + 1)
        val ey = cutY(i, i + 1)
        segments.add(QuadTo(vX, vY, ex, ey))

        if (k < 0.5) {
            val lx = cutX(i + 1, i)
            val ly = cutY(i + 1, i)
            segments.add(LineTo(lx, ly))
        }
    }
    segments.add(ClosePath)
    return BlobPath(segments)
}

/**
 * The straight run of a capsule, as a plain box.
 */
fun box(cx: Double, cy: Double, rx: Double, ry: Double): BlobPath = BlobPath(
    listOf(
        MoveTo(cx - rx, cy - ry),
        HorizontalLineTo(cx + rx),
        VerticalLineTo(cy + ry),
        HorizontalLineTo(cx - rx),
        ClosePath
    )
)

/**
 * The taper of a droplet: the two tangents from an apex to the body ellipse.
 */
fun taper(cx: Double, cy: Double, rx: Double, ry: Double, tip: Double): BlobPath {
    val t = max(1.05, tip)
    val tx = rx * sqrt(1.0 - 1.0 / (t * t))
    val ty = cy - ry / t
    val apex = cy - t * ry
    val px = tx * 0.14
    val py = ty + 0.86 * (apex - ty)

    return BlobPath(
        listOf(
            MoveTo(cx - tx, ty),
            LineTo(cx - px, py),
            QuadTo(cx, apex, cx + px, py),
            LineTo(cx + tx, ty),
            ClosePath
        )
    )
}
