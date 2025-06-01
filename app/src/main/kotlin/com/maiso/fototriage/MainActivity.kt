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
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
        enableEdgeToEdge()

        FotoDatabase.getAllPhotos(
            application.applicationContext
        ) {
            withContext(Dispatchers.Main) {
                navController.navigate("OverViewScreen")
            }
        }
        setContent {
            navController = rememberNavController()

            FotoTriageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavHost(navController = navController, startDestination = "LoadingScreen") {
                    composable("LoadingScreen") {
                        val loadingScreenViewModel: LoadingScreenViewModel =
                            viewModel(factory = LoadingScreenViewModel.Factory)

                        val uiState by loadingScreenViewModel.uiState.collectAsState()

                        LoadingScreen(uiState)
                    }
                    composable("OverViewScreen") {
                        val overviewScreenViewModel: OverviewScreenViewModel =
                            viewModel(factory = OverviewScreenViewModel.Factory)

                        val uiState by overviewScreenViewModel.uiState.collectAsState()

                        OverviewScreen(
                            uiState,
                            modifier = Modifier.padding(padding),
                            onClick = {
                                navController.navigate("FotoTriage")
                            }
                        )
                    }
                    composable("FotoTriage") {
                        val photoTriageViewModel: PhotoTriageViewModel =
                            viewModel(factory = PhotoTriageViewModel.Factory)

                        val uiState by photoTriageViewModel.uiState.collectAsState()

                        PhotoTriage(
                            uiState,
                            onPreviousPhoto = photoTriageViewModel::onPreviousPhoto,
                            onNextPhoto = photoTriageViewModel::onNextPhoto,
                            modifier = Modifier.padding(padding),
                        )
                    }


                }

                }
            }
        }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this) {

            Log.i(
                "MVDB",
                "${navController.currentDestination?.route}"
            )
            // Check if the current destination is Screen2
            if (navController.currentDestination?.route == "FotoTriage") {
                // If on Screen2, finish the activity to exit the app
                finish()
            } else {
                // Otherwise, use the default back button behavior
                navController.popBackStack()
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
