package com.navbyte.blobavatar.sample

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Blobavatar Studio (KMP)",
        state = rememberWindowState(width = 1100.dp, height = 840.dp)
    ) {
        App()
    }
}
