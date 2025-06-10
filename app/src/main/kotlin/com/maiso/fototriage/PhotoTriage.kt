package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.maiso.fototriage.ui.theme.FotoTriageTheme

@Composable
fun PhotoTriage(
    uiState: PhotoTriageUiState,
    onPreviousPhoto: () -> Unit,
    onNextPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Foto: ${uiState.fileName}",
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.triaged) {
                Box(
                    modifier = Modifier
                        .size(32.dp) // Size of the badge
                        .background(
                            Color(0xFFB2FF59),
                            shape = RoundedCornerShape(16.dp)
                        ) // Light green background with rounded corners
                        .padding(4.dp), // Padding around the checkbox
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Checked",
                        tint = Color.Green, // Set the color of the checkbox to green
                        modifier = Modifier.size(16.dp) // Size of the checkbox icon
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(1.0f),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.photo)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .border(1.dp, Color.Red)
                    .fillMaxSize(),
            )

            Row {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp) // Set a width for the button container
                        .clickable { onPreviousPhoto() }
                        .border(1.dp, Color.Blue)
                ) {

                }
                Spacer(modifier = Modifier.weight(1.0f))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp) // Set a width for the button container
                        .clickable { onNextPhoto() }
                        .border(1.dp, Color.Blue)
                ) {
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            RoundIconButton(
                icon = Icons.Outlined.Favorite,
                onClick = { /* Handle favorite click */ })
            RoundIconButton(icon = Icons.Filled.Refresh, onClick = { /* Handle rotate click */ })
            RoundIconButton(
                icon = Icons.Filled.Refresh,
                onClick = { /* Handle mirrored rotate click */ },
                isMirrored = true
            )
            RoundIconButton(icon = Icons.Filled.Delete, onClick = { /* Handle delete click */ })
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
        PhotoTriage(PhotoTriageUiState(), {}, {})
    }
}