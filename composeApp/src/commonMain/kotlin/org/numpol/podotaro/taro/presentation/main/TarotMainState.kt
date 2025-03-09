package org.numpol.podotaro.taro.presentation.main

import org.numpol.podotaro.core.presentation.UiText
import org.numpol.podotaro.taro.presentation.AppLanguage
import org.numpol.podotaro.taro.domain.FortuneRecord


data class TarotMainState(
    val fortuneRecords: List<FortuneRecord> = emptyList(),
    val language: AppLanguage = AppLanguage.TH,
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null,
    val currentScreen: TarotScreen = TarotScreen.Shuffle
)