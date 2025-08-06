package com.maiso.fototriage.screens.phototriage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun PhotoTriage(
    uiState: PhotoTriageUiState,
    onDeletePhoto: (photo: Photo) -> Unit,
    onTriagedPhoto: (photo: Photo) -> Unit,
    onFavoritePhoto: (photo: Photo) -> Unit,
    onShowTriagedChange: (showTriaged: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for scrolling

    val pagerState = rememberPagerState(0) { uiState.photos.size }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spacer to push the first text to the center
                Spacer(modifier = Modifier.weight(1f))

                // First Text centered in the available space
                if (uiState.photos.isNotEmpty() && pagerState.currentPage in uiState.photos.indices) {
                    Text(
                        text = uiState.photos[pagerState.currentPage].fileName,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray) // Secondary text style
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Existing Text with secondary style
                Text(
                    text = "Show triaged",
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray) // Secondary text style
                )

                // Switch with less prominent color
                Switch(
                    checked = uiState.showTriaged,
                    onCheckedChange = { onShowTriagedChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Gray, // Less prominent color for checked state
                        uncheckedThumbColor = Color.DarkGray, // Less prominent color for unchecked state
                        checkedTrackColor = Color.LightGray, // Less prominent color for track when checked
                        uncheckedTrackColor = Color.Gray // Less prominent color for track when unchecked
                    )
                )
            }
        }
        PhotoPager(
            pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            uiState.photos[index]
        }

        PhotoThumbnailRow(
            uiState.photos,
            pagerState.currentPage,
        ) { index ->
            // Update the current photo and pager state when a thumbnail is clicked
            coroutineScope.launch {
                pagerState.scrollToPage(index) // Change the current photo
            }
        }

        ButtonsRow(
            uiState.photos,
            pagerState.currentPage,
            onDeletePhoto,
            onTriagedPhoto,
            onFavoritePhoto,
        )

    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun PhotoTriagePreview() {
    FotoTriageTheme {
        PhotoTriage(
            PhotoTriageUiState(
                listOf(
                    Photo(
                        uri = "".toUri(),
                        fileName = "FileName.Jpg",
                        filePath = "",
                        dateTaken = Date(),
                        dateTakenMillis = 0,
                        triaged = true,
                        favorite = true
                    )
                ),
                true
            ), {}, {}, {}, {})
    }
}