package com.maiso.fototriage.screens.phototriage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.util.Date

@Composable
fun PhotoThumbnailRow(
    photos: List<Photo>,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onPhotoClicked: (index: Int) -> Unit = {}
) {
    val listState = rememberLazyListState()

    val screenWidth = LocalWindowInfo.current.containerSize
    val smallImageSize = (screenWidth.width * 0.045f).dp

    LaunchedEffect(currentPage) {
        val itemWidth = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
        val viewportWidth = listState.layoutInfo.viewportSize.width
        listState.animateScrollToItem(
            index = currentPage,
            scrollOffset = -(viewportWidth / 2 - itemWidth / 2),
        )
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .border(
                            width = if (isCurrent) 2.dp else 0.dp,
                            color = if (isCurrent) Color.LightGray else Color.Transparent,
                        )
                        .clickable { onPhotoClicked(index) }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.uri)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(smallImageSize),
                    )
                    if (photo.isVideo) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Underline for favorite or triaged photos
                Box(
                    modifier = Modifier
                        .width(smallImageSize * 0.65f) // Set width to 80% of the image size
                        .height(2.dp) // Height of the underline

                        .background(
                            color = when {
                                photo.favorite -> Color.Magenta.copy(alpha = 0.4f)
                                photo.triaged -> Color.Green.copy(alpha = 0.4f)
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun PhotoThumbnailRowPreview() {
    FotoTriageTheme {
        PhotoThumbnailRow(
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
            ),
            0
        )
    }
}