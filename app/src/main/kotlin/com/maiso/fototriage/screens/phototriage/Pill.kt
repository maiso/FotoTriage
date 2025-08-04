package com.maiso.fototriage.screens.phototriage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


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