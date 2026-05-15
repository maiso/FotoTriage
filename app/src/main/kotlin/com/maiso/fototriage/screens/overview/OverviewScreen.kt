package com.maiso.fototriage.screens.overview

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Month
import java.time.Year

private val ColorDone = Color(0xFF4CAF50)

@Composable
fun OverviewScreen(
    uiState: OverviewScreenUiState,
    modifier: Modifier = Modifier,
    onYearClick: (Year) -> Unit,
    onMonthClick: (Year, Month) -> Unit,
    onSettingsClick: () -> Unit = {},
) {
    var showCompleted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Voltooide maanden tonen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = showCompleted,
                    onCheckedChange = { showCompleted = it },
                )
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Mappen selecteren",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        uiState.yearPhotos.sortedByDescending { it.year }.forEach { yearUiState ->
            val monthsForYear = uiState.monthPhotos
                .filter { it.year == yearUiState.year && it.nrOfPhoto != 0 }
                .filter { if (showCompleted) true else it.nrOfUntriaged > 0 }

            item {
                Spacer(Modifier.height(12.dp))
                YearRow(
                    yearUiState = yearUiState,
                    onClick = { onYearClick(yearUiState.year) },
                )
            }

            if (monthsForYear.isEmpty()) {
                item {
                    Text(
                        text = "Alle maanden voltooid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 6.dp),
                    )
                }
            } else {
                monthsForYear.forEach { month ->
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 20.dp),
                        )
                        MonthRow(
                            month = month.month.toDutchString(),
                            untriaged = month.nrOfUntriaged,
                            favorites = month.nrOfFavorites,
                            onClick = { onMonthClick(yearUiState.year, month.month) },
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun YearRow(
    yearUiState: YearUiState,
    onClick: () -> Unit,
) {
    val done = yearUiState.nrOfUntriaged == 0
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = yearUiState.year.value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )

            if (yearUiState.nrOfFavorites > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${yearUiState.nrOfFavorites}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (done) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Klaar",
                        tint = ColorDone,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Klaar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = ColorDone,
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = primaryContainer,
                ) {
                    Text(
                        text = "${yearUiState.nrOfUntriaged} te doen",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun OverviewPanelPreview() {
    FotoTriageTheme {
        OverviewScreen(
            OverviewScreenUiState(
                yearPhotos = listOf(
                    YearUiState(Year.of(2025), 785, 10, 14, 123),
                    YearUiState(Year.of(2024), 456, 15, 42, 0)
                ),
                monthPhotos = listOf(
                    MonthUiState(Year.of(2025), Month.JANUARY, 123, 100, 21, 2),
                    MonthUiState(Year.of(2025), Month.FEBRUARY, 123, 90, 11, 2),
                    MonthUiState(Year.of(2025), Month.MARCH, 123, 80, 1, 2),
                    MonthUiState(Year.of(2025), Month.APRIL, 123, 70, 1, 0),
                    MonthUiState(Year.of(2025), Month.MAY, 123, 60, 1, 0),
                    MonthUiState(Year.of(2025), Month.JUNE, 123, 50, 1, 0),
                    MonthUiState(Year.of(2024), Month.JANUARY, 456, 0, 40, 5),
                    MonthUiState(Year.of(2024), Month.FEBRUARY, 123, 0, 30, 3),
                    MonthUiState(Year.of(2024), Month.MARCH, 123, 0, 20, 0),
                    MonthUiState(Year.of(2024), Month.APRIL, 123, 0, 10, 1),
                    MonthUiState(Year.of(2024), Month.MAY, 123, 0, 1, 0),
                ),
            ),
            onMonthClick = { _, _ -> },
            onYearClick = {},
        )
    }
}
