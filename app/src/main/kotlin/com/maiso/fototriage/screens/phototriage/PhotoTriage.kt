package com.maiso.fototriage.screens.phototriage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maiso.fototriage.composables.LongPressButton
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
    val pagerState = rememberPagerState(0) { uiState.photos.size }
    val listState = rememberLazyListState() // Create a LazyListState
    val coroutineScope = rememberCoroutineScope() // Coroutine scope for scrolling

    var scale by remember { mutableFloatStateOf(1f) } // Scale for zooming
    var offsetX by remember { mutableFloatStateOf(0f) } // X offset for panning
    var offsetY by remember { mutableFloatStateOf(0f) } // Y offset for panning
    var currentPhoto by remember { mutableStateOf<Photo?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f

        coroutineScope.launch {
            val targetIndex = pagerState.currentPage - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex) // Shift left to center the current photo
            }
        }
    }

    val screenWidth =
        LocalWindowInfo.current.containerSize // LocalConfiguration.current.screenWidthDp // Get screen width in dp
    val smallImageSize = (screenWidth.width * 0.045f).dp

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(bottom = 15.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1.0f)
                .padding(vertical = 10.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                // Reset scale and offsets on double-tap
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = scale * zoom

                        // Calculate new offsets
                        val newOffsetX = offsetX + pan.x
                        val newOffsetY = offsetY + pan.y

                        // Get the size of the container and the image
                        val containerWidth = size.width // Width of the container
                        val containerHeight = size.height // Height of the container
                        val imageWidth = containerWidth * scale // Width of the image after scaling
                        val imageHeight =
                            containerHeight * scale // Height of the image after scaling

                        // Calculate the minimum scale to fit the image in the container
                        val minScaleX = containerWidth / imageWidth
                        val minScaleY = containerHeight / imageHeight
                        val minScale = minOf(minScaleX, minScaleY)

                        // Update scale only if within bounds
                        scale = when {
                            newScale < minScale -> minScale // Prevent zooming out too much
                            else -> newScale // Valid scale
                        }

                        val extraWidthRight = (containerWidth - imageWidth) / 2
                        val extraWidthLeft = (imageWidth - containerWidth) / 2

                        // Boundary checks for X offset
                        offsetX = when {
                            newOffsetX < extraWidthRight -> extraWidthRight
                            newOffsetX > extraWidthLeft -> extraWidthLeft
                            else -> newOffsetX // Valid offset
                        }

                        val extraHeightTop = (containerHeight - imageHeight) / 2
                        val extraHeightBottom = (imageHeight - containerHeight) / 2

                        // Boundary checks for Y offset
                        offsetY = when {
                            newOffsetY < extraHeightTop -> extraHeightTop
                            newOffsetY > extraHeightBottom -> extraHeightBottom
                            else -> newOffsetY // Valid offset
                        }
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        ) { page ->
            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.BottomCenter
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.photos[page].uri)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                )

                if (uiState.photos[page].favorite) {
                    FavoritePill()
                } else if (uiState.photos[page].triaged) {
                    TriagedPill()
                }
            }
        }
        LazyRow(
            state = listState, // Use the LazyListState
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center // Center the items in the row
        ) {
            itemsIndexed(uiState.photos) { index, photo ->
                val isCurrent =
                    index == pagerState.currentPage // Check if the index matches the current page

                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp) // Padding between images
                        .border(
                            width = if (isCurrent) 2.dp else 0.dp, // Outline only for the current image
                            color = if (isCurrent) Color.LightGray else Color.Transparent, // Color for the outline
                        )
                        .clickable {
                            // Update the current photo and pager state when a thumbnail is clicked
                            coroutineScope.launch {
                                pagerState.scrollToPage(index) // Change the current photo
                            }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LongPressButton(
                onLongPress = { onDeletePhoto(uiState.photos[pagerState.currentPage]) },
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable {
                        onFavoritePhoto(uiState.photos[pagerState.currentPage])
                    }
                    .background(Color.Magenta.copy(alpha = 0.4f), shape = CircleShape),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Magenta, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clickable {
                        onTriagedPhoto(uiState.photos[pagerState.currentPage])
                    }
                    .background(Color.Green.copy(alpha = 0.4f), shape = CircleShape),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Green, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun RoundIconButton(icon: ImageVector, onClick: () -> Unit, isMirrored: Boolean = false) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp) // Size of the button
            .clip(CircleShape) // Make it circular
            .background(MaterialTheme.colorScheme.primary) // Background color
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Provide a description for accessibility
            tint = Color.White, // Icon color
            modifier = if (isMirrored) {
                Modifier.graphicsLayer(scaleX = -1f) // Mirror the icon
            } else {
                Modifier
            }
        )
    }
}

@Composable
fun Pill(
    color: Color,
    imageVector: ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 52.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color.White
            )
            Text(
                text = text,
                color = Color.White
            )
        }
    }
}

@Composable
fun TriagedPill() {
    Pill(
        color = Color.Green.copy(alpha = 0.4f),
        imageVector = Icons.Filled.Check,
        text = "Triaged",
    )
}

@Composable
fun FavoritePill() {
    Pill(
        color = Color.Magenta.copy(alpha = 0.4f),
        imageVector = Icons.Outlined.Favorite,
        text = "Favoriet",
    )
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