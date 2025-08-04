package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maiso.fototriage.database.Photo

@Composable
fun PhotoPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    getPhoto: (index: Int) -> Photo,
) {
    var scale by remember { mutableFloatStateOf(1f) } // Scale for zooming
    var offsetX by remember { mutableFloatStateOf(0f) } // X offset for panning
    var offsetY by remember { mutableFloatStateOf(0f) } // Y offset for panning

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
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
        val photo = getPhoto(page)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.uri)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
            )

            if (photo.favorite) {
                FavoritePill()
            } else if (photo.triaged) {
                TriagedPill()
            }
        }
    }
}