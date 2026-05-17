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
import androidx.compose.ui.res.stringResource
import com.maiso.fototriage.R

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
            Text(stringResource(R.string.export_complete), color = MaterialTheme.colorScheme.onBackground)
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.exporting), color = MaterialTheme.colorScheme.onBackground)
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
            Button(onClose) { Text(stringResource(R.string.btn_close)) }
        }
    }
}