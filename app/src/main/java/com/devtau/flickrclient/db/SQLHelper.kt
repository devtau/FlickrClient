package com.devtau.flickrclient.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLHelper private constructor(context: Context, dbName: String):
    SQLiteOpenHelper(context, dbName, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}


    companion object {
        private const val DB_VERSION = 1

        @Synchronized
        fun getInstance(context: Context, dbName: String): SQLHelper = SQLHelper(context, dbName)
    }
}