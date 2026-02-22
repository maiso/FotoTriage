package com.maiso.fototriage.screens.overview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.time.Month
import java.time.Year

@Composable
fun OverviewScreen(
    uiState: OverviewScreenUiState,
    modifier: Modifier = Modifier,
    onYearClick: (Year) -> Unit,
    onMonthClick: (Year, Month) -> Unit
) {
    var showCompleted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Show completed months",
                    modifier = Modifier.padding(end = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray) // Secondary text style
                )

                // Switch with less prominent color
                Switch(
                    checked = showCompleted,
                    onCheckedChange = { showCompleted = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Gray, // Less prominent color for checked state
                        uncheckedThumbColor = Color.DarkGray, // Less prominent color for unchecked state
                        checkedTrackColor = Color.LightGray, // Less prominent color for track when checked
                        uncheckedTrackColor = Color.Gray // Less prominent color for track when unchecked
                    )
                )
            }
        }
        uiState.yearPhotos.forEach { yearUiState ->
            item {
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .then(
                            if (yearUiState.nrOfUntriaged == 0) {
                                Modifier.background(Color.Green.copy(alpha = 0.5f))
                            } else Modifier
                        )
                        .padding(vertical = 15.dp)
                        .clickable {
                            onYearClick(yearUiState.year)
                        },
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = yearUiState.year.value.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextIcon("${yearUiState.nrOfPhoto}", Icons.Outlined.AccountCircle)
                    TextIcon("${yearUiState.nrOfUntriaged}", Icons.Filled.Warning)
                    TextIcon(
                        "${yearUiState.nrOfTriaged}",
                        Icons.Outlined.CheckCircle,
                        Color.Green.copy(alpha = 0.5f)
                    )


                    TextIcon(
                        "${yearUiState.nrOfFavorites}",
                        Icons.Outlined.Favorite,
                        Color.Magenta.copy(alpha = 0.5f)
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            }
            uiState.monthPhotos.filter { it.year == yearUiState.year }
                .filter { it.nrOfPhoto != 0 }
                .filter { if (showCompleted) true else it.nrOfUntriaged > 0 }
                .forEach { month ->
                    item {
                        MonthRow(
                            modifier = if (month.nrOfUntriaged == month.nrOfTriaged + month.nrOfFavorites) {
                                Modifier.background(Color.Green.copy(alpha = 0.5f))
                            } else Modifier,
                            month = month.month.toDutchString(),
                            nrOfPhoto = month.nrOfPhoto,
                            untriaged = month.nrOfUntriaged,
                            triaged = month.nrOfTriaged,
                            favorites = month.nrOfFavorites,
                        ) { onMonthClick(yearUiState.year, month.month) }
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
                    YearUiState(Year.of(2024), 456, 15, 42, 321)
                ),
                monthPhotos = listOf(
                    MonthUiState(Year.of(2025), Month.JANUARY, 123, 100, 21, 2),
                    MonthUiState(Year.of(2025), Month.FEBRUARY, 123, 90, 11, 2),
                    MonthUiState(Year.of(2025), Month.MARCH, 123, 80, 1, 2),
                    MonthUiState(Year.of(2025), Month.APRIL, 123, 70, 1, 2),
                    MonthUiState(Year.of(2025), Month.MAY, 123, 60, 1, 2),
                    MonthUiState(Year.of(2025), Month.JUNE, 123, 50, 1, 2),
                    MonthUiState(Year.of(2024), Month.JANUARY, 456, 40, 1, 2),
                    MonthUiState(Year.of(2024), Month.FEBRUARY, 123, 30, 1, 2),
                    MonthUiState(Year.of(2024), Month.MARCH, 123, 20, 1, 2),
                    MonthUiState(Year.of(2024), Month.APRIL, 123, 10, 1, 2),
                    MonthUiState(Year.of(2024), Month.MAY, 123, 0, 1, 2),
                    MonthUiState(Year.of(2024), Month.JUNE, 123, 0, 1, 2),
                ),
            ),
            onMonthClick = { _, _ -> },
            onYearClick = {}
        )
    }
}