package com.devtau.database.tables

import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteCreator
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType

@StorIOSQLiteType(table = LastSearchQuery.TABLE_NAME)
data class LastSearchQuery @StorIOSQLiteCreator constructor(
    @StorIOSQLiteColumn(name = "_id", key = true) var id: Long,
    @StorIOSQLiteColumn(name = "query") var query: String?
) {

    companion object {
        const val TABLE_NAME = "LastSearchQuery"
    }
}