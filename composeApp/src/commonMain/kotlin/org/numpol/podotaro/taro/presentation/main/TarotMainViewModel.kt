package org.numpol.podotaro.taro.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.numpol.podotaro.taro.domain.FortuneRecordRepository
import org.numpol.podotaro.taro.domain.FortuneRecord
import org.numpol.podotaro.taro.presentation.AppLanguage

class TarotMainViewModel(
    private val fortuneRecordRepository: FortuneRecordRepository
): ViewModel() {

    private var observeFavoriteJob: Job? = null

    private val _state = MutableStateFlow(TarotMainState())
    val state = _state
        .onStart {
            observeFortuneRecords()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: TarotMainAction) {
        when (action) {
            is TarotMainAction.OnNavigateTo -> {
                when (action.screen) {
                    is TarotScreen.Fortune -> {
                        upsertRecord(action.screen.fortuneRecord)
                    }
                    else -> { }
                }
                _state.update { it.copy(currentScreen = action.screen) }
            }

            is TarotMainAction.OnRestart -> {
                _state.update { it.copy(currentScreen = TarotScreen.Home) }
            }

            TarotMainAction.OnChangeLanguage -> {
                _state.update {
                    val language: AppLanguage
                    if (it.currentLanguage == AppLanguage.TH) {
                        language = AppLanguage.EN
                    } else {
                        language = AppLanguage.TH
                    }
                    it.copy(currentLanguage = language)
                }
            }
        }
    }

    private fun upsertRecord(fortuneRecord: FortuneRecord) {
        viewModelScope.launch {
            fortuneRecordRepository.upsertRecord(fortuneRecord)
        }
    }
    private fun observeFortuneRecords() {
        observeFavoriteJob?.cancel()
        observeFavoriteJob = fortuneRecordRepository
            .getFortuneRecords()
            .onEach { fortuneRecords ->
                _state.update { it.copy(
                    fortuneRecords = fortuneRecords
                ) }
            }
            .launchIn(viewModelScope)
    }
}
