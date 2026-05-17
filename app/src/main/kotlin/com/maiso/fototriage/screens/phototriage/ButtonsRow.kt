package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.R
import com.maiso.fototriage.composables.LongPressButton
import com.maiso.fototriage.database.Photo
import com.maiso.fototriage.preferences.FolderPreferences

@Composable
fun ButtonsRow(
    photos: List<Photo>,
    currentPage: Int,
    onDeletePhoto: (photo: Photo) -> Unit,
    onTriagedPhoto: (photo: Photo) -> Unit,
    onFavoritePhoto: (photo: Photo) -> Unit,
) {
    val context = LocalContext.current
    val currentPhoto = photos[currentPage]
    val isFavorite = currentPhoto.favorite
    val isTriaged = currentPhoto.triaged && !currentPhoto.favorite

    var pendingDelete by remember { mutableStateOf<Photo?>(null) }
    var dontAskAgain by remember { mutableStateOf(false) }

    pendingDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = {
                pendingDelete = null
                dontAskAgain = false
            },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dialog_delete_message))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { dontAskAgain = !dontAskAgain },
                    ) {
                        Checkbox(
                            checked = dontAskAgain,
                            onCheckedChange = { dontAskAgain = it },
                        )
                        Text(stringResource(R.string.checkbox_dont_ask_again))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (dontAskAgain) FolderPreferences.setDeleteWarningDismissed(context)
                    onDeletePhoto(photo)
                    pendingDelete = null
                    dontAskAgain = false
                }) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    dontAskAgain = false
                }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LongPressButton(
            onLongPress = {
                if (FolderPreferences.isDeleteWarningDismissed(context)) {
                    onDeletePhoto(currentPhoto)
                } else {
                    pendingDelete = currentPhoto
                }
            },
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
                .clickable { onFavoritePhoto(currentPhoto) }
                .background(Color.Magenta.copy(alpha = if (isFavorite) 1f else 0.4f), shape = CircleShape),
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
                .clickable { onTriagedPhoto(currentPhoto) }
                .background(Color.Green.copy(alpha = if (isTriaged) 1f else 0.4f), shape = CircleShape),
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