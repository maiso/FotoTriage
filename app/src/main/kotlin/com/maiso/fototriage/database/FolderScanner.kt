package com.maiso.fototriage.database

import android.util.Log
import java.io.File

private const val TAG = "FolderScanner"

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic", "heif", "webp", "gif", "bmp", "raw", "dng")
private val VIDEO_EXTENSIONS = setOf("mp4", "mov", "avi", "mkv", "3gp", "webm", "m4v")
private val MEDIA_EXTENSIONS = IMAGE_EXTENSIONS + VIDEO_EXTENSIONS

fun scanMediaFolders(): Set<String> {
    val root = File("/storage/emulated/0")
    val folders = mutableSetOf<String>()

    if (!root.exists() || !root.canRead()) {
        Log.e(TAG, "Cannot read $root — missing MANAGE_APP_ALL_FILES_ACCESS?")
        return folders
    }

    fun scan(dir: File, depth: Int) {
        if (depth > 6) return
        val children = dir.listFiles()
        if (children == null) {
            Log.w(TAG, "listFiles() returned null for ${dir.absolutePath}")
            return
        }
        for (child in children) {
            when {
                child.isDirectory && !child.name.startsWith(".") -> scan(child, depth + 1)
                child.isFile && child.extension.lowercase() in MEDIA_EXTENSIONS -> {
                    folders.add(dir.absolutePath)
                }
            }
        }
    }

    scan(root, 0)
    Log.d(TAG, "Scan complete — ${folders.size} folder(s) with media: $folders")
    return folders
}

fun isMediaFile(file: File) = file.isFile && file.extension.lowercase() in MEDIA_EXTENSIONS
fun isVideoFile(file: File) = file.isFile && file.extension.lowercase() in VIDEO_EXTENSIONS
