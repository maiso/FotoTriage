package com.maiso.fototriage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Month
import java.time.Year

data class PhotoTriageUiState(
    val photo: Uri? = null,
    val fileName: String? = null,
    val triaged: Boolean = false,
    val favorite: Boolean = false,
)

class PhotoTriageViewModel(
    year: Year,
    month: Month,
    private val onLastPhotoReached: () -> Unit,
) : ViewModel() {

    private var photoNumber: Int = 0
    private var photos: List<Photo> = FotoDatabase.photos.filterByMonth(year, month)

    val uiState = MutableStateFlow(PhotoTriageUiState())

    init {
        Log.i("MVDB", "FotoDatabase: ${FotoDatabase.photos.size}")
        updateFoto()
    }

    fun onNextPhoto() {
        if (photoNumber + 1 > photos.size) {
            onLastPhotoReached()
        } else {
            photoNumber = (photoNumber + 1).coerceIn(photos.indices)

            Log.i("MVDB", "Select photo $photoNumber/${photos.size}")

            updateFoto()
        }
    }

    fun onPreviousPhoto() {
        photoNumber = (photoNumber - 1).coerceIn(photos.indices)
        Log.i("MVDB", "Select photo $photoNumber/${photos.size}")
        updateFoto()
    }

    private fun updateFoto() {
        uiState.update {
            val photo = photos[photoNumber]
            it.copy(
                photo = photo.uri,
                fileName = photo.fileName,
                triaged = photo.triaged
            )
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