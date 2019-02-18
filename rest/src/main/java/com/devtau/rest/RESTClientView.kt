package com.devtau.rest

import android.content.Context
import android.support.annotation.StringRes
import com.devtau.rest.model.Image
/**
 * Этот интерфейс должен реализовать каждый клиент сетевого слоя для работы с API.
 * Обобщает преимущественно методы обратного вызова, вызываемые после успешного ответа сервера
 */
interface RESTClientView {
    fun getLogTag(): String
    fun getContext(): Context
    fun getApiKey(): String

    fun showToast(msg: String)
    fun showToast(@StringRes msgId: Int)
    fun showDialog(msg: String)
    fun showDialog(@StringRes msgId: Int)

    fun processToken(callbackConfirmed: Boolean?, tempToken: String?, tempTokenSecret: String?)
    fun showWebPage(page: String?)
    fun processAccessToken(fullName: String?, receivedToken: String?, receivedTokenSecret: String?, userNsid: String?, username: String?)
    fun processSearchResult(images: List<Image>?)
}