package org.numpol.podotaro.taro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel
import org.numpol.podotaro.taro.presentation.card_shuffle.CardShuffleScreen
import org.numpol.podotaro.taro.presentation.components.FullScreenCardView
import org.numpol.podotaro.taro.presentation.fortune_result.FortuneResultScreen
import org.numpol.podotaro.taro.presentation.history.HistoryScreen
import org.numpol.podotaro.taro.presentation.main.TarotScreen
import org.numpol.podotaro.taro.presentation.main.TarotMainViewModel

@Composable
fun TarotMainScreen(
    tarotCards: List<TarotCard> = allTarotCards,
    viewModel: TarotMainViewModel = koinViewModel(),
) {
    when (val screen = viewModel.currentScreen) {
        is TarotScreen.Home -> {
            LaunchedEffect(true) {
                // Delay for a splash effect, then navigate to shuffle.
                viewModel.navigateToShuffle()
            }
        }
        is TarotScreen.Shuffle -> {
            CardShuffleScreen(
                tarotCards = tarotCards,
                fortuneHistory = viewModel.fortuneHistory,
                onShowHistory = { viewModel.navigateToHistory() },
                onShowFortune = { fortuneRecord -> viewModel.navigateToFortune(fortuneRecord) },
                onRestart = { viewModel.restart() }
            )
        }
        is TarotScreen.FullScreen -> {
            FullScreenCardView(
                cardState = screen.cardState,
                currentLanguage = AppLanguage.EN,
                onClick = { viewModel.navigateToShuffle() }
            )
        }
        is TarotScreen.History -> {
            HistoryScreen(
                history = viewModel.fortuneHistory,
                onSelect = { record -> viewModel.navigateToFortune(record) },
                onClose = { viewModel.navigateToShuffle() }
            )
        }
        is TarotScreen.Fortune -> {
            FortuneResultScreen(
                cardStates = screen.fortuneRecord.cardStates,
                language = AppLanguage.EN,
                onRestart = { viewModel.navigateToShuffle() }
            )
        }
    }
}
