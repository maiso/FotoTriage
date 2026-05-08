# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run lint
./gradlew lint

# Run a single unit test class
./gradlew test --tests "com.maiso.fototriage.ExampleUnitTest"
```

## Architecture

FotoTriage is a single-module Android app (Kotlin, Jetpack Compose) for triaging photos on-device.

### Navigation

Uses **androidx.navigation3** (`NavDisplay`) with a manually managed `backStack: SnapshotStateList<Any>`. Routes are defined as `sealed interface Dest` in `MainActivity.kt`. Each screen entry is registered via `entryProvider { entry<Dest.X> { ... } }`. Navigation happens by pushing/removing items on the backstack directly.

### State Management

Each screen has a corresponding `ViewModel` with a `uiState: StateFlow<XUiState>`. ViewModels read from `PhotoDatabase.photos` (a global `StateFlow<List<Photo>>`) and derive their UI state via `combine`/`onEach`. ViewModelFactory pattern is used for ViewModels requiring constructor parameters.

### Data Layer

`PhotoDatabase` is a global `object` (singleton) serving as the single source of truth for photo state:
- Loads photos from Android's **MediaStore** (`Images.Media.EXTERNAL_CONTENT_URI`), scoped to `/DCIM/Camera/`
- Triage/favorite state is persisted in a **SQLite database** (`FotoTriage.db`) stored *inside the DCIM/Camera folder* on external storage (co-located with photos), managed by `DatabaseHelper`
- The DB stores: `filename`, `dateTakenMillis`, `triaged` (bool), `favorite` (bool)
- On startup, every photo from MediaStore is either added to the DB (new, untriaged) or its existing state is retrieved

### Photo States

A photo can be in one of three mutually exclusive states:
- **Untriaged**: `!triaged && !favorite`
- **Triaged**: `triaged && !favorite`
- **Favorite**: `favorite` (triaged status irrelevant)

Marking a photo as triaged also clears its favorite flag (`unfavorite = true`).

### Screens

| Screen | File | Purpose |
|--------|------|---------|
| Loading | `screens/loading/` | Progress indicator while MediaStore is scanned |
| Overview | `screens/overview/` | Year/month list with counts; green = fully triaged; toggle to hide completed months |
| PhotoTriage | `screens/phototriage/` | Per-month photo review; swipe through, mark triaged/favorite/delete |
| TriageFinished | `screens/phototriage/triagefinished/` | Summary shown when all photos in a month are triaged |
| FavoriteOverview | `screens/favoriteoverview/` | Grid of all favorites for a selected year |
| Export | `export/` | Progress screen while favorites are copied to USB via `USBFileCopier` |

### USB Export

`USBFileCopier` copies favorite photos to a USB drive using `DocumentFile` (SAF). The user picks a directory via `ACTION_OPEN_DOCUMENT_TREE`; files are written into a `FotoFavorites<year>` subfolder. Progress is exposed as `StateFlow<CopyProgress>`.

### Permissions

The app requires `READ_MEDIA_IMAGES` and `MANAGE_APP_ALL_FILES_ACCESS` (for deleting files directly via `File.delete()`). The latter is requested via `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`.

### Notifications

A monthly `AlarmManager` notification reminds the user to triage photos. Scheduled on first launch via `scheduleMonthlyNotification()`, received by `NotificationReceiver`.
