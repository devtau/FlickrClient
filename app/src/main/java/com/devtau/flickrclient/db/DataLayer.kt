package com.devtau.flickrclient.db

import io.reactivex.disposables.Disposable
import com.devtau.flickrclient.rest.model.Image
import com.devtau.flickrclient.rest.model.SearchItem
import io.reactivex.functions.Consumer

interface DataLayer {

    fun saveImages(newList: List<Image>)
    fun saveSearchQuery(query: String)

    //возвращают подписку
    //returns subscription
    fun getImages(listener: Consumer<List<Image>>): Disposable?
    fun getSearchHistory(listener: Consumer<List<SearchItem>?>): Disposable?
    fun getSearchHistoryStrings(listener: Consumer<List<String>?>): Disposable?

    //возвращают результат и закрывают подключение к бд
    //returns result and closes db connection
    fun getLastSearched(listener: Consumer<String?>)
}