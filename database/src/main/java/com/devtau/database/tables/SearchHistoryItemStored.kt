package com.devtau.database.tables

import com.devtau.rest.model.SearchHistoryItem
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteCreator
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType

@StorIOSQLiteType(table = SearchHistoryItemStored.TABLE_NAME)
data class SearchHistoryItemStored @StorIOSQLiteCreator constructor (
    @StorIOSQLiteColumn(name = "_id", key = true) var id: Long?,
    @StorIOSQLiteColumn(name = "query") var query: String
) {
    private fun convertToSearchHistoryItem(): SearchHistoryItem = SearchHistoryItem(id, query)


    companion object {
        const val TABLE_NAME = "SearchHistoryItems"

        fun convertListToSearchHistoryItems(list: List<SearchHistoryItemStored>): List<SearchHistoryItem> {
            val images = ArrayList<SearchHistoryItem>()
            for (next in list) images.add(next.convertToSearchHistoryItem())
            return images
        }

        fun convertListToQueryStrings(list: List<SearchHistoryItemStored>): List<String> {
            val strings = ArrayList<String>()
            for (next in list) strings.add(next.query)
            return strings
        }

        fun convertListToStored(list: List<SearchHistoryItem>): List<SearchHistoryItemStored> {
            val converted = ArrayList<SearchHistoryItemStored>()
            for (next in list) converted.add(convertFromSearchHistoryItem(next))
            return converted
        }

        fun convertFromSearchHistoryItem(item: SearchHistoryItem): SearchHistoryItemStored
                = SearchHistoryItemStored(item.id, item.query)
    }
}