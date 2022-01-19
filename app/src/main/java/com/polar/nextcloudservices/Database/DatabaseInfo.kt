package com.polar.nextcloudservices.Database

class DatabaseInfo {
    companion object {

        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ncservices.db"

        val NOTIFICATIONS_TABLE = "notifications"
        val NOTIFICATION_ID = "id"
        val NOTIFICATION_NOTIFICATION_ID = "notification_id"
        val NOTIFICATION_CONTENT = "content"
        val NOTIFICATION_TIMESTAMP = "timestamp"

        val SQL_CREATE_ENTRIES =
            "CREATE TABLE $NOTIFICATIONS_TABLE (" +
                    "$NOTIFICATION_ID INTEGER PRIMARY KEY," +
                    "$NOTIFICATION_NOTIFICATION_ID INTEGER," +
                    "$NOTIFICATION_TIMESTAMP LONG," +
                    "$NOTIFICATION_CONTENT TEXT)"
    }
}