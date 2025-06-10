package com.maiso.fototriage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.maiso.fototriage.ui.theme.FotoTriageTheme

@Composable
fun LoadingScreen(
    uiState: StartTriageUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onBackground
        )
        Text("Loading...",
            color = MaterialTheme.colorScheme.onBackground) // Show loading text
        Text("${uiState.progressPercentage}%",
            color = MaterialTheme.colorScheme.onBackground)
        Text("${uiState.progressCurrent}/${uiState.progressTotal} ",
            color = MaterialTheme.colorScheme.onBackground)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun StartTriagePreview() {
    FotoTriageTheme {
        LoadingScreen(StartTriageUiState())
    }
}