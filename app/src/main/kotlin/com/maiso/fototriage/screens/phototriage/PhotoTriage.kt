package com.maiso.fototriage.screens.phototriage

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun PhotoTriage(
    uiState: PhotoTriageUiState,
    onDeletePhoto: (photo: Photo) -> Unit,
    onTriagedPhoto: (photo: Photo) -> Unit,
    onFavoritePhoto: (photo: Photo) -> Unit,
    onShowTriagedChange: (showTriaged: Boolean) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }

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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back to overview",
                        tint = Color.Gray,
                    )
                }

                if (uiState.photos.isNotEmpty() && pagerState.currentPage in uiState.photos.indices) {
                    val photo = uiState.photos[pagerState.currentPage]
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = photo.fileName,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        StartEllipsisText(
                            text = File(photo.filePath).parent ?: photo.filePath,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                        )
                        val remaining = uiState.photos.size - pagerState.currentPage
                        Text(
                            text = "$remaining ${if (remaining == 1) "foto" else "fotos"} te gaan",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (uiState.photos.isNotEmpty() && pagerState.currentPage in uiState.photos.indices) {
                    val photo = uiState.photos[pagerState.currentPage]
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (photo.isVideo) "video/*" else "image/*"
                            putExtra(Intent.EXTRA_STREAM, photo.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Show triaged") },
                            leadingIcon = {
                                Checkbox(
                                    checked = uiState.showTriaged,
                                    onCheckedChange = null,
                                )
                            },
                            onClick = {
                                onShowTriagedChange(!uiState.showTriaged)
                                menuExpanded = false
                            },
                        )
                    }
                }
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

@Composable
private fun StartEllipsisText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = constraints.maxWidth
        val displayText = remember(text, maxWidthPx, style) {
            val full = textMeasurer.measure(
                AnnotatedString(text), style = style, maxLines = 1,
                constraints = Constraints(maxWidth = maxWidthPx),
            )
            if (!full.hasVisualOverflow) {
                text
            } else {
                // Binary search for the smallest start index where "…<suffix>" fits.
                var lo = 0; var hi = text.length
                while (lo < hi) {
                    val mid = (lo + hi) / 2
                    val candidate = "…" + text.substring(mid)
                    val r = textMeasurer.measure(
                        AnnotatedString(candidate), style = style, maxLines = 1,
                        constraints = Constraints(maxWidth = maxWidthPx),
                    )
                    if (r.hasVisualOverflow) lo = mid + 1 else hi = mid
                }
                "…" + text.substring(lo)
            }
        }
        Text(text = displayText, style = style, color = color, maxLines = 1)
    }
}