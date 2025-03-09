package org.numpol.podotaro.taro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.numpol.podotaro.taro.presentation.card_shuffle.CardShuffleScreen
import org.numpol.podotaro.taro.presentation.components.FullScreenCardView
import org.numpol.podotaro.taro.presentation.fortune_result.FortuneResultScreen
import org.numpol.podotaro.taro.presentation.history.HistoryScreen
import org.numpol.podotaro.taro.presentation.main.TarotMainAction
import org.numpol.podotaro.taro.presentation.main.TarotScreen
import org.numpol.podotaro.taro.presentation.main.TarotMainViewModel

@Composable
fun TarotMainScreen(
    tarotCards: List<TarotCard> = allTarotCards,
    viewModel: TarotMainViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val screen = viewModel.currentScreen) {
        is TarotScreen.Home -> {
            LaunchedEffect(true) {
                // Delay for a splash effect, then navigate to shuffle.
                viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
            }
        }

        is TarotScreen.Shuffle -> {
            CardShuffleScreen(
                tarotCards = tarotCards,
                onShowHistory = {
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.History))
                },
                onShowFortune = { fortuneRecord ->
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Fortune(fortuneRecord)))
                },
                onRestart = {
                    viewModel.onAction(TarotMainAction.OnRestart)
                }
            )
        }

        is TarotScreen.FullScreen -> {
            FullScreenCardView(
                tarotCard = screen.cardState.card,
                currentLanguage = state.language,
                onClick = {
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
                }
            )
        }

        is TarotScreen.History -> {
            HistoryScreen(
                history = state.fortuneRecords,
                onSelect = { record ->
                    //viewModel.navigateToFortune(record)
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Fortune(record)))
                },
                onClose = {
                    //viewModel.navigateToShuffle()
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
                }
            )
        }

        is TarotScreen.Fortune -> {
            FortuneResultScreen(
                cardIds = screen.fortuneRecord.cards,
                language = AppLanguage.EN,
                onRestart = {
                    //viewModel.navigateToShuffle()
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
                },
            )
        }
    }
}
