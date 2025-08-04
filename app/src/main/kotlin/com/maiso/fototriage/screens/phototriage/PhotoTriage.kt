package com.maiso.fototriage.screens.phototriage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for scrolling

    val pagerState = rememberPagerState(0) { uiState.photos.size }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(bottom = 15.dp),
    ) {
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
                        fileName = "",
                        filePath = "",
                        dateTaken = Date(),
                        dateTakenMillis = 0,
                        triaged = true,
                        favorite = true
                    )
                )
            ), {}, {}, {})
    }
}