package com.navbyte.blobavatar.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.BlobatarLayout
import com.navbyte.blobavatar.core.BlobatarOptions
import com.navbyte.blobavatar.core.COLOR_EYE
import com.navbyte.blobavatar.core.COLOR_HEAD
import com.navbyte.blobavatar.core.Expression
import com.navbyte.blobavatar.core.MotionFrame
import com.navbyte.blobavatar.core.MotionSeeds
import com.navbyte.blobavatar.core.Palette
import com.navbyte.blobavatar.core.Petal
import com.navbyte.blobavatar.core.Pose
import com.navbyte.blobavatar.core.backdropFor
import com.navbyte.blobavatar.core.expressionPalette
import com.navbyte.blobavatar.core.idle
import com.navbyte.blobavatar.core.motionSeeds
import com.navbyte.blobavatar.core.EmotionDynamics
import com.navbyte.blobavatar.core.identityEmotionDynamics
import com.navbyte.blobavatar.core.resolve
import com.navbyte.blobavatar.core.style
import kotlin.math.min

/**
 * Everything the animated renderer needs for one paint.
 */
data class AnimatedBlobatarFrame(
    val motion: MotionFrame,
    val pose: Pose,
    val headColor: Color,
    val eyeColor: Color,
    val hover: Double = 0.0,
    val amplitude: Double = 0.0,
    val emotionDynamics: EmotionDynamics = identityEmotionDynamics
)

/**
 * A resolved generation-2 figure whose paths stay fixed while transforms move.
 */
class AnimatedBlobatarRenderer(
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
    val basePalette: Palette
    val motionSeeds: MotionSeeds

    private val headPaint = Paint().apply { style = PaintingStyle.Fill }
    private val eyePaint = Paint().apply { style = PaintingStyle.Fill }
    private val backdropPaint = Paint().apply { style = PaintingStyle.Fill }

    init {
        val resolved = resolve(name, options)
        layout = style.layout(resolved.t)
        petals = layout.petals
        extraPaths = layout.extra.map { it.toComposePath() }
        bodyPath = layout.bodyPath().toComposePath()
        eyePaths = layout.eyePaths().map { it.toComposePath() }
        basePalette = resolved.palette
        motionSeeds = motionSeeds(resolved.t)

        val bg = backdropFor(options.background, basePalette, styleDefault = Backdrop.NONE)
        if (bg != null) {
            backdropPath = bg.path.toComposePath()
            backdropColor = colorFromHex(bg.fill)
        } else {
            backdropPath = null
            backdropColor = null
        }
    }

    fun paletteFor(expression: Expression?): Palette =
        expressionPalette(basePalette, expression ?: idle)

    val hasBackdrop: Boolean get() = backdropPath != null

    fun draw(drawScope: DrawScope, frame: AnimatedBlobatarFrame) {
        val size = drawScope.size
        val side = min(size.width, size.height)
        if (side <= 0f) return

        drawScope.drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate((size.width - side) / 2f, (size.height - side) / 2f)
            canvas.scale(side / 100f, side / 100f)

            if (backdropPath != null && backdropColor != null) {
                backdropPaint.color = backdropColor
                canvas.drawPath(backdropPath, backdropPaint)
            }

            val motion = frame.motion
            val pose = frame.pose
            val emotion = frame.emotionDynamics
            val hoverScale = (1.0 + 0.04 * frame.hover).toFloat()

            canvas.save()
            canvas.translate(
                (motion.shake.first + emotion.dx).toFloat(),
                (motion.shake.second + emotion.dy).toFloat()
            )
            canvas.translate(50f, 50f)
            if (emotion.rotation != 0.0) {
                canvas.rotate(emotion.rotation.toFloat())
            }
            if (emotion.scaleX != 1.0 || emotion.scaleY != 1.0) {
                canvas.scale(emotion.scaleX.toFloat(), emotion.scaleY.toFloat())
            }
            canvas.translate(0f, (-1.5 * frame.hover).toFloat())
            canvas.scale(hoverScale, hoverScale)
            canvas.translate(-50f, -50f)

            canvas.translate(50f, 50f)
            canvas.scale(motion.breathe.first.toFloat(), motion.breathe.second.toFloat())
            canvas.translate(-50f, -50f)

            canvas.translate(0f, (pose.bdy + motion.bob).toFloat())

            headPaint.color = frame.headColor
            for (p in petals) {
                canvas.drawCircle(Offset(p.cx.toFloat(), p.cy.toFloat()), p.r.toFloat(), headPaint)
            }
            for (extra in extraPaths) {
                canvas.drawPath(extra, headPaint)
            }
            canvas.drawPath(bodyPath, headPaint)

            canvas.save()
            canvas.translate(motion.saccade.first.toFloat(), motion.saccade.second.toFloat())
            for (index in eyePaths.indices) {
                paintEye(canvas, index, frame)
            }
            canvas.restore()

            canvas.restore()
            canvas.restore()
        }
    }

    private fun paintEye(
        canvas: androidx.compose.ui.graphics.Canvas,
        index: Int,
        frame: AnimatedBlobatarFrame
    ) {
        val eye = layout.eyes[index]
        val pose = frame.pose
        val motion = frame.motion
        val side = if (index == 0) -1.0 else 1.0
        val selected = if (index == 0) 0.0 else 1.0
        val phase = selected * (1.0 - pose.rock) +
            pose.rock * ((1.0 + side * motion.thinkingPhase) / 2.0)

        canvas.save()
        canvas.translate(
            (eye.cx + pose.edx * side).toFloat(),
            (eye.cy + pose.edy + phase * pose.edy2).toFloat()
        )
        val deg1 = (pose.tilt + selected * pose.tilt2) * side + eye.rot * (1.0 - pose.lock)
        canvas.rotate(deg1.toFloat())
        canvas.scale(
            (pose.esx + selected * pose.esx2).toFloat(),
            (pose.esy + selected * pose.esy2).toFloat()
        )
        canvas.rotate((-eye.rot).toFloat())
        canvas.translate((-eye.cx).toFloat(), (-eye.cy).toFloat())

        canvas.translate(eye.cx.toFloat(), eye.cy.toFloat())
        canvas.rotate((motion.wrap.rotation * side).toFloat())
        canvas.scale(
            (1.0 + motion.wrap.magnitudeX + motion.wrap.side * side).toFloat(),
            (1.0 + motion.wrap.scaleY).toFloat()
        )
        canvas.rotate(eye.rot.toFloat())
        canvas.scale(1f, motion.blink.toFloat())
        canvas.rotate((-eye.rot).toFloat())
        canvas.translate((-eye.cx).toFloat(), (-eye.cy).toFloat())

        eyePaint.color = frame.eyeColor
        canvas.drawPath(eyePaths[index], eyePaint)
        canvas.restore()
    }
}
