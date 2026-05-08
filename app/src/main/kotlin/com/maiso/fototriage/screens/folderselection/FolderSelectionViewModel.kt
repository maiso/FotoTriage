package com.maiso.fototriage.screens.folderselection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maiso.fototriage.database.scanMediaFolders
import com.maiso.fototriage.preferences.FolderPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FolderSelectionUiState(
    val isLoading: Boolean = true,
    val folders: List<FolderItem> = emptyList(),
    val canContinue: Boolean = false,
)

data class FolderItem(
    val path: String,
    val selected: Boolean,
)

class FolderSelectionViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderSelectionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val savedFolders = FolderPreferences.getSelectedFolders(context).toSet()
            val allFolders = scanMediaFolders()
            val items = allFolders
                .map { path ->
                    FolderItem(
                        path = path,
                        selected = if (savedFolders.isEmpty()) true else path in savedFolders
                    )
                }
                .sortedBy { it.path }
            withContext(Dispatchers.Main) {
                _uiState.value = FolderSelectionUiState(
                    isLoading = false,
                    folders = items,
                    canContinue = items.any { it.selected }
                )
            }
        }
    }

    fun toggle(path: String) {
        _uiState.update { state ->
            val updated = state.folders.map {
                if (it.path == path) it.copy(selected = !it.selected) else it
            }
            state.copy(folders = updated, canContinue = updated.any { it.selected })
        }
    }

    fun saveSelection() {
        val selected = _uiState.value.folders.filter { it.selected }.map { it.path }
        FolderPreferences.saveSelectedFolders(context, selected)
    }

    companion object {
        class FolderSelectionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FolderSelectionViewModel(context) as T
            }
        }
    }
}
