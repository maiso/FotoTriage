package com.maiso.fototriage.screens.overview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthRow(
    month: String,
    nrOfPhoto: Int,
    untriaged: Int,
    triaged: Int,
    favorites: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Define the background color based on the untriaged count and current color scheme
    val backgroundColor = when {
        untriaged == 0 -> {
            // Light green for light mode
            if (isSystemInDarkTheme()) Color(0xFF3C763D) // Slightly darker green for dark mode
            else Color(0xFFE0F7E0) // Light green for light mode
        }
        else -> Color.Transparent // Default background color when untriaged items are present
    }

    Row(
        modifier = modifier
            .background(backgroundColor)
            .clickable {
                onClick()
            }
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        TextIcon("$nrOfPhoto", Icons.Outlined.Image)
        TextIcon("$untriaged", Icons.AutoMirrored.Filled.HelpOutline)
        TextIcon("$triaged", Icons.Outlined.CheckCircle, Color.Green.copy(alpha = 0.5f))
        TextIcon("$favorites", Icons.Outlined.Favorite, Color.Magenta.copy(alpha = 0.5f))
    }
}

@Composable
fun TextIcon(text: String, icon: ImageVector, iconColor: Color = LocalContentColor.current) {
    Row {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(2.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor
        )
    }
}

fun Month.toDutchString(): String {
    return getDisplayName(TextStyle.FULL, Locale.getDefault())
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun MonthRowPreview() {
    FotoTriageTheme {
        MonthRow("Maart", 123, 0, 25, 2) {

        }
    }
}