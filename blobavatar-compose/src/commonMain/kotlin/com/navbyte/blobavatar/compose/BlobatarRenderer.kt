package com.navbyte.blobavatar.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.BlobatarLayout
import com.navbyte.blobavatar.core.BlobatarOptions
import com.navbyte.blobavatar.core.COLOR_EYE
import com.navbyte.blobavatar.core.COLOR_HEAD
import com.navbyte.blobavatar.core.Petal
import com.navbyte.blobavatar.core.backdropFor
import com.navbyte.blobavatar.core.partsFor
import kotlin.math.min

/**
 * Pure static renderer for a blobatar.
 *
 * Resolves one seed's layout and palette once, then paints the generation-2
 * figure onto a Compose `DrawScope` — mapping the 100-by-100 viewBox onto
 * the largest centered square.
 */
import com.navbyte.blobavatar.core.buildHorns
import com.navbyte.blobavatar.core.buildSunglasses
import com.navbyte.blobavatar.core.buildTears

class BlobatarRenderer(
    val name: String,
    val options: BlobatarOptions = BlobatarOptions()
) {
    val layout: BlobatarLayout
    val petals: List<Petal>
    val extraPaths: List<Path>
    val bodyPath: Path
    val eyePaths: List<Path>
    val backdropPath: Path?
    val backdropColor: Color?
    val headColor: Color
    val eyeColor: Color

    val hornPaths: List<Path>
    val hornAccentPaths: List<Path>
    val hornFillColor: Color
    val hornAccentColor: Color

    val shadesFramePaths: List<Path>
    val shadesGlintPaths: List<Path>
    val shadesFrameColor: Color
    val shadesGlintColor: Color

    val tearStreamPaths: List<Path>
    val tearHighlightPaths: List<Path>
    val tearColor: Color
    val tearHighlightColor: Color

    init {
        val (l, palette) = partsFor(name, options)
        layout = l
        petals = l.petals
        extraPaths = l.extra.map { it.toComposePath() }
        bodyPath = l.bodyPath().toComposePath()
        eyePaths = l.eyePaths().map { it.toComposePath() }

        headColor = colorFromHex(palette[COLOR_HEAD]!!)
        eyeColor = colorFromHex(palette[COLOR_EYE]!!)

        val horns = if (options.expression?.name == "mischievous") buildHorns(layout.body) else null
        hornPaths = horns?.let { listOf(it.leftHorn.toComposePath(), it.rightHorn.toComposePath()) } ?: emptyList()
        hornAccentPaths = horns?.let { listOf(it.leftAccent.toComposePath(), it.rightAccent.toComposePath()) } ?: emptyList()
        hornFillColor = horns?.let { colorFromHex(it.fillColor) } ?: Color.Transparent
        hornAccentColor = horns?.let { colorFromHex(it.accentColor) } ?: Color.Transparent

        val sunglasses = if (options.expression?.name == "cool") buildSunglasses(layout.eyes) else null
        shadesFramePaths = sunglasses?.let {
            listOf(it.leftLens.toComposePath(), it.rightLens.toComposePath(), it.bridge.toComposePath(), it.leftTemple.toComposePath(), it.rightTemple.toComposePath())
        } ?: emptyList()
        shadesGlintPaths = sunglasses?.let {
            listOf(it.leftGlint.toComposePath(), it.rightGlint.toComposePath())
        } ?: emptyList()
        shadesFrameColor = sunglasses?.let { colorFromHex(it.frameColor) } ?: Color.Black
        shadesGlintColor = sunglasses?.let { colorFromHex(it.glintColor) } ?: Color.Cyan

        val tears = if (options.expression?.name == "crying") buildTears(layout.eyes) else null
        tearStreamPaths = tears?.let {
            listOf(it.leftStream.toComposePath(), it.rightStream.toComposePath(), it.leftDrop.toComposePath(), it.rightDrop.toComposePath())
        } ?: emptyList()
        tearHighlightPaths = tears?.let {
            listOf(it.leftHighlight.toComposePath(), it.rightHighlight.toComposePath())
        } ?: emptyList()
        tearColor = tears?.let { colorFromHex(it.tearColor) } ?: Color.Transparent
        tearHighlightColor = tears?.let { colorFromHex(it.highlightColor) } ?: Color.Transparent

        val bg = backdropFor(options.background, palette, styleDefault = Backdrop.NONE)
        if (bg != null) {
            backdropPath = bg.path.toComposePath()
            backdropColor = colorFromHex(bg.fill)
        } else {
            backdropPath = null
            backdropColor = null
        }
    }

    val hasBackdrop: Boolean get() = backdropPath != null

    fun draw(drawScope: DrawScope) {
        val size = drawScope.size
        val side = min(size.width, size.height)
        if (side <= 0f) return

        drawScope.withTransform({
            translate((size.width - side) / 2f, (size.height - side) / 2f)
            scale(side / 100f, side / 100f, Offset.Zero)
        }) {
            if (backdropPath != null && backdropColor != null) {
                drawPath(backdropPath, backdropColor)
            }
            withTransform({
                translate(0f, layout.bodyOffsetY.toFloat())
            }) {
                // Mischievous Horns
                for (p in hornPaths) {
                    drawPath(p, hornFillColor)
                }
                for (p in hornAccentPaths) {
                    drawPath(p, hornAccentColor)
                }

                for (p in petals) {
                    drawCircle(
                        color = headColor,
                        radius = p.r.toFloat(),
                        center = Offset(p.cx.toFloat(), p.cy.toFloat())
                    )
                }
                for (extra in extraPaths) {
                    drawPath(extra, headColor)
                }
                drawPath(bodyPath, headColor)
                for (eye in eyePaths) {
                    drawPath(eye, eyeColor)
                }

                // Crying Tears
                for (p in tearStreamPaths) {
                    drawPath(p, tearColor)
                }
                for (p in tearHighlightPaths) {
                    drawPath(p, tearHighlightColor)
                }

                // Cool Sunglasses
                for (p in shadesFramePaths) {
                    drawPath(p, shadesFrameColor)
                }
                for (p in shadesGlintPaths) {
                    drawPath(p, shadesGlintColor)
                }
            }
        }
    }
}
