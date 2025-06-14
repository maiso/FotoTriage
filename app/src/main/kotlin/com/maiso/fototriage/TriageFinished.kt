package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Triage is klaar",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp
        )
        Spacer(Modifier.size(45.dp))

        Button(
            onClick = { onShowAllPhoto() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary, // Set the background color
                contentColor = MaterialTheme.colorScheme.onSecondary // Set the text color
            )
        ) {
            Text(text = "Alle fotos ${month.toDutchString()} $year")
        }

        Button(onClick = { onClosePanel() }) {
            Text(text = "Sluiten")
        }
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