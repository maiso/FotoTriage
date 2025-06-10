package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
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
    onClick: (Year, Month) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        uiState.yearPhotos.forEach { year ->
            item {
                Row {
                    Text(
                        text = year.first.value.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${year.second} foto's",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            }
            uiState.monthPhotos.filter { it.year == year.first }.filter { it.nrOfPhoto != 0 }
                .forEach { month ->
                    item {
                        MonthRow(
                            month.month.toDutchString(),
                            month.nrOfPhoto,
                            month.percentageTriaged,
                        ) { onClick(year.first, month.month) }
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
                yearPhotos = listOf(Year.of(2025) to 123, Year.of(2024) to 456),
                monthPhotos = listOf(
                    MonthUiState(Year.of(2025), Month.JANUARY, 123, 100),
                    MonthUiState(Year.of(2025), Month.FEBRUARY, 123, 90),
                    MonthUiState(Year.of(2025), Month.MARCH, 123, 80),
                    MonthUiState(Year.of(2025), Month.APRIL, 123, 70),
                    MonthUiState(Year.of(2025), Month.MAY, 123, 60),
                    MonthUiState(Year.of(2025), Month.JUNE, 123, 50),
                    MonthUiState(Year.of(2024), Month.JANUARY, 456, 40),
                    MonthUiState(Year.of(2024), Month.FEBRUARY, 123, 30),
                    MonthUiState(Year.of(2024), Month.MARCH, 123, 20),
                    MonthUiState(Year.of(2024), Month.APRIL, 123, 10),
                    MonthUiState(Year.of(2024), Month.MAY, 123, 0),
                    MonthUiState(Year.of(2024), Month.JUNE, 123, 0),
                ),
            ),
            onClick = { _, _ -> }
        )
    }
}