package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    LazyColumn(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 5.dp)) {
        uiState.yearPhotos.forEach { yearUiState ->
            item {
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                Row(
                    modifier = Modifier
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
                    TextIcon("${yearUiState.nrOfPhoto}", Icons.Outlined.Image)

                    TextIcon(
                        "${yearUiState.nrOfFavorites}",
                        Icons.Outlined.Favorite,
                        Color.Magenta.copy(alpha = 0.5f)
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            }
            uiState.monthPhotos.filter { it.year == yearUiState.year }.filter { it.nrOfPhoto != 0 }
                .forEach { month ->
                    item {
                        MonthRow(
                            modifier = if (month.nrOfUntriaged == month.nrOfTriaged + month.nrOfFavorites) {
                                Modifier.background(Color.Green.copy(alpha = 0.5f))
                            } else Modifier,
                            month = month.month.toDutchString(),
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
                    YearUiState(Year.of(2025), 785, 10),
                    YearUiState(Year.of(2024), 456, 15)
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