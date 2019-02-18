package com.devtau.database

import io.reactivex.disposables.Disposable
import com.devtau.rest.model.*
import com.devtau.database.listeners.*
/**
 * Прокси интерфейс для работы с слоем базы данных. Здесь все, что умеет модуль database
 */
interface DataSource {

    fun saveImages(newList: List<Image>)
    fun saveSearchQuery(query: String)

    //возвращают подписку
    fun getImages(listener: ImagesListener): Disposable
    fun getSearchHistory(listener: SearchHistoryListener): Disposable

    //возвращают результат и закрывают подключение к бд
    fun getLastSearchQuery(listener: LastSearchQueryListener)
}