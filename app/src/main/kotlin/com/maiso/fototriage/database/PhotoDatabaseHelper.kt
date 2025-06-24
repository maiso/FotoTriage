package com.maiso.fototriage.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FotoTriage.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "FotoTriage"
        private const val COLUMN_FILENAME = "filename"
        private const val COLUMN_DATA_TAKEN_MILLIS = "data_taken_millis"
        private const val COLUMN_TRIAGED = "triaged"
        private const val COLUMN_FAVORITE = "favorite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_FILENAME TEXT PRIMARY KEY, "
                + "$COLUMN_DATA_TAKEN_MILLIS INTEGER, "
                + "$COLUMN_TRIAGED INTEGER,"
                + "$COLUMN_FAVORITE INTEGER)") // Store favorite as INTEGER (0 or 1)
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("onUpgrade(old=$oldVersion, new=$newVersion) not defined")
    }

    fun insertData(data: PhotoDataBaseEntry) {
        val db = this.writableDatabase
        Log.d("FotoTriage", "Insert $data in database.")

        val values = ContentValues().apply {
            put(COLUMN_FILENAME, data.fileName)
            put(COLUMN_DATA_TAKEN_MILLIS, data.dateTakenMillis)
            put(COLUMN_TRIAGED, if (data.triaged) 1 else 0) // Store boolean as INTEGER
            put(COLUMN_FAVORITE, if (data.favorite) 1 else 0) // Store boolean as INTEGER
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

    @SuppressLint("Range")
    fun addOrRetrieveEntry(filename: String, dateTakenMillis: Long): Pair<Boolean, Boolean> {
        val db = this.readableDatabase
        val query =
            "SELECT $COLUMN_TRIAGED, $COLUMN_FAVORITE FROM $TABLE_NAME WHERE $COLUMN_FILENAME LIKE ?"
        val cursor = db.rawQuery(query, arrayOf("%$filename%"))

        val exists = cursor.count > 0
        var status: Pair<Boolean, Boolean>? = null

        if (exists) {
            Log.d("FotoTriage", "$filename exists in database, retrieving.")
            if (cursor.moveToFirst()) {
                val triaged = cursor.getInt(cursor.getColumnIndex(COLUMN_TRIAGED)) == 1
                val favorite = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE)) == 1
                status = triaged to favorite
                Log.d("FotoTriage", "$filename: Status $status")
            } else {
                Log.e("FotoTriage", "Cannot move cursur to first")
            }
        } else {
            Log.d("FotoTriage", "$filename not found in database, inserting")
            insertData(
                PhotoDataBaseEntry(
                    filename, dateTakenMillis, triaged = false, favorite = false
                )
            )
            status = false to false
        }
        cursor.close()
        db.close()
        return status!!
    }

    @SuppressLint("Range")
    fun cleanUpDatabase(fileList: List<String>) {
        val db = this.writableDatabase

        // Step 1: Retrieve all filenames from the database
        val query = "SELECT $COLUMN_FILENAME FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        // Step 2: Create a set of filenames from the database
        val dbFilenames = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            val filename = cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME))
            dbFilenames.add(filename)
        }
        cursor.close()

        // Step 3: Identify filenames to remove
        val filesToRemove = dbFilenames.filter { !fileList.contains(it) }

        Log.w("FotoTriage", "filesToRemove: $filesToRemove")
        // Step 4: Remove entries from the database
        for (filename in filesToRemove) {
            db.delete(TABLE_NAME, "$COLUMN_FILENAME = ?", arrayOf(filename))
        }

        db.close()
    }

}

data class PhotoDataBaseEntry(
    val fileName: String,
    val dateTakenMillis: Long,
    val triaged: Boolean,
    val favorite: Boolean,
)