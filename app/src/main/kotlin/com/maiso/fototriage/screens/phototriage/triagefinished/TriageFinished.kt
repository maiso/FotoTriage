package com.maiso.fototriage.screens.phototriage.triagefinished

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maiso.fototriage.database.PhotoDatabase
import com.maiso.fototriage.database.filterByMonth
import com.maiso.fototriage.database.filterByYear
import com.maiso.fototriage.screens.overview.toDutchString
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Month
import java.time.Year

@Composable
fun TriageFinished(
    year: Year,
    month: Month,
    onShowAllPhoto: () -> Unit,
    onClosePanel: () -> Unit,
) {
    val photos by PhotoDatabase.photos.collectAsState()
    val deletedByYearMonth by PhotoDatabase.deletedByYearMonth.collectAsState()

    val monthPhotos = photos.filterByMonth(year, month)
    val yearPhotos = photos.filterByYear(year)

    val monthDeleted = deletedByYearMonth[year.value to month.value] ?: 0
    val yearDeleted = deletedByYearMonth.entries
        .filter { (key, _) -> key.first == year.value }
        .sumOf { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Gefeliciteerd!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${month.toDutchString()} $year is klaar",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(28.dp))

        StatsCard(
            title = "${month.toDutchString()} $year",
            triaged = monthPhotos.count { it.triaged && !it.favorite },
            favorites = monthPhotos.count { it.favorite },
            deleted = monthDeleted,
        )

        Spacer(Modifier.height(16.dp))

        StatsCard(
            title = "Heel $year",
            triaged = yearPhotos.count { it.triaged && !it.favorite },
            favorites = yearPhotos.count { it.favorite },
            deleted = yearDeleted,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { onShowAllPhoto() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(text = "Alle fotos ${month.toDutchString()} $year")
        }

        Button(
            onClick = { onClosePanel() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Sluiten")
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    triaged: Int,
    favorites: Int,
    deleted: Int,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            StatRow(icon = Icons.Outlined.CheckCircle, label = "Getriaged", count = triaged)
            Spacer(Modifier.height(6.dp))
            StatRow(icon = Icons.Outlined.Favorite, label = "Favorieten", count = favorites)
            Spacer(Modifier.height(6.dp))
            StatRow(icon = Icons.Filled.Delete, label = "Verwijderd", count = deleted)
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun TriageFinishedPreview() {
    FotoTriageTheme {
        TriageFinished(Year.now(), Month.MARCH, {}, {})
    }
}
