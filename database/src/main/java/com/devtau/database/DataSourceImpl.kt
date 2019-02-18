package com.devtau.database

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.devtau.database.listeners.LastSearchQueryListener
import com.devtau.database.listeners.ImagesListener
import com.devtau.database.listeners.SearchHistoryListener
import com.devtau.database.tables.LastSearchQuery
import com.devtau.database.tables.ImageStored
import com.devtau.database.tables.SearchHistoryItemStored
import com.devtau.database.util.Logger
import com.devtau.rest.model.Image
import com.devtau.rest.model.SearchHistoryItem
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

@SuppressLint("CheckResult")
class DataSourceImpl(context: Context, dbName: String): DataSource {

    private var sqlHelper = SQLHelper.getInstance(context, dbName)
    private var storio = SQLHelper.getStorio(sqlHelper)


    override fun saveImages(newList: List<Image>) {
        Logger.d(LOG_TAG, "saveImages newList size=" + newList.size)
        storio.get()
            .listOfObjects(ImageStored::class.java)
            .withQuery(Query.builder().table(ImageStored.TABLE_NAME).build())
            .prepare()
            .asRxSingle()
            .subscribe({ oldList -> compareImagesLists(ImageStored.convertListToStored(newList), oldList) },
                { throwable -> Logger.e(LOG_TAG, "Error in saveImages: " + throwable.message) })
    }

    override fun saveSearchQuery(query: String) {
        Logger.d(LOG_TAG, "saveSearchQuery query=$query")
        if (TextUtils.isEmpty(query)) return
        val searchHistoryItem = SearchHistoryItem(null, query)
        storio.get()
            .listOfObjects(SearchHistoryItemStored::class.java)
            .withQuery(Query.builder().table(SearchHistoryItemStored.TABLE_NAME).build())
            .prepare()
            .asRxSingle()
            .subscribe({ oldList -> compareQueriesLists(SearchHistoryItemStored.convertFromSearchHistoryItem(searchHistoryItem), oldList) },
                { throwable -> Logger.e(LOG_TAG, "Error in saveSearchQuery: " + throwable.message) })
        storio.put().`object`(LastSearchQuery(1, query)).prepare().asRxSingle().subscribe()
    }

    override fun getImages(listener: ImagesListener): Disposable = storio.get()
        .listOfObjects(ImageStored::class.java)
        .withQuery(Query.builder().table(ImageStored.TABLE_NAME).build())
        .prepare()
        .asRxFlowable(BackpressureStrategy.LATEST)
        .map { result -> ImageStored.convertListToImages(result) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(listener::processImages)
        { throwable -> Logger.e(LOG_TAG, "Error in getImages: " + throwable.message) }

    override fun getSearchHistory(listener: SearchHistoryListener): Disposable = storio.get()
        .listOfObjects(SearchHistoryItemStored::class.java)
        .withQuery(Query.builder().table(SearchHistoryItemStored.TABLE_NAME).build())
        .prepare()
        .asRxFlowable(BackpressureStrategy.LATEST)
        .map { result -> SearchHistoryItemStored.convertListToQueryStrings(result) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(listener::processQueries)
        { throwable -> Logger.e(LOG_TAG, "Error in getSearchHistory: " + throwable.message) }

    override fun getLastSearchQuery(listener: LastSearchQueryListener) {
        storio.get()
            .`object`(LastSearchQuery::class.java)
            .withQuery(Query.builder().table(LastSearchQuery.TABLE_NAME).where("_id = 1").build())
            .prepare()
            .asRxSingle()
            .map { result -> result.orNull()?.query }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listener::processQuery)
            { throwable -> Logger.e(LOG_TAG, "Error in getLastSearchQuery: " + throwable.message) }
    }


    private fun compareImagesLists(newList: List<ImageStored>, oldList: List<ImageStored>) {
        val toBeDeleted = ArrayList<ImageStored>()
        for (nextOld in oldList) {
            var foundInNewList = false
            for (nextNew in newList) {
                if (nextNew.id == nextOld.id) {
                    foundInNewList = true
                    break
                }
            }
            if (!foundInNewList) toBeDeleted.add(nextOld)
        }
        Logger.d(LOG_TAG, "compareImagesLists. going to put newList to db. size=" + newList.size)
        storio.put().objects(newList).prepare().asRxSingle().subscribe()
        if (toBeDeleted.isNotEmpty()) storio.delete().objects(toBeDeleted).prepare().asRxSingle().subscribe()
    }

    private fun compareQueriesLists(newItem: SearchHistoryItemStored, oldList: List<SearchHistoryItemStored>) {
        var foundInOldList = false
        var toBeUpdated: SearchHistoryItemStored? = null
        for (nextOld in oldList) {
            if (TextUtils.equals(newItem.query, nextOld.query)) {
                foundInOldList = true
                break
            }
            if (newItem.query.startsWith(nextOld.query)) {
                toBeUpdated = nextOld
                toBeUpdated.query = newItem.query
                break
            }
        }
        Logger.d(LOG_TAG, "compareQueriesLists. newItem query=" + newItem.query + " foundInOldList=$foundInOldList")
        if (!foundInOldList) storio.put().`object`(toBeUpdated ?: newItem).prepare().asRxSingle().subscribe()
    }


    companion object {
        private const val LOG_TAG = "DataSourceImpl"
    }
}