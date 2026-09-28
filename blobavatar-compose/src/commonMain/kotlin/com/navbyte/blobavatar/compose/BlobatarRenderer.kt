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

    init {
        val (l, palette) = partsFor(name, options)
        layout = l
        petals = l.petals
        extraPaths = l.extra.map { it.toComposePath() }
        bodyPath = l.bodyPath().toComposePath()
        eyePaths = l.eyePaths().map { it.toComposePath() }

        headColor = colorFromHex(palette[COLOR_HEAD]!!)
        eyeColor = colorFromHex(palette[COLOR_EYE]!!)

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
            }
        }
    }
}
