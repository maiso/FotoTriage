package com.maiso.fototriage.screens.phototriage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.database.PhotoDatabase
import com.maiso.fototriage.database.filterByMonth
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
    private val deletePhoto: (Photo) -> Unit,
) : ViewModel() {

    val uiState = MutableStateFlow(
        PhotoTriageUiState()
    )

    init {
        PhotoDatabase.photos.onEach { allPhotos ->
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
        deletePhoto(photo)
    }

    fun onTriagedPhoto(photo: Photo) {
        PhotoDatabase.markPhotoTriaged(photo)
    }

    fun onFavoritePhoto(photo: Photo) {
        PhotoDatabase.markPhotoFavorite(photo)
    }

    companion object {
        class PhotoTriageViewModelFactory(
            private val year: Year,
            private val month: Month,
            private val showAllPhotos: Boolean,
            private val onLastPhotoReached: () -> Unit,
            private val deletePhoto: (Photo) -> Unit
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PhotoTriageViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PhotoTriageViewModel(
                        year,
                        month,
                        showAllPhotos,
                        onLastPhotoReached,
                        deletePhoto
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}