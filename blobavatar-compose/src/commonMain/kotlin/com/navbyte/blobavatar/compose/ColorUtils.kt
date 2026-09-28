package com.navbyte.blobavatar.compose

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color {
    val clean = if (hex.startsWith("#")) hex.substring(1) else hex
    val value = clean.toLong(16)
    return when (clean.length) {
        6 -> Color(
            red = ((value shr 16) and 255).toInt(),
            green = ((value shr 8) and 255).toInt(),
            blue = (value and 255).toInt(),
            alpha = 255
        )
        8 -> Color(
            alpha = ((value shr 24) and 255).toInt(),
            red = ((value shr 16) and 255).toInt(),
            green = ((value shr 8) and 255).toInt(),
            blue = (value and 255).toInt()
        )
        else -> Color.Black
    }
}
