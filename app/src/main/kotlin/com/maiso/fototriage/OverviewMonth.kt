package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
fun MonthRow(month: String, nrOfFotos: Int, triaged: Int, favorites: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 15.dp)
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        TextIcon("$nrOfFotos", Icons.Outlined.Image)
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
    return getDisplayName(TextStyle.FULL, Locale.getDefault()) // Dutch locale
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun MonthRowPreview() {
    FotoTriageTheme {
        MonthRow("Maart", 100, 25, 2, {})
    }
}