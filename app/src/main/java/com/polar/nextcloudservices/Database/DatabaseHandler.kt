package com.polar.nextcloudservices.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.DATABASE_NAME
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.DATABASE_VERSION
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.NOTIFICATIONS_TABLE
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.NOTIFICATION_CONTENT
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.NOTIFICATION_ID
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.NOTIFICATION_NOTIFICATION_ID
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.NOTIFICATION_TIMESTAMP
import com.polar.nextcloudservices.Database.DatabaseInfo.Companion.SQL_CREATE_ENTRIES
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHandler (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private val NOTIFICATION_PROJECTION = arrayOf(
            NOTIFICATION_ID,
            NOTIFICATION_NOTIFICATION_ID,
            NOTIFICATION_TIMESTAMP,
            NOTIFICATION_CONTENT
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        /*if(oldVersion<X){
            db.execSQL("SQL_NEW_TABLE")
        }*/
    }

    fun getAllNotifications(): ArrayList<NotificationDatabaseentry> {
        val selection = ""
        val selectionArgs = arrayOf<String>()
        return getAllNotifications(selection, selectionArgs)
    }

    fun getAllNotifications(selection: String, selectionArgs: Array<String>): ArrayList<NotificationDatabaseentry> {
        val db = readableDatabase
        val sortOrder = NOTIFICATION_TIMESTAMP + " DESC"
        val cursor = db.query(
            NOTIFICATIONS_TABLE,
            NOTIFICATION_PROJECTION,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        val results = arrayListOf<NotificationDatabaseentry>()
        while (cursor.moveToNext()) {
            val entry = NotificationDatabaseentry(
                cursor.getLong(0),
                cursor.getString(3),
                cursor.getLong(2),
                cursor.getLong(1),
            )
            Log.e("DB", "NID: "+entry.notificationid)
            Log.e("DB", "CONT: "+entry.content)
            results.add(entry)
        }
        cursor.close()

        db.close()
        return results
    }

    /**
     * Creates a Notification entry
     * @param content Json content of notification
     * @return NotificationDatabaseentry
     */
    fun createNotificationEntry(content: String): NotificationDatabaseentry? {

        val currentTime: Date = Calendar.getInstance().time
        val notificationId = JSONObject(content).getLong("notification_id")

        Log.e("DB", "add")
        if(checkIfNotificationPresent(notificationId)){
            return null
        }

        // Create a new map of values, where column names are the keys
        val values = ContentValues()
        values.put(NOTIFICATION_NOTIFICATION_ID, notificationId)
        values.put(NOTIFICATION_TIMESTAMP, currentTime.time)
        values.put(NOTIFICATION_CONTENT, content)

        val db = writableDatabase
        // Insert the new row, returning the primary key value of the new row
        val newRowId = db.insert(NOTIFICATIONS_TABLE, null, values)
        db.close()
        Log.e("DB", "add $newRowId")
        return NotificationDatabaseentry(newRowId, content, currentTime.time, notificationId)

    }

    fun checkIfNotificationPresent(id: Long): Boolean {

        val selection = "$NOTIFICATION_NOTIFICATION_ID LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        var size = getAllNotifications(selection, selectionArgs).size

        Log.e("DB", "Check existing database $size")
        if(size == 0){
            return false
        }
        return true
    }

}