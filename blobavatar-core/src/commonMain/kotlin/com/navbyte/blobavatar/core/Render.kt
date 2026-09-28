package com.navbyte.blobavatar.core

/**
 * How the backdrop is drawn.
 */
enum class Backdrop {
    NONE,
    SQUARE,
    CIRCLE,
    SQUIRCLE
}

/**
 * Options configuring blobatar generation and rendering.
 */
data class BlobatarOptions(
    val palette: Map<String, String>? = null,
    val hue: Double? = null,
    val tone: Double? = null,
    val traits: Map<String, Any>? = null,
    val normalize: Boolean = true,
    val contrast: Boolean = true,
    val background: Backdrop? = null,
    val expression: Expression? = null,
    val animateEmotions: Boolean = true
)

typealias BlobavatarOptions = BlobatarOptions

/**
 * The resolved inputs: the trait reader and the final palette.
 */
data class Resolved(
    val t: Traits,
    val palette: Palette
)

/**
 * Normalizes and hashes the name once, builds the trait reader, and resolves
 * the palette — including the `hue`/`tone` friendly units and palette overrides.
 */
fun resolve(seed: String, opts: BlobatarOptions = BlobatarOptions()): Resolved {
    val t = traitsFor(
        seed,
        normalize = opts.normalize,
        overrides = opts.traits
    )
    val p = palette(
        hue = opts.hue ?: t.numIn("hue", 0.0, 360.0),
        enforce = opts.contrast,
        tone = opts.tone ?: t("tone")
    ).toMutableMap()

    if (opts.palette != null) {
        p.putAll(opts.palette)
    }
    return Resolved(t, p)
}

/**
 * The plate behind the figure, as geometry.
 */
data class BackdropGeometry(
    val path: BlobPath,
    val fill: String
)

/**
 * Resolves the backdrop plate geometry for the avatar.
 */
fun backdropFor(
    background: Backdrop?,
    p: Palette,
    styleDefault: Backdrop = Backdrop.NONE
): BackdropGeometry? {
    val bg = background ?: styleDefault
    return when (bg) {
        Backdrop.NONE -> null
        Backdrop.SQUARE -> BackdropGeometry(box(50.0, 50.0, 50.0, 50.0), p[COLOR_BG]!!)
        Backdrop.CIRCLE -> BackdropGeometry(
            superellipse(Superellipse(cx = 50.0, cy = 50.0, rx = 50.0, ry = 50.0, n = 2.0)),
            p[COLOR_BG]!!
        )
        Backdrop.SQUIRCLE -> BackdropGeometry(
            superellipse(Superellipse(cx = 50.0, cy = 50.0, rx = 50.0, ry = 50.0, n = 6.0)),
            p[COLOR_BG]!!
        )
    }
}

/**
 * The resolved layout for one seed.
 */
fun layoutFor(name: String, opts: BlobatarOptions = BlobatarOptions()): BlobatarLayout {
    val r = resolve(name, opts)
    val layout = style.layout(r.t)
    return if (opts.expression == null) layout else bakePose(layout, opts.expression.pose)
}

/**
 * Layout and palette together, for renderers that own the drawing.
 */
fun partsFor(name: String, opts: BlobatarOptions = BlobatarOptions()): Pair<BlobatarLayout, Palette> {
    val r = resolve(name, opts)
    val expression = opts.expression
    val layout = style.layout(r.t)
    if (expression == null) return Pair(layout, r.palette)
    return Pair(
        bakePose(layout, expression.pose),
        expressionPalette(r.palette, expression)
    )
}
