package com.maiso.fototriage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Spacer(Modifier.size(20.dp))
        Text(
            text = "Wil je alle foto's van $month $year zien?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp
        )

        Button(onClick = { onShowAllPhoto() }) {
            Text(text = "Alle fotos")
        }

        Button(onClick = { onClosePanel() }) {
            Text(text = "Close")
        }
    }
}