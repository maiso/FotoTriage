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
    private val onLastPhotoReached: () -> Unit,
) : ViewModel() {

    private var photos: List<Photo> = FotoDatabase.photos.filterByMonth(year, month)

    val uiState = MutableStateFlow(
        PhotoTriageUiState(
            photos = photos
        )
    )

    init {
        Log.i("MVDB", "FotoDatabase: ${FotoDatabase.photos.size}")
    }

    fun onDeletePhoto(photo: Photo) {
        Log.i("MVDB", "Delete photo $photo")
    }

    fun onTriagedPhoto(photo: Photo) {
        Log.i("MVDB", "Traiged photo $photo")
        FotoDatabase.markFotoTriaged(photo)
        photos = FotoDatabase.photos.filterByMonth(year, month)

        if (!photos.any { !it.triaged && !it.favorite }) {
            onLastPhotoReached()
        }
    }

    fun onFavoritePhoto(photo: Photo) {
        Log.i("MVDB", "Favorite photo $photo")
        FotoDatabase.markFotoFavorite(photo)
        photos = FotoDatabase.photos.filterByMonth(year, month)

        if (!photos.any { !it.triaged && !it.favorite }) {
            onLastPhotoReached()
        }
    }

    companion object {
        class PhotoTriageViewModelFactory(
            private val year: Year,
            private val month: Month,
            private val onLastPhotoReached: () -> Unit
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PhotoTriageViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PhotoTriageViewModel(year, month, onLastPhotoReached) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}