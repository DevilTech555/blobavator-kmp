package com.navbyte.blobavatar.core

import kotlin.math.max

/**
 * Procedural horn accessories for the "mischievous" expression.
 */
data class Horns(
    val leftHorn: BlobPath,
    val rightHorn: BlobPath,
    val leftAccent: BlobPath,
    val rightAccent: BlobPath,
    val fillColor: String = "#E11D48",
    val accentColor: String = "#FDA4AF"
)

/**
 * Procedural cooling glasses (sunglasses) for the "cool" expression.
 */
data class Sunglasses(
    val leftLens: BlobPath,
    val rightLens: BlobPath,
    val bridge: BlobPath,
    val leftTemple: BlobPath,
    val rightTemple: BlobPath,
    val leftGlint: BlobPath,
    val rightGlint: BlobPath,
    val frameColor: String = "#090D16",
    val glintColor: String = "#38BDF8"
)

/**
 * Constructs devil horn geometry matching the head position and curvature of any blob shape.
 */
fun buildHorns(body: Body): Horns {
    val hx = body.cx
    val hy = body.cy - body.ry
    val rx = body.rx
    val ry = body.ry

    // Left horn points
    val lBaseOutX = hx - rx * 0.46
    val lBaseOutY = hy + ry * 0.16
    val lTipX = hx - rx * 0.76
    val lTipY = hy - ry * 0.35
    val lBaseInX = hx - rx * 0.20
    val lBaseInY = hy + ry * 0.05

    val leftPath = BlobPath(
        listOf(
            MoveTo(lBaseOutX, lBaseOutY),
            CubicTo(
                hx - rx * 0.54, hy + ry * 0.02,
                hx - rx * 0.74, hy - ry * 0.12,
                lTipX, lTipY
            ),
            CubicTo(
                hx - rx * 0.58, hy - ry * 0.20,
                hx - rx * 0.32, hy - ry * 0.06,
                lBaseInX, lBaseInY
            ),
            ClosePath
        )
    )

    // Left horn inner glint/accent ridge
    val leftAccent = BlobPath(
        listOf(
            MoveTo(hx - rx * 0.40, hy + ry * 0.04),
            CubicTo(
                hx - rx * 0.50, hy - ry * 0.08,
                hx - rx * 0.64, hy - ry * 0.18,
                hx - rx * 0.70, hy - ry * 0.26
            ),
            LineTo(hx - rx * 0.67, hy - ry * 0.24),
            CubicTo(
                hx - rx * 0.61, hy - ry * 0.16,
                hx - rx * 0.48, hy - ry * 0.06,
                hx - rx * 0.38, hy + ry * 0.06
            ),
            ClosePath
        )
    )

    // Right horn points (mirrored around hx)
    val rBaseOutX = hx + rx * 0.46
    val rBaseOutY = hy + ry * 0.16
    val rTipX = hx + rx * 0.76
    val rTipY = hy - ry * 0.35
    val rBaseInX = hx + rx * 0.20
    val rBaseInY = hy + ry * 0.05

    val rightPath = BlobPath(
        listOf(
            MoveTo(rBaseOutX, rBaseOutY),
            CubicTo(
                hx + rx * 0.54, hy + ry * 0.02,
                hx + rx * 0.74, hy - ry * 0.12,
                rTipX, rTipY
            ),
            CubicTo(
                hx + rx * 0.58, hy - ry * 0.20,
                hx + rx * 0.32, hy - ry * 0.06,
                rBaseInX, rBaseInY
            ),
            ClosePath
        )
    )

    // Right horn inner glint/accent ridge
    val rightAccent = BlobPath(
        listOf(
            MoveTo(hx + rx * 0.40, hy + ry * 0.04),
            CubicTo(
                hx + rx * 0.50, hy - ry * 0.08,
                hx + rx * 0.64, hy - ry * 0.18,
                hx + rx * 0.70, hy - ry * 0.26
            ),
            LineTo(hx + rx * 0.67, hy - ry * 0.24),
            CubicTo(
                hx + rx * 0.61, hy - ry * 0.16,
                hx + rx * 0.48, hy - ry * 0.06,
                hx + rx * 0.38, hy + ry * 0.06
            ),
            ClosePath
        )
    )

    return Horns(
        leftHorn = leftPath,
        rightHorn = rightPath,
        leftAccent = leftAccent,
        rightAccent = rightAccent
    )
}

/**
 * Constructs cooling glasses (sunglasses) geometry fitted over the eye coordinates.
 */
fun buildSunglasses(eyes: List<Eye>): Sunglasses? {
    if (eyes.size < 2) return null
    val e0 = eyes[0]
    val e1 = eyes[1]

    val lensRx0 = max(e0.rx * 2.2, 8.8)
    val lensRy0 = max(e0.ry * 2.0, 6.2)
    val lensRx1 = max(e1.rx * 2.2, 8.8)
    val lensRy1 = max(e1.ry * 2.0, 6.2)

    val leftLens = superellipse(
        Superellipse(
            cx = e0.cx,
            cy = e0.cy,
            rx = lensRx0,
            ry = lensRy0,
            n = 3.2,
            rot = e0.rot
        )
    )

    val rightLens = superellipse(
        Superellipse(
            cx = e1.cx,
            cy = e1.cy,
            rx = lensRx1,
            ry = lensRy1,
            n = 3.2,
            rot = e1.rot
        )
    )

    // Center bridge connecting both frames
    val bridgeLeftX = e0.cx + lensRx0 * 0.65
    val bridgeRightX = e1.cx - lensRx1 * 0.65
    val bridgeY = (e0.cy + e1.cy) / 2.0 - 1.2
    val bridgeThickness = 2.4

    val bridge = BlobPath(
        listOf(
            MoveTo(bridgeLeftX, bridgeY - bridgeThickness / 2.0),
            LineTo(bridgeRightX, bridgeY - bridgeThickness / 2.0),
            LineTo(bridgeRightX, bridgeY + bridgeThickness / 2.0),
            LineTo(bridgeLeftX, bridgeY + bridgeThickness / 2.0),
            ClosePath
        )
    )

    // Outer temples (ear stems)
    val leftTemple = BlobPath(
        listOf(
            MoveTo(e0.cx - lensRx0 * 0.8, e0.cy - 1.2),
            LineTo(e0.cx - lensRx0 * 1.35, e0.cy - 1.8),
            LineTo(e0.cx - lensRx0 * 1.35, e0.cy + 0.4),
            LineTo(e0.cx - lensRx0 * 0.8, e0.cy + 1.0),
            ClosePath
        )
    )

    val rightTemple = BlobPath(
        listOf(
            MoveTo(e1.cx + lensRx1 * 0.8, e1.cy - 1.2),
            LineTo(e1.cx + lensRx1 * 1.35, e1.cy - 1.8),
            LineTo(e1.cx + lensRx1 * 1.35, e1.cy + 0.4),
            LineTo(e1.cx + lensRx1 * 0.8, e1.cy + 1.0),
            ClosePath
        )
    )

    // Dynamic lens glare/glint lines
    val leftGlint = BlobPath(
        listOf(
            MoveTo(e0.cx - lensRx0 * 0.55, e0.cy - lensRy0 * 0.20),
            LineTo(e0.cx - lensRx0 * 0.15, e0.cy - lensRy0 * 0.62),
            LineTo(e0.cx - lensRx0 * 0.02, e0.cy - lensRy0 * 0.48),
            LineTo(e0.cx - lensRx0 * 0.42, e0.cy - lensRy0 * 0.06),
            ClosePath
        )
    )

    val rightGlint = BlobPath(
        listOf(
            MoveTo(e1.cx - lensRx1 * 0.55, e1.cy - lensRy1 * 0.20),
            LineTo(e1.cx - lensRx1 * 0.15, e1.cy - lensRy1 * 0.62),
            LineTo(e1.cx - lensRx1 * 0.02, e1.cy - lensRy1 * 0.48),
            LineTo(e1.cx - lensRx1 * 0.42, e1.cy - lensRy1 * 0.06),
            ClosePath
        )
    )

    return Sunglasses(
        leftLens = leftLens,
        rightLens = rightLens,
        bridge = bridge,
        leftTemple = leftTemple,
        rightTemple = rightTemple,
        leftGlint = leftGlint,
        rightGlint = rightGlint
    )
}

/**
 * Procedural crying tears for the "crying" expression.
 */
data class Tears(
    val leftStream: BlobPath,
    val rightStream: BlobPath,
    val leftDrop: BlobPath,
    val rightDrop: BlobPath,
    val leftHighlight: BlobPath,
    val rightHighlight: BlobPath,
    val tearColor: String = "#38BDF8",
    val highlightColor: String = "#E0F2FE"
)

/**
 * Constructs crying tears and droplets streaming down from each eye.
 */
fun buildTears(eyes: List<Eye>): Tears? {
    if (eyes.size < 2) return null
    val e0 = eyes[0]
    val e1 = eyes[1]

    val topY0 = e0.cy + e0.ry * 0.4
    val topY1 = e1.cy + e1.ry * 0.4

    // Left stream flowing down from eye
    val leftStream = BlobPath(
        listOf(
            MoveTo(e0.cx - 1.8, topY0),
            LineTo(e0.cx + 1.8, topY0),
            CubicTo(
                e0.cx + 2.5, topY0 + 4.5,
                e0.cx + 3.2, topY0 + 9.0,
                e0.cx + 2.8, topY0 + 13.0
            ),
            CubicTo(
                e0.cx + 2.4, topY0 + 16.2,
                e0.cx - 3.6, topY0 + 16.2,
                e0.cx - 3.2, topY0 + 13.0
            ),
            CubicTo(
                e0.cx - 2.8, topY0 + 9.0,
                e0.cx - 2.5, topY0 + 4.5,
                e0.cx - 1.8, topY0
            ),
            ClosePath
        )
    )

    // Left falling tear droplet
    val dropCenterY0 = topY0 + 20.0
    val leftDrop = BlobPath(
        listOf(
            MoveTo(e0.cx - 0.4, dropCenterY0 - 2.8),
            CubicTo(
                e0.cx + 1.8, dropCenterY0 - 0.5,
                e0.cx + 2.2, dropCenterY0 + 2.0,
                e0.cx - 0.4, dropCenterY0 + 2.8
            ),
            CubicTo(
                e0.cx - 2.6, dropCenterY0 + 2.0,
                e0.cx - 2.2, dropCenterY0 - 0.5,
                e0.cx - 0.4, dropCenterY0 - 2.8
            ),
            ClosePath
        )
    )

    // Left highlight shine
    val leftHighlight = BlobPath(
        listOf(
            MoveTo(e0.cx - 2.0, topY0 + 5.0),
            CubicTo(
                e0.cx - 2.4, topY0 + 8.5,
                e0.cx - 2.2, topY0 + 12.0,
                e0.cx - 1.2, topY0 + 14.0
            ),
            LineTo(e0.cx - 0.4, topY0 + 13.6),
            CubicTo(
                e0.cx - 1.4, topY0 + 11.8,
                e0.cx - 1.5, topY0 + 8.5,
                e0.cx - 1.2, topY0 + 5.0
            ),
            ClosePath
        )
    )

    // Right stream
    val rightStream = BlobPath(
        listOf(
            MoveTo(e1.cx - 1.8, topY1),
            LineTo(e1.cx + 1.8, topY1),
            CubicTo(
                e1.cx + 2.5, topY1 + 4.5,
                e1.cx + 2.8, topY1 + 9.0,
                e1.cx + 3.2, topY1 + 13.0
            ),
            CubicTo(
                e1.cx + 3.6, topY1 + 16.2,
                e1.cx - 2.4, topY1 + 16.2,
                e1.cx - 2.8, topY1 + 13.0
            ),
            CubicTo(
                e1.cx - 3.2, topY1 + 9.0,
                e1.cx - 2.5, topY1 + 4.5,
                e1.cx - 1.8, topY1
            ),
            ClosePath
        )
    )

    // Right falling tear droplet
    val dropCenterY1 = topY1 + 20.0
    val rightDrop = BlobPath(
        listOf(
            MoveTo(e1.cx + 0.4, dropCenterY1 - 2.8),
            CubicTo(
                e1.cx + 2.6, dropCenterY1 - 0.5,
                e1.cx + 2.2, dropCenterY1 + 2.0,
                e1.cx + 0.4, dropCenterY1 + 2.8
            ),
            CubicTo(
                e1.cx - 1.8, dropCenterY1 + 2.0,
                e1.cx - 2.2, dropCenterY1 - 0.5,
                e1.cx + 0.4, dropCenterY1 - 2.8
            ),
            ClosePath
        )
    )

    // Right highlight shine
    val rightHighlight = BlobPath(
        listOf(
            MoveTo(e1.cx - 1.2, topY1 + 5.0),
            CubicTo(
                e1.cx - 1.5, topY1 + 8.5,
                e1.cx - 1.4, topY1 + 11.8,
                e1.cx - 0.4, topY1 + 13.6
            ),
            LineTo(e1.cx + 0.4, topY1 + 14.0),
            CubicTo(
                e1.cx - 0.6, topY1 + 12.0,
                e1.cx - 0.7, topY1 + 8.5,
                e1.cx - 0.4, topY1 + 5.0
            ),
            ClosePath
        )
    )

    return Tears(
        leftStream = leftStream,
        rightStream = rightStream,
        leftDrop = leftDrop,
        rightDrop = rightDrop,
        leftHighlight = leftHighlight,
        rightHighlight = rightHighlight
    )
}
