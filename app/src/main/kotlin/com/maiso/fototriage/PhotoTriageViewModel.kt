package com.maiso.fototriage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.Month
import java.time.Year

data class PhotoTriageUiState(
    val photos: List<Photo> = emptyList(),
)

class PhotoTriageViewModel(
    private val year: Year,
    private val month: Month,
    private val showAllPhotos: Boolean,
    private val onLastPhotoReached: () -> Unit,
) : ViewModel() {

    val uiState = MutableStateFlow(
        PhotoTriageUiState()
    )

    init {
        FotoDatabase.photos.onEach { allPhotos ->
            allPhotos.filterByMonth(year, month).let { filteredPhotos ->
                if (filteredPhotos.isEmpty() || (!showAllPhotos && !filteredPhotos.any { !it.triaged && !it.favorite })) {
                    onLastPhotoReached()
                } else {
                    uiState.update {
                        it.copy(
                            photos =
                                if (!showAllPhotos) filteredPhotos.filter { !it.favorite && !it.triaged } else filteredPhotos
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)

    }

    fun onDeletePhoto(photo: Photo) {
        Log.i("MVDB", "Delete photo $photo")
    }

    fun onTriagedPhoto(photo: Photo) {
        FotoDatabase.markFotoTriaged(photo)
    }

    fun onFavoritePhoto(photo: Photo) {
        FotoDatabase.markFotoFavorite(photo)
    }

    companion object {
        class PhotoTriageViewModelFactory(
            private val year: Year,
            private val month: Month,
            private val showAllPhotos: Boolean,
            private val onLastPhotoReached: () -> Unit
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PhotoTriageViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PhotoTriageViewModel(year, month, showAllPhotos, onLastPhotoReached) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}