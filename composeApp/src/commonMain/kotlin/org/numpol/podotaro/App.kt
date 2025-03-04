package org.numpol.podotaro

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.numpol.podotaro.taro.CardShuffleScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(color = Color.LightGray) {
//            val viewModel = remember { GameViewModel() }
//            TokimekiGameScreen(viewModel)

            CardShuffleScreen()
        }
    }
}