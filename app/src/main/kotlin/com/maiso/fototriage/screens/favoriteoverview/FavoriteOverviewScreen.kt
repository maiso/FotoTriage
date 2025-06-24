package com.maiso.fototriage.screens.favoriteoverview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Year

@Composable
fun FavoriteOverviewScreen(
    uiState: FavoriteOverviewUiState,
    modifier: Modifier = Modifier,
    openExportPanel: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { openExportPanel() },
                enabled = uiState.photos.isNotEmpty()
            ) {
                Text("Exporteer foto's naar Usb-Stick")
            }
        }
        if (uiState.photos.isNotEmpty()) {
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
                    val imageUrl = uiState.photos[index].uri
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f) // Ensures the height and width are the same
                            .padding(4.dp) // Padding between items
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun FavoriteOverviewPreview() {
    FotoTriageTheme {
        FavoriteOverviewScreen(FavoriteOverviewUiState(year = Year.of(2025)), Modifier, {})
    }
}