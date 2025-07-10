package org.numpol.podotaro

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.numpol.podotaro.taro.presentation.main.TarotMainScreen
import org.numpol.podotaro.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        Surface(color = Color.LightGray) {
            TarotMainScreen()
        }
    }
}