package com.maiso.fototriage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Year

data class FavoriteOverviewUiState(
    val photos: List<Photo> = emptyList(),
)

class FavoriteOverviewViewModel(
    private val year: Year,
) : ViewModel() {

    private var photos: List<Photo> = FotoDatabase.photos.filterByYear(year).filter { it.favorite }

    val uiState = MutableStateFlow(
        FavoriteOverviewUiState(
            photos = photos
        )
    )

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