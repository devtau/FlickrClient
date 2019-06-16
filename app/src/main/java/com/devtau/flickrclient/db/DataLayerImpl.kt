package com.devtau.flickrclient.db

import android.content.Context
import android.text.TextUtils
import com.devtau.flickrclient.util.Logger
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.model.SearchItem
import com.devtau.flickrclient.util.Threading
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class DataLayerImpl(context: Context): DataLayer {

    private var db = FlickrDatabase.getInstance(context)


    override fun saveImages(newList: List<Image>) {
        Logger.d(LOG_TAG, "saveImages newList size=${newList.size}")
        var disposable: Disposable? = null
        disposable = getImages(Consumer { oldList ->
            compareImagesLists(newList, oldList)
            disposable?.dispose()
        })
    }

    override fun saveSearchQuery(query: String) {
        Logger.d(LOG_TAG, "saveSearchQuery query=$query")
        if (TextUtils.isEmpty(query)) return
        var disposable: Disposable? = null
        disposable = getSearchHistory(Consumer { oldList ->
            val searchItem = SearchItem(null, query, getLastUsed(oldList) + 1L)
            compareQueriesLists(searchItem, oldList)
            disposable?.dispose()
        })
    }

    override fun getImages(listener: Consumer<List<Image>>): Disposable? = db.imageDao().getList()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(listener::accept)
        { throwable -> Logger.e(LOG_TAG, "Error in getImages: " + throwable.message) }

    override fun getSearchHistory(listener: Consumer<List<SearchItem>?>): Disposable? = db.searchItemDao().getList()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(listener::accept)
        { throwable -> Logger.e(LOG_TAG, "Error in getSearchHistory: " + throwable.message) }

    override fun getSearchHistoryStrings(listener: Consumer<List<String>?>): Disposable? =
        getSearchHistory(Consumer { t -> listener.accept(SearchItem.convertListToQueryStrings(t)) })

    override fun getLastSearched(listener: Consumer<String?>) {
        var disposable: Disposable? = null
        disposable = db.searchItemDao().getLastSearched()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ searchItem ->
                listener.accept(searchItem.query)
                disposable?.dispose()
            })
            { throwable -> Logger.e(LOG_TAG, "Error in getLastSearched: " + throwable.message) }
    }


    private fun compareImagesLists(newList: List<Image>, oldList: List<Image>) { Threading.async( Callable {
        val toBeInserted = ArrayList<Image>()
        val toBeUpdated = ArrayList<Image>()
        val toBeDeleted = ArrayList<Image>()

        for (nextOld in oldList) {
            var foundInNewList = false
            for (nextNew in newList) {
                if (nextNew.id == nextOld.id) {
                    foundInNewList = true
                    if (!nextNew.deepEquals(nextOld)) toBeUpdated.add(nextNew)
                    break
                }
            }
            if (!foundInNewList) toBeDeleted.add(nextOld)
        }

        for (nextNew in newList) {
            var foundInOldList = false
            for (nextOld in oldList) {
                if (nextNew.id == nextOld.id) {
                    foundInOldList = true
                    break
                }
            }
            if (!foundInOldList) toBeInserted.add(nextNew)
        }
        Logger.d(LOG_TAG, "compareImagesLists. insert=${toBeInserted.size}, update=${toBeUpdated.size}, delete=${toBeDeleted.size}")
        if (toBeInserted.isNotEmpty()) db.imageDao().insert(*toBeInserted.toTypedArray()).subscribeDefault("compareImagesLists. inserted")
        if (toBeUpdated.isNotEmpty()) db.imageDao().insert(*toBeUpdated.toTypedArray()).subscribeDefault("compareImagesLists. updated")
        if (toBeDeleted.isNotEmpty()) db.imageDao().delete(*toBeDeleted.toTypedArray()).subscribeDefault("compareImagesLists. deleted")
    })}

    private fun Completable.subscribeDefault(msg: String?) {
        var disposable: Disposable? = null
        disposable = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            Logger.d(LOG_TAG, msg)
            disposable?.dispose()
        }
    }

    private fun compareQueriesLists(newItem: SearchItem, oldList: List<SearchItem>?) {
        var toBeUpdated: SearchItem? = null
        if (oldList != null) for (nextOld in oldList) {
            if (newItem.query.startsWith(nextOld.query)) {
                toBeUpdated = nextOld
                toBeUpdated.query = newItem.query
                toBeUpdated.lastUsed = newItem.lastUsed
                break
            }
        }
        Logger.d(LOG_TAG, "compareQueriesLists. newItem query=${newItem.query}")
        db.searchItemDao().insert(toBeUpdated ?: newItem).subscribeDefault("compareQueriesLists. inserted")
    }

    private fun getLastUsed(list: List<SearchItem>?): Long {
        var lastSearched: SearchItem? = null
        if (list != null) for (next in list) if (lastSearched?.lastUsed ?: 0L < next.lastUsed) lastSearched = next
        return lastSearched?.lastUsed ?: 0L
    }


    companion object {
        private const val LOG_TAG = "DataLayer"
    }
}