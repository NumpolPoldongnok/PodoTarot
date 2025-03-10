package org.numpol.podotaro.taro.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.numpol.podotaro.taro.presentation.TarotCard
import org.numpol.podotaro.taro.presentation.allTarotCards
import org.numpol.podotaro.taro.presentation.card_shuffle.CardShuffleScreen
import org.numpol.podotaro.taro.presentation.fortune_result.FortuneResultScreen
import org.numpol.podotaro.taro.presentation.history.HistoryScreen

@Composable
fun TarotMainScreen(
    tarotCards: List<TarotCard> = allTarotCards,
    viewModel: TarotMainViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val screen = state.currentScreen) {
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
                },
                currentLanguage = state.currentLanguage,
                onChangeLanguage = {
                    viewModel.onAction(TarotMainAction.OnChangeLanguage)
                }
            )
        }

        is TarotScreen.History -> {
            HistoryScreen(
                history = state.fortuneRecords,
                onSelect = { record ->
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Fortune(record)))
                },
                onClose = {
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
                },
                currentLanguage = state.currentLanguage,
                onChangeLanguage = { viewModel.onAction(TarotMainAction.OnChangeLanguage)}
            )
        }

        is TarotScreen.Fortune -> {
            FortuneResultScreen(
                cardIds = screen.fortuneRecord.cards,
                currentLanguage = state.currentLanguage,
                onChangeLanguage = {
                    viewModel.onAction(TarotMainAction.OnChangeLanguage)
                },
                onRestart = {
                    viewModel.onAction(TarotMainAction.OnNavigateTo(TarotScreen.Shuffle))
                },
            )
        }
    }
}
