package org.numpol.podotaro

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.numpol.podotaro.taro.presentation.TarotMainScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(color = Color.LightGray) {
            TarotMainScreen()
        }
    }
}