package com.navbyte.blobavatar.core

/**
 * Pure SVG generator for blobatars.
 *
 * Emits clean, deterministic, dependency-free SVG strings that can be saved
 * to disk, rendered in HTML, sent over network, or used in web views.
 */
object SvgRenderer {
    private fun escapeXml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    /**
     * Renders a static blobatar into an SVG markup string.
     */
    fun toSvg(
        name: String,
        options: BlobatarOptions = BlobatarOptions(),
        size: Int? = null,
        title: String? = null
    ): String {
        val (layout, palette) = partsFor(name, options)
        val bg = backdropFor(options.background, palette)

        val dim = if (size != null) " width=\"$size\" height=\"$size\"" else ""
        val titleTag = if (title != null) "<title>${escapeXml(title)}</title>" else ""

        val bgTag = if (bg != null) {
            "<path d=\"${bg.path.toPathData()}\" fill=\"${bg.fill}\"/>"
        } else ""

        val petalsTag = layout.petals.joinToString("") { p ->
            "<circle cx=\"${r2(p.cx)}\" cy=\"${r2(p.cy)}\" r=\"${r2(p.r)}\"/>"
        }
        val extraTag = layout.extra.joinToString("") { e ->
            "<path d=\"${e.toPathData()}\"/>"
        }
        val bodyPathTag = "<path d=\"${layout.bodyPath().toPathData()}\"/>"

        val headGroup = "<g fill=\"${palette[COLOR_HEAD]}\">$petalsTag$extraTag$bodyPathTag</g>"

        val eyePaths = layout.eyePaths()
        val eyesTag = eyePaths.joinToString("") { e ->
            "<path d=\"${e.toPathData()}\"/>"
        }
        val eyeGroup = "<g fill=\"${palette[COLOR_EYE]}\">$eyesTag</g>"

        val inner = if (layout.bodyOffsetY != 0.0) {
            "<g transform=\"translate(0, ${r2(layout.bodyOffsetY)})\">$headGroup$eyeGroup</g>"
        } else {
            "$headGroup$eyeGroup"
        }

        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"$dim>$titleTag$bgTag$inner</svg>"
    }
}
