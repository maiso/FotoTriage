package com.maiso.fototriage.screens.phototriage

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope
    val pagerState = rememberPagerState(0) { uiState.photos.size }
    var overlayVisible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500) // Duration of the fade-out animation
    )

    var scale by remember { mutableFloatStateOf(1f) } // Scale for zooming
    var offsetX by remember { mutableFloatStateOf(0f) } // X offset for panning
    var offsetY by remember { mutableFloatStateOf(0f) } // Y offset for panning

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
        overlayVisible = true
    }

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
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (scale * zoom > 1) {
                            scale *= zoom // Update scale based on pinch zoom
                            if (zoom.toInt() >= 1.0) {
                                offsetX += pan.x // Update X offset based on pan
                                offsetY += pan.y // Update Y offset based on pan
                            }
                        } else {
                            offsetX = 0f
                            offsetY = 0f
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

                        if (pagerState.currentPage < uiState.photos.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1) // Animate to the next page
                            }
                        }
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

                        if (pagerState.currentPage < uiState.photos.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1) // Animate to the next page
                            }
                        }

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