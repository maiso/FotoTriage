package com.maiso.fototriage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun FavoriteOverviewScreen(
    uiState: FavoriteOverviewUiState,
    modifier: Modifier = Modifier
) {
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