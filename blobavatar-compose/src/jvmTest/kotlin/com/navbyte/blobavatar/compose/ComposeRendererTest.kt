package com.navbyte.blobavatar.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.BlobatarOptions
import com.navbyte.blobavatar.core.happy
import com.navbyte.blobavatar.core.mad
import com.navbyte.blobavatar.core.thinking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComposeRendererTest {

    @Test
    fun testStaticRendererCreationAndDraw() {
        val renderer = BlobatarRenderer("alain", BlobatarOptions(background = Backdrop.SQUIRCLE, expression = happy))
        assertNotNull(renderer.backdropPath)
        assertTrue(renderer.eyePaths.isNotEmpty())

        val bitmap = ImageBitmap(100, 100)
        val canvas = Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(100f, 100f)
        ) {
            renderer.draw(this)
        }
    }

    @Test
    fun testAnimatedRendererCreationAndDraw() {
        val renderer = AnimatedBlobatarRenderer("ada", BlobatarOptions(background = Backdrop.CIRCLE))
        val palette = renderer.paletteFor(thinking)

        val frame = AnimatedBlobatarFrame(
            motion = com.navbyte.blobavatar.core.motionAt(renderer.motionSeeds, 1500.0, 1.0),
            pose = thinking.pose,
            headColor = colorFromHex(palette[com.navbyte.blobavatar.core.COLOR_HEAD]!!),
            eyeColor = colorFromHex(palette[com.navbyte.blobavatar.core.COLOR_EYE]!!),
            hover = 0.5,
            amplitude = 1.0
        )

        val bitmap = ImageBitmap(200, 200)
        val canvas = Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density = Density(2f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(200f, 200f)
        ) {
            renderer.draw(this, frame)
        }
    }

    @Test
    fun testPathConversion() {
        val path = com.navbyte.blobavatar.core.box(50.0, 50.0, 20.0, 20.0).toComposePath()
        assertNotNull(path)
    }
}
