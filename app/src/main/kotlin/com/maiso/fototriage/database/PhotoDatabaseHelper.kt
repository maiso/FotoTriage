package com.maiso.fototriage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File

class DatabaseHelper(context: Context, folderPath: String, private val year: Int) :
    SQLiteOpenHelper(context, "FotoTriage_$year.db", null, DATABASE_VERSION) {

    private val databasePath: String = folderPath
    private val databaseName: String = "FotoTriage_$year.db"

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_FILENAME TEXT PRIMARY KEY, "
                + "$COLUMN_DATA_TAKEN_MILLIS INTEGER, "
                + "$COLUMN_TRIAGED INTEGER,"
                + "$COLUMN_FAVORITE INTEGER)")
        Log.d("FotoTriage", "DatabaseHelper.onCreate()")
        db.execSQL(createTable)
        db.execSQL("CREATE TABLE $METADATA_TABLE (key TEXT PRIMARY KEY, value INTEGER NOT NULL DEFAULT 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("onUpgrade(old=$oldVersion, new=$newVersion) not defined")
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        val dbFile = File(databasePath, databaseName)
        val db = SQLiteDatabase.openOrCreateDatabase(dbFile.path, null)
        db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", arrayOf(TABLE_NAME))
            .use { if (it.count == 0) onCreate(db) }
        db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", arrayOf(METADATA_TABLE))
            .use { if (it.count == 0) db.execSQL("CREATE TABLE $METADATA_TABLE (key TEXT PRIMARY KEY, value INTEGER NOT NULL DEFAULT 0)") }
        return db
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        val dbFile = File(databasePath, databaseName)
        getWritableDatabase().close()
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun insertData(data: PhotoDataBaseEntry) {
        val db = this.writableDatabase
        Log.d("FotoTriage", "Insert $data in database.")

        val values = ContentValues().apply {
            put(COLUMN_FILENAME, data.fileName)
            put(COLUMN_DATA_TAKEN_MILLIS, data.dateTakenMillis)
            put(COLUMN_TRIAGED, if (data.triaged) 1 else 0)
            put(COLUMN_FAVORITE, if (data.favorite) 1 else 0)
        }
        val result =
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        Log.d("FotoTriage", "insertData result: $result ")
        db.close()
    }

    @SuppressLint("Range")
    fun getAllData(): List<PhotoDataBaseEntry> {
        val dataList = mutableListOf<PhotoDataBaseEntry>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val filename = cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME))
                val dataTakenMillis =
                    cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TAKEN_MILLIS))
                val triaged = cursor.getInt(cursor.getColumnIndex(COLUMN_TRIAGED)) == 1
                val favorite = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE)) == 1

                dataList.add(PhotoDataBaseEntry(filename, dataTakenMillis, triaged, favorite))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return dataList
    }

    fun loadAllAsMap(): Map<String, Pair<Boolean, Boolean>> {
        val map = mutableMapOf<String, Pair<Boolean, Boolean>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_FILENAME, $COLUMN_TRIAGED, $COLUMN_FAVORITE FROM $TABLE_NAME",
            null
        )
        while (cursor.moveToNext()) {
            map[cursor.getString(0)] = (cursor.getInt(1) == 1) to (cursor.getInt(2) == 1)
        }
        cursor.close()
        db.close()
        return map
    }

    fun insertBatch(entries: List<PhotoDataBaseEntry>) {
        if (entries.isEmpty()) return
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (entry in entries) {
                val values = ContentValues().apply {
                    put(COLUMN_FILENAME, entry.fileName)
                    put(COLUMN_DATA_TAKEN_MILLIS, entry.dateTakenMillis)
                    put(COLUMN_TRIAGED, if (entry.triaged) 1 else 0)
                    put(COLUMN_FAVORITE, if (entry.favorite) 1 else 0)
                }
                db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    @SuppressLint("Range")
    fun cleanUpDatabase(fileList: List<String>) {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT $COLUMN_FILENAME FROM $TABLE_NAME", null)
        val dbFilenames = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            dbFilenames.add(cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME)))
        }
        cursor.close()

        val filesToRemove = dbFilenames.filter { !fileList.contains(it) }
        Log.w("FotoTriage", "filesToRemove: $filesToRemove")
        for (filename in filesToRemove) {
            db.delete(TABLE_NAME, "$COLUMN_FILENAME = ?", arrayOf(filename))
        }

        db.close()
    }

    fun incrementDeletedCount(month: Int) {
        val key = "deleted_%02d".format(month)
        val db = writableDatabase
        db.execSQL(
            "INSERT INTO $METADATA_TABLE (key, value) VALUES (?, 1) ON CONFLICT(key) DO UPDATE SET value = value + 1",
            arrayOf(key)
        )
        db.close()
    }

    fun getDeletedCounts(): Map<Int, Int> {
        val db = readableDatabase
        val map = mutableMapOf<Int, Int>()
        db.rawQuery("SELECT key, value FROM $METADATA_TABLE WHERE key LIKE 'deleted_%'", null).use { cursor ->
            while (cursor.moveToNext()) {
                val month = cursor.getString(0).removePrefix("deleted_").toIntOrNull() ?: continue
                map[month] = cursor.getInt(1)
            }
        }
        db.close()
        return map
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "FotoTriage"
        private const val METADATA_TABLE = "metadata"
        private const val COLUMN_FILENAME = "filename"
        private const val COLUMN_DATA_TAKEN_MILLIS = "data_taken_millis"
        private const val COLUMN_TRIAGED = "triaged"
        private const val COLUMN_FAVORITE = "favorite"
    }
}

data class PhotoDataBaseEntry(
    val fileName: String,
    val dateTakenMillis: Long,
    val triaged: Boolean,
    val favorite: Boolean,
)
