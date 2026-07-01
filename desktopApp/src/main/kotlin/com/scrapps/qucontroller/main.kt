package com.scrapps.qucontroller

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Qu Controller",
    ) {
        App()
    }
}