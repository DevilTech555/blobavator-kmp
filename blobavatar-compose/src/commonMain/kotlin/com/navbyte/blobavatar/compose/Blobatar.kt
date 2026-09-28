package com.navbyte.blobavatar.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.navbyte.blobavatar.core.BlobatarOptions

/**
 * Deterministic static blobavatar Composable.
 *
 * Paints the generation-2 layout through Compose Canvas primitives — the same math
 * the parity fixture pins against the TypeScript core.
 *
 * @param name The identity string (email, username, uuid) to generate the avatar from.
 * @param modifier The layout modifier.
 * @param size Fixed square size, or null to fill parent constraints with 1:1 aspect ratio.
 * @param options Deterministic options (palette, tone, hue, backdrop, traits, expression).
 * @param contentDescription Optional accessibility label.
 */
@Composable
fun Blobavatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    options: BlobatarOptions = BlobatarOptions(),
    contentDescription: String? = null
) {
    val renderer = remember(name, options) {
        BlobatarRenderer(name, options)
    }

    val boxModifier = (if (size != null) modifier.size(size) else modifier.aspectRatio(1f))
        .semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        }

    Canvas(modifier = boxModifier) {
        renderer.draw(this)
    }
}

@Composable
fun Blobatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    options: BlobatarOptions = BlobatarOptions(),
    contentDescription: String? = null
) = Blobavatar(name, modifier, size, options, contentDescription)
