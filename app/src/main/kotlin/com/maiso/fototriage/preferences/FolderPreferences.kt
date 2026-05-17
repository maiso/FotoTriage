package com.maiso.fototriage.preferences

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "FotoTriagePrefs"
private const val KEY_SELECTED_FOLDERS = "selected_folders"
private const val KEY_DELETE_WARNING_DISMISSED = "delete_warning_dismissed"

object FolderPreferences {

    fun getSelectedFolders(context: Context): List<String> {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_FOLDERS, null) ?: return emptyList()
        return stored.split("\n").filter { it.isNotBlank() }
    }

    fun saveSelectedFolders(context: Context, folders: List<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_SELECTED_FOLDERS, folders.joinToString("\n"))
            }
    }

    fun hasSavedFolders(context: Context): Boolean = getSelectedFolders(context).isNotEmpty()

    fun isDeleteWarningDismissed(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DELETE_WARNING_DISMISSED, false)

    fun setDeleteWarningDismissed(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_DELETE_WARNING_DISMISSED, true) }
    }
}
