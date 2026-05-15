package com.maiso.fototriage.screens.overview

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private val ColorDone = Color(0xFF4CAF50)

@Composable
fun MonthRow(
    month: String,
    untriaged: Int,
    favorites: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val done = untriaged == 0
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (done) drawRect(color = ColorDone, size = Size(4.dp.toPx(), size.height))
            }
            .clickable { onClick() }
            .padding(start = 20.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        if (favorites > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "$favorites",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (done) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Klaar",
                tint = ColorDone,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Surface(
                shape = RoundedCornerShape(50),
                color = primaryContainer,
            ) {
                Text(
                    text = "$untriaged",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onPrimaryContainer,
                )
            }
        }
    }
}

fun Month.toDutchString(): String = getDisplayName(TextStyle.FULL, Locale.getDefault())

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun MonthRowPreview() {
    FotoTriageTheme {
        MonthRow(month = "Maart", untriaged = 14, favorites = 3, onClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun MonthRowDonePreview() {
    FotoTriageTheme {
        MonthRow(month = "Januari", untriaged = 0, favorites = 7, onClick = {})
    }
}
