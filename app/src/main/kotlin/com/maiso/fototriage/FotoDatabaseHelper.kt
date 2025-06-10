package com.maiso.fototriage

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FotoTriage.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "FotoTriage"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FILENAME = "filename"
        private const val COLUMN_DATA_TAKEN_MILLIS = "data_taken_millis"
        private const val COLUMN_HASH = "hash"
        private const val COLUMN_FAVORITE = "favorite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_FILENAME TEXT, "
                + "$COLUMN_DATA_TAKEN_MILLIS INTEGER, "
                + "$COLUMN_HASH TEXT, "
                + "$COLUMN_FAVORITE INTEGER)") // Store favorite as INTEGER (0 or 1)
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(data: FotoDataBaseEntry) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILENAME, data.fileName)
            put(COLUMN_DATA_TAKEN_MILLIS, data.dateTakenMillis)
            put(COLUMN_HASH, data.hash)
            put(COLUMN_FAVORITE, if (data.favorite) 1 else 0) // Store boolean as INTEGER
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllData(): List<FotoDataBaseEntry> {
        val dataList = mutableListOf<FotoDataBaseEntry>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val filename = cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME))
                val dataTakenMillis =
                    cursor.getLong(cursor.getColumnIndex(COLUMN_DATA_TAKEN_MILLIS))
                val hash = cursor.getString(cursor.getColumnIndex(COLUMN_HASH))
                val favorite = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE)) == 1

                dataList.add(FotoDataBaseEntry(filename, dataTakenMillis, hash, favorite))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return dataList
    }

    fun isFileExists(filename: String, hash: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_HASH = ? AND $COLUMN_FILENAME LIKE ?"
        val cursor = db.rawQuery(query, arrayOf(hash, "%$filename%"))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    @SuppressLint("Range")
    fun checkHashesInDatabase(hashes: List<String>): Pair<List<String>, List<String>> {
        val db = this.readableDatabase
        val notInDatabase = mutableListOf<String>()
        val inDatabaseButNotInList = mutableListOf<String>()

        // Query to get all hashes from the database
        val cursor = db.rawQuery("SELECT $COLUMN_HASH FROM $TABLE_NAME", null)

        // Create a set of hashes from the provided list for quick lookup
        val hashSet = hashes.toSet()

        // Check each hash in the database
        if (cursor.moveToFirst()) {
            do {
                val dbHash = cursor.getString(cursor.getColumnIndex(COLUMN_HASH))
                if (dbHash !in hashSet) {
                    // If the hash is in the database but not in the provided list
                    inDatabaseButNotInList.add(dbHash)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        // Check which hashes from the provided list are not in the database
        for (hash in hashes) {
            if (!inDatabaseButNotInList.contains(hash)) {
                notInDatabase.add(hash)
            }
        }

        return Pair(notInDatabase, inDatabaseButNotInList)
    }

}

data class FotoDataBaseEntry(
    val fileName: String,
    val dateTakenMillis: Long,
    val hash: String,
    val favorite: Boolean,
)