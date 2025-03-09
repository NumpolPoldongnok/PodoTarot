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

            /*
            var cardState1 = CardState(
                card = majorArcanaCards[0],
                id = 1,
                x = 0f,
                y = 0f,
                rotation = 0f,
                faceUp = true,
                selected = true,
                handIndex = 0,
                flipAngle = 0f
            )
            var cardState2 = CardState(
                card = majorArcanaCards[1],
                id = 1,
                x = 0f,
                y = 0f,
                rotation = 0f,
                faceUp = true,
                selected = true,
                handIndex = 0,
                flipAngle = 0f
            )
            var cardState3 = CardState(
                card = majorArcanaCards[2],
                id = 1,
                x = 0f,
                y = 0f,
                rotation = 0f,
                faceUp = true,
                selected = true,
                handIndex = 0,
                flipAngle = 0f
            )
            var cardState4 = CardState(
                card = majorArcanaCards[3],
                id = 1,
                x = 0f,
                y = 0f,
                rotation = 0f,
                faceUp = true,
                selected = true,
                handIndex = 0,
                flipAngle = 0f
            )
            var cardState5= CardState(
                card = majorArcanaCards[4],
                id = 1,
                x = 0f,
                y = 0f,
                rotation = 0f,
                faceUp = true,
                selected = true,
                handIndex = 0,
                flipAngle = 0f
            )
            FortuneResultScreen(
                cardStates = listOf(cardState1,cardState2,cardState3,cardState4,cardState5),
                language = AppLanguage.TH,
                onRestart = {  }
            )

             */
        }
    }
}