package com.example.aeroluggage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import retrofit2.Call

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

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY, 
                $COLUMN_TAG TEXT, 
                $COLUMN_ROOM TEXT, 
                $COLUMN_DATE_TIME TEXT, 
                $COLUMN_ISSYNC INTEGER DEFAULT 0,  -- Use INTEGER to represent BOOLEAN
                $COLUMN_USER_ID TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN userID TEXT")
        }
    }

    // To add data to the database
    fun insertTag(tag: Tag) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TAG, tag.bagtag)
            put(COLUMN_ROOM, tag.room)
            put(COLUMN_DATE_TIME, tag.dateTime)
            put(COLUMN_ISSYNC, 0) // Ensure isSync is set to 0 (unsynced)
            put(COLUMN_USER_ID, tag.userID)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // To read data from the database
    fun getAllTags(): List<Tag> {
        val tagsList = mutableListOf<Tag>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        // Using a while loop to retrieve data
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val bagtag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
            val room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM))
            val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME))
            val userID = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))

            // Log data
            Log.d("TAG_QUERY_RESULT", "ID: $id")
            Log.d("TAG_QUERY_RESULT", "BagTag: $bagtag")
            Log.d("TAG_QUERY_RESULT", "Room: $room")
            Log.d("TAG_QUERY_RESULT", "DateTime: $dateTime")
            Log.d("TAG_QUERY_RESULT", "UserID: $userID")

            // When all the data are retrieved, pass them as an argument and store it in a tag variable and add it into the tagsList
            val tag = Tag(id, bagtag, room, dateTime, userID)
            tagsList.add(tag)
        }
        cursor.close()
        db.close()
        return tagsList // This tagsList acts as a list which consists of all the data retrieved from the db
    }

    // Delete function
    fun deleteTag(tagId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(tagId.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

    // Function to mark a tag as synced in the local database
    fun markAsSynced(bagTag: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ISSYNC, 1)  // Set issync to 1, meaning the tag is synced
        }
        val whereClause = "$COLUMN_TAG = ?"
        val whereArgs = arrayOf(bagTag)

        db.update(TABLE_NAME, values, whereClause, whereArgs)
        db.close()
    }

    //to get unsynced tags
    fun getUnsyncedTags(): List<SyncData> {
        val syncDataList = mutableListOf<SyncData>()
        val db = readableDatabase
        //val query = "SELECT * FROM $TABLE_NAME"
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ISSYNC = 0"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, null)

            if (cursor == null) {
                Log.d("DB_DEBUG", "Cursor is null. Query might have failed.")
                return syncDataList
            }

            Log.d("DB_DEBUG", "Cursor count: ${cursor.count}")

            if (cursor.count == 0) {
                Log.d("DB_DEBUG", "No unsynced tags found.")
            } else {
                while (cursor.moveToNext()) {
                    val bagtag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
                    val room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM))
                    val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME))
                    val userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))

                    val storageRoom = StorageRoom(RoomId = room)
                    val syncData = SyncData(
                        AddedDate = dateTime,
                        AddedUser = userId,
                        BagTag = bagtag,
                        StorageRoom = storageRoom,
                    )
                    syncDataList.add(syncData)
                }
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error retrieving unsynced tags: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }
        return syncDataList
    }

    fun convertToJSON(syncDataList: List<SyncData>): String {
        val gson = Gson()
        return gson.toJson(syncDataList)
    }

    // Method to get distinct room numbers
    fun getDistinctRooms(): List<String> {
        val db = readableDatabase
        val query = "SELECT DISTINCT $COLUMN_ROOM FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        val roomList = mutableListOf<String>()

        while (cursor.moveToNext()) {
            roomList.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM)))
        }

        cursor.close()
        db.close()
        return roomList
    }

    // Method to get tag count by room number
    fun getTagCountByRoom(roomNumber: String): Int {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_ROOM = ?"
        val cursor = db.rawQuery(query, arrayOf(roomNumber))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    //to retrieve tags based on a specific room number
    fun getTagsByRoom(room: String): List<Tag> {
        val tagsList = mutableListOf<Tag>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ROOM = ?"
        val cursor = db.rawQuery(query, arrayOf(room))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val bagtag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG))
            val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME))
            val tag = Tag(id, bagtag, room, dateTime, userID = "")
            tagsList.add(tag)
        }

        cursor.close()
        db.close()
        return tagsList
    }

    fun saveUserId(userId: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

}
