package org.numpol.podotaro.taro.presentation.main

import org.numpol.podotaro.taro.presentation.TarotCard

sealed interface TarotMainAction {
    data class OnNavigateTo(val screen: TarotScreen): TarotMainAction
    data object OnRestart: TarotMainAction
    data class OnRecordFortune(val cards: List<TarotCard>)
    data object OnChangeLanguage: TarotMainAction
}