package com.maiso.fototriage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.Year

data class FavoriteOverviewUiState(
    val photos: List<Photo> = emptyList(),
)

class FavoriteOverviewViewModel(
    private val year: Year,
) : ViewModel() {
    val uiState = MutableStateFlow(
        FavoriteOverviewUiState()
    )

    init {
        FotoDatabase.photos.onEach { photos ->
            uiState.update {
                it.copy(photos = photos.filterByYear(year).filter { it.favorite })
            }
        }.launchIn(viewModelScope)
    }

    companion object {
        class FavoriteOverviewViewModelFactory(
            private val year: Year,
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FavoriteOverviewViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FavoriteOverviewViewModel(year) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}