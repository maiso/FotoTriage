package com.maiso.fototriage.screens.favoriteoverview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Year
import kotlinx.coroutines.delay

@Composable
fun FavoriteOverviewScreen(
    uiState: FavoriteOverviewUiState,
    modifier: Modifier = Modifier,
    openExportPanel: () -> Unit,
    unfavoritePhoto: (Photo) -> Unit,
    onBack: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to overview",
                )
            }
            Box(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Exporteer foto's naar Usb-Stick") },
                        enabled = uiState.photos.isNotEmpty(),
                        onClick = {
                            openExportPanel()
                            menuExpanded = false
                        },
                    )
                }
            }
        }
        if (uiState.photos.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text("Geen favorite foto's")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Two columns
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp) // Optional padding
            ) {
                items(uiState.photos.size) { index ->
                    val photo = uiState.photos[index]
                    UnfavoriteButton(
                        onLongPress = { unfavoritePhoto(photo) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp),
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop,
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
                }
            }
        }
    }
}

@Composable
private fun UnfavoriteButton(
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            progress = 0f
            while (progress < 1f) {
                delay(10)
                progress += 0.02f
            }
            onLongPress()
            isPressed = false
        } else {
            progress = 0f
        }
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
    ) {
        content()
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Green.copy(alpha = progress * 0.65f))
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun FavoriteOverviewPreview() {
    FotoTriageTheme {
        FavoriteOverviewScreen(FavoriteOverviewUiState(year = Year.of(2025)), Modifier, {}, {})
    }
}