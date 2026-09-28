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
        val hornsTag = if (options.expression?.name == "mischievous") {
            val horns = buildHorns(layout.body)
            "<g fill=\"${horns.fillColor}\"><path d=\"${horns.leftHorn.toPathData()}\"/><path d=\"${horns.rightHorn.toPathData()}\"/></g>" +
            "<g fill=\"${horns.accentColor}\"><path d=\"${horns.leftAccent.toPathData()}\"/><path d=\"${horns.rightAccent.toPathData()}\"/></g>"
        } else ""

        val bodyPathTag = "<path d=\"${layout.bodyPath().toPathData()}\"/>"
        val headGroup = "<g fill=\"${palette[COLOR_HEAD]}\">$petalsTag$extraTag$bodyPathTag</g>$hornsTag"

        val eyePaths = layout.eyePaths()
        val eyesTag = eyePaths.joinToString("") { e ->
            "<path d=\"${e.toPathData()}\"/>"
        }
        val eyeGroup = "<g fill=\"${palette[COLOR_EYE]}\">$eyesTag</g>"

        val sunglassesTag = if (options.expression?.name == "cool") {
            val shades = buildSunglasses(layout.eyes)
            if (shades != null) {
                "<g fill=\"${shades.frameColor}\"><path d=\"${shades.leftLens.toPathData()}\"/><path d=\"${shades.rightLens.toPathData()}\"/><path d=\"${shades.bridge.toPathData()}\"/><path d=\"${shades.leftTemple.toPathData()}\"/><path d=\"${shades.rightTemple.toPathData()}\"/></g>" +
                "<g fill=\"${shades.glintColor}\"><path d=\"${shades.leftGlint.toPathData()}\"/><path d=\"${shades.rightGlint.toPathData()}\"/></g>"
            } else ""
        } else ""

        val tearsTag = if (options.expression?.name == "crying") {
            val tears = buildTears(layout.eyes)
            if (tears != null) {
                "<g fill=\"${tears.tearColor}\"><path d=\"${tears.leftStream.toPathData()}\"/><path d=\"${tears.rightStream.toPathData()}\"/><path d=\"${tears.leftDrop.toPathData()}\"/><path d=\"${tears.rightDrop.toPathData()}\"/></g>" +
                "<g fill=\"${tears.highlightColor}\"><path d=\"${tears.leftHighlight.toPathData()}\"/><path d=\"${tears.rightHighlight.toPathData()}\"/></g>"
            } else ""
        } else ""

        val inner = if (layout.bodyOffsetY != 0.0) {
            "<g transform=\"translate(0, ${r2(layout.bodyOffsetY)})\">$headGroup$eyeGroup$sunglassesTag$tearsTag</g>"
        } else {
            "$headGroup$eyeGroup$sunglassesTag$tearsTag"
        }

        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"$dim>$titleTag$bgTag$inner</svg>"
    }
}
