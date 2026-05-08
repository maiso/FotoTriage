package com.maiso.fototriage.screens.folderselection

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maiso.fototriage.ui.theme.FotoTriageTheme
import java.io.File

@Composable
fun FolderSelectionScreen(
    uiState: FolderSelectionUiState,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Select folders to index",
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
        if (!uiState.isLoading && uiState.folders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "No photos found",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No image folders were found on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(uiState.folders) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(folder.path) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = folder.selected,
                    onCheckedChange = { onToggle(folder.path) }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = File(folder.path).name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = folder.path.removePrefix("/storage/emulated/0"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider()
        }
        item {
            Button(
                onClick = onContinue,
                enabled = uiState.canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FolderSelectionScreenEmptyPreview() {
    FotoTriageTheme {
        FolderSelectionScreen(
            uiState = FolderSelectionUiState(folders = emptyList(), canContinue = false),
            onToggle = {},
            onContinue = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun FolderSelectionScreenPreview() {
    FotoTriageTheme {
        FolderSelectionScreen(
            uiState = FolderSelectionUiState(
                folders = listOf(
                    FolderItem("/storage/emulated/0/DCIM/Camera", true),
                    FolderItem("/storage/emulated/0/Pictures/Instagram", false),
                    FolderItem("/storage/emulated/0/DCIM/100ANDRO", true),
                ),
                canContinue = true
            ),
            onToggle = {},
            onContinue = {},
        )
    }
}
