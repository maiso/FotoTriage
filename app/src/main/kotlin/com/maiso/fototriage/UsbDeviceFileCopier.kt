package com.maiso.fototriage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.maiso.fototriage.database.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

data class CopyProgress(
    val total: Int,
    val current: Int,
    val progress: Int,
)

class USBFileCopier(private val context: Context) {

    private val _progress = MutableStateFlow(CopyProgress(0, 0, 0))
    val progress: StateFlow<CopyProgress> = _progress.asStateFlow()

    @Suppress("Deprecated")
    fun copyPhotosToUsb(
        usbUri: Uri,
        photos: List<Photo>,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Create a folder on the USB stick
            val usbRoot = DocumentFile.fromTreeUri(context, usbUri)!!
            val localDate =
                photos[0].dateTaken.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val year = localDate.year

            val newFolder =
                usbRoot.createDirectory("FotoFavorites$year")!!

            // Copy each file to the USB stick
            var currentPhotoIndex = 0
            photos.forEach { photo ->
                currentPhotoIndex++
                try {
                    copyFileToUsb(photo, newFolder)
                } catch (e: Exception) {
                    e.message?.let { toast(it) }
                }
                _progress.update {
                    CopyProgress(
                        photos.size,
                        currentPhotoIndex,
                        ((currentPhotoIndex / 100) * photos.size)
                    )
                }
            }
        }
    }

    private fun copyFileToUsb(
        photo: Photo,
        targetFolder: DocumentFile,
    ) {
        val contentResolver: ContentResolver = context.contentResolver

        val mimeType = contentResolver.getType(photo.uri)
            ?: throw IllegalStateException("Unable to determine mimetype")

        // Create a new file in the target folder
        val targetFile = targetFolder.createFile(
            mimeType,
            photo.fileName
        ) ?: throw IllegalStateException("File Copy Failed to target File tar ${photo.fileName}.")

        // Open input stream for the source file
        contentResolver.openInputStream(photo.uri)?.use { inputStream ->
            // Open output stream for the target file
            contentResolver.openOutputStream(targetFile.uri)?.use { outputStream ->
                // Buffer for copying
                val buffer = ByteArray(1024)
                var length: Int
                val fileDesc = contentResolver.openFileDescriptor(photo.uri, "r")
                    ?: throw IllegalStateException("File Copy File failed fileDesc =-= null")

                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush() // Ensure all data is written
                fileDesc.close()
            }
                ?: throw IllegalStateException("File Copy Failed to open output stream for ${targetFile.name}.")
        } ?: throw IllegalStateException("File Copy Failed to open input stream for ${photo.uri}.")
    }

    private fun toast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}