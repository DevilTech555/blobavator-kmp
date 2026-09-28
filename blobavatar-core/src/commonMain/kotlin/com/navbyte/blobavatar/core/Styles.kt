package com.navbyte.blobavatar.core

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class Body(
    var cx: Double,
    var cy: Double,
    var rx: Double,
    var ry: Double,
    var n: Double,
    var rot: Double,
    var radii: List<Double>,
    var sides: Int? = null,
    var round: Double? = null
)

data class Ellipse(
    var cx: Double,
    var cy: Double,
    var rx: Double,
    var ry: Double
)

data class Petal(
    val cx: Double,
    val cy: Double,
    val r: Double
)

class Deco {
    val petals = mutableListOf<Petal>()
    val extra = mutableListOf<BlobPath>()
}

abstract class Shape(
    val name: String,
    val core: Double
) {
    open fun body(t: Traits, b: Body) {}
    open fun face(b: Body): Ellipse? = null
    open fun decorate(t: Traits, b: Body, out: Deco) {}
    open fun path(b: Body): BlobPath? = null
}

private fun poly(b: Body): BlobPath = polygon(
    Polygon(
        cx = b.cx,
        cy = b.cy,
        rx = b.rx,
        ry = b.ry,
        sides = b.sides!!,
        round = b.round!!,
        rot = b.rot
    )
)

private fun spline(b: Body): BlobPath =
    blobPath(b.cx, b.cy, b.rx, b.ry, b.radii, b.rot)

private fun shrunk(b: Body, k: Double): Ellipse =
    Ellipse(b.cx, b.cy, b.rx * k, b.ry * k)

private fun splineFace(b: Body): Ellipse {
    var minR = b.radii[0]
    for (r in b.radii) {
        if (r < minR) minR = r
    }
    return shrunk(b, minR * 0.95)
}

class RoundShape : Shape("round", 1.0)

class OrganicShape : Shape("organic", 0.98) {
    override fun path(b: Body): BlobPath = spline(b)
    override fun face(b: Body): Ellipse = splineFace(b)
}

class BoxyShape : Shape("boxy", 0.86) {
    override fun body(t: Traits, b: Body) {
        b.n = t.numIn("body.n", 3.4, 6.0)
        b.rot = t.numIn("body.rot", -20.0, 20.0)
    }
}

class CapsuleShape : Shape("capsule", 1.02) {
    override fun body(t: Traits, b: Body) {
        b.ry *= t.numIn("capsule.squat", 0.55, 0.68)
    }

    override fun face(b: Body): Ellipse = shrunk(b, 0.94)

    override fun decorate(t: Traits, b: Body, out: Deco) {
        for (s in intArrayOf(-1, 1)) {
            out.petals.add(Petal(b.cx + s * (b.rx - b.ry), b.cy, b.ry))
        }
    }

    override fun path(b: Body): BlobPath = box(b.cx, b.cy, b.rx - b.ry, b.ry)
}

class NubShape : Shape("nub", 0.88) {
    override fun decorate(t: Traits, b: Body, out: Deco) {
        val count = t.intIn("nub.n", 1, 2)
        for (i in 0 until count) {
            val a = t.numIn("nub.a$i", 0.0, 2.0 * PI)
            out.petals.add(
                Petal(
                    b.cx + cos(a) * b.rx * 0.88,
                    b.cy + sin(a) * b.rx * 0.88,
                    b.rx * t.numIn("nub.r$i", 0.24, 0.4)
                )
            )
        }
    }
}

class CloudShape : Shape("cloud", 0.78) {
    override fun path(b: Body): BlobPath = spline(b)
    override fun face(b: Body): Ellipse = splineFace(b)

    override fun decorate(t: Traits, b: Body, out: Deco) {
        val count = t.intIn("cloud.n", 4, 6)
        for (i in 0 until count) {
            val a = PI + (PI * (i + 0.5)) / count
            out.petals.add(
                Petal(
                    b.cx + cos(a) * b.rx * 0.8,
                    b.cy + sin(a) * b.rx * 0.5,
                    b.rx * t.numIn("cloud.r$i", 0.44, 0.62)
                )
            )
        }
    }
}

class DropletShape : Shape("droplet", 0.78) {
    override fun body(t: Traits, b: Body) {
        b.cy += 0.22 * b.ry
        b.n = 2.0
    }

    override fun face(b: Body): Ellipse =
        Ellipse(b.cx, b.cy + b.ry * 0.05, b.rx * 0.88, b.ry * 0.88)

    override fun decorate(t: Traits, b: Body, out: Deco) {
        out.extra.add(taper(b.cx, b.cy, b.rx, b.ry, t.numIn("droplet.tip", 1.4, 1.65)))
    }
}

class HexagonShape : Shape("hexagon", 1.05) {
    override fun path(b: Body): BlobPath = poly(b)
    override fun face(b: Body): Ellipse = shrunk(b, 0.84)

    override fun body(t: Traits, b: Body) {
        b.sides = 6
        b.rot = t.numIn("body.rot", -12.0, 12.0)
        b.round = t.numIn("poly.round", 0.24, 0.5)
    }
}

class SunShape : Shape("sun", 0.7) {
    override fun decorate(t: Traits, b: Body, out: Deco) {
        val count = t.intIn("sun.n", 6, 9)
        val dist = b.rx * t.numIn("sun.dist", 1.0, 1.08)
        val pr = b.rx * t.numIn("sun.r", 0.2, 0.26)
        val off = t.numIn("sun.rot", 0.0, 2.0 * PI)
        for (i in 0 until count) {
            val a = off + (2.0 * PI * i) / count
            out.petals.add(Petal(b.cx + cos(a) * dist, b.cy + sin(a) * dist, pr))
        }
    }
}

class TriangleShape : Shape("triangle", 1.15) {
    override fun path(b: Body): BlobPath = poly(b)

    override fun body(t: Traits, b: Body) {
        b.sides = 3
        b.rot = t.numIn("body.rot", -5.0, 5.0)
        b.round = t.numIn("poly.round", 0.24, 0.5)
    }

    override fun face(b: Body): Ellipse =
        Ellipse(b.cx, b.cy + b.ry * 0.1, b.rx * 0.54, b.ry * 0.36)
}

val round = RoundShape()
val organic = OrganicShape()
val boxy = BoxyShape()
val capsule = CapsuleShape()
val nub = NubShape()
val cloud = CloudShape()
val droplet = DropletShape()
val hexagon = HexagonShape()
val sun = SunShape()
val triangle = TriangleShape()

data class Eye(
    var cx: Double,
    var cy: Double,
    var rx: Double,
    var ry: Double,
    var n: Double,
    var rot: Double
)

data class BlobatarLayout(
    val shape: String,
    val body: Body,
    val face: Ellipse,
    val eyes: List<Eye>,
    val petals: List<Petal>,
    val extra: List<BlobPath>,
    val draw: ((Body) -> BlobPath?)?,
    val bodyOffsetY: Double = 0.0
) {
    fun bodyPath(): BlobPath {
        val traced = draw?.invoke(body)
        if (traced != null) return traced
        return superellipse(
            Superellipse(
                cx = body.cx,
                cy = body.cy,
                rx = body.rx,
                ry = body.ry,
                n = body.n,
                rot = body.rot
            )
        )
    }

    fun eyePaths(): List<BlobPath> = eyes.map { e ->
        superellipse(
            Superellipse(
                cx = e.cx,
                cy = e.cy,
                rx = e.rx,
                ry = e.ry,
                n = e.n,
                rot = e.rot
            )
        )
    }
}

fun faceFit(t: Traits, b: Body, face: Ellipse): List<Eye> {
    val rx = b.rx
    val er0 = t.numIn("eye.rx", 0.075, 0.105) * rx
    val ratio = t.numIn("eye.ratio", 1.9, 3.2)
    val scale = t.numIn("eye.scale", 0.78, 1.24)
    val stretch = t.numIn("eye.stretch", 0.85, 1.18)
    val clearance = t.numIn("eye.gap", 0.1, 0.24) * rx
    val wide = er0 * max(1.0, scale)
    val tall = er0 * ratio * max(1.0, scale * stretch)
    val gap0 = wide + rx * 0.03 + clearance

    val gx = t.jitter("gaze.x", 0.09) * face.rx
    val gy = t.numIn("gaze.y", -0.2, 0.08) * face.ry
    val dy = t.jitter("eye.dy", 0.04) * face.ry
    val reach = sqrt(wide * wide + tall * tall)
    val need = sqrt(
        ((kotlin.math.abs(gx) + gap0 + reach) / face.rx).let { it * it } +
        ((kotlin.math.abs(gy) + kotlin.math.abs(dy) + reach) / face.ry).let { it * it }
    )
    val fit = if (need > 0.9) 0.9 / need else 1.0

    val er = er0 * fit
    val eyeRy = er * ratio
    val gap = gap0 * fit
    val room = max(0.0, min(1.0, clearance / tall))
    val bound = min(12.0, asin(room) * 180.0 / PI)
    val lean = t.numIn("eye.lean", -1.0, 1.0) * bound
    val lean2 = max(-12.0, min(12.0, lean + t.jitter("eye.lean2", 3.5)))

    val cx = face.cx + gx * fit
    val cy = face.cy + gy * fit
    return listOf(
        Eye(cx - gap, cy, er, eyeRy, t.numIn("eye.n", 3.5, 6.0), lean),
        Eye(
            cx + gap,
            cy + dy * fit,
            er * scale,
            eyeRy * scale * stretch,
            t.numIn("eye.n", 3.5, 6.0),
            lean2
        )
    )
}

data class Band(val shape: Shape, val upTo: Double)

class BlobatarStyle(
    val bands: List<Band>,
    val fit: (Traits, Body, Ellipse) -> List<Eye>,
    val background: Boolean = false
) {
    fun pick(v: Double): Shape {
        for (band in bands) {
            if (v < band.upTo) return band.shape
        }
        return bands.last().shape
    }

    fun layout(t: Traits): BlobatarLayout {
        val shape = pick(t("shape"))
        val r = t.numIn("body.r", 31.0, 38.0) * shape.core
        val ptsCount = t.intIn("body.pts", 6, 8)
        val radii = ArrayList<Double>(ptsCount)
        for (i in 0 until ptsCount) {
            radii.add(1.0 + t.jitter("body.r$i", 0.16))
        }

        val body = Body(
            cx = 50.0 + t.jitter("body.x", 1.5),
            cy = 50.0 + t.jitter("body.y", 1.5),
            rx = r,
            ry = r * t.numIn("body.ratio", 0.92, 1.08),
            n = t.numIn("body.n", 1.9, 2.5),
            rot = 0.0,
            radii = radii
        )
        shape.body(t, body)

        val face = shape.face(body) ?: Ellipse(body.cx, body.cy, body.rx, body.ry)
        val deco = Deco()
        shape.decorate(t, body, deco)

        return BlobatarLayout(
            shape = shape.name,
            body = body,
            face = face,
            eyes = fit(t, body, face),
            petals = deco.petals,
            extra = deco.extra,
            draw = { shape.path(it) }
        )
    }
}

val bands = listOf(
    Band(round, 0.22),
    Band(organic, 0.48),
    Band(boxy, 0.6),
    Band(capsule, 0.7),
    Band(nub, 0.79),
    Band(cloud, 0.86),
    Band(droplet, 0.915),
    Band(hexagon, 0.95),
    Band(sun, 0.98),
    Band(triangle, 1.0)
)

val style = BlobatarStyle(bands, ::faceFit)
