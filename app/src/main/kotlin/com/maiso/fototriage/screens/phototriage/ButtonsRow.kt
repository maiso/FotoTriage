package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.composables.LongPressButton
import com.maiso.fototriage.database.Photo

@Composable
fun ButtonsRow(
    photos: List<Photo>,
    currentPage: Int,
    onDeletePhoto: (photo: Photo) -> Unit,
    onTriagedPhoto: (photo: Photo) -> Unit,
    onFavoritePhoto: (photo: Photo) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LongPressButton(
            onLongPress = { onDeletePhoto(photos[currentPage]) },
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
                    onFavoritePhoto(photos[currentPage])
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
                    onTriagedPhoto(photos[currentPage])
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