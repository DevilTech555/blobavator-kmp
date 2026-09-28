package com.navbyte.blobavatar.core

/**
 * Top-level entry point for blobavatar core calculations and SVG rendering.
 */
object Blobavatar {
    const val PARITY_VERSION = "2.4.0"

    fun toSvg(
        name: String,
        options: BlobavatarOptions = BlobavatarOptions(),
        size: Int? = null,
        title: String? = null
    ): String = SvgRenderer.toSvg(name, options, size, title)

    fun layout(name: String, options: BlobavatarOptions = BlobavatarOptions()): BlobatarLayout =
        layoutFor(name, options)

    fun parts(name: String, options: BlobavatarOptions = BlobavatarOptions()): Pair<BlobatarLayout, Palette> =
        partsFor(name, options)

    fun motion(name: String, normalize: Boolean = true, traits: TraitOverrides? = null): MotionSeeds =
        motionSeedsFor(name, normalize, traits)
}

val Blobatar = Blobavatar
typealias BlobavatarLayout = BlobatarLayout
typealias BlobavatarStyle = BlobatarStyle
