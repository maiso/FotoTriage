package com.maiso.fototriage

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import coil3.ImageLoader
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
        enableEdgeToEdge()
        setContent {
            FotoTriageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val mainViewModel: MainViewModel =
                        viewModel(factory = MainViewModel.Factory)

                    val uiState by mainViewModel.uiState.collectAsState()

                    PhotoTriage(
                        uiState,
                        onPreviousPhoto = mainViewModel::onPreviousPhoto,
                        onNextPhoto = mainViewModel::onNextPhoto,
                        modifier = Modifier.padding(padding),
                    )
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, you can now access the photos
            } else {
                // Permission denied
                error("No read access")
            }
        }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the permission
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
//
//    fun getPhotosTakenLastMonth(contentResolver: ContentResolver): List<Uri> {
//        val uris = mutableListOf<Uri>()
//        val oneMonthAgo = Calendar.getInstance().apply {
//            add(Calendar.MONTH, -1)
//        }.timeInMillis
//
//        val projection = arrayOf(MediaStore.Images.Media._ID)
//        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
//        val selectionArgs = arrayOf(oneMonthAgo.toString())
//
//        val cursor: Cursor? = contentResolver.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            projection,
//            selection,
//            selectionArgs,
//            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
//        )
//
//        cursor?.use {
//            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
//            while (it.moveToNext()) {
//                val id = it.getLong(idColumn)
//                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
//                uris.add(contentUri)
//            }
//        }
//
//        return uris
//    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
