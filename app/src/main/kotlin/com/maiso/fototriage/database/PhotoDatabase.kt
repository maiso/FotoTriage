package com.maiso.fototriage.database

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video

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
import java.time.Month
import java.time.Year
import java.util.Calendar
import java.util.Date
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
    val isVideo: Boolean = false,
)

fun List<Photo>.filterByYear(year: Year): List<Photo> = this.filter { photo ->
    val calendar = Calendar.getInstance().apply { time = photo.dateTaken }
    calendar.get(Calendar.YEAR) == year.value
}

fun List<Photo>.filterByMonth(year: Year, month: Month): List<Photo> = this.filter { photo ->
    val calendar = Calendar.getInstance().apply { time = photo.dateTaken }
    calendar.get(Calendar.YEAR) == year.value &&
            (calendar.get(Calendar.MONTH) + 1) == month.value
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

    private data class RawMediaItem(
        val uri: Uri,
        val fileName: String,
        val filePath: String,
        val dateTakenMillis: Long,
        val isVideo: Boolean,
    )

    private fun helperForPhoto(photo: Photo): DatabaseHelper {
        val folder = File(photo.filePath).parent
            ?: error("No parent directory for ${photo.filePath}")
        return databaseHelpers[folder]
            ?: error("No database registered for folder $folder")
    }

    fun getAllPhotos(context: Context, folderPaths: List<String>, onFinished: suspend () -> Unit) {
        databaseHelpers = folderPaths.associateWith { DatabaseHelper(context, it) }
        _photos.value = emptyList()
        progress.value = null

        coroutineScope.launch {
            val selection = folderPaths.joinToString(" OR ") { "${MediaStore.MediaColumns.DATA} LIKE ?" }
            val selectionArgs = folderPaths.map { "$it/%" }.toTypedArray()

            syncNewFilesToMediaStore(context, folderPaths, selection, selectionArgs)

            val rawItems = queryMediaStoreItems(context, selection, selectionArgs)
            Log.i("FotoTriage", "MediaStore returned ${rawItems.count { !it.isVideo }} image(s) and ${rawItems.count { it.isVideo }} video(s)")

            val dbMaps = databaseHelpers.mapValues { (_, helper) -> helper.loadAllAsMap() }
            val (photoList, newEntriesByFolder) = buildPhotoList(rawItems, dbMaps)

            persistNewEntries(newEntriesByFolder)

            _photos.value = photoList
            Log.i("FotoTriage", "Loaded ${photoList.size} item(s) across ${folderPaths.size} folder(s)")

            cleanUpStaleDatabaseEntries(photoList)
            onFinished()
        }
    }

    private suspend fun syncNewFilesToMediaStore(
        context: Context,
        folderPaths: List<String>,
        selection: String,
        selectionArgs: Array<String>,
    ) {
        val alreadyIndexed = mutableSetOf<String>()
        for (contentUri in listOf(Images.Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI)) {
            context.contentResolver.query(
                contentUri,
                arrayOf(MediaStore.MediaColumns.DATA),
                selection, selectionArgs, null
            )?.use { cursor ->
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (cursor.moveToNext()) alreadyIndexed += cursor.getString(dataCol)
            }
        }
        val filesToScan = folderPaths.flatMap { folder ->
            File(folder).listFiles()
                ?.filter { isMediaFile(it) && it.absolutePath !in alreadyIndexed }
                ?.map { it.absolutePath }
                ?: emptyList()
        }
        Log.i("FotoTriage", "Scanning ${filesToScan.size} new file(s) into MediaStore (${alreadyIndexed.size} already indexed)")
        if (filesToScan.isNotEmpty()) {
            val scanTotal = filesToScan.size
            progress.value = Triple(0, scanTotal, 0)
            scanIntoMediaStore(context, filesToScan) { scanned ->
                progress.value = Triple(scanned, scanTotal, (scanned * 100) / scanTotal)
            }
        }
        Log.i("FotoTriage", "MediaStore scan complete")
    }

    private fun queryMediaStoreItems(
        context: Context,
        selection: String,
        selectionArgs: Array<String>,
    ): List<RawMediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DATE_TAKEN,
        )
        val items = mutableListOf<RawMediaItem>()

        fun readCursor(contentUri: Uri, isVideo: Boolean) {
            context.contentResolver.query(
                contentUri, projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                while (cursor.moveToNext()) {
                    items += RawMediaItem(
                        uri = ContentUris.withAppendedId(contentUri, cursor.getLong(idCol)),
                        fileName = cursor.getString(nameCol),
                        filePath = cursor.getString(dataCol),
                        dateTakenMillis = cursor.getLong(dateCol),
                        isVideo = isVideo,
                    )
                }
            }
        }

        readCursor(Images.Media.EXTERNAL_CONTENT_URI, isVideo = false)
        readCursor(Video.Media.EXTERNAL_CONTENT_URI, isVideo = true)
        items.sortBy { it.dateTakenMillis }
        return items
    }

    private fun buildPhotoList(
        rawItems: List<RawMediaItem>,
        dbMaps: Map<String, Map<String, Pair<Boolean, Boolean>>>,
    ): Pair<List<Photo>, Map<String, List<PhotoDataBaseEntry>>> {
        val totalCount = rawItems.size
        progress.value = Triple(0, totalCount, 0)

        val photoList = ArrayList<Photo>(totalCount)
        val newEntriesByFolder = mutableMapOf<String, MutableList<PhotoDataBaseEntry>>()

        for ((index, item) in rawItems.withIndex()) {
            val folder = File(item.filePath).parent ?: continue
            if (databaseHelpers[folder] == null) {
                Log.w("FotoTriage", "No database helper for ${item.filePath}, skipping")
                continue
            }
            val (triaged, favorite) = dbMaps[folder]?.get(item.fileName) ?: run {
                newEntriesByFolder.getOrPut(folder) { mutableListOf() } +=
                    PhotoDataBaseEntry(item.fileName, item.dateTakenMillis, false, false)
                false to false
            }
            photoList += Photo(
                uri = item.uri,
                filePath = item.filePath,
                fileName = item.fileName,
                dateTaken = Date(item.dateTakenMillis),
                dateTakenMillis = item.dateTakenMillis,
                triaged = triaged,
                favorite = favorite,
                isVideo = item.isVideo,
            )
            if (index % 50 == 49 || index == totalCount - 1) {
                val current = index + 1
                progress.value = Triple(current, totalCount, (current * 100) / totalCount)
            }
        }

        return photoList to newEntriesByFolder
    }

    private fun persistNewEntries(newEntriesByFolder: Map<String, List<PhotoDataBaseEntry>>) {
        for ((folder, entries) in newEntriesByFolder) {
            Log.i("FotoTriage", "Inserting ${entries.size} new DB entries for $folder")
            databaseHelpers[folder]?.insertBatch(entries)
        }
    }

    private fun cleanUpStaleDatabaseEntries(photoList: List<Photo>) {
        databaseHelpers.forEach { (folderPath, helper) ->
            val folderFileNames = photoList
                .filter { File(it.filePath).parent == folderPath }
                .map { it.fileName }
            helper.cleanUpDatabase(folderFileNames)
        }
    }

    private suspend fun scanIntoMediaStore(context: Context, paths: List<String>, onProgress: (scanned: Int) -> Unit = {}) {
        if (paths.isEmpty()) return
        suspendCancellableCoroutine { cont ->
            val remaining = AtomicInteger(paths.size)
            val scanned = AtomicInteger(0)
            MediaScannerConnection.scanFile(
                context,
                paths.toTypedArray(),
                null
            ) { path, uri ->
                Log.d("FotoTriage", "MediaStore scanned: $path -> $uri")
                onProgress(scanned.incrementAndGet())
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
