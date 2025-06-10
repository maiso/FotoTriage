package com.maiso.fototriage

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    }.distinct().toSet() // Convert to Set to ensure uniqueness
}


object FotoDatabase {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var databaseHelper: DatabaseHelper

    private val _photos: MutableList<Photo> = mutableListOf()
    val photos: List<Photo> = _photos

    val progress = MutableStateFlow<Triple<Int, Int, Int>?>(null)

    fun getAllPhotos(context: Context, onFinished: suspend () -> Unit) {

        databaseHelper = DatabaseHelper(context)

        _photos.clear()

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
                "$orderBy DESC"
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
                        "MVDB",
                        "Photo ID:$id " +
                                "URI: $uri" +
                                "NAME:${fileName} " +
                                "PATH:${filePath}  " +
                                "DATETAKEN:${cursor.getString(dateTakenColumn)} " +
                                "FORMATTED:$formattedDate"
                    )


                    val triaged = databaseHelper.isFileExists(fileName)
                    Log.i("MVDB", "File in database: $triaged")

                    _photos.add(
                        Photo(
                            uri = uri,
                            filePath = filePath,
                            fileName = fileName,
                            dateTaken = dateTaken,
                            dateTakenMillis = dateTakenMillis,
                            triaged = triaged,
                            favorite = false
                        )
                    )

                    currentCount++
                    val percentage = ((currentCount * 100) / totalCount)
                    progress.value = Triple(currentCount, totalCount, percentage)

                    //TODO Check for any files in database but not on device.
                }
            }
            Log.i("MVDB", "Got ${_photos.size} photos")

            onFinished()
//        preCacheImages(galleryImageUrls)
        }
    }

    fun markFotoTriaged(foto: Photo) {
        databaseHelper.insertData(
            FotoDataBaseEntry(
                fileName = foto.fileName,
                dateTakenMillis = foto.dateTakenMillis,
                favorite = false,
            )
        )
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
//                            Log.i("MVDB", "Image loaded successfully: $uri")
//                        },
//                        onError = {
//                            // Handle error in loading image
//                            Log.i("MVDB", "Error loading image: $uri")
//                        }
//                    )
//                    .build()
//
//                SingletonImageLoader.get(appContext).enqueue(request)
//            }
//        }
//    }

    fun filterDatesByYear(dates: List<Date>, year: Int): List<Date> {
        return dates.filter { date ->
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.get(Calendar.YEAR) == year
        }
    }

    fun filterDatesByMonth(dates: List<Date>, year: Int, month: Int): List<Date> {
        return dates.filter { date ->
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.get(Calendar.YEAR) == year && (calendar.get(Calendar.MONTH) + 1) == month // Month is 0-based
        }
    }

}