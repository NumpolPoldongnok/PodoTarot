package org.numpol.podotaro.taro.presentation.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.numpol.podotaro.taro.presentation.CardState
import org.numpol.podotaro.taro.presentation.FortuneRecord

class TarotMainViewModel: ViewModel() {

    // The current navigation screen state.
    var currentScreen by mutableStateOf<TarotScreen>(TarotScreen.Shuffle)
        private set

    // History is maintained as a mutable state list.
    val fortuneHistory = mutableStateListOf<FortuneRecord>()

    // Navigation actions:
    fun navigateToHome() {
        currentScreen = TarotScreen.Home
    }

    fun navigateToShuffle() {
        currentScreen = TarotScreen.Shuffle
    }

    fun navigateToFullScreen(cardState: CardState) {
        currentScreen = TarotScreen.FullScreen(cardState)
    }

    fun navigateToHistory() {
        currentScreen = TarotScreen.History
    }

    fun navigateToFortune(record: FortuneRecord) {
        currentScreen = TarotScreen.Fortune(record)
    }

    fun recordFortune(record: FortuneRecord) {
        fortuneHistory.add(record)
        currentScreen = TarotScreen.Fortune(record)
    }

    fun restart() {
        currentScreen = TarotScreen.Home
    }
}
