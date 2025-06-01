package com.maiso.fototriage

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

data class PhotoTriageUiState(
    val photo: Uri? = null,
)

class PhotoTriageViewModel : ViewModel() {

    private var photoNumber: Int = 0
    private var photos: List<Photo> = FotoDatabase.photos

    val uiState = MutableStateFlow(PhotoTriageUiState())

    init {
        Log.i("MVDB", "FotoDatabase: ${FotoDatabase.photos.size}")
        uiState.update { it.copy(photo = photos[0].uri) }
    }

    fun onNextPhoto() {
        photoNumber = (photoNumber + 1).coerceIn(photos.indices)

        Log.i("MVDB", "Select photo $photoNumber/${photos.size}")
        uiState.update { it.copy(photo = photos[photoNumber].uri) }
    }

    fun onPreviousPhoto() {
        photoNumber = (photoNumber - 1).coerceIn(photos.indices)
        Log.i("MVDB", "Select photo $photoNumber/${photos.size}")
        uiState.update { it.copy(photo = photos[photoNumber].uri) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PhotoTriageViewModel()
            }
        }
    }
}