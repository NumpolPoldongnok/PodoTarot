package org.numpol.podotaro.taro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import org.numpol.podotaro.taro.presentation.card_shuffle.CardShuffleScreen
import org.numpol.podotaro.taro.presentation.components.FullScreenCardView
import org.numpol.podotaro.taro.presentation.fortune_result.FortuneResultScreen
import org.numpol.podotaro.taro.presentation.history.HistoryScreen

// --- Navigation State ---
sealed class TarotScreenState {
    data object Home : TarotScreenState()
    data object Shuffle : TarotScreenState()
    data class FullScreen(val cardState: CardState) : TarotScreenState()
    data object History : TarotScreenState()
    data class Fortune(val fortuneRecord: FortuneRecord) : TarotScreenState()
}

// --- Main Navigation Composable ---
@Composable
fun TarotMainScreen(
    tarotCards: List<TarotCard> = allTarotCards,
    fortuneHistory: MutableList<FortuneRecord> = remember { mutableStateListOf() }
) {
    var currentScreen by remember { mutableStateOf<TarotScreenState>(TarotScreenState.Shuffle) }

    when (val screen = currentScreen) {
        is TarotScreenState.Home -> {
            LaunchedEffect(true) {
                delay(100)
                currentScreen = TarotScreenState.Shuffle
            }
        }
        is TarotScreenState.Shuffle -> {
            CardShuffleScreen(
                tarotCards = tarotCards,
                fortuneHistory = fortuneHistory,
                onShowHistory = {
                    currentScreen = TarotScreenState.History
                },
                onShowFortune = { fortuneRecord ->
                    currentScreen = TarotScreenState.Fortune(fortuneRecord)
                },
                onRestart = {
                    currentScreen = TarotScreenState.Home
                }
            )
        }
        is TarotScreenState.FullScreen -> {
            FullScreenCardView(
                cardState = screen.cardState,
                currentLanguage = AppLanguage.EN, // Adjust language as needed.
                onClick = {
                    currentScreen = TarotScreenState.Shuffle
                }
            )
        }
        is TarotScreenState.History -> {
            HistoryScreen(
                history = fortuneHistory,
                onSelect = { record ->
                    currentScreen = TarotScreenState.Fortune(record)
                },
                onClose = {
                    currentScreen = TarotScreenState.Shuffle
                }
            )
        }
        is TarotScreenState.Fortune -> {
            FortuneResultScreen(
                cardStates = screen.fortuneRecord.cardStates,
                language = AppLanguage.EN,
                onRestart = {
                    currentScreen = TarotScreenState.Shuffle
                }
            )
        }
    }
}