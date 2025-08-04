package com.maiso.fototriage.database

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.Month
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Photo(
    val uri: Uri,
    val fileName: String,
    val filePath: String,
    val dateTaken: Date,
    val dateTakenMillis: Long,
    val triaged: Boolean,
    val favorite: Boolean,
)

fun List<Photo>.filterByYear(year: Year): List<Photo> = this.filter { photo ->
    val calendar = Calendar.getInstance().apply { time = photo.dateTaken }
    calendar.get(Calendar.YEAR) == year.value
}

fun List<Photo>.filterByMonth(year: Year, month: Month): List<Photo> = this.filter { photo ->
    val calendar = Calendar.getInstance().apply { time = photo.dateTaken }
    calendar.get(Calendar.YEAR) == year.value &&
            (calendar.get(Calendar.MONTH) + 1) == month.value // Month is 0-based
}

fun List<Photo>.findUniqueYears(): Set<Year> {
    return this.map { photo ->
        val calendar = Calendar.getInstance().apply { time = photo.dateTaken }
        Year.of(calendar.get(Calendar.YEAR))
    }.distinct().toSet()
}


object PhotoDatabase {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var databaseHelper: DatabaseHelper

    private val _photos: MutableStateFlow<List<Photo>> = MutableStateFlow(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    val progress = MutableStateFlow<Triple<Int, Int, Int>?>(null)

    private fun getUniqueParentFolders(contentResolver: ContentResolver): Set<String> {
        val parentFolders = mutableSetOf<String>() // Use a Set to store unique parent folder paths

        // Define the projection to specify which columns to retrieve
        val projection = arrayOf(Images.Media.DATA)

        // Query the MediaStore for images
        val cursor = contentResolver.query(
            Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(Images.Media.DATA)
            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                val parentFolder = File(imagePath).parent // Get the parent folder
                if (parentFolder != null && parentFolder.endsWith("/DCIM/Camera")) {
                    parentFolders.add(parentFolder) // Add to Set
                }
            }
        }

        return parentFolders
    }

    fun getAllPhotos(context: Context, onFinished: suspend () -> Unit) {

        val imageLocation = getUniqueParentFolders(context.contentResolver)
        if (imageLocation.size != 1) {
            Log.e("FotoTriage", "Image locations: $imageLocation")

            toast(context, "Images are in different locations. Not supported.")
            return
        }
        Log.d("FotoTriage", "Image location: ${imageLocation.first()}")

        databaseHelper = DatabaseHelper(context, imageLocation.first())

        _photos.value = emptyList()

        databaseHelper.getAllData().let {
            Log.d("FotoTriage", "Db entires: ${it.size}")
            it.forEach {
                Log.d("FotoTriage", "Db Entry: $it")
            }
        }

        coroutineScope.launch {
            val projection = arrayOf(
                Images.Media._ID,
                Images.Media.DISPLAY_NAME,
                Images.Media.DATA,
                Images.Media.DATE_TAKEN
            )

            val orderBy = Images.Media.DATE_TAKEN

            // Define the selection criteria
            val selection = "${Images.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/DCIM/Camera/%")

            context.contentResolver.query(
                Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "$orderBy ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(Images.Media.DATA)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(Images.Media.DATE_TAKEN)

                val totalCount = cursor.count
                var currentCount = 0
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id)

                    val dateTakenMillis = cursor.getLong(dateTakenColumn)
                    val dateTaken = Date(dateTakenMillis)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedDate = dateFormat.format(dateTaken)

                    val filePath = cursor.getString(dataColumn)
                    val fileName = cursor.getString(nameColumn)

                    Log.i(
                        "FotoTriage",
                        "Photo ID:$id " +
                                "URI: $uri " +
                                "NAME:${fileName} " +
                                "PATH:${filePath}  " +
                                "DATETAKEN:${cursor.getString(dateTakenColumn)} " +
                                "FORMATTED:$formattedDate"
                    )


                    val triaged = databaseHelper.addOrRetrieveEntry(fileName, dateTakenMillis)

                    _photos.update {
                        it + Photo(
                            uri = uri,
                            filePath = filePath,
                            fileName = fileName,
                            dateTaken = dateTaken,
                            dateTakenMillis = dateTakenMillis,
                            triaged = triaged.first,
                            favorite = triaged.second
                        )
                    }
                    currentCount++
                    val percentage = ((currentCount * 100) / totalCount)
                    progress.value = Triple(currentCount, totalCount, percentage)

                    //TODO Check for any files in database but not on device.
                }
            }
            Log.i("FotoTriage", "Got ${_photos.value.size} photos")

            databaseHelper.cleanUpDatabase(_photos.value.map { it.fileName })
            onFinished()
//        preCacheImages(galleryImageUrls)
        }
    }

    fun markPhotoTriaged(photo: Photo) {
        databaseHelper.insertData(
            PhotoDataBaseEntry(
                fileName = photo.fileName,
                dateTakenMillis = photo.dateTakenMillis,
                triaged = !photo.triaged,
                favorite = photo.favorite,
            )
        )
        //TODO check result
        _photos.update { photos ->
            photos.map {
                if (it.fileName == photo.fileName) {
                    it.copy(triaged = !photo.triaged)
                } else {
                    it
                }
            }
        }
    }

    fun markPhotoFavorite(photo: Photo) {
        databaseHelper.insertData(
            PhotoDataBaseEntry(
                fileName = photo.fileName,
                dateTakenMillis = photo.dateTakenMillis,
                triaged = photo.triaged,
                favorite = !photo.favorite,
            )
        )
        _photos.update { photos ->
            photos.map {
                if (it.fileName == photo.fileName) {
                    it.copy(favorite = !photo.favorite)
                } else {
                    it
                }
            }
        }
    }

    fun deleteFile(context: Context, fileName: String, path: String) {
        val target = File(path)
        if (target.isDirectory) {
            toast(context, "Target is a directory, not deleting: $path")
            return
        }

        if (target.exists()) {
            try {
                val deleted: Boolean = target.delete()
                if (deleted) {
                    _photos.update { photos ->
                        photos.filterNot {
                            it.filePath == path
                        }
                    }
                    toast(context, "$fileName deleted successfully")
                } else {
                    toast(context, "Failed  to delete $fileName")
                }
            } catch (e: SecurityException) {
                toast(context, "SecurityException deleting file:\n${e.message}")
            } catch (e: Exception) {
                toast(context, "Error deleting file:\n${e.localizedMessage}")
            }
        } else {
            // file not found
            toast(context, "$fileName not found on storage")
        }
    }

    private fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

//    private fun preCacheImages(photos: List<Uri>) {
//
//        coroutineScope.launch {
//            for (uri in photos) {
//                val request = ImageRequest.Builder(context)
//                    .data(uri)
//                    .target(
//                        onSuccess = {
//                            // Handle successful image loading
//                            // You can log or perform any action with the loaded drawable
//                            Log.i("FotoTriage", "Image loaded successfully: $uri")
//                        },
//                        onError = {
//                            // Handle error in loading image
//                            Log.i("FotoTriage", "Error loading image: $uri")
//                        }
//                    )
//                    .build()
//
//                SingletonImageLoader.get(appContext).enqueue(request)
//            }
//        }
//    }
//}