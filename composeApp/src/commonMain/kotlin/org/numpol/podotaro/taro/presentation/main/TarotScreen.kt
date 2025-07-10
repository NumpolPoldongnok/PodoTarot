package org.numpol.podotaro.taro.presentation.main

import org.numpol.podotaro.taro.presentation.CardState
import org.numpol.podotaro.taro.domain.FortuneRecord

// --- Navigation State ---
sealed interface TarotScreen {
    data object Home : TarotScreen
    data object Shuffle : TarotScreen
    data object History : TarotScreen
    data class Fortune(val fortuneRecord: FortuneRecord) : TarotScreen
}