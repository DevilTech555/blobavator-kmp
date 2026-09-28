package com.navbyte.blobavatar.core

import java.text.Normalizer

internal actual fun nfcNormalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFC)
