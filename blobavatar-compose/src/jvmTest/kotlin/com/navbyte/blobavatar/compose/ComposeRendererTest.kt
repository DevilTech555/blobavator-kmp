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

    @Test
    fun testCoolAndMischievousAccessories() {
        // Test static cool sunglasses
        val coolRenderer = BlobatarRenderer("alain", BlobatarOptions(expression = com.navbyte.blobavatar.core.cool))
        assertTrue(coolRenderer.shadesFramePaths.isNotEmpty(), "Cool should have sunglasses frame paths")

        // Test static mischievous horns
        val mischRenderer = BlobatarRenderer("alain", BlobatarOptions(expression = com.navbyte.blobavatar.core.mischievous))
        assertTrue(mischRenderer.hornPaths.isNotEmpty(), "Mischievous should have horn paths")

        // Test static crying tears
        val cryingRenderer = BlobatarRenderer("alain", BlobatarOptions(expression = com.navbyte.blobavatar.core.crying))
        assertTrue(cryingRenderer.tearStreamPaths.isNotEmpty(), "Crying should have tear stream paths")

        // Test animated renderer drawing with accessories
        val animRenderer = AnimatedBlobatarRenderer("alain")
        val coolFrame = AnimatedBlobatarFrame(
            motion = com.navbyte.blobavatar.core.motionAt(animRenderer.motionSeeds, 0.0, 1.0),
            pose = com.navbyte.blobavatar.core.cool.pose,
            headColor = androidx.compose.ui.graphics.Color.Blue,
            eyeColor = androidx.compose.ui.graphics.Color.White,
            expressionName = "cool",
            accessoryAlpha = 1.0f
        )
        val mischFrame = AnimatedBlobatarFrame(
            motion = com.navbyte.blobavatar.core.motionAt(animRenderer.motionSeeds, 0.0, 1.0),
            pose = com.navbyte.blobavatar.core.mischievous.pose,
            headColor = androidx.compose.ui.graphics.Color.Magenta,
            eyeColor = androidx.compose.ui.graphics.Color.White,
            expressionName = "mischievous",
            accessoryAlpha = 1.0f
        )
        val cryingFrame = AnimatedBlobatarFrame(
            motion = com.navbyte.blobavatar.core.motionAt(animRenderer.motionSeeds, 0.0, 1.0),
            pose = com.navbyte.blobavatar.core.crying.pose,
            headColor = androidx.compose.ui.graphics.Color.Cyan,
            eyeColor = androidx.compose.ui.graphics.Color.White,
            expressionName = "crying",
            accessoryAlpha = 1.0f
        )

        val bitmap = ImageBitmap(100, 100)
        val canvas = Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(Density(1f), LayoutDirection.Ltr, canvas, Size(100f, 100f)) {
            coolRenderer.draw(this)
            mischRenderer.draw(this)
            cryingRenderer.draw(this)
            animRenderer.draw(this, coolFrame)
            animRenderer.draw(this, mischFrame)
            animRenderer.draw(this, cryingFrame)
        }
    }
}
