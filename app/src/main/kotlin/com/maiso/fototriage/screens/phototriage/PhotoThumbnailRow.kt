package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maiso.fototriage.database.Photo
import kotlinx.coroutines.launch

@Composable
fun PhotoThumbnailRow(
    photos: List<Photo>,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onPhotoClicked: (index: Int) -> Unit = {}
) {
    val listState = rememberLazyListState() // Create a LazyListState
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for scrolling
    val context = LocalContext.current

    val screenWidth =
        LocalWindowInfo.current.containerSize // LocalConfiguration.current.screenWidthDp // Get screen width in dp
    val smallImageSize = (screenWidth.width * 0.045f).dp

    LaunchedEffect(currentPage) {
        coroutineScope.launch {
            val targetIndex = currentPage - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex) // Shift left to center the current photo
            }
        }
    }

    LazyRow(
        state = listState, // Use the LazyListState
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center // Center the items in the row
    ) {
        itemsIndexed(photos) { index, photo ->
            val isCurrent = index == currentPage // Check if the index matches the current page

            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp) // Padding between images
                    .border(
                        width = if (isCurrent) 2.dp else 0.dp, // Outline only for the current image
                        color = if (isCurrent) Color.LightGray else Color.Transparent, // Color for the outline
                    )
                    .clickable {
                        onPhotoClicked(index)

                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uri)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(smallImageSize) // Keep the size consistent for all images

                )
            }
        }
    }
}