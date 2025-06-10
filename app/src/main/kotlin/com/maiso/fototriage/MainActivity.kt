package com.maiso.fototriage

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Month
import java.time.Year
import java.util.Calendar

sealed interface Dest {
    data object LoadingScreen

    data object OverviewScreen

    data class PhotoTriageScreen(
        val year: Year,
        val month: Month
    )

    data object TriageFinished

}

class MainActivity : ComponentActivity() {

    private lateinit var backStack: SnapshotStateList<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
        enableEdgeToEdge()

        FotoDatabase.getAllPhotos(
            application.applicationContext
        ) {
            withContext(Dispatchers.Main) {
                backStack.add(Dest.OverviewScreen)
                backStack.remove(Dest.LoadingScreen)

            }
        }
        setContent {
            backStack = remember { mutableStateListOf(Dest.LoadingScreen) }

            FotoTriageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<Dest.LoadingScreen> { _ ->
                                val loadingScreenViewModel: LoadingScreenViewModel =
                                    viewModel(factory = LoadingScreenViewModel.Factory)

                                val uiState by loadingScreenViewModel.uiState.collectAsState()

                                LoadingScreen(uiState, modifier = Modifier.fillMaxSize())
                            }
                            entry<Dest.OverviewScreen> { _ ->
                                val overviewScreenViewModel: OverviewScreenViewModel =
                                    viewModel(factory = OverviewScreenViewModel.Factory)

                                val uiState by overviewScreenViewModel.uiState.collectAsState()

                                OverviewScreen(
                                    uiState,
                                    modifier = Modifier.padding(padding),
                                    onClick = { year, month ->
                                        backStack.add(
                                            Dest.PhotoTriageScreen(
                                                year, month
                                            )
                                        )
                                    }
                                )
                            }
                            entry<Dest.PhotoTriageScreen> { key ->
                                val factory =
                                    PhotoTriageViewModel.Companion.PhotoTriageViewModelFactory(
                                        key.year,
                                        key.month
                                    ) {
                                        backStack.add(Dest.TriageFinished)
                                    }
                                val photoTriageViewModel: PhotoTriageViewModel =
                                    viewModel(factory = factory)

                                val uiState by photoTriageViewModel.uiState.collectAsState()

                                PhotoTriage(
                                    uiState,
                                    onPreviousPhoto = photoTriageViewModel::onPreviousPhoto,
                                    onNextPhoto = photoTriageViewModel::onNextPhoto,
                                    onDeletePhoto = photoTriageViewModel::onDeletePhoto,
                                    modifier = Modifier.padding(padding),
                                )
                            }
                            entry<Dest.TriageFinished> { _ ->
                                TriageFinished()
                            }
                        }
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
                Log.e("MVDB", "Permission denied")
            }
        }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        Log.i("MVDB", "permissions: $permission")

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the permission
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    fun getPhotosTakenLastMonth(contentResolver: ContentResolver): List<Uri> {
        val uris = mutableListOf<Uri>()
        val oneMonthAgo = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.timeInMillis

        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
        val selectionArgs = arrayOf(oneMonthAgo.toString())

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                uris.add(contentUri)
            }
        }

        return uris
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
