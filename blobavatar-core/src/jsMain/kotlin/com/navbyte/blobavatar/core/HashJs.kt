package com.navbyte.blobavatar.core

internal actual fun nfcNormalize(s: String): String =
    s.asDynamic().normalize("NFC") as String
