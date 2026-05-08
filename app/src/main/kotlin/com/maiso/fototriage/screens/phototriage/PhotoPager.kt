package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
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
    HorizontalPager(
        state = pagerState,
        modifier = modifier.padding(vertical = 10.dp),
    ) { page ->
        val photo = getPhoto(page)
        val isCurrentPage = page == pagerState.currentPage

        if (photo.isVideo) {
            VideoPage(photo = photo, isCurrentPage = isCurrentPage)
        } else {
            PhotoPage(photo = photo)
        }
    }
}

@Composable
private fun PhotoPage(photo: Photo) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { scale = 1f; offsetX = 0f; offsetY = 0f }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = scale * zoom
                    val newOffsetX = offsetX + pan.x
                    val newOffsetY = offsetY + pan.y

                    val containerWidth = size.width.toFloat()
                    val containerHeight = size.height.toFloat()
                    val imageWidth = containerWidth * scale
                    val imageHeight = containerHeight * scale

                    val minScaleX = containerWidth / imageWidth
                    val minScaleY = containerHeight / imageHeight
                    val minScale = minOf(minScaleX, minScaleY)

                    scale = if (newScale < minScale) minScale else newScale

                    val extraWidthRight = (containerWidth - imageWidth) / 2
                    val extraWidthLeft = (imageWidth - containerWidth) / 2
                    offsetX = when {
                        newOffsetX < extraWidthRight -> extraWidthRight
                        newOffsetX > extraWidthLeft -> extraWidthLeft
                        else -> newOffsetX
                    }

                    val extraHeightTop = (containerHeight - imageHeight) / 2
                    val extraHeightBottom = (imageHeight - containerHeight) / 2
                    offsetY = when {
                        newOffsetY < extraHeightTop -> extraHeightTop
                        newOffsetY > extraHeightBottom -> extraHeightBottom
                        else -> newOffsetY
                    }
                }
            }
            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (photo.favorite) FavoritePill() else if (photo.triaged) TriagedPill()
    }
}

@Composable
private fun VideoPage(photo: Photo, isCurrentPage: Boolean) {
    VideoPlayer(
        uri = photo.uri,
        isCurrentPage = isCurrentPage,
        modifier = Modifier.fillMaxSize(),
        overlay = {
            if (photo.favorite) FavoritePill() else if (photo.triaged) TriagedPill()
        },
    )
}
