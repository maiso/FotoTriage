package com.maiso.fototriage

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.Drawable
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
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers

data class PhotoTriageUiState(
    val photo: Uri? = null,
)

class MainViewModel(application: Application) : ViewModel() {
    private val appContext: Context = application.applicationContext

    private var photoNumber: Int = 0
    private var photos: List<Uri> = emptyList()

    val uiState = MutableStateFlow(PhotoTriageUiState())

    init {
        viewModelScope.launch {

            getAllPhotos().let { listOfPhotos ->
                photos = listOfPhotos
                uiState.update { it.copy(photo = photos[0]) }
            }

        }
    }

    fun onNextPhoto() {
        photoNumber = (photoNumber + 1).coerceIn(photos.indices)

        Log.i("MVDB", "Select photo $photoNumber/${photos.size}")
        uiState.update { it.copy(photo = photos[photoNumber]) }
    }

    fun onPreviousPhoto() {
        photoNumber = (photoNumber - 1).coerceIn(photos.indices)
        Log.i("MVDB", "Select photo $photoNumber/${photos.size}")
        uiState.update { it.copy(photo = photos[photoNumber]) }
    }


    private fun getAllPhotos(): MutableList<Uri> {

        val projection = arrayOf(
            Images.Media._ID,
            Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val galleryImageUrls = mutableListOf<Uri>()
        val orderBy = Images.Media.DATE_TAKEN
        val selection = "${Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/DCIM/Camera/%") // Filter for images in the Camera folder

        appContext.contentResolver.query(
            Images.Media.EXTERNAL_CONTENT_URI,
            projection,//            columns,
            selection,
            selectionArgs,
            "$orderBy DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(Images.Media.DATA)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(Images.Media.DATE_TAKEN)


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                Log.i(
                    "MVDB",
                    "Photo $idColumn ${cursor.getString(nameColumn)} ${cursor.getString(dataColumn)}  ${
                        cursor.getString(dateTakenColumn)
                    }"
                )

                galleryImageUrls.add(
                    ContentUris.withAppendedId(
                        Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            }
        }
        Log.i("MVDB", "Got ${galleryImageUrls.size} photos")

        galleryImageUrls.forEach {
            Log.i("MVDB", "Photo $it")

        }
        preCacheImages(galleryImageUrls)
        return galleryImageUrls
    }

    private fun preCacheImages(photos : List<Uri>) {

        viewModelScope.launch(Dispatchers.IO) {
            for (uri in photos) {
                val request = ImageRequest.Builder(appContext)
                    .data(uri)
                    .target(
                        onSuccess = {
                            // Handle successful image loading
                            // You can log or perform any action with the loaded drawable
                            Log.i("MVDB","Image loaded successfully: $uri")
                        },
                        onError = {
                            // Handle error in loading image
                            Log.i("MVDB","Error loading image: $uri")
                        }
                    )
                    .build()

                    SingletonImageLoader.get(appContext).enqueue(request) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel((this[APPLICATION_KEY]!!))
            }
        }
    }
}