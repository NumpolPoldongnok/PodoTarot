package org.numpol.podotaro

import androidx.compose.ui.window.ComposeUIViewController
import org.numpol.podotaro.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }