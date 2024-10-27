package dev.sajidali.tvguide

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        window.title = "Compose TV Guide"
        App()
    }
}