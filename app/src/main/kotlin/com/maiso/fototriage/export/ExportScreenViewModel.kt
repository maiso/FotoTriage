package com.maiso.fototriage.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maiso.fototriage.USBFileCopier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class ExportScreenUiState(
    val total: Int,
    val current: Int,
    val percentage: Int,
)

class ExportScreenViewModel(
    usbFileCopier: USBFileCopier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExportScreenUiState(0, 0, 0)
    )

    val uiState = _uiState.asStateFlow()

    init {
        usbFileCopier.progress.onEach { prog ->
            _uiState.update {
                it.copy(
                    total = prog.total,
                    current = prog.current,
                    percentage = prog.progress
                )
            }
        }.launchIn(viewModelScope)

    }

    companion object {
        class ExportScreenViewModelFactory(
            private val usbFileCopier: USBFileCopier,
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ExportScreenViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ExportScreenViewModel(usbFileCopier) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}