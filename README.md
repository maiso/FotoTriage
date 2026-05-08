# FotoTriage

An Android app for triaging your camera roll on-device. Browse photos month by month, mark them as triaged or favorite, delete the ones you don't need, and export your favorites to a USB drive — all without any cloud involvement.

## What it does

Most cameras produce hundreds of photos a month. FotoTriage gives you a simple workflow to go through them:

1. **Open a month** from the overview — months with all photos triaged are highlighted in green
2. **Swipe through photos** one by one and decide: keep (triaged), favorite, or delete
3. **Review your favorites** per year in a grid overview
4. **Export favorites to USB** — copies files into a `FotoFavorites<year>` folder on the connected drive

A monthly notification reminds you to stay on top of your backlog.

## Features

- Browse photos grouped by year and month
- Three photo states: **Untriaged** → **Triaged** / **Favorite**
- Delete photos directly from the triage view
- Toggle to hide fully triaged months from the overview
- Favorites grid per year with export to USB (via Android Storage Access Framework)
- Configurable source folders (defaults to `DCIM/Camera`)
- Triage state stored in a local SQLite database, co-located with your photos
- Monthly reminder notification

## Tech stack

- Kotlin + Jetpack Compose
- androidx.navigation3 for navigation
- MediaStore for photo discovery
- SQLite for persisting triage state
- DocumentFile (SAF) for USB export

## Permissions

| Permission | Why |
|---|---|
| `READ_MEDIA_IMAGES` | Read photos from storage |
| `MANAGE_APP_ALL_FILES_ACCESS` | Delete photos directly from the filesystem |

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Unit tests
./gradlew test

# Lint
./gradlew lint
```

Requires Android Studio with SDK 35+ and a device or emulator running Android 13+.
