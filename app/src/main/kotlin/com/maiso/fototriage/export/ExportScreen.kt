package com.maiso.fototriage.export

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.R
import com.maiso.fototriage.ui.theme.FotoTriageTheme

@Composable
fun ExportScreen(
    uiState: ExportScreenUiState,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val isComplete = uiState.total > 0 && uiState.current >= uiState.total
    val animatedProgress by animateFloatAsState(
        targetValue = if (uiState.total > 0) uiState.current.toFloat() / uiState.total else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "exportProgress",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Outlined.CheckCircle else Icons.Outlined.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isComplete) stringResource(R.string.export_complete) else stringResource(R.string.exporting),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(48.dp))
        LinearProgressIndicator(
            progress = { if (isComplete) 1f else animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.export_progress, uiState.current, uiState.total),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isComplete) {
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.btn_close))
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun ExportScreenInProgressPreview() {
    FotoTriageTheme {
        ExportScreen(ExportScreenUiState(total = 42, current = 17, percentage = 40), onClose = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun ExportScreenCompletePreview() {
    FotoTriageTheme {
        ExportScreen(ExportScreenUiState(total = 42, current = 42, percentage = 100), onClose = {})
    }
}
