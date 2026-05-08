package com.maiso.fototriage.database

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
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
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.time.Month
import java.time.Year
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

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

    private var databaseHelpers: Map<String, DatabaseHelper> = emptyMap()

    private val _photos: MutableStateFlow<List<Photo>> = MutableStateFlow(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    val progress = MutableStateFlow<Triple<Int, Int, Int>?>(null)

    private fun helperForPhoto(photo: Photo): DatabaseHelper {
        val folder = File(photo.filePath).parent
            ?: error("No parent directory for ${photo.filePath}")
        return databaseHelpers[folder]
            ?: error("No database registered for folder $folder")
    }

    fun getAllPhotos(context: Context, folderPaths: List<String>, onFinished: suspend () -> Unit) {
        databaseHelpers = folderPaths.associateWith { DatabaseHelper(context, it) }

        databaseHelpers.forEach { (folder, helper) ->
            Log.d("FotoTriage", "DB entries for $folder: ${helper.getAllData().size}")
        }

        _photos.value = emptyList()
        progress.value = null

        coroutineScope.launch {
            // Collect all image files in the selected folders and ensure MediaStore knows about them
            val filesToScan = folderPaths.flatMap { folder ->
                File(folder).listFiles()
                    ?.filter { isImageFile(it) }
                    ?.map { it.absolutePath }
                    ?: emptyList()
            }
            Log.i("FotoTriage", "Triggering MediaStore scan for ${filesToScan.size} file(s)")
            scanIntoMediaStore(context, filesToScan)
            Log.i("FotoTriage", "MediaStore scan complete")

            val projection = arrayOf(
                Images.Media._ID,
                Images.Media.DISPLAY_NAME,
                Images.Media.DATA,
                Images.Media.DATE_TAKEN
            )

            val selection = folderPaths.joinToString(" OR ") { "${Images.Media.DATA} LIKE ?" }
            val selectionArgs = folderPaths.map { "$it/%" }.toTypedArray()

            context.contentResolver.query(
                Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${Images.Media.DATE_TAKEN} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(Images.Media.DATA)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(Images.Media.DATE_TAKEN)

                val totalCount = cursor.count
                Log.i("FotoTriage", "MediaStore returned $totalCount photo(s)")
                var currentCount = 0

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id)
                    val filePath = cursor.getString(dataColumn)
                    val fileName = cursor.getString(nameColumn)
                    val dateTakenMillis = cursor.getLong(dateTakenColumn)
                    val dateTaken = Date(dateTakenMillis)

                    val folder = File(filePath).parent
                    val helper = if (folder != null) databaseHelpers[folder] else null
                    if (helper == null) {
                        Log.w("FotoTriage", "No database helper for $filePath, skipping")
                        continue
                    }

                    val triaged = helper.addOrRetrieveEntry(fileName, dateTakenMillis)

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
                    progress.value = Triple(currentCount, totalCount, (currentCount * 100) / totalCount)
                }
            }

            Log.i("FotoTriage", "Loaded ${_photos.value.size} photo(s) across ${folderPaths.size} folder(s)")

            databaseHelpers.forEach { (folderPath, helper) ->
                val folderFileNames = _photos.value
                    .filter { File(it.filePath).parent == folderPath }
                    .map { it.fileName }
                helper.cleanUpDatabase(folderFileNames)
            }

            onFinished()
        }
    }

    private suspend fun scanIntoMediaStore(context: Context, paths: List<String>) {
        if (paths.isEmpty()) return
        suspendCancellableCoroutine { cont ->
            val remaining = AtomicInteger(paths.size)
            MediaScannerConnection.scanFile(
                context,
                paths.toTypedArray(),
                null
            ) { path, uri ->
                Log.d("FotoTriage", "MediaStore scanned: $path -> $uri")
                if (remaining.decrementAndGet() == 0) cont.resume(Unit)
            }
        }
    }

    /**
     * @param forceTriaged if true will mark photo as triaged regardless of previous state.
     * @param unfavorite if true will mark photo as non-favorite regardless of previous state.
     */
    fun markPhotoTriaged(photo: Photo, forceTriaged: Boolean = false, unfavorite: Boolean = false) {
        val triaged = if (forceTriaged) true else !photo.triaged
        val favorite = if (unfavorite) false else photo.favorite
        helperForPhoto(photo).insertData(
            PhotoDataBaseEntry(
                fileName = photo.fileName,
                dateTakenMillis = photo.dateTakenMillis,
                triaged = triaged,
                favorite = favorite,
            )
        )
        _photos.update { photos ->
            photos.map {
                if (it.fileName == photo.fileName) it.copy(triaged = triaged, favorite = favorite)
                else it
            }
        }
    }

    fun markPhotoFavorite(photo: Photo) {
        helperForPhoto(photo).insertData(
            PhotoDataBaseEntry(
                fileName = photo.fileName,
                dateTakenMillis = photo.dateTakenMillis,
                triaged = photo.triaged,
                favorite = !photo.favorite,
            )
        )
        _photos.update { photos ->
            photos.map {
                if (it.fileName == photo.fileName) it.copy(favorite = !photo.favorite)
                else it
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
                        photos.filterNot { it.filePath == path }
                    }
                } else {
                    toast(context, "Failed to delete $fileName")
                }
            } catch (e: SecurityException) {
                toast(context, "SecurityException deleting file:\n${e.message}")
            } catch (e: Exception) {
                toast(context, "Error deleting file:\n${e.localizedMessage}")
            }
        } else {
            toast(context, "$fileName not found on storage")
        }
    }

    private fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
