package com.devtau.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import io.reactivex.schedulers.Schedulers
import com.devtau.database.tables.*
import com.devtau.database.util.Logger

class SQLHelper private constructor(
    context: Context,
    dbName: String
): SQLiteOpenHelper(context, dbName, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        updateMyDatabase(db, 0, DB_VERSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Logger.d(LOG_TAG, "Found new DB version. About to update to: " + DB_VERSION.toString())
        updateMyDatabase(db, oldVersion, newVersion)
    }

    private fun updateMyDatabase(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.beginTransaction()
        try {
            ImageStoredTable.createTable(db)
            SearchHistoryItemStoredTable.createTable(db)
            LastSearchQueryTable.createTable(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun dropDB(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS " + ImageStoredTable.NAME)
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryItemStoredTable.NAME)
        db.execSQL("DROP TABLE IF EXISTS " + LastSearchQueryTable.NAME)
    }


    companion object {
        private const val DB_VERSION = 1
        private const val LOG_TAG = "SQLHelper"

        private var storioInstance: StorIOSQLite? = null

        @Synchronized
        fun getInstance(context: Context, dbName: String): SQLHelper = SQLHelper(context, dbName)

        @Synchronized
        fun getStorio(SQLHelper: SQLHelper): StorIOSQLite {
            if (storioInstance == null) {
                storioInstance = DefaultStorIOSQLite.builder()
                    .sqliteOpenHelper(SQLHelper)
                    .defaultRxScheduler(Schedulers.io())
                    .addTypeMapping(ImageStored::class.java, ImageStoredSQLiteTypeMapping())
                    .addTypeMapping(SearchHistoryItemStored::class.java, SearchHistoryItemStoredSQLiteTypeMapping())
                    .addTypeMapping(LastSearchQuery::class.java, LastSearchQuerySQLiteTypeMapping())
                    .build()
            }
            return storioInstance!!
        }
    }
}