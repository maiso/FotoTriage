package com.maiso.fototriage.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ExportScreen(
    uiState: ExportScreenUiState,
    modifier: Modifier,
    onClose: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (uiState.current == uiState.total) {
            Text("Export klaar.", color = MaterialTheme.colorScheme.onBackground)
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            Text("Exporting...", color = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "${uiState.percentage}%",
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "${uiState.current}/${uiState.total} ",
            color = MaterialTheme.colorScheme.onBackground
        )

        if (uiState.current == uiState.total) {
            Button(onClose) { Text("Close") }
        }
    }
}