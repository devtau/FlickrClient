package com.devtau.flickrclient.rest.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchItems")
data class SearchItem(
    @PrimaryKey @ColumnInfo(name = "_id") var id: Long?,
    var query: String,
    var lastUsed: Long
) {
    companion object {
        fun convertListToQueryStrings(list: List<SearchItem>?): List<String> {
            val strings = ArrayList<String>()
            if (list != null) for (next in list) strings.add(next.query)
            return strings
        }
    }
}