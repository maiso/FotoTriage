package com.maiso.fototriage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
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

    private var photos: List<Photo> = FotoDatabase.photos.filterByMonth(year, month).let {
        if (!showAllPhotos) it.filter { !it.favorite && !it.triaged } else it
    }

    val uiState = MutableStateFlow(
        PhotoTriageUiState(
            photos = photos
        )
    )

    init {
        if (photos.isEmpty()) {
            onLastPhotoReached()
        }
    }

    fun onDeletePhoto(photo: Photo) {
        Log.i("MVDB", "Delete photo $photo")
    }

    fun onTriagedPhoto(photo: Photo) {
        FotoDatabase.markFotoTriaged(photo)
        updatePhotos()
    }

    fun onFavoritePhoto(photo: Photo) {
        FotoDatabase.markFotoFavorite(photo)
        updatePhotos()
    }

    private fun updatePhotos() {
        photos = FotoDatabase.photos.filterByMonth(year, month)

        if (!photos.any { !it.triaged && !it.favorite }) {
            onLastPhotoReached()
        }
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