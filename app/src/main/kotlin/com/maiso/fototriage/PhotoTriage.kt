package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maiso.fototriage.ui.theme.FotoTriageTheme

@Composable
fun PhotoTriage(
    uiState: PhotoTriageUiState,
    onDeletePhoto: (photo: Photo) -> Unit,
    onTraigedPhoto: (photo: Photo) -> Unit,
    onFavoritePhoto: (photo: Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(0) { uiState.photos.size }
    var overlayVisible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500) // Duration of the fade-out animation
    )

    var scale by remember { mutableStateOf(1f) } // Scale for zooming
    var offsetX by remember { mutableStateOf(0f) } // X offset for panning
    var offsetY by remember { mutableStateOf(0f) } // Y offset for panning

    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1.0f)
                .border(1.dp, Color.Red)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom // Update scale based on pinch zoom
                        offsetX += pan.x // Update X offset based on pan
                        offsetY += pan.y // Update Y offset based on pan
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
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uiState.photos[page].uri)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                )
                if (uiState.photos[page].triaged) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { overlayVisible = false }
                            .graphicsLayer(alpha = alpha),
                    ) {
                        Surface(
                            color = Color.Green.copy(alpha = 0.5f), // Semi-transparent green
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }

                }
            }
        }
        Column {
//            Row(
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .wrapContentHeight()
//                    .fillMaxWidth()
//            ) {
//                RoundIconButton(
//                    icon = Icons.Filled.Refresh,
//                    onClick = { /* Handle rotate click */ })
//                RoundIconButton(
//                    icon = Icons.Filled.Refresh,
//                    onClick = { /* Handle mirrored rotate click */ },
//                    isMirrored = true
//                )
//            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
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
                Spacer(Modifier.size(20.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { onFavoritePhoto(uiState.photos[pagerState.currentPage]) }
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
                            tint = Color.Red,
                        )
                    }
                }
                Spacer(Modifier.size(20.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { onTraigedPhoto(uiState.photos[pagerState.currentPage]) }
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
                            tint = Color.Black,
                        )
                    }
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


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun PhotoTriagePreview() {
    FotoTriageTheme {
        PhotoTriage(PhotoTriageUiState(), {}, {}, {})
    }
}