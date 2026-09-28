package com.navbyte.blobavatar.core

private fun jsNormalize(s: String): String = js("s.normalize('NFC')")

internal actual fun nfcNormalize(s: String): String = jsNormalize(s)
