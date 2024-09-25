package com.example.aeroluggage.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AlertDialog
import com.example.aeroluggage.data.models.SyncData
import com.example.aeroluggage.data.models.Tag
import com.google.gson.Gson

class TagDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bagtag.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "allbagtags"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TAG = "bagtag"
        private const val COLUMN_ROOM = "room"
        private const val COLUMN_DATE_TIME = "dateTime"
        private const val COLUMN_ISSYNC = "issync"
        private const val COLUMN_USER_ID = "userID"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_TAG TEXT UNIQUE, 
                $COLUMN_ROOM TEXT, 
                $COLUMN_DATE_TIME TEXT, 
                $COLUMN_ISSYNC INTEGER DEFAULT 0, 
                $COLUMN_USER_ID TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_USER_ID TEXT")
        }
    }

    fun insertTag(context: Context, tag: Tag, onTagInserted: () -> Unit) {
        // Check if the tag already exists
        val existingTagCursor = readableDatabase.query(
            TABLE_NAME, null, "$COLUMN_TAG = ?", arrayOf(tag.bagtag),
            null, null, null
        )

        if (existingTagCursor.moveToFirst()) {
            // Show AlertDialog to ask user if they want to overwrite the tag
            AlertDialog.Builder(context)
                .setTitle("Tag Exists")
                .setMessage("This tag already exists. Do you want to overwrite it?")
                .setPositiveButton("Yes") { dialog, _ ->
                    val db = writableDatabase // Open the writable database inside the click listener
                    db.beginTransaction()
                    try {
                        val id = existingTagCursor.getInt(existingTagCursor.getColumnIndexOrThrow(COLUMN_ID))
                        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))

                        // Insert the new record
                        val values = ContentValues().apply {
                            put(COLUMN_TAG, tag.bagtag)
                            put(COLUMN_ROOM, tag.room)
                            put(COLUMN_DATE_TIME, tag.dateTime)
                            put(COLUMN_ISSYNC, 0) // Unsynced by default
                            put(COLUMN_USER_ID, tag.userID)
                        }
                        db.insert(TABLE_NAME, null, values)
                        db.setTransactionSuccessful()

                        //call the callback function to refresh the recyclerView
                        onTagInserted()
                    } finally {
                        db.endTransaction()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Do nothing, just dismiss the dialog
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    existingTagCursor.close()
                }
                .create()
                .show()
        } else {
            // If tag doesn't exist, insert the new record
            val db = writableDatabase // Open the writable database here
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(COLUMN_TAG, tag.bagtag)
                    put(COLUMN_ROOM, tag.room)
                    put(COLUMN_DATE_TIME, tag.dateTime)
                    put(COLUMN_ISSYNC, 0) // Unsynced by default
                    put(COLUMN_USER_ID, tag.userID)
                }
                db.insert(TABLE_NAME, null, values)
                db.setTransactionSuccessful()

                // Call the callback to refresh the RecyclerView
                onTagInserted()
            } finally {
                db.endTransaction()
                existingTagCursor.close()
            }
        }
    }

    // Get all unsynced tags
    fun getUnsyncedTags(room: String, dateTime: String): List<Tag> {
        val tagsList = mutableListOf<Tag>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME, null,
            "$COLUMN_ROOM = ? AND $COLUMN_DATE_TIME = ? AND $COLUMN_ISSYNC = ?",
//            arrayOf(arrayOf(room, dateTime, "0").toString()),
            arrayOf(room, dateTime, "0"),
            null, null, null
        )

        useCursor(cursor) {
            while (cursor.moveToNext()) {
                val tag = Tag(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    bagtag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG)),
                    room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM)),
                    dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)),
                    userID = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
                )
                tagsList.add(tag)
            }
        }

        db.close()
        return tagsList
    }

    // Fetch all tags for a given room
    fun getTagsByRoom(roomId: String): List<Tag> {
        val db = this.readableDatabase
        val tagsList = mutableListOf<Tag>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ROOM = ? AND $COLUMN_ISSYNC = ?", arrayOf(roomId, "0"))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val bagtag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
                val room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM))
                val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME))
                val userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))

                val tag = Tag(id, bagtag, room, dateTime, userId)
                tagsList.add(tag)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tagsList
    }

    // Get distinct room numbers
    fun getDistinctRooms(): List<String> {
        val roomList = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_ROOM FROM $TABLE_NAME", null)

        useCursor(cursor) {
            while (cursor.moveToNext()) {
                roomList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM)))
            }
        }

        db.close()
        return roomList
    }

    // Get tag count by room number
    fun getTagCountByRoom(roomNumber: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_ROOM = ? AND $COLUMN_ISSYNC = ?",
            arrayOf(roomNumber, "0")
        )

        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    // Mark a tag as synced
    fun markAsSynced(bagTag: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ISSYNC, 1)  // Set issync to 1
        }

        db.update(TABLE_NAME, values, "$COLUMN_TAG = ?", arrayOf(bagTag))
        db.close()
    }

    // Delete a tag by its ID
    fun deleteTag(tagId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(tagId.toString()))
        db.close()
    }



    // Convert a list of SyncData to JSON
    fun convertToJSON(syncDataList: List<SyncData>): String {
        return Gson().toJson(syncDataList)
    }

    // Utility function to handle cursor closing
    private inline fun useCursor(cursor: Cursor, block: () -> Unit) {
        cursor.use {
            block()
        }
    }
}
