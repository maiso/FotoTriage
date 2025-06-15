package com.maiso.fototriage.screens.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maiso.fototriage.database.PhotoDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class StartTriageUiState(
    val isLoading: Boolean = true,
    val progressPercentage: Int = 0,
    val progressTotal: Int = 0,
    val progressCurrent: Int = 0
)

class LoadingScreenViewModel : ViewModel() {
    val uiState = MutableStateFlow(StartTriageUiState())

    init {
        uiState.update {
            it.copy(
                isLoading = true
            )
        }

        PhotoDatabase.progress.filterNotNull().onEach { (current, total, percentage) ->
            uiState.update {
                it.copy(
                    progressCurrent = current,
                    progressTotal = total,
                    progressPercentage = percentage
                )
            }
        }.launchIn(viewModelScope)
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoadingScreenViewModel()
            }
        }
    }
}