package com.maiso.fototriage

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
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

    data class FavoriteOverviewScreen(
        val year: Year
    )

    data object TriageFinished

}

class MainActivity : ComponentActivity() {

    private lateinit var backStack: SnapshotStateList<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission()
        enableEdgeToEdge()

        createNotificationChannel()
        if (!isNotificationScheduled(this)) {
            scheduleMonthlyNotification(this)
        } else {
            // Notification is already scheduled
        }

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
                        modifier = Modifier.padding(padding),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = listOf(
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
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
                                    onMonthClick = { year, month ->
                                        backStack.add(
                                            Dest.PhotoTriageScreen(
                                                year, month
                                            )
                                        )
                                    },
                                    onYearClick = { year ->
                                        backStack.add(
                                            Dest.FavoriteOverviewScreen(year)
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
                                    onTriagedPhoto = photoTriageViewModel::onTriagedPhoto,
                                    onFavoritePhoto = photoTriageViewModel::onFavoritePhoto,
                                    onDeletePhoto = photoTriageViewModel::onDeletePhoto,
                                    modifier = Modifier.padding(padding),
                                )
                            }
                            entry<Dest.TriageFinished> { _ ->
                                TriageFinished()
                            }
                            entry<Dest.FavoriteOverviewScreen> { key ->
                                val factory =
                                    FavoriteOverviewViewModel.Companion.FavoriteOverviewViewModelFactory(
                                        key.year,
                                    )
                                val favoriteOverviewViewModel: FavoriteOverviewViewModel =
                                    viewModel(factory = factory)

                                val uiState by favoriteOverviewViewModel.uiState.collectAsState()

                                FavoriteOverviewScreen(uiState)
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

    private fun createNotificationChannel() {
        val channelId = "fototriage_monthly_notification_channel"
        val channelName = "FotoTriage Monthly Notifications"
        val channelDescription = "Channel for monthly notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }


    private fun scheduleMonthlyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the calendar to the first day of the next month
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 9) // Set the time you want the notification to appear
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.MONTH, 1) // Move to the next month if the date is in the past
            }
        }

        // Set the alarm to repeat every month
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 30, // Roughly one month
            pendingIntent
        )
    }

    private fun isNotificationScheduled(context: Context): Boolean {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
    companion object {
        private const val REQUEST_CODE = 100
    }
}
