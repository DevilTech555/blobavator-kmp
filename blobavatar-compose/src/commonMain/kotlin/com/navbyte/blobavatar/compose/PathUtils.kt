package com.navbyte.blobavatar.compose

import androidx.compose.ui.graphics.Path
import com.navbyte.blobavatar.core.BlobPath
import com.navbyte.blobavatar.core.ClosePath
import com.navbyte.blobavatar.core.CubicTo
import com.navbyte.blobavatar.core.HorizontalLineTo
import com.navbyte.blobavatar.core.LineTo
import com.navbyte.blobavatar.core.MoveTo
import com.navbyte.blobavatar.core.QuadTo
import com.navbyte.blobavatar.core.VerticalLineTo

/**
 * Converts a deterministic [BlobPath] into an `androidx.compose.ui.graphics.Path`.
 */
fun BlobPath.toComposePath(): Path {
    val path = Path()
    var curX = 0f
    var curY = 0f

    for (seg in segments) {
        when (seg) {
            is MoveTo -> {
                curX = seg.x.toFloat()
                curY = seg.y.toFloat()
                path.moveTo(curX, curY)
            }
            is LineTo -> {
                curX = seg.x.toFloat()
                curY = seg.y.toFloat()
                path.lineTo(curX, curY)
            }
            is CubicTo -> {
                curX = seg.x.toFloat()
                curY = seg.y.toFloat()
                path.cubicTo(
                    seg.c1x.toFloat(), seg.c1y.toFloat(),
                    seg.c2x.toFloat(), seg.c2y.toFloat(),
                    curX, curY
                )
            }
            is QuadTo -> {
                curX = seg.x.toFloat()
                curY = seg.y.toFloat()
                path.quadraticTo(
                    seg.cx.toFloat(), seg.cy.toFloat(),
                    curX, curY
                )
            }
            is HorizontalLineTo -> {
                curX = seg.x.toFloat()
                path.lineTo(curX, curY)
            }
            is VerticalLineTo -> {
                curY = seg.y.toFloat()
                path.lineTo(curX, curY)
            }
            is ClosePath -> {
                path.close()
            }
        }
    }
    return path
}
