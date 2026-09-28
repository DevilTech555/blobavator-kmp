package com.navbyte.blobavatar.core

import platform.Foundation.NSString
import platform.Foundation.precomposedStringWithCanonicalMapping

internal actual fun nfcNormalize(s: String): String =
    (s as NSString).precomposedStringWithCanonicalMapping
